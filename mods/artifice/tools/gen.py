#!/usr/bin/env python3
"""Generates the assets/data JSON of the Artifice port (mods/artifice/src/main/resources), the language file and
original-assets.txt. Everything under resources/assets and resources/data is overwritten."""
import json, os, sys, shutil, glob, zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = ROOT + '/src/main/resources'
# Vanilla assets/data of 26.3, unpacked once from the patched Minecraft jar (run ./gradlew :artifice:compileJava first).
VAN = os.path.join(ROOT, 'build', 'vanilla-data')
if not os.path.isdir(VAN):
    jars = glob.glob(os.path.join(ROOT, 'build/moddev/artifacts/minecraft-patched-*.jar'))
    if not jars:
        sys.exit('no patched Minecraft jar: run ./gradlew :artifice:compileJava first')
    with zipfile.ZipFile(jars[0]) as z:
        z.extractall(VAN, [m for m in z.namelist() if m.startswith(('assets/minecraft/', 'data/minecraft/')) and m.endswith('.json')])
NS = 'artifice'

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


assets = []  # (path in the original jar, path in the ported jar)


def tex_block(src, dst):
    assets.append((f'mods/artifice/textures/blocks/{src}.png', f'assets/artifice/textures/block/{dst}.png'))


def tex_item(src, dst):
    assets.append((f'mods/artifice/textures/items/{src}.png', f'assets/artifice/textures/item/{dst}.png'))


lang = {'itemGroup.artifice': 'Artifice'}


def name_block(b, s): lang[f'block.artifice.{b}'] = s
def name_item(i, s): lang[f'item.artifice.{i}'] = s


def tips(i, *lines):
    """ArtificeTooltips: plain lines (grey) and flavor lines (starting with '~': dark grey italic)."""
    plain = [l for l in lines if not l.startswith('~')]
    flavor = [l[1:] for l in lines if l.startswith('~')]
    for k, l in enumerate(plain):
        lang[f'tooltip.artifice.{i}.{k}'] = l
    for k, l in enumerate(flavor):
        lang[f'flavor.artifice.{i}.{k}'] = l


AQUA = '§b'

tags = {}


def tag(reg, t, *values):
    tags.setdefault((reg, t), [])
    for v in values:
        if v not in tags[(reg, t)]:
            tags[(reg, t)].append(v)


def btag(t, *v): tag('block', t, *v)
def itag(t, *v): tag('item', t, *v)
def bitag(t, *v):
    btag(t, *v); itag(t, *v)


# ---------------------------------------------------------------- model helpers
def model(name, obj): write(f'assets/artifice/models/block/{name}.json', obj)
def item_model(name, obj): write(f'assets/artifice/models/item/{name}.json', obj)
def blockstate(name, obj): write(f'assets/artifice/blockstates/{name}.json', obj)


def item_def(name, model_ref, tints=None):
    m = {'type': 'minecraft:model', 'model': model_ref}
    if tints: m['tints'] = tints
    write(f'assets/artifice/items/{name}.json', {'model': m})


def simple_state(name, model_ref=None):
    blockstate(name, {'variants': {'': {'model': model_ref or n(f'block/{name}')}}})


def generated_item(name, texture):
    item_model(name, {'parent': 'minecraft:item/generated', 'textures': {'layer0': texture}})
    item_def(name, n(f'item/{name}'))


def handheld_item(name, texture):
    item_model(name, {'parent': 'minecraft:item/handheld', 'textures': {'layer0': texture}})
    item_def(name, n(f'item/{name}'))


def cube_all(name, texture):
    model(name, {'parent': 'minecraft:block/cube_all', 'textures': {'all': texture}})


def face(tex, cull=None, uv=(0, 0, 16, 16)):
    f = {'uv': list(uv), 'texture': tex}
    if cull: f['cullface'] = cull
    return f


SIDES = ('down', 'up', 'north', 'south', 'west', 'east')


def cube_element(textures, cull):
    """A full cube element; textures: side -> '#tex' (sides left out have no face)."""
    return {'from': [0, 0, 0], 'to': [16, 16, 16],
            'faces': {s: face(t, s if cull else None) for s, t in textures.items()}}


# ---------------------------------------------------------------- loot helpers
def loot(name, pools):
    write(f'data/artifice/loot_table/blocks/{name}.json', {'type': 'minecraft:block', 'pools': pools,
                                                           'random_sequence': f'artifice:blocks/{name}'})


def self_drop(name, item=None):
    loot(name, [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n(item or name)}],
                 'condition': {'type': 'minecraft:survives_explosion'}}])


def slab_loot(name):
    loot(name, [{'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n(name), 'modifier': [
        {'type': 'minecraft:set_count', 'count': 2, 'condition': {'type': 'minecraft:match_block', 'blocks': n(name), 'state': {'type': 'double'}}},
        {'type': 'minecraft:explosion_decay'}]}]}])


# ---------------------------------------------------------------- recipe helpers
recipes = {}


def shaped(name, pattern, key, result, count=1, category='misc'):
    assert name not in recipes, name
    recipes[name] = {'type': 'minecraft:crafting_shaped', 'category': category, 'key': key, 'pattern': pattern,
                     'result': {'count': count, 'id': result}}


