package hexxit.worldgen152.core.structure;

import hexxit.worldgen152.core.BiomeLayout;
import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.OldChunk;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The structures of one 1.5.2 world: where mineshafts, villages, strongholds and the scattered features
 * (desert and jungle temples, witch huts) start and how they are laid out - {@code MapGenStructure} and its
 * four subclasses - plus the biome questions of {@code WorldChunkManager} they ask.
 *
 * <p>An instance belongs to one {@link OldWorld}. Pieces remember what they have built (a chest that is
 * placed, the ground level of a house), so every world needs its own.
 *
 * <p>In 1.5.2 a start only existed once a chunk within 8 chunks of it had been generated, and starts that
 * overlap in a chunk were built in the order of a hash table that grew with the session. Here every start
 * exists from the beginning, and overlapping starts of a kind are built ordered by chunk x, then z.
 */
public final class OldStructures {
    /** MapGenVillage.villageSpawnBiomes (BiomeManager.addVillageBiome). */
    public static final List<OldBiome> VILLAGE_BIOMES = new CopyOnWriteArrayList<>(List.of(OldBiomes.PLAINS, OldBiomes.DESERT));
    /** MapGenStronghold.allowedBiomes (BiomeManager.addStrongholdBiome). */
    public static final List<OldBiome> STRONGHOLD_BIOMES = new CopyOnWriteArrayList<>(List.of(
            OldBiomes.DESERT, OldBiomes.FOREST, OldBiomes.EXTREME_HILLS, OldBiomes.SWAMPLAND, OldBiomes.TAIGA, OldBiomes.ICE_PLAINS,
            OldBiomes.ICE_MOUNTAINS, OldBiomes.DESERT_HILLS, OldBiomes.FOREST_HILLS, OldBiomes.EXTREME_HILLS_EDGE, OldBiomes.JUNGLE,
            OldBiomes.JUNGLE_HILLS));
    /** WorldChunkManager.allowedBiomes: where the world spawn may lie (BiomeManager.addSpawnBiome). */
    public static final List<OldBiome> SPAWN_BIOMES = new CopyOnWriteArrayList<>(List.of(
            OldBiomes.FOREST, OldBiomes.PLAINS, OldBiomes.TAIGA, OldBiomes.TAIGA_HILLS, OldBiomes.FOREST_HILLS, OldBiomes.JUNGLE,
            OldBiomes.JUNGLE_HILLS));

    /** Forge's BiomeEvent.GetVillageBlockID / GetVillageBlockMeta: a mod's own village blocks per biome. */
    public interface VillageBlocks {
        /** The block id to use instead, or -1 to leave the choice to the village. */
        int blockId(OldBiome biome, int blockId, int metadata);

        /** The metadata to use instead, or -1. */
        int metadata(OldBiome biome, int blockId, int metadata);
    }

    public static volatile VillageBlocks villageBlocks;

    /** How many chunks away from its start chunk a structure can reach. */
    private static final int REACH = 9;
    private static final int MINESHAFT = 0;
    private static final int VILLAGE = 1;
    private static final int STRONGHOLD = 2;
    private static final int SCATTERED = 3;
    private static final StructureStart NONE = new StructureStart() {
        @Override
        public boolean isSizeableStructure() {
            return false;
        }
    };
    /** StructureStrongholdPieces keeps the piece weights of the stronghold under construction in static fields. */
    private static final Object STRONGHOLD_LOCK = new Object();

    private final long seed;
    private final BiomeLayout layout;
    private final long xMul;
    private final long zMul;
    @SuppressWarnings("unchecked")
    private final Map<Long, StructureStart>[] starts = new Map[] {new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>()};
    private int[][] strongholds;

    public OldStructures(long seed, BiomeLayout layout) {
        this.seed = seed;
        this.layout = layout;
        Random random = new Random(seed);
        this.xMul = random.nextLong();
        this.zMul = random.nextLong();
    }

