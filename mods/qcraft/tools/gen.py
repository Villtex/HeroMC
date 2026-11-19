#!/usr/bin/env python3
"""Writes the resources of the qCraft port (blockstates, block/item models, item definitions, animation metadata,
equipment asset, loot tables, recipes, tags, language file). Run from anywhere: python3 mods/qcraft/tools/gen.py

Textures come from the original archive at build time (original-assets.txt). The 1.5.2 animation lists (<texture>.txt,
"frame" or "frame*ticks", separated by commas or white space) are turned into .png.mcmeta files."""
import json
import os
import re
import shutil

MOD = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(MOD, "src", "main", "resources")
NS = "quantumcraft"

# BlockQBlock.getImpostorBlockList (index = type number), the 26.3 blocks of qcraft.block.Impostors
IMPOSTORS = [None, "stone", "grass_block", "dirt", "bedrock", "sand", "gravel", "gold_ore", "iron_ore", "coal_ore",
             "oak_log", "lapis_ore", "sandstone", "diamond_ore", "redstone_ore", "emerald_ore", "ice", "clay",
             "carved_pumpkin", "melon", "mycelium", "obsidian", "cobblestone", "oak_planks", "bookshelf",
             "mossy_cobblestone", "netherrack", "soul_sand", "glowstone", "end_stone", "iron_block", "gold_block",
             "diamond_block", "lapis_block", "white_wool", "glass", "orange_wool", "magenta_wool", "light_blue_wool",
             "yellow_wool", "lime_wool", "pink_wool", "gray_wool", "light_gray_wool", "cyan_wool", "purple_wool",
             "blue_wool", "brown_wool", "green_wool", "red_wool", "black_wool", "spruce_log", "birch_log", "jungle_log",
             "spruce_planks", "birch_planks", "jungle_planks", "chiseled_sandstone", "cut_sandstone", "stone_bricks",
             "mossy_stone_bricks", "cracked_stone_bricks", "chiseled_stone_bricks", "nether_bricks", "bricks",
             "redstone_block", "nether_quartz_ore", "quartz_block", "chiseled_quartz_block", "quartz_pillar"]
assert len(IMPOSTORS) == 70

# <texture>.txt of the original
ANIMATIONS = {
    "block/qblock_fuzz": "0,1,2,3",
    "block/qblock_unobserved": "0,1,2,3,4",
    "block/quantum_computer": "0,1,2,3,4",
    "block/quantum_computer_side": "0,1,2,3,4",
    "block/quantum_computer_top": "0,1,2,3,4",
    "item/eos": "0,1,2,3,4,5,6,3",
    "item/eoo": "0,1,10,11,12,0,1,10,11,12,0,1,10,11,12,0,1,10,11,12,0,1,10,11,12,0,1,10,11,12,0,1,10,11,12,0,1,10,11,12,"
                " 2,4,6,8,9*2,8,6,4,2 0,1,10,11,12,0,1,10,11,12,0,1,10,11,12,0,1,10,11,12, 2,4,6,8,9*10,8,6,4,2 "
                "0,1,10,11,12,0,1,10,11,12,0,1,10,11,12,0,1,10,11,12,0,1,10,11,12,0,1,10,11,12, 2,6,9,6,2 0,1,10,11,2, 6,9,6,2",
    "item/eoe": "0,20,21,22,0,20,21,22,0,20,21,22 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19*3",
    "item/quantum_dust": "0,1,2,3",
}

