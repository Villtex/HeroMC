#!/usr/bin/env python3
"""Generates the assets/data JSON of the Falling Meteors port (mods/meteorsmod/src/main/resources), the language files
(from the original en_US/de_DE.lang in .reference/meteorsmod) and original-assets.txt. Everything under
resources/assets and resources/data is overwritten."""
import json, os, shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
REPO = os.path.dirname(os.path.dirname(ROOT))
RES = ROOT + '/src/main/resources'
NS = 'meteors'
LANG_SRC = REPO + '/.reference/meteorsmod/src/meteor/lang/'
for d in ('assets', 'data'):
    shutil.rmtree(os.path.join(RES, d), ignore_errors=True)


def _lower(obj):
    """26.3 resource paths are lowercase only: every own resource id in the JSON is written in lower case."""
    if isinstance(obj, str):
        return obj.lower() if obj.startswith(NS + ':') else obj
    if isinstance(obj, list):
        return [_lower(x) for x in obj]
    if isinstance(obj, dict):
        return {k: _lower(v) for k, v in obj.items()}
    return obj


def write(path, obj):
    path = path.lower()
    obj = _lower(obj)
    full = os.path.join(RES, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, 'w') as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write('\n')


def n(x):
    return f'{NS}:{x}'


def read_lang(name):
    out = {}
    for line in open(LANG_SRC + name, encoding='utf-8'):
        line = line.strip()
        if line and not line.startswith('#') and '=' in line:
            k, v = line.split('=', 1)
            out[k] = v
    return out


old = {'en_us': read_lang('en_US.lang'), 'de_de': read_lang('de_DE.lang')}
lang = {'en_us': {}, 'de_de': {}}


def name(key, old_key):
    for l in lang:
        v = old[l].get(old_key, old['en_us'].get(old_key))
        if v is not None:
            lang[l][key] = v


def raw(key, english, german=None):
    lang['en_us'][key] = english
    lang['de_de'][key] = german or old['de_de'].get(key, english)


assets = []


def tex(src, dst):
    assets.append((src, f'assets/{NS}/textures/{dst}.png'))


def plain(m):
    return {'type': 'minecraft:model', 'model': m}


def item_def(item, model):
    write(f'assets/{NS}/items/{item}.json', {'model': model})


def flat(item, texture, old_key, handheld=False):
    tex(f'textures/items/{texture}.png', f'item/{texture}')
    write(f'assets/{NS}/models/item/{item}.json', {'parent': 'minecraft:item/handheld' if handheld else 'minecraft:item/generated',
                                                    'textures': {'layer0': n(f'item/{texture}')}})
    item_def(item, plain(n(f'item/{item}')))
    name(f'item.{NS}.{item}', old_key)


# ---------------------------------------------------------------- items
for item, texture, key in (('meteor_chips', 'MeteorChips', 'item.MeteorChips.name'), ('red_meteor_gem', 'RedMeteorGem', 'item.RedMeteorGem.name'),
                           ('frezarite_crystal', 'FrezariteCrystal', 'item.FrezariteCrystal.name'),
                           ('kreknorite_chip', 'KreknoriteChip', 'item.KreknoriteChip.name'),
                           ('vanilla_ice_cream', 'VanillaIceCream', 'item.VanillaIceCream.name'),
                           ('chocolate_ice_cream', 'ChocolateIceCream', 'item.ChocolateIceCream.name')):
    flat(item, texture, key)
for item, texture, key in (('meteor_summoner', 'summoner', 'random'), ('meteorite_meteor_summoner', 'sum_met', 'meteorite'),
                           ('frezarite_meteor_summoner', 'sum_frez', 'frezarite'), ('kreknorite_meteor_summoner', 'sum_krek', 'kreknorite'),
                           ('unknown_meteor_summoner', 'sum_unk', 'unknown'), ('kitty_comet_summoner', 'sum_kitty', 'kitty')):
    flat(item, texture, f'item.MeteorSummoner.{key}.name')
