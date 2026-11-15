#!/usr/bin/env python3
"""Converts the item tree of Inventory Tweaks 1.54b (Minecraft 1.5.2, numeric ids) to the 26.3 format.

    python3 mods/inventorytweaks/tools/convert_tree.py \
        .reference/z-hexxit-configs/pack-files/config/InvTweaksTree.txt \
        mods/inventorytweaks/src/main/resources/invtweaks/DefaultTree.txt \
        --items items.txt --tags <dir with data/*/tags/item> [<dir> ...]

The category structure and order of the original stay as they are. Every 1.5.2 id/damage becomes the item(s) it is
in 26.3 (`id="minecraft:..."`, potions with `potion="minecraft:..."`), ore dictionary names become item tags
(`tag="c:..."`). Entries of blocks that had no item in 1.5.2 (water, fire, crops, door blocks ...) are kept as empty
keywords when their name is unique, otherwise dropped. Items added after 1.5.2 (and 1.5.2 items the tree did not
list) are appended to the most fitting category, see ADDITIONS. With --items/--tags the script reports every item of
the game that matches no entry of the converted tree.
"""
import argparse
import json
import os
import re
import sys
import xml.etree.ElementTree as ET

TREE_VERSION = "26.3"

# --- 1.5.2 id[:damage] -> 26.3 item ids. [] = block without item (empty keyword) -------------------------------------
M = {}


def m(key, *items):
    M[key] = list(items)


