#!/usr/bin/env python3
"""Generates the assets/data JSON of the Natura port (mods/natura/src/main/resources) and original-assets.txt."""
import json, os, re, sys, shutil

import glob, zipfile
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = ROOT + '/src/main/resources'
# Vanilla assets/data of 26.3, unpacked once from the patched Minecraft jar (run a build of :natura first).
VAN = os.path.join(ROOT, 'build', 'vanilla-data')
if not os.path.isdir(VAN):
    jars = glob.glob(os.path.join(ROOT, 'build/moddev/artifacts/minecraft-patched-*.jar'))
    if not jars:
        sys.exit('no patched Minecraft jar: run ./gradlew :natura:compileJava first')
    with zipfile.ZipFile(jars[0]) as z:
        z.extractall(VAN, [m for m in z.namelist() if m.startswith(('assets/minecraft/', 'data/minecraft/')) and m.endswith('.json')])
NS = 'natura'

for d in ('assets', 'data'):
    shutil.rmtree(os.path.join(RES, d), ignore_errors=True)

def write(path, obj):
    full = os.path.join(RES, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, 'w') as f:
        json.dump(obj, f, indent=2)
        f.write('\n')

def van(path):
    with open(os.path.join(VAN, path)) as f:
        return json.load(f)

def n(x):
    return f'{NS}:{x}'

# ---------------------------------------------------------------- woods
# (id, english, texture prefix in the original, flammable)
WOODS = [
    ('eucalyptus', 'Eucalyptus', 'eucalyptus', True),
    ('sakura', 'Sakura', 'sakura', True),
    ('ghostwood', 'Ghostwood', 'ghostwood', False),
    ('redwood', 'Redwood', 'redwood', True),
    ('bloodwood', 'Bloodwood', 'bloodwood', False),
    ('hopseed', 'Hopseed', 'hopseed', True),
    ('maple', 'Maple', 'maple', True),
    ('silverbell', 'Silverbell', 'silverbell', True),
    ('amaranth', 'Amaranth', 'purpleheart', True),
    ('tiger', 'Tigerwood', 'tiger', True),
    ('willow', 'Willow', 'willow', True),
    ('darkwood', 'Darkwood', 'darkwood', False),
    ('fusewood', 'Fusewood', 'fusewood', False),
]
WOOD_IDS = [w[0] for w in WOODS]
TITLE = {w[0]: w[1] for w in WOODS}
TEX = {w[0]: w[2] for w in WOODS}
FLAM = {w[0]: w[3] for w in WOODS}

assets = []  # (source in jar, target in jar)
def tex_block(src, dst):
    assets.append((f'mods/natura/textures/blocks/{src}.png', f'assets/natura/textures/block/{dst}.png'))
def tex_item(src, dst):
    assets.append((f'mods/natura/textures/items/{src}.png', f'assets/natura/textures/item/{dst}.png'))

lang = {'itemGroup.natura': 'Natura'}
def name_block(b, s): lang[f'block.natura.{b}'] = s
def name_item(i, s): lang[f'item.natura.{i}'] = s
def tooltip(i, *lines):
    for k, l in enumerate(lines):
        lang[f'tooltip.natura.{i}.{k}'] = l

tags = {}  # (registry, tag id) -> list
def tag(reg, t, *values):
    tags.setdefault((reg, t), [])
    for v in values:
        if v not in tags[(reg, t)]:
            tags[(reg, t)].append(v)
def btag(t, *v): tag('block', t, *v)
def itag(t, *v): tag('item', t, *v)
def bitag(t, *v):
    btag(t, *v); itag(t, *v)

flammable = {}
def flam(block, ignite, burn): flammable[n(block)] = {'ignite_odds': ignite, 'burn_odds': burn}

# ---------------------------------------------------------------- model helpers
def model(name, obj): write(f'assets/natura/models/block/{name}.json', obj)
def item_model(name, obj): write(f'assets/natura/models/item/{name}.json', obj)
def blockstate(name, obj): write(f'assets/natura/blockstates/{name}.json', obj)
def item_def(name, model_ref, tints=None):
    m = {'type': 'minecraft:model', 'model': model_ref}
    if tints: m['tints'] = tints
    write(f'assets/natura/items/{name}.json', {'model': m})
def simple_state(name, model_ref=None):
    blockstate(name, {'variants': {'': {'model': model_ref or n(f'block/{name}')}}})
def generated_item(name, texture, layers=None):
    t = {'layer0': texture}
    if layers:
        for k, l in enumerate(layers, 1): t[f'layer{k}'] = l
    item_model(name, {'parent': 'minecraft:item/generated', 'textures': t})
    item_def(name, n(f'item/{name}'))
def cube_all(name, texture, parent='minecraft:block/cube_all'):
    model(name, {'parent': parent, 'textures': {'all': texture}})

# ---------------------------------------------------------------- loot helpers
def loot(name, pools):
    write(f'data/natura/loot_table/blocks/{name}.json', {'type': 'minecraft:block', 'pools': pools,
                                                         'random_sequence': f'natura:blocks/{name}'})
def self_drop(name, item=None):
    loot(name, [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n(item or name)}],
                 'condition': {'type': 'minecraft:survives_explosion'}}])

# ---------------------------------------------------------------- recipe helpers
recipes = {}
def ing(x):
    return x
def shaped(name, pattern, key, result, count=1, category='misc', group=None):
    r = {'type': 'minecraft:crafting_shaped', 'category': category, 'key': key, 'pattern': pattern,
         'result': {'count': count, 'id': result}}
    if group: r['group'] = group
    recipes[name] = r
def shapeless(name, ingredients, result, count=1, category='misc', group=None):
    r = {'type': 'minecraft:crafting_shapeless', 'category': category, 'ingredients': ingredients,
         'result': {'count': count, 'id': result}}
    if group: r['group'] = group
    recipes[name] = r
def smelting(name, ingredient, result, xp):
    recipes[name] = {'type': 'minecraft:smelting', 'category': 'misc', 'cookingtime': 200, 'experience': xp,
                     'ingredient': ingredient, 'result': {'id': result}}

# ================================================================= LOGS
LOGS = [  # block, side texture (orig), end texture (orig), wood
    ('eucalyptus_log', 'eucalyptus_bark', 'eucalyptus_heart', 'eucalyptus', 'Eucalyptus Wood'),
    ('sakura_log', 'sakura_bark', 'sakura_heart', 'sakura', 'Sakura Wood'),
    ('ghostwood_log', 'ghostwood_bark', 'ghostwood_heart', 'ghostwood', 'Ghostwood'),
    ('hopseed_log', 'hopseed_bark', 'hopseed_heart', 'hopseed', 'Hopseed Wood'),
    ('maple_log', 'maple_bark', 'maple_heart', 'maple', 'Maple Wood'),
    ('silverbell_log', 'silverbell_bark', 'silverbell_heart', 'silverbell', 'Silverbell Wood'),
    ('amaranth_log', 'purpleheart_bark', 'purpleheart_heart', 'amaranth', 'Amaranth Wood'),
    ('tiger_log', 'tiger_bark', 'tiger_heart', 'tiger', 'Tiger Wood'),
    ('willow_log', 'willow_bark', 'willow_heart', 'willow', 'Willow Wood'),
    ('darkwood_log', 'darkwood_bark', 'darkwood_heart', 'darkwood', 'Darkwood'),
    ('fusewood_log', 'fusewood_bark', 'fusewood_heart', 'fusewood', 'Fusewood'),
]
ALL_LOGS = []
for b, side, end, w, title in LOGS:
    tex_block(side, b); tex_block(end, b + '_top')
    model(b, {'parent': 'minecraft:block/cube_column', 'textures': {'end': n(f'block/{b}_top'), 'side': n(f'block/{b}')}})
    model(b + '_horizontal', {'parent': 'minecraft:block/cube_column_horizontal',
                              'textures': {'end': n(f'block/{b}_top'), 'side': n(f'block/{b}')}})
    blockstate(b, {'variants': {'axis=x': {'model': n(f'block/{b}_horizontal'), 'x': 90, 'y': 90},
                                'axis=y': {'model': n(f'block/{b}')},
                                'axis=z': {'model': n(f'block/{b}_horizontal'), 'x': 90}}})
    item_def(b, n(f'block/{b}'))
    name_block(b, title)
    self_drop(b)
    ALL_LOGS.append(b)
    if FLAM[w]: flam(b, 5, 20)
# redwood: bark, heart, root (one texture on all sides)
for b, title in (('redwood_bark', 'Redwood Bark'), ('redwood_heart', 'Redwood'), ('redwood_root', 'Redwood Root')):
    tex_block(b, b)
    cube_all(b, n(f'block/{b}'))
    blockstate(b, {'variants': {f'axis={a}': {'model': n(f'block/{b}')} for a in 'xyz'}})
    item_def(b, n(f'block/{b}'))
    name_block(b, title); self_drop(b); ALL_LOGS.append(b); flam(b, 5, 20)
