#!/usr/bin/env python3
"""Generates the assets/data JSON of the LegendGear port (mods/legendgear/src/main/resources), the language files and
original-assets.txt. Everything under resources/assets and resources/data is overwritten."""
import json, os, shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = ROOT + '/src/main/resources'
NS = 'legendgear'
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


def tex(src, dst):
    assets.append((src, f'assets/{NS}/textures/{dst}.png'))


ITEM_TEX = 'mods/legendgear/textures/items/'
BLOCK_TEX = 'mods/legendgear/textures/blocks/'


def flat_model(model, texture):
    write(f'assets/{NS}/models/item/{model}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': n(f'item/{texture}')}})


def handheld_model(model, texture):
    write(f'assets/{NS}/models/item/{model}.json', {'parent': 'minecraft:item/handheld', 'textures': {'layer0': n(f'item/{texture}')}})


def item_def(name_, model):
    write(f'assets/{NS}/items/{name_}.json', {'model': model})


def plain(m):
    return {'type': 'minecraft:model', 'model': m}


def item(name_, texture, english, german, handheld=False):
    """A flat item with the original icon (textures/items/<texture>.png)."""
    tex(ITEM_TEX + texture + '.png', 'item/' + texture)
    (handheld_model if handheld else flat_model)(name_, texture)
    item_def(name_, plain(n(f'item/{name_}')))
    name(f'item.{NS}.{name_}', english, german)


# ---------------------------------------------------------------- items
ITEMS = [
    ('headband', 'itemHeadband', 'Headband of Valor', 'Stirnband der Tapferkeit', False),
    ('wind_boots', 'itemWindBoots', 'Whirlwind Boots', 'Wirbelwindstiefel', False),
    ('emerald_shard', 'itemEmeraldShard', 'Emerald Shard', 'Smaragdsplitter', False),
    ('emerald_piece', 'itemEmeraldPiece', 'Emerald Piece', 'Smaragdstück', False),
    ('quiver', 'itemQuiver', 'Quiver', 'Köcher', False),
    ('heart', 'itemHeart', 'Heart', 'Herz', False),
    ('mystic_seed', 'itemMysticSeed', 'Mystic Seed', 'Mystischer Samen', False),
    ('phoenix_feather', 'itemPhoenixFeather', 'Phoenix Feather', 'Phönixfeder', False),
    ('earth_medallion', 'itemMedallionEarth', 'Earth Medallion', 'Erdmedaillon', True),
    ('wind_medallion', 'itemMedallionWind', 'Wind Medallion', 'Windmedaillon', True),
    ('fire_medallion', 'itemMedallionFire', 'Fire Medallion', 'Feuermedaillon', True),
    ('ender_medallion', 'itemMedallionEnder', 'Ender Medallion', 'Endermedaillon', True),
    ('aero_amulet', 'itemAmuletAero', 'Aero Amulet', 'Aero-Amulett', False),
    ('geo_amulet', 'itemAmuletGeo', 'Geo Amulet', 'Geo-Amulett', False),
    ('pyro_amulet', 'itemAmuletPyro', 'Pyro Amulet', 'Pyro-Amulett', False),
    ('titan_band', 'itemTitanBand', 'Titan Band', 'Titanenband', False),
    ('magic_boomerang', 'itemBoomerang', 'Magic Boomerang', 'Magischer Bumerang', True),
    ('bomb', 'itemBomb', 'Bomb', 'Bombe', False),
    ('bomb_bag', 'itemBombBag', 'Bomb Bag', 'Bombentasche', False),
    ('redstone_rock_candy', 'itemRockCandy_1', 'Redstone Rock Candy', 'Redstone-Kandis', False),
    ('lapis_rock_candy', 'itemRockCandy_2', 'Lapis Rock Candy', 'Lapis-Kandis', False),
    ('emerald_rock_candy', 'itemRockCandy_3', 'Emerald Rock Candy', 'Smaragd-Kandis', False),
    ('diamond_rock_candy', 'itemRockCandy_4', 'Diamond Rock Candy', 'Diamant-Kandis', False),
    ('milk_chocolate', 'itemMilkChocolate', 'Milk Chocolate', 'Milchschokolade', False),
    ('iron_key', 'itemKey_iron', 'Iron Key', 'Eisenschlüssel', False),
    ('gold_key', 'itemKey_gold', 'Gold Key', 'Goldschlüssel', False),
    ('diamond_key', 'itemKey_diamond', 'Diamond Key', 'Diamantschlüssel', False),
    ('stardust', 'itemStarDust', 'Stardust', 'Sternenstaub', False),
    ('star_piece', 'itemStarPiece', 'Star Piece', 'Sternenstück', False),
    ('magic_powder', 'itemMagicPowder', 'Magic Powder', 'Zauberpulver', False),
    ('reed_pipes', 'itemReedPipes', 'Reed Pipes', 'Schilfflöte', False),
    ('slime_sword', 'itemSlimeSword', 'Slime Sword', 'Schleimschwert', True),
]
for name_, texture, english, german, hand in ITEMS:
    item(name_, texture, english, german, hand)

