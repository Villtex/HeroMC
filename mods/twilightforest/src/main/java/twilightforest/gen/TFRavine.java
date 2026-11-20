package twilightforest.gen;

import hexxit.worldgen152.core.OldMath;

import java.util.Random;

/**
 * TFGenRavine (a MapGenBase4096 with range 8): one ravine start in 127 chunks, only where the nearest feature allows
 * decorations, at y 20 to 67. As in the caves, the carving loop starts one index above the y it tests.
 */
final class TFRavine {
    private static final int RANGE = 8;

    private final long seed;
    private final TFLayout layout;

    TFRavine(long seed, TFLayout layout) {
        this.seed = seed;
        this.layout = layout;
    }

    void generate(int cx, int cz, short[] storage) {
        Random rand = new Random(this.seed);
        long long1 = rand.nextLong();
        long long2 = rand.nextLong();
        Boolean decorations = null;
        for (int gx = cx - RANGE; gx <= cx + RANGE; gx++) {
            for (int gz = cz - RANGE; gz <= cz + RANGE; gz++) {
                long var13 = gx * long1;
                long var15 = gz * long2;
                rand.setSeed(var13 ^ var15 ^ this.seed);
                if (rand.nextInt(127) == 0) {
                    if (decorations == null) {
                        decorations = TFFeature.getNearestFeature(cx, cz, this.layout).chunkDecorationsEnabled;
                    }
                    if (decorations) {
                        this.start(rand, gx, gz, cx, cz, storage);
                    }
                }
            }
        }
    }

