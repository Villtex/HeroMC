package hexxit.worldgen152.core;

import java.util.Random;

/** One octave of improved Perlin noise, seeded and evaluated exactly like 1.5.2's NoiseGeneratorPerlin. */
public final class PerlinNoise {
    private final int[] permutations = new int[512];
    private final double xCoord;
    private final double yCoord;
    private final double zCoord;

    public PerlinNoise(Random random) {
        this.xCoord = random.nextDouble() * 256.0;
        this.yCoord = random.nextDouble() * 256.0;
        this.zCoord = random.nextDouble() * 256.0;
        for (int i = 0; i < 256; i++) {
            this.permutations[i] = i;
        }
        for (int i = 0; i < 256; i++) {
            int j = random.nextInt(256 - i) + i;
            int swap = this.permutations[i];
            this.permutations[i] = this.permutations[j];
            this.permutations[j] = swap;
            this.permutations[i + 256] = this.permutations[i];
        }
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad2(int hash, double x, double z) {
        int h = hash & 15;
        double u = (1 - ((h & 8) >> 3)) * x;
        double v = h < 4 ? 0.0 : (h != 12 && h != 14 ? z : x);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    private static double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : (h != 12 && h != 14 ? z : x);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    /** Adds this octave to {@code out}, laid out as [x][z][y]. */
    public void populate(double[] out, double x0, double y0, double z0, int xSize, int ySize, int zSize,
                         double xScale, double yScale, double zScale, double amplitude) {
        int[] p = this.permutations;
        if (ySize == 1) {
            int index = 0;
            double inv = 1.0 / amplitude;
            for (int ix = 0; ix < xSize; ix++) {
                double x = x0 + ix * xScale + this.xCoord;
                int xi = (int) x;
                if (x < xi) {
                    xi--;
                }
                int xp = xi & 0xFF;
                x -= xi;
                double xFade = x * x * x * (x * (x * 6.0 - 15.0) + 10.0);
                for (int iz = 0; iz < zSize; iz++) {
                    double z = z0 + iz * zScale + this.zCoord;
                    int zi = (int) z;
                    if (z < zi) {
                        zi--;
                    }
                    int zp = zi & 0xFF;
                    z -= zi;
                    double zFade = z * z * z * (z * (z * 6.0 - 15.0) + 10.0);
                    int a = p[xp] + 0;
                    int aa = p[a] + zp;
                    int b = p[xp + 1] + 0;
                    int ba = p[b] + zp;
                    double n0 = lerp(xFade, grad2(p[aa], x, z), grad(p[ba], x - 1.0, 0.0, z));
                    double n1 = lerp(xFade, grad(p[aa + 1], x, 0.0, z - 1.0), grad(p[ba + 1], x - 1.0, 0.0, z - 1.0));
                    double n = lerp(zFade, n0, n1);
                    out[index++] += n * inv;
                }
            }
            return;
        }

        int index = 0;
        double inv = 1.0 / amplitude;
        int lastY = -1;
        double n00 = 0.0;
        double n01 = 0.0;
        double n10 = 0.0;
        double n11 = 0.0;
        for (int ix = 0; ix < xSize; ix++) {
            double x = x0 + ix * xScale + this.xCoord;
            int xi = (int) x;
            if (x < xi) {
                xi--;
            }
            int xp = xi & 0xFF;
            x -= xi;
            double xFade = x * x * x * (x * (x * 6.0 - 15.0) + 10.0);
            for (int iz = 0; iz < zSize; iz++) {
                double z = z0 + iz * zScale + this.zCoord;
                int zi = (int) z;
                if (z < zi) {
                    zi--;
                }
                int zp = zi & 0xFF;
                z -= zi;
                double zFade = z * z * z * (z * (z * 6.0 - 15.0) + 10.0);
                for (int iy = 0; iy < ySize; iy++) {
                    double y = y0 + iy * yScale + this.yCoord;
                    int yi = (int) y;
                    if (y < yi) {
                        yi--;
                    }
                    int yp = yi & 0xFF;
                    y -= yi;
                    double yFade = y * y * y * (y * (y * 6.0 - 15.0) + 10.0);
                    // The original only refreshes the lattice corners when the integer y cell changes.
                    if (iy == 0 || yp != lastY) {
                        lastY = yp;
                        int a = p[xp] + yp;
                        int aa = p[a] + zp;
                        int ab = p[a + 1] + zp;
                        int b = p[xp + 1] + yp;
                        int ba = p[b] + zp;
                        int bb = p[b + 1] + zp;
                        n00 = lerp(xFade, grad(p[aa], x, y, z), grad(p[ba], x - 1.0, y, z));
                        n01 = lerp(xFade, grad(p[ab], x, y - 1.0, z), grad(p[bb], x - 1.0, y - 1.0, z));
                        n10 = lerp(xFade, grad(p[aa + 1], x, y, z - 1.0), grad(p[ba + 1], x - 1.0, y, z - 1.0));
                        n11 = lerp(xFade, grad(p[ab + 1], x, y - 1.0, z - 1.0), grad(p[bb + 1], x - 1.0, y - 1.0, z - 1.0));
                    }
                    double a = lerp(yFade, n00, n01);
                    double b = lerp(yFade, n10, n11);
                    double n = lerp(zFade, a, b);
                    out[index++] += n * inv;
                }
            }
        }
    }
}
