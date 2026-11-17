package projectzulu.entity;

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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Set;

/**
 * The shared behaviour of the Project Zulu monsters (Batch D), all EntityGenericAnimal + IMob in the original.
 *
 * <p>An anger counter (angerLevel, counted down every AI tick) drives the AI state of EntityGenericAnimal.updateAIState:
 * angry with a target or attacker = attacking, angry without = looking, else idle. The state is synced (the models and
 * textures depend on it) and gates the nearest-player targeting (EntityAINearestAttackableTarget with a set of valid
 * states). A player's hit sets the anger to 400 (entityAttackedReaction). A successful melee hit starts the 20-tick
 * attack animation (animTime, synced by a packet in the original). Experience is always 5 (getExperiencePoints of an
 * IMob). Defaults of EntityGenericAnimal: health 20, damage 3/4/5/6 (peaceful/easy/normal/hard, normal value here,
 * vanilla scales against players itself), target distance 16. Hurt and death sound "damage.hurtflesh" of 1.5.2
 * EntityLiving.
 */
public abstract class ZuluMonster extends Monster {
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(ZuluMonster.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ANIM_TIME = SynchedEntityData.defineId(ZuluMonster.class, EntityDataSerializers.INT);
    /** maxAnimTime of EntityGenericAnimal. */
    public static final int MAX_ANIM_TIME = 20;

    /** EntityStates the monsters can reach (fleeing needs shouldPanic, which no monster has). */
    public enum State {
        IDLE, LOOKING, ATTACKING;

        private static final State[] VALUES = values();

        public static State byId(int id) {
            return id >= 0 && id < VALUES.length ? VALUES[id] : IDLE;
        }
    }

    public static final Set<State> ANGRY = EnumSet.of(State.ATTACKING, State.LOOKING);
    public static final Set<State> ALL_STATES = EnumSet.allOf(State.class);

    protected int angerLevel;

    protected ZuluMonster(EntityType<? extends ZuluMonster> type, Level level) {
        super(type, level);
        this.xpReward = 5;
        if (this.avoidsWater()) {
            this.setPathfindingMalus(PathType.WATER, -1.0F);
        }
    }

    /** Defaults of EntityGenericAnimal. */
    public static AttributeSupplier.Builder createZuluAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    /** getNavigator().setAvoidsWater(true) of the original. */
    protected boolean avoidsWater() {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATE, State.IDLE.ordinal());
        builder.define(ANIM_TIME, 0);
    }

    public State getState() {
        return State.byId(this.entityData.get(STATE));
    }

    public int getAnimTime() {
        return this.entityData.get(ANIM_TIME);
    }

    public int getAngerLevel() {
        return this.angerLevel;
    }

    public void setAngerLevel(int angerLevel) {
        this.angerLevel = angerLevel;
    }

    /** updateAITick of the original (runs inside the AI update, before the counters go down). */
    protected void updateAITick(ServerLevel level) {
    }

    /** EntityGenericAnimal.updateAIState. */
    protected State computeState() {
        if (this.angerLevel <= 0 || this.getTarget() == null && this.getLastHurtByMob() == null) {
            return this.angerLevel > 0 ? State.LOOKING : State.IDLE;
        }
        return State.ATTACKING;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        this.updateAITick(level);
        super.customServerAiStep(level);
        this.angerLevel = Math.max(this.angerLevel - 1, 0);
        this.entityData.set(STATE, this.computeState().ordinal());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide() && this.getAnimTime() > 0) {
            this.entityData.set(ANIM_TIME, this.getAnimTime() - 1);
        }
    }

    // ---- reactions (EntityGenericAnimal.attackEntityFrom / attackEntityAsMob) ----

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (super.hurtServer(level, source, damage)) {
            if (source.getEntity() instanceof Player player) {
                this.attackedByPlayer(player);
            }
            return true;
        }
        return false;
    }

    /** entityAttackedReaction of the original. */
    protected void attackedByPlayer(Player player) {
        this.angerLevel = 400;
    }

    /** Only hits when the bounding boxes overlap vertically; starts the attack animation. */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (target.getBoundingBox().maxY > this.getBoundingBox().minY && target.getBoundingBox().minY < this.getBoundingBox().maxY) {
            this.entityData.set(ANIM_TIME, MAX_ANIM_TIME);
            return super.doHurtTarget(level, target);
        }
        return false;
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel level) {
        return 5;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_HURT;
    }

    /** setInWeb() emptied / isInWeb = false (mummy, minotaur): cobwebs do not slow these. */
    protected boolean ignoresCobwebs() {
        return false;
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 speedMultiplier) {
        if (!(this.ignoresCobwebs() && state.is(Blocks.COBWEB))) {
            super.makeStuckInBlock(state, speedMultiplier);
        }
    }

    // ---- shared AI tasks ----

    /** EntityAINearestAttackableTarget: only in the given states, keeps the target only while attacking/looking. */
    public static class StateTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        private final ZuluMonster monster;
        private final Set<State> validStates;

        public StateTargetGoal(ZuluMonster monster, Class<T> targetType, Set<State> validStates) {
            super(monster, targetType, 0, true, false, null);
            this.monster = monster;
            this.validStates = validStates;
        }

        @Override
        public boolean canUse() {
            return this.validStates.contains(this.monster.getState()) && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return ANGRY.contains(this.monster.getState()) && super.canContinueToUse();
        }
    }

    /** EntityAIStayStill: on the ground and out of water in the triggering state, the monster does not move. */
    public static class StayStillGoal extends Goal {
        private final ZuluMonster monster;
        private final State triggeringState;

        public StayStillGoal(ZuluMonster monster, State triggeringState) {
            this.monster = monster;
            this.triggeringState = triggeringState;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return !this.monster.isInWater() && this.monster.onGround() && this.monster.getState() == this.triggeringState;
        }

        @Override
        public void start() {
            this.monster.getNavigation().stop();
        }
    }

    /**
     * EntityAIAttackOnCollide with an explicit attackDistanceSq (minotaur 5.29, centipede 6.25): measured from the
     * centre to the target's feet instead of vanilla's bounding-box reach.
     */
    public static class ReachAttackGoal extends MeleeAttackGoal {
        private final double reachSq;

        public ReachAttackGoal(ZuluMonster monster, boolean continuous, double reachSq) {
            super(monster, 1.0, continuous);
            this.reachSq = reachSq;
        }

        @Override
        protected boolean canPerformAttack(LivingEntity target) {
            return this.isTimeToAttack() && this.mob.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ()) <= this.reachSq;
        }
    }
}