def shapeless(name, ingredients, result, count=1, category='misc'):
    assert name not in recipes, name
    recipes[name] = {'type': 'minecraft:crafting_shapeless', 'category': category, 'ingredients': ingredients,
                     'result': {'count': count, 'id': result}}


def smelting(name, ingredient, result, xp):
    recipes[name] = {'type': 'minecraft:smelting', 'category': 'misc', 'cookingtime': 200, 'experience': xp,
                     'ingredient': ingredient, 'result': {'id': result}}


PLANKS = '#minecraft:planks'   # "plankWood"
STICK = '#c:rods/wooden'      # "stickWood"
STEEL = '#c:ingots/steel'     # "ingotSteel"

# ================================================================= BASALT / MARBLE (BlockBasalt, BlockMarble)
# metadata 0 stone, 1 cobblestone, 2 brick, 3 paver, 4 antipaver, 5 chiseled
ROCK_FLAVOR = {
    'basalt': ['Volcanic stone', '~You can see shiny specks on the surface'],
    'basalt_cobblestone': ['~Covered in jagged edges'],
    'basalt_brick': ['~Basalt lends itself well to smooth surfaces'],
    'basalt_paver': ['Connected Textured', '~The trick is snug surfaces'],
    'basalt_antipaver': ['Connected Textured', '~The secret is careful carving'],
    'chiseled_basalt': ['~Precise edges define geometric patterns'],
    'marble': ['Metamorphic stone', '~Filled with interesting patterns'],
    'marble_cobblestone': ['~As it turns out, rather soft'],
    'marble_brick': ['~Polishing really brings out the glow'],
    'marble_paver': ['Connected Textured', '~The trick is snug surfaces'],
    'marble_antipaver': ['Connected Textured', '~The secret is careful carving'],
    'chiseled_marble': ['~There\'s a reason marble is prized'],
    'basalt_brick_stairs': ['~The pieces slot easily into place'],
    'marble_brick_stairs': ['~Marble wears easily'],
    'basalt_cobblestone_stairs': ['~It can be tricky to get the desired angles'],
    'marble_cobblestone_stairs': ['~Lends a rustic air'],
    'basalt_brick_slab': ['~Thin layers of basalt can fracture easily'],
    'marble_brick_slab': ['~Marble is often used as a facade'],
    'basalt_cobblestone_slab': ['~Basalt easily shears apart'],
    'marble_cobblestone_slab': ['~Cutting marble generates a lot of dust'],
    'basalt_paver_slab': ['Connected Textured', '~The trick is snug surfaces'],
    'marble_paver_slab': ['Connected Textured', '~The trick is snug surfaces'],
    'basalt_antipaver_slab': ['Connected Textured', '~The secret is careful carving'],
    'marble_antipaver_slab': ['Connected Textured', '~The secret is careful carving'],
}
for b, lines in ROCK_FLAVOR.items():
    tips(b, *lines)

