package projectzulu.potion;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

/**
 * PotionIncendiary (instant): a thrown potion (splash, lingering cloud) sets the hit entity on fire for 60 seconds
 * (affectEntity: setFire(60)); drinking it or an active effect burns for "amplifier" seconds (performEffect:
 * setFire(par2)), so the level-I potion does nothing when drunk, level II burns for one second.
 */
public class IncendiaryEffect extends ZuluEffect {
    public IncendiaryEffect(int color) {
        super(color, true);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity mob, int amplifier) {
        if (amplifier > 0) {
            mob.igniteForSeconds(amplifier);
        }
        return true;
    }

    @Override
    public void applyInstantaneousEffect(ServerLevel level, @Nullable Entity source, @Nullable Entity owner, LivingEntity mob,
                                         int amplifier, double scale) {
        if (source == mob) {
            // drunk (PotionContents passes the drinker as source)
            this.applyEffectTick(level, mob, amplifier);
        } else {
            mob.igniteForSeconds(60);
        }
    }
}
