#!/usr/bin/env python3
"""Compare a running dev server's world with a prediction file from tools/expect.sh, over RCON.

Input lines:
  "x y z oldBlockId meta"   block check (meta may be missing = 0)
  "B x z namespace:biome"   biome check at y 70
  "S x y z Mob"             mob spawner with that 1.5.2 entity name
  "C x y z n"               chest or dispenser with n filled slots
  "E x y z Kind"            entity of a structure (Villager, Witch, MinecartChest) within 48 blocks of that place
Needs enable-rcon=true, rcon.password=hexxit, rcon.port=25575 in run/server/server.properties.

Springs start to flow once their chunk is live, as in 1.5.2. Flowing (not source) water or lava where the
prediction has something else is therefore counted separately and is not a difference.

Usage:  python3 tools/verify_blocks.py expect.txt
"""
import os, sys, time
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from rcon import packet
import socket

SIMPLE = {0: 'air', 1: 'stone', 2: 'grass_block', 3: 'dirt', 4: 'cobblestone', 7: 'bedrock',
          8: 'water[level=0]', 9: 'water[level=0]', 10: 'lava[level=0]', 11: 'lava[level=0]', 12: 'sand', 13: 'gravel',
          14: 'gold_ore', 15: 'iron_ore', 16: 'coal_ore', 21: 'lapis_ore', 24: 'sandstone', 32: 'dead_bush',
          37: 'dandelion', 38: 'poppy', 39: 'brown_mushroom', 40: 'red_mushroom', 48: 'mossy_cobblestone', 49: 'obsidian',
          30: 'cobweb', 46: 'tnt', 47: 'bookshelf', 52: 'spawner', 58: 'crafting_table', 70: 'stone_pressure_plate',
          72: 'oak_pressure_plate', 85: 'oak_fence', 101: 'iron_bars', 102: 'glass_pane', 56: 'diamond_ore', 73: 'redstone_ore', 79: 'ice', 80: 'snow_block', 81: 'cactus', 82: 'clay',
          83: 'sugar_cane', 110: 'mycelium', 111: 'lily_pad', 129: 'emerald_ore'}
MODS = {254: 'extrabiomesxl:red_rock', 255: 'extrabiomesxl:cracked_sand'}
WOOD = ['oak', 'spruce', 'birch', 'jungle']
FACING = ['south', 'west', 'north', 'east']
MOBS = {'Skeleton': 'minecraft:skeleton', 'Zombie': 'minecraft:zombie', 'Spider': 'minecraft:spider',
        'CaveSpider': 'minecraft:cave_spider', 'Silverfish': 'minecraft:silverfish'}
ENTITIES = {'Villager': 'minecraft:villager', 'Witch': 'minecraft:witch', 'MinecartChest': 'minecraft:chest_minecart'}
WOOL = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan', 'purple', 'blue',
        'brown', 'green', 'red', 'black']
SIDE = {2: 'north', 3: 'south', 4: 'west', 5: 'east'}
STAIRS = {53: 'oak', 67: 'cobblestone', 108: 'brick', 109: 'stone_brick', 114: 'nether_brick', 128: 'sandstone', 134: 'spruce',
          135: 'birch', 136: 'jungle'}
SLABS = ['smooth_stone', 'sandstone', 'petrified_oak', 'cobblestone', 'brick', 'stone_brick', 'nether_brick', 'quartz']
RAILS = ['north_south', 'east_west', 'ascending_east', 'ascending_west', 'ascending_north', 'ascending_south', 'south_east',
         'south_west', 'north_west', 'north_east']
