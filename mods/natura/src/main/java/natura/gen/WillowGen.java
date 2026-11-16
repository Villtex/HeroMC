package natura.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo.Material;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/** WillowGen: the swamp tree shape with willow wood and hanging willow leaves in place of vines. */
public class WillowGen extends NaturaGen {
    public final boolean seekHeight;

    public WillowGen(boolean notify) {
        super(notify);
        this.seekHeight = !notify;
    }

    int findGround(OldWorld world, int x, int y, int z) {
        int ret = -1;
        int height = y;
        do {
            int heightID = world.getBlockId(x, height, z);
            if ((heightID == DIRT || heightID == GRASS || heightID == SAND) && !opaqueCube(world.getBlockId(x, height + 1, z))) {
                ret = height + 1;
                break;
            }
        } while (--height > NaturaIds.SEA_LEVEL);
        return ret;
    }

    @Override
    public boolean generate(OldWorld world, Random random, int x, int y, int z) {
        if (this.seekHeight) {
            y = this.findGround(world, x, y, z);
            if (y == -1) {
                return false;
            }
        }

        int l = random.nextInt(4) + 5;
        while (world.material(x, y - 1, z) == Material.WATER) {
            y--;
        }

        boolean flag = true;
        if (y >= 1 && y + l + 1 <= 128) {
            for (int i1 = y; i1 <= y + 1 + l; i1++) {
                byte b0 = 1;
                if (i1 == y) {
                    b0 = 0;
                }
                if (i1 >= y + 1 + l - 2) {
                    b0 = 3;
                }
                for (int j1 = x - b0; j1 <= x + b0 && flag; j1++) {
                    for (int k1 = z - b0; k1 <= z + b0 && flag; k1++) {
                        if (i1 >= 0 && i1 < 128) {
                            int l1 = world.getBlockId(j1, i1, k1);
                            if (l1 != 0 && !isLeaves(l1)) {
                                if (l1 != WATER_STILL && l1 != WATER_MOVING) {
                                    flag = false;
                                } else if (i1 > y) {
                                    flag = false;
                                }
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

            int var17 = world.getBlockId(x, y - 1, z);
            if ((var17 == GRASS || var17 == DIRT) && y < 128 - l - 1) {
                this.setBlock(world, x, y - 1, z, DIRT);

                for (int j2 = y - 3 + l; j2 <= y + l; j2++) {
                    int j1 = j2 - (y + l);
                    int k1 = 2 - j1 / 2;
                    for (int l1 = x - k1; l1 <= x + k1; l1++) {
                        int i2 = l1 - x;
                        for (int k2 = z - k1; k2 <= z + k1; k2++) {
                            int l2 = k2 - z;
                            int block = world.getBlockId(l1, j2, k2);
                            if ((Math.abs(i2) != k1 || Math.abs(l2) != k1 || random.nextInt(2) != 0 && j1 != 0) && replaceable(block)) {
                                this.setBlockAndMetadata(world, l1, j2, k2, NaturaIds.FLORA_LEAVES_NO_COLOR, NaturaIds.LEAVES_WILLOW);
                            }
                        }
                    }
                }

                for (int var27 = 0; var27 < l; var27++) {
                    int j1 = world.getBlockId(x, y + var27, z);
                    if (j1 == 0 || isLeaves(j1) || j1 == WATER_MOVING || j1 == WATER_STILL) {
                        this.setBlock(world, x, y + var27, z, NaturaIds.WILLOW);
                    }
                }

                for (int var28 = y - 3 + l; var28 <= y + l; var28++) {
                    int j1 = var28 - (y + l);
                    int k1 = 2 - j1 / 2;
                    for (int l1 = x - k1; l1 <= x + k1; l1++) {
                        for (int i2 = z - k1; i2 <= z + k1; i2++) {
                            if (isLeaves(world.getBlockId(l1, var28, i2))) {
                                if (random.nextInt(4) == 0 && world.getBlockId(l1 - 1, var28, i2) == 0) {
                                    this.generateVines(world, l1 - 1, var28, i2, 3);
                                }
                                if (random.nextInt(4) == 0 && world.getBlockId(l1 + 1, var28, i2) == 0) {
                                    this.generateVines(world, l1 + 1, var28, i2, 3);
                                }
                                if (random.nextInt(4) == 0 && world.getBlockId(l1, var28, i2 - 1) == 0) {
                                    this.generateVines(world, l1, var28, i2 - 1, 3);
                                }
                                if (random.nextInt(4) == 0 && world.getBlockId(l1, var28, i2 + 1) == 0) {
                                    this.generateVines(world, l1, var28, i2 + 1, 3);
                                }
                            }
                        }
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

    private void generateVines(OldWorld world, int par2, int par3, int par4, int par5) {
        this.setBlockAndMetadata(world, par2, par3, par4, NaturaIds.FLORA_LEAVES_NO_COLOR, par5);
        for (int i1 = 4; world.getBlockId(par2, --par3, par4) == 0 && i1 > 0; i1--) {
            this.setBlockAndMetadata(world, par2, par3, par4, NaturaIds.FLORA_LEAVES_NO_COLOR, par5);
        }
    }
}
