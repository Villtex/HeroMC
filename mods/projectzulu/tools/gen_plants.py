# Teil 11, batch Plants (ZuluPlants). Runs inside tools/gen.py (write, n, NS, lang, assets, simple_item).
# Aloe vera, watered dirt, tumbleweed, palm tree, coconut, quicksand, night bloom, creeper blossom, universal flower pot.
# Original: blocks/*.java, blocks/itemblockdeclarations/*.java, blocks/ItemBlockRecipeManager.java of Project Zulu.

_PZ_BLOCK_TEX = 'mods/projectzulublock/textures/blocks'

def _pz_tex(src, dst):
    assets.append((f'{_PZ_BLOCK_TEX}/{src}.png', f'assets/{NS}/textures/block/{dst}.png'))

def _pz_model(name, obj):
    write(f'assets/{NS}/models/block/{name}.json', obj)

def _pz_state(name, variants):
    write(f'assets/{NS}/blockstates/{name}.json', {'variants': variants})

def _pz_item_def(name, model, tints=None):
    m = {'type': 'minecraft:model', 'model': model}
    if tints:
        m['tints'] = tints
    write(f'assets/{NS}/items/{name}.json', {'model': m})

def _pz_flat_item(name, texture):
    write(f'assets/{NS}/models/item/{name}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': texture}})
    _pz_item_def(name, n(f'item/{name}'))

def _pz_cross(name, texture):
    _pz_model(name, {'parent': 'minecraft:block/cross', 'textures': {'cross': n(f'block/{texture}')}})

def _pz_cube(name, texture=None):
    _pz_model(name, {'parent': 'minecraft:block/cube_all', 'textures': {'all': n(f'block/{texture or name}')}})

def _pz_block_name(b, title):
    lang[f'block.{NS}.{b}'] = title

def _pz_loot(name, pools):
    for pool in pools:  # 26.3: one 'condition' per pool
        conds = pool.pop('conditions', None)
        if conds:
            pool['condition'] = conds[0] if len(conds) == 1 else {'type': 'minecraft:all_of', 'terms': conds}
    write(f'data/{NS}/loot_table/blocks/{name}.json',
          {'type': 'minecraft:block', 'pools': pools, 'random_sequence': n(f'blocks/{name}')})

_PZ_SURVIVES = {'type': 'minecraft:survives_explosion'}

def _pz_item_entry(item, count=1, condition=None):
    e = {'type': 'minecraft:item', 'name': item}
    if count != 1:
        e['modifier'] = [{'type': 'minecraft:set_count', 'count': count}]
    if condition:
        e['condition'] = condition
    return e

def _pz_self_drop(name):
    _pz_loot(name, [{'rolls': 1, 'entries': [_pz_item_entry(n(name))], 'condition': _PZ_SURVIVES}])

def _pz_state_is(block, **props):
    return {'type': 'minecraft:match_block', 'blocks': n(block), 'state': {k: str(v).lower() for k, v in props.items()}}

# ---- shared files (minecraft tags, flammable data map): merge, other batches write into the same files ----
def _pz_merge(path, key, values):
    full = os.path.join(RES, path)
    obj = {'replace': False, 'values': []} if key == 'values' else {'values': {}}
    if os.path.isfile(full):
        with open(full) as f:
            obj = json.load(f)
    if isinstance(values, dict):
        obj.setdefault('values', {}).update(values)
    else:
        for v in values:
            if v not in obj['values']:
                obj['values'].append(v)
    write(path, obj)

def _pz_tag(registry, tag, *values):
    ns, path = tag.split(':')
    _pz_merge(f'data/{ns}/tags/{registry}/{path}.json', 'values', [n(v) for v in values])

def _pz_btag(tag, *values):
    _pz_tag('block', tag, *values)

def _pz_bitag(tag, *values):
    _pz_tag('block', tag, *values)
    _pz_tag('item', tag, *values)

def _pz_recipe(name, obj):
    write(f'data/{NS}/recipe/{name}.json', obj)

