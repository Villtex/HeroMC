package hexxit.worldgen152.core.populate;

import hexxit.worldgen152.core.populate.OldBlockInfo.Material;

import java.util.Random;

/** Flowers, mushrooms, grass and the other small plants, with the placement rules of their 1.5.2 blocks. */
public final class OldPlantGens {
    private OldPlantGens() {
    }

    // ---- the blocks' own rules ----

    /** BlockFlower.canBlockStay for flowers and tall grass: light or open sky, on grass, dirt or farmland. */
    public static boolean flowerCanStay(OldWorld world, int x, int y, int z) {
        int soil = world.getBlockId(x, y - 1, z);
        return (soil == 2 || soil == 3 || soil == 60) && (world.skyLight(x, y, z) >= 8 || world.canBlockSeeTheSky(x, y, z));
    }

    /** Dead bushes are flowers that only accept sand. */
    public static boolean deadBushCanStay(OldWorld world, int x, int y, int z) {
        return world.getBlockId(x, y - 1, z) == 12 && (world.skyLight(x, y, z) >= 8 || world.canBlockSeeTheSky(x, y, z));
    }

    /** BlockMushroom.canBlockStay: on mycelium anywhere, otherwise in dim light on a solid block. */
    public static boolean mushroomCanStay(OldWorld world, int x, int y, int z) {
        if (y < 0 || y >= OldWorld.HEIGHT) {
            return false;
        }
        int soil = world.getBlockId(x, y - 1, z);
        if (soil == 0 || soil != 110 && world.skyLight(x, y, z) >= 13) {
            return false;
        }
        return OldBlockInfo.opaqueCube(soil) || OldBlockInfo.normalCube(soil);
    }

    /** BlockReed.canBlockStay: on reed, or on grass, dirt or sand that touches water. */
    public static boolean reedCanStay(OldWorld world, int x, int y, int z) {
        int soil = world.getBlockId(x, y - 1, z);
        if (soil == 83) {
            return true;
        }
        if (soil != 2 && soil != 3 && soil != 12) {
            return false;
        }
        return world.material(x - 1, y - 1, z) == Material.WATER || world.material(x + 1, y - 1, z) == Material.WATER
                || world.material(x, y - 1, z - 1) == Material.WATER || world.material(x, y - 1, z + 1) == Material.WATER;
    }

    /** BlockCactus.canBlockStay: nothing solid beside it, on cactus or sand. */
    public static boolean cactusCanStay(OldWorld world, int x, int y, int z) {
        if (world.material(x - 1, y, z).solid || world.material(x + 1, y, z).solid
                || world.material(x, y, z - 1).solid || world.material(x, y, z + 1).solid) {
            return false;
        }
        int soil = world.getBlockId(x, y - 1, z);
        return soil == 81 || soil == 12;
    }

    // ---- generators ----

    /** WorldGenFlowers for yellow flowers (37), roses (38) and the small mushrooms (39, 40). */
    public static final class Flowers extends OldGen {
        private final int blockId;

