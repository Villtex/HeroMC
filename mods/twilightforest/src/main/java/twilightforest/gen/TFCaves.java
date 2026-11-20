package twilightforest.gen;

import hexxit.worldgen152.core.OldMath;

import java.util.Random;

/**
 * TFGenCaves (a MapGenBase4096 with range 8): caves of the Twilight Forest. Unlike the overworld ones they stop at
 * y 120, fill with flowing water below y 10 instead of lava and leave a shell of dirt (grass under grass) around them.
 *
 * <p>The carving loops of the original start one index above the y they test; that offset is kept.
 */
final class TFCaves {
    private static final int RANGE = 8;

    private final long seed;
    private final TFLayout layout;

    TFCaves(long seed, TFLayout layout) {
        this.seed = seed;
        this.layout = layout;
    }

    /** MapGenBase4096.generate. */
    void generate(int cx, int cz, short[] storage) {
        Random rand = new Random(this.seed);
        long long1 = rand.nextLong();
        long long2 = rand.nextLong();
        for (int gx = cx - RANGE; gx <= cx + RANGE; gx++) {
            for (int gz = cz - RANGE; gz <= cz + RANGE; gz++) {
                long var13 = gx * long1;
                long var15 = gz * long2;
                rand.setSeed(var13 ^ var15 ^ this.seed);
                this.recursiveGenerate(rand, gx, gz, cx, cz, storage);
            }
        }
    }

    private void generateLargeCaveNode(Random rand, long par1, int par3, int par4, short[] shortStorage, double par6, double par8, double par10) {
        this.generateCaveNode(par1, par3, par4, shortStorage, par6, par8, par10, 1.0F + rand.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5);
    }

