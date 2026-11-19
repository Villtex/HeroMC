package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.Vec3;

/**
 * MM_Ghastly of Infernal Mobs 1.3.1, ported 1:1.
 * The ghast's shooting sound is level event 1016 today (1008 in 1.5.2).
 */
public class MM_Ghastly extends MobModifier {
    private long nextAbilityUse = 0L;
    private static final long coolDown = 6000L;
    private static final float MIN_DISTANCE = 3.0F;
    private static final String[] suffix = {" OMFGFIREBALLS", " the Bomber", " of Balls of Fire"};
    private static final String[] prefix = {" bombing ", " fireballsy "};

    public MM_Ghastly(LivingEntity mob, MobModifier prevMod) {
        this.modName = "Ghastly";
        this.nextMod = prevMod;
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        long time = System.currentTimeMillis();
        if (time > this.nextAbilityUse) {
            this.nextAbilityUse = time + coolDown;
            this.tryAbility(mob, closestVulnerablePlayer(mob, 12.0));
        }
        return super.onUpdate(mob);
    }

    private void tryAbility(LivingEntity mob, LivingEntity target) {
        if (target != null && mob.hasLineOfSight(target) && mob.distanceTo(target) > MIN_DISTANCE) {
            double diffX = target.getX() - mob.getX();
            double diffY = target.getBoundingBox().minY + target.getBbHeight() / 2.0F - (mob.getY() + mob.getBbHeight() / 2.0F);
            double diffZ = target.getZ() - mob.getZ();
            float yaw = -((float) Math.atan2(diffX, diffZ)) * 180.0F / (float) Math.PI;
            mob.setYRot(yaw);
            mob.yBodyRot = yaw;
            mob.level().levelEvent(null, LevelEvent.SOUND_GHAST_FIREBALL, mob.blockPosition(), 0);
            LargeFireball entFB = new LargeFireball(mob.level(), mob, new Vec3(diffX, diffY, diffZ), 1);
            double spawnOffset = 2.0;
            Vec3 mobLook = mob.getViewVector(1.0F);
            entFB.setPos(mob.getX() + mobLook.x * spawnOffset, mob.getY() + mob.getBbHeight() / 2.0F + 0.5, mob.getZ() + mobLook.z * spawnOffset);
            mob.level().addFreshEntity(entFB);
        }
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
