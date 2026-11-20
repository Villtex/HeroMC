package hexxit.worldgen152.core;

/**
 * A generated 1.5.2 chunk column before population: 16x16x128 block ids (index (x * 16 + z) * 128 + y,
 * see {@link OldBlocks}) and the biome id of each column (index x + z * 16).
 *
 * <p>Terrain of chunk providers that wrote ids above 255 and metadata (Twilight Forest's short array) comes as
 * {@code cells} instead, {@code id << 4 | metadata} with the same index; {@code blocks} is then {@code null}.
 */
public record OldChunk(int chunkX, int chunkZ, byte[] blocks, byte[] biomes, char[] cells) {
    public static final int HEIGHT = 128;

    public OldChunk(int chunkX, int chunkZ, byte[] blocks, byte[] biomes) {
        this(chunkX, chunkZ, blocks, biomes, null);
    }

    /** Terrain with ids up to 4095 and metadata. */
    public static OldChunk wide(int chunkX, int chunkZ, char[] cells, byte[] biomes) {
        return new OldChunk(chunkX, chunkZ, null, biomes, cells);
    }

    /** id << 4 | metadata. */
    public int cell(int x, int y, int z) {
        int index = (x * 16 + z) * HEIGHT + y;
        return this.cells != null ? this.cells[index] : (this.blocks[index] & 0xFF) << 4;
    }

    public int blockId(int x, int y, int z) {
        return this.cell(x, y, z) >> 4;
    }

    public int biomeId(int x, int z) {
        return this.biomes[x + z * 16] & 0xFF;
    }

    /** Y of the highest non-air block of a column, or -1. */
    public int topY(int x, int z) {
        for (int y = HEIGHT - 1; y >= 0; y--) {
            if (this.cell(x, y, z) != 0) {
                return y;
            }
        }
        return -1;
    }
}
