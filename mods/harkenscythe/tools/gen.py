#!/usr/bin/env python3
"""Generates the assets and data JSON, the language file and original-assets.txt of the Harken Scythe core port
(mods/harkenscythe). Overwrites src/main/resources/assets and src/main/resources/data. Run from anywhere:
    python3 mods/harkenscythe/tools/gen.py
The vanilla cauldron model is not needed: the crucible model is built here after RenderHSCrucible."""
import json, os, shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, 'src/main/resources')
NS = 'harkenscythe_core'
SRC = 'mods/mod_HarkenScythe/textures'
for d in ('assets', 'data'):
    shutil.rmtree(os.path.join(RES, d), ignore_errors=True)

assets = []      # (path in the archive, path in the jar)
lang = {}
tags = {}        # 'item:ns:path' -> values
recipes = {}


def n(x):
    return f'{NS}:{x}'


def write(path, obj):
    full = os.path.join(RES, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, 'w') as f:
        json.dump(obj, f, indent=2)
        f.write('\n')


def tex(src, dst):
    """An original texture (src below textures/) in the jar at assets/<ns>/textures/<dst>.png; returns ns:dst."""
    pair = (f'{SRC}/{src}', f'assets/{NS}/textures/{dst}.png')
    if pair not in assets:
        assets.append(pair)
    return n(dst)


def tag(kind, name, *values):
    lst = tags.setdefault(f'{kind}:{name}', [])
    for v in values:
        if v not in lst:
            lst.append(v)


def argb(rgb):
    v = 0xFF000000 | rgb
    return v - (1 << 32)


# ------------------------------------------------------------------------------------------------ items
def item_def(name, model):
    write(f'assets/{NS}/items/{name}.json', {'model': model})


def model(ref, tints=None):
    m = {'type': 'minecraft:model', 'model': ref}
    if tints:
        m['tints'] = tints
    return m


def flat(name, title, texture, parent='minecraft:item/generated', layers=None):
    layers = layers or [texture]
    write(f'assets/{NS}/models/item/{name}.json', {'parent': parent, 'textures': {f'layer{i}': t for i, t in enumerate(layers)}})
    item_def(name, model(n(f'item/{name}')))
    if title:
        lang[f'item.{NS}.{name}'] = title


def icon(old):
    return tex(f'items/item.{old}.png', f'item/{old_to_new(old)}')


def old_to_new(old):
    return ICON_NAMES.get(old, old)


ICON_NAMES = {}

MATS = [('wooden', 'Wood', 'Wooden', '#minecraft:planks'), ('stone', 'Stone', 'Stone', 'minecraft:cobblestone'),
        ('iron', 'Iron', 'Iron', 'minecraft:iron_ingot'), ('diamond', 'Diamond', 'Diamond', 'minecraft:diamond'),
        ('golden', 'Gold', 'Golden', 'minecraft:gold_ingot'), ('livingmetal', 'Livingmetal', 'Livingmetal', n('livingmetal_ingot')),
        ('biomass', 'Biomass', 'Biomass', n('biomass'))]
WEAPONS = []
for mat, old, title, material in MATS:
    for kind, oldkind, pattern in (('scythe', 'Scythe', ['XXX', '  *', ' * ']), ('glaive', 'Glaive', [' XX', 'X*X', '*X '])):
        name = f'{mat}_{kind}'
        ICON_NAMES[f'HS{oldkind}{old}'] = name
        flat(name, f'{title} {kind.capitalize()}', icon(f'HS{oldkind}{old}'), 'minecraft:item/handheld')
        recipes[name] = ('shaped', pattern, {'X': material, '*': 'minecraft:stick'}, n(name))
        WEAPONS.append(name)
ICON_NAMES['HSScytheSoulReaper'] = 'soul_reaper'
flat('soul_reaper', 'Soul Reaper', icon('HSScytheSoulReaper'), 'minecraft:item/handheld')
WEAPONS.append('soul_reaper')
for w in WEAPONS:
    for t in ('sword', 'sharp_weapon', 'melee_weapon', 'weapon', 'fire_aspect', 'durability'):
        tag('item', f'minecraft:enchantable/{t}', n(w))
    tag('item', f'{NS}:enchantable/weapon', n(w))
tag('item', f'{NS}:repairs/soul_reaper', 'minecraft:bone')

# keepers: the fill level picks one of four icons (getIconFromDamage)
KEEPERS = [('essence_keeper', 'Essence Keeper', 'HSEssenceKeeper', 0), ('soulkeeper', 'Soulkeeper', 'HSSoulkeeper', 20),
           ('bloodkeeper', 'Bloodkeeper', 'HSBloodkeeper', 40), ('essence_vessel', 'Essence Vessel', 'HSEssenceVessel', 0),
           ('soul_vessel', 'Soul Vessel', 'HSSoulVessel', 40), ('blood_vessel', 'Blood Vessel', 'HSBloodVessel', 80)]
for name, title, old, cap in KEEPERS:
    lang[f'item.{NS}.{name}'] = title
    if cap == 0:
        ICON_NAMES[old] = name
        flat(name, None, icon(old))
        continue
    models = []
    for i in range(4):
        ICON_NAMES[f'{old}_{i}'] = f'{name}_{i}'
        write(f'assets/{NS}/models/item/{name}_{i}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': icon(f'{old}_{i}')}})
        models.append(model(n(f'item/{name}_{i}')))
    q = cap // 4
    # damage 0: full (_3); up to a quarter: _2; up to half: _1; more: _0
    item_def(name, {'type': 'minecraft:range_dispatch', 'property': 'minecraft:damage', 'normalize': False,
                    'entries': [{'threshold': 0, 'model': models[3]}, {'threshold': 1, 'model': models[2]},
                                {'threshold': q + 1, 'model': models[1]}, {'threshold': 2 * q + 1, 'model': models[0]}],
                    'fallback': models[3]})

# books
for name, title, old in (('book_of_shadows', 'Book of Shadows', 'HSBookShadow'), ('book_of_carnage', 'Book of Carnage', 'HSBookCarnage'),
                         ('necronomicon_page', 'Necronomicon Page', 'HSBookNecronomiconPage')):
    ICON_NAMES[old] = name
    flat(name, title, icon(old))
ICON_NAMES['HSBookNecronomicon'] = 'necronomicon'
ICON_NAMES['HSBookNecronomiconOpen'] = 'necronomicon_open'
write(f'assets/{NS}/models/item/necronomicon.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': icon('HSBookNecronomicon')}})
write(f'assets/{NS}/models/item/necronomicon_open.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': icon('HSBookNecronomiconOpen')}})
item_def('necronomicon', {'type': 'minecraft:condition', 'property': 'minecraft:using_item',
                          'on_true': model(n('item/necronomicon_open')), 'on_false': model(n('item/necronomicon'))})
lang[f'item.{NS}.necronomicon'] = 'Necronomicon'

