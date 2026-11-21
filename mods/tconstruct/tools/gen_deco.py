# Part 25f: decoration, traps, redstone machines, essence extractor. Run by gen.py (exec, after gen_player.py, whose and
# gen_smeltery.py's helpers it uses) before the language files are written.

deco_blocks = []  # (registry name, tool tag or None)
COLORS = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green',
          'red', 'black']
OLD_COLORS = ['white', 'orange', 'magenta', 'lightblue', 'yellow', 'lime', 'pink', 'gray', 'lightgray', 'cyan', 'purple', 'blue', 'brown', 'green',
              'red', 'black']
CLOTH = ['white', 'orange', 'magenta', 'lightBlue', 'yellow', 'lime', 'pink', 'gray', 'silver', 'cyan', 'purple', 'blue', 'brown', 'green', 'red',
         'black']
FACES = ['down', 'up', 'north', 'south', 'west', 'east']
OFFSET = {(-1, 0, 0): 'west', (1, 0, 0): 'east', (0, -1, 0): 'down', (0, 1, 0): 'up', (0, 0, -1): 'north', (0, 0, 1): 'south'}


def tag_add(path, values):
    """Adds to a tag file the earlier parts already wrote."""
    full = os.path.join(RES, path)
    old = json.load(open(full))['values'] if os.path.exists(full) else []
    write(path, {'replace': False, 'values': old + [v for v in values if v not in old]})


def translucent(t):
    return {'force_translucent': True, 'sprite': t}


def slab_loot(reg):
    write(f'data/{NS}/loot_table/blocks/{reg}.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{
        'type': 'minecraft:item', 'name': n(reg), 'modifier': [
            {'type': 'minecraft:set_count', 'count': 2, 'condition': {'type': 'minecraft:match_block', 'blocks': n(reg), 'state': {'type': 'double'}}},
            {'type': 'minecraft:explosion_decay'}]}]}]})


def slab(reg, full_model, textures):
    """A vanilla slab of a full block (bottom, top, double) with the textures of the full block."""
    write(f'assets/{NS}/models/block/{reg}.json', {'parent': 'minecraft:block/slab', 'textures': textures})
    write(f'assets/{NS}/models/block/{reg}_top.json', {'parent': 'minecraft:block/slab_top', 'textures': textures})
    write(f'assets/{NS}/blockstates/{reg}.json', {'variants': {'type=bottom': {'model': n(f'block/{reg}')}, 'type=top': {'model': n(f'block/{reg}_top')},
                                                               'type=double': {'model': full_model}}})
    block_item(reg)
    slab_loot(reg)


# ---------------------------------------------------------------- connected glass (GlassBlockConnected.getConnectedBlockTexture)
GLASS_ICONS = ['glass', 'glass_1_d', 'glass_1_u', 'glass_1_l', 'glass_1_r', 'glass_2_h', 'glass_2_v', 'glass_2_dl', 'glass_2_dr', 'glass_2_ul',
               'glass_2_ur', 'glass_3_d', 'glass_3_u', 'glass_3_l', 'glass_3_r', 'glass_4']


def glass_table():
    """Per face: the four neighbour directions (Down, Up, Left, Right of 1.5.2) and the icon for each of the 16 combinations,
    read from the if chains of the original source."""
    src = open(os.path.join(os.path.dirname(REF.rstrip('/')), 'src/mods/tinker/tconstruct/blocks/decorative/GlassBlockConnected.java')).read()
    body = src[src.index('getConnectedBlockTexture'):src.index('shouldSideBeRendered')]
    parts = re.split(r'\n         case (\d):', body)
    table = {}
    for i in range(1, len(parts), 2):
        side, txt = int(parts[i]), parts[i + 1]
        dirs = {}
        for flag, args in zip(re.findall(r'(isOpen\w+) = true', txt), re.findall(r'getBlockId\(([^)]*)\)', txt)):
            off = []
            for a in args.split(','):
                a = a.strip()
                off.append(1 if '+ 1' in a else -1 if '- 1' in a else 0)
            dirs[flag] = OFFSET[tuple(off)]
        chain = [(cond.split(' && '), int(icon)) for cond, icon in re.findall(r'if \(((?:isOpen\w+(?: && )?)+)\) \{\n\s*return icons\[(\d+)\]', txt)]
        flags = ['isOpenDown', 'isOpenUp', 'isOpenLeft', 'isOpenRight']
        combos = {}
        for mask in range(16):
            on = {f for k, f in enumerate(flags) if mask >> k & 1}
            icon = 0
            for cond, ic in chain:
                if all(c in on for c in cond):
                    icon = ic
                    break
            combos[tuple(sorted(dirs[f] for f in on))] = icon
        table[FACES[side]] = ([dirs[f] for f in flags], combos)
    return table


