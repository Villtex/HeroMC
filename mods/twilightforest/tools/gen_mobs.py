# Teil 24c: the mobs (run from gen.py, which defines write, n, NS, ASSETS, LANG_EN_B, LANG_DE_B, TAGS, tag)
import json
import zipfile

# id, English name (the old entity name), German name, egg colours (TwilightForestMod.registerCreatures) or None
MOBS = [
    ('wild_boar', 'Wild Boar', 'Wildschwein', (8611131, 16773066)),
    ('bighorn_sheep', 'Bighorn Sheep', 'Mufflonschaf', (14405295, 14141297)),
    ('wild_deer', 'Wild Deer', 'Wilder Hirsch', (8080686, 4924445)),
    ('redcap', 'Redcap', 'Rotkappe', (3881580, 11214356)),
    ('swarm_spider', 'Swarm Spider', 'Schwarmspinne', (3277358, 1516830)),
    ('skeleton_druid', 'Skeleton Druid', 'Skelett-Druide', (10724259, 2767639)),
    ('hostile_wolf', 'Hostile Wolf', 'Feindseliger Wolf', (14144467, 11214356)),
    ('wraith', 'Twilight Wraith', 'Zwielicht-Gespenst', (5263440, 8618883)),
    ('hedge_spider', 'Hedge Spider', 'Heckenspinne', (2318099, 5645907)),
    ('penguin', 'Penguin', 'Pinguin', (1185051, 16379346)),
    ('tiny_bird', 'Tiny Bird', 'Winziger Vogel', (3386077, 1149166)),
    ('squirrel', 'Forest Squirrel', 'Wald-Eichhörnchen', (9457426, 15658734)),
    ('bunny', 'Forest Bunny', 'Wald-Häschen', (16711406, 13413017)),
    ('raven', 'Forest Raven', 'Waldrabe', (17, 2236979)),
    ('quest_ram', 'Questing Ram', 'Suchender Widder', None),
    ('kobold', 'Twilight Kobold', 'Kobold', (3612822, 9002267)),
    ('mosquito_swarm', 'Mosquito Swarm', 'Mückenschwarm', (526596, 2961185)),
    ('death_tome', 'Death Tome', 'Todesbuch', (7818786, 14405054)),
    ('minotaur', 'Minotaur', 'Minotaurus', (4141092, 11173222)),
    ('fire_beetle', 'Fire Beetle', 'Feuerkäfer', (1903360, 13332261)),
    ('slime_beetle', 'Slime Beetle', 'Schleimkäfer', (792070, 6334284)),
    ('pinch_beetle', 'Pinch Beetle', 'Zangenkäfer', (12358439, 2364937)),
    ('maze_slime', 'Maze Slime', 'Labyrinthschleim', (10724259, 2767639)),
    ('redcap_sapper', 'Redcap Sapper', 'Rotkappen-Pionier', (5725473, 11214356)),
    ('mist_wolf', 'Mist Wolf', 'Nebelwolf', (3806225, 14862474)),
    ('king_spider', 'King Spider', 'Spinnenkönig', (2890254, 16760855)),
    ('firefly', 'Firefly', 'Glühwürmchen', (10801942, 12250626)),
    ('mini_ghast', 'Mini Ghast', 'Mini-Ghast', (12369084, 10961731)),
    ('tower_ghast', 'Tower Ghast', 'Turm-Ghast', (12369084, 12023928)),
    ('tower_golem', 'Carminite Golem', 'Karminit-Golem', (7028000, 14867930)),
    ('tower_termite', 'Tower Termite', 'Turm-Termite', (6105889, 11313210)),
    ('tower_broodling', 'Redscale Broodling', 'Rotschuppen-Brütling', (3423252, 12250626)),
    ('block_goblin', 'Block&Chain Goblin', 'Kettengoblin', (3881580, 11214356)),
    ('upper_goblin_knight', 'Upper Goblin Knight', 'Goblin-Ritter (oben)', None),
    ('lower_goblin_knight', 'Goblin Knight', 'Goblin-Ritter', (3881580, 11214356)),
    ('helmet_crab', 'Helmet Crab', 'Helmkrabbe', (16486475, 13887420)),
]
PROJECTILES = [('nature_bolt', 'Nature Bolt', 'Naturbolzen'), ('tome_bolt', 'Tome Bolt', 'Buchbolzen'),
               ('slime_blob', 'Slime Blob', 'Schleimklumpen')]

