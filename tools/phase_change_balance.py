#!/usr/bin/env python3
"""相变控制炉数值模型（纯标准库，无第三方依赖）。

用途：扫描结构规模 / 热容 / 发热上限 / 冷却剂参数，输出：

1. 每个尺寸的平衡温度、通量、冷却剂与燃料消耗表（stdout，供写入设计文档）；
2. 三张 SVG 图（docs/figures/），可直接在浏览器或 Markdown 中查看；
3. 参数扫描矩阵，用于挑默认值（哪一档尺寸能进高温带）。

模型与常量来自 `docs/kubejs_mekanism_notes.md` 第 6 节（裂变堆实测公式）与
`docs/multiblock_design.md` §1（相变控制炉设计决策 1-10）。

核心公式：
    外壳块数 shell(n) = n^3 - (n-2)^3        （n 为边长，3..18）
    内部核心 cores(n) = (n-2)^3              （玩家把内部填满时的上限）
    热容     C  = casingHeatCapacity * shell(n)
    发热上限 Qmax = baseHeatPerShell * shell(n) + maxHeatPerCore * cores(n)
    平衡温度 T* = T_boil + Q / (k * C)       （水 k=0.5，钠 k=1.35）
    通量     F  = Q / heatPerMaterialMb      （mB/tick 材料）
    锭速率    = F / mbPerItem                （1 锭 = 90 mB）
    冷却剂流量：水 = Q / 50 mB/t，钠 = Q / 1 mB/t（每 mB 带热 50 / 1）
    燃料消耗：Q / fuelHeatPerItem 个/t
"""

from __future__ import annotations

import os
import math

# ---------------------------------------------------------------- 模型参数

T_BOIL = 373.15          # K，水/钠的沸点基准（裂变堆 BASE_BOIL_TEMP 实测值）
T_MID = 773.0            # K，用户设定的温度分界（500 °C）
T_LIMIT = 1500.0         # K，强制停堆温度
MB_PER_ITEM = 90.0       # mB/锭，与匠魂一致

# 水冷沿用裂变堆实测值 0.5；钠冷取 1.35（2026-09-27 决定：让大机器满功率的钠冷能逼近
# 773 K 分界，使"钠供应不足"自然变成温度惩罚，而不是像 5.0 那样把温度钉死在 475 K）
HEAT_PER_MB_COOLANT = {"水冷": 50.0, "钠冷": 1.0}  # 每 mB 冷却剂带走的热


def k_of(coolant: str, params: dict) -> float:
    """冷却剂散热斜率 k。"""
    return params["k_water"] if coolant == "水冷" else params["k_sodium"]


DEFAULTS = {
    "casing_heat_capacity": 50.0,     # 每个外壳方块提供的热容
    "base_heat_per_shell": 2000.0,    # A 项：每个外壳方块的发热上限
    "max_heat_per_core": 10000.0,     # B 项：每个内部核心的发热上限
    "heat_per_material_mb": 500000.0,  # 每 mB 材料需要的热量（反应速率换算系数）
    # 默认让"1 个燃料物品 ≈ 1 锭的处理量"：heat_per_material_mb * 90 = 45M
    "fuel_heat_per_item": 45_000_000.0,
    "k_water": 0.5,                   # 水冷散热斜率（裂变堆实测 0.5）
    "k_sodium": 1.35,                 # 钠冷散热斜率（2026-09-27 决定）
}

SIZES = [3, 5, 7, 9, 12, 15, 18]


def shell_blocks(n: int) -> int:
    """边长 n 的空心立方体外壳块数。"""
    return n ** 3 - max(n - 2, 0) ** 3


def core_blocks(n: int) -> int:
    """内部可放核心的块数（填满时）。"""
    return max(n - 2, 0) ** 3


def heat_capacity(n: int, casing_heat_capacity: float) -> float:
    return casing_heat_capacity * shell_blocks(n)


def max_heat(n: int, base_heat_per_shell: float, max_heat_per_core: float) -> float:
    return base_heat_per_shell * shell_blocks(n) + max_heat_per_core * core_blocks(n)


def temperature(q_heat: float, k: float, c_heat: float) -> float:
    """平衡温度（K）。"""
    return T_BOIL + q_heat / (k * c_heat)


def heat_for_temperature(target: float, k: float, c_heat: float) -> float:
    return (target - T_BOIL) * k * c_heat


def throughput_items_per_tick(q_heat: float, params: dict) -> float:
    return q_heat / params["heat_per_material_mb"] / MB_PER_ITEM


