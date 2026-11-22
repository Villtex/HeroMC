package legendgear.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldTerrainGen;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * The world generation of LegendGear (ShrubGenerator, BombFlowerGenerator, in that order), without game classes so
 * that tools/expect.sh can run it. Ids and values of the pack's LegendGear.cfg.
 */
public final class LGWorldGen {
    /** FML mod id of the original. */
    public static final String FML_ID = "LegendGear";
    public static final int BOMB_FLOWER = 1176;
    public static final int MYSTIC_SHRUB = 1182;
    /** general.MysticShrubRarity: a cluster in 1 of 8 chunks. */
    static final int SHRUB_RARITY = 8;
    /** general.MysticShrubGenStarChance. */
    static final double SHRUB_STAR_CHANCE = 0.3;
    /** general.ShrubDisabledBiomes. */
    static final int[] SHRUB_DISABLED_BIOMES = {2, 5, 8, 9, 10, 11, 12, 13, 17, 19};

    private LGWorldGen() {
    }

    public static void register() {
        // MysticShrub(Material.plants) and BlockBombFlower(Material.plants): not opaque, crossed squares
        OldBlockInfo.register(MYSTIC_SHRUB, OldBlockInfo.Material.PLANTS, false, 0, false);
        OldBlockInfo.register(BOMB_FLOWER, OldBlockInfo.Material.PLANTS, false, 0, false);
        OldTerrainGen.registerWorldGenerator(FML_ID, "legendgear:shrubs", LGWorldGen::shrubs);
        OldTerrainGen.registerWorldGenerator(FML_ID, "legendgear:bomb_flowers", LGWorldGen::bombFlowers);
    }

    // ---------------------------------------------------------------- mystic shrubs

    /** ShrubGenerator: in the overworld (canRespawnHere) 1 in 8 chunks, not in the disabled biomes. */
    static void shrubs(Random random, int chunkX, int chunkZ, OldWorld world) {
        if (world.dimension() != 0 || random.nextInt(SHRUB_RARITY) != 0) {
            return;
        }
        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        int biome = world.getBiome(x, z).id;
        for (int disabled : SHRUB_DISABLED_BIOMES) {
            if (biome == disabled) {
                return;
            }
        }
        cluster(world, random, x, z);
    }

    /** ShrubClusterMaker.generate: a star (30%) or a blob of 12 shrubs on the top block. */
    static void cluster(OldWorld world, Random random, int x, int z) {
        int y = world.getTopSolidOrLiquidBlock(x, z);
        if (random.nextDouble() < SHRUB_STAR_CHANCE) {
            star(world, x, y, z);
        } else {
            putShrub(world, x, y, z + 1);
            putShrub(world, x + 1, y, z + 1);
            for (int i = -1; i < 3; i++) {
                putShrub(world, x + i, y, z);
            }
            for (int i = -1; i < 3; i++) {
                putShrub(world, x + i, y, z - 1);
            }
            putShrub(world, x, y, z - 2);
            putShrub(world, x + 1, y, z - 2);
        }
    }

    static void star(OldWorld world, int x, int y, int z) {
        int[][] offsets = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}, {-3, 0}, {0, -3}, {3, 0}, {0, 3}, {-2, -2}, {2, -2}, {2, 2}, {-2, 2}};
        for (int[] o : offsets) {
            putShrub(world, x + o[0], y, z + o[1]);
        }
    }

    /** Block.canPlaceBlockAt: air or a replaceable material. */
    private static boolean replaceable(OldWorld world, int x, int y, int z) {
        int id = world.getBlockId(x, y, z);
        return id == 0 || OldBlockInfo.material(id).replaceable;
    }

    /** MysticShrub.canBlockStay: open sky, the top block of the column, EnumPlantType.Plains soil (grass, dirt, farmland). */
    private static boolean shrubCanStay(OldWorld world, int x, int y, int z) {
        int soil = world.getBlockId(x, y - 1, z);
        return world.canBlockSeeTheSky(x, y, z) && world.getTopSolidOrLiquidBlock(x, z) == y && (soil == 2 || soil == 3 || soil == 60);
    }

    static boolean putShrub(OldWorld world, int x, int y, int z) {
        if (replaceable(world, x, y, z) && shrubCanStay(world, x, y, z)) {
            world.setBlock(x, y, z, MYSTIC_SHRUB, 0);
            return true;
        }
        return false;
    }

    // ---------------------------------------------------------------- bomb flowers

    /** BlockBombFlower.canBlockStay: on stone or netherrack, air above, lava under an air block of the 3x3 around. */
    private static boolean flowerCanStay(OldWorld world, int x, int y, int z) {
        int below = world.getBlockId(x, y - 1, z);
        if (below != 1 && below != 87) {
            return false;
        }
        if (world.material(x, y + 1, z) != OldBlockInfo.Material.AIR) {
            return false;
        }
        for (int ix = x - 1; ix <= x + 1; ix++) {
            for (int iz = z - 1; iz <= z + 1; iz++) {
                if (world.material(ix, y, iz) == OldBlockInfo.Material.AIR && world.material(ix, y - 1, iz) == OldBlockInfo.Material.LAVA) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean flowerCanPlace(OldWorld world, int x, int y, int z) {
        return replaceable(world, x, y, z) && flowerCanStay(world, x, y, z);
    }

    /**
     * BombFlowerGenerator (every dimension): 8 tries; from y 1 up to the first air block of the column; where a flower
     * fits, each block of the 9x9 around that fits gets one with chance 1/2.
     */
    static void bombFlowers(Random random, int chunkX, int chunkZ, OldWorld world) {
        int spread = 9;
        for (int i = 0; i < 8; i++) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            int y = 1;
            while (!world.isAirBlock(x, y, z)) {
                y++;
            }
            if (flowerCanPlace(world, x, y, z)) {
                for (int xo = 0; xo < spread; xo++) {
                    for (int zo = 0; zo < spread; zo++) {
                        int nx = x + xo - spread / 2;
                        int nz = z + zo - spread / 2;
                        if (flowerCanPlace(world, nx, y, nz) && random.nextFloat() < 0.5F) {
                            world.setBlock(nx, y, nz, BOMB_FLOWER, 0);
                        }
                    }
                }
            }
        }
    }
}
