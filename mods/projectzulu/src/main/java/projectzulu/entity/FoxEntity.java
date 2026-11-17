package projectzulu.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import projectzulu.ZuluEntities;
import projectzulu.ZuluSounds;

/**
 * EntityFox: a wild fox flees for 80 ticks when a player hits it (with every wild fox within 20 blocks), then goes
 * for the attacker. Tamed with an egg (1 in 3 per egg, the egg is used up either way); a tame fox sits first, and
 * its owner's right click (with anything but an egg) cycles sit -> follow -> guard (attacks monsters nearby) ->
 * stay (wander) -> sit, the states of EntityGenericTameable. An egg heals a hurt tame fox by 2 (the heal amount of
 * a melon slice), otherwise it puts a tame fox in love; wild foxes only take eggs for taming. Health 12, armor 2,
 * speed 0.3, damage 5 (base table 3/4/5/6). Size 0.6 x 1.0. Only a hurt sound.
 * Not ported: the paper GUI of tamed creatures and the name plate with the state icon above tame ones.
 */
public class FoxEntity extends TamableAnimal implements NeutralMob {
    /** setAngerLevel(400) of the original. */
    private static final UniformInt ANGER_TIME = TimeUtil.rangeOfSeconds(20, 20);

    /** The tame states the owner cycles through (sitting, following, attacking, idle of the original). */
    public enum Mode {
        SIT, FOLLOW, GUARD, STAY;

        Mode next() {
            return values()[(this.ordinal() + 1) % values().length];
        }
    }

    private long angerEndTime = NeutralMob.NO_ANGER_END_TIME;
    private @Nullable EntityReference<LivingEntity> angerTarget;
    private int fleeTicks;
    private Mode mode = Mode.SIT;

    public FoxEntity(EntityType<? extends FoxEntity> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ZuluLandAnimal.FleeGoal(this, 1.0, () -> this.fleeTicks > 0));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F) {
            @Override
            public boolean canUse() {
                return FoxEntity.this.mode == Mode.FOLLOW && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return FoxEntity.this.mode == Mode.FOLLOW && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(5, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(6, new TemptGoal(this, 1.0, stack -> stack.is(Items.EGG), false));
        this.goalSelector.addGoal(7, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 1.0, 120));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        // monsters (IMob.mobSelector) while attacking: a tame fox on guard, a wild one while angry
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Mob.class, 10, false, true,
                (target, level) -> target instanceof Enemy && (this.isTame() ? this.mode == Mode.GUARD : this.isAngry())));
        this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    public Mode getMode() {
        return this.mode;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.isTame()) {
            if (this.isOwnedBy(player)) {
                if (stack.is(Items.EGG) && this.getHealth() < this.getMaxHealth()) {
                    if (!this.level().isClientSide()) {
                        this.usePlayerItem(player, hand, stack);
                        this.heal(2.0F);
                    }
                    return InteractionResult.SUCCESS;
                }
                if (!this.isFood(stack)) {
                    if (!this.level().isClientSide()) {
                        this.setMode(this.mode.next());
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        } else if (stack.is(Items.EGG)) {
            if (!this.level().isClientSide()) {
                this.usePlayerItem(player, hand, stack);
                if (this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.stopBeingAngry();
                    this.navigation.stop();
                    this.setMode(Mode.SIT);
                    this.setHealth(this.getMaxHealth());
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    private void setMode(Mode mode) {
        this.mode = mode;
        boolean sit = mode == Mode.SIT;
        this.setOrderedToSit(sit);
        this.setInSittingPose(sit);
        this.jumping = false;
        if (mode == Mode.STAY || mode == Mode.SIT) {
            this.setTarget(null);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && source.getEntity() instanceof Player player) {
            // shouldNotifySimilar
            for (FoxEntity ally : level.getEntitiesOfClass(FoxEntity.class, this.getBoundingBox().inflate(20.0), e -> e != this)) {
                ally.attackedBy();
            }
            this.attackedBy();
        }
        return hurt;
    }

    /** entityAttackedReaction: a wild fox flees (shouldPanic = !isTamed), a tame one ignores it. */
    private void attackedBy() {
        if (!this.isTame()) {
            this.fleeTicks = 80;
        }
    }

    @Override
    public void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        // despawnInPeaceful of the pack: wild foxes leave, tame ones stay
        if (level.getDifficulty() == Difficulty.PEACEFUL && !this.isTame()) {
            this.discard();
            return;
        }
        this.updatePersistentAnger(level, true);
        this.fleeTicks = Math.max(this.fleeTicks - 1, 0);
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel level) {
        if (this.isTame()) {
            return 0;
        }
        return this.isAngry() ? 5 : this.random.nextInt(2) + 1;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ZuluSounds.FOX_HURT.get();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.EGG);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        // the original made an untamed copy (getBabyAnimalEntity)
        return ZuluEntities.FOX.get().create(level, EntitySpawnReason.BREEDING);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        this.addPersistentAngerSaveData(output);
        output.putInt("ZuluMode", this.mode.ordinal());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.readPersistentAngerSaveData(this.level(), input);
        int ordinal = input.getIntOr("ZuluMode", this.isOrderedToSit() ? Mode.SIT.ordinal() : Mode.STAY.ordinal());
        this.mode = Mode.values()[Math.floorMod(ordinal, Mode.values().length)];
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
}