for rock, Rock in (('basalt', 'Basalt'), ('marble', 'Marble')):
    stone, cobble, brick, paver, anti, chis = (rock, f'{rock}_cobblestone', f'{rock}_brick', f'{rock}_paver',
                                               f'{rock}_antipaver', f'chiseled_{rock}')
    tex_block(f'{rock}/{rock}', stone)
    tex_block(f'{rock}/cobblestone', cobble)
    tex_block(f'{rock}/bricks', f'{rock}_bricks')
    tex_block(f'{rock}/chiseled', chis)
    # paver and antipaver share the connected texture "<rock>_paver"; without connected textures the port shows
    # the piece the original showed in the inventory (textureList[0])
    tex_block(f'{rock}/paver/{rock}_paver_0', paver)
    tex_block(f'{rock}/paverside', f'{rock}_paver_side')
    textures = {stone: stone, cobble: cobble, brick: f'{rock}_bricks', paver: paver, anti: paver, chis: chis}
    names = {stone: Rock, cobble: f'{Rock} Cobblestone', brick: f'{Rock} Brick', paver: f'{Rock} Paver',
             anti: f'{Rock} Antipaver', chis: f'Chiseled {Rock}'}
    for b, t in textures.items():
        cube_all(b, n(f'block/{t}'))
        simple_state(b)
        item_def(b, n(f'block/{b}'))
        name_block(b, names[b])
        btag('minecraft:mineable/pickaxe', n(b))
        if b != stone:
            self_drop(b)
    # the stone drops its cobblestone (damageDropped 0 -> 1); silk touch (createStackedBlock) keeps the stone
    stone_loot = van('data/minecraft/loot_table/blocks/stone.json')
    loot(stone, json.loads(json.dumps(stone_loot['pools']).replace('minecraft:stone', n(stone)).replace('minecraft:cobblestone', n(cobble))))
    smelting(f'{stone}_from_smelting', n(cobble), n(stone), 0.1)
    shaped(brick, ['XX', 'XX'], {'X': n(stone)}, n(brick), 4, 'building')
    shaped(paver, ['XX', 'XX'], {'X': n(brick)}, n(paver), 4, 'building')
    shaped(chis, ['X', 'X'], {'X': n(brick)}, n(chis), 2, 'building')
    shapeless(anti, [n(paver)], n(anti), 1, 'building')
    shapeless(f'{paver}_from_antipaver', [n(anti)], n(paver), 1, 'building')

    # stairs (BlockStairsArtifice: brick = meta 2, cobblestone = meta 1)
    for base, tex in ((brick, f'{rock}_bricks'), (cobble, cobble)):
        s = f'{base}_stairs'
        st = van('assets/minecraft/blockstates/stone_brick_stairs.json')
        blockstate(s, json.loads(json.dumps(st).replace('minecraft:block/stone_brick_stairs', n(f'block/{s}'))))
        for suffix in ('', '_inner', '_outer'):
            m = van(f'assets/minecraft/models/block/stone_brick_stairs{suffix}.json')
            m = json.loads(json.dumps(m).replace('minecraft:block/stone_bricks', n(f'block/{tex}')))
            if not m['parent'].startswith('minecraft:'):
                m['parent'] = 'minecraft:' + m['parent']
            model(s + suffix, m)
        item_def(s, n(f'block/{s}'))
        name_block(s, f'{names[base]} Stairs' if base == brick else f'{Rock} Cobblestone Stairs')
        self_drop(s)
        bitag('minecraft:stairs', n(s))
        btag('minecraft:mineable/pickaxe', n(s))
        shaped(s, ['X  ', 'XX ', 'XXX'], {'X': n(base)}, n(s), 4, 'building')

    # slabs (BlockBasaltSlab/BlockMarbleSlab: 0 brick, 1 cobble, 2 paver, 3 antipaver). Brick and cobble slabs use
    # the textures of the block; paver slabs the paver on top and bottom and "paverside" on the sides (also double).
    for base, slab_name, title in ((brick, f'{brick}_slab', f'{Rock} Brick Slab'), (cobble, f'{cobble}_slab', f'{Rock} Cobblestone Slab'),
                                   (paver, f'{paver}_slab', f'{Rock} Paver Slab'), (anti, f'{anti}_slab', f'{Rock} Antipaver Slab')):
        if base in (paver, anti):
            tx = {'bottom': n(f'block/{paver}'), 'top': n(f'block/{paver}'), 'side': n(f'block/{rock}_paver_side')}
            double = f'{slab_name}_double'
            model(double, {'parent': 'minecraft:block/cube_bottom_top', 'textures': tx})
            double = n(f'block/{double}')
        else:
            t = n(f'block/{textures[base]}')
            tx = {'bottom': t, 'top': t, 'side': t}
            double = n(f'block/{base}')
        model(slab_name, {'parent': 'minecraft:block/slab', 'textures': tx})
        model(slab_name + '_top', {'parent': 'minecraft:block/slab_top', 'textures': tx})
        blockstate(slab_name, {'variants': {'type=bottom': {'model': n(f'block/{slab_name}')}, 'type=double': {'model': double},
                                            'type=top': {'model': n(f'block/{slab_name}_top')}}})
        item_def(slab_name, n(f'block/{slab_name}'))
        name_block(slab_name, title)
        slab_loot(slab_name)
        bitag('minecraft:slabs', n(slab_name))
        btag('minecraft:mineable/pickaxe', n(slab_name))
        shaped(slab_name, ['XXX'], {'X': n(base)}, n(slab_name), 6, 'building')
    # paver and antipaver slabs turn into each other
    shapeless(f'{anti}_slab_from_{paver}_slab', [n(f'{paver}_slab')], n(f'{anti}_slab'), 1, 'building')
    shapeless(f'{paver}_slab_from_{anti}_slab', [n(f'{anti}_slab')], n(f'{paver}_slab'), 1, 'building')

# ================================================================= FLORA (BlockFlora, BlockLotus, ItemDye)
FLORA = [  # block, texture, english, tooltip
    ('bluebell', 'bluebell', 'Bluebell', ['A pretty shade of blue', '~Has a pleasant aroma']),
    ('orchid', 'orchid', 'Orchid', ['Luxuriously dark', '~Velvety soft']),
    ('iris', 'iris', 'Iris', ['Earthy and subdued', '~A hardy flower']),
]
for b, t, title, lines in FLORA:
    tex_block(f'flora/{t}', b)
    model(b, {'parent': 'minecraft:block/cross', 'textures': {'cross': n(f'block/{b}')}})
    simple_state(b)
    generated_item(b, n(f'block/{b}'))
    name_block(b, title); tips(b, *lines); self_drop(b)
    bitag('minecraft:small_flowers', n(b))
# the lotus closes in low light (metadata 4); it drops and picks as the open lotus
tex_block('flora/lotus', 'lotus'); tex_block('flora/lotusclosed', 'lotus_closed')
model('lotus', {'parent': 'minecraft:block/cross', 'textures': {'cross': n('block/lotus')}})
model('lotus_closed', {'parent': 'minecraft:block/cross', 'textures': {'cross': n('block/lotus_closed')}})
blockstate('lotus', {'variants': {'open=true': {'model': n('block/lotus')}, 'open=false': {'model': n('block/lotus_closed')}}})
generated_item('lotus', n('block/lotus'))
name_block('lotus', 'Lotus'); tips('lotus', 'Alabaster white', 'Closes in low light', '~What a delightful flower'); self_drop('lotus')
bitag('minecraft:small_flowers', n('lotus'))

