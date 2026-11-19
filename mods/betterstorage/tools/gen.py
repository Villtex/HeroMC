#!/usr/bin/env python3
"""Writes the resources of the BetterStorage port (blockstates, models, item definitions, equipment assets, loot
tables, recipes, enchantments, tags, language file). Run from anywhere: python3 mods/betterstorage/tools/gen.py

The textures come from the original jar at build time (original-assets.txt). Reinforced chests, the locker, the armor
stand and the backpacks are drawn by block entity renderers, so their block models only carry the particle texture;
their items (but the armor stand's icon) use the special model renderer "betterstorage:block"."""
import json
import os
import shutil

MOD = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(MOD, "src", "main", "resources")
NS = "betterstorage"

# ChestMaterial: name, English name, ingot, storage block (ore dictionary names -> c: tags)
MATERIALS = [
    ("iron", "Iron", "minecraft:iron_ingot", "minecraft:iron_block"),
    ("gold", "Gold", "minecraft:gold_ingot", "minecraft:gold_block"),
    ("diamond", "Diamond", "minecraft:diamond", "minecraft:diamond_block"),
    ("emerald", "Emerald", "minecraft:emerald", "minecraft:emerald_block"),
    ("copper", "Copper", "#c:ingots/copper", "#c:storage_blocks/copper"),
    ("tin", "Tin", "#c:ingots/tin", "#c:storage_blocks/tin"),
    ("silver", "Silver", "#c:ingots/silver", "#c:storage_blocks/silver"),
    ("zinc", "Zinc", "#c:ingots/zinc", "#c:storage_blocks/zinc"),
]
CHESTS = [f"reinforced_{m[0]}_chest" for m in MATERIALS]

# block icon of the original (only the breaking particles use it)
PARTICLE = {"locker": "minecraft:block/oak_planks", "armor_stand": "minecraft:block/iron_block",
            "backpack": "minecraft:block/brown_wool", "ender_backpack": "minecraft:block/obsidian"}
PARTICLE.update({c: "minecraft:block/oak_log" for c in CHESTS})

# EnchantmentBetterStorage: name, type, weight, max level, min base, min scaling (max = min + 30)
ENCHANTMENTS = [
    ("unlocking", "key", 4, 5, 5, 10), ("lockpicking", "key", 6, 5, 5, 8), ("morphing", "key", 1, 5, 10, 12),
    ("persistance", "lock", 5, 5, 1, 8), ("security", "lock", 4, 5, 1, 10), ("shock", "lock", 3, 3, 5, 10),
    ("trigger", "lock", 1, 1, 20, 0),
]
EXCLUSIVE = {"lockpicking": "morphing", "morphing": "lockpicking"}
ANVIL_COST = {10: 1, 6: 2, 5: 2, 4: 2, 3: 4, 2: 4, 1: 8}

LANG = {
    "itemGroup.betterstorage": "BetterStorage",
    "container.betterstorage.crate": "Storage Crate",
    "container.betterstorage.reinforced_chest": "Reinforced Chest",
    "container.betterstorage.reinforced_chest_large": "Large Reinforced Chest",
    "container.betterstorage.locker": "Locker",
    "container.betterstorage.locker_large": "Large Locker",
    "container.betterstorage.backpack": "Backpack",
    "container.betterstorage.ender_backpack": "Ender Backpack",
    "container.betterstorage.keyring": "Keyring",
    "block.betterstorage.crate": "Storage Crate",
    "block.betterstorage.locker": "Locker",
    "block.betterstorage.armor_stand": "Armor Stand",
    "block.betterstorage.backpack": "Backpack",
    "block.betterstorage.ender_backpack": "Ender Backpack",
    "item.betterstorage.backpack": "Backpack",
    "item.betterstorage.ender_backpack": "Ender Backpack",
    "item.betterstorage.key": "Key",
    "item.betterstorage.lock": "Lock",
    "item.betterstorage.keyring": "Keyring",
    "entity.betterstorage.frienderman": "Frienderman",
    "jei.betterstorage.key": "A key opens the lock made from it. Craft a key together with another key to copy it; "
                             "a key alone with an iron ingot is iron plated, with dyes it is coloured. Sneak and right "
                             "click a locked chest with the key to take the lock off.",
    "jei.betterstorage.crate": "Storage crates next to each other form a pile with one shared inventory.",
    "jei.betterstorage.backpack": "Worn in the chest slot. Sneak and right click the ground with an empty hand to set "
                                  "it down; others can open it from behind.",
}
for name, english, _, _ in MATERIALS:
    LANG[f"block.betterstorage.reinforced_{name}_chest"] = f"Reinforced {english} Chest"
for name, kind, *_ in ENCHANTMENTS:
    LANG[f"enchantment.betterstorage.{name}"] = name.capitalize()


