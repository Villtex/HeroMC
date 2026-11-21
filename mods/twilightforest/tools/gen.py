#!/usr/bin/env python3
"""Writes the data and asset files of the Twilight Forest port (run from anywhere)."""
import json
import os

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src', 'main', 'resources')
NS = 'twilightforest'


def write(path, data):
    full = os.path.join(ROOT, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, 'w') as f:
        json.dump(data, f, indent=2)
        f.write('\n')


def rgb(r, g, b):
    return '#%02x%02x%02x' % (int(r * 255), int(g * 255), int(b * 255))


# WorldProviderTwilightForest: sky 0.168/0.180/0.387, fog from cos(1.5707965) * 2 + 0.5 = 0.5, celestial angle 0.225
# (81 degrees, skylightSubtracted 2), stars at full brightness, no sunrise colours.
bright = 0.5
FOG = rgb(0.7529412 * (bright * 0.94 + 0.06), 1.0 * (bright * 0.94 + 0.06), 0.8470588 * (bright * 0.91 + 0.09))
SKY = rgb(0.16796875, 0.1796875, 0.38671875)

write(f'data/{NS}/dimension_type/twilight_forest.json', {
    'coordinate_scale': 1.0,
    'has_ceiling': False,
    'has_ender_dragon_fight': False,
    'has_skylight': True,
    'has_fixed_time': True,
    'height': 256,
    'logical_height': 256,
    'min_y': 0,
    'infiniburn': '#minecraft:infiniburn_overworld',
    'monster_spawn_block_light_limit': 0,
    'monster_spawn_light_level': {'type': 'minecraft:uniform', 'min_inclusive': 0, 'max_inclusive': 7},
    'ambient_light': 0.0,
    'attributes': {
        'minecraft:gameplay/bed_rule': {'can_set_spawn': 'always', 'can_sleep': 'always'},
        'minecraft:gameplay/respawn_anchor_works': False,
        'minecraft:gameplay/sky_light_level': 13.0,
        'minecraft:visual/sky_color': SKY,
        'minecraft:visual/fog_color': FOG,
        'minecraft:visual/cloud_color': '#ccffffff',
        'minecraft:visual/cloud_height': 128.0,
        'minecraft:visual/sun_angle': 81.0,
        'minecraft:visual/moon_angle': 261.0,
        'minecraft:visual/star_angle': 81.0,
        'minecraft:visual/star_brightness': 1.0,
        'minecraft:visual/sky_light_factor': 0.82,
    },
})

write(f'data/{NS}/dimension/twilight_forest.json', {
    'type': f'{NS}:twilight_forest',
    'generator': {'type': f'{NS}:twilight_forest', 'biome_source': {'type': f'{NS}:twilight_forest'}},
})

# TFBiomeBase and its subclasses: temperature, rainfall, water colour (waterColorMultiplier)
BIOMES = {
    'twilight_lake': (0.66, 1.0, None), 'twilight_forest': (0.5, 0.5, None), 'dense_twilight_forest': (0.7, 0.8, None),
    'highlands': (0.5, 0.3, None), 'mushrooms': (0.5, 0.8, None), 'twilight_swamp': (0.8, 0.9, 14745518),
    'twilight_stream': (0.5, 1.0, None), 'snowy_forest': (0.125, 0.9, None), 'glacier': (0.0, 0.1, None),
    'twilight_clearing': (0.8, 0.4, None), 'clearing_border': (0.5, 0.5, None), 'lake_border': (0.5, 0.5, None),
    'deep_mushrooms': (0.8, 1.0, None), 'major_feature': (0.5, 0.5, None), 'minor_feature': (0.5, 0.5, None),
    'dark_forest': (0.7, 0.8, None), 'enchanted_forest': (0.5, 0.5, None), 'fire_swamp': (1.0, 0.4, 7089196),
}
EMPTY_SPAWNS = {k: [] for k in ['ambient', 'axolotls', 'creature', 'misc', 'monster', 'underground_water_creature',
                                'water_ambient', 'water_creature']}
for name, (temp, rain, water) in BIOMES.items():
    write(f'data/{NS}/worldgen/biome/{name}.json', {
        # the biome lists of TFBiomeBase come with Teil 24c
        'attributes': {
            'minecraft:gameplay/natural_mob_spawns': {'argument': {'spawn_costs': {}, 'spawns_by_category': EMPTY_SPAWNS},
                                                       'modifier': 'overlay'},
        },
        'carvers': [],
        'downfall': rain,
        'effects': {'water_color': '#%06x' % water if water is not None else '#3f76e4'},
        'features': [],
        'has_precipitation': True,
        'temperature': temp,
    })

# blocks
CUBES = {'hedge': 'hedge', 'darkwood_leaves': 'darkwood_leaves'}
for name, texture in CUBES.items():
    write(f'assets/{NS}/blockstates/{name}.json', {'variants': {'': {'model': f'{NS}:block/{name}'}}})
    write(f'assets/{NS}/models/block/{name}.json', {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'{NS}:block/{texture}'}})
    write(f'assets/{NS}/items/{name}.json', {'model': {'type': 'minecraft:model', 'model': f'{NS}:block/{name}'}})

# BlockTFPortal: the nether portal texture on a slab of 12/16, drawn on the top face and the four sides
write(f'assets/{NS}/blockstates/twilight_portal.json', {'variants': {'': {'model': f'{NS}:block/twilight_portal'}}})
write(f'assets/{NS}/models/block/twilight_portal.json', {
    'textures': {'particle': 'minecraft:block/nether_portal', 'portal': 'minecraft:block/nether_portal'},
    'elements': [{
        'from': [0, 0, 0], 'to': [16, 12, 16],
        'faces': {
            'up': {'texture': '#portal', 'cullface': 'up'},
            'north': {'uv': [0, 4, 16, 16], 'texture': '#portal', 'cullface': 'north'},
            'south': {'uv': [0, 4, 16, 16], 'texture': '#portal', 'cullface': 'south'},
            'west': {'uv': [0, 4, 16, 16], 'texture': '#portal', 'cullface': 'west'},
            'east': {'uv': [0, 4, 16, 16], 'texture': '#portal', 'cullface': 'east'},
        },
    }],
})

