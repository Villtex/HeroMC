package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * MM_Regen of Infernal Mobs 1.3.1, ported 1:1.
 */
public class MM_Regen extends MobModifier {
    private long nextAbilityUse = 0L;
    private static final long coolDown = 500L;
    private static final String[] suffix = {" of WTF IMBA", " the Cancerous", " of First Aid"};
    private static final String[] prefix = {" regenerating ", " healing ", " nigh unkillable "};

    public MM_Regen(LivingEntity mob, MobModifier prevMod) {
        this.modName = "Regen";
        this.nextMod = prevMod;
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        if (mob.getHealth() < this.getActualMaxHealth(mob)) {
            long time = System.currentTimeMillis();
            if (time > this.nextAbilityUse) {
                this.nextAbilityUse = time + coolDown;
                setHealth(mob, mob.getHealth() + 1);
            }
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
