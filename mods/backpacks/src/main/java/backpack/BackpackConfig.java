package backpack;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * ConfigurationBackpack, section "general". The defaults are the values of the pack's Backpack.cfg (they equal the
 * mod's own defaults). The item ids of the "item" section are the old ids in {@link Backpack#registerOldIds()}.
 * Out-of-range values fall back like the original (sizes to 6, the ender recipe to 0, the amount to 0).
 */
public final class BackpackConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue AIRSHIP_MOD_COMPATIBILITY;
    private static final ModConfigSpec.IntValue BACKPACK_SIZE_L;
    private static final ModConfigSpec.IntValue BACKPACK_SIZE_M;
    private static final ModConfigSpec.IntValue ENDER_RECIPE;
    private static final ModConfigSpec.IntValue MAX_BACKPACK_AMOUNT;
    private static final ModConfigSpec.BooleanValue OPEN_ONLY_WEARED_BACKPACKS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("general");
        AIRSHIP_MOD_COMPATIBILITY = b.comment("If true normal backpack requires a chest in the middle")
                .define("airshipModCompatibility", false);
        BACKPACK_SIZE_L = b.comment("Number of rows (9 slots) a backpack has", "valid: integers 1-6")
                .defineInRange("backpackSizeL", 6, Integer.MIN_VALUE, Integer.MAX_VALUE);
        BACKPACK_SIZE_M = b.defineInRange("backpackSizeM", 3, Integer.MIN_VALUE, Integer.MAX_VALUE);
        ENDER_RECIPE = b.comment("Recipe to craft ender backpack", "0 ender chest", "1 eye of the ender")
                .defineInRange("enderRecipe", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        MAX_BACKPACK_AMOUNT = b.comment("Number of backpacks a player can have in his inventory", "valid: integers 0-36", "0 = unlimited")
                .defineInRange("maxBackpackAmount", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        OPEN_ONLY_WEARED_BACKPACKS = b.comment("If true you can only open a backpack that you wear in your chest slot")
                .define("openOnlyWearedBackpacks", false);
        b.pop();
        SPEC = b.build();
    }

    private BackpackConfig() {
    }

    private static int get(ModConfigSpec.IntValue value, int def) {
        return SPEC.isLoaded() ? value.get() : def;
    }

    private static boolean get(ModConfigSpec.BooleanValue value, boolean def) {
        return SPEC.isLoaded() ? value.get() : def;
    }

    public static boolean airshipModCompatibility() {
        return get(AIRSHIP_MOD_COMPATIBILITY, false);
    }

    /** Rows of a big backpack (1..6, else 6). */
    public static int sizeL() {
        int v = get(BACKPACK_SIZE_L, 6);
        return v < 1 || v > 6 ? 6 : v;
    }

    /** Rows of a normal backpack (1..6, else 6 as in the original). */
    public static int sizeM() {
        int v = get(BACKPACK_SIZE_M, 3);
        return v < 1 || v > 6 ? 6 : v;
    }

    public static int enderRecipe() {
        int v = get(ENDER_RECIPE, 0);
        return v < 0 || v > 1 ? 0 : v;
    }

    public static int maxBackpackAmount() {
        int v = get(MAX_BACKPACK_AMOUNT, 0);
        return v < 0 || v > 36 ? 0 : v;
    }

    public static boolean openOnlyWearedBackpacks() {
        return get(OPEN_ONLY_WEARED_BACKPACKS, false);
    }
}