for k, v in {
    "267": "iron_sword", "276": "diamond_sword", "272": "stone_sword", "283": "golden_sword", "268": "wooden_sword",
    "261": "bow", "262": "arrow", "332": "snowball",
    "278": "diamond_pickaxe", "285": "golden_pickaxe", "257": "iron_pickaxe", "274": "stone_pickaxe", "270": "wooden_pickaxe",
    "284": "golden_shovel", "277": "diamond_shovel", "256": "iron_shovel", "273": "stone_shovel", "269": "wooden_shovel",
    "286": "golden_axe", "279": "diamond_axe", "258": "iron_axe", "275": "stone_axe", "271": "wooden_axe",
    "294": "golden_hoe", "293": "diamond_hoe", "291": "iron_hoe", "292": "stone_hoe", "290": "wooden_hoe",
    "359": "shears", "346": "fishing_rod", "398": "carrot_on_a_stick",
    "310": "diamond_helmet", "302": "chainmail_helmet", "306": "iron_helmet", "314": "golden_helmet", "298": "leather_helmet",
    "311": "diamond_chestplate", "303": "chainmail_chestplate", "307": "iron_chestplate", "315": "golden_chestplate",
    "299": "leather_chestplate",
    "312": "diamond_leggings", "304": "chainmail_leggings", "308": "iron_leggings", "316": "golden_leggings", "300": "leather_leggings",
    "313": "diamond_boots", "305": "chainmail_boots", "309": "iron_boots", "317": "golden_boots", "301": "leather_boots",
    "1": "stone", "4": "cobblestone", "48": "mossy_cobblestone", "121": "end_stone",
    "16": "coal_ore", "21": "lapis_ore", "56": "diamond_ore", "129": "emerald_ore", "3": "dirt",
    "17:0": "oak_log", "12": "sand", "24:0": "sandstone", "24:1": "chiseled_sandstone", "24:2": "cut_sandstone",
    "13": "gravel", "82": "clay", "89": "glowstone", "87": "netherrack", "88": "soul_sand", "153": "nether_quartz_ore",
    "30": "cobweb", "52": "spawner", "7": "bedrock", "19": "sponge", "2": "grass_block", "110": "mycelium",
    "60": "farmland", "80": "snow_block", "79": "ice", "120": "end_portal_frame", "49": "obsidian",
    "99": "brown_mushroom_block", "100": "red_mushroom_block", "122": "dragon_egg",
    "65": "ladder", "85": "oak_fence", "113": "nether_brick_fence", "107": "oak_fence_gate",
    "139:0": "cobblestone_wall", "139:1": "mossy_cobblestone_wall", "91": "jack_o_lantern",
    "5:0": "oak_planks", "5:1": "spruce_planks", "5:2": "birch_planks", "5:3": "jungle_planks",
    "45": "bricks", "98:0": "stone_bricks", "98:1": "mossy_stone_bricks", "98:2": "cracked_stone_bricks",
    "98:3": "chiseled_stone_bricks", "112": "nether_bricks", "20": "glass", "102": "glass_pane", "101": "iron_bars",
    "53": "oak_stairs", "134": "spruce_stairs", "135": "birch_stairs", "136": "jungle_stairs", "67": "cobblestone_stairs",
    "109": "stone_brick_stairs", "108": "brick_stairs", "114": "nether_brick_stairs", "128": "sandstone_stairs",
    "156": "quartz_stairs",
    "44:2": "petrified_oak_slab", "126:0": "oak_slab", "126:1": "spruce_slab", "126:2": "birch_slab", "126:3": "jungle_slab",
    "44:0": "smooth_stone_slab", "44:1": "sandstone_slab", "44:3": "cobblestone_slab", "44:4": "stone_brick_slab",
    "44:5": "brick_slab",
    "42": "iron_block", "41": "gold_block", "22": "lapis_block", "57": "diamond_block", "133": "emerald_block",
    "280": "stick",
    "66": "rail", "27": "powered_rail", "28": "detector_rail", "157": "activator_rail", "50": "torch",
    "123": "redstone_lamp", "124": "redstone_lamp", "33": "piston", "29": "sticky_piston", "69": "lever",
    "72": "oak_pressure_plate", "70": "stone_pressure_plate", "147": "light_weighted_pressure_plate",
    "148": "heavy_weighted_pressure_plate", "77": "stone_button", "143": "oak_button", "324": "oak_door", "330": "iron_door",
    "96": "oak_trapdoor", "75": "redstone_torch", "76": "redstone_torch", "356": "repeater", "23": "dispenser",
    "25": "note_block", "137": "command_block", "151": "daylight_detector", "152": "redstone_block", "154": "hopper",
    "158": "dropper", "404": "comparator",
    "14": "gold_ore", "15": "iron_ore", "263:0": "coal", "263:1": "charcoal", "266": "gold_ingot", "265": "iron_ingot",
    "264": "diamond", "388": "emerald", "331": "redstone", "348": "glowstone_dust", "336": "brick", "337": "clay_ball",
    "318": "flint", "406": "quartz",
    "344": "egg", "352": "bone", "334": "leather", "287": "string", "288": "feather", "289": "gunpowder",
    "341": "slime_ball", "368": "ender_pearl", "369": "blaze_rod", "370": "ghast_tear", "371": "gold_nugget",
    "399": "nether_star", "335": "milk_bucket", "296": "wheat", "339": "paper", "340": "book", "338": "sugar_cane",
    "351:15": "bone_meal", "351:0": "ink_sac", "351:1": "red_dye", "351:2": "green_dye", "351:3": "cocoa_beans",
    "351:4": "lapis_lazuli", "351:11": "yellow_dye",
    "259": "flint_and_steel", "58": "crafting_table", "61": "furnace", "54": "chest", "130": "ender_chest",
    "138": "beacon", "84": "jukebox", "386": "writable_book", "387": "written_book", "46": "tnt",
    "131": "tripwire_hook", "281": "bowl", "380": "cauldron", "379": "brewing_stand", "116": "enchanting_table",
    "381": "ender_eye", "325": "bucket", "326": "water_bucket", "327": "lava_bucket", "347": "clock",
    "345": "compass", "395": "map", "358": "filled_map", "333": "oak_boat", "328": "minecart",
    "343": "furnace_minecart", "342": "chest_minecart", "407": "tnt_minecart", "408": "hopper_minecart", "329": "saddle",
    "355": "red_bed", "321": "painting", "323": "oak_sign", "47": "bookshelf", "390": "flower_pot", "389": "item_frame",
    "282": "mushroom_stew", "320": "cooked_porkchop", "350": "cooked_cod", "364": "cooked_beef",
    "366": "cooked_chicken", "393": "baked_potato", "297": "bread", "400": "pumpkin_pie", "360": "melon_slice",
    "391": "carrot", "396": "golden_carrot", "260": "apple", "357": "cookie", "319": "porkchop", "349": "cod",
    "363": "beef", "365": "chicken", "392": "potato", "367": "rotten_flesh", "375": "spider_eye",
    "394": "poisonous_potato", "354": "cake", "353": "sugar", "37": "dandelion", "38": "poppy",
    "39": "brown_mushroom", "40": "red_mushroom", "6:0": "oak_sapling", "6:1": "spruce_sapling", "6:2": "birch_sapling",
    "6:3": "jungle_sapling", "18:0": "oak_leaves", "18:1": "spruce_leaves", "18:2": "birch_leaves",
    "18:3": "jungle_leaves", "295": "wheat_seeds", "361": "pumpkin_seeds", "362": "melon_seeds", "372": "nether_wart",
    "106": "vine", "81": "cactus", "103": "melon", "86": "pumpkin", "32": "dead_bush", "111": "lily_pad",
    "384": "experience_bottle", "374": "glass_bottle", "376": "fermented_spider_eye", "377": "blaze_powder",
    "378": "magma_cream", "382": "glistering_melon_slice",
}.items():
    m(k, v)