for mat, old_mat in (('meteorite', 'Meteorite'), ('frezarite', 'Frezarite'), ('kreknorite', 'Kreknorite')):
    for piece, old_piece in (('helmet', 'Helmet'), ('chestplate', 'Chest'), ('leggings', 'Legs'), ('boots', 'Boots')):
        flat(f'{mat}_{piece}', f'{old_mat}{old_piece}', f'item.{old_mat}{old_piece}.name')
    tex(f'armor/{old_mat}Armor_1.png', f'entity/equipment/humanoid/{mat}')
    tex(f'armor/{old_mat}Armor_2.png', f'entity/equipment/humanoid_leggings/{mat}')
    write(f'assets/{NS}/equipment/{mat}.json', {'layers': {'humanoid': [{'texture': n(mat)}], 'humanoid_leggings': [{'texture': n(mat)}]}})
for mat, old_mat in (('meteorite', 'Meteorite'), ('frezarite', 'Frezarite')):
    for tool, old_tool in (('axe', 'Axe'), ('shovel', 'Spade'), ('sword', 'Sword'), ('pickaxe', 'Pickaxe'), ('hoe', 'Hoe')):
        flat(f'{mat}_{tool}', f'{old_mat}{old_tool}', f'item.{old_mat}{old_tool}.name', True)
flat('kreknorite_sword', 'KreknoriteSword', 'item.KreknoriteSword.name', True)
write(f'data/{NS}/tags/item/no_repair.json', {'values': []})

DISPLAY = {'gui': {'rotation': [0, 0, 0], 'translation': [0, 0, 0], 'scale': [1, 1, 1]},
           'ground': {'translation': [0, 2, 0], 'scale': [0.5, 0.5, 0.5]},
           'thirdperson_righthand': {'translation': [0, 3, 1], 'scale': [0.55, 0.55, 0.55]},
           'firstperson_righthand': {'rotation': [0, -90, 25], 'translation': [1.13, 3.2, 1.13], 'scale': [0.68, 0.68, 0.68]},
           'fixed': {'rotation': [0, 180, 0]}}
# detectors: the 32-frame needle strip (TextureDetector), one flat quad per frame, picked by meteors:detector
for kind, (item, texture, key) in enumerate((('meteor_time_detector', 'MeteorDetectorTime', 'item.MeteorDetectorTime.name'),
                                             ('meteor_proximity_detector', 'MeteorDetectorProximity', 'item.MeteorDetectorProximity.name'),
                                             ('meteor_crash_detector', 'MeteorDetectorCrash', 'item.MeteorDetectorCrash.name'))):
    tex(f'textures/items/{texture}.png', f'item/{texture}')
    entries = []
    for f in range(32):
        uv = [0, f * 0.5, 16, f * 0.5 + 0.5]
        face = {'texture': '#strip', 'uv': uv}
        write(f'assets/{NS}/models/item/{item}_{f:02d}.json', {
            'gui_light': 'front', 'display': DISPLAY, 'textures': {'strip': n(f'item/{texture}'), 'particle': n(f'item/{texture}')},
            'elements': [{'from': [0, 0, 7.5], 'to': [16, 16, 8.5], 'faces': {'north': dict(face), 'south': dict(face)}}]})
        entries.append({'threshold': f / 32.0, 'model': plain(n(f'item/{item}_{f:02d}'))})
    item_def(item, {'type': 'minecraft:range_dispatch', 'property': n('detector'), 'kind': kind, 'entries': entries,
                    'fallback': plain(n(f'item/{item}_00'))})
    name(f'item.{NS}.{item}', key)

