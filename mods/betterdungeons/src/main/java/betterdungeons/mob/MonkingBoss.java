package betterdungeons.mob;

import betterdungeons.BDFx;
import betterdungeons.BDSounds;
import betterdungeons.mob.ai.BossAttackGoal;
import betterdungeons.projectile.BDBlockProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

/**
 * EntityGiant ("Monking", the gorilla king of the jungle caves): 1500 health, regenerates 1 every 100 ticks, hits for
 * 7, speed 0.3, 20 xp, no knockback, no fall damage. Attacks other monsters (IMob) and whatever hurts it.
 *
 * <p>Attacks ({@link #attackEntity}, squared distance d): closer than 8 it winds up a 30 tick blow (watcher 16 the
 * type, 18 the time): 0/1 right/left arm smash, 4/5 right/left upper cut (depending on which side the target faces),
 * below a third of its health 2 (both smash) or 6 (both launch). Further than 50: 1 in 10 per tick (if standing on a
 * block) it tears the block out under itself and throws it (three at low health; type 7), else 1 in 15 it jumps at the
 * target (type 3) and body-slams where it lands. A hit from behind (attacker looking within 50 degrees of its own
 * direction) does ten times the damage and makes it jump (below a third of its health into a body slam). While an
 * attack winds up the AI is off.
 */
