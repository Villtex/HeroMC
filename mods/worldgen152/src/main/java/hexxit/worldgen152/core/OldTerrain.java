package hexxit.worldgen152.core;

import java.util.Random;

/**
 * The overworld terrain of Minecraft 1.5.2 (ChunkProviderGenerate.provideChunk): density-noise landscape
 * shaped by the biome height ranges, biome surface layers, bedrock, caves and ravines.
 *
 * <p>A chunk is a pure function of world seed and chunk position, so one instance serves all threads.
 */
public final class OldTerrain {
    private static final int SEA_LEVEL = 63;
    private static final float[] PARABOLIC_FIELD = new float[25];

    static {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                PARABOLIC_FIELD[x + 2 + (z + 2) * 5] = 10.0F / OldMath.sqrt(x * x + z * z + 0.2F);
            }
        }
    }

    private final long seed;
    private final BiomeLayout layout;
    private final OctaveNoise lowerLimit;
    private final OctaveNoise upperLimit;
    private final OctaveNoise mainNoise;
    private final OctaveNoise surfaceNoise;
    private final OctaveNoise unusedNoise;
    private final OctaveNoise depthNoise;
    private final OldCaves caves;
    private final OldRavines ravines;

    public OldTerrain(BiomeLayout layout) {
        this.seed = layout.seed();
        this.layout = layout;
        // Construction order matters: all generators draw from the same seeded stream.
        Random random = new Random(this.seed);
        this.lowerLimit = new OctaveNoise(random, 16);
        this.upperLimit = new OctaveNoise(random, 16);
        this.mainNoise = new OctaveNoise(random, 8);
        this.surfaceNoise = new OctaveNoise(random, 4);
        this.unusedNoise = new OctaveNoise(random, 10);
        this.depthNoise = new OctaveNoise(random, 16);
        this.caves = new OldCaves(this.seed);
        this.ravines = new OldRavines(this.seed);
    }

    public BiomeLayout layout() {
        return this.layout;
    }

    public OldChunk generate(int chunkX, int chunkZ) {
        Random random = new Random(chunkX * 341873128712L + chunkZ * 132897987541L);
        byte[] blocks = new byte[32768];
        this.shape(chunkX, chunkZ, blocks);
        byte[] biomeIds = this.layout.chunkIds(chunkX, chunkZ);
        OldBiome[] biomes = new OldBiome[256];
        for (int i = 0; i < 256; i++) {
            biomes[i] = OldBiomes.byId(biomeIds[i] & 0xFF);
        }
        this.surface(chunkX, chunkZ, blocks, biomes, random);
        this.caves.carve(chunkX, chunkZ, blocks, biomes);
        this.ravines.carve(chunkX, chunkZ, blocks, biomes);
        return new OldChunk(chunkX, chunkZ, blocks, biomeIds);
    }

    /** generateTerrain: stone below the density surface, water up to sea level. */
    private void shape(int chunkX, int chunkZ, byte[] blocks) {
        byte b0 = 4;
        byte b1 = 16;
        byte b2 = SEA_LEVEL;
        int k = b0 + 1;
        byte b3 = 17;
        int l = b0 + 1;
        int[] quarterIds = this.layout.quarterIds(chunkX * 4 - 2, chunkZ * 4 - 2, k + 5, l + 5);
        double[] noise = this.densityField(quarterIds, chunkX * b0, 0, chunkZ * b0, k, b3, l);

        for (int i1 = 0; i1 < b0; i1++) {
            for (int j1 = 0; j1 < b0; j1++) {
                for (int k1 = 0; k1 < b1; k1++) {
                    double d0 = 0.125;
                    double d1 = noise[((i1 + 0) * l + j1 + 0) * b3 + k1 + 0];
                    double d2 = noise[((i1 + 0) * l + j1 + 1) * b3 + k1 + 0];
                    double d3 = noise[((i1 + 1) * l + j1 + 0) * b3 + k1 + 0];
                    double d4 = noise[((i1 + 1) * l + j1 + 1) * b3 + k1 + 0];
                    double d5 = (noise[((i1 + 0) * l + j1 + 0) * b3 + k1 + 1] - d1) * d0;
                    double d6 = (noise[((i1 + 0) * l + j1 + 1) * b3 + k1 + 1] - d2) * d0;
                    double d7 = (noise[((i1 + 1) * l + j1 + 0) * b3 + k1 + 1] - d3) * d0;
                    double d8 = (noise[((i1 + 1) * l + j1 + 1) * b3 + k1 + 1] - d4) * d0;

                    for (int l1 = 0; l1 < 8; l1++) {
                        double d9 = 0.25;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * d9;
                        double d13 = (d4 - d2) * d9;

                        for (int i2 = 0; i2 < 4; i2++) {
                            int j2 = i2 + i1 * 4 << 11 | 0 + j1 * 4 << 7 | k1 * 8 + l1;
                            short step = 128;
                            j2 -= step;
                            double d14 = 0.25;
                            double d15 = (d11 - d10) * d14;
                            double d16 = d10 - d15;

                            for (int k2 = 0; k2 < 4; k2++) {
                                if ((d16 += d15) > 0.0) {
                                    blocks[j2 += step] = OldBlocks.STONE;
                                } else if (k1 * 8 + l1 < b2) {
                                    blocks[j2 += step] = OldBlocks.WATER_STILL;
                                } else {
                                    blocks[j2 += step] = 0;
                                }
                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }
    }

    /** initializeNoiseField: 5x17x5 density samples; {@code quarterIds} is the 10x10 biome grid around them. */
    private double[] densityField(int[] quarterIds, int x, int y, int z, int xSize, int ySize, int zSize) {
        double[] out = new double[xSize * ySize * zSize];
        double d0 = 684.412;
        double d1 = 684.412;
        double[] depth = this.depthNoise.generate(x, z, xSize, zSize, 200.0, 200.0);
        double[] main = this.mainNoise.generate(x, y, z, xSize, ySize, zSize, d0 / 80.0, d1 / 160.0, d0 / 80.0);
        double[] lower = this.lowerLimit.generate(x, y, z, xSize, ySize, zSize, d0, d1, d0);
        double[] upper = this.upperLimit.generate(x, y, z, xSize, ySize, zSize, d0, d1, d0);
        int i2 = 0;
        int j2 = 0;

        for (int k2 = 0; k2 < xSize; k2++) {
            for (int l2 = 0; l2 < zSize; l2++) {
                float f1 = 0.0F;
                float f2 = 0.0F;
                float f3 = 0.0F;
                byte b0 = 2;
                OldBiome center = OldBiomes.byId(quarterIds[k2 + 2 + (l2 + 2) * (xSize + 5)]);

                for (int i3 = -b0; i3 <= b0; i3++) {
                    for (int j3 = -b0; j3 <= b0; j3++) {
                        OldBiome near = OldBiomes.byId(quarterIds[k2 + i3 + 2 + (l2 + j3 + 2) * (xSize + 5)]);
                        float f4 = PARABOLIC_FIELD[i3 + 2 + (j3 + 2) * 5] / (near.minHeight + 2.0F);
                        if (near.minHeight > center.minHeight) {
                            f4 /= 2.0F;
                        }

                        f1 += near.maxHeight * f4;
                        f2 += near.minHeight * f4;
                        f3 += f4;
                    }
                }

                f1 /= f3;
                f2 /= f3;
                f1 = f1 * 0.9F + 0.1F;
                f2 = (f2 * 4.0F - 1.0F) / 8.0F;
                double d2 = depth[j2] / 8000.0;
                if (d2 < 0.0) {
                    d2 = -d2 * 0.3;
                }

                d2 = d2 * 3.0 - 2.0;
                if (d2 < 0.0) {
                    d2 /= 2.0;
                    if (d2 < -1.0) {
                        d2 = -1.0;
                    }

                    d2 /= 1.4;
                    d2 /= 2.0;
                } else {
                    if (d2 > 1.0) {
                        d2 = 1.0;
                    }

                    d2 /= 8.0;
                }

                j2++;

                for (int k3 = 0; k3 < ySize; k3++) {
                    double d3 = f2;
                    double d4 = f1;
                    d3 += d2 * 0.2;
                    d3 = d3 * ySize / 16.0;
                    double d5 = ySize / 2.0 + d3 * 4.0;
                    double d6 = 0.0;
                    double d7 = (k3 - d5) * 12.0 * 128.0 / 128.0 / d4;
                    if (d7 < 0.0) {
                        d7 *= 4.0;
                    }

                    double d8 = lower[i2] / 512.0;
                    double d9 = upper[i2] / 512.0;
                    double d10 = (main[i2] / 10.0 + 1.0) / 2.0;
                    if (d10 < 0.0) {
                        d6 = d8;
                    } else if (d10 > 1.0) {
                        d6 = d9;
                    } else {
                        d6 = d8 + (d9 - d8) * d10;
                    }

                    d6 -= d7;
                    if (k3 > ySize - 4) {
                        double d11 = (k3 - (ySize - 4)) / 3.0F;
                        d6 = d6 * (1.0 - d11) + -10.0 * d11;
                    }

                    out[i2] = d6;
                    i2++;
                }
            }
        }

        return out;
    }

    /** replaceBlocksForBiome: bedrock floor and the top/filler layers of each column's biome. */
    private void surface(int chunkX, int chunkZ, byte[] blocks, OldBiome[] biomes, Random random) {
        byte b0 = SEA_LEVEL;
        double d0 = 0.03125;
        double[] stoneNoise = this.surfaceNoise.generate(chunkX * 16, chunkZ * 16, 0, 16, 16, 1, d0 * 2.0, d0 * 2.0, d0 * 2.0);

        for (int k = 0; k < 16; k++) {
            for (int l = 0; l < 16; l++) {
                OldBiome biome = biomes[l + k * 16];
                float f = biome.temperature;
                int i1 = (int) (stoneNoise[k + l * 16] / 3.0 + 3.0 + random.nextDouble() * 0.25);
                int j1 = -1;
                byte b1 = biome.topBlock;
                byte b2 = biome.fillerBlock;

                for (int k1 = 127; k1 >= 0; k1--) {
                    int l1 = (l * 16 + k) * 128 + k1;
                    if (k1 <= 0 + random.nextInt(5)) {
                        blocks[l1] = OldBlocks.BEDROCK;
                    } else {
                        byte b3 = blocks[l1];
                        if (b3 == 0) {
                            j1 = -1;
                        } else if (b3 == OldBlocks.STONE) {
                            if (j1 == -1) {
                                if (i1 <= 0) {
                                    b1 = 0;
                                    b2 = OldBlocks.STONE;
                                } else if (k1 >= b0 - 4 && k1 <= b0 + 1) {
                                    b1 = biome.topBlock;
                                    b2 = biome.fillerBlock;
                                }

                                if (k1 < b0 && b1 == 0) {
                                    if (f < 0.15F) {
                                        b1 = OldBlocks.ICE;
                                    } else {
                                        b1 = OldBlocks.WATER_STILL;
                                    }
                                }

                                j1 = i1;
                                if (k1 >= b0 - 1) {
                                    blocks[l1] = b1;
                                } else {
                                    blocks[l1] = b2;
                                }
                            } else if (j1 > 0) {
                                j1--;
                                blocks[l1] = b2;
                                if (j1 == 0 && b2 == OldBlocks.SAND) {
                                    j1 = random.nextInt(4);
                                    b2 = OldBlocks.SANDSTONE;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
