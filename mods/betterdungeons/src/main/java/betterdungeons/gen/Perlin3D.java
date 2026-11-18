package betterdungeons.gen;

import java.util.Random;

/**
 * Perlin3D of Better Dungeons: value noise on a grid of {@code frequency} blocks, cosine-interpolated. The value of a
 * grid point reseeds the random it was given (the builder's own) from sines and cosines of its position, so that
 * random is left reseeded afterwards. Math.sin/cos/tan as in the original.
 */
final class Perlin3D {
    private final long seed;
    private final Random rand;
    private final int frequency;

    Perlin3D(long seed, int octave, Random random) {
        this.seed = seed;
        this.frequency = octave;
        this.rand = random;
    }

    double getNoiseAt(int x, int y, int z) {
        int ymin = (int) Math.floor((double) y / this.frequency);
        int ymax = ymin + 1;
        return this.cosineInterpolate((float) this.getNoiseLevelAtPosition(x, ymin, z), (float) this.getNoiseLevelAtPosition(x, ymax, z),
                (y - (float) ymin * this.frequency) / this.frequency);
    }

    private double getNoiseLevelAtPosition(int x, int y, int z) {
        int xmin = (int) Math.floor((double) x / this.frequency);
        int xmax = xmin + 1;
        int zmin = (int) Math.floor((double) z / this.frequency);
        int zmax = zmin + 1;
        return this.cosineInterpolate(
                this.cosineInterpolate((float) this.getRandomAtPosition(xmin, y, zmin), (float) this.getRandomAtPosition(xmax, y, zmin),
                        (float) (x - xmin * this.frequency) / this.frequency),
                this.cosineInterpolate((float) this.getRandomAtPosition(xmin, y, zmax), (float) this.getRandomAtPosition(xmax, y, zmax),
                        (float) (x - xmin * this.frequency) / this.frequency),
                (z - (float) zmin * this.frequency) / this.frequency);
    }

    private float cosineInterpolate(float a, float b, float x) {
        float f = (float) ((1.0 - Math.cos(x * Math.PI)) * 0.5);
        return a * (1.0F - f) + b * f;
    }

    private double getRandomAtPosition(int x, int y, int z) {
        this.rand.setSeed((long) (10000.0 * (Math.sin(x) + Math.cos(z) + Math.cos(y) + Math.tan(this.seed))));
        return this.rand.nextDouble();
    }
}
