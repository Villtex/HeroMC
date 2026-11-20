package atomicstryker.infernalmobs.common;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * InfernalMobs.cfg. The defaults are the values of the pack's config (z-hexxit-configs), not the mod's own defaults.
 * The entity name sections ("permittedentities", "entitiesalwaysinfernal") are lists of entity type ids here; the
 * item list uses item ids instead of the old numeric ids (syntax {@code id-meta-stackSize-stackSizeRandomizer}).
 */
public final class IMConfig {
    /** Server settings, synced to the clients. */
    public static final ModConfigSpec SPEC;
    /** disableGUIoverlay only matters on the client and is a client setting here. */
    public static final ModConfigSpec CLIENT_SPEC;
    private static final ModConfigSpec.IntValue ELITE_RARITY;
    private static final ModConfigSpec.IntValue ULTRA_RARITY;
    private static final ModConfigSpec.IntValue INFERNO_RARITY;
    private static final ModConfigSpec.DoubleValue MOB_HEALTH_FACTOR;
    private static final ModConfigSpec.BooleanValue DISABLE_GUI_OVERLAY;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> DROPPED_ITEMS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> FORBIDDEN_ENTITIES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ALWAYS_INFERNAL;
    private static final Map<String, ModConfigSpec.BooleanValue> MODIFIERS = new LinkedHashMap<>();

    /**
     * droppedItemIDs of the pack: 6232,322:1,396,277,278,279,403,5871,5873,276,310,311,312,313,5564,5567,5569,5574,
     * 5575,5577,5580,5585,5827,5829,5830,5880,5882,6112,6108,6114,6058-6081 (Falling Meteors),6408 (Xeno's
     * Reliquary),7707. "322:1" never parsed in the original (a colon instead of a dash) and is left out; the items of
     * mods that are not ported yet are left out until their part adds them.
     */
    public static final List<String> DEFAULT_ITEMS = List.of(
            "offlawn:sunflower_seed",
            "minecraft:golden_carrot",
            "minecraft:diamond_shovel",
            "minecraft:diamond_pickaxe",
            "minecraft:diamond_axe",
            "minecraft:enchanted_book",
            "harkenscythe_core:livingmetal_axe",
            "harkenscythe_core:livingmetal_pickaxe",
            "minecraft:diamond_sword",
            "minecraft:diamond_helmet",
            "minecraft:diamond_chestplate",
            "minecraft:diamond_leggings",
            "minecraft:diamond_boots",
            "asaddons:livingmetal_giant_sword",
            "asaddons:gilded_livingmetal_shield",
            "asaddons:ender_giant_sword",
            "asaddons:nether_quartz_shield",
            "asgardshield_core:diamond_giant_sword",
            "asgardshield_core:iron_giant_sword",
            "asgardshield_core:diamond_shield",
            "asgardshield_core:iron_shield",
            "harkenscythe_core:diamond_glaive",
            "harkenscythe_core:iron_glaive",
            "harkenscythe_core:livingmetal_glaive",
            "harkenscythe_core:diamond_scythe",
            "harkenscythe_core:iron_scythe",
            "morebows:iron_bow",
            "morebows:diamond_bow",
            "morebows:stone_bow",
            "hexxitgear:hexical_diamond",
            "meteors:frezarite_axe",
            "meteors:frezarite_chestplate",
            "meteors:frezarite_boots",
            "meteors:frezarite_helmet",
            "meteors:frezarite_leggings",
            "meteors:frezarite_sword",
            "meteors:kreknorite_chestplate",
            "meteors:kreknorite_boots",
            "meteors:kreknorite_helmet",
            "meteors:kreknorite_leggings",
            "meteors:kreknorite_sword",
            "meteors:meteorite_axe",
            "meteors:meteorite_chestplate",
            "meteors:meteorite_boots",
            "meteors:meteorite_helmet",
            "meteors:meteorite_leggings");

