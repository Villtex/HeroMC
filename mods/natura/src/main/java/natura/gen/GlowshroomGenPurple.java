package natura.gen;

import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/** GlowshroomGenPurple: the huge purple glowshroom, low with a wide cap. */
public class GlowshroomGenPurple extends NaturaGen {
    public GlowshroomGenPurple(boolean flag) {
        super(flag);
    }

    @Override
    public boolean generate(OldWorld world, Random random, int x, int y, int z) {
        int type = 1;
        int height = random.nextInt(4) + 1;
        boolean flag = true;
        if (y >= 1 && y + height + 1 < 256) {
            for (int blockID = y; blockID <= y + 1 + height; blockID++) {
                byte b0 = 3;
                if (blockID <= y + 3) {
                    b0 = 0;
                }
                for (int posY = x - b0; posY <= x + b0 && flag; posY++) {
                    for (int range = z - b0; range <= z + b0 && flag; range++) {
                        if (blockID >= 0 && blockID < 256) {
                            int posX = world.getBlockId(posY, blockID, range);
                            if (posX != 0 && !isLeaves(posX)) {
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

            int var17 = world.getBlockId(x, y - 1, z);
            if (var17 != MYCELIUM && var17 != NETHERRACK && var17 != NaturaIds.TAINTED_SOIL && var17 != SLOW_SAND) {
                return false;
            }

            int heightPos = y + height - 1;
            for (int posY = heightPos; posY <= y + height; posY++) {
                int range = 2;
                if (posY < y + height) {
                    range++;
                }
                for (int posX = x - range; posX <= x + range; posX++) {
                    for (int posZ = z - range; posZ <= z + range; posZ++) {
                        int meta = 5;
                        if (posX == x - range) {
                            meta--;
                        }
                        if (posX == x + range) {
                            meta++;
                        }
                        if (posZ == z - range) {
                            meta -= 3;
                        }
                        if (posZ == z + range) {
                            meta += 3;
                        }

                        if (type == 1 || posY < y + height) {
                            int swap = posY < y + height ? 2 : 1;
                            if (type == 1 && (posX == x - range || posX == x + range) && (posZ == z - range || posZ == z + range)
                                    || posY < y + height && (posX <= x - range + 1 || posX >= x + range - 1) && (posZ <= z - range + 1 || posZ >= z + range - 1)) {
                                continue;
                            }
                            if (posX == x - (range - swap) && posZ == z - range) {
                                meta = 1;
                            }
                            if (posX == x - range && posZ == z - (range - swap)) {
                                meta = 1;
                            }
                            if (posX == x + (range - swap) && posZ == z - range) {
                                meta = 3;
                            }
                            if (posX == x + range && posZ == z - (range - swap)) {
                                meta = 3;
                            }
                            if (posX == x - (range - swap) && posZ == z + range) {
                                meta = 7;
                            }
                            if (posX == x - range && posZ == z + (range - swap)) {
                                meta = 7;
                            }
                            if (posX == x + (range - swap) && posZ == z + range) {
                                meta = 9;
                            }
                            if (posX == x + range && posZ == z + (range - swap)) {
                                meta = 9;
                            }
                        }

                        if (meta == 5 && posY < y + height) {
                            meta = 0;
                        }

                        if ((meta != 0 || y >= y + height - 1) && replaceable(world.getBlockId(posX, posY, posZ))) {
                            this.setBlockAndMetadata(world, posX, posY, posZ, NaturaIds.GLOWSHROOM_PURPLE, meta);
                        }
                    }
                }
            }

            this.setBlockAndMetadata(world, x - 2, y + height - 1, z - 2, NaturaIds.GLOWSHROOM_PURPLE, 1);
            this.setBlockAndMetadata(world, x + 2, y + height - 1, z - 2, NaturaIds.GLOWSHROOM_PURPLE, 3);
            this.setBlockAndMetadata(world, x - 2, y + height - 1, z + 2, NaturaIds.GLOWSHROOM_PURPLE, 7);
            this.setBlockAndMetadata(world, x + 2, y + height - 1, z + 2, NaturaIds.GLOWSHROOM_PURPLE, 9);

            for (int var19 = 0; var19 < height; var19++) {
                if (replaceable(world.getBlockId(x, y + var19, z))) {
                    this.setBlockAndMetadata(world, x, y + var19, z, NaturaIds.GLOWSHROOM_PURPLE, 10);
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
