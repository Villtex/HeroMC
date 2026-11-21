# Part 25e: heart canisters, knapsack, manuals, jeweled apple, golden head, essence crystal, the slime spawn eggs, the GUIs of
# the extended armor and the knapsack, the manual files. Run by gen.py (exec, after gen_world.py) before the language files are written.

# ---------------------------------------------------------------- items
flat('empty_canister', [tex('items/canister_empty.png', 'item/canister_empty')])
old_name('item.tconstruct.canister.empty.name', f'item.{NS}.empty_canister', 'Empty Canister', 'Leerer Kanister')
name(f'item.{NS}.empty_canister.tip.1', 'Crafting Item', 'Zutat')
flat('red_heart_canister', [tex('items/canister_heart.png', 'item/canister_heart')])
old_name('item.tconstruct.canister.heart.name', f'item.{NS}.red_heart_canister', 'Heart Canister', 'Herzkanister')
name(f'item.{NS}.red_heart_canister.tip.1', 'Accessory', 'Zubehör')
name(f'item.{NS}.red_heart_canister.tip.2', 'Permanent health increase', 'Erhöht dauerhaft die Gesundheit')
flat('knapsack', [tex('items/armor/knapsack.png', 'item/knapsack')])
old_name('item.tconstruct.storage.knapsack.name', f'item.{NS}.knapsack', 'Knapsack', 'Rucksack')
name(f'item.{NS}.knapsack.tip.1', 'A Knapsack to hold your things.', 'Ein Rucksack für deine Sachen.')
old_name('inventory.knapsack', 'inventory.knapsack', 'Knapsack', 'Rucksack')
name('key.tarmor', 'Armor GUI', DE.get('key.tarmor', 'Rüstungs-GUI'))

for i, (reg, key, texture, author) in enumerate((('beginner_manual', 'beginner', 'tinkerbook_diary', 'Skyla'),
                                                 ('materials_manual', 'toolstation', 'tinkerbook_toolstation', 'Skyla'),
                                                 ('smeltery_manual', 'smeltery', 'tinkerbook_smeltery', "Thruul M'gon"))):
    flat(reg, [tex(f'items/{texture}.png', f'item/{texture}')])
    old_name(f'item.tconstruct.manual.{key}.name', f'item.{NS}.{reg}', 'Materials and You', 'Materials and You')
    name(f'item.{NS}.manual.tip.{i}.1', f'By: {author}', f'Von: {author}')

flat('jeweled_apple', [tex('items/apple_diamond.png', 'item/apple_diamond')])
old_name('item.food.apple.diamond.name', f'item.{NS}.jeweled_apple', 'Jeweled Apple', 'Verzierter Apfel')
flat('golden_head', [tex('items/skull_char_gold.png', 'item/skull_char_gold')])
old_name('item.goldenhead.name', f'item.{NS}.golden_head', 'Golden Head', 'Goldener Kopf')
name(f'item.{NS}.golden_head.tip.1', 'Some say consuming the head of a', 'Man sagt, wer den Kopf eines')
name(f'item.{NS}.golden_head.tip.2', 'fallen foe strengthens the blood', 'gefallenen Feindes isst, stärkt sein Blut')

flat('essence_crystal', [tex('items/materials/material_essencecrystal.png', 'item/material_essencecrystal')])
old_name('item.tconstruct.crystal.essence.name', f'item.{NS}.essence_crystal', 'Essence Crystal', 'Essenzkristall')
name(f'item.{NS}.essence_crystal.levels', 'Stored Levels: %s', 'Gespeicherte Level: %s')
txt = os.path.join(REF, 'mods/tinker/textures/items/materials/material_essencecrystal.txt')
if os.path.exists(txt):
    frames = []
    for line in open(txt).read().split():
        m = re.match(r'(\d+)\*(\d+)', line)
        frames.append({'index': int(m.group(1)), 'time': int(m.group(2))} if m else int(line))
    write(f'assets/{NS}/textures/item/material_essencecrystal.png.mcmeta', {'animation': {'frames': frames}})

