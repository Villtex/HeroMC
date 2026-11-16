package asgardshield;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Client settings: the HUD offsets of AsgardShield_Core.cfg ("general") and the default shield colours of
 * ASAddon_ColorAPI.cfg ("Shield Color Override"), with the values of the pack. "Override Equipment Duriablity" (1 in
 * the pack) is fixed, because item durability is set at registration; the item ids and the SmartMoving option are
 * gone; the Harken Scythe augment ids are replaced by the enchantment ids harkenscythe_core:vitality etc.
 */
public final class ASConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue HUD_VANGUARD_Y;
    public static final ModConfigSpec.IntValue HUD_GUARD_GAUGE_Y;
    private static final Map<String, ModConfigSpec.IntValue> COLORS = new LinkedHashMap<>();
    private static final Map<String, Integer> DEFAULT_COLORS = new LinkedHashMap<>();

    static {
        DEFAULT_COLORS.put("wood", 10454093);
        DEFAULT_COLORS.put("stone", 13487565);
        DEFAULT_COLORS.put("iron", 13027014);
        DEFAULT_COLORS.put("diamond", 3402699);
        DEFAULT_COLORS.put("nether", 15065046);
        DEFAULT_COLORS.put("ender", 6314865);
        DEFAULT_COLORS.put("skull", 16184280);
        DEFAULT_COLORS.put("patchwork", 44975);
        DEFAULT_COLORS.put("livingmetal", 12905471);
        DEFAULT_COLORS.put("biomass", 13136249);
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("general");
        HUD_VANGUARD_Y = b.comment("Override HUD Vanguard Count Element y_offset (in steps of 16 pixels)")
                .defineInRange("hudVanguardYOffset", 0, -64, 64);
        HUD_GUARD_GAUGE_Y = b.comment("Override HUD Guard Gauge Element y_offset (in steps of 9 pixels)")
                .defineInRange("hudGuardGaugeYOffset", 0, -64, 64);
        b.pop();
        b.comment("Shield Color Override (ASAddon_ColorAPI): the overlay colour of undyed shields, as RGB integer").push("shieldColorOverride");
        DEFAULT_COLORS.forEach((key, value) -> COLORS.put(key, b.defineInRange(key, value, 0, 0xFFFFFF)));
        b.pop();
        SPEC = b.build();
    }

    private ASConfig() {
    }

    /** The colour of a shield material (wood, stone, iron, diamond, nether, ender, skull, patchwork, livingmetal, biomass). */
    public static int color(String key) {
        ModConfigSpec.IntValue value = COLORS.get(key);
        if (value == null) {
            return 0xFFFFFF;
        }
        try {
            return value.get();
        } catch (IllegalStateException notLoaded) {
            return DEFAULT_COLORS.get(key);
        }
    }
}
