package twilightforest.gen;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldGen;
import hexxit.worldgen152.core.populate.OldPlantGens;
import hexxit.worldgen152.core.populate.OldTreeGens;

/**
 * The biomes of the Twilight Forest (TFBiomeBase) with the ids of the pack's TwilightForest.cfg and the values of
 * their constructors: height range and climate. They are only produced by the Twilight Forest's own layout, never
 * by the overworld one, so they have no key in {@link OldBiome} (the game side names them in {@code TFBiomeSource}).
 *
 * <p>Decoration (Teil 24b): every biome has a {@link TFDecorator} with the per-chunk counts of its constructor, its
 * trees and grass (getRandomWorldGenForTrees / getRandomWorldGenForGrass) and what its decorate() adds after the
 * decorator (vines, penguins, fire jets).
 */
public final class TFBiomes {
    public static final OldBiome LAKE = biome(72, "Twilight Lake").height(-1.9F, 0.5F).climate(0.66F, 1.0F);
    public static final OldBiome TWILIGHT_FOREST = biome(80, "Twilight Forest");
    public static final OldBiome TWILIGHT_FOREST_2 = biome(81, "Dense Twilight Forest").climate(0.7F, 0.8F).height(0.15F, 0.4F);
    public static final OldBiome HIGHLANDS = biome(71, "Highlands").height(2.5F, 0.4F).climate(0.5F, 0.3F);
    public static final OldBiome MUSHROOMS = biome(76, "Mushrooms").climate(0.5F, 0.8F);
    public static final OldBiome SWAMP = biome(79, "Twilight Swamp").height(-0.25F, 0.0F).climate(0.8F, 0.9F);
    public static final OldBiome STREAM = biome(78, "Twilight Stream").height(-0.75F, -0.1F).climate(0.5F, 1.0F);
    public static final OldBiome SNOW = biome(77, "Snowy Forest").climate(0.125F, 0.9F).snow();
    public static final OldBiome GLACIER = biome(70, "Glacier").climate(0.0F, 0.1F).snow();
    public static final OldBiome CLEARING = biome(64, "Twilight Clearing").climate(0.8F, 0.4F).height(0.01F, 0.0F);
    public static final OldBiome CLEARING_BORDER = biome(65, "Clearing Border");
    public static final OldBiome LAKE_BORDER = biome(73, "Lake Border");
    public static final OldBiome DEEP_MUSHROOMS = biome(67, "Lots of Mushrooms").climate(0.8F, 1.0F).height(0.15F, 0.4F);
    public static final OldBiome MAJOR_FEATURE = biome(74, "Major Feature");
    public static final OldBiome MINOR_FEATURE = biome(75, "Minor Feature");
    public static final OldBiome DARK_FOREST = biome(66, "Dark Forest").climate(0.7F, 0.8F).height(0.05F, 0.05F);
    public static final OldBiome ENCHANTED_FOREST = biome(68, "Enchanted Forest");
    public static final OldBiome FIRE_SWAMP = biome(69, "Fire Swamp").height(-0.25F, 0.0F).climate(1.0F, 0.4F);

    /** All biomes in the order of TFBiomeBase. */
    public static final OldBiome[] ALL = {LAKE, TWILIGHT_FOREST, TWILIGHT_FOREST_2, HIGHLANDS, MUSHROOMS, SWAMP, STREAM, SNOW, GLACIER,
            CLEARING, CLEARING_BORDER, LAKE_BORDER, DEEP_MUSHROOMS, MAJOR_FEATURE, MINOR_FEATURE, DARK_FOREST, ENCHANTED_FOREST, FIRE_SWAMP};

    private TFBiomes() {
    }

    private static OldBiome biome(int id, String name) {
        OldBiome biome = OldBiomes.register(new OldBiome(id, name, null));
        // TFBiomeBase: TFBiomeDecorator with 10 trees and 2 grass per chunk, no big oak of the biome
        biome.decorator = new TFDecorator();
        biome.decorator.treesPerChunk = 10;
        biome.decorator.grassPerChunk = 2;
        biome.trees = (world, random) -> random.nextInt(5) == 0 ? OldBiome.BIRCH
                : random.nextInt(10) == 0 ? new OldTreeGens.BigTree() : OldBiome.OAK;
        biome.grass = random -> random.nextInt(4) == 0 ? FERN : GRASS;
        return biome;
    }

    private static final OldGen GRASS = new OldPlantGens.TallGrass(1);
    private static final OldGen FERN = new OldPlantGens.TallGrass(2);
    private static final OldGen SHRUB = new OldTreeGens.Shrub(3, 0);
    private static final OldGen PINE = new OldTreeGens.Pine();
    private static final OldGen SPRUCE = new OldTreeGens.Spruce();
    private static final OldGen VINES = new OldPlantGens.Vines();