def _pz_shaped(name, pattern, key, result, count=1, category='misc'):
    _pz_recipe(name, {'type': 'minecraft:crafting_shaped', 'category': category, 'key': key, 'pattern': pattern,
                      'result': {'count': count, 'id': result}})

def _pz_shapeless(name, ingredients, result, count=1, category='misc'):
    _pz_recipe(name, {'type': 'minecraft:crafting_shapeless', 'category': category, 'ingredients': ingredients,
                      'result': {'count': count, 'id': result}})

# ================================================================= aloe vera (951) + seeds
_ALOE_TEX = ['b0', 'b1', 'b2', 'b3', 't0', 't1', 't2', 't3', 't4']
for i, t in enumerate(_ALOE_TEX):
    _pz_tex(f'aloevera_{t}', f'aloe_vera_{t}')
    _pz_cross(f'aloe_vera_age{i}', f'aloe_vera_{t}')
_pz_state('aloe_vera', {f'age={i}': {'model': n(f'block/aloe_vera_age{i}')} for i in range(9)})
_pz_flat_item('aloe_vera', n('block/aloe_vera_b0'))
_pz_block_name('aloe_vera', 'Aloe Vera Plant')
# getBlockDropped: 0-2 a seed, 3 the plant and two seeds, 7 one dandelion yellow, 8 two, 4-6 nothing
_pz_loot('aloe_vera', [
    {'rolls': 1, 'entries': [_pz_item_entry(n('aloe_vera_seeds'))], 'conditions': [
        _PZ_SURVIVES, {'type': 'minecraft:any_of', 'terms': [_pz_state_is('aloe_vera', age=a) for a in (0, 1, 2)]}]},
    {'rolls': 1, 'entries': [_pz_item_entry(n('aloe_vera'))], 'conditions': [_PZ_SURVIVES, _pz_state_is('aloe_vera', age=3)]},
    {'rolls': 1, 'entries': [_pz_item_entry(n('aloe_vera_seeds'), 2)], 'conditions': [_PZ_SURVIVES, _pz_state_is('aloe_vera', age=3)]},
    {'rolls': 1, 'entries': [_pz_item_entry('minecraft:yellow_dye')], 'conditions': [_PZ_SURVIVES, _pz_state_is('aloe_vera', age=7)]},
    {'rolls': 1, 'entries': [_pz_item_entry('minecraft:yellow_dye', 2)], 'conditions': [_PZ_SURVIVES, _pz_state_is('aloe_vera', age=8)]},
])
simple_item('aloe_vera_seeds', 'aloeveraseeds', 'Aloe Vera Seeds')

# ================================================================= watered dirt (971) + water droplets
_WD_TEX = ['d0', 'd1', 'd2', 'd3', 's0', 's1', 's2', 's3']
for i, t in enumerate(_WD_TEX):
    _pz_tex(f'watereddirt_{t}', f'watered_dirt_{t}')
    _pz_cube(f'watered_dirt_{t}')
_pz_state('watered_dirt', {f'moisture={m & 3},sandy={"true" if m >= 4 else "false"}': {'model': n(f'block/watered_dirt_{t}')}
                           for m, t in enumerate(_WD_TEX)})
_pz_item_def('watered_dirt', n('block/watered_dirt_d0'))
_pz_block_name('watered_dirt', 'Watered Dirt')
_PZ_SILK = 'minecraft:tool/can_silk_touch'
_PZ_NO_SILK = {'type': 'minecraft:inverted', 'term': _PZ_SILK}
# silk touch keeps the block (ItemWateredDirt keeps the metadata); otherwise dirt or sand, and 3 droplets when wet
_pz_loot('watered_dirt', [
    {'rolls': 1, 'conditions': [_PZ_SURVIVES], 'entries': [{'type': 'minecraft:alternatives', 'children': [
        {'type': 'minecraft:item', 'name': n('watered_dirt'), 'condition': _PZ_SILK,
         'modifier': [{'type': 'minecraft:copy_state', 'block': n('watered_dirt'), 'properties': ['moisture', 'sandy']}]},
        _pz_item_entry('minecraft:sand', condition=_pz_state_is('watered_dirt', sandy='true')),
        _pz_item_entry('minecraft:dirt')]}]},
    {'rolls': 1, 'entries': [_pz_item_entry(n('water_droplet'), 3)],
     'conditions': [_PZ_SURVIVES, _PZ_NO_SILK, _pz_state_is('watered_dirt', moisture=3)]},
])
simple_item('water_droplet', 'waterdroplet', 'Water Droplets')