LANG_EN = {
    f'block.{NS}.twilight_portal': 'Twilight Portal',
    f'block.{NS}.hedge': 'Hedge',
    f'block.{NS}.darkwood_leaves': 'Darkwood Leaves',
    f'biome.{NS}.twilight_lake': 'Twilight Lake', f'biome.{NS}.twilight_forest': 'Twilight Forest',
    f'biome.{NS}.dense_twilight_forest': 'Dense Twilight Forest', f'biome.{NS}.highlands': 'Highlands',
    f'biome.{NS}.mushrooms': 'Mushrooms', f'biome.{NS}.twilight_swamp': 'Twilight Swamp',
    f'biome.{NS}.twilight_stream': 'Twilight Stream', f'biome.{NS}.snowy_forest': 'Snowy Forest',
    f'biome.{NS}.glacier': 'Glacier', f'biome.{NS}.twilight_clearing': 'Twilight Clearing',
    f'biome.{NS}.clearing_border': 'Clearing Border', f'biome.{NS}.lake_border': 'Lake Border',
    f'biome.{NS}.deep_mushrooms': 'Lots of Mushrooms', f'biome.{NS}.major_feature': 'Major Feature',
    f'biome.{NS}.minor_feature': 'Minor Feature', f'biome.{NS}.dark_forest': 'Dark Forest',
    f'biome.{NS}.enchanted_forest': 'Enchanted Forest', f'biome.{NS}.fire_swamp': 'Fire Swamp',
}
LANG_DE = dict(LANG_EN)
LANG_DE.update({
    f'block.{NS}.twilight_portal': 'Zwielicht-Portal', f'block.{NS}.hedge': 'Hecke', f'block.{NS}.darkwood_leaves': 'Dunkelholzlaub',
    f'biome.{NS}.twilight_lake': 'Zwielichtsee', f'biome.{NS}.twilight_forest': 'Zwielichtwald',
    f'biome.{NS}.dense_twilight_forest': 'Dichter Zwielichtwald', f'biome.{NS}.highlands': 'Hochland',
    f'biome.{NS}.mushrooms': 'Pilzwald', f'biome.{NS}.twilight_swamp': 'Zwielichtsumpf', f'biome.{NS}.twilight_stream': 'Zwielichtbach',
    f'biome.{NS}.snowy_forest': 'Verschneiter Wald', f'biome.{NS}.glacier': 'Gletscher', f'biome.{NS}.twilight_clearing': 'Zwielichtlichtung',
    f'biome.{NS}.clearing_border': 'Lichtungsrand', f'biome.{NS}.lake_border': 'Seeufer', f'biome.{NS}.deep_mushrooms': 'Viele Pilze',
    f'biome.{NS}.major_feature': 'Großes Bauwerk', f'biome.{NS}.minor_feature': 'Kleines Bauwerk', f'biome.{NS}.dark_forest': 'Dunkelwald',
    f'biome.{NS}.enchanted_forest': 'Verzauberter Wald', f'biome.{NS}.fire_swamp': 'Feuersumpf',
})

# ================================================================= Teil 24b: blocks and items of the game side
ASSETS = []  # (path in the original zip, path in the jar)
LANG_EN_B = {}
LANG_DE_B = {}
TAGS = {}
FLAMMABLE = {}
RECIPES = {}


def n(x):
    return f'{NS}:{x}'


def tex(src, dst, kind='block'):
    folder = 'blocks' if kind == 'block' else 'items'
    ASSETS.append((f'mods/twilightforest/textures/{folder}/{src}.png', f'assets/{NS}/textures/{kind}/{dst}.png'))


def names(key, en, de):
    LANG_EN_B[key] = en
    LANG_DE_B[key] = de


def block_name(b, en, de):
    names(f'block.{NS}.{b}', en, de)


def item_name(i, en, de):
    names(f'item.{NS}.{i}', en, de)


def tag(reg, t, *values):
    TAGS.setdefault((reg, t), []).extend(values)


def btag(t, *v):
    tag('block', t, *[n(x) for x in v])


def bitag(t, *v):
    tag('block', t, *[n(x) for x in v])
    tag('item', t, *[n(x) for x in v])


def flam(block, ignite, burn):
    FLAMMABLE[n(block)] = {'ignite_odds': ignite, 'burn_odds': burn}


def model(name, obj):
    write(f'assets/{NS}/models/block/{name}.json', obj)


def state(name, variants):
    write(f'assets/{NS}/blockstates/{name}.json', {'variants': variants})


def simple_state(name, model_ref=None):
    state(name, {'': {'model': model_ref or n(f'block/{name}')}})


def item_def(name, model_ref, tint=None):
    m = {'type': 'minecraft:model', 'model': model_ref}
    if tint is not None:
        m['tints'] = [{'type': 'minecraft:constant', 'value': tint - (1 << 32) if tint >= (1 << 31) else tint}]
    write(f'assets/{NS}/items/{name}.json', {'model': m})


def generated_item(name, texture, tint=None):
    write(f'assets/{NS}/models/item/{name}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': texture}})
    item_def(name, n(f'item/{name}'), tint)


def argb(rgb):
    return 0xFF000000 | rgb


def loot(name, pools):
    write(f'data/{NS}/loot_table/blocks/{name}.json', {'type': 'minecraft:block', 'pools': pools, 'random_sequence': f'{NS}:blocks/{name}'})


