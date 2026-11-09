package hexxit.worldgen152.core;

import hexxit.worldgen152.core.layer.Layers;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Where which biome lies for one world seed: the 1.5.2 WorldChunkManager. Safe to use from many threads;
 * every thread works on its own layer chain and the per-chunk results are shared through a small cache.
 */
public final class BiomeLayout {
    private static final int CACHE_SIZE = 4096;

    private final long seed;
    private final OldBiome[] landBiomes;
    private final int biomeSize;
    private final ThreadLocal<Layers.Stack> stack;
    private final Map<Long, byte[]> chunkCache = new LinkedHashMap<>(256, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, byte[]> eldest) {
            return this.size() > CACHE_SIZE;
        }
    };

    public BiomeLayout(long seed) {
        this(seed, OldBiomes.overworld(), 4);
    }

    public BiomeLayout(long seed, OldBiome[] landBiomes, int biomeSize) {
        this.seed = seed;
        this.landBiomes = landBiomes.clone();
        this.biomeSize = biomeSize;
        this.stack = ThreadLocal.withInitial(() -> Layers.build(this.seed, this.landBiomes, this.biomeSize));
    }

    public long seed() {
        return this.seed;
    }

    /** Biome ids at 1:4 scale (the grid the terrain shape is blended on); coordinates are block / 4. */
    public int[] quarterIds(int x, int z, int width, int height) {
        return this.stack.get().quarter().sample(x, z, width, height);
    }

    /** Biome ids per block column, row by row (index x + z * width). */
    public int[] blockIds(int x, int z, int width, int height) {
        return this.stack.get().block().sample(x, z, width, height);
    }

    /** The 16x16 biome ids of a chunk (index x + z * 16). The returned array is shared: do not modify. */
    public byte[] chunkIds(int chunkX, int chunkZ) {
        long key = (long) chunkX << 32 | chunkZ & 0xFFFFFFFFL;
        synchronized (this.chunkCache) {
            byte[] cached = this.chunkCache.get(key);
            if (cached != null) {
                return cached;
            }
        }
        int[] ids = this.blockIds(chunkX << 4, chunkZ << 4, 16, 16);
        byte[] out = new byte[256];
        for (int i = 0; i < 256; i++) {
            out[i] = (byte) ids[i];
        }
        synchronized (this.chunkCache) {
            this.chunkCache.put(key, out);
        }
        return out;
    }

    /** Biome of one block column. */
    public OldBiome biomeAt(int x, int z) {
        return OldBiomes.byId(this.chunkIds(x >> 4, z >> 4)[(x & 15) + (z & 15) * 16] & 0xFF);
    }
}
