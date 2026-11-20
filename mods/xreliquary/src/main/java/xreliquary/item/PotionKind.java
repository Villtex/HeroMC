package xreliquary.item;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.List;

/**
 * The 29 damage values of ItemCondensedPotion with their colour (getColor; -1: changes over time, see
 * {@link #animatedColor}) and, for the elixirs, their effects (getPotionEffects).
 */
public enum PotionKind {
    SPLASH_BASE("splash_base", 0x0055DD),
    APHRODITE("aphrodite", 0xCC3366),
    POISON("poison", 0x44BB44),
    ACID("acid", 0xDD9900),
    CONFUSION("confusion", 0xDD22DD),
    SLOWING("slowing", 0x0022FF),
    WEAKNESS("weakness", 0xDDDD22),
    WITHER("wither", 0x777777),
    BLINDING("blinding", 0x333333),
    RUINATION("ruination", -1),
    FERTILIZER("fertilizer", 0xDDDDDD),
    EMPTY_VIAL("empty_vial", 0xFFFFFF),
    SOLVENT("solvent", 0x0055DD),
    SPEED("speed", 0x0022FF, e(MobEffects.SPEED, 6000, 1)),
    DIGGING("digging", 0xDDDD22, e(MobEffects.HASTE, 6000, 1)),
    STRENGTH("strength", 0x440000, e(MobEffects.STRENGTH, 6000, 1)),
    HEALING("healing", 0xEE5566, e(MobEffects.INSTANT_HEALTH, 12, 0)),
    BOUNDING("bounding", 0x44BB44, e(MobEffects.JUMP_BOOST, 6000, 1)),
    REGENERATION("regeneration", 0xDD22DD, e(MobEffects.REGENERATION, 1200, 1)),
    RESISTANCE("resistance", 0x999999, e(MobEffects.RESISTANCE, 6000, 0)),
    FIRE_WARDING("fire_warding", 0xDD9900, e(MobEffects.FIRE_RESISTANCE, 6000, 0)),
    BREATHING("breathing", 0x0088FF, e(MobEffects.WATER_BREATHING, 6000, 0)),
    INVISIBILITY("invisibility", -1, e(MobEffects.INVISIBILITY, 6000, 0)),
    INFRAVISION("infravision", 0x000044, e(MobEffects.NIGHT_VISION, 6000, 0)),
    PROTECTION("protection", -1, e(MobEffects.RESISTANCE, 3600, 1), e(MobEffects.FIRE_RESISTANCE, 3600, 1)),
    POTENCE("potence", -1, e(MobEffects.STRENGTH, 3600, 1), e(MobEffects.HASTE, 3600, 1)),
    CELERITY("celerity", -1, e(MobEffects.JUMP_BOOST, 3600, 1), e(MobEffects.SPEED, 3600, 1)),
    PANACEA("panacea", -1, e(MobEffects.INSTANT_HEALTH, 6, 0), e(MobEffects.REGENERATION, 600, 1)),
    WATER("water", 0x0055DD);

    public final String id;
    public final int color;
    private final Effect[] effects;

    PotionKind(String id, int color, Effect... effects) {
        this.id = id;
        this.color = color;
        this.effects = effects;
    }

    private record Effect(Holder<MobEffect> effect, int duration, int amplifier) {
    }

    private static Effect e(Holder<MobEffect> effect, int duration, int amplifier) {
        return new Effect(effect, duration, amplifier);
    }

    public List<MobEffectInstance> effects() {
        return java.util.Arrays.stream(this.effects).map(e -> new MobEffectInstance(e.effect, e.duration, e.amplifier)).toList();
    }

    /** isSplash: 1..10. */
    public boolean isSplash() {
        return this.ordinal() > 0 && this.ordinal() < 11;
    }

    /** isPotion: 13..27 (drinkable). */
    public boolean isPotion() {
        return this.ordinal() > 12 && this.ordinal() < 28;
    }

    /** hasEffect: all but the empty vial, the two bases and water. */
    public boolean hasEffect() {
        return this != EMPTY_VIAL && this != SPLASH_BASE && this != SOLVENT && this != WATER;
    }

    /** The splash icon (reliquaryPotionSplash) for 0..10, the plain vial for the rest. */
    public boolean splashIcon() {
        return this.ordinal() <= 10;
    }

    /** getColor with TimeKeeperHandler's time (10..88, folded at 43) for the shimmering kinds. */
    public int animatedColor(int time) {
        int t = time > 43 ? 87 - time : time;
        return switch (this) {
            case RUINATION -> rgb(t * 5 + 22, 0xFF, 0);
            case INVISIBILITY -> rgb(t * 3 + 22, t * 3 + 22, t * 3 + 22);
            case PROTECTION -> rgb(t * 3 + 88, t * 3 + 88, t * 3 + 88);
            case POTENCE -> rgb(t * 4 + 22, t * 2 + 22, 0);
            case CELERITY -> rgb(0, t * 4 + 22, 0xFF);
            case PANACEA -> rgb(t * 4 + 22, 0, 0xFF);
            default -> this.color;
        };
    }

    /** The parts stay below 256 (time folds at 43), so the hex strings of 1.5.2 are plain RGB. */
    private static int rgb(int r, int g, int b) {
        return r << 16 | g << 8 | b;
    }
}
