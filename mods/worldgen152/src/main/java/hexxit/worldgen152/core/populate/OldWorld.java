package hexxit.worldgen152.core.populate;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.OldChunk;
import hexxit.worldgen152.core.populate.OldBlockInfo.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * The part of the 1.5.2 {@code World} that chunk population works on: block ids and metadata in 256 block
 * high columns, the height queries, sky light and the snow and ice rules. Columns start as bare terrain and
 * are created when first touched, like chunks that load on demand.
 *
 * <p>Sky light is the light of the column alone (15 above the highest light-blocking block, fading below
 * it), which is what 1.5.2 had during population in freshly generated terrain; light spreading sideways and
 * block light are not modelled.
 */
public final class OldWorld {
    public static final int HEIGHT = 256;

    /** Supplies the bare terrain of a chunk. */
    public interface TerrainSource {
        OldChunk chunk(int chunkX, int chunkZ);
    }

    static final class Column {
        final int chunkX;
        final int chunkZ;
        /** id << 4 | metadata, index (x * 16 + z) * 16 + (y & 15); a section stays null until a block is set in it. */
        final char[][] sections = new char[16][];
        final byte[] biomes;
        final Map<Integer, OldTile> tiles = new HashMap<>();
        int topSection = -1;

        Column(OldChunk terrain) {
            this.chunkX = terrain.chunkX();
            this.chunkZ = terrain.chunkZ();
            this.biomes = terrain.biomes();
            byte[] blocks = terrain.blocks();
            for (int column = 0; column < 256; column++) {
                int base = column * OldChunk.HEIGHT;
                for (int y = 0; y < OldChunk.HEIGHT; y++) {
                    int id = blocks[base + y] & 0xFF;
                    if (id != 0) {
                        this.section(y >> 4)[column * 16 + (y & 15)] = (char) (id << 4);
                    }
                }
            }
        }

        char[] section(int index) {
            char[] section = this.sections[index];
            if (section == null) {
                section = new char[4096];
                this.sections[index] = section;
                if (index > this.topSection) {
                    this.topSection = index;
                }
            }
            return section;
        }

        int cell(int x, int y, int z) {
            char[] section = this.sections[y >> 4];
            return section == null ? 0 : section[(x * 16 + z) * 16 + (y & 15)];
        }

        /** Highest y that can hold a block: top of the highest section in use (Chunk.getTopFilledSegment() + 15). */
        int topY() {
            return this.topSection < 0 ? 15 : this.topSection * 16 + 15;
        }
    }

    private final long seed;
    private final TerrainSource terrain;
    private final Map<Long, Column> columns = new HashMap<>();
    private Column last;
    private OldTreeGens.BigTree bigTree;

    public OldWorld(long seed, TerrainSource terrain) {
        this.seed = seed;
        this.terrain = terrain;
    }

    public long seed() {
        return this.seed;
    }

    Column column(int chunkX, int chunkZ) {
        Column cached = this.last;
        if (cached != null && cached.chunkX == chunkX && cached.chunkZ == chunkZ) {
            return cached;
        }
        long key = (long) chunkX << 32 | chunkZ & 0xFFFFFFFFL;
        Column column = this.columns.get(key);
        if (column == null) {
            column = new Column(this.terrain.chunk(chunkX, chunkZ));
            this.columns.put(key, column);
        }
        this.last = column;
        return column;
    }

    /**
     * The big oak generator of the chunk being populated. 1.5.2 kept one instance per biome for the whole
     * session, so the height limit of one tree leaked into the next; here that state lasts for one chunk,
     * which keeps the result independent of the order chunks are visited in.
     */
    public OldTreeGens.BigTree bigTree() {
        return this.bigTree;
    }

    void beginChunk() {
        this.bigTree = new OldTreeGens.BigTree();
    }

    // ---- blocks ----

    public int getBlockId(int x, int y, int z) {
        if (y < 0 || y >= HEIGHT) {
            return 0;
        }
        return this.column(x >> 4, z >> 4).cell(x & 15, y, z & 15) >> 4;
    }

    public int getBlockMetadata(int x, int y, int z) {
        if (y < 0 || y >= HEIGHT) {
            return 0;
        }
        return this.column(x >> 4, z >> 4).cell(x & 15, y, z & 15) & 15;
    }

    public boolean isAirBlock(int x, int y, int z) {
        return this.getBlockId(x, y, z) == 0;
    }

    public Material material(int x, int y, int z) {
        return OldBlockInfo.material(this.getBlockId(x, y, z));
    }

    public boolean setBlock(int x, int y, int z, int id) {
        return this.setBlock(x, y, z, id, 0);
    }