# wildcards (the 1.5.2 id covered several subtypes that are separate items now)
m("97", "infested_stone", "infested_cobblestone", "infested_stone_bricks", "infested_mossy_stone_bricks",
  "infested_cracked_stone_bricks", "infested_chiseled_stone_bricks")
m("155", "quartz_block", "chiseled_quartz_block", "quartz_pillar")
m("145", "anvil", "chipped_anvil", "damaged_anvil")
m("322", "golden_apple", "enchanted_golden_apple")
m("31", "short_grass", "fern")
m("383", *[])  # spawner eggs: filled from the item list (every *_spawn_egg)
m("397", "skeleton_skull", "wither_skeleton_skull", "zombie_head", "player_head", "creeper_head")
# the tree says damage 1 = birch, 2 = spruce, but 17:1 was spruce: map by the keyword, not the damage value
LOG_BY_NAME = {"birchwood": "birch_log", "sprucewood": "spruce_log", "junglewood": "jungle_log"}

# blocks that never were in an inventory
for k in ["8", "9", "10", "11", "90", "119", "0", "51", "43:0", "43:1", "43:2", "43:3", "43:4", "43:5", "125:0",
          "125:1", "125:2", "125:3", "63", "118", "117", "34", "64", "71", "55", "93", "94", "149", "150", "62", "26",
          "132", "140", "144", "92", "115", "83", "127", "59", "104", "105", "141", "142", "74"]:
    m(k)
# 1.5.2 tree bugs: "dandelionBlock" etc. pointed at the rail ids 27/28
BUG_NAMES = {"dandelionblock", "floweryellowblock", "roseblock", "flowerredblock"}

POTIONS = {0: "water", 16: "awkward", 32: "thick", 64: "mundane", 8192: "mundane",
           8193: "regeneration", 8257: "long_regeneration", 8225: "strong_regeneration",
           8194: "swiftness", 8258: "long_swiftness", 8226: "strong_swiftness",
           8195: "fire_resistance", 8259: "long_fire_resistance", 8227: None,
           8197: "healing", 8261: None, 8229: "strong_healing",
           8201: "strength", 8265: "long_strength", 8233: "strong_strength",
           8196: "poison", 8260: "long_poison", 8228: "strong_poison",
           8200: "weakness", 8264: "long_weakness", 8232: None,
           8202: "slowness", 8266: "long_slowness", 8234: "strong_slowness",
           8204: "harming", 8268: None, 8236: "strong_harming"}

ORE_DICT = {"logWood": "minecraft:logs", "plankWood": "minecraft:planks", "stairWood": "minecraft:wooden_stairs",
            "slabWood": "minecraft:wooden_slabs", "stickWood": "c:rods/wooden", "treeSapling": "minecraft:saplings",
            "treeLeaves": "minecraft:leaves"}
for metal in ["gold", "silver", "lead", "iron", "copper", "tin"]:
    ORE_DICT["ore" + metal.capitalize()] = "c:ores/" + metal
for metal in ["gold", "silver", "lead", "bronze", "brass", "iron", "tin", "copper"]:
    ORE_DICT["dust" + metal.capitalize()] = "c:dusts/" + metal
    ORE_DICT["ingot" + metal.capitalize()] = "c:ingots/" + metal
for color in ["white", "black", "red", "green", "brown", "blue", "purple", "cyan", "light_gray", "gray", "pink", "lime",
              "yellow", "light_blue", "magenta", "orange"]:
    ORE_DICT["dye" + "".join(p.capitalize() for p in color.split("_"))] = "c:dyes/" + color

COLORS = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple",
          "blue", "brown", "green", "red", "black"]
WOOL_NAMES = {"whitewool": 0, "orangewool": 1, "magentawool": 2, "lightbluewool": 3, "yellowwool": 4,
              "lightgreenwool": 5, "limewool": 5, "pinkwool": 6, "graywool": 7, "lightgraywool": 8, "cyanwool": 9,
              "purplewool": 10, "bluewool": 11, "brownwool": 12, "greenwool": 13, "darkgreenwool": 13, "redwool": 14,
              "blackwool": 15}
DISCS_152 = ["13", "cat", "blocks", "chirp", "far", "mall", "mellohi", "stal", "strad", "ward", "11"]

