package chococraft.entity;

import chococraft.ChocoConfig;
import chococraft.ChocoCraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Locale;

/**
 * EntityChicobo: the chick of every colour (one entity type, the colour is synced), 0.5 x 0.5, rendered at 1.32.
 * Health 10/15/20 by colour. With the AI tasks of the original: follow the owner (ChocoboAIFollowOwner: from 3 blocks,
 * teleport beyond 20 when no path is found), swim, panic, wander (one in 120 ticks, only with a player near), watch
 * players, look around. A gysahl cake (owner) or the time grows it up: timeUntilAdult starts at 27000-29000 and went
 * down by two each tick (EntityAgeable counted the age down and EntityChicobo once more). It turns into an adult chocobo
 * of its colour with its name, owner and state as soon as there is air 1 block around, 3 high, with a breeding
 * cool-down of 6000. A feather toggles follow/stay/wander.
 * <p>
 * Movement: the AI speeds of 1.5.2 were absolute (acceleration speed^2): wander 0.25, panic 0.38, follow 0.7; here
 * MOVEMENT_SPEED 0.217 (0.25 with the 1.327 of 26.3) and the others as factors of it.
 */
public class Chicobo extends AbstractChocobo {
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(Chicobo.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TIME_UNTIL_ADULT = SynchedEntityData.defineId(Chicobo.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_CAN_GROW_UP = SynchedEntityData.defineId(Chicobo.class, EntityDataSerializers.BOOLEAN);
    private static final double SPEED = 0.217;

    private boolean growUp;

    public Chicobo(EntityType<? extends Chicobo> type, Level level) {
        super(type, level);
        this.setTimeUntilAdult(this.random.nextInt(2000) + 27000);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createAnimalAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, SPEED);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FollowOwner(this, 0.608 / SPEED, 3.0F, 20.0F));
        this.goalSelector.addGoal(1, new Swim(this));
        this.goalSelector.addGoal(2, new PanicGoal(this, 0.33 / SPEED));
        this.goalSelector.addGoal(3, new Wander(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_COLOR, ChocoboColor.YELLOW.ordinal());
        builder.define(DATA_TIME_UNTIL_ADULT, 28000);
        builder.define(DATA_CAN_GROW_UP, false);
    }

    @Override
    public ChocoboColor getColor() {
        return ChocoboColor.values()[this.entityData.get(DATA_COLOR)];
    }

    /** setColor: health by colour, full health. */
    public void setColor(ChocoboColor color) {
        this.entityData.set(DATA_COLOR, color.ordinal());
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(color.chicoboHealth);
        this.setHealth(color.chicoboHealth);
    }

    public int getTimeUntilAdult() {
        return this.entityData.get(DATA_TIME_UNTIL_ADULT);
    }

    public void setTimeUntilAdult(int ticks) {
        this.entityData.set(DATA_TIME_UNTIL_ADULT, ticks);
    }

    public boolean isCanGrowUp() {
        return this.entityData.get(DATA_CAN_GROW_UP);
    }

    public void setCanGrowUp(boolean canGrowUp) {
        this.entityData.set(DATA_CAN_GROW_UP, canGrowUp);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("TimeToAdult", this.getTimeUntilAdult());
        output.putString("Color", this.getColor().name());
        output.putBoolean("CanGrow", this.isCanGrowUp());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setTimeUntilAdult(input.getIntOr("TimeToAdult", 28000));
        float health = this.getHealth();
        try {
            this.setColor(ChocoboColor.valueOf(input.getStringOr("Color", "YELLOW").toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            this.setColor(ChocoboColor.YELLOW);
        }
        this.setHealth(health);
        this.setCanGrowUp(input.getBooleanOr("CanGrow", false));
        this.setWander(!this.isFollowing());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        if (!this.growUp) {
            this.setTimeUntilAdult(this.getTimeUntilAdult() - 2);
            if (this.getTimeUntilAdult() <= 0) {
                this.growUp = true;
                this.setCanGrowUp(true);
            }
        }
        this.doGrowUp();
    }

    /** doGrowUp: the adult of the colour takes the chicobo's place when there is room. */
    private void doGrowUp() {
        if (!(this.level() instanceof ServerLevel level) || !this.growUp || !this.isCanGrowUp()
                || !isSpaceAroundLocationFree(level, (int) this.getX(), (int) this.getY(), (int) this.getZ(), 1, 3, 1)) {
            return;
        }
        Chocobo adult = ChocoCraft.chocoboType(this.getColor()).create(level, EntitySpawnReason.CONVERSION);
        if (adult == null) {
            return;
        }
        adult.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
        adult.setChocoboName(this.getChocoboName());
        adult.setOwnerReference(this.getOwnerReference());
        adult.setOwnerName(this.getOwnerName());
        adult.setHidename(this.isHidename());
        adult.setTame(this.isTame(), false);
        adult.setFollowing(this.isFollowing());
        adult.setWander(this.isWander());
        adult.setIsMale(this.isMale());
        adult.setAge(6000);
        level.addFreshEntity(adult);
        adult.particles(ParticleTypes.POOF, 7);
        this.growUp = false;
        this.discard();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.random.nextInt(2) == 0 ? ChocoCraft.KWEH.get() : null;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult result = this.interactCommon(player, hand);
        if (result != null) {
            return result;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(ChocoCraft.GYSAHL_CAKE.get())) {
            if (!this.level().isClientSide()) {
                if (this.isTame() && this.isOwner(player)) {
                    this.useItem(player, stack);
                    this.setCanGrowUp(true);
                    this.growUp = true;
                } else {
                    this.particles(ParticleTypes.SMOKE, 7);
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.is(ChocoCraft.CHOCOBO_FEATHER.get())) {
            if (!this.level().isClientSide()) {
                if (this.isTame() && this.isOwner(player)) {
                    this.toggleFollowWanderStay();
                } else {
                    this.particles(ParticleTypes.SMOKE, 7);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    /** ChocoboAIFollowOwner. */
    static final class FollowOwner extends Goal {
        private final Chicobo chicobo;
        private final double speed;
        private final float followDistance;
        private final float teleportDistance;
        private int delay;

        FollowOwner(Chicobo chicobo, double speed, float followDistance, float teleportDistance) {
            this.chicobo = chicobo;
            this.speed = speed;
            this.followDistance = followDistance;
            this.teleportDistance = teleportDistance;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            Player owner = this.chicobo.getOwnerPlayer();
            return owner != null && this.chicobo.isFollowing()
                    && this.chicobo.distanceToSqr(owner) >= this.followDistance * this.followDistance;
        }

        @Override
        public boolean canContinueToUse() {
            Player owner = this.chicobo.getOwnerPlayer();
            // continueExecuting ended as soon as the path was done
            return this.chicobo.isFollowing() && !this.chicobo.getNavigation().isDone() && owner != null && owner.isAlive()
                    && this.chicobo.distanceToSqr(owner) >= this.followDistance * this.followDistance;
        }

        @Override
        public void start() {
            this.delay = 0;
            this.chicobo.setPathfindingMalus(PathType.WATER, 0.0F);
            if (this.chicobo.isInWater()) {
                Vec3 dm = this.chicobo.getDeltaMovement();
                this.chicobo.setDeltaMovement(dm.x, 0.1, dm.z);
                this.chicobo.getJumpControl().jump();
            }
        }

        @Override
        public void stop() {
            this.chicobo.getNavigation().stop();
            this.chicobo.setPathfindingMalus(PathType.WATER, -1.0F);
        }

        @Override
        public void tick() {
            Player owner = this.chicobo.getOwnerPlayer();
            if (owner == null || --this.delay > 0) {
                return;
            }
            this.delay = 10;
            if (!this.chicobo.getNavigation().moveTo(owner, this.speed)
                    && this.chicobo.distanceToSqr(owner) > this.teleportDistance * this.teleportDistance) {
                this.chicobo.teleportToOwner();
                this.chicobo.getNavigation().stop();
            }
        }
    }

    /** ChocoboAISwimming: jumps with 80 % in water or lava. */
    static final class Swim extends Goal {
        private final Chicobo chicobo;

        Swim(Chicobo chicobo) {
            this.chicobo = chicobo;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP));
            chicobo.getNavigation().setCanFloat(true);
        }

        @Override
        public boolean canUse() {
            return this.chicobo.isInWater() || this.chicobo.isInLava();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.chicobo.getRandom().nextFloat() < 0.8F) {
                Vec3 dm = this.chicobo.getDeltaMovement();
                this.chicobo.setDeltaMovement(dm.x, 0.1, dm.z);
                this.chicobo.getJumpControl().jump();
            }
        }
    }

    /** ChocoboAIWander: only with a player near (age below 100), wandering on, one in 120 ticks, up to 10 blocks. */
    static final class Wander extends Goal {
        private final Chicobo chicobo;
        private final double speed;
        private Vec3 target = Vec3.ZERO;

        Wander(Chicobo chicobo, double speed) {
            this.chicobo = chicobo;
            this.speed = speed;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.chicobo.getNoActionTime() >= 100 || !this.chicobo.isWander() || this.chicobo.getRandom().nextInt(120) != 0) {
                return false;
            }
            Vec3 pos = DefaultRandomPos.getPos(this.chicobo, 10, 7);
            if (pos == null) {
                return false;
            }
            this.target = pos;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.chicobo.getNavigation().isDone();
        }

        @Override
        public void start() {
            this.chicobo.getNavigation().moveTo(this.target.x, this.target.y, this.target.z, this.speed);
        }
    }
}
