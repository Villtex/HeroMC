#!/usr/bin/env python3
"""Writes the resources of the BiblioCraft port (blockstates, block/item models, item definitions, loot tables,
recipes, tags, language file, sounds). Run from anywhere: python3 mods/bibliocraft/tools/gen.py

Every BiblioCraft block is drawn by its block entity renderer (the original had a TileEntitySpecialRenderer for
each one), so the block models only carry the particle texture. Items are drawn by the special model renderer
"bibliocraft:block" (client/BlockItemRenderers), which hands the stack to the renderer the block's group registered.
The item base models (models/item/<kind>.json) only hold the display transforms; see DISPLAY."""
import json
import os
import shutil

MOD = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(MOD, "src", "main", "resources")
NS = "bibliocraft"

WOODS = ["oak", "spruce", "birch", "jungle"]  # metadata 0..3 of the original
WOOD_NAMES = ["Oak", "Spruce", "Birch", "Jungle"]
# BlockLoader.dyes: metadata i of lamps and lanterns is the colour of dyes[i]
COLORS = ["white", "light_gray", "gray", "black", "red", "orange", "yellow", "lime", "green", "cyan", "light_blue", "blue",
          "purple", "magenta", "pink", "brown"]
CASE_CLOTH = ["red", "blue", "green", "black"]  # weapon case metadata wood * 4 + cloth (wool 14, 11, 13, 15)

# kind -> list of (block id, English name, original metadata)
KINDS = {
    "desk": [(f"{w}_desk", f"{n} Desk", i) for i, (w, n) in enumerate(zip(WOODS, WOOD_NAMES))],
    "bookcase": [(f"{w}_bookcase", f"{n} Bookcase", i) for i, (w, n) in enumerate(zip(WOODS, WOOD_NAMES))],
    "potion_shelf": [(f"{w}_potion_shelf", f"{n} Potion Shelf", i) for i, (w, n) in enumerate(zip(WOODS, WOOD_NAMES))],
    "shelf": [(f"{w}_shelf", f"{n} Shelf", i) for i, (w, n) in enumerate(zip(WOODS, WOOD_NAMES))],
    "tool_rack": [(f"{w}_tool_rack", f"{n} Tool Rack", i) for i, (w, n) in enumerate(zip(WOODS, WOOD_NAMES))],
    "weapon_case": [(f"{w}_case_{c}", f"{n} Case", i * 4 + j) for i, (w, n) in enumerate(zip(WOODS, WOOD_NAMES))
                    for j, c in enumerate(CASE_CLOTH)],
    "label": [(f"{w}_label", f"{n} Wood Label", i) for i, (w, n) in enumerate(zip(WOODS, WOOD_NAMES))],
    "table": [(f"{w}_table", f"{n} Table", i) for i, (w, n) in enumerate(zip(WOODS, WOOD_NAMES))],
    "armor_stand": [("armor_stand", "Armor Stand", 0)],
    "typesetting_table": [("typesetting_table", "Typesetting Table", 0)],
    "printing_press": [("printing_press", "Printing Press", 0)],
    "lantern": [(f"{c}_lantern", "Golden Lantern", i) for i, c in enumerate(COLORS)],
    "lamp": [(f"{c}_lamp", "Fancy Lamp", i) for i, c in enumerate(COLORS)],
}
# block icon of the original ("wood" = oak planks for all wooden blocks): only the breaking particles use it
PARTICLE = {"armor_stand": "minecraft:block/smooth_stone", "typesetting_table": "minecraft:block/oak_planks",
            "printing_press": "minecraft:block/iron_block", "lantern": "minecraft:block/gold_block",
            "lamp": "minecraft:block/gold_block"}

# Display transforms of the item base model per kind (stand-in for the per-type translations of the original
# IItemRenderer). Default: the vanilla block transforms (minecraft:block/block).
DISPLAY = {}

ITEMS = [("tape_measure", "Tape Measure"), ("tape_reel", "Tape Measure Reel"), ("chase", "Print Press Chase"),
         ("plate", "Print Press Plate"), ("enchanted_plate", "Enchanted Plate"), ("redstone_book", "Redstone: Volume I"),
         ("reading_glasses", "Reading Glasses"), ("tinted_glasses", "Tinted Glasses"), ("monocle", "Monocle")]
ITEM_TEXTURE = {"tape_measure": "tapemeasure", "tape_reel": "tape", "chase": "chase", "plate": "plate",
                "enchanted_plate": "plate", "redstone_book": "minecraft:item/written_book", "reading_glasses": "readingglasses",
                "tinted_glasses": "tintedglasses", "monocle": "monocle"}

LANG = {
    "itemGroup.bibliocraft": "BiblioCraft",
    "enchantment.bibliocraft.reading": "Reading",
}