GLASS_TABLE = glass_table()
# the down face of 1.5.2 ran its v along +z, the model's default along -z
FACE_UV = {'down': [0, 16, 16, 0]}


def connected_glass(reg, folder, stained):
    """Multipart: per face and per combination of its four in-plane neighbours one single-face model; a face next to the
    same glass is left out."""
    textures = {}
    for icon in GLASS_ICONS:
        textures[icon] = block_tex(f'glass/{folder}/{icon}')
    parts = []
    used = set()
    for face, (dirs, combos) in GLASS_TABLE.items():
        for combo, icon in combos.items():
            used.add((face, icon))
            when = {face: 'false'}
            for d in set(dirs):
                when[d] = 'true' if d in combo else 'false'
            parts.append({'when': when, 'apply': {'model': n(f'block/{reg}_{face}_{icon}')}})
    for face, icon in used:
        f = {'texture': '#glass', 'cullface': face}
        if face in FACE_UV:
            f['uv'] = FACE_UV[face]
        t = textures[GLASS_ICONS[icon]]
        write(f'assets/{NS}/models/block/{reg}_{face}_{icon}.json', {
            'parent': 'minecraft:block/block', 'textures': {'glass': translucent(t) if stained else t, 'particle': textures['glass']},
            'elements': [{'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': {face: f}}]})
    write(f'assets/{NS}/blockstates/{reg}.json', {'multipart': parts})
    t = textures['glass']
    model(f'{reg}_inventory', 'minecraft:block/cube_all', {'all': translucent(t) if stained else t})
    block_item(reg, f'{reg}_inventory')
    loot_self(reg)
    deco_blocks.append((reg, None))


connected_glass('clear_glass', 'clear', False)
old_name('block.glass.pure.name', f'block.{NS}.clear_glass', 'Clear Glass', 'Klares Glas')
for i, c in enumerate(COLORS):
    connected_glass(f'{c}_clear_stained_glass', f'stained/{c}', True)
    old_name(f'block.stainedglass.{OLD_COLORS[i]}.name', f'block.{NS}.{c}_clear_stained_glass', c, c)


# ---------------------------------------------------------------- panes (PaneBase: <texture> and <texture>_side)
def pane(reg, texture, stained):
    pane_t = block_tex(f'glass/{texture}')
    edge_t = block_tex(f'glass/{texture}_side')
    tx = {'pane': translucent(pane_t) if stained else pane_t, 'edge': translucent(edge_t) if stained else edge_t}
    for part in ('post', 'side', 'side_alt', 'noside', 'noside_alt'):
        model(f'{reg}_{part}', f'minecraft:block/template_glass_pane_{part}', tx)
    m = lambda p: n(f'block/{reg}_{p}')
    write(f'assets/{NS}/blockstates/{reg}.json', {'multipart': [
        {'apply': {'model': m('post')}},
        {'apply': {'model': m('side')}, 'when': {'north': 'true'}}, {'apply': {'model': m('side'), 'y': 90}, 'when': {'east': 'true'}},
        {'apply': {'model': m('side_alt')}, 'when': {'south': 'true'}}, {'apply': {'model': m('side_alt'), 'y': 90}, 'when': {'west': 'true'}},
        {'apply': {'model': m('noside')}, 'when': {'north': 'false'}}, {'apply': {'model': m('noside_alt')}, 'when': {'east': 'false'}},
        {'apply': {'model': m('noside_alt'), 'y': 90}, 'when': {'south': 'false'}}, {'apply': {'model': m('noside'), 'y': 270}, 'when': {'west': 'false'}}]})
    flat(reg, [pane_t])
    loot_self(reg)
    deco_blocks.append((reg, None))


