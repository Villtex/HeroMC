#!/usr/bin/env python3
"""Generates the assets/data JSON of the ChocoCraft port (mods/chococraft/src/main/resources), the language file and
original-assets.txt. Everything under resources/assets and resources/data is overwritten.

The Chocopedia pages are taken from the original (ChocopediaPages in .reference/chococraft, python3 tools/reference.py
--only chococraft); without it the pages already in the language file are kept."""
import json, os, re, shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
REPO = os.path.dirname(os.path.dirname(ROOT))
RES = ROOT + '/src/main/resources'
NS = 'chococraft'
LANG = f'{RES}/assets/{NS}/lang/en_us.json'
PAGES_SRC = f'{REPO}/.reference/chococraft/src/chococraft/common/gui/ChocopediaPages.java'

old_lang = json.load(open(LANG)) if os.path.exists(LANG) else {}
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
lang = {}


def tex(src, dst):
    assets.append((src, f'assets/{NS}/textures/{dst}.png'))


def item_def(name, model, tints=None):
    m = {'type': 'minecraft:model', 'model': model}
    if tints:
        m['tints'] = tints
    write(f'assets/{NS}/items/{name}.json', {'model': m})


def item(name, texture, display):
    """A flat item with the original icon (mods/chococraft/textures/items/<texture>.png)."""
    tex(f'mods/chococraft/textures/items/{texture}.png', f'item/{name}')
    write(f'assets/{NS}/models/item/{name}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': n(f'item/{name}')}})
    item_def(name, n(f'item/{name}'))
    lang[f'item.{NS}.{name}'] = display


# ---------------------------------------------------------------- items (createItemInstances)
ITEMS = [
    ('chocopedia', 'chocopedia', 'Chocopedia'), ('chocobo_feather', 'chocoboFeather', 'Chocobo Feather'),
    ('chocobo_saddle', 'chocoboSaddle', 'Chocobo Saddle'), ('chocobo_saddle_bags', 'saddlebag', 'Chocobo Saddle Bags'),
    ('chocobo_pack_bags', 'packbag', 'Chocobo Pack Bags'), ('gysahl_loverly', 'gysahl_loverly', 'Loverly Gysahl'),
    ('gysahl_golden', 'gysahl_golden', 'Golden Gysahl'), ('gysahl_pink', 'gysahl_pink', 'Pink Gysahl'),
    ('gysahl_red', 'gysahl_red', 'Red Gysahl'), ('gysahl_cake', 'gysahl_cake', 'Gysahl Cake'),
    ('chocobo_whistle', 'whistle', 'Chocobo Whistle'), ('purple_chocobo_egg', 'eggPurple', 'Purple Chocobo Egg'),
    ('chocobo_leg_raw', 'legRaw', 'Raw Chocobo Leg'), ('chocobo_leg_cooked', 'legCooked', 'Cooked Chocobo Leg'),
    ('gysahl_raw_pickles', 'gysahl_rawpickles', 'Gysahl Raw Pickles'), ('gysahl_pickles', 'gysahl_pickles', 'Gysahl Pickles'),
    ('choco_disguise_helmet', 'disguiseHead', 'Chocodisguise Helmet'), ('choco_disguise_chestplate', 'disguiseBody', 'Chocodisguise Body'),
    ('choco_disguise_leggings', 'disguiseLegs', 'Chocodisguise Legs'), ('choco_disguise_boots', 'disguiseBoots', 'Chocodisguise Boots'),
]
for name, texture, display in ITEMS:
    item(name, texture, display)
# ItemGysahlSeeds: its own icon, the block of the stem
item('gysahl_seeds', 'gysahl_seeds', 'Gysahl Seeds')
lang[f'block.{NS}.gysahl_stem'] = 'Gysahl Stem'

# the disguise: chocoDisguise_1 (head, body, boots) and _2 (legs)
tex('resources/armor/chocoDisguise_1.png', 'entity/equipment/humanoid/choco_disguise')
tex('resources/armor/chocoDisguise_2.png', 'entity/equipment/humanoid_leggings/choco_disguise')
write(f'assets/{NS}/equipment/choco_disguise.json',
      {'layers': {'humanoid': [{'texture': n('choco_disguise')}], 'humanoid_leggings': [{'texture': n('choco_disguise')}]}})
write(f'data/{NS}/tags/item/repairs/choco_disguise.json', {'values': []})