# skulls: ItemHSSkull, damage values 0-15 (item icon, head texture, name)
SKULLS = [('spider', 'spider', 'spider', 'Spider'), ('cave_spider', 'cavespider', 'cavespider', 'Cave Spider'),
          ('enderman', 'enderman', 'enderman', 'Enderman'), ('pigman_zombie', 'pigzombie', 'pigmanzombie', 'Pigman Zombie'),
          ('villager_zombie', 'zombie_villager', 'villagerzombie', 'Villager Zombie'), ('villager', 'villager', 'villager', 'Villager'),
          ('cow', 'cow', 'cow', 'Cow'), ('mooshroom', 'mooshroom', 'mooshroom', 'Mooshroom'), ('pig', 'pig', 'pig', 'Pig'),
          ('squid', 'squid', 'squid', 'Squid'), ('sheep', 'sheep', 'sheep', 'Sheep'), ('wolf', 'wolf', 'wolf', 'Wolf'),
          ('ocelot', 'ozelot', 'ocelot', 'Ocelot'), ('ender_dragon', 'enderdragon', 'enderdragon', 'Ender Dragon'),
          ('bat', 'bat', 'bat', 'Bat'), ('chicken', 'chicken', 'chicken', 'Chicken')]
for sid, old_icon, head, title in SKULLS:
    name = f'{sid}_skull'
    ICON_NAMES[f'HSSkull_{old_icon}'] = name
    flat(name, f'{title} Head', icon(f'HSSkull_{old_icon}'))
    # the head texture, for the block renderer and worn on the helmet layer (getArmorTextureFile)
    tex(f'models/mob/skulls/{head}.png', f'entity/equipment/humanoid/skull_{sid}')
    write(f'assets/{NS}/equipment/skull_{sid}.json', {'layers': {'humanoid': [{'texture': n(f'skull_{sid}')}],
                                                                 'humanoid_baby': [{'texture': n(f'skull_{sid}')}]}})
    tag('item', 'minecraft:enchantable/head_armor', n(name))
    tag('item', 'minecraft:enchantable/equippable', n(name))
for acc in ('enderdragonsnout', 'enderdragonnose', 'enderdragonhorn'):
    tex(f'models/mob/skulls/{acc}.png', f'entity/skull/{acc}')

# talismans, amulets
for name, title, old in (('talisman', 'Talisman', 'HSTalisman'), ('ethereal_talisman', 'Ethereal Talisman', 'HSTalismanEthereal'),
                         ('overworld_amulet', 'Overworld Amulet', 'HSAmuletOverworld'), ('nether_amulet', 'Nether Amulet', 'HSAmuletNether'),
                         ('ethereal_amulet', 'Ethereal Amulet', 'HSAmuletEthereal')):
    ICON_NAMES[old] = name
    flat(name, title, icon(old))
    tag('item', f'{NS}:enchantable/attunable', n(name))

# materials
for name, title, old in (('livingmetal_ingot', 'Livingmetal Ingot', 'HSIngotLivingmetal'), ('biomass', 'Biomass', 'HSBiomass'),
                         ('ectoplasm', 'Ectoplasm', 'HSEctoplasm'), ('abyss_fragment', 'Abyss Fragment', 'HSAbyssFragment'),
                         ('spectral_dye', 'Spectral Dye', 'HSDyeSpectral'), ('creepball', 'Creepball', 'HSCreepball'),
                         ('biomass_seed', 'Biomass Seed', 'HSBiomassSeed'),
                         ('germinated_biomass_seed', 'Germinated Biomass Seed', 'HSBiomassSeedGerminated')):
    ICON_NAMES[old] = name
    flat(name, title, icon(old))
tag('item', 'c:ingots', n('livingmetal_ingot'))
tag('item', 'c:ingots/livingmetal', n('livingmetal_ingot'))
tag('item', f'{NS}:repairs/livingmetal', n('livingmetal_ingot'))
tag('item', f'{NS}:repairs/biomass', n('biomass'))

# livingmetal and biomass tools and armour
TOOLS = [('sword', 'Sword', 'Sword', [' X ', ' X ', ' * '], 'minecraft:swords'), ('shovel', 'Shovel', 'Spade', [' X ', ' * ', ' * '], 'minecraft:shovels'),
         ('pickaxe', 'Pickaxe', 'Pickaxe', ['XXX', ' * ', ' * '], 'minecraft:pickaxes'), ('axe', 'Axe', 'Axe', ['XX ', 'X* ', ' * '], 'minecraft:axes'),
         ('hoe', 'Hoe', 'Hoe', ['XX ', ' * ', ' * '], 'minecraft:hoes')]
ARMOR = [('helmet', 'Helmet', 'Helmet', ['XXX', 'X X'], 'head'), ('chestplate', 'Chestplate', 'Chestplate', ['X X', 'XXX', 'XXX'], 'chest'),
         ('leggings', 'Leggings', 'Leggings', ['XXX', 'X X', 'X X'], 'leg'), ('boots', 'Boots', 'Boots', ['X X', 'X X'], 'foot')]
for mat, title, old, material in (('livingmetal', 'Livingmetal', 'Livingmetal', n('livingmetal_ingot')), ('biomass', 'Biomass', 'Biomass', n('biomass'))):
    for kind, ktitle, oldkind, pattern, t in TOOLS:
        name = f'{mat}_{kind}'
        ICON_NAMES[f'HS{oldkind}{old}'] = name
        flat(name, f'{title} {ktitle}', icon(f'HS{oldkind}{old}'), 'minecraft:item/handheld')
        recipes[name] = ('shaped', pattern, {'X': material, '*': 'minecraft:stick'}, n(name))
        tag('item', t, n(name))
        tag('item', f'{NS}:{mat}_equipment', n(name))
    for kind, ktitle, oldkind, pattern, slot in ARMOR:
        name = f'{mat}_{kind}'
        ICON_NAMES[f'HS{oldkind}{old}'] = name
        flat(name, f'{title} {ktitle}', icon(f'HS{oldkind}{old}'))
        recipes[name] = ('shaped', pattern, {'X': material}, n(name))
        for t in (f'{slot}_armor', 'armor', 'equippable', 'durability'):
            tag('item', f'minecraft:enchantable/{t}', n(name))
        tag('item', f'{NS}:{mat}_equipment', n(name))
        tag('item', f'{NS}:{mat}_armor', n(name))
    tag('item', f'{NS}:{mat}_equipment', n(f'{mat}_scythe'), n(f'{mat}_glaive'))
    write(f'assets/{NS}/equipment/{mat}.json', {'layers': {
        'humanoid': [{'texture': n(mat)}], 'humanoid_baby': [{'texture': n(mat)}], 'humanoid_leggings': [{'texture': n(mat)}]}})
    tex(f'models/armor/{mat}/{mat}_1.png', f'entity/equipment/humanoid/{mat}')
    tex(f'models/armor/{mat}/{mat}_2.png', f'entity/equipment/humanoid_leggings/{mat}')
# the shields of ASAddon_HarkenScythe repair themselves too
for mat in ('livingmetal', 'biomass'):
    tag('item', f'{NS}:{mat}_equipment', {'id': f'asaddons:{mat}_shield', 'required': False},
        {'id': f'asaddons:gilded_{mat}_shield', 'required': False})

