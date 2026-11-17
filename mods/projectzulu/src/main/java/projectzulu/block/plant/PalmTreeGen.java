package projectzulu.block.plant;

import hexxit.worldgen152.core.populate.OldGen;
import hexxit.worldgen152.core.populate.OldWorld;
import projectzulu.gen.ZuluIds;

import java.util.Random;

/**
 * BlockPalmTreeSapling.growTree / spawnLeaves, 1:1: a trunk of 4-6 (+2) logs that leans towards the water within 4
 * blocks (the axis with more water columns wins), a cross of fronds on top, and the sapling itself turns into a log.
 * Nothing is checked before placing, as in the original. Only 1.5.2 ids, no game classes.
 */
public class PalmTreeGen extends OldGen {
    /** How far the tree reaches from the sapling: the trunk leans at most 7 blocks, the fronds reach 4 further. */
    public static final int RADIUS = 12;
    public static final int DEPTH = 2;
    public static final int HEIGHT = 12;

    private static boolean water(int id) {
        return id == 8 || id == 9;
    }

    @Override
    public boolean generate(OldWorld world, Random random, int x, int y, int z) {
        int log = ZuluIds.PALM_TREE_LOG;
        int height = random.nextInt(3) + 4;
        int favoredDirectionX = 0;
        int favoredDirectionZ = 0;
        for (int i = -4; i <= 4; i++) {
            for (int k = -4; k <= 4; k++) {
                for (int j = -4; j <= 4; j++) {
                    if (water(world.getBlockId(x + i, y + j, z + k))) {
                        if (i != 0) {
                            favoredDirectionX += i / Math.abs(i);
                        }
                        if (k != 0) {
                            favoredDirectionZ += k / Math.abs(k);
                        }
                    }
                }
            }
        }
        if (Math.abs(favoredDirectionX) - Math.abs(favoredDirectionZ) >= 0) {
            favoredDirectionZ = 0;
        } else {
            favoredDirectionX = 0;
        }
        int localX = 0;
        int localY = 0;
        int localZ = 0;
        int horizontalFactor = random.nextInt(10) + 20;
        while (localY <= height) {
            world.setBlock(x + localX, y + ++localY, z + localZ, log);
            if (favoredDirectionX > 0) {
                localX++;
                favoredDirectionX = Math.max(favoredDirectionX - horizontalFactor, 0);
                world.setBlock(x + localX, y + localY, z + localZ, log);
            } else if (favoredDirectionX < 0) {
                localX--;
                favoredDirectionX = Math.min(favoredDirectionX + horizontalFactor, 0);
                world.setBlock(x + localX, y + localY, z + localZ, log);
            }
            if (favoredDirectionZ > 0) {
                localZ++;
                favoredDirectionZ = Math.max(favoredDirectionZ - horizontalFactor, 0);
                world.setBlock(x + localX, y + localY, z + localZ, log);
            } else if (favoredDirectionZ < 0) {
                localZ--;
                favoredDirectionZ = Math.min(favoredDirectionZ + horizontalFactor, 0);
                world.setBlock(x + localX, y + localY, z + localZ, log);
            }
            if (localY + 1 == height) {
                world.setBlock(x + localX, y + ++localY, z + localZ, log);
                spawnLeaves(world, x + localX, y + ++localY, z + localZ, height);
                world.setBlock(x, y, z, log);
            }
        }
        return true;
    }

    private static void spawnLeaves(OldWorld world, int x, int y, int z, int height) {
        int l = ZuluIds.PALM_TREE_LEAVES;
        if (height + 1 >= 7) {
            world.setBlock(x, y, z, l);
            world.setBlock(x - 1, y, z, l);
            world.setBlock(x - 2, y, z, l);
            world.setBlock(x - 3, y, z, l);
            world.setBlock(x - 4, y - 1, z, l);
            world.setBlock(x + 1, y, z, l);
            world.setBlock(x + 2, y, z, l);
            world.setBlock(x + 3, y, z, l);
            world.setBlock(x + 3, y - 1, z, l);
            world.setBlock(x, y, z - 1, l);
            world.setBlock(x, y, z - 2, l);
            world.setBlock(x, y, z - 3, l);
            world.setBlock(x, y - 1, z - 3, l);
            world.setBlock(x, y, z + 1, l);
            world.setBlock(x, y, z + 2, l);
            world.setBlock(x, y, z + 3, l);
            world.setBlock(x, y - 1, z + 3, l);
        } else {
            world.setBlock(x, y, z, l);
            world.setBlock(x - 1, y, z, l);
            world.setBlock(x - 2, y, z, l);
            world.setBlock(x - 2, y - 1, z, l);
            world.setBlock(x + 1, y, z, l);
            world.setBlock(x + 2, y, z, l);
            world.setBlock(x + 2, y - 1, z, l);
            world.setBlock(x, y, z - 1, l);
            world.setBlock(x, y, z - 2, l);
            world.setBlock(x, y - 1, z - 2, l);
            world.setBlock(x, y, z + 1, l);
            world.setBlock(x, y, z + 2, l);
            world.setBlock(x, y - 1, z + 2, l);
        }
    }
}
