#!/usr/bin/env python3
"""Generates the item/block assets and data of the Better Dungeons port (Teil 13: items, blocks, projectiles) and the
texture/sound part of original-assets.txt.

Sources: the item list mirrors betterdungeons.BDItems (BetterDungeons.registerItems of 1.5.2), the recipes
BetterDungeons.registerRecipes, the names Chocolate/lang/*.txt of the pack's data archive (.reference/betterdungeons-data1).
Files written: assets/betterdungeons/{items,models,blockstates,equipment,lang,sounds.json}, data/betterdungeons/{recipe,
loot_table,tags}, data/minecraft/tags/item/enchantable/*. Run from anywhere: python3 mods/betterdungeons/tools/gen.py
"""
import json, os, re, shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
REPO = os.path.dirname(os.path.dirname(ROOT))
RES = ROOT + '/src/main/resources'
LANG_DIR = os.path.join(REPO, '.reference/betterdungeons-data1/pack-files/Chocolate/lang')
NS = 'betterdungeons'

# generated folders (the spawner's blockstate/model from Teil 12 stay)
for d in ('assets/betterdungeons/items', 'assets/betterdungeons/models/item', 'assets/betterdungeons/equipment',
          'data/betterdungeons/recipe', 'data/betterdungeons/tags', 'data/betterdungeons/loot_table/blocks', 'data/minecraft/tags/item'):
    # the loot tables of the mobs and the entity tags come from tools/gen_humans.py (and the other mob generators)
    shutil.rmtree(os.path.join(RES, d), ignore_errors=True)


def write(path, obj):
    full = os.path.join(RES, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, 'w', encoding='utf-8') as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write('\n')


def n(x):
    return x if ':' in x else f'{NS}:{x}'


def snake(name):
    return re.sub(r'(?<=[a-z0-9])([A-Z])', r'_\1', name).lower()


textures = {}  # original texture (textures/items/bd/X.png ...) -> target in the jar


def item_tex(orig):
    """bd/<orig> of 1.5.2 -> betterdungeons:item/<snake>."""
    textures[f'textures/items/bd/{orig}.png'] = f'assets/{NS}/textures/item/{snake(orig)}.png'
    return f'{NS}:item/{snake(orig)}'


def tint(color):
    """An RGB int as the signed ARGB of a constant tint."""
    v = 0xFF000000 | color
    return v - (1 << 32) if v >= 1 << 31 else v


def model(name, layers, parent='minecraft:item/generated'):
    write(f'assets/{NS}/models/item/{name}.json', {'parent': parent, 'textures': {f'layer{i}': t for i, t in enumerate(layers)}})


def plain_def(name, tints=None, model_name=None):
    m = {'type': 'minecraft:model', 'model': f'{NS}:item/{model_name or name}'}
    if tints:
        m['tints'] = tints
    return m


def simple(name, orig, handheld=False):
    model(name, [item_tex(orig)], 'minecraft:item/handheld' if handheld else 'minecraft:item/generated')
    write(f'assets/{NS}/items/{name}.json', {'model': plain_def(name)})


ELEMENTS = ['water', 'poison', 'ice', 'fire', 'dark', 'electric', 'wind', 'life']
KINDS = ['end', 'nether', 'dwarf', 'zombie', 'wooden', 'pirate', 'shadows', 'round_wood', 'green', 'ensaimada', 'squid', 'minotaur',
         'colorful']

# ---------------------------------------------------------------- item models
for i, e in enumerate(ELEMENTS):
    simple(f'{e}_essence', f't{i}')
    simple(f'{e}_grenade', f'g{i}')
    simple(f'{e}_arrow', f'a{i}')
simple('magic_essence', 't8')
for i, m in enumerate(['staff', 'turtle_scale', 'handle']):
    simple(m, f'mat{i}')
for name, orig in [('quiver', 'quiver'), ('bullet', 'bullets'), ('revolver', 'revolver'), ('golden_feather', 'goldenFeather'),
                   ('cursed_bone', 'cursedBone'), ('scroll_of_escape', 'soe'), ('scouter', 'scouter'), ('cloud_boots', 'cloudBoots'),
                   ('turtle_helmet', 'turtle0'), ('turtle_plate', 'turtle1'), ('turtle_pants', 'turtle2'), ('turtle_boots', 'turtle3'),
                   ('controller', 'i0'), ('path_marker', 'pathMap')]:
    simple(name, orig)