# the magic mirror: dim, lit (remembers the sky, custom model data flag 0), active while held up
for t in ('itemMirror_dim', 'itemMirror_lit', 'itemMirror_active'):
    tex(ITEM_TEX + t + '.png', 'item/' + t)
write(f'assets/{NS}/textures/item/itemMirror_active.png.mcmeta', {'animation': {'frametime': 1}})  # itemMirror_active.txt: 0,1,2,3
flat_model('magic_mirror', 'itemMirror_dim')
flat_model('magic_mirror_lit', 'itemMirror_lit')
flat_model('magic_mirror_active', 'itemMirror_active')
item_def('magic_mirror', {'type': 'minecraft:condition', 'property': 'minecraft:using_item',
                          'on_true': plain(n('item/magic_mirror_active')),
                          'on_false': {'type': 'minecraft:condition', 'property': 'minecraft:custom_model_data', 'index': 0,
                                       'on_true': plain(n('item/magic_mirror_lit')), 'on_false': plain(n('item/magic_mirror'))}})
name(f'item.{NS}.magic_mirror', 'Magic Mirror', 'Zauberspiegel')
# the hookshot: fired while in use
for t in ('itemHookshot', 'itemHookshot_fired', 'itemHookshot_barb'):
    tex(ITEM_TEX + t + '.png', 'item/' + t)
handheld_model('hookshot', 'itemHookshot')
handheld_model('hookshot_fired', 'itemHookshot_fired')
item_def('hookshot', {'type': 'minecraft:condition', 'property': 'minecraft:using_item',
                      'on_true': plain(n('item/hookshot_fired')), 'on_false': plain(n('item/hookshot'))})
name(f'item.{NS}.hookshot', 'Hookshot', 'Enterhaken')

name(f'item.{NS}.quiver.count', 'Arrows: %s / %s', 'Pfeile: %s / %s')
name(f'item.{NS}.bomb_bag.count', 'Bombs: %s / %s', 'Bomben: %s / %s')
name(f'item.{NS}.augment', 'Augment: %s', 'Verstärkung: %s')
name(f'itemGroup.{NS}', 'LegendGear', 'LegendGear')

# ---------------------------------------------------------------- blocks
def block_tex(t):
    tex(BLOCK_TEX + t + '.png', 'block/' + t)
    return n('block/' + t)


def block_model(model, obj):
    write(f'assets/{NS}/models/block/{model}.json', obj)


def block_item(name_, model):
    item_def(name_, plain(model))


def states(block, variants):
    write(f'assets/{NS}/blockstates/{block}.json', {'variants': variants})


# mystic shrub (stage 0 plain, 1 charged, 2 cut); the energized shrub item shows the charged one
for i, t in enumerate(('blockShrub_plain', 'blockShrub_charged', 'blockShrub_cut')):
    block_model(f'mystic_shrub_{i}', {'parent': 'minecraft:block/cross', 'textures': {'cross': block_tex(t)}, 'render_type': 'minecraft:cutout'})
states('mystic_shrub', {f'stage={i}': {'model': n(f'block/mystic_shrub_{i}')} for i in range(3)})
write(f'assets/{NS}/models/item/mystic_shrub.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': n('block/blockShrub_plain')}})
write(f'assets/{NS}/models/item/energized_mystic_shrub.json', {'parent': 'minecraft:item/generated',
                                                                'textures': {'layer0': n('block/blockShrub_charged')}})