# ================================================================= tumbleweed (969), quicksand (966)
for b, tex, title in (('tumbleweed', 'tumbleweed', 'Tumbleweed'), ('quicksand', 'quicksand', 'QuickSand')):
    _pz_tex(tex, b)
    _pz_cube(b)
    _pz_state(b, {'': {'model': n(f'block/{b}')}})
    _pz_item_def(b, n(f'block/{b}'))
    _pz_block_name(b, title)
    _pz_self_drop(b)
_pz_btag('minecraft:mineable/shovel', 'watered_dirt', 'quicksand')

# ================================================================= night bloom (958), creeper blossom (954)
for i in range(5):
    _pz_tex(f'nightbloom_{i}', f'night_bloom_{i}')
    _pz_cross(f'night_bloom_stage{i}', f'night_bloom_{i}')
_pz_state('night_bloom', {f'stage={i}': {'model': n(f'block/night_bloom_stage{i}')} for i in range(5)})
_pz_flat_item('night_bloom', n('block/night_bloom_0'))
_pz_block_name('night_bloom', 'NightBloom')
_pz_self_drop('night_bloom')

_pz_tex('creeperblossom', 'creeper_blossom')
_pz_cross('creeper_blossom', 'creeper_blossom')
_pz_state('creeper_blossom', {'': {'model': n('block/creeper_blossom')}})
_pz_flat_item('creeper_blossom', n('block/creeper_blossom'))
_pz_block_name('creeper_blossom', 'Creeper Blosson')  # sic, the name of the original
_pz_self_drop('creeper_blossom')
lang[f'entity.{NS}.creeper_blossom_primed'] = 'Creeper Blossom'

# ================================================================= palm tree (959-965)
_pz_tex('palmtreelog', 'palm_log')
_pz_tex('palmtreeplank', 'palm_planks')
_pz_tex('palmtreeleaves', 'palm_leaves')
_pz_tex('palmtreesapling', 'palm_sapling')
# log: one texture on all sides (BlockPalmTreeLog had no orientation)
_pz_cube('palm_log')
_pz_state('palm_log', {f'axis={a}': {'model': n('block/palm_log')} for a in ('x', 'y', 'z')})
_pz_item_def('palm_log', n('block/palm_log'))
_pz_block_name('palm_log', 'Palm Tree Log')
_pz_self_drop('palm_log')

_pz_cube('palm_planks')
_pz_state('palm_planks', {'': {'model': n('block/palm_planks')}})
_pz_item_def('palm_planks', n('block/palm_planks'))
_pz_block_name('palm_planks', 'Palm Tree Plank')
_pz_self_drop('palm_planks')

_PLANK_TEX = {'bottom': n('block/palm_planks'), 'side': n('block/palm_planks'), 'top': n('block/palm_planks')}
_pz_model('palm_slab', {'parent': 'minecraft:block/slab', 'textures': _PLANK_TEX})
_pz_model('palm_slab_top', {'parent': 'minecraft:block/slab_top', 'textures': _PLANK_TEX})
_pz_state('palm_slab', {'type=bottom': {'model': n('block/palm_slab')}, 'type=double': {'model': n('block/palm_planks')},
                        'type=top': {'model': n('block/palm_slab_top')}})
