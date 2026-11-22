package natura.gen;

import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * Natura's Nether ground for the world type Hexxit Integrated (feature {@code natura:nether_soil}). In 1.5.2 Natura
 * replaced the Nether chunk provider (worldgen152 {@code NetherTerrain}, Hexxit Legacy): below every netherrack floor
 * the next 2 to 4 blocks were tainted soil, below the soul sand beaches heat sand, and very dense rock deep inside
 * was tainted soil. The Nether of today has its own terrain, so this does the same afterwards: it looks at each
 * column of the chunk from the top down.
 */
public final class NetherSoilGen {
    private static final int NETHERRACK = 87;
    private static final int SOUL_SAND = 88;
    /** Netherrack this far below the floor and above the ceiling of its run counts as dense rock. */
    private static final int DENSE = 24;

    private NetherSoilGen() {
    }

    public static void generate(Random random, int chunkX, int chunkZ, OldWorld world) {
        if (!world.isHellWorld()) {
            return;
        }
        int x0 = chunkX * 16 + 8;
        int z0 = chunkZ * 16 + 8;
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                column(world, random, x0 + dx, z0 + dz);
            }
        }
    }

    private static void column(OldWorld world, Random random, int x, int z) {
        // depth of the layer: netherrackExclusivityNoise / 3 + 3 + random in the original, about 2 to 4
        int depth = 2 + random.nextInt(3);
        int y = 126;
        while (y > 0) {
            int id = world.getBlockId(x, y, z);
            if (id != NETHERRACK) {
                y--;
                continue;
            }
            int above = world.getBlockId(x, y + 1, z);
            int top = y;
            while (y > 0 && world.getBlockId(x, y, z) == NETHERRACK) {
                y--;
            }
            int bottom = y + 1;   // lowest netherrack of the run
            if (above == 0) {
                // a floor: the top block stays netherrack, the layer below becomes tainted soil
                for (int ty = top - 1; ty >= Math.max(bottom, top - depth); ty--) {
                    world.setBlock(x, ty, z, NaturaIds.TAINTED_SOIL, 0);
                }
            } else if (above == SOUL_SAND) {
                // under the soul sand: heat sand
                for (int ty = top; ty >= Math.max(bottom, top - depth + 1); ty--) {
                    world.setBlock(x, ty, z, NaturaIds.HEAT_SAND, 0);
                }
            }
            for (int ty = top - DENSE; ty >= bottom + DENSE; ty--) {
                world.setBlock(x, ty, z, NaturaIds.TAINTED_SOIL, 0);
            }
        }
    }
}
