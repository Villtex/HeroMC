# Teil 24f: items, armour, tools, recipes (run from gen.py, which defines write, n, NS, ASSETS, names, tag, RECIPES, shaped,
# shapeless, generated_item, item_name)

def item_tex(src, dst):
    ASSETS.append((f'mods/twilightforest/textures/items/{src}.png', f'assets/{NS}/textures/item/{dst}.png'))


def simple_item(i, src, en, de, texture=None, handheld=False):
    if texture is None:
        item_tex(src, i)
        texture = n(f'item/{i}')
    write(f'assets/{NS}/models/item/{i}.json', {'parent': 'minecraft:item/handheld' if handheld else 'minecraft:item/generated',
                                                'textures': {'layer0': texture}})
    write(f'assets/{NS}/items/{i}.json', {'model': {'type': 'minecraft:model', 'model': n(f'item/{i}')}})
    item_name(i, en, de)


def ench(enchantments):
    return {'minecraft:enchantments': enchantments}


def shaped_c(name, pattern, key, result, components=None, count=1, category='equipment'):
    shaped(name, pattern, key, result, count, category)
    if components:
        RECIPES[name]['result']['components'] = components


def shapeless_c(name, ingredients, result, components=None, count=1, category='misc'):
    shapeless(name, ingredients, result, count, category)
    if components:
        RECIPES[name]['result']['components'] = components


def smelt(name, ingredient, result, xp, count=1):
    RECIPES[name] = {'type': 'minecraft:smelting', 'category': 'misc', 'ingredient': ingredient, 'result': {'id': result, 'count': count},
                     'experience': xp, 'cookingtime': 200}


# ---------------------------------------------------------------- simple items (ItemTF)
for i, src, en, de in [
    ('naga_scale', 'nagaScale', 'Naga Scale', 'Nagaschuppe'),
    ('raw_ironwood', 'ironwoodRaw', 'Raw Ironwood Materials', 'Eisenholz-Rohstoff'),
    ('ironwood_ingot', 'ironwoodIngot', 'Ironwood Ingot', 'Eisenholzbarren'),
    ('fiery_blood', 'fieryBlood', 'Fiery Blood', 'Feuriges Blut'),
    ('fiery_ingot', 'fieryIngot', 'Fiery Ingot', 'Feuriger Barren'),
    ('steeleaf_ingot', 'steeleafIngot', 'Steeleaf', 'Stahlblatt'),
    ('charm_of_life_2', 'charmOfLife2', 'Charm of Life II', 'Amulett des Lebens II'),
    ('charm_of_keeping_2', 'charmOfKeeping2', 'Charm of Keeping II', 'Amulett der Bewahrung II'),
    ('charm_of_keeping_3', 'charmOfKeeping3', 'Charm of Keeping III', 'Amulett der Bewahrung III'),
    ('armor_shards', 'armorShards', 'Armor Shards', 'Rüstungssplitter'),
    ('knight_metal', 'knightMetal', 'Knightmetal Ingot', 'Rittermetallbarren'),
    ('maze_wafer', 'mazeWafer', 'Maze Wafer', 'Labyrinthwaffel'),
    ('experiment_115', 'experiment115', 'Experiment 115', 'Experiment 115'),
]:
    simple_item(i, src, en, de)
# ItemTFFood.setLooksLike: the hydra chop looks like a cooked porkchop, the stroganoff like mushroom stew
simple_item('hydra_chop', None, 'Hydra Chop', 'Hydrakotelett', 'minecraft:item/cooked_porkchop')
simple_item('meef_stroganoff', None, 'Meef Stroganoff', 'Meef Stroganoff', 'minecraft:item/mushroom_stew')
tag('item', 'minecraft:meat', n('hydra_chop'))
tag('item', 'minecraft:wolf_food', n('hydra_chop'))
tag('item', 'c:foods/cooked_meat', n('hydra_chop'))
tag('item', 'c:foods/soup', n('meef_stroganoff'))
tag('item', 'c:ingots', n('ironwood_ingot'), n('fiery_ingot'), n('knight_metal'))

# ---------------------------------------------------------------- armour (textures/armor/<mat>_1/_2) and tools
for mat, old in (('naga_scale', 'naga_scale'), ('ironwood', 'ironwood'), ('fiery', 'fiery'), ('steeleaf', 'steeleaf')):
    ASSETS.append((f'mods/twilightforest/textures/armor/{old}_1.png', f'assets/{NS}/textures/entity/equipment/humanoid/{mat}.png'))
    ASSETS.append((f'mods/twilightforest/textures/armor/{old}_2.png', f'assets/{NS}/textures/entity/equipment/humanoid_leggings/{mat}.png'))
    write(f'assets/{NS}/equipment/{mat}.json', {'layers': {'humanoid': [{'texture': n(mat)}], 'humanoid_leggings': [{'texture': n(mat)}]}})

