package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;

/**
 * MM_Lifesteal of Infernal Mobs 1.3.1, ported 1:1.
 */
public class MM_Lifesteal extends MobModifier {
    private static final Class<?>[] disallowed = {Creeper.class};
    private static final String[] suffix = {" the Vampire", " of Transfusion", " the Bloodsucker"};
    private static final String[] prefix = {" vampiric ", " transfusing ", " bloodsucking "};

    public MM_Lifesteal(LivingEntity mob, MobModifier prevMod) {
        this.modName = "LifeSteal";
        this.nextMod = prevMod;
    }

    @Override
    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        LivingEntity mob = (LivingEntity) source.getEntity();
        if (entity != null && mob.getHealth() < this.getActualMaxHealth(mob)) {
            setHealth(mob, mob.getHealth() + damage);
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