for reg, texture, key, e, g in (('clear_glass_pane', 'glass_clear', 'glass.pure', 'Clear Pane', 'Klare Scheibe'),
                                ('soul_glass_pane', 'soulglass', 'glass.soul', 'Soul Pane', 'Seelenglasscheibe'),
                                ('clear_soul_glass_pane', 'soulglass_clear', 'glass.soul.pure', 'Clear Soul Pane', 'Klare Seelenglasscheibe')):
    pane(reg, texture, False)
    old_name(f'block.{key}.pane.name', f'block.{NS}.{reg}', e, g)
for i, c in enumerate(COLORS):
    pane(f'{c}_clear_stained_glass_pane', f'stainedglass_{OLD_COLORS[i]}', True)
    old_name(f'block.stainedglass.{OLD_COLORS[i]}.pane.name', f'block.{NS}.{c}_clear_stained_glass_pane', c, c)

# ---------------------------------------------------------------- multi bricks (MultiBrick, MultiBrickFancy)
BRICKS = [('obsidian', 'obsidian'), ('sandstone', 'sandstone'), ('netherrack', 'netherrack'), ('polished_stone', 'stone_refined'), ('iron', 'iron'),
          ('gold', 'gold'), ('lapis', 'lapis'), ('diamond', 'diamond'), ('redstone', 'redstone'), ('bone', 'bone'), ('slime', 'slime'),
          ('blue_slime', 'blueslime'), ('endstone', 'endstone'), ('obsidian_ingot', 'obsidian_ingot')]
for prefix, tex_prefix, key in (('', 'brick_', 'brick'), ('fancy_', 'fancybrick_', 'fancybrick')):
    for meta, (reg, t) in enumerate(BRICKS):
        name_ = f'{prefix}{reg}_brick'
        texture = block_tex(f'bricks/{tex_prefix}{t}')
        if meta in (10, 11):  # the slime bricks: 10 pixels high
            model(name_, 'minecraft:block/block', {'all': texture, 'particle': texture}, [el([0, 0, 0], [16, 10, 16])])
            simple_state(name_)
            block_item(name_)
        else:
            cube_all(name_, texture)
        old_name(f'block.{key}.{t.replace("_", ".")}.name', f'block.{NS}.{name_}', name_, name_)
        loot_self(name_)
        deco_blocks.append((name_, 'pickaxe'))
for name_, t, e, g in (('fancy_stone_brick', 'fancybrick_stone', 'Fancy Stone Brick', 'Verzierter Steinziegel'),
                       ('stone_road', 'road_stone', 'Stone Road', 'Steinstraße')):
    cube_all(name_, block_tex(f'bricks/{t}'))
    loot_self(name_)
    deco_blocks.append((name_, 'pickaxe'))