# lotus lily (LotusRenderer: the vanilla lily pad, tinted like it, plus the lotus as crossed squares)
tex_block('flora/waterlotus', 'lotus_lily'); tex_block('flora/waterlotusclosed', 'lotus_lily_closed')
pad = van('assets/minecraft/models/block/lily_pad.json')
cross = van('assets/minecraft/models/block/cross.json')
for b in ('lotus_lily', 'lotus_lily_closed'):
    model(b, {'ambientocclusion': False, 'textures': {'particle': 'minecraft:block/lily_pad', 'texture': 'minecraft:block/lily_pad',
                                                      'cross': n(f'block/{b}')},
              'elements': pad['elements'] + cross['elements']})
blockstate('lotus_lily', {'variants': {f'open={o}': [dict({'model': n('block/lotus_lily' if o == 'true' else 'block/lotus_lily_closed')},
                                                          **({'y': y} if y else {})) for y in (0, 90, 180, 270)]
                                       for o in ('true', 'false')}})
generated_item('lotus_lily', n('block/lotus_lily'))
name_block('lotus_lily', 'Lotus Lily'); tips('lotus_lily', 'Closes in low light', '~Reminiscent of candles on the water')
self_drop('lotus_lily')

DYES = [  # item, texture, english, colour tag, tooltip, flower
    ('blue_dye', 'blue', 'Sea Blue', 'blue', ['Blue Dye', '~Well, some seas'], 'bluebell'),
    ('black_dye', 'black', 'Midnight Black', 'black', ['Black Dye', '~Darker than soot'], 'orchid'),
    ('brown_dye', 'brown', 'Oak Brown', 'brown', ['Brown Dye', '~Almost reddish'], 'iris'),
    ('white_dye', 'white', 'Bone White', 'white', ['White Dye', '~Stark as bleached bones'], 'lotus'),
]
for i, t, title, colour, lines, flower in DYES:
    tex_item(f'dye/{t}', i); generated_item(i, n(f'item/{i}')); name_item(i, title); tips(i, *lines)
    itag(f'c:dyes/{colour}', n(i))
    itag('c:dyes', n(i))
    # real dyes (DyeItem with the dye component): also usable wherever vanilla takes dyes
    for t in ('minecraft:dyes', 'minecraft:loom_dyes', 'minecraft:cat_collar_dyes', 'minecraft:wolf_collar_dyes'):
        itag(t, n(i))
    shapeless(i, [n(flower)], n(i), 2)
# "dyeBlue" + "dyeYellow" -> 2 cactus green
shapeless('green_dye_from_blue_and_yellow', ['#c:dyes/blue', '#c:dyes/yellow'], 'minecraft:green_dye', 2)

# ================================================================= STEEL (ItemSteel, BlockSteel)
tex_item('ingot_steel', 'steel_ingot'); generated_item('steel_ingot', n('item/steel_ingot')); name_item('steel_ingot', 'Steel Ingot')
tips('steel_ingot', 'A strong iron alloy', '~Steel is progress')
tex_item('dust_steel', 'steel_dust'); generated_item('steel_dust', n('item/steel_dust')); name_item('steel_dust', 'Steel Dust')
tips('steel_dust', 'A pile of coal dust and iron bits', '~Sooty')
tex_block('misc/steel', 'steel_block'); cube_all('steel_block', n('block/steel_block')); simple_state('steel_block')
item_def('steel_block', n('block/steel_block')); name_block('steel_block', 'Block of Steel'); self_drop('steel_block')
tips('steel_block', 'A strong iron alloy', '~Steel is progress')
btag('minecraft:mineable/pickaxe', n('steel_block'))
itag('c:ingots/steel', n('steel_ingot')); itag('c:ingots', '#c:ingots/steel')
itag('c:dusts/steel', n('steel_dust')); itag('c:dusts', '#c:dusts/steel')
btag('c:storage_blocks/steel', n('steel_block')); itag('c:storage_blocks/steel', n('steel_block'))
btag('c:storage_blocks', '#c:storage_blocks/steel'); itag('c:storage_blocks', '#c:storage_blocks/steel')
smelting('steel_ingot', 'minecraft:iron_ingot', n('steel_ingot'), 1.0)
shaped('steel_block', ['XXX', 'XXX', 'XXX'], {'X': STEEL}, n('steel_block'), 1, 'building')
shapeless('steel_ingot_from_block', [n('steel_block')], n('steel_ingot'), 9)

# ================================================================= FRAMES (block/frame)
TIERS = [('basic', 'Basic'), ('reinforced', 'Reinforced'), ('industrial', 'Industrial'), ('advanced', 'Advanced')]
FRAME_TIPS = [['Sturdy wooden frame', '~You just put that bit here...'], ['Sturdy frame reinforced with iron', '~A little ingenuity goes a long way'],
              ['Strong iron frame', '~With a firm foundation...'], ['Very strong steel frame', '~...anything is possible']]
GLASS_TIPS = [('Reinforced Glass', 15, 'Strengthening glass is tricky'), ('Ceramic Glass', 25, 'Ceramics make the world go round'),
              ('Rock Glass', 45, 'Volcanic glass is naturally strong'), ('Laminated Glass', 75, 'Filled with thin steel threads')]