PIECES = {'helmet': ('Helm', 'Helm', 'head'), 'chestplate': ('Plate', 'Harnisch', 'chest'), 'leggings': ('Legs', 'Beinschutz', 'leg'),
          'boots': ('Boots', 'Stiefel', 'foot')}
TOOLS = {'sword': ('Sword', 'schwert', 'swords'), 'shovel': ('Shovel', 'schaufel', 'shovels'), 'pickaxe': ('Pick', 'spitzhacke', 'pickaxes'),
         'axe': ('Axe', 'axt', 'axes'), 'hoe': ('Hoe', 'hacke', 'hoes')}
for mat, old, en_mat, de_mat, pieces, tools in (
        ('ironwood', 'ironwood', 'Ironwood', 'Eisenholz', PIECES, TOOLS),
        ('fiery', 'fiery', 'Fiery', 'Feuer', PIECES, {k: TOOLS[k] for k in ('sword', 'pickaxe')}),
        ('steeleaf', 'steeleaf', 'Steeleaf', 'Stahlblatt', PIECES, TOOLS)):
    for p, (old_p, de_p, slot) in pieces.items():
        i = f'{mat}_{p}'
        simple_item(i, f'{old}{old_p}', f'{en_mat} {old_p}', f'{de_mat}-{de_p}' if mat != 'fiery' else f'Feuer{de_p.lower()}')
        tag('item', {'head': 'minecraft:head_armor', 'chest': 'minecraft:chest_armor', 'leg': 'minecraft:leg_armor',
                     'foot': 'minecraft:foot_armor'}[slot], n(i))
    for t, (old_t, de_t, ttag) in tools.items():
        i = f'{mat}_{t}'
        simple_item(i, f'{old}{old_t}', f'{en_mat} {old_t}', f'{de_mat}{de_t}' if mat != 'steeleaf' else f'Stahlblatt{de_t}', handheld=True)
        tag('item', f'minecraft:{ttag}', n(i))
names(f'item.{NS}.fiery_helmet', 'Fiery Helm', 'Feuerhelm')
for i, src, en, de in (('naga_chestplate', 'plateNaga', 'Naga Scale Tunic', 'Nagaschuppen-Wams'),
                       ('naga_leggings', 'legsNaga', 'Naga Scale Leggings', 'Nagaschuppen-Beinlinge')):
    simple_item(i, src, en, de)
tag('item', 'minecraft:chest_armor', n('naga_chestplate'))
tag('item', 'minecraft:leg_armor', n('naga_leggings'))
simple_item('minotaur_axe', 'minotaurAxe', 'Minotaur Axe', 'Minotaurusaxt', handheld=True)
simple_item('mazebreaker_pickaxe', 'mazebreakerPick', 'Mazebreaker', 'Labyrinthbrecher', handheld=True)
tag('item', 'minecraft:axes', n('minotaur_axe'))
tag('item', 'minecraft:pickaxes', n('mazebreaker_pickaxe'))
names(f'item.{NS}.minotaur_axe.tooltip', 'Extra charge damage', 'Mehr Schaden im Ansturm')
names(f'item.{NS}.fiery_pickaxe.tooltip', 'Auto-smelting', 'Schmilzt von selbst')
tag('item', 'minecraft:enchantable/fire_aspect', n('fiery_pickaxe'))

# repair items (getIsRepairable)
tag('item', f'{NS}:repairs_ironwood', n('ironwood_ingot'))
tag('item', f'{NS}:repairs_steeleaf', n('steeleaf_ingot'))
tag('item', f'{NS}:repairs_fiery', n('fiery_ingot'))
tag('item', f'{NS}:repairs_naga_scale', n('naga_scale'))

# ---------------------------------------------------------------- Fiery Aura (EnchantmentTFFireReact: weight 2, armour, level 1, 11..16)
write(f'data/{NS}/enchantment/fiery_aura.json', {
    'anvil_cost': 4, 'description': {'translate': f'enchantment.{NS}.fiery_aura'}, 'max_cost': {'base': 16, 'per_level_above_first': 10},
    'max_level': 1, 'min_cost': {'base': 11, 'per_level_above_first': 10}, 'slots': ['armor'],
    'supported_items': '#minecraft:enchantable/armor', 'weight': 2})
names(f'enchantment.{NS}.fiery_aura', 'Fiery Aura', 'Feurige Aura')
tag('enchantment', 'minecraft:in_enchanting_table', n('fiery_aura'))
tag('enchantment', 'minecraft:non_treasure', n('fiery_aura'))

