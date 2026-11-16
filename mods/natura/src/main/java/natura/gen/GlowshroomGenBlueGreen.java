package natura.gen;

import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/** GlowshroomGenBlueGreen: the huge green (flat cap) and blue (round cap) glowshrooms. */
public class GlowshroomGenBlueGreen extends NaturaGen {
    int mushroomType = -1;

    public GlowshroomGenBlueGreen(boolean flag) {
        super(flag);
    }

    public GlowshroomGenBlueGreen(boolean flag, int type) {
        super(flag);
        this.mushroomType = type;
    }

    @Override
    public boolean generate(OldWorld world, Random random, int x, int y, int z) {
        int type;
        if (this.mushroomType >= 0) {
            type = this.mushroomType;
        } else {
            type = random.nextInt(2);
        }

        int height = random.nextInt(3) + 4;
        boolean flag = true;
        if (y >= 1 && y + height + 1 < 256) {
            for (int blockID = y; blockID <= y + 1 + height; blockID++) {
                byte b0 = 3;
                if (blockID <= y + 3) {
                    b0 = 0;
                }
                for (int posY = x - b0; posY <= x + b0 && flag; posY++) {
                    for (int l1 = z - b0; l1 <= z + b0 && flag; l1++) {
                        if (blockID >= 0 && blockID < 256) {
                            int posX = world.getBlockId(posY, blockID, l1);
                            if (posX != 0 && !isLeaves(posX) && posX != NaturaIds.GLOWSHROOM) {
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

            int var18 = world.getBlockId(x, y - 1, z);
            if (var18 != MYCELIUM && var18 != NETHERRACK && var18 != NaturaIds.TAINTED_SOIL && var18 != SLOW_SAND) {
                return false;
            }

            int j2 = y + height;
            if (type == 1) {
                j2 = y + height - 3;
            }

            for (int posY = j2; posY <= y + height; posY++) {
                int l1 = 1;
                if (posY < y + height) {
                    l1++;
                }
                if (type == 0) {
                    l1 = 3;
                }
                for (int posX = x - l1; posX <= x + l1; posX++) {
                    for (int posZ = z - l1; posZ <= z + l1; posZ++) {
                        int meta = 5;
                        if (posX == x - l1) {
                            meta--;
                        }
                        if (posX == x + l1) {
                            meta++;
                        }
                        if (posZ == z - l1) {
                            meta -= 3;
                        }
                        if (posZ == z + l1) {
                            meta += 3;
                        }

                        if (type == 0 || posY < y + height) {
                            if ((posX == x - l1 || posX == x + l1) && (posZ == z - l1 || posZ == z + l1)) {
                                continue;
                            }
                            if (posX == x - (l1 - 1) && posZ == z - l1) {
                                meta = 1;
                            }
                            if (posX == x - l1 && posZ == z - (l1 - 1)) {
                                meta = 1;
                            }
                            if (posX == x + (l1 - 1) && posZ == z - l1) {
                                meta = 3;
                            }
                            if (posX == x + l1 && posZ == z - (l1 - 1)) {
                                meta = 3;
                            }
                            if (posX == x - (l1 - 1) && posZ == z + l1) {
                                meta = 7;
                            }
                            if (posX == x - l1 && posZ == z + (l1 - 1)) {
                                meta = 7;
                            }
                            if (posX == x + (l1 - 1) && posZ == z + l1) {
                                meta = 9;
                            }
                            if (posX == x + l1 && posZ == z + (l1 - 1)) {
                                meta = 9;
                            }
                        }

                        if (meta == 5 && posY < y + height) {
                            meta = 0;
                        }

                        if ((meta != 0 || y >= y + height - 1) && replaceable(world.getBlockId(posX, posY, posZ))) {
                            int localID = type == 1 ? NaturaIds.GLOWSHROOM_BLUE : NaturaIds.GLOWSHROOM_GREEN;
                            this.setBlockAndMetadata(world, posX, posY, posZ, localID, meta);
                        }
                    }
                }
            }

            for (int var20 = 0; var20 < height; var20++) {
                if (replaceable(world.getBlockId(x, y + var20, z))) {
                    int localID = type == 1 ? NaturaIds.GLOWSHROOM_BLUE : NaturaIds.GLOWSHROOM_GREEN;
                    this.setBlockAndMetadata(world, x, y + var20, z, localID, 10);
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
