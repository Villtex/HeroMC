package extrabiomes.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldBlockInfo.Material;
import hexxit.worldgen152.core.populate.OldGen;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

import static extrabiomes.gen.EbxlIds.*;

/**
 * The small generators of ExtrabiomesXL 3.13.4 (flowers, grass, cattails, leaf piles, marsh, oasis, melons,
 * quicksand) and the placement rules of the blocks they place ({@code canBlockStay}).
 */
public final class EbxlPlantGens {
    private EbxlPlantGens() {
    }

    // ---- the blocks' own rules ----

    private static boolean light(OldWorld world, int x, int y, int z) {
        return world.skyLight(x, y, z) >= 8 || world.canBlockSeeTheSky(x, y, z);
    }

    /** {@code (byte) id == mountainridge.topBlock}: the original compared the low byte only. */
    static boolean isRedRock(int id) {
        return (byte) id == (byte) RED_ROCK;
    }

    /** BlockCustomFlower.canBlockStay. */
    public static boolean flowerCanStay(OldWorld world, int x, int y, int z) {
        int soil = world.getBlockId(x, y - 1, z);
        return light(world, x, y, z) && (soil == 2 || soil == 3 || soil == 60 || soil == 12 || isRedRock(soil));
    }

    /** BlockCustomTallGrass.canBlockStay (BlockFlower with red rock and cracked sand as extra soil). */
    public static boolean grassCanStay(OldWorld world, int x, int y, int z) {
        int soil = world.getBlockId(x, y - 1, z);
        return light(world, x, y, z) && (soil == 2 || soil == 3 || soil == 60 || isRedRock(soil) || (byte) soil == (byte) CRACKED_SAND);
    }

    /** BlockCatTail.canBlockStay: on grass or dirt that touches water. */
    public static boolean cattailCanStay(OldWorld world, int x, int y, int z) {
        int soil = world.getBlockId(x, y - 1, z);
        if (soil != 2 && soil != 3) {
            return false;
        }
        y--;
        for (int offset = -1; offset < 2; offset += 2) {
            if (world.material(x + offset, y, z) == Material.WATER || world.material(x, y, z + offset) == Material.WATER) {
                return true;
            }
        }
        return false;
    }

    /** BlockLeafPile.canBlockStay: on grass or dirt. */
    public static boolean leafPileCanStay(OldWorld world, int x, int y, int z) {
        int soil = world.getBlockId(x, y - 1, z);
        return soil == 2 || soil == 3;
    }

    /** {@code while ((block == null || block.isLeaves()) && y > 0) y--}. */
    private static int downThroughAirAndLeaves(OldWorld world, int x, int y, int z) {
        int id;
        while (((id = world.getBlockId(x, y, z)) == 0 || OldBlockInfo.leaves(id)) && y > 0) {
            y--;
        }
        return y;
    }

    // ---- generators ----

    /** WorldGenTallGrass with an ExtrabiomesXL grass (brown, dead, ...). */
    public static final class TallGrass extends OldGen {
        private final int meta;