# ---------------------------------------------------------------- TFRecipes
I = 'minecraft:'
shaped_c('naga_chestplate', ['# #', '###', '###'], {'#': n('naga_scale')}, n('naga_chestplate'), ench({I + 'fire_protection': 3}))
shaped_c('naga_leggings', ['###', '# #', '# #'], {'#': n('naga_scale')}, n('naga_leggings'), ench({I + 'protection': 3}))
shapeless_c('raw_ironwood', [n('liveroot'), I + 'iron_ingot', I + 'gold_nugget'], n('raw_ironwood'))
smelt('ironwood_ingot', n('raw_ironwood'), n('ironwood_ingot'), 1.0, 2)
smelt('knight_metal', n('armor_shards'), n('knight_metal'), 1.0)
shapeless_c('fiery_ingot', [n('fiery_blood'), I + 'iron_ingot'], n('fiery_ingot'))
ARMOR_PATTERNS = {'helmet': ['###', '# #'], 'chestplate': ['# #', '###', '###'], 'leggings': ['###', '# #', '# #'], 'boots': ['# #', '# #']}
TOOL_PATTERNS = {'sword': ['#', '#', 'X'], 'shovel': ['#', 'X', 'X'], 'pickaxe': ['###', ' X ', ' X '], 'axe': ['##', '#X', ' X'],
                 'hoe': ['##', ' X', ' X']}
for mat, ingot, stick, enchants in (
        ('ironwood', n('ironwood_ingot'), I + 'stick', {'helmet': ('aqua_affinity', 1), 'chestplate': ('protection', 1), 'leggings': ('protection', 1),
                                                         'boots': ('feather_falling', 1), 'sword': ('knockback', 1), 'shovel': ('unbreaking', 1),
                                                         'pickaxe': ('efficiency', 1), 'axe': ('fortune', 1), 'hoe': None}),
        ('fiery', n('fiery_ingot'), I + 'blaze_rod', {'helmet': (n('fiery_aura'), 2), 'chestplate': (n('fiery_aura'), 2),
                                                      'leggings': (n('fiery_aura'), 2), 'boots': (n('fiery_aura'), 2),
                                                      'sword': ('fire_aspect', 2), 'pickaxe': None}),
        ('steeleaf', n('steeleaf_ingot'), I + 'stick', {'helmet': ('projectile_protection', 2), 'chestplate': ('blast_protection', 2),
                                                        'leggings': ('fire_protection', 2), 'boots': ('feather_falling', 2),
                                                        'sword': ('looting', 2), 'shovel': ('efficiency', 2), 'pickaxe': ('fortune', 2),
                                                        'axe': ('efficiency', 2), 'hoe': None})):
    for piece, e in enchants.items():
        pattern = ARMOR_PATTERNS.get(piece) or TOOL_PATTERNS[piece]
        key = {'#': ingot}
        if piece in TOOL_PATTERNS:
            key['X'] = stick
        comp = None
        if e:
            eid = e[0] if ':' in e[0] else I + e[0]
            comp = ench({eid: e[1]})
        shaped_c(f'{mat}_{piece}', pattern, key, n(f'{mat}_{piece}'), comp)
shapeless_c('charm_of_keeping_2', [n('charm_of_keeping_1')] * 4, n('charm_of_keeping_2'))
shapeless_c('charm_of_keeping_3', [n('charm_of_keeping_2')] * 4, n('charm_of_keeping_3'))
shapeless_c('charm_of_life_2', [n('charm_of_life_1')] * 4, n('charm_of_life_2'))
shaped_c('arrow_from_raven_feather', ['X', '#', 'Y'], {'X': I + 'flint', '#': I + 'stick', 'Y': n('raven_feather')}, I + 'arrow', None, 4)
shapeless_c('magic_map_focus', [n('raven_feather'), n('torchberries'), I + 'glowstone_dust'], n('magic_map_focus'))
shaped_c('carminite', ['brb', 'rgr', 'brb'], {'b': n('borer_essence'), 'r': I + 'redstone', 'g': I + 'ghast_tear'}, n('carminite'), None, 1, 'misc')
shaped_c('carminite_2', ['rbr', 'bgb', 'rbr'], {'b': n('borer_essence'), 'r': I + 'redstone', 'g': I + 'ghast_tear'}, n('carminite'), None, 1, 'misc')
shaped_c('vanishing_block', ['ewe', 'wcw', 'ewe'], {'e': n('encased_tower_wood'), 'w': n('tower_wood'), 'c': n('carminite')},
         n('vanishing_block'), None, 8, 'redstone')
shaped_c('reappearing_block', ['ere', 'rcr', 'ere'], {'e': n('encased_tower_wood'), 'r': I + 'redstone', 'c': n('carminite')},
         n('reappearing_block'), None, 2, 'redstone')
shaped_c('carminite_builder', ['ece', 'cdc', 'ece'], {'e': n('encased_tower_wood'), 'd': I + 'dispenser', 'c': n('carminite')},
         n('carminite_builder'), None, 1, 'redstone')
