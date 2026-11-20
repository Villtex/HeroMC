package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * MM_Darkness of Infernal Mobs 1.3.1, ported 1:1.
 */
public class MM_Darkness extends MobModifier {
    private static final String[] suffix = {" of Darkness", " the Shadow", " the Eclipse"};
    private static final String[] prefix = {" dark ", " shadowkin ", " eclipsed "};

    public MM_Darkness(LivingEntity mob, MobModifier prevMod) {
        this.modName = "Darkness";
        this.nextMod = prevMod;
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getEntity() instanceof LivingEntity ent) {
            ent.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
        }
        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        if (entity != null) {
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
        }
        return super.onAttack(entity, source, damage);
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