    /** World.setBlock with flag 2: no neighbour updates, but the block's own onBlockAdded runs. */
    public boolean setBlock(int x, int y, int z, int id, int meta) {
        if (y < 0 || y >= HEIGHT) {
            return false;
        }
        Column column = this.column(x >> 4, z >> 4);
        int lx = x & 15;
        int lz = z & 15;
        int cell = id << 4 | meta & 15;
        int index = (lx * 16 + lz) * 16 + (y & 15);
        char[] section = column.sections[y >> 4];
        int old = section == null ? 0 : section[index];
        if (old == cell) {
            return false;
        }
        if (section == null) {
            if (id == 0) {
                return false;
            }
            section = column.section(y >> 4);
        }
        section[index] = (char) cell;
        if (old >> 4 != id) {
            column.tiles.remove((lx * 16 + lz) * HEIGHT + y);
        }
        if (id == 10 || id == 11) {
            this.hardenLava(x, y, z);
        } else if (id == 54) {
            this.chestAdded(x, y, z);
        }
        return true;
    }

    public boolean setBlockToAir(int x, int y, int z) {
        return this.setBlock(x, y, z, 0, 0);
    }

    private void setMetadata(int x, int y, int z, int meta) {
        Column column = this.column(x >> 4, z >> 4);
        char[] section = column.sections[y >> 4];
        if (section != null) {
            int index = ((x & 15) * 16 + (z & 15)) * 16 + (y & 15);
            section[index] = (char) (section[index] & 0xFFF0 | meta & 15);
        }
    }

    /** BlockFluid.checkForHarden: lava set next to water turns to obsidian. */
    private void hardenLava(int x, int y, int z) {
        if (this.material(x, y, z - 1) == Material.WATER || this.material(x, y, z + 1) == Material.WATER
                || this.material(x - 1, y, z) == Material.WATER || this.material(x + 1, y, z) == Material.WATER
                || this.material(x, y + 1, z) == Material.WATER) {
            int meta = this.getBlockMetadata(x, y, z);
            if (meta == 0) {
                this.setBlock(x, y, z, 49, 0);
            } else if (meta <= 4) {
                this.setBlock(x, y, z, 4, 0);
            }
        }
    }

    /** BlockChest.onBlockAdded: a chest turns its front away from walls and lines up with a neighbour chest. */
    private void chestAdded(int x, int y, int z) {
        this.unifyChest(x, y, z);
        if (this.getBlockId(x, y, z - 1) == 54) {
            this.unifyChest(x, y, z - 1);
        }
        if (this.getBlockId(x, y, z + 1) == 54) {
            this.unifyChest(x, y, z + 1);
        }
        if (this.getBlockId(x - 1, y, z) == 54) {
            this.unifyChest(x - 1, y, z);
        }
        if (this.getBlockId(x + 1, y, z) == 54) {
            this.unifyChest(x + 1, y, z);
        }
    }

    private void unifyChest(int x, int y, int z) {
        int north = this.getBlockId(x, y, z - 1);
        int south = this.getBlockId(x, y, z + 1);
        int west = this.getBlockId(x - 1, y, z);
        int east = this.getBlockId(x + 1, y, z);
        int facing;
        if (north == 54 || south == 54) {
            int otherZ = north == 54 ? z - 1 : z + 1;
            int westOfOther = this.getBlockId(x - 1, y, otherZ);
            int eastOfOther = this.getBlockId(x + 1, y, otherZ);
            facing = 5;
            if (this.getBlockMetadata(x, y, otherZ) == 4) {
                facing = 4;
            }
            if ((opaque(west) || opaque(westOfOther)) && !opaque(east) && !opaque(eastOfOther)) {
                facing = 5;
            }
            if ((opaque(east) || opaque(eastOfOther)) && !opaque(west) && !opaque(westOfOther)) {
                facing = 4;
            }
        } else if (west != 54 && east != 54) {
            facing = 3;
            if (opaque(north) && !opaque(south)) {
                facing = 3;
            }
            if (opaque(south) && !opaque(north)) {
                facing = 2;
            }
            if (opaque(west) && !opaque(east)) {
                facing = 5;
            }
            if (opaque(east) && !opaque(west)) {
                facing = 4;
            }
        } else {
            int otherX = west == 54 ? x - 1 : x + 1;
            int northOfOther = this.getBlockId(otherX, y, z - 1);
            int southOfOther = this.getBlockId(otherX, y, z + 1);
            facing = 3;
            if (this.getBlockMetadata(otherX, y, z) == 2) {
                facing = 2;
            }
            if ((opaque(north) || opaque(northOfOther)) && !opaque(south) && !opaque(southOfOther)) {
                facing = 3;
            }
            if ((opaque(south) || opaque(southOfOther)) && !opaque(north) && !opaque(northOfOther)) {
                facing = 2;
            }
        }
        this.setMetadata(x, y, z, facing);
    }

