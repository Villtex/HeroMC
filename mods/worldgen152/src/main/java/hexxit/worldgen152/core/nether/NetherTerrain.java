package hexxit.worldgen152.core.nether;

import hexxit.worldgen152.core.OctaveNoise;
import hexxit.worldgen152.core.OldChunk;

import java.util.Arrays;
import java.util.Random;

/**
 * The Nether terrain of Hexxit 1.0.11: {@code NetheriteChunkProvider.provideChunk} of Natura 2.1.5.1, which
 * the pack ("Override Nether") put in place of the {@code ChunkProviderHell} of Minecraft 1.5.2. It is the
 * vanilla generator with two changes: very dense rock is tainted soil, and the soul sand and gravel beaches
 * around the lava sea get heat sand and tainted soil below them.
 *
 * <p>A chunk is a pure function of world seed and chunk position, so one instance serves all threads.
 */
public final class NetherTerrain {
    public static final int BEDROCK = 7;
    public static final int LAVA_STILL = 11;
    public static final int GRAVEL = 13;
    public static final int NETHERRACK = 87;
    public static final int SOUL_SAND = 88;
    /** Natura's heat sand ("Requires blockID &lt; 256"; id of the pack configuration). */
    public static final int HEAT_SAND = 252;
    /** Natura's tainted soil (id of the pack configuration). */
    public static final int TAINTED_SOIL = 253;
    /** BiomeGenBase.hell. */
    public static final int HELL_BIOME = 8;

    private final long seed;
    private final OctaveNoise netherNoiseGen1;
    private final OctaveNoise netherNoiseGen2;
    private final OctaveNoise netherNoiseGen3;
    private final OctaveNoise slowsandGravelNoiseGen;
    private final OctaveNoise netherrackExclusivityNoiseGen;
    private final OctaveNoise netherNoiseGen4;
    private final OctaveNoise netherNoiseGen5;
    private final NetherCaves caves;

    public NetherTerrain(long seed) {
        this.seed = seed;
        // Construction order matters: all generators draw from the same seeded stream.
        Random random = new Random(seed);
        this.netherNoiseGen1 = new OctaveNoise(random, 16);
        this.netherNoiseGen2 = new OctaveNoise(random, 16);
        this.netherNoiseGen3 = new OctaveNoise(random, 8);
        this.slowsandGravelNoiseGen = new OctaveNoise(random, 4);
        this.netherrackExclusivityNoiseGen = new OctaveNoise(random, 4);
        this.netherNoiseGen4 = new OctaveNoise(random, 10);
        this.netherNoiseGen5 = new OctaveNoise(random, 16);
        this.caves = new NetherCaves(seed);
    }

    public long seed() {
        return this.seed;
    }

    /** provideChunk without the fortress, which only writes blocks when the chunk is populated. */
    public OldChunk generate(int chunkX, int chunkZ) {
        Random hellRNG = new Random(chunkX * 341873128712L + chunkZ * 132897987541L);
        byte[] blocks = new byte[32768];
        this.generateNetherTerrain(chunkX, chunkZ, blocks);
        this.replaceBlocksForBiome(chunkX, chunkZ, blocks, hellRNG);
        this.caves.carve(chunkX, chunkZ, blocks);
        byte[] biomes = new byte[256];
        Arrays.fill(biomes, (byte) HELL_BIOME);
        return new OldChunk(chunkX, chunkZ, blocks, biomes);
    }

    /**
     * The state the chunk provider's random generator is in after {@link #generate} of a chunk.
     *
     * <p>1.5.2 populated Nether chunks with that generator without seeding it again, so what a chunk got
     * depended on which chunk had been generated last. On a row by row generation that is the chunk at +1/+1
     * of the one being populated, see {@code OldNetherPopulator}. The number of draws does not depend on the
     * terrain, so this needs no noise.
     */
    public static Random randomAfter(int chunkX, int chunkZ) {
        Random hellRNG = new Random(chunkX * 341873128712L + chunkZ * 132897987541L);
        for (int column = 0; column < 256; column++) {
            hellRNG.nextDouble();
            hellRNG.nextDouble();
            hellRNG.nextDouble();
            for (int y = 127; y >= 0; y--) {
                if (y < 127 - hellRNG.nextInt(5)) {
                    hellRNG.nextInt(5);
                }
            }
        }
        return hellRNG;
    }

