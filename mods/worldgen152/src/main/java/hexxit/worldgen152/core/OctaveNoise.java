package hexxit.worldgen152.core;

import java.util.Random;

/** Octave stack of {@link PerlinNoise}, equivalent to 1.5.2's NoiseGeneratorOctaves. Immutable once built. */
public final class OctaveNoise {
    private final PerlinNoise[] octaves;

    public OctaveNoise(Random random, int count) {
        this.octaves = new PerlinNoise[count];
        for (int i = 0; i < count; i++) {
            this.octaves[i] = new PerlinNoise(random);
        }
    }

    /** 3D sample block, laid out as [x][z][y]. */
    public double[] generate(int x, int y, int z, int xSize, int ySize, int zSize, double xScale, double yScale, double zScale) {
        double[] out = new double[xSize * ySize * zSize];
        double frequency = 1.0;
        for (PerlinNoise octave : this.octaves) {
            double dx = x * frequency * xScale;
            double dy = y * frequency * yScale;
            double dz = z * frequency * zScale;
            long lx = OldMath.floorLong(dx);
            long lz = OldMath.floorLong(dz);
            dx -= lx;
            dz -= lz;
            lx %= 16777216L;
            lz %= 16777216L;
            dx += lx;
            dz += lz;
            octave.populate(out, dx, dy, dz, xSize, ySize, zSize, xScale * frequency, yScale * frequency, zScale * frequency, frequency);
            frequency /= 2.0;
        }
        return out;
    }

    /** 2D sample block, laid out as [x][z]. */
    public double[] generate(int x, int z, int xSize, int zSize, double xScale, double zScale) {
        return this.generate(x, 10, z, xSize, 1, zSize, xScale, 1.0, zScale);
    }
}