        public Flowers(int blockId) {
            this.blockId = blockId;
        }

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            boolean mushroom = this.blockId == 39 || this.blockId == 40;
            for (int i = 0; i < 64; i++) {
                int bx = x + random.nextInt(8) - random.nextInt(8);
                int by = y + random.nextInt(4) - random.nextInt(4);
                int bz = z + random.nextInt(8) - random.nextInt(8);
                if (world.isAirBlock(bx, by, bz)
                        && (mushroom ? mushroomCanStay(world, bx, by, bz) : flowerCanStay(world, bx, by, bz))) {
                    world.setBlock(bx, by, bz, this.blockId, 0);
                }
            }
            return true;
        }
    }

    /** WorldGenTallGrass: metadata 1 is grass, 2 fern. */
    public static final class TallGrass extends OldGen {
        private final int meta;

        public TallGrass(int meta) {
            this.meta = meta;
        }

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            y = skipAirAndLeaves(world, x, y, z);
            for (int i = 0; i < 128; i++) {
                int bx = x + random.nextInt(8) - random.nextInt(8);
                int by = y + random.nextInt(4) - random.nextInt(4);
                int bz = z + random.nextInt(8) - random.nextInt(8);
                if (world.isAirBlock(bx, by, bz) && flowerCanStay(world, bx, by, bz)) {
                    world.setBlock(bx, by, bz, 31, this.meta);
                }
            }
            return true;
        }
    }

    /** WorldGenDeadBush. */
    public static final class DeadBush extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            y = skipAirAndLeaves(world, x, y, z);
            for (int i = 0; i < 4; i++) {
                int bx = x + random.nextInt(8) - random.nextInt(8);
                int by = y + random.nextInt(4) - random.nextInt(4);
                int bz = z + random.nextInt(8) - random.nextInt(8);
                if (world.isAirBlock(bx, by, bz) && deadBushCanStay(world, bx, by, bz)) {
                    world.setBlock(bx, by, bz, 32, 0);
                }
            }
            return true;
        }
    }

    /** The descent both grass generators and the shrub start with: down through air and leaves. */
    static int skipAirAndLeaves(OldWorld world, int x, int y, int z) {
        while (true) {
            int id = world.getBlockId(x, y, z);
            if (id != 0 && !OldBlockInfo.leaves(id) || --y <= 0) {
                return y;
            }
        }
    }

    /** WorldGenWaterlily. */
    public static final class Waterlily extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            for (int i = 0; i < 10; i++) {
                int bx = x + random.nextInt(8) - random.nextInt(8);
                int by = y + random.nextInt(4) - random.nextInt(4);
                int bz = z + random.nextInt(8) - random.nextInt(8);
                if (world.isAirBlock(bx, by, bz) && by >= 0 && by < OldWorld.HEIGHT
                        && world.material(bx, by - 1, bz) == Material.WATER && world.getBlockMetadata(bx, by - 1, bz) == 0) {
                    world.setBlock(bx, by, bz, 111, 0);
                }
            }
            return true;
        }
    }

    /** WorldGenReed. */
    public static final class Reed extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            for (int i = 0; i < 20; i++) {
                int bx = x + random.nextInt(4) - random.nextInt(4);
                int bz = z + random.nextInt(4) - random.nextInt(4);
                if (world.isAirBlock(bx, y, bz)
                        && (world.material(bx - 1, y - 1, bz) == Material.WATER || world.material(bx + 1, y - 1, bz) == Material.WATER
                        || world.material(bx, y - 1, bz - 1) == Material.WATER || world.material(bx, y - 1, bz + 1) == Material.WATER)) {
                    int height = 2 + random.nextInt(random.nextInt(3) + 1);
                    for (int h = 0; h < height; h++) {
                        if (reedCanStay(world, bx, y + h, bz)) {
                            world.setBlock(bx, y + h, bz, 83, 0);
                        }
                    }
                }
            }
            return true;
        }
    }

    /** WorldGenPumpkin: metadata is the direction the face looks. */
    public static final class Pumpkin extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            for (int i = 0; i < 64; i++) {
                int bx = x + random.nextInt(8) - random.nextInt(8);
                int by = y + random.nextInt(4) - random.nextInt(4);
                int bz = z + random.nextInt(8) - random.nextInt(8);
                if (world.isAirBlock(bx, by, bz) && world.getBlockId(bx, by - 1, bz) == 2) {
                    world.setBlock(bx, by, bz, 86, random.nextInt(4));
                }
            }
            return true;
        }
    }

    /** WorldGenCactus. */
    public static final class Cactus extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            for (int i = 0; i < 10; i++) {
                int bx = x + random.nextInt(8) - random.nextInt(8);
                int by = y + random.nextInt(4) - random.nextInt(4);
                int bz = z + random.nextInt(8) - random.nextInt(8);
                if (world.isAirBlock(bx, by, bz)) {
                    int height = 1 + random.nextInt(random.nextInt(3) + 1);
                    for (int h = 0; h < height; h++) {
                        if (cactusCanStay(world, bx, by + h, bz)) {
                            world.setBlock(bx, by + h, bz, 81, 0);
                        }
                    }
                }
            }
            return true;
        }
    }

    /** WorldGenVines: climbs from y to 127, hanging a vine wherever a wall is next to air. */
    public static final class Vines extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            int startX = x;
            int startZ = z;
            for (; y < 128; y++) {
                if (world.isAirBlock(x, y, z)) {
                    // sides 2..5 of the original: the wall is to the south, north, east, west
                    if (OldBlockInfo.holdsVines(world.getBlockId(x, y, z + 1))) {
                        world.setBlock(x, y, z, 106, 1);
                    } else if (OldBlockInfo.holdsVines(world.getBlockId(x, y, z - 1))) {
                        world.setBlock(x, y, z, 106, 4);
                    } else if (OldBlockInfo.holdsVines(world.getBlockId(x + 1, y, z))) {
                        world.setBlock(x, y, z, 106, 8);
                    } else if (OldBlockInfo.holdsVines(world.getBlockId(x - 1, y, z))) {
                        world.setBlock(x, y, z, 106, 2);
                    }
                } else {
                    x = startX + random.nextInt(4) - random.nextInt(4);
                    z = startZ + random.nextInt(4) - random.nextInt(4);
                }
            }
            return true;
        }
    }
}
