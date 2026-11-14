package hexxit.worldgen152.core.populate;

import hexxit.worldgen152.core.OldMath;
import hexxit.worldgen152.core.populate.OldBlockInfo.Material;

import java.util.Random;

/** Ore veins and the sand, gravel and clay discs under water. */
public final class OldOreGens {
    private OldOreGens() {
    }

    /** WorldGenMinable: a vein of {@code count} blocks replacing {@code target} (stone by default). */
    public static final class Minable extends OldGen {
        private final int blockId;
        private final int blockMeta;
        private final int count;
        private final int target;

        public Minable(int blockId, int count) {
            this(blockId, 0, count, 1);
        }

        public Minable(int blockId, int blockMeta, int count, int target) {
            this.blockId = blockId;
            this.blockMeta = blockMeta;
            this.count = count;
            this.target = target;
        }

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            float angle = random.nextFloat() * (float) Math.PI;
            double x0 = x + 8 + OldMath.sin(angle) * this.count / 8.0F;
            double x1 = x + 8 - OldMath.sin(angle) * this.count / 8.0F;
            double z0 = z + 8 + OldMath.cos(angle) * this.count / 8.0F;
            double z1 = z + 8 - OldMath.cos(angle) * this.count / 8.0F;
            double y0 = y + random.nextInt(3) - 2;
            double y1 = y + random.nextInt(3) - 2;

            for (int step = 0; step <= this.count; step++) {
                double cx = x0 + (x1 - x0) * step / this.count;
                double cy = y0 + (y1 - y0) * step / this.count;
                double cz = z0 + (z1 - z0) * step / this.count;
                double size = random.nextDouble() * this.count / 16.0;
                double width = (OldMath.sin(step * (float) Math.PI / this.count) + 1.0F) * size + 1.0;
                double height = (OldMath.sin(step * (float) Math.PI / this.count) + 1.0F) * size + 1.0;
                int minX = OldMath.floor(cx - width / 2.0);
                int minY = OldMath.floor(cy - height / 2.0);
                int minZ = OldMath.floor(cz - width / 2.0);
                int maxX = OldMath.floor(cx + width / 2.0);
                int maxY = OldMath.floor(cy + height / 2.0);
                int maxZ = OldMath.floor(cz + width / 2.0);

                for (int bx = minX; bx <= maxX; bx++) {
                    double dx = (bx + 0.5 - cx) / (width / 2.0);
                    if (dx * dx < 1.0) {
                        for (int by = minY; by <= maxY; by++) {
                            double dy = (by + 0.5 - cy) / (height / 2.0);
                            if (dx * dx + dy * dy < 1.0) {
                                for (int bz = minZ; bz <= maxZ; bz++) {
                                    double dz = (bz + 0.5 - cz) / (width / 2.0);
                                    if (dx * dx + dy * dy + dz * dz < 1.0 && world.getBlockId(bx, by, bz) == this.target) {
                                        world.setBlock(bx, by, bz, this.blockId, this.blockMeta);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return true;
        }
    }

    /** WorldGenSand: a disc of sand or gravel replacing dirt and grass under water. */
    public static final class Sand extends OldGen {
        private final int blockId;
        private final int radius;

        public Sand(int radius, int blockId) {
            this.blockId = blockId;
            this.radius = radius;
        }

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            if (world.material(x, y, z) != Material.WATER) {
                return false;
            }
            int r = random.nextInt(this.radius - 2) + 2;
            for (int bx = x - r; bx <= x + r; bx++) {
                for (int bz = z - r; bz <= z + r; bz++) {
                    int dx = bx - x;
                    int dz = bz - z;
                    if (dx * dx + dz * dz <= r * r) {
                        for (int by = y - 2; by <= y + 2; by++) {
                            int id = world.getBlockId(bx, by, bz);
                            if (id == 3 || id == 2) {
                                world.setBlock(bx, by, bz, this.blockId, 0);
                            }
                        }
                    }
                }
            }
            return true;
        }
    }

    /** WorldGenClay: a disc of clay replacing dirt under water. */
    public static final class Clay extends OldGen {
        private final int size;

        public Clay(int size) {
            this.size = size;
        }

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            if (world.material(x, y, z) != Material.WATER) {
                return false;
            }
            int r = random.nextInt(this.size - 2) + 2;
            for (int bx = x - r; bx <= x + r; bx++) {
                for (int bz = z - r; bz <= z + r; bz++) {
                    int dx = bx - x;
                    int dz = bz - z;
                    if (dx * dx + dz * dz <= r * r) {
                        for (int by = y - 1; by <= y + 1; by++) {
                            int id = world.getBlockId(bx, by, bz);
                            if (id == 3 || id == 82) {
                                world.setBlock(bx, by, bz, 82, 0);
                            }
                        }
                    }
                }
            }
            return true;
        }
    }
}
