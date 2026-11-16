package natura.gen;

import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/** BloodTreeLargeGen: the bloodwood, a 2x2 trunk hanging down from the Nether ceiling. */
public class BloodTreeLargeGen extends NaturaGen {
    private final int mdWood;
    private final int mdLeaves;

    public BloodTreeLargeGen(int wood, int leaves) {
        this.mdWood = wood;
        this.mdLeaves = leaves;
    }

    @Override
    public boolean generate(OldWorld world, Random random, int x, int y, int z) {
        int height = this.findCeiling(world, x, y, z);
        return height == -1 ? false : this.generateRandomTree(world, random, x, height, z);
    }

    int findCeiling(OldWorld world, int x, int y, int z) {
        int ret = -1;
        int height = y;
        do {
            int heightID = world.getBlockId(x, height, z);
            if (netherSoil(heightID) && !opaqueCube(world.getBlockId(x, height - 1, z))) {
                ret = height - 1;
                break;
            }
        } while (++height <= 120);
        return ret;
    }

    public boolean generateRandomTree(OldWorld world, Random random, int x, int y, int z) {
        int treeHeight = random.nextInt(8) + 8;
        for (int heightIter = 0; heightIter < treeHeight; heightIter++) {
            int localID = world.getBlockId(x, y - heightIter, z);
            if (localID == 0 || localID == NaturaIds.FLORA_LEAVES) {
                this.setBlockAndMetadata(world, x, y - heightIter, z, NaturaIds.BLOODWOOD, 0);
                this.setBlockAndMetadata(world, x + 1, y - heightIter, z, NaturaIds.BLOODWOOD, 1);
                this.setBlockAndMetadata(world, x, y - heightIter, z + 1, NaturaIds.BLOODWOOD, 2);
                this.setBlockAndMetadata(world, x + 1, y - heightIter, z + 1, NaturaIds.BLOODWOOD, 3);
            }
        }

        this.genBranch(world, random, x, y, z, -treeHeight, 1);
        this.genBranch(world, random, x + 1, y, z, -treeHeight, 2);
        this.genBranch(world, random, x, y, z + 1, -treeHeight, 3);
        this.genBranch(world, random, x + 1, y, z + 1, -treeHeight, 4);
        this.genStraightBranch(world, random, x, y, z, -treeHeight, 1);
        this.genStraightBranch(world, random, x + 1, y, z, -treeHeight, 2);
        this.genStraightBranch(world, random, x, y, z + 1, -treeHeight, 3);
        this.genStraightBranch(world, random, x + 1, y, z + 1, -treeHeight, 4);
        return true;
    }

    private void genBranch(OldWorld world, Random random, int x, int y, int z, int height, int direction) {
        int xPos = x;
        int yPos = y + height;
        int zPos = z;
        byte offsetX = 0;
        byte offsetZ = 0;
        switch (direction) {
            case 1:
                offsetX = 1;
                offsetZ = 1;
                break;
            case 2:
                offsetX = -1;
                offsetZ = 1;
                break;
            case 3:
                offsetX = 1;
                offsetZ = -1;
                break;
            case 4:
                offsetX = -1;
                offsetZ = -1;
        }

        int i2 = random.nextInt(15);
        for (int j2 = 4; j2 > 0; j2--) {
            if (i2 % 3 != 0) {
                xPos += offsetX;
            }
            if (i2 % 3 != 1) {
                zPos += offsetZ;
            }
            yPos += i2 % 3 - 1;
            this.generateNode(world, random, xPos, yPos, zPos);
            i2 = random.nextInt(15);
        }
    }

    private void genStraightBranch(OldWorld world, Random random, int x, int y, int z, int height, int direction) {
        int xPos = x;
        int yPos = y + height;
        int zPos = z;
        byte offsetX = 0;
        byte offsetZ = 0;
        switch (direction) {
            case 1:
                offsetX = 1;
                offsetZ = 0;
                break;
            case 2:
                offsetX = 0;
                offsetZ = 1;
                break;
            case 3:
                offsetX = -1;
                offsetZ = 0;
                break;
            case 4:
                offsetX = 0;
                offsetZ = -1;
        }

        int i2 = random.nextInt(5);
        for (int j2 = 4; j2 > 0; j2--) {
            if (offsetX == 0) {
                xPos = xPos + random.nextInt(3) - 1;
                zPos += offsetZ;
            }
            if (offsetZ == 0) {
                xPos += offsetX;
                zPos = zPos + random.nextInt(3) - 1;
            }
            yPos -= i2 % 3 - 1;
            this.generateNode(world, random, xPos, yPos, zPos);
            i2 = random.nextInt(5);
        }
    }

    public boolean generateNode(OldWorld world, Random random, int x, int y, int z) {
        this.setBlockAndMetadata(world, x, y, z, NaturaIds.BLOODWOOD, 15);

        for (int l = x - 1; l <= x + 1; l++) {
            for (int k1 = z - 1; k1 <= z + 1; k1++) {
                for (int j2 = y - 1; j2 <= y + 1; j2++) {
                    int i3 = world.getBlockId(l, j2, k1);
                    if (i3 != NaturaIds.FLORA_LEAVES && !opaqueCube(i3)) {
                        this.setBlockAndMetadata(world, l, j2, k1, NaturaIds.FLORA_LEAVES_NO_COLOR, this.mdLeaves);
                    }
                }
            }
        }

        for (int i1 = x - 1; i1 <= x + 1; i1++) {
            for (int l1 = z - 2; l1 <= z + 2; l1++) {
                int k2 = world.getBlockId(i1, y, l1);
                if (k2 != NaturaIds.FLORA_LEAVES && !opaqueCube(k2)) {
                    this.setBlockAndMetadata(world, i1, y, l1, NaturaIds.FLORA_LEAVES_NO_COLOR, this.mdLeaves);
                }
            }
        }

        // the original looked one block higher than it placed
        for (int j1 = x - 2; j1 <= x + 2; j1++) {
            for (int i2 = z - 1; i2 <= z + 1; i2++) {
                int l2 = world.getBlockId(j1, y + 1, i2);
                if (l2 != NaturaIds.FLORA_LEAVES && !opaqueCube(l2)) {
                    this.setBlockAndMetadata(world, j1, y, i2, NaturaIds.FLORA_LEAVES_NO_COLOR, this.mdLeaves);
                }
            }
        }
        return true;
    }
}
