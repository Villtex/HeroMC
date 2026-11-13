#!/usr/bin/env python3
"""Compare a running dev server's terrain with a prediction file, over RCON.

Input lines: "x y z oldBlockId" (block check) or "B x z namespace:biome" (biome check at y 70),
as written by a small Java driver around hexxit.worldgen152.core (see README, "Weltgenerierung").
Needs enable-rcon=true, rcon.password=hexxit, rcon.port=25575 in run/server/server.properties.

Usage:  python3 tools/verify_blocks.py expect.txt
"""
import os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from rcon import packet
import socket

NAMES = {0: 'minecraft:air', 1: 'minecraft:stone', 2: 'minecraft:grass_block', 3: 'minecraft:dirt', 7: 'minecraft:bedrock',
         8: 'minecraft:water', 9: 'minecraft:water', 10: 'minecraft:lava', 11: 'minecraft:lava', 12: 'minecraft:sand',
         13: 'minecraft:gravel', 24: 'minecraft:sandstone', 79: 'minecraft:ice', 80: 'minecraft:snow_block',
         110: 'minecraft:mycelium', 254: 'extrabiomesxl:red_rock', 255: 'extrabiomesxl:cracked_sand'}


def main():
    lines = [l.split() for l in open(sys.argv[1]) if l.strip()]
    chunks = sorted({(int(l[0]) >> 4, int(l[2]) >> 4) for l in lines if l[0] != 'B'})
    with socket.create_connection(('127.0.0.1', 25575), timeout=60) as s:
        if packet(s, 1, 3, 'hexxit')[0] == -1:
            raise SystemExit('rcon: wrong password')
        for cx, cz in chunks:
            packet(s, 2, 2, f'forceload add {cx * 16} {cz * 16}')
        ok = bad = bok = bbad = shown = 0
        for l in lines:
            if l[0] == 'B':
                r = packet(s, 3, 2, f'execute if biome {l[1]} 70 {l[2]} {l[3]}')[1]
                if 'passed' in r:
                    bok += 1
                else:
                    bbad += 1
                    if shown < 12:
                        shown += 1
                        print('BIOME MISMATCH', ' '.join(l), r)
                continue
            x, y, z, bid = map(int, l)
            r = packet(s, 3, 2, f'execute if block {x} {y} {z} {NAMES[bid]}')[1]
            if 'passed' in r:
                ok += 1
            else:
                bad += 1
                if shown < 12:
                    shown += 1
                    print('BLOCK MISMATCH', x, y, z, NAMES[bid], '->', r)
        for cx, cz in chunks:
            packet(s, 2, 2, f'forceload remove {cx * 16} {cz * 16}')
    print(f'blocks: {ok} match, {bad} differ; biomes: {bok} match, {bbad} differ; {len(chunks)} chunks')
    sys.exit(1 if bad or bbad else 0)


if __name__ == '__main__':
    main()