LANG = {
    "itemGroup.quantumcraft": "qCraft",
    "block.quantumcraft.quantum_ore": "Quantum Ore",
    "block.quantumcraft.quantum_ore_glowing": "Quantum Ore",
    "block.quantumcraft.qblock": "Observer Dependent Block",
    "item.quantumcraft.qblock": "Observer Dependent Block",
    "item.quantumcraft.qblock.super": "Quantum Block",
    "item.quantumcraft.qblock.entangled": "Entangled Block",
    "item.quantumcraft.qblock.super.entangled": "Entangled Quantum Block",
    "block.quantumcraft.quantum_computer": "Quantum Computer",
    "item.quantumcraft.quantum_computer.entangled": "Entangled Quantum Computer",
    "item.quantumcraft.eos": "Essence of Superposition",
    "item.quantumcraft.eoo": "Essence of Observation",
    "item.quantumcraft.eoe": "Essence of Entanglement",
    "item.quantumcraft.quantum_dust": "Quantum Dust",
    "item.quantumcraft.quantum_goggles": "Quantum Goggles",
    "tooltip.quantumcraft.group": "Group: %s",
    "tooltip.quantumcraft.stored_area": "Stored Area: %sm x %sm x %sm",
    "gui.quantumcraft.energize": "Energize",
    "gui.quantumcraft.no_destination": "No destination",
    "gui.quantumcraft.frame_incomplete": "Frame incomplete",
    "gui.quantumcraft.destination_frame_incomplete": "Destination frame incomplete",
    "gui.quantumcraft.frame_mismatch": "Frame mismatch",
    "gui.quantumcraft.insufficient_cooling": "Insufficient cooling",
    "gui.quantumcraft.area_not_transportable": "Area not transportable",
    "gui.quantumcraft.destination_not_transportable": "Destination not transportable",
    "gui.quantumcraft.frame_not_activated": "Frame not activated",
    "jei.quantumcraft.qblock": "An Essence of Observation (Observer Dependent Block) or of Superposition (Quantum Block) "
                               "in the crafting grid, with up to six blocks around it: above it the north side, below it "
                               "the south side, left the west side, right the east side, upper left the top and lower "
                               "left the bottom. Empty places show nothing. The block shows the side it is looked at "
                               "from; a Quantum Block may show the opposite one. Players wearing a pumpkin or Quantum "
                               "Goggles do not observe.",
    "jei.quantumcraft.entangled": "An Essence of Entanglement between two equal qBlocks or two Quantum Computers gives two "
                                  "entangled ones (a new group). Entangled blocks are observed together.",
    "jei.quantumcraft.quantum_computer": "Teleports the area inside a frame: four qBlocks in the computer's layer, up to 8 "
                                         "blocks away in each direction, made of obsidian with gold on the side facing "
                                         "the computer; obsidian pillars above and below them and glass on top of the "
                                         "pillars. Needs ice next to it for cooling. Blocks with block entities cannot "
                                         "be moved. A single computer swaps the area with the one it stored; entangled "
                                         "computers swap their areas.",
    "jei.quantumcraft.quantum_goggles": "Shows the superposition of qBlocks; the wearer does not observe them.",
    "jei.quantumcraft.quantum_ore": "Glows when touched. Found deep underground (below y 24).",
}


