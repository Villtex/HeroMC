#!/usr/bin/env python3
"""Generates the assets/data JSON of the SecretRoomsMod port (mods/secretrooms/src/main/resources), the language files
and original-assets.txt. The door, trapdoor and wall torch block states are taken from the vanilla 26.3 jar in the
Gradle cache. Everything under resources/assets and resources/data is overwritten."""
import glob, json, os, re, shutil, zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = ROOT + '/src/main/resources'
NS = 'secretroomsmod'
M = 'minecraft:'
for d in ('assets', 'data'):
    shutil.rmtree(os.path.join(RES, d), ignore_errors=True)
VANILLA = glob.glob(os.path.expanduser('~/.gradle/caches/neoformruntime/intermediate_results/applyDevTransforms_*_output.jar'))[0]
vanilla = zipfile.ZipFile(VANILLA)


def vjson(path):
    return json.loads(vanilla.read(path))


def write(path, obj):
    full = os.path.join(RES, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, 'w') as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write('\n')


def n(x):
    return x if ':' in x else f'{NS}:{x}'


assets = []
en, de = {}, {}


def name(key, english, german):
    en[key] = english
    de[key] = german


def snake(s):
    return re.sub(r'(?<!^)(?=[A-Z])', '_', s).lower()


def btex(t):
    dst = snake(t)
    assets.append((f'mods/secretroomsmod/textures/blocks/{t}.png', f'assets/{NS}/textures/block/{dst}.png'))
    return n('block/' + dst)


def itex(t):
    dst = snake(t)
    assets.append((f'mods/secretroomsmod/textures/items/{t}.png', f'assets/{NS}/textures/item/{dst}.png'))
    return n('item/' + dst)


def block_model(m, obj):
    write(f'assets/{NS}/models/block/{m}.json', obj)
    return n('block/' + m)


def item_def(item, model):
    write(f'assets/{NS}/items/{item}.json', {'model': {'type': 'minecraft:model', 'model': model}})


def drop(block, item=None):
    write(f'data/{NS}/loot_table/blocks/{block}.json', {'type': 'minecraft:block', 'pools': [
        {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n(item or block)}], 'condition': {'type': 'minecraft:survives_explosion'}}]})


# ---------------------------------------------------------------- full camo blocks: own texture on all sides
FULL = [('camo_gate', 'CamoGate', 'Camo Gate', 'Tarntor'),
        ('camo_dummy', 'CamoBase', 'Dummy Camo block (does nothing)', 'Tarn-Attrappe (tut nichts)'),
        ('ghost_block', 'CamoBase', 'Ghost Block', 'Geisterblock'),
        ('secret_lever', 'CamoLever', 'Secret Lever', 'Geheimer Hebel'),
        ('secret_redstone', 'CamoRedstone', 'Secret Redstone', 'Geheimes Redstone'),
        ('secret_stone_button', 'CamoButtonStone', 'Secret Stone Button', 'Geheimer Steinknopf'),
        ('secret_wood_button', 'CamoButtonWood', 'Secret Wood Button', 'Geheimer Holzknopf'),
        ('secret_pressure_plate', 'CamoPlateWood', 'Secret PressurePlate', 'Geheime Druckplatte'),
        ('secret_player_plate', 'CamoPlatePlayer', 'Secret PlayerPlate', 'Geheime Spielerplatte'),
        ('secret_light_plate', 'CamoPlateGold', 'Secret Weighted Plate (light)', 'Geheime Wägeplatte (leicht)'),
        ('secret_heavy_plate', 'CamoPlateIron', 'Secret Weighted Plate (heavy)', 'Geheime Wägeplatte (schwer)'),
        ('secret_stairs', 'CamoStair', 'Secret Stairs', 'Geheime Treppe'),
        ('secret_chest', 'CamoChest', 'Secret Chest', 'Geheime Truhe'),
        ('secret_trapped_chest', 'CamoChest', 'Secret Trapped Chest', 'Geheime Redstone-Truhe'),
        ('secret_light_detector', 'CamoDetector', 'Secret Light Detector', 'Geheimer Tageslichtsensor')]