_pz_item_def('palm_slab', n('block/palm_slab'))
_pz_block_name('palm_slab', 'Palm Single Slab')
_pz_loot('palm_slab', [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n('palm_slab'), 'modifier': [
    {'type': 'minecraft:set_count', 'count': 2, 'condition': _pz_state_is('palm_slab', type='double')},
    {'type': 'minecraft:explosion_decay'}]}]}])

for suffix, parent in (('', 'stairs'), ('_inner', 'inner_stairs'), ('_outer', 'outer_stairs')):
    _pz_model(f'palm_stairs{suffix}', {'parent': f'minecraft:block/{parent}', 'textures': _PLANK_TEX})
_stairs = {}
for facing, base in (('east', 0), ('south', 90), ('west', 180), ('north', 270)):
    for half in ('bottom', 'top'):
        for shape in ('straight', 'inner_left', 'inner_right', 'outer_left', 'outer_right'):
            model = n('block/palm_stairs' + ('_inner' if shape.startswith('inner') else '_outer' if shape.startswith('outer') else ''))
            y = base
            x = 0
            if half == 'bottom' and shape.endswith('left'):
                y -= 90
            if half == 'top':
                x = 180
                if shape.endswith('right'):
                    y += 90
            y %= 360
            v = {'model': model}
            if x or y:
                v['uvlock'] = True
            if x:
                v['x'] = x
            if y:
                v['y'] = y
            _stairs[f'facing={facing},half={half},shape={shape}'] = v
_pz_state('palm_stairs', _stairs)
_pz_item_def('palm_stairs', n('block/palm_stairs'))
_pz_block_name('palm_stairs', 'Palm Tree Stairs')
_pz_self_drop('palm_stairs')

_pz_model('palm_leaves', {'parent': 'minecraft:block/leaves', 'textures': {'all': n('block/palm_leaves')}})
_pz_state('palm_leaves', {'': {'model': n('block/palm_leaves')}})
# ItemPalmTreeLeaves: ColorizerFoliage.getFoliageColorBasic (0x48b518)
_pz_item_def('palm_leaves', n('block/palm_leaves'), [{'type': 'minecraft:constant', 'value': -12012264}])
_pz_block_name('palm_leaves', 'Palm Tree Leaves')
# shears: the leaves; otherwise rand(100) <= 10 a whole coconut, else <= 30 a sapling
_PZ_SHEARS = 'minecraft:tool/can_shear'
_pz_loot('palm_leaves', [{'rolls': 1, 'entries': [{'type': 'minecraft:alternatives', 'children': [
    {'type': 'minecraft:item', 'name': n('palm_leaves'), 'condition': _PZ_SHEARS},
    _pz_item_entry(n('coconut_item'), condition={'type': 'minecraft:random_chance', 'chance': 0.11}),
    _pz_item_entry(n('palm_sapling'), condition={'type': 'minecraft:random_chance', 'chance': 0.2 / 0.89})]}]}])

_pz_cross('palm_sapling', 'palm_sapling')
_pz_state('palm_sapling', {'': {'model': n('block/palm_sapling')}})
_pz_flat_item('palm_sapling', n('block/palm_sapling'))
_pz_block_name('palm_sapling', 'Palm Tree Sapling')
_pz_self_drop('palm_sapling')

_pz_bitag('minecraft:logs', 'palm_log')  # not logs_that_burn: that tag smelts into charcoal, the original gave coal
_pz_bitag('minecraft:planks', 'palm_planks')
_pz_bitag('minecraft:wooden_slabs', 'palm_slab')
_pz_bitag('minecraft:wooden_stairs', 'palm_stairs')
_pz_bitag('minecraft:leaves', 'palm_leaves')
_pz_bitag('minecraft:saplings', 'palm_sapling')
_pz_btag('minecraft:mineable/axe', 'palm_log', 'palm_planks', 'palm_slab', 'palm_stairs')
_pz_btag('minecraft:mineable/hoe', 'palm_leaves')
# Block.setBurnProperties(id, 5, 20) of log, planks, slab and stairs (the leaves did not burn)
_pz_merge('data/neoforge/data_maps/block/flammable.json', 'map',
          {n(b): {'ignite_odds': 5, 'burn_odds': 20} for b in ('palm_log', 'palm_planks', 'palm_slab', 'palm_stairs')})