# soulweave and bloodweave: dyed like leather; the texture of a single dye's colour (client), item tinted
COLORS = ['black', 'blue', 'brown', 'cyan', 'default', 'gray', 'green', 'lightblue', 'lime', 'magenta', 'orange', 'pink', 'purple', 'red',
          'silver', 'white', 'yellow']
WEAVE = [('hood', 'Hood', 'Helmet', 'head'), ('robe', 'Robe', 'Chestplate', 'chest'), ('pants', 'Pants', 'Leggings', 'leg'),
         ('shoes', 'Shoes', 'Boots', 'foot')]
WEAVE_PATTERNS = {'hood': ['XXX', 'X X'], 'robe': ['X X', 'XXX', 'XXX'], 'pants': ['XXX', 'X X', 'X X'], 'shoes': ['X X', 'X X']}
for mat, title, old, undyed, overlay in (('soulweave', 'Soulweave', 'Soulweave', 2603484, 0xFFFFFF),
                                         ('bloodweave', 'Bloodweave', 'Bloodweave', 10944540, 12632256)):
    for kind, ktitle, oldkind, slot in WEAVE:
        name = f'{mat}_{kind}'
        ICON_NAMES[f'HS{oldkind}{old}'] = name
        ICON_NAMES[f'HS{oldkind}{old}_overlay'] = f'{name}_overlay'
        write(f'assets/{NS}/models/item/{name}.json', {'parent': 'minecraft:item/generated', 'textures': {
            'layer0': icon(f'HS{oldkind}{old}'), 'layer1': icon(f'HS{oldkind}{old}_overlay')}})
        item_def(name, model(n(f'item/{name}'), [{'type': 'minecraft:dye', 'default': argb(undyed)},
                                                {'type': 'minecraft:constant', 'value': argb(overlay)}]))
        lang[f'item.{NS}.{name}'] = f'{title} {ktitle}'
        recipes[name] = ('shaped', WEAVE_PATTERNS[kind], {'X': n(f'{mat}_cloth')}, n(name))
        recipes[f'dye_{name}'] = {'type': 'minecraft:crafting_dye', 'category': 'equipment', 'group': f'{mat}_dyed', 'dye': '#minecraft:dyes',
                                  'target': n(name), 'result': {'id': n(name)}}
        for t in (f'{slot}_armor', 'armor', 'equippable', 'durability'):
            tag('item', f'minecraft:enchantable/{t}', n(name))
        tag('item', 'minecraft:dyeable', n(name))
        tag('item', 'minecraft:cauldron_can_remove_dye', n(name))
    for c in COLORS:
        tex(f'models/armor/{mat}/{mat}_1_{c}.png', f'entity/equipment/humanoid/{mat}_{c}')
        tex(f'models/armor/{mat}/{mat}_2_{c}.png', f'entity/equipment/humanoid_leggings/{mat}_{c}')
    write(f'assets/{NS}/equipment/{mat}.json', {'layers': {
        'humanoid': [{'texture': n(f'{mat}_default')}], 'humanoid_baby': [{'texture': n(f'{mat}_default')}],
        'humanoid_leggings': [{'texture': n(f'{mat}_default')}]}})
    tag('item', f'{NS}:repairs/{mat}', n(f'{mat}_cloth'))

# spectral armour (RecipesHSArmorDye)
SPECTRAL = [('leather', 'Leather', ['cap', 'tunic', 'pants', 'shoes'], ['Cap', 'Tunic', 'Pants', 'Shoes']),
            ('soulweave', 'Soulweave', ['hood', 'robe', 'pants', 'shoes'], ['Hood', 'Robe', 'Pants', 'Shoes']),
            ('bloodweave', 'Bloodweave', ['hood', 'robe', 'pants', 'shoes'], ['Hood', 'Robe', 'Pants', 'Shoes'])]
OLD_SLOT = ['Helmet', 'Chestplate', 'Leggings', 'Boots']
SLOTS = ['head', 'chest', 'leg', 'foot']
for mat, old, kinds, titles in SPECTRAL:
    for i, kind in enumerate(kinds):
        name = f'spectral_{mat}_{kind}'
        ICON_NAMES[f'HS{OLD_SLOT[i]}Spectral{old}'] = name
        flat(name, f'{old} {titles[i]}', icon(f'HS{OLD_SLOT[i]}Spectral{old}'))
        for t in (f'{SLOTS[i]}_armor', 'armor', 'equippable', 'durability'):
            tag('item', f'minecraft:enchantable/{t}', n(name))
write(f'assets/{NS}/equipment/spectral.json', {'layers': {
    'humanoid': [{'texture': n('spectral')}], 'humanoid_baby': [{'texture': n('spectral')}], 'humanoid_leggings': [{'texture': n('spectral')}]}})
tex('models/armor/spectral/spectral_1.png', 'entity/equipment/humanoid/spectral')
tex('models/armor/spectral/spectral_2.png', 'entity/equipment/humanoid_leggings/spectral')
recipes['spectral_dye_armor'] = {'type': f'{NS}:spectral_dye', 'category': 'equipment'}

# lunar clock: the moon phase in the overworld (ItemHSLunarDial)
moons = []
for i in range(8):
    ICON_NAMES[f'HSLunarDial_{i}'] = f'lunar_clock_{i}'
    write(f'assets/{NS}/models/item/lunar_clock_{i}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': icon(f'HSLunarDial_{i}')}})
    moons.append(model(n(f'item/lunar_clock_{i}')))
item_def('lunar_clock', {'type': 'minecraft:select', 'property': 'minecraft:context_dimension', 'cases': [{'when': 'minecraft:overworld', 'model': {
    'type': 'minecraft:range_dispatch', 'property': 'minecraft:time', 'source': 'moon_phase', 'wobble': False, 'scale': 8.0,
    'entries': [{'threshold': i - 0.5 if i else 0.0, 'model': moons[i]} for i in range(8)]}}], 'fallback': moons[0]})
lang[f'item.{NS}.lunar_clock'] = 'Lunar Clock'

# crucibles: placed like a block from their own item (ItemHSCrucible)
for name, title, old in (('soul_crucible', 'Soul Crucible', 'HSCrucibleSoul'), ('blood_crucible', 'Blood Crucible', 'HSCrucibleBlood')):
    ICON_NAMES[old] = name
    flat(name, None, icon(old))
    lang[f'block.{NS}.{name}'] = title

# ------------------------------------------------------------------------------------------------ blocks
def blockstate(name, variants):
    write(f'assets/{NS}/blockstates/{name}.json', {'variants': variants})


def block_model(name, obj):
    write(f'assets/{NS}/models/block/{name}.json', obj)


def loot_self(name, item=None):
    write(f'data/{NS}/loot_table/blocks/{name}.json', {'type': 'minecraft:block', 'pools': [{
        'condition': {'type': 'minecraft:survives_explosion'}, 'entries': [{'type': 'minecraft:item', 'name': item or n(name)}], 'rolls': 1}],
        'random_sequence': n(f'blocks/{name}')})


def block_item(name):
    item_def(name, model(n(f'block/{name}')))


