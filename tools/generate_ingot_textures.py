#!/usr/bin/env python3
"""生成 kubejs 自定义锭物品的 16x16 材质。

轮廓来源
--------
tinkers_advanced 的全部锭共用同一块 16x16 轮廓掩码：对 densium_ingot / neutronite_ingot /
irradium_ingot / bismuth_ingot 逐像素比对，透明区完全一致（差异 0 像素）。新锭若另起一个轮廓，
放进物品栏会明显不协调，因此本脚本直接复用 densium_ingot 的轮廓与明暗结构，只替换调色板。

SHADE_MASK 是把 densium_ingot 的像素按感知亮度排序后压缩成的"明暗等级图"：
'.' 为透明，'0' 最暗，'B'（即 11）最亮。

调色板来源
----------
densium_ingot 每个等级只有一种颜色，整条色阶是"同一色相 × 不同明度"，感知亮度从 6.9 到 80.2。
新锭用 TARGET_LUMINANCE 对准这条曲线：只对齐 HSL 明度是不够的，因为偏蓝的色相同样明度下
感知亮度会低一半，成品会明显比超致密锭发暗，看不出是同一族。

亮度曲线可以按材料替换（见 HIGH_LUMINANCE）：以别的材料为基底时，它的明暗跨度才是对的基准。
另外 Material(accents=...) 可以把指定像素改成异材质的高光，用于表现"掺了晶体"之类的内容。

调参
----
每种材料一行 Material(...)：色相 hue、饱和度 sat、整体明度倍率 brightness。
改完重跑脚本即可覆盖生成材质。

用法
----
    python tools/generate_ingot_textures.py
    python tools/generate_ingot_textures.py --preview-dir <目录>   # 额外输出放大预览图
"""

import argparse
import colorsys
import os
import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TEXTURE_DIR = ROOT / "kubejs" / "assets" / "kubejs" / "textures" / "item"
SIZE = 16

# densium_ingot 的轮廓与明暗等级（'.'=透明，0=最暗 ... B=最亮），16 行 16 列。
SHADE_MASK = (
    "................",
    "................",
    "..........AA....",
    ".......AAA31A...",
    "....AAA661311A..",
    ".AAA6866613111A.",
    "AB88686661316669",
    "A7B8686668880259",
    "A74B68AAA2220259",
    "A742BB4242220559",
    "A742A4424255599.",
    ".A72A44555999...",
    "..A7855999......",
    "...AA99.........",
    "................",
    "................",
)

# densium_ingot 各等级的感知亮度（0.299R + 0.587G + 0.114B），由实际材质逐像素统计得到。
TARGET_LUMINANCE = (
    6.9, 9.9, 16.9, 20.2, 25.3, 27.4,
    30.1, 33.7, 40.0, 50.6, 60.2, 80.2,
)

# 以 osgloglas_ingot（tinkers_advanced 的亮绿金属，整块没有暗部）为基底的锭用这条曲线。
# 注意这里是按形态重新设计的连续曲线，而不是照搬 osgloglas 自身的逐像素亮度：
# osgloglas 的亮度集中在 96.9~238.5，直接套用会让 16x16 的立方感被冲平（实测成一团浅青）。
# 所以起点压到 62 保住暗部结构，上限抬到 228 保住它"亮金属"的特征。
HIGH_LUMINANCE = (
    62.0, 78.0, 94.0, 108.0, 122.0, 134.0,
    146.0, 158.0, 172.0, 186.0, 202.0, 228.0,
)


class Material:
    """一种锭的调色板参数。

    hue / sat   色相与饱和度。
    brightness  整体明度倍率，乘在亮度曲线上。
    luminance   12 级感知亮度曲线，省略时用 TARGET_LUMINANCE。
    accents     定点着色，形如 ((x, y, 0xFF80D7), ...)，用于掺入异材质的高光。
    """

    def __init__(self, hue, sat, brightness=1.0, luminance=None, accents=()):
        self.hue = hue
        self.sat = sat
        self.brightness = brightness
        self.luminance = luminance or TARGET_LUMINANCE
        self.accents = accents


