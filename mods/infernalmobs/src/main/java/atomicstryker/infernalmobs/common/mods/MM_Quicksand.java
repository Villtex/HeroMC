package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * MM_Quicksand of Infernal Mobs 1.3.1, ported 1:1.
 */
public class MM_Quicksand extends MobModifier {
    int ticker = 0;
    private static final String[] suffix = {" of You-Cant-Run", " the slowing B****"};
    private static final String[] prefix = {" slowing ", " Quicksand "};

    public MM_Quicksand(LivingEntity mob, MobModifier prevMod) {
        this.modName = "Quicksand";
        this.nextMod = prevMod;
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        if (this.getMobTarget() != null && mob.hasLineOfSight(this.getMobTarget()) && ++this.ticker == 50) {
            this.ticker = 0;
            this.getMobTarget().addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 0));
        }
        return super.onUpdate(mob);
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }
}
