package hexxit.worldgen152.core.populate;

import java.util.Map;

/**
 * A chunk after population: blocks with metadata for the full 256 block height, tile entity data, and for
 * every leaf block its distance to the nearest log.
 */
public final class OldDecoratedChunk {
    private final int chunkX;
    private final int chunkZ;
    private final char[][] sections;
    private final byte[][] leafDistance;
    private final byte[] biomes;
    private final Map<Integer, OldTile> tiles;

    OldDecoratedChunk(OldWorld.Column column, byte[][] leafDistance) {
        this.chunkX = column.chunkX;
        this.chunkZ = column.chunkZ;
        this.sections = column.sections;
        this.leafDistance = leafDistance;
        this.biomes = column.biomes;
        this.tiles = column.tiles;
    }

    public int chunkX() {
        return this.chunkX;
    }

    public int chunkZ() {
        return this.chunkZ;
    }

    /** One above the highest y that can hold a block; everything from here up is air. */
    public int height() {
        for (int i = 15; i >= 0; i--) {
            if (this.sections[i] != null) {
                return i * 16 + 16;
            }
        }
        return 0;
    }

    /** id << 4 | metadata of the block at chunk-local x, z. */
    public int cell(int x, int y, int z) {
        char[] section = this.sections[y >> 4];
        return section == null ? 0 : section[(x * 16 + z) * 16 + (y & 15)];
    }

    public int blockId(int x, int y, int z) {
        return this.cell(x, y, z) >> 4;
    }

    public int metadata(int x, int y, int z) {
        return this.cell(x, y, z) & 15;
    }

    /** Steps from a leaf block to the nearest log through leaves, 1..6, or 7 if there is none that near. */
    public int leafDistance(int x, int y, int z) {
        byte[] section = this.leafDistance[y >> 4];
        int distance = section == null ? 0 : section[(x * 16 + z) * 16 + (y & 15)];
        return distance == 0 ? 7 : distance;
    }

    public int biomeId(int x, int z) {
        return this.biomes[x + z * 16] & 0xFF;
    }

    /** Tile entity data by (x * 16 + z) * 256 + y. */
    public Map<Integer, OldTile> tiles() {
        return this.tiles;
    }

    public OldTile tile(int x, int y, int z) {
        return this.tiles.get((x * 16 + z) * OldWorld.HEIGHT + y);
    }
}