def simple_block(name, title, texture, render_type=None):
    m = {'parent': 'minecraft:block/cube_all', 'textures': {'all': texture}}
    if render_type:
        m['render_type'] = render_type
    block_model(name, m)
    blockstate(name, {'': {'model': n(f'block/{name}')}})
    block_item(name)
    loot_self(name)
    lang[f'block.{NS}.{name}'] = title


simple_block('soulweave_cloth', 'Soulweave Cloth', tex('blocks/tile.HSSoulweaveClothBlock.png', 'block/soulweave_cloth'), 'minecraft:cutout')
simple_block('bloodweave_cloth', 'Bloodweave Cloth', tex('blocks/tile.HSBloodweaveClothBlock.png', 'block/bloodweave_cloth'), 'minecraft:cutout')
simple_block('spectral_wool', 'Spectral Wool', tex('blocks/tile.HSSpectralClothBlock.png', 'block/spectral_wool'), 'minecraft:translucent')
simple_block('livingmetal_block', 'Livingmetal Block', tex('blocks/tile.HSLivingmetalBlock.png', 'block/livingmetal_block'))
simple_block('biomass_block', 'Biomass Block', tex('blocks/tile.HSBiomassBlock.png', 'block/biomass_block'))
write('data/neoforge/data_maps/block/flammable.json', {'values': {n(c): {'ignite_odds': 500, 'burn_odds': 50}
                                                                  for c in ('soulweave_cloth', 'bloodweave_cloth', 'spectral_wool')}})
tag('block', 'minecraft:mineable/pickaxe', n('livingmetal_block'), n('soul_crucible'), n('blood_crucible'))
tag('block', 'minecraft:mineable/axe', n('biomass_block'))
tag('block', 'minecraft:mineable/shovel', n('creep_block'), n('blood_splash_residue'))
tag('block', 'minecraft:beacon_base_blocks', n('livingmetal_block'), n('biomass_block'))
recipes['livingmetal_block'] = ('shaped', ['XXX', 'XXX', 'XXX'], {'X': n('livingmetal_ingot')}, n('livingmetal_block'))
recipes['biomass_block'] = ('shaped', ['XXX', 'XXX', 'XXX'], {'X': n('biomass')}, n('biomass_block'))
recipes['livingmetal_ingot'] = ('shapeless', [n('livingmetal_block')], n('livingmetal_ingot'), 9)
recipes['biomass'] = ('shapeless', [n('biomass_block')], n('biomass'), 9)
recipes['spectral_wool'] = ('shapeless', [n('spectral_dye'), 'minecraft:white_wool'], n('spectral_wool'), 1)

# creep: plain, tilled, tilled with blood (metadata 0-2); drops soul sand
side = tex('blocks/tile.HSCreepBlock_side.png', 'block/creep_block_side')
tops = [tex('blocks/tile.HSCreepBlock_top.png', 'block/creep_block_top'), tex('blocks/tile.HSCreepBlock_tilled.png', 'block/creep_block_tilled'),
        tex('blocks/tile.HSCreepBlock_tilledBloodied.png', 'block/creep_block_tilled_bloodied')]
for i, top in enumerate(tops):
    block_model(f'creep_block_{i}', {'parent': 'minecraft:block/cube_bottom_top', 'textures': {'top': top, 'bottom': side, 'side': side}})
blockstate('creep_block', {f'soil={i}': {'model': n(f'block/creep_block_{i}')} for i in range(3)})
item_def('creep_block', model(n('block/creep_block_0')))
loot_self('creep_block', 'minecraft:soul_sand')
lang[f'block.{NS}.creep_block'] = 'Creep Block'

# biomass plant (BlockHSCrops, render type 1 = cross)
for age in range(4):
    block_model(f'biomass_plant_{age}', {'parent': 'minecraft:block/cross', 'render_type': 'minecraft:cutout', 'textures': {
        'cross': tex(f'blocks/tile.HSBiomassPlant_{age + 1}.png', f'block/biomass_plant_{age + 1}')}})
blockstate('biomass_plant', {f'age={a}': {'model': n(f'block/biomass_plant_{a}')} for a in range(4)})
ripe = {'type': 'minecraft:match_block', 'blocks': n('biomass_plant'), 'state': {'age': '3'}}
write(f'data/{NS}/loot_table/blocks/biomass_plant.json', {'type': 'minecraft:block', 'modifier': {'type': 'minecraft:explosion_decay'}, 'pools': [
    {'entries': [{'type': 'minecraft:alternatives', 'children': [
        {'type': 'minecraft:item', 'condition': ripe, 'name': n('biomass')}, {'type': 'minecraft:item', 'name': n('biomass_seed')}]}], 'rolls': 1},
    {'condition': ripe, 'entries': [{'type': 'minecraft:item', 'name': n('biomass_seed'), 'modifier': {
        'type': 'minecraft:apply_bonus', 'enchantment': 'minecraft:fortune', 'formula': 'minecraft:binomial_with_bonus_count',
        'parameters': {'extra': 3, 'probability': 4.0 / 15.0}}}], 'rolls': 1}], 'random_sequence': n('blocks/biomass_plant')})
lang[f'block.{NS}.biomass_plant'] = 'Biomass Plant'
recipes['biomass_seed'] = ('shapeless', ['minecraft:fermented_spider_eye', 'minecraft:nether_wart'], n('biomass_seed'), 1)

# blood splash residue: colour and stage (metadata 1-12), a thin see-through layer
for color in ('red', 'green', 'purple'):
    for stage in range(1, 5):
        t = tex(f'blocks/tile.HSSplashBloodBlock_{color}_{stage}.png', f'block/blood_splash_{color}_{stage}')
        block_model(f'blood_splash_{color}_{stage}', {'render_type': 'minecraft:translucent', 'ambientocclusion': False,
                                                       'textures': {'particle': t, 'blood': t}, 'elements': [{
            'from': [0, 0, 0], 'to': [16, 0.16, 16], 'faces': {
                'up': {'texture': '#blood', 'uv': [0, 0, 16, 16]}, 'down': {'texture': '#blood', 'uv': [0, 0, 16, 16], 'cullface': 'down'}}}]})
blockstate('blood_splash_residue', {f'color={c},stage={s}': {'model': n(f'block/blood_splash_{c}_{s}')}
                                    for c in ('red', 'green', 'purple') for s in range(1, 5)})
lang[f'block.{NS}.blood_splash_residue'] = 'Blood Splash Residue'

# soul light and skull block: drawn by nothing / by the block entity renderer; particle textures only
block_model('soul_light', {'textures': {'particle': tex('blocks/tile.HSSoulLight.png', 'block/soul_light')}})
blockstate('soul_light', {'': {'model': n('block/soul_light')}})
lang[f'block.{NS}.soul_light'] = 'Soul Light'
block_model('skull', {'textures': {'particle': 'minecraft:block/soul_sand'}})
blockstate('skull', {f'facing={f}': {'model': n('block/skull')} for f in ('up', 'north', 'south', 'west', 'east')})
lang[f'block.{NS}.skull'] = 'Skull'