public class MonkingBoss extends BDBoss {
    private static final EntityDataAccessor<Byte> DATA_ATTACK_TYPE = SynchedEntityData.defineId(MonkingBoss.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_ATTACK_TIME = SynchedEntityData.defineId(MonkingBoss.class, EntityDataSerializers.INT);
    /** Block.leaves (id 18, all four kinds) that the arm smash clears. */
    private static final Set<Block> LEAVES = Set.of(Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES);

    public int totalAttackAnimTime = 30;

    public MonkingBoss(EntityType<? extends MonkingBoss> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH, 1500.0).add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FOLLOW_RANGE, 16.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    /** addAITasks. */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BossAttackGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 0, true, false, (t, l) -> t instanceof Enemy));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACK_TYPE, (byte) 0);
        builder.define(DATA_ATTACK_TIME, 0);
    }

    // ---------------------------------------------------------------- state (EntityGiant compares the whole byte 17 with 1)

    @Override
    public boolean isAttacking() {
        return this.getAnimByte() == 1;
    }

    @Override
    public void setAttacking(boolean attacking) {
        this.setAnimByte((byte) (attacking ? 1 : 0));
    }

    public int getAttackAnimTime() {
        return this.entityData.get(DATA_ATTACK_TIME);
    }

    public void setAttackAnimTime(int time) {
        this.entityData.set(DATA_ATTACK_TIME, time);
    }

    public byte getAttackType() {
        return this.entityData.get(DATA_ATTACK_TYPE);
    }

    public void setAttackType(byte type) {
        this.entityData.set(DATA_ATTACK_TYPE, type);
    }

    @Override
    protected boolean isAIEnabled() {
        return !this.isAttacking();
    }

    // ---------------------------------------------------------------- onLivingUpdate

    @Override
    public void aiStep() {
        if (this.level().isClientSide()) {
            this.clientParticles();
        } else {
            this.serverUpdate();
        }
        super.aiStep();
    }

    /** The smoke from its back below a third of its health (client side in 1.5.2, with the client's random). */
    private void clientParticles() {
        if (this.getHealth() < (int) this.getMaxHealth() / 3 && this.random.nextInt(50) == 0) {
            double ry = Math.toRadians(this.getYRot() - 180.0F);
            double x = -Math.sin(ry);
            double z = Math.cos(ry);
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(ParticleTypes.POOF, this.getX() + x, this.getY() + 2.0, this.getZ() + z,
                        x / 10.0 + (this.random.nextFloat() - 0.5) / 10.0, 0.0, z / 10.0 + (this.random.nextFloat() - 0.5) / 10.0);
            }
            for (int i = 0; i < 6; i++) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getX() + x, this.getY() + 2.0, this.getZ() + z,
                        x / 10.0 + (this.random.nextFloat() - 0.5) / 10.0, 0.0, z / 10.0 + (this.random.nextFloat() - 0.5) / 10.0);
            }
        }
    }

    private void serverUpdate() {
        if (this.tickCount % 100 == 0 && this.getHealth() < this.getMaxHealth()) {
            this.setHealth(this.getHealth() + 1.0F);
        }
        if (this.isAttacking()) {
            this.setAttackAnimTime(this.getAttackAnimTime() + 1);
            if (this.getAttackAnimTime() > this.totalAttackAnimTime) {
                this.setAttacking(false);
                switch (this.getAttackType()) {
                    case 0 -> this.armSmash(true);
                    case 1 -> this.armSmash(false);
                    case 2 -> {
                        this.armSmash(true);
                        this.armSmash(false);
                    }
                    case 4 -> this.armLaunch(true);
                    case 5 -> this.armLaunch(false);
                    case 6 -> {
                        this.armLaunch(true);
                        this.armLaunch(false);
                    }
                    case 7, 8 -> {
                        this.rockLaunch();
                        this.setAttacking(true);
                        this.setAttackAnimTime(0);
                        this.setAttackType((byte) -1);
                    }
                    default -> {
                    }
                }
            }
            if (this.getAttackType() == 3 && this.onGround()) {
                this.bodySmash();
            }
        }
    }

    protected boolean lowHealth() {
        return this.getHealth() < (int) this.getMaxHealth() / 3;
    }

    // ---------------------------------------------------------------- attacks

    /** attackEntityAsMob on every living entity in the box, ignoring the boss itself. */
    private void hitAll(AABB box, java.util.function.Consumer<LivingEntity> after) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        List<Entity> list = level.getEntities(this, box);
        for (Entity e : list) {
            if (e instanceof LivingEntity living) {
                this.doHurtTarget(level, living);
                after.accept(living);
            }
        }
    }

    protected void bodySmash() {
        this.setAttacking(false);
        for (int i = 0; i < 3; i++) {
            BDFx.particle(this.level(), ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        }
        this.hitAll(new AABB(this.getX() - 2.0, this.getY() - 1.0, this.getZ() - 2.0, this.getX() + 3.0, this.getY() + 2.0, this.getZ() + 2.0), e -> {
        });
    }

    protected void armSmash(boolean arm) {
        int effect = arm ? 20 : -20;
        double ry = Math.toRadians(this.getYRot() + effect);
        double x = this.getX() + -Math.sin(ry) * 2.0;
        double z = this.getZ() + Math.cos(ry) * 2.0;
        for (int i = 0; i < 3; i++) {
            BDFx.particle(this.level(), ParticleTypes.EXPLOSION, x, this.getY() + 1.0, z, 0.0, 0.0, 0.0);
        }
        for (int i = -1; i < 2; i++) {
            int bx = (int) x;
            int by = (int) this.getY() + i;
            int bz = (int) z;
            this.clearLeaves(bx, by, bz);
            this.clearLeaves(bx, by, bz + 1);
            this.clearLeaves(bx + 1, by, bz);
            this.clearLeaves(bx, by, bz - 1);
            this.clearLeaves(bx - 1, by, bz);
            this.hitAll(new AABB(x - 2.0, this.getY() - 1.0, z - 2.0, x + 2.0, this.getY() + 3.0, z + 2.0), e -> {
                e.setPos(e.getX(), e.getY() - 0.2, e.getZ());
                Vec3 m = e.getDeltaMovement();
                e.setDeltaMovement(m.x, 0.0, m.z);
                e.needsSync = true;
            });
        }
    }

    private void clearLeaves(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (LEAVES.contains(this.level().getBlockState(pos).getBlock())) {
            this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 0);
        }
    }

    protected void armLaunch(boolean arm) {
        int effect = arm ? 20 : -20;
        double ry = Math.toRadians(this.getYRot() + effect);
        double x = this.getX() + -Math.sin(ry) * 2.0;
        double z = this.getZ() + Math.cos(ry) * 2.0;
        for (int i = 0; i < 3; i++) {
            BDFx.particle(this.level(), ParticleTypes.POOF, x, this.getY() + 1.0, z, 0.0, 0.0, 0.0);
        }
        for (int i = -1; i < 2; i++) {
            this.hitAll(new AABB(x - 2.0, this.getY() - 1.0, z - 2.0, x + 2.0, this.getY() + 3.0, z + 2.0), e -> {
                Vec3 m = e.getDeltaMovement();
                if (m.y < 1.5) {
                    e.setDeltaMovement(m.x, m.y + 0.4F, m.z);
                    e.needsSync = true;
                }
            });
        }
    }

    /** RockLaunch: tears out the block under its feet and throws it (Block.blocksList[id]: without its metadata). */
    protected void rockLaunch() {
        BlockPos under = new BlockPos((int) this.getX(), (int) this.getY() - 1, (int) this.getZ());
        BlockState state = this.level().getBlockState(under);
        if (state.isAir() || !(this.level() instanceof ServerLevel level)) {
            return;
        }
        level.setBlock(under, Blocks.AIR.defaultBlockState(), 0);
        Entity e = this.getTarget();
        if (e == null) {
            return;
        }
        double d = this.distanceToSqr(e.getX(), e.getBoundingBox().minY, e.getZ());
        double ry = Math.atan2(this.getX() - e.getX(), this.getZ() - e.getZ());
        double x = -Math.sin(ry);
        double z = -Math.cos(ry);
        double y = d / 100.0;
        BlockState thrown = state.getBlock().defaultBlockState();
        if (this.getHealth() > (int) this.getMaxHealth() / 3) {
            level.addFreshEntity(BDBlockProjectile.create(this, x, y, z, thrown));
        } else {
            for (int i = 0; i < 3; i++) {
                level.addFreshEntity(BDBlockProjectile.create(this, x + (this.random.nextFloat() - 0.5) / 6.0, y + (this.random.nextFloat() - 0.5) / 20.0,
                        z + (this.random.nextFloat() - 0.5) / 6.0, thrown));
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        Entity entity = source.getEntity();
        if (entity != null && this.isAssWeakPoint()) {
            float angle = norm360(this.getYRot() - entity.getYRot());
            angle = Math.abs(angle - 180.0F);
            if (angle > 130.0F) {
                if (this.onGround()) {
                    Vec3 m = this.getDeltaMovement();
                    this.setDeltaMovement(m.x, 1.2, m.z);
                    if (this.lowHealth()) {
                        this.setAttacking(true);
                        this.setAttackType((byte) 3);
                        this.setAttackAnimTime(-9000);
                        this.setOnGround(false);
                    }
                }
                damage *= 10.0F;
            }
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public void attackEntity(Entity entity, float f) {
        if (this.isAttacking()) {
            return;
        }
        if (f < 8.0F) {
            this.startMelee(entity);
        } else if (f > 50.0F) {
            BlockPos under = new BlockPos((int) this.getX(), (int) this.getY() - 1, (int) this.getZ());
            if (!this.level().getBlockState(under).isAir() && this.random.nextInt(10) == 0) {
                this.setAttacking(true);
                this.setAttackAnimTime(0);
                this.setAttackType((byte) 7);
            } else if (this.random.nextInt(15) == 0) {
                double ry = Math.atan2(this.getX() - entity.getX(), this.getZ() - entity.getZ());
                Vec3 m = this.getDeltaMovement();
                this.setDeltaMovement(-Math.sin(ry), m.y + 0.6, -Math.cos(ry));
                this.setAttacking(true);
                this.setAttackType((byte) 3);
                this.setAttackAnimTime(-9000);
                this.setOnGround(false);
            }
        }
    }

    /** The close attacks (the same for the golem boss). */
    protected void startMelee(Entity entity) {
        this.setAttacking(true);
        this.setAttackAnimTime(0);
        double angle = norm360((double) (this.getYRot() - entity.getYRot()));
        boolean b = this.random.nextBoolean();
        if (this.lowHealth() && this.random.nextBoolean()) {
            this.setAttackType((byte) (b ? 2 : 6));
            return;
        }
        if (angle < 180.0) {
            this.setAttackType((byte) (b ? 0 : 4));
        } else {
            this.setAttackType((byte) (b ? 1 : 5));
        }
    }

    public boolean isAssWeakPoint() {
        return true;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource source) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_DRAGON_GROWL;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return BDSounds.BREATH_LOUD.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return BDSounds.BREATH_DOUBLE.get();
    }
}
