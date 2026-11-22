#!/usr/bin/env python3
"""Counts blocks in an area of a running test server over RCON (destructive: they are replaced by air).

    python3 tools/count_blocks.py [--dim minecraft:overworld] X1 Z1 X2 Z2 YMIN YMAX BLOCK [BLOCK ...]

`fill ... X replace X` only reports blocks whose state changes, so it cannot count (an upright log is already in
its default state). This tool fills each block with air in 16x16x128 pieces and adds up the counts. Use it only in
a throwaway world; forceload the area first so it is generated. BLOCK may be a tag (#c:ores).
"""
import argparse, os, re, socket, sys

sys.path.insert(0, os.path.dirname(__file__))
from rcon import packet  # noqa: E402


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--port", type=int, default=25575)
    ap.add_argument("--password", default="hexxit")
    ap.add_argument("--dim", default="minecraft:overworld")
    ap.add_argument("x1", type=int)
    ap.add_argument("z1", type=int)
    ap.add_argument("x2", type=int)
    ap.add_argument("z2", type=int)
    ap.add_argument("ymin", type=int)
    ap.add_argument("ymax", type=int)
    ap.add_argument("blocks", nargs="+")
    a = ap.parse_args()
    counts = {b: 0 for b in a.blocks}
    with socket.create_connection(("127.0.0.1", a.port), timeout=30) as sock:
        if packet(sock, 1, 3, a.password)[0] == -1:
            raise SystemExit("rcon: wrong password")
        n = 10
        for x in range(a.x1, a.x2 + 1, 16):
            for z in range(a.z1, a.z2 + 1, 16):
                for y in range(a.ymin, a.ymax + 1, 128):
                    box = f"{x} {y} {z} {min(x + 15, a.x2)} {min(y + 127, a.ymax)} {min(z + 15, a.z2)}"
                    for b in a.blocks:
                        n += 1
                        out = packet(sock, n, 2, f"execute in {a.dim} run fill {box} minecraft:air replace {b}")[1]
                        m = re.search(r"filled (\d+)", out)
                        if m:
                            counts[b] += int(m.group(1))
                        elif "not loaded" in out:
                            print("not loaded:", box, file=sys.stderr)
    for b, c in counts.items():
        print(f"{c:8d}  {b}")


if __name__ == "__main__":
    main()
