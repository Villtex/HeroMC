package hexxit.worldgen152.core.populate;

import hexxit.worldgen152.core.populate.OldBlockInfo.Material;

import java.util.Random;

/** Water and lava lakes, springs and the desert well. */
public final class OldLakeGens {
    private OldLakeGens() {
    }

    /** WorldGenLakes: {@code liquid} is 9 (still water) or 11 (still lava). */
    public static final class Lake extends OldGen {
        private final int liquid;

        public Lake(int liquid) {
            this.liquid = liquid;
        }

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            x -= 8;
            z -= 8;
            while (y > 5 && world.isAirBlock(x, y, z)) {
                y--;
            }
            if (y <= 4) {
                return false;
            }
            y -= 4;
            boolean[] shape = new boolean[2048];
            int blobs = random.nextInt(4) + 4;
            for (int i = 0; i < blobs; i++) {
                double sx = random.nextDouble() * 6.0 + 3.0;
                double sy = random.nextDouble() * 4.0 + 2.0;
                double sz = random.nextDouble() * 6.0 + 3.0;
                double cx = random.nextDouble() * (16.0 - sx - 2.0) + 1.0 + sx / 2.0;
                double cy = random.nextDouble() * (8.0 - sy - 4.0) + 2.0 + sy / 2.0;
                double cz = random.nextDouble() * (16.0 - sz - 2.0) + 1.0 + sz / 2.0;
                for (int bx = 1; bx < 15; bx++) {
                    for (int bz = 1; bz < 15; bz++) {
                        for (int by = 1; by < 7; by++) {
                            double dx = (bx - cx) / (sx / 2.0);
                            double dy = (by - cy) / (sy / 2.0);
                            double dz = (bz - cz) / (sz / 2.0);
                            if (dx * dx + dy * dy + dz * dz < 1.0) {
                                shape[(bx * 16 + bz) * 8 + by] = true;
                            }
                        }
                    }
                }
            }

            for (int bx = 0; bx < 16; bx++) {
                for (int bz = 0; bz < 16; bz++) {
                    for (int by = 0; by < 8; by++) {
                        if (border(shape, bx, by, bz)) {
                            Material material = world.material(x + bx, y + by, z + bz);
                            if (by >= 4 && material.liquid) {
                                return false;
                            }
                            if (by < 4 && !material.solid && world.getBlockId(x + bx, y + by, z + bz) != this.liquid) {
                                return false;
                            }
                        }
                    }
                }
            }

            for (int bx = 0; bx < 16; bx++) {
                for (int bz = 0; bz < 16; bz++) {
                    for (int by = 0; by < 8; by++) {
                        if (shape[(bx * 16 + bz) * 8 + by]) {
                            world.setBlock(x + bx, y + by, z + bz, by >= 4 ? 0 : this.liquid, 0);
                        }
                    }
                }
            }

            for (int bx = 0; bx < 16; bx++) {
                for (int bz = 0; bz < 16; bz++) {
                    for (int by = 4; by < 8; by++) {
                        if (shape[(bx * 16 + bz) * 8 + by] && world.getBlockId(x + bx, y + by - 1, z + bz) == 3
                                && world.skyLight(x + bx, y + by, z + bz) > 0) {
                            boolean mycelium = (world.getBiome(x + bx, z + bz).topBlock & 0xFF) == 110;
                            world.setBlock(x + bx, y + by - 1, z + bz, mycelium ? 110 : 2, 0);
                        }
                    }
                }
            }

            if (this.liquid == 10 || this.liquid == 11) {
                for (int bx = 0; bx < 16; bx++) {
                    for (int bz = 0; bz < 16; bz++) {
                        for (int by = 0; by < 8; by++) {
                            if (border(shape, bx, by, bz) && (by < 4 || random.nextInt(2) != 0)
                                    && world.material(x + bx, y + by, z + bz).solid) {
                                world.setBlock(x + bx, y + by, z + bz, 1, 0);
                            }
                        }
                    }
                }
            }

