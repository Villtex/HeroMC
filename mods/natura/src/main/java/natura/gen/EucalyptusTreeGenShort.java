package natura.gen;

import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/** EucalyptusTreeGenShort: a thin trunk with eight branches that end in flat leaf nodes. */
public class EucalyptusTreeGenShort extends NaturaGen {
    private final int mdWood;
    private final int mdLeaves;

    public EucalyptusTreeGenShort(int metadata, int leavesMetadata) {
        this.mdWood = metadata;
        this.mdLeaves = leavesMetadata;
    }

    @Override
    public boolean generate(OldWorld world, Random random, int x, int y, int z) {
        int height = this.findGround(world, x, y, z);
        this.generateRandomTree(world, random, x, height, z);
        return true;
    }

    int findGround(OldWorld world, int x, int y, int z) {
        int l = 0;
        int i1 = world.getBlockId(x, y - 1, z);
        if (opaqueCube(world.getBlockId(x, y, z)) || i1 != GRASS && i1 != DIRT) {
            int k1 = 96;
            while (k1 >= 32) {
                int j1 = world.getBlockId(x, k1, z);
                if ((j1 == GRASS || j1 == DIRT) && !opaqueCube(world.getBlockId(x, k1 + 1, z))) {
                    l = k1 + 1;
                    break;
                }
                if (--k1 <= 0) {
                    break;
                }
            }
            return l;
        } else {
            return y;
        }
    }

