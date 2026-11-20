#!/usr/bin/env python3
"""Generates the assets/data JSON of the Tinkers' Construct port (mods/tconstruct/src/main/resources), the language
files and original-assets.txt. Everything under resources/assets and resources/data is overwritten.
Part 25a: materials, patterns, casts, parts, tools, the tables, their recipes and tags. Part 25b: the layered
tool models, the potion launcher, the chisel recipe, the TreeCapitator axes."""
import json, os, shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = ROOT + '/src/main/resources'
NS = 'tconstruct'
M = 'minecraft:'
for d in ('assets', 'data'):
    shutil.rmtree(os.path.join(RES, d), ignore_errors=True)


def write(path, obj):
    full = os.path.join(RES, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, 'w') as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write('\n')


def n(x):
    return f'{NS}:{x}'


assets = []  # (path in the original jar, path in the ported jar)
en = {}
de = {}


def name(key, english, german):
    en[key] = english
    de[key] = german


T = 'mods/tinker/textures/'


def tex(src, dst):
    """src relative to mods/tinker/textures/, dst relative to assets/tconstruct/textures/ (without .png)."""
    assets.append((T + src, f'assets/{NS}/textures/{dst}.png'))
    return n(dst)


def item_def(name_, model):
    write(f'assets/{NS}/items/{name_}.json', {'model': {'type': 'minecraft:model', 'model': model}})


def flat(name_, layers, handheld=False):
    write(f'assets/{NS}/models/item/{name_}.json', {'parent': 'minecraft:item/handheld' if handheld else 'minecraft:item/generated',
                                                    'textures': {f'layer{i}': l for i, l in enumerate(layers)}})
    item_def(name_, n(f'item/{name_}'))


# ---------------------------------------------------------------- materials (MaterialItem)
MATERIALS = [  # registry name, texture, English, German
    ('paper_stack', 'paperstack', 'Paper Stack', 'Papierstapel'),
    ('slime_crystal', 'slimecrystal', 'Slime Crystal', 'Schleimkristall'),
    ('seared_brick', 'searedbrick', 'Seared Brick', 'Versengter Ziegelstein'),
    ('cobalt_ingot', 'cobaltingot', 'Cobalt Ingot', 'Cobaltbarren'),
    ('ardite_ingot', 'arditeingot', 'Ardite Ingot', 'Arditbarren'),
    ('manyullyn_ingot', 'manyullyningot', 'Manyullyn Ingot', 'Manyullynbarren'),
    ('moss_ball', 'mossball', 'Ball of Moss', 'Moosballen'),
    ('lava_crystal', 'lavacrystal', 'Lava Crystal', 'Lavakristall'),
    ('necrotic_bone', 'necroticbone', 'Necrotic Bone', 'Nekrotischer Knochen'),
    ('copper_ingot', 'copperingot', 'Copper Ingot', 'Kupferbarren'),
    ('tin_ingot', 'tiningot', 'Tin Ingot', 'Zinnbarren'),
    ('aluminum_ingot', 'aluminumingot', 'Aluminum Ingot', 'Aluminiumbarren'),
    ('raw_aluminum', 'aluminumraw', 'Raw Aluminum', 'Rohaluminium'),
    ('bronze_ingot', 'bronzeingot', 'Bronze Ingot', 'Bronzebarren'),
    ('alubrass_ingot', 'alubrassingot', 'Aluminum Brass Ingot', 'Duraluminiumbarren'),
    ('alumite_ingot', 'alumiteingot', 'Alumite Ingot', 'Alumitbarren'),
    ('steel_ingot', 'steelingot', 'Steel Ingot', 'Stahlbarren'),
    ('blue_slime_crystal', 'blueslimecrystal', 'Slime Crystal', 'Blauer Schleimkristall'),
    ('obsidian_ingot', 'obsidianingot', 'Obsidian Ingot', 'Obsidianbarren'),
    None,  # 19: iron nugget = minecraft:iron_nugget
    ('copper_nugget', 'nugget_copper', 'Copper Nugget', 'Kupferklumpen'),
    ('tin_nugget', 'nugget_tin', 'Tin Nugget', 'Zinnklumpen'),
    ('aluminum_nugget', 'nugget_aluminum', 'Aluminum Nugget', 'Aluminiumklumpen'),
    ('false_essence_crystal', 'essencecrystal', 'Fake Essence Crystal', 'Falscher Essenzkristall'),
    ('alubrass_nugget', 'nugget_alubrass', 'Aluminum Brass Nugget', 'Duraluminiumklumpen'),
    ('silky_cloth', 'silkycloth', 'Silky Cloth', 'Seide'),
    ('silky_jewel', 'silkyjewel', 'Silky Jewel', 'Seidiger Edelstein'),
    ('obsidian_nugget', 'nugget_obsidian', 'Obsidian Nugget', 'Obsidianklumpen'),
    ('cobalt_nugget', 'nugget_cobalt', 'Cobalt Nugget', 'Cobaltklumpen'),
    ('ardite_nugget', 'nugget_ardite', 'Ardite Nugget', 'Arditklumpen'),
    ('manyullyn_nugget', 'nugget_manyullyn', 'Manyullyn Nugget', 'Manyullynklumpen'),
    ('bronze_nugget', 'nugget_bronze', 'Bronze Nugget', 'Bronzeklumpen'),
    ('alumite_nugget', 'nugget_alumite', 'Alumite Nugget', 'Alumitklumpen'),
    ('steel_nugget', 'nugget_steel', 'Steel Nugget', 'Stahlklumpen'),
]
for m in MATERIALS:
    if m is None:
        continue
    reg, t, e, g = m
    flat(reg, [tex(f'items/materials/material_{t}.png', f'item/materials/material_{t}')])
    name(f'item.{NS}.{reg}', e, g)
# material_essencecrystal.txt: frames 0..6..1, two ticks each
write(f'assets/{NS}/textures/item/materials/material_essencecrystal.png.mcmeta',
      {'animation': {'frametime': 2, 'frames': [0, 1, 2, 3, 4, 5, 6, 5, 4, 3, 2, 1]}})

for reg, t, e, g in (('blank_pattern', 'blank_pattern', 'Blank Pattern', 'Leere Schablone'), ('blank_cast', 'blank_cast', 'Blank Cast', 'Leere Gussform'),
                     ('gold_blank_cast', 'blank_cast', 'Blank Cast', 'Leere Gussform')):
    flat(reg, [tex(f'items/materials/{t}.png', f'item/materials/{t}')])
    name(f'item.{NS}.{reg}', e, g)