block_item('mystic_shrub', n('item/mystic_shrub'))
block_item('energized_mystic_shrub', n('item/energized_mystic_shrub'))
name(f'block.{NS}.mystic_shrub', 'Mystic Shrub', 'Mystischer Strauch')
name(f'item.{NS}.energized_mystic_shrub', 'Energized Mystic Shrub', 'Aufgeladener mystischer Strauch')
name(f'block.{NS}.energized_mystic_shrub', 'Energized Mystic Shrub', 'Aufgeladener mystischer Strauch')

# bomb flower
block_model('bomb_flower', {'parent': 'minecraft:block/cross', 'textures': {'cross': block_tex('blockBombFlower')}, 'render_type': 'minecraft:cutout'})
block_model('bomb_flower_cut', {'parent': 'minecraft:block/cross', 'textures': {'cross': block_tex('blockBombFlower_cut')},
                                'render_type': 'minecraft:cutout'})
states('bomb_flower', {'cut=false': {'model': n('block/bomb_flower')}, 'cut=true': {'model': n('block/bomb_flower_cut')}})
write(f'assets/{NS}/models/item/bomb_flower.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': n('block/blockBombFlower')}})
block_item('bomb_flower', n('item/bomb_flower'))
name(f'block.{NS}.bomb_flower', 'Bomb Flower', 'Bombenblume')

# caltrops: render type 6 (crops)
block_model('caltrops', {'parent': 'minecraft:block/crop', 'textures': {'crop': block_tex('blockCaltrops')}, 'render_type': 'minecraft:cutout'})
states('caltrops', {'': {'model': n('block/caltrops')}})
write(f'assets/{NS}/models/item/caltrops.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': n('block/blockCaltrops')}})
block_item('caltrops', n('item/caltrops'))
name(f'block.{NS}.caltrops', 'Caltrops', 'Krähenfüße')

# chained block: chains, unchaining, iron, gold, diamond lock
for i, t in enumerate(('blockChained', 'blockChained_unlocked', 'blockChained_iron', 'blockChained_gold', 'blockChained_diamond')):
    block_model(f'chained_block_{i}', {'parent': 'minecraft:block/cube_all', 'textures': {'all': block_tex(t)}, 'render_type': 'minecraft:cutout'})
states('chained_block', {f'kind={i}': {'model': n(f'block/chained_block_{i}')} for i in range(5)})
for name_, i, english, german in (('chained_block', 0, 'Chained Block', 'Angeketteter Block'), ('iron_lock', 2, 'Iron Lock', 'Eisenschloss'),
                                  ('gold_lock', 3, 'Gold Lock', 'Goldschloss'), ('diamond_lock', 4, 'Diamond Lock', 'Diamantschloss')):
    block_item(name_, n(f'block/chained_block_{i}'))
    name(f'item.{NS}.{name_}' if i else f'block.{NS}.{name_}', english, german)

# clay jar: the stacked rings of RenderJar (half width, height) from the top down
rings = [(4, 1), (3, 1), (4, 1), (5, 2), (6, 3), (7, 5), (6, 2), (4, 1)]
elements, top = [], 16
for w, h in rings:
    faces = {d: {'texture': '#side'} for d in ('north', 'south', 'west', 'east')}
    faces['up'] = {'texture': '#top'}
    faces['down'] = {'texture': '#bottom'}
    elements.append({'from': [8 - w, top - h, 8 - w], 'to': [8 + w, top, 8 + w], 'faces': faces})
    top -= h
block_model('clay_jar', {'textures': {'top': block_tex('blockJar_top'), 'side': block_tex('blockJar_side'), 'bottom': block_tex('blockJar_bottom'),
                                      'particle': n('block/blockJar_side')}, 'elements': elements})
states('clay_jar', {'': {'model': n('block/clay_jar')}})
block_item('clay_jar', n('block/clay_jar'))
name(f'block.{NS}.clay_jar', 'Clay Jar', 'Tonkrug')

# cubes
for block, t, english, german in (('sugar_cube', 'blockSugar', 'Sugar Cube', 'Zuckerwürfel'), ('skybeam', 'blockSkybeam', 'Skybeam Block', 'Himmelsstrahl-Block'),
                                  ('starstone', 'blockStarstone', 'Starstone', 'Sternenstein')):
    block_model(block, {'parent': 'minecraft:block/cube_all', 'textures': {'all': block_tex(t)}})
    states(block, {'': {'model': n(f'block/{block}')}})
    name(f'block.{NS}.{block}', english, german)
