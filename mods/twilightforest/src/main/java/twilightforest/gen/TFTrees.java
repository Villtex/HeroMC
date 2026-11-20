package twilightforest.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldBlockInfo.Material;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldTreeGens;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/** The trees of the Twilight Forest (TFGenCanopyTree and the others), for the decoration and for saplings. */
public final class TFTrees {
    private TFTrees() {
    }

    /** TFTreeGenerator.buildRoot: a root line of 5 from below the trunk. */
    static void buildRoot(OldWorld world, int x, int y, int z, double offset, int b, int rootBlock, int rootMeta) {
        int[] dest = TFGen.translate(x, y - b - 2, z, 5.0, 0.3 * b + offset, 0.8);
        int[] line = TFGen.getBresehnamArray(x, y - b - 2, z, dest[0], dest[1], dest[2]);
        for (int i = 0; i < line.length; i += 3) {
            if (canRootGrowIn(world, line[i], line[i + 1], line[i + 2])) {
                world.setBlock(line[i], line[i + 1], line[i + 2], rootBlock, rootMeta);
            }
        }
    }

    private static boolean canRootGrowIn(OldWorld world, int x, int y, int z) {
        int id = world.getBlockId(x, y, z);
        return id == 0 ? TFGen.isNearSolid(world, x, y, z) : id != 7 && id != 49 && id != 112;
    }

    /** The firefly next to the trunk of the canopy tree, mushroom and mangrove (metadata 0: it picks its side). */
    static void addFirefly(OldWorld world, int x, int y, int z, int height, double angle) {
        int iAngle = (int) (angle * 4.0);
        if (iAngle == 0) {
            TFGen.putBlockAndMetadata(world, x + 1, y + height, z, TFGen.FIREFLY, 0, false);
        } else if (iAngle == 1) {
            TFGen.putBlockAndMetadata(world, x - 1, y + height, z, TFGen.FIREFLY, 0, false);
        } else if (iAngle == 2) {
            TFGen.putBlockAndMetadata(world, x, y + height, z + 1, TFGen.FIREFLY, 0, false);
        } else if (iAngle == 3) {
            TFGen.putBlockAndMetadata(world, x, y + height, z - 1, TFGen.FIREFLY, 0, false);
        }
    }

    /** TFGenCanopyTree, and with other blocks and a shorter trunk the tree of transformation. */
    public static class Canopy extends TFGen {
        protected int minHeight = 20;
        protected int chanceAddFirstFive = 3;
        protected int chanceAddSecondFive = 8;
        protected int treeBlock = LOG;
        protected int treeMeta = 1;
        protected int branchMeta = 13;
        protected int leafBlock = LEAVES;
        protected int leafMeta = 1;
        protected int rootBlock = ROOT;
        protected int rootMeta = 0;

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            Material under = world.material(x, y - 1, z);
            if ((under == Material.GRASS || under == Material.GROUND) && y < MAXHEIGHT - 12) {
                int treeHeight = this.minHeight;
                if (random.nextInt(this.chanceAddFirstFive) == 0) {
                    treeHeight += random.nextInt(5);
                    if (random.nextInt(this.chanceAddSecondFive) == 0) {
                        treeHeight += random.nextInt(5);
                    }
                }
                this.buildBranch(world, x, y, z, 0, treeHeight, 0.0, 0.0, true, random);
                int numBranches = 3 + random.nextInt(2);
                double offset = random.nextDouble();
                for (int b = 0; b < numBranches; b++) {
                    this.buildBranch(world, x, y, z, treeHeight - 10 + b, 9.0, 0.3 * b + offset, 0.2, false, random);
                }
                if (hasAirAround(world, x, y - 1, z)) {
                    world.setBlock(x, y - 1, z, this.treeBlock, this.treeMeta);
                } else {
                    world.setBlock(x, y - 1, z, this.rootBlock, this.rootMeta);
                }
                int numRoots = 3 + random.nextInt(2);
                offset = random.nextDouble();
                for (int b = 0; b < numRoots; b++) {
                    buildRoot(world, x, y, z, offset, b, this.rootBlock, this.rootMeta);
                }
                return true;
            }
            return false;
        }