for name, orig in [('staff_life', 'staffLife'), ('staff_water', 'staffWater'), ('staff_poison', 'staffPoison'), ('staff_ice', 'staffIce'),
                   ('staff_fire', 'staffFire'), ('staff_dark', 'staffDark'), ('staff_electric', 'staffElectric'), ('staff_wind', 'staffWind'),
                   ('ninja_dagger', 'daggerNinja'), ('walker_sword', 'swordEnd'), ('monking_sword', 'swordMonking'),
                   ('magic_pickaxe', 'pickaxeMagic'), ('dwarf_spear', 'spearDwarf'), ('iron_dagger', 'daggerIron'),
                   ('diamond_dagger', 'daggerDiamond'), ('iron_spear', 'spearIron'), ('diamond_spear', 'spearDiamond'),
                   ('iron_bigsword', 'bigSwordIron'), ('diamond_bigsword', 'bigSwordDiamond'), ('earth_sword', 'bigSwordEarth'),
                   ('moon_sword', 'swordMoonLight'), ('defense_sword', 'swordDefensive'), ('trickster_dagger', 'daggerPirate')]:
    simple(name, orig, handheld=True)
for i, k in enumerate(KINDS):
    simple(f'{k}_shield', f's{i}')
    # ItemBanner: two passes, the holder and the shield's emblem
    model(f'{k}_banner', [item_tex('bannerHolder'), item_tex(f's{i}')])
    write(f'assets/{NS}/items/{k}_banner.json', {'model': plain_def(f'{k}_banner')})

# ItemBag.getColorFromItemStack, damage 0
model('bag', [item_tex('bag')])
write(f'assets/{NS}/items/bag.json', {'model': plain_def('bag', [{'type': 'minecraft:constant', 'value': tint(13789470)}])})

# ItemCapsule: capsule + capsule0, the second pass tinted 34184 while full
model('capsule', [item_tex('capsule'), item_tex('capsule0')])
write(f'assets/{NS}/items/capsule.json', {'model': {
    'type': 'minecraft:condition', 'property': 'minecraft:has_component', 'component': 'minecraft:custom_data',
    'on_true': plain_def('capsule', [{'type': 'minecraft:constant', 'value': -1}, {'type': 'minecraft:constant', 'value': tint(34184)}]),
    'on_false': plain_def('capsule')}})

# ItemHookShoot: the loaded hook over the item while no hook is out (damage 0)
for name, orig, hook in [('hookshot', 'hookShoot', 'hook'), ('longshot', 'hookLong', 'hook'), ('manual_hook', 'hookManual', 'hook'),
                         ('spider_hook', 'hookSpider', 'hookWeb')]:
    model(name, [item_tex(hook), item_tex(orig)], 'minecraft:item/handheld')
    model(name + '_fired', [item_tex(orig)], 'minecraft:item/handheld')
    write(f'assets/{NS}/items/{name}.json', {'model': {
        'type': 'minecraft:condition', 'property': 'minecraft:has_component', 'component': 'minecraft:custom_data',
        'on_true': plain_def(name, model_name=name + '_fired'), 'on_false': plain_def(name)}})

# ItemBowBase: bow0 idle, bow1..3 while drawing (vanilla bow thresholds and display)
for i, suffix in enumerate(['', '_pulling_0', '_pulling_1', '_pulling_2']):
    model('long_bow' + suffix, [item_tex(f'bow{i}')], 'minecraft:item/bow')
write(f'assets/{NS}/items/long_bow.json', {'model': {
    'type': 'minecraft:condition', 'property': 'minecraft:using_item', 'on_false': plain_def('long_bow'),
    'on_true': {'type': 'minecraft:range_dispatch', 'property': 'minecraft:use_duration', 'scale': 0.05,
                'entries': [{'threshold': 0.65, 'model': plain_def('long_bow', model_name='long_bow_pulling_1')},
                            {'threshold': 0.9, 'model': plain_def('long_bow', model_name='long_bow_pulling_2')}],
                'fallback': plain_def('long_bow', model_name='long_bow_pulling_0')}}})