    private static boolean opaque(int id) {
        return OldBlockInfo.opaqueCube(id);
    }

    // ---- tile entities ----

    public void setTile(int x, int y, int z, OldTile tile) {
        this.column(x >> 4, z >> 4).tiles.put(((x & 15) * 16 + (z & 15)) * HEIGHT + y, tile);
    }

    public OldTile getTile(int x, int y, int z) {
        return this.column(x >> 4, z >> 4).tiles.get(((x & 15) * 16 + (z & 15)) * HEIGHT + y);
    }

    // ---- biomes, heights, light ----

    public OldBiome getBiome(int x, int z) {
        return OldBiomes.byId(this.column(x >> 4, z >> 4).biomes[(x & 15) + (z & 15) * 16] & 0xFF);
    }

    /** World.getHeightValue: one above the highest block that dims light. */
    public int getHeightValue(int x, int z) {
        Column column = this.column(x >> 4, z >> 4);
        int lx = x & 15;
        int lz = z & 15;
        int y = column.topY() + 1;
        while (y > 0 && OldBlockInfo.lightOpacity(column.cell(lx, y - 1, lz) >> 4) == 0) {
            y--;
        }
        return y;
    }

    /** World.getTopSolidOrLiquidBlock: one above the highest block that blocks movement and is not leaves, or -1. */
    public int getTopSolidOrLiquidBlock(int x, int z) {
        Column column = this.column(x >> 4, z >> 4);
        int lx = x & 15;
        int lz = z & 15;
        for (int y = column.topY(); y > 0; y--) {
            int id = column.cell(lx, y, lz) >> 4;
            if (id != 0) {
                Material material = OldBlockInfo.material(id);
                if (material.blocksMovement && material != Material.LEAVES) {
                    return y + 1;
                }
            }
        }
        return -1;
    }

    /** World.getPrecipitationHeight: one above the highest block that blocks movement or is liquid, or -1. */
    public int getPrecipitationHeight(int x, int z) {
        Column column = this.column(x >> 4, z >> 4);
        int lx = x & 15;
        int lz = z & 15;
        for (int y = column.topY(); y > 0; y--) {
            Material material = OldBlockInfo.material(column.cell(lx, y, lz) >> 4);
            if (material.blocksMovement || material.liquid) {
                return y + 1;
            }
        }
        return -1;
    }

    public boolean canBlockSeeTheSky(int x, int y, int z) {
        return y >= this.getHeightValue(x, z);
    }

    /** Sky light of a block from its own column (Chunk.generateSkylightMap). */
    public int skyLight(int x, int y, int z) {
        if (y < 0) {
            return 0;
        }
        Column column = this.column(x >> 4, z >> 4);
        int top = column.topY();
        if (y > top) {
            return 15;
        }
        int lx = x & 15;
        int lz = z & 15;
        int light = 15;
        for (int yy = top; yy >= y; yy--) {
            int opacity = OldBlockInfo.lightOpacity(column.cell(lx, yy, lz) >> 4);
            if (opacity == 0 && light != 15) {
                opacity = 1;
            }
            light -= opacity;
            if (light <= 0) {
                return 0;
            }
        }
        return light;
    }

    /** World.isBlockFreezable: still water in a freezing biome. */
    public boolean isBlockFreezable(int x, int y, int z) {
        if (this.getBiome(x, z).temperature > 0.15F || y < 0 || y >= HEIGHT) {
            return false;
        }
        int id = this.getBlockId(x, y, z);
        return (id == 8 || id == 9) && this.getBlockMetadata(x, y, z) == 0;
    }

    /** World.canSnowAt. */
    public boolean canSnowAt(int x, int y, int z) {
        if (this.getBiome(x, z).temperature > 0.15F || y < 0 || y >= HEIGHT) {
            return false;
        }
        int below = this.getBlockId(x, y - 1, z);
        return this.getBlockId(x, y, z) == 0 && this.snowCanStay(x, y, z) && below != 0 && below != 79
                && OldBlockInfo.material(below).blocksMovement;
    }

    /** BlockSnow.canPlaceBlockAt. */
    public boolean snowCanStay(int x, int y, int z) {
        int below = this.getBlockId(x, y - 1, z);
        if (below == 0) {
            return false;
        }
        if (below == 78 && (this.getBlockMetadata(x, y - 1, z) & 7) == 7) {
            return true;
        }
        return (OldBlockInfo.leaves(below) || OldBlockInfo.opaqueCube(below)) && OldBlockInfo.material(below).blocksMovement;
    }
}
