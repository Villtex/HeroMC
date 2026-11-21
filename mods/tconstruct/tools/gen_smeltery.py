# Part 25c: the smeltery. Run by gen.py (exec, shares its helpers) before the language files are written.
import re
import xml.etree.ElementTree as ET

LANG_DIR = os.path.join(os.path.dirname(os.path.dirname(ROOT)), '.reference/tconstruct/res/mods/tinker/resources/lang/')


def load_lang(file):
    out = {}
    try:
        for entry in ET.parse(LANG_DIR + file).getroot().iter('entry'):
            out[entry.get('key')] = entry.text or ''
    except (OSError, ET.ParseError):
        pass
    return out


EN, DE = load_lang('en_US.xml'), load_lang('de_DE.xml')


def old_name(key, reg_key, en_fallback, de_fallback):
    name(reg_key, EN.get(key, en_fallback), DE.get(key, de_fallback))


def animated(src, dst):
    """A liquid texture with its frame list (<name>.txt: 'frame*time' per line)."""
    tex(f'blocks/{src}.png', f'block/{dst}')
    txt = os.path.join(REF, f'mods/tinker/textures/blocks/{src}.txt')
    frames = []
    if os.path.exists(txt):
        for line in open(txt).read().split():
            m = re.match(r'(\d+)\*(\d+)', line)
            if m:
                frames.append({'index': int(m.group(1)), 'time': int(m.group(2))})
            elif line.strip().isdigit():
                frames.append(int(line))
    write(f'assets/{NS}/textures/block/{dst}.png.mcmeta', {'animation': {'frames': frames}} if frames else {'animation': {}})
    return n(f'block/{dst}')


smelt_blocks = []  # (registry name, tool, drops itself)


def loot_self(reg):
    write(f'data/{NS}/loot_table/blocks/{reg}.json', {'type': 'minecraft:block', 'pools': [
        {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n(reg)}], 'condition': {'type': 'minecraft:survives_explosion'}}]})


def model(name_, parent, textures, elements=None):
    m = {'parent': parent, 'textures': textures}
    if elements is not None:
        m = {'textures': textures, 'elements': elements}
        if parent:
            m['parent'] = parent
    write(f'assets/{NS}/models/block/{name_}.json', m)


def simple_state(reg, model_=None):
    write(f'assets/{NS}/blockstates/{reg}.json', {'variants': {'': {'model': n(f'block/{model_ or reg}')}}})


def block_item(reg, model_=None):
    item_def(reg, n(f'block/{model_ or reg}'))


def cube_all(reg, texture):
    model(reg, 'minecraft:block/cube_all', {'all': texture})
    simple_state(reg)
    block_item(reg)


def el(f, t, tex_='#all', faces=('down', 'up', 'north', 'south', 'west', 'east')):
    return {'from': f, 'to': t, 'faces': {d: {'texture': tex_ if not isinstance(tex_, dict) else tex_[d]} for d in faces}}


ROT = {'north': 0, 'east': 90, 'south': 180, 'west': 270}

