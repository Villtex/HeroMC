package projectzulu.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The flying base of the Project Zulu birds: EntityAerial + EntityGenericCreature + EntityGenericAnimal of 1.5.2,
 * reduced to what the birds use (no taming, riding or breeding: none of the birds has a breeding or taming item).
 *
 * <p>Flight: while not "grounded" the bird ignores gravity; every server tick (updateAITasks of EntityAerial, before
 * the AI runs) its motion is pulled towards {@link #targetPosition} with signum deltas (horizontal 0.5, vertical 0.7,
 * factor 0.03) and it turns to its flight direction, then {@link #travel} moves it with the 1.5.2 flying friction (0.91
 * in the air). The target point is set by the goals ({@link AerialGoals}) and is never cleared when a goal stops, so
 * a bird that reached its point keeps hovering around it, as in the original. Grounded birds (only the pelican lands)
 * walk with gravity and the path finder.
 *
 * <p>The AI state (EntityStates: idle, looking, attacking, following, fleeing, posture) drives the models; it is
 * recomputed every server tick and synced together with the grounded flag. Anger (angerLevel, 400 ticks after being
 * hit by a player) and fleeing (fleeingTick, birds with shouldPanic) are plain counters as in the original.
 *
 * <p>Base class: PathfinderMob instead of Animal. The birds cannot breed, an Animal would get baby spawns from the
 * AgeableMob group data and never despawn; all birds have "Should Despawn"=true in the pack.
 */
