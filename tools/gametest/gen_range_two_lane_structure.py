#!/usr/bin/env python3
"""Generates the range_two_lane.nbt GameTest arena structure.

Standalone, standard-library-only generator for
`src/main/resources/data/irons_artifice/structure/range_two_lane.nbt`.

Imports the NBT `Writer` and helpers from `gen_box_small_structure.py`
rather than duplicating them.

The structure file format is a gzip-compressed, unnamed root NBT compound,
matching the shape of vanilla's `minecraft:empty` structure (see
`gen_box_small_structure.py` for the full field-by-field description).

The arena is an 11x7x33 range (X wide, Y high, Z long): a solid stone floor
at y=0, air for y=1..6. It is wide enough to hold two parallel firing lanes
and long enough for bullets to travel, so a block-breaking or area-effect
modifier fired in one lane cannot contaminate the other.

Run directly to (re)generate the structure file:

    python3 tools/gametest/gen_range_two_lane_structure.py
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from gen_box_small_structure import (
    DATA_VERSION,
    Writer,
    write_block_entry,
    write_int_list,
    write_palette_entry,
)

import gzip
import struct

TAG_END = 0
TAG_INT = 3
TAG_LIST = 9
TAG_COMPOUND = 10

SIZE = (11, 7, 33)

PALETTE = ["minecraft:air", "minecraft:stone"]
AIR = 0
STONE = 1


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
        / "src/main/resources/data/irons_artifice/structure/range_two_lane.nbt"
    )
    output_path.parent.mkdir(parents=True, exist_ok=True)
    data = build_file()
    with gzip.GzipFile(filename="", mode="wb", fileobj=open(output_path, "wb"), mtime=0) as f:
        f.write(data)
    print(f"Wrote {output_path} ({output_path.stat().st_size} bytes)")


if __name__ == "__main__":
    main()
