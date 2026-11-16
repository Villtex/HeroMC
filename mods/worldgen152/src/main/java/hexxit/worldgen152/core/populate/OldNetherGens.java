package hexxit.worldgen152.core.populate;

import java.util.Random;

/**
 * What populated a Nether chunk of Hexxit: the lava springs and glowstone of Minecraft 1.5.2 and the fire
 * and glowshroom patches of Natura's {@code NetheriteChunkProvider}.
 */
public final class OldNetherGens {
    private static final int NETHERRACK = 87;
    private static final int GLOWSTONE = 89;
    private static final int FIRE = 51;

    private OldNetherGens() {
    }

    /**
     * WorldGenHellLava: a lava spring in netherrack. The original let the lava flow at once; here the
     * "flowing" id marks a source that starts to flow in the game (see OldChunkWriter), as for the springs of
     * the overworld.
     */
    public static final class HellLava extends OldGen {
        private final int lavaId;
        private final boolean enclosed;

        public HellLava(int lavaId, boolean enclosed) {
            this.lavaId = lavaId;
            this.enclosed = enclosed;
        }

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            if (world.getBlockId(x, y + 1, z) != NETHERRACK) {
                return false;
            }
            if (world.getBlockId(x, y, z) != 0 && world.getBlockId(x, y, z) != NETHERRACK) {
                return false;
            }
            int rock = 0;
            if (world.getBlockId(x - 1, y, z) == NETHERRACK) {
                rock++;
            }
            if (world.getBlockId(x + 1, y, z) == NETHERRACK) {
                rock++;
            }
            if (world.getBlockId(x, y, z - 1) == NETHERRACK) {
                rock++;
            }
            if (world.getBlockId(x, y, z + 1) == NETHERRACK) {
                rock++;
            }
            if (world.getBlockId(x, y - 1, z) == NETHERRACK) {
                rock++;
            }
            int air = 0;
            if (world.isAirBlock(x - 1, y, z)) {
                air++;
            }
            if (world.isAirBlock(x + 1, y, z)) {
                air++;
            }
            if (world.isAirBlock(x, y, z - 1)) {
                air++;
            }
            if (world.isAirBlock(x, y, z + 1)) {
                air++;
            }
            if (world.isAirBlock(x, y - 1, z)) {
                air++;
            }
            if (!this.enclosed && rock == 4 && air == 1 || rock == 5) {
                world.setBlock(x, y, z, this.lavaId, 0);
            }
            return true;
        }
    }

    /** Natura's FireGen (WorldGenFire that also burns on tainted soil). */
    public static final class Fire extends OldGen {
        private final int taintedSoilId;

        public Fire(int taintedSoilId) {
            this.taintedSoilId = taintedSoilId;
        }

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            for (int i = 0; i < 64; i++) {
                int bx = x + random.nextInt(8) - random.nextInt(8);
                int by = y + random.nextInt(4) - random.nextInt(4);
                int bz = z + random.nextInt(8) - random.nextInt(8);
                if (world.isAirBlock(bx, by, bz)) {
                    int below = world.getBlockId(bx, by - 1, bz);
                    if (below == NETHERRACK || below == this.taintedSoilId) {
                        world.setBlock(bx, by, bz, FIRE, 0);
                    }
                }
            }
            return true;
        }
    }

    /** WorldGenGlowStone1 and WorldGenGlowStone2 (the same code twice): a glowstone cluster hanging from netherrack. */
    public static final class Glowstone extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            if (!world.isAirBlock(x, y, z)) {
                return false;
            }
            if (world.getBlockId(x, y + 1, z) != NETHERRACK) {
                return false;
            }
            world.setBlock(x, y, z, GLOWSTONE, 0);
            for (int i = 0; i < 1500; i++) {
                int bx = x + random.nextInt(8) - random.nextInt(8);
                int by = y - random.nextInt(12);
                int bz = z + random.nextInt(8) - random.nextInt(8);
                if (world.getBlockId(bx, by, bz) == 0) {
                    int neighbours = 0;
                    for (int side = 0; side < 6; side++) {
                        int id = 0;
                        if (side == 0) {
                            id = world.getBlockId(bx - 1, by, bz);
                        }
                        if (side == 1) {
                            id = world.getBlockId(bx + 1, by, bz);
                        }
                        if (side == 2) {
                            id = world.getBlockId(bx, by - 1, bz);
                        }
                        if (side == 3) {
                            id = world.getBlockId(bx, by + 1, bz);
                        }
                        if (side == 4) {
                            id = world.getBlockId(bx, by, bz - 1);
                        }
                        if (side == 5) {
                            id = world.getBlockId(bx, by, bz + 1);
                        }
                        if (id == GLOWSTONE) {
                            neighbours++;
                        }
                    }
                    if (neighbours == 1) {
                        world.setBlock(bx, by, bz, GLOWSTONE, 0);
                    }
                }
            }
            return true;
        }
    }

    /**
     * Natura's FlowerGen for its glowshrooms (a BlockMushroom that grows on every opaque cube or solid top).
     * The light test of the mushroom (below 13) always passes: the Nether has no sky light, and block light
     * is not modelled.
     */
    public static final class Glowshroom extends OldGen {
        private final int plantId;
        private final int metadata;

        public Glowshroom(int plantId, int metadata) {
            this.plantId = plantId;
            this.metadata = metadata;
        }

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            for (int i = 0; i < 64; i++) {
                int bx = x + random.nextInt(8) - random.nextInt(8);
                int by = y + random.nextInt(4) - random.nextInt(4);
                int bz = z + random.nextInt(8) - random.nextInt(8);
                if (world.isAirBlock(bx, by, bz) && (!world.isHellWorld() || by < 127) && canStay(world, bx, by, bz)) {
                    world.setBlock(bx, by, bz, this.plantId, this.metadata);
                }
            }
            return true;
        }

        /** Glowshroom.canBlockStay: Forge's canSustainPlant for a BlockFlower whose soil is any opaque cube, else plant type Cave. */
        public static boolean canStay(OldWorld world, int x, int y, int z) {
            if (y < 0 || y >= OldWorld.HEIGHT) {
                return false;
            }
            int below = world.getBlockId(x, y - 1, z);
            return below != 0 && (OldBlockInfo.opaqueCube(below) || world.isSolidOnSide(x, y - 1, z, 1));
        }
    }
}
