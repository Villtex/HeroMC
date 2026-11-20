package twilightforest.gen;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.layer.Layer;
import hexxit.worldgen152.core.layer.Layers;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Where which biome lies in the Twilight Forest of one seed: TFWorldChunkManager with the layer chain of
 * GenLayerTF.makeTheWorld. Safe to use from many threads (one chain per thread, a shared cache of chunk biomes).
 */
public final class TFLayout {
    private static final int CACHE_SIZE = 4096;
    private static final OldBiome[] COMMON = {TFBiomes.TWILIGHT_FOREST, TFBiomes.TWILIGHT_FOREST_2, TFBiomes.HIGHLANDS, TFBiomes.MUSHROOMS,
            TFBiomes.SWAMP, TFBiomes.CLEARING, TFBiomes.DARK_FOREST};
    private static final OldBiome[] RARE = {TFBiomes.LAKE, TFBiomes.GLACIER, TFBiomes.DEEP_MUSHROOMS, TFBiomes.ENCHANTED_FOREST,
            TFBiomes.FIRE_SWAMP};

    private final long seed;
    private final ThreadLocal<Layers.Stack> stack;
    private final Map<Long, byte[]> chunkCache = new LinkedHashMap<>(256, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, byte[]> eldest) {
            return this.size() > CACHE_SIZE;
        }
    };

    public TFLayout(long seed) {
        this.seed = seed;
        this.stack = ThreadLocal.withInitial(() -> makeTheWorld(seed));
    }

    public long seed() {
        return this.seed;
    }

    /** GenLayerTF.makeTheWorld. */
    private static Layers.Stack makeTheWorld(long seed) {
        Layer biomes = new Biomes(1L);
        biomes = new Layers.Zoom(1000L, biomes);
        biomes = new Layers.Zoom(1001L, biomes);
        biomes = new BiomeBorders(500L, biomes);
        biomes = new Layers.Zoom(1002L, biomes);
        biomes = new Layers.Zoom(1003L, biomes);
        biomes = new Layers.Zoom(1004L, biomes);
        biomes = new Layers.Zoom(1005L, biomes);
        Layer rivers = new Stream(1L, biomes);
        rivers = new Layers.Smooth(7000L, rivers);
        Layer mixed = new Layers.RiverMix(100L, biomes, rivers);
        Layer voronoi = new Layers.VoronoiZoom(10L, mixed);
        mixed.initWorldSeed(seed);
        voronoi.initWorldSeed(seed);
        return new Layers.Stack(mixed, voronoi);
    }

    /** getBiomesForGeneration: ids at 1:4 scale. */
    public int[] quarterIds(int x, int z, int width, int height) {
        return this.stack.get().quarter().sample(x, z, width, height);
    }

    /** Ids per block column (index x + z * width). */
    public int[] blockIds(int x, int z, int width, int height) {
        return this.stack.get().block().sample(x, z, width, height);
    }

    /** The 16x16 biome ids of a chunk (index x + z * 16). Shared: do not modify. */
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
            // a negative id became Twilight Forest (getBiomeGenAt); the chain here never yields one
            out[i] = (byte) (ids[i] >= 0 ? ids[i] : TFBiomes.TWILIGHT_FOREST.id);
        }
        synchronized (this.chunkCache) {
            this.chunkCache.put(key, out);
        }
        return out;
    }

    /** world.getBiomeGenForCoords in the Twilight Forest. */
    public OldBiome biomeAt(int x, int z) {
        return OldBiomes.byId(this.chunkIds(x >> 4, z >> 4)[(x & 15) + (z & 15) * 16] & 0xFF);
    }

    /** GenLayerTFBiomes: a common biome, one in fifteen a rare one. */
    static final class Biomes extends Layer {
        Biomes(long salt) {
            super(salt);
        }

        @Override
        public int[] sample(int x, int z, int width, int depth) {
            int[] dest = new int[width * depth];
            for (int dz = 0; dz < depth; dz++) {
                for (int dx = 0; dx < width; dx++) {
                    this.initChunkSeed(dx + x, dz + z);
                    if (this.nextInt(15) == 0) {
                        dest[dx + dz * width] = RARE[this.nextInt(RARE.length)].id;
                    } else {
                        dest[dx + dz * width] = COMMON[this.nextInt(COMMON.length)].id;
                    }
                }
            }
            return dest;
        }
    }

    /** GenLayerTFBiomeBorders: lake, clearing, deep mushroom and glacier biomes get a ring of their border biome. */
    static final class BiomeBorders extends Layer {
        BiomeBorders(long salt, Layer parent) {
            super(salt);
            this.parent = parent;
        }

        @Override
        public int[] sample(int x, int z, int width, int depth) {
            int nwidth = width + 2;
            int[] input = this.parent.sample(x - 1, z - 1, nwidth, depth + 2);
            int[] output = new int[width * depth];
            for (int dz = 0; dz < depth; dz++) {
                for (int dx = 0; dx < width; dx++) {
                    int right = input[dx + 0 + (dz + 1) * nwidth];
                    int left = input[dx + 2 + (dz + 1) * nwidth];
                    int up = input[dx + 1 + (dz + 0) * nwidth];
                    int down = input[dx + 1 + (dz + 2) * nwidth];
                    int center = input[dx + 1 + (dz + 1) * nwidth];
                    if (onBorder(TFBiomes.LAKE.id, center, right, left, up, down)) {
                        output[dx + dz * width] = TFBiomes.LAKE_BORDER.id;
                    } else if (onBorder(TFBiomes.CLEARING.id, center, right, left, up, down)) {
                        output[dx + dz * width] = TFBiomes.CLEARING_BORDER.id;
                    } else if (onBorder(TFBiomes.DEEP_MUSHROOMS.id, center, right, left, up, down)) {
                        output[dx + dz * width] = TFBiomes.MUSHROOMS.id;
                    } else if (onBorder(TFBiomes.GLACIER.id, center, right, left, up, down)) {
                        output[dx + dz * width] = TFBiomes.SNOW.id;
                    } else {
                        output[dx + dz * width] = center;
                    }
                }
            }
            return output;
        }

        private static boolean onBorder(int biome, int center, int right, int left, int up, int down) {
            if (center != biome) {
                return false;
            } else if (right != biome && !TFBiomes.isFeature(right)) {
                return true;
            } else if (left != biome && !TFBiomes.isFeature(left)) {
                return true;
            } else {
                return up != biome && !TFBiomes.isFeature(up) || down != biome && !TFBiomes.isFeature(down);
            }
        }
    }

    /** GenLayerTFStream: a stream where two different biomes meet (not at lakes, clearings and related pairs). */
    static final class Stream extends Layer {
        Stream(long salt, Layer parent) {
            super(salt);
            this.parent = parent;
        }

        @Override
        public int[] sample(int x, int z, int width, int depth) {
            int nwidth = width + 2;
            int[] input = this.parent.sample(x - 1, z - 1, nwidth, depth + 2);
            int[] output = new int[width * depth];
            for (int dz = 0; dz < depth; dz++) {
                for (int dx = 0; dx < width; dx++) {
                    int left = input[dx + 0 + (dz + 1) * nwidth];
                    int right = input[dx + 2 + (dz + 1) * nwidth];
                    int down = input[dx + 1 + (dz + 0) * nwidth];
                    int up = input[dx + 1 + (dz + 2) * nwidth];
                    int mid = input[dx + 1 + (dz + 1) * nwidth];
                    output[dx + dz * width] = shouldStream(mid, left, down, right, up) ? TFBiomes.STREAM.id : -1;
                }
            }
            return output;
        }

        private static boolean shouldStream(int mid, int left, int down, int right, int up) {
            return shouldStream(mid, left) || shouldStream(mid, right) || shouldStream(mid, down) || shouldStream(mid, up);
        }

        private static boolean shouldStream(int biome1, int biome2) {
            int glacier = TFBiomes.GLACIER.id;
            int snow = TFBiomes.SNOW.id;
            int deep = TFBiomes.DEEP_MUSHROOMS.id;
            int mushrooms = TFBiomes.MUSHROOMS.id;
            int swamp = TFBiomes.SWAMP.id;
            int fire = TFBiomes.FIRE_SWAMP.id;
            if (biome1 == biome2) {
                return false;
            } else if (biome1 == -biome2) {
                return false;
            } else if (biome1 == glacier && biome2 == snow || biome1 == snow && biome2 == glacier) {
                return false;
            } else if (biome1 == deep && biome2 == mushrooms || biome1 == mushrooms && biome2 == deep) {
                return false;
            } else if (biome1 == swamp && biome2 == fire || biome1 == fire && biome2 == swamp) {
                return false;
            } else if (biome1 == TFBiomes.LAKE.id || biome2 == TFBiomes.LAKE.id) {
                return false;
            } else if (biome1 == TFBiomes.CLEARING.id || biome2 == TFBiomes.CLEARING.id) {
                return false;
            }
            return !TFBiomes.isFeature(biome1) && !TFBiomes.isFeature(biome2);
        }
    }
}
