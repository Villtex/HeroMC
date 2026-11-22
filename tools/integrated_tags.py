#!/usr/bin/env python3
"""Biome tags of the ExtrabiomesXL biomes and the mob spawns of the Hexxit mods for Hexxit Integrated (part 26d).

1. Writes the biome tags (c:, minecraft:, minecraft:has_structure/*) of the 28 ExtrabiomesXL biomes, so that the
   structures and features of today and of other mods find them, plus worldgen152:old_biomes (all biomes of
   1.5.2: worldgen152 and ExtrabiomesXL).
2. Rewrites every neoforge:add_spawns biome modifier of the mods (and the wild chocobo tag of ChocoCraft): the
   biome list of 1.5.2 stays as it is, and every other biome (vanilla, other mods) gets the spawn through the tags
   that stand for the listed biomes:
       {"type": "neoforge:or", "values": [[<old list>], {"type": "neoforge:and", "values": [<tag or "or" of tags>,
        {"type": "neoforge:not", "value": "#worldgen152:old_biomes"}]}]}
   so the old biomes (Hexxit Legacy, and ExtrabiomesXL in Integrated) keep exactly the spawns of the original.
   Run again after changing an old list: the old list is read back from the "or".

Usage:  python3 tools/integrated_tags.py
"""
import glob, json, os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
EBXL_DATA = os.path.join(ROOT, "mods/ebxl/src/main/resources/data")
WG_DATA = os.path.join(ROOT, "mods/worldgen152/src/main/resources/data")

FOREST = ["c:is_forest", "minecraft:is_forest", "c:is_tree/deciduous"]
CONIFER = ["c:is_tree/coniferous"]
TAIGA = ["minecraft:is_taiga", "c:is_taiga"]
HILL = ["minecraft:is_hill", "c:is_hill"]
MOUNTAIN = ["c:is_mountain", "minecraft:is_mountain"]
HOT, COLD, TEMPERATE = ["c:is_hot", "c:is_hot/overworld"], ["c:is_cold", "c:is_cold/overworld"], ["c:is_temperate", "c:is_temperate/overworld"]
WET, DRY = ["c:is_wet", "c:is_wet/overworld"], ["c:is_dry", "c:is_dry/overworld"]
DENSE = ["c:is_dense_vegetation", "c:is_dense_vegetation/overworld"]
SPARSE = ["c:is_sparse_vegetation", "c:is_sparse_vegetation/overworld"]
SNOWY = ["c:is_snowy"]
JUNGLE = ["minecraft:is_jungle", "c:is_jungle", "c:is_tree/jungle"]

EBXL = {
    "alpine": SNOWY + COLD + MOUNTAIN + ["c:is_mountain/peak"] + CONIFER + SPARSE,
    "autumn_woods": FOREST + TEMPERATE,
    "birch_forest": FOREST + TEMPERATE + ["c:is_birch_forest"],
    "extreme_jungle": JUNGLE + ["c:is_mountain"] + HOT + WET + DENSE,
    "forested_hills": FOREST + HILL + TEMPERATE,
    "forested_island": FOREST + TEMPERATE,
    "glacier": ["c:is_icy"] + SNOWY + COLD + MOUNTAIN + ["c:is_mountain/peak"],
    "green_hills": ["c:is_plains", "c:is_floral"] + HILL + TEMPERATE,
    "green_swamp": ["c:is_swamp"] + WET + TEMPERATE,
    "ice_wasteland": ["c:is_icy", "c:is_snowy_plains"] + SNOWY + COLD + SPARSE,
    "marsh": ["c:is_swamp"] + WET + TEMPERATE,
    "meadow": ["c:is_plains", "c:is_floral"] + TEMPERATE,
    "mini_jungle": JUNGLE + HOT + WET + DENSE,
    "mountain_desert": ["c:is_desert", "c:is_sandy"] + HILL + HOT + DRY + SPARSE,
    "mountain_ridge": ["c:is_badlands", "c:is_plateau", "c:is_mountain"] + HOT + DRY + SPARSE,
    "mountain_taiga": TAIGA + CONIFER + HILL + COLD,
    "pine_forest": TAIGA + ["c:is_forest"] + CONIFER + TEMPERATE,
    "rainforest": FOREST + HILL + HOT + WET + DENSE + ["c:is_lush"],
    "redwood_forest": ["c:is_forest", "c:is_old_growth"] + CONIFER + HOT + WET + DENSE,
    "redwood_lush": ["c:is_forest", "c:is_old_growth", "c:is_lush"] + CONIFER + HOT + WET + DENSE,
    "savanna": ["c:is_savanna", "minecraft:is_savanna", "c:is_tree/savanna"] + HOT + DRY + SPARSE,
    "shrubland": ["c:is_plains"] + TEMPERATE + SPARSE,
    "snowy_forest": FOREST + SNOWY + COLD,
    "snowy_rainforest": ["c:is_forest"] + TAIGA + CONIFER + SNOWY + COLD + WET + DENSE + HILL,
    "temperate_rainforest": ["c:is_forest"] + TAIGA + CONIFER + TEMPERATE + WET + DENSE + HILL,
    "tundra": ["c:is_snowy_plains", "c:is_plains"] + SNOWY + COLD + SPARSE,
    "wasteland": ["c:is_wasteland", "c:is_dead"] + HOT + DRY + SPARSE,
    "woodlands": FOREST + TEMPERATE,
}