item_def('alien_creeper_spawn_egg', plain('minecraft:item/creeper_spawn_egg'))
item_def('comet_kitty_spawn_egg', plain('minecraft:item/cat_spawn_egg'))
raw(f'item.{NS}.alien_creeper_spawn_egg', 'Alien Creeper Spawn Egg', 'Alien-Creeper-Spawn-Ei')
raw(f'item.{NS}.comet_kitty_spawn_egg', 'Comet Kitty Spawn Egg', 'Kometenkatzen-Spawn-Ei')


# ---------------------------------------------------------------- blocks
def block_tex(t):
    tex(f'textures/blocks/{t}.png', f'block/{t}')
    return n(f'block/{t}')


def block_model(model, obj):
    write(f'assets/{NS}/models/block/{model}.json', obj)


def states(block, variants):
    write(f'assets/{NS}/blockstates/{block}.json', {'variants': variants})


def cube(block, texture, old_key):
    block_model(block, {'parent': 'minecraft:block/cube_all', 'textures': {'all': block_tex(texture)}})
    states(block, {'': {'model': n(f'block/{block}')}})
    item_def(block, plain(n(f'block/{block}')))
    name(f'block.{NS}.{block}', old_key)


def heated(block, cold, hot, old_key):
    block_model(block, {'parent': 'minecraft:block/cube_all', 'textures': {'all': block_tex(cold)}})
    if hot:
        block_model(block + '_hot', {'parent': 'minecraft:block/cube_all', 'textures': {'all': block_tex(hot)}})
    states(block, {f'heat={h}': {'model': n(f'block/{block}' + ('_hot' if hot and h > 0 else ''))} for h in range(5)})
    item_def(block, plain(n(f'block/{block}')))
    name(f'block.{NS}.{block}', old_key)


heated('fallen_meteor', 'Meteor', 'Meteor_Hot', 'tile.Meteor.name')
heated('rare_fallen_meteor', 'MeteorRare', 'Meteor_Hot', 'tile.MeteorRare.name')
heated('frezarite', 'Frezarite', None, 'tile.Frezarite.name')
heated('kreknorite', 'Kreknorite', None, 'tile.Kreknorite.name')
cube('meteor_ore', 'MeteorOre', 'tile.MeteorOre.name')
cube('red_meteor_gem_block', 'blockRedGem', 'tile.blockRedGem.name')

# meteor shield: top lit from power 1, gems on north (2+), south (3+), west (4+), east (5); all sides cracked at 5
gem, nogem, cracked = block_tex('sideGem'), block_tex('sideNoGem'), block_tex('shieldSideCracked')
top_lit, top_unlit, bottom = block_tex('shieldTop_lit'), block_tex('shieldTop_unlit'), block_tex('shieldBottom')
shield_states = {}
for p in range(6):
    def side(need):
        return cracked if p == 5 and need != 5 else (gem if p >= need else nogem)
    block_model(f'meteor_shield_{p}', {'parent': 'minecraft:block/cube', 'textures': {
        'up': top_lit if p > 0 else top_unlit, 'down': bottom, 'north': side(2), 'south': side(3), 'west': side(4),
        'east': gem if p == 5 else nogem, 'particle': nogem}})
    shield_states[f'power={p}'] = {'model': n(f'block/meteor_shield_{p}')}
states('meteor_shield', shield_states)
item_def('meteor_shield', plain(n('block/meteor_shield_0')))
name(f'block.{NS}.meteor_shield', 'tile.MeteorShield.name')

# meteor timer: half slab with the timer face on top (ModelMeteorTimer is drawn as the slab)
block_model('meteor_timer', {'parent': 'minecraft:block/slab', 'textures': {'top': block_tex('MeteorTimer'), 'bottom': n('block/MeteorTimer'),
                                                                             'side': block_tex('timerSide')}})
states('meteor_timer', {f'power={p}': {'model': n('block/meteor_timer')} for p in range(16)})
item_def('meteor_timer', plain(n('block/meteor_timer')))
name(f'block.{NS}.meteor_timer', 'tile.MeteorTimer.name')