for mob, en, de, egg in MOBS:
    LANG_EN_B[f'entity.{NS}.{mob}'] = en
    LANG_DE_B[f'entity.{NS}.{mob}'] = de
    if egg:
        name = f'{mob}_spawn_egg'
        write(f'assets/{NS}/models/item/{name}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'minecraft:item/egg'}})
        write(f'assets/{NS}/items/{name}.json', {'model': {'type': 'minecraft:model', 'model': n(f'item/{name}'),
                                                           'tints': [{'type': 'minecraft:constant', 'value': (0xFF000000 | egg[0]) - (1 << 32)}]}})
        LANG_EN_B[f'item.{NS}.{name}'] = f'{en} Spawn Egg'
        LANG_DE_B[f'item.{NS}.{name}'] = f'Spawn-Ei ({de})'
for p, en, de in PROJECTILES:
    LANG_EN_B[f'entity.{NS}.{p}'] = en
    LANG_DE_B[f'entity.{NS}.{p}'] = de

# entity textures (textures/model of the original; names in lower case with underscores)
for src in ['wildboar', 'bighorn', 'wilddeer', 'redcap', 'redcapsapper', 'swarmspider', 'skeletondruid', 'hedgespider', 'kingspider',
            'penguin', 'tinybirdbrown', 'tinybirdblue', 'tinybirdred', 'tinybirdgold', 'squirrel2', 'bunnydutch', 'bunnywhite',
            'bunnybrown', 'raven', 'questram', 'questram_lines', 'kobold', 'mosquitoswarm', 'minotaur', 'firebeetle', 'slimebeetle',
            'pinchbeetle', 'mazeslime', 'mistwolf', 'firefly-tiny', 'ghost-crown', 'ghost', 'towerghast', 'towerghast_openeyes',
            'towerghast_fire', 'carminitegolem', 'towertermite', 'towerbroodling', 'blockgoblin', 'doublegoblin', 'helmetcrab']:
    dst = {'squirrel2': 'squirrel'}.get(src, src).replace('-', '_')
    ASSETS.append((f'mods/twilightforest/textures/model/{src}.png', f'assets/{NS}/textures/entity/{dst}.png'))

# items the mobs drop (ItemTF / ItemTFFood; the meats look like beef: setLooksLike)
for i, tex_ref, en, de in [('raven_feather', None, 'Raven Feather', 'Rabenfeder'),
                           ('venison', 'minecraft:item/beef', 'Raw Venison', 'Rohes Wildbret'),
                           ('cooked_venison', 'minecraft:item/cooked_beef', 'Venison Steak', 'Wildbretsteak'),
                           ('raw_meef', 'minecraft:item/beef', 'Raw Meef', 'Rohes Meef'),
                           ('meef_steak', 'minecraft:item/cooked_beef', 'Meef Steak', 'Meef-Steak'),
                           ('magic_map_focus', None, 'Magic Map Focus', 'Magischer Kartenfokus'),
                           ('maze_map_focus', None, 'Maze Map Focus', 'Labyrinth-Kartenfokus'),
                           ('borer_essence', None, 'Borer Essence', 'Bohreressenz')]:
    if tex_ref is None:
        src = {'raven_feather': 'tfFeather', 'magic_map_focus': 'magicMapFocus', 'maze_map_focus': 'mazeMapFocus', 'borer_essence': 'borerEssence'}[i]
        ASSETS.append((f'mods/twilightforest/textures/items/{src}.png', f'assets/{NS}/textures/item/{i}.png'))
        tex_ref = n(f'item/{i}')
    write(f'assets/{NS}/models/item/{i}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': tex_ref}})
    write(f'assets/{NS}/items/{i}.json', {'model': {'type': 'minecraft:model', 'model': n(f'item/{i}')}})
    LANG_EN_B[f'item.{NS}.{i}'] = en
    LANG_DE_B[f'item.{NS}.{i}'] = de