# TitleIcon: the vanilla egg in the primary colour 6732776 (26.3 has no blank egg to tint, see the mobs-263 skill)
for reg, entity in (('blue_slime_spawn_egg', 'EdibleSlime'), ('king_slime_spawn_egg', 'KingSlime')):
    write(f'assets/{NS}/models/item/{reg}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'minecraft:item/egg'}})
    write(f'assets/{NS}/items/{reg}.json', {'model': {'type': 'minecraft:model', 'model': n(f'item/{reg}'),
                                                       'tints': [{'type': 'minecraft:constant', 'value': (0xFF000000 | 6732776) - (1 << 32)}]}})
    name(f'item.{NS}.{reg}', EN.get('item.tconstruct.titleicon.name', 'Spawn ') + EN.get(f'entity.TConstruct.{entity}.name', entity),
         DE.get('item.tconstruct.titleicon.name', 'Erschaffe ') + DE.get(f'entity.TConstruct.{entity}.name', entity))

# ---------------------------------------------------------------- GUIs, manual files
for g in ('armorextended', 'knapsack', 'bookleft', 'bookright', 'bookcrafting', 'bookfurnace', 'booksmeltery', 'bookmodify', 'newhearts'):
    tex(f'gui/{g}.png', f'gui/{g}')
for f in ('firstday', 'materials', 'smeltery', 'diary'):
    assets.append((f'mods/tinker/resources/manuals/{f}.xml', f'assets/{NS}/manuals/{f}.xml'))
for p in ('oreberries', 'casting', 'smelterysmall'):
    assets.append((f'mods/tinker/resources/manuals/{p}.png', f'assets/{NS}/textures/manual/{p}.png'))

# ---------------------------------------------------------------- recipes (TContent.addCraftingRecipes)
shaped('beginner_manual', ['wp'], {'w': BP, 'p': M + 'paper'}, 'beginner_manual')
shapeless('beginner_manual_copy', [n('beginner_manual'), M + 'book'], 'beginner_manual', 2)
shapeless('materials_manual', [n('beginner_manual')], 'materials_manual')
shapeless('materials_manual_copy', [n('materials_manual'), M + 'book'], 'materials_manual', 2)
shapeless('smeltery_manual', [n('materials_manual')], 'smeltery_manual')
shapeless('smeltery_manual_copy', [n('smeltery_manual'), M + 'book'], 'smeltery_manual', 2)
shaped('empty_canister', ['##', '##'], {'#': n('aluminum_ingot')}, 'empty_canister')  # "ingotNaturalAluminum"
shaped('jeweled_apple', [' d ', 'd#d', ' d '], {'d': M + 'diamond', '#': M + 'apple'}, 'jeweled_apple', category='misc')
shapeless('red_heart_canister', [n('jeweled_apple'), n('necrotic_bone'), n('empty_canister'), n('miniature_red_heart')], 'red_heart_canister')
shaped('knapsack', ['###', 'rmr', '###'], {'#': M + 'leather', 'r': n('iron_tough_rod'), 'm': M + 'gold_ingot'}, 'knapsack')
shaped('knapsack_alubrass', ['###', 'rmr', '###'], {'#': M + 'leather', 'r': n('iron_tough_rod'), 'm': n('alubrass_ingot')}, 'knapsack')
# "Change Crafting Recipes" (UHC) is off: the other branch of TContent
shaped('golden_head', ['###', '#m#', '###'], {'#': M + 'gold_nugget', 'm': M + 'player_head'}, 'golden_head')
shaped('golden_apple_from_oreberries', ['###', '#m#', '###'], {'#': n('gold_oreberry'), 'm': M + 'apple'}, M + 'golden_apple')
# The golden carrot and the golden head from gold oreberries around an apple have the same grid as the golden apple, which
# 1.5.2 registered first and therefore always made: only the golden apple recipe is kept.
