package damageindicators;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Damage Indicators 2.7.0.1: a portrait (skin "Default") with name, health bar, effects and a mob preview for the mob
 * under the crosshair, and numbers that pop off mobs when their health changes. The original's server part only told
 * the client the health and effects of nearby mobs; 26.3 already synchronizes both, so the whole mod is client-only.
 * Standard values are those of the pack's DamageIndicatorsMod.cfg.
 */
@Mod(DamageIndicatorsMod.MOD_ID)
public final class DamageIndicatorsMod {
    public static final String MOD_ID = "damageindicatorsmod";

    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    // popoffs.behavior
    public static final ModConfigSpec.BooleanValue POPOFFS = B.comment("Damage pop-offs on or off (key '.' in game)").define("popoffs_enabled", true);
    public static final ModConfigSpec.DoubleValue BOUNCE = B.defineInRange("bounce_strength", 1.5, 0.0, 100.0);
    public static final ModConfigSpec.DoubleValue GRAVITY = B.defineInRange("gravity", 1.6, 0.0, 100.0);
    public static final ModConfigSpec.IntValue LIFESPAN = B.comment("Ticks the numbers last").defineInRange("lifespan", 12, 1, 1000);
    public static final ModConfigSpec.IntValue POP_RANGE = B.comment("Blocks around the player that are watched").defineInRange("popoff_range", 30, 1, 256);
    public static final ModConfigSpec.DoubleValue SIZE = B.defineInRange("size", 3.0, 0.1, 100.0);
    // popoffs.appearance
    public static final ModConfigSpec.ConfigValue<String> COLOR = B.comment("RGB hex of damage numbers").define("color", "FFAA00");
    public static final ModConfigSpec.ConfigValue<String> HEAL_COLOR = B.comment("RGB hex of healing numbers").define("heal_color", "00FF00");
    public static final ModConfigSpec.DoubleValue TRANSPARENCY = B.defineInRange("transparency", 1.0, 0.0, 1.0);
    public static final ModConfigSpec.BooleanValue DEPTH_TEST = B.comment("When true, blocks between you and the mob hide the numbers (original Enable_Depth_Test; false = numbers show when the mob is in sight)")
            .define("enable_depth_test", false);
    public static final ModConfigSpec.BooleanValue ALWAYS_RENDER = B.comment("Numbers always render, also for mobs you cannot see").define("always_render", false);
    // portrait
    public static final ModConfigSpec.BooleanValue PORTRAIT = B.define("portrait_enabled", true);
    public static final ModConfigSpec.IntValue PORTRAIT_LIFETIME = B.comment("Frames the portrait stays after you looked away (-1 = forever)").defineInRange("portrait_lifetime", 160, -1, 1000000);
    public static final ModConfigSpec.BooleanValue POTIONS = B.define("show_potion_effects", true);
    public static final ModConfigSpec.DoubleValue GUI_SCALE = B.defineInRange("gui_scale", 0.76, 0.1, 5.0);
    public static final ModConfigSpec.BooleanValue LOCK_MOB = B.comment("The mob in the portrait does not follow the camera").define("lock_mob_position", true);
    public static final ModConfigSpec.IntValue X_POS = B.defineInRange("portrait_x", 15, 0, 10000);
    public static final ModConfigSpec.IntValue Y_POS = B.defineInRange("portrait_y", 15, 0, 10000);
    public static final ModConfigSpec.IntValue PORTRAIT_RANGE = B.defineInRange("portrait_range", 30, 1, 256);
    public static final ModConfigSpec.BooleanValue DEBUG_HIDES = B.comment("F3 hides the portrait").define("debug_hides_window", true);
    private static final ModConfigSpec SPEC = B.build();

    public DamageIndicatorsMod(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SPEC);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            damageindicators.client.DIClient.register(modBus);
        }
    }

    /** "FFAA00" or "80FFAA00" to RGB; the bad value gives the fallback. */
    public static int rgb(String hex, int fallback) {
        try {
            return (int) (Long.parseLong(hex.trim(), 16) & 0xFFFFFF);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