def self_drop(name, item=None):
    loot(name, [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': item or n(name)}], 'condition': {'type': 'minecraft:survives_explosion'}}])


def chance(c):
    return {'type': 'minecraft:random_chance', 'chance': c}


SHEARS = 'minecraft:tool/can_shear'


def shaped(name, pattern, key, result, count=1, category='misc'):
    RECIPES[name] = {'type': 'minecraft:crafting_shaped', 'category': category, 'key': key, 'pattern': pattern,
                     'result': {'count': count, 'id': result}}


def shapeless(name, ingredients, result, count=1, category='misc'):
    RECIPES[name] = {'type': 'minecraft:crafting_shapeless', 'category': category, 'ingredients': ingredients,
                     'result': {'count': count, 'id': result}}


DIRS = ['down', 'up', 'north', 'south', 'west', 'east']


def cube(textures, tint=False, rotations=None):
    """A full cube with one texture variable per face (1.5.2 side order: down, up, north, south, west, east)."""
    faces = {}
    for i, d in enumerate(DIRS):
        f = {'texture': f'#{d}', 'cullface': d}
        if tint:
            f['tintindex'] = 0
        if rotations and rotations.get(d):
            f['rotation'] = rotations[d]
        faces[d] = f
    t = {d: textures[i] for i, d in enumerate(DIRS)}
    t['particle'] = textures[2]
    return {'parent': 'minecraft:block/block', 'textures': t, 'elements': [{'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': faces}]}


def column_state(name, horizontal=True):
    if horizontal:
        state(name, {'axis=x': {'model': n(f'block/{name}_horizontal'), 'x': 90, 'y': 90},
                     'axis=y': {'model': n(f'block/{name}')},
                     'axis=z': {'model': n(f'block/{name}_horizontal'), 'x': 90}})
    else:
        state(name, {f'axis={a}': {'model': n(f'block/{name}')} for a in 'xyz'})


def log(name, side, end):
    model(name, {'parent': 'minecraft:block/cube_column', 'textures': {'end': side if end is None else end, 'side': side}})
    if end is not None:
        model(name + '_horizontal', {'parent': 'minecraft:block/cube_column_horizontal', 'textures': {'end': end, 'side': side}})
    column_state(name, end is not None)
    item_def(name, n(f'block/{name}'))


# ---------------------------------------------------------------- logs (BlockTFLog)
WOODS = [  # id prefix, original texture prefix, en log, de log, plank of the recipe
    ('twilight_oak', 'oak', 'Twilight Oak', 'Zwielichteichen', 'minecraft:oak_planks'),
    ('canopy', 'canopy', 'Canopy Tree', 'Blätterdachbaum', 'minecraft:spruce_planks'),
    ('mangrove', 'mangrove', 'Mangrove', 'Mangroven', 'minecraft:birch_planks'),
    ('darkwood', 'darkwood', 'Darkwood', 'Dunkelholz', 'minecraft:spruce_planks'),
]
for w, src, en, de, planks in WOODS:
    tex(f'{src}_side', f'{w}_log')
    tex(f'{src}_top', f'{w}_log_top')
    log(f'{w}_log', n(f'block/{w}_log'), n(f'block/{w}_log_top'))
    log(f'{w}_wood', n(f'block/{w}_log'), None)
    block_name(f'{w}_log', f'{en} Log', f'{de}stamm' if not de.endswith('holz') else f'{de}stamm')
    block_name(f'{w}_wood', f'{en} Wood', f'{de}holz' if not de.endswith('holz') else de)
    # BlockLog.damageDropped = meta & 3: the all-bark block drops the log
    self_drop(f'{w}_log')
    self_drop(f'{w}_wood', n(f'{w}_log'))
    bitag('minecraft:logs', f'{w}_log', f'{w}_wood')
    bitag('minecraft:logs_that_burn', f'{w}_log', f'{w}_wood')
    btag('minecraft:mineable/axe', f'{w}_log', f'{w}_wood')
    flam(f'{w}_log', 5, 5)
    flam(f'{w}_wood', 5, 5)
    shaped(f'{planks.split(":")[1]}_from_{w}_log', ['w'], {'w': n(f'{w}_log')}, planks, 4, 'building')
# fix the German names (Dunkelholz has no "holz" twice)
LANG_DE_B[f'block.{NS}.twilight_oak_log'] = 'Zwielichteichenstamm'
LANG_DE_B[f'block.{NS}.twilight_oak_wood'] = 'Zwielichteichenholz'
LANG_DE_B[f'block.{NS}.canopy_log'] = 'Blätterdachbaumstamm'
LANG_DE_B[f'block.{NS}.canopy_wood'] = 'Blätterdachbaumholz'
LANG_DE_B[f'block.{NS}.mangrove_log'] = 'Mangrovenstamm'
LANG_DE_B[f'block.{NS}.mangrove_wood'] = 'Mangrovenholz'
LANG_DE_B[f'block.{NS}.darkwood_log'] = 'Dunkelholzstamm'
LANG_DE_B[f'block.{NS}.darkwood_wood'] = 'Dunkelholz'

# ---------------------------------------------------------------- leaves (BlockTFLeaves: the vanilla leaf textures of meta 0/1/2, rainbow = 0)
SAPLING_CHANCE = 1 / 40
LEAVES = [  # id, vanilla texture, inventory colour, sapling, chance, en, de
    ('twilight_oak_leaves', 'oak_leaves', 0x48B518, 'twilight_oak_sapling', 1 / 40, 'Twilight Oak Leaves', 'Zwielichteichenlaub'),
    ('canopy_leaves', 'spruce_leaves', 6330464, 'canopy_sapling', 1 / 40, 'Canopy Tree Leaves', 'Blätterdachbaumlaub'),
    ('mangrove_leaves', 'birch_leaves', 8431445, 'mangrove_sapling', 1 / 20, 'Mangrove Leaves', 'Mangrovenlaub'),
    ('rainbow_oak_leaves', 'oak_leaves', 0x48B518, 'rainbow_oak_sapling', 1 / 40, 'Rainbow Oak Leaves', 'Regenbogeneichenlaub'),
]


def leaves_loot(b, pools_extra, drop, drop_chance):
    loot(b, [{'rolls': 1, 'entries': [{'type': 'minecraft:alternatives', 'children': [
        {'type': 'minecraft:item', 'name': n(b), 'condition': SHEARS},
        {'type': 'minecraft:item', 'name': drop, 'condition': {'type': 'minecraft:all_of', 'terms': [
            {'type': 'minecraft:survives_explosion'}, chance(drop_chance)]}}]}]}] + pools_extra)


for b, van_tex, color, sapling, c, en, de in LEAVES:
    model(b, {'parent': 'minecraft:block/leaves', 'textures': {'all': f'minecraft:block/{van_tex}'}})
    simple_state(b)
    item_def(b, n(f'block/{b}'), argb(color))
    leaves_loot(b, [], n(sapling), c)
    block_name(b, en, de)
    bitag('minecraft:leaves', b)
    btag('minecraft:mineable/hoe', b)
    flam(b, 30, 60)

# darkwood leaves (hedge metadata 1): a darkwood sapling 1 in 40, nothing else; flammability 1, no spreading
loot('darkwood_leaves', [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n('darkwood_sapling')}],
                          'condition': {'type': 'minecraft:all_of', 'terms': [{'type': 'minecraft:survives_explosion'}, chance(1 / 40)]}}])
flam('darkwood_leaves', 0, 1)

# ---------------------------------------------------------------- saplings (BlockTFSapling)
SAPLINGS = [  # id, original texture, en, de
    ('twilight_oak_sapling', 'sapling_oak', 'Sickly Twilight Oak Sapling', 'Kränklicher Zwielichteichensetzling'),
    ('canopy_sapling', 'sapling_canopy', 'Canopy Tree Sapling', 'Blätterdachbaumsetzling'),
    ('mangrove_sapling', 'sapling_mangrove', 'Twilight Mangrove Sapling', 'Zwielicht-Mangrovensetzling'),
    ('darkwood_sapling', 'sapling_darkwood', 'Darkwood Sapling', 'Dunkelholzsetzling'),
    ('hollow_oak_sapling', 'sapling_hollow_oak', 'Robust Twilight Oak Sapling', 'Kräftiger Zwielichteichensetzling'),
    ('time_sapling', 'sapling_time', 'Tree of Time Sapling', 'Setzling des Baums der Zeit'),
    ('transformation_sapling', 'sapling_transformation', 'Tree of Transformation Sapling', 'Setzling des Baums der Verwandlung'),
    ('mining_sapling', 'sapling_mining', "Miner's Tree Sapling", 'Setzling des Bergarbeiterbaums'),
    ('sorting_sapling', 'sapling_sorting', 'Sorting Tree Sapling', 'Setzling des Sortierbaums'),
    ('rainbow_oak_sapling', 'sapling_rainboak', 'Rainbow Oak Sapling', 'Regenbogeneichensetzling'),
]
for b, src, en, de in SAPLINGS:
    tex(src, b)
    model(b, {'parent': 'minecraft:block/cross', 'render_type': 'minecraft:cutout', 'textures': {'cross': n(f'block/{b}')}})
    simple_state(b)
    generated_item(b, n(f'block/{b}'))
    self_drop(b)
    block_name(b, en, de)
    bitag('minecraft:saplings', b)

