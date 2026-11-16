package natura.gen;

import hexxit.worldgen152.core.OldMath;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * RedwoodTreeGen: the giant redwood, 80 to 139 blocks high. A trunk of rings that narrow towards the top (13 to
 * 3 blocks across), roots below the ground and big-oak style branch clusters on the upper half. Converted line by
 * line from the original; the block lists of the rings and roots are unchanged.
 *
 * <p>The world generators of the overworld never call it ({@code BaseTreeWorldgen} only creates it); it grows from
 * redwood saplings. An instance holds the tree it is working on: one instance per tree, not shared between threads.
 */
public class RedwoodTreeGen extends NaturaGen {
    static final byte[] otherCoordPairs = new byte[]{2, 0, 0, 1, 2, 1};
    Random rand;
    OldWorld worldObj;
    int[] basePos = new int[]{0, 0, 0};
    int heightLimit;
    int height;
    double heightAttenuation;
    double field_875_h;
    double field_874_i;
    double field_873_j;
    double field_872_k;
    int trunkSize;
    int heightLimitLimit;
    int leafDistanceLimit;
    int[][] leafNodes;
    int genWoodID;
    int genWoodMetadata = 0;
    boolean useHeight;

    public RedwoodTreeGen(boolean flag, int bID) {
        super(flag);
        this.useHeight = flag;
        this.rand = new Random();
        this.heightLimit = 0;
        this.heightAttenuation = 0.618;
        this.field_875_h = 1.0;
        this.field_874_i = 0.381;
        this.field_873_j = 1.0;
        this.field_872_k = 1.0;
        this.trunkSize = 1;
        this.heightLimitLimit = 12;
        this.leafDistanceLimit = 4;
        this.genWoodID = bID;
    }

    int findGround(OldWorld world, int x, int y, int z) {
        boolean foundGround = false;
        int height = NaturaIds.SEA_LEVEL + 64;

        do {
            int underID = world.getBlockId(x, --height, z);
            if (underID == DIRT || underID == GRASS || height < NaturaIds.SEA_LEVEL) {
                foundGround = true;
            }
        } while (!foundGround);

        return height;
    }

    public boolean isValidSpawn(OldWorld world, int x, int y, int z) {
        int bID = world.getBlockId(x, y, z);
        boolean ground = bID == DIRT || bID == GRASS;
        boolean transparent = !opaqueCube(world.getBlockId(x, y + 1, z));
        if (ground && transparent) {
            boolean valid = true;
        } else {
            boolean valid = false;
        }

        return ground && transparent;
    }