        private void buildBranch(OldWorld world, int x, int y, int z, int height, double length, double angle, double tilt, boolean trunk,
                                 Random random) {
            int[] src = {x, y + height, z};
            int[] dest = translate(src[0], src[1], src[2], length, angle, tilt);
            drawBresehnam(world, src[0], src[1], src[2], dest[0], dest[1], dest[2], this.treeBlock, trunk ? this.treeMeta : this.branchMeta, true);
            if (trunk) {
                addFirefly(world, x, y, z, 3 + random.nextInt(7), random.nextDouble());
            }
            putBlockAndMetadata(world, dest[0] + 1, dest[1], dest[2], this.treeBlock, this.branchMeta, true);
            putBlockAndMetadata(world, dest[0] - 1, dest[1], dest[2], this.treeBlock, this.branchMeta, true);
            putBlockAndMetadata(world, dest[0], dest[1], dest[2] + 1, this.treeBlock, this.branchMeta, true);
            putBlockAndMetadata(world, dest[0], dest[1], dest[2] - 1, this.treeBlock, this.branchMeta, true);
            drawCircle(world, dest[0], dest[1] - 1, dest[2], 3, this.leafBlock, this.leafMeta, false);
            drawCircle(world, dest[0], dest[1], dest[2], 4, this.leafBlock, this.leafMeta, false);
            drawCircle(world, dest[0], dest[1] + 1, dest[2], 2, this.leafBlock, this.leafMeta, false);
        }
    }

    /** TFGenTreeOfTransformation: a canopy tree of 11 of transformation wood with its heart at y + 3. */
    public static final class Transformation extends Canopy {
        public Transformation() {
            this.treeBlock = MAGIC_LOG;
            this.treeMeta = 1;
            this.branchMeta = 13;
            this.leafBlock = MAGIC_LEAVES;
            this.leafMeta = 1;
            this.minHeight = 11;
            this.chanceAddFirstFive = Integer.MAX_VALUE;
            this.chanceAddSecondFive = Integer.MAX_VALUE;
        }

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            if (super.generate(world, random, x, y, z)) {
                putBlockAndMetadata(world, x, y + 3, z, MAGIC_LOG_SPECIAL, 1, true);
                return true;
            }
            return false;
        }
    }

    /** TFGenHugeCanopyTree: a 2×2 trunk of 35 and more with big leaf nodes. */
    public static final class HugeCanopy extends TFGen {
        private static final int TREE_META = 1;
        private static final int BRANCH_META = 13;
        private static final int LEAF_META = 1;

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            int treeHeight = 35;
            if (random.nextInt(3) == 0) {
                treeHeight += random.nextInt(10);
                if (random.nextInt(8) == 0) {
                    treeHeight += random.nextInt(10);
                }
            }
            int under = world.getBlockId(x, y - 1, z);
            if ((under == 2 || under == 3) && y < MAXHEIGHT - treeHeight) {
                this.buildTrunk(world, x, y, z, treeHeight);
                int numBranches = 5 + random.nextInt(3);
                double offset = random.nextDouble();
                for (int b = 0; b < numBranches; b++) {
                    this.buildBranch(world, x, y, z, treeHeight - 23 + (int) (b * 1.5), 17.0, 0.3 * b + offset, 0.25);
                }
                int numRoots = 4 + random.nextInt(3);
                offset = random.nextDouble();
                for (int b = 0; b < numRoots; b++) {
                    int[] dest = translate(x, y - b - 2, z, 8.0, 0.278 * b + offset, 0.8);
                    drawRoot(world, x, y - b - 2, z, dest[0], dest[1], dest[2], ROOT, 0);
                }
                return true;
            }
            return false;
        }

        private void buildBranch(OldWorld world, int x, int y, int z, int height, double length, double angle, double tilt) {
            int[] src = {x, y + height, z};
            int[] dest = translate(src[0], src[1], src[2], length, angle, tilt);
            drawBresehnam(world, src[0], src[1], src[2], dest[0], dest[1], dest[2], LOG, BRANCH_META, true);
            drawBresehnam(world, src[0], src[1] - 1, src[2], dest[0], dest[1] - 1, dest[2], LOG, BRANCH_META, true);
            this.makeLeafNode(world, dest);
        }

        private void makeLeafNode(OldWorld world, int[] d) {
            drawBresehnam(world, d[0], d[1], d[2], d[0] + 4, d[1], d[2], LOG, BRANCH_META, true);
            drawBresehnam(world, d[0], d[1], d[2], d[0] - 4, d[1], d[2], LOG, BRANCH_META, true);
            drawBresehnam(world, d[0], d[1], d[2], d[0], d[1], d[2] + 4, LOG, BRANCH_META, true);
            drawBresehnam(world, d[0], d[1], d[2], d[0], d[1], d[2] - 4, LOG, BRANCH_META, true);
            putBlockAndMetadata(world, d[0] + 5, d[1], d[2] + 1, LOG, BRANCH_META, true);
            putBlockAndMetadata(world, d[0] + 5, d[1], d[2] - 1, LOG, BRANCH_META, true);
            putBlockAndMetadata(world, d[0] - 5, d[1], d[2] + 1, LOG, BRANCH_META, true);
            putBlockAndMetadata(world, d[0] - 5, d[1], d[2] - 1, LOG, BRANCH_META, true);
            putBlockAndMetadata(world, d[0] + 1, d[1], d[2] + 5, LOG, BRANCH_META, true);
            putBlockAndMetadata(world, d[0] - 1, d[1], d[2] + 5, LOG, BRANCH_META, true);
            putBlockAndMetadata(world, d[0] + 1, d[1], d[2] - 5, LOG, BRANCH_META, true);
            putBlockAndMetadata(world, d[0] - 1, d[1], d[2] - 5, LOG, BRANCH_META, true);
            drawBresehnam(world, d[0] + 1, d[1], d[2], d[0] + 4, d[1], d[2] + 3, LOG, BRANCH_META, true);
            drawBresehnam(world, d[0] - 1, d[1], d[2], d[0] - 4, d[1], d[2] - 3, LOG, BRANCH_META, true);
            drawBresehnam(world, d[0], d[1], d[2] + 1, d[0] - 3, d[1], d[2] + 4, LOG, BRANCH_META, true);
            drawBresehnam(world, d[0], d[1], d[2] - 1, d[0] + 3, d[1], d[2] - 4, LOG, BRANCH_META, true);
            drawCircle(world, d[0], d[1] - 2, d[2], 4, LEAVES, LEAF_META, false);
            drawCircle(world, d[0], d[1] - 1, d[2], 7, LEAVES, LEAF_META, false);
            drawCircle(world, d[0], d[1], d[2], 8, LEAVES, LEAF_META, false);
            drawCircle(world, d[0], d[1] + 1, d[2], 6, LEAVES, LEAF_META, false);
            drawCircle(world, d[0], d[1] + 2, d[2], 3, LEAVES, LEAF_META, false);
        }

        private void buildTrunk(OldWorld world, int x, int y, int z, int length) {
            int[] src = {x, y, z};
            int[] dest = translate(src[0], src[1], src[2], length, 0.0, 0.0);
            for (int dy = -6; dy < 0; dy++) {
                this.drawRootBlock(world, x, y + dy, z);
                this.drawRootBlock(world, x + 1, y + dy, z);
                this.drawRootBlock(world, x, y + dy, z + 1);
                this.drawRootBlock(world, x + 1, y + dy, z + 1);
            }
            drawBresehnam(world, src[0], src[1], src[2], dest[0], dest[1], dest[2], LOG, TREE_META, true);
            drawBresehnam(world, src[0] + 1, src[1], src[2], dest[0] + 1, dest[1], dest[2], LOG, TREE_META, true);
            drawBresehnam(world, src[0] + 1, src[1], src[2] + 1, dest[0] + 1, dest[1], dest[2] + 1, LOG, TREE_META, true);
            drawBresehnam(world, src[0], src[1], src[2] + 1, dest[0], dest[1], dest[2] + 1, LOG, TREE_META, true);
            this.makeLeafNode(world, dest);
        }

        private void drawRootBlock(OldWorld world, int x, int y, int z) {
            if (hasAirAround(world, x, y, z)) {
                world.setBlock(x, y, z, LOG, BRANCH_META);
            } else {
                world.setBlock(x, y, z, ROOT, 0);
            }
        }
    }

    /** TFGenHugeCanopyTree.drawRoot and TFGenMinersTree.drawRoot: into the ground, or along whatever it touches. */
    static void drawRoot(OldWorld world, int sx, int sy, int sz, int dx, int dy, int dz, int rootBlock, int rootMeta) {
        if (world.material(dx, dy, dz).solid) {
            TFGen.drawBresehnam(world, sx, sy, sz, dx, dy, dz, rootBlock, rootMeta, true);
        } else {
            int[] line = TFGen.getBresehnamArray(sx, sy, sz, dx, dy, dz);
            for (int i = 0; i < line.length; i += 3) {
                if (world.getBlockId(line[i], line[i + 1], line[i + 2]) > 0 || TFGen.isNearSolid(world, line[i], line[i + 1], line[i + 2])) {
                    world.setBlock(line[i], line[i + 1], line[i + 2], rootBlock, rootMeta);
                }
            }
        }
    }

    /** TFGenDarkCanopyTree: a short darkwood trunk with four branches under a wide hedge-leaf canopy. */
    public static final class DarkCanopy extends TFGen {
        private static final int TREE_META = 3;
        private static final int BRANCH_META = 15;
        private static final int LEAF_META = 1;

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            boolean foundDirt = false;
            for (int dy = y; dy >= TFTerrain.SEALEVEL; dy--) {
                Material under = world.material(x, dy - 1, z);
                if (under == Material.GRASS || under == Material.GROUND) {
                    foundDirt = true;
                    y = dy;
                    break;
                }
                if (under == Material.ROCK || under == Material.SAND) {
                    break;
                }
            }
            if (!foundDirt) {
                return false;
            }
            if (world.material(x + 1, y, z) == Material.WOOD || world.material(x - 1, y, z) == Material.WOOD
                    || world.material(x, y, z + 1) == Material.WOOD || world.material(x, y, z - 1) == Material.WOOD) {
                return false;
            }
            int treeHeight = 4 + random.nextInt(3);
            drawBresehnam(world, x, y, z, x, y + treeHeight, z, LOG, TREE_META, true);
            int numBranches = 4;
            double offset = random.nextFloat();
            for (int b = 0; b < numBranches; b++) {
                this.buildBranch(world, x, y, z, treeHeight - numBranches + b / 2, 8.0, 0.23 * b + offset, 0.23, random);
            }
            if (hasAirAround(world, x, y - 1, z)) {
                world.setBlock(x, y - 1, z, LOG, TREE_META);
            } else {
                world.setBlock(x, y - 1, z, ROOT, 0);
            }
            int numRoots = 3 + random.nextInt(2);
            offset = random.nextDouble();
            for (int b = 0; b < numRoots; b++) {
                buildRoot(world, x, y, z, offset, b, ROOT, 0);
            }
            return true;
        }

        private void buildBranch(OldWorld world, int x, int y, int z, int height, double length, double angle, double tilt, Random random) {
            int[] src = {x, y + height, z};
            int[] dest = translate(src[0], src[1], src[2], length, angle, tilt);
            drawBresehnam(world, src[0], src[1], src[2], dest[0], dest[1], dest[2], LOG, BRANCH_META, true);
            byte leafSize = 4;
            if (random.nextInt(3) == 0) {
                leafSize = (byte) (leafSize + (random.nextInt(3) - 1));
            }
            if (world.getBlockId(dest[0], dest[1] - 1, dest[2]) == 0 || world.getBlockId(dest[0], dest[1] + 1, dest[2]) == 0
                    || world.getBlockId(dest[0] + 4, dest[1], dest[2]) == 0 || world.getBlockId(dest[0] - 4, dest[1], dest[2]) == 0
                    || world.getBlockId(dest[0], dest[1], dest[2] + 4) == 0 || world.getBlockId(dest[0], dest[1], dest[2] - 4) == 0) {
                drawCircle(world, dest[0], dest[1] - 1, dest[2], leafSize, HEDGE, LEAF_META, false);
                drawCircle(world, dest[0], dest[1], dest[2], leafSize + 1, HEDGE, LEAF_META, false);
                drawCircle(world, dest[0], dest[1] + 1, dest[2], leafSize, HEDGE, LEAF_META, false);
                drawCircle(world, dest[0], dest[1] + 2, dest[2], leafSize - 2, HEDGE, LEAF_META, false);
            }
        }
    }

    /** TFGenCanopyMushroom: a giant mushroom with a trunk and several caps (red 1 in 3, else brown). */
    public static final class CanopyMushroom extends TFGen {
        private static final int TREE_META = 10;
        private static final int BRANCH_META = 15;

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            int treeHeight = 12;
            if (random.nextInt(3) == 0) {
                treeHeight += random.nextInt(5);
                if (random.nextInt(8) == 0) {
                    treeHeight += random.nextInt(5);
                }
            }
            int under = world.getBlockId(x, y - 1, z);
            if ((under == 2 || under == 3 || under == 110) && y < 256 - treeHeight - 1) {
                int block = random.nextInt(3) == 0 ? 100 : 99;
                this.buildBranch(world, x, y, z, 0, treeHeight, 0.0, 0.0, true, random, block);
                int numBranches = 3 + random.nextInt(2);
                double offset = random.nextDouble();
                for (int b = 0; b < numBranches; b++) {
                    this.buildBranch(world, x, y, z, treeHeight - 5 + b, 9.0, 0.3 * b + offset, 0.2, false, random, block);
                }
                return true;
            }
            return false;
        }

        private void buildBranch(OldWorld world, int x, int y, int z, int height, double length, double angle, double tilt, boolean trunk,
                                 Random random, int block) {
            int[] src = {x, y + height, z};
            int[] dest = translate(src[0], src[1], src[2], length, angle, tilt);
            if (src[0] == dest[0] && src[2] == dest[2]) {
                drawBresehnam(world, src[0], src[1], src[2], dest[0], dest[1] - 1, dest[2], block, TREE_META, true);
            } else {
                drawBresehnam(world, src[0], src[1], src[2], dest[0], src[1], dest[2], block, BRANCH_META, true);
                drawBresehnam(world, dest[0], src[1] + 1, dest[2], dest[0], dest[1] - 1, dest[2], block, TREE_META, true);
            }
            if (trunk) {
                addFirefly(world, x, y, z, 3 + random.nextInt(7), random.nextDouble());
            }
            drawMushroomCircle(world, dest[0], dest[1], dest[2], 4, block, true);
        }

        private static void drawMushroomCircle(OldWorld world, int sx, int sy, int sz, int rad, int block, boolean priority) {
            for (byte dx = 0; dx <= rad; dx++) {
                for (byte dz = 0; dz <= rad; dz++) {
                    int dist = (int) (Math.max(dx, dz) + Math.min(dx, dz) * 0.5);
                    if (dx == 3 && dz == 3) {
                        dist = 6;
                    }
                    if (dx == 0) {
                        if (dz < rad) {
                            putBlockAndMetadata(world, sx, sy, sz + dz, block, 5, priority);
                            putBlockAndMetadata(world, sx, sy, sz - dz, block, 5, priority);
                        } else {
                            putBlockAndMetadata(world, sx, sy, sz + dz, block, 8, priority);
                            putBlockAndMetadata(world, sx, sy, sz - dz, block, 2, priority);
                        }
                    } else if (dz == 0) {
                        if (dx < rad) {
                            putBlockAndMetadata(world, sx + dx, sy, sz, block, 5, priority);
                            putBlockAndMetadata(world, sx - dx, sy, sz, block, 5, priority);
                        } else {
                            putBlockAndMetadata(world, sx + dx, sy, sz, block, 6, priority);
                            putBlockAndMetadata(world, sx - dx, sy, sz, block, 4, priority);
                        }
                    } else if (dist < rad) {
                        putBlockAndMetadata(world, sx + dx, sy, sz + dz, block, 5, priority);
                        putBlockAndMetadata(world, sx + dx, sy, sz - dz, block, 5, priority);
                        putBlockAndMetadata(world, sx - dx, sy, sz + dz, block, 5, priority);
                        putBlockAndMetadata(world, sx - dx, sy, sz - dz, block, 5, priority);
                    } else if (dist == rad) {
                        putBlockAndMetadata(world, sx + dx, sy, sz + dz, block, 9, priority);
                        putBlockAndMetadata(world, sx + dx, sy, sz - dz, block, 3, priority);
                        putBlockAndMetadata(world, sx - dx, sy, sz + dz, block, 7, priority);
                        putBlockAndMetadata(world, sx - dx, sy, sz - dz, block, 1, priority);
                    }
                }
            }
        }
    }

    /** TFGenMangroveTree: on still water, roots arching down into the swamp. {@code checkForWater} is false for saplings. */
    public static final class Mangrove extends TFGen {
        private static final int TREE_META = 2;
        private static final int BRANCH_META = 14;
        private static final int LEAF_META = 2;
        private final boolean checkForWater;

        public Mangrove(boolean checkForWater) {
            this.checkForWater = checkForWater;
        }

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            if ((!this.checkForWater || world.getBlockId(x, y - 1, z) == 9) && y < 109) {
                this.buildBranch(world, random, x, y, z, 5, 6 + random.nextInt(3), 0.0, 0.0, true);
                int numBranches = random.nextInt(3);
                double offset = random.nextDouble();
                for (int b = 0; b < numBranches; b++) {
                    this.buildBranch(world, random, x, y, z, 7 + b, 6 + random.nextInt(2), 0.3 * b + offset, 0.25, false);
                }
                int numRoots = 3 + random.nextInt(2);
                offset = random.nextDouble();
                for (int i = 0; i < numRoots; i++) {
                    double rTilt = 0.75 + random.nextDouble() * 0.1;
                    this.buildRoot(world, x, y, z, 5, 12.0, 0.4 * i + offset, rTilt);
                }
                addFirefly(world, x, y, z, 5 + random.nextInt(5), random.nextDouble());
                return true;
            }
            return false;
        }

        private void buildBranch(OldWorld world, Random random, int x, int y, int z, int height, double length, double angle, double tilt,
                                 boolean trunk) {
            int[] src = {x, y + height, z};
            int[] dest = translate(src[0], src[1], src[2], length, angle, tilt);
            drawBresehnam(world, src[0], src[1], src[2], dest[0], dest[1], dest[2], LOG, trunk ? TREE_META : BRANCH_META, true);
            int bSize = 2 + random.nextInt(3);
            if (bSize > 2) {
                putBlockAndMetadata(world, dest[0] + 1, dest[1], dest[2], LOG, BRANCH_META, true);
                putBlockAndMetadata(world, dest[0] - 1, dest[1], dest[2], LOG, BRANCH_META, true);
                putBlockAndMetadata(world, dest[0], dest[1], dest[2] + 1, LOG, BRANCH_META, true);
                putBlockAndMetadata(world, dest[0], dest[1], dest[2] - 1, LOG, BRANCH_META, true);
            }
            drawCircle(world, dest[0], dest[1] - 1, dest[2], (byte) (bSize - 1), LEAVES, LEAF_META, false);
            drawCircle(world, dest[0], dest[1], dest[2], (byte) bSize, LEAVES, LEAF_META, false);
            drawCircle(world, dest[0], dest[1] + 1, dest[2], (byte) (bSize - 2), LEAVES, LEAF_META, false);
        }

        private void buildRoot(OldWorld world, int x, int y, int z, int height, double length, double angle, double tilt) {
            int[] src = {x, y + height, z};
            int[] dest = translate(src[0], src[1], src[2], length, angle, tilt);
            int[] line = getBresehnamArray(src[0], src[1], src[2], dest[0], dest[1], dest[2]);
            boolean stillAboveGround = true;
            for (int i = 0; i < line.length; i += 3) {
                if (stillAboveGround && hasAirAround(world, line[i], line[i + 1], line[i + 2])) {
                    world.setBlock(line[i], line[i + 1], line[i + 2], LOG, BRANCH_META);
                    world.setBlock(line[i], line[i + 1] - 1, line[i + 2], LOG, BRANCH_META);
                } else {
                    world.setBlock(line[i], line[i + 1], line[i + 2], ROOT, 0);
                    world.setBlock(line[i], line[i + 1] - 1, line[i + 2], ROOT, 0);
                    stillAboveGround = false;
                }
            }
        }
    }

    /** TFGenSmallTwilightOak (leaves of metadata 0), and with leaves 3 the small rainbow oak. */
    public static final class SmallOak extends TFGen {
        private final int minTreeHeight = 4;
        private final int leafMeta;

        public SmallOak(int leafMeta) {
            this.leafMeta = leafMeta;
        }

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            int height = random.nextInt(3) + this.minTreeHeight;
            boolean allClear = true;
            if (y < 1 || y + height + 1 > 256) {
                return false;
            }
            for (int cy = y; cy <= y + 1 + height; cy++) {
                int width = 1;
                if (cy == y) {
                    width = 0;
                }
                if (cy >= y + 1 + height - 2) {
                    width = 2;
                }
                for (int cx = x - width; cx <= x + width && allClear; cx++) {
                    for (int cz = z - width; cz <= z + width && allClear; cz++) {
                        if (cy >= 0 && cy < 256) {
                            int id = world.getBlockId(cx, cy, cz);
                            if (id != 0 && !OldBlockInfo.leaves(id) && id != 2 && id != 3 && !OldBlockInfo.wood(id)) {
                                allClear = false;
                            }
                        } else {
                            allClear = false;
                        }
                    }
                }
            }
            if (!allClear) {
                return false;
            }
            int under = world.getBlockId(x, y - 1, z);
            if ((under != 2 && under != 3) || y >= 256 - height - 1) {
                return false;
            }
            world.setBlock(x, y - 1, z, 3, 0);
            for (int cy = y - 3 + height; cy <= y + height; cy++) {
                int dy = cy - (y + height);
                int treeWidth = 1 - dy / 2;
                for (int tx = x - treeWidth; tx <= x + treeWidth; tx++) {
                    int ox = tx - x;
                    for (int tz = z - treeWidth; tz <= z + treeWidth; tz++) {
                        int oz = tz - z;
                        int id = world.getBlockId(tx, cy, tz);
                        if ((Math.abs(ox) != treeWidth || Math.abs(oz) != treeWidth || random.nextInt(2) != 0 && dy != 0)
                                && (id == 0 || OldBlockInfo.replacedByLeaves(id))) {
                            world.setBlock(tx, cy, tz, LEAVES, this.leafMeta);
                        }
                    }
                }
            }
            for (int dy = 0; dy < height; dy++) {
                int id = world.getBlockId(x, y + dy, z);
                if (id == 0 || OldBlockInfo.leaves(id)) {
                    world.setBlock(x, y + dy, z, LOG, 0);
                }
            }
            return true;
        }
    }

    /** TFGenLargeRainboak: the big oak of 1.5.2 made of twilight oak with rainbow leaves. */
    public static OldTreeGens.BigTree largeRainboak() {
        return new OldTreeGens.BigTree(TFGen.LOG, TFGen.LEAVES, 3, true);
    }

    /**
     * TFGenHollowTree: a giant hollow oak with a crown of branches, fireflies and cicadas on the bark, vines inside and
     * now and then a leaf dungeon with a spider spawner and a chest. Subclasses: the stump and the tree of time.
     */
    public static class Hollow extends TFGen {
        protected int treeBlock = LOG;
        protected int treeMeta = 0;
        protected int branchMeta = 12;
        protected int leafBlock = LEAVES;
        protected int leafMeta = 0;
        protected int rootBlock = ROOT;
        protected int rootMeta = 0;

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            int height = random.nextInt(64) + 32;
            int diameter = random.nextInt(4) + 1;
            if (y < 1 || y + height + diameter > MAXHEIGHT) {
                return false;
            }
            int crownRadius = diameter * 4 + 8;
            for (int dx = -crownRadius; dx <= crownRadius; dx++) {
                for (int dz = -crownRadius; dz <= crownRadius; dz++) {
                    for (int dy = height - crownRadius; dy <= height + crownRadius; dy++) {
                        int there = world.getBlockId(dx + x, dy + y, dz + z);
                        if (there != 0 && there != 18) {
                            return false;
                        }
                    }
                }
            }
            int under = world.getBlockId(x, y - 1, z);
            if (under != 2 && under != 3) {
                return false;
            }
            this.buildTrunk(world, random, x, y, z, diameter, height);
            int numFireflies = random.nextInt(3 * diameter) + 5;
            for (int i = 0; i <= numFireflies; i++) {
                int fHeight = (int) (height * random.nextDouble() * 0.9) + height / 10;
                double fAngle = random.nextDouble();
                this.addCritter(world, x, y, z, diameter, fHeight, fAngle, FIREFLY, 0);
            }
            numFireflies = random.nextInt(3 * diameter) + 5;
            for (int i = 0; i <= numFireflies; i++) {
                int fHeight = (int) (height * random.nextDouble() * 0.9) + height / 10;
                double fAngle = random.nextDouble();
                this.addCritter(world, x, y, z, diameter, fHeight, fAngle, CICADA, 1);
            }
            this.buildFullCrown(world, random, x, y, z, diameter, height);
            int numBranches = random.nextInt(3) + 3;
            for (int i = 0; i <= numBranches; i++) {
                int branchHeight = (int) (height * random.nextDouble() * 0.9) + height / 10;
                double branchRotation = random.nextDouble();
                this.makeSmallBranch(world, random, x, y, z, diameter, branchHeight, 4.0, branchRotation, 0.35, true);
            }
            this.buildBranchRing(world, random, x, y, z, diameter, 3, 2, 6, 0, 0.75, 0.0, 3, 5, 3, false);
            this.buildBranchRing(world, random, x, y, z, diameter, 1, 2, 8, 0, 0.9, 0.0, 3, 5, 3, false);
            return true;
        }

        protected void buildFullCrown(OldWorld world, Random random, int x, int y, int z, int diameter, int height) {
            int crownRadius = diameter * 4 + 4;
            int bvar = diameter + 2;
            this.buildBranchRing(world, random, x, y, z, diameter, height - crownRadius, 0, crownRadius, 0, 0.35, 0.0, bvar, bvar + 2, 2, true);
            this.buildBranchRing(world, random, x, y, z, diameter, height - crownRadius / 2, 0, crownRadius, 0, 0.28, 0.0, bvar, bvar + 2, 1, true);
            this.buildBranchRing(world, random, x, y, z, diameter, height, 0, crownRadius, 0, 0.15, 0.0, 2, 4, 2, true);
            this.buildBranchRing(world, random, x, y, z, diameter, height, 0, crownRadius / 2, 0, 0.05, 0.0, bvar, bvar + 2, 1, true);
        }

        protected void buildBranchRing(OldWorld world, Random random, int x, int y, int z, int diameter, int branchHeight, int heightVar,
                                       int length, int lengthVar, double tilt, double tiltVar, int minBranches, int maxBranches, int size,
                                       boolean leafy) {
            int numBranches = random.nextInt(maxBranches - minBranches) + minBranches;
            double branchRotation = 1.0 / (numBranches + 1);
            double branchOffset = random.nextDouble();
            for (int i = 0; i <= numBranches; i++) {
                int dHeight;
                if (heightVar > 0) {
                    dHeight = branchHeight - heightVar + random.nextInt(2 * heightVar);
                } else {
                    dHeight = branchHeight;
                }
                double angle = i * branchRotation + branchOffset;
                if (size == 2) {
                    this.makeLargeBranch(world, random, x, y, z, diameter, dHeight, length, angle, tilt, leafy);
                } else if (size == 1) {
                    this.makeMedBranch(world, random, x, y, z, diameter, dHeight, length, angle, tilt, leafy);
                } else if (size == 3) {
                    this.makeRoot(world, x, y, z, diameter, dHeight, length, angle, tilt);
                } else {
                    this.makeSmallBranch(world, random, x, y, z, diameter, dHeight, length, angle, tilt, leafy);
                }
            }
        }

        protected void buildTrunk(OldWorld world, Random random, int x, int y, int z, int diameter, int height) {
            int hollow = diameter / 2;
            this.buildTrunkBase(world, x, y, z, diameter);
            for (int dx = -diameter; dx <= diameter; dx++) {
                for (int dz = -diameter; dz <= diameter; dz++) {
                    for (int dy = 0; dy <= height; dy++) {
                        int ax = Math.abs(dx);
                        int az = Math.abs(dz);
                        int dist = (int) (Math.max(ax, az) + Math.min(ax, az) * 0.5);
                        if (dist <= diameter && dist > hollow) {
                            putBlockAndMetadata(world, dx + x, dy + y, dz + z, this.treeBlock, this.treeMeta, true);
                        }
                        if (dist == hollow && dx == hollow) {
                            putBlockAndMetadata(world, dx + x, dy + y, dz + z, 106, 8, true);
                        }
                    }
                }
            }
        }

        /** The four layers below the trunk: bark where air touches it, roots elsewhere. */
        protected void buildTrunkBase(OldWorld world, int x, int y, int z, int diameter) {
            int hollow = diameter / 2;
            for (int dx = -diameter; dx <= diameter; dx++) {
                for (int dz = -diameter; dz <= diameter; dz++) {
                    for (int dy = -4; dy < 0; dy++) {
                        int ax = Math.abs(dx);
                        int az = Math.abs(dz);
                        int dist = (int) (Math.max(ax, az) + Math.min(ax, az) * 0.5);
                        if (dist <= diameter) {
                            if (hasAirAround(world, dx + x, dy + y, dz + z)) {
                                world.setBlock(dx + x, dy + y, dz + z, this.treeBlock, dist > hollow ? this.treeMeta : this.branchMeta);
                            } else {
                                world.setBlock(dx + x, dy + y, dz + z, this.rootBlock, this.rootMeta);
                            }
                        }
                    }
                }
            }
        }

        protected void makeMedBranch(OldWorld world, Random random, int x, int y, int z, int diameter, int branchHeight, double length,
                                     double angle, double tilt, boolean leafy) {
            int[] src = translate(x, y + branchHeight, z, diameter, angle, 0.5);
            this.makeMedBranch(world, random, src[0], src[1], src[2], length, angle, tilt, leafy);
        }

        protected void makeMedBranch(OldWorld world, Random random, int sx, int sy, int sz, double length, double angle, double tilt,
                                     boolean leafy) {
            int[] src = {sx, sy, sz};
            int[] dest = translate(src[0], src[1], src[2], length, angle, tilt);
            drawBresehnam(world, src[0], src[1], src[2], dest[0], dest[1], dest[2], this.treeBlock, this.branchMeta, true);
            if (leafy) {
                drawBlob(world, dest[0], dest[1], dest[2], 2, this.leafBlock, this.leafMeta, false);
            }
            int numShoots = random.nextInt(2) + 1;
            double angleInc = 0.8 / numShoots;
            for (int i = 0; i <= numShoots; i++) {
                double angleVar = angleInc * i - 0.4;
                double outVar = random.nextDouble() * 0.8 + 0.2;
                double tiltVar = random.nextDouble() * 0.75 + 0.15;
                int[] bsrc = translate(src[0], src[1], src[2], length * outVar, angle, tilt);
                double slength = length * 0.4;
                this.makeSmallBranch(world, random, bsrc[0], bsrc[1], bsrc[2], slength, angle + angleVar, tilt * tiltVar, leafy);
            }
        }

        protected void makeSmallBranch(OldWorld world, Random random, int sx, int sy, int sz, double length, double angle, double tilt,
                                       boolean leafy) {
            int[] src = {sx, sy, sz};
            int[] dest = translate(src[0], src[1], src[2], length, angle, tilt);
            drawBresehnam(world, src[0], src[1], src[2], dest[0], dest[1], dest[2], this.treeBlock, this.branchMeta, true);
            if (leafy) {
                byte leafRad = (byte) (random.nextInt(2) + 1);
                drawBlob(world, dest[0], dest[1], dest[2], leafRad, this.leafBlock, this.leafMeta, false);
            }
        }

        protected void makeSmallBranch(OldWorld world, Random random, int x, int y, int z, int diameter, int branchHeight, double length,
                                       double angle, double tilt, boolean leafy) {
            int[] src = translate(x, y + branchHeight, z, diameter, angle, 0.5);
            this.makeSmallBranch(world, random, src[0], src[1], src[2], length, angle, tilt, leafy);
        }

        protected void makeRoot(OldWorld world, int x, int y, int z, int diameter, int branchHeight, double length, double angle, double tilt) {
            int[] src = translate(x, y + branchHeight, z, diameter, angle, 0.5);
            int[] dest = translate(src[0], src[1], src[2], length, angle, tilt);
            int[] line = getBresehnamArray(src[0], src[1], src[2], dest[0], dest[1], dest[2]);
            boolean stillAboveGround = true;
            for (int i = 0; i < line.length; i += 3) {
                if (stillAboveGround && hasAirAround(world, line[i], line[i + 1], line[i + 2])) {
                    world.setBlock(line[i], line[i + 1], line[i + 2], this.treeBlock, this.branchMeta);
                    world.setBlock(line[i], line[i + 1] - 1, line[i + 2], this.treeBlock, this.branchMeta);
                } else {
                    world.setBlock(line[i], line[i + 1], line[i + 2], this.rootBlock, this.rootMeta);
                    world.setBlock(line[i], line[i + 1] - 1, line[i + 2], this.rootBlock, this.rootMeta);
                    stillAboveGround = false;
                }
            }
        }

        protected void makeLargeBranch(OldWorld world, Random random, int sx, int sy, int sz, double length, double angle, double tilt,
                                       boolean leafy) {
            int[] src = {sx, sy, sz};
            int[] dest = translate(src[0], src[1], src[2], length, angle, tilt);
            drawBresehnam(world, src[0], src[1], src[2], dest[0], dest[1], dest[2], this.treeBlock, this.branchMeta, true);
            int reinforcements = random.nextInt(3);
            for (int i = 0; i <= reinforcements; i++) {
                int vx = (i & 2) == 0 ? 1 : 0;
                int vy = (i & 1) == 0 ? 1 : -1;
                int vz = (i & 2) == 0 ? 0 : 1;
                drawBresehnam(world, src[0] + vx, src[1] + vy, src[2] + vz, dest[0], dest[1], dest[2], this.treeBlock, this.branchMeta, true);
            }
            if (leafy) {
                drawBlob(world, dest[0], dest[1] + 1, dest[2], 3, this.leafBlock, this.leafMeta, false);
            }
            int numMedBranches = random.nextInt((int) (length / 6.0)) + random.nextInt(2) + 1;
            for (int i = 0; i <= numMedBranches; i++) {
                double outVar = random.nextDouble() * 0.3 + 0.3;
                double angleVar = random.nextDouble() * 0.225 * ((i & 1) == 0 ? 1.0 : -1.0);
                int[] bsrc = translate(src[0], src[1], src[2], length * outVar, angle, tilt);
                this.makeMedBranch(world, random, bsrc[0], bsrc[1], bsrc[2], length * 0.6, angle + angleVar, tilt, leafy);
            }
            int numSmallBranches = random.nextInt(2) + 1;
            for (int i = 0; i <= numSmallBranches; i++) {
                double outVar = random.nextDouble() * 0.25 + 0.25;
                double angleVar = random.nextDouble() * 0.25 * ((i & 1) == 0 ? 1.0 : -1.0);
                int[] bsrc = translate(src[0], src[1], src[2], length * outVar, angle, tilt);
                this.makeSmallBranch(world, random, bsrc[0], bsrc[1], bsrc[2], Math.max(length * 0.3, 2.0), angle + angleVar, tilt, leafy);
            }
            if (random.nextInt(8) == 0) {
                this.makeLeafDungeon(world, random, dest[0], dest[1] + 1, dest[2]);
            }
        }

        private void makeLeafDungeon(OldWorld world, Random random, int x, int y, int z) {
            drawBlob(world, x, y, z, 4, this.leafBlock, this.leafMeta, true);
            drawBlob(world, x, y, z, 3, this.treeBlock, this.branchMeta, true);
            drawBlob(world, x, y, z, 2, 0, 0, true);
            world.setBlock(x, y + 1, z, 52, 0);
            world.setTile(x, y + 1, z, new OldTile.Spawner(TFTreasure.SPAWNER_PREFIX + "Swarm Spider"));
            int dir = random.nextInt(4);
            // Direction.offsetX / offsetZ of 1.5.2: south, west, north, east
            int[] offX = {0, -1, 0, 1};
            int[] offZ = {1, 0, -1, 0};
            x += offX[dir] * 2;
            z += offZ[dir] * 2;
            TFTreasure.TREE_CACHE.generate(world, x, y - 1, z);
        }

        protected void makeLargeBranch(OldWorld world, Random random, int x, int y, int z, int diameter, int branchHeight, double length,
                                       double angle, double tilt, boolean leafy) {
            int[] src = translate(x, y + branchHeight, z, diameter, angle, 0.5);
            this.makeLargeBranch(world, random, src[0], src[1], src[2], length, angle, tilt, leafy);
        }

        /** addFirefly (default metadata 0) and addCicada (default 1): on the bark, facing away from the trunk. */
        private void addCritter(OldWorld world, int x, int y, int z, int diameter, int fHeight, double fAngle, int block, int defaultMeta) {
            int[] src = translate(x, y + fHeight, z, diameter + 1, fAngle, 0.5);
            fAngle %= 1.0;
            int meta = defaultMeta;
            if (fAngle > 0.875 || fAngle <= 0.125) {
                meta = 3;
            } else if (fAngle > 0.125 && fAngle <= 0.375) {
                meta = 1;
            } else if (fAngle > 0.375 && fAngle <= 0.625) {
                meta = 4;
            } else if (fAngle > 0.625 && fAngle <= 0.875) {
                meta = 2;
            }
            if (critterCanPlaceBlockAt(world, src[0], src[1], src[2])) {
                putBlockAndMetadata(world, src[0], src[1], src[2], block, meta, false);
            }
        }
    }

    /** TFGenHollowStump: a short hollow trunk with roots, ragged at the top. */
    public static final class HollowStump extends Hollow {
        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            int radius = random.nextInt(2) + 2;
            if (!isAreaClear(world, x - radius, y, z - radius, 2 * radius, 6, 2 * radius)) {
                return false;
            }
            this.buildTrunk(world, random, x, y, z, radius, 6);
            this.buildBranchRing(world, random, x, y, z, radius, 3, 2, 6, 0, 0.75, 0.0, 3, 5, 3, false);
            this.buildBranchRing(world, random, x, y, z, radius, 1, 2, 8, 0, 0.9, 0.0, 3, 5, 3, false);
            return true;
        }

        @Override
        protected void buildTrunk(OldWorld world, Random random, int x, int y, int z, int diameter, int maxHeight) {
            int hollow = diameter / 2;
            this.buildTrunkBase(world, x, y, z, diameter);
            for (int dx = -diameter; dx <= diameter; dx++) {
                for (int dz = -diameter; dz <= diameter; dz++) {
                    int height = 2 + random.nextInt(3) + random.nextInt(2);
                    for (int dy = 0; dy <= height; dy++) {
                        int ax = Math.abs(dx);
                        int az = Math.abs(dz);
                        int dist = (int) (Math.max(ax, az) + Math.min(ax, az) * 0.5);
                        if (dist <= diameter && dist > hollow) {
                            putBlockAndMetadata(world, dx + x, dy + y, dz + z, this.treeBlock, this.treeMeta, true);
                        }
                    }
                }
            }
        }
    }

    /** TFGenTreeOfTime: a small hollow tree of timewood with its clock at x - 1, y + 2. */
    public static final class TreeOfTime extends Hollow {
        public TreeOfTime() {
            this.treeBlock = MAGIC_LOG;
            this.treeMeta = 0;
            this.branchMeta = 12;
            this.leafBlock = MAGIC_LEAVES;
            this.leafMeta = 0;
        }

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            int height = 8;
            int diameter = 1;
            if (y < 1 || y + height + diameter > MAXHEIGHT) {
                return false;
            }
            int under = world.getBlockId(x, y - 1, z);
            if (under != 2 && under != 3) {
                return false;
            }
            this.buildTrunk(world, random, x, y, z, diameter, height);
            int crownRadius = 4;
            int bvar = 1;
            this.buildBranchRing(world, random, x, y, z, diameter, height - crownRadius, 0, crownRadius, 0, 0.35, 0.0, bvar, bvar + 2, 1, true);
            this.buildBranchRing(world, random, x, y, z, diameter, height - crownRadius / 2, 0, crownRadius, 0, 0.28, 0.0, bvar, bvar + 2, 1, true);
            this.buildBranchRing(world, random, x, y, z, diameter, height, 0, crownRadius, 0, 0.15, 0.0, 2, 4, 0, true);
            this.buildBranchRing(world, random, x, y, z, diameter, height, 0, crownRadius / 2, 0, 0.05, 0.0, bvar, bvar + 2, 0, true);
            this.buildBranchRing(world, random, x, y, z, diameter, 1, 0, 12, 0, 0.75, 0.0, 3, 5, 3, false);
            this.buildBranchRing(world, random, x, y, z, diameter, 1, 2, 18, 0, 0.9, 0.0, 3, 5, 3, false);
            world.setBlock(x - 1, y + 2, z, MAGIC_LOG_SPECIAL, 0);
            return true;
        }
    }

    /** TFGenMinersTree: a mining-wood pole with two drooping branches and its core at y + 1. */
    public static final class Miners extends TFGen {
        private static final int TREE_META = 2;
        private static final int BRANCH_META = 14;
        private static final int LEAF_META = 2;

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            Material under = world.material(x, y - 1, z);
            if ((under != Material.GRASS && under != Material.GROUND) || y >= MAXHEIGHT - 12) {
                return false;
            }
            for (int dy = 0; dy < 10; dy++) {
                putBlockAndMetadata(world, x, y + dy, z, MAGIC_LOG, BRANCH_META, true);
            }
            putBranchWithLeaves(world, x, y + 9, z + 1, true);
            putBranchWithLeaves(world, x, y + 9, z + 2, false);
            putBranchWithLeaves(world, x, y + 8, z + 3, false);
            putBranchWithLeaves(world, x, y + 7, z + 4, false);
            putBranchWithLeaves(world, x, y + 6, z + 5, false);
            putBranchWithLeaves(world, x, y + 9, z - 1, true);
            putBranchWithLeaves(world, x, y + 9, z - 2, false);
            putBranchWithLeaves(world, x, y + 8, z - 3, false);
            putBranchWithLeaves(world, x, y + 7, z - 4, false);
            putBranchWithLeaves(world, x, y + 6, z - 5, false);
            putBlockAndMetadata(world, x, y + 1, z, MAGIC_LOG_SPECIAL, 2, true);
            if (hasAirAround(world, x, y - 1, z)) {
                world.setBlock(x, y - 1, z, MAGIC_LOG, TREE_META);
            } else {
                world.setBlock(x, y - 1, z, ROOT, 0);
            }
            int numRoots = 3 + random.nextInt(2);
            double offset = random.nextDouble();
            for (int b = 0; b < numRoots; b++) {
                int[] dest = translate(x, y - b - 2, z, 6.0, 0.3 * b + offset, 0.8);
                drawRoot(world, x, y - b - 2, z, dest[0], dest[1], dest[2], ROOT, 0);
            }
            return true;
        }

        private static void putBranchWithLeaves(OldWorld world, int bx, int by, int bz, boolean bushy) {
            putBlockAndMetadata(world, bx, by, bz, MAGIC_LOG, BRANCH_META, true);
            for (int lx = -1; lx <= 1; lx++) {
                for (int ly = -1; ly <= 1; ly++) {
                    for (int lz = -1; lz <= 1; lz++) {
                        if (bushy || Math.abs(ly) <= 0 || Math.abs(lx) <= 0) {
                            putBlockAndMetadata(world, bx + lx, by + ly, bz + lz, MAGIC_LEAVES, LEAF_META, false);
                        }
                    }
                }
            }
        }
    }

    /** TFGenSortingTree: four blocks of sorting wood with leaves and its engine at y + 1. */
    public static final class Sorting extends TFGen {
        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            Material under = world.material(x, y - 1, z);
            if ((under != Material.GRASS && under != Material.GROUND) || y >= MAXHEIGHT - 12) {
                return false;
            }
            for (int dy = 0; dy < 4; dy++) {
                putBlockAndMetadata(world, x, y + dy, z, MAGIC_LOG, 3, true);
            }
            putLeaves(world, x, y + 2, z);
            putLeaves(world, x, y + 3, z);
            putBlockAndMetadata(world, x, y + 1, z, MAGIC_LOG_SPECIAL, 3, true);
            return true;
        }

        private static void putLeaves(OldWorld world, int bx, int by, int bz) {
            for (int lx = -1; lx <= 1; lx++) {
                for (int ly = -1; ly <= 1; ly++) {
                    for (int lz = -1; lz <= 1; lz++) {
                        if (Math.abs(ly) <= 0 || Math.abs(lx) + Math.abs(lz) <= 1) {
                            putBlockAndMetadata(world, bx + lx, by + ly, bz + lz, MAGIC_LEAVES, 3, false);
                        }
                    }
                }
            }
        }
    }
}
