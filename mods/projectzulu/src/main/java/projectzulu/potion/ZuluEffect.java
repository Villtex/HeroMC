package projectzulu.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * PotionZulu: an effect whose work happens in {@link PotionEvents} (slowfall, cleansing, curse, thorn) or nowhere at
 * all (bubbling = "Shiny!", an instant effect without any action that only marks the "Shiny" brewing base).
 * All six were registered with isBadEffect = true in 1.5.2, so they keep the harmful category (red tooltip).
 */
public class ZuluEffect extends MobEffect {
    private final boolean instant;

    public ZuluEffect(int color, boolean instant) {
        super(MobEffectCategory.HARMFUL, color);
        this.instant = instant;
    }

    @Override
    public boolean isInstantaneous() {
        return this.instant;
    }
}
