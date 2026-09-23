#!/usr/bin/env python3
"""Generates the box_small.nbt GameTest arena structure.

Standalone, standard-library-only generator for
`src/main/resources/data/irons_artifice/structure/box_small.nbt`.

The structure file format is a gzip-compressed, unnamed root NBT compound,
matching the shape of vanilla's `minecraft:empty` structure:

    size:        TAG_List of 3 TAG_Int              -> [7, 5, 7]
    entities:    TAG_List of TAG_Compound, empty
    blocks:      TAG_List of TAG_Compound, one per block position:
                     { "pos": TAG_List of 3 TAG_Int, "state": TAG_Int }
                 where `state` indexes into `palette`
    palette:     TAG_List of TAG_Compound, one per block state:
                     { "Name": TAG_String }
    DataVersion: TAG_Int

The arena is a 7x5x7 box: a solid stone floor at y=0, air for y=1..4.

Run directly to (re)generate the structure file:

    python3 tools/gametest/gen_box_small_structure.py
"""
import gzip
import struct
from pathlib import Path

TAG_END = 0
TAG_BYTE = 1
TAG_INT = 3
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10

SIZE = (7, 5, 7)
DATA_VERSION = 4790

PALETTE = ["minecraft:air", "minecraft:stone"]
AIR = 0
STONE = 1


class Writer:
    def __init__(self):
        self.buf = bytearray()

    def bytes(self, b):
        self.buf += b

    def byte(self, v):
        self.buf += struct.pack(">b", v)

    def unsigned_short(self, v):
        self.buf += struct.pack(">H", v)

    def int(self, v):
        self.buf += struct.pack(">i", v)

    def tag_id(self, tag_id):
        self.byte(tag_id)

    def name(self, name):
        encoded = name.encode("utf-8")
        self.unsigned_short(len(encoded))
        self.bytes(encoded)

    def named_tag_header(self, tag_id, name):
        self.tag_id(tag_id)
        self.name(name)

    def string_payload(self, value):
        encoded = value.encode("utf-8")
        self.unsigned_short(len(encoded))
        self.bytes(encoded)

    def list_header(self, element_type, length):
        self.tag_id(element_type)
        self.int(length)


def write_int_list(w, name, values):
    w.named_tag_header(TAG_LIST, name)
    w.list_header(TAG_INT, len(values))
    for v in values:
        w.int(v)


def write_pos(w, pos):
    w.named_tag_header(TAG_LIST, "pos")
    w.list_header(TAG_INT, 3)
    for v in pos:
        w.int(v)


def write_block_entry(w, pos, state):
    write_pos(w, pos)
    w.named_tag_header(TAG_INT, "state")
    w.int(state)
    w.byte(TAG_END)


def write_palette_entry(w, name):
    w.named_tag_header(TAG_STRING, "Name")
    w.string_payload(name)
    w.byte(TAG_END)


def build_root_compound():
    w = Writer()

    # size
    write_int_list(w, "size", list(SIZE))

    # entities (empty list of compounds)
    w.named_tag_header(TAG_LIST, "entities")
    w.list_header(TAG_COMPOUND, 0)

    # blocks
    sx, sy, sz = SIZE
    positions = [
        (x, y, z)
        for y in range(sy)
        for z in range(sz)
        for x in range(sx)
    ]
    w.named_tag_header(TAG_LIST, "blocks")
    w.list_header(TAG_COMPOUND, len(positions))
    for (x, y, z) in positions:
        state = STONE if y == 0 else AIR
        write_block_entry(w, (x, y, z), state)

    # palette
    w.named_tag_header(TAG_LIST, "palette")
    w.list_header(TAG_COMPOUND, len(PALETTE))
    for name in PALETTE:
        write_palette_entry(w, name)

    # DataVersion
    w.named_tag_header(TAG_INT, "DataVersion")
    w.int(DATA_VERSION)

    w.byte(TAG_END)
    return bytes(w.buf)


def build_file():
    root_payload = build_root_compound()
    out = bytearray()
    out += struct.pack(">b", TAG_COMPOUND)
    out += struct.pack(">H", 0)  # empty root name
    out += root_payload
    return bytes(out)


def main():
    output_path = (
        Path(__file__).resolve().parents[2]
        / "src/main/resources/data/irons_artifice/structure/box_small.nbt"
    )
    output_path.parent.mkdir(parents=True, exist_ok=True)
    data = build_file()
    with gzip.GzipFile(filename="", mode="wb", fileobj=open(output_path, "wb"), mtime=0) as f:
        f.write(data)
    print(f"Wrote {output_path} ({output_path.stat().st_size} bytes)")


if __name__ == "__main__":
    main()