# bloodwood quarters
for t in ('bark', 'upper_left', 'upper_right', 'lower_left', 'lower_right', 'side_left', 'side_right'):
    tex_block(f'bloodwood_{t}', f'bloodwood_{t}')
B = lambda t: n(f'block/bloodwood_{t}')
quarters = {  # LogTwoxTwo.getIcon for metadata 0-3 (sides 0 down, 1 up, 2 north, 3 south, 4 west, 5 east)
    'nw': ('upper_left', 'bark', 'side_left', 'bark', 'side_right'),
    'ne': ('upper_right', 'bark', 'side_right', 'side_left', 'bark'),
    'sw': ('lower_left', 'side_right', 'bark', 'bark', 'side_left'),
    'se': ('lower_right', 'side_right', 'bark', 'side_left', 'bark'),
}
variants = {}
for q, (top, north, south, west, east) in quarters.items():
    model(f'bloodwood_log_{q}', {'parent': 'minecraft:block/cube', 'textures': {
        'particle': B('bark'), 'up': B(top), 'down': B(top), 'north': B(north), 'south': B(south), 'west': B(west), 'east': B(east)}})
    variants[f'axis=y,bark={q}'] = {'model': n(f'block/bloodwood_log_{q}')}
    variants[f'axis=x,bark={q}'] = {'model': n(f'block/bloodwood_log_{q}'), 'x': 90, 'y': 90}
    variants[f'axis=z,bark={q}'] = {'model': n(f'block/bloodwood_log_{q}'), 'x': 90}
blockstate('bloodwood_log', {'variants': variants})
item_def('bloodwood_log', n('block/bloodwood_log_nw'))
cube_all('bloodwood_bark', B('bark')); simple_state('bloodwood_bark'); item_def('bloodwood_bark', n('block/bloodwood_bark'))
name_block('bloodwood_log', 'Bloodwood'); name_block('bloodwood_bark', 'Bloodwood Bark')
self_drop('bloodwood_log'); self_drop('bloodwood_bark')
ALL_LOGS += ['bloodwood_log', 'bloodwood_bark']
bitag('minecraft:logs', *[n(b) for b in ALL_LOGS])
burning_logs = [n(b) for b in ALL_LOGS if b not in ('ghostwood_log', 'darkwood_log', 'fusewood_log', 'bloodwood_log', 'bloodwood_bark')]
bitag('minecraft:logs_that_burn', *burning_logs)
btag('minecraft:mineable/axe', *[n(b) for b in ALL_LOGS])
tooltip('fusewood_log', 'Explosive personality')
tooltip('bloodwood_log', 'Lava-resistant')

# ================================================================= PLANKS & building blocks
def van_copy_state(kind, wood, prefix_tex):
    """Copies the oak blockstate/models of a kind (stairs, fence, ...) for a Natura wood."""
    state = van(f'assets/minecraft/blockstates/oak_{kind}.json')
    text = json.dumps(state).replace('minecraft:block/oak_', f'natura:block/{wood}_')
    blockstate(f'{wood}_{kind}', json.loads(text))
    names = set(re.findall(r'natura:block/([a-z0-9_]+)', text))
    for name in names:
        vm = van(f'assets/minecraft/models/block/{name.replace(wood + "_", "oak_", 1)}.json')
        t = json.dumps(vm)
        t = t.replace('minecraft:block/oak_planks', n(f'block/{prefix_tex}_planks'))
        t = t.replace('minecraft:block/oak_door_', n(f'block/{prefix_tex}_door_'))
        t = t.replace('minecraft:block/oak_trapdoor', n(f'block/{prefix_tex}_trapdoor'))
        m = json.loads(t)
        if 'parent' in m and ':' not in m['parent']:
            m['parent'] = 'minecraft:' + m['parent']
        model(name, m)
    return names

for w in WOOD_IDS:
    tp = TEX[w]; T = TITLE[w]
    planks = f'{w}_planks'
    tex_block(f'{tp}_planks', planks)
    cube_all(planks, n(f'block/{planks}')); simple_state(planks); item_def(planks, n(f'block/{planks}'))
    name_block(planks, f'{T} Planks'); self_drop(planks)
    bitag('minecraft:planks', n(planks)); btag('minecraft:mineable/axe', n(planks))
    if FLAM[w]: flam(planks, 5, 20)
    # stairs
    van_copy_state('stairs', w, w); item_def(f'{w}_stairs', n(f'block/{w}_stairs'))
    name_block(f'{w}_stairs', f'{"Blood" if w == "bloodwood" else T} Stairs'); self_drop(f'{w}_stairs')
    bitag('minecraft:wooden_stairs', n(f'{w}_stairs'))
    shaped(f'{w}_stairs', ['#  ', '## ', '###'], {'#': n(planks)}, n(f'{w}_stairs'), 4, 'building', 'wooden_stairs')
    # slab
    model(f'{w}_slab', {'parent': 'minecraft:block/slab', 'textures': {'bottom': n(f'block/{planks}'), 'side': n(f'block/{planks}'), 'top': n(f'block/{planks}')}})
    model(f'{w}_slab_top', {'parent': 'minecraft:block/slab_top', 'textures': {'bottom': n(f'block/{planks}'), 'side': n(f'block/{planks}'), 'top': n(f'block/{planks}')}})
    blockstate(f'{w}_slab', {'variants': {'type=bottom': {'model': n(f'block/{w}_slab')}, 'type=double': {'model': n(f'block/{planks}')},
                                         'type=top': {'model': n(f'block/{w}_slab_top')}}})
    item_def(f'{w}_slab', n(f'block/{w}_slab'))
    name_block(f'{w}_slab', f'{T} Slab')
    loot(f'{w}_slab', [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n(f'{w}_slab'), 'modifier': [
        {'type': 'minecraft:set_count', 'count': 2, 'condition': {'type': 'minecraft:match_block', 'blocks': n(f'{w}_slab'), 'state': {'type': 'double'}}},
        {'type': 'minecraft:explosion_decay'}]}]}])
    bitag('minecraft:wooden_slabs', n(f'{w}_slab'))
    shaped(f'{w}_slab', ['bbb'], {'b': n(planks)}, n(f'{w}_slab'), 6, 'building', 'wooden_slab')
    # fence (AlternateFence: 6 sticks of the wood -> 2 fences)
    van_copy_state('fence', w, w); item_def(f'{w}_fence', n(f'block/{w}_fence_inventory'))
    model(f'{w}_fence_inventory', {'parent': 'minecraft:block/fence_inventory', 'textures': {'texture': n(f'block/{planks}')}})
    name_block(f'{w}_fence', f'{T} Fence'); self_drop(f'{w}_fence')
    bitag('minecraft:wooden_fences', n(f'{w}_fence')); bitag('c:fences/wooden', n(f'{w}_fence'))
    shaped(f'{w}_fence', ['###', '###'], {'#': n(f'{w}_stick')}, n(f'{w}_fence'), 2, 'misc', 'wooden_fence')
    # fence gate
    van_copy_state('fence_gate', w, w); item_def(f'{w}_fence_gate', n(f'block/{w}_fence_gate'))
    name_block(f'{w}_fence_gate', f'{"Blood" if w == "bloodwood" else T} Fence Gate'); self_drop(f'{w}_fence_gate')
    bitag('minecraft:fence_gates', n(f'{w}_fence_gate')); bitag('c:fence_gates/wooden', n(f'{w}_fence_gate'))
    shaped(f'{w}_fence_gate', ['s#s', 's#s'], {'#': n(planks), 's': n(f'{w}_stick')}, n(f'{w}_fence_gate'), 1, 'redstone', 'wooden_fence_gate')
    # pressure plate
    van_copy_state('pressure_plate', w, w); item_def(f'{w}_pressure_plate', n(f'block/{w}_pressure_plate'))
    name_block(f'{w}_pressure_plate', f'{"Blood" if w == "bloodwood" else T} Pressure Plate'); self_drop(f'{w}_pressure_plate')
    bitag('minecraft:wooden_pressure_plates', n(f'{w}_pressure_plate'))
    shaped(f'{w}_pressure_plate', ['##'], {'#': n(planks)}, n(f'{w}_pressure_plate'), 1, 'redstone', 'wooden_pressure_plate')
    # button
    van_copy_state('button', w, w)
    model(f'{w}_button_inventory', {'parent': 'minecraft:block/button_inventory', 'textures': {'texture': n(f'block/{planks}')}})
    item_def(f'{w}_button', n(f'block/{w}_button_inventory'))
    name_block(f'{w}_button', f'{"Blood" if w == "bloodwood" else T} Button'); self_drop(f'{w}_button')
    bitag('minecraft:wooden_buttons', n(f'{w}_button'))
    shaped(f'{w}_button', ['#'], {'#': n(planks)}, n(f'{w}_button'), 1, 'redstone', 'wooden_button')
    # trapdoor (maple, silverbell, amaranth, tiger, willow, fusewood: the "window" textures of the jar)
    tex_block(f'{tp}_trapdoor' if w in ('eucalyptus', 'sakura', 'ghostwood', 'redwood', 'bloodwood', 'hopseed', 'darkwood') else f'{tp}_window', f'{w}_trapdoor')
    van_copy_state('trapdoor', w, w); item_def(f'{w}_trapdoor', n(f'block/{w}_trapdoor_bottom'))
    name_block(f'{w}_trapdoor', f'{"Blood" if w == "bloodwood" else T} Trapdoor'); self_drop(f'{w}_trapdoor')
    bitag('minecraft:wooden_trapdoors', n(f'{w}_trapdoor'))
    shaped(f'{w}_trapdoor', ['###', '###'], {'#': n(planks)}, n(f'{w}_trapdoor'), 2, 'redstone', 'wooden_trapdoor')
    # workbench
    for part, orig in (('top', 'top'), ('side', 'side'), ('front', 'face')):
        tex_block(f'{tp}_workbench_{orig}', f'{w}_workbench_{part}')
    wb = f'{w}_workbench'
    model(wb, {'parent': 'minecraft:block/cube', 'textures': {
        'down': n(f'block/{planks}'), 'up': n(f'block/{wb}_top'), 'north': n(f'block/{wb}_front'), 'west': n(f'block/{wb}_front'),
        'south': n(f'block/{wb}_side'), 'east': n(f'block/{wb}_side'), 'particle': n(f'block/{wb}_front')}})
    simple_state(wb); item_def(wb, n(f'block/{wb}'))
    name_block(wb, f'{T} Crafting Table'); self_drop(wb)
    bitag('c:player_workstations/crafting_tables', n(wb))
    shaped(wb, ['##', '##'], {'#': n(planks)}, n(wb), 1, 'misc', 'crafting_table')
    # bookshelf: 3 books
    bs = f'{w}_bookshelf'
    tex_block(f'{tp}_bookshelf', bs)
    model(bs, {'parent': 'minecraft:block/cube_column', 'textures': {'end': n(f'block/{planks}'), 'side': n(f'block/{bs}')}})
    simple_state(bs); item_def(bs, n(f'block/{bs}'))
    name_block(bs, f'{T} Bookshelf')
    loot(bs, [{'rolls': 1, 'entries': [{'type': 'minecraft:alternatives', 'children': [
        {'type': 'minecraft:item', 'condition': 'minecraft:tool/can_silk_touch', 'name': n(bs)},
        {'type': 'minecraft:item', 'name': 'minecraft:book', 'modifier': [{'type': 'minecraft:set_count', 'count': 3}, {'type': 'minecraft:explosion_decay'}]}]}]}])
    btag('minecraft:enchantment_power_provider', n(bs)); bitag('c:bookshelves', n(bs))
    shaped(bs, ['###', 'bbb', '###'], {'#': n(planks), 'b': 'minecraft:book'}, n(bs), 1, 'building')
    # stick
    tex_item(f'{tp}_stick', f'{w}_stick'); generated_item(f'{w}_stick', n(f'item/{w}_stick'))
    name_item(f'{w}_stick', f'{T} Stick')
    itag('natura:sticks', n(f'{w}_stick')); itag('c:rods/wooden', n(f'{w}_stick'))
    shaped(f'{w}_stick', ['#', '#'], {'#': n(planks)}, n(f'{w}_stick'), 4, 'misc', 'sticks')
    btag('minecraft:mineable/axe', *[n(f'{w}_{k}') for k in ('stairs', 'slab', 'fence', 'fence_gate', 'pressure_plate', 'button', 'trapdoor', 'workbench', 'bookshelf')])