            if (this.liquid == 8 || this.liquid == 9) {
                for (int bx = 0; bx < 16; bx++) {
                    for (int bz = 0; bz < 16; bz++) {
                        if (world.isBlockFreezable(x + bx, y + 4, z + bz)) {
                            world.setBlock(x + bx, y + 4, z + bz, 79, 0);
                        }
                    }
                }
            }
            return true;
        }

        /** Outside the lake, but touching it. */
        private static boolean border(boolean[] shape, int x, int y, int z) {
            return !shape[(x * 16 + z) * 8 + y]
                    && (x < 15 && shape[((x + 1) * 16 + z) * 8 + y]
                    || x > 0 && shape[((x - 1) * 16 + z) * 8 + y]
                    || z < 15 && shape[(x * 16 + z + 1) * 8 + y]
                    || z > 0 && shape[(x * 16 + (z - 1)) * 8 + y]
                    || y < 7 && shape[(x * 16 + z) * 8 + y + 1]
                    || y > 0 && shape[(x * 16 + z) * 8 + (y - 1)]);
        }
    }

    /**
     * WorldGenLiquids: a spring in a stone wall. {@code liquid} is 8 (flowing water) or 10 (flowing lava);
     * the flowing id tells the game to let the liquid run once the chunk is live, as the original did.
     */
    public static final class Spring extends OldGen {
        private final int liquid;

        public Spring(int liquid) {
            this.liquid = liquid;
        }

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            if (world.getBlockId(x, y + 1, z) != 1 || world.getBlockId(x, y - 1, z) != 1) {
                return false;
            }
            int here = world.getBlockId(x, y, z);
            if (here != 0 && here != 1) {
                return false;
            }
            int stone = 0;
            int air = 0;
            int[][] sides = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] side : sides) {
                if (world.getBlockId(x + side[0], y, z + side[1]) == 1) {
                    stone++;
                }
            }
            for (int[] side : sides) {
                if (world.isAirBlock(x + side[0], y, z + side[1])) {
                    air++;
                }
            }
            if (stone == 3 && air == 1) {
                world.setBlock(x, y, z, this.liquid, 0);
            }
            return true;
        }
    }

    /** WorldGenDesertWells. */
    public static final class DesertWell extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            while (world.isAirBlock(x, y, z) && y > 2) {
                y--;
            }
            if (world.getBlockId(x, y, z) != 12) {
                return false;
            }
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (world.isAirBlock(x + dx, y - 1, z + dz) && world.isAirBlock(x + dx, y - 2, z + dz)) {
                        return false;
                    }
                }
            }
            for (int dy = -1; dy <= 0; dy++) {
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        world.setBlock(x + dx, y + dy, z + dz, 24, 0);
                    }
                }
            }
            world.setBlock(x, y, z, 8, 0);
            world.setBlock(x - 1, y, z, 8, 0);
            world.setBlock(x + 1, y, z, 8, 0);
            world.setBlock(x, y, z - 1, 8, 0);
            world.setBlock(x, y, z + 1, 8, 0);
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == -2 || dx == 2 || dz == -2 || dz == 2) {
                        world.setBlock(x + dx, y + 1, z + dz, 24, 0);
                    }
                }
            }
            world.setBlock(x + 2, y + 1, z, 44, 1);
            world.setBlock(x - 2, y + 1, z, 44, 1);
            world.setBlock(x, y + 1, z + 2, 44, 1);
            world.setBlock(x, y + 1, z - 2, 44, 1);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) {
                        world.setBlock(x + dx, y + 4, z + dz, 24, 0);
                    } else {
                        world.setBlock(x + dx, y + 4, z + dz, 44, 1);
                    }
                }
            }
            for (int dy = 1; dy <= 3; dy++) {
                world.setBlock(x - 1, y + dy, z - 1, 24, 0);
                world.setBlock(x - 1, y + dy, z + 1, 24, 0);
                world.setBlock(x + 1, y + dy, z - 1, 24, 0);
                world.setBlock(x + 1, y + dy, z + 1, 24, 0);
            }
            return true;
        }
    }
}