    private void generateCaveNode(long caveSeed, int par3, int par4, short[] shortStorage, double par6, double par8, double par10,
                                  float par12, float par13, float par14, int par15, int par16, double par17) {
        double var19 = par3 * 16 + 8;
        double var21 = par4 * 16 + 8;
        float var23 = 0.0F;
        float var24 = 0.0F;
        Random caveRNG = new Random(caveSeed);
        if (par16 <= 0) {
            int var26 = RANGE * 16 - 16;
            par16 = var26 - caveRNG.nextInt(var26 / 4);
        }

        boolean var54 = false;
        if (par15 == -1) {
            par15 = par16 / 2;
            var54 = true;
        }

        int var27 = caveRNG.nextInt(par16 / 2) + par16 / 4;
        boolean var28 = caveRNG.nextInt(6) == 0;

        while (par15 < par16) {
            double var29 = 1.5 + OldMath.sin(par15 * (float) Math.PI / par16) * par12 * 1.0F;
            double var31 = var29 * par17;
            float var33 = OldMath.cos(par14);
            float var34 = OldMath.sin(par14);
            par6 += OldMath.cos(par13) * var33;
            par8 += var34;
            par10 += OldMath.sin(par13) * var33;
            if (var28) {
                par14 *= 0.92F;
            } else {
                par14 *= 0.7F;
            }

            par14 += var24 * 0.1F;
            par13 += var23 * 0.1F;
            var24 *= 0.9F;
            var23 *= 0.75F;
            var24 += (caveRNG.nextFloat() - caveRNG.nextFloat()) * caveRNG.nextFloat() * 2.0F;
            var23 += (caveRNG.nextFloat() - caveRNG.nextFloat()) * caveRNG.nextFloat() * 4.0F;
            if (!var54 && par15 == var27 && par12 > 1.0F && par16 > 0) {
                this.generateCaveNode(caveRNG.nextLong(), par3, par4, shortStorage, par6, par8, par10, caveRNG.nextFloat() * 0.5F + 0.5F,
                        par13 - (float) (Math.PI / 2), par14 / 3.0F, par15, par16, 1.0);
                this.generateCaveNode(caveRNG.nextLong(), par3, par4, shortStorage, par6, par8, par10, caveRNG.nextFloat() * 0.5F + 0.5F,
                        par13 + (float) (Math.PI / 2), par14 / 3.0F, par15, par16, 1.0);
                return;
            }

            if (var54 || caveRNG.nextInt(4) != 0) {
                double var35 = par6 - var19;
                double var37 = par10 - var21;
                double var39 = par16 - par15;
                double var41 = par12 + 2.0F + 16.0F;
                if (var35 * var35 + var37 * var37 - var39 * var39 > var41 * var41) {
                    return;
                }

                if (par6 >= var19 - 16.0 - var29 * 2.0 && par10 >= var21 - 16.0 - var29 * 2.0
                        && par6 <= var19 + 16.0 + var29 * 2.0 && par10 <= var21 + 16.0 + var29 * 2.0) {
                    int var55 = OldMath.floor(par6 - var29) - par3 * 16 - 1;
                    int var36 = OldMath.floor(par6 + var29) - par3 * 16 + 1;
                    int var57 = OldMath.floor(par8 - var31) - 1;
                    int var38 = OldMath.floor(par8 + var31) + 1;
                    int var56 = OldMath.floor(par10 - var29) - par4 * 16 - 1;
                    int var40 = OldMath.floor(par10 + var29) - par4 * 16 + 1;
                    if (var55 < 0) {
                        var55 = 0;
                    }

                    if (var36 > 16) {
                        var36 = 16;
                    }

                    if (var57 < 1) {
                        var57 = 1;
                    }

                    if (var38 > 120) {
                        var38 = 120;
                    }

                    if (var56 < 0) {
                        var56 = 0;
                    }

                    if (var40 > 16) {
                        var40 = 16;
                    }

                    boolean var58 = false;

                    for (int var42 = var55; !var58 && var42 < var36; var42++) {
                        for (int var43 = var56; !var58 && var43 < var40; var43++) {
                            for (int var44 = var38 + 1; !var58 && var44 >= var57 - 1; var44--) {
                                int var45 = (var42 * 16 + var43) * 128 + var44;
                                if (var44 >= 0 && var44 < 128) {
                                    if (shortStorage[var45] == TFTerrain.WATER_MOVING || shortStorage[var45] == TFTerrain.WATER_STILL) {
                                        var58 = true;
                                    }

                                    if (var44 != var57 - 1 && var42 != var55 && var42 != var36 - 1 && var43 != var56 && var43 != var40 - 1) {
                                        var44 = var57;
                                    }
                                }
                            }
                        }
                    }

                    if (!var58) {
                        for (int var66 = var55; var66 < var36; var66++) {
                            double var59 = (var66 + par3 * 16 + 0.5 - par6) / var29;

                            for (int var45 = var56; var45 < var40; var45++) {
                                double var46 = (var45 + par4 * 16 + 0.5 - par10) / var29;
                                int caveIndex = (var66 * 16 + var45) * 128 + var38;
                                boolean hitGrass = false;
                                if (var59 * var59 + var46 * var46 < 1.0) {
                                    for (int caveY = var38 - 1; caveY >= var57; caveY--) {
                                        double var51 = (caveY + 0.5 - par8) / var31;
                                        if (var51 > -0.7 && var59 * var59 + var51 * var51 + var46 * var46 < 20.0) {
                                            short blockID = shortStorage[caveIndex];
                                            if (blockID == TFTerrain.GRASS) {
                                                hitGrass = true;
                                            }

                                            if (blockID == TFTerrain.STONE || blockID == TFTerrain.DIRT || blockID == TFTerrain.GRASS) {
                                                if (var59 * var59 + var51 * var51 + var46 * var46 < 0.85) {
                                                    shortStorage[caveIndex] = caveY < 10 ? (short) TFTerrain.WATER_MOVING : 0;
                                                } else {
                                                    shortStorage[caveIndex] = hitGrass ? (short) TFTerrain.GRASS : (short) TFTerrain.DIRT;
                                                    hitGrass = false;
                                                }

                                                if (hitGrass && shortStorage[caveIndex - 1] == TFTerrain.DIRT) {
                                                    shortStorage[caveIndex - 1] = this.layout.biomeAt(var66 + par3 * 16, var45 + par4 * 16).topBlock;
                                                }
                                            }
                                        }

                                        caveIndex--;
                                    }
                                }
                            }
                        }

                        if (var54) {
                            break;
                        }
                    }
                }
            }

            par15++;
        }
    }

    private void recursiveGenerate(Random rand, int par2, int par3, int par4, int par5, short[] storage) {
        int var7 = rand.nextInt(rand.nextInt(rand.nextInt(40) + 1) + 1);
        if (rand.nextInt(15) != 0) {
            var7 = 0;
        }

        for (int var8 = 0; var8 < var7; var8++) {
            double var9 = par2 * 16 + rand.nextInt(16);
            double var11 = rand.nextInt(rand.nextInt(120) + 8);
            double var13 = par3 * 16 + rand.nextInt(16);
            int var15 = 1;
            if (rand.nextInt(4) == 0) {
                this.generateLargeCaveNode(rand, rand.nextLong(), par4, par5, storage, var9, var11, var13);
                var15 += rand.nextInt(4);
            }

            for (int var16 = 0; var16 < var15; var16++) {
                float var17 = rand.nextFloat() * (float) Math.PI * 2.0F;
                float var18 = (rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float var19 = rand.nextFloat() * 2.0F + rand.nextFloat();
                if (rand.nextInt(10) == 0) {
                    var19 *= rand.nextFloat() * rand.nextFloat() * 3.0F + 1.0F;
                }

                this.generateCaveNode(rand.nextLong(), par4, par5, storage, var9, var11, var13, var19, var17, var18, 0, 0, 1.0);
            }
        }
    }
}
