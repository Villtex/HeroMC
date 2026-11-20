package twilightforest.gen;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;

/**
 * The biomes of the Twilight Forest (TFBiomeBase) with the ids of the pack's TwilightForest.cfg and the values of
 * their constructors: height range and climate. They are only produced by the Twilight Forest's own layout, never
 * by the overworld one, so they have no key in {@link OldBiome} (the game side names them in {@code TFBiomeSource}).
 *
 * <p>Decoration of the chunks follows in Teil 24b; until then they decorate nothing.
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
        // TFBiomeDecorator comes with Teil 24b
        biome.decoration = (b, world, random, x, z) -> {
        };
        return biome;
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
