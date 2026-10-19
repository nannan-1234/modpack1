#!/usr/bin/env python3
"""相变控制炉的贴图生成器（纯标准库，可重复运行）。

为什么这样做：Mekanism 的机器贴图 = **灰阶主体 + 一小块彩色强调色**。我们不改灰阶
（这样机器看起来仍然是 Mekanism 家族的一员：灰色、低饱和、有金属质感），只把"有饱和度"的像素
换到目标色相/饱和度上。于是：

* 外壳 casing   = 裂变堆外壳，强调色换成暖琥珀（相变/高温主题，和相变燃料 #FF9A3C 呼应）
* 控制器        = 裂变堆逻辑适配器，同样的琥珀但更亮一点（一眼能认出是"控制"那块）
* 阀门 valve    = 裂变堆输入端口，按阀门模式换 5 种强调色（用户指定：原料亮绿 / 燃料土黄 /
                  冷却剂青蓝 / 产物黄 / 冷却剂输出银白）
* 核心 core     = 裂变堆燃料组件的杆件贴图，同样换成琥珀色

贴图来源：Mekanism 与 MekanismGenerators（MIT 许可，作者 Aidancboyd/Aidancbrady 等，美术 CyanideX 等）。
本脚本只读取它们的 jar，把**改色结果**写进本模组自己的 assets/，不修改 Mod 本体。

用法（在本模组目录下）：
    python tools/recolor_textures.py                          # 生成全部贴图
    python tools/recolor_textures.py --preview preview.png    # 顺便输出"源贴图 / 生成贴图"对比图
"""

from __future__ import annotations

import argparse
import colorsys
import glob
import os
import struct
import sys
import zlib
import zipfile

# ---------------------------------------------------------------- PNG 读写（只覆盖 MC 贴图用到的情况）


