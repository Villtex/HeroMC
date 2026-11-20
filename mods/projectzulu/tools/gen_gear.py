# Teil 11, batch Gear (ZuluGear). Runs inside tools/gen.py (write, n, NS, lang, assets, simple_item).
# Armour sets, jasper, ivory spikes, campfires, mob heads, tombstone: lang, item/block models, blockstates, loot tables,
# tags, recipes, equipment assets and textures. Runs before gen_plants/gen_potions (alphabetical): shared files under
# data/minecraft/tags are merged, helper names are prefixed _zg_ so they do not clash with the other batches.
# Original: blocks/*.java, blocks/itemblockdeclarations/*.java, blocks/ItemBlockRecipeManager.java, blocks/Render*.java.

import math

_ZG_BLOCK_TEX = 'mods/projectzulublock/textures/blocks'
_ZG_ITEM_TEX = 'mods/projectzulublock/textures/items'

def _zg_merge_tag(registry, tag, values):
    ns, path = tag.split(':')
    rel = f'data/{ns}/tags/{registry}/{path}.json'
    full = os.path.join(RES, rel)
    obj = {'replace': False, 'values': []}
    if os.path.isfile(full):
        with open(full) as f:
            obj = json.load(f)
    for v in values:
        if v not in obj['values']:
            obj['values'].append(v)
    write(rel, obj)

def _zg_model(name, obj):
    write(f'assets/{NS}/models/block/{name}.json', obj)

def _zg_block_tex(src, dst):
    assets.append((f'{_ZG_BLOCK_TEX}/{src}.png', f'assets/{NS}/textures/block/{dst}.png'))

def _zg_item_def(name, model):
    write(f'assets/{NS}/items/{name}.json', {'model': {'type': 'minecraft:model', 'model': model}})

def _zg_flat_item(name, texture):
    write(f'assets/{NS}/models/item/{name}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': texture}})
    _zg_item_def(name, n(f'item/{name}'))

def _zg_loot(name, item):
    write(f'data/{NS}/loot_table/blocks/{name}.json',
          {'type': 'minecraft:block', 'random_sequence': n(f'blocks/{name}'),
           'pools': [{'rolls': 1, 'condition': {'type': 'minecraft:survives_explosion'},
                      'entries': [{'type': 'minecraft:item', 'name': item}]}]})

def _zg_recipe(name, obj, conditions=None):
    if conditions:
        obj = {'neoforge:conditions': conditions, **obj}
    write(f'data/{NS}/recipe/{name}.json', obj)

def _zg_shaped(name, pattern, key, result, category='misc', conditions=None):
    _zg_recipe(name, {'type': 'minecraft:crafting_shaped', 'category': category, 'key': key, 'pattern': pattern,
                      'result': {'count': 1, 'id': result}}, conditions)