# ---------------------------------------------------------------- liquids (LiquidMetalBase, FilledBucket)
LIQUIDS = [  # registry name, texture, bucket texture, LiquidMetal key, bucket key
    ('molten_iron', 'iron', 'iron', 'Iron', 'Iron'), ('molten_gold', 'gold', 'gold', 'Gold', 'Gold'),
    ('molten_copper', 'copper', 'copper', 'Copper', 'Copper'), ('molten_tin', 'tin', 'tin', 'Tin', 'Tin'),
    ('molten_aluminum', 'aluminum', 'aluminum', 'Aluminum', 'Aluminum'), ('molten_cobalt', 'cobalt', 'cobalt', 'Cobalt', 'Cobalt'),
    ('molten_ardite', 'ardite', 'ardite', 'Ardite', 'Ardite'), ('molten_bronze', 'bronze', 'bronze', 'Bronze', 'Bronze'),
    ('molten_alubrass', 'alubrass', 'alubrass', 'AluBrass', 'AluBrass'), ('molten_manyullyn', 'manyullyn', 'manyullyn', 'Manyullyn', 'Manyullyn'),
    ('molten_alumite', 'alumite', 'alumite', 'Alumite', 'Alumite'), ('molten_obsidian', 'obsidian', 'obsidian', 'Obsidian', 'Obsidian'),
    ('molten_steel', 'steel', 'steel', 'Steel', 'Steel'), ('molten_glass', 'glass', 'glass', 'Glass', 'Glass'),
    ('liquid_seared_stone', 'stone', 'stone', 'Stone', 'Stone'), ('molten_emerald', 'villager', 'emerald', 'Villager', 'Villager'),
    ('blood', 'cow', 'blood', 'Cow', 'Cow'), ('molten_nickel', 'ferrous', 'nickel', 'Nickel', 'Nickel'),
    ('molten_lead', 'lead', 'lead', 'Lead', 'Lead'), ('molten_silver', 'silver', 'silver', 'Silver', 'Silver'),
    ('molten_platinum', 'shiny', 'shiny', 'Shiny', 'Shiny'), ('molten_invar', 'invar', 'invar', 'Invar', 'Invar'),
    ('molten_electrum', 'electrum', 'electrum', 'Electrum', 'Electrum'), ('liquid_ender', 'ender', 'ender', 'Ender', 'Ender')]
for reg, t, bt, key, bkey in LIQUIDS:
    still = animated(f'liquid_{t}', f'liquid_{t}')
    animated(f'liquid_{t}_flow', f'liquid_{t}_flow')
    write(f'assets/{NS}/models/block/{reg}.json', {'textures': {'particle': still}})
    write(f'assets/{NS}/blockstates/{reg}.json', {'variants': {'': {'model': n(f'block/{reg}')}}})
    e, g = EN.get(f'LiquidMetal.{key}.name', reg), DE.get(f'LiquidMetal.{key}.name', EN.get(f'LiquidMetal.{key}.name', reg))
    name(f'block.{NS}.{reg}', e, g)
    name(f'fluid.{NS}.{reg}', e, g)
    flat(f'{reg}_bucket', [tex(f'items/materials/bucket_{bt}.png', f'item/materials/bucket_{bt}')])
    old_name(f'item.tconstruct.bucket.{bkey}.name', f'item.{NS}.{reg}_bucket', e + ' Bucket', g + ' (Eimer)')

# ---------------------------------------------------------------- the smeltery (SmelteryBlock)
side = block_tex('smeltery_side')
for lit, front in ((False, 'smeltery_inactive'), (True, 'smeltery_active')):
    model(f'smeltery_controller{"_on" if lit else ""}', 'minecraft:block/orientable',
          {'top': side, 'side': side, 'front': block_tex(front), 'bottom': side})
write(f'assets/{NS}/blockstates/smeltery_controller.json', {'variants': {
    f'facing={f},lit={str(l).lower()}': {'model': n('block/smeltery_controller' + ('_on' if l else '')), **({'y': r} if r else {})}
    for f, r in ROT.items() for l in (False, True)}})
block_item('smeltery_controller')
old_name('Smeltery.Controller.name', f'block.{NS}.smeltery_controller', 'Smeltery Controller', 'Schmelzereikern')
smelt_blocks.append(('smeltery_controller', 'pickaxe'))
# drain: the front (facing) shows the basin, the back the outlet, the other sides drain_side
model('smeltery_drain', 'minecraft:block/cube', {'north': block_tex('drain_basin'), 'south': block_tex('drain_out'), 'east': block_tex('drain_side'),
                                                 'west': block_tex('drain_side'), 'up': block_tex('drain_side'), 'down': block_tex('drain_side'),
                                                 'particle': block_tex('drain_side')})
