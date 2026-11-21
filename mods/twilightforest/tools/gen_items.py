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