BLAST_TIPS = [('Reinforced Stone Bricks', 20, 'A good first attempt'), ('Reinforced Bricks', 30, 'Hardened bricks, eh?'),
              ('Obsidian Bricks', 50, 'There is always progress to be made...'), ('Steel Plating', 80, '...and progress always provides')]
SCAFFOLD_TIPS = [('Simple scaffold', 4, 'Handy to have'), ('Composite scaffold', 8, 'A bit less rickety'),
                 ('Construction scaffold', 12, '100% safe'), ('Rugged scaffold', 16, 'Precisely machined')]


def layered(name, inner, outer, cull, inner_sides=SIDES, outer_tex=None):
    """FrameRenderer: the inner layer (getRenderIcon) and over it the frame (the connected texture, piece 0)."""
    outer_tex = outer_tex or {s: '#frame' for s in SIDES}
    textures = {'particle': outer if isinstance(outer, str) else outer['particle'], 'inner': inner}
    if isinstance(outer, str):
        textures['frame'] = outer
    else:
        textures.update(outer)
    elements = []
    if inner_sides:
        elements.append(cube_element({s: '#inner' for s in inner_sides}, cull))
    elements.append(cube_element(outer_tex, cull))
    model(name, {'parent': 'minecraft:block/block', 'textures': textures, 'elements': elements})


for k, (tier, Tier) in enumerate(TIERS):
    tex_block(f'frame/{tier}', f'{tier}_frame')
    tex_block(f'frame/{tier}/frame_{tier}_0', f'{tier}_frame_overlay')
    # frame: not opaque, every side drawn (shouldSideBeRendered always true)
    b = f'{tier}_frame'
    model(b, {'parent': 'minecraft:block/block', 'textures': {'particle': n(f'block/{b}'), 'all': n(f'block/{b}')},
              'elements': [cube_element({s: '#all' for s in SIDES}, False)]})
    simple_state(b); item_def(b, n(f'block/{b}')); name_block(b, f'{Tier} Frame'); self_drop(b); tips(b, *FRAME_TIPS[k])
    # blast wall (opaque)
    b = f'{tier}_blast_wall'
    tex_block(f'blastwall/{tier}', b)
    layered(b, n(f'block/{b}'), n(f'block/{tier}_frame_overlay'), True)
    title, res, flavor = BLAST_TIPS[k]
    simple_state(b); item_def(b, n(f'block/{b}')); name_block(b, f'{Tier} Blast Wall'); self_drop(b)
    tips(b, title, f'Resistance: {AQUA}{res}', '~' + flavor)
    # glass wall (transparent)
    b = f'{tier}_glass_wall'
    tex_block(f'glasswall/{tier}', b)
    layered(b, n(f'block/{b}'), n(f'block/{tier}_frame_overlay'), False)
    title, res, flavor = GLASS_TIPS[k]
    simple_state(b); item_def(b, n(f'block/{b}')); name_block(b, f'{Tier} Glass Wall'); self_drop(b)
    tips(b, title, f'Resistance: {AQUA}{res}', '~' + flavor)
    # scaffolding: sides "<tier>_side"; top and bottom "<tier>_vert" under the frame
    b = f'{tier}_scaffolding'
    tex_block(f'scaffold/{tier}_side', f'{b}_side'); tex_block(f'scaffold/{tier}_vert', f'{b}_vert')
    layered(b, n(f'block/{b}_vert'), {'particle': n(f'block/{b}_side'), 'frame': n(f'block/{tier}_frame_overlay'), 'side': n(f'block/{b}_side')},
            False, ('down', 'up'), {'down': '#frame', 'up': '#frame', 'north': '#side', 'south': '#side', 'west': '#side', 'east': '#side'})
    title, overhang, flavor = SCAFFOLD_TIPS[k]
    simple_state(b); item_def(b, n(f'block/{b}')); name_block(b, f'{Tier} Scaffolding'); self_drop(b)
    tips(b, title, f'Max Overhang: {AQUA}{overhang}', '~' + flavor)
    btag('minecraft:mineable/pickaxe', *[n(f'{tier}_{x}') for x in ('frame', 'blast_wall', 'glass_wall', 'scaffolding')])

tex_block('misc/detector', 'detector'); cube_all('detector', n('block/detector'))
blockstate('detector', {'variants': {f'pulse={p}': {'model': n('block/detector')} for p in range(3)}})
item_def('detector', n('block/detector')); name_block('detector', 'Detector'); self_drop('detector')
tips('detector', 'Pulses on adjacent block updates', '~A rather delicate contraption')
btag('minecraft:mineable/pickaxe', n('detector'))