    @Override
    public boolean generate(OldWorld world, Random random, int x, int yPos, int z) {
        int groundPoint = yPos;
        if (!this.useHeight) {
            groundPoint = this.findGround(world, x, yPos, z);
            if (!this.isValidSpawn(world, x, groundPoint, z)) {
                return false;
            }
        }

        int treeHeight = random.nextInt(60) + 80;
        this.worldObj = world;
        long ran = random.nextLong();
        this.rand.setSeed(ran);
        this.basePos[0] = x;
        this.basePos[1] = groundPoint;
        this.basePos[2] = z;
        this.heightLimit = 5 + this.rand.nextInt(this.heightLimitLimit);
        if (treeHeight > 120) {
            for (int currentHeight = 0; currentHeight < treeHeight; currentHeight++) {
                if (currentHeight < treeHeight * 1 / 10) {
                    this.genRing13(world, random, x, currentHeight + groundPoint, z);
                } else if (currentHeight < treeHeight * 2 / 10) {
                    this.genRing12(world, random, x, currentHeight + groundPoint, z);
                } else if (currentHeight < treeHeight * 3 / 10) {
                    this.genRing11(world, random, x, currentHeight + groundPoint, z);
                } else if (currentHeight < treeHeight * 4 / 10) {
                    this.genRing10(world, random, x, currentHeight + groundPoint, z);
                } else if (currentHeight < treeHeight * 5 / 10) {
                    this.genRing9(world, random, x, currentHeight + groundPoint, z);
                    this.growLowBranch(world, random, x, currentHeight + groundPoint, z);
                } else if (currentHeight < treeHeight * 6 / 10) {
                    this.genRing8(world, random, x, currentHeight + groundPoint, z);
                    this.growLowBranch(world, random, x, currentHeight + groundPoint, z);
                } else if (currentHeight < treeHeight * 7 / 10) {
                    this.genRing7(world, random, x, currentHeight + groundPoint, z);
                    this.growMiddleBranch(world, random, x, currentHeight + groundPoint, z);
                } else if (currentHeight < treeHeight * 8 / 10) {
                    this.genRing6(world, random, x, currentHeight + groundPoint, z);
                    this.growMiddleBranch(world, random, x, currentHeight + groundPoint, z);
                } else if (currentHeight < treeHeight * 9 / 10) {
                    this.genRing5(world, random, x, currentHeight + groundPoint, z);
                    this.growHighBranch(world, random, x, currentHeight + groundPoint, z);
                } else {
                    this.genRing3(world, random, x, currentHeight + groundPoint, z);
                    this.growHighBranch(world, random, x, currentHeight + groundPoint, z);
                }
            }

            this.growBigRoots(world, random, x, groundPoint - 1, z);
            this.growTop(world, random, x, this.height + groundPoint, z);
        } else if (treeHeight > 100) {
            for (int currentHeight = 0; currentHeight < treeHeight; currentHeight++) {
                if (currentHeight < treeHeight * 1 / 8) {
                    this.genRing11(world, random, x, currentHeight + groundPoint, z);
                } else if (currentHeight < treeHeight * 2 / 8) {
                    this.genRing10(world, random, x, currentHeight + groundPoint, z);
                } else if (currentHeight < treeHeight * 3 / 8) {
                    this.genRing9(world, random, x, currentHeight + groundPoint, z);
                } else if (currentHeight < treeHeight * 4 / 8) {
                    this.genRing8(world, random, x, currentHeight + groundPoint, z);
                } else if (currentHeight < treeHeight * 5 / 8) {
                    this.genRing7(world, random, x, currentHeight + groundPoint, z);
                    this.growMiddleBranch(world, random, x, currentHeight + groundPoint, z);
                } else if (currentHeight < treeHeight * 6 / 8) {
                    this.genRing6(world, random, x, currentHeight + groundPoint, z);
                    this.growMiddleBranch(world, random, x, currentHeight + groundPoint, z);
                } else if (currentHeight < treeHeight * 7 / 8) {
                    this.genRing5(world, random, x, currentHeight + groundPoint, z);
                    this.growHighBranch(world, random, x, currentHeight + groundPoint, z);
                } else {
                    this.genRing3(world, random, x, currentHeight + groundPoint, z);
                    this.growHighBranch(world, random, x, currentHeight + groundPoint, z);
                }
            }

            this.growMediumRoots(world, random, x, groundPoint - 1, z);
            this.growTop(world, random, x, this.height + groundPoint, z);
        } else {
            for (int currentHeight = 0; currentHeight < treeHeight; currentHeight++) {
                if (currentHeight < treeHeight * 1 / 6) {
                    this.genRing9(world, random, x, currentHeight + groundPoint, z);
                } else if (currentHeight < treeHeight * 2 / 6) {
                    this.genRing8(world, random, x, currentHeight + groundPoint, z);
                } else if (currentHeight < treeHeight * 3 / 6) {
                    this.genRing7(world, random, x, currentHeight + groundPoint, z);
                } else if (currentHeight < treeHeight * 4 / 6) {
                    this.genRing6(world, random, x, currentHeight + groundPoint, z);
                    this.growMiddleBranch(world, random, x, currentHeight + groundPoint, z);
                } else if (currentHeight < treeHeight * 5 / 6) {
                    this.genRing5(world, random, x, currentHeight + groundPoint, z);
                    this.growHighBranch(world, random, x, currentHeight + groundPoint, z);
                } else {
                    this.genRing3(world, random, x, currentHeight + groundPoint, z);
                    this.growHighBranch(world, random, x, currentHeight + groundPoint, z);
                }
            }

            this.growSmallRoots(world, random, x, groundPoint - 1, z);
            this.growTop(world, random, x, this.height + groundPoint, z);
        }

        return true;
    }

