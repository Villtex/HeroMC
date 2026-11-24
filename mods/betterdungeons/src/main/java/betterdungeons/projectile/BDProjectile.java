package betterdungeons.projectile;

import betterdungeons.BDEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * EntityBaseBall ("BDProjectile"): one entity for all magic balls, grenades, bullets, webs and hooks of Better
 * Dungeons; the {@link ProjectileType} (a synced byte) picks the {@link ProjectileBehavior}. Flies along its motion plus
 * an acceleration (friction 0.95, 0.8 in water), hits blocks and entities on its path every tick, is not saved.
 *
 * <p>How to fire one (items, mob AI of the mages, gunners, grenadiers, spiders, bosses), always on the server:
 * <pre>
 * BDProjectile.spawn(shooter, ProjectileType.BULLET_PISTOL);           // EntityBaseBall(world, shooter, type)
 * BDProjectile.spawn(shooter, ProjectileType.EARTHQUAKE, 1);           // ... (world, shooter, type, lvl)
 * BDProjectile.spawnAimed(shooter, dx, dy, dz, ProjectileType.VAMPIRE, 2); // ... (world, shooter, dx, dy, dz, type, lvl)
 * </pre>
 * The first forms start at the shooter's eyes along its look vector; {@code spawnAimed} starts at its feet and
 * accelerates towards (dx, dy, dz) with 1.5.2's Gaussian spread of 0.4. {@code create(...)} builds without spawning.
 */