# ---------------------------------------------------------------- patterns and casts (Pattern / MetalPattern)
PATTERNS = [  # registry name, texture of 1.5.2, English (TProxyCommon.patternNames), German
    ('ingot', 'ingot', 'Ingot', 'Barren'),
    ('tool_rod', 'rod', 'Tool Rod', 'Werkzeuggriff'),
    ('pickaxe_head', 'pickaxe', 'Pickaxe Head', 'Spitzhackenkopf'),
    ('shovel_head', 'shovel', 'Shovel Head', 'Schaufelkopf'),
    ('axe_head', 'axe', 'Axe Head', 'Axtkopf'),
    ('sword_blade', 'swordblade', 'Sword Blade', 'Schwertklinge'),
    ('wide_guard', 'largeguard', 'Wide Guard', 'Breiter Schutz'),
    ('hand_guard', 'mediumguard', 'Hand Guard', 'Handschutz'),
    ('crossbar', 'crossbar', 'Crossbar', 'Querstange'),
    ('binding', 'binding', 'Tool Binding', 'Bindung'),
    ('pan', 'frypan', 'Pan', 'Pfanne'),
    ('sign_head', 'sign', 'Board', 'Schild'),
    ('knife_blade', 'knifeblade', 'Knife Blade', 'Messerklinge'),
    ('chisel_head', 'chisel', 'Chisel Head', 'Meißelkopf'),
    ('tough_rod', 'largerod', 'Tough Tool Rod', 'Robuster Griff'),
    ('tough_binding', 'toughbinding', 'Tough Binding', 'Robuste Bindung'),
    ('large_plate', 'largeplate', 'Large Plate', 'Große Platte'),
    ('broad_axe_head', 'broadaxe', 'Broad Axe Head', 'Breitaxtkopf'),
    ('scythe_blade', 'scythe', 'Scythe Head', 'Sensenkopf'),
    ('excavator_head', 'excavator', 'Broad Shovel Head', 'Breiter Schaufelkopf'),
    ('large_sword_blade', 'largeblade', 'Large Blade', 'Große Klinge'),
    ('hammer_head', 'hammerhead', 'Hammer Head', 'Hammerkopf'),
    ('full_guard', 'fullguard', 'Full Guard', 'Voller Schutz'),
    ('bowstring', 'bowstring', 'Bowstring', 'Bogensehne'),
    ('fletching', 'fletching', 'Fletching', 'Befiederung'),
    ('arrowhead', 'arrowhead', 'Arrowhead', 'Pfeilspitze'),
]
for i, (reg, t, e, g) in enumerate(PATTERNS):
    if i > 0:
        flat(reg + '_pattern', [tex(f'items/materials/pattern_{t}.png', f'item/materials/pattern_{t}')])
        name(f'item.{NS}.{reg}_pattern', e + ' Pattern', g + '-Schablone')
    if i not in (23, 24):
        flat(reg + '_cast', [tex(f'items/materials/cast_{t}.png', f'item/materials/cast_{t}')])
        name(f'item.{NS}.{reg}_cast', e + ' Cast', g + '-Gussform')

# ---------------------------------------------------------------- tool materials and parts
TMATS = [  # key, texture, English, German
    ('wood', 'wood', 'Wooden', 'Holz'), ('stone', 'stone', 'Stone', 'Stein'), ('iron', 'iron', 'Iron', 'Eisen'), ('flint', 'flint', 'Flint', 'Feuerstein'),
    ('cactus', 'cactus', 'Cactus', 'Kaktus'), ('bone', 'bone', 'Bone', 'Knochen'), ('obsidian', 'obsidian', 'Obsidian', 'Obsidian'),
    ('netherrack', 'netherrack', 'Netherrack', 'Netherstein'), ('slime', 'slime', 'Slime', 'Schleim'), ('paper', 'paper', 'Paper', 'Papier'),
    ('cobalt', 'cobalt', 'Cobalt', 'Cobalt'), ('ardite', 'ardite', 'Ardite', 'Ardit'), ('manyullyn', 'manyullyn', 'Manyullyn', 'Manyullyn'),
    ('copper', 'copper', 'Copper', 'Kupfer'), ('bronze', 'bronze', 'Bronze', 'Bronze'), ('alumite', 'alumite', 'Alumite', 'Alumit'),
    ('steel', 'steel', 'Steel', 'Stahl'), ('blue_slime', 'blueslime', 'Slime', 'Blauschleim'),
]
MAT_NAMES = {  # material.tconstruct.<key> (part builder, creative tools)
    'wood': ('Wood', 'Holz'), 'blue_slime': ('Blue Slime', 'Blauer Schleim')}
for key, t, e, g in TMATS:
    me, mg = MAT_NAMES.get(key, (e, g))
    name(f'material.{NS}.{key}', me, mg)

PARTS = [  # key, texture suffix, English, German (toolpart.* of 1.5.2)
    ('tool_rod', '_rod', 'Tool Rod', 'Werkzeuggriff'), ('pickaxe_head', '_pickaxe_head', 'Pickaxe Head', 'Spitzhackenkopf'),
    ('shovel_head', '_shovel_head', 'Shovel Head', 'Schaufelkopf'), ('axe_head', '_axe_head', 'Axe Head', 'Axtkopf'),
    ('sword_blade', '_sword_blade', 'Sword Blade', 'Schwertklinge'), ('wide_guard', '_large_guard', 'Wide Guard', 'Breiter Schutz'),
    ('hand_guard', '_medium_guard', 'Hand Guard', 'Handschutz'), ('crossbar', '_crossbar', 'Crossbar', 'Querstange'),
    ('binding', '_binding', 'Binding', 'Bindung'), ('pan', '_frypan_head', 'Pan', 'Pfanne'), ('sign_head', '_battlesign_head', 'Board', 'Schild'),
    ('knife_blade', '_knife_blade', 'Knife Blade', 'Messerklinge'), ('chisel_head', '_chisel_head', 'Chisel Head', 'Meißelkopf'),
    ('tough_rod', '_toughrod', 'Tough Rod', 'Robuster Griff'), ('tough_binding', '_toughbind', 'Tough Binding', 'Robuste Bindung'),
    ('large_plate', '_largeplate', 'Large Plate', 'Große Platte'), ('broad_axe_head', '_lumberaxe_head', 'Broad Axe Head', 'Breitaxtkopf'),
    ('scythe_blade', '_scythe_head', 'Scythe Head', 'Sensenkopf'), ('excavator_head', '_excavator_head', 'Excavator Head', 'Exkavatorkopf'),
    ('large_sword_blade', '_large_sword_blade', 'Large Sword Blade', 'Große Schwertklinge'), ('hammer_head', '_hammer_head', 'Hammer Head', 'Hammerkopf'),
    ('full_guard', '_full_guard', 'Full Guard', 'Voller Schutz'), ('arrowhead', '_arrowhead', 'Arrowhead', 'Pfeilspitze'),
]
SHARD_NAMES = {'stone': ('Stone Shard', 'Steinsplitter'), 'iron': ('Iron Chunk', 'Eisenbrocken'), 'flint': ('Flint Shard', 'Feuersteinsplitter'),
               'cactus': ('Cactus Shard', 'Kaktussplitter'), 'obsidian': ('Obsidian Shard', 'Obsidiansplitter'),
               'netherrack': ('Netherrack Shard', 'Nethersteinsplitter'), 'slime': ('Slime Crystal Fragment', 'Schleimkristallsplitter'),
               'cobalt': ('Cobalt Chunk', 'Cobaltbrocken'), 'ardite': ('Ardite Chunk', 'Arditbrocken'), 'manyullyn': ('Manyullyn Chunk', 'Manyullynbrocken'),
               'copper': ('Copper Chunk', 'Kupferbrocken'), 'bronze': ('Bronze Chunk', 'Bronzebrocken'), 'alumite': ('Alumite Chunk', 'Alumitbrocken'),
               'steel': ('Steel Chunk', 'Stahlbrocken'), 'blue_slime': ('Slime Crystal Fragment', 'Blauer Schleimkristallsplitter')}
