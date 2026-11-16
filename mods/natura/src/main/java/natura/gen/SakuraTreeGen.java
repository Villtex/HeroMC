package natura.gen;

import hexxit.worldgen152.core.OldMath;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * SakuraTreeGen: the vanilla big oak with sakura wood and leaves, branches turned along their axis.
 *
 * <p>Like the big oak it keeps its height limit from one tree to the next: it is drawn once and afterwards only
 * shrinks when a tree meets an obstacle. The world generator of the original used one instance for the whole
 * session; callers here create one per chunk ({@code OldWorld.chunkState}), saplings one per tree as the
 * original did. An instance must not be shared between threads.
 */
public class SakuraTreeGen extends NaturaGen {
    static final byte[] otherCoordPairs = new byte[] {2, 0, 0, 1, 2, 1};
    Random rand = new Random();
    int[] basePos = new int[] {0, 0, 0};
    int heightLimit = 0;
    int height;
    double heightAttenuation = 0.618;
    double branchDensity = 1.0;
    double branchSlope = 0.381;
    double scaleWidth = 1.0;
    double leafDensity = 1.0;
    int trunkSize = 1;
    int heightLimitLimit = 12;
    int leafDistanceLimit = 4;
    int[][] leafNodes;
    int metaWood;
    int metaLeaves;
    boolean dontFindHeight;

    public SakuraTreeGen(boolean notify, int mdwood, int mdleaves) {
        super(notify);
        this.dontFindHeight = notify;
        this.metaWood = mdwood;
        this.metaLeaves = mdleaves;
    }

    void generateLeafNodeList(OldWorld world) {
        this.height = (int) (this.heightLimit * this.heightAttenuation);
        if (this.height >= this.heightLimit) {
            this.height = this.heightLimit - 1;
        }

        int var1x = (int) (1.382 + Math.pow(this.leafDensity * this.heightLimit / 13.0, 2.0));
        if (var1x < 1) {
            var1x = 1;
        }

        int[][] var2 = new int[var1x * this.heightLimit][4];
        int var3 = this.basePos[1] + this.heightLimit - this.leafDistanceLimit;
        int var4 = 1;
        int var5 = this.basePos[1] + this.height;
        int var6 = var3 - this.basePos[1];
        var2[0][0] = this.basePos[0];
        var2[0][1] = var3;
        var2[0][2] = this.basePos[2];
        var2[0][3] = var5;
        var3--;

        while (var6 >= 0) {
            int var7 = 0;
            float var8 = this.layerSize(var6);
            if (var8 < 0.0F) {
                var3--;
                var6--;
            } else {
                double var9 = 0.5;
                while (var7 < var1x) {
                    double var11 = this.scaleWidth * var8 * (this.rand.nextFloat() + 0.328);
                    double var13 = this.rand.nextFloat() * 2.0 * Math.PI;
                    int var15 = OldMath.floor(var11 * Math.sin(var13) + this.basePos[0] + var9);
                    int var16 = OldMath.floor(var11 * Math.cos(var13) + this.basePos[2] + var9);
                    int[] var17 = new int[] {var15, var3, var16};
                    int[] var18 = new int[] {var15, var3 + this.leafDistanceLimit, var16};
                    if (this.checkBlockLine(var17, var18, world) == -1) {
                        int[] var19 = new int[] {this.basePos[0], this.basePos[1], this.basePos[2]};
                        double var20 = Math.sqrt(Math.pow(Math.abs(this.basePos[0] - var17[0]), 2.0) + Math.pow(Math.abs(this.basePos[2] - var17[2]), 2.0));
                        double var22 = var20 * this.branchSlope;
                        if (var17[1] - var22 > var5) {
                            var19[1] = var5;
                        } else {
                            var19[1] = (int) (var17[1] - var22);
                        }
                        if (this.checkBlockLine(var19, var17, world) == -1) {
                            var2[var4][0] = var15;
                            var2[var4][1] = var3;
                            var2[var4][2] = var16;
                            var2[var4][3] = var19[1];
                            var4++;
                        }
                    }
                    var7++;
                }
                var3--;
                var6--;
            }
        }

        this.leafNodes = new int[var4][4];
        System.arraycopy(var2, 0, this.leafNodes, 0, var4);
    }