# crucibles (RenderHSCrucible): the outer block, the inner walls 2/16 in, the floor at 4/16, the content at (3.6875 + 1.1 * level) / 16
for kind, old in (('soul', 'Soul'), ('blood', 'Blood')):
    b = f'{kind}_crucible'
    t = {p: tex(f'blocks/tile.HS{old}Crucible_{p}.png', f'block/{b}_{p}') for p in ('side', 'top', 'bottom', 'inner', 'liquid')}
    for level in range(11):
        elements = [
            {'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': {
                'north': {'texture': '#side', 'cullface': 'north'}, 'south': {'texture': '#side', 'cullface': 'south'},
                'west': {'texture': '#side', 'cullface': 'west'}, 'east': {'texture': '#side', 'cullface': 'east'},
                'up': {'texture': '#top', 'cullface': 'up'}, 'down': {'texture': '#bottom', 'cullface': 'down'}}},
            {'from': [2, 0, 0], 'to': [2, 16, 16], 'faces': {'east': {'texture': '#side'}}},
            {'from': [14, 0, 0], 'to': [14, 16, 16], 'faces': {'west': {'texture': '#side'}}},
            {'from': [0, 0, 2], 'to': [16, 16, 2], 'faces': {'south': {'texture': '#side'}}},
            {'from': [0, 0, 14], 'to': [16, 16, 14], 'faces': {'north': {'texture': '#side'}}},
            {'from': [0, 4, 0], 'to': [16, 4, 16], 'faces': {'up': {'texture': '#inner'}, 'down': {'texture': '#inner'}}}]
        if level > 0:
            y = round(3.6875 + level * 1.1, 4)
            elements.append({'from': [0, y, 0], 'to': [16, y, 16], 'faces': {'up': {'texture': '#liquid'}}})
        block_model(f'{b}_level_{level}', {'render_type': 'minecraft:cutout', 'ambientocclusion': False, 'textures': {
            'particle': t['side'], 'side': t['side'], 'top': t['top'], 'bottom': t['bottom'], 'inner': t['inner'], 'liquid': t['liquid']},
            'elements': elements})
    blockstate(b, {f'level={lv}': {'model': n(f'block/{b}_level_{lv}')} for lv in range(11)})
    loot_self(b)
recipes['soul_crucible'] = ('shaped', ['X X', 'X*X', 'XXX'], {'X': 'minecraft:obsidian', '*': 'minecraft:ender_pearl'}, n('soul_crucible'))
recipes['blood_crucible'] = ('shaped', ['X X', 'X*X', 'XXX'], {'X': 'minecraft:netherrack', '*': 'minecraft:ender_pearl'}, n('blood_crucible'))

# ------------------------------------------------------------------------------------------------ other recipes of mod_HarkenScythe
def full(item):
    """new ItemStack(keeper) of 1.5.2: damage 0, a full keeper or vessel."""
    return damaged(item, 0)


def damaged(item, damage):
    return {'neoforge:ingredient_type': 'neoforge:components', 'items': n(item), 'components': {'minecraft:damage': damage}}


recipes['essence_keeper'] = ('shaped', ['X X', ' X '], {'X': 'minecraft:ender_pearl'}, n('essence_keeper'))
recipes['book_of_shadows'] = ('shapeless', ['minecraft:book', 'minecraft:feather', 'minecraft:gold_ingot', full('soulkeeper')], n('book_of_shadows'), 1)
recipes['book_of_carnage'] = ('shapeless', ['minecraft:book', 'minecraft:feather', 'minecraft:gold_ingot', full('bloodkeeper')], n('book_of_carnage'), 1)
recipes['necronomicon'] = ('shaped', ['XXX', '**X', '**X'], {'X': n('necronomicon_page'), '*': 'minecraft:rotten_flesh'}, n('necronomicon'))
recipes['talisman'] = ('shaped', ['X X', ' ^ ', ' * '], {'X': 'minecraft:string', '^': 'minecraft:ender_pearl', '*': 'minecraft:diamond'}, n('talisman'))
recipes['ethereal_talisman'] = ('shaped', ['X X', ' ^ ', ' * '], {'X': 'minecraft:string', '^': 'minecraft:ender_pearl', '*': 'minecraft:nether_star'},
                                n('ethereal_talisman'))
for name, mid in (('overworld_amulet', 'minecraft:clay_ball'), ('nether_amulet', 'minecraft:fire_charge'), ('ethereal_amulet', 'minecraft:nether_star')):
    recipes[name] = ('shaped', ['X X', '*^*', ' * '], {'X': 'minecraft:string', '*': mid, '^': 'minecraft:ender_pearl'}, n(name))
recipes['ender_pearl_from_abyss_fragments'] = ('shaped', ['XX', 'XX'], {'X': n('abyss_fragment')}, 'minecraft:ender_pearl')
recipes['creepball'] = ('shaped', ['X*X', '*^*', 'X*X'], {'X': 'minecraft:rotten_flesh', '*': 'minecraft:spider_eye', '^': 'minecraft:slime_ball'},
                        n('creepball'))
recipes['creepball_2'] = ('shaped', ['X*X', '*^*', 'X*X'], {'X': 'minecraft:spider_eye', '*': 'minecraft:rotten_flesh', '^': 'minecraft:slime_ball'},
                          n('creepball'))
recipes['spectral_dye'] = ('shapeless', [n('ectoplasm'), 'minecraft:bone_meal'], n('spectral_dye'), 2)
recipes['lunar_clock'] = ('shaped', ['X*X', '*^*', 'X*X'], {'X': n('ectoplasm'), '*': 'minecraft:gold_ingot', '^': 'minecraft:redstone'}, n('lunar_clock'))

# RecipeHSAttunement: a full keeper, or a full or half-full vessel, attunes a talisman or an amulet (the keeper is used up, the
# vessel gives half its capacity; see KeeperItem.getCraftingRemainder)
for target in ('talisman', 'ethereal_talisman', 'overworld_amulet', 'nether_amulet', 'ethereal_amulet'):
    for aug, keeper, vessel, half in (('soul_attuned', 'soulkeeper', 'soul_vessel', 20), ('blood_attuned', 'bloodkeeper', 'blood_vessel', 40)):
        result = {'id': n(target), 'components': {'minecraft:enchantments': {n(aug): 1}, 'minecraft:rarity': 'epic'}}
        for suffix, ing in (('keeper', full(keeper)), ('vessel', full(vessel)), ('half_vessel', damaged(vessel, half))):
            recipes[f'attunement/{target}_{aug}_{suffix}'] = {'type': 'minecraft:crafting_shapeless', 'category': 'misc',
                                                             'group': f'{target}_{aug}', 'ingredients': [n(target), ing], 'result': result}


def write_recipes():
    for name, r in recipes.items():
        if isinstance(r, dict):
            obj = r
        elif r[0] == 'shaped':
            _, pattern, key, result = r
            obj = {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'key': key, 'pattern': pattern, 'result': {'count': 1, 'id': result}}
        else:
            _, ingredients, result, count = r
            obj = {'type': 'minecraft:crafting_shapeless', 'category': 'misc', 'ingredients': ingredients, 'result': {'count': count, 'id': result}}
        write(f'data/{NS}/recipe/{name}.json', obj)