# frame recipes
shaped('basic_frame', ['XYX', 'YYY', 'XYX'], {'X': PLANKS, 'Y': STICK}, n('basic_frame'), 8, 'building')
shaped('reinforced_frame', ['XYX', 'YYY', 'XYX'], {'X': PLANKS, 'Y': 'minecraft:iron_ingot'}, n('reinforced_frame'), 8, 'building')
shaped('industrial_frame', ['X X', ' Y ', 'X X'], {'X': 'minecraft:iron_ingot', 'Y': 'minecraft:iron_block'}, n('industrial_frame'), 16, 'building')
shaped('advanced_frame', ['X X', ' Y ', 'X X'], {'X': STEEL, 'Y': '#c:storage_blocks/steel'}, n('advanced_frame'), 16, 'building')
shaped('detector', [' Q ', 'YXY', ' Y '], {'Y': 'minecraft:string', 'X': n('basic_frame'), 'Q': 'minecraft:redstone'}, n('detector'), 1, 'redstone')
# blast walls
shaped('basic_blast_wall', ['YXY', ' Z '], {'X': n('basic_frame'), 'Y': 'minecraft:stone_bricks', 'Z': 'minecraft:clay_ball'}, n('basic_blast_wall'), 2, 'building')
shaped('reinforced_blast_wall', ['YXY', 'YZY'], {'X': n('reinforced_frame'), 'Y': 'minecraft:brick', 'Z': 'minecraft:clay_ball'}, n('reinforced_blast_wall'), 2, 'building')
shaped('industrial_blast_wall', ['YXY', ' Z '], {'X': n('industrial_frame'), 'Y': 'minecraft:obsidian', 'Z': 'minecraft:clay_ball'}, n('industrial_blast_wall'), 2, 'building')
shaped('advanced_blast_wall', ['YXY', 'YYY'], {'X': n('advanced_frame'), 'Y': STEEL}, n('advanced_blast_wall'), 2, 'building')
# glass walls
shaped('basic_glass_wall', ['YXY', ' Z '], {'X': n('basic_frame'), 'Y': 'minecraft:clay_ball', 'Z': 'minecraft:glass'}, n('basic_glass_wall'), 2, 'building')
shaped('reinforced_glass_wall', ['YXY', 'YZY'], {'X': n('reinforced_frame'), 'Y': 'minecraft:clay_ball', 'Z': 'minecraft:glass'}, n('reinforced_glass_wall'), 2, 'building')
shaped('industrial_glass_wall', [' Q ', 'YXY', ' Z '], {'X': n('industrial_frame'), 'Q': 'minecraft:glass', 'Y': 'minecraft:obsidian', 'Z': 'minecraft:clay_ball'},
       n('industrial_glass_wall'), 2, 'building')
shaped('advanced_glass_wall', [' Q ', 'YXY', 'YYY'], {'X': n('advanced_frame'), 'Q': 'minecraft:glass', 'Y': STEEL}, n('advanced_glass_wall'), 2, 'building')
# scaffolding
for tier, y in (('basic', PLANKS), ('reinforced', PLANKS), ('industrial', 'minecraft:iron_ingot'), ('advanced', STEEL)):
    shaped(f'{tier}_scaffolding', ['Y', 'X', 'Y'], {'X': n(f'{tier}_frame'), 'Y': y}, n(f'{tier}_scaffolding'), 8, 'building')

# ================================================================= TOOLS (ItemSledge, ItemSickle)
MATERIALS = [  # id, 1.5.2 texture suffix, english, head ingredient, loss chance, radius, sickle flavor
    ('wooden', 'wood', 'Wooden', PLANKS, 50, 1, 'Cutting branches with wood...'),
    ('stone', 'stone', 'Stone', 'minecraft:cobblestone', 40, 2, 'Reasonably effective'),
    ('iron', 'iron', 'Iron', 'minecraft:iron_ingot', 30, 3, 'Durable and perfect for horticulture'),
    ('golden', 'gold', 'Gold', 'minecraft:gold_ingot', 25, 4, "So soft you're almost smashing them..."),
    ('diamond', 'emerald', 'Diamond', 'minecraft:diamond', 20, 5, 'The ultimate in anti-plant weaponry'),
]
for m, t, title, head, loss, radius, flavor in MATERIALS:
    sl, si = f'{m}_sledge', f'{m}_sickle'
    tex_item(f'sledge_{t}', sl); handheld_item(sl, n(f'item/{sl}')); name_item(sl, f'{title} Sledge')
    tips(sl, 'Smash blocks into parts', f'Loss Chance: {AQUA}{loss}%%', "~When all you've got is a hammer...")
    tex_item(f'sickle_{t}', si); handheld_item(si, n(f'item/{si}')); name_item(si, f'{title} Sickle')
    tips(si, 'Harvest plants and leaves', f'Radius: {AQUA}{radius}', '~' + flavor)
    # "stickWood" with the head material: sledge " YX"/" XY"/"X  ", sickle "X "/" X"/"Y "
    shaped(sl, [' YX', ' XY', 'X  '], {'X': STICK, 'Y': head}, n(sl), 1, 'equipment')
    shaped(si, ['X ', ' X', 'Y '], {'X': head, 'Y': STICK}, n(si), 1, 'equipment')
    itag('artifice:sledges', n(sl)); itag('artifice:sickles', n(si))
# ItemTool: enchantable like the 1.5.2 "digger" type (efficiency, silk touch, fortune, unbreaking)
for t in ('minecraft:enchantable/mining', 'minecraft:enchantable/mining_loot', 'minecraft:enchantable/durability'):
    itag(t, '#artifice:sledges', '#artifice:sickles')
COLORS = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan', 'purple', 'blue',
          'brown', 'green', 'red', 'black']