# minecraft:has_structure/<name>; the overworld-wide ones (stronghold, Hexxit structures) come with is_overworld
PLAINS_VILLAGE = ["autumn_woods", "birch_forest", "forested_hills", "forested_island", "green_hills", "meadow", "shrubland",
                  "woodlands", "rainforest"]
TAIGA_VILLAGE = ["pine_forest", "mountain_taiga", "redwood_forest", "redwood_lush", "temperate_rainforest"]
SNOWY_VILLAGE = ["tundra", "ice_wasteland", "snowy_forest", "snowy_rainforest"]
DESERTS = ["mountain_desert", "wasteland"]
JUNGLES = ["extreme_jungle", "mini_jungle", "rainforest"]
SWAMPS = ["green_swamp", "marsh"]
STRUCTURES = {
    "village_plains": PLAINS_VILLAGE,
    "village_taiga": TAIGA_VILLAGE,
    "village_snowy": SNOWY_VILLAGE,
    "village_savanna": ["savanna"],
    "village_desert": DESERTS,
    "pillager_outpost": ["meadow", "shrubland", "savanna", "tundra", "pine_forest", "mountain_taiga", "alpine"] + DESERTS,
    "mineshaft": [b for b in EBXL if b != "mountain_ridge"],
    "mineshaft_mesa": ["mountain_ridge"],
    "ruined_portal_standard": PLAINS_VILLAGE + TAIGA_VILLAGE + SNOWY_VILLAGE + ["savanna"],
    "ruined_portal_desert": DESERTS,
    "ruined_portal_jungle": JUNGLES,
    "ruined_portal_swamp": SWAMPS,
    "ruined_portal_mountain": ["alpine", "glacier", "mountain_ridge"],
    "desert_pyramid": DESERTS,
    "jungle_temple": ["extreme_jungle", "mini_jungle"],
    "swamp_hut": SWAMPS,
    "igloo": ["tundra", "ice_wasteland", "snowy_forest"],
    "woodland_mansion": ["temperate_rainforest"],
    "trail_ruins": ["pine_forest", "redwood_forest", "redwood_lush", "birch_forest", "mini_jungle"],
}

# For the spawn lists: the tag(s) that stand for a biome of 1.5.2 among the biomes of today
OLD_TAGS = {
    "worldgen152:plains": ["#c:is_plains"],
    "worldgen152:forest": ["#minecraft:is_forest"], "worldgen152:forest_hills": ["#minecraft:is_forest"],
    "worldgen152:taiga": ["#minecraft:is_taiga"], "worldgen152:taiga_hills": ["#minecraft:is_taiga"],
    "worldgen152:jungle": ["#minecraft:is_jungle"], "worldgen152:jungle_hills": ["#minecraft:is_jungle"],
    "worldgen152:desert": ["#c:is_desert"], "worldgen152:desert_hills": ["#c:is_desert"],
    "worldgen152:swampland": ["#c:is_swamp"],
    "worldgen152:extreme_hills": ["#c:is_windswept"], "worldgen152:extreme_hills_edge": ["#c:is_windswept"],
    "worldgen152:ice_plains": ["#c:is_snowy_plains"], "worldgen152:ice_mountains": ["#c:is_icy"],
    "worldgen152:ocean": ["#minecraft:is_ocean"], "worldgen152:frozen_ocean": ["#minecraft:is_ocean"],
    "worldgen152:river": ["#minecraft:is_river"], "worldgen152:frozen_river": ["#minecraft:is_river"],
    "worldgen152:beach": ["#minecraft:is_beach"],
    "worldgen152:mushroom_island": ["#c:is_mushroom"], "worldgen152:mushroom_island_shore": ["#c:is_mushroom"],
    "extrabiomesxl:savanna": ["#c:is_savanna"], "extrabiomesxl:mountain_ridge": ["#c:is_badlands"],
    "extrabiomesxl:mountain_desert": ["#c:is_desert"], "extrabiomesxl:wasteland": ["#c:is_wasteland"],
    "extrabiomesxl:glacier": ["#c:is_icy"], "extrabiomesxl:ice_wasteland": ["#c:is_icy"],
    "extrabiomesxl:tundra": ["#c:is_snowy_plains"],
    "extrabiomesxl:green_swamp": ["#c:is_swamp"], "extrabiomesxl:marsh": ["#c:is_swamp"],
    "extrabiomesxl:extreme_jungle": ["#minecraft:is_jungle"], "extrabiomesxl:mini_jungle": ["#minecraft:is_jungle"],
    "extrabiomesxl:alpine": ["#c:is_mountain/peak"],
    "extrabiomesxl:green_hills": ["#c:is_plains"], "extrabiomesxl:meadow": ["#c:is_plains"],
    "extrabiomesxl:shrubland": ["#c:is_plains"],
}
for b in ["autumn_woods", "birch_forest", "forested_hills", "forested_island", "rainforest", "snowy_forest", "woodlands"]:
    OLD_TAGS["extrabiomesxl:" + b] = ["#minecraft:is_forest"]