# ---------------------------------------------------------------- plants (BlockTFPlant, RenderBlockTFPlants)
tex('mosspatch', 'moss_patch')
tex('mayapple', 'mayapple')
tex('mayapple_side', 'mayapple_side')
tex('cloverpatch', 'clover_patch')
tex('fiddlehead', 'fiddlehead')
tex('mushgloom', 'mushgloom')
tex('torchberry', 'torchberry_plant')
tex('rootstrand', 'root_strand')


def box(frm, to, texture, faces=DIRS, tint=False, cull=False):
    fs = {}
    for d in faces:
        f = {'texture': texture}
        if tint:
            f['tintindex'] = 0
        fs[d] = f
    return {'from': frm, 'to': to, 'faces': fs}


# moss: a carpet of 1/16 (the random insets and side tufts of RenderBlockTFPlants are left out)
model('moss_patch', {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout', 'textures': {'particle': n('block/moss_patch'), 'm': n('block/moss_patch')},
                     'elements': [box([1, 0, 1], [15, 1, 15], '#m')]})
# mayapple: the leaf at 6/16, a stem of 1 x 6 pixels with mayapple_side (both grass-coloured)
model('mayapple', {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout',
                   'textures': {'particle': n('block/mayapple'), 'top': n('block/mayapple'), 'stem': n('block/mayapple_side')},
                   'elements': [box([4, 6, 4], [13, 6, 13], '#top', ['up', 'down'], True),
                                box([8, 0, 8], [9, 6, 9], '#stem', ['north', 'south', 'west', 'east'], True)]})
# clover: a top at 2/16 and a body below it 1/16 smaller on every side
model('clover_patch', {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout',
                       'textures': {'particle': n('block/clover_patch'), 'c': n('block/clover_patch')},
                       'elements': [box([0, 2, 0], [16, 2, 16], '#c', ['up', 'down'], True),
                                    box([1, 0, 1], [15, 1.84, 15], '#c', DIRS, True)]})
model('fiddlehead', {'parent': 'minecraft:block/tinted_cross', 'render_type': 'minecraft:cutout', 'textures': {'cross': n('block/fiddlehead')}})
model('mushgloom', {'parent': 'minecraft:block/cross', 'render_type': 'minecraft:cutout', 'textures': {'cross': n('block/mushgloom')}})
model('torchberry_plant', {'parent': 'minecraft:block/cross', 'render_type': 'minecraft:cutout', 'textures': {'cross': n('block/torchberry_plant')}})
model('root_strand', {'parent': 'minecraft:block/crop', 'render_type': 'minecraft:cutout', 'textures': {'crop': n('block/root_strand')}})
PLANTS = [  # id, grass coloured, en, de
    ('moss_patch', False, 'Moss Patch', 'Moosfleck'),
    ('mayapple', True, 'Mayapple', 'Maiapfel'),
    ('clover_patch', True, 'Clover Patch', 'Kleefleck'),
    ('fiddlehead', True, 'Fiddlehead', 'Farnspross'),
    ('mushgloom', False, 'Mushgloom', 'Pilzglühen'),
    ('torchberry_plant', False, 'Torchberry Plant', 'Fackelbeerenpflanze'),
    ('root_strand', False, 'Root Strands', 'Wurzelstrang'),
]
for b, grass, en, de in PLANTS:
    simple_state(b)
    generated_item(b, n(f'block/{b}'), argb(0x48B518) if grass else None)
    block_name(b, en, de)
# getBlockDropped: moss, mayapple, clover, root strand nothing; fiddlehead and mushgloom themselves; torchberries the berries.
# IShearable: shears drop the plant itself.
for b in ('moss_patch', 'mayapple', 'clover_patch', 'root_strand'):
    loot(b, [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n(b)}], 'condition': SHEARS}])
for b in ('fiddlehead', 'mushgloom'):
    loot(b, [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n(b)}]}])
loot('torchberry_plant', [{'rolls': 1, 'entries': [{'type': 'minecraft:alternatives', 'children': [
    {'type': 'minecraft:item', 'name': n('torchberry_plant'), 'condition': SHEARS},
    {'type': 'minecraft:item', 'name': n('torchberries')}]}]}])
shapeless('stick_from_root_strand', [n('root_strand')], 'minecraft:stick')
shaped('torch_from_torchberries', ['B', 'S'], {'B': n('torchberries'), 'S': 'minecraft:stick'}, 'minecraft:torch', 5)

# ---------------------------------------------------------------- roots (BlockTFRoots)
tex('rootblock', 'root')
tex('oreroots', 'liveroot_block')
for b, en, de in (('root', 'Roots', 'Wurzel'), ('liveroot_block', 'Liveroots', 'Lebenswurzelblock')):
    model(b, {'parent': 'minecraft:block/cube_all', 'textures': {'all': n(f'block/{b}')}})
    simple_state(b)
    item_def(b, n(f'block/{b}'))
    block_name(b, en, de)
    btag('minecraft:mineable/axe', b)
