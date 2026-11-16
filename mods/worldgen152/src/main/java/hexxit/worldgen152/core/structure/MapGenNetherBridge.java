package hexxit.worldgen152.core.structure;

import hexxit.worldgen152.core.populate.OldWorld;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The nether fortresses of one 1.5.2 world: where they start and how they are laid out
 * ({@code MapGenNetherBridge} with {@code MapGenStructure}).
 *
 * <p>An instance belongs to one {@link OldWorld}. Pieces remember what they have built (the blaze spawner of
 * a throne), so every world needs its own.
 *
 * <p>Each 16x16 chunk region holds at most one fortress, in a chunk chosen by a random seeded from the
 * region alone (canSpawnStructureAtCoords); the start is laid out with that same random, right after those
 * draws. In 1.5.2 a start only existed once a chunk within 8 chunks of it had been generated, and starts that
 * overlap in a chunk were built in the order of a hash table. Here every start exists from the beginning,
 * and overlapping starts are built ordered by chunk x, then z.
 */
public final class MapGenNetherBridge {
    /** An entry of MapGenNetherBridge.spawnList: what spawns inside a fortress (EntityList names). */
    public record SpawnEntry(String mob, int weight, int min, int max) {
    }

    /** MapGenNetherBridge.spawnList, in its order. */
    public static final List<SpawnEntry> SPAWN_LIST = List.of(
            new SpawnEntry("Blaze", 10, 2, 3),
            new SpawnEntry("PigZombie", 5, 4, 4),
            new SpawnEntry("Skeleton", 10, 4, 4),
            new SpawnEntry("LavaSlime", 3, 4, 4));
    /** The mob names of {@link #SPAWN_LIST}. */
    public static final String[] SPAWN_LIST_MOBS = {"Blaze", "PigZombie", "Skeleton", "LavaSlime"};

    /**
     * How many chunks away from its start chunk a fortress can reach: pieces attach within 112 blocks of the
     * start piece and are at most 19 blocks long.
     */
    private static final int REACH = 10;
    /** StructureNetherBridgePieces shares its piece weights between all fortresses. */
    private static final Object NETHER_BRIDGE_LOCK = new Object();

    /** The fortress of a region, or none (start null). */
    private record Region(int chunkX, int chunkZ, StructureStart start) {
    }

    private static final Region NONE = new Region(0, 0, null);
    private static final Comparator<Region> ORDER = Comparator.comparingInt(Region::chunkX).thenComparingInt(Region::chunkZ);

    private final long seed;
    private final Map<Long, Region> regions = new HashMap<>();

    public MapGenNetherBridge(long worldSeed) {
        this.seed = worldSeed;
    }

    /**
     * Builds the parts of all fortresses that lie in the population area of a chunk
     * (ChunkProviderHell.populate). Returns whether there were any.
     */
    public boolean generateStructuresInChunk(OldWorld world, Random random, int chunkX, int chunkZ) {
        int minX = (chunkX << 4) + 8;
        int minZ = (chunkZ << 4) + 8;
        boolean generated = false;
        for (Region region : this.near(chunkX, chunkZ)) {
            StructureStart start = region.start();
            if (start.isSizeableStructure() && start.getBoundingBox().intersectsWith(minX, minZ, minX + 15, minZ + 15)) {
                start.generateStructure(world, random, new StructureBoundingBox(minX, minZ, minX + 15, minZ + 15));
                generated = true;
            }
        }
        return generated;
    }

    /** MapGenStructure.hasStructureAt: whether a block lies inside a piece of a fortress (where its mobs spawn). */
    public boolean hasStructureAt(int x, int y, int z) {
        for (Region region : this.near(x >> 4, z >> 4)) {
            StructureStart start = region.start();
            if (start.isSizeableStructure() && start.getBoundingBox().intersectsWith(x, z, x, z)) {
                for (StructureComponent component : start.getComponents()) {
                    if (component.getBoundingBox().isVecInside(x, y, z)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** A fortress found by {@link #find}: the chunk it starts in and the middle of its first piece. */
    public record Found(int chunkX, int chunkZ, int x, int y, int z, int pieces) {
    }

    /** All fortresses starting within a radius (in chunks) of chunk 0,0; for tools. */
    public synchronized List<Found> find(int radiusChunks) {
        List<Region> starts = this.within(-radiusChunks, -radiusChunks, radiusChunks, radiusChunks);
        List<Found> found = new ArrayList<>();
        for (Region region : starts) {
            StructureStart start = region.start();
            if (start.isSizeableStructure()) {
                StructureBoundingBox box = start.getComponents().get(0).getBoundingBox();
                found.add(new Found(region.chunkX(), region.chunkZ(), box.getCenterX(), box.getCenterY(), box.getCenterZ(),
                        start.getComponents().size()));
            }
        }
        return found;
    }

    /** The fortresses that may reach into a chunk, ordered by chunk x, then z. */
    private List<Region> near(int chunkX, int chunkZ) {
        List<Region> found = new ArrayList<>();
        for (int regionX = chunkX - REACH >> 4; regionX <= chunkX + REACH >> 4; regionX++) {
            for (int regionZ = chunkZ - REACH >> 4; regionZ <= chunkZ + REACH >> 4; regionZ++) {
                Region region = this.region(regionX, regionZ);
                if (region.start() != null) {
                    found.add(region);
                }
            }
        }
        found.sort(ORDER);
        return found;
    }

    /** The fortresses whose start chunk lies in a rectangle of chunks, ordered by chunk x, then z. */
    private List<Region> within(int minChunkX, int minChunkZ, int maxChunkX, int maxChunkZ) {
        List<Region> found = new ArrayList<>();
        for (int regionX = minChunkX >> 4; regionX <= maxChunkX >> 4; regionX++) {
            for (int regionZ = minChunkZ >> 4; regionZ <= maxChunkZ >> 4; regionZ++) {
                Region region = this.region(regionX, regionZ);
                if (region.start() != null && region.chunkX() >= minChunkX && region.chunkX() <= maxChunkX
                        && region.chunkZ() >= minChunkZ && region.chunkZ() <= maxChunkZ) {
                    found.add(region);
                }
            }
        }
        found.sort(ORDER);
        return found;
    }

    /**
     * The fortress of a 16x16 chunk region. MapGenNetherBridge.canSpawnStructureAtCoords asks every chunk the
     * same random, seeded from the region; only the chunk matching both draws holds a start, and
     * getStructureStart lays it out with that random as the draws left it (MapGenStructure.recursiveGenerate
     * draws once before, but the reseed makes that draw irrelevant).
     */
    private synchronized Region region(int regionX, int regionZ) {
        long key = (long) regionX << 32 | regionZ & 0xFFFFFFFFL;
        Region region = this.regions.get(key);
        if (region == null) {
            Random random = new Random();
            random.setSeed((long) (regionX ^ regionZ << 4) ^ this.seed);
            random.nextInt();
            if (random.nextInt(3) != 0) {
                region = NONE;
            } else {
                int chunkX = (regionX << 4) + 4 + random.nextInt(8);
                int chunkZ = (regionZ << 4) + 4 + random.nextInt(8);
                StructureStart start;
                synchronized (NETHER_BRIDGE_LOCK) {
                    start = new StructureNetherBridgeStart(random, chunkX, chunkZ);
                }
                region = new Region(chunkX, chunkZ, start);
            }
            this.regions.put(key, region);
        }
        return region;
    }
}