    void genTreeLayer(int x, int y, int z, float size, byte loc, int blockID, OldWorld world) {
        int var7x = (int) (size + 0.618);
        byte var8 = otherCoordPairs[loc];
        byte var9 = otherCoordPairs[loc + 3];
        int[] origPos = new int[] {x, y, z};
        int[] leafPos = new int[] {0, 0, 0};
        int var12 = -var7x;
        int var13 = -var7x;

        for (leafPos[loc] = origPos[loc]; var12 <= var7x; var12++) {
            leafPos[var8] = origPos[var8] + var12;
            var13 = -var7x;
            while (var13 <= var7x) {
                double var15 = Math.pow(Math.abs(var12) + 0.5, 2.0) + Math.pow(Math.abs(var13) + 0.5, 2.0);
                if (var15 > size * size) {
                    var13++;
                } else {
                    leafPos[var9] = origPos[var9] + var13;
                    int block = world.getBlockId(leafPos[0], leafPos[1], leafPos[2]);
                    if (block != 0 && !isLeaves(block)) {
                        var13++;
                    } else {
                        this.setBlockAndMetadata(world, leafPos[0], leafPos[1], leafPos[2], blockID, this.metaLeaves);
                        var13++;
                    }
                }
            }
        }
    }

    float layerSize(int par1) {
        if (par1 < this.heightLimit * 0.3) {
            return -1.618F;
        }

        float var2 = this.heightLimit / 2.0F;
        float var3 = this.heightLimit / 2.0F - par1;
        float var4;
        if (var3 == 0.0F) {
            var4 = var2;
        } else if (Math.abs(var3) >= var2) {
            var4 = 0.0F;
        } else {
            var4 = (float) Math.sqrt(Math.pow(Math.abs(var2), 2.0) - Math.pow(Math.abs(var3), 2.0));
        }
        return var4 * 0.5F;
    }

    float leafSize(int par1) {
        return par1 >= 0 && par1 < this.leafDistanceLimit ? (par1 != 0 && par1 != this.leafDistanceLimit - 1 ? 3.0F : 2.0F) : -1.0F;
    }

    void generateLeafNode(int x, int y, int z, OldWorld world) {
        int height = y;
        for (int iter = y + this.leafDistanceLimit; height < iter; height++) {
            float var6 = this.leafSize(height - y);
            this.genTreeLayer(x, height, z, var6, (byte) 1, NaturaIds.FLORA_LEAVES_NO_COLOR, world);
        }
    }

    void placeBlockLine(int[] par1ArrayOfInteger, int[] par2ArrayOfInteger, int blockID, OldWorld world) {
        int[] var4x = new int[] {0, 0, 0};
        byte var5 = 0;
        byte var6 = 0;
        while (var5 < 3) {
            var4x[var5] = par2ArrayOfInteger[var5] - par1ArrayOfInteger[var5];
            if (Math.abs(var4x[var5]) > Math.abs(var4x[var6])) {
                var6 = var5;
            }
            var5++;
        }

        if (var4x[var6] != 0) {
            byte var7 = otherCoordPairs[var6];
            byte var8 = otherCoordPairs[var6 + 3];
            byte var9;
            if (var4x[var6] > 0) {
                var9 = 1;
            } else {
                var9 = -1;
            }

            double var10 = (double) var4x[var7] / var4x[var6];
            double var12 = (double) var4x[var8] / var4x[var6];
            int[] posArray = new int[] {0, 0, 0};
            int var15 = 0;
            for (int var16 = var4x[var6] + var9; var15 != var16; var15 += var9) {
                posArray[var6] = OldMath.floor(par1ArrayOfInteger[var6] + var15 + 0.5);
                posArray[var7] = OldMath.floor(par1ArrayOfInteger[var7] + var15 * var10 + 0.5);
                posArray[var8] = OldMath.floor(par1ArrayOfInteger[var8] + var15 * var12 + 0.5);
                int metadata = this.metaWood;
                int var18 = Math.abs(posArray[0] - par1ArrayOfInteger[0]);
                int var19 = Math.abs(posArray[2] - par1ArrayOfInteger[2]);
                int var20 = Math.max(var18, var19);
                if (var20 > 0) {
                    if (var18 == var20) {
                        metadata += 4;
                    } else if (var19 == var20) {
                        metadata += 8;
                    }
                }
                this.setBlockAndMetadata(world, posArray[0], posArray[1], posArray[2], blockID, metadata);
            }
        }
    }

    void generateLeaves(OldWorld world) {
        for (int iter = 0; iter < this.leafNodes.length; iter++) {
            int posX = this.leafNodes[iter][0];
            int posY = this.leafNodes[iter][1];
            int posZ = this.leafNodes[iter][2];
            this.generateLeafNode(posX, posY, posZ, world);
        }
    }

    boolean leafNodeNeedsBase(int par1) {
        return par1 >= this.heightLimit * 0.2;
    }