# ItemEggBD.getIconFromDamage: shields, boss icons b0..b5, other items; 116 had no icon (shown with the controller's)
EGGS = [(100, 'ArmoredSkeleton', 'Skeleton', 's4'), (101, 'ArmoredZombie', 'Zombie', 's3'), (102, 'Pirate', 'Pirate', 's5'),
        (103, 'Specter', 'Specter', 's8'), (104, 'Dwarf', 'Dwarf', 's2'), (105, 'Walker', 'Shadow', 's6'),
        (106, 'Goblin', 'Goblin green', 's7'), (107, 'SnowGoblin', 'Goblin blue', 's7'), (108, 'ArmoredPigZombie', 'Pig man', 's1'),
        (109, 'ArmoredEnderman', 'Enderman', 's0'), (110, 'Monkey', 'Monkey', 's9'), (111, 'Triton', 'Triton', 's10'),
        (112, 'Gremlin', 'Gremlin', 's12'), (113, 'GolemSmall', 'Golem', 's2'), (114, 'OgreArmored', 'Ogre', 's9'),
        (115, 'MinotaurArmored', 'Minotaur', 's11'), (116, 'FriendlyHuman', 'Mercenary', 'i0'),
        (500, 'SpiderBoss', 'Spider', 'b0'), (501, 'Monking', 'Monking', 'b2'), (502, 'TurtleBoss', 'Turtle', 'b3'),
        (503, 'IceBull', 'Bull', 'b4'), (504, 'GolemBoss', 'Exterminator Prototype', 'minecraft:item/iron_ingot'),
        (505, 'PirateBoss', 'Pirate Captain', 'daggerPirate'), (506, 'WalkerBoss', 'Walker King', 'swordEnd'),
        (507, 'Necromancer', 'Necromancer', 'staffPoison'), (508, 'Liche', 'Liche', 'staffDark'), (509, 'PigMage', 'Pig Mage', 'staffFire'),
        (510, 'SpecterBoss', 'Specter Lord', 'swordDefensive'), (511, 'GremlinBoss', 'Gremlin Shaman', 'staffWind'),
        (512, 'MinotaurBoss', 'Minotaur Boss', 'bigSwordEarth'), (513, 'goblinBoss', 'Goblin Boss', 'swordMoonLight'),
        (514, 'Wyvern', 'Dragon', 'b5')]
ENTITY_PATHS = {}
with open(os.path.join(ROOT, 'src/main/java/betterdungeons/api/BDEntityIds.java'), encoding='utf-8') as f:
    for old, path in re.findall(r'\{"(\w+)", "(\w+)"\}', f.read()):
        ENTITY_PATHS[old] = path
lang = {}
for dmg, mob, title, tex in EGGS:
    name = 'egg_' + ENTITY_PATHS[mob]
    model(name, [tex if ':' in tex else item_tex(tex)])
    write(f'assets/{NS}/items/{name}.json', {'model': plain_def(name)})
    lang[f'item.{NS}.{name}'] = title

# ---------------------------------------------------------------- blocks: capsule stations (BlockCapsule: bottom e2, top e3, sides e4)
STATIONS = [('editable', 'Editable capsule station'), ('storage', 'Storage capsule station'), ('small', 'Small capsule station'),
            ('medium', 'Medium capsule station'), ('big', 'Big capsule station')]
for orig, target in [('e2', 'capsule_station_bottom'), ('e3', 'capsule_station_top'), ('e4', 'capsule_station_side')]:
    textures[f'textures/blocks/bd/{orig}.png'] = f'assets/{NS}/textures/block/{target}.png'
write(f'assets/{NS}/models/block/capsule_station.json', {'parent': 'minecraft:block/cube_bottom_top', 'textures': {
    'bottom': f'{NS}:block/capsule_station_bottom', 'top': f'{NS}:block/capsule_station_top', 'side': f'{NS}:block/capsule_station_side'}})
for kind, title in STATIONS:
    name = f'capsule_station_{kind}'
    write(f'assets/{NS}/blockstates/{name}.json', {'variants': {'': {'model': f'{NS}:block/capsule_station'}}})
    write(f'assets/{NS}/items/{name}.json', {'model': {'type': 'minecraft:model', 'model': f'{NS}:block/capsule_station'}})
    write(f'data/{NS}/loot_table/blocks/{name}.json', {'type': 'minecraft:block', 'pools': [{
        'condition': {'type': 'minecraft:survives_explosion'}, 'entries': [{'type': 'minecraft:item', 'name': f'{NS}:{name}'}], 'rolls': 1}],
        'random_sequence': f'{NS}:blocks/{name}'})
    lang[f'block.{NS}.{name}'] = title