# TreeCapitator.cfg of the pack: projectzulu_palm = PalmTreeLog / PalmTreeLeaves, no other settings
write(f'data/{NS}/treecapitator/tree/projectzulu_palm.json', {'logs': [n('palm_log')], 'leaves': [n('palm_leaves')]})

# ================================================================= coconut (953) + coconut items
for i in range(3):
    _pz_tex(f'coconut_{i}', f'coconut_{i}')
    _pz_model(f'coconut_stage{i}', {'parent': f'minecraft:block/cocoa_stage{i}',
                                    'textures': {'particle': n(f'block/coconut_{i}'), 'cocoa': n(f'block/coconut_{i}')}})
_pz_state('coconut', {f'age={a},facing={f}': ({'model': n(f'block/coconut_stage{a}'), 'y': y} if y else {'model': n(f'block/coconut_stage{a}')})
                      for a in range(3) for f, y in (('east', 270), ('north', 180), ('south', 0), ('west', 90))})
_pz_block_name('coconut', 'Coconut')
_pz_loot('coconut', [{'rolls': 1, 'conditions': [_PZ_SURVIVES], 'entries': [{'type': 'minecraft:alternatives', 'children': [
    _pz_item_entry(n('coconut_item'), condition=_pz_state_is('coconut', age=2)),
    _pz_item_entry(n('coconut_seed'))]}]}])
simple_item('coconut_item', 'coconutitem', 'Whole Coconut')
simple_item('coconut_seed', 'coconutseed', 'Coconut Seed')
simple_item('coconut_shell', 'coconutshell', 'Coconut Shell')
simple_item('coconut_milk_fragment', 'coconutmilkfragment', 'Coconut Milk Fragment')

# ================================================================= universal flower pot (970)
_pz_state('universal_flower_pot', {'': {'model': 'minecraft:block/flower_pot'}})
_pz_block_name('universal_flower_pot', 'Flower Pot')
_pz_loot('universal_flower_pot', [{'rolls': 1, 'conditions': [_PZ_SURVIVES], 'entries': [_pz_item_entry('minecraft:flower_pot')]}])

# ================================================================= recipes (ItemBlockRecipeManager)
_PZ_SWORDS = ['minecraft:wooden_sword', 'minecraft:stone_sword', 'minecraft:iron_sword', 'minecraft:golden_sword', 'minecraft:diamond_sword']
_pz_recipe('coal_from_palm_log', {'type': 'minecraft:smelting', 'category': 'misc', 'cookingtime': 200, 'experience': 0.0,
                                  'ingredient': n('palm_log'), 'result': {'id': 'minecraft:coal'}})
_pz_shapeless('palm_planks', [n('palm_log')], n('palm_planks'), 4, 'building')
_pz_shaped('bowl_from_coconut_shell', ['X X', ' X '], {'X': n('coconut_shell')}, 'minecraft:bowl')
# CoconutCraftingHandler (ZuluPlants.crafted): the sword stays, milk fragments and shells on top
_pz_shaped('coconut_seed', ['X', 'Y'], {'X': _PZ_SWORDS, 'Y': n('coconut_item')}, n('coconut_seed'))
_pz_shaped('string_from_tumbleweed', ['C', 'C', 'C'], {'C': n('tumbleweed')}, 'minecraft:string', 6)
_pz_shapeless('aloe_vera_from_tumbleweed', [n('tumbleweed')], n('aloe_vera'), 2)
_pz_shapeless('string_from_raw_fiber', [n('raw_fiber')] * 4, 'minecraft:string')
# RawFiberCraftingHandler (ZuluPlants.crafted): two raw fibers on top
_pz_shapeless('leather_from_fur_pelt', [n('fur_pelt')], 'minecraft:leather')
