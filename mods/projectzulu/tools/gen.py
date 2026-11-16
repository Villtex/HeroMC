#!/usr/bin/env python3
"""Generates the assets/data JSON of the Project Zulu port (Teil 10: creatures) and original-assets.txt.

Sources of truth:
  - ProjectZuluMobBiomeConfig.cfg of the pack (.reference/z-hexxit-configs): which mobs exist, spawn weights and
    pack sizes per biome, spawn-egg colours, drops. The pack values are the standard, not the mod defaults.
  - tools/texture-map.txt / tools/sound-map.txt: maintained by the entity ports (original texture/ogg -> target).
"""
import json, os, re, sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
REPO = os.path.dirname(os.path.dirname(ROOT))
RES = ROOT + '/src/main/resources'
CFG = os.path.join(REPO, '.reference/z-hexxit-configs/pack-files/config/Project Zulu/ProjectZuluMobBiomeConfig.cfg')
NS = 'projectzulu'

import shutil
for d in ('assets', 'data'):
    shutil.rmtree(os.path.join(RES, d), ignore_errors=True)

def write(path, obj):
    full = os.path.join(RES, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, 'w') as f:
        json.dump(obj, f, indent=2)
        f.write('\n')

def n(x):
    return f'{NS}:{x}'

lang = {}
assets = []  # (source path in ProjectZuluCompletev1.0.3.8.zip, target path in the ported jar)

# ---------------------------------------------------------------- parse the pack config
def parse_cfg(path):
    """-> {section -> {mob -> {key: value}}} for the two top-level sections."""
    tree, stack = {}, [({}, None)]
    with open(path) as f:
        for raw in f:
            line = raw.strip()
            if not line or line.startswith('#'):
                continue
            m = re.match(r'^"?([^"{]*?)"? \{$', line)
            if m:
                child = {}
                stack[-1][0][m.group(1)] = child
                stack.append((child, m.group(1)))
                continue
            if line == '}':
                stack.pop()
                continue
            m = re.match(r'^[IBSD]:"?([^"=]+?)"?=(.*)$', line)
            if m:
                stack[-1][0][m.group(1)] = m.group(2)
    return stack[0][0]

cfg = parse_cfg(CFG)
controls = cfg['mob controls']
biomes_cfg = cfg['mob spawn biome controls']

# config section name -> registration id. Display names as in the 1.5.2 declarations.
MOBS = {
    'armadillo': ('armadillo', 'Armadillo'),
    'sandworm': ('sandworm', 'SandWorm'),
    'lizard': ('lizard', 'Lizard'),
    'mummy pharaoh': ('mummy_pharaoh', 'Mummy Pharaoh'),
    'mummy': ('mummy', 'Mummy'),
    'vulture': ('vulture', 'Vulture'),
    'treeent': ('tree_ent', 'TreeEnt'),
    'mammoth': ('mammoth', 'Mammoth'),
    'fox': ('fox', 'Fox'),
    'boar': ('boar', 'Boar'),
    'mimic': ('mimic', 'Mimic'),
    'alligator': ('alligator', 'Alligator'),
    'frog': ('frog', 'Frog'),
    'penguin': ('penguin', 'Penguin'),
    'beaver': ('beaver', 'Beaver'),
    'black bear': ('black_bear', 'Black Bear'),
    'brown bear': ('brown_bear', 'Brown Bear'),
    'polar bear': ('polar_bear', 'Polar Bear'),
    'ostrich': ('ostrich', 'Ostrich'),
    'rhino': ('rhino', 'Rhino'),
    'rabbit': ('rabbit', 'Rabbit'),
    'red finch': ('red_finch', 'Red Finch'),
    'green finch': ('green_finch', 'Green Finch'),
    'blue finch': ('blue_finch', 'Blue Finch'),
    'gorilla': ('gorilla', 'Gorilla'),
    'giraffe': ('giraffe', 'Giraffe'),
    'elephant': ('elephant', 'Elephant'),
    'horse beige': ('horse_beige', 'Horse Beige'),
    'horse black': ('horse_black', 'Horse Black'),
    'horse brown': ('horse_brown', 'Horse Brown'),
    'horse dark black': ('horse_dark_black', 'Horse Dark Black'),
    'horse dark brown': ('horse_dark_brown', 'Horse Dark Brown'),
    'horse grey': ('horse_grey', 'Horse Grey'),
    'horse white': ('horse_white', 'Horse White'),
    'eagle': ('eagle', 'Eagle'),
    'horn bill': ('horn_bill', 'Horn Bill'),
    'pelican': ('pelican', 'Pelican'),
    'minotaur': ('minotaur', 'Minotaur'),
    'haunted armor': ('haunted_armor', 'Haunted Armor'),
    'centipede': ('centipede', 'Centipede'),
    # horserandom: shouldExist=false in the pack -> not ported
}