# --- additions: items that the 1.5.2 tree did not have ------------------------------------------------------------
# (regex on the item path, category element name of the tree (lower case), position) - first match wins.
# position: "end" = append to the category, or "after:<elementname>" = after that element of the category.
ADDITIONS = [
    # equipment
    (r"^(netherite|copper)_sword$", "sword", "end"),
    (r"_spear$", "weapon", "after:sword"),
    (r"^(trident|mace)$", "weapon", "end"),
    (r"^(crossbow|spectral_arrow|tipped_arrow|wind_charge|firework_rocket)$", "munition", "end"),
    (r"^(netherite|copper)_pickaxe$", "pickaxe", "end"),
    (r"^(netherite|copper)_shovel$", "shovel", "end"),
    (r"^(netherite|copper)_axe$", "axe", "end"),
    (r"^(netherite|copper)_hoe$", "hoe", "end"),
    (r"^(brush|warped_fungus_on_a_stick|spyglass)$", "tool", "end"),
    (r"^((netherite|copper)_helmet|turtle_helmet)$", "helmet", "end"),
    (r"^((netherite|copper)_chestplate|elytra)$", "chestplate", "end"),
    (r"^(netherite|copper)_leggings$", "leggings", "end"),
    (r"^(netherite|copper)_boots$", "boots", "end"),
    (r"^(shield|totem_of_undying)$", "armor", "end"),
    # natural blocks
    (r"^(granite|diorite|andesite|deepslate|cobbled_deepslate|tuff|calcite|smooth_stone|blackstone|basalt|"
     r"smooth_basalt|dripstone_block|pointed_dripstone|end_stone_bricks|cinnabar|sulfur|potent_sulfur)$", "stones", "end"),
    (r"^(deepslate_(coal|lapis|diamond|emerald)_ore)$", "oreblock", "end"),
    (r"^(coarse_dirt|rooted_dirt|podzol|mud|dirt_path)$", "naturalblock", "after:dirt"),
    (r"^(acacia|dark_oak|mangrove|cherry|pale_oak|poplar)_log$", "wood", "after:junglewood"),
    (r"^(crimson|warped)_stem$", "wood", "after:junglewood"),
    (r"^bamboo_block$", "wood", "after:junglewood"),
    (r"^(red_sand|suspicious_sand)$", "sands", "after:sand"),
    (r"^(red_sandstone|chiseled_red_sandstone|cut_red_sandstone|smooth_sandstone|smooth_red_sandstone)$", "sandstone", "end"),
    (r"^suspicious_gravel$", "naturalblock", "after:gravel"),
    (r"^(soul_soil|crimson_nylium|warped_nylium|nether_gold_ore|ancient_debris|magma_block|nether_wart_block|"
     r"warped_wart_block|shroomlight|gilded_blackstone|crying_obsidian)$", "netherblock", "end"),
    (r"^(packed_ice|blue_ice|powder_snow_bucket)$", "naturalblock", "after:ice"),
    (r"^snow$", "naturalblock", "after:snow"),
    (r"^(mushroom_stem)$", "mushroomblock", "end"),
    (r"^(wet_sponge)$", "naturalblock", "after:sponge"),
    (r"^(amethyst_block|budding_amethyst|amethyst_cluster|(small|medium|large)_amethyst_bud|"
     r"moss_block|pale_moss_block|sculk|sculk_vein|sculk_catalyst|sculk_shrieker|reinforced_deepslate|"
     r"prismarine|sea_lantern|chorus_flower|chorus_plant|purpur_block|bee_nest|honeycomb_block|"
     r"(ochre|verdant|pearlescent)_froglight|frogspawn|sniffer_egg|turtle_egg|dried_ghast|creaking_heart|"
     r"resin_block|sulfur_spike|infested_deepslate|trial_spawner|vault)$", "naturalblock", "end"),
    # colours
    (r"^(%s)_carpet$" % "|".join(COLORS), "wool", "end"),
    # manufactured blocks
    (r"_(fence|fence_gate)$", "fence", "end"),
    (r"_wall$", "stonewall", "end"),
    (r"_(sign|hanging_sign)$", "manufacturedblock", "after:sign"),
    (r"^(acacia|dark_oak|mangrove|cherry|pale_oak|poplar|crimson|warped|bamboo)_planks$", "woodenplank", "after:jungleplank"),
    (r"^bamboo_mosaic$", "woodenplank", "after:jungleplank"),
    (r"^(stripped_.*|.*_wood|.*_hyphae)$", "wood", "end"),
    (r"^(oak|spruce|birch|jungle|acacia|dark_oak|mangrove|cherry|pale_oak|poplar|crimson|warped|bamboo)_stairs$", "woodenstairs", "end"),
    (r"_stairs$", "stairs", "end"),
    (r"^(oak|spruce|birch|jungle|acacia|dark_oak|mangrove|cherry|pale_oak|poplar|crimson|warped|bamboo|bamboo_mosaic)_slab$", "woodenslabs", "end"),
    (r"_slab$", "slabs", "end"),
    (r"^(netherite_block|coal_block|redstone_block_x|copper_block|raw_(iron|gold|copper)_block|amethyst_block_x|"
     r"(exposed|weathered|oxidized|waxed|waxed_exposed|waxed_weathered|waxed_oxidized)_copper|"
     r"(|exposed_|weathered_|oxidized_|waxed_|waxed_exposed_|waxed_weathered_|waxed_oxidized_)(cut|chiseled)_copper|"
     r"waxed_copper_block|.*copper_grate)$", "constructionblock", "after:oreblock"),
    (r"^chiseled_bookshelf$", "decoration", "after:bookshelf"),
    (r"^(.*_bricks|.*_tiles|chiseled_.*|cracked_.*|mossy_.*|polished_.*|quartz_bricks|smooth_quartz|"
     r"dark_prismarine|purpur_pillar|packed_mud|mud_bricks|resin_brick|tuff_bricks)$", "bricks", "end"),
    (r"^(tinted_glass|(%s)_stained_glass)$" % "|".join(COLORS), "glass", "after:glassblock"),
    (r"^(%s)_stained_glass_pane$" % "|".join(COLORS), "glass", "end"),
    (r"^(iron_chain|.*copper_bars|.*copper_chain)$", "constructionblock", "after:ironbars"),
    (r"^(%s)_(terracotta|glazed_terracotta)$|^terracotta$" % "|".join(COLORS), "manufacturedblock", "end"),
    (r"^(%s)_(concrete|concrete_powder)$" % "|".join(COLORS), "manufacturedblock", "end"),
    (r"^(hay_block|bone_block|dried_kelp_block|honey_block|slime_block|scaffolding)$", "constructionblock", "end"),
    (r"^(.*copper_bulb|.*copper_lantern|lantern|soul_lantern|copper_torch|soul_torch|end_rod|.*lightning_rod|"
     r"campfire|soul_campfire)$", "lighting", "end"),
    (r"^(.*copper_door|(acacia|dark_oak|mangrove|cherry|pale_oak|poplar|crimson|warped|bamboo|spruce|birch|jungle)_door|"
     r".*_trapdoor)$", "doors", "end"),
    (r"^(acacia|dark_oak|mangrove|cherry|pale_oak|poplar|crimson|warped|bamboo|spruce|birch|jungle|polished_blackstone)_button$", "buttons", "end"),
    (r"_pressure_plate$", "pressureplates", "end"),
    (r"^(observer|target|crafter|lectern|sculk_sensor|calibrated_sculk_sensor|tripwire_hook_x|chain_command_block|"
     r"repeating_command_block|command_block_minecart|structure_block|structure_void|jigsaw|barrier|light|debug_stick|"
     r"test_block|test_instance_block)$", "mechanism", "end"),
    # resources
    (r"^(deepslate_(gold|iron|copper)_ore|copper_ore|raw_(iron|gold|copper))$", "miningresources", "after:ironoreblock"),
    (r"^nether_brick$", "miningresources", "after:claybrick"),
    (r"^(copper_ingot|netherite_ingot|netherite_scrap|iron_nugget|copper_nugget|amethyst_shard|echo_shard|"
     r"prismarine_shard|prismarine_crystals|resin_clump|disc_fragment_5)$", "miningresources", "end"),
    (r"^(rabbit_hide|rabbit_foot|phantom_membrane|shulker_shell|turtle_scute|armadillo_scute|nautilus_shell|"
     r"heart_of_the_sea|breeze_rod|honeycomb|ink_sac_x|glow_ink_sac|goat_horn|dragon_breath|blue_egg|brown_egg|"
     r"heavy_core|trial_key|ominous_trial_key|ominous_bottle)$", "huntingresources", "end"),
    (r"^(%s)_dye$" % "|".join(COLORS), "dye", "end"),
    # items
    (r"^(%s)_shulker_box$|^shulker_box$" % "|".join(COLORS), "chests", "end"),
    (r"^(trapped_chest|barrel|.*copper_chest)$", "chests", "end"),
    (r"^(%s)_bundle$|^bundle$" % "|".join(COLORS), "utility", "end"),
    (r"^(smoker|blast_furnace|smithing_table|fletching_table|cartography_table|loom|grindstone|stonecutter|"
     r"composter|beehive|respawn_anchor|lodestone|conduit|bell|end_crystal|fire_charge|lead|name_tag|"
     r"armor_stand|knowledge_book|enchanted_book|recovery_compass_x|decorated_pot)$", "utility", "end"),
    (r"^(%s)_(bed)$|^straw_bed$" % "|".join(COLORS), "decoration", "after:bed"),
    (r"^(axolotl|cod|salmon|pufferfish|tropical_fish|tadpole|sulfur_cube)_bucket$", "bucket", "end"),
    (r"^(recovery_compass|.*_map)$", "instrument", "end"),
    (r"_(boat|chest_boat|raft)$", "vehicle", "after:boat"),
    (r"^(.*_horse_armor|wolf_armor|.*_nautilus_armor|(%s)_harness)$" % "|".join(COLORS), "vehicle", "end"),
    (r"^(glow_item_frame)$", "decoration", "after:frame"),
    (r"^(dragon_head|piglin_head)$", "decoration", "after:head"),
    (r"^music_disc_.*$", "musicdisc", "end"),
    (r"^(.*_pottery_sherd|.*_banner_pattern|.*_armor_trim_smithing_template|netherite_upgrade_smithing_template|"
     r"(%s)_banner|(%s)_candle|candle|(%s)_cushion|.*_shelf|chiseled_bookshelf|.*copper_golem_statue|firework_star)$"
     % ("|".join(COLORS), "|".join(COLORS), "|".join(COLORS)), "decoration", "end"),
    (r"^(cooked_(mutton|rabbit|salmon))$", "cookedfood", "end"),
    (r"^(beetroot_soup|rabbit_stew|suspicious_stew)$", "ediblefood", "after:mushroomstew"),
    (r"^(sweet_berries|glow_berries|chorus_fruit|dried_kelp|beetroot|honey_bottle|enchanted_golden_apple_x)$", "ediblefood", "end"),
    (r"^(mutton|rabbit|salmon|tropical_fish)$", "rawfood", "end"),
    (r"^(pufferfish)$", "poisonedfood", "end"),
    (r"^(popped_chorus_fruit)$", "unediblefood", "end"),
    (r"^(allium|azure_bluet|blue_orchid|cornflower|lily_of_the_valley|oxeye_daisy|(orange|pink|red|white)_tulip|"
     r"wither_rose|torchflower|sunflower|lilac|rose_bush|peony|pitcher_plant|pink_petals|wildflowers|"
     r"open_eyeblossom|closed_eyeblossom|cactus_flower|spore_blossom|golden_dandelion|firefly_bush)$", "flower", "end"),
    (r"^(crimson_fungus|warped_fungus|shelf_mushroom)$", "mushroom", "end"),
    (r"^(azalea|flowering_azalea|mangrove_propagule|(acacia|dark_oak|cherry|pale_oak|poplar)_sapling)$", "sapling", "after:junglesapling"),
    (r"_leaves$", "plant", "after:jungleleaves"),
    (r"^(beetroot_seeds|torchflower_seeds|pitcher_pod)$", "seed", "end"),
    (r"^(carved_pumpkin)$", "plant", "after:pumpkin"),
    (r"^(large_fern|tall_grass|short_dry_grass|tall_dry_grass|bush|leaf_litter|seagrass|kelp|sea_pickle|bamboo|"
     r"twisting_vines|weeping_vines|crimson_roots|warped_roots|nether_sprouts|hanging_roots|glow_lichen|"
     r"big_dripleaf|small_dripleaf|moss_carpet|pale_moss_carpet|pale_hanging_moss|mangrove_roots|"
     r"muddy_mangrove_roots|red_shrub|.*coral.*)$", "plant", "end"),
    (r"^(lingering_potion)$", "potion", "after:splashpotion"),
]