# ------------------------------------------------------------------------------------------------ augments
# EnchantmentHS*: weight 1, enchantability 100 (not from the table, the altars apply them), levels as in 1.5.2
AUGMENTS = [('soul_attuned', 'Soul Attuned', 1, f'#{NS}:enchantable/attunable', ['any'], 'attunement'),
            ('blood_attuned', 'Blood Attuned', 1, f'#{NS}:enchantable/attunable', ['any'], 'attunement'),
            ('soulsteal', 'Soulsteal', 3, f'#{NS}:enchantable/weapon', ['mainhand'], None),
            ('bloodletting', 'Bloodletting', 3, f'#{NS}:enchantable/weapon', ['mainhand'], None),
            ('blight', 'Blight', 3, '#minecraft:enchantable/bow', ['mainhand'], 'projectile'),
            ('hemorrhage', 'Hemorrhage', 3, '#minecraft:enchantable/bow', ['mainhand'], 'projectile'),
            ('vitality', 'Vitality', 4, f'#{NS}:enchantable/armor', ['armor'], None),
            ('exude', 'Exude', 4, f'#{NS}:enchantable/armor', ['armor'], None),
            ('afterlife', 'Afterlife', 3, '#minecraft:enchantable/durability', ['mainhand'], 'death'),
            ('decapitate', 'Decapitate', 3, '#minecraft:enchantable/durability', ['mainhand'], 'death'),
            ('ward', 'Ward', 3, f'#{NS}:enchantable/shield', ['mainhand'], None),
            ('sanguinary', 'Sanguinary', 3, f'#{NS}:enchantable/shield', ['mainhand'], None)]
for name, title, levels, items, slots, exclusive in AUGMENTS:
    e = {'anvil_cost': 8, 'description': {'translate': f'enchantment.{NS}.{name}'}, 'max_cost': {'base': 100, 'per_level_above_first': 0},
         'max_level': levels, 'min_cost': {'base': 100, 'per_level_above_first': 0}, 'slots': slots, 'supported_items': items, 'weight': 1}
    if exclusive:
        e['exclusive_set'] = f'#{NS}:exclusive_set/{exclusive}'
        tag('enchantment', f'{NS}:exclusive_set/{exclusive}', n(name))
    write(f'data/{NS}/enchantment/{name}.json', e)
    lang[f'enchantment.{NS}.{name}'] = title
# weapon augments: swords (EnumEnchantmentType.weapon) incl. scythes, glaives and the giant swords of Asgard Shield;
# Ward and Sanguinary work on the shields of Asgard Shield too; Vitality and Exude on all armour and the shields
tag('item', f'{NS}:enchantable/weapon', '#minecraft:enchantable/sword', {'id': '#asgardshield_core:giant_swords', 'required': False})
tag('item', f'{NS}:enchantable/shield', f'#{NS}:enchantable/weapon', {'id': '#asgardshield_core:shields', 'required': False})
tag('item', f'{NS}:enchantable/armor', '#minecraft:enchantable/armor', {'id': '#asgardshield_core:shields', 'required': False})
tag('item', f'{NS}:enchantable/attunable', '#minecraft:enchantable/durability')

# ------------------------------------------------------------------------------------------------ altars
# BlockHSSoulAltar / BlockHSBloodAltar: 12/16 high (setBlockBounds 0.75), top, side and bottom texture
for kind, old, title, base, book in (('souls', 'Soul', 'Altar of Souls', 'minecraft:obsidian', 'book_of_shadows'),
                                     ('blood', 'Blood', 'Altar of Blood', 'minecraft:netherrack', 'book_of_carnage')):
    b = f'altar_of_{kind}'
    t = {p: tex(f'blocks/tile.HS{old}Altar_{p}.png', f'block/{b}_{p}') for p in ('side', 'top', 'bottom')}
    side = {'texture': '#side', 'uv': [0, 4, 16, 16]}
    block_model(b, {'textures': {'particle': t['side'], 'side': t['side'], 'top': t['top'], 'bottom': t['bottom']},
                    'elements': [{'from': [0, 0, 0], 'to': [16, 12, 16], 'faces': {
                        'north': dict(side, cullface='north'), 'south': dict(side, cullface='south'),
                        'west': dict(side, cullface='west'), 'east': dict(side, cullface='east'),
                        'up': {'texture': '#top'}, 'down': {'texture': '#bottom', 'cullface': 'down'}}}]})
    blockstate(b, {'': {'model': n(f'block/{b}')}})
    block_item(b)
    loot_self(b)
    lang[f'block.{NS}.{b}'] = title
    tag('block', 'minecraft:mineable/pickaxe', n(b))
    recipes[b] = ('shaped', [' ^ ', '*X*', 'XXX'], {'X': base, '*': 'minecraft:ender_pearl', '^': n(book)}, n(b))
tex('gui/guiSoulAlter.png', 'gui/altar_of_souls')
tex('gui/guiBloodAlter.png', 'gui/altar_of_blood')
tex('gui/bookShadow.png', 'entity/altar/book_of_shadows')
tex('gui/bookCarnage.png', 'entity/altar/book_of_carnage')

# RecipeHSSoulAltar / RecipeHSBloodAltar: "order" is the old recipe number (soulAltarRecipeList 1-6, soulAltarAugmentList
# 20-27, the highest match wins); cost per item in souls or blood (the GUI showed it times the stack size)
# the wool of woolColorCheck: every colour but orange (missing in 1.5.2), and spectral wool
WOOLS = ['white', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red',
         'black']
tag('item', f'{NS}:altar/wool', *[f'minecraft:{c}_wool' for c in WOOLS], n('spectral_wool'))
# ItemSword: swords, scythes, glaives and the giant swords of Asgard Shield (ItemZedSword)
tag('item', f'{NS}:altar/sword', '#minecraft:swords', *[n(w) for w in WEAPONS], {'id': '#asgardshield_core:giant_swords', 'required': False})
tag('item', f'{NS}:altar/attunable', *[n(x) for x in ('talisman', 'ethereal_talisman', 'overworld_amulet', 'nether_amulet', 'ethereal_amulet')])
tag('item', f'{NS}:altar/skulls', *[n(f'{sid}_skull') for sid, _, _, _ in SKULLS])
ASGARD = [{'type': 'neoforge:mod_loaded', 'modid': 'asgardshield_core'}]


def rows(ench, levels, roll=(1, 100)):
    """Rows of one enchantment: levels = [(lowest rank, level), ...] from the highest rank down."""
    out, top = [], 100
    for low, lv in levels:
        out.append({'enchantment': n(ench), 'level': lv, 'min_roll': roll[0], 'max_roll': roll[1], 'min_rank': low, 'max_rank': top})
        top = low - 1
    return out


STD = [(90, 3), (60, 2), (1, 1)]
FOUR = [(90, 4), (80, 3), (50, 2), (1, 1)]


