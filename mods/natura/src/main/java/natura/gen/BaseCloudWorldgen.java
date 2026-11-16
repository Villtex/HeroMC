package natura.gen;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * BaseCloudWorldgen, the second IWorldGenerator of Natura: white clouds high above the overworld, dark clouds in
 * the End, ash and sulfur clouds in the Nether. The pack's Natura.txt switches all four off; each branch tests its
 * switch first, so with the pack settings this generator draws no random numbers at all.
 */
public final class BaseCloudWorldgen {
    private static final CloudGen SMALL_CLOUD = new CloudGen(NaturaIds.CLOUD, NaturaIds.CLOUD_WHITE, 10, false);
    private static final CloudGen MEDIUM_CLOUD = new CloudGen(NaturaIds.CLOUD, NaturaIds.CLOUD_WHITE, 20, false);
    private static final CloudGen LARGE_CLOUD = new CloudGen(NaturaIds.CLOUD, NaturaIds.CLOUD_WHITE, 30, false);
    private static final CloudGen HUGE_CLOUD = new CloudGen(NaturaIds.CLOUD, NaturaIds.CLOUD_WHITE, 40, false);
    private static final CloudGen SMALL_DARK_CLOUD = new CloudGen(NaturaIds.CLOUD, NaturaIds.CLOUD_DARK, 10, false);
    private static final CloudGen LARGE_DARK_CLOUD = new CloudGen(NaturaIds.CLOUD, NaturaIds.CLOUD_DARK, 30, false);
    private static final CloudGen HUGE_DARK_CLOUD = new CloudGen(NaturaIds.CLOUD, NaturaIds.CLOUD_DARK, 40, false);
    private static final CloudGen TINY_ASH_CLOUD = new CloudGen(NaturaIds.CLOUD, NaturaIds.CLOUD_ASH, 3, false);
    private static final CloudGen SMALL_ASH_CLOUD = new CloudGen(NaturaIds.CLOUD, NaturaIds.CLOUD_ASH, 10, false);
    private static final CloudGen LARGE_ASH_CLOUD = new CloudGen(NaturaIds.CLOUD, NaturaIds.CLOUD_ASH, 27, false);
    private static final CloudGen HUGE_ASH_CLOUD = new CloudGen(NaturaIds.CLOUD, NaturaIds.CLOUD_ASH, 37, false);
    private static final CloudGen TINY_SULFUR_CLOUD = new CloudGen(NaturaIds.CLOUD, NaturaIds.CLOUD_SULFUR, 3, false);
    private static final CloudGen SMALL_SULFUR_CLOUD = new CloudGen(NaturaIds.CLOUD, NaturaIds.CLOUD_SULFUR, 10, false);
    private static final CloudGen LARGE_SULFUR_CLOUD = new CloudGen(NaturaIds.CLOUD, NaturaIds.CLOUD_SULFUR, 27, false);
    private static final CloudGen HUGE_SULFUR_CLOUD = new CloudGen(NaturaIds.CLOUD, NaturaIds.CLOUD_SULFUR, 37, false);
    // (mediumdarkcloud, mediumashcloud and mediumsulfurcloud of the original are never used)

    /** {@code world.provider.dimensionId}: the world of the core is the overworld. */
    private static final int DIMENSION_ID = 0;

    private BaseCloudWorldgen() {
    }