tag('item', 'minecraft:meat', n('venison'), n('cooked_venison'), n('raw_meef'), n('meef_steak'))
tag('item', 'c:foods/raw_meat', n('venison'), n('raw_meef'))
tag('item', 'c:foods/cooked_meat', n('cooked_venison'), n('meef_steak'))
tag('item', 'minecraft:wolf_food', n('venison'), n('cooked_venison'), n('raw_meef'), n('meef_steak'))
# smelting and the campfire (ItemFood of 1.5.2: FurnaceRecipes for venison and meef)
for raw, cooked in (('venison', 'cooked_venison'), ('raw_meef', 'meef_steak')):
    for kind, t in (('smelting', 200), ('smoking', 100), ('campfire_cooking', 600)):
        RECIPES[f'{cooked}_from_{kind}'] = {'type': f'minecraft:{kind}', 'category': 'food', 'ingredient': n(raw),
                                            'result': {'id': n(cooked)}, 'experience': 0.35, 'cookingtime': t}

# undead and arthropods (smite, bane of arthropods)
tag('entity_type', 'minecraft:undead', n('skeleton_druid'))
ARTHROPODS = [n(x) for x in ('swarm_spider', 'hedge_spider', 'king_spider', 'fire_beetle', 'slime_beetle', 'pinch_beetle', 'tower_termite',
                               'tower_broodling', 'helmet_crab')]
tag('entity_type', 'minecraft:arthropod', *ARTHROPODS)
tag('entity_type', 'minecraft:sensitive_to_bane_of_arthropods', *ARTHROPODS)
tag('entity_type', 'minecraft:sensitive_to_smite', n('skeleton_druid'))