# planks from logs
PLANK_SOURCES = {
    'eucalyptus': ['eucalyptus_log'], 'sakura': ['sakura_log'], 'ghostwood': ['ghostwood_log'], 'hopseed': ['hopseed_log'],
    'redwood': ['redwood_heart'], 'bloodwood': ['bloodwood_log', 'bloodwood_bark'], 'maple': ['maple_log'],
    'silverbell': ['silverbell_log'], 'amaranth': ['amaranth_log'], 'tiger': ['tiger_log'], 'willow': ['willow_log'],
    'darkwood': ['darkwood_log'], 'fusewood': ['fusewood_log']}
for w, srcs in PLANK_SOURCES.items():
    ingr = n(srcs[0]) if len(srcs) == 1 else [n(s) for s in srcs]
    shapeless(f'{w}_planks', [ingr], n(f'{w}_planks'), 4, 'building', 'planks')
smelting('charcoal_from_ghostwood_log', n('ghostwood_log'), 'minecraft:charcoal', 0.15)

# doors (7)
DOORS = [('redwood', 'redwood', 'Redwood Door'), ('eucalyptus', 'eucalyptus', 'Eucalyptus Door'), ('hopseed', 'hopseed', 'Hopseed Door'),
         ('sakura', 'sakura', 'Sakura Door'), ('ghostwood', 'ghostwood', 'Ghostwood Door'), ('bloodwood', 'bloodwood', 'Bloodwood Door'),
         ('redwood_bark', 'redwoodbark', 'Redwood Bark Door')]
DOOR_PLANKS = {'redwood': n('redwood_planks'), 'eucalyptus': n('eucalyptus_planks'), 'hopseed': n('hopseed_planks'), 'sakura': n('sakura_planks'),
               'ghostwood': n('ghostwood_planks'), 'bloodwood': n('bloodwood_planks'), 'redwood_bark': n('redwood_bark')}
for d, orig, title in DOORS:
    tex_block(f'{orig}_door_top', f'{d}_door_top'); tex_block(f'{orig}_door_bottom', f'{d}_door_bottom')
    tex_item(f'{orig}_door_item', f'{d}_door')
    van_copy_state('door', d, d)
    generated_item(f'{d}_door', n(f'item/{d}_door'))
    name_block(f'{d}_door', title)
    loot(f'{d}_door', [{'rolls': 1, 'condition': {'type': 'minecraft:survives_explosion'}, 'entries': [{'type': 'minecraft:item', 'name': n(f'{d}_door'),
        'condition': {'type': 'minecraft:match_block', 'blocks': n(f'{d}_door'), 'state': {'half': 'lower'}}}]}])
    bitag('minecraft:wooden_doors', n(f'{d}_door')); btag('minecraft:mineable/axe', n(f'{d}_door'))
    shaped(f'{d}_door', ['##', '##', '##'], {'#': DOOR_PLANKS[d]}, n(f'{d}_door'), 1, 'redstone', 'wooden_door')

# ================================================================= LEAVES
LEAVES = [  # block, orig texture, title, sapling, tints (item), flammable, group
    ('redwood_leaves', 'redwood', 'Redwood Leaves', 'redwood_sapling', 'foliage', True),
    ('eucalyptus_leaves', 'eucalyptus', 'Eucalyptus Leaves', 'eucalyptus_sapling', 'foliage', True),
    ('hopseed_leaves', 'hopseed', 'Hopseed Leaves', 'hopseed_sapling', 'foliage', True),
    ('sakura_leaves', 'sakura', 'Sakura Leaves', 'sakura_sapling', None, True),
    ('ghostwood_leaves', 'ghostwood', 'Ghostwood Leaves', 'ghostwood_sapling', None, False),
    ('bloodwood_leaves', 'bloodwood', 'Bloodleaves', 'bloodwood_sapling', None, False),
    ('willow_leaves', 'willow', 'Willow Leaves', 'willow_sapling', None, False),
    ('maple_leaves', 'maple', 'Maple Leaves', 'maple_sapling', 0xFFCC5818, True),
    ('silverbell_leaves', 'silverbell', 'Silverbell Leaves', 'silverbell_sapling', None, True),
    ('amaranth_leaves', 'purpleheart', 'Amaranth Leaves', 'amaranth_sapling', 0xFF6AD73A, True),
    ('tiger_leaves', 'tiger', 'Tiger Leaves', 'tiger_sapling', None, True),
    ('darkwood_leaves', 'darkwood', 'Darkwood Leaves', 'darkwood_sapling', None, False),
    ('flowering_darkwood_leaves', 'darkwood_flowering', 'Darkwood Leaves', 'darkwood_sapling', None, False),
    ('fruit_darkwood_leaves', 'darkwood_fruit', 'Darkwood Leaves', None, None, False),
    ('fusewood_leaves', 'fusewood', 'Fusewood Leaves', 'fusewood_sapling', None, False),
]
SHEARS = {'type': 'minecraft:any_of', 'terms': ['minecraft:tool/can_shear', 'minecraft:tool/can_silk_touch',
                                                {'type': 'minecraft:match_tool', 'predicate': {'items': '#natura:kamas'}}]}