    /** permittedentities=false of the pack: EntityHSSoulLost, Harbinger, SandWorm, entity.DimDoors.Monolith.name. */
    public static final List<String> DEFAULT_FORBIDDEN = List.of(
            "harkenscythe_core:soul_lost",
            "harkenscythe_core:harbinger",
            "projectzulu:sandworm",
            "dimdoors:monolith");

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("general");
        ELITE_RARITY = b.comment("One in THIS many Mobs will become atleast rare").defineInRange("eliteRarity", 30, 1, Integer.MAX_VALUE);
        ULTRA_RARITY = b.comment("One in THIS many already rare Mobs will become atleast ultra")
                .defineInRange("ultraRarity", 10, 1, Integer.MAX_VALUE);
        INFERNO_RARITY = b.comment("One in THIS many already ultra Mobs will become infernal")
                .defineInRange("infernoRarity", 10, 1, Integer.MAX_VALUE);
        MOB_HEALTH_FACTOR = b.comment("Multiplier applied ontop of all of the modified Mobs health")
                .defineInRange("mobHealthFactor", 1.0, 0.0, 10000.0);
        DROPPED_ITEMS = b.comment("List of equally likely to drop Items, syntax: itemId-meta-stackSize-stackSizeRandomizer,",
                        "everything but the item id is optional (meta = damage of damageable items). Unknown items are skipped.")
                .defineListAllowEmpty("droppedItemIDs", DEFAULT_ITEMS, () -> "minecraft:diamond", o -> o instanceof String);
        b.pop();
        b.push("modifiers");
        for (String name : InfernalMobsCore.MODIFIER_NAMES) {
            MODIFIERS.put(name, b.define(name + "_enabled", true));
        }
        b.pop();
        FORBIDDEN_ENTITIES = b.comment("Entity type ids that never become rare (permittedentities=false of the original)")
                .defineListAllowEmpty("forbiddenEntities", DEFAULT_FORBIDDEN, () -> "minecraft:zombie", o -> o instanceof String);
        ALWAYS_INFERNAL = b.comment("Entity type ids that always become rare (entitiesalwaysinfernal=true of the original)")
                .defineListAllowEmpty("entitiesAlwaysInfernal", List.of(), () -> "minecraft:zombie", o -> o instanceof String);
        SPEC = b.build();
        ModConfigSpec.Builder c = new ModConfigSpec.Builder();
        c.push("general");
        DISABLE_GUI_OVERLAY = c.comment("Disables the ingame Health and Name overlay").define("disableGUIoverlay", false);
        c.pop();
        CLIENT_SPEC = c.build();
    }

    private IMConfig() {
    }

    public static int eliteRarity() {
        return SPEC.isLoaded() ? ELITE_RARITY.get() : 30;
    }

    public static int ultraRarity() {
        return SPEC.isLoaded() ? ULTRA_RARITY.get() : 10;
    }

    public static int infernoRarity() {
        return SPEC.isLoaded() ? INFERNO_RARITY.get() : 10;
    }

    public static double mobHealthFactor() {
        return SPEC.isLoaded() ? MOB_HEALTH_FACTOR.get() : 1.0;
    }

    public static boolean disableGuiOverlay() {
        return CLIENT_SPEC.isLoaded() && DISABLE_GUI_OVERLAY.get();
    }

    public static List<? extends String> droppedItems() {
        return SPEC.isLoaded() ? DROPPED_ITEMS.get() : DEFAULT_ITEMS;
    }

    public static List<? extends String> forbiddenEntities() {
        return SPEC.isLoaded() ? FORBIDDEN_ENTITIES.get() : DEFAULT_FORBIDDEN;
    }

    public static List<? extends String> alwaysInfernal() {
        return SPEC.isLoaded() ? ALWAYS_INFERNAL.get() : List.of();
    }

    public static boolean modifierEnabled(String className) {
        ModConfigSpec.BooleanValue v = MODIFIERS.get(className);
        return v == null || !SPEC.isLoaded() || v.get();
    }
}