# blocks a sledge breaks into parts (compat/vanilla/Vanilla.loadSledges; the parts are in artifice.item.SledgeParts)
SLEDGE_BLOCKS = ['crafting_table', 'furnace', 'chest', *[f'{c}_wool' for c in COLORS], 'tnt', 'bricks', 'nether_bricks', 'bookshelf', 'sandstone',
                 'quartz_block', 'jack_o_lantern', 'anvil', 'chipped_anvil', 'damaged_anvil', 'beacon', 'note_block', 'jukebox',
                 'dispenser', 'dropper', 'piston', 'sticky_piston', 'redstone_lamp', 'daylight_detector', 'hopper', 'trapped_chest',
                 'oak_door', 'iron_door', 'repeater', 'comparator', 'oak_fence_gate', *[f'{c}_bed' for c in COLORS], 'enchanting_table',
                 'cauldron', 'water_cauldron', 'lava_cauldron', 'powder_snow_cauldron', 'brewing_stand']
btag('artifice:sledge_breakable', *[b if b.startswith('#') else 'minecraft:' + b for b in SLEDGE_BLOCKS])

# ================================================================= BOX (ItemBox, RecipeBox)
tex_item('box', 'box'); generated_item('box', n('item/box')); name_item('box', 'Box')
tips('box', 'Stores items', '~Watch out for splinters')
lang['tooltip.artifice.box.contains'] = 'Contains: %s'
lang['tooltip.artifice.box.amount'] = 'Amount: %s'
shaped('box', [' Y ', 'YXY', ' Y '], {'Y': PLANKS, 'X': STICK}, n('box'), 16)
recipes['box_packing'] = {'type': 'artifice:box'}

# ================================================================= COINS (ItemNugget, ItemCoin)
NUGGETS = [('copper_nugget', 'copper', 'Copper Nugget', 'Slightly tarnished'), ('silver_nugget', 'silver', 'Silver Nugget', 'Like a drop of quicksilver'),
           ('platinum_nugget', 'platinum', 'Platinum Nugget', 'Surprisingly heavy')]
for i, t, title, flavor in NUGGETS:
    tex_item(f'nugget/{t}', i); generated_item(i, n(f'item/{i}')); name_item(i, title); tips(i, '~' + flavor)
    itag(f'c:nuggets/{t}', n(i)); itag('c:nuggets', f'#c:nuggets/{t}')
COINS = [('shilling', 'copper', 'Shilling', 'Copper Coin', 'Dependable'), ('florin', 'silver', 'Florin', 'Silver Coin', 'Has a mirrored finish'),
         ('sovereign', 'gold', 'Sovereign', 'Gold Coin', 'Coin of the realm'), ('marque', 'platinum', 'Marque', 'Platinum Coin', 'A dollar saved...')]
for k, (i, metal, title, tip, flavor) in enumerate(COINS):
    tex_item(f'coin/{metal}', i); generated_item(i, n(f'item/{i}')); name_item(i, title); tips(i, tip, '~' + flavor)
    itag(f'c:coins/{metal}', n(i)); itag('c:coins', f'#c:coins/{metal}')
    # minting: 3 nuggets -> 3 coins
    nug = f'#c:nuggets/{metal}'
    shapeless(f'{i}_from_nuggets', [nug, nug, nug], n(i), 3)
    # changing: 4 -> 1 of the next, 1 -> 4 of the one below
    if k < 3:
        up = COINS[k + 1][0]
        shapeless(f'{up}_from_{i}', [f'#c:coins/{metal}'] * 4, n(up), 1)
        shapeless(f'{i}_from_{up}', [f'#c:coins/{COINS[k + 1][1]}'], n(i), 4)

# ================================================================= UPGRADES (ItemUpgrade, EnumUpgrades, RecipeUpgrade)
UPGRADES = [  # id, 1.5.2 texture, english, kind, flavor
    ('sharpening_kit', 'sharpeningkit', 'Sharpening Kit', 'Weapon or Tool Upgrade', 'A simple kit to sharpen blades'),
    ('reinforcement', 'reinforcement', 'Reinforcement', 'Universal Upgrade', 'The perfect materials to reinforce weak points'),
    ('reinforced_limbs', 'reinforcedlimbs', 'Reinforced Limbs', 'Bow Upgrade', 'Allows for a stronger pull'),
    ('plaited_string', 'plaitedstring', 'Plaited String', 'Bow Upgrade', 'Improves accuracy and stability'),
    ('counterweight', 'counterweight', 'Counterweight', 'Weapon Upgrade', 'Improves weapon balance'),
    ('armor_spikes', 'armorspikes', 'Armor Spikes', 'Armor Upgrade', 'Sharp spikes deter attacks'),
    ('laminated_padding', 'laminatedpadding', 'Laminated Padding', 'Armor Upgrade', 'Layered materials improve protection'),
    ('quilted_cover', 'quiltedcover', 'Quilted Cover', 'Armor Upgrade', 'Thick cloth helps block arrows'),
    ('elastic_soles', 'elasticsoles', 'Elastic Soles', 'Boot Upgrade', 'Slime makes the perfect shock absorber'),
    ('firedamp', 'firedamp', 'Firedamp', 'Armor Upgrade', 'A thick slurry that helps reduce flammability'),
    ('elastic_layering', 'elasticlayering', 'Elastic Layering', 'Armor Upgrade', 'Helpful in reducing impacts'),
    ('scuba_tank', 'scubatank', 'Scuba Tank', 'Helmet Upgrade', 'Crude, but effective air tank'),
    ('dive_kit', 'divekit', 'Dive Kit', 'Helmet Upgrade', 'Allows for clearer underwater vision'),
]
for i, t, title, kind, flavor in UPGRADES:
    tex_item(f'upgrade/{t}', i); generated_item(i, n(f'item/{i}')); name_item(i, title); tips(i, kind, '~' + flavor)