# ---------------------------------------------------------------- blocks
def cross_block(name, texture, display=None):
    tex(f'mods/chococraft/textures/blocks/{texture}.png', f'block/{texture.lower()}')
    write(f'assets/{NS}/models/block/{name}.json', {'parent': 'minecraft:block/cross', 'render_type': 'minecraft:cutout',
                                                     'textures': {'cross': n(f'block/{texture.lower()}')}})
    if display:
        lang[f'block.{NS}.{name}'] = display


cross_block('gysahl_green', 'gysahl_green', 'Gysahl Green')
write(f'assets/{NS}/blockstates/gysahl_green.json', {'variants': {'': {'model': n('block/gysahl_green')}}})
write(f'assets/{NS}/models/item/gysahl_green.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': n('block/gysahl_green')}})
item_def('gysahl_green', n('item/gysahl_green'))
for age in range(5):
    cross_block(f'gysahl_stem_stage{age}', f'gysahl_stem0{age + 1}')
write(f'assets/{NS}/blockstates/gysahl_stem.json', {'variants': {f'age={a}': {'model': n(f'block/gysahl_stem_stage{a}')} for a in range(5)}})

tex('mods/chococraft/textures/blocks/chocoboStraw.png', 'block/straw')
write(f'assets/{NS}/models/block/straw.json', {
    'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout',
    'textures': {'particle': n('block/straw'), 'texture': n('block/straw')},
    'elements': [{'from': [0, 0, 0], 'to': [16, 1, 16], 'faces': {
        'down': {'uv': [0, 0, 16, 16], 'texture': '#texture', 'cullface': 'down'},
        'up': {'uv': [0, 0, 16, 16], 'texture': '#texture'},
        'north': {'uv': [0, 15, 16, 16], 'texture': '#texture'}, 'south': {'uv': [0, 15, 16, 16], 'texture': '#texture'},
        'west': {'uv': [0, 15, 16, 16], 'texture': '#texture'}, 'east': {'uv': [0, 15, 16, 16], 'texture': '#texture'}}}]})
write(f'assets/{NS}/blockstates/straw.json', {'variants': {'': {'model': n('block/straw')}}})
write(f'assets/{NS}/models/item/straw.json', {'parent': n('block/straw')})
item_def('straw', n('item/straw'))
lang[f'block.{NS}.straw'] = 'Straw'


def self_drop(name):
    write(f'data/{NS}/loot_table/blocks/{name}.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [
        {'type': 'minecraft:item', 'name': n(name)}], 'condition': {'type': 'minecraft:survives_explosion'}}],
        'random_sequence': n(f'blocks/{name}')})


self_drop('gysahl_green')
self_drop('straw')


def stage(a):
    return {'type': 'minecraft:match_block', 'blocks': n('gysahl_stem'), 'state': {'age': str(a)}}


# BlockGysahlStem.idDropped / dropBlockAsItemWithChance: ripe 899/1000 green, of the rest 899/1000 loverly, else golden;
# 3 (+ fortune) tries for a seed with (stage + 1) / 15 each, at every stage
seed_pools = [{'rolls': 1, 'condition': stage(a), 'entries': [{'type': 'minecraft:item', 'name': n('gysahl_seeds'), 'modifier': [
    {'type': 'minecraft:set_count', 'count': 0},
    {'type': 'minecraft:apply_bonus', 'enchantment': 'minecraft:fortune', 'formula': 'minecraft:binomial_with_bonus_count',
     'parameters': {'extra': 3, 'probability': (a + 1) / 15}}]}]} for a in range(5)]
write(f'data/{NS}/loot_table/blocks/gysahl_stem.json', {'type': 'minecraft:block', 'modifier': {'type': 'minecraft:explosion_decay'}, 'pools': [
    {'rolls': 1, 'condition': stage(4), 'entries': [{'type': 'minecraft:alternatives', 'children': [
        {'type': 'minecraft:item', 'name': n('gysahl_green'), 'condition': {'type': 'minecraft:random_chance', 'chance': 0.899}},
        {'type': 'minecraft:item', 'name': n('gysahl_loverly'), 'condition': {'type': 'minecraft:random_chance', 'chance': 0.899}},
        {'type': 'minecraft:item', 'name': n('gysahl_golden')}]}]}] + seed_pools, 'random_sequence': n('blocks/gysahl_stem')})

