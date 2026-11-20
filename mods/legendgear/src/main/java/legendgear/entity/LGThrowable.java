package legendgear.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * EntityThrowable of 1.5.2: starts at the thrower's eyes, 0.16 to the side and 0.1 lower, along its look at 1.5 with
 * a spread of 0.0075; every tick it traces its path against blocks (both sides) and entities (server only, the thrower
 * itself only after 5 ticks), then moves, slows by 0.99 (0.8 in water) and falls by {@link #gravity()}.
 */
public abstract class LGThrowable extends Projectile {
    protected int ticksInAir;

    protected LGThrowable(EntityType<? extends LGThrowable> type, Level level) {
        super(type, level);
    }

    /** EntityThrowable(world, thrower). */
    protected LGThrowable(EntityType<? extends LGThrowable> type, Level level, LivingEntity thrower) {
        super(type, level);
        this.setOwner(thrower);
        float yaw = thrower.getYRot();
        float pitch = thrower.getXRot();
        double x = thrower.getX() - Mth.cos(yaw / 180.0F * (float) Math.PI) * 0.16F;
        double y = thrower.getEyeY() - 0.1F;
        double z = thrower.getZ() - Mth.sin(yaw / 180.0F * (float) Math.PI) * 0.16F;
        this.snapTo(x, y, z, yaw, pitch);
        float f = 0.4F;
        double mx = -Mth.sin(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI) * f;
        double mz = Mth.cos(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI) * f;
        double my = -Mth.sin(pitch / 180.0F * (float) Math.PI) * f;
        this.heading(mx, my, mz, 1.5F, 1.0F);
    }

    /** setThrowableHeading. */
    public void heading(double x, double y, double z, float speed, float inaccuracy) {
        double len = Math.sqrt(x * x + y * y + z * z);
        x /= len;
        y /= len;
        z /= len;
        x += this.random.nextGaussian() * 0.0075F * inaccuracy;
        y += this.random.nextGaussian() * 0.0075F * inaccuracy;
        z += this.random.nextGaussian() * 0.0075F * inaccuracy;
        x *= speed;
        y *= speed;
        z *= speed;
        this.setDeltaMovement(x, y, z);
        double h = Math.sqrt(x * x + z * z);
        this.setYRot((float) (Math.atan2(x, z) * 180.0 / Math.PI));
        this.setXRot((float) (Math.atan2(y, h) * 180.0 / Math.PI));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    /** getGravityVelocity: 0.03 for the throwables of 1.5.2. */
    protected float gravity() {
        return 0.03F;
    }

    @Override
    protected double getDefaultGravity() {
        return this.gravity();
    }

    protected abstract void impact(HitResult hit);

    @Override
    public void tick() {
        this.xOld = this.getX();
        this.yOld = this.getY();
        this.zOld = this.getZ();
        this.baseTick();
        if (this.isRemoved()) {
            return;
        }
        this.ticksInAir++;
        Vec3 start = this.position();
        Vec3 motion = this.getDeltaMovement();
        Vec3 end = start.add(motion);
        HitResult hit = this.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() != HitResult.Type.MISS) {
            end = hit.getLocation();
        }
        if (!this.level().isClientSide()) {
            Entity owner = this.getOwner();
            Entity closest = null;
            double best = 0.0;
            AABB area = this.getBoundingBox().expandTowards(motion).inflate(1.0);
            for (Entity e : this.level().getEntities(this, area)) {
                if (e.isPickable() && e.isAlive() && (e != owner || this.ticksInAir >= 5)) {
                    Optional<Vec3> clip = e.getBoundingBox().inflate(0.3).clip(start, end);
                    if (clip.isPresent()) {
                        double d = start.distanceTo(clip.get());
                        if (d < best || best == 0.0) {
                            closest = e;
                            best = d;
                        }
                    }
                }
            }
            if (closest != null) {
                hit = new EntityHitResult(closest);
            }
        }
        if (hit.getType() != HitResult.Type.MISS) {
            this.impact(hit);
            if (this.isRemoved()) {
                return;
            }
        }
        motion = this.getDeltaMovement();
        this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
        this.updateRotation();
        float f = 0.99F;
        if (this.isInWater()) {
            for (int i = 0; i < 4; i++) {
                this.level().addParticle(ParticleTypes.BUBBLE, this.getX() - motion.x * 0.25, this.getY() - motion.y * 0.25,
                        this.getZ() - motion.z * 0.25, motion.x, motion.y, motion.z);
            }
            f = 0.8F;
        }
        this.setDeltaMovement(motion.x * f, motion.y * f - this.gravity(), motion.z * f);
    }

    protected static @Nullable BlockHitResult block(HitResult hit) {
        return hit instanceof BlockHitResult b && hit.getType() == HitResult.Type.BLOCK ? b : null;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