    public long seed() {
        return this.seed;
    }

    // ---- WorldChunkManager ----

    public OldBiome getBiomeGenAt(int x, int z) {
        return this.layout.biomeAt(x, z);
    }

    /** WorldChunkManager.areBiomesViable: every biome of the 1:4 grid within the radius is in the list. */
    public boolean areBiomesViable(int x, int z, int radius, List<OldBiome> allowed) {
        return areBiomesViable(this.layout, x, z, radius, allowed);
    }

    public static boolean areBiomesViable(BiomeLayout layout, int x, int z, int radius, List<OldBiome> allowed) {
        int minX = x - radius >> 2;
        int minZ = z - radius >> 2;
        int width = (x + radius >> 2) - minX + 1;
        int height = (z + radius >> 2) - minZ + 1;
        int[] ids = layout.quarterIds(minX, minZ, width, height);
        for (int i = 0; i < width * height; i++) {
            if (!allowed.contains(OldBiomes.byId(ids[i]))) {
                return false;
            }
        }
        return true;
    }

    /** WorldChunkManager.findBiomePosition: a random cell of the 1:4 grid with an allowed biome, as {x, z}, or null. */
    public static int[] findBiomePosition(BiomeLayout layout, int x, int z, int radius, List<OldBiome> allowed, Random random) {
        int minX = x - radius >> 2;
        int minZ = z - radius >> 2;
        int width = (x + radius >> 2) - minX + 1;
        int height = (z + radius >> 2) - minZ + 1;
        int[] ids = layout.quarterIds(minX, minZ, width, height);
        int[] found = null;
        int count = 0;
        for (int i = 0; i < width * height; i++) {
            int cellX = minX + i % width << 2;
            int cellZ = minZ + i / width << 2;
            if (allowed.contains(OldBiomes.byId(ids[i])) && (found == null || random.nextInt(count + 1) == 0)) {
                found = new int[] {cellX, cellZ};
                count++;
            }
        }
        return found;
    }

    /** World.setRandomSeed. */
    private Random regionRandom(int x, int z, int salt) {
        return new Random(x * 341873128712L + z * 132897987541L + this.seed + salt);
    }

    // ---- MapGenStructure ----

    /**
     * Builds the parts of all structures that lie in the population area of a chunk
     * (ChunkProviderGenerate.populate). Returns whether a village was among them.
     */
    public boolean generateStructuresInChunk(OldWorld world, Random random, int chunkX, int chunkZ) {
        this.generate(MINESHAFT, world, random, chunkX, chunkZ);
        boolean village = this.generate(VILLAGE, world, random, chunkX, chunkZ);
        this.generate(STRONGHOLD, world, random, chunkX, chunkZ);
        this.generate(SCATTERED, world, random, chunkX, chunkZ);
        return village;
    }

    private boolean generate(int kind, OldWorld world, Random random, int chunkX, int chunkZ) {
        int minX = (chunkX << 4) + 8;
        int minZ = (chunkZ << 4) + 8;
        boolean generated = false;
        for (int x = chunkX - REACH; x <= chunkX + REACH; x++) {
            for (int z = chunkZ - REACH; z <= chunkZ + REACH; z++) {
                StructureStart start = this.start(kind, x, z);
                if (start.isSizeableStructure() && start.getBoundingBox().intersectsWith(minX, minZ, minX + 15, minZ + 15)) {
                    start.generateStructure(world, random, new StructureBoundingBox(minX, minZ, minX + 15, minZ + 15));
                    generated = true;
                }
            }
        }
        return generated;
    }

    /** A structure found by {@link #find}: its kind, the chunk it starts in and the middle of its first piece. */
    public record Found(String kind, int chunkX, int chunkZ, int x, int y, int z, int pieces) {
    }