for key, t, me, mg in TMATS:
    for part, suffix, pe, pg in PARTS:
        reg = f'{key}_{part}'
        flat(reg, [tex(f'items/parts/{t}{suffix}.png', f'item/parts/{t}{suffix}')])
        name(f'item.{NS}.{reg}', f'{me} {pe}', f'{mg}-{pg[0].lower() + pg[1:] if mg.endswith("-") else pg}')
    if key in SHARD_NAMES:
        reg = f'{key}_shard'
        flat(reg, [tex(f'items/parts/{t}_chunk.png', f'item/parts/{t}_chunk')])
        name(f'item.{NS}.{reg}', *SHARD_NAMES[key])
flat('bowstring', [tex('items/parts/string_bowstring.png', 'item/parts/string_bowstring')])
name(f'item.{NS}.bowstring', 'Bowstring', 'Bogensehne')
for key, t, e, g in (('feather', 'feather', 'Feather Fletching', 'Federne Befiederung'), ('leaf', 'leaf', 'Leaf Fletching', 'Blätterne Befiederung'),
                     ('slime', 'slime', 'Slime Fletching', 'Schleimige Befiederung'),
                     ('blue_slime', 'blueslime', 'Slime Fletching', 'Blaue schleimige Befiederung')):
    flat(f'{key}_fletching', [tex(f'items/parts/{t}_fletching.png', f'item/parts/{t}_fletching')])
    name(f'item.{NS}.{key}_fletching', e, g)

# ---------------------------------------------------------------- tools: layered models (ToolCore.getIcon, BowBase.getIcon1..3)
REF = os.path.join(os.path.dirname(os.path.dirname(ROOT)), '.reference/tconstruct/res/')
if not os.path.isdir(REF + T):
    raise SystemExit('needs the original textures: python3 tools/reference.py --only tconstruct')
EFFECTS = ['diamond', 'emerald', 'redstone', 'piston', 'moss', 'ice', 'lava', 'blaze', 'necrotic', 'electric', 'lapis', 'quartz', 'silk',
           'beheading', 'smite', 'spider', 'reinforced']  # effect id = index (TProxyClient.addRenderMappings)
MAT_TEX = [t for _, t, _, _ in TMATS]  # material id = index
FLETCH_TEX = ['feather', 'leaf', 'slime', 'blueslime']
TOOLS = [  # key, folder, English, German, {part: suffix} (getIconSuffix), broken part, effect suffix
    ('pickaxe', 'pickaxe', 'Pickaxe', 'Spitzhacke', {'handle': '_pickaxe_handle', 'head': '_pickaxe_head', 'accessory': '_pickaxe_accessory'},
     ('head', '_pickaxe_head_broken'), '_pickaxe_effect'),
    ('shovel', 'shovel', 'Shovel', 'Schaufel', {'handle': '_shovel_handle', 'head': '_shovel_head'}, ('head', '_shovel_head_broken'), '_shovel_effect'),
    ('hatchet', 'axe', 'Hatchet', 'Beil', {'handle': '_axe_handle', 'head': '_axe_head'}, ('head', '_axe_head_broken'), '_axe_effect'),
    ('broadsword', 'broadsword', 'Broadsword', 'Breitschwert', {'handle': '_sword_handle', 'head': '_sword_blade', 'accessory': '_sword_accessory'},
     ('head', '_sword_blade_broken'), '_sword_effect'),
    ('longsword', 'longsword', 'Longsword', 'Langschwert',
     {'handle': '_longsword_handle', 'head': '_longsword_blade', 'accessory': '_longsword_accessory'}, ('head', '_longsword_blade_broken'),
     '_longsword_effect'),
    ('rapier', 'rapier', 'Rapier', 'Rapier', {'handle': '_rapier_handle', 'head': '_rapier_blade', 'accessory': '_rapier_accessory'},
     ('head', '_rapier_blade_broken'), '_rapier_effect'),
    ('frypan', 'frypan', 'Frying Pan', 'Bratpfanne', {'handle': '_frypan_handle', 'head': '_frypan_head'}, ('head', '_frypan_head_broken'),
     '_frypan_effect'),
    ('battlesign', 'battlesign', 'Battlesign', 'Kampfschild', {'handle': '_battlesign_handle', 'head': '_battlesign_head'},
     ('head', '_battlesign_head_broken'), '_battlesign_effect'),
    ('mattock', 'mattock', 'Mattock', 'Hacke', {'handle': '_mattock_handle', 'head': '_mattock_head', 'accessory': '_mattock_back'},
     ('head', '_mattock_head_broken'), '_mattock_effect'),
    ('dagger', 'dagger', 'Dagger', 'Dolch', {'handle': '_dagger_handle', 'head': '_dagger_blade', 'accessory': '_dagger_accessory'},
     ('head', '_dagger_blade_broken'), '_dagger_effect'),
    ('cutlass', 'cutlass', 'Cutlass', 'Entermesser', {'handle': '_cutlass_handle', 'head': '_cutlass_blade', 'accessory': '_cutlass_guard'},
     ('head', '_cutlass_blade_broken'), '_cutlass_effect'),
    ('chisel', 'chisel', 'Chisel', 'Meißel', {'handle': '_chisel_handle', 'head': '_chisel_head'}, ('head', '_chisel_head_broken'), '_chisel_effect'),
    ('scythe', 'scythe', 'Scythe', 'Sense',
     {'handle': '_scythe_handle', 'head': '_scythe_head', 'accessory': '_scythe_binding', 'extra': '_scythe_accessory'},
     ('head', '_scythe_head_broken'), '_scythe_effect'),
    ('lumberaxe', 'lumberaxe', 'Lumber Axe', 'Holzfälleraxt',
     {'handle': '_lumberaxe_handle', 'head': '_lumberaxe_head', 'accessory': '_lumberaxe_shield', 'extra': '_lumberaxe_binding'},
     ('head', '_lumberaxe_head_broken'), '_lumberaxe_effect'),
    ('cleaver', 'cleaver', 'Cleaver', 'Hackmesser',
     {'handle': '_cleaver_handle', 'head': '_cleaver_head', 'accessory': '_cleaver_shield', 'extra': '_cleaver_guard'},
     ('head', '_cleaver_head_broken'), '_cleaver_effect'),
    ('excavator', 'excavator', 'Excavator', 'Exkavator',
     {'handle': '_excavator_handle', 'head': '_excavator_head', 'accessory': '_excavator_binding', 'extra': '_excavator_grip'},
     ('head', '_excavator_head_broken'), '_excavator_effect'),
    ('hammer', 'hammer', 'Hammer', 'Hammer', {'handle': '_hammer_handle', 'head': '_hammer_head', 'accessory': '_hammer_front', 'extra': '_hammer_back'},
     ('head', '_hammer_handle_broken'), '_hammer_effect'),
    ('battleaxe', 'battleaxe', 'Battleaxe', 'Streitaxt',
     {'handle': '_battleaxe_handle', 'head': '_battleaxe_fronthead', 'accessory': '_battleaxe_backhead', 'extra': '_battleaxe_binding'},
     ('head', '_battleaxe_fronthead_broken'), '_battleaxe_effect'),
    ('shortbow', 'shortbow', 'Shortbow', 'Kurzbogen', {'handle': '_bowstring', 'head': '_bow_top', 'accessory': '_bow_bottom'},
     ('handle', '_bowstring_broken'), '_bow_effect'),
    ('arrow', 'arrow', 'Arrow', 'Pfeil', {'handle': '_arrow_shaft', 'head': '_arrow_head', 'accessory': '_arrow_fletching'}, None, '_arrow_effect'),
]


def has_tex(path):
    return os.path.isfile(REF + T + path)