# ---------------------------------------------------------------- biomes: config display name -> biome id
W = 'worldgen152:'
X = 'extrabiomesxl:'
BIOME_IDS = {
    'Beach': W + 'beach', 'Desert': W + 'desert', 'DesertHills': W + 'desert_hills',
    'Extreme Hills': W + 'extreme_hills', 'Extreme Hills Edge': W + 'extreme_hills_edge',
    'Forest': W + 'forest', 'ForestHills': W + 'forest_hills', 'FrozenOcean': W + 'frozen_ocean',
    'FrozenRiver': W + 'frozen_river', 'Hell': W + 'hell', 'Ice Mountains': W + 'ice_mountains',
    'Ice Plains': W + 'ice_plains', 'Jungle': W + 'jungle', 'JungleHills': W + 'jungle_hills',
    'MushroomIsland': W + 'mushroom_island', 'MushroomIslandShore': W + 'mushroom_island_shore',
    'Ocean': W + 'ocean', 'Plains': W + 'plains', 'River': W + 'river', 'Swampland': W + 'swampland',
    'Taiga': W + 'taiga', 'TaigaHills': W + 'taiga_hills',
    'Alpine': X + 'alpine', 'Autumn Woods': X + 'autumn_woods', 'Birch Forest': X + 'birch_forest',
    'Extreme Jungle': X + 'extreme_jungle', 'Forested Hills': X + 'forested_hills',
    'Forested Island': X + 'forested_island', 'Glacier': X + 'glacier', 'Green Hills': X + 'green_hills',
    'Green Swamplands': X + 'green_swamp', 'Ice Wasteland': X + 'ice_wasteland', 'Marsh': X + 'marsh',
    'Meadow': X + 'meadow', 'Mini Jungle': X + 'mini_jungle', 'Mountain Ridge': X + 'mountain_ridge',
    'Mountain Taiga': X + 'mountain_taiga', 'Mountainous Desert': X + 'mountain_desert',
    'Pine Forest': X + 'pine_forest', 'Rainforest': X + 'rainforest', 'Redwood Forest': X + 'redwood_forest',
    'Lush Redwoods': X + 'redwood_lush', 'Savanna': X + 'savanna', 'Shrubland': X + 'shrubland',
    'Snowy Forest': X + 'snowy_forest', 'Snowy Rainforest': X + 'snowy_rainforest',
    'Temperate Rainforest': X + 'temperate_rainforest', 'Tundra': X + 'tundra', 'Wasteland': X + 'wasteland',
    'Woodlands': X + 'woodlands',
}
# biomes of mods that are not (yet) ported: Twilight Forest, Dimensional Doors, the 1.5.2 "Sky"
SKIPPED_BIOMES = {
    'Clearing Border', 'Dark Forest', 'Dense Twilight Forest', 'Enchanted Forest', 'Fire Swamp', 'Highlands',
    'Lake Border', 'Limbo', 'Lots of Mushrooms', 'Major Feature', 'Minor Feature', 'Mushrooms',
    'Pocket Dimension', 'Sky', 'Snow Forest', 'Twilight Clearing', 'Twilight Forest', 'Twilight Lake',
    'Twilight Stream', 'Twilight Swamp',
}