# ================================================================= armour (10 sets)
# set id -> (equipment asset = ArmorMaterial asset in ZuluGear, 1.5.2 armour texture armor_sets/<x>_1/_2.png,
#            item icon prefix textures/items/<prefix>helmet/chest/legs/boots.png, display prefix)
_ZG_SETS = [
    ('scale', 'scale', 'scaleArmor', 'scale', 'Scale'),
    ('gold_scale', 'gold_scale', 'goldscale', 'goldscale', 'Gold Scale'),
    ('iron_scale', 'iron_scale', 'ironscale', 'ironscale', 'Iron Plated Scale'),
    ('diamond_scale', 'diamond_scale', 'diamondscale', 'diamondscale', 'Diamond Plated Scale'),
    ('white_cloth', 'white_desert_cloth', 'whitedesertcloth', 'whitecloth', 'White Cloth'),
    ('red_cloth', 'red_desert_cloth', 'reddesertcloth', 'redcloth', 'Red Cloth'),
    ('green_cloth', 'green_desert_cloth', 'greendesertcloth', 'greencloth', 'Green Cloth'),
    ('blue_cloth', 'blue_desert_cloth', 'bluedesertcloth', 'bluecloth', 'Blue Cloth'),
    ('cactus', 'cactus', 'cactusarmor', 'cactus', 'Cactus Cloth'),
    ('fur', 'mammoth_fur', 'mammothfur', 'fur', 'Fur Cloth'),
]
_ZG_PARTS = [  # item suffix, icon suffix, name suffix, enchantable slot
    ('helmet', 'helmet', 'Helmet', 'head'),
    ('chestplate', 'chest', 'Chestplate', 'chest'),
    ('leggings', 'legs', 'Leggings', 'leg'),
    ('boots', 'boots', 'Boots', 'foot'),
]
_zg_armor_items = {}
for _set, _asset, _tex, _icon, _title in _ZG_SETS:
    for _part, _ipart, _ptitle, _slot in _ZG_PARTS:
        _item = f'{_set}_{_part}'
        _zg_armor_items[_item] = _item
        simple_item(_item, f'{_icon}{_ipart}', f'{_title} {_ptitle}')
        for _t in (f'minecraft:enchantable/{_slot}_armor', 'minecraft:enchantable/armor', 'minecraft:enchantable/equippable',
                   'minecraft:enchantable/durability'):
            _zg_merge_tag('item', _t, [n(_item)])
    # 1.5.2 armour textures: _1 = helmet/chest/boots layer, _2 = leggings layer
    assets.append((f'mods/projectzulublock/armor_sets/{_tex}_1.png', f'assets/{NS}/textures/entity/equipment/humanoid/{_asset}.png'))
    assets.append((f'mods/projectzulublock/armor_sets/{_tex}_2.png',
                   f'assets/{NS}/textures/entity/equipment/humanoid_leggings/{_asset}.png'))
    write(f'assets/{NS}/equipment/{_asset}.json', {'layers': {'humanoid': [{'texture': n(_asset)}],
                                                             'humanoid_leggings': [{'texture': n(_asset)}]}})
# EnumHelper materials have no repair material in 1.5.2: ZuluGear.NO_REPAIR is an empty tag
write(f'data/{NS}/tags/item/zulu_armor_repair.json', {'replace': False, 'values': []})

# ================================================================= jasper (BlockJasper) "Jasper Block"
_zg_block_tex('jasper', 'jasper')
_zg_model('jasper', {'parent': 'minecraft:block/cube_all', 'textures': {'all': n('block/jasper')}})
write(f'assets/{NS}/blockstates/jasper.json', {'variants': {'': {'model': n('block/jasper')}}})
_zg_item_def('jasper', n('block/jasper'))
lang[f'block.{NS}.jasper'] = 'Jasper Block'
_zg_loot('jasper', n('jasper'))  # idDropped = own id, quantityDropped 1

# ================================================================= spikes (BlockSpikes) "Ivory Spikes"
# RenderSpike: two crossed squares over the whole block (the points face FACING), on a fence two narrow crossed planes
# 0.6 high, on iron bars one plane along every connected arm (half arms for one connection, a cross for none).
_ZG_SPIKE_TEX = {'normal': 'spikes', 'poison': 'spikes_poison', 'sticky': 'spikes_sticky'}
_HEIGHT = 9.6  # maxY 0.6
for _t, _tex in _ZG_SPIKE_TEX.items():
    _zg_block_tex(_tex, _tex)

def _zg_plane_cross(tex, half, height, y_rescale):
    """two planes crossing at 45 degrees around the vertical axis (like minecraft:block/cross)"""
    els = []
    for angle in (45, -45):
        rot = {'origin': [8, 0, 8], 'axis': 'y', 'angle': angle}
        if y_rescale:
            rot['rescale'] = True
        els.append({'from': [8 - half, 0, 8], 'to': [8 + half, height, 8], 'rotation': rot,
                    'shade': False,
                    'faces': {'north': {'uv': [0, 0, 16, 16] if y_rescale else [4, 0, 12, 16], 'texture': '#t'},
                              'south': {'uv': [0, 0, 16, 16] if y_rescale else [4, 0, 12, 16], 'texture': '#t'}}})
    return els

def _zg_strip(axis, lo, hi):
    """one plane along z (axis 'z', faces east/west) or x (axis 'x', faces north/south) from lo to hi (0-16),
    texture u as in the original (u grows with the coordinate; the half arms use half of the texture)"""
    ua, ub = lo, hi
    if axis == 'z':
        return {'from': [8, 0, lo], 'to': [8, _HEIGHT, hi], 'shade': False,
                'faces': {'west': {'uv': [ua, 0, ub, 16], 'texture': '#t'}, 'east': {'uv': [ub, 0, ua, 16], 'texture': '#t'}}}
    return {'from': [lo, 0, 8], 'to': [hi, _HEIGHT, 8], 'shade': False,
            'faces': {'south': {'uv': [ua, 0, ub, 16], 'texture': '#t'}, 'north': {'uv': [ub, 0, ua, 16], 'texture': '#t'}}}