def write(path, data):
    full = os.path.join(RES, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


for d in ["assets/bibliocraft/blockstates", "assets/bibliocraft/items", "assets/bibliocraft/models/block",
          "assets/bibliocraft/models/item", "data/bibliocraft/loot_table", "data/bibliocraft/recipe"]:
    shutil.rmtree(os.path.join(RES, d), ignore_errors=True)

ALL_BLOCKS = []
for kind, blocks in KINDS.items():
    particle = PARTICLE.get(kind, "minecraft:block/oak_planks")
    write(f"assets/{NS}/models/block/{kind}.json", {"textures": {"particle": particle}})
    base = {"parent": "minecraft:block/block"}
    if kind in DISPLAY:
        base = {"display": DISPLAY[kind]}
    base["textures"] = {"particle": particle}
    write(f"assets/{NS}/models/item/{kind}.json", base)
    for block, name, meta in blocks:
        ALL_BLOCKS.append(block)
        write(f"assets/{NS}/blockstates/{block}.json", {"variants": {"": {"model": f"{NS}:block/{kind}"}}})
        write(f"assets/{NS}/items/{block}.json", {"model": {
            "type": "minecraft:special", "base": f"{NS}:item/{kind}",
            "model": {"type": f"{NS}:block", "block": f"{NS}:{block}"}}})
        LANG[f"block.{NS}.{block}"] = name
        write(f"data/{NS}/loot_table/blocks/{block}.json", {
            "type": "minecraft:block",
            "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{NS}:{block}"}],
                       "conditions": [{"condition": "minecraft:survives_explosion"}]}],
            "random_sequence": f"{NS}:blocks/{block}"})

for item, name in ITEMS:
    tex = ITEM_TEXTURE[item]
    tex = tex if ":" in tex else f"{NS}:item/{tex}"
    write(f"assets/{NS}/models/item/{item}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": tex}})
    model = {"type": "minecraft:model", "model": f"{NS}:item/{item}"}
    write(f"assets/{NS}/items/{item}.json", {"model": model})
    LANG[f"item.{NS}.{item}"] = name

# ---- recipes (BlockLoader.addRecipies, ItemLoader.addRecipies) ----


def shaped(name, result, pattern, key, count=1):
    write(f"data/{NS}/recipe/{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
                                            "key": key, "result": {"id": result, "count": count}})


def shapeless(name, result, ingredients):
    write(f"data/{NS}/recipe/{name}.json", {"type": "minecraft:crafting_shapeless", "category": "misc",
                                            "ingredients": ingredients, "result": {"id": result, "count": 1}})


shaped("golden_lantern", f"{NS}:white_lantern", ["PIP", "GTG", "PIP"],
       {"P": "minecraft:glass_pane", "I": "minecraft:gold_ingot", "G": "minecraft:glowstone_dust", "T": "minecraft:torch"})
shaped("golden_lantern_2", f"{NS}:white_lantern", ["PGP", "ITI", "PGP"],
       {"P": "minecraft:glass_pane", "I": "minecraft:gold_ingot", "G": "minecraft:glowstone_dust", "T": "minecraft:torch"})
shaped("fancy_lamp", f"{NS}:white_lamp", ["BGB", " I ", "NIN"],
       {"B": "minecraft:glass", "G": "minecraft:glowstone", "I": "minecraft:gold_ingot", "N": "minecraft:gold_nugget"})
shaped("printing_press", f"{NS}:printing_press", ["IRI", "SPS", "BBB"],
       {"I": "minecraft:iron_ingot", "R": "minecraft:blaze_rod", "S": "#minecraft:wooden_slabs",
        "P": "minecraft:heavy_weighted_pressure_plate", "B": "minecraft:iron_block"})
shaped("typesetting_table", f"{NS}:typesetting_table", ["ICI", "SSS", "PRP"],
       {"I": "minecraft:iron_ingot", "C": f"{NS}:chase", "S": "#minecraft:wooden_slabs", "P": "#minecraft:planks",
        "R": "minecraft:redstone"})
for i, c in enumerate(COLORS):
    # any lamp / lantern + dye of colour i (16 x 16 recipes in the original, one per result here)
    shapeless(f"{c}_lamp_from_dye", f"{NS}:{c}_lamp", [f"#{NS}:lamps", f"#c:dyes/{c}"])
    shapeless(f"{c}_lantern_from_dye", f"{NS}:{c}_lantern", [f"#{NS}:lanterns", f"#c:dyes/{c}"])