block_item('sugar_cube', n('block/sugar_cube'))
block_item('skybeam', n('block/skybeam'))
tex(ITEM_TEX + 'itemStarStone.png', 'item/itemStarStone')
flat_model('starstone', 'itemStarStone')
block_item('starstone', n('item/starstone'))

# sword pedestal: half high, the face texture on the long sides
for along_x in (False, True):
    sides = {}
    for d in ('north', 'south'):
        sides[d] = '#face' if along_x else '#side'
    for d in ('west', 'east'):
        sides[d] = '#side' if along_x else '#face'
    box = ([0, 0, 4], [16, 8, 12]) if along_x else ([4, 0, 0], [12, 8, 16])
    faces = {d: {'texture': t} for d, t in sides.items()}
    faces['up'] = {'texture': '#top'}
    faces['down'] = {'texture': '#top'}
    block_model(f'sword_pedestal_{"x" if along_x else "z"}', {
        'parent': 'minecraft:block/block',
        'textures': {'face': block_tex('blockSwordPedestal_front'), 'side': block_tex('blockSwordPedestal_side'),
                     'top': block_tex('blockSwordPedestal_top_h' if along_x else 'blockSwordPedestal_top_v'), 'particle': n('block/blockSwordPedestal_side')},
        'elements': [{'from': box[0], 'to': box[1], 'faces': faces}]})
states('sword_pedestal', {f'along_x={str(a).lower()},has_sword={str(s).lower()}': {'model': n(f'block/sword_pedestal_{"x" if a else "z"}')}
                          for a in (False, True) for s in (False, True)})
block_item('sword_pedestal', n('block/sword_pedestal_z'))
name(f'block.{NS}.sword_pedestal', 'Sword Pedestal', 'Schwertsockel')
block_model('sword_pedestal_technical', {'textures': {'particle': n('block/blockSwordPedestal_side')}, 'elements': []})
states('sword_pedestal_technical', {f'along_x={a}': {'model': n('block/sword_pedestal_technical')} for a in ('false', 'true')})
name(f'block.{NS}.sword_pedestal_technical', 'Sword Pedestal', 'Schwertsockel')

# starbeam torch
torch = block_tex('blockStarbeamTorch')
block_model('starbeam_torch', {'parent': 'minecraft:block/template_torch', 'textures': {'torch': torch}})
block_model('starbeam_wall_torch', {'parent': 'minecraft:block/template_torch_wall', 'textures': {'torch': torch}})
states('starbeam_torch', {'': {'model': n('block/starbeam_torch')}})
states('starbeam_wall_torch', {f'facing={f}': {'model': n('block/starbeam_wall_torch'), **({'y': y} if y else {})}
                               for f, y in (('east', 0), ('south', 90), ('west', 180), ('north', 270))})
write(f'assets/{NS}/models/item/starbeam_torch.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': torch}})
block_item('starbeam_torch', n('item/starbeam_torch'))
name(f'block.{NS}.starbeam_torch', 'Starbeam Torch', 'Sternenstrahl-Fackel')

# ---------------------------------------------------------------- entities, equipment, particles
for e, english, german in (('medallion', 'Medallion', 'Medaillon'), ('quake', 'Quake', 'Beben'), ('arrow_storm', 'Arrow Storm', 'Pfeilsturm'),
                           ('magic_boomerang', 'Magic Boomerang', 'Magischer Bumerang'), ('bomb', 'Bomb', 'Bombe'),
                           ('bomb_blast', 'Bomb Blast', 'Bombenexplosion'), ('fireblast', 'Firestorm', 'Feuersturm'),
                           ('whirlwind', 'Whirlwind', 'Wirbelwind'), ('hookshot_hook', 'Hookshot', 'Enterhaken'),
                           ('grind_star', 'Starbeam', 'Sternenstrahl'), ('falling_star', 'Falling Star', 'Sternschnuppe'),
                           ('ender_bomb', 'Ender Bomb', 'Enderbombe')):
    name(f'entity.{NS}.{e}', english, german)