shaped_c('carminite_reactor', ['ece', 'coc', 'ece'], {'e': n('encased_tower_wood'), 'o': I + 'redstone_ore', 'c': n('carminite')},
         n('carminite_reactor'), None, 1, 'redstone')
shaped_c('encased_smoker', ['ere', 'rsr', 'ere'], {'e': n('encased_tower_wood'), 'r': I + 'redstone', 's': n('smoker')},
         n('encased_smoker'), None, 1, 'redstone')
shaped_c('encased_fire_jet', ['ere', 'rjr', 'lll'], {'e': n('encased_tower_wood'), 'r': I + 'redstone', 'l': I + 'lava_bucket', 'j': n('fire_jet')},
         n('encased_fire_jet'), None, 1, 'redstone')

# ---------------------------------------------------------------- the special items (scepters, fan, horn, magnet, queen, powder, meter)
for i, src, en, de, handheld in [
    ('twilight_scepter', 'scepterTwilight', 'Scepter of Twilight', 'Zepter des Zwielichts', True),
    ('lifedrain_scepter', 'scepterLifeDrain', 'Scepter of Life Draining', 'Zepter des Lebensentzugs', True),
    ('zombie_scepter', 'scepterZombie', 'Zombie Scepter', 'Zombie-Zepter', True),
    ('ore_meter', 'oreMeter', 'Ore Meter [WIP]', 'Erzmesser [WIP]', False),
    ('crumble_horn', 'crumbleHorn', 'Crumble Horn', 'Zerbröselhorn', False),
    ('peacock_fan', 'peacockFan', 'Peacock Feather Fan', 'Pfauenfederfächer', True),
    ('transform_powder', 'transformPowder', 'Transformation Powder', 'Verwandlungspulver', False),
]:
    simple_item(i, src, en, de, handheld=handheld)
names(f'item.{NS}.charges', '%s charges left', 'Noch %s Ladungen')


def using_item(i, icons, en, de, entries):
    """An item whose icon changes while it is held down (getIcon with useRemaining): range_dispatch on use_duration."""
    for k, src in enumerate(icons):
        name = i if k == 0 else f'{i}_{k}'
        item_tex(src, name)
        write(f'assets/{NS}/models/item/{name}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': n(f'item/{name}')}})
    model = lambda k: {'type': 'minecraft:model', 'model': n(f'item/{i}' if k == 0 else f'item/{i}_{k}')}
    write(f'assets/{NS}/items/{i}.json', {'model': {
        'type': 'minecraft:condition', 'property': 'minecraft:using_item', 'on_false': model(0),
        'on_true': {'type': 'minecraft:range_dispatch', 'property': 'minecraft:use_duration', 'scale': 1.0, 'fallback': model(0),
                    'entries': [{'threshold': t, 'model': model(k)} for t, k in entries]}}})
    item_name(i, en, de)


# ItemTFOreMagnet.getIcon: icon 1 after 10 ticks, from 20 ticks icons 2 and 1 alternating every 4 ticks
using_item('ore_magnet', ['oreMagnet', 'oreMagnet1', 'oreMagnet2'], 'Ore Magnet', 'Erzmagnet',
           [(11, 1)] + [(t, 2 if (t >> 2) % 2 == 0 else 1) for t in range(20, 400, 4)])
# ItemTFMoonwormQueen.getIcon: from 12 ticks icons 0 and 1 alternating every 2 ticks
using_item('moonworm_queen', ['moonwormQueen', 'moonwormQueenAlt'], 'Moonworm Queen', 'Mondwurm-Königin',
           [(t, 0 if (t >> 1) % 2 == 0 else 1) for t in range(12, 400, 2)])

# TFRecipes: an empty scepter (damage = maximum) is recharged
for scepter, max_damage, extra in (('twilight_scepter', 99, [[I + 'ender_pearl']]), ('lifedrain_scepter', 99, [[I + 'fermented_spider_eye']])):
    shapeless_c(f'{scepter}_recharge', [{'neoforge:ingredient_type': 'neoforge:components', 'items': n(scepter),
                                         'components': {'minecraft:damage': max_damage}}] + extra[0], n(scepter))
# the zombie scepter: rotten flesh and a potion of strength (8201, 8265, 8233 and the splash potions 16281 .. 16377)
for kind in ('potion', 'splash_potion'):
    for potion in ('strength', 'long_strength', 'strong_strength'):
        shapeless_c(f'zombie_scepter_recharge_{kind}_{potion}', [
            {'neoforge:ingredient_type': 'neoforge:components', 'items': n('zombie_scepter'), 'components': {'minecraft:damage': 9}},
            I + 'rotten_flesh',
            {'neoforge:ingredient_type': 'neoforge:components', 'items': I + kind, 'components': {'minecraft:potion_contents': {'potion': I + potion}}}],
            n('zombie_scepter'))
