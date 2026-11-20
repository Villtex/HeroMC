package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

/**
 * MM_Blastoff of Infernal Mobs 1.3.1, ported 1:1.
 * The velocity packet to the player is a server-side push with {@code needsSync}.
 */
public class MM_Blastoff extends MobModifier {
    private long nextAbilityUse = 0L;
    private static final long coolDown = 15000L;
    private static final Class<?>[] modBans = {MM_Webber.class};
    private static final String[] suffix = {" of Mission Control", " the NASA", " of WEEEEE"};
    private static final String[] prefix = {" thumping ", " trolling ", " byebye "};

    public MM_Blastoff(LivingEntity mob, MobModifier prevMod) {
        this.modName = "Blastoff";
        this.nextMod = prevMod;
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        if (this.getMobTarget() instanceof Player) {
            this.tryAbility(mob, this.getMobTarget());
        }
        return super.onUpdate(mob);
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getEntity() instanceof LivingEntity attacker) {
            this.tryAbility(mob, attacker);
        }
        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(LivingEntity mob, LivingEntity target) {
        if (target != null && mob.hasLineOfSight(target)) {
            long time = System.currentTimeMillis();
            if (time > this.nextAbilityUse) {
                this.nextAbilityUse = time + coolDown;
                playSound(mob, SoundEvents.SLIME_ATTACK, 1.0F, randomPitch(mob));
                target.push(0.0, 1.1, 0.0);
            }
        }
    }

    @Override
    public Class<?>[] getModsNotToMixWith() {
        return modBans;
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