for b, orig, title, sapling, tint, fl in LEAVES:
    tex_block(f'{orig}_leaves_fancy', b)
    cube_all(b, n(f'block/{b}'), 'minecraft:block/leaves'); simple_state(b)
    tints = None
    if tint == 'foliage': tints = [{'type': 'minecraft:constant', 'value': -12012264}]
    elif isinstance(tint, int): tints = [{'type': 'minecraft:constant', 'value': tint - (1 << 32)}]
    item_def(b, n(f'block/{b}'), tints)
    name_block(b, title)
    if sapling:  # NLeaves: 1/20, no fortune
        drop = {'type': 'minecraft:item', 'name': n(sapling), 'condition': {'type': 'minecraft:all_of', 'terms': [
            {'type': 'minecraft:survives_explosion'}, {'type': 'minecraft:random_chance', 'chance': 0.05}]}}
    else:  # NLeavesDark metadata 2: always one potash apple
        drop = {'type': 'minecraft:item', 'name': n('potash_apple'), 'condition': {'type': 'minecraft:survives_explosion'}}
    loot(b, [{'rolls': 1, 'entries': [{'type': 'minecraft:alternatives', 'children': [
        {'type': 'minecraft:item', 'condition': SHEARS, 'name': n(b)}, drop]}]}])
    bitag('minecraft:leaves', n(b)); btag('minecraft:mineable/hoe', n(b))
    if fl: flam(b, 30, 60)
tooltip('fruit_darkwood_leaves', 'Produces chalky apples')

# ================================================================= SAPLINGS
SAPLINGS = [  # block, texture, title, tooltip
    ('redwood_sapling', 'redwood', 'Redwood Sapling', ['Plant these in a 7x7', 'It takes days to grow']),
    ('eucalyptus_sapling', 'eucalyptus', 'Eucalyptus Sapling', ['The pink wood']),
    ('hopseed_sapling', 'hopseed', 'Hopseed Sapling', ['Ascended Glitch']),
    ('sakura_sapling', 'sakura', 'Sakura Sapling', ['Flowering Cherry']),
    ('ghostwood_sapling', 'ghostwood', 'Ghostwood Sapling', ['Pale as a ghost']),
    ('bloodwood_sapling', 'bloodwood', 'Blood Sapling', ['Lava-resistant', 'Grows on the ceiling']),
    ('darkwood_sapling', 'darkwood', 'Darkwood Sapling', ['Produces chalky apples']),
    ('fusewood_sapling', 'fusewood', 'Fusewood Sapling', ['Explosive personality']),
    ('maple_sapling', 'maple', 'Maple Sapling', []),
    ('silverbell_sapling', 'silverbell', 'Silverbell Sapling', []),
    ('amaranth_sapling', 'purpleheart', 'Amaranth Sapling', []),
    ('tiger_sapling', 'tiger', 'Tigerwood Sapling', []),
    ('willow_sapling', 'willow', 'Willow Sapling', []),
]
for b, orig, title, tips in SAPLINGS:
    tex_block(f'{orig}_sapling', b)
    model(b, {'parent': 'minecraft:block/cross', 'textures': {'cross': n(f'block/{b}')}})
    simple_state(b)
    item_model(b, {'parent': 'minecraft:item/generated', 'textures': {'layer0': n(f'block/{b}')}})
    item_def(b, n(f'item/{b}'))
    name_block(b, title); tooltip(b, *tips); self_drop(b)
    bitag('minecraft:saplings', n(b))

# ================================================================= CLOUDS
for b, orig, title, tips in (('white_cloud', 'cloud_white', 'Cloud', ['So light and fluffy!', 'Saves you when you fall']),
                             ('dark_cloud', 'cloud_gray', 'Dark Cloud', ['So light and fluffy!', 'Causes lightning strikes']),
                             ('ash_cloud', 'cloud_dark', 'Ash Cloud', ['So light and fluffy!', '3x3 Converts into charcoal']),
                             ('sulfur_cloud', 'cloud_sulfur', 'Sulfur Cloud', ['So light and fluffy!', '2x2 Converts into sulfur'])):
    tex_block(orig, b); cube_all(b, n(f'block/{b}')); simple_state(b); item_def(b, n(f'block/{b}'))
    name_block(b, title); tooltip(b, *tips)
    loot(b, [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n(b)}]}])
shaped('charcoal_from_ash_cloud', ['ccc', 'ccc', 'ccc'], {'c': n('ash_cloud')}, 'minecraft:charcoal')
shaped('sulfur_from_sulfur_cloud', ['cc', 'cc'], {'c': n('sulfur_cloud')}, n('sulfur'))
shaped('gunpowder_from_sulfur', ['cc', 'cc'], {'c': n('sulfur')}, 'minecraft:gunpowder')

# ================================================================= SAGUARO
for t in ('bottom', 'top', 'side', 'fruit'):
    tex_block(f'saguaro_{t}', f'saguaro_{t}')
S = lambda t: n(f'block/saguaro_{t}')
def box(frm, to, tex_all=None, faces=None):
    f = {}
    for d in ('down', 'up', 'north', 'south', 'west', 'east'):
        f[d] = {'texture': (faces or {}).get(d, tex_all)}
    return {'from': frm, 'to': to, 'faces': f}
SAG_FACES = {'down': '#bottom', 'up': '#top', 'north': '#side', 'south': '#side', 'west': '#side', 'east': '#side'}
for name, y0, y1 in (('saguaro_core', 0, 14), ('saguaro_core_up', 0, 16), ('saguaro_core_lifted', 2, 14), ('saguaro_core_lifted_up', 2, 16)):
    model(name, {'parent': 'minecraft:block/block', 'textures': {'particle': S('side'), 'bottom': S('bottom'), 'top': S('top'), 'side': S('side')},
                 'elements': [box([2, y0, 2], [14, y1, 14], faces=SAG_FACES)]})
model('saguaro_arm', {'parent': 'minecraft:block/block', 'textures': {'particle': S('side'), 'bottom': S('bottom'), 'top': S('top'), 'side': S('side')},
                      'elements': [box([2, 2, 0], [14, 14, 2], faces=SAG_FACES)]})
mp = []
for down, up, m in (('true', 'false', 'saguaro_core'), ('true', 'true', 'saguaro_core_up'), ('false', 'false', 'saguaro_core_lifted'), ('false', 'true', 'saguaro_core_lifted_up')):
    mp.append({'when': {'down': down, 'up': up}, 'apply': {'model': n(f'block/{m}')}})
for d, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
    a = {'model': n('block/saguaro_arm')}
    if y: a['y'] = y
    mp.append({'when': {d: 'true'}, 'apply': a})
blockstate('saguaro', {'multipart': mp})
item_def('saguaro', n('block/saguaro_core'))
name_block('saguaro', 'Saguaro Cactus'); tooltip('saguaro', 'Grows like a tree', 'Gives fruit after a rainstorm')
self_drop('saguaro')
model('saguaro_sprout', {'parent': 'minecraft:block/block', 'textures': {'particle': S('top'), 'all': S('top')},
                         'elements': [box([5.2, 0, 5.2], [10.8, 8, 10.8], '#all')]})
blockstate('saguaro_sprout', {'variants': {f'age={a}': {'model': n('block/saguaro_sprout')} for a in (0, 1)}})
name_block('saguaro_sprout', 'Saguaro Cactus')
loot('saguaro_sprout', [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n('saguaro_fruit')}]}])
model('saguaro_fruit', {'parent': 'minecraft:block/block', 'textures': {'particle': S('fruit'), 'all': S('fruit')},
                        'elements': [box([10, 3, 4], [16, 12, 12], '#all')]})
blockstate('saguaro_fruit', {'variants': {
    'facing=east': {'model': n('block/saguaro_fruit')}, 'facing=south': {'model': n('block/saguaro_fruit'), 'y': 90},
    'facing=west': {'model': n('block/saguaro_fruit'), 'y': 180}, 'facing=north': {'model': n('block/saguaro_fruit'), 'y': 270}}})
name_block('saguaro_fruit', 'Saguaro Fruit')
loot('saguaro_fruit', [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n('saguaro_fruit')}]}])
smelting('green_dye_from_saguaro', n('saguaro'), 'minecraft:green_dye', 0.2)

