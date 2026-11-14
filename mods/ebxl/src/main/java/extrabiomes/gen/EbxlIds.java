package extrabiomes.gen;

/**
 * Block ids of ExtrabiomesXL in the Hexxit pack ({@code config/extrabiomes/extrabiomes.cfg}) and the metadata
 * values of their sub-blocks, as the 1.5.2 world generator wrote them. No game classes: tools/expect.sh
 * compiles this with plain javac.
 */
public final class EbxlIds {
    public static final int AUTUMN_LEAVES = 902;
    public static final int CATTAIL = 903;
    public static final int CUSTOM_LOG = 904;
    public static final int FLOWER = 908;
    public static final int GRASS = 909;
    public static final int GREEN_LEAVES = 910;
    public static final int LEAF_PILE = 911;
    public static final int PLANKS = 912;
    /** quarterlog0..3: bark on the north-west, north-east, south-west, south-east side. */
    public static final int QUARTER_LOG_NW = 913;
    public static final int QUARTER_LOG_NE = 914;
    public static final int QUARTER_LOG_SW = 915;
    public static final int QUARTER_LOG_SE = 916;
    public static final int QUICKSAND = 917;
    public static final int RED_ROCK = 254;
    public static final int CRACKED_SAND = 255;
    public static final int SAPLING = 922;

    // BlockAutumnLeaves.BlockType
    public static final int AUTUMN_BROWN = 0;
    public static final int AUTUMN_ORANGE = 1;
    public static final int AUTUMN_PURPLE = 2;
    public static final int AUTUMN_YELLOW = 3;
    // BlockGreenLeaves.BlockType
    public static final int LEAVES_FIR = 0;
    public static final int LEAVES_REDWOOD = 1;
    public static final int LEAVES_ACACIA = 2;
    // BlockCustomLog.BlockType
    public static final int LOG_FIR = 0;
    public static final int LOG_ACACIA = 1;
    // BlockQuarterLog.BlockType
    public static final int QUARTER_REDWOOD = 0;
    public static final int QUARTER_FIR = 1;
    public static final int QUARTER_OAK = 2;
    // BlockCustomFlower.BlockType
    public static final int FLOWER_AUTUMN_SHRUB = 0;
    public static final int FLOWER_HYDRANGEA = 1;
    public static final int FLOWER_ORANGE = 2;
    public static final int FLOWER_PURPLE = 3;
    public static final int FLOWER_TINY_CACTUS = 4;
    public static final int FLOWER_ROOT = 5;
    public static final int FLOWER_TOADSTOOL = 6;
    public static final int FLOWER_WHITE = 7;
    // BlockCustomTallGrass.BlockType
    public static final int GRASS_BROWN = 0;
    public static final int GRASS_SHORT_BROWN = 1;
    public static final int GRASS_DEAD = 2;
    public static final int GRASS_DEAD_TALL = 3;
    public static final int GRASS_DEAD_YELLOW = 4;
    // BlockCustomSapling.BlockType
    public static final int SAPLING_BROWN = 0;
    public static final int SAPLING_ORANGE = 1;
    public static final int SAPLING_PURPLE = 2;
    public static final int SAPLING_YELLOW = 3;
    public static final int SAPLING_FIR = 4;
    public static final int SAPLING_REDWOOD = 5;
    public static final int SAPLING_ACACIA = 6;

    private EbxlIds() {
    }
}