drain_rot = {'north': {}, 'east': {'y': 90}, 'south': {'y': 180}, 'west': {'y': 270}, 'up': {'x': 270}, 'down': {'x': 90}}
write(f'assets/{NS}/blockstates/smeltery_drain.json', {'variants': {f'facing={f}': {'model': n('block/smeltery_drain'), **r} for f, r in drain_rot.items()}})
block_item('smeltery_drain')
old_name('Smeltery.Drain.name', f'block.{NS}.smeltery_drain', 'Smeltery Drain', 'Schmelzereiausguss')
smelt_blocks.append(('smeltery_drain', 'pickaxe'))
SEARED = [('seared_bricks', 'searedbrick', 'Smeltery.Brick'), ('seared_stone', 'searedstone', 'Smeltery.Stone'),
          ('seared_cobblestone', 'searedcobble', 'Smeltery.Cobblestone'), ('seared_paver', 'searedpaver', 'Smeltery.Paver'),
          ('cracked_seared_bricks', 'searedbrickcracked', 'Smeltery.Brick.Cracked'), ('seared_road', 'searedroad', 'Smeltery.Road'),
          ('fancy_seared_bricks', 'searedbrickfancy', 'Smeltery.Brick.Fancy'), ('square_seared_bricks', 'searedbricksquare', 'Smeltery.Brick.Square'),
          ('chiseled_seared_bricks', 'searedcreeper', 'Smeltery.Brick.Creeper')]
for reg, t, key in SEARED:
    if reg == 'chiseled_seared_bricks':
        model(reg, 'minecraft:block/cube_bottom_top', {'side': block_tex(t), 'top': block_tex('searedpaver'), 'bottom': block_tex('searedpaver')})
        simple_state(reg)
        block_item(reg)
    else:
        cube_all(reg, block_tex(t))
    old_name(f'{key}.name', f'block.{NS}.{reg}', reg, reg)
    smelt_blocks.append((reg, 'pickaxe'))
# seared slabs (SearedSlab)
SLABS = [('seared_brick_slab', 'seared_bricks', 'brick'), ('seared_stone_slab', 'seared_stone', 'stone'),
         ('seared_cobblestone_slab', 'seared_cobblestone', 'cobble'), ('seared_paver_slab', 'seared_paver', 'paver'),
         ('seared_road_slab', 'seared_road', 'road'), ('fancy_seared_slab', 'fancy_seared_bricks', 'fancy'),
         ('square_seared_slab', 'square_seared_bricks', 'square'), ('chiseled_seared_slab', 'chiseled_seared_bricks', 'creeper')]
SEARED_TEX = {r: t for r, t, _ in SEARED}
for reg, full, key in SLABS:
    s = block_tex(SEARED_TEX[full])
    top = block_tex('searedpaver') if full == 'chiseled_seared_bricks' else s
    model(reg, 'minecraft:block/slab', {'bottom': top, 'top': top, 'side': s})
    model(reg + '_top', 'minecraft:block/slab_top', {'bottom': top, 'top': top, 'side': s})
    write(f'assets/{NS}/blockstates/{reg}.json', {'variants': {'type=bottom': {'model': n(f'block/{reg}')}, 'type=top': {'model': n(f'block/{reg}_top')},
                                                               'type=double': {'model': n(f'block/{full}')}}})
    block_item(reg)
    old_name(f'block.searedstone.slab.{key}.name', f'block.{NS}.{reg}', reg, reg)
    write(f'data/{NS}/loot_table/blocks/{reg}.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{
        'type': 'minecraft:item', 'name': n(reg), 'modifier': [{'type': 'minecraft:set_count', 'count': 2, 'condition': {
            'type': 'minecraft:match_block', 'blocks': n(reg), 'state': {'type': 'double'}}}, {'type': 'minecraft:explosion_decay'}]}]}]})
    smelt_blocks.append((reg, 'pickaxe-noloot'))
write('data/minecraft/tags/block/slabs.json', {'replace': False, 'values': [n(r) for r, _, _ in SLABS]})
write('data/minecraft/tags/item/slabs.json', {'replace': False, 'values': [n(r) for r, _, _ in SLABS]})