assets.append(('nmccoy/legendgear/assets/star.png', f'assets/{NS}/textures/entity/star.png'))
assets.append(('nmccoy/legendgear/assets/beam.png', f'assets/{NS}/textures/entity/beam.png'))
assets.append(('nmccoy/legendgear/assets/red_headband.png', f'assets/{NS}/textures/entity/equipment/humanoid/headband.png'))
assets.append(('nmccoy/legendgear/assets/windboots.png', f'assets/{NS}/textures/entity/equipment/humanoid/wind_boots.png'))
write(f'assets/{NS}/equipment/headband.json', {'layers': {'humanoid': [{'texture': n('headband')}]}})
write(f'assets/{NS}/equipment/wind_boots.json', {'layers': {'humanoid': [{'texture': n('wind_boots')}]}})
write(f'data/{NS}/tags/item/repairs/none.json', {'values': []})

glyphs = [f'minecraft:sga_{c}' for c in 'abcdefghijklmnopqrstuvwxyz']
write(f'assets/{NS}/particles/rune.json', {'textures': glyphs})
write(f'assets/{NS}/particles/scramble.json', {'textures': glyphs})
write(f'assets/{NS}/particles/flare.json', {'textures': [f'minecraft:generic_{i}' for i in range(7, -1, -1)]})
write(f'assets/{NS}/particles/sparkle.json', {'textures': ['minecraft:spark_4']})

# ---------------------------------------------------------------- sounds
SOUNDS = {'sine': 'sine', 'heart': 'aheart', 'speedboost': 'speedboost', 'moneysmall': 'moneysmall', 'moneymid': 'moneymid',
          'moneybig': 'moneybig', 'partialcharge': 'partialcharge', 'fullcharge': 'fullcharge', 'repel': 'repel', 'boomerang': 'boomerang',
          'lift': 'lift', 'throw': 'throw', 'firebuild': 'firebuild', 'highcharge': 'highcharge', 'swordcharge': 'swordcharge',
          'whirlwind': 'whirlwind', 'popin': 'popin', 'swordplace': 'swordplace', 'swordtake': 'swordtake', 'unchain': 'unchain', 'wah': 'wah',
          'wahwah': 'wahwah', 'bah': 'bah', 'gah': 'gah', 'starfall': 'starfall', 'starcaught': 'starcaught', 'twinkle': 'twinkle',
          'sprinkle': 'sprinkle', 'transform': 'transform', 'fluteattack': 'fluteattack', 'flutesustain': 'flutesustain', 'dash': 'dash',
          'escalate': 'escalate'}
sounds = {}
for event, file in SOUNDS.items():
    assets.append((f'nmccoy/legendgear/assets/{file}.ogg', f'assets/{NS}/sounds/{file}.ogg'))
    sounds[event] = {'sounds': [n(file)], 'subtitle': f'subtitles.{NS}.{event}'}
    name(f'subtitles.{NS}.{event}', event.capitalize(), event.capitalize())
write(f'assets/{NS}/sounds.json', sounds)

# ---------------------------------------------------------------- enchantments, damage type
name(f'enchantment.{NS}.focus', 'Focus', 'Fokus')
name(f'enchantment.{NS}.soul_tether', 'Soul Tether', 'Seelenband')
# EnchantmentFocus(weight 5, weapon): min 5 + 20 (l - 1), max 1 + 10 l + 50, levels 1-2
write(f'data/{NS}/enchantment/focus.json', {
    'anvil_cost': 2, 'description': {'translate': f'enchantment.{NS}.focus'}, 'max_cost': {'base': 61, 'per_level_above_first': 10},
    'max_level': 2, 'min_cost': {'base': 5, 'per_level_above_first': 20}, 'slots': ['mainhand'],
    'supported_items': '#minecraft:enchantable/sword', 'weight': 5})
# EnchantmentSoulTether(weight 1): any damageable item, 20..50
write(f'data/{NS}/enchantment/soul_tether.json', {
    'anvil_cost': 8, 'description': {'translate': f'enchantment.{NS}.soul_tether'}, 'max_cost': {'base': 50, 'per_level_above_first': 0},
    'max_level': 1, 'min_cost': {'base': 20, 'per_level_above_first': 0}, 'slots': ['any'],
    'supported_items': '#minecraft:enchantable/durability', 'weight': 1})
for tag in ('in_enchanting_table', 'non_treasure', 'tradeable'):
    write(f'data/minecraft/tags/enchantment/{tag}.json', {'replace': False, 'values': [n('focus'), n('soul_tether')]})
