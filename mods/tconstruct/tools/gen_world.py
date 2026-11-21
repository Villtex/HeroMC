# Part 25d: ore berries, the slime islands, the blue slime, the tinker villager. Run by gen.py (exec, after gen_smeltery.py, whose
# helpers it uses) before the language files are written.

world_blocks = []  # (registry name, tool tag or None)
BERRY_NAMES = ['iron', 'gold', 'copper', 'tin', 'aluminum', 'essence']
BERRY_SHAPES = {0: ([4, 0, 4], [12, 8, 12]), 1: ([2, 0, 2], [14, 12, 14]), 2: ([0, 0, 0], [16, 16, 16]), 3: ([0, 0, 0], [16, 16, 16])}

# ---------------------------------------------------------------- ore berry bushes (OreberryBush, OreberryRender) and berries
for b in BERRY_NAMES:
    reg = f'{b}_oreberry_bush'
    plain = block_tex(f'crops/berry_{b}_fancy')
    ripe = block_tex(f'crops/berry_{b}_ripe_fancy')
    if b == 'essence':
        animated_ripe = os.path.join(REF, 'mods/tinker/textures/blocks/crops/berry_essence_ripe_fancy.txt')
        if os.path.exists(animated_ripe):
            animated('crops/berry_essence_ripe_fancy', 'crops/berry_essence_ripe_fancy')
    for age, (f, t) in BERRY_SHAPES.items():
        texture = ripe if age == 3 else plain
        write(f'assets/{NS}/models/block/{reg}_{age}.json', {'textures': {'all': texture, 'particle': texture}, 'render_type': 'minecraft:cutout_mipped',
                                                             'elements': [el(f, t)]})
    write(f'assets/{NS}/blockstates/{reg}.json', {'variants': {f'age={a}': {'model': n(f'block/{reg}_{a}')} for a in range(4)}})
    item_def(reg, n(f'block/{reg}_2'))
    old_name(f'block.oreberry.{b}.name', f'block.{NS}.{reg}', reg, reg)
    world_blocks.append((reg, None))
    berry = f'{b}_oreberry'
    flat(berry, [tex(f'items/oreberries/oreberry_{b}.png', f'item/oreberry_{b}')])
    old_name(f'item.oreberry.{b}.name', f'item.{NS}.{berry}', berry, berry)
txt = os.path.join(REF, 'mods/tinker/textures/items/oreberries/oreberry_essence.txt')
if os.path.exists(txt):
    frames = []
    for line in open(txt).read().split():
        m = re.match(r'(\d+)\*(\d+)', line)
        frames.append({'index': int(m.group(1)), 'time': int(m.group(2))} if m else int(line))
    write(f'assets/{NS}/textures/item/oreberry_essence.png.mcmeta', {'animation': {'frames': frames}})

# ---------------------------------------------------------------- slime island blocks
for color in ('blue', 'green', 'purple'):
    reg = f'congealed_{color}_slime'
    cube_all(reg, block_tex(f'slimeblock_{color}'))
    world_blocks.append((reg, None))
old_name('block.slime.congealed.blue.name', f'block.{NS}.congealed_blue_slime', 'Congealed Blue Slime', 'Erstarrter Blauer Schleim')
old_name('block.slime.congealed.green.name', f'block.{NS}.congealed_green_slime', 'Congealed Green Slime', 'Erstarrter Grüner Schleim')
name(f'block.{NS}.congealed_purple_slime', 'Congealed Purple Slime', 'Erstarrter Lila Schleim')
for reg, bottom, side_tex, key in (('slime_grass', block_tex('slimedirt_blue'), 'slimegrass_green_blue_side', 'blue'),
                                   ('slime_grass_dirt', 'minecraft:block/dirt', 'slimegrass_green_dirt_side', 'dirt')):
    model(reg, 'minecraft:block/cube_bottom_top', {'top': block_tex('slimegrass_green_top'), 'bottom': bottom, 'side': block_tex(side_tex)})
    simple_state(reg)
    block_item(reg)
    old_name(f'block.slime.grass.{key}.name', f'block.{NS}.{reg}', 'Slimy Grass', 'Schleimiges Gras')
    world_blocks.append((reg, 'shovel'))
write(f'assets/{NS}/models/block/slime_tall_grass.json', {'parent': 'minecraft:block/cross', 'textures': {'cross': block_tex('slimegrass_blue_tall')},
                                                          'render_type': 'minecraft:cutout'})
simple_state('slime_tall_grass')
flat('slime_tall_grass', [block_tex('slimegrass_blue_tall')])
old_name('block.slime.tallgrass.name', f'block.{NS}.slime_tall_grass', 'Slimy Grass', 'Schleimiges Gras')
world_blocks.append(('slime_tall_grass', 'hoe'))
write(f'assets/{NS}/models/block/slime_leaves.json', {'parent': 'minecraft:block/leaves', 'textures': {'all': block_tex('slimeleaves_blue_fancy')},
                                                      'render_type': 'minecraft:cutout_mipped'})
