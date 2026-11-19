#!/usr/bin/env python3
"""Writes original-assets.txt and the resources of the Dimensional Doors port (blockstates, models, loot tables,
recipes, tags, language files, sounds, dimensions). Run from anywhere: python3 mods/dimensional-doors/tools/gen.py"""
import json
import os

MOD = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(MOD, "src", "main", "resources")
NS = "dimdoors"


def write(path, data):
    full = os.path.join(RES, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


# ---- original-assets.txt ----
T = "mods/DimDoors/textures/"
BLOCK_TEX = {"dimDoor_top": "dimensional_door_top", "dimDoor_bottom": "dimensional_door_bottom", "dimDoorWarp_top": "warp_door_top",
             "dimDoorWarp_bottom": "warp_door_bottom", "chaosDoor_top": "unstable_door_top", "chaosDoor_bottom": "unstable_door_bottom",
             "transientDoor_top": "transient_door_top", "dimHatch": "transdimensional_trapdoor", "blockDimWall": "fabric_of_reality",
             "blockDimWallPerm": "eternal_fabric", "BlockLimbo": "unraveled_fabric", "rift": "rift"}
ITEM_TEX = {"itemDimDoor": "dimensional_door", "itemDimDoorWarp": "warp_door", "item.itemChaosDoor": "unstable_door",
            "item.itemLinkSignature": "rift_signature", "itemStabilizedRiftSig": "stabilized_rift_signature",
            "item.itemRiftRemover": "rift_remover", "itemStableFabric": "stable_fabric", "item.ItemRiftBlade": "rift_blade"}
SOUNDS = {"monk": "monk", "crack": "crack", "tearing": "tearing", "rift": "rift", "riftStart": "rift_start", "riftEnd": "rift_end",
          "riftClose": "rift_close", "riftDoor": "rift_door"}
lines = ["# path in DimensionalDoors-1.5.2R1.4.1RC1-194.zip -> path in the ported jar (written by tools/gen.py)"]
for k, v in BLOCK_TEX.items():
    lines.append(f"{T}blocks/{k}.png -> assets/{NS}/textures/block/{v}.png")
for k, v in ITEM_TEX.items():
    lines.append(f"{T}items/{k}.png -> assets/{NS}/textures/item/{v}.png")
for i in range(19):
    lines.append(f"{T}mobs/Monolith{i}.png -> assets/{NS}/textures/entity/monolith/monolith{i}.png")
lines.append(f"{T}other/limboSun.png -> assets/{NS}/textures/environment/limbo_sun.png")
lines.append(f"{T}other/limboMoon.png -> assets/{NS}/textures/environment/limbo_moon.png")
for k, v in SOUNDS.items():
    lines.append(f"mods/DimDoors/sfx/{k}.ogg -> assets/{NS}/sounds/{v}.ogg")
lines.append("# the dungeons (SchematicLoader read them from /schematics/ of the jar)")
lines.append("schematics/ -> dimdoors-original/schematics/")
lines.append("# the pack's configuration, for reference")
lines.append("[z-hexxit-configs]")
lines.append("config/DimDoors.cfg -> dimdoors-original/DimDoors.cfg")
with open(os.path.join(MOD, "original-assets.txt"), "w") as f:
    f.write("\n".join(lines) + "\n")

# ---- doors ----
DOORS = {"dimensional_door": ("dimensional_door_bottom", "dimensional_door_top"), "warp_door": ("warp_door_bottom", "warp_door_top"),
         "unstable_door": ("unstable_door_bottom", "unstable_door_top"), "transient_door": ("transient_door_top", "transient_door_top")}
# the variants of minecraft:iron_door, with the model names of the door
VANILLA_DOOR = {
    "east": 0, "south": 90, "west": 180, "north": 270}
variants = {}
for facing, rot in VANILLA_DOOR.items():
    for half in ("lower", "upper"):
        for hinge in ("left", "right"):
            for open_ in ("false", "true"):
                part = "bottom" if half == "lower" else "top"
                model = f"{part}_{hinge}" + ("_open" if open_ == "true" else "")
                y = rot
                if open_ == "true":
                    y = (rot + (90 if hinge == "left" else 270)) % 360
                v = {"model": model}
                if y:
                    v["y"] = y
                variants[f"facing={facing},half={half},hinge={hinge},open={open_}"] = v
for name, (bottom, top) in DOORS.items():
    write(f"assets/{NS}/blockstates/{name}.json", {"variants": {k: dict(v, model=f"{NS}:block/{name}_{v['model']}") for k, v in variants.items()}})
    for part in ("bottom", "top"):
        for hinge in ("left", "right"):
            for open_ in ("", "_open"):
                write(f"assets/{NS}/models/block/{name}_{part}_{hinge}{open_}.json",
                      {"parent": f"minecraft:block/door_{part}_{hinge}{open_}", "textures": {"bottom": f"{NS}:block/{bottom}", "top": f"{NS}:block/{top}"}})

# ---- cubes and the trapdoor ----
CUBES = {"fabric_of_reality": "fabric_of_reality", "ancient_fabric": "eternal_fabric", "eternal_fabric": "eternal_fabric",
         "unraveled_fabric": "unraveled_fabric"}
for name, tex in CUBES.items():
    write(f"assets/{NS}/blockstates/{name}.json", {"variants": {"": {"model": f"{NS}:block/{name}"}}})
    write(f"assets/{NS}/models/block/{name}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{NS}:block/{tex}"}})
    write(f"assets/{NS}/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/{name}"}})
