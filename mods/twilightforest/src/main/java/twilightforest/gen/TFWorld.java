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

    public OldDecoration decoration() {
        return this.decoration;
    }

    /** The populated chunk. */
    public OldDecoratedChunk populated(int chunkX, int chunkZ) {
        return this.decoration.chunk(chunkX, chunkZ);
    }
}