# ---------------------------------------------------------------- entities
COLORS = [  # enum, texture file, english, egg colour, egg dots (registerChocobos)
    ('yellow', 'chocobo', 'Yellow Chocobo', (250, 250, 50), (180, 150, 1)),
    ('green', 'greenchocobo', 'Green Chocobo', (50, 250, 50), (1, 100, 1)),
    ('blue', 'bluechocobo', 'Blue Chocobo', (100, 100, 250), (100, 100, 200)),
    ('white', 'whitechocobo', 'White Chocobo', (240, 240, 240), (252, 252, 252)),
    ('black', 'blackchocobo', 'Black Chocobo', (1, 1, 1), (150, 150, 150)),
    ('gold', 'goldchocobo', 'Gold Chocobo', (250, 130, 70), (111, 90, 33)),
    ('pink', 'pinkchocobo', 'Pink Chocobo', (222, 20, 222), (250, 200, 250)),
    ('red', 'redchocobo', 'Red Chocobo', (250, 20, 20), (100, 20, 20)),
    ('purple', 'purplechocobo', 'Purple Chocobo', (170, 70, 160), (60, 50, 111)),
]
GEAR = [('Untamed', 'untamed'), ('Tamed', 'tamed'), ('Saddled', 'saddled'), ('SaddleBagged', 'saddle_bagged'), ('PackBagged', 'pack_bagged')]


