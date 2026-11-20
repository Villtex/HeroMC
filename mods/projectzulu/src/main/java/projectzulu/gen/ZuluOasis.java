package projectzulu.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo.Material;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * WorldGenOasis of Project Zulu 1.0.3.8: a pond with a grass rim and a sand border, palm trees on the grass. All
 * setBlock calls are flag 3 ({@link ZuluUpdates}). The biome lookups of the original (five samples) were never used
 * and draw no random numbers, so they are left out. No game classes.
 */
final class ZuluOasis {
    private ZuluOasis() {
    }

    private static void set(OldWorld world, int x, int y, int z, int id) {
        ZuluUpdates.set(world, x, y, z, id);
    }

    static boolean generate(OldWorld world, Random random, int x, int y, int z) {
        int log = ZuluIds.PALM_TREE_LOG;
        int cx = x + random.nextInt(8) - random.nextInt(8);
        int cz = z + random.nextInt(8) - random.nextInt(8);
        int cy = (world.getTopSolidOrLiquidBlock(cx, cz) + world.getTopSolidOrLiquidBlock(cx - 1, cz) + world.getTopSolidOrLiquidBlock(cx, cz - 1)
                + world.getTopSolidOrLiquidBlock(cx - 1, cz - 1) + world.getTopSolidOrLiquidBlock(cx + 1, cz - 1)
                + world.getTopSolidOrLiquidBlock(cx + 1, cz) + world.getTopSolidOrLiquidBlock(cx, cz + 1)
                + world.getTopSolidOrLiquidBlock(cx - 1, cz + 1) + world.getTopSolidOrLiquidBlock(cx + 1, cz + 1)) / 9;
        if (!world.isAirBlock(cx, cy, cz) || world.material(cx, cy - 1, cz) == Material.WATER) {
            return true;
        }
        int width = random.nextInt(4) + 2;
        int width2 = random.nextInt(4) + 2;
        for (int i = -width; i <= width; i++) {
            for (int k = -width2; k <= width2; k++) {
                int depth = random.nextInt(1) + 1;
                for (int j = 0; j >= -depth; j--) {
                    set(world, cx + i, cy + j, cz + k, 9);
                }
            }
        }
        int rngGrassAway = 3;
        int minGrassAway = (int) ((width + 0.2 * width2) / 2.0) + 1;
        for (int i = -width - 1; i <= width + 1; i++) {
            int numGrassAway = random.nextInt(rngGrassAway) + minGrassAway;
            for (int k = width2; k <= width2 + numGrassAway; k++) {
                grass(world, cx + i, cy, cz + k);
            }
            for (int k = width2 + numGrassAway + 1; k <= width2 + numGrassAway + 3; k++) {
                sand(world, cx + i, cy, cz + k);
            }
        }
        for (int i = -width - 1; i <= width + 1; i++) {
            int numGrassAway = random.nextInt(rngGrassAway) + minGrassAway;
            for (int k = -width2; k >= -(width2 + numGrassAway); k--) {
                grass(world, cx + i, cy, cz + k);
            }
            for (int k = -(width2 + numGrassAway + 1); k >= -(width2 + numGrassAway + 3); k--) {
                sand(world, cx + i, cy, cz + k);
            }
        }
        minGrassAway = (int) ((width2 + 0.2 * width) / 2.0) + 1;
        for (int k = -width2 - 1; k <= width2 + 1; k++) {
            int numGrassAway = random.nextInt(rngGrassAway) + minGrassAway;
            for (int i = width; i <= width + numGrassAway; i++) {
                grass(world, cx + i, cy, cz + k);
            }
            for (int i = width + numGrassAway + 1; i <= width + numGrassAway + 3; i++) {
                sand(world, cx + i, cy, cz + k);
            }
        }
        for (int k = -width2 - 1; k <= width2 + 1; k++) {
            int numGrassAway = random.nextInt(rngGrassAway) + minGrassAway;
            for (int i = -width - 1; i >= -width - numGrassAway; i--) {
                grass(world, cx + i, cy, cz + k);
            }
            for (int i = -(width + numGrassAway + 1); i >= -(width + numGrassAway + 3); i--) {
                sand(world, cx + i, cy, cz + k);
            }
        }
        int ax = width + 2;
        int az = width2 + 2;
        for (int j = 0; j > -3; j--) {
            int y0 = cy + j;
            set(world, cx + ax, y0, cz + az, 12);
            set(world, cx + ax, y0, cz + az + 1, 12);
            set(world, cx + ax, y0, cz + az + 2, 12);
            set(world, cx + ax + 1, y0, cz + az, 12);
            set(world, cx + ax + 2, y0, cz + az, 12);
            set(world, cx + ax + 1, y0, cz + az + 1, 12);
            set(world, cx - ax, y0, cz + az, 12);
            set(world, cx - ax, y0, cz + az + 1, 12);
            set(world, cx - ax, y0, cz + az + 2, 12);
            set(world, cx - (ax + 1), y0, cz + az, 12);
            set(world, cx - (ax + 2), y0, cz + az, 12);
            set(world, cx - (ax + 1), y0, cz + az + 1, 12);
            set(world, cx + ax, y0, cz - az, 12);
            set(world, cx + ax, y0, cz - (az + 1), 12);
            set(world, cx + ax, y0, cz - (az + 2), 12);
            set(world, cx + ax + 1, y0, cz - az, 12);
            set(world, cx + ax + 2, y0, cz - az, 12);
            set(world, cx + ax + 1, y0, cz - (az + 1), 12);
            set(world, cx - ax, y0, cz - az, 12);
            set(world, cx - ax, y0, cz - (az + 1), 12);
            set(world, cx - ax, y0, cz - (az + 2), 12);
            set(world, cx - (ax + 1), y0, cz - az, 12);
            set(world, cx - (ax + 2), y0, cz - az, 12);
            set(world, cx - (ax + 1), y0, cz - (az + 1), 12);
        }
        int numTrees = random.nextInt((width + width2) / 2 - 1) + 1;
        // the original looped until enough spots were found; the cap only matters where 1.5.2 hung the server
        for (int attempts = 0; numTrees > 0 && attempts < 100000; attempts++) {
            int tx = random.nextInt(2 * (width + 2)) - (width + 2);
            int tz = random.nextInt(2 * (width2 + 2)) - (width2 + 2);
            if (world.getBlockId(cx + tx, cy, cz + tz) == 2 && world.getBlockId(cx + tx - 1, cy + 1, cz + tz) != log
                    && world.getBlockId(cx + tx + 1, cy + 1, cz + tz) != log && world.getBlockId(cx + tx, cy + 1, cz + tz - 1) != log
                    && world.getBlockId(cx + tx, cy + 1, cz + tz + 1) != log) {
                growTree(world, cx + tx, cy + 1, cz + tz, random);
                numTrees--;
            }
        }
        return true;
    }