    public boolean growTop(OldWorld world, Random random, int x, int y, int z) {
        this.basePos[0] = x;
        this.basePos[1] = y + 4;
        this.basePos[2] = z;
        this.generateLeafNodeList();
        this.generateLeaves();
        this.generateLeafNodeBases();
        this.basePos[0] = x;
        this.basePos[1] = y + 4;
        this.basePos[2] = z;
        this.generateLeafNodeList();
        this.generateLeaves();
        this.generateLeafNodeBases();
        this.basePos[0] = x;
        this.basePos[1] = y;
        this.basePos[2] = z;
        this.generateLeafNodeList();
        this.generateLeaves();
        this.generateLeafNodeBases();
        this.basePos[0] = x;
        this.basePos[1] = y;
        this.basePos[2] = z;
        this.generateLeafNodeList();
        this.generateLeaves();
        this.generateLeafNodeBases();
        return false;
    }

    public boolean growHighBranch(OldWorld world, Random random, int x, int y, int z) {
        for (int iter = 0; iter < 3; iter++) {
            this.basePos[0] = x + random.nextInt(21) - 10;
            this.basePos[1] = y;
            this.basePos[2] = z + random.nextInt(21) - 10;
            this.generateLeafNodeList();
            this.generateLeaves();
            this.generateLeafNodeBases();
        }

        return false;
    }

    public boolean growMiddleBranch(OldWorld world, Random random, int x, int y, int z) {
        for (int iter = 0; iter < 6; iter++) {
            this.basePos[0] = x + random.nextInt(31) - 15;
            this.basePos[1] = y;
            this.basePos[2] = z + random.nextInt(31) - 15;
            this.generateLeafNodeList();
            this.generateLeaves();
            this.generateLeafNodeBases();
        }

        return false;
    }

    public boolean growLowBranch(OldWorld world, Random random, int i, int j, int k) {
        this.basePos[0] = i + random.nextInt(17) - 8;
        this.basePos[1] = j;
        this.basePos[2] = k + random.nextInt(17) - 8;
        this.generateLeafNodeList();
        this.generateLeaves();
        this.generateLeafNodeBases();
        if (random.nextInt(2) == 0) {
            this.basePos[0] = i + random.nextInt(17) - 8;
            this.basePos[1] = j;
            this.basePos[2] = k + random.nextInt(17) - 8;
            this.generateLeafNodeList();
            this.generateLeaves();
            this.generateLeafNodeBases();
        }

        this.basePos[0] = i + random.nextInt(17) - 8;
        this.basePos[1] = j;
        this.basePos[2] = k + random.nextInt(17) - 8;
        this.generateLeafNodeList();
        this.generateLeaves();
        this.generateLeafNodeBases();
        return false;
    }

    public boolean growSmallRoots(OldWorld world, Random random, int i, int j, int k) {
        this.genRing9(world, random, i, j, k);
        this.smallRoot1(world, random, i, j - 1, k);
        this.smallRoot1(world, random, i, j - 2, k);
        this.smallRoot1(world, random, i, j - 3, k);
        this.smallRoot2(world, random, i, j - 4, k);
        this.smallRoot2(world, random, i, j - 5, k);
        this.smallRoot3(world, random, i, j - 6, k);
        this.smallRoot3(world, random, i, j - 7, k);
        this.smallRoot3(world, random, i, j - 8, k);
        this.smallRoot3(world, random, i, j - 9, k);
        this.smallRoot4(world, random, i, j - 10, k);
        this.smallRoot4(world, random, i, j - 11, k);
        return true;
    }