old_name('block.fancybrick.stone.name', f'block.{NS}.fancy_stone_brick', 'Fancy Stone Brick', 'Verzierter Steinziegel')
old_name('block.fancybrick.stone.road.name', f'block.{NS}.stone_road', 'Stone Road', 'Steinstraße')
# the second (blue) slime brick has the same name as the first in 1.5.2
name(f'block.{NS}.blue_slime_brick', 'Blue Slime Brick', 'Blauer Schleimziegel')
name(f'block.{NS}.fancy_blue_slime_brick', 'Fancy Blue Slime Brick', 'Verzierter blauer Schleimziegel')
name(f'block.{NS}.obsidian_ingot_brick', 'Obsidian Ingot Brick', 'Obsidianbarrenziegel')
name(f'block.{NS}.fancy_obsidian_ingot_brick', 'Fancy Obsidian Ingot Brick', 'Verzierter Obsidianbarrenziegel')
# MultiBrickItem.addInformation (client/TDecoClient shows them)
name(f'tooltip.{NS}.brick.bouncy', 'Bouncy!', 'Federnd!')
name(f'tooltip.{NS}.brick.redstone', 'Emits Redstone signal up to 4 blocks away', 'Sendet ein Redstone-Signal bis 4 Blöcke weit')
name(f'tooltip.{NS}.brick.somewhat_expensive', 'Somewhat expensive', 'Etwas teuer')
name(f'tooltip.{NS}.brick.quite_expensive', 'Quite expensive', 'Ziemlich teuer')

# ---------------------------------------------------------------- brownstone (SpeedBlock, SpeedSlab)
BROWN = [('rough_brownstone', 'brownstone_rough', 'rough'), ('rough_brownstone_road', 'brownstone_rough_road', 'rough.road'),
         ('brownstone', 'brownstone_smooth', 'smooth'), ('brownstone_brick', 'brownstone_smooth_brick', 'smooth.brick'),
         ('brownstone_road', 'brownstone_smooth_road', 'smooth.road'), ('fancy_brownstone', 'brownstone_smooth_fancy', 'smooth.fancy'),
         ('chiseled_brownstone', 'brownstone_smooth_chiseled', 'smooth.chiseled')]
for reg, t, key in BROWN:
    texture = block_tex(t)
    cube_all(reg, texture)
    loot_self(reg)
    old_name(f'block.brownstone.{key}.name', f'block.{NS}.{reg}', reg, reg)
    slab(f'{reg}_slab', n(f'block/{reg}'), {'bottom': texture, 'top': texture, 'side': texture})
    old_name(f'block.brownstone.{key}.slab.name', f'block.{NS}.{reg}_slab', reg, reg)
    deco_blocks += [(reg, 'pickaxe'), (f'{reg}_slab', 'pickaxe')]
    shaped(f'{reg}_slab', ['bbb'], {'b': n(reg)}, f'{reg}_slab', 6, category='building')
smelting('brownstone', n('rough_brownstone'), n('brownstone'), 0.2)

# ---------------------------------------------------------------- wool slabs (SlabBase on Block.cloth)
for i, c in enumerate(COLORS):
    reg = f'{c}_wool_slab'
    wool = M + f'block/{c}_wool'
    slab(reg, M + f'block/{c}_wool', {'bottom': wool, 'top': wool, 'side': wool})
    old_name(f'tile.cloth.{CLOTH[i]}.slab.name', f'block.{NS}.{reg}', reg, reg)
    deco_blocks.append((reg, None))

# ---------------------------------------------------------------- stone torch, hambone
torch = block_tex('torch_stone')
model('stone_torch', 'minecraft:block/template_torch', {'torch': torch})
model('stone_wall_torch', 'minecraft:block/template_torch_wall', {'torch': torch})
simple_state('stone_torch')
write(f'assets/{NS}/blockstates/stone_wall_torch.json', {'variants': {
    'facing=east': {'model': n('block/stone_wall_torch')}, 'facing=north': {'model': n('block/stone_wall_torch'), 'y': 270},
    'facing=south': {'model': n('block/stone_wall_torch'), 'y': 90}, 'facing=west': {'model': n('block/stone_wall_torch'), 'y': 180}}})
flat('stone_torch', [torch])
loot_self('stone_torch')
write(f'data/{NS}/loot_table/blocks/stone_wall_torch.json', {'type': 'minecraft:block', 'pools': [
    {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n('stone_torch')}], 'condition': {'type': 'minecraft:survives_explosion'}}]})