write(f'data/{NS}/damage_type/firestorm.json', {'message_id': 'inFire', 'exhaustion': 0.1, 'scaling': 'when_caused_by_living_non_player'})
for tag in ('bypasses_armor', 'is_fire', 'witch_resistant_to'):
    write(f'data/minecraft/tags/damage_type/{tag}.json', {'replace': False, 'values': [n('firestorm')]})
name('death.attack.inFire.legendgear', '%1$s went up in flames', '%1$s ging in Flammen auf')

# ---------------------------------------------------------------- loot tables (blocks that drop something)
def self_drop(block, item_=None, count=None):
    entry = {'type': 'minecraft:item', 'name': n(item_ or block)}
    if count:
        entry['name'] = item_
        entry['modifier'] = [{'type': 'minecraft:set_count', 'count': count}]
    write(f'data/{NS}/loot_table/blocks/{block}.json', {'type': 'minecraft:block', 'pools': [
        {'rolls': 1, 'entries': [entry], 'condition': {'type': 'minecraft:survives_explosion'}}]})


self_drop('caltrops')
self_drop('sword_pedestal')
self_drop('skybeam')
self_drop('starstone')
self_drop('starbeam_torch')
self_drop('sugar_cube', 'minecraft:sugar', 9)
write(f'data/minecraft/tags/block/mineable/pickaxe.json', {'replace': False, 'values': [n('sword_pedestal'), n('skybeam'), n('starstone')]})
write(f'data/minecraft/tags/block/needs_diamond_tool.json', {'replace': False, 'values': [n('skybeam')]})
write(f'data/minecraft/tags/block/wall_post_override.json', {'replace': False, 'values': [n('starbeam_torch')]})

# ---------------------------------------------------------------- recipes (LegendGear.load)
def result(item_, count=1, components=None):
    r = {'id': item_ if ':' in item_ else n(item_)}
    if count != 1:
        r['count'] = count
    if components:
        r['components'] = components
    return r


def shaped(file, pattern, key, out, count=1, components=None, category='misc'):
    write(f'data/{NS}/recipe/{file}.json', {'type': 'minecraft:crafting_shaped', 'category': category, 'pattern': pattern,
                                            'key': key, 'result': result(out, count, components)})


def shapeless(file, ingredients, out, count=1, category='misc'):
    write(f'data/{NS}/recipe/{file}.json', {'type': 'minecraft:crafting_shapeless', 'category': category, 'ingredients': ingredients,
                                            'result': result(out, count)})


M = 'minecraft:'
shaped('magic_mirror', ['ege', 'gdg', 'ege'], {'e': M + 'ender_pearl', 'g': M + 'gold_ingot', 'd': M + 'diamond'}, 'magic_mirror', category='equipment')
shaped('emerald_piece', ['e'], {'e': M + 'emerald'}, 'emerald_piece', 8)
shaped('emerald_shard', ['p'], {'p': n('emerald_piece')}, 'emerald_shard', 8)
shaped('quiver', ['l l', 'lal', ' l '], {'l': M + 'leather', 'a': M + 'arrow'}, 'quiver', components={n('ammo'): 1}, category='equipment')
shaped('phoenix_feather', ['tpt', 'pfp', 'tpt'], {'t': M + 'ghast_tear', 'p': M + 'blaze_powder', 'f': M + 'feather'}, 'phoenix_feather')
for med, core in (('earth_medallion', M + 'dirt'), ('wind_medallion', M + 'arrow'), ('fire_medallion', [M + 'coal', M + 'charcoal']),
                  ('ender_medallion', M + 'ender_pearl')):
    shaped(med, ['gbg', 'bcb', 'gbg'], {'g': M + 'gold_nugget', 'b': M + 'brick', 'c': core}, med, components={M + 'damage': 50},
           category='equipment')
for amulet, med in (('aero_amulet', 'wind_medallion'), ('geo_amulet', 'earth_medallion'), ('pyro_amulet', 'fire_medallion')):
    shaped(amulet, [' g ', 'g g', ' m '], {'g': M + 'gold_ingot', 'm': n(med)}, amulet, category='equipment')