shapeless_c('moonworm_queen_recharge', [n('moonworm_queen')] + [n('torchberries')] * 3, n('moonworm_queen'))
tag('entity_type', 'minecraft:undead', n('loyal_zombie'))
names(f'entity.{NS}.twilight_wand_bolt', 'Twilight Wand Bolt', 'Zwielichtzepter-Bolzen')
names(f'entity.{NS}.moonworm_shot', 'Moonworm Shot', 'Mondwurm-Schuss')
names(f'entity.{NS}.charm_effect', 'Charm Effect', 'Amulett-Effekt')
names(f'entity.{NS}.loyal_zombie', 'Loyal Zombie', 'Treuer Zombie')

# ================================================================= the bosses (TFBosses)
for mob, en, de, egg in [('naga', 'Naga', 'Naga', (10801942, 1783819)), ('lich', 'Twilight Lich', 'Zwielicht-Lich', (11314313, 3540082)),
                         ('lich_minion', 'Lich Minion', 'Lich-Diener', None), ('minoshroom', 'Minoshroom', 'Minoshroom', (11014162, 11173222)),
                         ('ur_ghast', 'Ur-Ghast', 'Ur-Ghast', (12369084, 12023928))]:
    names(f'entity.{NS}.{mob}', en, de)
    if egg:
        name = f'{mob}_spawn_egg'
        write(f'assets/{NS}/models/item/{name}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'minecraft:item/egg'}})
        write(f'assets/{NS}/items/{name}.json', {'model': {'type': 'minecraft:model', 'model': n(f'item/{name}'),
                                                           'tints': [{'type': 'minecraft:constant', 'value': (0xFF000000 | egg[0]) - (1 << 32)}]}})
        names(f'item.{NS}.{name}', f'{en} Spawn Egg', f'Spawn-Ei ({de})')
names(f'entity.{NS}.lich_bolt', 'Lich Bolt', 'Lich-Bolzen')
names(f'entity.{NS}.lich_bomb', 'Lich Bomb', 'Lich-Bombe')
for src in ['nagahead', 'nagasegment', 'twilightlich64', 'minoshroomtaur', 'towerboss', 'towerboss_openeyes', 'towerboss_fire']:
    ASSETS.append((f'mods/twilightforest/textures/model/{src}.png', f'assets/{NS}/textures/entity/{src}.png'))
tag('entity_type', 'minecraft:undead', n('lich'), n('lich_minion'))
tag('entity_type', 'minecraft:sensitive_to_smite', n('lich'), n('lich_minion'), n('loyal_zombie'))
# the naga's sounds (TFSounds: mob.tf.naga.hiss / rattle / hurt)
for event, files, en, de in (('naga.hiss', ['nagahiss%d' % i for i in range(3)], 'Naga hisses', 'Naga zischt'),
                             ('naga.rattle', ['nagarattle%d' % i for i in range(2)], 'Naga rattles', 'Naga rasselt'),
                             ('naga.hurt', ['nagahurt%d' % i for i in range(3)], 'Naga hurts', 'Naga verletzt')):
    SOUNDS_JSON[event] = {'sounds': [n(f'naga/{f}') for f in files], 'subtitle': f'subtitles.{NS}.{event}'}
    for f in files:
        ASSETS.append((f'mods/twilightforest/sounds/naga/{f}.ogg', f'assets/{NS}/sounds/naga/{f}.ogg'))
    names(f'subtitles.{NS}.{event}', en, de)
write(f'assets/{NS}/sounds.json', SOUNDS_JSON)

# loot (dropFewItems)
mob_loot('naga', [drop(n('naga_scale'), 6, 11, 0), drop(n('naga_trophy'), 1, 1, 0)])
GOLD = [I + x for x in ('golden_sword', 'golden_helmet', 'golden_chestplate', 'golden_leggings', 'golden_boots')]
mob_loot('lich', [
    {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n(s)} for s in ('zombie_scepter', 'lifedrain_scepter', 'twilight_scepter')]},
    # dropGoldThing: rand(3 + looting) + 2 golden things, each with addRandomEnchantment(10 + rand(30))
    {'rolls': {'type': 'minecraft:uniform', 'min': 2, 'max': 4}, 'entries': [{'type': 'minecraft:item', 'name': g, 'modifier': [
        {'type': 'minecraft:enchant_with_levels', 'levels': {'type': 'minecraft:uniform', 'min': 10, 'max': 39}}]} for g in GOLD]},
    drop(I + 'ender_pearl', 1, 4), drop(I + 'bone', 5, 9), drop(n('lich_trophy'), 1, 1, 0)])