    void generateTrunk(OldWorld world) {
        int var1x = this.basePos[0];
        int var2 = this.basePos[1];
        int var3 = this.basePos[1] + this.height;
        int var4 = this.basePos[2];
        int[] var5 = new int[] {var1x, var2, var4};
        int[] var6 = new int[] {var1x, var3, var4};
        this.placeBlockLine(var5, var6, NaturaIds.TREE, world);
        if (this.trunkSize == 2) {
            var5[0]++;
            var6[0]++;
            this.placeBlockLine(var5, var6, NaturaIds.TREE, world);
            var5[2]++;
            var6[2]++;
            this.placeBlockLine(var5, var6, NaturaIds.TREE, world);
            var5[0] += -1;
            var6[0] += -1;
            this.placeBlockLine(var5, var6, NaturaIds.TREE, world);
        }
    }

    void generateLeafNodeBases(OldWorld world) {
        int var1x = 0;
        int var2 = this.leafNodes.length;
        int[] var3 = new int[] {this.basePos[0], this.basePos[1], this.basePos[2]};
        while (var1x < var2) {
            int[] var4 = this.leafNodes[var1x];
            int[] var5 = new int[] {var4[0], var4[1], var4[2]};
            var3[1] = var4[3];
            int var6 = var3[1] - this.basePos[1];
            if (this.leafNodeNeedsBase(var6)) {
                this.placeBlockLine(var3, var5, NaturaIds.TREE, world);
            }
            var1x++;
        }
    }

    int checkBlockLine(int[] par1ArrayOfInteger, int[] par2ArrayOfInteger, OldWorld world) {
        int[] var3x = new int[] {0, 0, 0};
        byte var4 = 0;
        byte var5 = 0;
        while (var4 < 3) {
            var3x[var4] = par2ArrayOfInteger[var4] - par1ArrayOfInteger[var4];
            if (Math.abs(var3x[var4]) > Math.abs(var3x[var5])) {
                var5 = var4;
            }
            var4++;
        }

        if (var3x[var5] == 0) {
            return -1;
        }

        byte var6 = otherCoordPairs[var5];
        byte var7 = otherCoordPairs[var5 + 3];
        byte var8;
        if (var3x[var5] > 0) {
            var8 = 1;
        } else {
            var8 = -1;
        }

        double var9 = (double) var3x[var6] / var3x[var5];
        double var11 = (double) var3x[var7] / var3x[var5];
        int[] var13 = new int[] {0, 0, 0};
        int var14 = 0;
        int var15;
        for (var15 = var3x[var5] + var8; var14 != var15; var14 += var8) {
            var13[var5] = par1ArrayOfInteger[var5] + var14;
            var13[var6] = OldMath.floor(par1ArrayOfInteger[var6] + var14 * var9);
            var13[var7] = OldMath.floor(par1ArrayOfInteger[var7] + var14 * var11);
            int block = world.getBlockId(var13[0], var13[1], var13[2]);
            if (block != 0 && !isLeaves(block)) {
                break;
            }
        }
        return var14 == var15 ? -1 : Math.abs(var14);
    }

    /** The ground the tree stands on: grass or dirt. */
    boolean validSoil(int id) {
        return id == GRASS || id == DIRT;
    }

    boolean validTreeLocation(OldWorld world) {
        int[] var1x = new int[] {this.basePos[0], this.basePos[1], this.basePos[2]};
        int[] var2 = new int[] {this.basePos[0], this.basePos[1] + this.heightLimit - 1, this.basePos[2]};
        int var3 = world.getBlockId(this.basePos[0], this.basePos[1] - 1, this.basePos[2]);
        if (!this.validSoil(var3)) {
            return false;
        }

        int var4 = this.checkBlockLine(var1x, var2, world);
        if (var4 == -1) {
            return true;
        }
        if (var4 < 6) {
            return false;
        }
        this.heightLimit = var4;
        return true;
    }

    @Override
    public void setScale(double par1, double par3, double par5) {
        this.heightLimitLimit = (int) (par1 * 12.0);
        if (par1 > 0.5) {
            this.leafDistanceLimit = 5;
        }
        this.scaleWidth = par3;
        this.leafDensity = par5;
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
        return height + 1;
    }

    @Override
    public boolean generate(OldWorld world, Random random, int x, int y, int z) {
        long var6 = random.nextLong();
        this.rand.setSeed(var6);
        this.basePos[0] = x;
        if (this.dontFindHeight) {
            this.basePos[1] = y;
        } else {
            this.basePos[1] = this.findGround(world, x, y, z);
        }
        this.basePos[2] = z;
        if (this.heightLimit == 0) {
            this.heightLimit = 5 + this.rand.nextInt(this.heightLimitLimit);
        }

        if (!this.validTreeLocation(world)) {
            return false;
        }

        this.generateLeafNodeList(world);
        this.generateLeaves(world);
        this.generateTrunk(world);
        this.generateLeafNodeBases(world);
        return true;
    }
}
