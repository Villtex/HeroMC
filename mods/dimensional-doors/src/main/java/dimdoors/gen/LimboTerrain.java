package dimdoors.gen;

import hexxit.worldgen152.core.OctaveNoise;
import hexxit.worldgen152.core.OldChunk;
import hexxit.worldgen152.core.OldMath;

import java.util.Random;

/**
 * LimboGenerator of Dimensional Doors 1.4.1 without game classes: the floating hills of Unraveled Fabric over a floor
 * of Eternal Fabric (everything empty below y = 19), 128 blocks high. The noise is seeded with the constant 45, not
 * the world seed: Limbo looks the same in every world. No caves, no population (the original marked its chunks as
 * populated right away), plains heights for every column.
 */
public final class LimboTerrain {
    private static final int FLOOR = 19;
    private static final float PLAINS_MIN = 0.1F;
    private static final float PLAINS_MAX = 0.3F;
    private static final float[] PARABOLIC_FIELD = new float[25];

    static {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                PARABOLIC_FIELD[x + 2 + (z + 2) * 5] = 10.0F / OldMath.sqrt(x * x + z * z + 0.2F);
            }
        }
    }

    private final OctaveNoise noise1;
    private final OctaveNoise noise2;
    private final OctaveNoise noise3;
    private final OctaveNoise noise6;

    public LimboTerrain() {
        Random rand = new Random(45L);
        this.noise1 = new OctaveNoise(rand, 16);
        this.noise2 = new OctaveNoise(rand, 16);
        this.noise3 = new OctaveNoise(rand, 80);
        new OctaveNoise(rand, 4);   // noiseGen4, unused
        new OctaveNoise(rand, 10);  // noiseGen5, sampled but never read
        this.noise6 = new OctaveNoise(rand, 16);
    }

    private double[] noiseField(int x, int y, int z, int xSize, int ySize, int zSize) {
        double[] field = new double[xSize * ySize * zSize];
        double horizontal = 884.412;
        double vertical = 9840.412;
        double[] n6 = this.noise6.generate(x, z, xSize, zSize, 200.0, 200.0);
        double[] n3 = this.noise3.generate(x, y, z, xSize, ySize, zSize, horizontal / 80.0, vertical / 160.0, horizontal / 80.0);
        double[] n1 = this.noise1.generate(x, y, z, xSize, ySize, zSize, horizontal, vertical, horizontal);
        double[] n2 = this.noise2.generate(x, y, z, xSize, ySize, zSize, horizontal, vertical, horizontal);
        int i = 0;
        int i2 = 0;
        for (int ix = 0; ix < xSize; ix++) {
            for (int iz = 0; iz < zSize; iz++) {
                float max = 0.0F;
                float min = 0.0F;
                float total = 0.0F;
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        float weight = PARABOLIC_FIELD[dx + 2 + (dz + 2) * 5] / (PLAINS_MIN + 9.0F);
                        max += PLAINS_MAX * weight + 4.0F;
                        min += PLAINS_MIN * weight - 1.0F;
                        total += weight;
                    }
                }
                max /= total;
                min /= total;
                max = max * 0.9F + 0.1F;
                min = (min * 4.0F - 1.0F) / 8.0F;
                double depth = n6[i2] / 8000.0;
                if (depth < 0.0) {
                    depth = -depth * 0.3;
                }
                depth = depth * 3.0 - 2.0;
                if (depth < 0.0) {
                    depth /= 2.0;
                    if (depth < -1.0) {
                        depth = -1.0;
                    }
                    depth /= 1.4;
                    depth /= 2.0;
                } else {
                    if (depth > 1.0) {
                        depth = 1.0;
                    }
                    depth /= 8.0;
                }
                i2++;
                for (int iy = 0; iy < ySize; iy++) {
                    double base = min;
                    double scale = max;
                    base += depth * 0.2;
                    base = base * ySize / 16.0;
                    double centre = ySize / 2.0 + base * 4.0;
                    double offset = (iy - centre) * 12.0 * 128.0 / 128.0 / scale;
                    if (offset < 0.0) {
                        offset *= 4.0;
                    }
                    double low = n1[i] / 512.0;
                    double high = n2[i] / 512.0;
                    double blend = (n3[i] / 10.0 + 1.0) / 2.0;
                    double value;
                    if (blend < 0.0) {
                        value = low;
                    } else if (blend > 1.0) {
                        value = high;
                    } else {
                        value = low + (high - low) * blend;
                    }
                    value -= offset;
                    if (iy > ySize - 4) {
                        double fade = (iy - (ySize - 4)) / 3.0F;
                        value = value * (1.0 - fade) + -10.0 * fade;
                    }
                    field[i] = value;
                    i++;
                }
            }
        }
        return field;
    }

    /** The blocks of a chunk, laid out as {@link OldChunk}: (x * 16 + z) * 128 + y. */
    public byte[] generate(int chunkX, int chunkZ) {
        byte[] blocks = new byte[16 * 16 * OldChunk.HEIGHT];
        int cells = 4;
        int xSize = cells + 1;
        int ySize = 17;
        int zSize = cells + 1;
        double[] noise = this.noiseField(chunkX * cells, 0, chunkZ * cells, xSize, ySize, zSize);
        for (int cx = 0; cx < cells; cx++) {
            for (int cz = 0; cz < cells; cz++) {
                for (int cy = 0; cy < 16; cy++) {
                    double step = 0.125;
                    double n00 = noise[((cx) * zSize + cz) * ySize + cy];
                    double n01 = noise[((cx) * zSize + cz + 1) * ySize + cy];
                    double n10 = noise[((cx + 1) * zSize + cz) * ySize + cy];
                    double n11 = noise[((cx + 1) * zSize + cz + 1) * ySize + cy];
                    double d00 = (noise[((cx) * zSize + cz) * ySize + cy + 1] - n00) * step;
                    double d01 = (noise[((cx) * zSize + cz + 1) * ySize + cy + 1] - n01) * step;
                    double d10 = (noise[((cx + 1) * zSize + cz) * ySize + cy + 1] - n10) * step;
                    double d11 = (noise[((cx + 1) * zSize + cz + 1) * ySize + cy + 1] - n11) * step;
                    for (int sy = 0; sy < 8; sy++) {
                        double xStep = 0.25;
                        double a = n00;
                        double b = n01;
                        double da = (n10 - n00) * xStep;
                        double db = (n11 - n01) * xStep;
                        for (int sx = 0; sx < 4; sx++) {
                            int index = sx + cx * 4 << 11 | cz * 4 << 7 | cy * 8 + sy;
                            index -= 128;
                            double zStep = 0.25;
                            double dz = (b - a) * zStep;
                            double value = a - dz;
                            for (int sz = 0; sz < 4; sz++) {
                                index += 128;
                                if ((value += dz) > 0.0) {
                                    blocks[index] = (byte) RiftGen.UNRAVELED_FABRIC;
                                } else if (cy * 8 + sy < FLOOR) {
                                    blocks[index] = (byte) RiftGen.ETERNAL_FABRIC;
                                }
                            }
                            a += da;
                            b += db;
                        }
                        n00 += d00;
                        n01 += d01;
                        n10 += d10;
                        n11 += d11;
                    }
                }
            }
        }
        return blocks;
    }
}