write(f"assets/{NS}/blockstates/rift.json", {"variants": {"": {"model": f"{NS}:block/rift"}}})
write(f"assets/{NS}/models/block/rift.json", {"textures": {"particle": f"{NS}:block/rift"}})
trap = {}
# as minecraft:iron_trapdoor: closed halves unturned, open ones turned to their facing
for facing, rot in {"north": 0, "east": 90, "south": 180, "west": 270}.items():
    for half in ("bottom", "top"):
        for open_ in ("false", "true"):
            v = {"model": f"{NS}:block/transdimensional_trapdoor_{'open' if open_ == 'true' else half}"}
            if open_ == "true" and rot:
                v["y"] = rot
            trap[f"facing={facing},half={half},open={open_}"] = v
write(f"assets/{NS}/blockstates/transdimensional_trapdoor.json", {"variants": trap})
for part in ("bottom", "top", "open"):
    write(f"assets/{NS}/models/block/transdimensional_trapdoor_{part}.json",
          {"parent": f"minecraft:block/template_trapdoor_{part}", "textures": {"texture": f"{NS}:block/transdimensional_trapdoor"}})
write(f"assets/{NS}/items/transdimensional_trapdoor.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/transdimensional_trapdoor_bottom"}})

# ---- items ----
for name in ("dimensional_door", "warp_door", "unstable_door", "rift_signature", "stabilized_rift_signature", "rift_remover", "stable_fabric",
             "rift_blade"):
    write(f"assets/{NS}/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{name}"}})
    write(f"assets/{NS}/models/item/{name}.json",
          {"parent": "minecraft:item/handheld" if name == "rift_blade" else "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{name}"}})
write(f"assets/{NS}/items/monolith_spawn_egg.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/monolith_spawn_egg",
                                                               "tints": [{"type": "minecraft:constant", "value": -16777216}]}})