# ---------------------------------------------------------------- entity and armour textures (bdimg)
for orig, target in [('items', 'entity/items'), ('arrows', 'entity/arrows'), ('arrows2', 'entity/arrows2'),
                     ('turtle_1', 'entity/equipment/humanoid/turtle'), ('turtle_2', 'entity/equipment/humanoid_leggings/turtle'),
                     ('cloud_1', 'entity/equipment/humanoid/cloud')]:
    textures[f'bdimg/{orig}.png'] = f'assets/{NS}/textures/{target}.png'
# the textures of the bosses that are no EntityHuman (Strang b2, betterdungeons.mob; the wyvern's dragonbd.png was missing)
for orig, target in [('icebull', 'ice_bull'), ('mandril', 'monking'), ('golemBoss', 'golem_boss'), ('spiderBoss', 'spider_boss'),
                     ('turtle', 'turtle_boss')]:
    textures[f'bdimg/{orig}.png'] = f'assets/{NS}/textures/entity/{target}.png'
write(f'assets/{NS}/equipment/turtle.json', {'layers': {'humanoid': [{'texture': f'{NS}:turtle'}],
                                                        'humanoid_leggings': [{'texture': f'{NS}:turtle'}]}})
write(f'assets/{NS}/equipment/cloud.json', {'layers': {'humanoid': [{'texture': f'{NS}:cloud'}]}})

# ---------------------------------------------------------------- sounds (EventHandlerBD.onSoundLoad)
SOUNDS = ['handgun', 'wind', 'smallMonsterDead', 'smallMonsterSpeak', 'smallMonsterHurt', 'smallMonsterLaught', 'breathLoud',
          'breathDouble', 'magic', 'bubble']
sounds = {}
sound_files = {}
for s in SOUNDS:
    sounds[snake(s)] = {'sounds': [f'{NS}:{snake(s)}'], 'subtitle': f'subtitles.{NS}.{snake(s)}'}
    sound_files[f'Chocolate/sounds/{s}.ogg'] = f'assets/{NS}/sounds/{snake(s)}.ogg'
    lang[f'subtitles.{NS}.{snake(s)}'] = re.sub(r'(?<=[a-z])([A-Z])', r' \1', s).capitalize()
write(f'assets/{NS}/sounds.json', sounds)

# ---------------------------------------------------------------- recipes (registerRecipes; enableCapsulesInSurvival = true)
def item(x):
    return n(x)


def shaped(name, pattern, key, result, count=1, category='misc'):
    r = {'type': 'minecraft:crafting_shaped', 'category': category, 'key': {k: item(v) for k, v in key.items()}, 'pattern': pattern,
         'result': {'id': n(result)}}
    if count != 1:
        r['result']['count'] = count
    write(f'data/{NS}/recipe/{name}.json', r)


def shapeless(name, ingredients, result, count=1, category='misc'):
    r = {'type': 'minecraft:crafting_shapeless', 'category': category, 'ingredients': [item(i) for i in ingredients], 'result': {'id': n(result)}}
    if count != 1:
        r['result']['count'] = count
    write(f'data/{NS}/recipe/{name}.json', r)


RING = ['xxx', 'xyx', 'xxx']
for i, e in enumerate(ELEMENTS):
    shaped(f'{e}_arrow', RING, {'x': 'minecraft:arrow', 'y': f'{e}_essence'}, f'{e}_arrow', 8, 'equipment')
    shaped(f'{e}_grenade', RING, {'x': 'minecraft:glass_bottle', 'y': f'{e}_essence'}, f'{e}_grenade', 8, 'equipment')
shapeless('bullet', ['minecraft:cobblestone', 'minecraft:iron_ingot'], 'bullet', 9, 'equipment')
shapeless('magic_essence', [f'{e}_essence' for e in ELEMENTS], 'magic_essence')
shapeless('staff', ['minecraft:stick', 'minecraft:diamond', 'magic_essence'], 'staff')
for e, staff in [('water', 'staff_water'), ('poison', 'staff_poison'), ('ice', 'staff_ice'), ('fire', 'staff_fire'), ('dark', 'staff_dark'),
                 ('electric', 'staff_electric'), ('wind', 'staff_wind'), ('life', 'staff_life')]:
    shapeless(staff, ['staff', f'{e}_essence'], staff, 1, 'equipment')
