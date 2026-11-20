package legendgear.entity;

import legendgear.LGEntities;
import legendgear.LGFx;
import legendgear.LGItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * EntityBomb: a lit bomb. Falls (0.04 a tick, friction 0.98, on the ground the block's slipperiness), fuse 80 ticks
 * (pulsing faster in the last 10), blows up into an {@link BombBlastEntity} when the fuse runs out or it burns, goes
 * out in water (and drops as an item), can be hit out by a bomb bag. Tied to an arrow it rides along and blows up when
 * the arrow sticks. Bombs with a fuse of 10 or less are not pushed by other blasts.
 */
public class BombEntity extends Entity implements net.minecraft.world.entity.projectile.ItemSupplier {
    private static final EntityDataAccessor<Integer> FUSE = SynchedEntityData.defineId(BombEntity.class, EntityDataSerializers.INT);
    public static final int LONG_FUSE_TIME = 80;
    public @Nullable LivingEntity thrower;
    public boolean primed;
    public double pulse;
    public boolean wasOnArrow;
    public boolean unblastable;

    public BombEntity(EntityType<? extends BombEntity> type, Level level) {
        super(type, level);
    }

    /** EntityBomb(world, x, y, z, fusetime). */
    public static BombEntity at(Level level, double x, double y, double z, int fuse) {
        BombEntity bomb = new BombEntity(LGEntities.BOMB.get(), level);
        bomb.setPos(x, y, z);
        bomb.setFuse(fuse);
        bomb.unblastable = fuse <= 10;
        return bomb;
    }

    /** EntityBomb(world, thrower, fusetime): from the eyes along the look at 0.5. */
    public static BombEntity thrown(Level level, LivingEntity thrower, int fuse) {
        BombEntity bomb = new BombEntity(LGEntities.BOMB.get(), level);
        bomb.thrower = thrower;
        float yaw = thrower.getYRot();
        float pitch = thrower.getXRot();
        bomb.snapTo(thrower.getX() - Mth.cos(yaw / 180.0F * (float) Math.PI) * 0.16F, thrower.getEyeY() - 0.1F,
                thrower.getZ() - Mth.sin(yaw / 180.0F * (float) Math.PI) * 0.16F, yaw, pitch);
        float f = 0.4F;
        double mx = -Mth.sin(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI) * f;
        double mz = Mth.cos(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI) * f;
        double my = -Mth.sin(pitch / 180.0F * (float) Math.PI) * f;
        bomb.heading(mx, my, mz, 0.5F, 0.0F);
        bomb.setFuse(fuse);
        bomb.unblastable = fuse <= 10;
        return bomb;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(LGItems.BOMB.get());
    }

    public void heading(double x, double y, double z, float speed, float inaccuracy) {
        double len = Math.sqrt(x * x + y * y + z * z);
        x = x / len + this.random.nextGaussian() * 0.0075F * inaccuracy;
        y = y / len + this.random.nextGaussian() * 0.0075F * inaccuracy;
        z = z / len + this.random.nextGaussian() * 0.0075F * inaccuracy;
        this.setDeltaMovement(x * speed, y * speed, z * speed);
    }

    public int fuse() {
        return this.entityData.get(FUSE);
    }

    public void setFuse(int fuse) {
        this.entityData.set(FUSE, fuse);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FUSE, LONG_FUSE_TIME);
    }

    @Override
    public boolean isPickable() {
        return this.isAlive() && this.getVehicle() == null && this.onGround();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    public void fizzle() {
        if (this.level() instanceof ServerLevel) {
            this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), new ItemStack(LGItems.BOMB.get())));
            LGFx.sound(this, SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.7F);
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.5, this.getZ(), 3, 0, 0, 0, 0);
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isInWater()) {
            this.fizzle();
            return;
        }
        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.6, this.getZ(), 0.0, 0.0, 0.0);
        }
        Vec3 m = this.getDeltaMovement();
        m = m.add(0.0, -0.04F, 0.0);
        float friction = 0.98F;
        if (this.onGround()) {
            BlockPos below = BlockPos.containing(this.getX(), this.getBoundingBox().minY - 1.0, this.getZ());
            friction = this.level().getBlockState(below).getFriction(this.level(), below, this) * 0.98F;
        }
        m = new Vec3(m.x * friction, m.y * 0.98F, m.z * friction);
        this.setDeltaMovement(m);
        if (this.getVehicle() == null) {
            this.move(MoverType.SELF, m);
        }
        if (!this.primed) {
            this.primed = true;
            LGFx.sound(this, SoundEvents.TNT_PRIMED, 1.0F, 1.0F);
        }
        int fuse = this.fuse() - 1;
        if (!this.level().isClientSide()) {
            this.setFuse(fuse);
        }
        this.pulse += fuse < 10 ? 1.0 : 0.1;
        if (fuse <= 0 || this.isOnFire()) {
            this.detonate();
        } else if (this.getVehicle() instanceof AbstractArrow arrow) {
            this.setDeltaMovement(arrow.getDeltaMovement());
            if (arrow.shakeTime != 0 || arrow.getDeltaMovement().lengthSqr() < 1.0E-4) {
                this.detonate();
            }
            this.wasOnArrow = true;
        } else if (this.wasOnArrow) {
            this.detonate();
        }
    }

    public void detonate() {
        if (this.level() instanceof ServerLevel) {
            Entity vehicle = this.getVehicle();
            double x = this.getX();
            double y = this.getY();
            double z = this.getZ();
            if (vehicle != null) {
                x = vehicle.getX();
                y = vehicle.getY();
                z = vehicle.getZ();
                this.stopRiding();
                vehicle.discard();
            }
            this.level().addFreshEntity(new BombBlastEntity(this.level(), this.thrower, x, y, z));
            this.discard();
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.setFuse(input.getIntOr("fuse", LONG_FUSE_TIME));
        this.unblastable = input.getBooleanOr("unblastable", false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("fuse", this.fuse());
        output.putBoolean("unblastable", this.unblastable);
    }
}