shaped('titan_band', ['glg', 'lml', 'glg'], {'g': M + 'gold_ingot', 'l': M + 'leather', 'm': n('earth_medallion')}, 'titan_band', category='equipment')
shaped('headband', ['www', 'wmw'], {'w': '#minecraft:wool', 'm': n('fire_medallion')}, 'headband', category='equipment')
shaped('wind_boots', ['g g', 'wmw'], {'g': M + 'gold_ingot', 'w': '#minecraft:wool', 'm': n('wind_medallion')}, 'wind_boots', category='equipment')
shaped('magic_boomerang', ['dgg', 'g  ', 'g  '], {'g': M + 'gold_ingot', 'd': M + 'diamond'}, 'magic_boomerang', category='equipment')
shaped('magic_boomerang_down', ['  g', '  g', 'ggd'], {'g': M + 'gold_ingot', 'd': M + 'diamond'}, 'magic_boomerang', category='equipment')
shaped('hookshot', [' ii', ' fi', 'd  '], {'i': M + 'iron_ingot', 'f': M + 'fishing_rod', 'd': M + 'dispenser'}, 'hookshot', category='equipment')
shaped('bomb_bag', ['l l', 'lbl', 'lll'], {'l': M + 'leather', 'b': n('bomb')}, 'bomb_bag', components={n('ammo'): 1}, category='equipment')
shaped('sugar_cube', ['sss', 'sss', 'sss'], {'s': M + 'sugar'}, 'sugar_cube', category='building')
shapeless('sugar_from_cube', [n('sugar_cube')], M + 'sugar', 9)
for candy, extra in (('redstone_rock_candy', M + 'redstone'), ('emerald_rock_candy', M + 'emerald'), ('lapis_rock_candy', M + 'lapis_lazuli'),
                     ('diamond_rock_candy', M + 'diamond')):
    shapeless(candy, [n('sugar_cube')] * 6 + [M + 'stick', extra, M + 'water_bucket'], candy)
shapeless('milk_chocolate', [M + 'milk_bucket', M + 'cocoa_beans', M + 'sugar'], 'milk_chocolate')
shaped('clay_jar', ['b b', 'b b', 'bbb'], {'b': M + 'brick'}, 'clay_jar', 16, category='building')
shaped('caltrops', [' i ', ' i ', 'i i'], {'i': M + 'iron_ingot'}, 'caltrops', 4)
shaped('sword_pedestal', [' s ', 'sss'], {'s': M + 'stone'}, 'sword_pedestal', category='building')
shapeless('stardust', [n('star_piece')], 'stardust', 3)
shapeless('star_piece_from_starstone', [n('starstone')], 'star_piece', 9)
shaped('starstone', ['sss', 'sss', 'sss'], {'s': n('star_piece')}, 'starstone', category='building')
shapeless('magic_powder', [n('stardust'), M + 'brown_mushroom'], 'magic_powder')
shapeless('magic_powder_red', [n('stardust'), M + 'red_mushroom'], 'magic_powder')
shaped('reed_pipes', ['rrr', 'rr ', 'r  '], {'r': M + 'sugar_cane'}, 'reed_pipes')
shaped('skybeam', ['ooo', 'oso', 'ooo'], {'o': M + 'obsidian', 's': n('starstone')}, 'skybeam', category='building')
shaped('slime_sword', ['b', 'b', 's'], {'b': M + 'slime_ball', 's': M + 'stick'}, 'slime_sword', category='equipment')
shaped('starbeam_torch', [' g ', 'gdg', ' i '], {'g': n('stardust'), 'd': M + 'diamond', 'i': M + 'gold_ingot'}, 'starbeam_torch', 8)
write(f'data/{NS}/recipe/empty_ammo_bag.json', {'type': n('empty_ammo_bag')})
write(f'data/{NS}/recipe/medallion_sword.json', {'type': n('medallion_sword')})

# ---------------------------------------------------------------- language files, original-assets.txt
for lang, d in (('en_us', en), ('de_de', de)):
    write(f'assets/{NS}/lang/{lang}.json', dict(sorted(d.items())))
with open(ROOT + '/original-assets.txt', 'w') as f:
    f.write('# path in NMcCoy-LegendGear-1.5.2.zip -> path in the ported jar (generated by tools/gen.py)\n')
    seen = set()
    for src, dst in assets:
        if dst not in seen:
            seen.add(dst)
            f.write(f'{src} -> {dst}\n')