simple_state('slime_leaves')
block_item('slime_leaves')
old_name('block.slime.leaves.blue.name', f'block.{NS}.slime_leaves', 'Slimy Leaves', 'Schleimige Blätter')
world_blocks.append(('slime_leaves', 'hoe'))
write(f'assets/{NS}/models/block/slime_sapling.json', {'parent': 'minecraft:block/cross', 'textures': {'cross': block_tex('slimesapling_blue')},
                                                       'render_type': 'minecraft:cutout'})
write(f'assets/{NS}/blockstates/slime_sapling.json', {'variants': {f'stage={s}': {'model': n('block/slime_sapling')} for s in (0, 1)}})
flat('slime_sapling', [block_tex('slimesapling_blue')])
old_name('block.slime.sapling.bluegreen.name', f'block.{NS}.slime_sapling', 'Slimy Sapling', 'Schleimiger Setzling')
world_blocks.append(('slime_sapling', None))

# liquid blue slime (SlimeFluid, textures slime_blue, slime_blue_flow)
still = animated('slime_blue', 'slime_blue')
animated('slime_blue_flow', 'slime_blue_flow')
write(f'assets/{NS}/models/block/liquid_blue_slime.json', {'textures': {'particle': still}})
write(f'assets/{NS}/blockstates/liquid_blue_slime.json', {'variants': {'': {'model': n('block/liquid_blue_slime')}}})
name(f'block.{NS}.liquid_blue_slime', 'Liquid Blue Slime', 'Flüssiger blauer Schleim')

# items
flat('gelatinous_slime', [tex('items/edibleslime.png', 'item/edibleslime')])
old_name('item.tconstruct.strangefood.edibleslime.name', f'item.{NS}.gelatinous_slime', 'Gelatinous Slime', 'Gallertartiger Schleim')
flat('miniature_red_heart', [tex('items/miniheart_red.png', 'item/miniheart_red')])
old_name('item.tconstruct.canister.miniheart.red.name', f'item.{NS}.miniature_red_heart', 'Miniature Red Heart', 'Kleines rotes Herz')

# ---------------------------------------------------------------- loot, tags
for reg, tool in world_blocks:
    if reg not in ('slime_tall_grass', 'slime_leaves'):
        loot_self(reg)
SHEARS = {'type': 'minecraft:match_tool', 'predicate': {'items': 'minecraft:shears'}}
# SlimeTallGrass: itself with shears, else 1 in 8 a grass seed (ForgeHooks.getGrassSeed: wheat seeds)
write(f'data/{NS}/loot_table/blocks/slime_tall_grass.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [
    {'type': 'minecraft:alternatives', 'children': [
        {'type': 'minecraft:item', 'name': n('slime_tall_grass'), 'condition': SHEARS},
        {'type': 'minecraft:item', 'name': M + 'wheat_seeds', 'condition': {'type': 'minecraft:random_chance', 'chance': 0.125}}]}]}]})
# SlimeLeaves.dropBlockAsItemWithChance: sapling 1 in 35 (fortune: 35 - (2 << f), at least 15), gelatinous slime 1 in 80 (80 - (10 << f), at
# least 20); shears give the leaves (BlockLeaves.onSheared)
SHEARS_OR_SILK = {'type': 'minecraft:any_of', 'terms': ['minecraft:tool/can_shear', 'minecraft:tool/can_silk_touch']}
write(f'data/{NS}/loot_table/blocks/slime_leaves.json', {'type': 'minecraft:block', 'pools': [
    {'rolls': 1, 'entries': [{'type': 'minecraft:alternatives', 'children': [
        {'type': 'minecraft:item', 'name': n('slime_leaves'), 'condition': SHEARS_OR_SILK},
        {'type': 'minecraft:item', 'name': n('slime_sapling'), 'condition': {'type': 'minecraft:all_of', 'terms': [
            {'type': 'minecraft:survives_explosion'},
            {'type': 'minecraft:table_bonus', 'enchantment': 'minecraft:fortune', 'chances': [1 / 35, 1 / 31, 1 / 27, 1 / 19]}]}}]}]},
    {'rolls': 1, 'condition': {'type': 'minecraft:inverted', 'term': SHEARS_OR_SILK}, 'entries': [
        {'type': 'minecraft:item', 'name': n('gelatinous_slime'), 'condition': {
            'type': 'minecraft:table_bonus', 'enchantment': 'minecraft:fortune', 'chances': [1 / 80, 1 / 60, 1 / 40, 1 / 20]}}]}]})