# ================================================================= BERRY BUSHES
BUSHES = [  # block, texture, title, berry, tooltip
    ('raspberry_bush', 'raspberry', 'Raspberry Bush', 'raspberry', ['Sweet and red', 'Found in warm areas']),
    ('blueberry_bush', 'blueberry', 'Blueberry Bush', 'blueberry', ['Tart and blue', 'Found in temperate areas']),
    ('blackberry_bush', 'blackberry', 'Blackberry Bush', 'blackberry', ['Sweet and black', 'Found in wet areas']),
    ('maloberry_bush', 'geoberry', 'Maloberry Bush', 'maloberry', ['Tasty and yellow-orange', 'Found in cold areas']),
    ('blightberry_bush', 'blightberry', 'Blightberry Bush', 'blightberry', ['A little bit evil', 'Killer healing']),
    ('duskberry_bush', 'duskberry', 'Duskberry Bush', 'duskberry', ['A little bit evil', 'Visible night']),
    ('skyberry_bush', 'skyberry', 'Skyberry Bush', 'skyberry', ['A little bit evil', 'Slow dive']),
    ('stingberry_bush', 'stingberry', 'Stingberry Bush', 'stingberry', ['A little bit evil', 'Hit like a truck']),
]
for b, orig, title, berry, tips in BUSHES:
    tex_block(f'{orig}_fancy', b); tex_block(f'{orig}_ripe_fancy', b + '_ripe')
    for suffix, lo, hi, h, tex in (('_small', 4, 12, 8, b), ('_medium', 2, 14, 12, b)):
        model(b + suffix, {'parent': 'minecraft:block/block', 'textures': {'particle': n(f'block/{tex}'), 'all': n(f'block/{tex}')},
                           'elements': [box([lo, 0, lo], [hi, h, hi], '#all')]})
    cube_all(b, n(f'block/{b}')); cube_all(b + '_ripe', n(f'block/{b}_ripe'))
    blockstate(b, {'variants': {'age=0': {'model': n(f'block/{b}_small')}, 'age=1': {'model': n(f'block/{b}_medium')},
                                'age=2': {'model': n(f'block/{b}')}, 'age=3': {'model': n(f'block/{b}_ripe')}}})
    item_def(b, n(f'block/{b}_ripe'))
    name_block(b, title); tooltip(b, *tips)
    loot(b, [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n(b)}], 'condition': {'type': 'minecraft:survives_explosion'}}])
    btag('minecraft:mineable/hoe', n(b))
    if not b.startswith(('blight', 'dusk', 'sky', 'sting')):
        flam(b, 4, 25)

# berries
BERRIES = [('raspberry', 'berry_rasp', 'Raspberry'), ('blueberry', 'berry_blue', 'Blueberry'), ('blackberry', 'berry_black', 'Blackberry'),
           ('maloberry', 'berry_geo', 'Maloberry')]
NBERRIES = [('blightberry', 'berry_blight', 'Blightberry', 'Killer healing'), ('duskberry', 'berry_dusk', 'Duskberry', 'Visible night'),
            ('skyberry', 'berry_sky', 'Skyberry', 'Slow dive'), ('stingberry', 'berry_sting', 'Stingberry', 'Hit like a truck')]
for i, orig, title in BERRIES:
    tex_item(orig, i); generated_item(i, n(f'item/{i}')); name_item(i, title); tooltip(i, 'Saturating!')
    itag('c:foods/berry', n(i)); itag('c:foods', n(i)); itag(f'c:crops/{i}', n(i))
for i, orig, title, tip in NBERRIES:
    tex_item(orig, i); generated_item(i, n(f'item/{i}')); name_item(i, title); tooltip(i, 'A little bit evil', tip)
    itag('c:foods/berry', n(i)); itag('c:foods', n(i)); itag(f'c:crops/{i}', n(i))
tex_item('berry_medley', 'berry_medley'); generated_item('berry_medley', n('item/berry_medley')); name_item('berry_medley', 'Berry Medley')
tooltip('berry_medley', 'An irresistable combination of berries')
import itertools
bl = [n(b[0]) for b in BERRIES]
for k, combo in enumerate(itertools.combinations(bl, 3)):
    shapeless(f'berry_medley_{k}', ['minecraft:bowl'] + list(combo), n('berry_medley'), 1, 'food', 'berry_medley')
shapeless('berry_medley_double', ['minecraft:bowl', 'minecraft:bowl'] + bl, n('berry_medley'), 2, 'food', 'berry_medley')

# ================================================================= CROPS
for a in range(4):
    tex_block(f'barley_{a + 1}', f'barley_stage{a}')
    model(f'barley_stage{a}', {'parent': 'minecraft:block/crop', 'textures': {'crop': n(f'block/barley_stage{a}')}})
blockstate('barley_crop', {'variants': {f'age={a}': {'model': n(f'block/barley_stage{a}')} for a in range(4)}})
for a in range(5):
    tex_block(f'cotton_{a + 1}', f'cotton_stage{a}')
    model(f'cotton_stage{a}', {'parent': 'minecraft:block/crop', 'textures': {'crop': n(f'block/cotton_stage{a}')}})
blockstate('cotton_crop', {'variants': {f'age={a}': {'model': n(f'block/cotton_stage{a}')} for a in range(5)}})
name_block('barley_crop', 'Barley'); name_block('cotton_crop', 'Cotton')
def age(block, a): return {'type': 'minecraft:match_block', 'blocks': n(block), 'state': {'age': str(a)}}
def ages(block, lo, hi): return {'type': 'minecraft:any_of', 'terms': [age(block, a) for a in range(lo, hi + 1)]}
def chance(c): return {'type': 'minecraft:random_chance', 'chance': c}
def pool(item, *conds):
    p = {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n(item)}]}
    if conds: p['condition'] = conds[0] if len(conds) == 1 else {'type': 'minecraft:all_of', 'terms': list(conds)}
    return p
# CropBlock.getBlockDropped: barley 1 seed (+1/3 from stage 2, +1/4 at stage 3) and the barley when ripe
write('data/natura/loot_table/blocks/barley_crop.json', {'type': 'minecraft:block', 'modifier': {'type': 'minecraft:explosion_decay'}, 'pools': [
    pool('barley_seeds'), pool('barley_seeds', ages('barley_crop', 2, 3), chance(1 / 3)), pool('barley_seeds', age('barley_crop', 3), chance(0.25)),
    pool('barley', age('barley_crop', 3))], 'random_sequence': 'natura:blocks/barley_crop'})
# cotton: 1 seed (+1/2 from stage 1, +1/2 from stage 3) and the cotton when ripe
write('data/natura/loot_table/blocks/cotton_crop.json', {'type': 'minecraft:block', 'modifier': {'type': 'minecraft:explosion_decay'}, 'pools': [
    pool('cotton_seeds'), pool('cotton_seeds', ages('cotton_crop', 1, 4), chance(0.5)), pool('cotton_seeds', ages('cotton_crop', 3, 4), chance(0.5)),
    pool('cotton', age('cotton_crop', 4))], 'random_sequence': 'natura:blocks/cotton_crop'})
btag('minecraft:crops', n('barley_crop'), n('cotton_crop')); btag('minecraft:bee_growables', n('barley_crop'), n('cotton_crop'))

# ================================================================= PLANTS
tex_block('flower_bluebells', 'bluebells')
model('bluebells', {'parent': 'minecraft:block/cross', 'textures': {'cross': n('block/bluebells')}}); simple_state('bluebells')
item_model('bluebells', {'parent': 'minecraft:item/generated', 'textures': {'layer0': n('block/bluebells')}}); item_def('bluebells', n('item/bluebells'))
name_block('bluebells', 'Bluebells'); self_drop('bluebells'); bitag('minecraft:small_flowers', n('bluebells'))
shapeless('blue_dye_from_bluebells', [n('bluebells')], n('blue_dye'), 2, 'misc', 'blue_dye')

tex_block('thornvine', 'thornvines')
vs = json.dumps(van('assets/minecraft/blockstates/vine.json')).replace('minecraft:block/vine', 'natura:block/thornvines')
blockstate('thornvines', json.loads(vs))
for name in set(re.findall(r'natura:block/([a-z0-9_]+)', vs)):
    vm = van(f'assets/minecraft/models/block/{name.replace("thornvines", "vine")}.json')
    t = json.dumps(vm).replace('"block/vine"', f'"{n("block/thornvines")}"').replace('minecraft:block/vine', n('block/thornvines'))
    model(name, json.loads(t))
item_model('thornvines', {'parent': 'minecraft:item/generated', 'textures': {'layer0': n('block/thornvines')}}); item_def('thornvines', n('item/thornvines'))
name_block('thornvines', 'Thornvines')
loot('thornvines', [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n('thornvines'), 'condition': 'minecraft:tool/can_shear'}]}])
btag('minecraft:climbable', n('thornvines')); btag('minecraft:mineable/axe', n('thornvines'))

