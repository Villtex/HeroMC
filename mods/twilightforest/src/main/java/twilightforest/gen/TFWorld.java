package twilightforest.gen;

import hexxit.worldgen152.core.OldChunk;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldDecoratedChunk;
import hexxit.worldgen152.core.populate.OldDecoration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Twilight Forest (dimension 7) of one seed: layout, bare terrain and populated chunks. The seed is the world
 * seed (the pack's TwilightForestSeed is empty). Safe to use from many threads.
 */
public final class TFWorld {
    /** Block ids of the pack's TwilightForest.cfg that the terrain or the portal set. */
    public static final int PORTAL = 1366;

    private static final int CACHE_SIZE = 1024;

    private final long seed;
    private final TFLayout layout;
    private final TFTerrain terrain;
    private final OldDecoration decoration;
    /** The structures for questions of the game (mob spawns); the population keeps its own per world. */
    private final twilightforest.gen.structure.MapGenTFMajorFeature features;
    private final Map<Long, OldChunk> cache = new LinkedHashMap<>(64, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, OldChunk> eldest) {
            return this.size() > CACHE_SIZE;
        }
    };

    public TFWorld(long seed) {
        register();
        this.seed = seed;
        this.layout = new TFLayout(seed);
        this.terrain = new TFTerrain(this.layout);
        this.features = new twilightforest.gen.structure.MapGenTFMajorFeature(this.layout);
        this.decoration = OldDecoration.dimension(seed, this::terrain, TFPopulator.DIMENSION, new TFPopulator(this.layout));
    }

    private static boolean registered;

    /** Biomes and the block descriptions the population needs; called by the mod and by tools/expect.sh. */
    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        TFBiomes.register();
        // BlockTFHedge: Material.cactus, a full block (metadata 1: darkwood leaves)
        OldBlockInfo.registerCube(TFTerrain.HEDGE, OldBlockInfo.Material.CACTUS);
        // BlockTFPortal: BlockBreakable of Material.portal, not opaque
        OldBlockInfo.register(PORTAL, OldBlockInfo.Material.PORTAL, false, 0, false);
        // Teil 24b: the blocks of the decoration and of the saplings
        for (int critter : new int[] {TFGen.CICADA, TFGen.FIREFLY, TFGen.MOONWORM}) {
            // BlockTFCritter: Material.circuits, not opaque, not a normal block; onBlockAdded picks a side or drops
            OldBlockInfo.register(critter, OldBlockInfo.Material.CIRCUITS, false, 0, false);
            OldBlockInfo.registerAdded(critter, TFGen::critterAdded);
        }
        OldBlockInfo.registerCube(TFGen.FIRE_JET, OldBlockInfo.Material.ROCK);
        // BlockTFFireflyJar: glass, neither opaque nor a normal block
        OldBlockInfo.register(TFGen.FIREFLY_JAR, OldBlockInfo.Material.GLASS, false, 0, false);
        // BlockTFLeaves asks Block.leaves.isOpaqueCube() (true while the blocks are made), light opacity 2
        OldBlockInfo.registerLeaves(TFGen.LEAVES, true, true, 2);
        OldBlockInfo.registerLog(TFGen.LOG);
        // BlockTFMagicLeaves: isOpaqueCube false, light opacity 2
        OldBlockInfo.registerLeaves(TFGen.MAGIC_LEAVES, false, true, 2);
        OldBlockInfo.registerLog(TFGen.MAGIC_LOG);
        OldBlockInfo.registerLog(TFGen.MAGIC_LOG_SPECIAL);
        OldBlockInfo.registerCube(TFGen.MAZESTONE, OldBlockInfo.Material.ROCK);
        OldBlockInfo.registerCube(TFGen.NAGASTONE, OldBlockInfo.Material.ROCK);
        // BlockTFPlant (a BlockFlower): plants, no light opacity
        OldBlockInfo.register(TFGen.PLANT, OldBlockInfo.Material.PLANTS, false, 0, false);
        OldBlockInfo.registerCube(TFGen.ROOT, OldBlockInfo.Material.WOOD);
        OldBlockInfo.register(TFGen.SAPLING, OldBlockInfo.Material.PLANTS, false, 0, false);
        OldBlockInfo.registerCube(TFGen.TOWER_WOOD, OldBlockInfo.Material.WOOD);
        // Teil 24e: BlockTFTowerDevice (Material.wood, a full block), BlockTFTowerTranslucent (Material.glass, not opaque)
        OldBlockInfo.registerCube(1370, OldBlockInfo.Material.WOOD);
        OldBlockInfo.register(1371, OldBlockInfo.Material.GLASS, false, 0, true);
        // Teil 24d: BlockTFBossSpawner (Material.iron, isOpaqueCube false, so light opacity 0)
        OldBlockInfo.register(TFGen.BOSS_SPAWNER, OldBlockInfo.Material.IRON, false, 0, true);
    }

    public long seed() {
        return this.seed;
    }

    public TFLayout layout() {
        return this.layout;
    }

    /** The bare terrain of a chunk (cached). */
    public OldChunk terrain(int chunkX, int chunkZ) {
        long key = (long) chunkX << 32 | chunkZ & 0xFFFFFFFFL;
        synchronized (this.cache) {
            OldChunk cached = this.cache.get(key);
            if (cached != null) {
                return cached;
            }
        }
        OldChunk generated = this.terrain.generate(chunkX, chunkZ);
        synchronized (this.cache) {
            this.cache.put(key, generated);
        }
        return generated;
    }

    /** MapGenTFMajorFeature.getSpawnListIndexAt. */
    public int spawnListIndexAt(int x, int y, int z) {
        return this.features.getSpawnListIndexAt(x, y, z);
    }

    public OldDecoration decoration() {
        return this.decoration;
    }

    /** The populated chunk. */
    public OldDecoratedChunk populated(int chunkX, int chunkZ) {
        return this.decoration.chunk(chunkX, chunkZ);
    }
}