# protected land tester (torch, lit while the land is shielded)
for active, t in ((False, 'ProtectedLandTester'), (True, 'ProtectedLandTesterActive')):
    suffix = '_active' if active else ''
    block_model('protected_land_tester' + suffix, {'parent': 'minecraft:block/template_torch', 'textures': {'torch': block_tex(t)}})
    block_model('wall_protected_land_tester' + suffix, {'parent': 'minecraft:block/template_torch_wall', 'textures': {'torch': n(f'block/{t}')}})
states('protected_land_tester', {f'active={str(a).lower()}': {'model': n('block/protected_land_tester' + ('_active' if a else ''))} for a in (False, True)})
states('wall_protected_land_tester', {f'active={str(a).lower()},facing={f}': {'model': n('block/wall_protected_land_tester' + ('_active' if a else '')),
                                                                              **({'y': y} if y else {})}
                                      for a in (False, True) for f, y in (('east', 0), ('south', 90), ('west', 180), ('north', 270))})
write(f'assets/{NS}/models/item/protected_land_tester.json', {'parent': 'minecraft:item/generated',
                                                               'textures': {'layer0': n('block/ProtectedLandTesterActive')}})
item_def('protected_land_tester', plain(n('item/protected_land_tester')))
name(f'block.{NS}.protected_land_tester', 'tile.ProtectedLandTester.name')
name(f'block.{NS}.wall_protected_land_tester', 'tile.ProtectedLandTester.name')

# ---------------------------------------------------------------- entities, particles, sounds
for t, file in (('falling_meteor', 'fallingMeteor'), ('freza_meteor', 'frezaMeteor'), ('krekno_meteor', 'kreknoMeteor'),
                ('unknown_meteor', 'unknownMeteor'), ('kitty', 'kitty'), ('meteorcreeper', 'meteorcreeper'), ('comet_kitty', 'cometKitty')):
    assets.append((f'meteor/textures/{file}.png', f'assets/{NS}/textures/entity/{t}.png'))
for e, english, german in (('falling_meteor', 'Falling Meteor', 'Fallender Meteorit'), ('meteor_summoner', 'Meteor Summoner', 'Meteorbeschwörer')):
    raw(f'entity.{NS}.{e}', english, german)
name(f'entity.{NS}.alien_creeper', 'entity.AlienCreeper.name')
name(f'entity.{NS}.comet_kitty', 'entity.CometKitty.name')
generic = [f'minecraft:generic_{i}' for i in range(7, -1, -1)]
for p in ['meteor_dust', 'freza_dust'] + [f'meteor_shield_{i}' for i in range(6)]:
    write(f'assets/{NS}/particles/{p}.json', {'textures': generic})
assets.append(('meteor/sound/meteor/crash.wav', f'assets/{NS}/sounds/meteor/crash.ogg'))
assets.append(('meteor/sound/shield/powerup.wav', f'assets/{NS}/sounds/shield/powerup.ogg'))
write(f'assets/{NS}/sounds.json', {'meteor.crash': {'sounds': [n('meteor/crash')], 'subtitle': f'subtitles.{NS}.meteor.crash'},
                                   'shield.powerup': {'sounds': [n('shield/powerup')], 'subtitle': f'subtitles.{NS}.shield.powerup'}})
raw(f'subtitles.{NS}.meteor.crash', 'Meteor crashes', 'Meteorit schlägt ein')
raw(f'subtitles.{NS}.shield.powerup', 'Meteor Shield charges', 'Meteoritenschild lädt')

# ---------------------------------------------------------------- texts the code uses with their old keys
for key in ('MeteorShield.charging', 'MeteorShield.noUpgrade', 'MeteorShield.PowerUpgradePercentage', 'MeteorShield.meteorBlocked',
            'MeteorTimer.modeChange.one', 'MeteorTimer.modeChange.two', 'ProtectionTorch.landOwnership', 'Meteor.netherPortalCreation',
            'Meteor.kittiesDetected.one', 'Meteor.kittiesDetected.two', 'Meteor.crashed', 'MeteorSummoner.incomingMeteor',
            'MeteorSummoner.wrongDimension', 'MeteorSummoner.landProtected'):
    name(key, key)