# ---------------------------------------------------------------- drops: config item name -> item id
DROP_ITEMS = {
    'item.projectzulublock:scrapmeat': n('scrap_meat'),
    'item.projectzulublock:furpelt': n('fur_pelt'),
    'item.projectzulublock:scaleitem': n('scale_item'),
    'item.projectzulublock:ankh': n('ankh'),
    'item.beefRaw': 'minecraft:beef',
    'item.chickenRaw': 'minecraft:chicken',
    'item.feather': 'minecraft:feather',
    'item.ingotIron': 'minecraft:iron_ingot',
    'tile.log.spruce': 'minecraft:spruce_log',
    'Small Heart': n('small_heart'), 'Large Heart': n('large_heart'), 'Black Lichen': n('black_lichen'),
    'Talon': n('talon'), 'Tusk': n('tusk'), 'Ectoplasm': n('ectoplasm'), 'Gill': n('gill'),
    'Rabbits Feet': n('rabbits_foot'), 'Poison Droplet': n('poison_droplet'), 'Frog Legs': n('frog_legs'),
    'Bark': n('bark'), 'Antennae': n('antennae'),
    # 'tile.projectzulublock:jasper': the jasper block comes with Teil 11; until then the pharaoh drops without it
}
SKIPPED_DROPS = {'tile.projectzulublock:jasper'}

def uniform(a, b):
    """loot number provider (rolls)"""
    return a if a == b else {'type': 'minecraft:uniform', 'min': a, 'max': b}

def uniform_int(a, b):
    """IntProvider (spawner pack size, 26.3 format)"""
    return a if a == b else {'type': 'minecraft:uniform', 'min_inclusive': a, 'max_inclusive': b}

for section, (mob, title) in MOBS.items():
    c = controls[section]
    if c.get(f'{section} shouldExist', 'true') != 'true':
        continue
    lang[f'entity.{NS}.{mob}'] = title

    # ---- spawns per biome (neoforge:add_spawns; weight and pack size per biome entry) ----
    bc = biomes_cfg.get(section, {})
    by_values = {}
    for key, value in bc.items():
        m = re.match(r'Spawn Entry: (.*):-?\d+$', key)
        if not m:
            continue
        name = m.group(1)
        enabled, weight, mn, mx = value.split(':')
        if enabled != 'true':
            continue
        if name in SKIPPED_BIOMES:
            continue
        if name not in BIOME_IDS:
            sys.exit(f'unknown biome in pack config: {name!r} ({section})')
        by_values.setdefault((int(weight), int(mn), int(mx)), set()).add(BIOME_IDS[name])
    for i, ((weight, mn, mx), biome_ids) in enumerate(sorted(by_values.items())):
        suffix = '' if len(by_values) == 1 else f'_{i + 1}'
        write(f'data/{NS}/neoforge/biome_modifier/spawn_{mob}{suffix}.json',
              {'type': 'neoforge:add_spawns', 'biomes': sorted(biome_ids),
               'spawners': [{'type': n(mob), 'count': uniform_int(mn, mx), 'weight': weight}]})

    # ---- drops (CustomMobData.getLoot: min..max rolls, each roll one weighted pick) ----
    drop_min = int(c.get('Items to Drop Min', '0'))
    drop_max = int(c.get('Items to Drop Max', '0'))
    entries = []
    for key in sorted(c):
        m = re.match(r'Item (.+) Quantity:$', key)
        if not m:
            continue
        item = m.group(1)
        weight = int(c.get(f'Item {item} Weight:', '0'))
        quantity = int(c[key])
        if weight <= 0 or item in SKIPPED_DROPS:
            continue
        if item not in DROP_ITEMS:
            sys.exit(f'unknown drop item in pack config: {item!r} ({section})')
        entry = {'type': 'minecraft:item', 'name': DROP_ITEMS[item], 'weight': weight}
        modifier = []
        if quantity != 1:
            modifier.append({'type': 'minecraft:set_count', 'count': quantity})
        # the original rolled min + rand(1 + max - min + looting) items; looting as extra count here
        modifier.append({'type': 'minecraft:enchanted_count_increase', 'enchantment': 'minecraft:looting',
                         'count': {'type': 'minecraft:uniform', 'min': 0.0, 'max': 1.0}})
        entry['modifier'] = modifier
        entries.append(entry)
    if entries and drop_max > 0:
        write(f'data/{NS}/loot_table/entities/{mob}.json',
              {'type': 'minecraft:entity',
               'pools': [{'rolls': uniform(drop_min, drop_max), 'entries': entries}],
               'random_sequence': n(f'entities/{mob}')})

    # ---- spawn egg (only mobs with egg colours in the pack config) ----
    if f'{section} EggColor1' in c:
        egg = f'{mob}_spawn_egg'
        primary = int(c[f'{section} EggColor1'])
        write(f'assets/{NS}/models/item/{egg}.json',
              {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'minecraft:item/egg'}})
        write(f'assets/{NS}/items/{egg}.json',
              {'model': {'type': 'minecraft:model', 'model': n(f'item/{egg}'),
                         'tints': [{'type': 'minecraft:constant', 'value': (0xFF000000 | primary) - (1 << 32)}]}})
        lang[f'item.{NS}.{egg}'] = f'{title} Spawn Egg'

