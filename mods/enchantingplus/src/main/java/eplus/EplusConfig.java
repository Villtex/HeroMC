package eplus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * eplus.cfg of 1.5.2. The "server" and "both" settings are a SERVER config (NeoForge sends it to every client on
 * login, so the cost preview of the GUI uses the same values as the server); "client" and the client half of "both"
 * are a CLIENT config. Defaults = the Hexxit pack config.
 */
public final class EplusConfig {
    private EplusConfig() {
    }

    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();

    static {
        SERVER_BUILDER.comment("Settings controlled by server.").push("server");
    }

    public static final ModConfigSpec.BooleanValue ALLOW_DISENCHANTING = SERVER_BUILDER.comment("Set to true to allow disenchanting.")
            .define("AllowDisenchanting", false);
    public static final ModConfigSpec.BooleanValue ALLOW_ENCHANT_DAMAGED = SERVER_BUILDER.comment("Determinds if a player can enchant a damaged item")
            .define("AllowEnchantDamaged", true);
    public static final ModConfigSpec.BooleanValue ALLOW_REPAIR = SERVER_BUILDER
            .comment("Set to true to allow repairing of items via enchantment table.").define("AllowRepair", true);
    public static final ModConfigSpec.IntValue COST_FACTOR = SERVER_BUILDER
            .comment("Determinds the cost factor of enchanting / disenchanting / repair").defineInRange("CostFactor", 5, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue REPAIR_FACTOR = SERVER_BUILDER
            .comment("factor which repair cost is divided by (Default = 8). Higher is Cheaper").defineInRange("RepairFactor", 5, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.BooleanValue HAS_LIGHT = SERVER_BUILDER.comment("Set to true to have the enchanting table emmit light.")
            .define("hasLight", true);
    public static final ModConfigSpec.IntValue MINIMUM_BOOK = SERVER_BUILDER
            .comment("Minimum level of enchanting cost before requiring bookcases").defineInRange("minimumBook", 5, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.BooleanValue NEEDS_BOOKSHELVES = SERVER_BUILDER.comment("Set to true to require book shelves to enchant.")
            .define("needsBookShelves", true);

    static {
        SERVER_BUILDER.pop().comment("Settings controlled by both.", "If client differs from server, server can disable client's setting if false.")
                .push("both");
    }

    public static final ModConfigSpec.BooleanValue SERVER_USE_MOD = SERVER_BUILDER
            .comment("Set to true to use custom Enchantment Table in place of Vanilla").define("useMod", true);

    static {
        SERVER_BUILDER.pop().comment("Enchantments can be disabled to be enchantable in the table.").push("enchantments");
    }

    /** The 1.5.2 file had one switch per enchantment (all true in the pack); here the ids of the disabled ones. */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DISABLED_ENCHANTMENTS = SERVER_BUILDER
            .comment("Ids of enchantments the table does not offer, e.g. [\"minecraft:thorns\"].",
                    "Which enchantments the table offers at all: enchantment tag #eplus:table.")
            .defineList("disabled", List.of(), () -> "minecraft:thorns", o -> o instanceof String);

    public static final ModConfigSpec SERVER_SPEC = SERVER_BUILDER.pop().build();

    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

    static {
        CLIENT_BUILDER.comment("Settings controlled by both. The server only opens the custom table if both are true.").push("both");
    }

    public static final ModConfigSpec.BooleanValue CLIENT_USE_MOD = CLIENT_BUILDER
            .comment("Set to true to use custom Enchantment Table in place of Vanilla").define("useMod", true);

    static {
        CLIENT_BUILDER.pop().comment("Settings controlled client.").push("client");
    }

    public static final ModConfigSpec.BooleanValue HAS_PARTICLES = CLIENT_BUILDER
            .comment("Set to true to have the enchanting table emmit particles.").define("hasParticles", true);

    public static final ModConfigSpec CLIENT_SPEC = CLIENT_BUILDER.pop().build();

    /** The server settings, or the pack defaults while no server config is loaded (e.g. JEI in the main menu). */
    public record Settings(boolean allowDisenchanting, boolean allowEnchantDamaged, boolean allowRepair, int costFactor, int repairFactor,
                           boolean hasLight, int minimumBook, boolean needsBookShelves, boolean useMod, Set<String> disabled) {
        public static final Settings DEFAULTS = new Settings(false, true, true, 5, 5, true, 5, true, true, Set.of());
    }

    public static Settings server() {
        if (!SERVER_SPEC.isLoaded()) {
            return Settings.DEFAULTS;
        }
        try {
            return new Settings(ALLOW_DISENCHANTING.get(), ALLOW_ENCHANT_DAMAGED.get(), ALLOW_REPAIR.get(), COST_FACTOR.get(), REPAIR_FACTOR.get(),
                    HAS_LIGHT.get(), MINIMUM_BOOK.get(), NEEDS_BOOKSHELVES.get(), SERVER_USE_MOD.get(), new HashSet<>(DISABLED_ENCHANTMENTS.get()));
        } catch (IllegalStateException e) {
            return Settings.DEFAULTS;
        }
    }

    public static boolean clientUseMod() {
        return !CLIENT_SPEC.isLoaded() || CLIENT_USE_MOD.get();
    }

    public static boolean hasParticles() {
        return !CLIENT_SPEC.isLoaded() || HAS_PARTICLES.get();
    }
}
