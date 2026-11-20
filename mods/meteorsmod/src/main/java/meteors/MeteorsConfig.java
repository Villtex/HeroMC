package meteors;

import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * The "general" section of the pack's Meteors.cfg (z-hexxit-configs) as a server config. Block and item ids are
 * fixed in {@link MeteorsMod#registerOldIds()}, the world generation values in {@link meteors.gen.MeteorsWorldGen}.
 */
public final class MeteorsConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ALLOW_SUMMONED_METEOR_GRIEF;
    public static final ModConfigSpec.BooleanValue FREZARITE_ENABLED;
    public static final ModConfigSpec.IntValue KITTY_ATTACK_CHANCE;
    public static final ModConfigSpec.BooleanValue KREKNORITE_ENABLED;
    public static final ModConfigSpec.IntValue MAX_METEOR_SIZE;
    public static final ModConfigSpec.IntValue METEOR_FALL_DETERRENCE;
    public static final ModConfigSpec.IntValue METEOR_FALL_RADIUS;
    public static final ModConfigSpec.BooleanValue METEORITE_ENABLED;
    public static final ModConfigSpec.BooleanValue ONLY_AT_NIGHT;
    public static final ModConfigSpec.IntValue MIN_METEOR_SIZE;
    public static final ModConfigSpec.IntValue SHIELD_RADIUS;
    public static final ModConfigSpec.BooleanValue TEXT_CRASH_NOTIFICATION;
    public static final ModConfigSpec.BooleanValue UNKNOWN_ENABLED;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ALLOW_SUMMONED_METEOR_GRIEF = b.comment("Summoned meteors may fall on land shielded by another player")
                .define("allowSummonedMeteorGrief", false);
        FREZARITE_ENABLED = b.define("frezariteMeteorEnabled", true);
        KITTY_ATTACK_CHANCE = b.comment("Percent chance that a meteor event is a comet kitty attack").defineInRange("kittyAttackChance", 0, 0, 100);
        KREKNORITE_ENABLED = b.define("kreknoriteMeteorEnabled", true);
        MAX_METEOR_SIZE = b.defineInRange("maximumMeteorSize", 3, 1, 3);
        METEOR_FALL_DETERRENCE = b.comment("x 2000 ticks: a quarter of it decides when the next meteor is announced, three quarters when it falls")
                .defineInRange("meteorFallDeterrence", 60, 1, 100000);
        METEOR_FALL_RADIUS = b.comment("Blocks around a random player where meteors fall").defineInRange("meteorFallRadius", 350, 1, 100000);
        METEORITE_ENABLED = b.define("meteoriteMeteorEnabled", true);
        ONLY_AT_NIGHT = b.define("meteorsOnlyFallAtNight", true);
        MIN_METEOR_SIZE = b.defineInRange("minimumMeteorSize", 1, 1, 3);
        SHIELD_RADIUS = b.comment("Radius in chunks per power level of a meteor shield").defineInRange("shieldRadiusInChunks", 4, 0, 64);
        TEXT_CRASH_NOTIFICATION = b.define("textCrashNotification", false);
        UNKNOWN_ENABLED = b.define("unknownMeteorEnabled", true);
        SPEC = b.build();
    }

    private MeteorsConfig() {
    }

    private static int configTicks() {
        return METEOR_FALL_DETERRENCE.get() * 2000;
    }

    /** MinTicksUntilMeteorSpawn: a quarter of a quarter. */
    public static int minTicksUntilSpawn() {
        return (int) ((int) (configTicks() * 0.25) * 0.25);
    }

    public static int randTicksUntilSpawn() {
        return (int) ((int) (configTicks() * 0.25) * 0.75);
    }

    public static int minTicksUntilCrash() {
        return (int) ((int) (configTicks() * 0.75) * 0.4);
    }

    public static int randTicksUntilCrash() {
        return (int) ((int) (configTicks() * 0.75) * 0.6);
    }

    /** setClientStartConfig: both clamped to 1..3, min not above max. */
    public static int minSize() {
        return Math.min(Mth.clamp(MIN_METEOR_SIZE.get(), 1, 3), maxSize());
    }

    public static int maxSize() {
        return Mth.clamp(MAX_METEOR_SIZE.get(), 1, 3);
    }
}