def write(path, data):
    full = os.path.join(RES, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def frames(spec):
    result = []
    for token in re.split(r"[,\s]+", spec.strip()):
        if not token:
            continue
        if "*" in token:
            index, time = token.split("*")
            result.append({"index": int(index), "time": int(time)})
        else:
            result.append(int(token))
    return result


def model_item(model, tints=None):
    m = {"type": "minecraft:model", "model": model}
    if tints:
        m["tints"] = tints
    return m


def main():
    for d in ("assets", "data"):
        shutil.rmtree(os.path.join(RES, d), ignore_errors=True)

    # ---- animations
    for tex, spec in ANIMATIONS.items():
        write(f"assets/{NS}/textures/{tex}.png.mcmeta", {"animation": {"frametime": 1, "frames": frames(spec)}})

    # ---- ores
    for ore in ("quantum_ore", "quantum_ore_glowing"):
        write(f"assets/{NS}/blockstates/{ore}.json", {"variants": {"": {"model": f"{NS}:block/quantum_ore"}}})
        write(f"assets/{NS}/items/{ore}.json", {"model": model_item(f"{NS}:block/quantum_ore")})
        write(f"data/{NS}/loot_table/blocks/{ore}.json", {
            "type": "minecraft:block",
            "pools": [{"rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": [
                {"type": "minecraft:item", "condition": "minecraft:tool/can_silk_touch", "name": f"{NS}:quantum_ore"},
                {"type": "minecraft:item", "name": f"{NS}:quantum_dust", "functions": [
                    {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}},
                    {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune",
                     "formula": "minecraft:uniform_bonus_count", "parameters": {"bonusMultiplier": 1}},
                    {"function": "minecraft:explosion_decay"}]}]}]}],
            "random_sequence": f"{NS}:blocks/{ore}"})
    write(f"assets/{NS}/models/block/quantum_ore.json",
          {"parent": "minecraft:block/cube_all", "textures": {"all": f"{NS}:block/quantum_ore"}})

    # ---- qBlock: every state uses the swirl model, the client replaces it (qcraft.client.QBlockModel)
    write(f"assets/{NS}/blockstates/qblock.json", {"variants": {"": {"model": f"{NS}:block/qblock_swirl"}}})
    write(f"assets/{NS}/models/block/qblock_swirl.json",
          {"parent": "minecraft:block/cube_all", "textures": {"all": f"{NS}:block/qblock_unobserved"}})
    write(f"assets/{NS}/models/block/qblock_fuzz.json",
          {"parent": "minecraft:block/cube_all", "textures": {"all": f"{NS}:block/qblock_fuzz"}})
    cases = [{"when": "fuzz", "model": model_item(f"{NS}:block/qblock_fuzz")}]
    for i, block in enumerate(IMPOSTORS):
        if block is None:
            continue
        tints = [{"type": "minecraft:grass", "temperature": 0.5, "downfall": 1.0}] if block == "grass_block" else None
        cases.append({"when": str(i), "model": model_item(f"minecraft:block/{block}", tints)})
    write(f"assets/{NS}/items/qblock.json", {"model": {
        "type": "minecraft:select", "property": f"{NS}:qblock_side", "cases": cases, "fallback": {"type": "minecraft:empty"}}})

    # ---- quantum computer: front = facing, top/bottom = top, others = side
    write(f"assets/{NS}/models/block/quantum_computer.json", {"parent": "minecraft:block/orientable_with_bottom", "textures": {
        "front": f"{NS}:block/quantum_computer", "side": f"{NS}:block/quantum_computer_side",
        "top": f"{NS}:block/quantum_computer_top", "bottom": f"{NS}:block/quantum_computer_top"}})
    write(f"assets/{NS}/blockstates/quantum_computer.json", {"variants": {
        f"facing={f}": ({"model": f"{NS}:block/quantum_computer", "y": y} if y else {"model": f"{NS}:block/quantum_computer"})
        for f, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}})
    write(f"assets/{NS}/items/quantum_computer.json", {"model": model_item(f"{NS}:block/quantum_computer")})

    # ---- items
    for item in ("eos", "eoo", "eoe", "quantum_dust", "quantum_goggles"):
        write(f"assets/{NS}/models/item/{item}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{item}"}})
        write(f"assets/{NS}/items/{item}.json", {"model": model_item(f"{NS}:item/{item}")})
    write(f"assets/{NS}/equipment/quantum_goggles.json", {"layers": {
        "humanoid": [{"texture": f"{NS}:quantum_goggles"}], "humanoid_baby": [{"texture": f"{NS}:quantum_goggles"}]}})

    # ---- tags
    write("data/minecraft/tags/block/mineable/pickaxe.json",
          {"values": [f"{NS}:quantum_ore", f"{NS}:quantum_ore_glowing", f"{NS}:quantum_computer"]})

    # ---- recipes (QuantumCraftProxyCommon.registerItems)
    dust = f"{NS}:quantum_dust"
    recipes = {
        "eos": {"pattern": ["XX", "XX"], "key": {"X": dust}, "result": {"id": f"{NS}:eos"}},
        "eoo": {"pattern": [" X ", "X X", " X "], "key": {"X": dust}, "result": {"id": f"{NS}:eoo"}},
        "eoe": {"pattern": ["X X", " Y ", "X X"], "key": {"X": dust, "Y": f"{NS}:eos"}, "result": {"id": f"{NS}:eoe"}},
        "quantum_computer": {"pattern": ["XXX", "XYX", "XZX"], "key": {"X": "minecraft:iron_ingot", "Y": dust,
                                                                     "Z": "minecraft:glass_pane"},
                             "result": {"id": f"{NS}:quantum_computer"}},
        "quantum_goggles": {"pattern": ["XYX"], "key": {"X": "minecraft:glass_pane", "Y": dust},
                            "result": {"id": f"{NS}:quantum_goggles"}},
    }
    for name, r in recipes.items():
        write(f"data/{NS}/recipe/{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc", **r})
    # enableWorldGenReplacementRecipes (pack: off): redstone + lime dye -> 2 quantum dust
    write(f"data/{NS}/recipe/quantum_dust.json", {
        "neoforge:conditions": [{"type": f"{NS}:config", "option": "enableWorldGenReplacementRecipes"}],
        "type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["XY"],
        "key": {"X": "minecraft:redstone", "Y": "minecraft:lime_dye"}, "result": {"id": dust, "count": 2}})
    for special in ("qblock", "entangled_qblock", "entangled_quantum_computer"):
        write(f"data/{NS}/recipe/{special}.json", {"type": f"{NS}:{special}"})

    write(f"assets/{NS}/lang/en_us.json", LANG)


if __name__ == "__main__":
    main()