def layer_model(folder, file, parent):
    """One layer of a tool: textures/item/<folder>/<file>.png as a flat item model."""
    model = f'item/tool/{folder}/{file}'
    write(f'assets/{NS}/models/{model}.json', {'parent': parent, 'textures': {'layer0': tex(f'items/{folder}/{file}.png', f'item/{folder}/{file}')}})
    return {'type': 'minecraft:model', 'model': n(model)}


def select(part, cases):
    """A layer chosen by the property tconstruct:tool_part (client/ToolPartProperty): material or effect id, "_broken", "_<draw state>"."""
    return {'type': 'minecraft:select', 'property': n('tool_part'), 'part': part,
            'cases': [{'when': w, 'model': m} for w, m in cases], 'fallback': {'type': 'minecraft:empty'}}


for key, folder, e, g, parts, broken, effect in TOOLS:
    parent = 'minecraft:item/generated' if key == 'arrow' else 'minecraft:item/bow' if key == 'shortbow' else 'minecraft:item/handheld'
    states = ['', '_1', '_2', '_3'] if key == 'shortbow' else ['']
    layers = []
    for part in ('handle', 'head', 'accessory', 'extra'):
        if part not in parts:
            continue
        names = MAT_TEX
        if key == 'shortbow' and part == 'handle':
            names = ['string', 'magicfabric']
        if key == 'arrow' and part == 'accessory':
            names = FLETCH_TEX
        cases = []
        for i, t in enumerate(names):
            for st in states:
                file = f'{t}{parts[part]}{st}'
                if has_tex(f'items/{folder}/{file}.png'):
                    cases.append((f'{i}{st}', layer_model(folder, file, parent)))
            if broken and broken[0] == part and has_tex(f'items/{folder}/{t}{broken[1]}.png'):
                cases.append((f'{i}_broken', layer_model(folder, f'{t}{broken[1]}', parent)))
        layers.append(select(part, cases))
    for k in range(1, 7):
        cases = []
        for i, fx in enumerate(EFFECTS):
            for st in states:
                file = f'{fx}{effect}{st}'
                if has_tex(f'items/{folder}/{file}.png'):
                    cases.append((f'{i}{st}', layer_model(folder, file, parent)))
        layers.append(select(f'effect{k}', cases))
    if key == 'shortbow':
        layers.append(select('arrow', [(f'{st[1:]}', layer_model(folder, f'arrow{st}', parent)) for st in states[1:]]))
    write(f'assets/{NS}/items/{key}.json', {'model': {'type': 'minecraft:composite', 'models': layers}})
    name(f'item.{NS}.{key}', e, g)

flat('potion_launcher', [tex('items/potionlauncher.png', 'item/potionlauncher')], handheld=True)
name(f'item.{NS}.potion_launcher', 'Potion Launcher', 'Trankwerfer')
for k, e, g in (('arrow', 'Arrow', 'Pfeil'), ('dagger', 'Dagger', 'Dolch')):
    name(f'entity.{NS}.{k}', e, g)

# the frying pan put down (EquipBlock, FrypanRender: plate, rim, handle sticking out; the iron block texture)
def box(f, t):
    return {'from': f, 'to': t, 'faces': {d: {'texture': '#iron'} for d in ('down', 'up', 'north', 'south', 'west', 'east')}}


write(f'assets/{NS}/models/block/frypan.json', {'parent': 'minecraft:block/block', 'textures': {'iron': M + 'block/iron_block', 'particle': M + 'block/iron_block'},
                                               'elements': [box([0, 0, 0], [16, 2, 16]), box([0, 2, 0], [2, 4, 16]), box([2, 2, 14], [14, 4, 16]),
                                                            box([14, 2, 0], [16, 4, 16]), box([2, 2, 0], [14, 4, 2]), box([16, 0, 7], [32, 2, 9])]})
write(f'assets/{NS}/blockstates/frypan.json', {'variants': {'lit=false': {'model': n('block/frypan')}, 'lit=true': {'model': n('block/frypan')}}})
tex('gui/frypan.png', 'gui/frypan')
name(f'block.{NS}.frypan', 'Frying Pan', 'Bratpfanne')
name(f'container.{NS}.frypan', 'Frying Pan', 'Bratpfanne')

# what the chisel makes (Detailing): a code recipe; TreeCapitator counts hatchet, lumber axe and mattock as axes (pack config)
write(f'data/{NS}/recipe/chisel_detailing.json', {'type': n('chisel_detailing')})
write('data/treecapitator/tags/item/axes.json', {'values': [n('hatchet'), n('lumberaxe'), n('mattock')]})

# ---------------------------------------------------------------- the tables
def block_tex(t):
    return tex(f'blocks/{t}.png', f'block/{t}')


def table_model(model, top, side, bottom):
    """TableRender: the top (12..16) and four legs (4 x 12 high)."""
    faces = lambda: {'up': {'texture': '#top'}, 'down': {'texture': '#bottom'}, 'north': {'texture': '#side'}, 'south': {'texture': '#side'},
                     'west': {'texture': '#side'}, 'east': {'texture': '#side'}}
    boxes = [([0, 12, 0], [16, 16, 16])] + [([x, 0, z], [x + 4, 12, z + 4]) for x in (0, 12) for z in (0, 12)]
    write(f'assets/{NS}/models/block/{model}.json', {'textures': {'top': top, 'side': side, 'bottom': bottom, 'particle': side},
                                                     'elements': [{'from': f, 'to': t, 'faces': faces()} for f, t in boxes]})


def box_model(model, top, side, bottom, y0, y1):
    write(f'assets/{NS}/models/block/{model}.json', {'parent': 'minecraft:block/block', 'textures': {'top': top, 'side': side, 'bottom': bottom,
                                                                                                    'particle': side},
                                                     'elements': [{'from': [0, y0, 0], 'to': [16, y1, 16], 'faces': {
                                                         'up': {'texture': '#top', 'cullface': 'up'} if y1 == 16 else {'texture': '#top'},
                                                         'down': {'texture': '#bottom', 'cullface': 'down'} if y0 == 0 else {'texture': '#bottom'},
                                                         'north': {'texture': '#side', 'uv': [0, 16 - y1, 16, 16 - y0]},
                                                         'south': {'texture': '#side', 'uv': [0, 16 - y1, 16, 16 - y0]},
                                                         'west': {'texture': '#side', 'uv': [0, 16 - y1, 16, 16 - y0]},
                                                         'east': {'texture': '#side', 'uv': [0, 16 - y1, 16, 16 - y0]}}}]})


blocks = []  # (registry name, harvest tool tag)


def simple_block(reg, english, german, tool='axe'):
    write(f'assets/{NS}/blockstates/{reg}.json', {'variants': {'': {'model': n(f'block/{reg}')}}})
    item_def(reg, n(f'block/{reg}'))
    name(f'block.{NS}.{reg}', english, german)
    blocks.append((reg, tool))


table_model('tool_station', block_tex('toolstation_top'), block_tex('toolstation_side'), block_tex('toolstation_bottom'))
simple_block('tool_station', 'Tool Station', 'Werkzeugstation')
WOODS = [('oak', 'Oak', 'Eichen'), ('spruce', 'Spruce', 'Fichten'), ('birch', 'Birch', 'Birken'), ('jungle', 'Jungle', 'Tropenholz')]
for w, we, wg in WOODS:
    table_model(f'{w}_part_builder', block_tex(f'partbuilder_{w}_top'), block_tex(f'partbuilder_{w}_side'), block_tex(f'partbuilder_{w}_bottom'))
    simple_block(f'{w}_part_builder', 'Part Builder', 'Basteltisch')
    table_model(f'{w}_stencil_table', block_tex(f'stenciltable_{w}_top'), block_tex(f'stenciltable_{w}_side'), block_tex(f'stenciltable_{w}_bottom'))
    simple_block(f'{w}_stencil_table', 'Stencil Table', 'Schablonentisch')