_ZG_SPIKE_SHAPES = {
    'cross': _zg_plane_cross('#t', 8, 16, True),
    'fence': _zg_plane_cross('#t', 2.83, _HEIGHT, False),
    'z_full': [_zg_strip('z', 0, 16)], 'z_south': [_zg_strip('z', 8, 16)], 'z_north': [_zg_strip('z', 0, 8)],
    'x_full': [_zg_strip('x', 0, 16)], 'x_east': [_zg_strip('x', 8, 16)], 'x_west': [_zg_strip('x', 0, 8)],
}
for _t, _tex in _ZG_SPIKE_TEX.items():
    for _shape, _els in _ZG_SPIKE_SHAPES.items():
        _zg_model(f'spikes_{_shape}_{_t}', {'textures': {'particle': n(f'block/{_tex}'), 't': n(f'block/{_tex}')}, 'elements': _els})

_zg_parts = []
# facing -> blockstate rotation of the upward model (barrel convention: x 90 turns up to north, y 90 north to east)
_ZG_FACE_ROT = {'up': {}, 'down': {'x': 180}, 'north': {'x': 90}, 'east': {'x': 90, 'y': 90},
                'south': {'x': 90, 'y': 180}, 'west': {'x': 90, 'y': 270}}
for _t in _ZG_SPIKE_TEX:
    def _p(when, shape, extra=None, _t=_t):
        _zg_parts.append({'when': {'type': _t, **when}, 'apply': {'model': n(f'block/spikes_{shape}_{_t}'), **(extra or {})}})
    for _f, _rot in _ZG_FACE_ROT.items():
        _p({'mount': 'none', 'facing': _f}, 'cross', _rot)
    _p({'mount': 'fence'}, 'fence')
    # iron bars: north/south arm along z, west/east arm along x; nothing connected: both strips
    for _n, _s, _shape in ((True, True, 'z_full'), (False, True, 'z_south'), (True, False, 'z_north')):
        _p({'mount': 'bars', 'north': str(_n).lower(), 'south': str(_s).lower()}, _shape)
    for _w, _e, _shape in ((True, True, 'x_full'), (False, True, 'x_east'), (True, False, 'x_west')):
        _p({'mount': 'bars', 'west': str(_w).lower(), 'east': str(_e).lower()}, _shape)
    _p({'mount': 'bars', 'north': 'false', 'south': 'false', 'west': 'false', 'east': 'false'}, 'z_full')
    _p({'mount': 'bars', 'north': 'false', 'south': 'false', 'west': 'false', 'east': 'false'}, 'x_full')
write(f'assets/{NS}/blockstates/spikes.json', {'multipart': _zg_parts})
_zg_flat_item('spikes', n('block/spikes'))
lang[f'block.{NS}.spikes'] = 'Ivory Spikes'
_zg_loot('spikes', n('spikes'))

# ================================================================= campfire (BlockCampfire, ItemCampFire metadata 0-3)
# RenderCampFire: wood = five flat logs (two diagonal rows), stone = small logs in the middle plus a ring of stones,
# lit = the vanilla fire texture. Boxes are turned around y; the original angle t (counter-clockwise in x/z) is -t here.
def _zg_box(dx, dz, w, d, h, angle, tex, uv):
    cx, cz = (0.5 + dx) * 16, (0.5 + dz) * 16
    face = {'uv': uv, 'texture': tex}
    faces = {side: dict(face) for side in ('north', 'south', 'east', 'west', 'up')}
    el = {'from': [round(cx - w * 8, 3), 0, round(cz - d * 8, 3)], 'to': [round(cx + w * 8, 3), round(h * 16, 3), round(cz + d * 8, 3)],
          'faces': faces}
    if angle:
        el['rotation'] = {'origin': [round(cx, 3), 0, round(cz, 3)], 'axis': 'y', 'angle': angle}
    return el