def coolant_mb_per_tick(q_heat: float, coolant: str) -> float:
    return q_heat / HEAT_PER_MB_COOLANT[coolant]


def fuel_items_per_tick(q_heat: float, params: dict) -> float:
    return q_heat / params["fuel_heat_per_item"]


def fuel_per_item(params: dict) -> float:
    """处理 1 锭（90 mB）需要消耗的燃料物品数。"""
    return params["heat_per_material_mb"] * MB_PER_ITEM / params["fuel_heat_per_item"]


# ---------------------------------------------------------------- 文本报表


def print_size_table(params: dict, sizes=SIZES) -> None:
    print("=== 表 A：各尺寸的温度与通量能力 ===")
    print(f"参数: {params}")
    print(f"1 锭 = {MB_PER_ITEM:.0f} mB；处理 1 锭需燃料 {fuel_per_item(params):.2f} 个（按当前参数）")
    header = ("边长 外壳 核心       热容C      发热上限Qmax  水冷Tmax  水冷低档上限  水冷高档区间   "
              "钠冷Tmax  钠冷低档上限  钠/水低档倍数")
    print(header)
    print("-" * len(header))
    for n in sizes:
        c_heat = heat_capacity(n, params["casing_heat_capacity"])
        q_max = max_heat(n, params["base_heat_per_shell"], params["max_heat_per_core"])
        row = [f"{n:>4}", f"{shell_blocks(n):>4}", f"{core_blocks(n):>4}",
               f"{c_heat:>11.0f}", f"{q_max:>14.0f}"]
        # 水冷
        q_mid_water = heat_for_temperature(T_MID, k_of("水冷", params), c_heat)
        f_max_water = throughput_items_per_tick(q_max, params)
        row.append(f"{temperature(q_max, k_of('水冷', params), c_heat):>9.1f}")
        if q_mid_water >= q_max:
            row.append(f"{f_max_water:>13.3f}")     # 全程都在低档
            row.append(f"{'不可达':>13}")
        else:
            row.append(f"{throughput_items_per_tick(q_mid_water, params):>13.3f}")
            row.append(f"{throughput_items_per_tick(q_mid_water, params):.3f}~{f_max_water:.3f}".rjust(13))
        # 钠冷
        q_mid_sodium = heat_for_temperature(T_MID, k_of("钠冷", params), c_heat)
        f_max_sodium = throughput_items_per_tick(q_max, params)
        row.append(f"{temperature(q_max, k_of('钠冷', params), c_heat):>9.1f}")
        f_low_sodium = (f_max_sodium if q_mid_sodium > q_max
                        else throughput_items_per_tick(q_mid_sodium, params))
        row.append(f"{f_low_sodium:>13.3f}")
        f_low_water = (f_max_water if q_mid_water >= q_max
                       else throughput_items_per_tick(q_mid_water, params))
        row.append(f"{f_low_sodium / f_low_water:>13.2f}x")
        print(" ".join(row))
    print()
    print("说明：'低档上限'=仍在 <773 K 时的最大通量；'水冷高档区间'=773 K 以上可用的通量范围；"
          "'钠/水低档倍数'=同一尺寸下钠冷能多跑多少倍通量。\n")
    print("=== 表 B：关键不变量 ===")
    for n in sizes:
        c_heat = heat_capacity(n, params["casing_heat_capacity"])
        q_max = max_heat(n, params["base_heat_per_shell"], params["max_heat_per_core"])
        t_water = temperature(q_max, k_of("水冷", params), c_heat)
        print(f"  边长 {n:>2}：水冷满功率 {t_water:>7.1f} K → 低档通量倍数上限 "
              f"{(t_water - T_BOIL) / (T_MID - T_BOIL):>5.2f}x"
              f"（= (T_water - 373.15) / (773 - 373.15)）")
    print(f"  理论上限：水冷即便顶到 {T_LIMIT:.0f} K，低档倍数也只有 "
          f"{(T_LIMIT - T_BOIL) / (T_MID - T_BOIL):.2f}x。")
    print()


