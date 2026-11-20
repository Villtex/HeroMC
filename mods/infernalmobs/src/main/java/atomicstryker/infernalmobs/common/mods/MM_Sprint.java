package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * MM_Sprint of Infernal Mobs 1.3.1, ported 1:1.
 */
public class MM_Sprint extends MobModifier {
    private long nextAbilityUse = 0L;
    private static final long coolDown = 5000L;
    private boolean sprinting;
    private double modMotionX;
    private double modMotionZ;
    private static final String[] suffix = {" of Bolting", " the Swift One", " of being in your Face"};
    private static final String[] prefix = {" sprinting ", " swift ", " charging "};

    public MM_Sprint(LivingEntity mob, MobModifier prevMod) {
        this.modName = "Sprint";
        this.nextMod = prevMod;
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        if (this.getMobTarget() != null) {
            long time = System.currentTimeMillis();
            if (time > this.nextAbilityUse) {
                this.nextAbilityUse = time + coolDown;
                this.sprinting = !this.sprinting;
            }
            if (this.sprinting) {
                this.doSprint(mob);
            }
        }
        return super.onUpdate(mob);
    }

    private void doSprint(LivingEntity mob) {
        Vec3 motion = mob.getDeltaMovement();
        float rotationMovement = (float) (Math.atan2(motion.x, motion.z) * 180.0 / 3.1415);
        float rotationLook = mob.getYRot();
        if (rotationLook > 360.0F) {
            rotationLook -= rotationLook % 360.0F * 360.0F;
        } else if (rotationLook < 0.0F) {
            rotationLook += rotationLook * -1.0F % 360.0F * 360.0F;
        }
        if (Math.abs(rotationMovement + rotationLook) > 10.0F) {
            rotationLook -= 360.0F;
        }
        double entspeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (Math.abs(rotationMovement + rotationLook) > 10.0F) {
            this.modMotionX = motion.x;
            this.modMotionZ = motion.z;
        }
        if (entspeed < 0.3) {
            if (this.getAbsModSpeed() > 0.6 || !mob.onGround()) {
                this.modMotionX /= 1.55;
                this.modMotionZ /= 1.55;
            }
            this.modMotionX *= 1.5;
            this.modMotionZ *= 1.5;
            mob.setDeltaMovement(this.modMotionX, motion.y, this.modMotionZ);
        }
    }

    private double getAbsModSpeed() {
        return Math.sqrt(this.modMotionX * this.modMotionX + this.modMotionZ * this.modMotionZ);
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