def read_png(data: bytes) -> tuple[int, int, list[list[tuple[int, int, int, int]]]]:
    """读 8bit、非隔行的 RGB/RGBA PNG（MC 贴图都是这个格式）。"""
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError("不是 PNG")
    pos = 8
    width = height = bit_depth = color_type = interlace = None
    idat = bytearray()
    palette: list[tuple[int, int, int]] = []
    while pos < len(data):
        (length,) = struct.unpack(">I", data[pos:pos + 4])
        chunk_type = data[pos + 4:pos + 8]
        chunk = data[pos + 8:pos + 8 + length]
        pos += 12 + length
        if chunk_type == b"IHDR":
            width, height, bit_depth, color_type, _, _, interlace = struct.unpack(">IIBBBBB", chunk)
        elif chunk_type == b"PLTE":
            palette = [tuple(chunk[i:i + 3]) for i in range(0, len(chunk), 3)]
        elif chunk_type == b"IDAT":
            idat += chunk
        elif chunk_type == b"IEND":
            break
    if bit_depth not in (1, 2, 4, 8) or color_type not in (0, 2, 3, 4, 6):
        raise ValueError(f"暂不支持的 PNG 格式: bit_depth={bit_depth} color_type={color_type} interlace={interlace}")
    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[color_type]
    bits_per_pixel = bit_depth * channels
    stride = (width * bits_per_pixel + 7) // 8
    bpp = max(1, bits_per_pixel // 8)
    raw = zlib.decompress(bytes(idat))
    samples: list[list[tuple[int, ...]]] = [[(0,) * channels for _ in range(width)] for _ in range(height)]
    if interlace == 0:
        rows, _ = _decode_scanlines(raw, 0, width, height, stride, bpp)
        for y, line in enumerate(rows):
            for x in range(width):
                samples[y][x] = _read_sample(line, x, bit_depth, channels)
    elif interlace == 1:
        # Adam7 隔行：7 个 pass，每行各自带 filter 字节，解出来再散点填回整图
        offset = 0
        for x0, y0, dx, dy in ((0, 0, 8, 8), (4, 0, 8, 8), (0, 4, 4, 8), (2, 0, 4, 4),
                               (0, 2, 2, 4), (1, 0, 2, 2), (0, 1, 1, 2)):
            pass_width = (width - x0 + dx - 1) // dx
            pass_height = (height - y0 + dy - 1) // dy
            if pass_width <= 0 or pass_height <= 0:
                continue
            pass_stride = (pass_width * bits_per_pixel + 7) // 8
            rows, offset = _decode_scanlines(raw, offset, pass_width, pass_height, pass_stride, bpp)
            for py, line in enumerate(rows):
                for px in range(pass_width):
                    samples[y0 + py * dy][x0 + px * dx] = _read_sample(line, px, bit_depth, channels)
    else:
        raise ValueError(f"未知的隔行方式: {interlace}")
    pixels: list[list[tuple[int, int, int, int]]] = []
    for row in samples:
        out_row = []
        for sample in row:
            if color_type == 0:
                out_row.append((sample[0], sample[0], sample[0], 255))
            elif color_type == 4:
                out_row.append((sample[0], sample[0], sample[0], sample[1]))
            elif color_type == 2:
                out_row.append((sample[0], sample[1], sample[2], 255))
            elif color_type == 6:
                out_row.append((sample[0], sample[1], sample[2], sample[3]))
            else:
                r, g, b = palette[sample[0]]
                out_row.append((r, g, b, 255))
        pixels.append(out_row)
    return width, height, pixels


def _read_sample(line: bytearray, x: int, bit_depth: int, channels: int) -> tuple[int, ...]:
    """从一行里取出第 x 个像素的样本（支持 8bit 与索引色的 1/2/4bit）。"""
    if bit_depth == 8:
        return tuple(line[x * channels:(x + 1) * channels])
    # 索引色：一个字节里塞多个像素，高位在前
    per_byte = 8 // bit_depth
    byte = line[x // per_byte]
    shift = 8 - bit_depth * (x % per_byte + 1)
    return ((byte >> shift) & ((1 << bit_depth) - 1),)


def _decode_scanlines(raw: bytes, offset: int, width: int, height: int, stride: int, bpp: int):
    """解出 width×height 个子图（每行一个 filter 字节），返回 (行列表, 新的读取偏移)。"""
    rows: list[bytearray] = []
    previous = bytearray(stride)
    for _ in range(height):
        filter_type = raw[offset]
        line = bytearray(raw[offset + 1:offset + 1 + stride])
        offset += 1 + stride
        _unfilter(filter_type, line, previous, bpp)
        rows.append(line)
        previous = line
    return rows, offset


def _unfilter(filter_type: int, line: bytearray, previous: bytearray, channels: int) -> None:
    if filter_type == 0:
        return
    if filter_type == 1:
        for i in range(channels, len(line)):
            line[i] = (line[i] + line[i - channels]) & 0xFF
    elif filter_type == 2:
        for i in range(len(line)):
            line[i] = (line[i] + previous[i]) & 0xFF
    elif filter_type == 3:
        for i in range(len(line)):
            left = line[i - channels] if i >= channels else 0
            line[i] = (line[i] + ((left + previous[i]) >> 1)) & 0xFF
    elif filter_type == 4:
        for i in range(len(line)):
            left = line[i - channels] if i >= channels else 0
            up_left = previous[i - channels] if i >= channels else 0
            up = previous[i]
            estimate = left + up - up_left
            pa, pb, pc = abs(estimate - left), abs(estimate - up), abs(estimate - up_left)
            if pa <= pb and pa <= pc:
                predictor = left
            elif pb <= pc:
                predictor = up
            else:
                predictor = up_left
            line[i] = (line[i] + predictor) & 0xFF
    else:
        raise ValueError(f"未知 PNG 过滤器: {filter_type}")


def write_png(path: str, width: int, height: int, pixels: list[list[tuple[int, int, int, int]]]) -> None:
    raw = bytearray()
    for row in pixels:
        raw.append(0)
        for r, g, b, a in row:
            raw += bytes((r, g, b, a))

    def chunk(tag: bytes, payload: bytes) -> bytes:
        return (struct.pack(">I", len(payload)) + tag + payload
                + struct.pack(">I", zlib.crc32(tag + payload) & 0xFFFFFFFF))

    header = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    body = (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", header)
            + chunk(b"IDAT", zlib.compress(bytes(raw), 9)) + chunk(b"IEND", b""))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as handle:
        handle.write(body)


# ---------------------------------------------------------------- 改色

SATURATION_THRESHOLD = 0.18


def recolor(pixels, hue_deg, saturation_scale: float, value_scale: float, gray_color=None,
            cutout_min_saturation=None):
    """灰阶像素原样保留，只给"有颜色"的像素换色相/饱和度（这就是保持 Mekanism 风格的关键）。

    ``gray_color`` 可选，形如 ``(hue_deg, saturation)``：给**灰阶**像素指定色相与饱和度（明度不动）。
    用于"底图本身完全是灰的"贴图（裂变堆外壳/逻辑适配器就是纯灰，没有强调色可换），
    这样是直接给灰阶上色而不是乘通道系数——不会把本来就亮的贴图压暗或截顶。

    ``cutout_min_saturation`` 可选：饱和度低于它的像素直接**变透明**。用来把
    Mekanism 那张"整面不透明"的 LED 贴图改造成真正的覆盖层（只留发光的那一圈）。
    """
    result = []
    for row in pixels:
        new_row = []
        for r, g, b, a in row:
            if a == 0:
                new_row.append((r, g, b, a))
                continue
            h, s, v = colorsys.rgb_to_hsv(r / 255.0, g / 255.0, b / 255.0)
            if cutout_min_saturation is not None and s < cutout_min_saturation:
                new_row.append((0, 0, 0, 0))
                continue
            if s >= SATURATION_THRESHOLD:
                if hue_deg is not None:
                    h = (hue_deg % 360.0) / 360.0
                s = min(1.0, s * saturation_scale)
            elif gray_color is not None:
                # 灰阶像素：直接指定色相/饱和度，明度保持原样
                h = (gray_color[0] % 360.0) / 360.0
                s = min(1.0, gray_color[1])
            v = min(1.0, v * value_scale)
            nr, ng, nb = colorsys.hsv_to_rgb(h, s, v)
            new_row.append((round(nr * 255), round(ng * 255), round(nb * 255), a))
        result.append(new_row)
    return result


# ---------------------------------------------------------------- 任务表

# 注意：整合包的 mod 文件名带中文前缀（如 "[通用机械] Mekanism-..."），所以用 *Mekanism... 匹配，
# 再把附属模组（Generators / Tools / Empowered / kubejs 插件 / JEI 插件）排除掉。
CORE_JAR_PATTERN = "*Mekanism-1.20.1-*.jar"
CORE_JAR_EXCLUDE = ("Generators", "Tools", "Empowered", "kubejs", "JustEnough")
GENERATORS_JAR_PATTERN = "*MekanismGenerators-1.20.1-*.jar"

# (源贴图在 jar 里的路径, 输出路径（相对 assets/nannan_multiblock/）, 色相°, 饱和度倍数, 明度倍数,
#  灰阶上色 (色相°, 饱和度) 或 None, 透明度裁剪阈值)
#
# 选底图的依据（都是 Mekanism 自己的贴图，改色后仍然是"灰阶主体 + 低饱和强调色"的 Mekanism 风格）：
#   外壳   ← **裂变堆外壳** fission_reactor_casing：底图干净（一块平板 + 细边），但**本身纯灰**、
#            没有强调色可换 → 用"灰阶上色"整体染成色相 42°、饱和 0.40，明度 ×1.14（对比过 4 组参数：
#            0.30/1.00 偏灰卡其、0.34/1.06 偏洗白、0.46/1.05 偏橄榄暗，0.40/1.14 最像"黄铜"）
#            （第一版用聚变堆框架做底图，用户反馈"纹理太杂乱""跟聚变堆太像""像刚出土的文物"）
#   控制器 ← **裂变堆逻辑适配器** fission_reactor_logic_adapter：干净的平板 + 一个小方块；
#            平板用灰阶上色染成色相 46°、饱和 0.46、明度 ×1.20（比外壳更亮更黄），
#            那个小方块（原本深红）换色相到 46° 变成金色指示灯
#   阀门   ← 锅炉阀门 boiler_valve_input：Mekanism 自己的阀门，强调色饱和度高，换成 5 种模式色后区分度好，
#            而且它自带 LED 贴图（template/cube_all_led）；注意那张 LED 贴图**整面不透明**，
#            直接抄会把阀门盖成一坨浅灰，所以这里用 cutout 阈值只保留发光的那一圈，做成真正的覆盖层
#            阀门"本体"也顺带用很低的饱和度（0.22）染一层同样的暖色 —— 不然它夹在黄铜外壳里会是一块灰板；
#            模式颜色仍然只体现在那一圈强调环 + LED 上，一眼能分清 5 种模式
#   核心   ← 裂变堆燃料组件的杆件：原图是灰度，用很低的饱和度（0.15）上个暖色，像热金属
JOBS = [
    ("assets/mekanismgenerators/textures/block/fission_reactor_casing.png",
     "textures/block/control_furnace_casing.png", 42, 1.00, 1.14, (42, 0.40), None),
    ("assets/mekanismgenerators/textures/ctm/fission_reactor_casing.png",
     "textures/ctm/control_furnace_casing.png", 42, 1.00, 1.14, (42, 0.40), None),
    ("assets/mekanismgenerators/textures/block/fission_reactor_logic_adapter.png",
     "textures/block/control_furnace_controller.png", 46, 1.60, 1.20, (46, 0.46), None),
    ("assets/mekanismgenerators/textures/ctm/fission_reactor_logic_adapter.png",
     "textures/ctm/control_furnace_controller.png", 46, 1.60, 1.20, (46, 0.46), None),
    ("assets/mekanismgenerators/textures/block/model/fuel_assembly/fuel_rod_assembly_rods_top.png",
     "textures/block/control_furnace_core_rods_top.png", None, 1.00, 1.00, (40, 0.15), None),
    ("assets/mekanismgenerators/textures/block/model/fuel_assembly/fuel_rod_assembly_side.png",
     "textures/block/control_furnace_core_side.png", None, 1.00, 1.00, (40, 0.15), None),
    ("assets/mekanismgenerators/textures/block/model/fuel_assembly/fuel_rod_assembly_base.png",
     "textures/block/control_furnace_core_base.png", None, 1.00, 1.00, (40, 0.15), None),
]

# 阀门：同一个源贴图 × 5 种模式配色（用户指定：亮绿 / 土黄 / 青蓝 / 黄 / 白）
VALVE_SOURCE = "assets/mekanism/textures/block/boiler_valve_input.png"
VALVE_LED_SOURCE = "assets/mekanism/textures/block/boiler_valve_input_led.png"
VALVE_CTM_SOURCE = "assets/mekanism/textures/ctm/boiler_valve_input.png"
VALVE_MODES = [
    ("input_material", "control_furnace_valve_input_material", 120.0, 0.85, 1.00),
    ("input_fuel", "control_furnace_valve_input_fuel", 45.0, 0.85, 1.00),
    ("input_coolant", "control_furnace_valve_input_coolant", 190.0, 0.85, 1.00),
    ("output_product", "control_furnace_valve_output_product", 55.0, 0.95, 1.05),
    ("output_coolant", "control_furnace_valve_output_coolant", None, 0.12, 1.05),
]
for _mode, _name, _hue, _sat, _val in VALVE_MODES:
    JOBS.append((VALVE_SOURCE, f"textures/block/{_name}.png", _hue, _sat, _val, (42, 0.22), None))
    # LED：只保留发光那一圈（cutout），做成会发光的模式指示环
    JOBS.append((VALVE_LED_SOURCE, f"textures/block/{_name}_led.png", _hue, _sat, _val, None, 0.35))
    JOBS.append((VALVE_CTM_SOURCE, f"textures/ctm/{_name}.png", _hue, _sat, _val, (42, 0.22), None))

# 方块贴图的 .png.mcmeta：Mekanism 用 CTM（连通纹理）把外壳连成一片，
# 我们照抄这套写法，只把路径换成自己的（没有 CTM 支持时这些文件会被忽略，不影响游戏）。
MCMETA_BLOCKS = [
    ("control_furnace_casing", ["nannan_multiblock:control_furnace_casing",
                                "nannan_multiblock:control_furnace_controller",
                                "nannan_multiblock:control_furnace_valve"]),
    ("control_furnace_controller", ["nannan_multiblock:control_furnace_casing",
                                    "nannan_multiblock:control_furnace_controller",
                                    "nannan_multiblock:control_furnace_valve"]),
] + [(_name, ["nannan_multiblock:control_furnace_casing",
              "nannan_multiblock:control_furnace_controller",
              "nannan_multiblock:control_furnace_valve"]) for _mode, _name, *_ in VALVE_MODES]


def mcmeta_json(name: str) -> str:
    targets = next(blocks for entry, blocks in MCMETA_BLOCKS if entry == name)
    connect_to = ",".join('{"block":"%s"}' % block for block in targets)
    return ('{"ctm":{"ctm_version":1,"type":"CTM","layer":"SOLID","textures":["nannan_multiblock:ctm/%s"],'
            '"extra":{"connect_to":[%s]}}}' % (name, connect_to))


# ---------------------------------------------------------------- 运行


def find_jar(mods_dir: str, pattern: str, exclude: tuple[str, ...] = ()) -> str:
    matches = [path for path in glob.glob(os.path.join(mods_dir, pattern))
               if not any(token in os.path.basename(path) for token in exclude)]
    if not matches:
        raise SystemExit(f"在 {mods_dir} 找不到 {pattern}")
    return matches[0]


def zoom(pixels, factor: int):
    out = []
    for row in pixels:
        zoomed = [pixel for pixel in row for _ in range(factor)]
        for _ in range(factor):
            out.append(list(zoomed))
    return out


def build_preview(core_jar: str, generators_jar: str, out_path: str, factor: int = 6) -> None:
    """把"源贴图 / 生成结果"拼成一张对比图，方便肉眼检查改色是否还像 Mekanism。"""
    columns = []
    for src, dst, hue, sat, val, gray_color, cutout in JOBS:
        jar = generators_jar if src.startswith("assets/mekanismgenerators") else core_jar
        with zipfile.ZipFile(jar) as archive:
            source_png = read_png(archive.read(src))
        generated = recolor(source_png[2], hue, sat, val, gray_color, cutout)
        columns.append((zoom(source_png[2], factor), zoom(generated, factor)))
    gap = 2
    tile_height = max(len(top) for top, _ in columns) + gap + max(len(bottom) for _, bottom in columns)
    tile_width = gap + sum(max(len(top[0]), len(bottom[0])) + gap for top, bottom in columns)
    sheet = [[(24, 24, 28, 255) for _ in range(tile_width)] for _ in range(tile_height)]
    x = gap
    for top, bottom in columns:
        width = max(len(top[0]), len(bottom[0]))
        for y, row in enumerate(top):
            for dx, pixel in enumerate(row):
                sheet[y][x + dx] = pixel
        offset = max(len(row) for row in top) + gap
        for y, row in enumerate(bottom):
            for dx, pixel in enumerate(row):
                sheet[offset + y][x + dx] = pixel
        x += width + gap
    write_png(out_path, tile_width, tile_height, sheet)
    print(f"预览图: {out_path}")


def main() -> int:
    parser = argparse.ArgumentParser(description="生成相变控制炉的自绘贴图（基于 Mekanism 贴图改色）")
    parser.add_argument("--mc-root", default=r"E:\minecraft\.minecraft")
    parser.add_argument("--mods-dir", default=None, help="默认 <mc-root>/versions/1.20.1-Forge_47.4.22-wip/mods")
    parser.add_argument("--out-root", default=None, help="默认本模组的 src/main/resources/assets/nannan_multiblock")
    parser.add_argument("--preview", default=None, help="额外输出一张源贴图/结果对比图")
    args = parser.parse_args()

    mods_dir = args.mods_dir or os.path.join(args.mc_root, "versions", "1.20.1-Forge_47.4.22-wip", "mods")
    core_jar = find_jar(mods_dir, CORE_JAR_PATTERN, CORE_JAR_EXCLUDE)
    generators_jar = find_jar(mods_dir, GENERATORS_JAR_PATTERN)
    here = os.path.dirname(os.path.abspath(__file__))
    out_root = args.out_root or os.path.join(os.path.dirname(here), "src", "main", "resources",
                                             "assets", "nannan_multiblock")

    for src, dst, hue, sat, val, gray_color, cutout in JOBS:
        jar = generators_jar if src.startswith("assets/mekanismgenerators") else core_jar
        with zipfile.ZipFile(jar) as archive:
            width, height, pixels = read_png(archive.read(src))
        result = recolor(pixels, hue, sat, val, gray_color, cutout)
        target = os.path.join(out_root, dst.replace("/", os.sep))
        write_png(target, width, height, result)
        print(f"{dst}  ({width}x{height})")

    for name, _ in MCMETA_BLOCKS:
        path = os.path.join(out_root, "textures", "block", name + ".png.mcmeta")
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8") as handle:
            handle.write(mcmeta_json(name))

    if args.preview:
        build_preview(core_jar, generators_jar, args.preview)
    print(f"完成：{len(JOBS)} 张贴图 -> {out_root}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