shaped('bag', ['xzx', 'xyx', 'xxx'], {'x': 'minecraft:leather', 'y': 'minecraft:chest', 'z': 'minecraft:string'}, 'bag')
shapeless('quiver', ['bag', 'minecraft:arrow'], 'quiver')
shapeless('scroll_of_escape', ['minecraft:ender_pearl', 'minecraft:paper'], 'scroll_of_escape')
shaped('golden_feather', RING, {'x': 'minecraft:gold_ingot', 'y': 'minecraft:feather'}, 'golden_feather')
shaped('cloud_boots', ['   ', 'x x', 'x x'], {'x': 'golden_feather'}, 'cloud_boots', 1, 'equipment')
for name, pattern in [('turtle_helmet', ['xxx', 'x x', '   ']), ('turtle_plate', ['x x', 'xxx', 'xxx']), ('turtle_pants', ['xxx', 'x x', 'x x']),
                      ('turtle_boots', ['   ', 'x x', 'x x'])]:
    shaped(name, pattern, {'x': 'turtle_scale'}, name, 1, 'equipment')
shapeless('capsule', ['minecraft:glass_bottle', 'minecraft:piston'], 'capsule')
shaped('capsule_station_storage', ['xzx', 'xyx', 'xxx'], {'x': 'minecraft:obsidian', 'y': 'minecraft:chest', 'z': 'minecraft:emerald'},
       'capsule_station_storage')
shaped('hookshot', ['xzx', 'xyx', 'xxx'], {'x': 'minecraft:diamond', 'y': 'minecraft:piston', 'z': 'minecraft:arrow'}, 'hookshot', 1, 'equipment')
shapeless('longshot', ['hookshot', 'hookshot'], 'longshot', 1, 'equipment')
shapeless('manual_hook', ['longshot', 'longshot'], 'manual_hook', 1, 'equipment')
shaped('handle', ['xyx', ' x '], {'x': 'minecraft:stick', 'y': 'minecraft:iron_ingot'}, 'handle')
for metal, ingot in [('iron', 'minecraft:iron_ingot'), ('diamond', 'minecraft:diamond')]:
    shaped(f'{metal}_dagger', [' y ', ' x '], {'x': 'handle', 'y': ingot}, f'{metal}_dagger', 1, 'equipment')
    shaped(f'{metal}_bigsword', [' y ', ' y ', ' x '], {'x': 'handle', 'y': ingot}, f'{metal}_bigsword', 1, 'equipment')
    shaped(f'{metal}_spear', [' y ', ' z ', ' x '], {'x': 'handle', 'y': ingot, 'z': 'minecraft:stick'}, f'{metal}_spear', 1, 'equipment')

# ---------------------------------------------------------------- tags
MELEE = ['ninja_dagger', 'walker_sword', 'monking_sword', 'dwarf_spear', 'iron_dagger', 'diamond_dagger', 'iron_spear', 'diamond_spear',
         'iron_bigsword', 'diamond_bigsword', 'earth_sword', 'moon_sword', 'defense_sword', 'trickster_dagger']
ARMOR = {'head_armor': ['scouter', 'turtle_helmet'], 'chest_armor': ['turtle_plate'], 'leg_armor': ['turtle_pants'],
         'foot_armor': ['cloud_boots', 'turtle_boots']}


def tag(path, values):
    write(path, {'values': [n(v) for v in values]})


tag('data/minecraft/tags/item/enchantable/melee_weapon.json', MELEE)
tag('data/minecraft/tags/item/enchantable/bow.json', ['long_bow'])
for k, v in ARMOR.items():
    tag(f'data/minecraft/tags/item/enchantable/{k}.json', v)
tag('data/minecraft/tags/item/enchantable/equippable.json', sum(ARMOR.values(), []))
tag('data/minecraft/tags/item/enchantable/durability.json', MELEE + ['long_bow'] + sum(ARMOR.values(), []))
# getIsRepairable: the material's item or the magic essence (end weapons, defensive swords); cloud boots with feathers
tag(f'data/{NS}/tags/item/repairs/diamond_weapons.json', ['minecraft:diamond', 'magic_essence'])
tag(f'data/{NS}/tags/item/repairs/iron_weapons.json', ['minecraft:iron_ingot', 'magic_essence'])
tag(f'data/{NS}/tags/item/repairs/cloud_boots.json', ['golden_feather', 'cloud_boots'])

