package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.monster.Creeper;

/**
 * MM_1UP of Infernal Mobs 1.3.1, ported 1:1.
 */
public class MM_1UP extends MobModifier {
    private boolean healed = false;
    private static final Class<?>[] disallowed = {Creeper.class};
    private static final String[] suffix = {" of Recurrence", " the Undying", " of twin Lives"};
    private static final String[] prefix = {" recurring ", " undying ", " twinlived "};

    public MM_1UP(LivingEntity mob, MobModifier prevMod) {
        this.modName = "1UP";
        this.nextMod = prevMod;
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        if (!this.healed && mob.getHealth() < this.getActualMaxHealth(mob) * 0.25) {
            setHealth(mob, this.getActualMaxHealth(mob));
            playSound(mob, SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F);
            this.healed = true;
        }
        return super.onUpdate(mob);
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