for w in WOODS:
    P, S = f"minecraft:{w}_planks", f"minecraft:{w}_slab"
    shaped(f"{w}_bookcase", f"{NS}:{w}_bookcase", ["XYX", "XYX", "XYX"], {"X": P, "Y": S})
    shaped(f"{w}_potion_shelf", f"{NS}:{w}_potion_shelf", ["YYY", "XBX", "YYY"], {"X": P, "Y": S, "B": "minecraft:glass_bottle"})
    shaped(f"{w}_shelf", f"{NS}:{w}_shelf", ["YYY", " X ", "YYY"], {"X": P, "Y": S})
    shaped(f"{w}_tool_rack", f"{NS}:{w}_tool_rack", ["YYY", "YXY", "YYY"], {"X": "minecraft:iron_ingot", "Y": S})
    shaped(f"{w}_label", f"{NS}:{w}_label", ["YYY", "YYY"], {"Y": S})
    for c in CASE_CLOTH:
        shaped(f"{w}_case_{c}", f"{NS}:{w}_case_{c}", ["YZY", "YXY", "YYY"],
               {"X": f"minecraft:{c}_wool", "Y": S, "Z": "minecraft:glass"})
    shaped(f"{w}_desk", f"{NS}:{w}_desk", ["T F", "XXX", "Y Y"], {"T": "minecraft:torch", "F": "minecraft:feather", "X": S, "Y": P})
    shaped(f"{w}_table", f"{NS}:{w}_table", ["XXX", " Y ", " Y "], {"X": S, "Y": P})
shaped("armor_stand", f"{NS}:armor_stand", [" Y ", " Y ", "XXX"], {"X": "minecraft:smooth_stone_slab", "Y": "minecraft:iron_ingot"})
shaped("redstone_book", f"{NS}:redstone_book", [" R ", " B "], {"R": "minecraft:redstone_torch", "B": "minecraft:book"})
shaped("monocle", f"{NS}:monocle", [" G ", "GPG", " G "], {"G": "minecraft:gold_nugget", "P": "minecraft:glass_pane"})
shaped("chase", f"{NS}:chase", [" W ", "WIW", " W "], {"W": "#minecraft:wooden_slabs", "I": "minecraft:iron_ingot"})
shaped("reading_glasses", f"{NS}:reading_glasses", ["PIP", "S S"],
       {"P": "minecraft:glass_pane", "I": "minecraft:iron_ingot", "S": "#c:dyes/black"})
shaped("tinted_glasses", f"{NS}:tinted_glasses", [" G ", " D "], {"G": f"{NS}:reading_glasses", "D": "#c:dyes/gray"})
shaped("tape_reel", f"{NS}:tape_reel", ["QQQ", "QDQ", "QQQ"], {"Q": "minecraft:string", "D": "minecraft:yellow_dye"})
shaped("tape_measure", f"{NS}:tape_measure", [" I ", "IQI", " I "], {"I": "minecraft:iron_ingot", "Q": f"{NS}:tape_reel"})

# ---- tags ----
write(f"data/{NS}/tags/item/lamps.json", {"values": [f"{NS}:{c}_lamp" for c in COLORS]})
write(f"data/{NS}/tags/item/lanterns.json", {"values": [f"{NS}:{c}_lantern" for c in COLORS]})
write("data/minecraft/tags/block/mineable/axe.json",
      {"replace": False, "values": [f"{NS}:{b}" for b in ALL_BLOCKS if not b.endswith(("_lamp", "_lantern"))
                                    and b not in ("armor_stand", "printing_press")]})
write("data/minecraft/tags/block/mineable/pickaxe.json",
      {"replace": False, "values": [f"{NS}:armor_stand", f"{NS}:printing_press"]})
# Reading glasses take the Reading enchantment like helmets (EnumEnchantmentType.armor_head)
write("data/minecraft/tags/item/enchantable/head_armor.json",
      {"replace": False, "values": [f"{NS}:reading_glasses", f"{NS}:tinted_glasses", f"{NS}:monocle"]})

# ---- Reading enchantment (EnchantmentReading: armor_head, weight 1, level 1, cost 10..61; at the enchanting table
# only on books, canApplyAtEnchantingTable = instanceof ItemBook -> empty primary_items) ----
write(f"data/{NS}/enchantment/reading.json", {
    "anvil_cost": 8, "description": {"translate": f"enchantment.{NS}.reading"}, "effects": {},
    "max_cost": {"base": 61, "per_level_above_first": 0}, "max_level": 1,
    "min_cost": {"base": 10, "per_level_above_first": 20}, "slots": ["head"],
    "supported_items": "#minecraft:enchantable/head_armor", "primary_items": f"#{NS}:no_primary_items", "weight": 1})
write(f"data/{NS}/tags/item/no_primary_items.json", {"values": []})
write("data/minecraft/tags/enchantment/non_treasure.json", {"replace": False, "values": [f"{NS}:reading"]})

# ---- glasses on the head (getArmorTexture) ----
for asset in ["glasses", "glasses_darktint", "glasses_mono"]:
    write(f"assets/{NS}/equipment/{asset}.json", {"layers": {"humanoid": [{"texture": f"{NS}:{asset}"}]}})

# ---- sounds (SoundLoader) ----
write(f"assets/{NS}/sounds.json", {s: {"sounds": [f"{NS}:{s}"]} for s in ["copen", "cclose", "tapeopen", "tapeclose"]})

write(f"assets/{NS}/lang/en_us.json", dict(sorted(LANG.items())))
print(f"{len(ALL_BLOCKS)} blocks, {len(ITEMS)} items")