def book_rows(augs, four):
    out = []
    for i, a in enumerate(augs):
        top = 4 if a == four else 3
        out += rows(a, [(91, top), (71, 3), (41, 2), (1, 1)], (i * 20 + 1, i * 20 + 20))
    return out


ALTARS = {
    'soul': {'keeper': 'soulkeeper', 'vessel': 'soul_vessel', 'keeper_cost': 20, 'vessel_cost': 40, 'step': 5, 'cloth': 'soulweave_cloth',
             'imbue': ('minecraft:iron_ingot', 'livingmetal_ingot', 'imbue', 5), 'aug': 50, 'attune': 20, 'attuned': 'soul_attuned',
             'slay': 'soulsteal', 'death': 'afterlife', 'bow': 'blight', 'armor': 'vitality', 'shield': 'ward',
             'book': ['soulsteal', 'blight', 'vitality', 'afterlife', 'ward'], 'sword2': STD},
    'blood': {'keeper': 'bloodkeeper', 'vessel': 'blood_vessel', 'keeper_cost': 40, 'vessel_cost': 80, 'step': 10, 'cloth': 'bloodweave_cloth',
              'imbue': (n('biomass_seed'), 'germinated_biomass_seed', 'germinate', 10), 'aug': 100, 'attune': 40, 'attuned': 'blood_attuned',
              'slay': 'bloodletting', 'death': 'decapitate', 'bow': 'hemorrhage', 'armor': 'exude', 'shield': 'sanguinary',
              'book': ['bloodletting', 'hemorrhage', 'exude', 'decapitate', 'sanguinary'], 'sword2': [(90, 3), (55, 2), (1, 1)]},
}
for altar, a in ALTARS.items():
    def altar_recipe(name, order, action, ingredient, cost, **extra):
        r = {'type': n('altar'), 'altar': altar, 'order': order, 'action': action, 'ingredient': ingredient, 'cost': cost}
        cond = extra.pop('conditions', None)
        r.update(extra)
        if cond:
            r['neoforge:conditions'] = cond
        recipes[f'altar/{altar}/{name}'] = r
    altar_recipe('essence_keeper', 1, 'enthrall', n('essence_keeper'), a['keeper_cost'], result=n(a['keeper']))
    altar_recipe(a['keeper'], 2, 'enthrall', n(a['keeper']), a['step'], recharge=True, result=n(a['keeper']))
    altar_recipe('essence_vessel', 3, 'enthrall', n('essence_vessel'), a['vessel_cost'], result=n(a['vessel']))
    altar_recipe(a['vessel'], 4, 'enthrall', n(a['vessel']), a['step'], recharge=True, result=n(a['vessel']))
    altar_recipe('wool', 5, 'entwine', f'#{NS}:altar/wool', a['step'], result=n(a['cloth']))
    src, dst, action, cost = a['imbue']
    altar_recipe(dst, 6, action, src, cost, result=n(dst))
    c = a['aug']
    altar_recipe('sword', 20, 'augment', f'#{NS}:altar/sword', c,
                 augments=rows(a['death'], STD, (85, 100)) + rows(a['slay'], a['sword2'], (1, 84)))
    altar_recipe('bow', 21, 'augment', '#minecraft:enchantable/bow', c, augments=rows(a['death'], STD, (85, 100)) + rows(a['bow'], STD, (1, 84)))
    altar_recipe('armor', 22, 'augment', '#minecraft:enchantable/armor', c, exclude=f'#{NS}:altar/skulls', augments=rows(a['armor'], FOUR))
    altar_recipe('axe', 23, 'augment', '#minecraft:axes', c, augments=rows(a['slay'], STD, (85, 100)) + rows(a['death'], STD, (1, 84)))
    altar_recipe('shield', 24, 'augment', '#asgardshield_core:shields', c, conditions=ASGARD,
                 augments=rows(a['armor'], STD, (85, 100)) + rows(a['shield'], STD, (1, 84)))
    # ItemZedSword: the third branch only took ranks of 60 and more, the rest fell to the last one
    altar_recipe('giant_sword', 25, 'augment', '#asgardshield_core:giant_swords', c, conditions=ASGARD,
                 augments=rows(a['death'], STD, (90, 100)) + rows(a['armor'], STD, (75, 89)) + rows(a['slay'], [(90, 3), (60, 2)], (1, 74))
                 + [{'enchantment': n(a['shield']), 'level': 1, 'min_roll': 1, 'max_roll': 74, 'min_rank': 1, 'max_rank': 59}])
    altar_recipe('book', 26, 'attune', 'minecraft:book', c, single=True, result='minecraft:enchanted_book',
                 augments=book_rows(a['book'], a['armor']))
    altar_recipe('attunement', 27, 'attune', f'#{NS}:altar/attunable', a['attune'], components={'minecraft:rarity': 'epic'},
                 augments=[{'enchantment': n(a['attuned']), 'level': 1}])
lang.update({
    f'container.{NS}.captured_souls': 'Captured Souls', f'container.{NS}.collected_blood': 'Collected Blood',
    f'container.{NS}.altar.enthrall': 'Enthrall', f'container.{NS}.altar.entwine': 'Entwine', f'container.{NS}.altar.imbue': 'Imbue',
    f'container.{NS}.altar.germinate': 'Germinate', f'container.{NS}.altar.augment': 'Augment', f'container.{NS}.altar.attune': 'Attune',
    f'jei.{NS}.altar.cost.soul': 'Cost: %s Souls', f'jei.{NS}.altar.cost.blood': 'Cost: %s Blood',
    f'jei.{NS}.altar.recharge.soul': 'Cost: %s Souls for every %s missing', f'jei.{NS}.altar.recharge.blood': 'Cost: %s Blood for every %s missing',
    f'jei.{NS}.altar.per_item': 'per item',
    f'jei.{NS}.altar.unenchanted': 'Only unenchanted items',
    f'jei.{NS}.altar.chance': '%s: %s%%',
    f'jei.{NS}.altar': 'Place crucibles two blocks away around the altar (one block lower to one block higher, with air between them '
                       'one block above the crucible). The altar uses their content.',
})

# ------------------------------------------------------------------------------------------------ mobs, loot, trades
for e in ('spectral_pets', 'harbingers', 'soulless'):
    tag('entity_type', f'{NS}:{e}')
for name, title in (('soul_common', 'Soul Common'), ('soul_grieving', 'Soul Grieving'), ('soul_culled', 'Soul Culled'),
                    ('soul_wrathful', 'Soul Wrathful'), ('soul_lost', 'Soul Lost')):
    lang[f'entity.{NS}.{name}'] = title
    tex(f'models/soul/{name.replace("_", "")}.png', f'entity/soul/{name.replace("_", "")}')
# EntityRegistry.addSpawn(EntityHSSoulLost, 3, 0, 1, monster, ...): monster weights of worldgen152 are ten times the old ones
write(f'data/{NS}/neoforge/biome_modifier/spawn_soul_lost.json', {
    'type': 'neoforge:add_spawns', 'biomes': [f'worldgen152:{b}' for b in (
        'beach', 'extreme_hills', 'extreme_hills_edge', 'forest', 'forest_hills', 'jungle', 'jungle_hills', 'mushroom_island',
        'mushroom_island_shore', 'ocean', 'plains', 'river', 'swampland')],
    'spawners': [{'type': n('soul_lost'), 'count': {'type': 'minecraft:uniform', 'min_inclusive': 0, 'max_inclusive': 1}, 'weight': 30}]})