    private static TFDecorator tf(OldBiome biome) {
        return (TFDecorator) biome.decorator;
    }

    /** WorldGenVines from the sea level, {@code count} times (the swamps and the enchanted forest). */
    private static OldBiome.Decoration vines(int count) {
        return (b, world, random, x, z) -> {
            for (int i = 0; i < count; i++) {
                int j = x + random.nextInt(16) + 8;
                int k = z + random.nextInt(16) + 8;
                VINES.generate(world, random, j, TFTerrain.SEALEVEL, k);
            }
        };
    }

    static {
        // TFBiomeTwilightForestVariant
        TWILIGHT_FOREST_2.decorator.treesPerChunk = 25;
        TWILIGHT_FOREST_2.decorator.grassPerChunk = 15;
        TWILIGHT_FOREST_2.decorator.flowersPerChunk = 8;
        TWILIGHT_FOREST_2.trees = (world, random) -> random.nextInt(5) == 0 ? SHRUB
                : random.nextInt(10) == 0 ? new OldTreeGens.BigTree() : OldBiome.OAK;
        TWILIGHT_FOREST_2.grass = random -> random.nextInt(4) != 0 ? FERN
                : random.nextBoolean() ? new TFPlants.TallPlant(4) : GRASS;
        // TFBiomeHighlands
        tf(HIGHLANDS).canopyPerChunk = -999;
        HIGHLANDS.trees = (world, random) -> random.nextInt(4) == 0 ? new OldTreeGens.BigTree()
                : random.nextInt(10) == 0 ? SPRUCE : OldBiome.BIRCH;
        // TFBiomeMushrooms
        MUSHROOMS.decorator.treesPerChunk = 8;
        MUSHROOMS.decorator.mushroomsPerChunk = 8;
        MUSHROOMS.decorator.bigMushroomsPerChunk = 2;
        tf(MUSHROOMS).chanceCanopyIsMushroom = 0.2F;
        // TFBiomeSwamp
        SWAMP.decorator.deadBushPerChunk = 1;
        SWAMP.decorator.mushroomsPerChunk = 8;
        SWAMP.decorator.reedsPerChunk = 10;
        SWAMP.decorator.clayPerChunk = 1;
        SWAMP.decorator.treesPerChunk = 2;
        SWAMP.decorator.waterlilyPerChunk = 6;
        tf(SWAMP).canopyPerChunk = -999;
        tf(SWAMP).lakesPerChunk = 2;
        tf(SWAMP).mangrovesPerChunk = 3;
        SWAMP.trees = (world, random) -> random.nextInt(3) == 0 ? SHRUB : OldBiome.SWAMP_OAK;
        SWAMP.grass = random -> random.nextInt(4) == 0 ? FERN : random.nextInt(4) == 0 ? new TFPlants.TallPlant(4) : GRASS;
        SWAMP.afterDecoration(vines(50));
        // TFBiomeStream
        STREAM.decorator.waterlilyPerChunk = 2;
        // TFBiomeSnow
        SNOW.decorator.treesPerChunk = 7;
        SNOW.decorator.grassPerChunk = 1;
        tf(SNOW).canopyPerChunk = -999;
        SNOW.trees = (world, random) -> random.nextInt(3) == 0 ? PINE : SPRUCE;
        // TFBiomeGlacier
        GLACIER.decorator.treesPerChunk = 1;
        GLACIER.decorator.grassPerChunk = 0;
        tf(GLACIER).canopyPerChunk = -999;
        GLACIER.trees = (world, random) -> random.nextInt(3) == 0 ? PINE : SPRUCE;
        GLACIER.afterDecoration((b, world, random, x, z) -> {
            if (random.nextInt(4) == 0) {
                int j = x + random.nextInt(16) + 8;
                int k = z + random.nextInt(16) + 8;
                new TFPlants.Penguins().generate(world, random, j, TFTerrain.SEALEVEL, k);
            }
        });
        // TFBiomeClearing
        tf(CLEARING).canopyPerChunk = -999;
        CLEARING.decorator.treesPerChunk = -999;
        CLEARING.decorator.flowersPerChunk = 4;
        CLEARING.decorator.grassPerChunk = 10;
        CLEARING.grass = random -> GRASS;
        // TFBiomeDeepMushrooms
        DEEP_MUSHROOMS.decorator.treesPerChunk = 1;
        DEEP_MUSHROOMS.decorator.mushroomsPerChunk = 12;
        DEEP_MUSHROOMS.decorator.bigMushroomsPerChunk = 8;
        tf(DEEP_MUSHROOMS).myceliumPerChunk = 3;
        tf(DEEP_MUSHROOMS).chanceCanopyIsMushroom = 0.9F;
        // TFBiomeDarkForest with TFDarkForestBiomeDecorator
        DARK_FOREST.decorator = new TFDecorator.DarkForest();
        tf(DARK_FOREST).canopyPerChunk = 5;
        DARK_FOREST.decorator.treesPerChunk = 10;
        DARK_FOREST.decorator.grassPerChunk = -99;
        DARK_FOREST.decorator.flowersPerChunk = -99;
        DARK_FOREST.decorator.mushroomsPerChunk = 1;
        DARK_FOREST.decorator.deadBushPerChunk = 10;
        DARK_FOREST.trees = (world, random) -> random.nextInt(5) == 0 ? SHRUB : random.nextInt(8) == 0 ? OldBiome.BIRCH : OldBiome.OAK;
        // TFBiomeEnchantedForest
        ENCHANTED_FOREST.decorator.grassPerChunk = 15;
        ENCHANTED_FOREST.decorator.flowersPerChunk = 8;
        ENCHANTED_FOREST.trees = (world, random) -> random.nextInt(15) == 0 ? new TFTrees.SmallOak(3)
                : random.nextInt(50) == 0 ? TFTrees.largeRainboak()
                : random.nextInt(5) == 0 ? OldBiome.BIRCH
                : random.nextInt(10) == 0 ? new OldTreeGens.BigTree() : OldBiome.OAK;
        ENCHANTED_FOREST.grass = random -> random.nextInt(3) > 0 ? FERN : random.nextInt(3) == 0 ? new TFPlants.TallPlant(8) : GRASS;
        ENCHANTED_FOREST.afterDecoration(vines(20));
        // TFBiomeFireSwamp
        FIRE_SWAMP.decorator.deadBushPerChunk = 2;
        FIRE_SWAMP.decorator.mushroomsPerChunk = 8;
        FIRE_SWAMP.decorator.reedsPerChunk = 4;
        FIRE_SWAMP.decorator.clayPerChunk = 1;
        FIRE_SWAMP.decorator.treesPerChunk = 3;
        FIRE_SWAMP.decorator.waterlilyPerChunk = 6;
        tf(FIRE_SWAMP).canopyPerChunk = -999;
        tf(FIRE_SWAMP).lavaPoolChance = 0.125F;
        tf(FIRE_SWAMP).mangrovesPerChunk = 3;
        FIRE_SWAMP.trees = (world, random) -> random.nextInt(3) == 0 ? SHRUB : OldBiome.SWAMP_OAK;
        OldBiome.Decoration fireVines = vines(50);
        FIRE_SWAMP.afterDecoration((b, world, random, x, z) -> {
            if (TFDecorator.nearFeature(world).chunkDecorationsEnabled) {
                fireVines.decorate(b, world, random, x, z);
                if (random.nextInt(4) == 0) {
                    int j = x + random.nextInt(16) + 8;
                    int k = z + random.nextInt(16) + 8;
                    new TFPlants.FireJet(TFGen.FIRE_JET, 0).generate(world, random, j, TFTerrain.SEALEVEL, k);
                }
                int j = x + random.nextInt(16) + 8;
                int k = z + random.nextInt(16) + 8;
                new TFPlants.FireJet(TFGen.FIRE_JET, 8).generate(world, random, j, TFTerrain.SEALEVEL, k);
            }
        });
    }