# ---------------------------------------------------------------- seared tanks (LavaTankBlock), liquid kept on the item
for reg, top, sd, bottom, key in (('seared_tank', 'lavatank_top', 'lavatank_side', 'lavatank_top', 'LavaTank.Tank'),
                                  ('seared_glass', 'searedgague_top', 'searedgague_side', 'searedgague_bottom', 'LavaTank.Gague'),
                                  ('seared_window', 'searedwindow_top', 'searedwindow_side', 'searedwindow_bottom', 'LavaTank.Window')):
    model(reg, 'minecraft:block/cube_bottom_top', {'top': block_tex(top), 'side': block_tex(sd), 'bottom': block_tex(bottom)})
    simple_state(reg)
    block_item(reg)
    old_name(f'{key}.name', f'block.{NS}.{reg}', reg, reg)
    write(f'data/{NS}/loot_table/blocks/{reg}.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{
        'type': 'minecraft:item', 'name': n(reg), 'modifier': {'type': 'minecraft:copy_components', 'source': 'block_entity',
                                                                 'include': [n('liquid')]}}], 'condition': {'type': 'minecraft:survives_explosion'}}]})
    smelt_blocks.append((reg, 'pickaxe-noloot'))
name(f'tooltip.{NS}.contains', 'Contains %s', 'Enthält %s')
name(f'tooltip.{NS}.mb', '%s mB', '%s mB')

# ---------------------------------------------------------------- casting table, faucet, basin (SearedBlock, SearedRender)
tt = {'top': block_tex('castingtable_top'), 'side': block_tex('castingtable_side'), 'bottom': block_tex('castingtable_bottom')}
TF = {'up': '#top', 'down': '#bottom', 'north': '#side', 'south': '#side', 'west': '#side', 'east': '#side'}
table_boxes = [([0, 10, 0], [16, 15, 16]), ([0, 15, 0], [1, 16, 16]), ([1, 15, 15], [15, 16, 16]), ([15, 15, 0], [16, 16, 16]), ([1, 15, 0], [15, 16, 1]),
               ([0, 0, 0], [5, 10, 5]), ([11, 0, 0], [16, 10, 4]), ([0, 0, 11], [5, 10, 16]), ([11, 0, 11], [16, 10, 16])]
model('casting_table', None, {**tt, 'particle': tt['side']}, [el(f, t, TF) for f, t in table_boxes])
simple_state('casting_table')
block_item('casting_table')
old_name('SearedBlock.Table.name', f'block.{NS}.casting_table', 'Casting Table', 'Gusstisch')
bt = {'top': block_tex('blockcast_top'), 'side': block_tex('blockcast_side'), 'bottom': block_tex('blockcast_bottom')}
basin_boxes = [([2, 2, 2], [14, 4, 14]), ([0, 2, 0], [2, 16, 16]), ([14, 2, 0], [16, 16, 16]), ([2, 2, 0], [14, 16, 2]), ([2, 2, 14], [14, 16, 16]),
               ([0, 0, 0], [5, 2, 5]), ([11, 0, 0], [16, 2, 5]), ([0, 0, 11], [5, 2, 16]), ([11, 0, 11], [16, 2, 16])]
model('casting_basin', None, {**bt, 'particle': bt['side']}, [el(f, t, TF) for f, t in basin_boxes])
simple_state('casting_basin')
block_item('casting_basin')
old_name('SearedBlock.Basin.name', f'block.{NS}.casting_basin', 'Casting Basin', 'Gussbecken')
fau = block_tex('faucet')
faucet_boxes = [([4, 4, 10], [12, 6, 16]), ([4, 6, 10], [6, 10, 16]), ([10, 6, 10], [12, 10, 16])]
model('seared_faucet', None, {'all': fau, 'particle': fau}, [el(f, t) for f, t in faucet_boxes])
write(f'assets/{NS}/blockstates/seared_faucet.json', {'variants': {f'facing={f}': {'model': n('block/seared_faucet'), **({'y': r} if r else {})}
                                                                    for f, r in ROT.items()}})