    /** All structures starting within a radius (in chunks) of chunk 0,0; for tools. */
    public synchronized List<Found> find(int radius) {
        String[] names = {"mineshaft", "village", "stronghold", "scattered"};
        List<Found> found = new ArrayList<>();
        for (int kind = 0; kind < 4; kind++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    StructureStart start = this.start(kind, x, z);
                    if (start.isSizeableStructure()) {
                        StructureComponent first = start.getComponents().get(0);
                        String name = kind == SCATTERED ? first.getClass().getSimpleName().replace("ComponentScatteredFeature", "") : names[kind];
                        StructureBoundingBox box = first.getBoundingBox();
                        found.add(new Found(name, x, z, box.getCenterX(), box.getCenterY(), box.getCenterZ(), start.getComponents().size()));
                    }
                }
            }
        }
        return found;
    }

    /** The structure starting in a chunk; a start that is not sizeable stands for "none". */
    private StructureStart start(int kind, int chunkX, int chunkZ) {
        long key = (long) chunkX << 32 | chunkZ & 0xFFFFFFFFL;
        StructureStart start = this.starts[kind].get(key);
        if (start == null) {
            Random random = new Random(chunkX * this.xMul ^ chunkZ * this.zMul ^ this.seed);
            random.nextInt();
            start = switch (kind) {
                case MINESHAFT -> this.mineshaft(random, chunkX, chunkZ);
                case VILLAGE -> this.village(random, chunkX, chunkZ);
                case STRONGHOLD -> this.stronghold(random, chunkX, chunkZ);
                default -> this.scattered(random, chunkX, chunkZ);
            };
            this.starts[kind].put(key, start);
        }
        return start;
    }

    private StructureStart mineshaft(Random random, int chunkX, int chunkZ) {
        if (random.nextDouble() < 0.01 && random.nextInt(80) < Math.max(Math.abs(chunkX), Math.abs(chunkZ))) {
            return new StructureMineshaftStart(this, random, chunkX, chunkZ);
        }
        return NONE;
    }

    /** The chunk of a 32x32 chunk region a village or scattered feature may start in. */
    private boolean isRegionStart(int chunkX, int chunkZ, int salt) {
        int regionX = Math.floorDiv(chunkX, 32);
        int regionZ = Math.floorDiv(chunkZ, 32);
        Random random = this.regionRandom(regionX, regionZ, salt);
        int x = regionX * 32 + random.nextInt(32 - 8);
        int z = regionZ * 32 + random.nextInt(32 - 8);
        return chunkX == x && chunkZ == z;
    }

    private StructureStart village(Random random, int chunkX, int chunkZ) {
        if (this.isRegionStart(chunkX, chunkZ, 10387312) && this.areBiomesViable(chunkX * 16 + 8, chunkZ * 16 + 8, 0, VILLAGE_BIOMES)) {
            return new StructureVillageStart(this, random, chunkX, chunkZ, 0);
        }
        return NONE;
    }

    private StructureStart scattered(Random random, int chunkX, int chunkZ) {
        if (this.isRegionStart(chunkX, chunkZ, 14357617)) {
            OldBiome biome = this.getBiomeGenAt(chunkX * 16 + 8, chunkZ * 16 + 8);
            if (biome == OldBiomes.DESERT || biome == OldBiomes.DESERT_HILLS || biome == OldBiomes.JUNGLE || biome == OldBiomes.JUNGLE_HILLS
                    || biome == OldBiomes.SWAMPLAND) {
                return new StructureScatteredFeatureStart(this, random, chunkX, chunkZ);
            }
        }
        return NONE;
    }

    private StructureStart stronghold(Random random, int chunkX, int chunkZ) {
        for (int[] position : this.strongholds()) {
            if (chunkX == position[0] && chunkZ == position[1]) {
                synchronized (STRONGHOLD_LOCK) {
                    StructureStrongholdStart start = new StructureStrongholdStart(this, random, chunkX, chunkZ);
                    while (start.getComponents().isEmpty() || ((ComponentStrongholdStairs2) start.getComponents().get(0)).strongholdPortalRoom == null) {
                        start = new StructureStrongholdStart(this, random, chunkX, chunkZ);
                    }
                    return start;
                }
            }
        }
        return NONE;
    }

    /** The chunks of the three strongholds, {x, z} each (MapGenStronghold.canSpawnStructureAtCoords). */
    public synchronized int[][] strongholds() {
        if (this.strongholds == null) {
            this.strongholds = strongholds(this.seed, this.layout);
        }
        return this.strongholds;
    }

    /** Centre of the portal room of every stronghold, {x, y, z} each: where eyes of ender lead. */
    public synchronized int[][] strongholdPortalRooms() {
        int[][] chunks = this.strongholds();
        int[][] rooms = new int[chunks.length][];
        for (int i = 0; i < chunks.length; i++) {
            StructureStart start = this.start(STRONGHOLD, chunks[i][0], chunks[i][1]);
            StructureBoundingBox box = ((ComponentStrongholdStairs2) start.getComponents().get(0)).strongholdPortalRoom.getBoundingBox();
            rooms[i] = new int[] {box.getCenterX(), box.getCenterY(), box.getCenterZ()};
        }
        return rooms;
    }

    /**
     * WorldServer.createSpawnPosition: a random place in a spawn biome within 256 blocks of 0,0, then a random
     * walk until the highest block of a column is grass (at most 1000 steps). Returns {x, z}.
     *
     * <p>The original looked at chunks as they were at that moment, a few of them already populated; this
     * looks at the bare terrain, so a tree or tall grass never sends the search one step further.
     */
    public static int[] spawnPoint(long seed, BiomeLayout layout, OldWorld.TerrainSource terrain) {
        Random random = new Random(seed);
        int[] found = findBiomePosition(layout, 0, 0, 256, SPAWN_BIOMES, random);
        int x = found == null ? 0 : found[0];
        int z = found == null ? 0 : found[1];
        for (int tries = 0; !isGrassOnTop(terrain, x, z); ) {
            x += random.nextInt(64) - random.nextInt(64);
            z += random.nextInt(64) - random.nextInt(64);
            if (++tries == 1000) {
                break;
            }
        }
        return new int[] {x, z};
    }

    /** WorldProvider.canCoordinateBeSpawn with World.getFirstUncoveredBlock. */
    private static boolean isGrassOnTop(OldWorld.TerrainSource terrain, int x, int z) {
        OldChunk chunk = terrain.chunk(x >> 4, z >> 4);
        int y = 63;
        while (y + 1 < OldChunk.HEIGHT && chunk.blockId(x & 15, y + 1, z & 15) != 0) {
            y++;
        }
        return chunk.blockId(x & 15, y, z & 15) == 2;
    }

    public static int[][] strongholds(long seed, BiomeLayout layout) {
        int[][] positions = new int[3][];
        Random random = new Random();
        random.setSeed(seed);
        double angle = random.nextDouble() * Math.PI * 2.0;
        int ring = 1;
        int spread = 3;
        for (int i = 0; i < positions.length; i++) {
            double distance = (1.25 * ring + random.nextDouble()) * 32.0 * ring;
            int x = (int) Math.round(Math.cos(angle) * distance);
            int z = (int) Math.round(Math.sin(angle) * distance);
            int[] found = findBiomePosition(layout, (x << 4) + 8, (z << 4) + 8, 112, new ArrayList<>(STRONGHOLD_BIOMES), random);
            if (found != null) {
                x = found[0] >> 4;
                z = found[1] >> 4;
            }
            positions[i] = new int[] {x, z};
            angle += Math.PI * 2 * ring / spread;
            if (i == spread) {
                ring += 2 + random.nextInt(5);
                spread = spread + 1 + random.nextInt(2);
            }
        }
        return positions;
    }
}