# ---------------------------------------------------------------- sounds (TFSounds)
SOUND_FILES = {
    'tiny_bird.chirp': [('tinybird/tinybirdchirp%d.ogg' % i, 'tiny_bird/chirp%d' % i) for i in range(3)],
    'tiny_bird.hurt': [('tinybird/tinybirdhurt%d.ogg' % i, 'tiny_bird/hurt%d' % i) for i in range(2)],
    'raven.caw': [('raven/ravencaw%d.ogg' % i, 'raven/caw%d' % i) for i in range(2)],
    'raven.squawk': [('raven/ravensquawk%d.ogg' % i, 'raven/squawk%d' % i) for i in range(2)],
    'redcap.ambient': [('redcap/redcap%d.ogg' % i, 'redcap/redcap%d' % i) for i in range(6)],
    'redcap.hurt': [('redcap/redcaphurt%d.ogg' % i, 'redcap/hurt%d' % i) for i in range(4)],
    'redcap.die': [('redcap/redcapdie%d.ogg' % i, 'redcap/die%d' % i) for i in range(3)],
    'kobold.ambient': [('kobold/kobold growl 1.ogg', 'kobold/kobold0'), ('kobold/kobold growl 2.ogg', 'kobold/kobold1'),
                       ('kobold/kobold growl 3.ogg', 'kobold/kobold2'), ('kobold/kobold sniff 1.ogg', 'kobold/kobold3'),
                       ('kobold/kobold snort 1.ogg', 'kobold/kobold4'), ('kobold/kobold snort 2.ogg', 'kobold/kobold5')],
    'kobold.hurt': [('kobold/kobold hurt %d.ogg' % (i + 1), 'kobold/hurt%d' % i) for i in range(3)],
    'kobold.die': [('kobold/kobold die %d.ogg' % (i + 1), 'kobold/die%d' % i) for i in range(3)],
    'wraith': [('wraith/wraith%d.ogg' % i, 'wraith/wraith%d' % i) for i in range(4)],
    'mosquito': [('mosquito/animals132.wav', 'mosquito/mosquito0')],
}
SOUNDS_JSON = {'cicada': {'sounds': [n('cicada2'), n('cicada3')], 'subtitle': f'subtitles.{NS}.cicada'}}
for event, files in SOUND_FILES.items():
    SOUNDS_JSON[event] = {'sounds': [n(dst) for src, dst in files], 'subtitle': f'subtitles.{NS}.{event}'}
    for src, dst in files:
        ASSETS.append((f'mods/twilightforest/sounds/{src}', f'assets/{NS}/sounds/{dst}.ogg'))
    LANG_EN_B[f'subtitles.{NS}.{event}'] = {'tiny_bird.chirp': 'Tiny bird chirps', 'tiny_bird.hurt': 'Tiny bird hurts',
                                            'raven.caw': 'Raven caws', 'raven.squawk': 'Raven squawks', 'redcap.ambient': 'Redcap mutters',
                                            'redcap.hurt': 'Redcap hurts', 'redcap.die': 'Redcap dies', 'kobold.ambient': 'Kobold growls',
                                            'kobold.hurt': 'Kobold hurts', 'kobold.die': 'Kobold dies', 'wraith': 'Wraith moans',
                                            'mosquito': 'Mosquitoes buzz'}[event]
    LANG_DE_B[f'subtitles.{NS}.{event}'] = {'tiny_bird.chirp': 'Vogel zwitschert', 'tiny_bird.hurt': 'Vogel verletzt',
                                            'raven.caw': 'Rabe krächzt', 'raven.squawk': 'Rabe kreischt', 'redcap.ambient': 'Rotkappe murmelt',
                                            'redcap.hurt': 'Rotkappe verletzt', 'redcap.die': 'Rotkappe stirbt', 'kobold.ambient': 'Kobold knurrt',
                                            'kobold.hurt': 'Kobold verletzt', 'kobold.die': 'Kobold stirbt', 'wraith': 'Gespenst stöhnt',
                                            'mosquito': 'Mücken summen'}[event]
write(f'assets/{NS}/sounds.json', SOUNDS_JSON)

# ---------------------------------------------------------------- loot tables (dropFewItems / dropRareDrop of 1.5.2)
SMELT = {'type': 'minecraft:furnace_smelt', 'condition': {'type': 'minecraft:any_of', 'terms': [
    {'type': 'minecraft:entity_properties', 'entity': 'this', 'predicate': {'minecraft:flags': {'is_on_fire': True}}},
    {'type': 'minecraft:entity_properties', 'entity': 'direct_attacker',
     'predicate': {'minecraft:equipment': {'mainhand': {'predicates': {'minecraft:enchantments': [{'enchantments': '#minecraft:smelts_loot'}]}}}}}]}}


def count(mn, mx):
    return mn if mn == mx else {'type': 'minecraft:uniform', 'min': mn, 'max': mx}