block_item('seared_faucet')
old_name('SearedBlock.Faucet.name', f'block.{NS}.seared_faucet', 'Seared Faucet', 'Zapfhahn')
for r in ('casting_table', 'casting_basin', 'seared_faucet'):
    smelt_blocks.append((r, 'pickaxe'))

# ---------------------------------------------------------------- casting channel (a gutter reaching out to tanks)
st = block_tex('searedstone')
model('casting_channel_center', None, {'all': st, 'particle': st}, [el([5, 6, 5], [11, 7, 11]), el([5, 7, 5], [6, 10, 11]), el([10, 7, 5], [11, 10, 11]),
                                                                    el([6, 7, 5], [10, 10, 6]), el([6, 7, 10], [10, 10, 11])])
model('casting_channel_side', None, {'all': st, 'particle': st}, [el([5, 6, 0], [11, 7, 5]), el([5, 7, 0], [6, 10, 5]), el([10, 7, 0], [11, 10, 5])])
write(f'assets/{NS}/blockstates/casting_channel.json', {'multipart': [{'apply': {'model': n('block/casting_channel_center')}}] + [
    {'when': {d: 'true'}, 'apply': {'model': n('block/casting_channel_side'), **({'y': r} if r else {})}} for d, r in ROT.items()]})
item_def('casting_channel', n('block/casting_channel_center'))
old_name('Smeltery.Channel.name', f'block.{NS}.casting_channel', 'Casting Channel', 'Gusskanal')
smelt_blocks.append(('casting_channel', 'pickaxe'))

# ---------------------------------------------------------------- drying rack (oak planks bar)
plank = M + 'block/oak_planks'
RACK = [([0, 0, 6], [16, 4, 10]), ([6, 0, 0], [10, 4, 16]), ([0, 12, 12], [16, 16, 16]), ([0, 12, 0], [16, 16, 4]), ([12, 12, 0], [16, 16, 16]),
        ([0, 12, 0], [4, 16, 16])]
for i, (f, t) in enumerate(RACK):
    model(f'drying_rack_{i}', None, {'all': plank, 'particle': plank}, [el(f, t)])
write(f'assets/{NS}/blockstates/drying_rack.json', {'variants': {f'meta={i}': {'model': n(f'block/drying_rack_{i}')} for i in range(6)}})
item_def('drying_rack', n('block/drying_rack_0'))
old_name('tile.Armor.DryingRack.name', f'block.{NS}.drying_rack', 'Drying Rack', 'Trockengestell')
smelt_blocks.append(('drying_rack', 'pickaxe'))

# ---------------------------------------------------------------- metal blocks, ores, gravel ores, soils
METALS = [('cobalt', 'Cobalt'), ('ardite', 'Ardite'), ('manyullyn', 'Manyullyn'), ('copper', 'Copper'), ('bronze', 'Bronze'), ('tin', 'Tin'),
          ('aluminum', 'Aluminum'), ('alubrass', 'AlBrass'), ('alumite', 'Alumite'), ('steel', 'Steel'), ('ender', 'Ender')]
for m, key in METALS:
    cube_all(f'{m}_block', block_tex(f'compressed_{m}'))
    old_name(f'StorageMetals.{key}.name', f'block.{NS}.{m}_block', m, m)
    smelt_blocks.append((f'{m}_block', 'pickaxe'))
write('data/minecraft/tags/block/beacon_base_blocks.json', {'replace': False, 'values': [n(f'{m}_block') for m, _ in METALS]})
ORES = [('nether_slag', 'nether_slag', 'NetherSlag', 0), ('cobalt_ore', 'nether_cobalt', 'Cobalt', 4), ('ardite_ore', 'nether_ardite', 'Ardite', 4),
        ('copper_ore', 'ore_copper', 'Copper', 1), ('tin_ore', 'ore_tin', 'Tin', 1), ('aluminum_ore', 'ore_aluminum', 'Aluminum', 1),
        ('stone_slag', 'ore_slag', 'Slag', 0)]