    public boolean generateRandomTree(OldWorld world, Random random, int posX, int posY, int posZ) {
        int height = random.nextInt(3) + 6;
        boolean flag = true;
        if (posY >= 1 && posY + height + 1 <= 256) {
            for (int i1 = posY; i1 <= posY + 1 + height; i1++) {
                byte byte0 = 1;
                if (i1 == posY) {
                    byte0 = 0;
                }
                if (i1 >= posY + 1 + height - 2) {
                    byte0 = 2;
                }
                for (int l1 = posX - byte0; l1 <= posX + byte0 && flag; l1++) {
                    for (int j2 = posZ - byte0; j2 <= posZ + byte0 && flag; j2++) {
                        if (i1 < 0 || i1 >= 256) {
                            flag = false;
                            break;
                        }
                        int k2 = world.getBlockId(l1, i1, j2);
                        if (k2 != 0 && k2 != NaturaIds.FLORA_LEAVES) {
                            flag = false;
                            break;
                        }
                    }
                }
            }
            if (!flag) {
                return false;
            }

            int j1 = world.getBlockId(posX, posY - 1, posZ);
            if ((j1 == GRASS || j1 == DIRT) && posY < 256 - height - 1) {
                world.setBlock(posX, posY - 1, posZ, DIRT);
                for (int k1 = 0; k1 < height; k1++) {
                    int i2 = world.getBlockId(posX, posY + k1, posZ);
                    if (i2 == 0 || i2 == NaturaIds.FLORA_LEAVES) {
                        this.setBlockAndMetadata(world, posX, posY + k1, posZ, NaturaIds.TREE, this.mdWood);
                    }
                }
                this.genBranch(world, random, posX, posY, posZ, height, 1);
                this.genBranch(world, random, posX, posY, posZ, height, 2);
                this.genBranch(world, random, posX, posY, posZ, height, 3);
                this.genBranch(world, random, posX, posY, posZ, height, 4);
                this.genStraightBranch(world, random, posX, posY, posZ, height, 1);
                this.genStraightBranch(world, random, posX, posY, posZ, height, 2);
                this.genStraightBranch(world, random, posX, posY, posZ, height, 3);
                this.genStraightBranch(world, random, posX, posY, posZ, height, 4);
                this.generateNode(world, random, posX, posY + height, posZ);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void genBranch(OldWorld world, Random random, int x, int y, int z, int height, int direction) {
        int posX = x;
        int posY = y + height - 3;
        int posZ = z;
        byte byte0 = 0;
        byte byte1 = 0;
        switch (direction) {
            case 1:
                byte0 = 1;
                byte1 = 1;
                break;
            case 2:
                byte0 = -1;
                byte1 = 1;
                break;
            case 3:
                byte0 = 1;
                byte1 = -1;
                break;
            case 4:
                byte0 = -1;
                byte1 = -1;
        }

        int heightShift = random.nextInt(6);
        for (int bIter = 4; bIter > 0; bIter--) {
            if (heightShift % 3 != 0) {
                posX += byte0;
            }
            if (heightShift % 3 != 1) {
                posZ += byte1;
            }
            int branch = heightShift % 3;
            posY += branch;
            if (branch == 2) {
                this.setBlockAndMetadata(world, posX, posY - 1, posZ, NaturaIds.TREE, this.mdWood);
            }
            this.setBlockAndMetadata(world, posX, posY, posZ, NaturaIds.TREE, this.mdWood);
            if (bIter == 1) {
                this.generateNode(world, random, posX, posY, posZ);
            }
            heightShift = random.nextInt(6);
        }
    }

    private void genStraightBranch(OldWorld world, Random random, int x, int y, int z, int height, int direction) {
        int posX = x;
        int posY = y + height - 3;
        int posZ = z;
        byte xShift = 0;
        byte zShift = 0;
        switch (direction) {
            case 1:
                xShift = 1;
                zShift = 0;
                break;
            case 2:
                xShift = 0;
                zShift = 1;
                break;
            case 3:
                xShift = -1;
                zShift = 0;
                break;
            case 4:
                xShift = 0;
                zShift = -1;
        }

        int heightShift = random.nextInt(6);
        for (int j2 = 4; j2 > 0; j2--) {
            if (xShift == 0) {
                posX = posX + random.nextInt(3) - 1;
                posZ += zShift;
            }
            if (zShift == 0) {
                posX += xShift;
                posZ = posZ + random.nextInt(3) - 1;
            }
            int branch = heightShift % 3;
            posY += branch;
            if (branch == 2) {
                this.setBlockAndMetadata(world, posX, posY - 1, posZ, NaturaIds.TREE, this.mdWood);
            }
            this.setBlockAndMetadata(world, posX, posY, posZ, NaturaIds.TREE, this.mdWood);
            if (j2 == 1) {
                this.generateNode(world, random, posX, posY, posZ);
            }
            heightShift = random.nextInt(6);
        }
    }

    public boolean generateNode(OldWorld world, Random random, int x, int y, int z) {
        this.setBlockAndMetadata(world, x, y, z, NaturaIds.TREE, this.mdWood);
        for (int xIter = x - 2; xIter <= x + 2; xIter++) {
            for (int zIter = z - 1; zIter <= z + 1; zIter++) {
                int bID = world.getBlockId(xIter, y, zIter);
                if (bID != NaturaIds.FLORA_LEAVES && !opaqueCube(bID)) {
                    this.setBlockAndMetadata(world, xIter, y, zIter, NaturaIds.FLORA_LEAVES, this.mdLeaves);
                }
            }
        }
        for (int xIter = x - 1; xIter <= x + 1; xIter++) {
            for (int zIter = z - 2; zIter <= z + 2; zIter++) {
                int bID = world.getBlockId(xIter, y, zIter);
                if (bID != NaturaIds.FLORA_LEAVES && !opaqueCube(bID)) {
                    this.setBlockAndMetadata(world, xIter, y, zIter, NaturaIds.FLORA_LEAVES, this.mdLeaves);
                }
            }
        }
        for (int xIter = x - 1; xIter <= x + 1; xIter++) {
            for (int zIter = z - 1; zIter <= z + 1; zIter++) {
                int bID = world.getBlockId(xIter, y + 1, zIter);
                if (bID != NaturaIds.FLORA_LEAVES && !opaqueCube(bID)) {
                    this.setBlockAndMetadata(world, xIter, y + 1, zIter, NaturaIds.FLORA_LEAVES, this.mdLeaves);
                }
            }
        }
        return true;
    }
}
