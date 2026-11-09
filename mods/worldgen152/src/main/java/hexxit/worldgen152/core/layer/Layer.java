package hexxit.worldgen152.core.layer;

/**
 * One stage of the 1.5.2 biome layout (GenLayer). Layers form a chain; {@link #sample} returns the ids of a
 * rectangle, row by row. Instances carry scratch state and must not be shared between threads.
 */
public abstract class Layer {
    private long worldSeed;
    private long chunkSeed;
    private long baseSeed;
    protected Layer parent;

    protected Layer(long salt) {
        this.baseSeed = salt;
        this.baseSeed = this.baseSeed * (this.baseSeed * 6364136223846793005L + 1442695040888963407L);
        this.baseSeed += salt;
        this.baseSeed = this.baseSeed * (this.baseSeed * 6364136223846793005L + 1442695040888963407L);
        this.baseSeed += salt;
        this.baseSeed = this.baseSeed * (this.baseSeed * 6364136223846793005L + 1442695040888963407L);
        this.baseSeed += salt;
    }

    public void initWorldSeed(long seed) {
        this.worldSeed = seed;
        if (this.parent != null) {
            this.parent.initWorldSeed(seed);
        }
        this.worldSeed = this.worldSeed * (this.worldSeed * 6364136223846793005L + 1442695040888963407L);
        this.worldSeed = this.worldSeed + this.baseSeed;
        this.worldSeed = this.worldSeed * (this.worldSeed * 6364136223846793005L + 1442695040888963407L);
        this.worldSeed = this.worldSeed + this.baseSeed;
        this.worldSeed = this.worldSeed * (this.worldSeed * 6364136223846793005L + 1442695040888963407L);
        this.worldSeed = this.worldSeed + this.baseSeed;
    }

    protected final void initChunkSeed(long x, long z) {
        this.chunkSeed = this.worldSeed;
        this.chunkSeed = this.chunkSeed * (this.chunkSeed * 6364136223846793005L + 1442695040888963407L);
        this.chunkSeed += x;
        this.chunkSeed = this.chunkSeed * (this.chunkSeed * 6364136223846793005L + 1442695040888963407L);
        this.chunkSeed += z;
        this.chunkSeed = this.chunkSeed * (this.chunkSeed * 6364136223846793005L + 1442695040888963407L);
        this.chunkSeed += x;
        this.chunkSeed = this.chunkSeed * (this.chunkSeed * 6364136223846793005L + 1442695040888963407L);
        this.chunkSeed += z;
    }

    protected final int nextInt(int bound) {
        int value = (int) ((this.chunkSeed >> 24) % bound);
        if (value < 0) {
            value += bound;
        }
        this.chunkSeed = this.chunkSeed * (this.chunkSeed * 6364136223846793005L + 1442695040888963407L);
        this.chunkSeed = this.chunkSeed + this.worldSeed;
        return value;
    }

    public abstract int[] sample(int x, int z, int width, int height);
}
