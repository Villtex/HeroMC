package buffbarmod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Buff Bar Mod 0.7.1: the active potion effects as a row of icons above the hotbar, each with a bar that runs out
 * with the effect (or the time left as text). Only the client draws anything; the server just loads the mod.
 */
@Mod(BuffBarMod.MOD_ID)
public final class BuffBarMod {
    public static final String MOD_ID = "mod_buffbarmod";

    // "buff bar controls" of mod_BuffBarMod.cfg, defaults = the pack config
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder().push("buff_bar_controls");
    public static final ModConfigSpec.IntValue X_OFFSET = BUILDER.defineInRange("display_x_offset", 10, -10000, 10000);
    public static final ModConfigSpec.IntValue Y_OFFSET = BUILDER.defineInRange("display_y_offset", -28, -10000, 10000);
    public static final ModConfigSpec.IntValue DISPLAY_TYPE = BUILDER
            .comment("0 = countdown bar, 1 = time left as text, 2 = both").defineInRange("display_type", 0, 0, 2);
    public static final ModConfigSpec.IntValue FONT_COLOR = BUILDER.defineInRange("display_font_color", 1030655, 0, 0xFFFFFF);
    public static final ModConfigSpec.IntValue CREATIVE_Y_OFFSET = BUILDER.defineInRange("display_y_offset_in_creative", 13, -10000, 10000);
    public static final ModConfigSpec.IntValue ANALOG_MAX_DURATION = BUILDER
            .comment("Seconds of effect time that fill the bar").defineInRange("analog_max_duration_length", 60, 1, 100000);
    private static final ModConfigSpec SPEC = BUILDER.pop().build();

    public BuffBarMod(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SPEC);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            buffbarmod.client.BuffBarHud.register(modBus);
        }
    }
}