_ZG_Q = -45   # original pi/4
_ZG_Q3 = 45   # original 3 pi/4 (a box is symmetric under 180 degrees: -135 = +45)
_zg_model('campfire_wood', {'textures': {'particle': 'minecraft:block/oak_log', 'log': 'minecraft:block/oak_log'}, 'elements': [
    _zg_box(0, 0, .35, .2, .2, _ZG_Q, '#log', [0, 0, 8, 8]),
    _zg_box(.24, .24, .33, .2, .2, _ZG_Q, '#log', [0, 0, 8, 8]),
    _zg_box(-.24, -.24, .33, .2, .2, _ZG_Q, '#log', [0, 0, 8, 8]),
    _zg_box(-.21, .21, .4, .2, .2, _ZG_Q3, '#log', [0, 0, 8, 8]),
    _zg_box(.21, -.21, .4, .2, .2, _ZG_Q3, '#log', [0, 0, 8, 8]),
]})
_zg_model('campfire_stone', {'textures': {'particle': 'minecraft:block/cobblestone', 'log': 'minecraft:block/oak_log',
                                          'stone': 'minecraft:block/cobblestone'}, 'elements': [
    _zg_box(0, 0, .2, .2, .2, _ZG_Q, '#log', [0, 0, 8, 8]),
    _zg_box(.14, .14, .2, .2, .2, _ZG_Q, '#log', [0, 0, 8, 8]),
    _zg_box(-.14, -.14, .2, .2, .2, _ZG_Q, '#log', [0, 0, 8, 8]),
    _zg_box(-.14, .14, .2, .2, .2, _ZG_Q3, '#log', [0, 0, 8, 8]),
    _zg_box(.14, -.14, .2, .2, .2, _ZG_Q3, '#log', [0, 0, 8, 8]),
    _zg_box(.4, 0, .2, .3, .3, 0, '#stone', [0, 0, 16, 16]),
    _zg_box(-.4, 0, .2, .3, .3, 0, '#stone', [0, 0, 16, 16]),
    _zg_box(0, .4, .3, .2, .3, 0, '#stone', [0, 0, 16, 16]),
    _zg_box(0, -.4, .3, .2, .3, 0, '#stone', [0, 0, 16, 16]),
    _zg_box(.33, .33, .3, .2, .3, _ZG_Q3, '#stone', [0, 0, 16, 16]),
    _zg_box(-.33, -.33, .3, .2, .3, _ZG_Q3, '#stone', [0, 0, 16, 16]),
    _zg_box(-.33, .33, .3, .2, .3, _ZG_Q, '#stone', [0, 0, 16, 16]),
    _zg_box(.33, -.33, .3, .2, .3, _ZG_Q, '#stone', [0, 0, 16, 16]),
]})
_zg_model('campfire_flame', {'textures': {'particle': 'minecraft:block/fire_0', 'fire': 'minecraft:block/fire_0'}, 'elements': [
    {'from': [1, 1, 8], 'to': [15, 15, 8], 'shade': False, 'rotation': {'origin': [8, 0, 8], 'axis': 'y', 'angle': a, 'rescale': True},
     'faces': {'north': {'uv': [0, 0, 16, 16], 'texture': '#fire'}, 'south': {'uv': [0, 0, 16, 16], 'texture': '#fire'}}}
    for a in (45, -45)]})
write(f'assets/{NS}/blockstates/campfire.json', {'multipart': [
    {'when': {'kind': 'wood'}, 'apply': {'model': n('block/campfire_wood')}},
    {'when': {'kind': 'stone'}, 'apply': {'model': n('block/campfire_stone')}},
    {'when': {'lit': 'true'}, 'apply': {'model': n('block/campfire_flame')}},
]})
lang[f'block.{NS}.campfire'] = 'Campfire'
_zg_loot('campfire', n('campfire'))  # no damageDropped: always the plain wood campfire
for _item, _icon, _title in (('campfire', 'campfire_wood', 'Campfire'), ('stone_campfire', 'campfire_stone', 'Stone Campfire'),
                             ('lit_campfire', 'campfire_woodfire', 'Lit Campfire'),
                             ('lit_stone_campfire', 'campfire_stonefire', 'Lit Stone Campfire')):
    simple_item(_item, _icon, _title, src_dir=_ZG_BLOCK_TEX)

