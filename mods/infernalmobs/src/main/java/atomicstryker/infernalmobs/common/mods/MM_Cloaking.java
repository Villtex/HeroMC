package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;

/**
 * MM_Cloaking of Infernal Mobs 1.3.1, ported 1:1.
 */
public class MM_Cloaking extends MobModifier {
    private long nextAbilityUse = 0L;
    private static final long coolDown = 10000L;
    private static final Class<?>[] disallowed = {Spider.class};
    private static final String[] suffix = {" of Stalking", " the Unseen", " the Predator"};
    private static final String[] prefix = {" stalking ", " unseen ", " hunting "};

    public MM_Cloaking(LivingEntity mob, MobModifier prevMod) {
        this.modName = "Cloaking";
        this.nextMod = prevMod;
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        if (this.getMobTarget() instanceof Player) {
            this.tryAbility(mob);
        }
        return super.onUpdate(mob);
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getEntity() instanceof LivingEntity) {
            this.tryAbility(mob);
        }
        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(LivingEntity mob) {
        long time = System.currentTimeMillis();
        if (time > this.nextAbilityUse) {
            this.nextAbilityUse = time + coolDown;
            mob.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200));
        }
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
