package natura.gen;

/**
 * Block ids of Natura in the Hexxit pack ({@code config/Natura.txt}), the metadata values the 1.5.2 world
 * generators wrote, and the world generation settings of that file ({@code PHNatura}). No game classes:
 * tools/expect.sh compiles this with plain javac.
 */
public final class NaturaIds {
    // ---- block ids (PHNatura field -> name in Natura.txt) ----
    /** NContent.berryBush, "Berry_Bush". */
    public static final int BERRY_BUSH = 1226;
    /** NContent.bloodwood, "Bloodwood Block". */
    public static final int BLOODWOOD = 1227;
    /** NContent.glowshroomBlue, "Blue Glowshroom". */
    public static final int GLOWSHROOM_BLUE = 1229;
    /** NContent.cloud, "Cloud Block". */
    public static final int CLOUD = 1231;
    /** NContent.crops, "Crops". */
    public static final int CROPS = 1233;
    /** NContent.darkLeaves, "Darkwood Leaves". */
    public static final int DARK_LEAVES = 1234;
    /** NContent.darkTree, "Darkwood Log". */
    public static final int DARK_TREE = 1235;
    /** NContent.floraLeaves, "Flora Leaves". */
    public static final int FLORA_LEAVES = 1238;
    /** NContent.bluebells, "Flower". */
    public static final int BLUEBELLS = 1239;
    /** NContent.glowshroom, "Glowing Mushroom". */
    public static final int GLOWSHROOM = 1241;
    /** NContent.glowshroomGreen, "Green Glowshroom". */
    public static final int GLOWSHROOM_GREEN = 1242;
    /** NContent.netherBerryBush, "Nether Berry Bush". */
    public static final int NETHER_BERRY_BUSH = 1244;
    /** NContent.planks, "Planks Block". */
    public static final int PLANKS = 1245;
    /** NContent.glowshroomPurple, "Purple Glowshroom". */
    public static final int GLOWSHROOM_PURPLE = 1246;
    /** NContent.rareLeaves, "Rare Leaves". */
    public static final int RARE_LEAVES = 1247;
    /** NContent.rareTree, "Rare Log". */
    public static final int RARE_TREE = 1248;
    /** NContent.rareSapling, "Rare Sapling". */
    public static final int RARE_SAPLING = 1249;
    /** NContent.redwood, "Redwood Block". */
    public static final int REDWOOD = 1251;
    /** NContent.saguaro, "Saguaro Cactus". */
    public static final int SAGUARO = 1253;
    /** NContent.floraLeavesNoColor, "Sakura Leaves". */
    public static final int FLORA_LEAVES_NO_COLOR = 1255;
    /** NContent.floraSapling, "Sapling". */
    public static final int FLORA_SAPLING = 1256;
    /** NContent.thornVines, "Thornvines". */
    public static final int THORN_VINES = 1257;
    /** NContent.willow, "Willow Log". */
    public static final int WILLOW = 1258;
    /** NContent.tree, "Wood Block". */
    public static final int TREE = 1259;
    /** NContent.heatSand, terrain block "Heat Sand". */
    public static final int HEAT_SAND = 252;
    /** NContent.taintedSoil, terrain block "Tainted Soil". */
    public static final int TAINTED_SOIL = 253;

