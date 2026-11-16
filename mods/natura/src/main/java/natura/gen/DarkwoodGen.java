package natura.gen;

import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/** DarkwoodGen: the darkwood of the Nether, an oak shape whose leaves sometimes flower or bear fruit. */
public class DarkwoodGen extends NaturaGen {
    private final int minTreeHeight;
    private final int metaWood;
    private final boolean seekGround;

    public DarkwoodGen(boolean notify) {
        this(notify, 4, 0);
    }

    public DarkwoodGen(boolean notify, int treeHeight, int woodMeta) {
        super(notify);
        this.minTreeHeight = treeHeight;
        this.metaWood = woodMeta;
        this.seekGround = !notify;
    }

    int findGround(OldWorld world, int x, int y, int z) {
        boolean foundGround = false;
        int height = y;
        do {
            int underID = world.getBlockId(x, --height, z);
            if (netherSoil(underID) || height < 0) {
                foundGround = true;
            }
        } while (!foundGround);
        return height + 1;
    }

    /** Metadata of one leaf block; the darkwood draws for flowers and fruit. */
    int leafMeta(Random random) {
        return random.nextInt(25) == 0 ? 2 : (random.nextInt(15) == 0 ? 1 : 0);
    }

    @Override
    public boolean generate(OldWorld world, Random random, int xPos, int yPos, int zPos) {
        int treeHeight = random.nextInt(3) + this.minTreeHeight;
        if (treeHeight < 4) {
            treeHeight = 4;
        }

        boolean flag = true;
        if (this.seekGround) {
            yPos = this.findGround(world, xPos, yPos, zPos);
        }

        if (yPos >= 1 && yPos + treeHeight + 1 <= 256) {
            for (int i1 = yPos; i1 <= yPos + 1 + treeHeight; i1++) {
                byte b0 = 1;
                if (i1 == yPos) {
                    b0 = 0;
                }
                if (i1 >= yPos + 1 + treeHeight - 2) {
                    b0 = 2;
                }
                for (int l1 = xPos - b0; l1 <= xPos + b0 && flag; l1++) {
                    for (int j1 = zPos - b0; j1 <= zPos + b0 && flag; j1++) {
                        if (i1 >= 0 && i1 < 256) {
                            int k1 = world.getBlockId(l1, i1, j1);
                            if (k1 != 0 && !isLeaves(k1) && !netherSoil(k1) && !isWood(k1)) {
                                flag = false;
                            }
                        } else {
                            flag = false;
                        }
                    }
                }
            }
            if (!flag) {
                return false;
            }

            int var22 = world.getBlockId(xPos, yPos - 1, zPos);
            // soil.canSustainPlant(..., floraSapling) || soil == netherrack
            boolean isSoil = var22 != 0 && sustainsSapling(var22);
            if (isSoil && yPos < 256 - treeHeight - 1) {
                // soil.onPlantGrow
                if (var22 == GRASS) {
                    world.setBlock(xPos, yPos - 1, zPos, DIRT, 0);
                }
                byte b0 = 3;
                byte b1 = 0;
                for (int j1 = yPos - b0 + treeHeight; j1 <= yPos + treeHeight; j1++) {
                    int k1 = j1 - (yPos + treeHeight);
                    int i2 = b1 + 1 - k1 / 2;
                    for (int j2 = xPos - i2; j2 <= xPos + i2; j2++) {
                        int k2 = j2 - xPos;
                        for (int l2 = zPos - i2; l2 <= zPos + i2; l2++) {
                            int i3 = l2 - zPos;
                            if (Math.abs(k2) != i2 || Math.abs(i3) != i2 || random.nextInt(2) != 0 && k1 != 0) {
                                if (replaceable(world.getBlockId(j2, j1, l2))) {
                                    this.setBlockAndMetadata(world, j2, j1, l2, NaturaIds.DARK_LEAVES, this.leafMeta(random));
                                }
                            }
                        }
                    }
                }

                for (int var25 = 0; var25 < treeHeight; var25++) {
                    int k1 = world.getBlockId(xPos, yPos + var25, zPos);
                    if (k1 == 0 || isLeaves(k1)) {
                        this.setBlockAndMetadata(world, xPos, yPos + var25, zPos, NaturaIds.DARK_TREE, this.metaWood);
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