public abstract class AerialAnimal extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> GROUNDED = SynchedEntityData.defineId(AerialAnimal.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(AerialAnimal.class, EntityDataSerializers.INT);

    /** EntityStates of the original (only the states the birds can reach have a meaning here). */
    public enum EntityState {
        IDLE, LOOKING, ATTACKING, FLEEING, FOLLOWING, IN_LOVE, SITTING, STAY_STILL, POSTURE, IN_COVER;

        private static final EntityState[] VALUES = values();

        public static EntityState byId(int id) {
            return id >= 0 && id < VALUES.length ? VALUES[id] : IDLE;
        }
    }

    protected @Nullable BlockPos targetPosition;
    protected int maxFlightHeight = 20;
    protected int fleeingTick;
    protected int angerLevel;
    /** shouldFollow of EntityGenericTameable; only the vulture sets it (curiosity). */
    protected boolean shouldFollow;

    protected AerialAnimal(EntityType<? extends AerialAnimal> type, Level level) {
        super(type, level);
        this.xpReward = 3;
        // getNavigator().setAvoidsWater(true)
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    /** Defaults of EntityGenericAnimal: health 20, follow range 16 (target distance of the AI). */
    public static AttributeSupplier.Builder createAerialAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                // getAttackStrength of EntityGenericAnimal: 3/4/5/6 (peaceful/easy/normal/hard), normal value;
                // vanilla scales the damage against players by difficulty itself
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(GROUNDED, this.defaultGrounded());
        builder.define(STATE, EntityState.IDLE.ordinal());
    }

    /** All Project Zulu birds start in the air. */
    public boolean defaultGrounded() {
        return false;
    }

    public boolean isEntityGrounded() {
        return this.entityData.get(GROUNDED);
    }

    protected void setEntityGrounded(boolean grounded) {
        this.entityData.set(GROUNDED, grounded);
    }

    public EntityState getEntityState() {
        return EntityState.byId(this.entityData.get(STATE));
    }

    protected void setEntityState(EntityState state) {
        this.entityData.set(STATE, state.ordinal());
    }

    public int getMaxFlightHeight() {
        return this.maxFlightHeight;
    }

    public int getAngerLevel() {
        return this.angerLevel;
    }

    public void setAngerLevel(int angerLevel) {
        this.angerLevel = angerLevel;
    }

    // ---- target point (EntityAerial) ----

    public boolean setTargetPosition(@Nullable BlockPos pos) {
        if (this.isTargetPositionValid(pos)) {
            this.targetPosition = pos;
            return true;
        }
        return false;
    }

    public boolean isTargetPositionValid() {
        return this.isTargetPositionValid(this.targetPosition);
    }

    /**
     * Air, not water, and at least 3 above the bottom of the world (1.5.2: posY >= 3, the world started at 0; the
     * water check of the original is redundant next to isAirBlock but kept).
     */
    public boolean isTargetPositionValid(@Nullable BlockPos pos) {
        return pos == null || this.level().isEmptyBlock(pos) && pos.getY() >= this.level().getMinY() + 3
                && !this.level().getFluidState(pos).is(Fluids.WATER);
    }

    /** getDistance to the block corner (no +0.5) below 2. */
    public boolean atTargetPosition() {
        BlockPos pos = this.targetPosition;
        return pos == null || Math.sqrt(this.distanceToSqr(pos.getX(), pos.getY(), pos.getZ())) < 2.0;
    }

    /**
     * RandomPositionGenerator.flyRandomlyTowardHeightLevel: one random point within xz +-range, vertically biased
     * towards heightLevel above the precipitation height (the block path weight of the birds is 0 everywhere, so
     * the first of the ten tries always wins). The birds have no home restriction.
     */
    public BlockPos randomFlightTarget(int range, int vertical, int heightLevel) {
        int dx = this.random.nextInt(2 * range) - range;
        double ground = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING, (int) this.getX(), (int) this.getZ());
        int dy;
        if (this.getY() > ground + heightLevel * 1.25) {
            dy = this.random.nextInt(2 * vertical) - vertical * 3 / 2;
        } else if (this.getY() < ground + heightLevel) {
            dy = this.random.nextInt(2 * vertical) - vertical / 2;
        } else {
            dy = this.random.nextInt(2 * vertical) - vertical;
        }
        int dz = this.random.nextInt(2 * range) - range;
        return new BlockPos(Mth.floor(this.getX()) + dx, Mth.floor(this.getY()) + dy, Mth.floor(this.getZ()) + dz);
    }

    // ---- AI state (EntityGenericAnimal.updateAIState) ----

    protected EntityState computeEntityState() {
        if (this.fleeingTick > 0) {
            return EntityState.FLEEING;
        }
        if (this.angerLevel <= 0 || this.getTarget() == null && this.getLastHurtByMob() == null) {
            if (this.angerLevel > 0) {
                return EntityState.LOOKING;
            }
            return this.shouldFollow ? EntityState.FOLLOWING : EntityState.IDLE;
        }
        return EntityState.ATTACKING;
    }

    /** Server-side work of updateAITasks that ran before the AI tasks: state update and flight steering. */
    protected void updateBeforeAi() {
        this.setEntityState(this.computeEntityState());
        if (!this.isEntityGrounded() && this.targetPosition != null) {
            Vec3 motion = this.getDeltaMovement();
            double dx = this.targetPosition.getX() + 0.5 - this.getX();
            double dy = this.targetPosition.getY() + 0.1 - this.getY();
            double dz = this.targetPosition.getZ() + 0.5 - this.getZ();
            double mx = motion.x + (Math.signum(dx) * 0.5 - motion.x) * 0.1F * 0.3;
            double my = motion.y + (Math.signum(dy) * 0.7F - motion.y) * 0.1F * 0.3;
            double mz = motion.z + (Math.signum(dz) * 0.5 - motion.z) * 0.1F * 0.3;
            this.setDeltaMovement(mx, my, mz);
            float yaw = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0F;
            this.setYRot(this.getYRot() + Mth.wrapDegrees(yaw - this.getYRot()));
            // moveForward = 0.5F of the original was reset by the move helper in the same tick
        }
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide()) {
            this.updateBeforeAi();
        }
        super.aiStep();
    }

    /** The counters of EntityGenericCreature.updateAITasks (after the AI tasks). */
    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.fleeingTick = Math.max(this.fleeingTick - 1, 0);
        this.angerLevel = Math.max(this.angerLevel - 1, 0);
    }

    // ---- movement (EntityAerial.moveEntityWithHeading) ----

    @Override
    public void travel(Vec3 input) {
        if (this.isEntityGrounded()) {
            super.travel(input);
            return;
        }
        if (this.isInWater()) {
            this.moveRelative(0.02F, input);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8F));
        } else if (this.isInLava()) {
            this.moveRelative(0.02F, input);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
        } else {
            float friction = this.groundFriction();
            float groundFactor = 0.16277136F / (friction * friction * friction);
            this.moveRelative(this.onGround() ? 0.1F * groundFactor : 0.02F, input);
            friction = this.groundFriction();
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(friction));
        }
    }

    private float groundFriction() {
        if (this.onGround()) {
            BlockState below = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement());
            return below.isAir() ? 0.54600006F : below.getBlock().getFriction() * 0.91F;
        }
        return 0.91F;
    }

    // ---- falling: fall() is empty in every bird ----

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
        return false;
    }

    /** updateFallState was emptied too by every bird except the vulture. */
    protected boolean keepsFallState() {
        return false;
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
        if (this.keepsFallState()) {
            super.checkFallDamage(ya, onGround, onState, pos);
        } else {
            this.resetFallDistance();
        }
    }

    // ---- reactions (EntityGenericAnimal.attackEntityFrom / attackEntityAsMob) ----

    /** shouldPanic: flee 80 ticks instead of getting angry for 400. */
    protected boolean shouldPanic() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (super.hurtServer(level, source, damage)) {
            if (source.getEntity() instanceof Player) {
                if (this.shouldPanic()) {
                    this.fleeingTick = 80;
                } else {
                    this.angerLevel = 400;
                }
            }
            return true;
        }
        return false;
    }

    /** Only hits when the bounding boxes overlap vertically. */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (target.getBoundingBox().maxY > this.getBoundingBox().minY && target.getBoundingBox().minY < this.getBoundingBox().maxY) {
            return super.doHurtTarget(level, target);
        }
        return false;
    }

    /** getExperiencePoints: 5 while angry, else 1-2. */
    @Override
    protected int getBaseExperienceReward(ServerLevel level) {
        EntityState state = this.getEntityState();
        return state == EntityState.ATTACKING || state == EntityState.LOOKING ? 5 : this.random.nextInt(2) + 1;
    }

    /** 1.5.2 EntityLiving.getDeathSound: "damage.hurtflesh". */
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_HURT;
    }

    // ---- NBT ----

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("IsGrounded", this.isEntityGrounded());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setEntityGrounded(input.getBooleanOr("IsGrounded", this.defaultGrounded()));
    }
}
