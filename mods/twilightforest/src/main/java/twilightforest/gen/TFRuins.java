package twilightforest.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo.Material;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/** The small features of TFBiomeDecorator.ruinList: stone circles, wells, huts, stalagmites, foundations, logs. */
public final class TFRuins {
    private static final int MOSSY = 48;
    private static final int FENCE = 85;
    private static final int WOOD_SLAB = 126;
    private static final int PLANKS = 5;
    private static final int WATER = 9;
    private static final int BRICK = 45;
    private static final int STONE_SLAB = 44;
    private static final int STONE_DOUBLE_SLAB = 43;
    private static final int STONE_BRICK = 98;

    private TFRuins() {
    }

    /** TFGenStoneCircle. */
    public static final class StoneCircle extends TFGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            if (!isAreaClear(world, x - 3, y, z - 3, 6, 4, 6)) {
                return false;
            }
            for (int cy = 0; cy <= 2; cy++) {
                putBlock(world, x - 3, y + cy, z, MOSSY, true);
                putBlock(world, x + 3, y + cy, z, MOSSY, true);
                putBlock(world, x, y + cy, z - 3, MOSSY, true);
                putBlock(world, x, y + cy, z + 3, MOSSY, true);
                putBlock(world, x - 2, y + cy, z - 2, MOSSY, true);
                putBlock(world, x + 2, y + cy, z - 2, MOSSY, true);
                putBlock(world, x - 2, y + cy, z + 2, MOSSY, true);
                putBlock(world, x + 2, y + cy, z + 2, MOSSY, true);
            }
            return true;
        }
    }

    /** TFGenWell: a 3×3 well (1 in 4 a 4×4 one) with its shaft of water, sometimes a basement chest at the bottom. */
    public static final class Well extends TFGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            return rand.nextInt(4) == 0 ? generate4x4(world, x, y, z) : generate3x3(world, rand, x, y, z);
        }

        private static boolean shaftContinues(OldWorld world, int x, int y, int z) {
            int id = world.getBlockId(x, y, z);
            return (id == 3 || id == 2 || id == 13 || id == 1) && world.material(x, y - 1, z).solid;
        }

        private static boolean generate3x3(OldWorld world, Random rand, int x, int y, int z) {
            if (!isAreaClear(world, x, y, z, 3, 4, 3)) {
                return false;
            }
            int[][] rim = {{0, 0}, {1, 0}, {2, 0}, {0, 2}, {1, 2}, {2, 2}, {0, 1}, {2, 1}};
            for (int[] p : rim) {
                putBlock(world, x + p[0], y, z + p[1], MOSSY, true);
            }
            putBlock(world, x + 1, y, z + 1, WATER, true);
            for (int dy = 1; dy <= 2; dy++) {
                putBlock(world, x, y + dy, z, FENCE, true);
                putBlock(world, x + 2, y + dy, z, FENCE, true);
                putBlock(world, x, y + dy, z + 2, FENCE, true);
                putBlock(world, x + 2, y + dy, z + 2, FENCE, true);
            }
            for (int[] p : rim) {
                putBlockAndMetadata(world, x + p[0], y + 3, z + p[1], WOOD_SLAB, 0, true);
            }
            putBlock(world, x + 1, y + 3, z + 1, PLANKS, true);
            boolean madeTreasure = false;
            for (int dy = -1; dy >= -20; dy--) {
                if (!shaftContinues(world, x + 1, y + dy, z + 1)) {
                    break;
                }
                putBlock(world, x + 1, y + dy, z + 1, WATER, true);
                if (dy < -15 && !madeTreasure && rand.nextInt(8) == 0) {
                    putBlock(world, x + 2, y + dy, z + 1, WATER, true);
                    putBlock(world, x + 3, y + dy + 1, z + 1, 0, true);
                    putBlock(world, x + 3, y + dy, z + 1, 0, true);
                    TFTreasure.BASEMENT.generate(world, x + 3, y + dy, z + 1);
                    madeTreasure = true;
                }
            }
            return true;
        }

        private static boolean generate4x4(OldWorld world, int x, int y, int z) {
            if (!isAreaClear(world, x, y, z, 4, 4, 4)) {
                return false;
            }
            int[][] rim = {{0, 0}, {1, 0}, {2, 0}, {3, 0}, {0, 3}, {1, 3}, {2, 3}, {3, 3}, {0, 1}, {0, 2}, {3, 1}, {3, 2}};
            for (int[] p : rim) {
                putBlock(world, x + p[0], y, z + p[1], MOSSY, true);
            }
            putBlock(world, x + 1, y, z + 1, WATER, true);
            putBlock(world, x + 2, y, z + 1, WATER, true);
            putBlock(world, x + 1, y, z + 2, WATER, true);
            putBlock(world, x + 2, y, z + 2, WATER, true);
            for (int dy = 1; dy <= 2; dy++) {
                putBlock(world, x, y + dy, z, FENCE, true);
                putBlock(world, x + 3, y + dy, z, FENCE, true);
                putBlock(world, x, y + dy, z + 3, FENCE, true);
                putBlock(world, x + 3, y + dy, z + 3, FENCE, true);
            }
            for (int[] p : rim) {
                putBlockAndMetadata(world, x + p[0], y + 3, z + p[1], WOOD_SLAB, 0, true);
            }
            putBlock(world, x + 1, y + 3, z + 1, PLANKS, true);
            putBlock(world, x + 2, y + 3, z + 1, PLANKS, true);
            putBlock(world, x + 1, y + 3, z + 2, PLANKS, true);
            putBlock(world, x + 2, y + 3, z + 2, PLANKS, true);
            for (int dx = 1; dx <= 2; dx++) {
                for (int dz = 1; dz <= 2; dz++) {
                    for (int dy = -1; dy >= -20; dy--) {
                        if (!shaftContinues(world, x + dx, y + dy, z + dz)) {
                            break;
                        }
                        putBlock(world, x + dx, y + dy, z + dz, WATER, true);
                    }
                }
            }
            return true;
        }
    }

    /** TFGenCaveStalactite: hanging (or standing) spikes of one block from a ceiling or floor. */
    public static class Stalactite extends TFGen {
        protected final int blockId;
        protected final boolean hang;
        private final float sizeFactor;
        private final int maxLength;
        private final int minHeight;

        public Stalactite(int blockId, float sizeFactor, boolean hang) {
            this(blockId, sizeFactor, -1, -1, hang);
        }

        public Stalactite(int blockId, float sizeFactor, int maxLength, int minHeight) {
            this(blockId, sizeFactor, maxLength, minHeight, true);
        }

        /** TFGenCaveStalactite.diamond ... glowstone: the ore spikes of the hollow hills. */
        private static final Stalactite DIAMOND = new Stalactite(56, 0.5F, 4, 16);
        private static final Stalactite LAPIS = new Stalactite(21, 0.8F, 8, 1);
        private static final Stalactite EMERALD = new Stalactite(129, 0.5F, 3, 12);
        private static final Stalactite GOLD = new Stalactite(14, 0.6F, 6, 1);
        private static final Stalactite REDSTONE = new Stalactite(73, 0.8F, 8, 1);
        private static final Stalactite IRON = new Stalactite(15, 0.7F, 8, 1);
        private static final Stalactite COAL = new Stalactite(16, 0.8F, 12, 1);
        private static final Stalactite GLOWSTONE = new Stalactite(89, 0.5F, 8, 1);

        /** TFGenCaveStalactite.makeRandomOreStalactite. */
        public static Stalactite makeRandomOreStalactite(Random rand, int hillSize) {
            if (hillSize >= 3 || hillSize >= 2 && rand.nextInt(5) == 0) {
                int s3 = rand.nextInt(13);
                if (s3 == 0 || s3 == 1) {
                    return DIAMOND;
                }
                if (s3 == 2 || s3 == 3) {
                    return LAPIS;
                }
                if (s3 == 4) {
                    return EMERALD;
                }
            }
            if (hillSize >= 2 || hillSize >= 1 && rand.nextInt(5) == 0) {
                int s2 = rand.nextInt(6);
                if (s2 == 0) {
                    return GOLD;
                }
                if (s2 == 1 || s2 == 2) {
                    return REDSTONE;
                }
            }
            int s1 = rand.nextInt(5);
            if (s1 == 0 || s1 == 1) {
                return IRON;
            }
            return s1 != 2 && s1 != 3 ? GLOWSTONE : COAL;
        }

        private Stalactite(int blockId, float sizeFactor, int maxLength, int minHeight, boolean hang) {
            this.blockId = blockId;
            this.sizeFactor = sizeFactor;
            this.maxLength = maxLength;
            this.minHeight = minHeight;
            this.hang = hang;
        }

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            int ceiling = Integer.MAX_VALUE;
            int floor = -1;
            for (int ty = y; ty < TFTerrain.WORLDHEIGHT; ty++) {
                Material m = world.material(x, ty, z);
                if (m != Material.AIR) {
                    if (m != Material.GROUND && m != Material.ROCK) {
                        return false;
                    }
                    ceiling = ty;
                    break;
                }
            }
            if (ceiling == Integer.MAX_VALUE) {
                return false;
            }
            for (int ty = y; ty > 4; ty--) {
                Material m = world.material(x, ty, z);
                if (m != Material.AIR) {
                    if (m != Material.GROUND && m != Material.ROCK && !this.hang && m != Material.WATER && m != Material.LAVA) {
                        return false;
                    }
                    floor = ty;
                    break;
                }
            }
            int length = (int) ((ceiling - floor) * this.sizeFactor * random.nextFloat());
            if (this.maxLength > -1 && length > this.maxLength) {
                length = this.maxLength;
            }
            if (this.minHeight > -1 && ceiling - floor - length < this.minHeight) {
                return false;
            }
            return this.makeSpike(world, random, x, this.hang ? ceiling : floor, z, length);
        }

        public boolean makeSpike(OldWorld world, Random random, int x, int y, int z, int maxLength) {
            int diameter = (int) (maxLength / 4.5);
            for (int dx = -diameter; dx <= diameter; dx++) {
                for (int dz = -diameter; dz <= diameter; dz++) {
                    int absx = Math.abs(dx);
                    int absz = Math.abs(dz);
                    int dist = (int) (Math.max(absx, absz) + Math.min(absx, absz) * 0.5);
                    int spikeLength = 0;
                    if (dist == 0) {
                        spikeLength = maxLength;
                    }
                    if (dist > 0) {
                        spikeLength = random.nextInt((int) (maxLength / (dist + 0.25)));
                    }
                    int dir = this.hang ? -1 : 1;
                    if (!world.material(x + dx, y - dir, z + dz).solid) {
                        spikeLength = 0;
                    }
                    for (int dy = 0; dy != spikeLength * dir; dy += dir) {
                        putBlock(world, x + dx, y + dy, z + dz, this.blockId, false);
                    }
                }
            }
            return true;
        }
    }

    /** TFGenOutsideStalagmite: a stone spike of 5 to 14 on open ground. */
    public static final class OutsideStalagmite extends Stalactite {
        public OutsideStalagmite() {
            super(1, 1.0F, false);
        }

        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            int length = rand.nextInt(10) + 5;
            return isAreaClear(world, x, y, z, 1, length, 1) && this.makeSpike(world, rand, x, y - 1, z, length);
        }
    }

    /** TFGenFoundation: the walls of a ruined house, half of them with a cellar and a basement chest. */
    public static final class Foundation extends TFGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            int sx = 5 + rand.nextInt(5);
            int sz = 5 + rand.nextInt(5);
            if (!isAreaClear(world, x, y, z, sx, 4, sz)) {
                return false;
            }
            for (int cx = 0; cx <= sx; cx++) {
                for (int cz = 0; cz <= sz; cz++) {
                    if (cx != 0 && cx != sx && cz != 0 && cz != sz) {
                        if (rand.nextInt(3) != 0) {
                            putBlock(world, x + cx, y - 1, z + cz, PLANKS, true);
                        }
                    } else {
                        int ht = rand.nextInt(4) + 1;
                        for (int cy = 0; cy <= ht; cy++) {
                            putBlock(world, x + cx, y + cy - 1, z + cz, randStone(rand, cy + 1), true);
                        }
                    }
                }
            }
            if (rand.nextInt(2) == 0) {
                for (int cx = 1; cx < sx; cx++) {
                    for (int cz = 1; cz < sz; cz++) {
                        putBlock(world, x + cx, y - 3, z + cz, 0, true);
                        putBlock(world, x + cx, y - 4, z + cz, 0, true);
                    }
                }
                int cx = rand.nextInt(sx - 1) + 1;
                int cz = rand.nextInt(sz - 1) + 1;
                TFTreasure.BASEMENT.generate(world, x + cx, y - 4, z + cz);
            }
            return true;
        }
    }

    /**
     * TFGenMonolith: four obsidian columns of different heights, lapis on top of the highest; two ravens spawn
     * nearby. The ravens are mobs of Teil 24c; their random numbers are drawn here already.
     */
    public static final class Monolith extends TFGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            int ht = rand.nextInt(10) + 10;
            int dir = rand.nextInt(4);
            if (!isAreaClear(world, x, y, z, 2, ht, 2)) {
                return false;
            }
            int h0;
            int h1;
            int h2;
            int h3;
            switch (dir) {
                case 0 -> {
                    h0 = ht;
                    h1 = (int) (ht * 0.75);
                    h2 = (int) (ht * 0.75);
                    h3 = (int) (ht * 0.5);
                }
                case 1 -> {
                    h0 = (int) (ht * 0.5);
                    h1 = ht;
                    h2 = (int) (ht * 0.75);
                    h3 = (int) (ht * 0.75);
                }
                case 2 -> {
                    h0 = (int) (ht * 0.75);
                    h1 = (int) (ht * 0.5);
                    h2 = ht;
                    h3 = (int) (ht * 0.75);
                }
                default -> {
                    h0 = (int) (ht * 0.75);
                    h1 = (int) (ht * 0.75);
                    h2 = (int) (ht * 0.5);
                    h3 = ht;
                }
            }
            column(world, x, y, z, h0, ht);
            column(world, x + 1, y, z, h1, ht);
            column(world, x, y, z + 1, h2, ht);
            column(world, x + 1, y, z + 1, h3, ht);
            for (int i = 0; i < 2; i++) {
                int dx = x + rand.nextInt(8) - rand.nextInt(8);
                int dz = z + rand.nextInt(8) - rand.nextInt(8);
                int dy = world.getTopSolidOrLiquidBlock(dx, dz);
                float yaw = rand.nextFloat() * 360.0F;
                TFMobs.spawn(world, "Forest Raven", dx, dy, dz, yaw);
            }
            return true;
        }

        private static void column(OldWorld world, int x, int y, int z, int h, int ht) {
            for (int cy = 0; cy <= h; cy++) {
                putBlock(world, x, y + cy - 1, z, cy == ht ? 22 : 49, true);
            }
        }
    }

    /** TFGenWitchHut: a tiny stone and brick hut with a fire on netherrack and a skeleton druid spawner. */
    public static final class WitchHut extends TFGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            if (!isAreaClear(world, x, y, z, 5, 7, 6)) {
                return false;
            }
            stone(world, rand, x + 1, y, z + 1, 1);
            stone(world, rand, x + 2, y, z + 1, 1);
            stone(world, rand, x + 3, y, z + 1, 1);
            stone(world, rand, x + 5, y, z + 1, 1);
            brick(world, x, y, z + 2);
            brick(world, x + 1, y, z + 2);
            stone(world, rand, x + 5, y, z + 2, 1);
            brick(world, x, y, z + 3);
            stone(world, rand, x + 5, y, z + 3, 1);
            brick(world, x, y, z + 4);
            brick(world, x + 1, y, z + 4);
            stone(world, rand, x + 5, y, z + 4, 1);
            stone(world, rand, x + 1, y, z + 5, 1);
            stone(world, rand, x + 2, y, z + 5, 1);
            stone(world, rand, x + 3, y, z + 5, 1);
            stone(world, rand, x + 5, y, z + 5, 1);

            stone(world, rand, x + 1, y + 1, z + 1, 2);
            stone(world, rand, x + 3, y + 1, z + 1, 2);
            stone(world, rand, x + 5, y + 1, z + 1, 2);
            brick(world, x, y + 1, z + 2);
            brick(world, x + 1, y + 1, z + 2);
            stone(world, rand, x + 5, y + 1, z + 2, 2);
            brick(world, x, y + 1, z + 3);
            brick(world, x, y + 1, z + 4);
            brick(world, x + 1, y + 1, z + 4);
            stone(world, rand, x + 5, y + 1, z + 4, 2);
            stone(world, rand, x + 1, y + 1, z + 5, 2);
            stone(world, rand, x + 3, y + 1, z + 5, 2);
            stone(world, rand, x + 5, y + 1, z + 5, 2);

            stone(world, rand, x + 1, y + 2, z + 1, 3);
            stone(world, rand, x + 2, y + 2, z + 1, 3);
            stone(world, rand, x + 3, y + 2, z + 1, 3);
            stone(world, rand, x + 4, y + 2, z + 1, 3);
            stone(world, rand, x + 5, y + 2, z + 1, 3);
            brick(world, x, y + 2, z + 2);
            brick(world, x + 1, y + 2, z + 2);
            stone(world, rand, x + 5, y + 2, z + 2, 3);
            brick(world, x, y + 2, z + 3);
            stone(world, rand, x + 5, y + 2, z + 3, 3);
            brick(world, x, y + 2, z + 4);
            brick(world, x + 1, y + 2, z + 4);
            stone(world, rand, x + 5, y + 2, z + 4, 1);
            stone(world, rand, x + 1, y + 2, z + 5, 3);
            stone(world, rand, x + 2, y + 2, z + 5, 3);
            stone(world, rand, x + 3, y + 2, z + 5, 3);
            stone(world, rand, x + 4, y + 2, z + 5, 3);
            stone(world, rand, x + 5, y + 2, z + 5, 3);

            brick(world, x, y + 3, z + 2);
            brick(world, x, y + 3, z + 3);
            brick(world, x, y + 3, z + 4);
            stone(world, rand, x + 2, y + 3, z + 1, 4);
            stone(world, rand, x + 3, y + 3, z + 1, 4);
            stone(world, rand, x + 4, y + 3, z + 1, 4);
            stone(world, rand, x + 2, y + 3, z + 5, 4);
            stone(world, rand, x + 3, y + 3, z + 5, 4);
            stone(world, rand, x + 4, y + 3, z + 5, 4);
            brick(world, x, y + 4, z + 3);
            stone(world, rand, x + 3, y + 4, z + 1, 5);
            stone(world, rand, x + 3, y + 4, z + 5, 5);
            brick(world, x, y + 5, z + 3);
            brick(world, x, y + 6, z + 3);

            // the roof: {dx, dy, dz, double slab (1) or slab (0)}
            int[][] roof = {
                    {0, 2, 0, 1}, {0, 2, 1, 1}, {0, 2, 5, 1}, {0, 2, 6, 1},
                    {6, 2, 0, 1}, {6, 2, 1, 1}, {6, 2, 2, 1}, {6, 2, 3, 1}, {6, 2, 4, 1}, {6, 2, 5, 1}, {6, 2, 6, 1},
                    {1, 3, 0, 1}, {1, 3, 1, 1}, {1, 3, 2, 1}, {1, 3, 4, 1}, {1, 3, 5, 1}, {1, 3, 6, 1},
                    {5, 3, 0, 1}, {5, 3, 1, 1}, {5, 3, 2, 1}, {5, 3, 3, 1}, {5, 3, 4, 1}, {5, 3, 5, 1}, {5, 3, 6, 1},
                    {1, 4, 0, 0}, {2, 4, 0, 1}, {2, 4, 1, 1}, {2, 4, 2, 1}, {2, 4, 3, 1}, {2, 4, 4, 1}, {2, 4, 5, 1}, {2, 4, 6, 1},
                    {1, 4, 6, 0}, {5, 4, 0, 0}, {4, 4, 0, 1}, {4, 4, 1, 1}, {4, 4, 2, 1}, {4, 4, 3, 1}, {4, 4, 4, 1}, {4, 4, 5, 1},
                    {4, 4, 6, 1}, {5, 4, 6, 0},
                    {2, 5, 0, 0}, {2, 5, 1, 0}, {4, 5, 0, 0}, {4, 5, 1, 0},
                    {3, 5, 0, 1}, {3, 5, 1, 1}, {3, 5, 2, 1}, {3, 5, 3, 1}, {3, 5, 4, 1}, {3, 5, 5, 1}, {3, 5, 6, 1},
                    {2, 5, 5, 0}, {2, 5, 6, 0}, {4, 5, 5, 0}, {4, 5, 6, 0},
                    {3, 6, 0, 1}, {3, 6, 1, 1}, {3, 6, 2, 0}, {3, 6, 4, 0}, {3, 6, 5, 1}, {3, 6, 6, 1},
                    {3, 7, 0, 0}, {3, 7, 6, 0}};
            for (int[] r : roof) {
                putBlockAndMetadata(world, x + r[0], y + r[1], z + r[2], r[3] == 1 ? STONE_DOUBLE_SLAB : STONE_SLAB, 2, true);
            }
            putBlock(world, x + 1, y - 1, z + 3, 87, true);
            putBlock(world, x + 1, y, z + 3, 51, true);
            world.setBlock(x + 3, y + 1, z + 3, 52, 0);
            world.setTile(x + 3, y + 1, z + 3, new OldTile.Spawner(TFTreasure.SPAWNER_PREFIX + "Skeleton Druid"));
            return true;
        }

        private static void stone(OldWorld world, Random rand, int x, int y, int z, int howMuch) {
            putBlock(world, x, y, z, randStone(rand, howMuch), true);
        }

        private static void brick(OldWorld world, int x, int y, int z) {
            putBlock(world, x, y, z, BRICK, true);
        }
    }

    /** TFGenGroveRuins: a large or a small arch of mossy stone bricks. */
    public static final class GroveRuins extends TFGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            return rand.nextBoolean() ? largeArch(world, x, y, z) : smallArch(world, x, y, z);
        }

        private static void brick(OldWorld world, int x, int y, int z, int meta) {
            world.setBlock(x, y, z, STONE_BRICK, meta);
        }

        private static boolean largeArch(OldWorld world, int x, int y, int z) {
            if (!isAreaClear(world, x, y, z, 2, 7, 6)) {
                return false;
            }
            for (int dy = -2; dy <= 7; dy++) {
                brick(world, x, y + dy, z + 1, 1);
                brick(world, x + 1, y + dy, z + 1, 1);
                brick(world, x, y + dy, z + 2, 1);
                brick(world, x + 1, y + dy, z + 2, 1);
            }
            int[][] rest = {{0, -1, 3}, {1, -1, 3}, {0, -2, 3}, {1, -2, 3}, {0, -1, 4}, {1, -1, 4}, {0, -2, 4}, {1, -2, 4}, {0, -1, 5},
                    {1, -2, 5}, {0, 6, 3}, {1, 6, 3}, {0, 7, 3}, {1, 7, 3}, {0, 6, 4}, {1, 6, 4}, {0, 7, 4}, {1, 7, 4}, {1, 7, 5}};
            for (int[] r : rest) {
                brick(world, x + r[0], y + r[1], z + r[2], 1);
            }
            brick(world, x, y + 5, z, 3);
            return true;
        }

        private static boolean smallArch(OldWorld world, int x, int y, int z) {
            if (!isAreaClear(world, x, y, z, 7, 5, 9)) {
                return false;
            }
            brick(world, x, y + 4, z, 3);
            brick(world, x, y + 3, z, 3);
            brick(world, x + 1, y + 4, z, 3);
            brick(world, x + 2, y + 4, z, 3);
            brick(world, x, y + 4, z + 1, 3);
            brick(world, x, y + 4, z + 2, 3);
            for (int dy = -1; dy <= 5; dy++) {
                brick(world, x + 3, y + dy, z, 1);
            }
            brick(world, x + 4, y - 1, z, 1);
            brick(world, x + 5, y - 1, z, 1);
            brick(world, x + 6, y - 1, z, 1);
            brick(world, x + 4, y + 5, z, 1);
            brick(world, x + 5, y + 5, z, 1);
            for (int dy = -1; dy <= 5; dy++) {
                brick(world, x, y + dy, z + 3, 1);
                brick(world, x, y + dy, z + 7, 1);
            }
            for (int dz = 4; dz < 7; dz++) {
                brick(world, x, y - 1, z + dz, 1);
                brick(world, x, y + 5, z + dz, 1);
            }
            brick(world, x, y + 4, z + 8, 3);
            return true;
        }
    }

    /** TFGenFallenHollowLog: a hollow log of four, mossy, with leaves and a firefly. */
    public static final class FallenHollowLog extends TFGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            return rand.nextBoolean() ? makeLog4Z(world, rand, x, y, z) : makeLog4X(world, rand, x, y, z);
        }

        private static boolean makeLog4Z(OldWorld world, Random rand, int x, int y, int z) {
            if (!isAreaClear(world, x, y, z, 9, 3, 4)) {
                return false;
            }
            int[][] jaggies = {{0, 0}, {3, 0}, {0, 1}, {3, 1}, {1, 2}, {2, 2}};
            for (int[] j : jaggies) {
                int length = rand.nextInt(3);
                for (int dz = -length; dz < 0; dz++) {
                    world.setBlock(x + j[0], y + j[1], z + dz + 3, LOG, 8);
                }
            }
            for (int[] j : jaggies) {
                int length = rand.nextInt(3);
                for (int dz = 0; dz < length; dz++) {
                    world.setBlock(x + j[0], y + j[1], z + dz + 7, LOG, 8);
                }
            }
            for (int dz = 0; dz < 4; dz++) {
                floor(world, rand, x + 1, y, z + dz + 3, 8);
                floor(world, rand, x + 2, y, z + dz + 3, 8);
                world.setBlock(x, y, z + dz + 3, LOG, 8);
                world.setBlock(x + 3, y, z + dz + 3, LOG, 8);
                world.setBlock(x, y + 1, z + dz + 3, LOG, 8);
                world.setBlock(x + 3, y + 1, z + dz + 3, LOG, 8);
                world.setBlock(x + 1, y + 2, z + dz + 3, LOG, 8);
                world.setBlock(x + 2, y + 2, z + dz + 3, LOG, 8);
                if (rand.nextBoolean()) {
                    world.setBlock(x + 1, y + 3, z + dz + 3, PLANT, 3);
                }
                if (rand.nextBoolean()) {
                    world.setBlock(x + 2, y + 3, z + dz + 3, PLANT, 3);
                }
            }
            int offZ = rand.nextInt(3) + 2;
            boolean plusX = rand.nextBoolean();
            for (int dz = 0; dz < 3; dz++) {
                if (rand.nextBoolean()) {
                    world.setBlock(x + (plusX ? 3 : 0), y + 2, z + dz + offZ, LEAVES, 0);
                    if (rand.nextBoolean()) {
                        world.setBlock(x + (plusX ? 3 : 0), y + 3, z + dz + offZ, LEAVES, 0);
                    }
                    if (rand.nextBoolean()) {
                        world.setBlock(x + (plusX ? 4 : -1), y + 2, z + dz + offZ, LEAVES, 0);
                    }
                }
            }
            world.setBlock(x + (plusX ? 0 : 3), y + 2, z + rand.nextInt(4) + 3, FIREFLY, 0);
            return true;
        }

        private static boolean makeLog4X(OldWorld world, Random rand, int x, int y, int z) {
            if (!isAreaClear(world, x, y, z, 4, 3, 9)) {
                return false;
            }
            int[][] jaggies = {{0, 0}, {3, 0}, {0, 1}, {3, 1}, {1, 2}, {2, 2}};
            for (int[] j : jaggies) {
                int length = rand.nextInt(3);
                for (int dx = -length; dx < 0; dx++) {
                    world.setBlock(x + 3 + dx, y + j[1], z + j[0], LOG, 4);
                }
            }
            for (int[] j : jaggies) {
                int length = rand.nextInt(3);
                for (int dx = 0; dx < length; dx++) {
                    world.setBlock(x + dx + 7, y + j[1], z + j[0], LOG, 4);
                }
            }
            for (int dx = 0; dx < 4; dx++) {
                floor(world, rand, x + dx + 3, y, z + 1, 4);
                floor(world, rand, x + dx + 3, y, z + 2, 4);
                world.setBlock(x + dx + 3, y, z, LOG, 4);
                world.setBlock(x + dx + 3, y, z + 3, LOG, 4);
                world.setBlock(x + dx + 3, y + 1, z, LOG, 4);
                world.setBlock(x + dx + 3, y + 1, z + 3, LOG, 4);
                world.setBlock(x + dx + 3, y + 2, z + 1, LOG, 4);
                world.setBlock(x + dx + 3, y + 2, z + 2, LOG, 4);
                if (rand.nextBoolean()) {
                    world.setBlock(x + dx + 3, y + 3, z + 1, PLANT, 3);
                }
                if (rand.nextBoolean()) {
                    world.setBlock(x + dx + 3, y + 3, z + 2, PLANT, 3);
                }
            }
            int offX = rand.nextInt(3) + 2;
            boolean plusZ = rand.nextBoolean();
            for (int dx = 0; dx < 3; dx++) {
                if (rand.nextBoolean()) {
                    world.setBlock(x + dx + offX, y + 2, z + (plusZ ? 3 : 0), LEAVES, 0);
                    if (rand.nextBoolean()) {
                        world.setBlock(x + dx + offX, y + 3, z + (plusZ ? 3 : 0), LEAVES, 0);
                    }
                    if (rand.nextBoolean()) {
                        world.setBlock(x + dx + offX, y + 2, z + (plusZ ? 4 : -1), LEAVES, 0);
                    }
                }
            }
            world.setBlock(x + rand.nextInt(4) + 3, y + 2, z + (plusZ ? 0 : 3), FIREFLY, 0);
            return true;
        }

        /** The floor of the hollow: log (and moss on it half the time) or dirt with moss. */
        private static void floor(OldWorld world, Random rand, int x, int y, int z, int logMeta) {
            if (rand.nextBoolean()) {
                world.setBlock(x, y - 1, z, LOG, logMeta);
                if (rand.nextBoolean()) {
                    world.setBlock(x, y, z, PLANT, 3);
                }
            } else {
                world.setBlock(x, y - 1, z, 3, 0);
                world.setBlock(x, y, z, PLANT, 3);
            }
        }
    }

    /**
     * TFGenFallenSmallLog: a fallen log of 3 to 6 with moss and maybe a branch. The original's switch over the log
     * type lacks its breaks, so every log is a jungle log (17:3); the type is still drawn.
     */
    public static final class FallenSmallLog extends TFGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            boolean goingX = rand.nextBoolean();
            int length = rand.nextInt(4) + 3;
            if (goingX) {
                if (!isAreaClear(world, x, y, z, length, 3, 2)) {
                    return false;
                }
            } else if (!isAreaClear(world, x, y, z, 3, length, 2)) {
                return false;
            }
            rand.nextInt(7);
            int logId = 17;
            int logMeta = 3;
            int logMetaBranch = logMeta;
            if (goingX) {
                logMeta |= 4;
                logMetaBranch |= 8;
                for (int lx = 0; lx < length; lx++) {
                    world.setBlock(x + lx, y, z + 1, logId, logMeta);
                    if (rand.nextInt(3) > 0) {
                        world.setBlock(x + lx, y + 1, z + 1, PLANT, 3);
                    }
                }
            } else {
                logMeta |= 8;
                logMetaBranch |= 4;
                for (int lz = 0; lz < length; lz++) {
                    world.setBlock(x + 1, y, z + lz, logId, logMeta);
                    if (rand.nextInt(3) > 0) {
                        world.setBlock(x + 1, y + 1, z + lz, PLANT, 3);
                    }
                }
            }
            if (rand.nextInt(3) > 0) {
                int bx;
                int bz;
                if (goingX) {
                    bx = rand.nextInt(length);
                    bz = rand.nextBoolean() ? 2 : 0;
                } else {
                    bx = rand.nextBoolean() ? 2 : 0;
                    bz = rand.nextInt(length);
                }
                world.setBlock(x + bx, y, z + bz, logId, logMetaBranch);
                if (rand.nextBoolean()) {
                    world.setBlock(x + bx, y + 1, z + bz, PLANT, 3);
                }
            }
            return true;
        }
    }
}
