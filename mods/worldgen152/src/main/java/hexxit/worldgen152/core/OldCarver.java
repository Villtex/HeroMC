package hexxit.worldgen152.core;

import java.util.Random;

/**
 * Shared frame of the 1.5.2 cave and ravine generators (MapGenBase): every chunk within 8 chunks may start
 * tunnels that reach into the chunk being generated.
 */
abstract class OldCarver {
    protected static final int RANGE = 8;

    private final long seed;

    protected OldCarver(long seed) {
        this.seed = seed;
    }

    final void carve(int chunkX, int chunkZ, byte[] blocks, OldBiome[] biomes) {
        Random random = new Random(this.seed);
        long xMul = random.nextLong();
        long zMul = random.nextLong();

        for (int x = chunkX - RANGE; x <= chunkX + RANGE; x++) {
            for (int z = chunkZ - RANGE; z <= chunkZ + RANGE; z++) {
                long a = x * xMul;
                long b = z * zMul;
                random.setSeed(a ^ b ^ this.seed);
                this.start(random, x, z, chunkX, chunkZ, blocks, biomes);
            }
        }
    }

    protected abstract void start(Random random, int originX, int originZ, int chunkX, int chunkZ, byte[] blocks, OldBiome[] biomes);

    protected static boolean isWater(byte block) {
        return block == OldBlocks.WATER_MOVING || block == OldBlocks.WATER_STILL;
    }

    private static boolean keepsGrassSurface(OldBiome biome) {
        return biome == OldBiomes.MUSHROOM_ISLAND || biome == OldBiomes.BEACH || biome == OldBiomes.DESERT;
    }

    protected static boolean isTopBlock(byte[] blocks, int index, int x, int z, OldBiome[] biomes) {
        OldBiome biome = biomes[x + z * 16];
        return keepsGrassSurface(biome) ? blocks[index] == OldBlocks.GRASS : blocks[index] == biome.topBlock;
    }

    protected static void dig(byte[] blocks, int index, int x, int y, int z, OldBiome[] biomes, boolean foundTop) {
        OldBiome biome = biomes[x + z * 16];
        int top = keepsGrassSurface(biome) ? OldBlocks.GRASS : biome.topBlock;
        int filler = keepsGrassSurface(biome) ? OldBlocks.DIRT : biome.fillerBlock;
        int block = blocks[index];
        if (block == OldBlocks.STONE || block == filler || block == top) {
            if (y < 10) {
                blocks[index] = OldBlocks.LAVA_MOVING;
            } else {
                blocks[index] = 0;
                if (foundTop && blocks[index - 1] == filler) {
                    blocks[index - 1] = (byte) top;
                }
            }
        }
    }
}