write(f"assets/{NS}/models/item/monolith_spawn_egg.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "minecraft:item/egg"}})

# ---- sounds ----
write(f"assets/{NS}/sounds.json", {v: {"sounds": [f"{NS}:{v}"]} for v in SOUNDS.values()})

# ---- language ----
NAMES = {"dimensional_door": ("Dimensional Door", "Dimensionstür"), "warp_door": ("Warp Door", "Warptür"),
         "unstable_door": ("Unstable Door", "Instabile Tür"), "transient_door": ("transientDoor", "transientDoor"),
         "transdimensional_trapdoor": ("Transdimensional Trapdoor", "Transdimensionale Falltür"),
         "fabric_of_reality": ("Fabric of Reality", "Gewebe der Realität"), "ancient_fabric": ("Ancient Fabric", "Uraltes Gewebe"),
         "eternal_fabric": ("Eternal Fabric", "Ewiges Gewebe"), "unraveled_fabric": ("Unraveled Fabric", "Aufgelöstes Gewebe"),
         "rift": ("Rift", "Riss")}
ITEM_NAMES = {"dimensional_door": ("Dimensional Door", "Dimensionstür"), "warp_door": ("Warp Door", "Warptür"),
              "unstable_door": ("Unstable Door", "Instabile Tür"), "rift_signature": ("Rift Signature", "Riss-Signatur"),
              "stabilized_rift_signature": ("Stabilized Rift Signature", "Stabilisierte Riss-Signatur"),
              "rift_remover": ("Rift Remover", "Riss-Entferner"), "stable_fabric": ("Stable Fabric", "Stabiles Gewebe"),
              "rift_blade": ("Rift Blade", "Rissklinge"), "monolith_spawn_egg": ("Monolith Spawn Egg", "Monolith-Spawn-Ei")}
for index, lang in ((0, "en_us"), (1, "de_de")):
    data = {f"block.{NS}.{k}": v[index] for k, v in NAMES.items()}
    data.update({f"item.{NS}.{k}": v[index] for k, v in ITEM_NAMES.items()})
    data.update({f"item.{NS}.{k}": v[index] for k, v in NAMES.items() if k in CUBES or k == "transdimensional_trapdoor"})
    data[f"entity.{NS}.monolith"] = "Monolith"
    data[f"biome.{NS}.limbo"] = "Limbo"
    data[f"biome.{NS}.pocket"] = ["Pocket Dimension", "Taschendimension"][index]
    write(f"assets/{NS}/lang/{lang}.json", data)

# ---- loot tables ----
def door_loot(name, drop):
    return {"type": "minecraft:block", "pools": [{"condition": {"type": "minecraft:survives_explosion"}, "rolls": 1, "entries": [
        {"type": "minecraft:item", "name": drop,
         "condition": {"type": "minecraft:match_block", "blocks": f"{NS}:{name}", "state": {"half": "lower"}}}]}],
            "random_sequence": f"{NS}:blocks/{name}"}


write(f"data/{NS}/loot_table/blocks/dimensional_door.json", door_loot("dimensional_door", "minecraft:iron_door"))
write(f"data/{NS}/loot_table/blocks/unstable_door.json", door_loot("unstable_door", "minecraft:iron_door"))
write(f"data/{NS}/loot_table/blocks/warp_door.json", door_loot("warp_door", "minecraft:oak_door"))
for name in ("transdimensional_trapdoor", "unraveled_fabric"):
    write(f"data/{NS}/loot_table/blocks/{name}.json", {"type": "minecraft:block", "pools": [{"condition": {"type": "minecraft:survives_explosion"},
          "rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{NS}:{name}"}]}], "random_sequence": f"{NS}:blocks/{name}"})