for color, title in (('green', 'Green'), ('purple', 'Purple'), ('blue', 'Blue')):
    b = f'{color}_glowshroom'
    tex_block(f'mushroom_{color}', b)
    model(b, {'parent': 'minecraft:block/cross', 'textures': {'cross': n(f'block/{b}')}}); simple_state(b)
    item_model(b, {'parent': 'minecraft:item/generated', 'textures': {'layer0': n(f'block/{b}')}}); item_def(b, n(f'item/{b}'))
    name_block(b, f'{title} Glowshroom'); tooltip(b, 'Neon juice'); self_drop(b)
    for part in ('skin', 'inside', 'stem'):
        tex_block(f'mushroom_{part}_{color}', f'{b}_{part}')
        model(f'{b}_{part}', {'parent': 'minecraft:block/template_single_face', 'textures': {'texture': n(f'block/{b}_{part}')}})
    for big, face, btitle in ((f'{b}_block', 'skin', f'{title} Glowshroom'), (f'{b}_stem', 'stem', f'{title} Glowshroom')):
        vs = van('assets/minecraft/blockstates/brown_mushroom_block.json')
        t = json.dumps(vs).replace('minecraft:block/brown_mushroom_block', n(f'block/{b}_{face}')).replace('minecraft:block/mushroom_block_inside', n(f'block/{b}_inside'))
        blockstate(big, json.loads(t))
        cube_all(big + '_inventory', n(f'block/{b}_{face}')); item_def(big, n(f'block/{big}_inventory'))
        name_block(big, btitle)
        loot(big, [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n(b), 'modifier': [
            {'type': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': -7, 'max': 2}},
            {'type': 'minecraft:limit_count', 'limit': {'min': 0}}, {'type': 'minecraft:explosion_decay'}]}]}])
        btag('minecraft:mineable/axe', n(big))

# ================================================================= TERRAIN
tex_block('tainted_soil', 'tainted_soil'); cube_all('tainted_soil', n('block/tainted_soil')); simple_state('tainted_soil')
item_def('tainted_soil', n('block/tainted_soil')); name_block('tainted_soil', 'Tainted Soil'); self_drop('tainted_soil')
tex_block('heatsand', 'heat_sand'); cube_all('heat_sand', n('block/heat_sand')); simple_state('heat_sand')
item_def('heat_sand', n('block/heat_sand')); name_block('heat_sand', 'Heat Sand'); self_drop('heat_sand')
btag('minecraft:mineable/shovel', n('tainted_soil'), n('heat_sand'))
btag('minecraft:infiniburn_overworld', n('tainted_soil')); btag('minecraft:infiniburn_nether', n('tainted_soil')); btag('minecraft:infiniburn_end', n('tainted_soil'))
btag('minecraft:supports_nether_wart', n('tainted_soil'))
shapeless('soul_sand', [n('heat_sand'), n('tainted_soil')], 'minecraft:soul_sand', 1, 'building')
bitag('c:soils/tainted', n('tainted_soil'))

# topiary grass: one texture, tinted on all sides
tex_block('grass_top', 'topiary_grass')
def tinted(m):
    m = json.loads(json.dumps(m))
    for e in m['elements']:
        for f in e['faces'].values(): f['tintindex'] = 0
    return m
model('tinted_slab', tinted(van('assets/minecraft/models/block/slab.json')) | {'parent': 'minecraft:block/block'})
model('tinted_slab_top', tinted(van('assets/minecraft/models/block/slab_top.json')) | {'parent': 'minecraft:block/block'})
GRASS_TINT = {'topiary_grass': {'type': 'minecraft:grass', 'temperature': 0.5, 'downfall': 1.0},
              'bluegrass': {'type': 'minecraft:constant', 'value': 0xFF00F3F6 - (1 << 32)},
              'autumnal_grass': {'type': 'minecraft:constant', 'value': 0xFFC92B00 - (1 << 32)}}
for g, title in (('topiary_grass', 'Topiary Grass'), ('bluegrass', 'Bluegrass'), ('autumnal_grass', 'Autumnal Grass')):
    model(g, {'parent': 'minecraft:block/leaves', 'textures': {'all': n('block/topiary_grass')}}); simple_state(g)
    item_def(g, n(f'block/{g}'), [GRASS_TINT[g]]); name_block(g, title); self_drop(g)
    s = g + '_slab'
    tx = {'bottom': n('block/topiary_grass'), 'top': n('block/topiary_grass'), 'side': n('block/topiary_grass')}
    model(s, {'parent': n('block/tinted_slab'), 'textures': tx}); model(s + '_top', {'parent': n('block/tinted_slab_top'), 'textures': tx})
    blockstate(s, {'variants': {'type=bottom': {'model': n(f'block/{s}')}, 'type=double': {'model': n(f'block/{g}')}, 'type=top': {'model': n(f'block/{s}_top')}}})
    item_def(s, n(f'block/{s}'), [GRASS_TINT[g]]); name_block(s, f'{title} Slab')
    loot(s, [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n(s), 'modifier': [
        {'type': 'minecraft:set_count', 'count': 2, 'condition': {'type': 'minecraft:match_block', 'blocks': n(s), 'state': {'type': 'double'}}},
        {'type': 'minecraft:explosion_decay'}]}]}])
    bitag('minecraft:slabs', n(s)); btag('minecraft:mineable/shovel', n(g), n(s))
    shaped(s, ['bbb'], {'b': n(g)}, n(s), 6, 'building')
shaped('topiary_grass', [' s ', 's#s', ' s '], {'s': 'minecraft:wheat_seeds', '#': 'minecraft:dirt'}, n('topiary_grass'), 1, 'building')
shapeless('bluegrass', [n('topiary_grass'), '#c:dyes/blue'], n('bluegrass'), 1, 'building')
shapeless('autumnal_grass', [n('topiary_grass'), '#c:dyes/red'], n('autumnal_grass'), 1, 'building')
assets.append(('mods/natura/textures/misc/bluegrasscolor.png', 'assets/natura/textures/colormap/bluegrass.png'))
assets.append(('mods/natura/textures/misc/orangegrasscolor.png', 'assets/natura/textures/colormap/autumnal_grass.png'))

# ================================================================= ITEMS
PLANT_ITEMS = [('barley', 'barley_plant', 'Barley', ['Similar to wheat, it grows in the wild']),
               ('barley_flour', 'barley_flour', 'Barley Flour', ['Bake me to make bread', 'Also used in baking cake']),
               ('wheat_flour', 'wheat_flour', 'Wheat Flour', ['Bake me to make bread', 'Also used in baking cake']),
               ('cotton', 'cotton_plant', 'Cotton', ['The source of all string and wool']),
               ('sulfur', 'sulfur', 'Sulfur', ['2x2 converts into gunpowder']),
               ('ghostwood_fletching', 'ghostwood_fletching', 'Ghostwood Fletching', ['Arrow crafting component']),
               ('imp_leather', 'leather_imp', 'Imp Leather', ['Resist the heat of the Nether']),
               ('flamestring', 'flamestring', 'Flamestring', ['Somewhat more durable than string']),
               ('blue_dye', 'dye_blue', 'Blue Dye', []),
               ('barley_seeds', 'barley_seeds', 'Barley Seeds', ['Similar to wheat, it grows in the wild']),
               ('cotton_seeds', 'cotton_seeds', 'Cotton Seeds', ['A great source of string and wool']),
               ('cactus_juice', 'waterdrop', 'Cactus Juice', ['Refreshing!']),
               ('saguaro_fruit', 'saguaro_fruit_item', 'Saguaro Fruit', ['Usable as food or seeds']),
               ('potash_apple', 'fruit_potashapple', 'Potash Apple', ['Tastes a bit like chalk']),
               ('wheat_seed_bag', 'seedbag_wheat', 'Wheat Seed Bag', ['Plants in a 3x3 area']),
               ('barley_seed_bag', 'seedbag_barley', 'Barley Seed Bag', ['Plants in a 3x3 area']),
               ('potato_bag', 'seedbag_potato', 'Potato Bag', ['Plants in a 3x3 area']),
               ('carrot_bag', 'seedbag_carrot', 'Carrot Bag', ['Plants in a 3x3 area']),
               ('nether_wart_bag', 'seedbag_netherwart', 'Nether Wart Bag', ['Plants in a 3x3 area']),
               ('cotton_seed_bag', 'seedbag_cotton', 'Cotton Seed Bag', ['Plants in a 3x3 area']),
               ('bonemeal_bag', 'seedbag_bone', 'Bonemeal Bag', ['Grows plants in a 3x3 area'])]
for i, orig, title, tips in PLANT_ITEMS:
    tex_item(orig, i); generated_item(i, n(f'item/{i}')); name_item(i, title); tooltip(i, *tips)