    private static void grass(OldWorld world, int x, int y, int z) {
        set(world, x, y, z, 2);
        for (int j = 1; j < 3; j++) {
            set(world, x, y + j, z, 0);
        }
    }

    private static void sand(OldWorld world, int x, int y, int z) {
        for (int j = 0; j > -5; j--) {
            set(world, x, y + j, z, 12);
        }
    }

    /** WorldGenOasis.growTree: a palm leaning towards the water nearby. */
    static void growTree(OldWorld world, int x, int y, int z, Random random) {
        int log = ZuluIds.PALM_TREE_LOG;
        int height = random.nextInt(3) + 4;
        int favoredX = 0;
        int favoredZ = 0;
        for (int i = -4; i <= 4; i++) {
            for (int k = -4; k <= 4; k++) {
                for (int j = -4; j <= 4; j++) {
                    int id = world.getBlockId(x + i, y + j, z + k);
                    if (id == 8 || id == 9) {
                        if (i != 0) {
                            favoredX += i / Math.abs(i);
                        }
                        if (k != 0) {
                            favoredZ += k / Math.abs(k);
                        }
                    }
                }
            }
        }
        if (Math.abs(favoredX) - Math.abs(favoredZ) >= 0) {
            favoredZ = 0;
        } else {
            favoredX = 0;
        }
        int lx = 0;
        int ly = 0;
        int lz = 0;
        int horizontalFactor = random.nextInt(10) + 20;
        while (ly <= height) {
            set(world, x + lx, y + ++ly, z + lz, log);
            if (favoredX > 0) {
                lx++;
                favoredX = Math.max(favoredX - horizontalFactor, 0);
                set(world, x + lx, y + ly, z + lz, log);
            } else if (favoredX < 0) {
                lx--;
                favoredX = Math.min(favoredX + horizontalFactor, 0);
                set(world, x + lx, y + ly, z + lz, log);
            }
            if (favoredZ > 0) {
                lz++;
                favoredZ = Math.max(favoredZ - horizontalFactor, 0);
                set(world, x + lx, y + ly, z + lz, log);
            } else if (favoredZ < 0) {
                lz--;
                favoredZ = Math.min(favoredZ + horizontalFactor, 0);
                set(world, x + lx, y + ly, z + lz, log);
            }
            if (ly + 1 == height) {
                set(world, x + lx, y + ++ly, z + lz, log);
                spawnLeaves(world, x + lx, y + ++ly, z + lz, height);
                set(world, x, y, z, log);
            }
        }
    }

    private static void spawnLeaves(OldWorld world, int x, int y, int z, int height) {
        int leaves = ZuluIds.PALM_TREE_LEAVES;
        int[][] points = height + 1 >= 7
                ? new int[][] {{0, 0, 0}, {-1, 0, 0}, {-2, 0, 0}, {-3, 0, 0}, {-4, -1, 0}, {1, 0, 0}, {2, 0, 0}, {3, 0, 0}, {3, -1, 0},
                        {0, 0, -1}, {0, 0, -2}, {0, 0, -3}, {0, -1, -3}, {0, 0, 1}, {0, 0, 2}, {0, 0, 3}, {0, -1, 3}}
                : new int[][] {{0, 0, 0}, {-1, 0, 0}, {-2, 0, 0}, {-2, -1, 0}, {1, 0, 0}, {2, 0, 0}, {2, -1, 0}, {0, 0, -1}, {0, 0, -2},
                        {0, -1, -2}, {0, 0, 1}, {0, 0, 2}, {0, -1, 2}};
        for (int[] p : points) {
            set(world, x + p[0], y + p[1], z + p[2], leaves);
        }
    }
}