# ================================================================= mob heads (BlockMobHeads, drawn by the block entity renderer)
_zg_model('mob_head', {'textures': {'particle': 'minecraft:block/bone_block_side'}})
write(f'assets/{NS}/blockstates/mob_head.json',
      {'variants': {f'facing={f}': {'model': n('block/mob_head')} for f in ('up', 'north', 'south', 'east', 'west')}})
lang[f'block.{NS}.mob_head'] = 'Mob Head'
# drops come from MobHeadBlock.getDrops (the head of the block entity), no loot table
for _head, _title, _icon in (
        ('red_finch', 'Stuffed Finch', 'redfinch'), ('alligator', 'Alligator Head', 'crocodile'), ('armadillo', 'Armadillo Head', 'armadillo'),
        ('black_bear', 'Black Bear Head', 'blackbear'), ('brown_bear', 'Brown Bear Head', 'brownbear'),
        ('polar_bear', 'Polar Bear Head', 'polarbear'), ('beaver', 'Beaver Head', 'beaver'), ('boar', 'Boar Head', 'boar'),
        ('giraffe', 'Giraffe Head', 'giraffe'), ('gorilla', 'Gorilla Head', 'gorilla'), ('lizard', 'Lizard Head', 'lizard'),
        ('mammoth', 'Mammoth Head', 'mammoth'), ('ostrich', 'Ostrich Head', 'ostrich'), ('penguin', 'Penguin Head', 'penguin'),
        ('rhino', 'Rhino Head', 'rhino'), ('tree_ent', 'TreeEnt Head', 'treeent'), ('vulture', 'Vulture Head', 'vulture'),
        ('elephant', 'Elephant Head', 'elephant')):
    simple_item(f'mob_head_{_head}', f'mobskulls_{_icon}', _title, src_dir=_ZG_BLOCK_TEX)

# ================================================================= tombstone (BlockTombstone, block entity renderer)
_zg_block_tex('tombstone', 'tombstone')
_zg_model('tombstone', {'textures': {'particle': n('block/tombstone')}})
write(f'assets/{NS}/blockstates/tombstone.json', {'variants': {f'rotation={r}': {'model': n('block/tombstone')} for r in range(8)}})
_zg_flat_item('tombstone', n('block/tombstone'))
lang[f'block.{NS}.tombstone'] = 'Tombstone'
_zg_loot('tombstone', n('tombstone'))
# TileEntityTombstoneRenderer texture (ModelTombstone, 64x32)
assets.append(('mods/projectzulublock/Tombstone.png', f'assets/{NS}/textures/entity/tombstone.png'))

# ================================================================= game rule doesCampfireBurn
lang[f'gamerule.{NS}.does_campfire_burn'] = 'Campfire burns'
lang[f'gamerule.{NS}.does_campfire_burn.description'] = 'Controls whether a lit campfire sets entities standing in it on fire.'

# ================================================================= tags (requiresCorrectToolForDrops)
_zg_merge_tag('block', 'minecraft:mineable/pickaxe', [n('jasper'), n('spikes'), n('tombstone')])
_zg_merge_tag('block', 'minecraft:mineable/axe', [n('campfire')])

# ================================================================= recipes (ItemBlockRecipeManager)
# setupCampfireRecipies: Block.wood (any log) and the ore dictionary "log" give the same recipe; the lit ones take a stick
# (Block.wood variant) or a cobblestone (ore dictionary variant) on top.
_LOG, _COBBLE = '#minecraft:logs', 'minecraft:cobblestone'
_ZG_EBXL = [{'type': 'neoforge:mod_loaded', 'modid': 'extrabiomesxl'}]
_zg_shaped('campfire', ['LLL'], {'L': _LOG}, n('campfire'))
_zg_shaped('stone_campfire', ['LLL', 'CCC'], {'L': _LOG, 'C': _COBBLE}, n('stone_campfire'))
_zg_shaped('lit_campfire', [' S ', 'LLL'], {'L': _LOG, 'S': 'minecraft:stick'}, n('lit_campfire'))
_zg_shaped('lit_campfire_from_cobblestone', [' S ', 'LLL'], {'L': _LOG, 'S': _COBBLE}, n('lit_campfire'))
_zg_shaped('lit_stone_campfire', [' S ', 'LLL', 'CCC'], {'L': _LOG, 'C': _COBBLE, 'S': 'minecraft:stick'}, n('lit_stone_campfire'))
_zg_shaped('lit_stone_campfire_from_cobblestone', [' S ', 'LLL', 'CCC'], {'L': _LOG, 'C': _COBBLE, 'S': _COBBLE}, n('lit_stone_campfire'))
# setupExtrabiomesXLRecipies: the ore dictionary "cobbleRed" (red cobblestone of Extrabiomes XL), only if that mod is there
_RED = 'extrabiomesxl:red_cobblestone'
_zg_shaped('stone_campfire_red_cobblestone', ['LLL', 'CCC'], {'L': _LOG, 'C': _RED}, n('stone_campfire'), conditions=_ZG_EBXL)
_zg_shaped('lit_stone_campfire_red_cobblestone', [' S ', 'LLL', 'CCC'], {'L': _LOG, 'C': _RED, 'S': 'minecraft:stick'},
           n('lit_stone_campfire'), conditions=_ZG_EBXL)
