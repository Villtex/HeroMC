package projectzulu.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * The shared behaviour of the untameable EntityGenericAnimal land creatures (Batch B: boar, frog, rabbit, beaver,
 * penguin, ostrich). In the original only a player's hit made them react (entityAttackedReaction): shy ones fled for
 * 80 ticks (shouldPanic), the others got angerLevel 400 and went for the nearest player; with shouldNotifySimilar
 * every creature of the same class within 20 blocks reacted as well. Experience: 5 while angry, else 1-2.
 * The navigator avoided water (setAvoidsWater).
 */
public abstract class ZuluLandAnimal extends Animal implements NeutralMob {
    /** setAngerLevel(400) of the original. */
    private static final UniformInt ANGER_TIME = TimeUtil.rangeOfSeconds(20, 20);

    private long angerEndTime = NeutralMob.NO_ANGER_END_TIME;
    private @Nullable EntityReference<LivingEntity> angerTarget;
    /** fleeingTick of the original. */
    private int fleeTicks;
    /** slimeJumpDelay of the hopping AI tasks (frog, rabbit). */
    private int hopDelay;

    protected ZuluLandAnimal(EntityType<? extends ZuluLandAnimal> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    /** shouldPanic of the original: flee from a player's hit instead of getting angry. */
    protected boolean shouldPanic() {
        return false;
    }

    /** shouldNotifySimilar of the original: alert the same kind within 20 blocks. */
    protected boolean shouldNotifySimilar() {
        return false;
    }

    /** The frog and the rabbit moved in hops (the shouldHop flag of their Panic/Mate/Tempt/Wander tasks). */
    protected boolean hops() {
        return false;
    }

    public boolean isFleeing() {
        return this.fleeTicks > 0;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && source.getEntity() instanceof Player player) {
            if (this.shouldNotifySimilar()) {
                for (ZuluLandAnimal ally : level.getEntitiesOfClass(ZuluLandAnimal.class, this.getBoundingBox().inflate(20.0),
                        e -> e != this && e.getClass() == this.getClass())) {
                    ally.attackedBy(player);
                }
            }
            this.attackedBy(player);
        }
        return hurt;
    }

    /** entityAttackedReaction of the original. */
    protected void attackedBy(Player player) {
        if (this.shouldPanic()) {
            this.fleeTicks = 80;
        } else if (!player.isCreative() && !player.isSpectator()) {
            this.setPersistentAngerTarget(EntityReference.of(player));
            this.startPersistentAngerTimer();
        }
    }

    @Override
    public void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.updatePersistentAnger(level, true);
        this.fleeTicks = Math.max(this.fleeTicks - 1, 0);
        if (this.hops()) {
            this.hop();
        }
    }

    /**
     * tryToHop of the hopping AI tasks: while the navigator moves, the creature only moves on the tick it jumps
     * (speed 0 in between), every 2-6 ticks when fleeing, else every 10-29 ticks.
     */
    private void hop() {
        MoveControl move = this.getMoveControl();
        if (!move.hasWanted()) {
            return;
        }
        if (this.onGround() && this.hopDelay-- <= 0) {
            this.hopDelay = this.isFleeing() ? this.random.nextInt(5) + 2 : this.random.nextInt(20) + 10;
            this.getJumpControl().jump();
        } else {
            move.setWantedPosition(move.getWantedX(), move.getWantedY(), move.getWantedZ(), 0.0);
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (!this.hops()) {
            super.playStepSound(pos, state);
        }
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel level) {
        return this.isAngry() ? 5 : this.random.nextInt(2) + 1;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        this.addPersistentAngerSaveData(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.readPersistentAngerSaveData(this.level(), input);
    }

    // ---- NeutralMob (angerLevel of the original) ----

    @Override
    public long getPersistentAngerEndTime() {
        return this.angerEndTime;
    }

    @Override
    public void setPersistentAngerEndTime(long endTime) {
        this.angerEndTime = endTime;
    }

    @Override
    public @Nullable EntityReference<LivingEntity> getPersistentAngerTarget() {
        return this.angerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> target) {
        this.angerTarget = target;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setTimeToRemainAngry(ANGER_TIME.sample(this.random));
    }

    /** EntityAIPanic of Project Zulu: runs only while the creature flees (fleeingTick), not on any damage. */
    public static class FleeGoal extends PanicGoal {
        private final java.util.function.BooleanSupplier fleeing;

        public FleeGoal(PathfinderMob mob, double speedModifier, java.util.function.BooleanSupplier fleeing) {
            super(mob, speedModifier);
            this.fleeing = fleeing;
        }

        @Override
        protected boolean shouldPanic() {
            return this.fleeing.getAsBoolean();
        }
    }
}