def egg(entity, rgb, display):
    name = f'{entity}_spawn_egg'
    write(f'assets/{NS}/models/item/{name}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'minecraft:item/egg'}})
    value = (0xFF000000 | rgb[0] << 16 | rgb[1] << 8 | rgb[2]) - (1 << 32)
    item_def(name, n(f'item/{name}'), [{'type': 'minecraft:constant', 'value': value}])
    lang[f'item.{NS}.{name}'] = display + ' Spawn Egg'


def leg_pool(smelt):
    # EntityLiving.dropFewItems: rand(3) (+ rand(looting + 1)) legs; getDropItemId cooked when burning
    mods = [{'type': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': 0, 'max': 2}}]
    if smelt:
        mods.append({'type': 'minecraft:furnace_smelt', 'condition': {'type': 'minecraft:entity_properties', 'entity': 'this',
                                                                         'predicate': {'minecraft:flags': {'is_on_fire': True}}}})
    mods.append({'type': 'minecraft:enchanted_count_increase', 'count': {'type': 'minecraft:uniform', 'min': 0.0, 'max': 1.0},
                 'enchantment': 'minecraft:looting'})
    return {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n('chocobo_leg_raw'), 'modifier': mods}]}


for color, file, display, primary, _ in COLORS:
    entity = f'chocobo_{color}'
    lang[f'entity.{NS}.{entity}'] = display
    egg(entity, primary, display)
    for gender in ('Male', 'Female'):
        for src, dst in GEAR:
            tex(f'resources/Chocobos/{src}/{gender}/{file}.png', f'entity/chocobo/{dst}/{gender.lower()}/{file}')
    # EntityChocoboPurple.getDropItemId: always the raw leg (isBurning is false for it)
    write(f'data/{NS}/loot_table/entities/{entity}.json', {'type': 'minecraft:entity', 'pools': [leg_pool(color != 'purple')],
                                                           'random_sequence': n(f'entities/{entity}')})
    tex(f'resources/Chicobos/{file}.png', f'entity/chicobo/{file}')
    tex(f'resources/Chicobos/Tamed/{file}.png', f'entity/chicobo/tamed/{file}')
lang[f'entity.{NS}.chicobo'] = 'Chicobo'
lang[f'entity.{NS}.purple_chocobo_egg'] = 'Purple Chocobo Egg'
egg('chicobo', (170, 70, 250), 'Chicobo')
write(f'data/{NS}/loot_table/entities/chicobo.json', {'type': 'minecraft:entity', 'pools': []})

# ---------------------------------------------------------------- sound
assets.append(('resources/sounds/choco_kweh.ogg', f'assets/{NS}/sounds/kweh.ogg'))
write(f'assets/{NS}/sounds.json', {'kweh': {'sounds': [n('kweh')], 'subtitle': f'subtitles.{NS}.kweh'}})
lang[f'subtitles.{NS}.kweh'] = 'Chocobo kwehs'

# ---------------------------------------------------------------- recipes (addRecipes / addSmeltings)
def shaped(name, pattern, key, result, count=1, category='misc'):
    write(f'data/{NS}/recipe/{name}.json', {'type': 'minecraft:crafting_shaped', 'category': category, 'key': key, 'pattern': pattern,
                                            'result': {'count': count, 'id': result}})


def shapeless(name, ingredients, result, count=1, category='misc'):
    write(f'data/{NS}/recipe/{name}.json', {'type': 'minecraft:crafting_shapeless', 'category': category, 'ingredients': ingredients,
                                            'result': {'count': count, 'id': result}})


def smelt(name, ingredient, result, xp):
    for kind, time in (('smelting', 200), ('campfire_cooking', 600), ('smoking', 100)):
        if kind != 'smelting' and 'pickles' in name:
            continue
        write(f'data/{NS}/recipe/{name}_{kind}.json', {'type': f'minecraft:{kind}', 'category': 'food', 'cookingtime': time,
                                                     'experience': xp, 'ingredient': ingredient, 'result': {'id': result}})


F = n('chocobo_feather')
G = n('gysahl_green')
shaped('chocobo_saddle', ['-X-', ' Y '], {'X': 'minecraft:leather', 'Y': F, '-': 'minecraft:string'}, n('chocobo_saddle'))
shapeless('chocobo_saddle_from_saddle', ['minecraft:saddle', F], n('chocobo_saddle'))
shaped('chocobo_saddle_bags', [' Y ', 'X X', ' X '], {'X': 'minecraft:leather', 'Y': F}, n('chocobo_saddle_bags'))
# Block.cloth without a damage value: white wool only
shaped('chocobo_pack_bags', ['-Y-', 'C C', '-X-'], {'C': 'minecraft:white_wool', 'X': 'minecraft:leather', 'Y': F, '-': 'minecraft:string'},
       n('chocobo_pack_bags'))
shaped('chocopedia', ['FGF', 'IBI', 'FLF'], {'B': 'minecraft:book', 'F': F, 'I': 'minecraft:ink_sac', 'L': 'minecraft:lapis_lazuli',
                                           'G': 'minecraft:gold_nugget'}, n('chocopedia'))
shapeless('chocobo_whistle', [F, 'minecraft:gold_ingot'], n('chocobo_whistle'))
shapeless('gysahl_seeds', [G], n('gysahl_seeds'), 3)
shaped('gysahl_cake', ['MGM', 'SES', 'WGW'], {'G': G, 'M': 'minecraft:milk_bucket', 'S': 'minecraft:sugar', 'W': 'minecraft:wheat',
                                             'E': 'minecraft:egg'}, n('gysahl_cake'))
shapeless('gysahl_raw_pickles', [G, 'minecraft:sugar'], n('gysahl_raw_pickles'), 2)
shapeless('gysahl_pink', [G, 'minecraft:pink_dye'], n('gysahl_pink'))
shapeless('gysahl_red', [G, 'minecraft:red_dye'], n('gysahl_red'))
shaped('choco_disguise_helmet', ['YYY', 'Y Y'], {'Y': F}, n('choco_disguise_helmet'), category='equipment')
shaped('choco_disguise_chestplate', ['Y Y', 'YYY', 'YYY'], {'Y': F}, n('choco_disguise_chestplate'), category='equipment')
shaped('choco_disguise_leggings', ['YYY', 'Y Y', 'Y Y'], {'Y': F}, n('choco_disguise_leggings'), category='equipment')
shaped('choco_disguise_boots', ['Y Y', 'Y Y'], {'Y': F}, n('choco_disguise_boots'), category='equipment')
shaped('straw', ['WW'], {'W': 'minecraft:wheat'}, n('straw'), 8, 'building')
shaped('arrow_from_chocobo_feather', [' F ', ' S ', ' Y '], {'F': 'minecraft:flint', 'S': 'minecraft:stick', 'Y': F}, 'minecraft:arrow', 4,
       'equipment')
shapeless('writable_book_from_chocobo_feather', ['minecraft:book', 'minecraft:ink_sac', F], 'minecraft:writable_book')
# GameRegistry.addSmelting (0.1 xp); 26.3 cooks meat in smokers and on campfires as well
smelt('chocobo_leg_cooked', n('chocobo_leg_raw'), n('chocobo_leg_cooked'), 0.1)
smelt('gysahl_pickles', n('gysahl_raw_pickles'), n('gysahl_pickles'), 0.1)

# ---------------------------------------------------------------- tags
write('data/minecraft/tags/item/wolf_food.json', {'replace': False, 'values': [n('chocobo_leg_raw')]})
write('data/minecraft/tags/item/meat.json', {'replace': False, 'values': [n('chocobo_leg_raw'), n('chocobo_leg_cooked')]})
write('data/minecraft/tags/block/crops.json', {'replace': False, 'values': [n('gysahl_stem')]})
write('data/minecraft/tags/block/maintains_farmland.json', {'replace': False, 'values': [n('gysahl_stem')]})
# the disguise is in no armour tag: those would make it trimmable and enchantable; the original was neither

# ---------------------------------------------------------------- loot modifiers
# MinecraftForge.addGrassSeed(gysahl seeds, 3): BlockTallGrass dropped a seed with 1/8, weighted against the wheat
# seeds (10), Natura's barley and cotton seeds (3 each) and these (3): 1/8 * 3/19
write(f'data/{NS}/loot_modifiers/grass_seeds.json', {'type': 'neoforge:add_table', 'condition': {'type': 'minecraft:any_of', 'terms': [
    {'type': 'neoforge:loot_table_id', 'loot_table_id': 'minecraft:blocks/short_grass'},
    {'type': 'neoforge:loot_table_id', 'loot_table_id': 'minecraft:blocks/fern'}]}, 'table': n('gameplay/grass_seeds')})
write(f'data/{NS}/loot_table/gameplay/grass_seeds.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'condition': {
    'type': 'minecraft:all_of', 'terms': [{'type': 'minecraft:inverted', 'term': 'minecraft:tool/can_shear'}, {'type': 'minecraft:random_chance', 'chance': 0.125 * 3 / 19}]},
    'entries': [{'type': 'minecraft:item', 'name': n('gysahl_seeds')}]}]})