    // ---- metadata ----
    // TreeBlock (TREE): the lower two bits are the wood, 4 and 8 the axis (x, z) as in BlockLog
    public static final int TREE_EUCALYPTUS = 0;
    public static final int TREE_SAKURA = 1;
    public static final int TREE_GHOSTWOOD = 2;
    public static final int TREE_HOPSEED = 3;
    // SimpleLog (REDWOOD)
    public static final int REDWOOD_BARK = 0;
    public static final int REDWOOD_HEART = 1;
    public static final int REDWOOD_ROOT = 2;
    // NLeaves (FLORA_LEAVES)
    public static final int LEAVES_REDWOOD = 0;
    public static final int LEAVES_EUCALYPTUS = 1;
    public static final int LEAVES_HOPSEED = 2;
    // NLeavesNocolor (FLORA_LEAVES_NO_COLOR)
    public static final int LEAVES_SAKURA = 0;
    public static final int LEAVES_GHOSTWOOD = 1;
    public static final int LEAVES_BLOODWOOD = 2;
    public static final int LEAVES_WILLOW = 3;
    // OverworldTreeBlock (RARE_TREE) and OverworldLeaves (RARE_LEAVES)
    public static final int RARE_MAPLE = 0;
    public static final int RARE_SILVERBELL = 1;
    public static final int RARE_PURPLEHEART = 2;
    public static final int RARE_TIGER = 3;
    // DarkTreeBlock (DARK_TREE)
    public static final int DARK_TREE_DARKWOOD = 0;
    public static final int DARK_TREE_FUSEWOOD = 1;
    // NLeavesDark (DARK_LEAVES): 0 plain, 1 flowering, 2 with fruit, 3 fusewood
    public static final int DARK_LEAVES_FUSEWOOD = 3;
    // CropBlock (CROPS): barley grows 0..3, cotton 4..8
    public static final int CROPS_BARLEY_GROWN = 3;
    public static final int CROPS_COTTON_GROWN = 8;
    // BerryBush / NetherBerryBush: kind + 4 * growth; growth 2 is full size, 3 full size with berries
    public static final int BERRY_RASPBERRY = 0;
    public static final int BERRY_BLUEBERRY = 1;
    public static final int BERRY_BLACKBERRY = 2;
    public static final int BERRY_MALOBERRY = 3;
    public static final int NETHER_BERRY_BLIGHTBERRY = 0;
    public static final int NETHER_BERRY_DUSKBERRY = 1;
    public static final int NETHER_BERRY_SKYBERRY = 2;
    public static final int NETHER_BERRY_STINGBERRY = 3;
    // CloudBlock (CLOUD)
    public static final int CLOUD_WHITE = 0;
    public static final int CLOUD_DARK = 1;
    public static final int CLOUD_ASH = 2;
    public static final int CLOUD_SULFUR = 3;
    // NSaplingBlock (FLORA_SAPLING)
    public static final int SAPLING_REDWOOD = 0;
    public static final int SAPLING_EUCALYPTUS = 1;
    public static final int SAPLING_HOPSEED = 2;
    public static final int SAPLING_SAKURA = 3;
    public static final int SAPLING_GHOSTWOOD = 4;
    public static final int SAPLING_BLOODWOOD = 5;
    public static final int SAPLING_DARKWOOD = 6;
    public static final int SAPLING_FUSEWOOD = 7;
    // OverworldSapling (RARE_SAPLING)
    public static final int RARE_SAPLING_MAPLE = 0;
    public static final int RARE_SAPLING_SILVERBELL = 1;
    public static final int RARE_SAPLING_PURPLEHEART = 2;
    public static final int RARE_SAPLING_TIGER = 3;
    public static final int RARE_SAPLING_WILLOW = 4;

    // ---- PHNatura: "general", "disabler" and "worldgen" of the pack's Natura.txt ----
    public static final int SEA_LEVEL = 64;

    public static final boolean GENERATE_REDWOOD = true;
    public static final boolean GENERATE_SAKURA = true;
    public static final boolean GENERATE_SMALL_EUCALYPTUS = true;
    public static final boolean GENERATE_BUSH = true;
    public static final boolean GENERATE_BLOODWOOD = true;
    public static final boolean GENERATE_GHOST = true;
    public static final boolean GENERATE_SAGUARO = true;
    public static final boolean GENERATE_OVERWORLD_CLOUDS = false;
    public static final boolean GENERATE_SULFUR_CLOUDS = false;
    public static final boolean GENERATE_ASH_CLOUDS = false;
    public static final boolean GENERATE_DARK_CLOUDS = false;
    public static final boolean GENERATE_PURPLEHEART = true;
    public static final boolean GENERATE_WILLOW = true;
    public static final boolean GENERATE_TIGER = true;
    public static final boolean GENERATE_SILVERBELL = true;
    public static final boolean GENERATE_MAPLE = true;
    public static final boolean GENERATE_DARKWOOD = true;
    public static final boolean GENERATE_FUSEWOOD = true;
    public static final boolean GENERATE_THORNVINES = true;
    public static final boolean GENERATE_BARLEY = true;
    public static final boolean GENERATE_COTTON = true;
    public static final boolean GENERATE_BLUEBELLS = true;
    public static final boolean GENERATE_BLUEBERRIES = true;
    public static final boolean GENERATE_BLACKBERRIES = true;
    public static final boolean GENERATE_RASPBERRIES = true;
    public static final boolean GENERATE_MALOBERRIES = true;
    public static final boolean GENERATE_BLIGHTBERRIES = true;
    public static final boolean GENERATE_DUSKBERRIES = true;
    public static final boolean GENERATE_SKYBERRIES = true;
    public static final boolean GENERATE_STINGBERRIES = true;