def camel(path):
    parts = re.split(r"[_/]", path)
    return parts[0] + "".join(p.capitalize() for p in parts[1:])


def resolve_tags(dirs):
    raw = {}
    for d in dirs:
        for ns in os.listdir(os.path.join(d, "data")):
            base = os.path.join(d, "data", ns, "tags", "item")
            if not os.path.isdir(base):
                continue
            for root, _, files in os.walk(base):
                for f in files:
                    if f.endswith(".json"):
                        tag = ns + ":" + os.path.relpath(os.path.join(root, f), base)[:-5].replace(os.sep, "/")
                        with open(os.path.join(root, f)) as fh:
                            values = json.load(fh).get("values", [])
                        raw.setdefault(tag, []).extend(v if isinstance(v, str) else v["id"] for v in values)
    cache = {}

    def get(tag, seen=()):
        if tag in cache:
            return cache[tag]
        out = set()
        for v in raw.get(tag, []):
            if v.startswith("#"):
                if v[1:] not in seen:
                    out |= get(v[1:], seen + (tag,))
            else:
                out.add(v)
        cache[tag] = out
        return out

    return get


class Out:
    def __init__(self):
        self.lines = []
        self.names = set()       # item keywords
        self.categories = set()  # category keywords
        self.ids = set()         # covered item ids
        self.tags = []
        self.used = set()

    def item(self, depth, name, **attrs):
        a = "".join(' %s="%s"' % (k, v) for k, v in attrs.items())
        self.lines.append("    " * depth + "<%s%s/>" % (name, a))
        self.names.add(name.lower())
        if "id" in attrs:
            self.ids.add(attrs["id"])
        if "tag" in attrs:
            self.tags.append(attrs["tag"])