for block, t, e, g in FULL:
    m = block_model(block, {'parent': 'minecraft:block/cube_all', 'textures': {'all': btex(t)}})
    write(f'assets/{NS}/blockstates/{block}.json', {'variants': {'': {'model': m}}})
    name(f'block.{NS}.{block}', e, g)
    if block != 'camo_dummy':
        item_def(block, m)
        drop(block)
write(f'data/{NS}/loot_table/blocks/camo_dummy.json', {'type': 'minecraft:block', 'pools': []})

# one-way glass: the camo face (south in the item, side 3 of 1.5.2) and glass elsewhere
base = btex('CamoBase')
ow = block_model('one_way_glass', {'parent': 'minecraft:block/cube', 'render_type': 'minecraft:cutout', 'textures': {
    'particle': M + 'block/glass', 'down': M + 'block/glass', 'up': M + 'block/glass', 'north': M + 'block/glass', 'south': base,
    'west': M + 'block/glass', 'east': M + 'block/glass'}})
ROT = {'north': {'y': 180}, 'south': {}, 'west': {'y': 90}, 'east': {'y': 270}, 'up': {'x': 270}, 'down': {'x': 90}}
write(f'assets/{NS}/blockstates/one_way_glass.json', {'variants': {f'facing={f}': dict({'model': ow}, **r) for f, r in ROT.items()}})
item_def('one_way_glass', ow)
drop('one_way_glass')
name(f'block.{NS}.one_way_glass', 'One-Way Glass', 'Einwegglas')

# solid air (invisible; its block model is only the particle)
write(f'assets/{NS}/blockstates/solid_air.json', {'variants': {'': {'model': block_model('solid_air', {'textures': {'particle': btex('SolidAir')}})}}})
write(f'assets/{NS}/models/item/solid_air.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': n('block/solid_air')}})
item_def('solid_air', n('item/solid_air'))
name(f'block.{NS}.solid_air', 'Solid Air', 'Feste Luft')
btex('clear')

# doors and the trapdoor: the vanilla oak models with the camo base texture
for door in ('secret_wooden_door', 'secret_iron_door'):
    states = vjson('assets/minecraft/blockstates/oak_door.json')
    for v in states['variants'].values():
        old = v['model'].split('/')[-1]
        part = old.replace('oak_door_', '')
        v['model'] = n(f'block/{door}_{part}')
    for f in vanilla.namelist():
        mm = re.match(r'assets/minecraft/models/block/oak_door_(.*)\.json$', f)
        if mm:
            j = vjson(f)
            block_model(f'{door}_{mm.group(1)}', {'parent': j['parent'], 'textures': {'bottom': base, 'top': base}})
    write(f'assets/{NS}/blockstates/{door}.json', states)
    write(f'data/{NS}/loot_table/blocks/{door}.json', {'type': 'minecraft:block', 'pools': [
        {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n(door), 'conditions': [
            {'condition': 'minecraft:block_state_property', 'block': n(door), 'properties': {'half': 'lower'}}]}],
         'condition': {'type': 'minecraft:survives_explosion'}}]})
item_def('secret_wooden_door', n('item/secret_wooden_door'))
write(f'assets/{NS}/models/item/secret_wooden_door.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': itex('CamoDoorWood')}})
item_def('secret_iron_door', n('item/secret_iron_door'))
write(f'assets/{NS}/models/item/secret_iron_door.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': itex('CamoDoorSteel')}})
name(f'block.{NS}.secret_wooden_door', 'Secret Wooden Door', 'Geheime Holztür')
name(f'block.{NS}.secret_iron_door', 'Secret Iron Door', 'Geheime Eisentür')
name(f'item.{NS}.secret_wooden_door', 'Secret Wooden Door', 'Geheime Holztür')
name(f'item.{NS}.secret_iron_door', 'Secret Iron Door', 'Geheime Eisentür')
states = vjson('assets/minecraft/blockstates/oak_trapdoor.json')
for v in states['variants'].values():
    v['model'] = n('block/secret_trapdoor_' + v['model'].split('/')[-1].replace('oak_trapdoor_', ''))
