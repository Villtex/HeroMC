package chococraft;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * chocobo_config.txt of the pack (z-hexxit-configs) as a common config (the entities read it on both sides). Left
 * out: hungerEnabled (false, hunger is not ported), hunger delays; the mutation rates and the world generation rate
 * are the pack values in the stem's loot table and in {@link chococraft.gen.ChocoWorldGen}.
 */
public final class ChocoConfig {
    /** Constants.DEFAULT_GENDER_MALE_CHANCE, not configurable in the original either. */
    public static final int GENDER_MALE_CHANCE = 50;

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue SHOW_CHOCOBO_NAMES;
    public static final ModConfigSpec.BooleanValue CHOCOBO_WING_FLUTTER;
    public static final ModConfigSpec.BooleanValue RIDER_BUFFS_ENABLED;
    public static final ModConfigSpec.BooleanValue SADDLED_CAN_WANDER;
    public static final ModConfigSpec.DoubleValue RENDER_NAME_HEIGHT;
    public static final ModConfigSpec.IntValue LIVING_SOUND_PROBABILITY;
    public static final ModConfigSpec.IntValue BREEDING_DELAY_FEMALE;
    public static final ModConfigSpec.IntValue BREEDING_DELAY_MALE;
    public static final ModConfigSpec.IntValue GROWUP_DELAY_STATIC;
    public static final ModConfigSpec.IntValue GROWUP_DELAY_RANDOM;
    public static final ModConfigSpec.IntValue PEN_HEAL_PROBABILITY;
    public static final ModConfigSpec.IntValue PEN_HEAL_CAULDRON_RANGE;
    public static final ModConfigSpec.BooleanValue WILD_CAN_DESPAWN;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> SPAWN_BIOMES;
    public static final ModConfigSpec.IntValue SPAWN_TIME_DELAY;
    public static final ModConfigSpec.IntValue SPAWN_PROBABILITY;
    public static final ModConfigSpec.IntValue SPAWN_GROUP_MIN;
    public static final ModConfigSpec.IntValue SPAWN_GROUP_MAX;
    public static final ModConfigSpec.IntValue SPAWN_TOTAL_MAX;
    public static final ModConfigSpec.IntValue SPAWN_LIMIT_CHUNK_RADIUS;
    public static final ModConfigSpec.IntValue DISTANCE_NEXT_WILD;
    public static final ModConfigSpec.IntValue FEATHER_DELAY_RANDOM;
    public static final ModConfigSpec.IntValue FEATHER_DELAY_STATIC;
    public static final ModConfigSpec.IntValue FEATHER_DROP_CHANCE;

