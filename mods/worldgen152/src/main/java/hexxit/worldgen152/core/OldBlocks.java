package hexxit.worldgen152.core;

/**
 * Numeric block ids of Minecraft 1.5.2 as the terrain generator wrote them into its byte arrays.
 * Ids above 127 are negative as bytes, exactly like in the original (mod terrain blocks sit at 254/255).
 */
public final class OldBlocks {
    public static final byte AIR = 0;
    public static final byte STONE = 1;
    public static final byte GRASS = 2;
    public static final byte DIRT = 3;
    public static final byte BEDROCK = 7;
    public static final byte WATER_MOVING = 8;
    public static final byte WATER_STILL = 9;
    public static final byte LAVA_MOVING = 10;
    public static final byte LAVA_STILL = 11;
    public static final byte SAND = 12;
    public static final byte GRAVEL = 13;
    public static final byte SANDSTONE = 24;
    public static final byte ICE = 79;
    public static final byte SNOW_BLOCK = 80;
    public static final byte MYCELIUM = 110;

    private OldBlocks() {
    }
}