# ---------------------------------------------------------------- lang (Chocolate/lang, items named by their unlocalized name)
NAME_KEYS = {  # item id -> 1.5.2 unlocalized name (item.<x>.name)
    'bag': 'bag', 'quiver': 'quiver', 'long_bow': 'longBow', 'revolver': 'revolver', 'bullet': 'bullet', 'staff_life': 'staffLife',
    'staff_poison': 'staffPoison', 'staff_water': 'staffWater', 'staff_ice': 'staffIce', 'staff_fire': 'staffFire', 'staff_dark': 'staffDark',
    'staff_electric': 'staffElectric', 'staff_wind': 'staffWind', 'ninja_dagger': 'ninjaDagger', 'walker_sword': 'walkerSword',
    'monking_sword': 'swordMonking', 'golden_feather': 'goldenFeather', 'cursed_bone': 'cursedBone', 'hookshot': 'hookshoot',
    'longshot': 'longshoot', 'manual_hook': 'manualshoot', 'spider_hook': 'spiderhook', 'scroll_of_escape': 'ScrollOfEscape',
    'cloud_boots': 'cloudBoots', 'turtle_helmet': 'turtleHelmet', 'turtle_plate': 'turtlePlate', 'turtle_pants': 'turtlePants',
    'turtle_boots': 'turtleBoots', 'dwarf_spear': 'dwarfSpear', 'iron_dagger': 'daggerIron', 'diamond_dagger': 'daggerDiamond',
    'iron_spear': 'spearIron', 'diamond_spear': 'spearDiamond', 'iron_bigsword': 'bigswordIron', 'diamond_bigsword': 'bigswordDiamond',
    'staff': 'staff', 'turtle_scale': 'turtleScale', 'handle': 'handle', 'earth_sword': 'earthSword', 'defense_sword': 'defenseSword',
    'moon_sword': 'moonSword', 'trickster_dagger': 'pirateDagger', 'magic_essence': 'magicEssence'}
ESSENCE = ['waterEssence', 'poisonEssence', 'iceEssence', 'fireEssence', 'darkEssence', 'electricEssence', 'windEssence', 'lifeEssence']
GRENADE = ['waterGrenade', 'poisonGrenade', 'iceGrenade', 'fireGrenade', 'darkGrenade', 'electricGrenade', 'windGrenade', 'lifeGrenade']
ARROW = ['arrowWater', 'arrowPoison', 'arrowIce', 'arrowFire', 'arrowDarkness', 'arrowElectric', 'arrowWind', 'arrowLife']
for i, e in enumerate(ELEMENTS):
    NAME_KEYS[f'{e}_essence'] = ESSENCE[i]
    NAME_KEYS[f'{e}_grenade'] = GRENADE[i]
    NAME_KEYS[f'{e}_arrow'] = ARROW[i]
for k in KINDS:
    NAME_KEYS[f'{k}_shield'] = 'shied'
    NAME_KEYS[f'{k}_banner'] = 'banner'
FIXED = {  # names the 1.5.2 code returned itself (getItemDisplayName)
    'magic_pickaxe': 'Super Tool!(Creative only)', 'scouter': 'Scouter', 'capsule': 'Empty capsule', 'controller': 'Mercenary controller',
    'path_marker': 'Path marker'}
MESSAGES = {
    f'item.{NS}.controller.assigned': 'Assigned §2%s§f to this item', f'item.{NS}.egg.assigned': 'Assigned §2%s§f to this item',
    f'item.{NS}.egg.fight': 'Left click to another entity to start a fight!',
    f'item.{NS}.egg.lost': 'Assigned entity not found, left click to assign a new entity',
    f'item.{NS}.egg.missing': '%s: this mob is not in the game yet', f'item.{NS}.path_marker.full': "Can't add more points",
    f'item.{NS}.path_marker.too_far': 'Too far from previous point', f'item.{NS}.path_marker.new_path': 'New path for §2%s',
    f'entity.{NS}.wyvern': 'Wyvern', f'entity.{NS}.summoned_bait': 'Bait',
    f'container.{NS}.bag': 'Bag', f'container.{NS}.quiver': 'Quiver', f'block.{NS}.spawner': 'BetterDungeonSpwaner'}