    private void generateNetherTerrain(int chunkX, int chunkZ, byte[] lowerIDs) {
        byte noiseInit = 4;
        byte b1 = 32;
        int k = noiseInit + 1;
        byte b2 = 17;
        int l = noiseInit + 1;
        double[] noiseField = this.initializeNoiseField(chunkX * noiseInit, 0, chunkZ * noiseInit, k, b2, l);

        for (int iterX = 0; iterX < noiseInit; iterX++) {
            for (int iterZ = 0; iterZ < noiseInit; iterZ++) {
                for (int iterY = 0; iterY < 16; iterY++) {
                    double noiseOffset = 0.125;
                    double n1 = noiseField[((iterX + 0) * l + iterZ + 0) * b2 + iterY + 0];
                    double n2 = noiseField[((iterX + 0) * l + iterZ + 1) * b2 + iterY + 0];
                    double n3 = noiseField[((iterX + 1) * l + iterZ + 0) * b2 + iterY + 0];
                    double n4 = noiseField[((iterX + 1) * l + iterZ + 1) * b2 + iterY + 0];
                    double n5 = (noiseField[((iterX + 0) * l + iterZ + 0) * b2 + iterY + 1] - n1) * noiseOffset;
                    double n6 = (noiseField[((iterX + 0) * l + iterZ + 1) * b2 + iterY + 1] - n2) * noiseOffset;
                    double n7 = (noiseField[((iterX + 1) * l + iterZ + 0) * b2 + iterY + 1] - n3) * noiseOffset;
                    double n8 = (noiseField[((iterX + 1) * l + iterZ + 1) * b2 + iterY + 1] - n4) * noiseOffset;

                    for (int offsetY = 0; offsetY < 8; offsetY++) {
                        double d9 = 0.25;
                        double d10 = n1;
                        double d11 = n2;
                        double d12 = (n3 - n1) * d9;
                        double d13 = (n4 - n2) * d9;

                        for (int offsetX = 0; offsetX < 4; offsetX++) {
                            int layerPos = offsetX + iterX * 4 << 11 | 0 + iterZ * 4 << 7 | iterY * 8 + offsetY;
                            short amountPerLayer = 128;
                            double d14 = 0.25;
                            double lValue = d10;
                            double lOffset = (d11 - d10) * d14;

                            for (int k2 = 0; k2 < 4; k2++) {
                                int blockID = 0;
                                if (iterY * 8 + offsetY < b1) {
                                    blockID = LAVA_STILL;
                                }

                                if (lValue > 0.0) {
                                    blockID = NETHERRACK;
                                }

                                if (lValue > 56.0) {
                                    blockID = TAINTED_SOIL;
                                }

                                lowerIDs[layerPos] = (byte) blockID;
                                layerPos += amountPerLayer;
                                lValue += lOffset;
                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        n1 += n5;
                        n2 += n6;
                        n3 += n7;
                        n4 += n8;
                    }
                }
            }
        }
    }

    private void replaceBlocksForBiome(int par1, int par2, byte[] lowerIDs, Random hellRNG) {
        byte seaLevel = 64;
        double d0 = 0.03125;
        double[] slowsandNoise = this.slowsandGravelNoiseGen.generate(par1 * 16, par2 * 16, 0, 16, 16, 1, d0, d0, 1.0);
        double[] gravelNoise = this.slowsandGravelNoiseGen.generate(par1 * 16, 109, par2 * 16, 16, 1, 16, d0, 1.0, d0);
        double[] netherrackExclusivityNoise = this.netherrackExclusivityNoiseGen
                .generate(par1 * 16, par2 * 16, 0, 16, 16, 1, d0 * 2.0, d0 * 2.0, d0 * 2.0);

        for (int iterX = 0; iterX < 16; iterX++) {
            for (int iterZ = 0; iterZ < 16; iterZ++) {
                boolean flag = slowsandNoise[iterX + iterZ * 16] + hellRNG.nextDouble() * 0.2 > 0.0;
                boolean flag1 = gravelNoise[iterX + iterZ * 16] + hellRNG.nextDouble() * 0.2 > 0.0;
                int i1 = (int) (netherrackExclusivityNoise[iterX + iterZ * 16] / 3.0 + 3.0 + hellRNG.nextDouble() * 0.25);
                int j1 = -1;
                byte b1 = (byte) NETHERRACK;
                byte b2 = (byte) TAINTED_SOIL;

                for (int k1 = 127; k1 >= 0; k1--) {
                    int l1 = (iterZ * 16 + iterX) * 128 + k1;
                    if (k1 < 127 - hellRNG.nextInt(5) && k1 > 0 + hellRNG.nextInt(5)) {
                        short b3 = lowerIDs[l1];
                        if (b3 == 0) {
                            j1 = -1;
                        } else if (b3 == NETHERRACK) {
                            if (j1 == -1) {
                                if (i1 <= 0) {
                                    b1 = 0;
                                    b2 = (byte) NETHERRACK;
                                } else if (k1 >= seaLevel - 4 && k1 <= seaLevel + 1) {
                                    b1 = (byte) NETHERRACK;
                                    b2 = (byte) TAINTED_SOIL;
                                    if (flag1) {
                                        b1 = (byte) GRAVEL;
                                    }

                                    if (flag1) {
                                        b2 = (byte) NETHERRACK;
                                    }

                                    if (flag) {
                                        b1 = (byte) SOUL_SAND;
                                    }

                                    if (flag) {
                                        b2 = (byte) HEAT_SAND;
                                    }
                                }

                                if (k1 < seaLevel && b1 == 0) {
                                    b1 = (byte) LAVA_STILL;
                                }

                                j1 = i1;
                                if (k1 >= seaLevel - 1) {
                                    lowerIDs[l1] = b1;
                                } else {
                                    lowerIDs[l1] = b2;
                                }
                            } else if (j1 > 0) {
                                j1--;
                                lowerIDs[l1] = b2;
                            }
                        }
                    } else {
                        lowerIDs[l1] = (byte) BEDROCK;
                    }
                }
            }
        }
    }

    private double[] initializeNoiseField(int par2, int par3, int par4, int par5, int par6, int par7) {
        double[] par1ArrayOfDouble = new double[par5 * par6 * par7];
        double d0 = 684.412;
        double d1 = 2053.236;
        double[] noiseData4 = this.netherNoiseGen4.generate(par2, par3, par4, par5, 1, par7, 1.0, 0.0, 1.0);
        double[] noiseData5 = this.netherNoiseGen5.generate(par2, par3, par4, par5, 1, par7, 100.0, 0.0, 100.0);
        double[] noiseData1 = this.netherNoiseGen3.generate(par2, par3, par4, par5, par6, par7, d0 / 80.0, d1 / 60.0, d0 / 80.0);
        double[] noiseData2 = this.netherNoiseGen1.generate(par2, par3, par4, par5, par6, par7, d0, d1, d0);
        double[] noiseData3 = this.netherNoiseGen2.generate(par2, par3, par4, par5, par6, par7, d0, d1, d0);
        int k1 = 0;
        int l1 = 0;
        double[] adouble1 = new double[par6];

        for (int i2 = 0; i2 < par6; i2++) {
            adouble1[i2] = Math.cos(i2 * Math.PI * 6.0 / par6) * 2.0;
            double d2 = i2;
            if (i2 > par6 / 2) {
                d2 = par6 - 1 - i2;
            }

            if (d2 < 4.0) {
                d2 = 4.0 - d2;
                adouble1[i2] -= d2 * d2 * d2 * 10.0;
            }
        }

        for (int i2 = 0; i2 < par5; i2++) {
            for (int j2 = 0; j2 < par7; j2++) {
                double d3 = (noiseData4[l1] + 256.0) / 512.0;
                if (d3 > 1.0) {
                    d3 = 1.0;
                }

                double d4 = 0.0;
                double d5 = noiseData5[l1] / 8000.0;
                if (d5 < 0.0) {
                    d5 = -d5;
                }

                d5 = d5 * 3.0 - 3.0;
                if (d5 < 0.0) {
                    d5 /= 2.0;
                    if (d5 < -1.0) {
                        d5 = -1.0;
                    }

                    d5 /= 1.4;
                    d5 /= 2.0;
                    d3 = 0.0;
                } else {
                    if (d5 > 1.0) {
                        d5 = 1.0;
                    }

                    d5 /= 6.0;
                }

                d3 += 0.5;
                d5 = d5 * par6 / 16.0;
                l1++;

                for (int k2 = 0; k2 < par6; k2++) {
                    double d6 = 0.0;
                    double d7 = adouble1[k2];
                    double d8 = noiseData2[k1] / 512.0;
                    double d9 = noiseData3[k1] / 512.0;
                    double d10 = (noiseData1[k1] / 10.0 + 1.0) / 2.0;
                    if (d10 < 0.0) {
                        d6 = d8;
                    } else if (d10 > 1.0) {
                        d6 = d9;
                    } else {
                        d6 = d8 + (d9 - d8) * d10;
                    }

                    d6 -= d7;
                    if (k2 > par6 - 4) {
                        double d11 = (k2 - (par6 - 4)) / 3.0F;
                        d6 = d6 * (1.0 - d11) + -10.0 * d11;
                    }

                    if (k2 < d4) {
                        double d11 = (d4 - k2) / 4.0;
                        if (d11 < 0.0) {
                            d11 = 0.0;
                        }

                        if (d11 > 1.0) {
                            d11 = 1.0;
                        }

                        d6 = d6 * (1.0 - d11) + -10.0 * d11;
                    }

                    par1ArrayOfDouble[k1] = d6;
                    k1++;
                }
            }
        }

        return par1ArrayOfDouble;
    }
}