old_name('tile.decoration.stonetorch.name', f'block.{NS}.stone_torch', 'Torch', 'Fackel')
model('hambone', 'minecraft:block/cube_column', {'end': block_tex('ham_bone'), 'side': block_tex('ham_skin')})
model('hambone_horizontal', 'minecraft:block/cube_column_horizontal', {'end': block_tex('ham_bone'), 'side': block_tex('ham_skin')})
write(f'assets/{NS}/blockstates/hambone.json', {'variants': {'axis=y': {'model': n('block/hambone')},
                                                             'axis=z': {'model': n('block/hambone_horizontal'), 'x': 90},
                                                             'axis=x': {'model': n('block/hambone_horizontal'), 'x': 90, 'y': 90}}})
block_item('hambone')
loot_self('hambone')
name(f'block.{NS}.hambone', 'Hambone', 'Schinkenknochen')
deco_blocks.append(('hambone', 'axe'))

# ---------------------------------------------------------------- traps: punji sticks (PunjiRender), barricades (BarricadeRender)
punji = block_tex('punji')
STICKS = [([7, 0, 7], [9, 6, 9]), ([2, 0, 2], [4, 6, 4]), ([12, 0, 12], [14, 6, 14]), ([2, 0, 12], [4, 6, 14]), ([12, 0, 2], [14, 6, 4])]
for count in range(1, 6):
    model(f'punji_sticks_{count}', 'minecraft:block/block', {'all': punji, 'particle': punji}, [el(f, t) for f, t in STICKS[:count]])
write(f'assets/{NS}/blockstates/punji_sticks.json', {'variants': {f'sticks={c}': {'model': n(f'block/punji_sticks_{c}')} for c in range(1, 6)}})
model('punji_sticks_inventory', 'minecraft:block/block', {'all': punji, 'particle': punji}, [el([7, 0, 7], [9, 16, 9])])
block_item('punji_sticks', 'punji_sticks_inventory')
write(f'data/{NS}/loot_table/blocks/punji_sticks.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{
    'type': 'minecraft:item', 'name': n('punji_sticks'), 'modifier': [
        {'type': 'minecraft:set_count', 'count': c, 'condition': {'type': 'minecraft:match_block', 'blocks': n('punji_sticks'), 'state': {'sticks': str(c)}}}
        for c in range(2, 6)] + [{'type': 'minecraft:explosion_decay'}]}]}]})
old_name('tile.trap.punji.name', f'block.{NS}.punji_sticks', 'Punji Stick', 'Punji-Stock')
deco_blocks.append(('punji_sticks', None))
shaped('punji_sticks', ['b b', ' b ', 'b b'], {'b': M + 'sugar_cane'}, 'punji_sticks', 5)

# BarricadeRender, rotation 0 (meta / 4 = 0, south): two posts, then two rails
BARRICADE = [([2, 0, 8], [6, 16, 12]), ([10, 0, 8], [14, 16, 12]), ([0, 2, 4], [16, 6, 8]), ([0, 10, 4], [16, 14, 8])]
BARRICADE_ROT = [('south', 0), ('west', 90), ('north', 180), ('east', 270)]
for wood, e, g in WOODS:
    reg = f'{wood}_barricade'
    log = M + f'block/{wood}_log'
    for count in range(1, 5):
        model(f'{reg}_{count}', 'minecraft:block/block', {'all': log, 'particle': log}, [el(f, t) for f, t in BARRICADE[:count]])
    write(f'assets/{NS}/blockstates/{reg}.json', {'variants': {f'facing={face},logs={c}': {'model': n(f'block/{reg}_{c}'), 'y': y}
                                                               for face, y in BARRICADE_ROT for c in range(1, 5)}})
    block_item(reg, f'{reg}_4')
    loot_self(reg)
    old_name(f'tile.trap.barricade.{wood}.name', f'block.{NS}.{reg}', f'{e} Barricade', f'{g}barrikade')
    deco_blocks.append((reg, 'axe'))
    shaped(reg, ['b', 'b'], {'b': M + f'{wood}_log'}, reg)