    public boolean growMediumRoots(OldWorld world, Random random, int i, int j, int k) {
        this.genRing11(world, random, i, j, k);
        this.mediumRoot1(world, random, i, j - 1, k);
        this.mediumRoot1(world, random, i, j - 2, k);
        this.mediumRoot1(world, random, i, j - 3, k);
        this.mediumRoot2(world, random, i, j - 4, k);
        this.mediumRoot2(world, random, i, j - 5, k);
        this.mediumRoot3(world, random, i, j - 6, k);
        this.mediumRoot3(world, random, i, j - 7, k);
        this.mediumRoot3(world, random, i, j - 8, k);
        this.mediumRoot3(world, random, i, j - 9, k);
        this.mediumRoot4(world, random, i, j - 10, k);
        this.mediumRoot4(world, random, i, j - 11, k);
        this.mediumRoot5(world, random, i, j - 12, k);
        this.mediumRoot5(world, random, i, j - 13, k);
        this.mediumRoot5(world, random, i, j - 14, k);
        return true;
    }

    public boolean growBigRoots(OldWorld world, Random random, int i, int j, int k) {
        this.genRing13(world, random, i, j, k);
        this.bigRoot1(world, random, i, j - 1, k);
        this.bigRoot1(world, random, i, j - 2, k);
        this.bigRoot1(world, random, i, j - 3, k);
        this.bigRoot2(world, random, i, j - 4, k);
        this.bigRoot2(world, random, i, j - 5, k);
        this.bigRoot3(world, random, i, j - 6, k);
        this.bigRoot3(world, random, i, j - 7, k);
        this.bigRoot3(world, random, i, j - 8, k);
        this.bigRoot3(world, random, i, j - 9, k);
        this.bigRoot4(world, random, i, j - 10, k);
        this.bigRoot4(world, random, i, j - 11, k);
        this.bigRoot5(world, random, i, j - 12, k);
        this.bigRoot5(world, random, i, j - 13, k);
        this.bigRoot5(world, random, i, j - 14, k);
        this.bigRoot6(world, random, i, j - 15, k);
        this.bigRoot6(world, random, i, j - 16, k);
        this.bigRoot6(world, random, i, j - 17, k);
        this.bigRoot6(world, random, i, j - 18, k);
        return true;
    }

    public boolean smallRoot1(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 4, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
        }