    /** IWorldGenerator.generate. */
    public static void generate(Random random, int chunkX, int chunkZ, OldWorld world) {
        int xChunk = chunkX * 16 + 8;
        int zChunk = chunkZ * 16 + 8;
        OldBiome biome = world.getBiome(xChunk + 16, zChunk + 16);
        if (NaturaIds.GENERATE_OVERWORLD_CLOUDS
                && biome.rainfall > 0.15F
                && random.nextInt(NaturaIds.CLOUD_SPAWN_RARITY) == 0
                && DIMENSION_ID != 1) {
            int xCh = xChunk + random.nextInt(16);
            int zCh = zChunk + random.nextInt(16);
            int yCh = random.nextInt(NaturaIds.CLOUD_SPAWN_RANGE) + NaturaIds.CLOUD_SPAWN_HEIGHT;
            int size = random.nextInt(12);
            if (size < 5) {
                SMALL_CLOUD.generate(world, random, xCh, yCh, zCh);
            } else if (size < 9) {
                MEDIUM_CLOUD.generate(world, random, xCh, yCh, zCh);
            } else if (size < 11) {
                LARGE_CLOUD.generate(world, random, xCh, yCh, zCh);
            } else {
                HUGE_CLOUD.generate(world, random, xCh, yCh, zCh);
            }
        }

        if (NaturaIds.GENERATE_DARK_CLOUDS && biome == OldBiomes.SKY && random.nextInt(4) == 0) {
            int xCh = xChunk + random.nextInt(16);
            int zCh = zChunk + random.nextInt(16);
            for (int iter = 0; iter < NaturaIds.DARK_CLOUD_SPAWN_RARITY; iter++) {
                int height = random.nextInt(NaturaIds.DARK_CLOUD_SPAWN_RANGE);
                if (random.nextInt(5) == 0) {
                    SMALL_DARK_CLOUD.generate(world, random, xCh, height + NaturaIds.DARK_CLOUD_SPAWN_HEIGHT, zCh);
                } else if (random.nextInt(7) == 0) {
                    // the original takes the white medium cloud here
                    MEDIUM_CLOUD.generate(world, random, xCh, height + NaturaIds.DARK_CLOUD_SPAWN_HEIGHT, zCh);
                } else if (random.nextInt(9) == 0) {
                    LARGE_DARK_CLOUD.generate(world, random, xCh, height + NaturaIds.DARK_CLOUD_SPAWN_HEIGHT, zCh);
                }
                if (random.nextInt(12) == 0) {
                    HUGE_DARK_CLOUD.generate(world, random, xCh, height + NaturaIds.DARK_CLOUD_SPAWN_HEIGHT, zCh);
                }
            }
        }

        if (BaseCropWorldgen.isHellWorld(world)) {
            if (NaturaIds.GENERATE_ASH_CLOUDS && random.nextInt(NaturaIds.ASH_SPAWN_RARITY) == 0) {
                int xCh = xChunk + random.nextInt(16);
                int yCh = random.nextInt(NaturaIds.ASH_SPAWN_RANGE) + NaturaIds.ASH_SPAWN_HEIGHT;
                int zCh = zChunk + random.nextInt(16);
                int size = random.nextInt(12);
                if (size < 5) {
                    TINY_ASH_CLOUD.generate(world, random, xCh, yCh, zCh);
                } else if (size < 9) {
                    SMALL_ASH_CLOUD.generate(world, random, xCh, yCh, zCh);
                } else if (size < 11) {
                    LARGE_ASH_CLOUD.generate(world, random, xCh, yCh, zCh);
                } else {
                    HUGE_ASH_CLOUD.generate(world, random, xCh, yCh, zCh);
                }
            }

            if (NaturaIds.GENERATE_SULFUR_CLOUDS && random.nextInt(NaturaIds.SULFUR_SPAWN_RARITY) == 0) {
                int xCh = xChunk + random.nextInt(16);
                int yCh = random.nextInt(NaturaIds.SULFUR_SPAWN_RANGE) + NaturaIds.SULFUR_SPAWN_HEIGHT;
                int zCh = zChunk + random.nextInt(16);
                int size = random.nextInt(12);
                if (size < 5) {
                    TINY_SULFUR_CLOUD.generate(world, random, xCh, yCh, zCh);
                } else if (size < 9) {
                    SMALL_SULFUR_CLOUD.generate(world, random, xCh, yCh, zCh);
                } else if (size < 11) {
                    LARGE_SULFUR_CLOUD.generate(world, random, xCh, yCh, zCh);
                } else {
                    HUGE_SULFUR_CLOUD.generate(world, random, xCh, yCh, zCh);
                }
            }
        }
    }
}