def print_boundary_table(params: dict, size: int) -> None:
    n = size
    c_heat = heat_capacity(n, params["casing_heat_capacity"])
    q_max = max_heat(n, params["base_heat_per_shell"], params["max_heat_per_core"])
    print(f"=== 边界工况明细（边长 {n}） ===")
    print(f"{'冷却剂':>5} {'目标T':>7} {'需要发热':>12} {'锭/t':>8} {'水/钠 mB/t':>12} {'燃料项/t':>10} {'是否超出Qmax':>12}")
    for coolant in ("水冷", "钠冷"):
        for target in (T_MID, T_LIMIT):
            q_need = heat_for_temperature(target, k_of(coolant, params), c_heat)
            flag = "超出" if q_need > q_max else "OK"
            print(f"{coolant:>5} {target:>7.0f} {q_need:>12.0f} "
                  f"{throughput_items_per_tick(q_need, params):>8.3f} "
                  f"{coolant_mb_per_tick(q_need, coolant):>12.0f} "
                  f"{fuel_items_per_tick(q_need, params):>10.3f} {flag:>12}")
    print()


def print_fuel_table(params: dict, sizes=SIZES) -> None:
    print("=== 表 C：燃料与冷却剂消耗（达到 773 K 边界的工况） ===")
    print(f"{'边长':>4} {'水冷发热':>12} {'水/水蒸气 mB/t':>14} {'燃料项/t':>9} "
          f"{'钠冷发热':>12} {'钠 mB/t':>12} {'燃料项/t':>9}")
    for n in sizes:
        c_heat = heat_capacity(n, params["casing_heat_capacity"])
        cells = [f"{n:>4}"]
        for coolant in ("水冷", "钠冷"):
            q_need = heat_for_temperature(T_MID, k_of(coolant, params), c_heat)
            cells.append(f"{q_need:>12.0f}")
            cells.append(f"{coolant_mb_per_tick(q_need, coolant):>14.0f}")
            cells.append(f"{fuel_items_per_tick(q_need, params):>9.3f}")
        print(" ".join(cells))
    print()


def sweep_threshold(params: dict, casing_grid, core_grid, base_grid) -> None:
    print("=== 参数扫描：水冷下最小能进入高温带(>=773K) / 触顶(>=1500K) 的尺寸 ===")
    print(f"{'外壳热容':>8} {'基础发热':>9} {'每核心发热':>10} {'3^3 Tmax':>9} {'9^3 Tmax':>9} "
          f"{'18^3 Tmax':>10} {'>=773尺寸':>9} {'>=1500尺寸':>10}")
    for cap in casing_grid:
        for base in base_grid:
            for per_core in core_grid:
                p = dict(params, casing_heat_capacity=cap, base_heat_per_shell=base,
                         max_heat_per_core=per_core)
                temps = {}
                for n in range(3, 19):
                    c_heat = heat_capacity(n, cap)
                    q_max = max_heat(n, base, per_core)
                    temps[n] = temperature(q_max, k_of("水冷", p), c_heat)
                n_mid = next((n for n in sorted(temps) if temps[n] >= T_MID), None)
                n_max = next((n for n in sorted(temps) if temps[n] >= T_LIMIT), None)
                print(f"{cap:>8.0f} {base:>9.0f} {per_core:>10.0f} {temps[3]:>9.1f} {temps[9]:>9.1f} "
                      f"{temps[18]:>10.1f} {str(n_mid):>9} {str(n_max):>10}")
    print()


# ---------------------------------------------------------------- SVG 绘图

SVG_FONT = ("font-family=\"'Microsoft YaHei','Segoe UI',system-ui,sans-serif\"")
THEME_STYLE = """
    .bg { fill: transparent; }
    .axis { stroke: #8a8a8a; stroke-width: 1; fill: none; }
    .grid { stroke: #8a8a8a; stroke-width: 0.6; opacity: 0.28; }
    .tick { fill: #6b6b6b; font-size: 11px; }
    .title { fill: #1f1f1f; font-size: 14px; font-weight: 500; }
    .label { fill: #1f1f1f; font-size: 11.5px; }
    .note { fill: #6b6b6b; font-size: 11px; }
    .band-low { fill: #2f7fd0; opacity: 0.10; }
    .band-high { fill: #d0742f; opacity: 0.10; }
    .limit { stroke: #c0392b; stroke-width: 1.2; stroke-dasharray: 4 3; }
    @media (prefers-color-scheme: dark) {
      .tick, .note { fill: #a8a8a8; }
      .title, .label { fill: #e8e8e8; }
      .axis { stroke: #6f6f6f; }
      .grid { stroke: #8a8a8a; opacity: 0.20; }
    }
"""

SERIES_COLORS = ["#2f7fd0", "#d0742f", "#4c9a52", "#a2569c", "#c8452b", "#3c8f8f", "#7a6a3f"]