for part in ('bottom', 'top', 'open'):
    block_model(f'secret_trapdoor_{part}', {'parent': f'minecraft:block/template_trapdoor_{part}', 'textures': {'texture': base}})
write(f'assets/{NS}/blockstates/secret_trapdoor.json', states)
item_def('secret_trapdoor', n('block/secret_trapdoor_bottom'))
drop('secret_trapdoor')
name(f'block.{NS}.secret_trapdoor', 'Secret TrapDoor', 'Geheime Falltür')

# torch lever: looks like the vanilla torch
write(f'assets/{NS}/blockstates/torch_lever.json', {'variants': {'': {'model': M + 'block/torch'}}})
write(f'assets/{NS}/blockstates/wall_torch_lever.json', vjson('assets/minecraft/blockstates/wall_torch.json'))
write(f'assets/{NS}/models/item/torch_lever.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': btex('TorchLever')}})
item_def('torch_lever', n('item/torch_lever'))
drop('torch_lever')
name(f'block.{NS}.torch_lever', 'Torch Lever', 'Fackelhebel')

write(f'assets/{NS}/models/item/camo_paste.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': itex('CamoPaste')}})
item_def('camo_paste', n('item/camo_paste'))
name(f'item.{NS}.camo_paste', 'Camoflage Paste', 'Tarnpaste')
name(f'itemGroup.{NS}', 'SecretRoomsMod', 'SecretRoomsMod')
name(f'container.{NS}.camo_chest', 'Hidden Chest', 'Versteckte Truhe')
name(f'key.{NS}.one_way_face', 'Change OneWayBlock face', 'Einwegglas-Seite wechseln')
name(f'message.{NS}.towards', '-- !!! OneWayBlock facing set to Towards !!! --', '-- !!! Einwegglas zeigt jetzt zu dir !!! --')
name(f'message.{NS}.away', '-- !!! OneWayBlock facing set to Away !!! --', '-- !!! Einwegglas zeigt jetzt von dir weg !!! --')
name(f'message.{NS}.secret', 'Camo blocks made secret', 'Tarnblöcke sind jetzt geheim')
name(f'message.{NS}.obvious', 'Camo blocks made obvious', 'Tarnblöcke sind jetzt sichtbar')
write('data/minecraft/tags/block/mineable/axe.json', {'replace': False, 'values': [n(b) for b, *_ in FULL] + [n('one_way_glass'), n('secret_wooden_door'),
                                                                                                           n('secret_trapdoor')]})
write('data/minecraft/tags/block/mineable/pickaxe.json', {'replace': False, 'values': [n('secret_iron_door')]})
write(f'data/c/tags/item/camo_paste.json', {'values': [n('camo_paste')]})

# ---------------------------------------------------------------- recipes (SecretRooms.addrecipes; "camoPaste" = the paste)
P = n('camo_paste')
WOOL = '#minecraft:wool'
DYES = '#c:dyes'


def shaped(file, pattern, key, out, count=1):
    write(f'data/{NS}/recipe/{file}.json', {'type': 'minecraft:crafting_shaped', 'category': 'redstone', 'pattern': pattern, 'key': key,
                                            'result': {'id': n(out), **({'count': count} if count != 1 else {})}})


def shapeless(file, ingredients, out, count=1):
    write(f'data/{NS}/recipe/{file}.json', {'type': 'minecraft:crafting_shapeless', 'category': 'redstone', 'ingredients': ingredients,
                                            'result': {'id': n(out), **({'count': count} if count != 1 else {})}})


