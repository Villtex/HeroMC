#!/usr/bin/env python3
"""Generates the assets/data JSON of the Backpack port (mods/backpacks/src/main/resources), the language file and
original-assets.txt. Everything under resources/assets and resources/data is overwritten."""
import json, os, shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = ROOT + '/src/main/resources'
NS = 'backpack'

for d in ('assets', 'data'):
    shutil.rmtree(os.path.join(RES, d), ignore_errors=True)


def write(path, obj):
    full = os.path.join(RES, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, 'w') as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write('\n')


# Constants.BACKPACK_COLORS / BACKPACK_NAMES (1.5.2 dye order); file names of the original textures
COLORS = ['black', 'red', 'green', 'brown', 'blue', 'purple', 'cyan', 'lightGray', 'gray', 'pink', 'lime', 'yellow', 'lightBlue',
          'magenta', 'orange', 'white']
NAMES = ['Black Backpack', 'Red Backpack', 'Green Backpack', 'Brown Backpack', 'Blue Backpack', 'Purple Backpack', 'Cyan Backpack',
         'Light Gray Backpack', 'Gray Backpack', 'Pink Backpack', 'Lime Green Backpack', 'Yellow Backpack', 'Light Blue Backpack',
         'Magenta Backpack', 'Orange Backpack', 'White Backpack']
# c:dyes tags per colour (RecipeHelper used the ore dictionary names dyeBlack, ...)
DYES = ['black', 'red', 'green', 'brown', 'blue', 'purple', 'cyan', 'light_gray', 'gray', 'pink', 'lime', 'yellow', 'light_blue',
        'magenta', 'orange', 'white']


def snake(c):
    return ''.join('_' + ch.lower() if ch.isupper() else ch for ch in c)


assets = []  # (path in the original jar, path in the ported jar)
lang = {
    'key.backpack.open': 'Open Backpack',
    'gui.backpack.rename': 'Rename your backpack',
    'gui.backpack.new_name': 'New name:',
    'message.backpack.limit': '[Backpacks] You are not allowed to have more than %s backpacks in your inventory.',
    'message.backpack.removed': '[Backpacks] %s backpacks were removed from your inventory. Look on the ground.',
}


def item(name, texture, display):
    """A flat item: models/item/<name>.json with the original icon, items/<name>.json, the English name."""
    assets.append((f'mods/backpack/textures/items/{texture}.png', f'assets/{NS}/textures/item/{name}.png'))
    write(f'assets/{NS}/models/item/{name}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{NS}:item/{name}'}})
    write(f'assets/{NS}/items/{name}.json', {'model': {'type': 'minecraft:model', 'model': f'{NS}:item/{name}'}})
    lang[f'item.{NS}.{name}'] = display


def shaped(name, pattern, key, result, conditions=None):
    r = {}
    if conditions:
        r['neoforge:conditions'] = conditions
    r.update({'type': 'minecraft:crafting_shaped', 'category': 'equipment', 'key': key, 'pattern': pattern,
              'result': {'count': 1, 'id': result}})
    write(f'data/{NS}/recipe/{name}.json', r)


def cond(option):
    return [{'type': f'{NS}:config', 'option': option}]


RING = ['LLL', 'LDL', 'LLL']
LEATHER = 'minecraft:leather'
TANNED = f'{NS}:leather_tanned'

# ItemBackpack: damage 0-15 colours, 16 plain, 32-48 big, 31999 ender
for i, c in enumerate(COLORS):
    item(f'backpack_{snake(c)}', f'backpack_{c}', NAMES[i])
    item(f'backpack_{snake(c)}_big', f'backpack_{c}_big', 'Big ' + NAMES[i])
    shaped(f'backpack_{snake(c)}', RING, {'L': LEATHER, 'D': f'#c:dyes/{DYES[i]}'}, f'{NS}:backpack_{snake(c)}')
    shaped(f'backpack_{snake(c)}_big', RING, {'L': TANNED, 'D': f'#c:dyes/{DYES[i]}'}, f'{NS}:backpack_{snake(c)}_big')
item('backpack', 'backpack', 'Backpack')
item('backpack_big', 'backpack_big', 'Big Backpack')
item('backpack_ender', 'backpack_ender', 'Ender Backpack')
# ItemWorkbenchBackpack: damage 18 and 50
item('backpack_workbench', 'backpack_workbench', 'Workbench Backpack')
item('backpack_workbench_big', 'backpack_workbench_big', 'Big Workbench Backpack')
# ItemLeather
item('leather_bound', 'leatherBound', 'Bound Leather')
item('leather_tanned', 'leatherTanned', 'Tanned Leather')

# RecipeHelper
shaped('backpack', ['LLL', 'L L', 'LLL'], {'L': LEATHER}, f'{NS}:backpack', cond('no_airship'))
shaped('backpack_airship', RING, {'L': LEATHER, 'D': 'minecraft:chest'}, f'{NS}:backpack', cond('airship'))
shaped('backpack_big', ['LLL', 'L L', 'LLL'], {'L': TANNED}, f'{NS}:backpack_big')
shaped('backpack_ender', RING, {'L': LEATHER, 'D': 'minecraft:ender_chest'}, f'{NS}:backpack_ender', cond('ender_chest'))
shaped('backpack_ender_eye', RING, {'L': LEATHER, 'D': 'minecraft:ender_eye'}, f'{NS}:backpack_ender', cond('eye'))
shaped('backpack_workbench', RING, {'L': LEATHER, 'D': 'minecraft:crafting_table'}, f'{NS}:backpack_workbench')
shaped('backpack_workbench_big', RING, {'L': TANNED, 'D': 'minecraft:crafting_table'}, f'{NS}:backpack_workbench_big')
r = {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'key': {'S': 'minecraft:string', 'L': LEATHER},
     'pattern': ['SSS', 'LSL', 'SSS'], 'result': {'count': 1, 'id': f'{NS}:leather_bound'}}
write(f'data/{NS}/recipe/leather_bound.json', r)
write(f'data/{NS}/recipe/leather_tanned.json', {'type': 'minecraft:smelting', 'category': 'misc', 'cookingtime': 200, 'experience': 0.1,
                                                'ingredient': f'{NS}:leather_bound', 'result': {'id': TANNED}})
write(f'data/{NS}/recipe/enhance_backpack.json', {'type': f'{NS}:enhance_backpack'})
write(f'data/{NS}/recipe/recolor_backpack.json', {'type': f'{NS}:recolor_backpack'})

# the backpack model in the chest slot (getArmorTexture: model/backpack.png for every backpack)
write(f'assets/{NS}/equipment/backpack.json', {'layers': {'humanoid': [{'texture': f'{NS}:backpack'}]}})
assets.append(('mods/backpack/textures/model/backpack.png', f'assets/{NS}/textures/entity/equipment/humanoid/backpack.png'))
assets.append(('mods/backpack/textures/gui/guiWorkbenchBackpack.png', f'assets/{NS}/textures/gui/workbench_backpack.png'))
assets.append(('mods/backpack/textures/gui/guiBackpackAlt.png', f'assets/{NS}/textures/gui/rename_backpack.png'))

write(f'assets/{NS}/lang/en_us.json', dict(sorted(lang.items())))
with open(os.path.join(ROOT, 'original-assets.txt'), 'w') as f:
    f.write('# path in backpack-1.12.13-1.5.2.zip -> path in the ported jar (generated by tools/gen.py)\n')
    for src, dst in sorted(assets):
        f.write(f'{src} -> {dst}\n')