loot('root', [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'minecraft:stick', 'modifier': [
    {'type': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': 3, 'max': 4}}]}],
    'condition': {'type': 'minecraft:survives_explosion'}}])
self_drop('liveroot_block', n('liveroot'))

# ---------------------------------------------------------------- critters (BlockTFCritter): drawn by the block entity renderer
for b, src, en, de in (('firefly', 'TFFirefly', 'Firefly', 'Glühwürmchen'), ('cicada', 'TFCicada', 'Cicada', 'Zikade'),
                       ('moonworm', 'TFMoonworm', 'Moonworm', 'Mondwurm')):
    tex(src, b)
    model(b, {'textures': {'particle': n(f'block/{b}')}})
    state(b, {f'facing={d}': {'model': n(f'block/{b}')} for d in DIRS})
    generated_item(b, n(f'block/{b}'))
    block_name(b, en, de)
    if b != 'moonworm':
        self_drop(b)
ASSETS.append(('mods/twilightforest/textures/model/firefly-tiny.png', f'assets/{NS}/textures/particle/firefly.png'))
ASSETS.append(('mods/twilightforest/textures/model/cicada-model.png', f'assets/{NS}/textures/entity/cicada.png'))
ASSETS.append(('mods/twilightforest/textures/model/moonworm.png', f'assets/{NS}/textures/entity/moonworm.png'))
write(f'assets/{NS}/particles/firefly.json', {'textures': [n('firefly')]})
for f in ('cicada2', 'cicada3'):
    ASSETS.append((f'mods/twilightforest/sounds/{f}.ogg', f'assets/{NS}/sounds/{f}.ogg'))
# sounds.json: tools/gen_mobs.py
names(f'subtitles.{NS}.cicada', 'Cicada sings', 'Zikade zirpt')

# firefly jar (RenderBlockTFFireflyJar): the jar 3..13 x 0..14, the cork 4..12 x 12..16
tex('fireflyjar_top', 'firefly_jar_top')
tex('fireflyjar_side', 'firefly_jar_side')
tex('fireflyjar_cork', 'firefly_jar_cork')
jar = {'from': [3, 0, 3], 'to': [13, 14, 13], 'faces': {d: {'texture': '#top' if d in ('up', 'down') else '#side'} for d in DIRS}}
jar['faces']['down']['cullface'] = 'down'
cork = {'from': [4, 12, 4], 'to': [12, 16, 12], 'faces': {d: {'texture': '#cork'} for d in DIRS}}
cork['faces']['up']['cullface'] = 'up'
model('firefly_jar', {'parent': 'minecraft:block/block', 'render_type': 'minecraft:translucent',
                      'textures': {'particle': n('block/firefly_jar_side'), 'top': n('block/firefly_jar_top'),
                                   'side': n('block/firefly_jar_side'), 'cork': n('block/firefly_jar_cork')},
                      'elements': [jar, cork]})
simple_state('firefly_jar')
item_def('firefly_jar', n('block/firefly_jar'))
block_name('firefly_jar', 'Firefly Jar', 'Glühwürmchenglas')
self_drop('firefly_jar')
shapeless('firefly_jar', [n('firefly'), 'minecraft:glass_bottle'], n('firefly_jar'), 1, 'building')

# ---------------------------------------------------------------- fire jets (BlockTFFireJet)
tex('firejet_side', 'fire_jet_side')
tex('firejet_top', 'fire_jet_top')
tex('towerdev_smoker_off', 'encased_smoker_off')
tex('towerdev_smoker_on', 'encased_smoker_on')
tex('towerdev_firejet_off', 'encased_fire_jet_off')
tex('towerdev_firejet_on', 'encased_fire_jet_on')
tex('towerdev_ghasttraplid_off', 'tower_device_lid_off')
tex('towerdev_ghasttraplid_on', 'tower_device_lid_on')
jet = [n('block/fire_jet_side'), n('block/fire_jet_top')] + [n('block/fire_jet_side')] * 4
model('fire_jet', cube(jet, tint=True))
simple_state('smoker', n('block/fire_jet'))
state('fire_jet', {f'state={s}': {'model': n('block/fire_jet')} for s in ('idle', 'popping', 'flame')})
for kind in ('smoker', 'fire_jet'):
    for onoff in ('off', 'on'):
        model(f'encased_{kind}_{onoff}', cube([n('block/encased_tower_wood'), n(f'block/tower_device_lid_{onoff}')] + [n(f'block/encased_{kind}_{onoff}')] * 4))
state('encased_smoker', {'active=false': {'model': n('block/encased_smoker_off')}, 'active=true': {'model': n('block/encased_smoker_on')}})
state('encased_fire_jet', {'state=idle': {'model': n('block/encased_fire_jet_off')}, 'state=popping': {'model': n('block/encased_fire_jet_on')},
                           'state=flame': {'model': n('block/encased_fire_jet_on')}})
item_def('smoker', n('block/fire_jet'))
item_def('fire_jet', n('block/fire_jet'))
item_def('encased_smoker', n('block/encased_smoker_off'))
item_def('encased_fire_jet', n('block/encased_fire_jet_off'))
for b, en, de in (('smoker', 'Smoking Block', 'Rauchblock'), ('fire_jet', 'Fire Jet', 'Feuerdüse'),
                  ('encased_smoker', 'Encased Smoker', 'Verkleideter Rauchblock'), ('encased_fire_jet', 'Encased Fire Jet', 'Verkleidete Feuerdüse')):
    block_name(b, en, de)
    self_drop(b)
    btag('minecraft:mineable/pickaxe', b)

# ---------------------------------------------------------------- maze stone (BlockTFMazestone)
for src, dst in (('plain', 'maze_stone'), ('brick', 'maze_stone_brick'), ('pillar', 'maze_stone_pillar'), ('decorative', 'maze_stone_decorative'),
                 ('cracked', 'maze_stone_cracked'), ('mossy', 'maze_stone_mossy'), ('mosaic', 'maze_stone_mosaic'), ('border', 'maze_stone_border')):
    tex(f'mazestone_{src}', dst)
MAZE = [  # id, side, top/bottom, en, de
    ('maze_stone', 'maze_stone', 'maze_stone', 'Mazestone', 'Labyrinthstein'),
    ('maze_stone_brick', 'maze_stone_brick', 'maze_stone_brick', 'Mazestone Brick', 'Labyrinthsteinziegel'),
    ('maze_stone_pillar', 'maze_stone_pillar', 'maze_stone', 'Chiseled Mazestone', 'Gemeißelter Labyrinthstein'),
    ('maze_stone_decorative', 'maze_stone_decorative', 'maze_stone_brick', 'Decorative Mazestone Brick', 'Verzierter Labyrinthsteinziegel'),
    ('maze_stone_cracked', 'maze_stone_cracked', 'maze_stone_cracked', 'Cracked Mazestone Brick', 'Rissiger Labyrinthsteinziegel'),
    ('maze_stone_mossy', 'maze_stone_mossy', 'maze_stone_mossy', 'Mossy Mazestone Brick', 'Bemooster Labyrinthsteinziegel'),
    ('maze_stone_mosaic', 'maze_stone_brick', 'maze_stone_mosaic', 'Mazestone Mosaic', 'Labyrinthsteinmosaik'),
    ('maze_stone_border', 'maze_stone_brick', 'maze_stone_border', 'Mazestone Border', 'Labyrinthsteinrand'),
]
for b, side, top, en, de in MAZE:
    model(b, {'parent': 'minecraft:block/cube_bottom_top', 'textures': {'side': n(f'block/{side}'), 'top': n(f'block/{top}'), 'bottom': n(f'block/{top}')}})
    simple_state(b)
    item_def(b, n(f'block/{b}'))
    block_name(b, en, de)
    self_drop(b)
    btag('minecraft:mineable/pickaxe', b)

# ---------------------------------------------------------------- naga stone (BlockTFNagastone.getIcon, RenderBlockTFNagastone uv rotations)
NAGA_TEX = ['face_left', 'face_right', 'cross_section', 'face_front', 'top_tip', 'tip_long', 'bottom_tip', 'bottom_long', 'left_down',
            'right_down', 'left_up', 'right_up', 'tip_left', 'long_side', 'tip_right', 'turn_top']
for t in NAGA_TEX:
    tex(f'nagastone_{t}', f'nagastone_{t}')
FL, FR, CS, FF, TT, TL, BT, BL, LD, RD, LU, RU, TIL, LS, TIR, TU = NAGA_TEX
TOP_LONG = TL
NAGA_ICONS = {  # metadata -> textures of sides 0..5 (down, up, north, south, west, east)
    0: [BT, TT, FF, CS, FL, FR], 1: [BT, TT, CS, FF, FR, FL], 2: [BT, TT, FR, FL, FF, CS], 3: [BT, TT, FL, FR, CS, FF],
    4: [CS, TT, CS, TIR, LD, RD], 5: [CS, TT, TIL, CS, RD, LD], 6: [CS, TT, RD, LD, CS, TIL], 7: [CS, TT, LD, RD, TIR, CS],
    8: [BT, CS, CS, TIL, LU, RU], 9: [BT, CS, TIR, CS, RU, LU], 10: [BT, CS, RU, LU, CS, TIL], 11: [BT, CS, LU, RU, TIL, CS],
    12: [BL, TOP_LONG, LS, LS, CS, CS], 13: [BL, TOP_LONG, CS, CS, LS, LS], 14: [CS, CS, LS, LS, LS, LS], 15: [CS, TU, LS, LS, LS, LS],
}
# setRenderRotate: (top, bottom, east = north face, west = south face, north = west face, south = east face); 1 -> 90, 2 -> 270, 3 -> 180
UV = {1: 90, 2: 270, 3: 180}


def naga_rot(meta):
    t, o = meta & 12, meta & 3
    r = {}
    if t == 0:
        r = [{'up': 1, 'down': 2}, {'up': 2, 'down': 1}, {'up': 0, 'down': 3}, {'up': 3, 'down': 0}][o]
    elif t in (4, 8):
        r = [{'up': 2, 'down': 1, 'south': 2}, {'up': 1, 'down': 2, 'north': 2}, {'up': 3, 'down': 0, 'east': 2},
             {'up': 0, 'down': 3, 'west': 2}][o]
    elif t == 12:
        r = [{}, {'up': 1, 'down': 1}, {'west': 2, 'east': 2, 'north': 2, 'south': 2}, {}][o]
    return {d: UV[v] for d, v in r.items() if v}


for meta in range(16):
    model(f'nagastone_{meta}', cube([n(f'block/nagastone_{t}') for t in NAGA_ICONS[meta]], rotations=naga_rot(meta)))
state('nagastone_head', {f'meta={m}': {'model': n(f'block/nagastone_{m}')} for m in range(4)})
state('nagastone_body', {f'meta={m}': {'model': n(f'block/nagastone_{m}')} for m in range(4, 16)})
item_def('nagastone_head', n('block/nagastone_1'))
item_def('nagastone_body', n('block/nagastone_13'))
block_name('nagastone_head', 'Nagastone Head', 'Nagasteinkopf')
block_name('nagastone_body', 'Nagastone', 'Nagastein')
for b in ('nagastone_head', 'nagastone_body'):
    self_drop(b)
    btag('minecraft:mineable/pickaxe', b)

# ---------------------------------------------------------------- boss spawners (BlockTFBossSpawner, Teil 24d: placeholders)
# metadata 0 naga, 1 lich, 2 hydra, 3 tower (ur-ghast); looks like the mob spawner, drops nothing; the bosses come with 24f
for b, en, de in (('naga_boss_spawner', 'Naga Spawner', 'Naga-Spawner'), ('lich_boss_spawner', 'Lich Spawner', 'Lich-Spawner'),
                  ('hydra_boss_spawner', 'Hydra Spawner', 'Hydra-Spawner'), ('tower_boss_spawner', 'Tower Boss Spawner', 'Turmboss-Spawner')):
    simple_state(b, 'minecraft:block/spawner')
    item_def(b, 'minecraft:block/spawner')
    block_name(b, en, de)
    btag('minecraft:mineable/pickaxe', b)

# ---------------------------------------------------------------- tower wood (BlockTFTowerWood)
TOWER = [  # id, original texture, tinted (colorMultiplier), en, de
    ('tower_wood', 'towerwood_planks', True, 'Towerwood Planks', 'Turmholzbretter'),
    ('encased_tower_wood', 'towerwood_encased', False, 'Encased Towerwood', 'Verkleidetes Turmholz'),
    ('tower_wood_cracked', 'towerwood_cracked', True, 'Cracked Towerwood', 'Rissiges Turmholz'),
    ('tower_wood_mossy', 'towerwood_mossy', True, 'Mossy Towerwood', 'Bemoostes Turmholz'),
    ('tower_wood_infested', 'towerwood_infested', True, 'Infested Towerwood', 'Befallenes Turmholz'),
]
for b, src, tinted, en, de in TOWER:
    tex(src, b)
    model(b, cube([n(f'block/{b}')] * 6, tint=tinted))
    simple_state(b)
    item_def(b, n(f'block/{b}'))
    block_name(b, en, de)
    btag('minecraft:mineable/axe', b)
    flam(b, 0, 1)
    if b != 'tower_wood_infested':
        self_drop(b)
    # OreDictionary "plankWood": the Forge recipes of 1.5.2 took it as planks
    bitag('minecraft:planks', b)
shaped('tower_wood', ['##', '##'], {'#': n('darkwood_log')}, n('tower_wood'), 4, 'building')
shaped('encased_tower_wood', ['#', '#', '#'], {'#': n('tower_wood')}, n('encased_tower_wood'), 3, 'building')

# ---------------------------------------------------------------- magic trees (BlockTFMagicLog, BlockTFMagicLeaves, BlockTFMagicLogSpecial)
MAGIC = [  # id, original prefix, core id, core textures, en log, de log, en core, de core, en leaves, de leaves, leaf texture, vanilla sapling, chance
    ('time', 'time', 'time_clock', 'time_clock', 'Timewood', 'Holz der Zeit', 'Timewood Clock', 'Uhr der Zeit', 'Timewood Leaves', 'Laub der Zeit',
     'time_leaves', 'minecraft:oak_sapling', 1 / 20),
    ('transformation', 'trans', 'transformation_heart', 'trans_heart', 'Transwood', 'Holz der Verwandlung', 'Heart of Transformation',
     'Herz der Verwandlung', 'Leaves of Transformation', 'Laub der Verwandlung', 'transformation_leaves', 'minecraft:spruce_sapling', 1 / 20),
    ('mining', 'mine', 'mining_core', 'mine_gem', 'Minewood', 'Bergarbeiterholz', 'Minewood Core', 'Bergarbeiterkern', "Miner's Leaves",
     'Bergarbeiterlaub', 'time_leaves', 'minecraft:birch_sapling', 1 / 20),
    ('sorting', 'sort', 'sorting_engine', 'sort_eye', 'Sortingwood', 'Holz der Sortierung', 'Sortingwood Engine', 'Sortiermaschine', 'Sorting Leaves',
     'Laub der Sortierung', 'sorting_leaves', 'minecraft:jungle_sapling', 1 / 40),
]
MAGIC_COLORS = {'time': 6986775, 'transformation': 7130346, 'mining': 16576836, 'sorting': 3558403}
tex('time_leaves', 'time_leaves')
tex('trans_leaves', 'transformation_leaves')
tex('sort_leaves', 'sorting_leaves')
for leaf in ('time_leaves', 'transformation_leaves', 'sorting_leaves'):
    # the .txt animation of 1.5.2: 16 frames of 2 ticks each, in order
    write(f'assets/{NS}/textures/block/{leaf}.png.mcmeta', {'animation': {'frametime': 2}})
for w, src, core, core_tex, en, de, en_core, de_core, en_leaves, de_leaves, leaf_tex, van_sapling, c in MAGIC:
    tex(f'{src}_side', f'{w}_log')
    tex(f'{src}_section', f'{w}_log_top')
    tex(core_tex, core)
    tex(f'{core_tex}_off', f'{core}_off')
    log(f'{w}_log', n(f'block/{w}_log'), n(f'block/{w}_log_top'))
    log(f'{w}_wood', n(f'block/{w}_log'), None)
    block_name(f'{w}_log', en, de)
    block_name(f'{w}_wood', f'{en} Bark', f'{de} (Rinde)')
    self_drop(f'{w}_log')
    self_drop(f'{w}_wood', n(f'{w}_log'))
    bitag('minecraft:logs', f'{w}_log', f'{w}_wood', core)
    btag('minecraft:mineable/axe', f'{w}_log', f'{w}_wood', core)
    # the core: clock/heart/gem/eye on the sides, the log section on top and bottom; "_off" when switched off
    for onoff, suffix in (('true', ''), ('false', '_off')):
        model(f'{core}{suffix}', {'parent': 'minecraft:block/cube_column', 'textures': {'end': n(f'block/{w}_log_top'), 'side': n(f'block/{core}{suffix}')}})
    state(core, {'active=true': {'model': n(f'block/{core}')}, 'active=false': {'model': n(f'block/{core}_off')}})
    item_def(core, n(f'block/{core}'))
    block_name(core, en_core, de_core)
    self_drop(core, n(f'{w}_log'))
    leaves = f'{w}_leaves'
    model(leaves, {'parent': 'minecraft:block/leaves', 'textures': {'all': n(f'block/{leaf_tex}')}})
    simple_state(leaves)
    item_def(leaves, n(f'block/{leaves}'), argb(MAGIC_COLORS[w]))
    block_name(leaves, en_leaves, de_leaves)
    # BlockLeaves: the vanilla sapling of meta & 3 (oak, spruce, birch, jungle), jungle 1 in 40; an apple 1 in 200 for meta 0
    extra = []
    if w == 'time':
        extra = [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'minecraft:apple'}], 'condition': {'type': 'minecraft:all_of', 'terms': [
            {'type': 'minecraft:inverted', 'term': SHEARS},
            {'type': 'minecraft:survives_explosion'}, chance(1 / 200)]}}]
    leaves_loot(leaves, extra, van_sapling, c)
    bitag('minecraft:leaves', leaves)
    btag('minecraft:mineable/hoe', leaves)

