package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * MM_Sapper of Infernal Mobs 1.3.1, ported 1:1.
 */
public class MM_Sapper extends MobModifier {
    private static final String[] suffix = {" of Hunger", " the Pale Rider"};
    private static final String[] prefix = {" hungering ", " starving "};

    public MM_Sapper(LivingEntity mob, MobModifier prevMod) {
        this.modName = "Sapper";
        this.nextMod = prevMod;
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getEntity() instanceof LivingEntity ent) {
            if (!ent.hasEffect(MobEffects.HUNGER)) {
                ent.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 0));
            }
        }
        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        if (entity != null && !entity.hasEffect(MobEffects.POISON)) {
            entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 0));
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