mob_loot('minoshroom', [drop(n('meef_stroganoff'), 2, 5)])
_zombie = vanilla_loot('zombie')
if _zombie is not None:
    mob_loot('lich_minion', _zombie['pools'])
import copy
with open(os.path.join(ROOT, f'data/{NS}/loot_table/entities/tower_ghast.json')) as f:
    mob_loot('ur_ghast', copy.deepcopy(json.load(f)['pools']))

# trophies: the item icons; the blocks are drawn by their renderer (soul sand for the break particles)
for kind, src, en, de in (('hydra', 'hydraTrophy', 'Hydra Trophy', 'Hydra-Trophäe'), ('naga', 'nagaTrophy', 'Naga Trophy', 'Naga-Trophäe'),
                          ('lich', 'lichTrophy', 'Lich Trophy', 'Lich-Trophäe'), ('ur_ghast', 'urGhastTrophy', 'Ur-Ghast Trophy', 'Ur-Ghast-Trophäe')):
    t = f'{kind}_trophy'
    item_tex(src, t)
    write(f'assets/{NS}/models/item/{t}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': n(f'item/{t}')}})
    write(f'assets/{NS}/items/{t}.json', {'model': {'type': 'minecraft:model', 'model': n(f'item/{t}')}})
    names(f'block.{NS}.{t}', en, de)
    names(f'block.{NS}.wall_{t}', en, de)
    for b in (t, f'wall_{t}'):
        write(f'assets/{NS}/models/block/{b}.json', {'textures': {'particle': 'minecraft:block/soul_sand'}})
        write(f'assets/{NS}/blockstates/{b}.json', {'variants': {'': {'model': n(f'block/{b}')}}})
        write(f'data/{NS}/loot_table/blocks/{b}.json', {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [
            {'type': 'minecraft:item', 'name': n(t)}], 'condition': {'type': 'minecraft:survives_explosion'}}], 'random_sequence': f'{NS}:blocks/{b}'})

# ================================================================= the maps (TFMaps: ItemTFMagicMap, ItemTFMazeMap, blank maps)
for i, src, en, de in [
    ('magic_map', 'magicMap', 'Magic Map', 'Magische Karte'),
    ('maze_map', 'mazeMap', 'Maze Map', 'Labyrinthkarte'),
    ('ore_map', 'oreMap', 'Maze/Ore Map', 'Labyrinth-/Erzkarte'),
    ('empty_magic_map', 'emptyMagicMap', 'Blank Magic Map', 'Leere magische Karte'),
    ('empty_maze_map', 'emptyMazeMap', 'Blank Maze Map', 'Leere Labyrinthkarte'),
    ('empty_ore_map', 'emptyOreMap', 'Blank Maze/Ore Map', 'Leere Labyrinth-/Erzkarte'),
]:
    simple_item(i, src, en, de)
# the feature icons of the magic map (8 x 8 icons, index = TFFeature.featureID), drawn by TFMapsClient
ASSETS.append(('mods/twilightforest/textures/gui/mapicons.png', f'assets/{NS}/textures/gui/mapicons.png'))
shaped_c('empty_magic_map', ['###', '#X#', '###'], {'#': I + 'paper', 'X': n('magic_map_focus')}, n('empty_magic_map'), None, 1, 'misc')
shaped_c('empty_maze_map', ['###', '#X#', '###'], {'#': I + 'paper', 'X': n('maze_map_focus')}, n('empty_maze_map'), None, 1, 'misc')
shapeless_c('empty_ore_map', [n('empty_maze_map'), I + 'gold_block', I + 'diamond_block', I + 'iron_block'], n('empty_ore_map'))
shapeless_c('empty_ore_map_from_maze_map', [n('maze_map'), I + 'gold_block', I + 'diamond_block', I + 'iron_block'], n('empty_ore_map'))
# TFMapCloningRecipe: a filled map and 1 to 8 blank maps of its kind give as many copies plus the map (name and all)
for full, blank in (('magic_map', 'empty_magic_map'), ('maze_map', 'empty_maze_map'), ('ore_map', 'empty_ore_map')):
    RECIPES[f'{full}_cloning'] = {'type': 'minecraft:crafting_transmute', 'category': 'misc', 'group': f'{NS}:{full}_cloning',
                                  'input': n(full), 'material': n(blank), 'material_count': {'min': 1, 'max': 8},
                                  'add_material_count_to_result': True, 'result': {}}

# ================================================================= TFAchievementPage -> advancements
HUNTER_PREY = ['fire_beetle', 'slime_beetle', 'pinch_beetle', 'death_tome', 'hostile_wolf', 'wraith', 'kobold', 'minotaur', 'hedge_spider', 'lich',
               'swarm_spider', 'block_goblin', 'helmet_crab', 'lich_minion', 'tiny_bird', 'penguin', 'wild_deer', 'bunny', 'bighorn_sheep',
               'squirrel', 'wild_boar', 'redcap', 'naga', 'mosquito_swarm', 'hydra', 'skeleton_druid']