for b in ["mountain_taiga", "pine_forest", "redwood_forest", "redwood_lush", "snowy_rainforest", "temperate_rainforest"]:
    OLD_TAGS["extrabiomesxl:" + b] = ["#minecraft:is_taiga"]

NOT_OLD = {"type": "neoforge:not", "value": "#worldgen152:old_biomes"}


def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


def biome_tags():
    tags = {}
    for name, names in EBXL.items():
        for tag in names + ["minecraft:is_overworld", "c:is_overworld", "extrabiomesxl:biomes", "worldgen152:old_biomes"]:
            tags.setdefault(tag, []).append("extrabiomesxl:" + name)
    for structure, names in STRUCTURES.items():
        tags["minecraft:has_structure/" + structure] = ["extrabiomesxl:" + n for n in names]
    for tag, values in tags.items():
        ns, path = tag.split(":")
        write(os.path.join(EBXL_DATA, ns, "tags/worldgen/biome", path + ".json"), {"values": sorted(set(values))})
    old = sorted(os.path.basename(p)[:-5] for p in glob.glob(os.path.join(WG_DATA, "worldgen152/worldgen/biome/*.json")))
    write(os.path.join(WG_DATA, "worldgen152/tags/worldgen/biome/old_biomes.json"), {"values": ["worldgen152:" + b for b in old]})
    print(f"{len(tags)} biome tags for {len(EBXL)} ExtrabiomesXL biomes, {len(old)} worldgen152 biomes in old_biomes")


def old_list(biomes):
    """The biome list of 1.5.2 of a spawn file, before or after this script ran."""
    if isinstance(biomes, dict) and biomes.get("type") == "neoforge:or":
        return biomes["values"][0]
    return biomes


def with_tags(old):
    tags = []
    for biome in old:
        for tag in OLD_TAGS.get(biome, []):
            if tag not in tags:
                tags.append(tag)
    if not tags:
        return old
    # a holder set list holds only ids, so several tags go into an "or"
    tagged = tags[0] if len(tags) == 1 else {"type": "neoforge:or", "values": tags}
    return {"type": "neoforge:or", "values": [old, {"type": "neoforge:and", "values": [tagged, NOT_OLD]}]}


def spawns():
    count = 0
    for path in sorted(glob.glob(os.path.join(ROOT, "mods/*/src/main/resources/data/*/neoforge/biome_modifier/*.json"))):
        with open(path) as f:
            data = json.load(f)
        if data.get("type") != "neoforge:add_spawns":
            continue
        old = old_list(data["biomes"])
        if isinstance(old, str):
            continue  # a tag already (#minecraft:is_nether)
        data["biomes"] = with_tags(old)
        write(path, data)
        count += 1
    print(f"{count} spawn biome modifiers")


def chocobos():
    """Wild yellow chocobos (ChocoCraft): biomes of today besides the configured 1.5.2 list."""
    import re
    src = open(os.path.join(ROOT, "mods/chococraft/src/main/java/chococraft/ChocoConfig.java")).read()
    block = src[src.index("PACK_BIOMES = List.of("):]
    block = block[:block.index(");")]
    old = re.findall(r'"([a-z0-9_]+:[a-z0-9_]+)"', block)
    tags = []
    for biome in old:
        for tag in OLD_TAGS.get(biome, []):
            if tag not in tags:
                tags.append(tag)
    write(os.path.join(ROOT, "mods/chococraft/src/main/resources/data/chococraft/tags/worldgen/biome/wild_chocobo_spawns.json"),
          {"values": tags})
    print(f"chococraft:wild_chocobo_spawns: {len(tags)} tags")


if __name__ == "__main__":
    biome_tags()
    spawns()
    chocobos()