_zg_shaped('lit_stone_campfire_red_cobblestone_top', [' S ', 'LLL', 'CCC'], {'L': _LOG, 'C': _RED, 'S': _RED},
           n('lit_stone_campfire'), conditions=_ZG_EBXL)
_zg_shaped('lit_campfire_red_cobblestone', [' S ', 'LLL'], {'L': _LOG, 'S': _RED}, n('lit_campfire'), conditions=_ZG_EBXL)

# jasper: sand around rose red or redstone
_zg_shaped('jasper_from_red_dye', ['XXX', 'XCX', 'XXX'], {'X': 'minecraft:sand', 'C': 'minecraft:red_dye'}, n('jasper'))
_zg_shaped('jasper_from_redstone', ['XXX', 'XCX', 'XXX'], {'X': 'minecraft:sand', 'C': 'minecraft:redstone'}, n('jasper'))
# spikes: three tusks
_zg_shaped('spikes', ['TTT'], {'T': n('tusk')}, n('spikes'))

# armour
_ZG_SHAPES = {'helmet': ['XXX', 'X X'], 'chestplate': ['X X', 'XXX', 'XXX'], 'leggings': ['XXX', 'X X', 'X X'], 'boots': ['X X', 'X X']}
_ZG_PLATED = {'helmet': ['XXX', 'XYX'], 'chestplate': ['XYX', 'XXX', 'XXX'], 'leggings': ['XXX', 'XYX', 'X X'], 'boots': ['XYX', 'X X']}
for _part in _ZG_SHAPES:
    _zg_shaped(f'scale_{_part}', _ZG_SHAPES[_part], {'X': n('scale_item')}, n(f'scale_{_part}'), 'equipment')
    for _set, _ingot in (('gold_scale', 'minecraft:gold_ingot'), ('iron_scale', 'minecraft:iron_ingot'), ('diamond_scale', 'minecraft:diamond')):
        _zg_shaped(f'{_set}_{_part}', _ZG_PLATED[_part], {'X': _ingot, 'Y': n(f'scale_{_part}')}, n(f'{_set}_{_part}'), 'equipment')
    # wool of 1.5.2: red 14, green 13 and lime 5, blue 11 and light blue 3 (white cloth has no recipe)
    for _set, _wools in (('red_cloth', ['red_wool']), ('green_cloth', ['green_wool', 'lime_wool']),
                         ('blue_cloth', ['blue_wool', 'light_blue_wool'])):
        for _wool in _wools:
            _name = f'{_set}_{_part}' if len(_wools) == 1 or _wool == _wools[0] else f'{_set}_{_part}_from_{_wool}'
            _zg_shaped(_name, _ZG_SHAPES[_part], {'X': f'minecraft:{_wool}'}, n(f'{_set}_{_part}'), 'equipment')
    _zg_shaped(f'cactus_{_part}', _ZG_SHAPES[_part], {'X': 'minecraft:cactus'}, n(f'cactus_{_part}'), 'equipment')
# Fur armour: the original recipes of helmet, chestplate and leggings used cactus (identical to the cactus armour, registered
# later, so they never matched); only the boots take fur pelts. Same here.
_zg_shaped('fur_boots', _ZG_SHAPES['boots'], {'X': n('fur_pelt')}, n('fur_boots'), 'equipment')