def convert(root, items, additions_by_cat, out):
    spawn_eggs = sorted(i for i in items if i.endswith("_spawn_egg"))
    all_names = set()
    for el in root.iter():
        all_names.add(el.tag.lower())
    item_elements = [el for el in root.iter() if el.attrib and "treeVersion" not in el.attrib and "min" not in el.attrib]
    real_item_names = set()
    for el in item_elements:
        key = el.get("id", "") + (":" + el.get("damage") if el.get("damage") else "")
        if el.get("oreDictName") or (M.get(key) or M.get(el.get("id"))) or el.get("id") == "373":
            real_item_names.add(el.tag.lower())

    parents = {}

    def emit_additions(cat, depth, anchor):
        if (cat, anchor) in out.used:
            return  # a category name can appear twice (oreBlock): add only to the first one
        out.used.add((cat, anchor))
        for name, attrs in additions_by_cat.get((cat, anchor), []):
            out.item(depth, name, **attrs)

    def walk(el, depth):
        name = el.tag
        low = name.lower()
        if el.get("treeVersion"):
            out.lines.append('<%s treeVersion="%s">' % (name, TREE_VERSION))
            out.categories.add(low)
        elif el.get("min") == "73":  # redstone ore range
            out.lines.append("    " * depth + "<%s>" % name)
            out.categories.add(low)
            out.item(depth + 1, "redstoneOre", id="minecraft:redstone_ore")
            out.item(depth + 1, "deepslateRedstoneOre", id="minecraft:deepslate_redstone_ore")
            out.lines.append("    " * depth + "</%s>" % name)
            return
        elif el.get("min") == "2256":  # music discs
            out.lines.append("    " * depth + "<%s>" % name)
            out.categories.add(low)
            for d in DISCS_152:
                out.item(depth + 1, "musicDisc" + d.capitalize(), id="minecraft:music_disc_" + d)
            emit_additions(low, depth + 1, "end")
            out.lines.append("    " * depth + "</%s>" % name)
            return
        elif el.attrib:
            # an item (also when it has children: they belong to the enclosing category, as in the original loader)
            if el.get("oreDictName"):
                out.item(depth, name, tag=ORE_DICT[el.get("oreDictName")])
            elif el.get("id") == "373":
                potion = POTIONS[int(el.get("damage"))] if int(el.get("damage")) < 16384 else POTIONS.get(int(el.get("damage")) - 8192)
                if int(el.get("damage")) == 16384:
                    potion = "mundane"
                kind = "minecraft:splash_potion" if int(el.get("damage")) >= 16384 else "minecraft:potion"
                if potion:
                    out.item(depth, name, id=kind, potion="minecraft:" + potion)
                else:
                    out.lines.append("    " * depth + "<%s/>" % name)  # no such potion any more
                    out.categories.add(low)
            elif low in LOG_BY_NAME:
                out.item(depth, name, id="minecraft:" + LOG_BY_NAME[low])
            elif low in WOOL_NAMES:
                out.item(depth, name, id="minecraft:%s_wool" % COLORS[WOOL_NAMES[low]])
            elif el.get("id") == "18" and not el.get("damage"):
                for leaves in ["oak", "spruce", "birch", "jungle"]:
                    out.item(depth, name, id="minecraft:%s_leaves" % leaves)
            elif el.get("id") == "383":
                for egg in spawn_eggs:
                    out.item(depth, name, id="minecraft:" + egg)
            elif el.get("id") == "355":
                out.item(depth, name, id="minecraft:red_bed")
            elif el.get("id") == "323":
                out.item(depth, name, id="minecraft:oak_sign")
            else:
                key = el.get("id") + (":" + el.get("damage") if el.get("damage") else "")
                if key not in M:
                    raise SystemExit("no mapping for %s (%s)" % (key, name))
                targets = M[key]
                if low in BUG_NAMES:
                    targets = []
                if targets:
                    for t in targets:
                        out.item(depth, name, id="minecraft:" + t)
                elif low not in real_item_names and low not in out.categories:
                    out.lines.append("    " * depth + "<%s/>" % name)  # keyword of a block without item
                    out.categories.add(low)
            parent = parents.get(el)
            emit_additions(parent, depth, "after:" + low)
            for child in el:
                parents[child] = parent
                walk(child, depth)
            return
        else:
            out.lines.append("    " * depth + "<%s>" % name)
            out.categories.add(low)
        for child in el:
            parents[child] = low
            walk(child, depth + 1)
        emit_additions(low, depth + 1, "end")
        out.lines.append("    " * depth + "</%s>" % name)
        emit_additions(parents.get(el), depth, "after:" + low)

    walk(root, 0)