public class BDProjectile extends Projectile {
    private static final EntityDataAccessor<Byte> DATA_TYPE = SynchedEntityData.defineId(BDProjectile.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_LEVEL = SynchedEntityData.defineId(BDProjectile.class, EntityDataSerializers.BYTE);

    public double accelerationX;
    public double accelerationY;
    public double accelerationZ;
    int ticksInAir;
    /** The capsule a thrown capsule carries (EntityBaseBall.item). */
    public ItemStack item = ItemStack.EMPTY;
    private boolean immuneToFire;
    private @Nullable ProjectileBehavior behavior;
    private float size = 1.0F;

    public BDProjectile(EntityType<? extends BDProjectile> type, Level level) {
        super(type, level);
    }

    // ---------------------------------------------------------------- factories (the 1.5.2 constructors)

    /** EntityBaseBall(world, shooter, type): from the eyes along the look vector, level 0. */
    public static BDProjectile create(LivingEntity shooter, ProjectileType type) {
        return create(shooter, type, ItemStack.EMPTY, 0);
    }

    /** EntityBaseBall(world, shooter, type, lvl). */
    public static BDProjectile create(LivingEntity shooter, ProjectileType type, int level) {
        return create(shooter, type, ItemStack.EMPTY, level);
    }

    /** EntityBaseBall(world, shooter, type, item, lvl): the hook shot passes its item. */
    public static BDProjectile create(LivingEntity shooter, ProjectileType type, ItemStack item, int level) {
        BDProjectile ball = new BDProjectile(BDEntities.PROJECTILE.get(), shooter.level());
        ball.setOwner(shooter);
        ball.snapTo(shooter.getX(), shooter.getEyeY(), shooter.getZ(), shooter.getYRot(), shooter.getXRot());
        Vec3 look = shooter.getLookAngle();
        ball.setDeltaMovement(look);
        ball.accelerationX = look.x * 0.1;
        ball.accelerationY = look.y * 0.1;
        ball.accelerationZ = look.z * 0.1;
        ball.setProjectileType(type.id);
        ball.setLvl(level);
        ball.item = item;
        ball.finishCreate();
        return ball;
    }

    /** EntityBaseBall(world, shooter, dx, dy, dz, type, lvl): from the shooter's feet, accelerating towards (dx, dy, dz). */
    public static BDProjectile createAimed(LivingEntity shooter, double dx, double dy, double dz, ProjectileType type, int level) {
        BDProjectile ball = new BDProjectile(BDEntities.PROJECTILE.get(), shooter.level());
        ball.setOwner(shooter);
        ball.snapTo(shooter.getX(), shooter.getY(), shooter.getZ(), shooter.getYRot(), shooter.getXRot());
        ball.setDeltaMovement(Vec3.ZERO);
        dx += ball.random.nextGaussian() * 0.4;
        dy += ball.random.nextGaussian() * 0.4;
        dz += ball.random.nextGaussian() * 0.4;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        ball.accelerationX = dx / length * 0.1;
        ball.accelerationY = dy / length * 0.1;
        ball.accelerationZ = dz / length * 0.1;
        ball.setProjectileType(type.id);
        ball.setLvl(level);
        ball.finishCreate();
        return ball;
    }

    public static BDProjectile spawn(LivingEntity shooter, ProjectileType type) {
        return add(create(shooter, type));
    }

    public static BDProjectile spawn(LivingEntity shooter, ProjectileType type, int level) {
        return add(create(shooter, type, level));
    }

    public static BDProjectile spawn(LivingEntity shooter, ProjectileType type, ItemStack item, int level) {
        return add(create(shooter, type, item, level));
    }

    public static BDProjectile spawnAimed(LivingEntity shooter, double dx, double dy, double dz, ProjectileType type, int level) {
        return add(createAimed(shooter, dx, dy, dz, type, level));
    }

    private static BDProjectile add(BDProjectile ball) {
        ball.level().addFreshEntity(ball);
        return ball;
    }

    private void finishCreate() {
        this.behavior = null;
        this.refreshDimensions();
        if (!this.level().isClientSide()) {
            this.behavior().onSpawn();
        }
    }

    // ---------------------------------------------------------------- data

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TYPE, (byte) 0);
        builder.define(DATA_LEVEL, (byte) 0);
    }

    public void setProjectileType(int type) {
        this.entityData.set(DATA_TYPE, (byte) type);
    }

    public int getProjectileType() {
        return this.entityData.get(DATA_TYPE);
    }

    public void setLvl(int level) {
        this.entityData.set(DATA_LEVEL, (byte) level);
    }

    /** EntityBaseBall.getlvl: damage or strength of some kinds, the hook shot's length (0..3). */
    public int getLvl() {
        return this.entityData.get(DATA_LEVEL);
    }

    /** getBallData: picked once, again only while it is still the dummy (the type arrives after the spawn packet). */
    public ProjectileBehavior behavior() {
        if (this.behavior == null || this.behavior instanceof Behaviors.Dummy) {
            this.behavior = ProjectileType.behavior(this);
            float s = this.behavior.getSize();
            if (s != this.size) {
                this.size = s;
                this.refreshDimensions();
            }
        }
        return this.behavior;
    }

    /**
     * A rider (the mob caught in a bubble) sits in the middle of the ball as BallRenderer draws it (from -0.25 to 0.75
     * of its size around the position), not on top of the box as today's default would put it.
     */
    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        return new Vec3(0.0, 0.25 * this.size - passenger.getBbHeight() / 2.0, 0.0);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(this.size, this.size);
    }

    public @Nullable LivingEntity shooter() {
        return this.getOwner() instanceof LivingEntity living ? living : null;
    }

    public void setImmuneToFire(boolean immune) {
        this.immuneToFire = immune;
    }

    @Override
    public boolean fireImmune() {
        return this.immuneToFire || super.fireImmune();
    }

    /** readEntityFromNBT killed the ball: they are never kept over a reload. */
    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("Type", (byte) this.getProjectileType());
        output.putByte("lvl", (byte) this.getLvl());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setProjectileType(input.getByteOr("Type", (byte) 0));
        this.setLvl(input.getByteOr("lvl", (byte) 0));
    }

    // ---------------------------------------------------------------- flight

    @Override
    public void tick() {
        LivingEntity shooter = this.shooter();
        if ((shooter == null || shooter.isAlive()) && this.level().isLoaded(this.blockPosition())) {
            super.tick();
            this.ticksInAir++;
            Vec3 pos = this.position();
            Vec3 motion = this.getDeltaMovement();
            Vec3 end = pos.add(motion);
            HitResult hit = this.level().clip(new ClipContext(pos, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this));
            if (hit.getType() != HitResult.Type.MISS) {
                end = hit.getLocation();
            }
            Entity hitEntity = null;
            double nearest = 0.0;
            for (Entity e : this.level().getEntities(this, this.getBoundingBox().expandTowards(motion).inflate(1.0))) {
                if (e.isPickable() && e.isAlive() && (e != shooter || this.ticksInAir >= 25)) {
                    Optional<Vec3> intercept = e.getBoundingBox().inflate(0.3).clip(pos, end);
                    if (intercept.isPresent()) {
                        double d = pos.distanceTo(intercept.get());
                        if (d < nearest || nearest == 0.0) {
                            hitEntity = e;
                            nearest = d;
                        }
                    }
                }
            }
            if (hitEntity != null) {
                hit = new EntityHitResult(hitEntity);
            }
            if (hit.getType() != HitResult.Type.MISS) {
                this.onImpact(hit);
            }
            motion = this.getDeltaMovement();
            double x = this.getX() + motion.x;
            double y = this.getY() + motion.y;
            double z = this.getZ() + motion.z;
            double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            this.setYRot((float) (Mth.atan2(motion.x, motion.z) * 180.0 / Math.PI));
            this.setXRot((float) (Mth.atan2(motion.y, horizontal) * 180.0 / Math.PI));
            float friction = 0.95F;
            if (this.isInWater()) {
                for (int i = 0; i < 4; i++) {
                    float f = 0.25F;
                    this.level().addParticle(ParticleTypes.BUBBLE, x - motion.x * f, y - motion.y * f, z - motion.z * f, motion.x, motion.y, motion.z);
                }
                friction = 0.8F;
            }
            this.setDeltaMovement(motion.add(this.accelerationX, this.accelerationY, this.accelerationZ).scale(friction));
            this.setPos(x, y, z);
            this.behavior().onUpdateInAir();
        } else if (!this.level().isClientSide()) {
            this.discard();
        }
        if (this.ticksInAir > this.behavior().getMaxLifeTime() && !this.level().isClientSide()) {
            this.discard();
        }
    }

    /** EntityBaseBall.onImpact: what the behaviour does (the block projectile has its own). */
    protected void onImpact(HitResult hit) {
        this.behavior().onImpact(hit);
    }

    /** Material.air/fire/plants/vine/web: what grenades and bullets fly through. */
    public static boolean passesThrough(BlockState state) {
        return state.isAir() || state.getBlock() instanceof BaseFireBlock || state.getBlock() instanceof VegetationBlock
                || state.getBlock() instanceof VineBlock || state.getBlock() instanceof WebBlock;
    }

    /** The block of a block hit; an entity hit counts as solid (1.5.2 looked at the block 0, 0, 0 then: bedrock). */
    public boolean hitsSolid(HitResult hit) {
        return !(hit instanceof BlockHitResult block) || !passesThrough(this.level().getBlockState(block.getBlockPos()));
    }

    /** attackEntityFrom: balls that can bounce fly off in the attacker's look direction and change sides. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        this.markHurt();
        Entity attacker = source.getEntity();
        if (attacker != null && this.behavior().canBounce()) {
            Vec3 look = attacker.getLookAngle();
            this.setDeltaMovement(look);
            this.accelerationX = look.x * 0.1;
            this.accelerationY = look.y * 0.1;
            this.accelerationZ = look.z * 0.1;
            if (attacker instanceof LivingEntity living) {
                this.setOwner(living);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0F;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.behavior != null) {
            this.behavior.setDead();
        }
        super.remove(reason);
    }
}
