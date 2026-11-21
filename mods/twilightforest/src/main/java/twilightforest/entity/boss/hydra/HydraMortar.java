package twilightforest.entity.boss.hydra;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;

/**
 * EntityTFHydraMortar ("tfhydramortar", mod entity 5, tracking 150, every 3 ticks, with velocity): an EntityThrowable
 * of 0.75 x 0.75, shot at 0.75 with 20 degrees more pitch, gravity 0.05, always burning. On a block it stops falling
 * and rolls (motion × 0.9 per tick) until its fuse of 80 ticks runs out; an entity hit (or a mega blast, when the
 * target was out of sight) sets it off at once: an explosion of 0.1 (4 for the mega blast, which goes through
 * everything but bedrock and the end portal at 0.8 resistance), then 18 fireball damage and 5 s burning for all
 * within 1 block. A hit sends it away along the attacker's look (+1 up, 1.5 fast) and adds 20 to the fuse; the
 * attacker then counts as the thrower. The flight is EntityThrowable.onUpdate of 1.5.2 (no block collision, only a
 * ray trace per tick), not today's ThrowableProjectile.
 */
public class HydraMortar extends Projectile {
    private static final int BURN_FACTOR = 5;
    private static final int DIRECT_DAMAGE = 18;
    private static final float GRAVITY = 0.05F;

    public @Nullable LivingEntity playerReflects;
    public int fuse = 80;
    public boolean megaBlast;
    /** onGround of 1.5.2: set by the impact, cleared only when the mortar is hit. */
    private boolean grounded;
    private int ticksInAir;

    public HydraMortar(EntityType<? extends HydraMortar> type, Level level) {
        super(type, level);
    }

    /** EntityThrowable(world, thrower): from the thrower's eyes along its look, 20 degrees higher, at 0.75. */
    public HydraMortar(EntityType<? extends HydraMortar> type, Level level, LivingEntity thrower) {
        this(type, level);
        this.setOwner(thrower);
        float yaw = thrower.getYRot();
        float pitch = thrower.getXRot();
        double x = thrower.getX() - Mth.cos(yaw / 180.0F * (float) Math.PI) * 0.16F;
        double y = thrower.getEyeY() - 0.1;
        double z = thrower.getZ() - Mth.sin(yaw / 180.0F * (float) Math.PI) * 0.16F;
        this.snapTo(x, y, z, yaw, pitch);
        float f = 0.4F;
        double mx = -Mth.sin(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI) * f;
        double mz = Mth.cos(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI) * f;
        double my = -Mth.sin((pitch - 20.0F) / 180.0F * (float) Math.PI) * f;
        this.setThrowableHeading(mx, my, mz, 0.75F, 1.0F);
    }

    /** EntityThrowable.setThrowableHeading of 1.5.2 (spread 0.0075 per unit of inaccuracy). */
    public void setThrowableHeading(double x, double y, double z, float velocity, float inaccuracy) {
        double length = Math.sqrt(x * x + y * y + z * z);
        x /= length;
        y /= length;
        z /= length;
        x += this.random.nextGaussian() * 0.0075 * inaccuracy;
        y += this.random.nextGaussian() * 0.0075 * inaccuracy;
        z += this.random.nextGaussian() * 0.0075 * inaccuracy;
        x *= velocity;
        y *= velocity;
        z *= velocity;
        this.setDeltaMovement(x, y, z);
        double horizontal = Math.sqrt(x * x + z * z);
        this.setYRot((float) (Math.atan2(x, z) * 180.0 / Math.PI));
        this.setXRot((float) (Math.atan2(y, horizontal) * 180.0 / Math.PI));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public void setToBlasting() {
        this.megaBlast = true;
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        this.ticksInAir++;
        Vec3 motion = this.getDeltaMovement();
        Vec3 from = this.position();
        Vec3 to = from.add(motion);
        HitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this));
        if (hit.getType() != HitResult.Type.MISS) {
            to = hit.getLocation();
        } else {
            hit = null;
        }
        if (!level.isClientSide()) {
            Entity hitEntity = null;
            double nearest = 0.0;
            Entity thrower = this.getThrower();
            for (Entity entity : level.getEntities(this, this.getBoundingBox().expandTowards(motion).inflate(1.0))) {
                if (entity.isPickable() && (entity != thrower || this.ticksInAir >= 5)) {
                    AABB box = entity.getBoundingBox().inflate(0.3);
                    Optional<Vec3> intercept = box.clip(from, to);
                    if (intercept.isPresent()) {
                        double dist = from.distanceTo(intercept.get());
                        if (dist < nearest || nearest == 0.0) {
                            hitEntity = entity;
                            nearest = dist;
                        }
                    }
                }
            }
            if (hitEntity != null) {
                this.onImpact(null, true);
            } else if (hit != null) {
                this.onImpact((BlockHitResult) hit, false);
            }
        } else if (hit != null) {
            this.onImpact((BlockHitResult) hit, false);
        }
        if (this.isRemoved()) {
            return;
        }
        motion = this.getDeltaMovement();
        double x = this.getX() + motion.x;
        double y = this.getY() + motion.y;
        double z = this.getZ() + motion.z;
        this.updateRotation();
        float drag = 0.99F;
        if (this.isInWater()) {
            for (int i = 0; i < 4; i++) {
                level.addParticle(ParticleTypes.BUBBLE, x - motion.x * 0.25, y - motion.y * 0.25, z - motion.z * 0.25, motion.x, motion.y, motion.z);
            }
            drag = 0.8F;
        }
        this.setDeltaMovement(motion.x * drag, motion.y * drag - GRAVITY, motion.z * drag);
        this.setPos(x, y, z);

