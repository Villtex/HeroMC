package twilightforest.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * EntityFlying of 1.5.2 for monsters with their own action state (wraith, tower ghasts): no gravity, no fall damage, air
 * friction 0.91; the subclass sets the motion in customServerAiStep.
 */
public abstract class TFFlyingMonster extends Monster {
    protected double waypointX;
    protected double waypointY;
    protected double waypointZ;
    protected int courseChangeCooldown;

    protected TFFlyingMonster(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    /** EntityFlying.moveEntityWithHeading. */
    @Override
    public void travel(Vec3 input) {
        if (this.isInWater()) {
            this.moveRelative(0.02F, input);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
        } else if (this.isInLava()) {
            this.moveRelative(0.02F, input);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
        } else {
            this.moveRelative(0.02F, input);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.91));
        }
        this.calculateEntityAnimation(false);
    }

    /** isCourseTraversable: the box moved step by step to the waypoint hits nothing. */
    protected boolean isCourseTraversable(double distance) {
        double dx = (this.waypointX - this.getX()) / distance;
        double dy = (this.waypointY - this.getY()) / distance;
        double dz = (this.waypointZ - this.getZ()) / distance;
        AABB box = this.getBoundingBox();
        for (int i = 1; i < distance; i++) {
            box = box.move(dx, dy, dz);
            if (!this.level().noCollision(this, box)) {
                return false;
            }
        }
        return true;
    }
}