# ---------------------------------------------------------------- the drop items of ZuluItems
ITEMS = [  # id, original texture (textures/items/), english (ItemGenerics/declaration display names)
    ('scrap_meat', 'scrapmeat', 'Scrap Meat'),
    ('fur_pelt', 'furpelt', 'Fur Pelt'),
    ('scale_item', 'scaleitem', 'Scale'),
    ('ankh', 'ankh', 'Ankh'),
    ('poison_droplet', 'poisondroplet', 'Poison Droplet'),
    ('tusk', 'tusk', 'Tusk'),
    ('black_lichen', 'blacklichen', 'Black Lichen'),
    ('antennae', 'antennae', 'Antennae'),
    ('talon', 'talon', 'Talon'),
    ('bark', 'bark', 'Bark'),
    ('small_heart', 'smallheart', 'Small Heart'),
    ('large_heart', 'largeheart', 'Large Heart'),
    ('gill', 'gill', 'Gill'),
    ('ectoplasm', 'ectoplasm', 'Ectoplasm'),
    ('frog_legs', 'froglegs', 'Frog Legs'),
    ('rabbits_foot', 'rabbitsfoot', 'Rabbits Feet'),
]
for item, tex, title in ITEMS:
    write(f'assets/{NS}/models/item/{item}.json',
          {'parent': 'minecraft:item/generated', 'textures': {'layer0': n(f'item/{item}')}})
    write(f'assets/{NS}/items/{item}.json', {'model': {'type': 'minecraft:model', 'model': n(f'item/{item}')}})
    lang[f'item.{NS}.{item}'] = title
    assets.append((f'mods/projectzulublock/textures/items/{tex}.png', f'assets/{NS}/textures/item/{item}.png'))

# ---------------------------------------------------------------- textures and sounds from the port maps
def map_lines(path):
    if not os.path.isfile(path):
        return []
    with open(path) as f:
        return [line.strip() for line in f if line.strip() and not line.strip().startswith('#')]

for line in map_lines(os.path.join(ROOT, 'tools/texture-map.txt')):
    src, dst = [p.strip() for p in line.split('->')]
    assets.append((src, dst))

sounds = {}
for line in map_lines(os.path.join(ROOT, 'tools/sound-map.txt')):
    event, files = [p.strip() for p in line.split(':')]
    oggs = [f.strip() for f in files.split(',')]
    names = []
    for ogg in oggs:
        base = re.sub(r'\.ogg$', '', ogg)
        assets.append((f'mods/projectzulucore/sounds/{ogg}', f'assets/{NS}/sounds/{base}.ogg'))
        names.append(n(base))
    sounds[event] = {'sounds': names}
if sounds:
    write(f'assets/{NS}/sounds.json', dict(sorted(sounds.items())))

# ---------------------------------------------------------------- lang + original-assets.txt
write(f'assets/{NS}/lang/en_us.json', dict(sorted(lang.items())))
with open(ROOT + '/original-assets.txt', 'w') as f:
    f.write('# path in ProjectZuluCompletev1.0.3.8.zip (projectzulu-v1.0.3.8.zip) -> path in the ported jar\n')
    seen = set()
    for s, d in assets:
        if d in seen:
            sys.exit(f'duplicate asset target: {d}')
        seen.add(d)
        f.write(f'{s} -> {d}\n')
print(f'projectzulu: {len(lang)} lang entries, {len(assets)} assets, {len(sounds)} sound events')