for tool in ('shovel', 'hoe'):
    write(f'data/minecraft/tags/block/mineable/{tool}.json', {'replace': False, 'values': [n(r) for r, t in blocks + world_blocks if t == tool]})
write('data/minecraft/tags/block/leaves.json', {'replace': False, 'values': [n('slime_leaves')]})
write('data/minecraft/tags/item/leaves.json', {'replace': False, 'values': [n('slime_leaves')]})
write('data/minecraft/tags/block/saplings.json', {'replace': False, 'values': [n('slime_sapling')]})
write('data/minecraft/tags/item/saplings.json', {'replace': False, 'values': [n('slime_sapling')]})
# SlimeGrass.canSustainPlant: every Plains plant but vanilla tall grass
write('data/minecraft/tags/block/supports_vegetation.json', {'replace': False, 'values': [n('slime_grass'), n('slime_grass_dirt')]})
write('data/minecraft/tags/block/replaceable_by_trees.json', {'replace': False, 'values': [n('slime_tall_grass')]})

# BlueSlime.dropFewItems: 1 in 2 (always for the King Slime) nextInt(3) + nextInt(size) (+ nextInt(looting + 1)) gelatinous slime
def slime_pool(size, chance):
    entry = {'type': 'minecraft:item', 'name': n('gelatinous_slime'), 'modifier': [
        {'type': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': 0, 'max': 2}},
        {'type': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': 0, 'max': size - 1}, 'add': True},
        {'type': 'minecraft:enchanted_count_increase', 'enchantment': 'minecraft:looting', 'count': {'type': 'minecraft:uniform', 'min': 0, 'max': 1}}]}
    cond = [{'type': 'minecraft:entity_properties', 'entity': 'this', 'predicate': {'minecraft:type_specific/cube_mob': {'size': size}}}]
    if chance:
        cond.append({'type': 'minecraft:random_chance', 'chance': 0.5})
    return {'rolls': 1, 'condition': {'type': 'minecraft:all_of', 'terms': cond}, 'entries': [entry]}


write(f'data/{NS}/loot_table/entities/blue_slime.json', {'type': 'minecraft:entity', 'pools': [slime_pool(s, s < 8) for s in range(1, 9)]})
tex('mob/slimeedible.png', 'entity/blue_slime')
old_name('entity.TConstruct.EdibleSlime.name', f'entity.{NS}.blue_slime', 'Blue Slime', 'Blauer Schleim')
old_name('entity.TConstruct.KingSlime.name', f'entity.{NS}.king_slime', 'King Slime', 'Königsschleim')

# ---------------------------------------------------------------- recipes
shaped('congealed_blue_slime', ['##', '##'], {'#': n('gelatinous_slime')}, 'congealed_blue_slime', category='building')
shaped('gelatinous_slime', ['#'], {'#': n('congealed_blue_slime')}, 'gelatinous_slime', 4)
shaped('congealed_green_slime', ['##', '##'], {'#': M + 'slime_ball'}, 'congealed_green_slime', category='building')
write(f'data/{NS}/recipe/slime_ball_from_congealed_slime.json', {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': ['#'],
                                                                   'key': {'#': n('congealed_green_slime')}, 'result': {'id': M + 'slime_ball', 'count': 4}})
shapeless('blue_slimy_mud', [n('gelatinous_slime')] * 4 + [M + 'sand', M + 'dirt'], 'blue_slimy_mud', category='building')
for b, out in (('iron', M + 'iron_nugget'), ('gold', M + 'gold_nugget'), ('copper', 'copper_nugget'), ('tin', 'tin_nugget'),
               ('aluminum', 'aluminum_nugget')):
    smelting(f'{out.split(":")[-1]}_from_oreberry', n(f'{b}_oreberry'), out, 0.2)

# ---------------------------------------------------------------- the tinker villager (78943) and its trades (TVillageTrades)
tex('mob/villagertools.png', 'entity/villager/profession/tinker')
name(f'entity.minecraft.villager.{NS}.tinker', 'Tinker', 'Tüftler')
# MerchantRecipeList.addToListWithCheck keeps the first offer per pair of items: 14 pistons for an aluminum bush, 23 for an iron bush
for file, pistons, bush in (('aluminum_oreberry_bush', 14, 'aluminum_oreberry_bush'), ('iron_oreberry_bush', 23, 'iron_oreberry_bush')):
    write(f'data/{NS}/villager_trade/tinker/1/{file}.json', {'wants': {'id': M + 'piston', 'count': pistons}, 'gives': {'id': n(bush)},
                                                             'max_uses': 7})
write(f'data/{NS}/trade_set/tinker/level_1.json', {'amount': 2, 'trades': [n('tinker/1/aluminum_oreberry_bush'), n('tinker/1/iron_oreberry_bush')]})