GRAVEL = [('iron', 1), ('gold', 2), ('copper', 1), ('tin', 1), ('aluminum', 1), ('cobalt', 4)]
needs = {1: [], 2: [], 4: []}
for reg, t, key, lvl in ORES:
    cube_all(reg, block_tex(t))
    old_name(f'MetalOre.{key}.name', f'block.{NS}.{reg}', reg, reg)
    smelt_blocks.append((reg, 'pickaxe'))
    if lvl:
        needs[lvl].append(n(reg))
for m, lvl in GRAVEL:
    reg = f'{m}_gravel_ore'
    cube_all(reg, block_tex(f'ore_{m}_gravel'))
    old_name(f'block.ore.gravel.{m}.name', f'block.{NS}.{reg}', reg, reg)
    smelt_blocks.append((reg, 'shovel'))
    needs[lvl].append(n(reg))
write('data/minecraft/tags/block/needs_stone_tool.json', {'replace': False, 'values': needs[1]})
write('data/minecraft/tags/block/needs_iron_tool.json', {'replace': False, 'values': needs[2]})
write(f'data/{NS}/tags/block/needs_cobalt_tool.json', {'values': needs[4]})
for tool in ('wooden', 'stone', 'copper', 'iron', 'golden', 'diamond'):
    write(f'data/minecraft/tags/block/incorrect_for_{tool}_tool.json', {'replace': False, 'values': [f'#{NS}:needs_cobalt_tool']})
SOILS = [('slimy_mud', 'slimesand', 'Slime'), ('grout', 'grout', 'Grout'), ('blue_slimy_mud', 'slimesandblue', 'BlueSlime'),
         ('graveyard_soil', 'graveyardsoil', 'GraveyardSoil'), ('consecrated_soil', 'consecratedsoil', 'ConsecratedSoil'),
         ('blue_slime_dirt', 'slimedirt_blue', 'BlueSlime')]
for reg, t, key in SOILS:
    cube_all(reg, block_tex(t))
    old_name(f'CraftedSoil.{key}.name', f'block.{NS}.{reg}', reg, reg)
    smelt_blocks.append((reg, 'shovel'))
name(f'block.{NS}.blue_slime_dirt', 'Slimy Dirt', 'Schleimige Erde')

for reg, tool in smelt_blocks:
    if not tool.endswith('-noloot'):
        loot_self(reg)
    blocks.append((reg, tool.split('-')[0]))
write('data/minecraft/tags/block/mineable/axe.json', {'replace': False, 'values': [n(r) for r, t in blocks if t == 'axe']})
write('data/minecraft/tags/block/mineable/pickaxe.json', {'replace': False, 'values': [n(r) for r, t in blocks if t == 'pickaxe']})
write('data/minecraft/tags/block/mineable/shovel.json', {'replace': False, 'values': [n(r) for r, t in blocks if t == 'shovel']})

# ---------------------------------------------------------------- tags (ore dictionary of 1.5.2)
for tag, values in (('ores/cobalt', ['cobalt_ore', 'cobalt_gravel_ore']), ('ores/ardite', ['ardite_ore']),
                    ('ores/copper', ['copper_ore', 'copper_gravel_ore']), ('ores/tin', ['tin_ore', 'tin_gravel_ore']),
                    ('ores/aluminum', ['aluminum_ore', 'aluminum_gravel_ore']), ('ores/iron', ['iron_gravel_ore']), ('ores/gold', ['gold_gravel_ore'])):
    for kind in ('block', 'item'):
        write(f'data/c/tags/{kind}/{tag}.json', {'replace': False, 'values': [n(v) for v in values]})