def read_lang(path):
    props = {}
    with open(path, encoding='utf-8-sig') as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith('#') or '=' not in line:
                continue
            k, v = line.split('=', 1)
            props[k.strip()] = v.strip()
    return props


for src, target in [('en_US', 'en_us'), ('es_ES', 'es_es'), ('zh_CN', 'zh_cn')]:
    props = read_lang(os.path.join(LANG_DIR, src + '.txt'))
    out = dict(lang) if target == 'en_us' else {}
    for item_id, key in NAME_KEYS.items():
        v = props.get(f'item.{key}.name')
        if v is None and target == 'en_us':
            # ItemMagicGrenade asked for "darknGrenade", which no language file had
            v = 'Darkness Grenade' if key == 'darkGrenade' else 'Magic Essence' if key == 'magicEssence' else None
        if v is not None:
            out[f'item.{NS}.{item_id}'] = v
    if target == 'en_us':
        # mobs the language files of 1.5.2 did not name (they showed the raw key)
        for old, v in {'OrcArmored': 'Orc', 'goblinBoss': 'Goblin Boss', 'FriendlyHuman': 'Mercenary'}.items():
            out.setdefault(f'entity.{NS}.{ENTITY_PATHS[old]}', v)
        for item_id, v in FIXED.items():
            out[f'item.{NS}.{item_id}'] = v
        out.update(MESSAGES)
    for k, v in props.items():
        m = re.match(r'entity\.BetterDungeons\.(\w+)\.name$', k)
        if m and m.group(1) in ENTITY_PATHS:
            out[f'entity.{NS}.{ENTITY_PATHS[m.group(1)]}'] = v
        m = re.match(r'(bd\.\w+)\.name$', k)
        if m:
            out[m.group(1)] = v
        m = re.match(r'itemGroup\.(\w+)\.name$', k)
        if m:
            out[f'itemGroup.{m.group(1)}'] = v.replace('Building', '')
    write(f'assets/{NS}/lang/{target}.json', dict(sorted(out.items())))

# ---------------------------------------------------------------- textures of the human mobs (betterdungeons.mob.BDHumans, strand b1)
for orig in ['dwarf', 'pirate', 'pirateBoss', 'shadow', 'goblinGreen', 'goblinBlue', 'goblin', 'goblinShaman', 'snake', 'mandrilSmall',
             'golemSmall', 'ogre', 'orc', 'minotaur', 'specterBoss', 'necromancer', 'liche', 'pigMage', 'enderman_cloth', 'enderman_chain',
             'enderman_iron', 'enderman_gold', 'enderman_diamond']:
    textures[f'bdimg/{orig}.png'] = f'assets/{NS}/textures/entity/{snake(orig)}.png'

# ---------------------------------------------------------------- original-assets.txt
with open(os.path.join(ROOT, 'original-assets.txt'), encoding='utf-8') as f:
    old = f.read()
data_part = old[old.index('[betterdungeons-data1]'):]
data1, data2 = data_part.split('[betterdungeons-data2]')
lines = ['# Better Dungeons 1.09 -> path in the ported jar',
         '# Item, block, projectile and armour textures (generated by tools/gen.py from betterdungeons-v1.09b.zip / BetterDungeons.zip)']
lines += [f'{k} -> {v}' for k, v in sorted(textures.items())]
lines += ['# The templates (Building), the dungeon and the chest configuration come from the data archives of the pack',
          '# (the Chocolate folder next to config/ in the original installation), read by betterdungeons.gen.BDFiles.',
          '# The sounds of Chocolate/sounds (EventHandlerBD.onSoundLoad) go to assets/betterdungeons/sounds.',
          '[betterdungeons-data1]']
lines += [f'{k} -> {v}' for k, v in sorted(sound_files.items())]
lines += [l for l in data1.splitlines()[1:] if l.strip() and not l.startswith('Chocolate/sounds/')]
lines += ['[betterdungeons-data2]'] + [l for l in data2.splitlines() if l.strip()]
with open(os.path.join(ROOT, 'original-assets.txt'), 'w', encoding='utf-8') as f:
    f.write('\n'.join(lines) + '\n')
print('textures', len(textures), 'lang', len(lang))