name(f'tooltip.{NS}.barricade', 'Stacks up to 4 times', 'Bis zu 4-fach stapelbar')
# "logWood": the other vanilla logs make oak barricades
write(f'data/{NS}/tags/item/other_logs.json', {'values': [M + 'acacia_log', M + 'dark_oak_log', M + 'mangrove_log', M + 'cherry_log',
                                                          M + 'pale_oak_log']})
shaped('oak_barricade_from_other_logs', ['b', 'b'], {'b': '#' + n('other_logs')}, 'oak_barricade')

# ---------------------------------------------------------------- landmines (BlockLandmine, RenderLandmine)
mine = tex('blocks/Landmine.png', 'block/landmine')
PLATES = {'down': ([1, 0, 1], [15, 1, 15]), 'up': ([1, 15, 1], [15, 16, 15]), 'north': ([1, 1, 0], [15, 15, 1]), 'south': ([1, 1, 15], [15, 15, 16]),
          'west': ([0, 1, 1], [1, 15, 15]), 'east': ([15, 1, 1], [16, 15, 15])}
for face, (f, t) in PLATES.items():
    model(f'landmine_{face}', 'minecraft:block/block', {'all': mine, 'particle': mine}, [el(f, t)])
LANDMINES = [('landmine', 'everything', 'alles'), ('stone_landmine', 'mobs, players and redstone', 'Mobs, Spieler und Redstone'),
             ('obsidian_landmine', 'players and redstone', 'Spieler und Redstone'), ('redstone_landmine', 'redstone only', 'nur Redstone')]
for reg, e, g in LANDMINES:
    write(f'assets/{NS}/blockstates/{reg}.json', {'variants': {f'facing={face}': {'model': n(f'block/landmine_{face}')} for face in PLATES}})
    block_item(reg, 'landmine_down')
    loot_self(reg)
    name(f'tooltip.{NS}.{reg}', f'Interacts with: {e}', f'Reagiert auf: {g}')
    deco_blocks.append((reg, None))
name(f'block.{NS}.landmine', 'Landmine', 'Landmine')
name(f'block.{NS}.stone_landmine', 'Stone Landmine', 'Steinlandmine')
name(f'block.{NS}.obsidian_landmine', 'Obsidian Landmine', 'Obsidianlandmine')
name(f'block.{NS}.redstone_landmine', 'Redstone Landmine', 'Redstone-Landmine')
name(f'container.{NS}.landmine', 'Landmine', 'Landmine')
tex('gui/landmine.png', 'gui/landmine')
for reg, m in (('landmine', '#minecraft:planks'), ('stone_landmine', M + 'stone'), ('obsidian_landmine', M + 'obsidian'),
               ('redstone_landmine', M + 'repeater')):
    shaped(reg, ['mcm', 'rpr'], {'m': m, 'c': n('blank_cast'), 'r': M + 'redstone', 'p': M + 'stone_pressure_plate'}, reg, category='redstone')
write(f'data/{NS}/damage_type/landmine.json', {'message_id': 'tconstruct.landmine', 'exhaustion': 0.0, 'scaling': 'never'})
tag_add('data/minecraft/tags/damage_type/bypasses_armor.json', [n('landmine')])
name('death.attack.tconstruct.landmine', '%1$s stepped on a landmine', '%1$s ist auf eine Landmine getreten')
name('death.attack.tconstruct.landmine.player', '%1$s stepped on a landmine', '%1$s ist auf eine Landmine getreten')


# ---------------------------------------------------------------- redstone machines (RedstoneMachine), essence extractor
def faced(reg, textures_of):
    """One model per facing with explicit textures for each face (textures_of(face, facing))."""
    for facing in FACES:
        tx = {f: textures_of(f, facing) for f in FACES}
        tx['particle'] = textures_of('north', 'up')
        write(f'assets/{NS}/models/block/{reg}_{facing}.json', {'parent': 'minecraft:block/block', 'textures': tx,
                                                               'elements': [{'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': {
                                                                   f: {'texture': f'#{f}', 'cullface': f} for f in FACES}}]})
    write(f'assets/{NS}/blockstates/{reg}.json', {'variants': {f'facing={facing}': {'model': n(f'block/{reg}_{facing}')} for facing in FACES}})


