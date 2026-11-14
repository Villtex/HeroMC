#!/usr/bin/env python3
"""Compare a running dev server's world with a prediction file from tools/expect.sh, over RCON.

Input lines:
  "x y z oldBlockId meta"   block check (meta may be missing = 0)
  "B x z namespace:biome"   biome check at y 70
  "S x y z Mob"             mob spawner with that 1.5.2 entity name
  "C x y z n"               chest with n filled slots
Needs enable-rcon=true, rcon.password=hexxit, rcon.port=25575 in run/server/server.properties.

Springs start to flow once their chunk is live, as in 1.5.2. Flowing (not source) water or lava where the
prediction has something else is therefore counted separately and is not a difference.

Usage:  python3 tools/verify_blocks.py expect.txt
"""
import os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from rcon import packet
import socket

SIMPLE = {0: 'air', 1: 'stone', 2: 'grass_block', 3: 'dirt', 4: 'cobblestone', 7: 'bedrock',
          8: 'water[level=0]', 9: 'water[level=0]', 10: 'lava[level=0]', 11: 'lava[level=0]', 12: 'sand', 13: 'gravel',
          14: 'gold_ore', 15: 'iron_ore', 16: 'coal_ore', 21: 'lapis_ore', 24: 'sandstone', 32: 'dead_bush',
          37: 'dandelion', 38: 'poppy', 39: 'brown_mushroom', 40: 'red_mushroom', 48: 'mossy_cobblestone', 49: 'obsidian',
          52: 'spawner', 56: 'diamond_ore', 73: 'redstone_ore', 79: 'ice', 80: 'snow_block', 81: 'cactus', 82: 'clay',
          83: 'sugar_cane', 97: 'infested_stone', 110: 'mycelium', 111: 'lily_pad', 129: 'emerald_ore'}
MODS = {254: 'extrabiomesxl:red_rock', 255: 'extrabiomesxl:cracked_sand'}
WOOD = ['oak', 'spruce', 'birch', 'jungle']
FACING = ['south', 'west', 'north', 'east']
MOBS = {'Skeleton': 'minecraft:skeleton', 'Zombie': 'minecraft:zombie', 'Spider': 'minecraft:spider'}


def flag(value):
    return 'true' if value else 'false'


def block(bid, meta):
    """Block (with the states that matter) a 1.5.2 id and metadata must have become."""
    if bid in MODS:
        return MODS[bid]
    if bid in SIMPLE:
        return 'minecraft:' + SIMPLE[bid]
    if bid == 17:
        kind = WOOD[meta & 3]
        if meta & 12 == 12:
            return f'minecraft:{kind}_wood'
        return f'minecraft:{kind}_log[axis={ {0: "y", 4: "x", 8: "z"}[meta & 12] }]'
    if bid == 18:
        return f'minecraft:{WOOD[meta & 3]}_leaves[persistent=false]'
    if bid == 31:
        return 'minecraft:' + {0: 'dead_bush', 1: 'short_grass', 2: 'fern'}[meta]
    if bid == 44 and meta & 7 == 1:
        return f'minecraft:sandstone_slab[type={"top" if meta & 8 else "bottom"}]'
    if bid == 54:
        return f'minecraft:chest[facing={ {2: "north", 3: "south", 4: "west", 5: "east"}.get(meta, "north") }]'
    if bid == 78:
        return f'minecraft:snow[layers={(meta & 7) + 1}]'
    if bid == 86:
        return f'minecraft:carved_pumpkin[facing={FACING[meta & 3]}]'
    if bid in (99, 100):
        if meta == 10:
            return 'minecraft:mushroom_stem[up=false,down=false,north=true,south=true,east=true,west=true]'
        cap = 'brown' if bid == 99 else 'red'
        top = 1 <= meta <= 9
        return (f'minecraft:{cap}_mushroom_block[up={flag(top)},down=false,north={flag(1 <= meta <= 3)},'
                f'south={flag(7 <= meta <= 9)},west={flag(meta in (1, 4, 7))},east={flag(meta in (3, 6, 9))}]')
    if bid == 106:
        return (f'minecraft:vine[south={flag(meta & 1)},west={flag(meta & 2)},north={flag(meta & 4)},east={flag(meta & 8)}]')
    if bid == 127:
        return f'minecraft:cocoa[facing={FACING[meta & 3]},age={min(meta >> 2, 2)}]'
    raise SystemExit(f'verify_blocks.py does not know block {bid}:{meta}')


def main():
    lines = [l.split() for l in open(sys.argv[1]) if l.strip()]
    chunks = sorted({(int(l[0]) >> 4, int(l[2]) >> 4) for l in lines if l[0][0] not in 'BSC'})
    with socket.create_connection(('127.0.0.1', 25575), timeout=300) as s:
        if packet(s, 1, 3, 'hexxit')[0] == -1:
            raise SystemExit('rcon: wrong password')

        def test(condition):
            return 'passed' in packet(s, 3, 2, 'execute if ' + condition)[1]

        # nothing may grow, spread or melt between generation and the comparison
        packet(s, 2, 2, 'gamerule minecraft:random_tick_speed 0')
        for cx, cz in chunks:
            packet(s, 2, 2, f'forceload add {cx * 16} {cz * 16}')
        ok = bad = flowed = bok = bbad = tok = tbad = shown = 0

        def report(*what):
            nonlocal shown
            if shown < 20:
                shown += 1
                print(*what)

        for l in lines:
            if l[0] == 'B':
                if test(f'biome {l[1]} 70 {l[2]} {l[3]}'):
                    bok += 1
                else:
                    bbad += 1
                    report('BIOME MISMATCH', ' '.join(l))
            elif l[0] == 'S':
                if test(f'block {l[1]} {l[2]} {l[3]} minecraft:spawner{{SpawnData:{{entity:{{id:"{MOBS[l[4]]}"}}}}}}'):
                    tok += 1
                else:
                    tbad += 1
                    report('SPAWNER MISMATCH', ' '.join(l))
            elif l[0] == 'C':
                n = int(l[4])
                pos = f'{l[1]} {l[2]} {l[3]}'
                if (n == 0 or test(f'data block {pos} Items[{n - 1}]')) and not test(f'data block {pos} Items[{n}]'):
                    tok += 1
                else:
                    tbad += 1
                    report('CHEST MISMATCH', ' '.join(l))
            else:
                x, y, z, bid = map(int, l[:4])
                meta = int(l[4]) if len(l) > 4 else 0
                want = block(bid, meta)
                if test(f'block {x} {y} {z} {want}'):
                    ok += 1
                elif bid not in (8, 9, 10, 11) and (
                        test(f'block {x} {y} {z} minecraft:water') and not test(f'block {x} {y} {z} minecraft:water[level=0]')
                        or test(f'block {x} {y} {z} minecraft:lava') and not test(f'block {x} {y} {z} minecraft:lava[level=0]')):
                    flowed += 1
                else:
                    bad += 1
                    report('BLOCK MISMATCH', x, y, z, f'{bid}:{meta}', want)
        for cx, cz in chunks:
            packet(s, 2, 2, f'forceload remove {cx * 16} {cz * 16}')
    print(f'blocks: {ok} match, {bad} differ, {flowed} reached by flowing liquid; biomes: {bok} match, {bbad} differ; '
          f'spawners/chests: {tok} match, {tbad} differ; {len(chunks)} chunks')
    sys.exit(1 if bad or bbad or tbad else 0)


if __name__ == '__main__':
    main()