itag('c:dyes/blue', n('blue_dye')); itag('c:dyes', n('blue_dye'))
itag('c:seeds', n('barley_seeds'), n('cotton_seeds')); itag('c:seeds/barley', n('barley_seeds')); itag('c:seeds/cotton', n('cotton_seeds'))
itag('c:crops', n('barley'), n('cotton')); itag('c:crops/barley', n('barley')); itag('c:crops/cotton', n('cotton'))
itag('c:dusts', n('sulfur')); itag('c:dusts/sulfur', n('sulfur'))
# Natura.interactEvent / spawnEvent: cows and sheep follow and breed with barley, chickens follow the seeds
itag('minecraft:cow_food', n('barley')); itag('minecraft:sheep_food', n('barley'))
itag('minecraft:chicken_food', n('barley_seeds'), n('cotton_seeds'))
for bag, content in (('wheat_seed_bag', 'minecraft:wheat_seeds'), ('barley_seed_bag', n('barley_seeds')), ('potato_bag', 'minecraft:potato'),
                     ('carrot_bag', 'minecraft:carrot'), ('nether_wart_bag', 'minecraft:nether_wart'), ('cotton_seed_bag', n('cotton_seeds')),
                     ('bonemeal_bag', 'minecraft:bone_meal')):
    shaped(bag, ['sss', 'sss', 'sss'], {'s': content}, n(bag), 1, 'misc')
    shaped(f'{content.split(":")[1]}_from_{bag}', ['s'], {'s': n(bag)}, content, 9, 'misc')
shaped('string_from_cotton', ['sss'], {'s': n('cotton')}, 'minecraft:string')
shaped('white_wool_from_cotton', ['sss', 'sss', 'sss'], {'s': n('cotton')}, 'minecraft:white_wool', 1, 'building')
shaped('cactus_juice', ['X'], {'X': 'minecraft:cactus'}, n('cactus_juice'), 1, 'food')
shaped('water_bucket_from_cactus_juice', ['www', 'wBw', 'www'], {'w': n('cactus_juice'), 'B': 'minecraft:bucket'}, 'minecraft:water_bucket')
shaped('bread_from_barley', ['bbb'], {'b': n('barley')}, 'minecraft:bread', 1, 'food')
shaped('barley_flour', ['X'], {'X': n('barley')}, n('barley_flour'), 1, 'food')
# "Enable wheat to flour recipe" is false in the pack: no wheat flour recipe
for flour in ('barley_flour', 'wheat_flour'):
    smelting(f'bread_from_{flour}', n(flour), 'minecraft:bread', 0.5)
    shaped(f'cake_from_{flour}', ['AAA', 'BEB', ' C '], {'A': 'minecraft:milk_bucket', 'B': 'minecraft:sugar', 'E': '#minecraft:eggs', 'C': n(flour)},
           'minecraft:cake', 1, 'food')
shaped('ghostwood_fletching', [' s ', '#s#', '#s#'], {'s': n('ghostwood_stick'), '#': n('ghostwood_leaves')}, n('ghostwood_fletching'))
shaped('arrow_from_ghostwood_fletching', [' f ', '#s#', ' # '], {'f': 'minecraft:flint', 's': '#c:rods/wooden', '#': n('ghostwood_fletching')},
       'minecraft:arrow', 4, 'equipment')
shaped('leather_from_imp_leather', ['##', '##'], {'#': n('imp_leather')}, 'minecraft:leather', 2)
itag('c:rods/wooden', 'minecraft:stick')
# NContent: wooden tools of vanilla from Natura sticks and any planks (the original used one pattern for all five, a bug)
for tool, pat in (('wooden_sword', ['#', '#', 's']), ('wooden_pickaxe', ['###', ' s ', ' s ']), ('wooden_shovel', ['#', 's', 's']),
                  ('wooden_axe', ['##', '#s', ' s']), ('wooden_hoe', ['##', ' s', ' s'])):
    # the four nether woods make their own tools with their own sticks
    planks = {'neoforge:ingredient_type': 'neoforge:difference', 'base': '#minecraft:planks', 'subtracted': '#natura:nether_wood_planks'}
    shaped(f'{tool}_from_natura_sticks', pat, {'#': planks, 's': '#natura:sticks'}, f'minecraft:{tool}', 1, 'equipment')

# bowls and stews
BOWLS = ['ghostwood', 'bloodwood', 'darkwood', 'fusewood']
tex_item('stew_mushroom', 'mushroom_stew_overlay'); tex_item('stew_glowshroom', 'glowshroom_stew_overlay')
for w in BOWLS:
    tex_item(f'{w}_bowl', f'{w}_bowl'); generated_item(f'{w}_bowl', n(f'item/{w}_bowl')); name_item(f'{w}_bowl', f'{TITLE[w]} Bowl')
    generated_item(f'{w}_mushroom_stew', n(f'item/{w}_bowl'), [n('item/mushroom_stew_overlay')]); name_item(f'{w}_mushroom_stew', 'Mushroom Stew')
    generated_item(f'{w}_glowshroom_stew', n(f'item/{w}_bowl'), [n('item/glowshroom_stew_overlay')]); name_item(f'{w}_glowshroom_stew', 'Glowshroom Stew')
    shaped(f'{w}_bowl', ['# #', ' # '], {'#': n(f'{w}_planks')}, n(f'{w}_bowl'), 4)
    shapeless(f'{w}_mushroom_stew', [n(f'{w}_bowl'), 'minecraft:brown_mushroom', 'minecraft:red_mushroom'], n(f'{w}_mushroom_stew'), 1, 'food')
    shapeless(f'{w}_glowshroom_stew', [n(f'{w}_bowl'), n('green_glowshroom'), n('purple_glowshroom'), n('blue_glowshroom')], n(f'{w}_glowshroom_stew'), 1, 'food')
generated_item('glowshroom_stew', 'minecraft:item/bowl', [n('item/glowshroom_stew_overlay')]); name_item('glowshroom_stew', 'Glowshroom Stew')
shapeless('glowshroom_stew', ['minecraft:bowl', n('green_glowshroom'), n('purple_glowshroom'), n('blue_glowshroom')], n('glowshroom_stew'), 1, 'food')

# tools
TOOLSETS = [('ghostwood', 'ghostwood', 'Ghostwood'), ('bloodwood', 'bloodwood', 'Bloodwood'), ('darkwood', 'darkwood', 'Darkwood'),
            ('fusewood', 'fusewood', 'Fusewood'), ('quartz', 'netherquartz', 'Quartz')]
for pre, orig, title in TOOLSETS:
    for kind, ktitle in (('sword', 'Sword'), ('pickaxe', 'Pickaxe'), ('shovel', 'Shovel'), ('hatchet', 'Hatchet'), ('kama', 'Kama')):
        i = f'{pre}_{kind}'
        tex_item(f'{orig}_{kind}', i)
        item_model(i, {'parent': 'minecraft:item/handheld', 'textures': {'layer0': n(f'item/{i}')}}); item_def(i, n(f'item/{i}'))
        name_item(i, f'{title} {ktitle}')
    tooltip(f'{pre}_kama', 'The great leaf harvester')
    itag('minecraft:swords', n(f'{pre}_sword'), n(f'{pre}_kama')); itag('minecraft:pickaxes', n(f'{pre}_pickaxe'))
    itag('minecraft:shovels', n(f'{pre}_shovel')); itag('minecraft:axes', n(f'{pre}_hatchet')); itag('natura:kamas', n(f'{pre}_kama'))
    if pre != 'quartz':
        P, St = n(f'{pre}_planks'), n(f'{pre}_stick')
        shaped(f'{pre}_sword', ['#', '#', 's'], {'#': P, 's': St}, n(f'{pre}_sword'), 1, 'equipment')
        shaped(f'{pre}_pickaxe', ['###', ' s ', ' s '], {'#': P, 's': St}, n(f'{pre}_pickaxe'), 1, 'equipment')
        shaped(f'{pre}_shovel', ['#', 's', 's'], {'#': P, 's': St}, n(f'{pre}_shovel'), 1, 'equipment')
        shaped(f'{pre}_hatchet', ['##', '#s', ' s'], {'#': P, 's': St}, n(f'{pre}_hatchet'), 1, 'equipment')
        shaped(f'{pre}_kama', ['##', ' s', ' s'], {'#': P, 's': St}, n(f'{pre}_kama'), 1, 'equipment')
        # bow
        b = f'{pre}_bow'
        tex_item(f'{orig}_bow', b)
        for k in range(3): tex_item(f'{orig}_bow_drawn_{k}', f'{b}_pulling_{k}')
        bow = van('assets/minecraft/models/item/bow.json'); bow['parent'] = 'minecraft:item/generated'; bow['textures'] = {'layer0': n(f'item/{b}')}
        item_model(b, bow)
        for k in range(3): item_model(f'{b}_pulling_{k}', {'parent': n(f'item/{b}'), 'textures': {'layer0': n(f'item/{b}_pulling_{k}')}})
        write(f'assets/natura/items/{b}.json', {'model': {'type': 'minecraft:condition', 'property': 'minecraft:using_item',
            'on_false': {'type': 'minecraft:model', 'model': n(f'item/{b}')},
            'on_true': {'type': 'minecraft:range_dispatch', 'property': 'minecraft:use_duration', 'scale': 1.0,
                        'entries': [{'threshold': 8, 'model': {'type': 'minecraft:model', 'model': n(f'item/{b}_pulling_1')}},
                                    {'threshold': 14, 'model': {'type': 'minecraft:model', 'model': n(f'item/{b}_pulling_2')}}],
                        'fallback': {'type': 'minecraft:model', 'model': n(f'item/{b}_pulling_0')}}}})
        name_item(b, f'{title} Bow')
        itag('minecraft:enchantable/bow', n(b)); itag('minecraft:enchantable/durability', n(b)); itag('c:tools/bow', n(b))
        shaped(b, ['#s ', '# s', '#s '], {'#': n('flamestring'), 's': St}, n(b), 1, 'equipment')