# the bonus chest (not generated by worldgen152): 10 draws among the 1.5.2 weights 46 + the page's 5
write(f'data/{NS}/loot_table/chests/necronomicon_page_bonus_chest.json', {'type': 'minecraft:chest', 'pools': [{
    'entries': [{'type': 'minecraft:item', 'name': n('necronomicon_page'), 'weight': 5}, {'type': 'minecraft:empty', 'weight': 46}], 'rolls': 10}]})
write(f'data/{NS}/loot_modifiers/necronomicon_page_bonus_chest.json', {
    'type': 'neoforge:add_table', 'condition': {'type': 'neoforge:loot_table_id', 'loot_table_id': 'minecraft:chests/spawn_bonus_chest'},
    'table': n('chests/necronomicon_page_bonus_chest')})
# TradeHandler: a librarian sells a page for 30 emeralds (MerchantRecipe: 7 uses)
write(f'data/{NS}/villager_trade/librarian/1/emerald_necronomicon_page.json', {
    'gives': {'id': n('necronomicon_page')}, 'max_uses': 7, 'wants': {'count': 30, 'id': 'minecraft:emerald'}})
tag('villager_trade', 'minecraft:librarian/level_1', n('librarian/1/emerald_necronomicon_page'))

# ------------------------------------------------------------------------------------------------ language
lang.update({
    f'tooltip.{NS}.dyed': 'Dyed',
    f'tooltip.{NS}.stored_souls': 'Stored Souls: %s / %s',
    f'tooltip.{NS}.stored_blood': 'Stored Blood: %s / %s',
    f'tooltip.{NS}.book.0.1': 'A book containing religious texts', f'tooltip.{NS}.book.0.2': 'and instructions for magical rituals',
    f'tooltip.{NS}.book.1.1': 'A book containing religious texts', f'tooltip.{NS}.book.1.2': 'and instructions for sacrificial rituals',
    f'tooltip.{NS}.book.2.1': 'The Book of the Dead: Bound in human', f'tooltip.{NS}.book.2.2': 'flesh and inked in blood, this ancient',
    f'tooltip.{NS}.book.2.3': 'Samarian text contained bizarre', f'tooltip.{NS}.book.2.4': 'burial rights, funeral incantations,',
    f'tooltip.{NS}.book.2.5': 'and demon resurrection passages.',
    f'tooltip.{NS}.book.3.1': 'A Missing page of the Necronomicon', f'tooltip.{NS}.book.3.2': 'writen in ancient Samarian text.',
    f'tooltip.{NS}.talisman.breaks_after': 'Breaks after %s resurrection',
    f'tooltip.{NS}.talisman.breaks_after_plural': 'Breaks after %s resurrections',
    f'tooltip.{NS}.talisman.soul': 'Added Effect: Valor', f'tooltip.{NS}.talisman.blood': 'Added Effect: Last Stand',
    f'tooltip.{NS}.talisman.cost': 'Cost: 10 Souls and 20 Blood', f'tooltip.{NS}.talisman.cost.soul': 'Cost: 20 Souls',
    f'tooltip.{NS}.talisman.cost.blood': 'Cost: 40 Blood',
    f'tooltip.{NS}.amulet': 'Teleport back to resting place',
    f'tooltip.{NS}.amulet.cost': 'Cost: 5 Souls and 10 Blood', f'tooltip.{NS}.amulet.cost.soul': 'Cost: 10 Souls',
    f'tooltip.{NS}.amulet.cost.blood': 'Cost: 20 Blood',
    f'jei.{NS}.scythe': 'Hold right-click for a moment and let go: the charged scythe strikes everything around you for double damage, '
                        'and every mob it kills leaves its soul behind. Right-click a soul with a keeper or vessel to take it in. '
                        'Right-click sugar cane, wheat or a biomass plant to harvest it.',
    f'jei.{NS}.glaive': 'Hold right-click for a moment and let go: the charged glaive strikes everything in a narrow cone ahead for double '
                        'damage and spills their blood on the ground. Right-click blood with a Bloodkeeper or Blood Vessel to take it in. '
                        'Right-click creep to till it.',
    f'jei.{NS}.keeper': 'Keepers and vessels store souls (scythe) and blood (glaive). Hold right-click to drink: souls give a random '
                        'effect, blood gives strength; both make you hungry. Talismans, amulets, the Necronomicon, the crucibles and '
                        'the altars use what they hold.',
    f'jei.{NS}.necronomicon': 'With two blood in a keeper, hold right-click until the ritual is done: every soul near you rises as a '
                              'spectral servant. Pages are found in dungeon, temple, mineshaft, stronghold and village chests.',
    f'jei.{NS}.talisman': 'Carried in the inventory, it saves you from a deadly blow for souls and blood from your keepers. Attune it with '
                          'a full keeper (or half a vessel) for other costs and effects.',
    f'jei.{NS}.amulet': 'Hold right-click to return to your bed, paid with souls and blood from your keepers: the Overworld Amulet in '
                        'the overworld, the Nether Amulet from the Nether, the Ethereal Amulet from anywhere but the End.',
    f'jei.{NS}.crucible': 'Right-click with a keeper or vessel to pour in souls (5) or blood (10). Breaking a filled crucible sets the '
                          'essence free.',
    f'jei.{NS}.creep': 'Use a creepball on soul sand to grow creep. Till it with a glaive and soak it with blood from a Bloodkeeper, '
                       'then plant a germinated biomass seed.',
    f'jei.{NS}.livingmetal': 'Livingmetal gear repairs itself whenever you take in souls.',
    f'jei.{NS}.biomass': 'Biomass gear repairs itself whenever you take in blood.',
    f'jei.{NS}.spectral_dye': 'Leather, soulweave or bloodweave armour crafted with spectral dye becomes spectral armour.',
})

# ------------------------------------------------------------------------------------------------ write
write_recipes()
for key, values in tags.items():
    kind, ns, path = key.split(':', 2)
    folder = {'item': 'item', 'block': 'block', 'entity_type': 'entity_type', 'enchantment': 'enchantment',
              'villager_trade': 'villager_trade'}[kind]
    write(f'data/{ns}/tags/{folder}/{path}.json', {'values': values})
write(f'assets/{NS}/lang/en_us.json', dict(sorted(lang.items())))
tex('gui/DrinkGauge.png', 'gui/drink_gauge')
with open(os.path.join(ROOT, 'original-assets.txt'), 'w') as f:
    f.write('# path in Harken Scythe 2.1.6 (harkenscythe-v2.1.6.zip) -> path in the ported jar (generated by tools/gen.py)\n')
    for s, d in assets:
        f.write(f'{s} -> {d}\n')
print('recipes', len(recipes), 'tags', len(tags), 'lang', len(lang), 'textures', len(assets))
