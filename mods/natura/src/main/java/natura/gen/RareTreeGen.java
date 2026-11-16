package natura.gen;

import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/** RareTreeGen: maple, silverbell, amaranth (purpleheart) and tigerwood, shaped like the vanilla oak. */
public class RareTreeGen extends NaturaGen {
    public final int minTreeHeight;
    public final int treeHeightRange;
    public final int metaWood;
    public final int metaLeaves;
    public final boolean seekHeight;

    public RareTreeGen(boolean notify, int treeHeight, int treeRange, int woodMeta, int leavesMeta) {
        super(notify);
        this.minTreeHeight = treeHeight;
        this.treeHeightRange = treeRange;
        this.metaWood = woodMeta;
        this.metaLeaves = leavesMeta;
        this.seekHeight = !notify;
    }

    @Override
    public boolean generate(OldWorld world, Random random, int xPos, int yPos, int zPos) {
        int height = random.nextInt(this.treeHeightRange) + this.minTreeHeight;
        if (this.seekHeight) {
            yPos = this.findGround(world, xPos, yPos, zPos);
            if (yPos == -1) {
                return false;
            }
        }

        if (yPos >= 1 && yPos + height + 1 <= 256) {
            int soil = world.getBlockId(xPos, yPos - 1, zPos);
            boolean isSoil = soil != 0 && sustainsSapling(soil);
            if (isSoil) {
                if (!this.checkClear(world, xPos, yPos, zPos, height)) {
                    return false;
                }
                // soil.onPlantGrow
                if (soil == GRASS) {
                    world.setBlock(xPos, yPos - 1, zPos, DIRT, 0);
                }
                this.placeCanopy(world, random, xPos, yPos, zPos, height);
                this.placeTrunk(world, xPos, yPos, zPos, height);
                return true;
            }
        }
        return false;
    }

    /** Looks at one block per layer only (both loops run from {@code range} to {@code range}), as the original did. */
    boolean checkClear(OldWorld world, int x, int y, int z, int treeHeight) {
        for (int yPos = 0; yPos < treeHeight + 1; yPos++) {
            int range = 1;
            if (yPos == 0) {
                range = 0;
            } else if (yPos >= treeHeight - 1) {
                range = 2;
            }
            for (int xPos = range; xPos <= range; xPos++) {
                for (int zPos = range; zPos <= range; zPos++) {
                    int blockID = world.getBlockId(x + xPos, y + yPos, z + zPos);
                    if (blockID != 0 && blockID != NaturaIds.RARE_SAPLING && !isLeaves(blockID)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    int findGround(OldWorld world, int x, int y, int z) {
        int ret = -1;
        int height = y;
        do {
            int heightID = world.getBlockId(x, height, z);
            if ((heightID == DIRT || heightID == GRASS) && !opaqueCube(world.getBlockId(x, height + 1, z))) {
                ret = height + 1;
                break;
            }
        } while (--height > NaturaIds.SEA_LEVEL);
        return ret;
    }

    void placeCanopy(OldWorld world, Random random, int xPos, int yPos, int zPos, int height) {
        for (int y = yPos - 3 + height; y <= yPos + height; y++) {
            int k1 = y - (yPos + height);
            int i2 = 1 - k1 / 2;
            for (int x = xPos - i2; x <= xPos + i2; x++) {
                int k2 = x - xPos;
                for (int z = zPos - i2; z <= zPos + i2; z++) {
                    int i3 = z - zPos;
                    if (Math.abs(k2) != i2 || Math.abs(i3) != i2 || random.nextInt(2) != 0 && k1 != 0) {
                        if (replaceable(world.getBlockId(x, y, z))) {
                            this.setBlockAndMetadata(world, x, y, z, NaturaIds.RARE_LEAVES, this.metaLeaves);
                        }
                    }
                }
            }
        }
    }

    void placeTrunk(OldWorld world, int xPos, int yPos, int zPos, int height) {
        for (int localHeight = 0; localHeight < height; localHeight++) {
            int blockID = world.getBlockId(xPos, yPos + localHeight, zPos);
            if (blockID == 0 || isLeaves(blockID)) {
                this.setBlockAndMetadata(world, xPos, yPos + localHeight, zPos, NaturaIds.RARE_TREE, this.metaWood);
            }
        }
    }
}