    /** recursiveGenerate after the one-in-127 check. */
    private void start(Random rand, int centerChunkX, int centerChunkZ, int currentChunkX, int currentChunkZ, short[] storage) {
        double d = centerChunkX * 16 + rand.nextInt(16);
        double d1 = rand.nextInt(rand.nextInt(40) + 8) + 20;
        double d2 = centerChunkZ * 16 + rand.nextInt(16);
        float f = rand.nextFloat() * 3.141593F * 2.0F;
        float f1 = (rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
        float f2 = (rand.nextFloat() * 2.0F + rand.nextFloat()) * 2.0F;
        this.generateRavine(rand.nextLong(), currentChunkX, currentChunkZ, storage, d, d1, d2, f2, f, f1, 0, 0, 3.0);
    }

    private void generateRavine(long l, int i, int j, short[] shortStorage, double d, double d1, double d2, float f, float f1, float f2,
                                int k, int i1, double d3) {
        Random random = new Random(l);
        float[] widths = new float[1024];
        double d4 = i * 16 + 8;
        double d5 = j * 16 + 8;
        float f3 = 0.0F;
        float f4 = 0.0F;
        if (i1 <= 0) {
            int j1 = RANGE * 16 - 16;
            i1 = j1 - random.nextInt(j1 / 4);
        }

        boolean flag = false;
        if (k == -1) {
            k = i1 / 2;
            flag = true;
        }

        float f5 = 1.0F;

        for (int k1 = 0; k1 < TFTerrain.WORLDHEIGHT; k1++) {
            if (k1 == 0 || random.nextInt(3) == 0) {
                f5 = 1.0F + random.nextFloat() * random.nextFloat() * 1.0F;
            }

            widths[k1] = f5 * f5;
        }

        for (; k < i1; k++) {
            double d6 = 1.5 + OldMath.sin(k * 3.141593F / i1) * f * 1.0F;
            double d7 = d6 * d3;
            d6 *= random.nextFloat() * 0.25 + 0.75;
            d7 *= random.nextFloat() * 0.25 + 0.75;
            float f6 = OldMath.cos(f2);
            float f7 = OldMath.sin(f2);
            d += OldMath.cos(f1) * f6;
            d1 += f7;
            d2 += OldMath.sin(f1) * f6;
            f2 *= 0.7F;
            f2 += f4 * 0.05F;
            f1 += f3 * 0.05F;
            f4 *= 0.8F;
            f3 *= 0.5F;
            f4 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            f3 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
            if (flag || random.nextInt(4) != 0) {
                double d8a = d - d4;
                double d9a = d2 - d5;
                double d10a = i1 - k;
                double d11 = f + 2.0F + 16.0F;
                if (d8a * d8a + d9a * d9a - d10a * d10a > d11 * d11) {
                    return;
                }

                if (!(d < d4 - 16.0 - d6 * 2.0) && !(d2 < d5 - 16.0 - d6 * 2.0) && !(d > d4 + 16.0 + d6 * 2.0) && !(d2 > d5 + 16.0 + d6 * 2.0)) {
                    int d8 = OldMath.floor(d - d6) - i * 16 - 1;
                    int l1 = OldMath.floor(d + d6) - i * 16 + 1;
                    int d9 = OldMath.floor(d1 - d7) - 1;
                    int i2 = OldMath.floor(d1 + d7) + 1;
                    int d10 = OldMath.floor(d2 - d6) - j * 16 - 1;
                    int j2 = OldMath.floor(d2 + d6) - j * 16 + 1;
                    if (d8 < 0) {
                        d8 = 0;
                    }

                    if (l1 > 16) {
                        l1 = 16;
                    }

                    if (d9 < 1) {
                        d9 = 1;
                    }

                    if (i2 > TFTerrain.WORLDHEIGHT - 8) {
                        i2 = TFTerrain.WORLDHEIGHT - 8;
                    }

                    if (d10 < 0) {
                        d10 = 0;
                    }

                    if (j2 > 16) {
                        j2 = 16;
                    }

                    boolean flag1 = false;

                    for (int k2 = d8; !flag1 && k2 < l1; k2++) {
                        for (int i3 = d10; !flag1 && i3 < j2; i3++) {
                            for (int j3 = i2 + 1; !flag1 && j3 >= d9 - 1; j3--) {
                                int k3 = (k2 * 16 + i3) * TFTerrain.WORLDHEIGHT + j3;
                                if (j3 >= 0 && j3 < TFTerrain.WORLDHEIGHT) {
                                    if (shortStorage[k3] == TFTerrain.WATER_MOVING || shortStorage[k3] == TFTerrain.WATER_STILL) {
                                        flag1 = true;
                                    }

                                    if (j3 != d9 - 1 && k2 != d8 && k2 != l1 - 1 && i3 != d10 && i3 != j2 - 1) {
                                        j3 = d9;
                                    }
                                }
                            }
                        }
                    }

                    if (!flag1) {
                        for (int l2 = d8; l2 < l1; l2++) {
                            double d12 = (l2 + i * 16 + 0.5 - d) / d6;

                            for (int l3 = d10; l3 < j2; l3++) {
                                double d13 = (l3 + j * 16 + 0.5 - d2) / d6;
                                int i4 = (l2 * 16 + l3) * TFTerrain.WORLDHEIGHT + i2;
                                boolean flag2 = false;
                                if (!(d12 * d12 + d13 * d13 >= 1.0)) {
                                    for (int j4 = i2 - 1; j4 >= d9; j4--) {
                                        double d14 = (j4 + 0.5 - d1) / d7;
                                        if ((d12 * d12 + d13 * d13) * widths[j4] + d14 * d14 / 6.0 < 1.0) {
                                            short curentBlock = shortStorage[i4];
                                            if (curentBlock == TFTerrain.GRASS) {
                                                flag2 = true;
                                            }

                                            if (curentBlock == TFTerrain.STONE || curentBlock == TFTerrain.DIRT || curentBlock == TFTerrain.GRASS) {
                                                shortStorage[i4] = 0;
                                                if (flag2 && shortStorage[i4 - 1] == TFTerrain.DIRT) {
                                                    shortStorage[i4 - 1] = this.layout.biomeAt(l2 + i * 16, l3 + j * 16).topBlock;
                                                }
                                            }
                                        }

                                        i4--;
                                    }
                                }
                            }
                        }

                        if (flag) {
                            break;
                        }
                    }
                }
            }
        }
    }
}