box_model('pattern_chest', block_tex('patternchest_top'), block_tex('patternchest_side'), block_tex('patternchest_bottom'), 0, 14)
simple_block('pattern_chest', 'Pattern Chest', 'Musterkiste')
FORGES = [('iron', M + 'block/iron_block'), ('gold', M + 'block/gold_block'), ('diamond', M + 'block/diamond_block'),
          ('emerald', M + 'block/emerald_block')] + [(m, None) for m in ('cobalt', 'ardite', 'manyullyn', 'copper', 'bronze', 'tin', 'aluminum',
                                                                         'alubrass', 'alumite', 'steel')]
for metal, side in FORGES:
    side = side or block_tex(f'compressed_{metal}')
    table_model(f'{metal}_tool_forge', block_tex('toolforge_top'), side, side)
    simple_block(f'{metal}_tool_forge', 'Tool Forge', 'Werkzeugschmiede', tool='pickaxe')
table_model('crafting_station', block_tex('craftingstation_top'), block_tex('craftingstation_side'), block_tex('craftingstation_bottom'))
simple_block('crafting_station', 'Crafting Station', 'Werkbank-Station')
SLABS = [('crafting_slab', 'craftingstation_top', 'craftingstation_slab_side', 'craftingstation_bottom', 'Crafting Slab', 'Werkbankstufe'),
         ('tool_station_slab', 'toolstation_top', 'toolstation_slab_side', 'toolstation_bottom', 'Tool Station Slab', 'Werkzeugstationsstufe'),
         ('part_builder_slab', 'partbuilder_oak_top', 'partbuilder_slab_side', 'partbuilder_oak_bottom', 'Part Builder Slab', 'Basteltischstufe'),
         ('stencil_table_slab', 'stenciltable_oak_top', 'stenciltable_slab_side', 'stenciltable_oak_bottom', 'Stencil Table Slab',
          'Schablonentischstufe'),
         ('pattern_chest_slab', 'patternchest_top', 'patternchest_slab_side', 'patternchest_bottom', 'Pattern Chest Slab', 'Musterkistenstufe'),
         ('tool_forge_slab', 'toolforge_top', 'toolforge_slab_side', 'toolforge_top', 'Tool Forge Slab', 'Werkzeugschmiedestufe')]
for reg, top, side, bottom, e, g in SLABS:
    box_model(reg, block_tex(top), block_tex(side), block_tex(bottom), 0, 8)
    box_model(reg + '_top', block_tex(top), block_tex(side), block_tex(bottom), 8, 16)
    write(f'assets/{NS}/blockstates/{reg}.json', {'variants': {'top=false': {'model': n(f'block/{reg}')}, 'top=true': {'model': n(f'block/{reg}_top')}}})
    item_def(reg, n(f'block/{reg}'))
    name(f'block.{NS}.{reg}', e, g)
    blocks.append((reg, 'axe'))

for reg, tool in blocks:
    write(f'data/{NS}/loot_table/blocks/{reg}.json', {'type': 'minecraft:block', 'pools': [
        {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n(reg)}], 'condition': {'type': 'minecraft:survives_explosion'}}]})
write('data/minecraft/tags/block/mineable/axe.json', {'replace': False, 'values': [n(r) for r, t in blocks if t == 'axe']})
write('data/minecraft/tags/block/mineable/pickaxe.json', {'replace': False, 'values': [n(r) for r, t in blocks if t == 'pickaxe']})

# ---------------------------------------------------------------- what the tools are effective on (getEffectiveMaterials)
write(f'data/{NS}/tags/block/effective/pickaxe.json', {'values': ['#minecraft:mineable/pickaxe']})
write(f'data/{NS}/tags/block/effective/hammer.json', {'values': ['#minecraft:mineable/pickaxe']})
write(f'data/{NS}/tags/block/effective/shovel.json', {'values': ['#minecraft:mineable/shovel']})
write(f'data/{NS}/tags/block/effective/hatchet.json', {'values': ['#minecraft:mineable/axe', '#minecraft:leaves', M + 'vine', M + 'cactus']})
write(f'data/{NS}/tags/block/effective/lumberaxe.json', {'values': ['#minecraft:mineable/axe', M + 'vine', M + 'cactus']})
write(f'data/{NS}/tags/block/effective/mattock_axe.json', {'values': [
    '#minecraft:mineable/axe', M + 'vine', M + 'cactus', '#minecraft:flowers', '#minecraft:saplings', '#minecraft:crops', M + 'short_grass', M + 'fern',
    M + 'dead_bush', M + 'sugar_cane', M + 'lily_pad', M + 'brown_mushroom', M + 'red_mushroom', M + 'nether_wart']})
write(f'data/{NS}/tags/block/effective/mattock_shovel.json', {'values': ['#minecraft:dirt', M + 'grass_block', M + 'clay', M + 'farmland',
                                                                         M + 'dirt_path', M + 'mycelium', M + 'podzol']})
write(f'data/{NS}/tags/block/effective/web.json', {'values': [M + 'cobweb']})
write(f'data/{NS}/tags/block/needs_cobalt_tool.json', {'values': []})
write(f'data/{NS}/tags/block/needs_manyullyn_tool.json', {'values': []})

# ---------------------------------------------------------------- recipes (TContent.addCraftingRecipes)
def result(item_, count=1):
    r = {'id': item_ if ':' in item_ else n(item_)}
    if count != 1:
        r['count'] = count
    return r


def shaped(file, pattern, key, out, count=1, category='misc'):
    write(f'data/{NS}/recipe/{file}.json', {'type': 'minecraft:crafting_shaped', 'category': category, 'pattern': pattern,
                                            'key': key, 'result': result(out, count)})


def shapeless(file, ingredients, out, count=1, category='misc'):
    write(f'data/{NS}/recipe/{file}.json', {'type': 'minecraft:crafting_shapeless', 'category': category, 'ingredients': ingredients,
                                            'result': result(out, count)})


BP = n('blank_pattern')
shaped('blank_pattern', ['ps', 'sp'], {'p': '#minecraft:planks', 's': M + 'stick'}, 'blank_pattern')
shaped('tool_station', ['p', 'w'], {'p': BP, 'w': M + 'crafting_table'}, 'tool_station', category='building')
for i, (w, we, wg) in enumerate(WOODS):
    shaped(f'{w}_part_builder', ['p', 'w'], {'p': BP, 'w': M + f'{w}_log'}, f'{w}_part_builder', category='building')
    shaped(f'{w}_stencil_table', ['p', 'w'], {'p': BP, 'w': M + f'{w}_planks'}, f'{w}_stencil_table', category='building')