raw('Meteor.kittyAttack', 'Kitty attack!', 'Katzenangriff!')
for key in ('addEnch.one', 'addEnch.two', 'frezAxe.one', 'frezAxe.two', 'frezHoe.one', 'frezHoe.two', 'frezPickaxe.one', 'frezPickaxe.two',
            'frezSpade.one', 'frezSpade.two', 'frezSword.one', 'frezSword.two'):
    name(f'enchantment.{NS}.{key}', f'enchantment.{key}')
name(f'enchantment.{NS}.magnetization', 'enchantment.Magnetization')
name(f'enchantment.{NS}.cold_touch', 'enchantment.ColdTouch')
name(f'itemGroup.{NS}.meteor', 'itemGroup.Meteor')

# ---------------------------------------------------------------- enchantments (min 40 above max 31: never on the table)
write(f'data/{NS}/enchantment/magnetization.json', {
    'anvil_cost': 4, 'description': {'translate': f'enchantment.{NS}.magnetization'}, 'max_cost': {'base': 60, 'per_level_above_first': 0},
    'max_level': 1, 'min_cost': {'base': 40, 'per_level_above_first': 0}, 'slots': ['any'],
    'supported_items': '#minecraft:enchantable/durability', 'weight': 3})
write(f'data/{NS}/enchantment/cold_touch.json', {
    'anvil_cost': 4, 'description': {'translate': f'enchantment.{NS}.cold_touch'}, 'max_cost': {'base': 60, 'per_level_above_first': 0},
    'max_level': 1, 'min_cost': {'base': 40, 'per_level_above_first': 0}, 'slots': ['armor'],
    'supported_items': '#minecraft:enchantable/armor', 'weight': 3})

# ---------------------------------------------------------------- advancements (HandlerAchievement)
ADV = [('material_gather', 'materialGather', 'meteor_chips', None), ('shield_crafted', 'shieldCrafted', 'meteor_shield', 'material_gather'),
       ('shield_fully_upgraded', 'shieldFullyUpgraded', 'red_meteor_gem', 'shield_crafted'),
       ('meteor_blocked', 'meteorBlocked', 'protected_land_tester', 'shield_crafted'),
       ('attracted_drop', 'attractedDrop', 'meteorite_pickaxe', 'material_gather'),
       ('crafted_krekno_sword', 'craftedKreknoSword', 'kreknorite_sword', 'attracted_drop'),
       ('crafted_detector', 'craftedDetector', 'meteor_proximity_detector', 'material_gather'),
       ('found_meteor', 'foundMeteor', 'fallen_meteor', 'crafted_detector'), ('crafted_meteor_timer', 'craftedMeteorTimer', 'meteor_timer', 'crafted_detector'),
       ('summon_meteor', 'summonMeteor', 'meteor_summoner', 'material_gather'), ('comet_kitty_event', 'cometKittyEvent', 'kitty_comet_summoner', 'summon_meteor'),
       ('kitty_tame', 'kittyTame', None, 'comet_kitty_event')]
CRAFTED = {'shield_crafted': ['meteor_shield'], 'crafted_krekno_sword': ['kreknorite_sword'],
           'crafted_detector': ['meteor_proximity_detector', 'meteor_time_detector', 'meteor_crash_detector'], 'crafted_meteor_timer': ['meteor_timer']}