HEADER = """<?xml version="1.0" encoding="utf-8" ?>
<!--
|==============================================================|
| INVENTORY TWEAKS Mod - By Jimeo Wan (jimeo.wan at gmail.com) |
| Tree configuration                                           |
| (converted to Minecraft 26.3 for HeroMC)                     |
|==============================================================|

# Q: WHAT IS THIS?
# A: This file is used to store all the keywords you can use to create item rules, but also to define the items order.
# You can move/change all that you want, but it's recommended not to delete any default item.

# FORMAT
# <keyword id="minecraft:iron_sword"/>                              one item
# <keyword id="minecraft:potion" potion="minecraft:swiftness"/>     a potion (also splash/lingering potions, tipped arrows)
# <keyword tag="c:ingots/gold"/>                                    every item of an item tag (was oreDictName)
# A keyword can appear several times (e.g. "bed" for every bed). Elements without attributes are categories.

# Q: THE FILE IS BROKEN AND I DON'T KNOW WHAT TO DO =(
# A: Just delete this file, the default tree will be reloaded next time you sort something!

# CREDITS
# Thanks to Larandar & Lunaqua for their contributions.
-->
<!-- Note: 'stuff' matches everything, even items that don't appear here -->
"""


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("source")
    ap.add_argument("target")
    ap.add_argument("--items", required=True, help="file with every item id path of the game, one per line")
    ap.add_argument("--tags", nargs="*", default=[], help="directories that contain data/<ns>/tags/item")
    args = ap.parse_args()

    with open(args.items) as fh:
        items = [l.strip() for l in fh if l.strip()]
    tree = ET.parse(args.source)
    root = tree.getroot()

    # ids that the converted 1.5.2 part covers directly
    base = set()
    for v in M.values():
        base.update("minecraft:" + i for i in v)
    base.update("minecraft:" + i for i in LOG_BY_NAME.values())
    base.update("minecraft:%s_wool" % c for c in COLORS)
    base.update("minecraft:%s_leaves" % l for l in ["oak", "spruce", "birch", "jungle"])
    base.update("minecraft:" + i for i in items if i.endswith("_spawn_egg"))
    base.update("minecraft:music_disc_" + d for d in DISCS_152)
    base.update(["minecraft:redstone_ore", "minecraft:deepslate_redstone_ore", "minecraft:potion",
                 "minecraft:splash_potion"])

    additions_by_cat = {}
    unplaced = []
    for i in items:
        if "minecraft:" + i in base or i == "air":
            continue
        for regex, cat, pos in ADDITIONS:
            if re.search(regex, i):
                additions_by_cat.setdefault((cat, pos), []).append((camel(i), {"id": "minecraft:" + i}))
                break
        else:
            unplaced.append(i)
    # potions that the 1.5.2 tree did not know, tipped arrows: one keyword per potion kind
    extra_potions = ["night_vision", "long_night_vision", "invisibility", "long_invisibility", "leaping",
                     "long_leaping", "strong_leaping", "water_breathing", "long_water_breathing", "slow_falling",
                     "long_slow_falling", "luck", "turtle_master", "long_turtle_master", "strong_turtle_master",
                     "wind_charged", "weaving", "oozing", "infested"]
    for kind, cat in [("potion", "positivepotion"), ("splash_potion", "positivesplashpotion")]:
        for p in extra_potions:
            prefix = "potion" if kind == "potion" else "splashPotion"
            additions_by_cat.setdefault((cat, "end"), []).append(
                (prefix + camel(p)[0].upper() + camel(p)[1:], {"id": "minecraft:" + kind, "potion": "minecraft:" + p}))
    # any other potion (custom or modded potion types) still lands in its potion category
    additions_by_cat.setdefault(("drinkablepotion", "end"), []).append(("otherPotion", {"id": "minecraft:potion"}))
    additions_by_cat.setdefault(("splashpotion", "end"), []).append(("otherSplashPotion", {"id": "minecraft:splash_potion"}))
    additions_by_cat[("potion", "after:splashpotion")] = [("lingeringPotion", {"id": "minecraft:lingering_potion"})]

    out = Out()
    convert(root, items, additions_by_cat, out)
    text = HEADER + "\n".join(out.lines) + "\n"
    with open(args.target, "w") as fh:
        fh.write(text)

    # report
    clash = out.names & out.categories
    if clash:
        print("keywords that are both item and category:", sorted(clash), file=sys.stderr)
    unused = [k for k in additions_by_cat if k not in out.used]
    if unused:
        print("additions whose anchor was not found:", unused, file=sys.stderr)
    covered = set(out.ids)
    if args.tags:
        get = resolve_tags(args.tags)
        for t in out.tags:
            covered |= get(t)
    missing = [i for i in items if "minecraft:" + i not in covered and i != "air"]
    print("items: %d, not in the tree: %d" % (len(items), len(missing)), file=sys.stderr)
    if missing:
        print(" ".join(missing), file=sys.stderr)


if __name__ == "__main__":
    main()