    private static final String[] PATHS = new String[256];

    static {
        String[][] paths = {{"LAKE", "twilight_lake"}, {"TWILIGHT_FOREST", "twilight_forest"}, {"TWILIGHT_FOREST_2", "dense_twilight_forest"},
                {"HIGHLANDS", "highlands"}, {"MUSHROOMS", "mushrooms"}, {"SWAMP", "twilight_swamp"}, {"STREAM", "twilight_stream"},
                {"SNOW", "snowy_forest"}, {"GLACIER", "glacier"}, {"CLEARING", "twilight_clearing"}, {"CLEARING_BORDER", "clearing_border"},
                {"LAKE_BORDER", "lake_border"}, {"DEEP_MUSHROOMS", "deep_mushrooms"}, {"MAJOR_FEATURE", "major_feature"},
                {"MINOR_FEATURE", "minor_feature"}, {"DARK_FOREST", "dark_forest"}, {"ENCHANTED_FOREST", "enchanted_forest"},
                {"FIRE_SWAMP", "fire_swamp"}};
        for (int i = 0; i < ALL.length; i++) {
            PATHS[ALL[i].id] = paths[i][1];
        }
    }

    /** Name of the biome in today's registry ({@code twilightforest:<path>}), or {@code null} for other ids. */
    public static String key(int id) {
        String path = PATHS[id & 255];
        return path == null ? null : "twilightforest:" + path;
    }

    /** Loads the class, which registers the biomes. */
    public static void register() {
    }

    /** TFBiomeBase.isFeature. */
    public static boolean isFeature(int id) {
        return id == MAJOR_FEATURE.id || id == MINOR_FEATURE.id;
    }
}
