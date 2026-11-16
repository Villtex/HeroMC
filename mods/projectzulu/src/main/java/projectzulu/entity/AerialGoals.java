package projectzulu.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

/**
 * The bird AI of Project Zulu 1.5.2 (projectzulu.common.mobs.entityai). Flying birds do not path find: the goals only
 * set the target point of {@link AerialAnimal}, the entity steers to it. Mutex bits of the original: 1 = MOVE,
 * 2 = LOOK, 4 = JUMP.
 */
public final class AerialGoals {
    private AerialGoals() {
    }

    /** EntityAIFlyingWander (mutex 1): a new random point in the air, kept until reached or 1 % chance per check. */
    public static class FlyingWander extends Goal {
        private final AerialAnimal bird;
        private @Nullable BlockPos wanted;

        public FlyingWander(AerialAnimal bird) {
            this.bird = bird;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            // getAge() of 1.5.2 = ticks without a player within 32 blocks (noActionTime)
            if (this.bird.getNoActionTime() >= 100 && this.bird.isEntityGrounded()) {
                return false;
            }
            this.wanted = this.bird.randomFlightTarget(10, 7, this.bird.getMaxFlightHeight());
            return this.bird.isTargetPositionValid(this.wanted);
        }

        @Override
        public boolean canContinueToUse() {
            return this.bird.getRandom().nextInt(100) != 0 && !this.bird.atTargetPosition() && this.bird.isTargetPositionValid();
        }

        @Override
        public void start() {
            this.bird.setTargetPosition(this.wanted);
        }
    }

    /** EntityAIStayStill (mutex 5): holds the bird while it is on the ground in the triggering state. */
    public static class StayStill extends Goal {
        private final AerialAnimal bird;
        private final AerialAnimal.EntityState triggeringState;

        public StayStill(AerialAnimal bird, AerialAnimal.EntityState triggeringState) {
            this.bird = bird;
            this.triggeringState = triggeringState;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return !this.bird.isInWater() && this.bird.onGround() && this.bird.getEntityState() == this.triggeringState;
        }

        @Override
        public void start() {
            this.bird.getNavigation().stop();
        }
    }

    /**
     * EntityAIAttackOnCollide (mutex 3) and EntityAIVultureFollow (the same code, aiming heightToFollow above the
     * target and only starting in the given states). In the air it aims at the target's block (the first check
     * without the height offset), on the ground it uses the path finder. Hits every 20 ticks in reach.
     */
    public static class Chase extends Goal {
        private final AerialAnimal bird;
        private final double attackDistanceSq;
        private final int firstHeight;
        private final double height;
        private final @Nullable Set<AerialAnimal.EntityState> validStates;
        private @Nullable LivingEntity target;
        private @Nullable Path path;
        private int attackTick;
        private int changeDirectionCooldown;

        /** EntityAIAttackOnCollide(entity, moveSpeed, false[, attackDistanceSq]); reach default (width * 2)^2. */
        public static Chase attackOnCollide(AerialAnimal bird, double attackDistanceSq) {
            return new Chase(bird, attackDistanceSq, 0, 1.0, null);
        }

        public static Chase attackOnCollide(AerialAnimal bird) {
            double reach = bird.getBbWidth() * 2.0F;
            return attackOnCollide(bird, reach * reach);
        }

        /** EntityAIVultureFollow(entity, moveSpeed, false).setValidStates(states): circles 10 above the target. */
        public static Chase vultureFollow(AerialAnimal bird, Set<AerialAnimal.EntityState> validStates) {
            double reach = bird.getBbWidth() * 2.0F;
            return new Chase(bird, reach * reach, 10, 10.0, validStates);
        }

        private Chase(AerialAnimal bird, double attackDistanceSq, int firstHeight, double height,
                @Nullable Set<AerialAnimal.EntityState> validStates) {
            this.bird = bird;
            this.attackDistanceSq = attackDistanceSq;
            this.firstHeight = firstHeight;
            this.height = height;
            this.validStates = validStates;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        private BlockPos aimAt(LivingEntity target, double offset) {
            return new BlockPos(Mth.floor(target.getX()), Mth.floor(target.getY() + offset), Mth.floor(target.getZ()));
        }

        @Override
        public boolean canUse() {
            if (this.validStates != null && !this.validStates.contains(this.bird.getEntityState())) {
                return false;
            }
            LivingEntity candidate = this.bird.getTarget();
            if (candidate == null) {
                return false;
            }
            this.target = candidate;
            if (this.bird.isEntityGrounded()) {
                this.path = this.bird.getNavigation().createPath(candidate, 0);
                return this.path != null;
            }
            return this.bird.setTargetPosition(this.aimAt(candidate, this.firstHeight));
        }

        @Override
        public boolean canContinueToUse() {
            if (this.bird.getTarget() == null || this.target == null || !this.target.isAlive()) {
                return false;
            }
            return this.bird.isEntityGrounded() ? !this.bird.getNavigation().isDone()
                    : !this.bird.atTargetPosition() && this.bird.isTargetPositionValid();
        }

        @Override
        public void start() {
            if (this.bird.isEntityGrounded()) {
                this.bird.getNavigation().moveTo(this.path, 1.0);
            } else if (this.target != null) {
                this.bird.setTargetPosition(this.aimAt(this.target, this.height));
            }
            this.changeDirectionCooldown = 0;
        }

        @Override
        public void stop() {
            this.target = null;
            this.bird.getNavigation().stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.target;
            if (target == null) {
                return;
            }
            this.bird.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (this.bird.getSensing().hasLineOfSight(target) && --this.changeDirectionCooldown <= 0) {
                this.changeDirectionCooldown = 4 + this.bird.getRandom().nextInt(7);
                if (this.bird.isEntityGrounded()) {
                    this.bird.getNavigation().moveTo(target, 1.0);
                } else {
                    this.bird.setTargetPosition(this.aimAt(target, this.height));
                }
            }
            this.attackTick = Math.max(this.attackTick - 1, 0);
            if (this.bird.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ()) <= this.attackDistanceSq
                    && this.attackTick <= 0) {
                this.attackTick = 20;
                this.bird.doHurtTarget(getServerLevel(this.bird), target);
            }
        }
    }

    /**
     * EntityAINearestAttackableTarget(entity, states, EntityPlayer, 16, 0, true): the nearest player in sight within
     * 16, only while the bird is in one of the states; it keeps the target only while attacking or looking.
     */
    public static class StateTarget extends NearestAttackableTargetGoal<Player> {
        private final AerialAnimal bird;
        private final Set<AerialAnimal.EntityState> validStates;

        public StateTarget(AerialAnimal bird, Set<AerialAnimal.EntityState> validStates) {
            super(bird, Player.class, 0, true, false, null);
            this.bird = bird;
            this.validStates = validStates;
        }

        @Override
        public boolean canUse() {
            return this.validStates.contains(this.bird.getEntityState()) && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            AerialAnimal.EntityState state = this.bird.getEntityState();
            return (state == AerialAnimal.EntityState.ATTACKING || state == AerialAnimal.EntityState.LOOKING) && super.canContinueToUse();
        }
    }

    /** EntityAIWander(entity, moveSpeed, 120): walking only while grounded (or while a player is near). */
    public static class GroundWander extends RandomStrollGoal {
        private final AerialAnimal bird;

        public GroundWander(AerialAnimal bird, int interval) {
            super(bird, 1.0, interval, false);
            this.bird = bird;
        }

        @Override
        public boolean canUse() {
            if (this.bird.getNoActionTime() >= 100 && !this.bird.isEntityGrounded()) {
                return false;
            }
            return super.canUse();
        }
    }
}
