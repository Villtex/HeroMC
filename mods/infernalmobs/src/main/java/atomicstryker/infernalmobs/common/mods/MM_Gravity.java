package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * MM_Gravity of Infernal Mobs 1.3.1, ported 1:1.
 * The knockback packet to the player is the same knockback on the server with {@code needsSync}.
 */
public class MM_Gravity extends MobModifier {
    private final boolean offensive;
    private long nextAbilityUse = 0L;
    private static final long coolDown = 5000L;
    private static final Class<?>[] modBans = {MM_Webber.class};
    private static final String[] suffix = {" of Repulsion", " the Flipper"};
    private static final String[] prefix = {" repulsing ", " sproing "};

    public MM_Gravity(LivingEntity mob, MobModifier prevMod) {
        this.modName = "Gravity";
        this.nextMod = prevMod;
        // original: isAssignableFrom the wrong way round, never true, so the mob only repels attackers
        this.offensive = mob.getClass().isAssignableFrom(Enemy.class);
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        if (this.offensive && this.getMobTarget() instanceof Player) {
            this.tryAbility(mob, this.getMobTarget());
        }
        return super.onUpdate(mob);
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (!this.offensive && source.getEntity() instanceof LivingEntity attacker) {
            this.tryAbility(mob, attacker);
        }
        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(LivingEntity mob, LivingEntity target) {
        if (target != null && mob.hasLineOfSight(target)) {
            long time = System.currentTimeMillis();
            if (time > this.nextAbilityUse) {
                this.nextAbilityUse = time + coolDown;
                LivingEntity source = this.offensive ? mob : target;
                LivingEntity destination = this.offensive ? target : mob;
                double diffX = destination.getX() - source.getX();
                double diffZ;
                for (diffZ = destination.getZ() - source.getZ(); diffX * diffX + diffZ * diffZ < 1.0E-4; diffZ = (Math.random() - Math.random()) * 0.01) {
                    diffX = (Math.random() - Math.random()) * 0.01;
                }
                playSound(mob, SoundEvents.IRON_GOLEM_ATTACK, 1.0F, randomPitch(mob));
                knockBack(target, diffX, diffZ);
            }
        }
    }

    public static void knockBack(LivingEntity target, double x, double z) {
        target.needsSync = true;
        float normalizedPower = (float) Math.sqrt(x * x + z * z);
        float knockPower = 0.8F;
        Vec3 m = target.getDeltaMovement();
        double motionX = m.x / 2.0 - x / normalizedPower * knockPower;
        double motionY = m.y / 2.0 + knockPower;
        double motionZ = m.z / 2.0 - z / normalizedPower * knockPower;
        if (motionY > 0.4F) {
            motionY = 0.4F;
        }
        target.setDeltaMovement(motionX, motionY, motionZ);
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
