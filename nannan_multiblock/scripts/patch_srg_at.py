#!/usr/bin/env python3
"""为编译期类路径生成"已应用访问转换"的 SRG jar 副本。

背景：Forge 在运行时（以及开发环境的 dev jar 里）会应用 `accesstransformer.cfg` 中的访问转换，
例如 `public net.minecraft.world.level.block.entity.BlockEntityType$BlockEntitySupplier`。
本项目用 javac 直接对**未打 AT 的** SRG jar 编译，于是 javac 认为该嵌套接口是包级私有，
拒绝把 lambda 当成它的实现（报 "BlockEntitySupplier 在 BlockEntityType 中是 private 访问控制"），
而 Mekanism 的 `TileEntityTypeDeferredRegister.register(...)` 恰好要求传这个函数式接口。

做法：复制 SRG jar，把指定类改成 public —— 同时改类自身的 access_flags 和它自己的
InnerClasses 记录（javac 判定嵌套类可见性会看 InnerClasses）。

用法：
    python patch_srg_at.py <input-srg.jar> <output.jar> <class.name.With$Nested> [more...]
"""

from __future__ import annotations

import sys
import zipfile

ACC_PUBLIC = 0x0001
CONSTANT_UTF8 = 1


def u2(data: bytes, offset: int) -> int:
    return int.from_bytes(data[offset:offset + 2], "big")


def u4(data: bytes, offset: int) -> int:
    return int.from_bytes(data[offset:offset + 4], "big")


def parse_constant_pool(data: bytes, cursor: int):
    """返回 (utf8_by_index, class_name_index_by_index, cursor_after_pool)。"""
    count = u2(data, cursor)
    cursor += 2
    utf8: dict[int, str] = {}
    class_name_index: dict[int, int] = {}
    index = 1
    while index < count:
        tag = data[cursor]
        cursor += 1
        if tag == CONSTANT_UTF8:
            length = u2(data, cursor)
            cursor += 2
            utf8[index] = data[cursor:cursor + length].decode("utf-8", "replace")
            cursor += length
        elif tag in (7, 8, 16, 19, 20):
            if tag == 7:
                class_name_index[index] = u2(data, cursor)
            cursor += 2
        elif tag == 15:
            cursor += 3
        elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
            cursor += 4
        elif tag in (5, 6):
            cursor += 8
            index += 1
        else:
            raise ValueError(f"unsupported constant pool tag {tag}")
        index += 1
    return utf8, class_name_index, cursor


def skip_attributes(data: bytes, cursor: int):
    """返回 (cursor_after, [(name_index, attr_body_offset, attr_length), ...])。"""
    count = u2(data, cursor)
    cursor += 2
    entries = []
    for _ in range(count):
        name_index = u2(data, cursor)
        cursor += 2
        length = u4(data, cursor)
        cursor += 4
        entries.append((name_index, cursor, length))
        cursor += length
    return cursor, entries


def patch_class(data: bytes, targets: set[str]) -> bytes:
    utf8, class_name_index, cursor = parse_constant_pool(data, 8)
    access_flags_offset = cursor
    access_flags = u2(data, cursor)
    cursor += 2
    this_class = u2(data, cursor)
    cursor += 2
    this_name = utf8.get(class_name_index.get(this_class, -1), "")
    cursor += 2  # super_class
    interfaces_count = u2(data, cursor)
    cursor += 2 + interfaces_count * 2

    for _ in range(2):  # fields then methods
        member_count = u2(data, cursor)
        cursor += 2
        for _ in range(member_count):
            cursor += 6
            cursor, _ = skip_attributes(data, cursor)
    cursor, class_attributes = skip_attributes(data, cursor)

    patches = []
    if this_name in targets:
        patches.append((access_flags_offset, access_flags | ACC_PUBLIC))

    # 嵌套类可见性由「外层类的 InnerClasses 属性」决定，因此两个方向都要改：
    #   1) 目标类自身文件里的 InnerClasses 记录（自己那条）；
    #   2) 外层类文件里指向目标类的那条记录（javac 实际看的是这一条）。
    for name_index, attr_start, _attr_len in class_attributes:
        if utf8.get(name_index) != "InnerClasses":
            continue
        inner_cursor = attr_start
        inner_count = u2(data, inner_cursor)
        inner_cursor += 2
        for _ in range(inner_count):
            inner_class_info = u2(data, inner_cursor)
            flags_offset = inner_cursor + 6
            inner_name = utf8.get(class_name_index.get(inner_class_info, -1), "")
            if inner_name in targets:
                patches.append((flags_offset, u2(data, flags_offset) | ACC_PUBLIC))
            inner_cursor += 8

    if not patches:
        return data

    patched = bytearray(data)
    for offset, value in patches:
        patched[offset:offset + 2] = value.to_bytes(2, "big")
    return bytes(patched)


def main() -> int:
    if len(sys.argv) < 4:
        print(__doc__)
        return 2
    source, destination = sys.argv[1], sys.argv[2]
    targets = {name.replace(".", "/") for name in sys.argv[3:]}
    # 目标类自身 + 外层类（外层类的 InnerClasses 属性决定嵌套类的可见性）。
    candidates = set()
    for target in targets:
        candidates.add(target)
        if "$" in target:
            candidates.add(target.rsplit("$", 1)[0])
    patched_count = 0
    with zipfile.ZipFile(source) as src, zipfile.ZipFile(destination, "w", zipfile.ZIP_DEFLATED) as dst:
        for entry in src.infolist():
            payload = src.read(entry.filename)
            if entry.filename.endswith(".class") and entry.filename[:-len(".class")] in candidates:
                new_payload = patch_class(payload, targets)
                if new_payload != payload:
                    patched_count += 1
                payload = new_payload
            dst.writestr(entry, payload)
    print(f"patched {patched_count} class(es) -> {destination}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