def write(path, data):
    full = os.path.join(RES, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


for sub in ["assets/betterstorage/blockstates", "assets/betterstorage/models", "assets/betterstorage/items",
            "assets/betterstorage/equipment", "data/betterstorage"]:
    shutil.rmtree(os.path.join(RES, sub), ignore_errors=True)

# ---- blocks ----
write(f"assets/{NS}/blockstates/crate.json", {"variants": {"": {"model": f"{NS}:block/crate"}}})
write(f"assets/{NS}/models/block/crate.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{NS}:block/crate"}})
write(f"assets/{NS}/items/crate.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/crate"}})
for block, particle in PARTICLE.items():
    write(f"assets/{NS}/blockstates/{block}.json", {"variants": {"": {"model": f"{NS}:block/{block}"}}})
    write(f"assets/{NS}/models/block/{block}.json", {"textures": {"particle": particle}})
    if block == "armor_stand":
        continue
    write(f"assets/{NS}/models/item/{block}.json", {"parent": "minecraft:item/template_chest", "textures": {"particle": particle}})
    write(f"assets/{NS}/items/{block}.json", {"model": {"type": "minecraft:special", "base": f"{NS}:item/{block}",
                                                        "model": {"type": f"{NS}:block", "block": f"{NS}:{block}"}}})

# ---- items with icons ----
def generated(name, *layers):
    write(f"assets/{NS}/models/item/{name}.json", {"parent": "minecraft:item/generated",
                                                   "textures": {f"layer{i}": t for i, t in enumerate(layers)}})


def model(name, tints=None):
    m = {"type": "minecraft:model", "model": f"{NS}:item/{name}"}
    if tints:
        m["tints"] = tints
    return m


generated("armor_stand", f"{NS}:item/armor_stand")
write(f"assets/{NS}/items/armor_stand.json", {"model": model("armor_stand")})
generated("lock", f"{NS}:item/lock")
write(f"assets/{NS}/items/lock.json", {"model": model("lock")})
# ItemKey: gold or iron (display.ironPlated), the colour layer on top when it has display.color
DYE_TINTS = [{"type": "minecraft:constant", "value": -1}, {"type": "minecraft:dye", "default": -1}]
for metal in ["gold", "iron"]:
    generated(f"key_{metal}", f"{NS}:item/key_{metal}")
    generated(f"key_{metal}_colored", f"{NS}:item/key_{metal}", f"{NS}:item/key_color")


def plated(colored):
    suffix = "_colored" if colored else ""
    tints = DYE_TINTS if colored else None
    return {"type": "minecraft:condition", "property": "minecraft:has_component", "component": f"{NS}:iron_plated",
            "on_true": model(f"key_iron{suffix}", tints), "on_false": model(f"key_gold{suffix}", tints)}


write(f"assets/{NS}/items/key.json", {"model": {"type": "minecraft:condition", "property": "minecraft:has_component",
                                                "component": "minecraft:dyed_color", "on_true": plated(True),
                                                "on_false": plated(False)}})
# ItemKeyring: icon by the number of keys (0..3+)
for i in range(4):
    generated(f"keyring_{i}", f"{NS}:item/keyring_{i}")
write(f"assets/{NS}/items/keyring.json", {"model": {
    "type": "minecraft:select", "property": "minecraft:component", "component": f"{NS}:keyring_keys",
    "cases": [{"when": 1, "model": model("keyring_1")}, {"when": 2, "model": model("keyring_2")},
              {"when": list(range(3, 10)), "model": model("keyring_3")}],
    "fallback": model("keyring_0")}})

# ---- equipment assets (the worn backpack: ModelBackpackArmor with these textures) ----
write(f"assets/{NS}/equipment/backpack.json", {"layers": {"humanoid": [
    {"texture": f"{NS}:backpack", "dyeable": {"color_when_undyed": -6265536}},
    {"texture": f"{NS}:backpack_overlay"}]}})
write(f"assets/{NS}/equipment/ender_backpack.json", {"layers": {"humanoid": [{"texture": f"{NS}:ender_backpack"}]}})

# ---- loot tables ----
def self_drop(block, conditions=None):
    entry = {"type": "minecraft:item", "name": f"{NS}:{block}"}
    pool = {"rolls": 1, "entries": [entry], "conditions": [{"condition": "minecraft:survives_explosion"}] + (conditions or [])}
    write(f"data/{NS}/loot_table/blocks/{block}.json", {"type": "minecraft:block", "pools": [pool],
                                                        "random_sequence": f"{NS}:blocks/{block}"})


for block in ["crate", "locker"] + CHESTS:
    self_drop(block)
self_drop("armor_stand", [{"condition": "minecraft:block_state_property", "block": f"{NS}:armor_stand",
                           "properties": {"half": "lower"}}])