tag('entity_type', f'{NS}:hunter_prey', *[n(m) for m in HUNTER_PREY if m != 'hydra'])
# the hydra is registered by TFHydra; optional so that the tag loads without it
TAGS[('entity_type', f'{NS}:hunter_prey')].append({'id': n('hydra'), 'required': False})


def killed(mob):
    return {'trigger': 'minecraft:player_killed_entity', 'conditions': {'entity': {'type': 'minecraft:entity_properties', 'entity': 'this',
                                                                                     'predicate': {'minecraft:entity_type': mob}}}}


def has(*items):
    return {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'items': n(i)} for i in items]}}


def crafted(recipe):
    return {'trigger': 'minecraft:recipe_crafted', 'conditions': {'recipes': n(recipe)}}


IMPOSSIBLE = {'trigger': 'minecraft:impossible'}
ARRIVED = {'trigger': 'minecraft:changed_dimension', 'conditions': {'to': n('twilight_forest')}}
# (advancement, old key, icon, parent, frame, criteria, en title, en description, de title, de description)
ADV = [
    ('portal', 'twilightPortal', 'minecraft:diamond', None, 'challenge', {'done': IMPOSSIBLE, 'arrived': ARRIVED},
     'Twilight Portal', 'Build a portal to the Twilight Forest', 'Zwielichtportal', 'Baue ein Portal in den Zwielichtwald'),
    ('arrival', 'twilightArrival', 'mining_log', 'portal', 'task', {'arrived': ARRIVED},
     'Twilight Arrival', 'Arrive in the Twilight Forest', 'Ankunft im Zwielicht', 'Komm im Zwielichtwald an'),
    ('hunter', 'twilightHunter', 'raven_feather', 'arrival', 'task', {'killed': killed(f'#{NS}:hunter_prey')},
     'Twilight Hunter', 'Hunt some of the local wildlife', 'Zwielichtjäger', 'Jage etwas von der heimischen Tierwelt'),
    ('magic_map_focus', 'twilightMagicMapFocus', 'magic_map_focus', 'hunter', 'task', {'crafted': crafted('magic_map_focus')},
     'With Fire It Writes', "Craft the magic map focus with a raven's feather, glowstone dust, and torchberries",
     'Mit Feuer geschrieben', 'Stelle den magischen Kartenfokus aus Rabenfeder, Glowstonestaub und Fackelbeeren her'),
    ('naga', 'twilightNaga', 'naga_scale', 'hunter', 'task', {'killed': killed(n('naga'))},
     'Naga Hunter', 'Slay the naga', 'Nagajäger', 'Erschlage die Naga'),
    ('naga_armors', 'twilightNagaArmors', 'naga_chestplate', 'naga', 'challenge', {'has': has('naga_chestplate', 'naga_leggings')},
     'Naga Armorer', 'Craft naga scale chest and leg armor', 'Naga-Rüstmeister', 'Stelle Wams und Beinlinge aus Nagaschuppen her'),
    ('lich', 'twilightLich', 'twilight_scepter', 'hunter', 'task', {'killed': killed(n('lich'))},
     'Lich Hunter', 'Slay the lich', 'Lichjäger', 'Erschlage den Lich'),
    ('lich_scepters', 'twilightLichScepters', 'zombie_scepter', 'lich', 'challenge',
     {'has': has('twilight_scepter', 'lifedrain_scepter', 'zombie_scepter')},
     'Scepter Mastery', 'Acquire all three scepters of power', 'Meister der Zepter', 'Besitze alle drei Zepter der Macht'),
    ('hill1', 'twilightHill1', 'minecraft:iron_ore', 'arrival', 'task', {'done': IMPOSSIBLE},
     'The Boots Are Mine!', 'Defeat a redcap goblin in a small hollow hill', 'Die Stiefel gehören mir!',
     'Besiege einen Rotkappen-Kobold in einem kleinen hohlen Hügel'),
    ('hill2', 'twilightHill2', 'minecraft:gold_ore', 'arrival', 'task', {'done': IMPOSSIBLE},
     'Medium Hill [NYI]', 'Defeat a (no unique monster) in a medium hollow hill', 'Mittlerer Hügel [NYI]',
     'Besiege ein (kein besonderes Monster) in einem mittleren hohlen Hügel'),
    ('hill3', 'twilightHill3', 'minecraft:diamond_ore', 'arrival', 'task', {'done': IMPOSSIBLE},
     'I See Right Through You', 'Defeat a twilight wraith in a large hollow hill', 'Ich durchschaue dich',
     'Besiege ein Zwielicht-Gespenst in einem großen hohlen Hügel'),
    ('hedge', 'twilightHedge', 'hedge', 'arrival', 'task', {'done': IMPOSSIBLE},
     'Bug Stomper', 'Defeat a spider in a hedge maze', 'Käferzertreter', 'Besiege eine Spinne in einem Heckenlabyrinth'),
    ('magic_map', 'twilightMagicMap', 'magic_map', 'magic_map_focus', 'task',
     {'crafted': crafted('empty_magic_map'), 'has': has('magic_map'), 'done': IMPOSSIBLE},
     'I Can See For Miles', 'Craft the magic map', 'Ich sehe meilenweit', 'Stelle die magische Karte her'),
    ('maze_map', 'twilightMazeMap', 'maze_map', 'hill1', 'task',
     {'crafted': crafted('empty_maze_map'), 'has': has('maze_map'), 'done': IMPOSSIBLE},
     'And Now, to Find the Exit', 'Craft the maze map after obtaining the focus from a goblin', 'Und jetzt den Ausgang finden',
     'Stelle die Labyrinthkarte her, nachdem du den Fokus von einem Kobold bekommen hast'),
    ('ore_map', 'twilightOreMap', 'ore_map', 'maze_map', 'challenge',
     {'crafted': crafted('empty_ore_map'), 'crafted2': crafted('empty_ore_map_from_maze_map'), 'has': has('ore_map'), 'done': IMPOSSIBLE},
     'How Can That Be Worth It?', 'Craft the maze/ore map', 'Wie kann sich das lohnen?', 'Stelle die Labyrinth-/Erzkarte her'),
]
# the three map advancements need the map items and their recipes (TFMaps); 'done' is awarded when a blank map is used
MAPS = True
for adv, old, icon, parent, frame, criteria, en_t, en_d, de_t, de_d in ADV:
    if not MAPS and adv in ('magic_map', 'maze_map', 'ore_map'):
        continue
    obj = {'display': {'icon': {'id': icon if ':' in icon else n(icon)}, 'title': {'translate': f'advancements.{NS}.{adv}.title'},
                       'description': {'translate': f'advancements.{NS}.{adv}.description'}, 'frame': frame},
           'criteria': criteria, 'requirements': [list(criteria.keys())]}
    if parent:
        obj['parent'] = n(parent)
    else:
        obj['display']['background'] = 'minecraft:block/obsidian'
    write(f'data/{NS}/advancement/{adv}.json', obj)
    names(f'advancements.{NS}.{adv}.title', en_t, de_t)
    names(f'advancements.{NS}.{adv}.description', en_d, de_d)