# onSkeletonAndZombiDropEvent: 3 % a Chocopedia from zombies, skeletons, zombie pigmen, witches and villagers
DROPPERS = ['zombie', 'zombie_villager', 'skeleton', 'wither_skeleton', 'zombified_piglin', 'witch', 'villager']
write(f'data/{NS}/loot_modifiers/chocopedia_drops.json', {'type': 'neoforge:add_table', 'condition': {'type': 'minecraft:any_of', 'terms': [
    {'type': 'neoforge:loot_table_id', 'loot_table_id': f'minecraft:entities/{e}'} for e in DROPPERS]}, 'table': n('gameplay/chocopedia_drop')})
write(f'data/{NS}/loot_table/gameplay/chocopedia_drop.json', {'type': 'minecraft:entity', 'pools': [{'rolls': 1, 'condition': {
    'type': 'minecraft:random_chance', 'chance': 0.03}, 'entries': [{'type': 'minecraft:item', 'name': n('chocopedia')}]}]})

# ---------------------------------------------------------------- language: GUI and Chocopedia
lang.update({
    'gui.chococraft.rename': 'Rename', 'gui.chococraft.name_hidden': 'Name Hidden', 'gui.chococraft.name_shown': 'Name Shown',
    'gui.chococraft.follow': 'follow', 'gui.chococraft.wander': 'wander', 'gui.chococraft.stay': 'stay',
    'gui.chococraft.confirm': 'Confirm', 'gui.chococraft.change_owner': 'change owner', 'gui.chococraft.drop_gear': 'Drop Gear',
    'gui.chococraft.owner': 'owner: %s', 'gui.chococraft.health': 'health: %s/%s', 'gui.chococraft.gender_breed': '%s (%s)',
    'gui.chococraft.male': 'Male', 'gui.chococraft.female': 'Female', 'gui.chococraft.can_breed': 'can breed',
    'gui.chococraft.cannot_breed': 'cannot breed', 'gui.chococraft.book_title': 'The Chocopedia', 'gui.chococraft.book_author': 'by Torojima',
    'gui.chococraft.name_chocobo': 'Name your Chocobo', 'gui.chococraft.name_chicobo': 'Name your Chicobo',
    'gui.chococraft.new_name': 'New name:', 'gui.chococraft.accept': 'Accept', 'gui.chococraft.select_owner': 'Select the new owner',
})
if os.path.exists(PAGES_SRC):
    src = open(PAGES_SRC).read()
    pages = re.findall(r'\.add\(\s*"((?:[^"\\]|\\.)*)"\s*\)', src, re.S)
    for i, p in enumerate(pages[1:], start=1):
        lang[f'{NS}.chocopedia.page.{i}'] = p.encode().decode('unicode_escape')
else:
    for k, v in old_lang.items():
        if k.startswith(f'{NS}.chocopedia.page.'):
            lang[k] = v
write(f'assets/{NS}/lang/en_us.json', dict(sorted(lang.items())))

with open(ROOT + '/original-assets.txt', 'w') as f:
    f.write('# path in zzzChococraft_2.8.8.zip -> path in the ported jar (generated by tools/gen.py)\n')
    for src, dst in assets:
        f.write(f'{src} -> {dst}\n')
print(f'{len(assets)} assets, {len(lang)} language entries')