        return true;
    }

    public boolean smallRoot2(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 4, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
        }

        return true;
    }

    public boolean smallRoot3(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
        }

        return true;
    }

    public boolean smallRoot4(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
        }

        return true;
    }

    public boolean mediumRoot1(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 5, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 5, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 5, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 5, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
        }

        return true;
    }

    public boolean mediumRoot2(OldWorld world, Random random, int i, int j, int k) {
        this.setBlockAndMetadata(world, i - 4, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i - 4, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i - 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i - 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i - 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i - 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i - 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i - 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i - 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i - 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i - 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i - 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i - 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i - 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i - 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i - 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i - 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i - 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i - 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i - 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i + 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i + 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i + 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i + 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i + 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i + 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i + 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i + 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i + 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i + 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i + 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i + 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i + 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i + 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i + 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i + 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i + 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i + 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i + 4, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
        this.setBlockAndMetadata(world, i + 4, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
        return true;
    }

    public boolean mediumRoot3(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
        }

        return true;
    }

    public boolean mediumRoot4(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
        }

        return true;
    }

    public boolean mediumRoot5(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
        }

        return true;
    }

    public boolean bigRoot1(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 6, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 6, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 6, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 6, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 5, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 5, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 5, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 5, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 5, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 5, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 5, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 5, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 6, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 6, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 6, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 6, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 6, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 6, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 6, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 6, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 5, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 5, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 5, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 5, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 5, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 5, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 5, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 5, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 6, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 6, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 6, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 6, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
        }

        return true;
    }

    public boolean bigRoot2(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 5, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 5, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 5, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 5, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 5, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 5, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 5, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 5, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 6, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 6, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 6, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 6, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 6, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 6, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 6, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 6, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 5, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 5, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 5, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 5, j, k - 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 5, j, k + 1, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 5, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 5, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 5, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
        }

        return true;
    }

    public boolean bigRoot3(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 4, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k - 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k + 2, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
        }

        return true;
    }

    public boolean bigRoot4(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 4, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 4, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 5, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 4, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
        }

        return true;
    }

    public boolean bigRoot5(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 3, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 3, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 3, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
        }

        return true;
    }

    public boolean bigRoot6(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 2, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i - 2, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k - 4, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 2);
            this.setBlockAndMetadata(world, i + 2, j, k + 4, this.genWoodID, this.genWoodMetadata + 2);
        }

        return true;
    }

    public boolean genRing13(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 6, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 6, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 6, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 6, j, k + 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 6, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 5, j, k - 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 5, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 5, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 5, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 5, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 5, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 5, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 5, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 5, j, k + 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 4, j, k - 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 4, j, k - 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 4, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 4, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 4, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 4, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 4, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 4, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 4, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 4, j, k + 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 4, j, k + 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k - 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k - 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k + 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k + 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k - 6, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k - 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k - 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k + 6, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k - 6, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k - 5, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 5, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 6, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 6, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 5, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k - 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 5, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 6, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k - 6, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k - 5, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 5, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 6, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k - 6, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k - 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k - 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k + 6, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k - 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k - 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k + 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k + 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 4, j, k - 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 4, j, k - 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 4, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 4, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 4, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 4, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 4, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 4, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 4, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 4, j, k + 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 4, j, k + 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 5, j, k - 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 5, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 5, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 5, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 5, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 5, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 5, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 5, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 5, j, k + 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 6, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 6, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 6, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 6, j, k + 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 6, j, k + 2, this.genWoodID, this.genWoodMetadata);
        }

        return true;
    }

    public boolean genRing12(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 6, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 6, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 6, j, k + 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 5, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 5, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 5, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 5, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 5, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 5, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 5, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 4, j, k - 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 4, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 4, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 4, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 4, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 4, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 4, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 4, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 4, j, k + 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k - 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k - 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k + 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k + 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k - 6, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k - 5, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k - 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 5, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 6, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k - 6, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k - 5, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 5, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 6, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 6, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 5, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k - 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 5, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 6, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k - 6, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k - 5, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 5, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 6, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k - 6, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k - 5, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k - 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 5, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 6, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k - 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k - 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k + 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k + 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 4, j, k - 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 4, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 4, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 4, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 4, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 4, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 4, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 4, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 4, j, k + 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 5, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 5, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 5, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 5, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 5, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 5, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 5, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 6, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 6, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 6, j, k + 1, this.genWoodID, this.genWoodMetadata);
        }

        return true;
    }

    public boolean genRing11(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 5, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 5, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 5, j, k + 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 4, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 4, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 4, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 4, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 4, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 4, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 4, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k - 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k + 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k - 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k - 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k - 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k - 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 4, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 5, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k - 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k - 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k + 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 4, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 4, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 4, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 4, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 4, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 4, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 4, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 5, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 5, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 5, j, k + 1, this.genWoodID, this.genWoodMetadata);
        }

        return true;
    }

    public boolean genRing10(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 4, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 4, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 4, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 4, j, k + 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 4, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k - 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k + 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k - 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k - 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k - 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k + 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 4, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 4, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 4, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 4, j, k + 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 4, j, k + 2, this.genWoodID, this.genWoodMetadata);
        }

        return true;
    }

    public boolean genRing9(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 4, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 4, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 4, j, k + 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 3, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k - 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k - 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k - 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 3, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 4, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 3, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 4, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 4, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 4, j, k + 1, this.genWoodID, this.genWoodMetadata);
        }

        return true;
    }

    public boolean genRing8(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 3, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k + 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k + 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k + 2, this.genWoodID, this.genWoodMetadata);
        }

        return true;
    }

    public boolean genRing7(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 3, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 3, j, k + 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 2, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k - 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k - 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 2, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 3, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 2, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 3, j, k + 1, this.genWoodID, this.genWoodMetadata);
        }

        return true;
    }

    public boolean genRing6(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 2, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k + 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k + 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k + 2, this.genWoodID, this.genWoodMetadata);
        }

        return true;
    }

    public boolean genRing5(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 2, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k + 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i + 1, j, k + 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 2, j, k + 1, this.genWoodID, this.genWoodMetadata);
        }

        return true;
    }

    public boolean genRing4(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 2, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 2, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i - 1, j, k + 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 2, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 1, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k, this.genWoodID, this.genWoodMetadata);
        }

        return true;
    }

    public boolean genRing3s(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 1, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k + 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k, this.genWoodID, this.genWoodMetadata);
        }

        return true;
    }

    public boolean genRing3(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 1, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k + 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k, this.genWoodID, this.genWoodMetadata + 1);
            this.setBlockAndMetadata(world, i, j, k + 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i + 1, j, k + 1, this.genWoodID, this.genWoodMetadata);
        }

        return true;
    }

    public boolean genRing2(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i - 1, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i - 1, j, k, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k - 1, this.genWoodID, this.genWoodMetadata);
            this.setBlockAndMetadata(world, i, j, k, this.genWoodID, this.genWoodMetadata);
        }

        return true;
    }

    public boolean genRing1(OldWorld world, Random random, int i, int j, int k) {
        if (world.getBlockId(i, j, k) != BEDROCK && j > 0) {
            this.setBlockAndMetadata(world, i, j, k, this.genWoodID, this.genWoodMetadata);
        }

        return true;
    }

    void generateLeafNodeList() {
        this.height = (int)(this.heightLimit * this.heightAttenuation);
        if (this.height >= this.heightLimit) {
            this.height = this.heightLimit - 1;
        }

        int i = (int)(1.382 + Math.pow(this.field_872_k * this.heightLimit / 13.0, 2.0));
        if (i < 1) {
            i = 1;
        }

        int[][] ai = new int[i * this.heightLimit][4];
        int j = this.basePos[1] + this.heightLimit - this.leafDistanceLimit;
        int k = 1;
        int l = this.basePos[1] + this.height;
        int i1 = j - this.basePos[1];
        ai[0][0] = this.basePos[0];
        ai[0][1] = j;
        ai[0][2] = this.basePos[2];
        ai[0][3] = l;
        j--;

        while (i1 >= 0) {
            int j1 = 0;
            float f = this.func_528_a(i1);
            if (f < 0.0F) {
                j--;
                i1--;
            } else {
                double d = 0.5;

                while (j1 < i) {
                    double d1 = this.field_873_j * (f * (this.rand.nextFloat() + 0.328));
                    double d2 = this.rand.nextFloat() * 2.0 * 3.14159;
                    int k1 = OldMath.floor(d1 * Math.sin(d2) + this.basePos[0] + d);
                    int l1 = OldMath.floor(d1 * Math.cos(d2) + this.basePos[2] + d);
                    int[] ai1 = new int[]{k1, j, l1};
                    int[] ai2 = new int[]{k1, j + this.leafDistanceLimit, l1};
                    if (this.checkBlockLine(ai1, ai2) == -1) {
                        int[] ai3 = new int[]{this.basePos[0], this.basePos[1], this.basePos[2]};
                        double d3 = Math.sqrt(Math.pow(Math.abs(this.basePos[0] - ai1[0]), 2.0) + Math.pow(Math.abs(this.basePos[2] - ai1[2]), 2.0));
                        double d4 = d3 * this.field_874_i;
                        if (ai1[1] - d4 > l) {
                            ai3[1] = l;
                        } else {
                            ai3[1] = (int)(ai1[1] - d4);
                        }

                        if (this.checkBlockLine(ai3, ai1) == -1) {
                            ai[k][0] = k1;
                            ai[k][1] = j;
                            ai[k][2] = l1;
                            ai[k][3] = ai3[1];
                            k++;
                        }
                    }

                    j1++;
                }

                j--;
                i1--;
            }
        }

        this.leafNodes = new int[k][4];
        System.arraycopy(ai, 0, this.leafNodes, 0, k);
    }

    void func_523_a(int i, int j, int k, float f, byte byte0, int l) {
        int i1 = (int)(f + 0.618);
        byte byte1 = otherCoordPairs[byte0];
        byte byte2 = otherCoordPairs[byte0 + 3];
        int[] ai = new int[]{i, j, k};
        int[] ai1 = new int[]{0, 0, 0};
        int j1 = -i1;
        int k1 = -i1;

        for (ai1[byte0] = ai[byte0]; j1 <= i1; j1++) {
            ai1[byte1] = ai[byte1] + j1;
            int l1 = -i1;

            while (l1 <= i1) {
                double d = Math.sqrt(Math.pow(Math.abs(j1) + 0.5, 2.0) + Math.pow(Math.abs(l1) + 0.5, 2.0));
                if (d > f) {
                    l1++;
                } else {
                    ai1[byte2] = ai[byte2] + l1;
                    int i2 = this.worldObj.getBlockId(ai1[0], ai1[1], ai1[2]);
                    if (i2 != 0 && i2 != 18) {
                        l1++;
                    } else {
                        this.setBlockAndMetadata(this.worldObj, ai1[0], ai1[1], ai1[2], l, 0);
                        l1++;
                    }
                }
            }
        }
    }

    float func_528_a(int i) {
        if (i < this.heightLimit * 0.3) {
            return -1.618F;
        }

        float f = this.heightLimit / 2.0F;
        float f1 = this.heightLimit / 2.0F - i;
        float f2;
        if (f1 == 0.0F) {
            f2 = f;
        } else if (Math.abs(f1) >= f) {
            f2 = 0.0F;
        } else {
            f2 = (float)Math.sqrt(Math.pow(Math.abs(f), 2.0) - Math.pow(Math.abs(f1), 2.0));
        }

        return f2 * 0.5F;
    }

    float func_526_b(int i) {
        if (i >= 0 && i < this.leafDistanceLimit) {
            return i != 0 && i != this.leafDistanceLimit - 1 ? 3.0F : 2.0F;
        } else {
            return -1.0F;
        }
    }

    void generateLeafNode(int i, int j, int k) {
        int l = j;

        for (int i1 = j + this.leafDistanceLimit; l < i1; l++) {
            float f = this.func_526_b(l - j);
            this.func_523_a(i, l, k, f, (byte)1, NaturaIds.FLORA_LEAVES);
        }
    }

    void placeBlockLine(int[] ai, int[] ai1, int i) {
        int[] ai2 = new int[]{0, 0, 0};
        byte byte0 = 0;
        int j = 0;

        while (byte0 < 3) {
            ai2[byte0] = ai1[byte0] - ai[byte0];
            if (Math.abs(ai2[byte0]) > Math.abs(ai2[j])) {
                j = byte0;
            }

            byte0++;
        }

        if (ai2[j] != 0) {
            byte byte1 = otherCoordPairs[j];
            byte byte2 = otherCoordPairs[j + 3];
            byte byte3;
            if (ai2[j] > 0) {
                byte3 = 1;
            } else {
                byte3 = -1;
            }

            double d = (double)ai2[byte1] / ai2[j];
            double d1 = (double)ai2[byte2] / ai2[j];
            int[] ai3 = new int[]{0, 0, 0};
            int k = 0;

            for (int l = ai2[j] + byte3; k != l; k += byte3) {
                ai3[j] = OldMath.floor(ai[j] + k + 0.5);
                ai3[byte1] = OldMath.floor(ai[byte1] + k * d + 0.5);
                ai3[byte2] = OldMath.floor(ai[byte2] + k * d1 + 0.5);
                this.setBlockAndMetadata(this.worldObj, ai3[0], ai3[1], ai3[2], i, 0);
            }
        }
    }

    void generateLeaves() {
        for (int iter = 0; iter < this.leafNodes.length; iter++) {
            int posX = this.leafNodes[iter][0];
            int posY = this.leafNodes[iter][1];
            int posZ = this.leafNodes[iter][2];
            this.generateLeafNode(posX, posY, posZ);
        }
    }

    boolean leafNodeNeedsBase(int i) {
        return i >= this.heightLimit * 0.2;
    }

    void generateLeafNodeBases() {
        int i = 0;
        int j = this.leafNodes.length;
        int[] ai = new int[]{this.basePos[0], this.basePos[1], this.basePos[2]};

        while (i < j) {
            int[] ai1 = this.leafNodes[i];
            int[] ai2 = new int[]{ai1[0], ai1[1], ai1[2]};
            ai[1] = ai1[3];
            int k = ai[1] - this.basePos[1];
            if (this.leafNodeNeedsBase(k)) {
                this.placeBlockLine(ai, ai2, NaturaIds.REDWOOD);
            }

            i++;
        }
    }

    int checkBlockLine(int[] ai, int[] ai1) {
        int[] ai2 = new int[]{0, 0, 0};
        byte byte0 = 0;
        int i = 0;

        while (byte0 < 3) {
            ai2[byte0] = ai1[byte0] - ai[byte0];
            if (Math.abs(ai2[byte0]) > Math.abs(ai2[i])) {
                i = byte0;
            }

            byte0++;
        }

        if (ai2[i] == 0) {
            return -1;
        }

        byte byte1 = otherCoordPairs[i];
        byte byte2 = otherCoordPairs[i + 3];
        byte byte3;
        if (ai2[i] > 0) {
            byte3 = 1;
        } else {
            byte3 = -1;
        }

        double d = (double)ai2[byte1] / ai2[i];
        double d1 = (double)ai2[byte2] / ai2[i];
        int[] ai3 = new int[]{0, 0, 0};
        int j = 0;

        int k;
        for (k = ai2[i] + byte3; j != k; j += byte3) {
            ai3[i] = ai[i] + j;
            ai3[byte1] = OldMath.floor(ai[byte1] + j * d);
            ai3[byte2] = OldMath.floor(ai[byte2] + j * d1);
            int l = this.worldObj.getBlockId(ai3[0], ai3[1], ai3[2]);
            if (l != 0 && l != 18) {
                break;
            }
        }

        return j == k ? -1 : Math.abs(j);
    }

    boolean validTreeLocation() {
        int[] ai = new int[]{this.basePos[0], this.basePos[1], this.basePos[2]};
        int[] ai1 = new int[]{this.basePos[0], this.basePos[1] + this.heightLimit - 1, this.basePos[2]};
        int i = this.worldObj.getBlockId(this.basePos[0], this.basePos[1] - 1, this.basePos[2]);
        if (i != 2 && i != 3) {
            return false;
        }

        int j = this.checkBlockLine(ai, ai1);
        if (j == -1) {
            return true;
        }

        if (j < 6) {
            return false;
        }

        this.heightLimit = j;
        return true;
    }

    public void func_517_a(double d, double d1, double d2) {
        this.heightLimitLimit = (int)(d * 12.0);
        if (d > 0.5) {
            this.leafDistanceLimit = 5;
        }

        this.field_873_j = d1;
        this.field_872_k = d2;
    }
}