STORAGE = {'cobalt': 'cobalt', 'ardite': 'ardite', 'manyullyn': 'manyullyn', 'copper': 'copper', 'bronze': 'bronze', 'tin': 'tin',
           'aluminum': 'aluminum', 'aluminum_brass': 'alubrass', 'alumite': 'alumite', 'steel': 'steel'}
for tag, m in STORAGE.items():
    for kind in ('block', 'item'):
        write(f'data/c/tags/{kind}/storage_blocks/{tag}.json', {'replace': False, 'values': [n(f'{m}_block')]})
write('data/c/tags/item/ingots/aluminum_brass.json', {'values': [n('alubrass_ingot')]})
write('data/c/tags/item/ingots/alumite.json', {'values': [n('alumite_ingot')]})
write('data/c/tags/item/ingots/obsidian.json', {'values': [n('obsidian_ingot')]})

# ---------------------------------------------------------------- recipes
SB = n('seared_brick')
G = [M + 'glass', n('clear_glass')]  # vanilla or clear glass (part 25f)
shaped('smeltery_controller', ['bbb', 'b b', 'bbb'], {'b': SB}, 'smeltery_controller', category='building')
shaped('smeltery_drain', ['b b', 'b b', 'b b'], {'b': SB}, 'smeltery_drain', category='building')
shaped('seared_bricks', ['bb', 'bb'], {'b': SB}, 'seared_bricks', category='building')
shaped('seared_tank', ['bbb', 'bgb', 'bbb'], {'b': SB, 'g': G}, 'seared_tank', category='building')
shaped('seared_glass', ['bgb', 'ggg', 'bgb'], {'b': SB, 'g': G}, 'seared_glass', category='building')
shaped('seared_window', ['bgb', 'bgb', 'bgb'], {'b': SB, 'g': G}, 'seared_window', category='building')
shaped('casting_table', ['bbb', 'b b', 'b b'], {'b': SB}, 'casting_table', category='building')
shaped('seared_faucet', ['b b', ' b '], {'b': SB}, 'seared_faucet', category='building')
shaped('casting_basin', ['b b', 'b b', 'bbb'], {'b': SB}, 'casting_basin', category='building')
shaped('casting_channel', ['b b', 'bbb'], {'b': SB}, 'casting_channel', 4, category='building')
for reg, full, _ in SLABS:
    shaped(reg, ['bbb'], {'b': n(full)}, reg, 6, category='building')
for ingot, m in (('copper_ingot', 'copper'), ('tin_ingot', 'tin'), ('raw_aluminum', 'aluminum'), ('bronze_ingot', 'bronze'),
                 ('alubrass_ingot', 'alubrass'), ('cobalt_ingot', 'cobalt'), ('ardite_ingot', 'ardite'), ('manyullyn_ingot', 'manyullyn'),
                 ('alumite_ingot', 'alumite'), ('steel_ingot', 'steel')):
    shaped(f'{ingot}_from_{m}_block', ['m'], {'m': n(f'{m}_block')}, ingot, 9)
FORGE_BLOCKS = {'iron': M + 'iron_block', 'gold': M + 'gold_block', 'diamond': M + 'diamond_block', 'emerald': M + 'emerald_block',
                'cobalt': '#c:storage_blocks/cobalt', 'ardite': '#c:storage_blocks/ardite', 'manyullyn': '#c:storage_blocks/manyullyn',
                'copper': '#c:storage_blocks/copper', 'bronze': '#c:storage_blocks/bronze', 'tin': '#c:storage_blocks/tin',
                'aluminum': '#c:storage_blocks/aluminum', 'alubrass': '#c:storage_blocks/aluminum_brass', 'alumite': '#c:storage_blocks/alumite',
                'steel': '#c:storage_blocks/steel'}
for m, b in FORGE_BLOCKS.items():
    shaped(f'{m}_tool_forge', ['bbb', 'msm', 'm m'], {'b': n('seared_bricks'), 's': n('tool_station'), 'm': b}, f'{m}_tool_forge', category='building')