# ---------------------------------------------------------------- items
for i, src, en, de in (('liveroot', 'liveRoot', 'Liveroot', 'Lebenswurzel'), ('torchberries', 'torchberries', 'Torchberries', 'Fackelbeeren'),
                       ('charm_of_life_1', 'charmOfLife1', 'Charm of Life I', 'Amulett des Lebens I'),
                       ('charm_of_keeping_1', 'charmOfKeeping1', 'Charm of Keeping I', 'Amulett der Bewahrung I')):
    tex(src, i, 'item')
    generated_item(i, n(f'item/{i}'))
    item_name(i, en, de)

# ---------------------------------------------------------------- TreeCapitator (TreeCapitator.cfg, tree_and_mod_configs.twilightforest)
TREES = {
    'oak': (['twilight_oak_log', 'twilight_oak_wood'], ['twilight_oak_leaves', 'rainbow_oak_leaves'], {}),
    'canopy': (['canopy_log', 'canopy_wood'], ['canopy_leaves'], {}),
    'mangrove': (['mangrove_log', 'mangrove_wood'], ['mangrove_leaves'], {}),
    'darkwood': (['darkwood_log', 'darkwood_wood'], ['darkwood_leaves'], {'maxHorLeafBreakDist': 5, 'maxLeafIDDist': 2, 'requireLeafDecayCheck': False}),
    'time': (['time_log', 'time_wood', 'time_clock'], ['time_leaves'], {}),
    'transformation': (['transformation_log', 'transformation_wood', 'transformation_heart'], ['transformation_leaves'], {}),
    'miner': (['mining_log', 'mining_wood', 'mining_core'], ['mining_leaves'], {'onlyDestroyUpwards': False}),
    'sorting': (['sorting_log', 'sorting_wood', 'sorting_engine'], ['sorting_leaves'], {}),
}
for key, (logs, leaves, extra) in TREES.items():
    d = {'logs': [n(x) for x in logs], 'leaves': [n(x) for x in leaves]}
    d.update(extra)
    write(f'data/{NS}/treecapitator/tree/{key}.json', d)