# ================================================================= the uncrafting table (BlockTFUncraftingTable, ContainerTFUncrafting)
tex('uncrafting_top', 'uncrafting_top')
tex('uncrafting_side', 'uncrafting_side')
ASSETS.append(('mods/twilightforest/textures/gui/guigoblintinkering.png', f'assets/{NS}/textures/gui/uncrafting.png'))
model('uncrafting_table', {'parent': 'minecraft:block/cube_bottom_top', 'textures': {'top': n('block/uncrafting_top'), 'bottom': n('block/uncrafting_side'),
                                                                                     'side': n('block/uncrafting_side')}})
simple_state('uncrafting_table')
item_def('uncrafting_table', n('block/uncrafting_table'))
block_name('uncrafting_table', 'Uncrafting Table', 'Entwerkbank')
names(f'container.{NS}.uncrafting_table', 'Uncrafting Table', 'Entwerkbank')
self_drop('uncrafting_table')
btag('minecraft:mineable/axe', 'uncrafting_table')
shaped_c('uncrafting_table', ['###', '#X#', '###'], {'#': I + 'crafting_table', 'X': n('maze_map_focus')}, n('uncrafting_table'), None, 1, 'misc')
names(f'jei.{NS}.uncrafting_table',
      'Put a crafted item in the left slot: it comes apart into the ingredients of its shaped recipe. Taking them costs one level per part '
      '(not sticks); damaged items lose some parts. While the parts are shown, items put into the grid replace them: a tool or armour '
      'piece of the same kind keeps its name and fitting enchantments, for the level cost on the right. Without an input it is a crafting table.',
      'Lege einen hergestellten Gegenstand links hinein: Er zerfällt in die Zutaten seines geformten Rezepts. Sie zu nehmen kostet eine '
      'Stufe je Teil (keine Stöcke); beschädigte Gegenstände verlieren Teile. Solange die Teile angezeigt werden, ersetzen Gegenstände im '
      'Raster sie: Ein Werkzeug oder Rüstungsteil derselben Art behält Namen und passende Verzauberungen, für die Stufenkosten rechts. '
      'Ohne Eingabe ist sie eine Werkbank.')
