package betterdungeons.gen;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * The great wall along chunk z = -500 (blocks -8000 to -7985) of WorldGeneratorNew: BuilderWall, and every ten
 * chunks in x a castle, snow castle or pirate ship 70 blocks north of it (BuilderCastle, BuilderCastleSnow,
 * BuilderPirateShip), all with the armoured endermen (mob 8).
 */
final class BuilderWall {
    static final int WALL_CHUNK_Z = -500;

    private BuilderWall() {
    }

    /** The part of WorldGeneratorNew.generateSurface for a chunk of the wall. */
    static void generate(Random random, OldWorld world, int i, int k) {
        wall(world, i, k);
        int x = i;
        int z = k - 70;
        OldBiome b = world.getBiome(x, z);
        if (i / 16 % 10 == 0) {
            int y = world.getTopSolidOrLiquidBlock(x, z);
            String kind = castleKind(b);
            if ("ship".equals(kind)) {
                // BuilderPirateShip: always at y 60
                new BuilderHelper(world).fillMap(world, random, BDTemplate.random("ships", random), x, 60, z, 8);
            } else if ("snow castle".equals(kind)) {
                castle(random, world, x, y, z, 8, "snowCastles", 5);
            } else if ("castle".equals(kind)) {
                castle(random, world, x, y, z, 8, "castles", 20);
            }
        }
    }

    /** The vanilla biomes (BiomeGenBase instances) the original asked for; mod biomes get nothing. */
    static String castleKind(OldBiome b) {
        if (b == OldBiomes.OCEAN) {
            return "ship";
        } else if (b == OldBiomes.TAIGA || b == OldBiomes.ICE_PLAINS) {
            return "snow castle";
        } else if (b == OldBiomes.DESERT || b == OldBiomes.PLAINS || b == OldBiomes.FOREST || b == OldBiomes.SWAMPLAND) {
            return "castle";
        }
        return null;
    }

    /** BuilderWall.generate: stone bricks from y 120 down through air, water, ice, leaves, logs, snow, grass and vines, battlements on the long sides. */
    private static void wall(OldWorld world, int i, int k) {
        int height = 120;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (z == 0 || z == 15) {
                    world.setBlock(i + x, height + 1, k + z, 98, 0);
                    if (x % 2 == 0) {
                        world.setBlock(i + x, height + 2, k + z, 98, 0);
                    }
                }
                // (the original never ends on a column open down to the void; bedrock stops it)
                for (int y = 0; height - y >= 0 && canPlaceWall(world.getBlockId(i + x, height - y, k + z)); y++) {
                    world.setBlock(i + x, height - y, k + z, 98, 0);
                }
            }
        }
    }

    private static boolean canPlaceWall(int id) {
        return id == 0 || id == 9 || id == 79 || id == 18 || id == 17 || id == 78 || id == 2 || id == 106;
    }

    /**
     * BuilderCastle / BuilderCastleSnow: a template of the folder, set {@code depth} blocks below the mean height
     * (getTopSolidOrLiquidBlock) of its area; never below y 0.
     */
    private static void castle(Random random, OldWorld world, int i, int j, int k, int idMob, String folder, int depth) {
        BuilderHelper b = new BuilderHelper(world);
        BDTemplate map = BDTemplate.random(folder, random);
        int maxX = map.sx;
        int maxY = map.sz;
        int cant = 0;
        int media = 0;
        for (int x = 0; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                media += world.getTopSolidOrLiquidBlock(i + x, y + k);
                cant++;
            }
        }
        int height = media / cant - depth;
        height = Math.max(0, height);
        b.fillMap(world, random, map, i, height, k, idMob);
    }
}