DB = {s: block_tex(f'machines/drawbridge_{s}') for s in ('top', 'side', 'bottom', 'top_face', 'side_face', 'bottom_face')}


def drawbridge_tex(face, facing):
    base = 'top' if face == 'up' else 'bottom' if face == 'down' else 'side'
    return DB[base + '_face'] if face == facing else DB[base]


faced('drawbridge', drawbridge_tex)
block_item('drawbridge', 'drawbridge_east')
FS = {s: block_tex(f'machines/firestarter_{s}') for s in ('top', 'side', 'bottom')}
OPPOSITE = {'down': 'up', 'up': 'down', 'north': 'south', 'south': 'north', 'west': 'east', 'east': 'west'}
faced('igniter', lambda face, facing: FS['top'] if face == facing else FS['bottom'] if face == OPPOSITE[facing] else FS['side'])
block_item('igniter', 'igniter_up')
# RedstoneMachine.removeBlockByPlayer: the drawbridge keeps its contents and placement
write(f'data/{NS}/loot_table/blocks/drawbridge.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [{
    'type': 'minecraft:item', 'name': n('drawbridge'), 'modifier': {'type': 'minecraft:copy_components', 'source': 'block_entity',
                                                                   'include': [M + 'container', n('placement')]}}],
    'condition': {'type': 'minecraft:survives_explosion'}}]})
loot_self('igniter')
old_name('block.drawbridge.name', f'block.{NS}.drawbridge', 'Drawbridge', 'Zugbrücke')
old_name('block.firestarter.name', f'block.{NS}.igniter', 'Igniter', 'Feueranzünder')
name(f'container.{NS}.drawbridge', 'Drawbridge', 'Zugbrücke')
tex('gui/drawbridge.png', 'gui/drawbridge')
deco_blocks += [('drawbridge', 'pickaxe'), ('igniter', 'pickaxe')]
for dim in ('overworld', 'nether', 'end'):
    tag_add(f'data/minecraft/tags/block/infiniburn_{dim}.json', [n('igniter')])
for reg, c in (('drawbridge', n('blank_cast')), ('drawbridge_from_gold_cast', n('gold_blank_cast'))):
    shaped(reg, ['aca', '#d#', '#r#'], {'a': '#c:ingots/aluminum_brass', 'c': c, '#': '#c:ingots/bronze', 'r': M + 'redstone', 'd': M + 'dispenser'},
           'drawbridge', category='redstone')
shaped('igniter', ['aca', '#d#', '#r#'], {'a': '#c:ingots/aluminum_brass', 'c': n('netherrack_large_plate'), '#': '#c:ingots/bronze',
                                          'r': M + 'redstone', 'd': M + 'flint_and_steel'}, 'igniter', category='redstone')

ex = {s: block_tex(f'extractor_{s}') for s in ('top', 'side', 'bottom')}
model('essence_extractor', 'minecraft:block/block', {'top': ex['top'], 'side': ex['side'], 'bottom': ex['bottom'], 'particle': ex['side']}, [
    {'from': [0, 0, 0], 'to': [16, 12, 16], 'faces': {
        'down': {'texture': '#bottom', 'cullface': 'down'}, 'up': {'texture': '#top'},
        **{f: {'texture': '#side', 'uv': [0, 4, 16, 16], 'cullface': f} for f in ('north', 'south', 'west', 'east')}}}])
