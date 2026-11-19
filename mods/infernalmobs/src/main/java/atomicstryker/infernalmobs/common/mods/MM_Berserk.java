package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;

/**
 * MM_Berserk of Infernal Mobs 1.3.1, ported 1:1.
 */
public class MM_Berserk extends MobModifier {
    private static final Class<?>[] disallowed = {Creeper.class};
    private static final String[] suffix = {" of Recklessness", " the Raging", " of Smashing"};
    private static final String[] prefix = {" reckless ", " raging ", " smashing "};

    public MM_Berserk(LivingEntity mob, MobModifier prevMod) {
        this.modName = "Berserk";
        this.nextMod = prevMod;
    }

    @Override
    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        if (entity != null) {
            hurt(source.getEntity(), entity.damageSources().generic(), damage);
            damage *= 2;
        }
        return super.onAttack(entity, source, damage);
    }

    @Override
    public Class<?>[] getBlackListMobClasses() {
        return disallowed;
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
