package hexxit.worldgen152.core;

/**
 * The terrain-relevant properties of a 1.5.2 biome (BiomeGenBase): numeric id, height range, climate and
 * surface blocks. {@link #key} names the biome in today's registry that represents it; it is {@code null}
 * for biomes the overworld layout never produces.
 */
public final class OldBiome {
    public final int id;
    public final String name;
    public final String key;
    public float minHeight = 0.1F;
    public float maxHeight = 0.3F;
    public float temperature = 0.5F;
    public float rainfall = 0.5F;
    public byte topBlock = OldBlocks.GRASS;
    public byte fillerBlock = OldBlocks.DIRT;
    public boolean enableSnow;
    public boolean enableRain = true;

    public OldBiome(int id, String name, String key) {
        this.id = id;
        this.name = name;
        this.key = key;
    }

    public OldBiome height(float min, float max) {
        this.minHeight = min;
        this.maxHeight = max;
        return this;
    }

    public OldBiome climate(float temperature, float rainfall) {
        this.temperature = temperature;
        this.rainfall = rainfall;
        return this;
    }

    public OldBiome surface(byte top, byte filler) {
        this.topBlock = top;
        this.fillerBlock = filler;
        return this;
    }

    public OldBiome snow() {
        this.enableSnow = true;
        return this;
    }

    public OldBiome noRain() {
        this.enableRain = false;
        return this;
    }

    @Override
    public String toString() {
        return this.name + "#" + this.id;
    }
}