Q = ['minecraft:quartz_block', 'minecraft:chiseled_quartz_block', 'minecraft:quartz_pillar']
GS = n('ghostwood_stick')
shaped('quartz_sword', ['#', '#', 's'], {'#': Q, 's': GS}, n('quartz_sword'), 1, 'equipment')
shaped('quartz_pickaxe', ['###', ' s ', ' s '], {'#': Q, 's': GS}, n('quartz_pickaxe'), 1, 'equipment')
shaped('quartz_shovel', ['#', 's', 's'], {'#': Q, 's': GS}, n('quartz_shovel'), 1, 'equipment')
shaped('quartz_hatchet', ['##', '#s', ' s'], {'#': Q, 's': GS}, n('quartz_hatchet'), 1, 'equipment')
itag('natura:bloodwood_tool_materials', n('bloodwood_planks'))

# imp armour
for i, orig, title, slot, pat, ench in (
        ('imp_helmet', 'armor_imp_helmet', 'Impskin Helmet', 'head', ['###', '# #'], 'minecraft:protection'),
        ('imp_jerkin', 'armor_imp_body', 'Impskin Jerkin', 'chest', ['# #', '###', '###'], 'minecraft:blast_protection'),
        ('imp_leggings', 'armor_imp_leggings', 'Impskin Leggings', 'leg', ['###', '# #', '# #'], 'minecraft:projectile_protection'),
        ('imp_boots', 'armor_imp_boots', 'Impskin Boots', 'foot', ['# #', '# #'], 'minecraft:feather_falling')):
    tex_item(orig, i); generated_item(i, n(f'item/{i}')); name_item(i, title)
    itag(f'minecraft:enchantable/{slot}_armor', n(i)); itag('minecraft:enchantable/armor', n(i))
    itag('minecraft:enchantable/equippable', n(i)); itag('minecraft:enchantable/durability', n(i))
    r = {'type': 'minecraft:crafting_shaped', 'category': 'equipment', 'key': {'#': n('imp_leather')}, 'pattern': pat,
         'result': {'id': n(i), 'count': 1, 'components': {'minecraft:enchantments': {ench: 1, 'minecraft:fire_protection': 1}}}}
    recipes[i] = r
itag('natura:repairs_imp_armor', n('imp_leather'))
assets.append(('mods/natura/textures/armor/imp_1.png', 'assets/natura/textures/entity/equipment/humanoid/imp.png'))
assets.append(('mods/natura/textures/armor/imp_2.png', 'assets/natura/textures/entity/equipment/humanoid_leggings/imp.png'))
write('assets/natura/equipment/imp.json', {'layers': {'humanoid': [{'texture': n('imp')}], 'humanoid_leggings': [{'texture': n('imp')}]}})

# ================================================================= misc data
# MinecraftForge.addGrassSeed(seeds 0 / 1, weight 3): 1/8 * 3/16 of the short grass and fern drops each
write('data/natura/loot_modifiers/grass_seeds.json', {'type': 'neoforge:add_table', 'condition': {'type': 'minecraft:any_of', 'terms': [
    {'type': 'neoforge:loot_table_id', 'loot_table_id': 'minecraft:blocks/short_grass'},
    {'type': 'neoforge:loot_table_id', 'loot_table_id': 'minecraft:blocks/fern'}]}, 'table': 'natura:gameplay/grass_seeds'})
noshear = {'type': 'minecraft:inverted', 'term': 'minecraft:tool/can_shear'}
write('data/natura/loot_table/gameplay/grass_seeds.json', {'type': 'minecraft:block', 'pools': [
    {'rolls': 1, 'condition': {'type': 'minecraft:all_of', 'terms': [noshear, chance(0.0234375)]}, 'entries': [{'type': 'minecraft:item', 'name': n('barley_seeds')}]},
    {'rolls': 1, 'condition': {'type': 'minecraft:all_of', 'terms': [noshear, chance(0.0234375)]}, 'entries': [{'type': 'minecraft:item', 'name': n('cotton_seeds')}]}]})
write('data/neoforge/data_maps/block/flammable.json', {'values': flammable})

# TreeCapitator (TreeCapitator.cfg, tree_and_mod_configs.natura; darkwood from Natura's own IMC message)
TREES = {
    'amaranth': (['amaranth_log'], ['amaranth_leaves'], None), 'bloodwood': (['bloodwood_log', 'bloodwood_bark'], ['bloodwood_leaves'], None),
    'darkwood': (['darkwood_log'], ['darkwood_leaves', 'flowering_darkwood_leaves', 'fruit_darkwood_leaves', 'fusewood_leaves'], None),
    'eucalyptus': (['eucalyptus_log'], ['eucalyptus_leaves'], None), 'ghostwood': (['ghostwood_log'], ['ghostwood_leaves'], None),
    'hopseed': (['hopseed_log'], ['hopseed_leaves'], None), 'maple': (['maple_log'], ['maple_leaves'], None),
    'redwood': (['redwood_bark', 'redwood_heart', 'redwood_root'], ['redwood_leaves'], None), 'sakura': (['sakura_log'], ['sakura_leaves'], None),
    'siverbell': (['silverbell_log'], ['silverbell_leaves'], None), 'tigerwood': (['tiger_log'], ['tiger_leaves'], None),
    'willow': (['willow_log'], ['willow_leaves'], 5)}
for key, (logs, leaves, hor) in TREES.items():
    t = {'logs': [n(x) for x in logs], 'leaves': [n(x) for x in leaves]}
    if hor: t['maxHorLeafBreakDist'] = hor
    write(f'data/natura/treecapitator/tree/{key}.json', t)

# jei
lang['jei.natura.redwood_sapling'] = 'Redwood saplings grow only when at least 40 of them are planted together in a 7x7 square. It takes days.'
lang['jei.natura.bloodwood_sapling'] = 'Bloodwood saplings are planted on the underside of netherrack, soul sand or tainted soil and grow downwards.'
lang['jei.natura.saguaro_fruit'] = 'Plant the fruit on sand: it grows into a saguaro cactus. Saguaros grow fruit on their sides when it rains.'
lang['jei.natura.cotton'] = 'Ripe cotton is picked by clicking the plant; it grows back.'
lang['jei.natura.berry_bush'] = 'Ripe bushes give a berry when clicked. Full bushes grow new bushes on top, up to three high.'
lang['jei.natura.sulfur_cloud'] = 'Sulfur clouds explode when hit by a burning arrow or lit with flint and steel.'

# NContent.addShapedRecipeFirst: the Natura recipes went before the vanilla ones. Vanilla recipes that would take
# Natura planks for the same pattern (crafting table, sticks, bookshelf, bowl) leave them out.
itag('natura:planks', *[n(f'{w}_planks') for w in WOOD_IDS])
itag('natura:nether_wood_planks', *[n(f'{w}_planks') for w in BOWLS])
for vr, sub in (('crafting_table', '#natura:planks'), ('stick', '#natura:planks'), ('bookshelf', '#natura:planks'), ('bowl', '#natura:nether_wood_planks')):
    r = van(f'data/minecraft/recipe/{vr}.json')
    for k, v in r['key'].items():
        if v == '#minecraft:planks':
            r['key'][k] = {'neoforge:ingredient_type': 'neoforge:difference', 'base': '#minecraft:planks', 'subtracted': sub}
    write(f'data/minecraft/recipe/{vr}.json', r)

# ---------------------------------------------------------------- write recipes, tags, lang, assets
for name, r in recipes.items():
    write(f'data/natura/recipe/{name}.json', r)
for (reg, t), values in tags.items():
    ns, path = t.split(':')
    write(f'data/{ns}/tags/{reg}/{path}.json', {'values': values})
write('assets/natura/lang/en_us.json', dict(sorted(lang.items())))
with open(ROOT + '/original-assets.txt', 'w') as f:
    f.write('# path in Natura_1.5.2_2.1.5.1.jar (natura-v2.1.5.1.zip) -> path in the ported jar\n')
    seen = set()
    for s, d in assets:
        if d in seen: continue
        seen.add(d)
        f.write(f'{s} -> {d}\n')
print('recipes', len(recipes), 'tags', len(tags), 'lang', len(lang), 'assets', len(seen))
