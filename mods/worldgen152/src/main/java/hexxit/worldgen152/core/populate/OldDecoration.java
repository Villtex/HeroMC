package hexxit.worldgen152.core.populate;

import hexxit.worldgen152.core.BiomeLayout;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * The populated 1.5.2 world of one seed, chunk by chunk.
 *
 * <p>In 1.5.2 populating a chunk changed blocks in its neighbours and read what earlier chunks had left
 * there, so the result depended on the order in which players made chunks load. Here the order is fixed:
 * the world is cut into tiles of {@value #TILE}x{@value #TILE} chunks; for a tile, the chunks of the tile
 * and of a margin around it are populated on bare terrain, x ascending, then z ascending - the order in
 * which the original server generated its spawn area. The same seed therefore always gives the same blocks,
 * whatever is loaded first. The margin covers everything that writes into the tile and what those writers
 * read; an effect that would have to travel further (a feature blocked by a feature that was itself blocked
 * by one outside the margin) is cut off, which can at worst clip such a feature at a tile border.
 */
public final class OldDecoration {
    /** Tile edge in chunks. */
    public static final int TILE = 8;
    /** Extra chunks populated before the tile (population reaches up to 2 chunks towards +x/+z). */
    private static final int MARGIN_LOW = 4;
    /** Extra chunks populated after the tile. */
    private static final int MARGIN_HIGH = 2;
    private static final int CACHED_TILES = 12;

    private final long seed;
    private final OldWorld.TerrainSource terrain;
    private final BiomeLayout layout;
    private final Map<Long, FutureTask<OldDecoratedChunk[]>> tiles = new LinkedHashMap<>(16, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, FutureTask<OldDecoratedChunk[]>> eldest) {
            return this.size() > CACHED_TILES;
        }
    };

    /** Population without structures. */
    public OldDecoration(long seed, OldWorld.TerrainSource terrain) {
        this(seed, terrain, null);
    }

    /** Population with the structures of 1.5.2, which need to know where which biome lies. */
    public OldDecoration(long seed, OldWorld.TerrainSource terrain, BiomeLayout layout) {
        this.seed = seed;
        this.terrain = terrain;
        this.layout = layout;
    }

    public long seed() {
        return this.seed;
    }

    /** The populated chunk. Computes its whole tile on first use; other threads asking for the same tile wait. */
    public OldDecoratedChunk chunk(int chunkX, int chunkZ) {
        int tileX = Math.floorDiv(chunkX, TILE);
        int tileZ = Math.floorDiv(chunkZ, TILE);
        long key = (long) tileX << 32 | tileZ & 0xFFFFFFFFL;
        FutureTask<OldDecoratedChunk[]> task;
        boolean compute = false;
        synchronized (this.tiles) {
            task = this.tiles.get(key);
            if (task == null) {
                task = new FutureTask<>(() -> this.populateTile(tileX, tileZ));
                this.tiles.put(key, task);
                compute = true;
            }
        }
        if (compute) {
            task.run();
        }
        try {
            return task.get()[(chunkX - tileX * TILE) * TILE + (chunkZ - tileZ * TILE)];
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while populating 1.5.2 chunks", e);
        } catch (ExecutionException e) {
            synchronized (this.tiles) {
                this.tiles.remove(key);
            }
            throw new IllegalStateException("Populating the 1.5.2 chunks around " + chunkX + ", " + chunkZ + " failed", e.getCause());
        }
    }

    private OldDecoratedChunk[] populateTile(int tileX, int tileZ) {
        int x0 = tileX * TILE;
        int z0 = tileZ * TILE;
        OldWorld world = new OldWorld(this.seed, this.terrain, this.layout);
        for (int x = x0 - MARGIN_LOW; x < x0 + TILE + MARGIN_HIGH; x++) {
            for (int z = z0 - MARGIN_LOW; z < z0 + TILE + MARGIN_HIGH; z++) {
                OldPopulator.populate(world, x, z);
            }
        }
        byte[][][] distances = leafDistances(world, x0, z0);
        OldDecoratedChunk[] out = new OldDecoratedChunk[TILE * TILE];
        for (int x = 0; x < TILE; x++) {
            for (int z = 0; z < TILE; z++) {
                out[x * TILE + z] = new OldDecoratedChunk(world.column(x0 + x, z0 + z), distances[(x + 1) * (TILE + 2) + z + 1]);
            }
        }
        return out;
    }

    /**
     * Distance of every leaf block of the tile to the nearest log, walking through leaves only (what today's
     * leaves store to know whether they decay). Looks one chunk beyond the tile for logs.
     */
    private static byte[][][] leafDistances(OldWorld world, int x0, int z0) {
        int size = TILE + 2;
        OldWorld.Column[] columns = new OldWorld.Column[size * size];
        byte[][][] distances = new byte[size * size][16][];
        long[] frontier = new long[4096];
        int count = 0;
        int minX = (x0 - 1) * 16;
        int minZ = (z0 - 1) * 16;
        int span = size * 16;
        for (int cx = 0; cx < size; cx++) {
            for (int cz = 0; cz < size; cz++) {
                OldWorld.Column column = world.column(x0 - 1 + cx, z0 - 1 + cz);
                columns[cx * size + cz] = column;
                for (int s = 0; s < 16; s++) {
                    char[] section = column.sections[s];
                    if (section == null) {
                        continue;
                    }
                    for (int i = 0; i < 4096; i++) {
                        if (OldBlockInfo.wood(section[i] >> 4)) {
                            if (count == frontier.length) {
                                frontier = Arrays.copyOf(frontier, count * 2);
                            }
                            frontier[count++] = pack(cx * 16 + (i >> 8), s * 16 + (i & 15), cz * 16 + (i >> 4 & 15));
                        }
                    }
                }
            }
        }
        int[][] steps = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
        for (int distance = 1; distance <= 6 && count > 0; distance++) {
            long[] next = new long[Math.max(1024, count)];
            int nextCount = 0;
            for (int n = 0; n < count; n++) {
                long from = frontier[n];
                for (int[] step : steps) {
                    int x = (int) (from >> 40) + step[0];
                    int y = (int) (from >> 20 & 0xFFFFF) + step[1];
                    int z = (int) (from & 0xFFFFF) + step[2];
                    if (x < 0 || x >= span || z < 0 || z >= span || y < 0 || y >= OldWorld.HEIGHT) {
                        continue;
                    }
                    int columnIndex = (x >> 4) * size + (z >> 4);
                    char[] section = columns[columnIndex].sections[y >> 4];
                    int index = ((x & 15) * 16 + (z & 15)) * 16 + (y & 15);
                    if (section == null || !OldBlockInfo.leaves(section[index] >> 4)) {
                        continue;
                    }
                    byte[] marks = distances[columnIndex][y >> 4];
                    if (marks == null) {
                        marks = new byte[4096];
                        distances[columnIndex][y >> 4] = marks;
                    }
                    if (marks[index] == 0) {
                        marks[index] = (byte) distance;
                        if (nextCount == next.length) {
                            next = Arrays.copyOf(next, nextCount * 2);
                        }
                        next[nextCount++] = pack(x, y, z);
                    }
                }
            }
            frontier = next;
            count = nextCount;
        }
        return distances;
    }

    private static long pack(int x, int y, int z) {
        return (long) x << 40 | (long) y << 20 | z;
    }
}