    /**
     * spawnBiomes of the pack. "Green Swamp", "Mountain Desert" and "Temporate Rainforest" matched no biome name of
     * 1.5.2 (Green Swamplands, Mountainous Desert, Temperate Rainforest) and spawned nothing; they are left out.
     */
    private static final List<String> PACK_BIOMES = List.of("worldgen152:extreme_hills", "worldgen152:extreme_hills_edge",
            "worldgen152:forest", "worldgen152:forest_hills", "worldgen152:jungle", "worldgen152:jungle_hills", "worldgen152:plains",
            "worldgen152:swampland", "worldgen152:taiga", "worldgen152:taiga_hills", "worldgen152:ice_mountains",
            "worldgen152:ice_plains", "worldgen152:hell", "extrabiomesxl:alpine", "extrabiomesxl:autumn_woods",
            "extrabiomesxl:birch_forest", "extrabiomesxl:extreme_jungle", "extrabiomesxl:forested_hills",
            "extrabiomesxl:forested_island", "extrabiomesxl:green_hills", "extrabiomesxl:meadow", "extrabiomesxl:mini_jungle",
            "extrabiomesxl:mountain_ridge", "extrabiomesxl:pine_forest", "extrabiomesxl:rainforest", "extrabiomesxl:savanna",
            "extrabiomesxl:shrubland", "extrabiomesxl:snowy_forest", "extrabiomesxl:snowy_rainforest", "extrabiomesxl:woodlands");

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        SHOW_CHOCOBO_NAMES = b.comment("Show the names of tamed chocobos whose name is not hidden").define("showChocoboNames", true);
        CHOCOBO_WING_FLUTTER = b.comment("Flapping wings in the air instead of spread ones").define("chocoboWingFlutter", false);
        RIDER_BUFFS_ENABLED = b.comment("Blue: water breathing, black: night vision, purple: fire resistance for the rider")
                .define("riderBuffsEnabled", true);
        SADDLED_CAN_WANDER = b.comment("Saddled chocobos and pack chocobos can be set to wander").define("saddledCanWander", false);
        RENDER_NAME_HEIGHT = b.comment("Extra height of the name above the chocobo, in blocks").defineInRange("renderNameHeight", 0.0, 0.0, 10.0);
        LIVING_SOUND_PROBABILITY = b.comment("How many of 100 'Kweh's are heard").defineInRange("livingSoundProbability", 100, 0, 100);
        BREEDING_DELAY_FEMALE = b.comment("Cool-down between breeding, in ticks").defineInRange("breedingDelayFemale", 9000, 0, 1000000);
        BREEDING_DELAY_MALE = b.defineInRange("breedingDelayMale", 3000, 0, 1000000);
        GROWUP_DELAY_STATIC = b.comment("Chicobos grow up after growUpDelayStatic plus 0 - growUpDelayRandom ticks")
                .defineInRange("growUpDelayStatic", 27000, 0, 1000000);
        GROWUP_DELAY_RANDOM = b.defineInRange("growUpDelayRandom", 2000, 1, 1000000);
        PEN_HEAL_PROBABILITY = b.comment("Percent chance every 2 seconds that a hurt tamed chocobo on straw near a filled cauldron heals")
                .defineInRange("penHealProbability", 20, 0, 100);
        PEN_HEAL_CAULDRON_RANGE = b.defineInRange("penHealCauldronRange", 6, 0, 32);
        WILD_CAN_DESPAWN = b.comment("Wild chocobos despawn when no player is around").define("wildCanDespawn", false);
        SPAWN_BIOMES = b.comment("Biomes wild yellow chocobos spawn in (in the Nether and the End purple ones spawn anywhere)")
                .defineListAllowEmpty("spawnBiomes", PACK_BIOMES, () -> "", o -> o instanceof String);
        SPAWN_TIME_DELAY = b.comment("Ticks between two spawn attempts around every player").defineInRange("spawnTimeDelay", 200, 1, 1000000);
        SPAWN_PROBABILITY = b.comment("Percent chance of a spawn attempt").defineInRange("spawnProbability", 2, 0, 100);
        SPAWN_GROUP_MIN = b.defineInRange("spawnGroupMin", 2, 1, 64);
        SPAWN_GROUP_MAX = b.defineInRange("spawnGroupMax", 4, 1, 64);
        SPAWN_TOTAL_MAX = b.comment("No spawning with more wild chocobos than this within spawnLimitChunkRadius blocks")
                .defineInRange("spawnTotalMax", 3, 0, 1000);
        SPAWN_LIMIT_CHUNK_RADIUS = b.defineInRange("spawnLimitChunkRadius", 100, 0, 1000);
        DISTANCE_NEXT_WILD = b.comment("Minimum distance of a new flock to the next wild chocobo").defineInRange("distanceNextWild", 300, 0, 10000);
        FEATHER_DELAY_RANDOM = b.comment("Chocobos have a featherDropChance in 100 to drop a feather every featherDelayStatic plus 0 - featherDelayRandom ticks")
                .defineInRange("featherDelayRandom", 600, 1, 1000000);
        FEATHER_DELAY_STATIC = b.defineInRange("featherDelayStatic", 600, 0, 1000000);
        FEATHER_DROP_CHANCE = b.defineInRange("featherDropChance", 15, 0, 100);
        SPEC = b.build();
    }

    private ChocoConfig() {
    }
}