# ---------------------------------------------------------------- Teil 24e: tower devices (BlockTFTowerDevice, BlockTFTowerTranslucent)
DEVICES = [  # id (by metadata), texture, item (damageDropped; None: no drop), en, de
    ('reappearing_block', 'reappearing_off', 'reappearing_block', 'Reappearing Block', 'Wiederkehrender Block'),
    ('reappearing_block_active', 'reappearing_on', 'reappearing_block', 'Active Reappearing Block', 'Aktiver wiederkehrender Block'),
    ('vanishing_block', 'vanish_off', 'vanishing_block', 'Vanishing Block', 'Verschwindender Block'),
    ('vanishing_block_active', 'vanish_on', 'vanishing_block', 'Active Vanishing Block', 'Aktiver verschwindender Block'),
    ('locked_vanishing_block', 'lock_on', 'locked_vanishing_block', 'Locked Vanishing Block', 'Verschlossener verschwindender Block'),
    ('unlocked_vanishing_block', 'lock_off', 'unlocked_vanishing_block', 'Unlocked Vanishing Block', 'Aufgeschlossener verschwindender Block'),
    ('carminite_builder', 'builder_off', 'carminite_builder', 'Carminite Builder', 'Karminit-Baumeister'),
    ('carminite_builder_active', 'builder_on', 'carminite_builder', 'Active Carminite Builder', 'Aktiver Karminit-Baumeister'),
    ('carminite_builder_timeout', 'builder_timeout', 'carminite_builder', 'Timed-out Carminite Builder', 'Ruhender Karminit-Baumeister'),
    ('antibuilder', 'antibuilder', None, 'Antibuilder', 'Rückbauer'),
    ('ghast_trap', 'ghasttrap_off', 'ghast_trap', 'Ghast Trap', 'Ghastfalle'),
    ('ghast_trap_active', 'ghasttrap_on', 'ghast_trap', 'Active Ghast Trap', 'Aktive Ghastfalle'),
    ('carminite_reactor', 'reactor_off', 'carminite_reactor', 'Carminite Reactor', 'Karminit-Reaktor'),
    ('carminite_reactor_active', 'reactor_on', 'carminite_reactor', 'Active Carminite Reactor', 'Aktiver Karminit-Reaktor'),
]
DEVICE_ITEMS = {0, 2, 4, 5, 6, 9, 10, 12}
for meta, (b, t, drop, en, de) in enumerate(DEVICES):
    tex(f'towerdev_{t}', b)
    if b.startswith('ghast_trap'):
        lid = 'on' if b.endswith('active') else 'off'
        model(b, cube([n('block/encased_tower_wood'), n(f'block/tower_device_lid_{lid}')] + [n(f'block/{b}')] * 4))
    else:
        model(b, cube([n(f'block/{b}')] * 6))
    simple_state(b)
    if meta in DEVICE_ITEMS:
        item_def(b, n(f'block/{b}'))
    block_name(b, en, de)
    btag('minecraft:mineable/axe', b)
    if drop:
        self_drop(b, n(drop))
    else:
        loot(b, [])