shaped('camo_gate', ['#0#', '0A0', '#@#'], {'#': '#minecraft:planks', '0': P, '@': M + 'redstone', 'A': M + 'ender_pearl'}, 'camo_gate')
shaped('torch_lever', ['#', 'X'], {'#': M + 'torch', 'X': M + 'redstone'}, 'torch_lever')
shapeless('secret_wooden_door', [P, M + 'oak_door'], 'secret_wooden_door')
shapeless('secret_iron_door', [P, M + 'iron_door'], 'secret_iron_door')
shapeless('secret_trapdoor', [P, M + 'oak_trapdoor'], 'secret_trapdoor')
for core in ('dirt', 'sand', 'clay_ball'):
    shaped(f'camo_paste_{core}', ['XXX', 'X0X', 'XXX'], {'X': DYES, '0': M + core}, 'camo_paste', 9)
shaped('one_way_glass_left', ['X00', 'X00', 'X00'], {'X': P, '0': M + 'glass'}, 'one_way_glass', 9)
shaped('one_way_glass_right', ['00X', '00X', '00X'], {'X': P, '0': M + 'glass'}, 'one_way_glass', 9)
shaped('one_way_glass_top', ['XXX', '000', '000'], {'X': P, '0': M + 'glass'}, 'one_way_glass', 9)
shaped('one_way_glass_bottom', ['000', '000', 'XXX'], {'X': P, '0': M + 'glass'}, 'one_way_glass', 9)
shapeless('one_way_glass', [P, M + 'glass'], 'one_way_glass')
for filler, tag in ((M + 'rotten_flesh', 'flesh'), (WOOL, 'wool')):
    shaped(f'ghost_block_{tag}', ['X0X', '0 0', 'X0X'], {'X': P, '0': filler}, 'ghost_block', 4)
    for out, core, count in (('secret_redstone', M + 'redstone', 4), ('secret_lever', M + 'lever', 4), ('secret_stone_button', M + 'stone_button', 4),
                             ('secret_wood_button', M + 'oak_button', 4), ('secret_pressure_plate', M + 'oak_pressure_plate', 1),
                             ('secret_player_plate', M + 'stone_pressure_plate', 1), ('secret_light_plate', M + 'light_weighted_pressure_plate', 1),
                             ('secret_heavy_plate', M + 'heavy_weighted_pressure_plate', 1), ('secret_stairs', '#minecraft:wooden_stairs', 4),
                             ('secret_stairs_cobble', M + 'cobblestone_stairs', 4), ('secret_chest', M + 'chest', 1),
                             ('secret_trapped_chest', M + 'trapped_chest', 1), ('secret_light_detector', M + 'daylight_detector', 1)):
        shaped(f'{out}_{tag}', ['X0X', '0@0', 'X0X'], {'X': P, '0': filler, '@': core}, out.replace('_cobble', ''), count)
WATER_BOTTLE = {'type': 'neoforge:components', 'items': M + 'potion', 'components': {M + 'potion_contents': {'potion': M + 'water'}}}
for fuel in ('blaze_powder', 'blaze_rod', 'magma_cream'):
    shapeless(f'solid_air_{fuel}_bucket', [P, M + fuel, M + 'water_bucket'], 'solid_air')
    shapeless(f'solid_air_{fuel}_bottle', [P, M + fuel, WATER_BOTTLE], 'solid_air')

# ---------------------------------------------------------------- language files, original-assets.txt
for lang, d in (('en_us', en), ('de_de', de)):
    write(f'assets/{NS}/lang/{lang}.json', dict(sorted(d.items())))
with open(ROOT + '/original-assets.txt', 'w') as f:
    f.write('# path in SecretRoomsMod-universal-4.6.0.283.zip -> path in the ported jar (generated by tools/gen.py)\n')
    seen = set()
    for src, dst in assets:
        if dst not in seen:
            seen.add(dst)
            f.write(f'{src} -> {dst}\n')