# "logWood" / "plankWood": any other log or planks make the oak table
shaped('oak_part_builder_from_logs', ['p', 'w'], {'p': BP, 'w': '#minecraft:logs'}, 'oak_part_builder', category='building')
shaped('oak_stencil_table_from_planks', ['p', 'w'], {'p': BP, 'w': '#minecraft:planks'}, 'oak_stencil_table', category='building')
shaped('pattern_chest', ['p', 'w'], {'p': BP, 'w': M + 'chest'}, 'pattern_chest', category='building')
shaped('crafting_station', ['b'], {'b': M + 'crafting_table'}, 'crafting_station', category='building')
shaped('crafting_slab', ['bbb'], {'b': M + 'crafting_table'}, 'crafting_slab', 6, category='building')
shaped('tool_station_slab', ['b'], {'b': n('tool_station')}, 'tool_station_slab', category='building')
shaped('part_builder_slab', ['b'], {'b': [n(f'{w}_part_builder') for w, _, _ in WOODS]}, 'part_builder_slab', category='building')
shaped('stencil_table_slab', ['b'], {'b': [n(f'{w}_stencil_table') for w, _, _ in WOODS]}, 'stencil_table_slab', category='building')
shaped('pattern_chest_slab', ['b'], {'b': n('pattern_chest')}, 'pattern_chest_slab', category='building')
shaped('tool_forge_slab', ['b'], {'b': [n(f'{m}_tool_forge') for m, _ in FORGES]}, 'tool_forge_slab', category='building')
shapeless('book_from_patterns', [BP, BP, M + 'paper', M + 'paper', M + 'paper', M + 'string'], M + 'book')
shaped('paper_stack', ['pp', 'pp'], {'p': M + 'paper'}, 'paper_stack')
shaped('moss_ball', ['ppp', 'ppp', 'ppp'], {'p': [M + 'mossy_stone_bricks', M + 'mossy_cobblestone']}, 'moss_ball')
shaped('lava_crystal', ['xcx', 'cbc', 'xcx'], {'b': M + 'lava_bucket', 'c': M + 'fire_charge', 'x': M + 'blaze_rod'}, 'lava_crystal')
shaped('lava_crystal_2', ['xcx', 'cbc', 'xcx'], {'b': M + 'lava_bucket', 'x': M + 'fire_charge', 'c': M + 'blaze_rod'}, 'lava_crystal')
shaped('silky_cloth', ['sss', 'sns', 'sss'], {'n': [n('alubrass_nugget'), M + 'gold_nugget'], 's': M + 'string'}, 'silky_cloth')
shaped('silky_jewel', [' c ', 'cec', ' c '], {'c': n('silky_cloth'), 'e': M + 'emerald'}, 'silky_jewel')
# nuggets <-> ingots (the iron nugget is the vanilla one, its recipes are vanilla's)
for nugget, ingot in (('copper_nugget', 'copper_ingot'), ('tin_nugget', 'tin_ingot'), ('aluminum_nugget', 'raw_aluminum'),
                      ('alubrass_nugget', 'alubrass_ingot'), ('obsidian_nugget', 'obsidian_ingot'), ('cobalt_nugget', 'cobalt_ingot'),
                      ('ardite_nugget', 'ardite_ingot'), ('manyullyn_nugget', 'manyullyn_ingot'), ('bronze_nugget', 'bronze_ingot'),
                      ('alumite_nugget', 'alumite_ingot'), ('steel_nugget', 'steel_ingot')):
    shaped(f'{ingot}_from_nuggets', ['mmm', 'mmm', 'mmm'], {'m': n(nugget)}, ingot)
    shaped(f'{nugget}_from_ingot', ['m'], {'m': n(ingot)}, nugget, 9)

# ---------------------------------------------------------------- tags
write('data/c/tags/item/ingots/cobalt.json', {'values': [n('cobalt_ingot')]})
write('data/c/tags/item/ingots/ardite.json', {'values': [n('ardite_ingot')]})
write('data/c/tags/item/ingots/manyullyn.json', {'values': [n('manyullyn_ingot')]})
write('data/c/tags/item/ingots/copper.json', {'replace': False, 'values': [n('copper_ingot')]})
write('data/c/tags/item/ingots/tin.json', {'values': [n('tin_ingot')]})
write('data/c/tags/item/ingots/aluminum.json', {'values': [n('aluminum_ingot'), n('raw_aluminum')]})
write('data/c/tags/item/ingots/bronze.json', {'values': [n('bronze_ingot')]})
write('data/c/tags/item/ingots/steel.json', {'values': [n('steel_ingot')]})

# ---------------------------------------------------------------- sounds
write(f'assets/{NS}/sounds.json', {'little_saw': {'sounds': [n('little_saw')]}, 'frypan_hit': {'sounds': [n('frypan_hit')]}})
assets.append(('sounds/little_saw.ogg', f'assets/{NS}/sounds/little_saw.ogg'))
assets.append(('sounds/frypan_hit.ogg', f'assets/{NS}/sounds/frypan_hit.ogg'))
name(f'subtitles.{NS}.little_saw', 'Tool built', 'Werkzeug gebaut')

# ---------------------------------------------------------------- GUI textures and texts
for t in ('toolstation', 'icons', 'description', 'toolparts', 'patternchest', 'patternchestmini', 'patternshaper'):
    tex(f'gui/{t}.png', f'gui/{t}')
name(f'itemGroup.{NS}.tools', 'TConstruct Tools', 'TConstruct Werkzeuge')
name(f'itemGroup.{NS}.materials', 'TConstruct Materials', 'TConstruct Materialien')
name(f'itemGroup.{NS}.blocks', 'TConstruct Blocks', 'TConstruct Blöcke')
name(f'tool.{NS}.creative_name', '%s %s', '%s-%s')
name(f'tooltip.{NS}.material_cost', 'Material Cost: %s', 'Materialkosten: %s')
name(f'tooltip.{NS}.broken', 'Broken', 'Kaputt')
name(f'tooltip.{NS}.reinforced', 'Reinforced %s', 'Verstärkt %s')
name(f'tooltip.{NS}.unbreakable', 'Unbreakable', 'Unzerbrechlich')
name(f'tooltip.{NS}.ability.stonebound', 'Stonebound', 'Steingebunden')
name(f'tooltip.{NS}.ability.jagged', 'Jagged', 'Gezackt')
name(f'tooltip.{NS}.ability.writable', 'Writable', 'Beschreibbar')
GUI = {
    'durability': ('Durability: %s', 'Haltbarkeit: %s'), 'durability_label': ('Durability:', 'Haltbarkeit:'),
    'attack': ('Attack: %s', 'Angriff: %s'), 'hearts': ('%s Hearts', '%s Herzen'), 'heart': ('%s Heart', '%s Herz'),
    'bonus': ('Bonus: %s', 'Bonus: %s'), 'loss': ('Loss: %s', 'Verlust: %s'), 'draw_speed': ('Draw Speed: %ss', 'Spannzeit: %ss'),
    'arrow_speed': ('Arrow Speed: %sx', 'Pfeiltempo: %sx'), 'base_attack': ('Base Attack:', 'Grundangriff:'),
    'shortbow_attack': ('Shortbow Attack:', 'Angriff mit Kurzbogen:'), 'weight': ('Weight: %s', 'Gewicht: %s'),
    'accuracy': ('Accuracy: %s%%', 'Genauigkeit: %s%%'), 'mining_speeds': ('Mining Speeds:', 'Abbautempo:'),
    'harvest_levels': ('Harvest Levels:', 'Abbaustufen:'), 'mining_speed': ('Mining Speed: %s', 'Abbautempo: %s'),
    'mining_level': ('Mining Level: %s', 'Abbaustufe: %s'), 'usage_speed': ('Usage Speed: %s', 'Arbeitstempo: %s'),
    'modifiers_remaining': ('Modifiers remaining: %s', 'Freie Modifikatoren: %s'), 'modifiers': ('Modifiers:', 'Modifikatoren:'),
    'level.0': ('Stone', 'Stein'), 'level.1': ('Iron', 'Eisen'), 'level.2': ('Redstone', 'Redstone'), 'level.3': ('Obsidian', 'Obsidian'),
    'level.4': ('Cobalt', 'Cobalt'), 'level.5': ('Manyullyn', 'Manyullyn'),
    'part_building': ('Tool Part Building', 'Werkzeugteile bauen'),
    'part_building.help': ('Place a pattern and a material on the left to get started.',
                           'Lege links eine Schablone und ein Material hinein, um zu beginnen.'),
    'base_durability': ('Base Durability: %s', 'Grundhaltbarkeit: %s'), 'handle_modifier': ('Handle Modifier: %sx', 'Griff-Faktor: %sx'),
    'next_pattern': ('Next Pattern', 'Nächste Schablone'), 'previous_pattern': ('Previous Pattern', 'Vorige Schablone'),
}
for k, (e, g) in GUI.items():
    name(f'gui.{NS}.{k}', e, g)
