package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * MM_Vengeance of Infernal Mobs 1.3.1, ported 1:1.
 */
public class MM_Vengeance extends MobModifier {
    private static final String[] suffix = {" of Retribution", " the Thorned", " of Striking Back"};
    private static final String[] prefix = {" thorned ", " thorny ", " spiky "};

    public MM_Vengeance(LivingEntity mob, MobModifier prevMod) {
        this.modName = "Vengeance";
        this.nextMod = prevMod;
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getEntity() != null && source.getEntity() != mob) {
            hurt(source.getEntity(), mob.damageSources().mobAttack(mob), Math.max((float) Math.floor(damage / 2), 1.0F));
        }
        return super.onHurt(mob, source, damage);
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