for adv, old_key, icon, parent in ADV:
    if adv in CRAFTED:
        criteria = {'has': {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'items': [n(i) for i in CRAFTED[adv]]}]}}}
    elif adv == 'kitty_tame':
        criteria = {'tame': {'trigger': 'minecraft:tame_animal', 'conditions': {'entity': {'type': 'minecraft:entity_properties', 'entity': 'this', 'predicate': {'minecraft:entity_type': n('comet_kitty')}}}}}
    else:
        criteria = {'done': {'trigger': 'minecraft:impossible'}}
    obj = {'display': {'icon': {'id': n(icon) if icon else 'minecraft:cod'}, 'title': {'translate': f'advancements.{NS}.{adv}.title'},
                       'description': {'translate': f'advancements.{NS}.{adv}.description'}}, 'criteria': criteria}
    if parent:
        obj['parent'] = n(parent)
    else:
        obj['display']['background'] = 'minecraft:block/obsidian'
    write(f'data/{NS}/advancement/{adv}.json', obj)
    name(f'advancements.{NS}.{adv}.title', f'achievement.{old_key}')
    name(f'advancements.{NS}.{adv}.description', f'achievement.{old_key}.desc')


# ---------------------------------------------------------------- loot tables
def pool(entries, conditions=None):
    p = {'rolls': 1, 'entries': entries}
    if conditions:
        p['condition'] = conditions[0] if len(conditions) == 1 else {'type': 'minecraft:all_of', 'terms': conditions}
    return p


def heat(h):
    return {'type': 'minecraft:match_block', 'blocks': None, 'state': {'heat': str(h)}}


def fortune_bonus():
    return {'type': 'minecraft:apply_bonus', 'enchantment': 'minecraft:fortune', 'formula': 'minecraft:uniform_bonus_count',
            'parameters': {'bonusMultiplier': 1}}


def block_loot(block, pools):
    write(f'data/{NS}/loot_table/blocks/{block}.json', {'type': 'minecraft:block', 'pools': pools})


def self_drop(block):
    block_loot(block, [pool([{'type': 'minecraft:item', 'name': n(block)}], [{'type': 'minecraft:survives_explosion'}])])