# 三种锭都从 densium 的紫罗兰色系（色相约 264°）出发，沿色相轴左右展开，再叠加整体明度差异，
# 保证在物品栏里既能一眼分辨、又明显是同族：
# durusium 偏冷蓝、整体更暗（硬质）；moderatium 最接近 densium（中庸）；lentusium 偏暖品红、更亮（柔韧）。
MATERIALS = {
    "durusium_ingot": Material(hue=246.0, sat=0.85, brightness=0.90),
    "moderatium_ingot": Material(hue=272.0, sat=0.45, brightness=1.20),
    "lentusium_ingot": Material(hue=304.0, sat=0.75, brightness=1.10),
    # fluix + osgloglas 合金：青绿(178°)是 osgloglas 的绿(约128°)与 fluix 的蓝紫(228~268°)
    # 夹出来的色相，两种原料各占一半；亮度走高亮曲线，因为基底 osgloglas 本身就是亮金属。
    # 两颗粉紫高光取自 fluix_crystal 自己的标志色，用来说明这块金属里掺了晶体。
    "fluix_osgloglas_ingot": Material(
        hue=178.0,
        sat=0.80,
        luminance=HIGH_LUMINANCE,
        
    ),
}


def luminance(rgb):
    r, g, b = rgb
    return 0.299 * r + 0.587 * g + 0.114 * b


def shade_to_rgb(material, shade):
    """把明暗等级（0-11）转成该材料的 RGB。"""
    # 二分求 HSL 明度，使该色恰好命中目标感知亮度；亮度对明度单调，可稳定收敛。
    target = material.luminance[shade] * material.brightness
    hue = material.hue / 360.0
    low, high = 0.0, 1.0
    for _ in range(40):
        mid = (low + high) / 2
        rgb = colorsys.hls_to_rgb(hue, mid, material.sat)
        if luminance(rgb) * 255 < target:
            low = mid
        else:
            high = mid
    rgb = colorsys.hls_to_rgb(hue, (low + high) / 2, material.sat)
    return tuple(round(channel * 255) for channel in rgb)


def build_palette(material):
    return [shade_to_rgb(material, i) for i in range(len(material.luminance))]


def build_rows(palette, accents=()):
    """按 SHADE_MASK 生成 16 行 RGBA 字节。"""
    rows = []
    for line in SHADE_MASK:
        row = bytearray()
        for char in line:
            if char == ".":
                row += b"\x00\x00\x00\x00"
            else:
                r, g, b = palette[int(char, 16)]
                row += bytes((r, g, b, 255))
        rows.append(bytes(row))

    # 定点着色：只改不透明像素，轮廓掩码不受影响。
    for x, y, color in accents:
        rows[y] = rows[y][:x * 4] + bytes(
            ((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, 255)
        ) + rows[y][x * 4 + 4:]
    return rows


def write_png(path, rows):
    """写出 8 位 RGBA（colortype 6）PNG，与 kubejs/assets/kubejs/textures 下既有材质一致。"""
    raw = b"".join(b"\x00" + row for row in rows)
    width = len(rows[0]) // 4
    height = len(rows)

    def chunk(tag, data):
        body = tag + data
        return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body) & 0xFFFFFFFF)

    header = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    png = (
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", header)
        + chunk(b"IDAT", zlib.compress(raw, 9))
        + chunk(b"IEND", b"")
    )
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as handle:
        handle.write(png)


def write_preview(path, rows, scale, cell=4):
    """输出放大预览：棋盘底 + 最近邻放大，便于肉眼检查像素与透明区。"""
    checker = ((0x4A, 0x4A, 0x4A), (0x66, 0x66, 0x66))
    preview_rows = []
    for y, row in enumerate(rows):
        out = bytearray()
        for x in range(SIZE):
            r, g, b, a = row[x * 4:x * 4 + 4]
            if a == 0:
                r, g, b = checker[(x // cell + y // cell) % 2]
            out += bytes((r, g, b, 255)) * scale
        preview_rows.extend([bytes(out)] * scale)
    write_png(path, preview_rows)


def main():
    parser = argparse.ArgumentParser(description="生成 kubejs 自定义锭材质")
    parser.add_argument("--preview-dir", help="额外输出放大预览图到此目录")
    parser.add_argument("--scale", type=int, default=8, help="预览放大倍数（默认 8）")
    args = parser.parse_args()

    for name, material in MATERIALS.items():
        palette = build_palette(material)
        rows = build_rows(palette, material.accents)
        path = TEXTURE_DIR / (name + ".png")
        write_png(path, rows)
        # 调色板按明暗等级由暗到亮打印，便于核对与调整。
        print("%s  palette: %s" % (path, " ".join("%02X%02X%02X" % rgb for rgb in palette)))

        if args.preview_dir:
            preview = os.path.join(args.preview_dir, name + "_preview.png")
            write_preview(preview, rows, args.scale)
            print("%s  (preview)" % preview)


if __name__ == "__main__":
    main()