TRANSLUCENTS = [  # id (by metadata), textures (down, up, north, south, west, east), en, de
    ('reappearing_trace', [n('block/reappearing_trace')] * 6, 'Reappearing Block (vanished)', 'Wiederkehrender Block (verschwunden)'),
    ('reappearing_trace_active', [n('block/reappearing_trace_active')] * 6, 'Reappearing Block (returning)', 'Wiederkehrender Block (kehrt zurück)'),
    ('built_block', [n('block/built_block')] * 6, 'Built Block', 'Gebauter Block'),
    ('built_block_active', [n('block/built_block_active')] * 6, 'Active Built Block', 'Aktiver gebauter Block'),
    ('antibuilt_block', [n('block/antibuilt_block')] * 6, 'Antibuilt Block', 'Rückgebauter Block'),
    # getIcon picked portal, netherrack, bedrock or obsidian at random for every face and frame
    ('reactor_debris', ['minecraft:block/bedrock', 'minecraft:block/netherrack', 'minecraft:block/obsidian', 'minecraft:block/nether_portal',
                        'minecraft:block/netherrack', 'minecraft:block/obsidian'], 'Reactor Debris', 'Reaktortrümmer'),
    ('fake_gold', ['minecraft:block/gold_block'] * 6, 'Fake Gold', 'Falsches Gold'),
    ('fake_diamond', ['minecraft:block/diamond_block'] * 6, 'Fake Diamond', 'Falscher Diamant'),
]
for src, dst in (('reappearing_trace_off', 'reappearing_trace'), ('reappearing_trace_on', 'reappearing_trace_active'),
                 ('built_off', 'built_block'), ('built_on', 'built_block_active'), ('antibuilt', 'antibuilt_block')):
    tex(f'towerdev_{src}', dst)
for b, textures, en, de in TRANSLUCENTS:
    m = cube(textures)
    m['render_type'] = 'minecraft:translucent'
    model(b, m)
    simple_state(b)
    block_name(b, en, de)
for i, src, en, de in (('tower_key', 'towerKey', 'Tower Key', 'Turmschlüssel'), ('carminite', 'carminite', 'Carminite', 'Karminit')):
    tex(src, i, 'item')
    generated_item(i, n(f'item/{i}'))
    item_name(i, en, de)
# mob.tf.urghast.trapwarmup / trapon (trapon0..4) / trapspindown
EXTRA_SOUNDS = {}
for event, files, en, de in (('ghast_trap.warmup', ['trapwarmup'], 'Ghast trap warms up', 'Ghastfalle läuft an'),
                             ('ghast_trap.on', ['trapon%d' % i for i in range(5)], 'Ghast trap hums', 'Ghastfalle summt'),
                             ('ghast_trap.spindown', ['trapspindown'], 'Ghast trap spins down', 'Ghastfalle läuft aus')):
    EXTRA_SOUNDS[event] = {'sounds': [n(f'urghast/{f}') for f in files], 'subtitle': f'subtitles.{NS}.{event}'}
    for f in files:
        ASSETS.append((f'mods/twilightforest/sounds/urghast/{f}.ogg', f'assets/{NS}/sounds/urghast/{f}.ogg'))
    names(f'subtitles.{NS}.{event}', en, de)

# ---------------------------------------------------------------- Teil 24c: mobs (tools/gen_mobs.py)
exec(open(os.path.join(os.path.dirname(os.path.abspath(__file__)), 'gen_mobs.py')).read())
# ---------------------------------------------------------------- Teil 24f: items, armour, tools, bosses (tools/gen_items.py)
exec(open(os.path.join(os.path.dirname(os.path.abspath(__file__)), 'gen_items.py')).read())
exec(open(os.path.join(os.path.dirname(os.path.abspath(__file__)), 'gen_hydra.py')).read())

# ---------------------------------------------------------------- write tags, data map, recipes
for (reg, t), values in TAGS.items():
    ns, path = t.split(':')
    write(f'data/{ns}/tags/{reg}/{path}.json', {'values': sorted(set(v for v in values if isinstance(v, str))) + [v for v in values if not isinstance(v, str)]})
write('data/neoforge/data_maps/block/flammable.json', {'values': FLAMMABLE})
for name, r in RECIPES.items():
    write(f'data/{NS}/recipe/{name}.json', r)

LANG_EN.update(LANG_EN_B)
LANG_DE.update(LANG_DE_B)
write(f'assets/{NS}/lang/en_us.json', LANG_EN)
write(f'assets/{NS}/lang/de_de.json', LANG_DE)

with open(os.path.join(ROOT, '..', '..', '..', 'original-assets.txt'), 'w') as f:
    f.write('# path in twilightforest-1.18.1.zip -> path in the ported jar (written by tools/gen.py)\n')
    for name, texture in CUBES.items():
        f.write(f'mods/twilightforest/textures/blocks/{texture}.png -> assets/{NS}/textures/block/{texture}.png\n')
    for src, dst in ASSETS:
        f.write(f'{src} -> {dst}\n')
print('ok')
