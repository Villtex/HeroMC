package hexxit.worldgen152.core;

/**
 * A generated 1.5.2 chunk column before population: 16x16x128 block ids (index (x * 16 + z) * 128 + y,
 * see {@link OldBlocks}) and the biome id of each column (index x + z * 16).
 */
public record OldChunk(int chunkX, int chunkZ, byte[] blocks, byte[] biomes) {
    public static final int HEIGHT = 128;

    public int blockId(int x, int y, int z) {
        return this.blocks[(x * 16 + z) * HEIGHT + y] & 0xFF;
    }

    public int biomeId(int x, int z) {
        return this.biomes[x + z * 16] & 0xFF;
    }

    /** Y of the highest non-air block of a column, or -1. */
    public int topY(int x, int z) {
        int base = (x * 16 + z) * HEIGHT;
        for (int y = HEIGHT - 1; y >= 0; y--) {
            if (this.blocks[base + y] != 0) {
                return y;
            }
        }
        return -1;
    }
}