def _nice_ticks(lo: float, hi: float, count: int = 6) -> list[float]:
    if hi <= lo:
        hi = lo + 1
    raw = (hi - lo) / count
    mag = 10 ** math.floor(math.log10(raw))
    for mult in (1, 2, 2.5, 5, 10):
        step = mag * mult
        if step >= raw:
            break
    start = math.floor(lo / step) * step
    ticks = []
    value = start
    while value <= hi + step * 0.5:
        if value >= lo - step * 0.001:
            ticks.append(round(value, 10))
        value += step
    return ticks


def _fmt(value: float) -> str:
    if abs(value) >= 1e6:
        return f"{value/1e6:.0f}M"
    if abs(value) >= 1e3:
        return f"{value/1e3:.0f}k"
    if abs(value) >= 10:
        return f"{value:.0f}"
    return f"{value:.2f}".rstrip("0").rstrip(".")


def line_chart(path: str, title: str, x_title: str, y_title: str,
               x_range, y_range, bands, guide_lines, series) -> None:
    """写出一张多序列折线图。

    bands: [(y0, y1, css_class)] 横向色带
    guide_lines: [(y, 文本)] 参考线
    series: [(名称, [(x, y), ...], 颜色, 是否虚线)]
    """
    width, height = 860, 470
    left, right, top, bottom = 78, 150, 46, 52
    plot_w = width - left - right
    plot_h = height - top - bottom
    x_lo, x_hi = x_range
    y_lo, y_hi = y_range

    def sx(x: float) -> float:
        return left + (x - x_lo) / (x_hi - x_lo) * plot_w

    def sy(y: float) -> float:
        return top + plot_h - (y - y_lo) / (y_hi - y_lo) * plot_h

    out = [
        f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {width} {height}" '
        f'width="100%" role="img" aria-label="{title}">',
        f"<title>{title}</title>",
        f"<style>{THEME_STYLE}</style>",
        f'<rect class="bg" x="0" y="0" width="{width}" height="{height}"/>',
    ]
    for y0, y1, css in bands:
        y0c, y1c = max(y0, y_lo), min(y1, y_hi)
        if y1c > y0c:
            out.append(f'<rect class="{css}" x="{left:.1f}" y="{sy(y1c):.1f}" '
                       f'width="{plot_w:.1f}" height="{sy(y0c)-sy(y1c):.1f}"/>')
    for y in _nice_ticks(y_lo, y_hi, 6):
        out.append(f'<line class="grid" x1="{left:.1f}" y1="{sy(y):.1f}" '
                   f'x2="{left+plot_w:.1f}" y2="{sy(y):.1f}"/>')
        out.append(f'<text class="tick" x="{left-8:.1f}" y="{sy(y)+4:.1f}" '
                   f'text-anchor="end">{y:.0f}</text>')
    for x in _nice_ticks(x_lo, x_hi, 6):
        out.append(f'<line class="grid" x1="{sx(x):.1f}" y1="{top:.1f}" '
                   f'x2="{sx(x):.1f}" y2="{top+plot_h:.1f}"/>')
        out.append(f'<text class="tick" x="{sx(x):.1f}" y="{top+plot_h+16:.1f}" '
                   f'text-anchor="middle">{_fmt(x)}</text>')
    out.append(f'<rect class="axis" x="{left:.1f}" y="{top:.1f}" '
               f'width="{plot_w:.1f}" height="{plot_h:.1f}"/>')
    out.append(f'<text class="title" x="{left:.1f}" y="{top-16:.1f}">{title}</text>')
    out.append(f'<text class="label" x="{left+plot_w/2:.1f}" y="{height-12:.1f}" '
               f'text-anchor="middle">{x_title}</text>')
    out.append(f'<text class="label" x="18" y="{top+plot_h/2:.1f}" text-anchor="middle" '
               f'transform="rotate(-90 18 {top+plot_h/2:.1f})">{y_title}</text>')
    for y, text in guide_lines:
        if y_lo <= y <= y_hi:
            out.append(f'<line class="limit" x1="{left:.1f}" y1="{sy(y):.1f}" '
                       f'x2="{left+plot_w:.1f}" y2="{sy(y):.1f}"/>')
            out.append(f'<text class="note" x="{left+plot_w-4:.1f}" y="{sy(y)-4:.1f}" '
                       f'text-anchor="end">{text}</text>')
    for index, (name, points, color, dashed) in enumerate(series):
        if not points:
            continue
        pts = " ".join(f"{sx(px):.1f},{sy(py):.1f}" for px, py in points)
        dash = ' stroke-dasharray="6 4"' if dashed else ""
        out.append(f'<polyline points="{pts}" fill="none" stroke="{color}" '
                   f'stroke-width="2"{dash}/>')
        ex, ey = points[-1]
        out.append(f'<circle cx="{sx(ex):.1f}" cy="{sy(ey):.1f}" r="3" fill="{color}"/>')
        out.append(f'<text class="label" x="{sx(ex)+8:.1f}" y="{sy(ey)+4:.1f}" '
                   f'fill="{color}">{name}</text>')
    out.append("</svg>")
    with open(path, "w", encoding="utf-8") as handle:
        handle.write("\n".join(out))
    print(f"figure: {path}")


