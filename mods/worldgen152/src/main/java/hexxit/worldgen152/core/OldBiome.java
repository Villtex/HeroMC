package hexxit.worldgen152.core;

import hexxit.worldgen152.core.populate.OldDecorator;
import hexxit.worldgen152.core.populate.OldGen;
import hexxit.worldgen152.core.populate.OldPlantGens;
import hexxit.worldgen152.core.populate.OldTreeGens;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * The world generation side of a 1.5.2 biome (BiomeGenBase): numeric id, height range, climate, surface
 * blocks and how its chunks are decorated. {@link #key} names the biome in today's registry that represents it; it is {@code null}
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

    /** BiomeGenBase.getRandomWorldGenForTrees. */
    public interface TreeChooser {
        OldGen pick(OldWorld world, Random random);
    }

    /** BiomeGenBase.getRandomWorldGenForGrass. */
    public interface GrassChooser {
        OldGen pick(Random random);
    }

    /** BiomeGenBase.decorate; {@code x}/{@code z} are the block coordinates of the chunk corner. */
    public interface Decoration {
        void decorate(OldBiome biome, OldWorld world, Random random, int x, int z);
    }

    public static final OldGen OAK = new OldTreeGens.Trees();
    public static final OldGen BIRCH = new OldTreeGens.Birch();
    public static final OldGen SWAMP_OAK = new OldTreeGens.Swamp();

    /** BiomeGenBase.theBiomeDecorator. Replace it to decorate differently (Forge: getModdedBiomeDecorator). */
    public OldDecorator decorator = new OldDecorator();
    public TreeChooser trees = (world, random) -> random.nextInt(10) == 0 ? world.bigTree() : OAK;
    public GrassChooser grass = random -> new OldPlantGens.TallGrass(1);
    /** What runs for a chunk of this biome; by default just the decorator. */
    public Decoration decoration = (biome, world, random, x, z) -> biome.decorator.decorate(world, random, biome, x, z);

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

    /** Adds a step after the current decoration, like a subclass calling super.decorate first. */
    public OldBiome afterDecoration(Decoration extra) {
        Decoration before = this.decoration;
        this.decoration = (biome, world, random, x, z) -> {
            before.decorate(biome, world, random, x, z);
            extra.decorate(biome, world, random, x, z);
        };
        return this;
    }

    @Override
    public String toString() {
        return this.name + "#" + this.id;
    }
}