TOOL_TEXTS = {  # TProxyClient.toolNames / toolDescriptions
    'repair': ('Repair and Modification', 'Reparieren und Verändern',
               'The main way to repair or change your tools. Place a tool and a material on the left to get started.',
               'Hier reparierst und veränderst du deine Werkzeuge. Lege links ein Werkzeug und ein Material hinein, um zu beginnen.'),
    'pickaxe': ('Pickaxe', 'Spitzhacke', 'The Pickaxe is a precise mining tool. It is effective on stone and ores.\n\nRequired parts:\n- Pickaxe Head\n- Tool Binding\n- Handle',
                'Die Spitzhacke ist ein genaues Abbauwerkzeug. Sie wirkt auf Stein und Erze.\n\nBenötigte Teile:\n- Spitzhackenkopf\n- Bindung\n- Griff'),
    'shovel': ('Shovel', 'Schaufel', 'The Shovel is a precise digging tool. It is effective on dirt, sand, and snow.\n\nRequired parts:\n- Shovel Head\n- Handle',
               'Die Schaufel ist ein genaues Grabwerkzeug. Sie wirkt auf Erde, Sand und Schnee.\n\nBenötigte Teile:\n- Schaufelkopf\n- Griff'),
    'hatchet': ('Hatchet', 'Beil', 'The Hatchet is a basic chopping tool. It is effective on wood and leaves.\n\nRequired parts:\n- Axe Head\n- Handle',
                'Das Beil ist ein einfaches Hackwerkzeug. Es wirkt auf Holz und Blätter.\n\nBenötigte Teile:\n- Axtkopf\n- Griff'),
    'mattock': ('Mattock', 'Hacke', 'The Cutter Mattock is a versatile farming tool. It is effective on wood, dirt, and plants.\n\nSpecial Ability: Hoe\n\nRequired parts:\n- Axe Head\n- Shovel Head\n- Handle',
                'Die Hacke ist ein vielseitiges Werkzeug für den Acker. Sie wirkt auf Holz, Erde und Pflanzen.\n\nFähigkeit: Pflügen\n\nBenötigte Teile:\n- Axtkopf\n- Schaufelkopf\n- Griff'),
    'broadsword': ('Broadsword', 'Breitschwert', 'The Broadsword is a defensive weapon. Blocking cuts damage in half.\n\nSpecial Ability: Block\nDamage: Moderate\nDurability: High\n\nRequired parts:\n- Sword Blade\n- Wide Guard\n- Handle',
                   'Das Breitschwert ist eine Verteidigungswaffe. Blocken halbiert den Schaden.\n\nFähigkeit: Blocken\nSchaden: Mittel\nHaltbarkeit: Hoch\n\nBenötigte Teile:\n- Schwertklinge\n- Breiter Schutz\n- Griff'),
    'longsword': ('Longsword', 'Langschwert', 'The Longsword is an offensive weapon. It is often used for charging into battle at full speed.\n\nNatural Ability:\n- Charge Boost\nSpecial Ability: Lunge\n\nDamage: Moderate\nDurability: Moderate',
                  'Das Langschwert ist eine Angriffswaffe, oft für den Sturm in die Schlacht.\n\nNatürliche Fähigkeit:\n- Sturmangriff\nFähigkeit: Ausfall\n\nSchaden: Mittel\nHaltbarkeit: Mittel'),
    'rapier': ('Rapier', 'Rapier', 'The Rapier is a special weapon that relies on quick strikes to defeat foes.\n\nNatural Abilities:\n- Armor Pierce\n- Quick Strike\n- Charge Boost\nSpecial Ability:\n- Backpedal\n\nDamage: Low\nDurability: Low',
               'Das Rapier setzt auf schnelle Stiche.\n\nNatürliche Fähigkeiten:\n- Rüstungsdurchschlag\n- Schneller Stich\n- Sturmangriff\nFähigkeit:\n- Rücksprung\n\nSchaden: Gering\nHaltbarkeit: Gering'),
    'dagger': ('Dagger', 'Dolch', 'The Dagger is a short blade that can be thrown.\n\nSpecial Ability:\n- Throw Item\n\nDamage: Low\nDurability: Moderate\n\nRequired parts:\n- Knife Blade\n- Crossbar\n- Handle',
               'Der Dolch ist eine kurze Klinge, die man werfen kann.\n\nFähigkeit:\n- Werfen\n\nSchaden: Gering\nHaltbarkeit: Mittel\n\nBenötigte Teile:\n- Messerklinge\n- Querstange\n- Griff'),
    'frypan': ('Frying Pan', 'Bratpfanne', 'The Frying is a heavy weapon that uses sheer weight to stun foes.\n\nSpecial Ability: Block\nNatural Ability: Heavy\nShift+rClick: Place Frying Pan\nDamage: Low\nDurability: High\n\nRequired parts:\n- Pan\n- Handle',
               'Die Bratpfanne betäubt Gegner mit ihrem Gewicht.\n\nFähigkeit: Blocken\nNatürliche Fähigkeit: Schwer\nShift+Rechtsklick: Pfanne abstellen\nSchaden: Gering\nHaltbarkeit: Hoch\n\nBenötigte Teile:\n- Pfanne\n- Griff'),
    'battlesign': ('Battlesign', 'Kampfschild', 'The Battlesign is an advance in weapon technology worthy of Zombie Pigmen everywhere.\n\nSpecial Ability: Block\nDamage: Low\nDurability: Average\n\nRequired parts:\n- Sign Board\n- Handle',
                   'Das Kampfschild ist ein Fortschritt der Waffentechnik, würdig aller Zombie-Pigmen.\n\nFähigkeit: Blocken\nSchaden: Gering\nHaltbarkeit: Mittel\n\nBenötigte Teile:\n- Schild\n- Griff'),
    'chisel': ('Chisel', 'Meißel', 'The Chisel is a utility tool that carves shapes into blocks.\n\nCrafting Grid:\n- Shape Items\nSpecial Ability: Chisel\nDurability: Average\n\nRequired parts:\n- Chisel Head\n- Handle',
               'Der Meißel schnitzt Formen in Blöcke.\n\nWerkbank:\n- Gegenstände formen\nFähigkeit: Meißeln\nHaltbarkeit: Mittel\n\nBenötigte Teile:\n- Meißelkopf\n- Griff'),
    'shortbow': ('Shortbow', 'Kurzbogen', 'The Shortbow is a ranged weapon. It fires arrows quickly and precisely at its foes.\n\nDraw Speed: Quick\n\nRequired parts:\n- Tool Rod\n- Bowstring\n- Tool Rod',
                 'Der Kurzbogen ist eine Fernwaffe. Er schießt Pfeile schnell und genau.\n\nSpannzeit: Kurz\n\nBenötigte Teile:\n- Werkzeuggriff\n- Bogensehne\n- Werkzeuggriff'),
    'arrow': ('Arrow', 'Pfeil', 'Arrows are projectiles usually fired from bows.\n\nRequired parts:\n- Arrowhead\n- Tool Rod\n- Fletching',
              'Pfeile verschießt man meist mit einem Bogen.\n\nBenötigte Teile:\n- Pfeilspitze\n- Werkzeuggriff\n- Befiederung'),
    'hammer': ('Hammer', 'Hammer', 'The Hammer is a broad mining tool. It harvests blocks in a wide range and is effective against undead.\n\nNatural Abilities:\nArea of Effect\n- (3x3)\n- Smite\n\nDurability: High',
               'Der Hammer baut in großer Breite ab und wirkt gegen Untote.\n\nNatürliche Fähigkeiten:\nFlächenwirkung\n- (3x3)\n- Bann\n\nHaltbarkeit: Hoch'),
    'lumberaxe': ('Lumber Axe', 'Holzfälleraxt', 'The Lumber Axe is a broad chopping tool. It can fell entire trees or gather wood in a wide range.\n\nNatural Abilities:\nArea of Effect\n- Fell Trees\n- (3x3x3)\n\nDurability: Average',
                  'Die Holzfälleraxt fällt ganze Bäume oder schlägt Holz in großer Breite.\n\nNatürliche Fähigkeiten:\nFlächenwirkung\n- Bäume fällen\n- (3x3x3)\n\nHaltbarkeit: Mittel'),
    'excavator': ('Excavator', 'Exkavator', 'The Excavator is a broad digging tool. It harvests soil and snow in a wide range.\n\nNatural Ability:\n- Area of Effect\n- (3x3)\n\nDurability: Average',
                  'Der Exkavator gräbt Erde und Schnee in großer Breite ab.\n\nNatürliche Fähigkeit:\n- Flächenwirkung\n- (3x3)\n\nHaltbarkeit: Mittel'),
    'scythe': ('Scythe', 'Sense', 'The Scythe is a broad reaping tool. It is effective on plants and attacks enemies in a wide range.\n\nNatural Ability:\nArea of Effect\n- (3x3x3)\n\nDurability: Average\nDamage: Low, AoE',
               'Die Sense mäht Pflanzen und trifft Gegner in großer Breite.\n\nNatürliche Fähigkeit:\nFlächenwirkung\n- (3x3x3)\n\nHaltbarkeit: Mittel\nSchaden: Gering, Fläche'),
    'cleaver': ('Cleaver', 'Hackmesser', 'The Cleaver is a heavy defensive weapon. It has powerful strikes, but is difficult to wield.\n\nSpecial Ability: Block\nNatural Ability:\n- Beheading\n\nDamage: High\nDurability: Average',
                'Das Hackmesser ist eine schwere Verteidigungswaffe mit wuchtigen, aber schwer zu führenden Schlägen.\n\nFähigkeit: Blocken\nNatürliche Fähigkeit:\n- Enthaupten\n\nSchaden: Hoch\nHaltbarkeit: Mittel'),
    'battleaxe': ('Battleaxe', 'Streitaxt', 'The Battleaxe is a heavy offensive weapon. It is capable of bringing down small trees and can send foes flying.\n\nSpecial Ability: Block\nNatural Abilities:\n- Knockback\n- Area of Effect\n- (1x9)\n\nDamage: Average\nDurability: Average',
                  'Die Streitaxt ist eine schwere Angriffswaffe. Sie fällt kleine Bäume und schleudert Gegner davon.\n\nFähigkeit: Blocken\nNatürliche Fähigkeiten:\n- Rückstoß\n- Flächenwirkung\n- (1x9)\n\nSchaden: Mittel\nHaltbarkeit: Mittel'),
}
for k, (te, tg, be, bg) in TOOL_TEXTS.items():
    name(f'gui.{NS}.tool.{k}.title', te, tg)
    name(f'gui.{NS}.tool.{k}.body', be, bg)