        public TallGrass(int meta) {
            this.meta = meta;
        }

        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            while (true) {
                int id = world.getBlockId(x, y, z);
                if (id != 0 && !OldBlockInfo.leaves(id) || --y <= 0) {
                    break;
                }
            }
            for (int i = 0; i < 128; i++) {
                int x1 = x + rand.nextInt(8) - rand.nextInt(8);
                int y1 = y + rand.nextInt(4) - rand.nextInt(4);
                int z1 = z + rand.nextInt(8) - rand.nextInt(8);
                if (world.isAirBlock(x1, y1, z1) && grassCanStay(world, x1, y1, z1)) {
                    world.setBlock(x1, y1, z1, GRASS, this.meta);
                }
            }
            return true;
        }
    }

    /** WorldGenMetadataFlowers. */
    public static final class Flowers extends OldGen {
        private final int meta;

        public Flowers(int meta) {
            this.meta = meta;
        }

        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            for (int i = 0; i < 64; i++) {
                int x1 = x + rand.nextInt(8) - rand.nextInt(8);
                int y1 = y + rand.nextInt(4) - rand.nextInt(4);
                int z1 = z + rand.nextInt(8) - rand.nextInt(8);
                if (world.isAirBlock(x1, y1, z1) && flowerCanStay(world, x1, y1, z1)) {
                    world.setBlock(x1, y1, z1, FLOWER, this.meta);
                }
            }
            return true;
        }
    }

    /** WorldGenRoot: short stacks of roots. */
    public static final class Root extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            for (int i = 0; i < 10; i++) {
                int x1 = x + rand.nextInt(8) - rand.nextInt(8);
                int y1 = y + rand.nextInt(4) - rand.nextInt(4);
                int z1 = z + rand.nextInt(8) - rand.nextInt(8);
                if (world.isAirBlock(x1, y1, z1)) {
                    int height = 1 + rand.nextInt(rand.nextInt(3) + 1);
                    for (int dy = 0; dy < height; dy++) {
                        if (flowerCanStay(world, x1, y1 + dy, z1)) {
                            world.setBlock(x1, y1 + dy, z1, FLOWER, FLOWER_ROOT);
                        }
                    }
                }
            }
            return true;
        }
    }

    /** WorldGenTinyCactus: on sand and red rock, without asking the block. */
    public static final class TinyCactus extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            for (int i = 0; i < 10; i++) {
                int x1 = x + rand.nextInt(8) - rand.nextInt(8);
                int y1 = y + rand.nextInt(4) - rand.nextInt(4);
                int z1 = z + rand.nextInt(8) - rand.nextInt(8);
                if (world.isAirBlock(x1, y1, z1)) {
                    int height = 1 + rand.nextInt(rand.nextInt(3) + 1);
                    for (int dy = 0; dy < height; dy++) {
                        int id = world.getBlockId(x1, y1 + dy - 1, z1);
                        if (id == 12 || isRedRock(id)) {
                            world.setBlock(x1, y1 + dy, z1, FLOWER, FLOWER_TINY_CACTUS);
                        }
                    }
                }
            }
            return true;
        }
    }

    /** WorldGenCatTail: cattails next to water. */
    public static final class CatTail extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            y = downThroughAirAndLeaves(world, x, y, z) + 1;
            for (int i = 0; i < 20; i++) {
                int x1 = x + rand.nextInt(4) - rand.nextInt(4);
                int z1 = z + rand.nextInt(4) - rand.nextInt(4);
                if (world.isAirBlock(x1, y, z1)
                        && (world.material(x1 - 1, y - 1, z1) == Material.WATER || world.material(x1 + 1, y - 1, z1) == Material.WATER
                        || world.material(x1, y - 1, z1 - 1) == Material.WATER || world.material(x1, y - 1, z1 + 1) == Material.WATER)) {
                    int height = 1 + rand.nextInt(rand.nextInt(1) + 1);
                    for (int dy = 0; dy < height; dy++) {
                        if (cattailCanStay(world, x1, y + dy, z1)) {
                            world.setBlock(x1, y + dy, z1, CATTAIL);
                        }
                    }
                }
            }
            return true;
        }
    }

    /** WorldGenLeafPile. */
    public static final class LeafPile extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            y = downThroughAirAndLeaves(world, x, y, z);
            for (int i = 0; i < 128; i++) {
                int x1 = x + rand.nextInt(8) - rand.nextInt(8);
                int y1 = y + rand.nextInt(4) - rand.nextInt(4);
                int z1 = z + rand.nextInt(8) - rand.nextInt(8);
                if (world.isAirBlock(x1, y1, z1) && leafPileCanStay(world, x1, y1, z1)) {
                    world.setBlock(x1, y1, z1, LEAF_PILE);
                }
            }
            return true;
        }
    }

    /**
     * Block.dirt.canPlaceBlockOnSide: the block there is replaceable (water). The metadata
     * {@code 1 << Direction.facingToDirection[Facing.oppositeSide[2]]} is 1, as side 2 always fits first.
     */
    private static boolean replaceableSolidCheck(OldWorld world, int x, int y, int z) {
        int id = world.getBlockId(x, y, z);
        return id == 0 || OldBlockInfo.material(id).replaceable;
    }

    /** WorldGenMarshGrass (blockId 2) and WorldGenMarshDirt (blockId 3): fill the water of a column below sea level. */
    public static final class Marsh extends OldGen {
        private final int blockId;
        private final int top;

        public Marsh(boolean grass) {
            this.blockId = grass ? 2 : 3;
            this.top = grass ? 63 : 62;
        }

        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            int x1 = x;
            int z1 = z;
            for (int y1 = y; y1 < this.top; y1++) {
                if (!world.isAirBlock(x1, y1, z1)) {
                    if (replaceableSolidCheck(world, x1, y1, z1)) {
                        world.setBlock(x1, y1, z1, this.blockId, 1);
                    }
                } else if (this.blockId == 2) {
                    x1 = x + rand.nextInt(4) - rand.nextInt(4);
                    z1 = z + rand.nextInt(4) - rand.nextInt(4);
                } else {
                    x1 = x + 16;
                    z1 = z + 16;
                }
            }
            return true;
        }
    }

    /** WorldGenOasis: grass around water in the Mountain Ridge. */
    public static final class Oasis extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            if (world.material(x, y, z) != Material.WATER) {
                return false;
            }
            int radius = rand.nextInt(5) + 2;
            for (int x1 = x - radius; x1 <= x + radius; x1++) {
                for (int z1 = z - radius; z1 <= z + radius; z1++) {
                    int a = x1 - x;
                    int b = z1 - z;
                    if (a * a + b * b <= radius * radius) {
                        for (int y1 = y - 2; y1 <= y + 2; y1++) {
                            int id = world.getBlockId(x1, y1, z1);
                            // the original also listed the ridge's top block, but compared the int id with a byte (-2): never true
                            if (id == 1 || id == 12 || id == 24) {
                                world.setBlock(x1, y1, z1, 2);
                            }
                        }
                    }
                }
            }
            return true;
        }
    }

    /** WorldGenMelon. */
    public static final class Melon extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            for (int i = 0; i < 64; i++) {
                int x1 = x + rand.nextInt(8) - rand.nextInt(8);
                int y1 = y + rand.nextInt(4) - rand.nextInt(4);
                int z1 = z + rand.nextInt(8) - rand.nextInt(8);
                // Block.pumpkin.canPlaceBlockAt adds nothing on air above grass
                if (world.isAirBlock(x1, y1, z1) && world.getBlockId(x1, y1 - 1, z1) == 2) {
                    world.setBlock(x1, y1, z1, 103);
                }
            }
            return true;
        }
    }

    /** WorldGenQuicksand: a 3x3x3 pit of quicksand in grass or sand. */
    public static final class Quicksand extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            while ((world.isAirBlock(x, y, z) || !OldBlockInfo.normalCube(world.getBlockId(x, y, z))) && y > 2) {
                y--;
            }
            int id = world.getBlockId(x, y, z);
            if (id != 2 && id != 12) {
                return false;
            }
            for (int x1 = -2; x1 <= 2; x1++) {
                for (int z1 = -2; z1 <= 2; z1++) {
                    if (world.isAirBlock(x + x1, y - 1, z + z1) && world.isAirBlock(x + x1, y - 2, z + z1)) {
                        return false;
                    }
                }
            }
            for (int x1 = -1; x1 <= 1; x1++) {
                for (int z1 = -1; z1 <= 1; z1++) {
                    for (int y1 = -2; y1 <= 0; y1++) {
                        world.setBlock(x + x1, y + y1, z + z1, QUICKSAND);
                    }
                }
            }
            return true;
        }
    }
}
