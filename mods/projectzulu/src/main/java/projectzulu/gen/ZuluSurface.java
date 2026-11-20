package projectzulu.gen;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * WorldGenReplaceTop and WorldGenOnSurface of Project Zulu 1.0.3.8. The biome checks compare the biome objects of
 * 1.5.2, so only the vanilla biomes count (not the ExtrabiomesXL ones of the same kind). No game classes.
 */
final class ZuluSurface {
    private ZuluSurface() {
    }

    /**
     * WorldGenReplaceTop.generate: in the 16x16 blocks from x, z (no +8), the two blocks from the top solid or
     * liquid height up that are one of {@code replace} become {@code id}, where the biome is one of {@code biomes}.
     */
    static void replaceTop(OldWorld world, int id, int[] replace, OldBiome[] biomes, int x, int z) {
        for (int i = 0; i < 16; i++) {
            for (int k = 0; k < 16; k++) {
                int bx = x + i;
                int bz = z + k;
                int by = world.getTopSolidOrLiquidBlock(bx, bz);
                OldBiome biome = world.getBiome(bx, bz);
                boolean there = false;
                for (OldBiome b : biomes) {
                    if (biome == b) {
                        there = true;
                    }
                }
                if (there) {
                    for (int j = 0; j < 2; j++) {
                        for (int r : replace) {
                            if (world.getBlockId(bx, by + j, bz) == r) {
                                ZuluUpdates.set(world, bx, by + j, bz, id);
                            }
                        }
                    }
                }
            }
        }
    }

    /** WorldGenOnSurface.generate(world, random, x, y, z) for the plant {@code plantId}. */
    static void onSurface(OldWorld world, Random random, int plantId, int numberPerChunk, int chancePerChunk, int x, int z) {
        if (chancePerChunk - random.nextInt(100) < 0) {
            return;
        }
        for (int n = 0; n < numberPerChunk; n++) {
            int px = x + random.nextInt(8) - random.nextInt(8);
            int pz = z + random.nextInt(8) - random.nextInt(8);
            int py = world.getTopSolidOrLiquidBlock(px, pz);
            int below = world.getBlockId(px, py - 1, pz);
            OldBiome biome = world.getBiome(px, pz);
            if (!world.isAirBlock(px, py, pz)) {
                continue;
            }
            if (plantId == ZuluIds.ALOE_VERA && (below == 2 || below == 3 || below == 12)
                    && (biome == OldBiomes.DESERT || biome == OldBiomes.DESERT_HILLS)) {
                ZuluUpdates.set(world, px, py, pz, plantId);
            }
            if (plantId == ZuluIds.PALM_TREE_SAPLING && (below == 12 || below == 3 || below == 2)
                    && (biome == OldBiomes.BEACH || biome == OldBiomes.RIVER)) {
                ZuluUpdates.set(world, px, py, pz, plantId);
            }
            if (plantId == ZuluIds.NIGHT_BLOOM && (below == 60 || below == 3 || below == 2 || below == ZuluIds.ALOE_VERA)
                    && (biome == OldBiomes.PLAINS || biome == OldBiomes.RIVER)) {
                ZuluUpdates.set(world, px, py, pz, plantId);
            }
            if (plantId == ZuluIds.CREEPER_BLOSSOM && (below == 3 || below == 2)
                    && (biome == OldBiomes.FOREST || biome == OldBiomes.FOREST_HILLS)) {
                ZuluUpdates.set(world, px, py, pz, plantId);
            }
        }
    }
}
