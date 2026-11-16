package natura.gen;

import hexxit.worldgen152.core.OldMath;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/** BushTreeGen: the hopseed tree, a low 2x2 trunk with side branches. */
public class BushTreeGen extends NaturaGen {
    private final int baseHeight;
    private final int woodMetadata;
    private final int leavesMetadata;

    public BushTreeGen(boolean notify, int height, int mdwood, int mdleaves) {
        super(notify);
        this.baseHeight = height;
        this.woodMetadata = mdwood;
        this.leavesMetadata = mdleaves;
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
        int var6 = random.nextInt(3) + this.baseHeight;
        boolean var7 = true;
        y = this.findGround(world, x, y, z);
        if (y >= 1 && y + var6 + 1 <= 256) {
            for (int var8 = y; var8 <= y + 1 + var6; var8++) {
                byte var9 = 2;
                if (var8 == y) {
                    var9 = 1;
                }
                if (var8 >= y + 1 + var6 - 2) {
                    var9 = 2;
                }
                for (int var10 = x - var9; var10 <= x + var9 && var7; var10++) {
                    for (int var11 = z - var9; var11 <= z + var9 && var7; var11++) {
                        if (var8 >= 0 && var8 < 256) {
                            int var12 = world.getBlockId(var10, var8, var11);
                            if (var12 != 0 && !isLeaves(var12) && var12 != GRASS && var12 != DIRT && !isWood(var12) && var12 != 6) {
                                var7 = false;
                            }
                        } else {
                            var7 = false;
                        }
                    }
                }
            }
            if (!var7) {
                return false;
            }

            int var16 = world.getBlockId(x, y - 1, z);
            if ((var16 == GRASS || var16 == DIRT) && y < 256 - var6 - 1) {
                world.setBlock(x, y - 1, z, DIRT);
                world.setBlock(x + 1, y - 1, z, DIRT);
                world.setBlock(x, y - 1, z + 1, DIRT);
                world.setBlock(x + 1, y - 1, z + 1, DIRT);
                this.growLeaves(world, x, z, y + var6, 2, random);

                for (int var14 = y + var6 - 2 - random.nextInt(4); var14 > y + var6 / 2; var14 -= 2 + random.nextInt(4)) {
                    float var15 = random.nextFloat() * (float) Math.PI * 2.0F;
                    int var11 = x + (int) (0.5F + OldMath.cos(var15) * 4.0F);
                    int var12 = z + (int) (0.5F + OldMath.sin(var15) * 4.0F);
                    this.growLeaves(world, var11, var12, var14, 0, random);
                    for (int var13 = 0; var13 < 5; var13++) {
                        var11 = x + (int) (1.5F + OldMath.cos(var15) * var13);
                        var12 = z + (int) (1.5F + OldMath.sin(var15) * var13);
                        this.setBlockAndMetadata(world, var11, var14 - 3 + var13 / 2, var12, NaturaIds.TREE, this.woodMetadata);
                    }
                }

                for (int var10 = 0; var10 < var6; var10++) {
                    int var11 = world.getBlockId(x, y + var10, z);
                    if (var11 == 0 || isLeaves(var11)) {
                        this.setBlockAndMetadata(world, x, y + var10, z, NaturaIds.TREE, this.woodMetadata);
                    }
                    if (var10 < var6 - 1) {
                        var11 = world.getBlockId(x + 1, y + var10, z);
                        if (var11 == 0 || isLeaves(var11)) {
                            this.setBlockAndMetadata(world, x + 1, y + var10, z, NaturaIds.TREE, this.woodMetadata);
                        }
                        var11 = world.getBlockId(x + 1, y + var10, z + 1);
                        if (var11 == 0 || isLeaves(var11)) {
                            this.setBlockAndMetadata(world, x + 1, y + var10, z + 1, NaturaIds.TREE, this.woodMetadata);
                        }
                        var11 = world.getBlockId(x, y + var10, z + 1);
                        if (var11 == 0 || isLeaves(var11)) {
                            this.setBlockAndMetadata(world, x, y + var10, z + 1, NaturaIds.TREE, this.woodMetadata);
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

    private void growLeaves(OldWorld world, int par2, int par3, int par4, int par5, Random random) {
        byte var7 = 2;
        for (int var8 = par4 - var7; var8 <= par4; var8++) {
            int var9 = var8 - par4;
            int var10 = par5 + 1 - var9;
            for (int var11 = par2 - var10; var11 <= par2 + var10 + 1; var11++) {
                int var12 = var11 - par2;
                for (int var13 = par3 - var10; var13 <= par3 + var10 + 1; var13++) {
                    int var14 = var13 - par3;
                    int block = world.getBlockId(var11, var8, var13);
                    if ((var12 >= 0 || var14 >= 0 || var12 * var12 + var14 * var14 <= var10 * var10)
                            && (var12 <= 0 && var14 <= 0 || var12 * var12 + var14 * var14 <= (var10 + 1) * (var10 + 1))
                            && (random.nextInt(4) != 0 || var12 * var12 + var14 * var14 <= (var10 - 1) * (var10 - 1))
                            && replaceable(block)) {
                        this.setBlockAndMetadata(world, var11, var8, var13, NaturaIds.FLORA_LEAVES, this.leavesMetadata);
                    }
                }
            }
        }
    }
}
