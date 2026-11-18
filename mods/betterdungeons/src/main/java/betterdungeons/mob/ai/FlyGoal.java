package betterdungeons.mob.ai;

import betterdungeons.mob.BDBoss;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

/**
 * AIFly (mutex 4, the jump bit) of the wyvern: on the ground it walks to the target (absolute speed 0.33) and hops up
 * when there is no path; in the air it flies towards it (charging up to 120 while it faces the target, speed
 * max(charge / 2, 10) * 0.012), climbs or sinks 0.3 per tick to stay min(8, dist / 3) above it and turns at most
 * (180 - off-angle) / 30 degrees per tick. Within 2 blocks it attacks (every tick, as in 1.5.2), further than 15 in
 * the air it spits a large fireball (1 in 50 per tick, explosion power 1).
 */
public class FlyGoal extends Goal {
    private final Mob owner;
    private final double groundSpeed;
    private @Nullable LivingEntity target;
    private int chargeTime;

    /** @param groundSpeed factor for the ground path (0.33 absolute over the owner's movement speed) */
    public FlyGoal(Mob owner, double groundSpeed) {
        this.owner = owner;
        this.groundSpeed = groundSpeed;
        this.setFlags(EnumSet.of(Flag.JUMP));
        owner.getNavigation().setCanFloat(true);
    }

    @Override
    public boolean canUse() {
        LivingEntity t = this.owner.getTarget();
        if (t == null) {
            return false;
        }
        if (!t.isAlive()) {
            this.owner.setTarget(null);
            return false;
        }
        this.target = t;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity t = this.owner.getTarget();
        return t != null && t == this.target && t.isAlive() && this.owner.isWithinHome(BlockPos.containing(t.position()));
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public int getChargeTime() {
        return this.chargeTime;
    }

    @Override
    public void tick() {
        LivingEntity t = this.target;
        if (t == null) {
            return;
        }
        this.owner.getLookControl().setLookAt(t, 30.0F, 30.0F);
        double dist = this.owner.distanceTo(t);
        Vec3 m = this.owner.getDeltaMovement();
        if (!this.owner.onGround()) {
            this.owner.getNavigation().stop();
            double rotDiff = Math.abs(BDBoss.angleBetween(this.owner, t));
            double rot = Math.abs(rotDiff - Math.abs(BDBoss.wrap(this.owner.getYRot())));
            if (rot < 15.0 + dist) {
                this.chargeTime = Math.min(this.chargeTime + 4, 120);
            } else {
                this.chargeTime = Math.max(this.chargeTime - 1, 0);
            }
            double my = t.getY() + Math.min(8.0, dist / 3.0) - this.owner.getY() > 0.0 ? 0.3 : -0.3;
            double ry = Math.toRadians(this.owner.getYRot() - 180.0F - 20.0F);
            int currentSpeed = Math.max(this.chargeTime / 2, 10);
            float moveSpeed = 0.12F;
            this.owner.setDeltaMovement(Math.sin(ry) * currentSpeed * moveSpeed / 10.0, my, -Math.cos(ry) * currentSpeed * moveSpeed / 10.0);
            float maxRot = (float) (180.0 - rot) / 30.0F;
            float yaw = this.owner.getYRot();
            if (this.owner.getYHeadRot() + maxRot > yaw) {
                this.owner.setYRot(yaw + maxRot);
            } else if (this.owner.getYHeadRot() - maxRot < yaw) {
                this.owner.setYRot(yaw - maxRot);
            }
        } else {
            this.owner.getNavigation().moveTo(t, this.groundSpeed);
            if (this.owner.getNavigation().isDone()) {
                this.owner.setDeltaMovement(m.x, 0.3, m.z);
            }
        }
        if (!(this.owner.level() instanceof ServerLevel level)) {
            return;
        }
        if (dist < 2.0) {
            this.owner.doHurtTarget(level, t);
            BDBoss.swingArm(this.owner);
        } else if (!this.owner.onGround() && dist > 15.0 && this.owner.getRandom().nextInt(50) == 0) {
            double d5 = t.getX() - this.owner.getX();
            double d6 = t.getBoundingBox().minY + t.getBbHeight() / 2.0F - (this.owner.getY() + 2.0);
            double d7 = t.getZ() - this.owner.getZ();
            // EntityFireball(world, shooter, x, y, z): gaussian spread 0.4 on the heading, power 1
            d5 += this.owner.getRandom().nextGaussian() * 0.4;
            d6 += this.owner.getRandom().nextGaussian() * 0.4;
            d7 += this.owner.getRandom().nextGaussian() * 0.4;
            LargeFireball fireball = new LargeFireball(level, this.owner, new Vec3(d5, d6, d7).normalize(), 1);
            level.addFreshEntity(fireball);
        }
    }
}