def meteor_drops(block, item):
    """BlockMeteor.quantityDropped: cold 1 in 4 one (+ fortune), hot 0..heat (+ fortune)."""
    pools = []
    for h in range(5):
        cond = heat(h)
        cond['blocks'] = n(block)
        if h == 0:
            pools.append(pool([{'type': 'minecraft:item', 'name': n(item), 'modifier': [fortune_bonus(), {'type': 'minecraft:explosion_decay'}]}],
                              [cond, {'type': 'minecraft:random_chance', 'chance': 0.25}]))
        else:
            pools.append(pool([{'type': 'minecraft:item', 'name': n(item), 'modifier': [
                {'type': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': 0, 'max': h}}, fortune_bonus(),
                {'type': 'minecraft:explosion_decay'}]}], [cond]))
    block_loot(block, pools)


meteor_drops('fallen_meteor', 'meteor_chips')
meteor_drops('kreknorite', 'kreknorite_chip')
meteor_drops('frezarite', 'frezarite_crystal')
# BlockRareFallenMeteor: one gem (+ fortune) while hot, nothing cold
rare = []
for h in range(1, 5):
    c = heat(h)
    c['blocks'] = n('rare_fallen_meteor')
    rare.append(pool([{'type': 'minecraft:item', 'name': n('red_meteor_gem'), 'modifier': [fortune_bonus()]}], [c]))
block_loot('rare_fallen_meteor', rare)
block_loot('meteor_ore', [pool([{'type': 'minecraft:item', 'name': n('meteor_chips'), 'modifier': [fortune_bonus(), {'type': 'minecraft:explosion_decay'}]}])])
self_drop('meteor_shield')
self_drop('meteor_timer')
self_drop('red_meteor_gem_block')
# dropFewItems: 0..2 (+ looting) of one item
looting = {'type': 'minecraft:enchanted_count_increase', 'enchantment': 'minecraft:looting', 'count': {'type': 'minecraft:uniform', 'min': 0.0, 'max': 1.0}}
write(f'data/{NS}/loot_table/entities/alien_creeper.json', {'type': 'minecraft:entity', 'pools': [pool([
    {'type': 'minecraft:item', 'name': n(i), 'modifier': [{'type': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': 0, 'max': 2}},
                                                             looting]} for i in ('red_meteor_gem', 'meteor_chips')])]})
write(f'data/{NS}/loot_table/entities/comet_kitty.json', {'type': 'minecraft:entity', 'pools': [pool([
    {'type': 'minecraft:item', 'name': n('red_meteor_gem'), 'modifier': [{'type': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': 0, 'max': 2}},
                                                                            looting]}])]})
write('data/minecraft/tags/block/mineable/pickaxe.json', {'replace': False, 'values': [n(b) for b in (
    'fallen_meteor', 'rare_fallen_meteor', 'frezarite', 'kreknorite', 'meteor_ore', 'red_meteor_gem_block', 'meteor_shield')]})
# harvest levels of MeteorsMod.load: frezarite 1 (stone), meteor ore and meteors 2 (iron), kreknorite 3 (diamond)
write('data/minecraft/tags/block/needs_iron_tool.json', {'replace': False, 'values': [n(b) for b in (
    'fallen_meteor', 'rare_fallen_meteor', 'meteor_ore')]})
write('data/minecraft/tags/block/needs_stone_tool.json', {'replace': False, 'values': [n('frezarite')]})
write('data/minecraft/tags/block/needs_diamond_tool.json', {'replace': False, 'values': [n('kreknorite')]})
write('data/minecraft/tags/item/axes.json', {'replace': False, 'values': [n('meteorite_axe'), n('frezarite_axe')]})


# ---------------------------------------------------------------- recipes (MeteorsMod.load)
def result(item, count=1):
    r = {'id': item if ':' in item else n(item)}
    if count != 1:
        r['count'] = count
    return r


def shaped(file, pattern, key, out, count=1, category='equipment'):
    write(f'data/{NS}/recipe/{file}.json', {'type': 'minecraft:crafting_shaped', 'category': category, 'pattern': pattern, 'key': key,
                                            'result': result(out, count)})


def shapeless(file, ingredients, out, count=1, category='misc'):
    write(f'data/{NS}/recipe/{file}.json', {'type': 'minecraft:crafting_shapeless', 'category': category, 'ingredients': ingredients,
                                            'result': result(out, count)})


M = 'minecraft:'
CHIP, FREZ, KREK, GEM = n('meteor_chips'), n('frezarite_crystal'), n('kreknorite_chip'), n('red_meteor_gem')
for mat, c in (('meteorite', CHIP), ('frezarite', FREZ), ('kreknorite', KREK)):
    shaped(f'{mat}_helmet', ['mmm', 'm m'], {'m': c}, f'{mat}_helmet')
    shaped(f'{mat}_chestplate', ['m m', 'mmm', 'mmm'], {'m': c}, f'{mat}_chestplate')
    shaped(f'{mat}_leggings', ['mmm', 'm m', 'm m'], {'m': c}, f'{mat}_leggings')
    shaped(f'{mat}_boots', ['m m', 'm m'], {'m': c}, f'{mat}_boots')
for mat, c, handle in (('meteorite', CHIP, M + 'iron_ingot'), ('frezarite', FREZ, M + 'stick')):
    shaped(f'{mat}_axe', ['mm', 'ms', ' s'], {'m': c, 's': handle}, f'{mat}_axe')
    shaped(f'{mat}_shovel', ['m', 's', 's'], {'m': c, 's': handle}, f'{mat}_shovel')
    shaped(f'{mat}_sword', ['m', 'm', 's'], {'m': c, 's': handle}, f'{mat}_sword')
    shaped(f'{mat}_pickaxe', ['mmm', ' s ', ' s '], {'m': c, 's': handle}, f'{mat}_pickaxe')
    shaped(f'{mat}_hoe', ['mm', ' s', ' s'], {'m': c, 's': handle}, f'{mat}_hoe')
shaped('meteor_ore', ['mm', 'mm'], {'m': CHIP}, 'meteor_ore', category='building')
shaped('protected_land_tester', ['m', 's'], {'m': CHIP, 's': M + 'stick'}, 'protected_land_tester', 4, category='misc')
shaped('meteor_shield', ['mmm', 'crc', 'ccc'], {'m': CHIP, 'c': M + 'cobblestone', 'r': M + 'redstone'}, 'meteor_shield', category='redstone')
shapeless('ice_from_frezarite', [M + 'water_bucket', FREZ], M + 'ice', 4)
shaped('kreknorite_sword', ['c', 'c', 's'], {'c': KREK, 's': M + 'stick'}, 'kreknorite_sword')
shapeless('vanilla_ice_cream', [M + 'bowl', M + 'sugar', M + 'milk_bucket', FREZ], 'vanilla_ice_cream', 4)
shapeless('chocolate_ice_cream', [M + 'bowl', M + 'sugar', M + 'milk_bucket', FREZ, M + 'cocoa_beans'], 'chocolate_ice_cream', 4)
shaped('meteor_timer', ['mfk', 'brb'], {'m': CHIP, 'f': FREZ, 'k': KREK, 'b': [n('meteor_ore'), n('fallen_meteor')], 'r': M + 'redstone'},
       'meteor_timer', category='redstone')
shaped('red_meteor_gem_block', ['rrr', 'rrr', 'rrr'], {'r': GEM}, 'red_meteor_gem_block', category='building')
shapeless('red_meteor_gem', [n('red_meteor_gem_block')], 'red_meteor_gem', 9)
shaped('meteor_proximity_detector', [' m ', 'mrm', ' m '], {'m': CHIP, 'r': M + 'redstone'}, 'meteor_proximity_detector', category='misc')
shaped('meteor_time_detector', [' f ', 'frf', ' f '], {'f': FREZ, 'r': GEM}, 'meteor_time_detector', category='misc')
shaped('meteor_crash_detector', [' k ', 'krk', ' k '], {'k': KREK, 'r': GEM}, 'meteor_crash_detector', category='misc')
shaped('meteor_summoner', ['rmr', 'mrm', 'rmr'], {'m': CHIP, 'r': GEM}, 'meteor_summoner', category='misc')
SUM = n('meteor_summoner')
shaped('meteorite_meteor_summoner', ['mmm', 'msm', 'mmm'], {'m': CHIP, 's': SUM}, 'meteorite_meteor_summoner', category='misc')
shaped('frezarite_meteor_summoner', ['mmm', 'msm', 'mmm'], {'m': FREZ, 's': SUM}, 'frezarite_meteor_summoner', category='misc')
shaped('kreknorite_meteor_summoner', ['mmm', 'msm', 'mmm'], {'m': KREK, 's': SUM}, 'kreknorite_meteor_summoner', category='misc')
shaped('unknown_meteor_summoner', ['rmr', 'ksk', 'rfr'], {'m': CHIP, 's': SUM, 'k': KREK, 'f': FREZ, 'r': GEM}, 'unknown_meteor_summoner', category='misc')
shaped('kitty_comet_summoner', ['mfm', 'fsf', 'mfm'], {'m': CHIP, 's': SUM, 'f': [M + 'cod', M + 'cooked_cod']}, 'kitty_comet_summoner', category='misc')

# ---------------------------------------------------------------- language files, original-assets.txt
for l, d in lang.items():
    write(f'assets/{NS}/lang/{l}.json', dict(sorted(d.items())))
with open(ROOT + '/original-assets.txt', 'w') as f:
    f.write('# path in Meteors Mod V2.10.2.zip -> path in the ported jar (generated by tools/gen.py)\n')
    seen = set()
    for src, dst in assets:
        if dst not in seen:
            seen.add(dst)
            f.write(f'{src} -> {dst.lower()}\n')