MODS = {  # modifier.tconstruct.<name>: the tooltips (Tooltip#) and station lines (ModifierTip#) of the modifiers
    'haste': ('Haste', 'Eile'), 'redstone': ('Redstone', 'Redstone'), 'luck': ('Luck', 'Glück'), 'lapis': ('Lapis', 'Lapis'),
    'auto_repair': ('Auto-Repair', 'Selbstreparatur'), 'moss': ('Moss', 'Moos'), 'fiery': ('Fiery', 'Feurig'), 'blaze': ('Blaze', 'Lohe'),
    'auto_smelt': ('Auto-Smelt', 'Selbstschmelzen'), 'lava': ('Lava', 'Lava'), 'life_steal': ('Life Steal', 'Lebensraub'),
    'necrotic': ('Necrotic', 'Nekrotisch'), 'sharpness': ('Sharpness', 'Schärfe'), 'quartz': ('Quartz', 'Quarz'), 'silky': ('Silky', 'Seidig'),
    'silk_touch': ('Silk Touch', 'Behutsamkeit'), 'knockback': ('Knockback', 'Rückstoß'), 'beheading': ('Beheading', 'Enthaupten'),
    'smite': ('Smite', 'Bann'), 'bane_of_arthropods': ('Bane of Arthropods', 'Nemesis der Gliederfüßer'), 'anti_spider': ('Anti-Spider', 'Anti-Spinne'),
    'reinforced': ('Reinforced', 'Verstärkt'), 'diamond': ('Diamond', 'Diamant'), 'emerald': ('Emerald', 'Smaragd'),
    'durability_500': ('Durability +500', 'Haltbarkeit +500'), 'durability_50': ('Durability +50%', 'Haltbarkeit +50%'),
}
for k, (e, g) in MODS.items():
    name(f'modifier.{NS}.{k}', e, g)
for k, (e, g) in {'part_builder': ('Part Builder', 'Teilbauer'), 'tool_assembly': ('Tool Building', 'Werkzeugbau'),
                  'modifiers': ('Tool Modifiers', 'Werkzeug-Modifikatoren'), 'stencil': ('Stencil Table', 'Schablonentisch'),
                  'modifier_note': ('Needs free modifiers on the tool', 'Braucht freie Modifikatoren am Werkzeug'),
                  'leftover': ('Leftover', 'Rest')}.items():
    name(f'jei.{NS}.{k}', e, g)

# ---------------------------------------------------------------- language files, original-assets.txt
for lang, d in (('en_us', en), ('de_de', de)):
    write(f'assets/{NS}/lang/{lang}.json', dict(sorted(d.items())))
with open(ROOT + '/original-assets.txt', 'w') as f:
    f.write('# path in TConstruct_1.5.2_1.4.3.final.jar -> path in the ported jar (generated by tools/gen.py)\n')
    seen = set()
    for src, dst in assets:
        if dst not in seen:
            seen.add(dst)
            f.write(f'{src} -> {dst}\n')