def write_figures(fig_dir: str, params: dict, sizes=SIZES) -> None:
    os.makedirs(fig_dir, exist_ok=True)
    bands = [(T_BOIL, T_MID, "band-low"), (T_MID, T_LIMIT, "band-high")]

    # 图 1：发热 - 温度操作线
    series = []
    q_max_all = 0.0
    for index, n in enumerate(sizes):
        c_heat = heat_capacity(n, params["casing_heat_capacity"])
        q_max = max_heat(n, params["base_heat_per_shell"], params["max_heat_per_core"])
        q_max_all = max(q_max_all, q_max)
        color = SERIES_COLORS[index % len(SERIES_COLORS)]
        for coolant, dashed in (("水冷", False), ("钠冷", True)):
            k = k_of(coolant, params)
            series.append((f"{n}³ {coolant}", [(0.0, T_BOIL), (q_max, temperature(q_max, k, c_heat))],
                           color, dashed))
    line_chart(
        os.path.join(fig_dir, "fig1_heat_vs_temperature.svg"),
        "发热速率 → 平衡温度（实线=水冷，虚线=钠冷）",
        "发热速率 Q（heat/tick）", "平衡温度 T（K）",
        (0, q_max_all * 1.02), (300, 1600), bands,
        [(T_LIMIT, "停堆线 1500 K"), (T_MID, "配方分界 773 K")], series)

    # 图 2：通量 - 温度操作线（玩家视角）
    series = []
    f_max_all = 0.0
    for index, n in enumerate(sizes):
        c_heat = heat_capacity(n, params["casing_heat_capacity"])
        q_max = max_heat(n, params["base_heat_per_shell"], params["max_heat_per_core"])
        f_max = throughput_items_per_tick(q_max, params)
        f_max_all = max(f_max_all, f_max)
        color = SERIES_COLORS[index % len(SERIES_COLORS)]
        for coolant, dashed in (("水冷", False), ("钠冷", True)):
            k = k_of(coolant, params)
            series.append((f"{n}³ {coolant}", [(0.0, T_BOIL), (f_max, temperature(q_max, k, c_heat))],
                           color, dashed))
    line_chart(
        os.path.join(fig_dir, "fig2_throughput_vs_temperature.svg"),
        "反应通量 → 平衡温度（实线=水冷，虚线=钠冷）",
        "反应通量（锭/tick，1 锭 = 90 mB）", "平衡温度 T（K）",
        (0, f_max_all * 1.02), (300, 1600), bands,
        [(T_LIMIT, "停堆线 1500 K"), (T_MID, "配方分界 773 K")], series)

    # 图 3：各尺寸最高可达温度
    series = []
    for index, coolant in enumerate(("水冷", "钠冷")):
        color = SERIES_COLORS[index]
        points = []
        for n in range(3, 19):
            c_heat = heat_capacity(n, params["casing_heat_capacity"])
            q_max = max_heat(n, params["base_heat_per_shell"], params["max_heat_per_core"])
            points.append((float(n), temperature(q_max, k_of(coolant, params), c_heat)))
        series.append((coolant, points, color, coolant == "钠冷"))
    line_chart(
        os.path.join(fig_dir, "fig3_max_temperature_vs_size.svg"),
        "满功率时的最高温度 vs 结构边长",
        "结构边长 n（3..18）", "最高可达温度 T（K）",
        (3, 18), (300, 1600), bands,
        [(T_LIMIT, "停堆线 1500 K"), (T_MID, "配方分界 773 K")], series)


def main() -> None:
    params = dict(DEFAULTS)
    print_size_table(params)
    print_fuel_table(params)
    print_boundary_table(params, 9)
    print_boundary_table(params, 18)
    sweep_threshold(
        params,
        casing_grid=[20.0, 50.0, 100.0, 200.0],
        base_grid=[1000.0, 2000.0, 4000.0],
        core_grid=[5000.0, 10000.0, 20000.0, 50000.0])
    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    write_figures(os.path.join(root, "docs", "figures"), params)


if __name__ == "__main__":
    main()