        // EntityTFHydraMortar.onUpdate
        this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
        if (this.grounded) {
            if (!level.isClientSide()) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
            }
            if (this.fuse-- <= 0 && level instanceof ServerLevel server) {
                this.detonate(server);
            }
        }
    }

    private void onImpact(@Nullable BlockHitResult block, boolean entity) {
        if (!entity && !this.megaBlast) {
            this.setDeltaMovement(this.getDeltaMovement().x, 0.0, this.getDeltaMovement().z);
            this.grounded = true;
        } else if (this.level() instanceof ServerLevel server) {
            this.detonate(server);
        }
    }

    @Override
    public float getBlockExplosionResistance(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, FluidState fluid, float resistance) {
        float r = super.getBlockExplosionResistance(explosion, level, pos, state, fluid, resistance);
        if (this.megaBlast && !state.is(Blocks.BEDROCK) && !state.is(Blocks.END_PORTAL) && !state.is(Blocks.END_PORTAL_FRAME)) {
            r = Math.min(0.8F, r);
        }
        return r;
    }

    protected void detonate(ServerLevel level) {
        float explosionPower = this.megaBlast ? 4.0F : 0.1F;
        level.explode(this, this.getX(), this.getY(), this.getZ(), explosionPower, true, Level.ExplosionInteraction.MOB);
        DamageSource source = level.damageSources().source(DamageTypes.FIREBALL, null, this.getThrower());
        for (Entity nearby : new ArrayList<>(level.getEntities(this, this.getBoundingBox().inflate(1.0)))) {
            if (nearby.hurtServer(level, source, DIRECT_DAMAGE) && !nearby.fireImmune()) {
                nearby.igniteForSeconds(BURN_FACTOR);
            }
        }
        this.discard();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        this.markHurt();
        Entity attacker = source.getEntity();
        if (attacker != null) {
            Vec3 look = attacker.getLookAngle();
            this.setThrowableHeading(look.x, look.y + 1.0, look.z, 1.5F, 0.1F);
            this.grounded = false;
            this.fuse += 20;
            if (attacker instanceof LivingEntity living) {
                this.playerReflects = living;
            }
            return true;
        }
        return false;
    }

    public @Nullable Entity getThrower() {
        return this.playerReflects != null ? this.playerReflects : this.getOwner();
    }

    @Override
    public boolean displayFireAnimation() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 1.5F;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Fuse", this.fuse);
        output.putBoolean("MegaBlast", this.megaBlast);
        output.putBoolean("Grounded", this.grounded);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.fuse = input.getIntOr("Fuse", 80);
        this.megaBlast = input.getBooleanOr("MegaBlast", false);
        this.grounded = input.getBooleanOr("Grounded", false);
    }
}