def drop(item, mn=0, mx=2, looting=1, smelt=False):
    """rand(mx - mn + 1) + mn of the item, + rand(1 + looting) per looting level (dropFewItems)."""
    mods = [{'type': 'minecraft:set_count', 'count': count(mn, mx)}]
    if smelt:
        mods.append(SMELT)
    if looting:
        mods.append({'type': 'minecraft:enchanted_count_increase', 'count': {'type': 'minecraft:uniform', 'min': 0.0, 'max': float(looting)},
                     'enchantment': 'minecraft:looting'})
    return {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': item, 'modifier': mods}]}


def rare(item):
    """dropRareDrop: killed by a player, rand(200) - looting < 5."""
    return {'rolls': 1, 'condition': {'type': 'minecraft:all_of', 'terms': [{'type': 'minecraft:killed_by_player'}, {
        'type': 'minecraft:random_chance_with_enchanted_bonus', 'enchantment': 'minecraft:looting', 'unenchanted_chance': 0.025,
        'enchanted_chance': {'type': 'minecraft:linear', 'base': 0.03, 'per_level_above_first': 0.005}}]},
            'entries': [{'type': 'minecraft:item', 'name': item}]}


def mob_loot(mob, pools):
    write(f'data/{NS}/loot_table/entities/{mob}.json', {'type': 'minecraft:entity', 'pools': pools, 'random_sequence': n(f'entities/{mob}')})


VANILLA = None


def vanilla_loot(name):
    """A vanilla loot table out of the 26.3 jar (for the spiders, the sheep's wool and the slime)."""
    global VANILLA
    import glob
    import os
    if VANILLA is None:
        jars = glob.glob('/root/.gradle/caches/neoformruntime/intermediate_results/applyDevTransforms_*_output.jar')
        VANILLA = zipfile.ZipFile(jars[0]) if jars else None
    if VANILLA is None:
        return None
    return json.loads(VANILLA.read(f'data/minecraft/loot_table/entities/{name}.json'))


mob_loot('wild_boar', [drop('minecraft:porkchop', smelt=True)])
mob_loot('wild_deer', [drop('minecraft:leather'), drop(n('venison'), 1, 3, smelt=True)])
mob_loot('penguin', [drop('minecraft:feather')])
mob_loot('tiny_bird', [drop('minecraft:feather')])
mob_loot('raven', [drop(n('raven_feather'))])
mob_loot('redcap', [drop('minecraft:coal')])
mob_loot('redcap_sapper', [drop('minecraft:coal')])
mob_loot('wraith', [drop('minecraft:glowstone_dust')])
mob_loot('kobold', [drop('minecraft:wheat'), {'rolls': 1, 'condition': {'type': 'minecraft:random_chance', 'chance': 0.5}, 'entries': [
    {'type': 'minecraft:item', 'name': 'minecraft:gold_nugget', 'modifier': [
        {'type': 'minecraft:enchanted_count_increase', 'count': {'type': 'minecraft:uniform', 'min': 1.0, 'max': 1.0},
         'enchantment': 'minecraft:looting'}]}]}])
mob_loot('skeleton_druid', [drop(n('torchberries')), drop('minecraft:bone')])
mob_loot('minotaur', [drop(n('raw_meef'), 0, 1, smelt=True), rare(n('maze_map_focus'))])
mob_loot('death_tome', [drop('minecraft:paper'), {'rolls': 1, 'entries': [{'type': 'minecraft:alternatives', 'children': [
    {'type': 'minecraft:item', 'name': 'minecraft:writable_book', 'condition': {
        'type': 'minecraft:random_chance_with_enchanted_bonus', 'enchantment': 'minecraft:looting', 'unenchanted_chance': 0.2,
        'enchanted_chance': {'type': 'minecraft:linear', 'base': 0.4, 'per_level_above_first': 0.2}}},
    {'type': 'minecraft:item', 'name': 'minecraft:book'}]}]}, rare(n('magic_map_focus'))])
mob_loot('fire_beetle', [drop('minecraft:gunpowder')])
mob_loot('slime_beetle', [drop('minecraft:slime_ball')])
# EntityGhast: 0..2 gunpowder, 0..1 ghast tears (+ looting)
for g in ('tower_ghast', 'mini_ghast'):
    mob_loot(g, [drop('minecraft:gunpowder'), drop('minecraft:ghast_tear', 0, 1)])
mob_loot('tower_golem', [drop('minecraft:iron_ingot', looting=0), drop(n('tower_wood'), looting=0)])
mob_loot('tower_termite', [drop(n('borer_essence'))])
mob_loot('block_goblin', [drop('minecraft:coal')])
mob_loot('helmet_crab', [drop('minecraft:wheat'), {'rolls': 1, 'condition': {'type': 'minecraft:random_chance', 'chance': 0.5}, 'entries': [
    {'type': 'minecraft:item', 'name': 'minecraft:gold_nugget', 'modifier': [
        {'type': 'minecraft:enchanted_count_increase', 'count': {'type': 'minecraft:uniform', 'min': 1.0, 'max': 1.0},
         'enchantment': 'minecraft:looting'}]}]}])
for empty in ('squirrel', 'bunny', 'quest_ram', 'firefly', 'mosquito_swarm', 'pinch_beetle', 'hostile_wolf', 'mist_wolf', 'upper_goblin_knight',
              'lower_goblin_knight'):
    mob_loot(empty, [])
spider = vanilla_loot('spider')
if spider:
    for s in ('swarm_spider', 'hedge_spider', 'king_spider', 'tower_broodling'):
        mob_loot(s, spider['pools'])
sheep = vanilla_loot('sheep')
if sheep:
    mob_loot('bighorn_sheep', sheep['pools'][1:])
slime = vanilla_loot('slime')
if slime:
    mob_loot('maze_slime', slime['pools'] + [rare(n('charm_of_keeping_1'))])

# ---------------------------------------------------------------- biome spawn lists (TFBiomeBase and its subclasses)
def spawn(t, weight, mn, mx):
    return {'type': t, 'weight': weight, 'count': count(mn, mx)}


BASE_CREATURES = [spawn(n('bighorn_sheep'), 12, 4, 4), spawn(n('wild_boar'), 10, 4, 4), spawn('minecraft:chicken', 10, 4, 4),
                  spawn(n('wild_deer'), 15, 4, 5), spawn('minecraft:wolf', 5, 4, 4), spawn(n('tiny_bird'), 15, 4, 8),
                  spawn(n('squirrel'), 10, 2, 4), spawn(n('bunny'), 10, 4, 5), spawn(n('raven'), 10, 1, 2)]
BASE_AMBIENT = [spawn('minecraft:bat', 10, 8, 8), spawn(n('firefly'), 10, 8, 8)]


def biome_spawns(name):
    creature = list(BASE_CREATURES)
    monster = []
    water = []
    if name == 'glacier':
        creature.append(spawn(n('penguin'), 10, 4, 4))
    if name == 'twilight_stream':
        creature = []
    if name == 'twilight_lake':
        water = [spawn('minecraft:squid', 10, 4, 4)]
    if name == 'twilight_swamp':
        monster = [spawn(n('mosquito_swarm'), 10, 1, 1), spawn('minecraft:creeper', 10, 4, 4), spawn('minecraft:zombie', 10, 4, 4)]
    if name == 'dark_forest':
        monster = [spawn('minecraft:enderman', 1, 1, 4), spawn('minecraft:zombie', 5, 1, 4), spawn('minecraft:skeleton', 5, 1, 4),
                   spawn(n('mist_wolf'), 10, 1, 4), spawn(n('skeleton_druid'), 10, 1, 4), spawn(n('king_spider'), 10, 1, 4),
                   spawn(n('kobold'), 10, 4, 8)]
    spawns = {k: [] for k in ['axolotls', 'misc', 'underground_water_creature', 'water_ambient']}
    spawns.update({'ambient': BASE_AMBIENT, 'creature': creature, 'monster': monster, 'water_creature': water})
    return spawns


for name in BIOMES:
    path = os.path.join(ROOT, f'data/{NS}/worldgen/biome/{name}.json')
    with open(path) as f:
        biome = json.load(f)
    biome['attributes']['minecraft:gameplay/natural_mob_spawns']['argument']['spawns_by_category'] = biome_spawns(name)
    write(f'data/{NS}/worldgen/biome/{name}.json', biome)
