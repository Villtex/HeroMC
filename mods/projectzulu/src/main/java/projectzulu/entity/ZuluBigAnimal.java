package projectzulu.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

/**
 * The Batch C big animals (bears, gorilla, rhino, alligator, giraffe, elephant, mammoth, tree ent) on top of the
 * Batch B {@link ZuluLandAnimal}. Adds what their models and AI need from EntityGenericAnimal:
 * <ul>
 * <li>animTime (maxAnimTime 20): set on every landed hit, counted down each tick, synced for the attack swings;</li>
 * <li>the entity state of updateAIState (idle, looking, attacking, fleeing, in love), synced for the models;</li>
 * <li>attackEntityAsMob only hit when the bounding boxes overlapped vertically;</li>
 * <li>EntityAIAttackOnCollide with its squared reach (default (2 x width)^2, measured to the target's feet, no
 * line-of-sight check) instead of vanilla's bounding-box reach, which is far shorter for these big bodies;</li>
 * <li>the reduced knockBack override of several of them (horizontal 0.08, fixed lift).</li>
 * </ul>
 */
public abstract class ZuluBigAnimal extends ZuluLandAnimal {
    public static final int MAX_ANIM_TIME = 20;
    public static final int STATE_IDLE = 0;
    public static final int STATE_LOOKING = 1;
    public static final int STATE_ATTACKING = 2;
    public static final int STATE_FLEEING = 3;
    public static final int STATE_IN_LOVE = 4;

    private static final EntityDataAccessor<Integer> ANIM_TIME = SynchedEntityData.defineId(ZuluBigAnimal.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(ZuluBigAnimal.class, EntityDataSerializers.INT);

    protected ZuluBigAnimal(EntityType<? extends ZuluBigAnimal> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIM_TIME, 0);
        builder.define(STATE, STATE_IDLE);
    }

    public int getAnimTime() {
        return this.entityData.get(ANIM_TIME);
    }

    public int getEntityState() {
        return this.entityData.get(STATE);
    }

    /** The squared reach of EntityAIAttackOnCollide: (2 x width)^2 unless the original passed its own value. */
    protected double attackReachSqr() {
        double reach = this.getType().getWidth() * 2.0;
        return reach * reach;
    }

    /** The motionY cap of the knockBack override, or NaN for vanilla knockback (no override in the original). */
    protected double knockbackLift() {
        return Double.NaN;
    }

    /** The EntityAINearestAttackableTarget for IMob (bears, tree ent): while angry, go for the nearest monster. */
    protected Goal monsterTargetGoal() {
        return new NearestAttackableTargetGoal<>(this, Mob.class, 10, false, true, (target, level) -> this.isAngry() && target instanceof Enemy);
    }

    @Override
    public void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        // updateAIState of EntityGenericAnimal
        int state;
        if (this.isFleeing()) {
            state = STATE_FLEEING;
        } else if (this.isInLove()) {
            state = STATE_IN_LOVE;
        } else if (this.isAngry() && (this.getTarget() != null || this.getLastHurtByMob() != null)) {
            state = STATE_ATTACKING;
        } else if (this.isAngry()) {
            state = STATE_LOOKING;
        } else {
            state = STATE_IDLE;
        }
        this.entityData.set(STATE, state);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide() && this.getAnimTime() > 0) {
            this.entityData.set(ANIM_TIME, this.getAnimTime() - 1);
        }
    }

    /** attackEntityAsMob of EntityGenericAnimal: only when the boxes overlap vertically; starts the attack animation. */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (target.getBoundingBox().maxY > this.getBoundingBox().minY && target.getBoundingBox().minY < this.getBoundingBox().maxY) {
            this.entityData.set(ANIM_TIME, MAX_ANIM_TIME);
            return super.doHurtTarget(level, target);
        }
        return false;
    }

    /**
     * knockBack override of the gorilla, giraffe, elephant, mammoth and tree ent: halve the motion, push 0.08 away
     * and lift by 0.4, capped at the creature's own value (the cap always applies in practice).
     */
    @Override
    public void knockback(double power, double xd, double zd, DamageSource source, float damage, boolean comesFromEffect) {
        double lift = this.knockbackLift();
        if (Double.isNaN(lift)) {
            super.knockback(power, xd, zd, source, damage, comesFromEffect);
            return;
        }
        // keep NeoForge's knockback event (cancel, strength and direction) like LivingEntity.knockback
        net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent event =
                net.neoforged.neoforge.common.CommonHooks.onLivingKnockBack(this, (float) power, xd, zd);
        if (event.isCanceled()) {
            return;
        }
        power = event.getStrength();
        xd = event.getRatioX();
        zd = event.getRatioZ();
        if (power * (1.0 - this.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)) <= 0.0) {
            return;
        }
        double dist = Math.sqrt(xd * xd + zd * zd);
        if (dist < 1.0E-4) {
            return;
        }
        this.needsSync = true;
        Vec3 motion = this.getDeltaMovement();
        double y = motion.y / 2.0 + 0.4;
        if (y > 0.1F) {
            y = lift;
        }
        this.setDeltaMovement(motion.x / 2.0 - xd / dist * 0.4 * 0.2, y, motion.z / 2.0 - zd / dist * 0.4 * 0.2);
    }

    /** EntityAIAttackOnCollide: path to the target and hit it once it is within the squared reach (every 20 ticks). */
    public static class ReachMeleeAttackGoal extends MeleeAttackGoal {
        private final ZuluBigAnimal animal;

        public ReachMeleeAttackGoal(ZuluBigAnimal animal, double speedModifier) {
            super(animal, speedModifier, false);
            this.animal = animal;
        }

        @Override
        protected boolean canPerformAttack(LivingEntity target) {
            return this.isTimeToAttack()
                    && this.animal.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ()) <= this.animal.attackReachSqr();
        }
    }

    /**
     * EntityAISmoothSwimming (alligator): in water or lava jump with 80 % chance each tick; unlike vanilla's
     * FloatGoal there is no minimum water depth.
     */
    public static class SmoothSwimGoal extends Goal {
        private final Mob mob;

        public SmoothSwimGoal(Mob mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP));
            mob.getNavigation().setCanFloat(true);
        }

        @Override
        public boolean canUse() {
            return this.mob.isInWater() || this.mob.isInLava();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.mob.getRandom().nextFloat() < 0.8F) {
                this.mob.getJumpControl().jump();
            }
        }
    }

    /** getBabyAnimalEntity of EntityGenericBreedable: a new creature of the same kind. */
    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return this.getType().create(level, EntitySpawnReason.BREEDING) instanceof AgeableMob baby ? baby : null;
    }

    /** Player targeting of EntityAINearestAttackableTarget(EntityPlayer, 16, checkSight), like the Batch B animals. */
    protected Goal playerTargetGoal() {
        return new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt);
    }
}
