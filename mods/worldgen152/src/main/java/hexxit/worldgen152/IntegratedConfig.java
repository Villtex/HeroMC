package hexxit.worldgen152;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code config/worldgen152-startup.toml}: which Hexxit generators run in the world type Hexxit Integrated. Each ore
 * that a mod of today may also bring has its own switch, as the original mods had ({@code generateCopper} …), so an
 * ore mod can take over; anything else can be switched off by name. Hexxit Legacy ignores this file (it is the pack
 * as it was).
 */
public final class IntegratedConfig {
    public static final ModConfigSpec SPEC;
    /** Feature name → switch. */
    private static final Map<String, ModConfigSpec.BooleanValue> ORES = new LinkedHashMap<>();
    private static final ModConfigSpec.ConfigValue<List<? extends String>> DISABLED;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("World type Hexxit Integrated: ores of the Hexxit mods. false = not generated (another mod or data pack takes over).")
                .push("ores");
        ore(b, "tconstruct:copper_ore", "generateCopper", "Tinkers' Construct copper ore (vanilla copper exists too: half the original amount)");
        ore(b, "tconstruct:tin_ore", "generateTin", "Tinkers' Construct tin ore");
        ore(b, "tconstruct:aluminum_ore", "generateAluminum", "Tinkers' Construct aluminum ore");
        ore(b, "tconstruct:cobalt_ore", "generateCobalt", "Tinkers' Construct cobalt ore (Nether)");
        ore(b, "tconstruct:ardite_ore", "generateArdite", "Tinkers' Construct ardite ore (Nether)");
        ore(b, "tconstruct:surface_ores", "generateSurfaceOres", "Tinkers' Construct ore gravel near the surface");
        ore(b, "cofhcore:custom_ores", "generateQuantumOre", "qCraft quantum ore (CoFH World custom generation)");
        ore(b, "meteors:meteors", "generateMeteorOre", "Falling Meteors underground meteor ore");
        b.pop();
        DISABLED = b.comment("Further Hexxit generators (feature names like \"natura:clouds\") that do not run in Hexxit Integrated.")
                .defineListAllowEmpty("disabledFeatures", List.of(), () -> "", o -> o instanceof String);
        SPEC = b.build();
    }

    private IntegratedConfig() {
    }

    private static void ore(ModConfigSpec.Builder b, String feature, String key, String comment) {
        ORES.put(feature, b.comment(comment).define(key, true));
    }

    /** Whether the generator {@code name} runs as a feature. */
    public static boolean enabled(String name) {
        if (!SPEC.isLoaded()) {
            return true;
        }
        ModConfigSpec.BooleanValue ore = ORES.get(name);
        if (ore != null && !ore.get()) {
            return false;
        }
        return !DISABLED.get().contains(name);
    }
}