I = 'minecraft:iron_ingot'; S = 'minecraft:string'; L = 'minecraft:leather'; SL = 'minecraft:slime_ball'
shaped('sharpening_kit', [' Z ', 'YXQ'], {'X': 'minecraft:coal', 'Y': I, 'Q': 'minecraft:stone', 'Z': 'minecraft:flint'}, n('sharpening_kit'))
shaped('reinforcement', ['XQZ', ' Y '], {'X': S, 'Q': L, 'Z': I, 'Y': STICK}, n('reinforcement'))
shaped('reinforced_limbs', ['YXZ', ' X '], {'X': STICK, 'Y': L, 'Z': I}, n('reinforced_limbs'))
shaped('plaited_string', ['  X', ' X ', 'X  '], {'X': S}, n('plaited_string'))
shaped('counterweight', ['X', 'X', 'Y'], {'X': I, 'Y': 'minecraft:stone'}, n('counterweight'))
shaped('armor_spikes', ['XXX', ' Y '], {'X': 'minecraft:iron_bars', 'Y': I}, n('armor_spikes'))
shaped('laminated_padding', ['YXY', ' X '], {'X': L, 'Y': S}, n('laminated_padding'))
shaped('quilted_cover', ['YXY', ' X '], {'X': '#minecraft:wool', 'Y': S}, n('quilted_cover'))
shaped('elastic_soles', ['X X', 'Y Y'], {'X': L, 'Y': SL}, n('elastic_soles'))
shaped('firedamp', ['X', 'Y', 'Q'], {'Q': 'minecraft:bowl', 'Y': SL, 'X': 'minecraft:bone_meal'}, n('firedamp'))
shaped('elastic_layering', ['YXQ', ' X '], {'X': SL, 'Y': L, 'Q': S}, n('elastic_layering'))
shaped('scuba_tank', ['Q', 'Y', 'X'], {'Q': S, 'Y': L, 'X': 'minecraft:bucket'}, n('scuba_tank'))
shaped('dive_kit', [' Q ', 'XYX', ' Z '], {'Y': 'minecraft:glass', 'X': L, 'Z': I, 'Q': S}, n('dive_kit'))
recipes['tool_upgrade'] = {'type': 'artifice:upgrade'}

# ================================================================= JEI info pages (artifice.compat.ArtificeJeiPlugin)
lang['jei.artifice.box'] = ('Put up to 8 equal stacks into a crafting grid together with an empty box to pack them into one box. '
                            'A filled box alone in the grid unpacks one stack (shift-click: the whole stack of boxes). Right-click '
                            'unpacks one box, sneak right-click the whole stack; a dispenser unpacks too.')
lang['jei.artifice.upgrade'] = ('Craft a tool, weapon or armour piece together with upgrades: every upgrade raises its enchantment by '
                                'one level, up to the level shown on the upgrade. Incompatible enchantments are refused.')
lang['jei.artifice.sledge'] = 'Breaks these blocks into their parts (each part may be lost):'
lang['jei.artifice.sledge.entry'] = '%s: %s'
lang['jei.artifice.sickle'] = ('Breaking leaves harvests all leaves within the radius; breaking a plant harvests all plants on the '
                               'same level within the radius.')
lang['jei.artifice.scaffolding'] = ('Walk against scaffolding to climb it, sneak to hold on. Left-click with the same scaffolding to '
                                    'stack one on top. Scaffolding needs support below or a rooted scaffold of the same kind within '
                                    'its maximum overhang.')
lang['jei.artifice.detector'] = 'Emits a short redstone pulse whenever an adjacent block changes.'
lang['jei.artifice.lotus'] = 'Opens in the light and closes in the dark (light level below 8).'
lang['jei.artifice.flowers'] = 'Grows on grass with bone meal.'
lang['jei.artifice.upgrade_type'] = 'Tool Upgrades'

# ---------------------------------------------------------------- write recipes, tags, lang, assets
for name, r in recipes.items():
    write(f'data/artifice/recipe/{name}.json', r)
for (reg, t), values in tags.items():
    ns, path = t.split(':')
    write(f'data/{ns}/tags/{reg}/{path}.json', {'values': values})
write('assets/artifice/lang/en_us.json', dict(sorted(lang.items())))
with open(ROOT + '/original-assets.txt', 'w') as f:
    f.write('# path in Artifice 1.1.2 (artifice.zip) -> path in the ported jar\n')
    seen = set()
    for s, d in assets:
        if d in seen: continue
        seen.add(d)
        f.write(f'{s} -> {d}\n')
print('recipes', len(recipes), 'tags', len(tags), 'lang', len(lang), 'assets', len(seen))