# EntityEnderman.getDropItemId: 0..1 ender pearls (+ looting)
write(f"data/{NS}/loot_table/entities/frienderman.json", {"type": "minecraft:entity", "pools": [{"rolls": 1, "entries": [{
    "type": "minecraft:item", "name": "minecraft:ender_pearl", "modifier": [
        {"type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 0, "max": 1}},
        {"type": "minecraft:enchanted_count_increase", "count": {"type": "minecraft:uniform", "min": 0.0, "max": 1.0},
         "enchantment": "minecraft:looting"}]}]}], "random_sequence": f"{NS}:entities/frienderman"})

# ---- recipes ----
def shaped(name, result, pattern, key, category="misc"):
    write(f"data/{NS}/recipe/{name}.json", {"type": "minecraft:crafting_shaped", "category": category, "key": key,
                                            "pattern": pattern, "result": {"id": result, "count": 1}})


shaped("crate", f"{NS}:crate", ["o/o", "/ /", "o/o"], {"o": "#minecraft:planks", "/": "minecraft:stick"})
for (name, _, ingot, block), chest in zip(MATERIALS, CHESTS):
    shaped(chest, f"{NS}:{chest}", ["o#o", "#C#", "oOo"], {"C": "minecraft:chest", "#": "#minecraft:logs", "o": ingot, "O": block})
shaped("locker", f"{NS}:locker", ["ooo", "o |", "ooo"], {"o": "#minecraft:planks", "|": "#minecraft:wooden_trapdoors"})
shaped("locker_mirrored", f"{NS}:locker", ["ooo", "| o", "ooo"], {"o": "#minecraft:planks", "|": "#minecraft:wooden_trapdoors"})
shaped("armor_stand", f"{NS}:armor_stand", [" i ", "/i/", " s "],
       {"s": "minecraft:smooth_stone_slab", "i": "minecraft:iron_ingot", "/": "minecraft:stick"})
shaped("backpack", f"{NS}:backpack", ["#i#", "#O#", "###"],
       {"#": "minecraft:leather", "O": "minecraft:white_wool", "i": "minecraft:gold_ingot"}, "equipment")
shaped("keyring", f"{NS}:keyring", ["...", ". .", "..."], {".": "minecraft:gold_nugget"})
write(f"data/{NS}/recipe/key.json", {"type": f"{NS}:key", "pattern": [".o", ".o", " o"],
                                     "key": {"o": "minecraft:gold_ingot", ".": "minecraft:gold_nugget"}})
write(f"data/{NS}/recipe/key_modify.json", {"type": f"{NS}:key", "pattern": ["k"], "key": {"k": f"{NS}:key"}})
write(f"data/{NS}/recipe/lock.json", {"type": f"{NS}:lock", "pattern": [" g ", "gkg", "gig"],
                                      "key": {"g": "minecraft:gold_ingot", "k": f"{NS}:key", "i": "minecraft:iron_ingot"}})
# RecipesArmorDyes: the backpack is CLOTH armor
write(f"data/{NS}/recipe/backpack_dyed.json", {"type": "minecraft:crafting_dye", "category": "equipment", "dye": "#minecraft:dyes",
                                               "target": f"{NS}:backpack", "result": {"id": f"{NS}:backpack"}})

# ---- enchantments ----
for name, kind, weight, max_level, min_base, min_scaling in ENCHANTMENTS:
    e = {"description": {"translate": f"enchantment.{NS}.{name}"},
         "supported_items": f"#{NS}:enchantable/{kind}", "primary_items": f"#{NS}:enchantable/{kind}",
         "weight": weight, "max_level": max_level,
         "min_cost": {"base": min_base, "per_level_above_first": min_scaling},
         "max_cost": {"base": min_base + 30, "per_level_above_first": min_scaling},
         "anvil_cost": ANVIL_COST[weight], "slots": ["any"]}
    if name in EXCLUSIVE:
        e["exclusive_set"] = f"{NS}:{EXCLUSIVE[name]}"
    write(f"data/{NS}/enchantment/{name}.json", e)

# ---- tags ----
def tag(path, values):
    write(path, {"replace": False, "values": values})


tag(f"data/{NS}/tags/item/enchantable/key.json", [f"{NS}:key"])
tag(f"data/{NS}/tags/item/enchantable/lock.json", [f"{NS}:lock"])
tag(f"data/{NS}/tags/item/repairs_backpack.json", ["minecraft:leather"])
tag(f"data/{NS}/tags/item/repairs_ender_backpack.json", ["minecraft:obsidian"])
tag("data/minecraft/tags/enchantment/in_enchanting_table.json", [f"{NS}:{e[0]}" for e in ENCHANTMENTS])
tag("data/minecraft/tags/enchantment/non_treasure.json", [f"{NS}:{e[0]}" for e in ENCHANTMENTS])
for t in ["chest_armor", "armor", "durability", "equippable"]:
    tag(f"data/minecraft/tags/item/enchantable/{t}.json", [f"{NS}:backpack", f"{NS}:ender_backpack"])
tag("data/minecraft/tags/item/dyeable.json", [f"{NS}:backpack"])
tag("data/minecraft/tags/block/mineable/axe.json", [f"{NS}:crate", f"{NS}:locker"] + [f"{NS}:{c}" for c in CHESTS])
tag("data/minecraft/tags/block/mineable/pickaxe.json", [f"{NS}:armor_stand"])

write(f"assets/{NS}/lang/en_us.json", dict(sorted(LANG.items())))