shapeless('grout', [M + 'clay_ball', M + 'sand', M + 'gravel'], 'grout', 2, category='building')
shapeless('grout_from_clay', [M + 'clay', M + 'sand', M + 'gravel', M + 'sand', M + 'gravel', M + 'sand', M + 'gravel', M + 'sand', M + 'gravel'],
          'grout', 8, category='building')
shapeless('slimy_mud', [M + 'slime_ball'] * 4 + [M + 'sand', M + 'dirt'], 'slimy_mud', category='building')
shapeless('graveyard_soil', [M + 'dirt', M + 'rotten_flesh', M + 'bone_meal'], 'graveyard_soil', category='building')


def smelting(file, ingredient, out, xp, time=200):
    write(f'data/{NS}/recipe/{file}.json', {'type': 'minecraft:smelting', 'category': 'misc', 'ingredient': ingredient,
                                            'result': result(out), 'experience': xp, 'cookingtime': time})


smelting('seared_brick', n('grout'), 'seared_brick', 2.0)
smelting('slime_crystal', n('slimy_mud'), 'slime_crystal', 2.0)
smelting('blue_slime_crystal', n('blue_slimy_mud'), 'blue_slime_crystal', 2.0)
smelting('consecrated_soil', n('graveyard_soil'), 'consecrated_soil', 0.2)
smelting('copper_ingot_from_ore', n('copper_ore'), 'copper_ingot', 0.5)
smelting('tin_ingot_from_ore', n('tin_ore'), 'tin_ingot', 0.5)
smelting('raw_aluminum_from_ore', n('aluminum_ore'), 'raw_aluminum', 0.5)
for m, out in (('iron', M + 'iron_ingot'), ('gold', M + 'gold_ingot'), ('copper', 'copper_ingot'), ('tin', 'tin_ingot'), ('aluminum', 'raw_aluminum')):
    smelting(f'{m}_from_gravel_ore', n(f'{m}_gravel_ore'), out, 0.2)

# ---------------------------------------------------------------- damage (SmelteryDamageSource) and GUI
for i, k in enumerate(('one', 'two', 'three', 'four'), 1):
    write(f'data/{NS}/damage_type/smeltery_{i}.json', {'message_id': f'{NS}.smeltery{i}', 'exhaustion': 0.1, 'scaling': 'when_caused_by_living_non_player'})
    old_name(f'death.{k}.smeltery', f'death.attack.{NS}.smeltery{i}', '%1$s melted', '%1$s ist geschmolzen')
tex('gui/smeltery.png', 'gui/smeltery')
tex('gui/smelteryside.png', 'gui/smelteryside')
old_name('crafters.Smeltery', f'container.{NS}.smeltery', 'Smeltery', 'Schmelzerei')
for key, e, g in (('fuel', 'Fuel', 'Brennstoff'), ('mb', 'mB: %s', 'mB: %s'), ('ingots', 'Ingots: %s', 'Barren: %s'), ('chunks', 'Chunks: %s', 'Brocken: %s'),
                  ('nuggets', 'Nuggets: %s', 'Klumpen: %s'), ('blocks', 'Blocks: %s', 'Blöcke: %s'), ('emeralds', 'Emeralds: %s', 'Smaragde: %s')):
    name(f'gui.{NS}.smeltery.{key}', e, g)
for key, e, g in (('melting', 'Smeltery', 'Schmelzerei'), ('alloy', 'Alloying', 'Legieren'), ('table', 'Casting Table', 'Gusstisch'),
                  ('basin', 'Casting Basin', 'Gussbecken'), ('drying', 'Drying Rack', 'Trockengestell'), ('temperature', 'Melts at %s°', 'Schmilzt bei %s°'),
                  ('cool', 'Cools in %s s', 'Kühlt %s s ab'), ('consumed', 'Cast is used up', 'Gussform wird verbraucht'),
                  ('time', 'Takes %s s', 'Dauert %s s')):
    name(f'jei.{NS}.{key}', e, g)