    public static final int REDWOOD_SPAWN_RARITY = 150;
    public static final int BLOOD_SPAWN_RARITY = 14;
    public static final int EUCALYPTUS_SHORT_SPAWN_RARITY = 25;
    public static final int EUCALYPTUS_SHORT_SPAWN_RANGE = 32;
    public static final int SAKURA_SPAWN_RARITY = 10;
    public static final int SAKURA_SPAWN_RANGE = 32;
    public static final int GHOST_SPAWN_RARITY = 10;
    public static final int BUSH_SPAWN_RARITY = 10;
    public static final int BUSH_SPAWN_RANGE = 20;
    public static final int WILLOW_RARITY = 10;
    public static final int PURPLEHEART_RARITY = 1;
    public static final int MAPLE_RARITY = 34;
    public static final int TIGER_RARITY = 30;
    public static final int SILVERBELL_RARITY = 70;
    public static final int DARK_SPAWN_RARITY = 10;
    public static final int FUSE_SPAWN_RARITY = 50;
    public static final int SAGUARO_SPAWN_RARITY = 5;
    public static final int CLOUD_SPAWN_RARITY = 10;
    public static final int CLOUD_SPAWN_HEIGHT = 192;
    public static final int CLOUD_SPAWN_RANGE = 48;
    /** "Dark Cloud Spawn Density". */
    public static final int DARK_CLOUD_SPAWN_RARITY = 10;
    /** "Dark Cloud Spawn MinX". */
    public static final int DARK_CLOUD_SPAWN_HEIGHT = 0;
    public static final int DARK_CLOUD_SPAWN_RANGE = 256;
    public static final int SULFUR_SPAWN_RARITY = 8;
    public static final int SULFUR_SPAWN_HEIGHT = 40;
    public static final int SULFUR_SPAWN_RANGE = 78;
    public static final int ASH_SPAWN_RARITY = 8;
    public static final int ASH_SPAWN_HEIGHT = 40;
    public static final int ASH_SPAWN_RANGE = 78;
    public static final int RASP_SPAWN_RARITY = 30;
    public static final int RASP_SPAWN_RANGE = 64;
    public static final int BLUE_SPAWN_RARITY = 34;
    public static final int BLUE_SPAWN_RANGE = 64;
    public static final int BLACK_SPAWN_RARITY = 48;
    public static final int BLACK_SPAWN_RANGE = 64;
    /** "Maloberry Spawn Rarity" / "Range". */
    public static final int GEO_SPAWN_RARITY = 40;
    public static final int GEO_SPAWN_RANGE = 64;
    public static final int BLIGHT_SPAWN_RARITY = 18;
    public static final int BLIGHT_SPAWN_RANGE = 100;
    public static final int DUSK_SPAWN_RARITY = 18;
    public static final int DUSK_SPAWN_RANGE = 100;
    public static final int SKY_SPAWN_RARITY = 18;
    public static final int SKY_SPAWN_RANGE = 100;
    public static final int STING_SPAWN_RARITY = 18;
    public static final int STING_SPAWN_RANGE = 100;
    public static final int THORN_SPAWN_RARITY = 40;

    private NaturaIds() {
    }
}