SILK = {"type": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [
    {"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}
for name in ("fabric_of_reality", "ancient_fabric"):
    # BlockDimWall: quantityDropped 0, canSilkHarvest; damageDropped 0 made ancient fabric drop fabric of reality
    write(f"data/{NS}/loot_table/blocks/{name}.json", {"type": "minecraft:block", "pools": [{"condition": SILK, "rolls": 1,
          "entries": [{"type": "minecraft:item", "name": f"{NS}:fabric_of_reality"}]}], "random_sequence": f"{NS}:blocks/{name}"})

# ---- tags ----
write(f"data/minecraft/tags/block/mineable/pickaxe.json", {"values": [f"{NS}:dimensional_door", f"{NS}:unstable_door",
                                                                      f"{NS}:transdimensional_trapdoor"]})
write(f"data/minecraft/tags/block/mineable/shovel.json", {"values": [f"{NS}:fabric_of_reality", f"{NS}:ancient_fabric", f"{NS}:unraveled_fabric"]})
write(f"data/minecraft/tags/block/mineable/axe.json", {"values": [f"{NS}:warp_door"]})
write(f"data/{NS}/tags/item/repairs/rift_blade.json", {"values": [f"{NS}:stable_fabric"]})


# ---- recipes (the pack allows all of them) ----
def shaped(name, result, pattern, key, count=1, suffix=""):
    write(f"data/{NS}/recipe/{name}{suffix}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
                                                    "key": key, "result": {"id": result, "count": count}})


PEARL = "minecraft:ender_pearl"
SF = f"{NS}:stable_fabric"
for x, suffix in ((PEARL, ""), (SF, "_from_stable_fabric")):
    shaped("dimensional_door", f"{NS}:dimensional_door", ["yxy"], {"x": x, "y": "minecraft:iron_door"}, suffix=suffix)
    shaped("warp_door", f"{NS}:warp_door", ["yxy"], {"x": x, "y": "minecraft:oak_door"}, suffix=suffix)
    shaped("transdimensional_trapdoor", f"{NS}:transdimensional_trapdoor", ["y", "x", "y"], {"x": x, "y": "minecraft:oak_trapdoor"},
           suffix=suffix)
    shaped("rift_signature", f"{NS}:rift_signature", [" y ", "yxy", " y "], {"x": x, "y": "minecraft:iron_ingot"}, suffix=suffix)
shaped("unstable_door", f"{NS}:unstable_door", ["yxy"], {"x": "minecraft:ender_eye", "y": f"{NS}:dimensional_door"})
shaped("rift_remover", f"{NS}:rift_remover", [" y ", "yxy", " y "], {"x": PEARL, "y": "minecraft:gold_ingot"})
shaped("rift_remover", f"{NS}:rift_remover", ["yyy", "yxy", "yyy"], {"x": SF, "y": "minecraft:gold_ingot"}, suffix="_from_stable_fabric")
shaped("rift_blade", f"{NS}:rift_blade", ["x", "x", "y"], {"x": PEARL, "y": f"{NS}:rift_remover"})
shaped("stable_fabric", SF, [" y ", "yxy", " y "], {"x": PEARL, "y": f"{NS}:fabric_of_reality"}, count=4)
shaped("stabilized_rift_signature", f"{NS}:stabilized_rift_signature", [" y ", "yxy", " y "], {"x": f"{NS}:rift_signature", "y": SF})

# ---- dimensions ----
NO_SPAWNS = {"argument": {"spawn_costs": {}, "spawns_by_category": {c: [] for c in (
    "ambient", "axolotls", "creature", "misc", "monster", "underground_water_creature", "water_ambient", "water_creature")}}, "modifier": "overlay"}
for biome, sky, fog in (("limbo", "#000000", "#333333"), ("pocket", "#000000", "#000000")):
    write(f"data/{NS}/worldgen/biome/{biome}.json", {
        "attributes": {"minecraft:gameplay/natural_mob_spawns": NO_SPAWNS, "minecraft:visual/sky_color": sky, "minecraft:visual/fog_color": fog},
        "carvers": [], "downfall": 0.0, "effects": {"water_color": "#3f76e4"}, "features": [], "has_precipitation": False, "temperature": 0.5})
NO_BEDS = {"can_set_spawn": "never", "can_sleep": "never", "destroy_on_use": True}
base_type = {"coordinate_scale": 1.0, "has_ceiling": False, "has_ender_dragon_fight": False, "height": 256, "logical_height": 256, "min_y": 0,
             "infiniburn": "#minecraft:infiniburn_overworld", "monster_spawn_block_light_limit": 0, "monster_spawn_light_level": 0}
# LimboProvider: sky, the sun always at noon, black sky, grey fog, no clouds, its light curve made everything bright
write(f"data/{NS}/dimension_type/limbo.json", dict(base_type, ambient_light=0.75, has_skylight=True, has_fixed_time=True, attributes={
    "minecraft:gameplay/bed_rule": NO_BEDS, "minecraft:gameplay/straw_bed_rule": NO_BEDS, "minecraft:gameplay/respawn_anchor_works": False,
    "minecraft:visual/sky_color": "#000000", "minecraft:visual/fog_color": "#333333", "minecraft:visual/cloud_color": "#00000000"}))
# pocketProvider: no sky, black sky and fog
write(f"data/{NS}/dimension_type/pocket.json", dict(base_type, ambient_light=0.0, has_skylight=False, has_fixed_time=True, skybox="none",
                                                    attributes={"minecraft:gameplay/bed_rule": NO_BEDS, "minecraft:gameplay/straw_bed_rule": NO_BEDS,
                                                                "minecraft:gameplay/respawn_anchor_works": False,
                                                                "minecraft:visual/sky_color": "#000000", "minecraft:visual/fog_color": "#000000",
                                                                "minecraft:visual/cloud_color": "#00000000"}))
write(f"data/{NS}/dimension/limbo.json", {"type": f"{NS}:limbo", "generator": {"type": f"{NS}:limbo",
                                                                               "biome_source": {"type": "minecraft:fixed", "biome": f"{NS}:limbo"}}})
write(f"data/{NS}/dimension/pocket.json", {"type": f"{NS}:pocket", "generator": {"type": "minecraft:flat", "settings": {
    "biome": f"{NS}:pocket", "features": False, "lakes": False, "layers": [], "structure_overrides": []}}})
print("written")
