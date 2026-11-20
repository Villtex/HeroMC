package twilightforest.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo.Material;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/** The small generators of the Twilight Forest decoration: roots, berries, mycelium, fire jets, penguins, plants. */
public final class TFPlants {
    private TFPlants() {
    }

    /** TFGenPlantRoots: root strands (plant 14) hanging from the ground down to y 6. */
    public static final class PlantRoots extends TFGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            int copyX = x;
            int copyZ = z;
            while (y > 5) {
                if (world.isAirBlock(x, y, z) && canPlaceRootBelow(world, x, y + 1, z) && rand.nextInt(6) > 0) {
                    world.setBlock(x, y, z, PLANT, 14);
                } else {
                    x = copyX + rand.nextInt(4) - rand.nextInt(4);
                    z = copyZ + rand.nextInt(4) - rand.nextInt(4);
                }
                y--;
            }
            return true;
        }
    }

    /** TFGenHangBerries: one torchberry plant (plant 13) under the ground or a root strand. */
    public static final class HangBerries extends TFGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            int copyX = x;
            int copyZ = z;
            while (y > 5) {
                if (world.isAirBlock(x, y, z) && canPlaceRootBelow(world, x, y + 1, z) && rand.nextInt(6) > 0) {
                    world.setBlock(x, y, z, PLANT, 13);
                    break;
                }
                x = copyX + rand.nextInt(4) - rand.nextInt(4);
                z = copyZ + rand.nextInt(4) - rand.nextInt(4);
                y--;
            }
            return true;
        }
    }

    /** TFGenWoodRoots: root lines through stone, with liveroot balls now and then. */
    public static final class WoodRoots extends TFGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            if (world.getBlockId(x, y, z) != 1) {
                return false;
            }
            float length = rand.nextFloat() * 6.0F + rand.nextFloat() * 6.0F + 4.0F;
            if (length > y) {
                length = y;
            }
            float tilt = 0.6F + rand.nextFloat() * 0.3F;
            return drawRoot(world, rand, x, y, z, length, rand.nextFloat(), tilt);
        }

        private static boolean drawRoot(OldWorld world, Random rand, int x, int y, int z, float length, float angle, float tilt) {
            int[] dest = translate(x, y, z, length, angle, tilt);
            int limit = 8;
            if (x + limit < dest[0]) {
                dest[0] = x + limit;
            }
            if (x - limit > dest[0]) {
                dest[0] = x - limit;
            }
            if (z + limit < dest[2]) {
                dest[2] = z + limit;
            }
            if (z - limit > dest[2]) {
                dest[2] = z - limit;
            }
            if (world.getBlockId(dest[0], dest[1], dest[2]) != 1) {
                return false;
            }
            drawBresehnam(world, x, y, z, dest[0], dest[1], dest[2], ROOT, 0, true);
            if (length > 8.0F && rand.nextInt(3) > 0) {
                int[] nextSrc = translate(x, y, z, length / 2.0F, angle, tilt);
                float nextAngle = (angle + 0.25F + rand.nextFloat() * 0.5F) % 1.0F;
                float nextTilt = 0.6F + rand.nextFloat() * 0.3F;
                drawRoot(world, rand, nextSrc[0], nextSrc[1], nextSrc[2], length / 2.0F, nextAngle, nextTilt);
            }
            if (length > 6.0F && rand.nextInt(4) == 0) {
                int[] ball = translate(x, y, z, length / 2.0F, angle, tilt);
                int[] ballDest = translate(ball[0], ball[1], ball[2], 1.5, (angle + 0.5F) % 1.0F, 0.75);
                world.setBlock(ball[0], ball[1], ball[2], ROOT, 1);
                world.setBlock(ball[0], ball[1], ballDest[2], ROOT, 1);
                world.setBlock(ballDest[0], ball[1], ball[2], ROOT, 1);
                world.setBlock(ballDest[0], ball[1], ballDest[2], ROOT, 1);
                world.setBlock(ball[0], ballDest[1], ball[2], ROOT, 1);
                world.setBlock(ball[0], ballDest[1], ballDest[2], ROOT, 1);
                world.setBlock(ballDest[0], ballDest[1], ball[2], ROOT, 1);
                world.setBlock(ballDest[0], ballDest[1], ballDest[2], ROOT, 1);
            }
            return true;
        }
    }

    /** TFGenMyceliumBlob: dirt and grass in a disc of up to 4 become mycelium. */
    public static final class MyceliumBlob extends TFGen {
        private final int numberOfBlocks;

        public MyceliumBlob(int numberOfBlocks) {
            this.numberOfBlocks = numberOfBlocks;
        }

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            int l = random.nextInt(this.numberOfBlocks - 2) + 2;
            int i1 = 1;
            for (int bx = x - l; bx <= x + l; bx++) {
                for (int bz = z - l; bz <= z + l; bz++) {
                    int dx = bx - x;
                    int dz = bz - z;
                    if (dx * dx + dz * dz <= l * l) {
                        for (int by = y - i1; by <= y + i1; by++) {
                            int id = world.getBlockId(bx, by, bz);
                            if (id == 3 || id == 2) {
                                world.setBlock(bx, by, bz, 110, 0);
                            }
                        }
                    }
                }
            }
            return true;
        }
    }

    /** TFGenFireJet: a fire jet (or smoker) on grass under the open sky, over a small pool of lava in stone. */
    public static final class FireJet extends TFGen {
        private final int blockId;
        private final int meta;

        public FireJet(int blockId, int meta) {
            this.blockId = blockId;
            this.meta = meta;
        }

        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            for (int i = 0; i < 4; i++) {
                int dx = x + rand.nextInt(8) - rand.nextInt(8);
                int dy = y + rand.nextInt(4) - rand.nextInt(4);
                int dz = z + rand.nextInt(8) - rand.nextInt(8);
                if (world.isAirBlock(dx, dy, dz) && world.canBlockSeeTheSky(dx, dy, dz) && world.material(dx, dy - 1, dz) == Material.GRASS
                        && world.material(dx + 1, dy - 1, dz) == Material.GRASS && world.material(dx - 1, dy - 1, dz) == Material.GRASS
                        && world.material(dx, dy - 1, dz + 1) == Material.GRASS && world.material(dx, dy - 1, dz - 1) == Material.GRASS) {
                    world.setBlock(dx, dy - 1, dz, this.blockId, this.meta);
                    for (int rx = -2; rx <= 2; rx++) {
                        for (int rz = -2; rz <= 2; rz++) {
                            if (rx != 1 && rx != 0 && rx != -1 || rz != 1 && rz != 0 && rz != -1) {
                                if (world.material(dx + rx, dy - 2, dz + rz) != Material.LAVA) {
                                    world.setBlock(dx + rx, dy - 2, dz + rz, 1, 0);
                                }
                            } else {
                                world.setBlock(dx + rx, dy - 2, dz + rz, 10, 0);
                            }
                            world.setBlock(dx + rx, dy - 3, dz + rz, 1, 0);
                        }
                    }
                }
            }
            return true;
        }
    }

    /** TFGenPenguins: ten penguins around (x, z) on the glacier (the mobs come with Teil 24c). */
    public static final class Penguins extends TFGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            for (int i = 0; i < 10; i++) {
                int dx = x + rand.nextInt(8) - rand.nextInt(8);
                int dz = z + rand.nextInt(8) - rand.nextInt(8);
                int dy = world.getTopSolidOrLiquidBlock(dx, dz);
                float yaw = rand.nextFloat() * 360.0F;
                TFMobs.spawn(world, "Penguin", dx, dy, dz, yaw);
            }
            return true;
        }
    }

    /**
     * WorldGenTallGrass for a plant of the Twilight Forest (mayapple 4, fiddlehead 8, mushgloom 9). BlockTFPlant asks
     * canBlockStay with the metadata of the air it is placed in (0): every kind needs grass, dirt or farmland and a
     * light of at least 3 or the open sky.
     */
    public static final class TallPlant extends TFGen {
        private final int meta;

        public TallPlant(int meta) {
            this.meta = meta;
        }

        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            while (true) {
                int id = world.getBlockId(x, y, z);
                if (id != 0 && !hexxit.worldgen152.core.populate.OldBlockInfo.leaves(id) || --y <= 0) {
                    break;
                }
            }
            for (int i = 0; i < 128; i++) {
                int bx = x + rand.nextInt(8) - rand.nextInt(8);
                int by = y + rand.nextInt(4) - rand.nextInt(4);
                int bz = z + rand.nextInt(8) - rand.nextInt(8);
                if (world.isAirBlock(bx, by, bz) && plantCanStay(world, bx, by, bz)) {
                    world.setBlock(bx, by, bz, PLANT, this.meta);
                }
            }
            return true;
        }
    }

    /** BlockTFPlant.canBlockStay for metadata 0. */
    static boolean plantCanStay(OldWorld world, int x, int y, int z) {
        int soil = world.getBlockId(x, y - 1, z);
        return (world.skyLight(x, y, z) >= 3 || world.canBlockSeeTheSky(x, y, z)) && (soil == 2 || soil == 3 || soil == 60);
    }
}