simple_state('essence_extractor')
block_item('essence_extractor')
loot_self('essence_extractor')
deco_blocks.append(('essence_extractor', 'pickaxe'))
old_name('tile.extractor.essence.name', f'block.{NS}.essence_extractor', 'Essence Extractor', 'Essenz-Extraktor')
shaped('essence_extractor', [' b ', 'eme', 'mmm'], {'b': M + 'book', 'e': M + 'emerald', 'm': M + 'end_stone'}, 'essence_extractor')
name(f'message.{NS}.essence.total', 'Total Essence: %s', 'Essenz gesamt: %s')
name(f'message.{NS}.essence.levels', 'Stored Levels: %s', 'Gespeicherte Level: %s')

# ---------------------------------------------------------------- glass recipes (TContent.addCraftingRecipes)
for i, c in enumerate(COLORS):
    dye = f'#c:dyes/{c}'
    shaped(f'{c}_clear_stained_glass', ['###', '#m#', '###'], {'m': dye, '#': n('clear_glass')}, f'{c}_clear_stained_glass', 8, category='building')
    shapeless(f'{c}_clear_stained_glass_single', [dye, n('clear_glass')], f'{c}_clear_stained_glass', category='building')
    shaped(f'{c}_clear_stained_glass_pane', ['###', '#m#', '###'], {'m': dye, '#': n('clear_glass_pane')}, f'{c}_clear_stained_glass_pane', 8,
           category='building')
    shapeless(f'{c}_clear_stained_glass_pane_single', [dye, n('clear_glass_pane')], f'{c}_clear_stained_glass_pane', category='building')
    # eight wool of any colour around a dye
    write(f'data/{NS}/recipe/{c}_wool_from_dye.json', {'type': 'minecraft:crafting_shaped', 'category': 'building', 'pattern': ['###', '#m#', '###'],
                                                         'key': {'m': dye, '#': '#minecraft:wool'}, 'result': {'id': M + f'{c}_wool', 'count': 8}})
write(f'data/{NS}/recipe/glass_bottle_from_clear_glass.json', {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': ['# #', ' # '],
                                                               'key': {'#': n('clear_glass')}, 'result': {'id': M + 'glass_bottle', 'count': 3}})
write(f'data/{NS}/recipe/beacon_from_clear_glass.json', {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': ['GGG', 'GSG', 'OOO'],
                                                         'key': {'G': n('clear_glass'), 'S': M + 'nether_star', 'O': M + 'obsidian'},
                                                         'result': {'id': M + 'beacon'}})
shaped('stone_torch', ['p', 'w'], {'p': [M + 'coal', M + 'charcoal'], 'w': n('stone_tool_rod')}, 'stone_torch', 4)

# ---------------------------------------------------------------- tags
for tool in ('pickaxe', 'axe'):
    tag_add(f'data/minecraft/tags/block/mineable/{tool}.json', [n(r) for r, t in deco_blocks if t == tool])
tag_add('data/minecraft/tags/block/slabs.json', [n(f'{r}_slab') for r, _, _ in BROWN] + [n(f'{c}_wool_slab') for c in COLORS])
tag_add('data/minecraft/tags/item/slabs.json', [n(f'{r}_slab') for r, _, _ in BROWN] + [n(f'{c}_wool_slab') for c in COLORS])
glass_all = ['clear_glass'] + [f'{c}_clear_stained_glass' for c in COLORS]
panes_all = ['clear_glass_pane', 'soul_glass_pane', 'clear_soul_glass_pane'] + [f'{c}_clear_stained_glass_pane' for c in COLORS]
tag_add('data/c/tags/block/glass_blocks.json', [n(r) for r in glass_all])
tag_add('data/c/tags/item/glass_blocks.json', [n(r) for r in glass_all])
tag_add('data/c/tags/block/glass_panes.json', [n(r) for r in panes_all])
tag_add('data/c/tags/item/glass_panes.json', [n(r) for r in panes_all])
tag_add('data/minecraft/tags/block/impermeable.json', [n(r) for r in glass_all])

name(f'jei.{NS}.chisel', 'Chisel', 'Meißel')
name(f'jei.{NS}.chisel.grid', 'Crafting grid only', 'Nur im Werkbankraster')