POTS = ['flower_pot', 'potted_poppy', 'potted_dandelion', 'potted_oak_sapling', 'potted_spruce_sapling', 'potted_birch_sapling',
        'potted_jungle_sapling', 'potted_red_mushroom', 'potted_brown_mushroom', 'potted_cactus', 'potted_dead_bush', 'potted_fern']


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
    if bid == 44:
        return f'minecraft:{SLABS[meta & 7]}_slab[type={"top" if meta & 8 else "bottom"}]'
    if bid == 43:
        return f'minecraft:{SLABS[meta & 7]}_slab[type=double]'
    if bid == 126:
        return f'minecraft:{WOOD[meta & 3]}_slab[type={"top" if meta & 8 else "bottom"}]'
    if bid == 5:
        return f'minecraft:{WOOD[meta & 3]}_planks'
    if bid == 35:
        return f'minecraft:{WOOL[meta]}_wool'
    if bid in (23, 29, 33):
        name = {23: 'dispenser', 29: 'sticky_piston', 33: 'piston'}[bid]
        return f'minecraft:{name}[facing={ {0: "down", 1: "up", **SIDE}[meta & 7] }]'
    if bid == 50:
        return 'minecraft:torch' if meta in (0, 5) else f'minecraft:wall_torch[facing={ {1: "east", 2: "west", 3: "south", 4: "north"}[meta] }]'
    if bid in STAIRS:
        return (f'minecraft:{STAIRS[bid]}_stairs[facing={["east", "west", "south", "north"][meta & 3]},'
                f'half={"top" if meta & 4 else "bottom"}]')
    if bid == 55:
        return f'minecraft:redstone_wire[power={meta}]'
    if bid in (59, 141, 142):
        return f'minecraft:{ {59: "wheat", 141: "carrots", 142: "potatoes"}[bid] }[age={meta & 7}]'
    if bid == 60:
        return 'minecraft:farmland'
    if bid in (61, 65):
        return f'minecraft:{"furnace" if bid == 61 else "ladder"}[facing={SIDE.get(meta, "north")}]'
    if bid in (64, 71):
        # facing and hinge are completed from the other half by the port; the half itself is what the metadata says
        name = 'oak_door' if bid == 64 else 'iron_door'
        if meta & 8:
            return f'minecraft:{name}[half=upper,hinge={"right" if meta & 1 else "left"}]'
        return f'minecraft:{name}[half=lower,facing={["east", "south", "west", "north"][meta & 3]},open={flag(meta & 4)}]'
    if bid == 66:
        return f'minecraft:rail[shape={RAILS[meta]}]'
    if bid == 69:
        face, facing = {1: ('wall', 'east'), 2: ('wall', 'west'), 3: ('wall', 'south'), 4: ('wall', 'north'), 5: ('floor', 'north'),
                        6: ('floor', 'east'), 7: ('ceiling', 'north'), 0: ('ceiling', 'east')}[meta & 7]
        return f'minecraft:lever[face={face},facing={facing},powered={flag(meta & 8)}]'
    if bid == 77:
        return f'minecraft:stone_button[face=wall,facing={ {1: "east", 2: "west", 3: "south", 4: "north"}.get(meta & 7, "north") }]'
    if bid == 93:
        return f'minecraft:repeater[facing={FACING[meta & 3]},delay={(meta >> 2) + 1}]'
    if bid == 97:
        return 'minecraft:' + ['infested_stone', 'infested_cobblestone', 'infested_stone_bricks'][meta]
    if bid == 98:
        return 'minecraft:' + ['stone_bricks', 'mossy_stone_bricks', 'cracked_stone_bricks', 'chiseled_stone_bricks'][meta & 3]
    if bid == 118:
        return 'minecraft:cauldron' if meta & 3 == 0 else f'minecraft:water_cauldron[level={meta & 3}]'
    if bid == 120:
        return f'minecraft:end_portal_frame[facing={FACING[meta & 3]},eye={flag(meta & 4)}]'
    if bid == 131:
        return f'minecraft:tripwire_hook[facing={FACING[meta & 3]},attached={flag(meta & 4)}]'
    if bid == 132:
        return f'minecraft:tripwire[attached={flag(meta & 4)}]'
    if bid == 140:
        return 'minecraft:' + POTS[meta]
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
    chunks = sorted({(int(l[0]) >> 4, int(l[2]) >> 4) for l in lines if l[0][0] not in 'BSCE'})
    with socket.create_connection(('127.0.0.1', 25575), timeout=300) as s:
        if packet(s, 1, 3, 'hexxit')[0] == -1:
            raise SystemExit('rcon: wrong password')

        def test(condition):
            return 'passed' in packet(s, 3, 2, 'execute if ' + condition)[1]

        # nothing may grow, spread or melt between generation and the comparison
        packet(s, 2, 2, 'gamerule minecraft:random_tick_speed 0')
        # ... and nothing may flow, fall or walk through a door while the chunks wait for their turn
        packet(s, 2, 2, 'tick freeze')
        # one chunk after the other: many far apart chunks at once make the generator recompute its tiles over and over
        for cx, cz in chunks:
            packet(s, 2, 2, f'forceload add {cx * 16} {cz * 16}')
            while not test(f'loaded {cx * 16} 64 {cz * 16}'):
                time.sleep(0.2)
        ok = bad = flowed = bok = bbad = tok = tbad = shown = 0

        def report(*what):
            nonlocal shown
            if shown < 20:
                shown += 1
                print(*what)

        for l in sorted(lines, key=lambda l: l[0] == 'E'):
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
            elif l[0] == 'E':
                # entities may have walked or rolled a little since they were generated
                x, y, z = (float(v) for v in l[1:4])
                selector = f'entity @e[type={ENTITIES[l[4]]},x={x},y={y},z={z},distance=..48]'
                # entities of a freshly generated chunk appear a moment after its blocks
                if test(selector) or any(time.sleep(1) or test(selector) for _ in range(10)):
                    tok += 1
                else:
                    tbad += 1
                    report('ENTITY MISMATCH', ' '.join(l))
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
                elif bid in (10, 11) and any(test(f'block {x} {y} {z} minecraft:{b}') for b in ('obsidian', 'cobblestone', 'stone')):
                    flowed += 1   # lava that flowing water has reached
                else:
                    bad += 1
                    report('BLOCK MISMATCH', x, y, z, f'{bid}:{meta}', want)
        for cx, cz in chunks:
            packet(s, 2, 2, f'forceload remove {cx * 16} {cz * 16}')
        packet(s, 2, 2, 'tick unfreeze')
    print(f'blocks: {ok} match, {bad} differ, {flowed} reached by flowing liquid; biomes: {bok} match, {bbad} differ; '
          f'spawners/chests/entities: {tok} match, {tbad} differ; {len(chunks)} chunks')
    sys.exit(1 if bad or bbad or tbad else 0)


if __name__ == '__main__':
    main()
