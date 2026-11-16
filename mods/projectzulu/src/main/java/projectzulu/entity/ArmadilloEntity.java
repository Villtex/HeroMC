package projectzulu.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import org.jspecify.annotations.Nullable;
import projectzulu.ZuluEntities;

/**
 * EntityArmadillo: a desert animal that only fights back when hit (anger 400 ticks in the original). When a nearby
 * player aims a bow at it, it curls up ("inCover": armor 30 instead of 4) and now and then it rolls as a ball for a
 * moment ("charging", animation only; the speed boost of the original never took effect). Heals 1 every 200 ticks in
 * daylight. Bred and tempted with a spider eye. Health 12, speed 0.3, damage 4 (3/4/6 by difficulty, which is
 * vanilla's own player-damage scaling). The 1.5.2 bounding box stayed the EntityLiving default 0.6 x 1.8 (the
 * setBounds call in the constructor was undone by setPosition).
 */
public class ArmadilloEntity extends Animal implements NeutralMob {
    private static final EntityDataAccessor<Boolean> COVERING = SynchedEntityData.defineId(ArmadilloEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CHARGING = SynchedEntityData.defineId(ArmadilloEntity.class, EntityDataSerializers.BOOLEAN);
    /** setAngerLevel(400) of the original. */
    private static final UniformInt ANGER_TIME = TimeUtil.rangeOfSeconds(20, 20);

    private int inCoverTimer;
    private float timeSinceLastCharge = 5.0F;
    private float chargeTriggerThreshold = 200.0F + this.random.nextInt(200);
    private long angerEndTime = NeutralMob.NO_ANGER_END_TIME;
    private @Nullable EntityReference<LivingEntity> angerTarget;

    public ArmadilloEntity(EntityType<? extends ArmadilloEntity> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.ARMOR, 4.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.0));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(6, new TemptGoal(this, 1.0, stack -> stack.is(Items.SPIDER_EYE), false));
        this.goalSelector.addGoal(7, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 1.0, 120));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COVERING, false);
        builder.define(CHARGING, false);
    }

    public boolean isCovering() {
        return this.entityData.get(COVERING);
    }

    public boolean isCharging() {
        return this.entityData.get(CHARGING);
    }

    /** inCover: armor 30 instead of 4. */
    @Override
    public int getArmorValue() {
        return this.isCovering() ? 30 : super.getArmorValue();
    }

    @Override
    public void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.updatePersistentAnger(level, true);
        // every 3 ticks (ticksToCheckAbilities): cover while a visible player within 16 blocks draws a bow at us
        if (this.tickCount % 3 == 0) {
            Player player = level.getNearestPlayer(this, 16.0);
            boolean takeCover = false;
            if (player != null && player.isUsingItem() && player.getUseItem().getItem() instanceof ProjectileWeaponItem
                    && this.hasLineOfSight(player)) {
                double angleToUs = Math.atan2(player.getX() - this.getX(), this.getZ() - player.getZ()) * (180.0 / Math.PI) + 180.0;
                takeCover = Math.abs(Mth.wrapDegrees(angleToUs - player.getYRot())) < 90.0;
            }
            this.inCoverTimer = takeCover ? 20 : Math.max(this.inCoverTimer - 3, 0);
            this.entityData.set(COVERING, this.inCoverTimer > 0);

            // the occasional roll: trigger every 200-400 ticks, roll for 20 % of that time
            if (this.timeSinceLastCharge > this.chargeTriggerThreshold && !this.isCharging()) {
                this.entityData.set(CHARGING, true);
                this.timeSinceLastCharge = 0.0F;
                this.chargeTriggerThreshold = 200.0F + this.random.nextInt(200);
            }
            if (this.isCharging() && this.timeSinceLastCharge > 0.2F * this.chargeTriggerThreshold) {
                this.entityData.set(CHARGING, false);
            }
            this.timeSinceLastCharge += 3.0F;
        }
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide() && this.level().isBrightOutside() && this.tickCount % 200 == 0) {
            this.heal(1.0F);
        }
        super.aiStep();
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return projectzulu.ZuluSounds.ARMADILLO_LIVING.get();
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return projectzulu.ZuluSounds.ARMADILLO_HURT.get();
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return projectzulu.ZuluSounds.ARMADILLO_DEATH.get();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.SPIDER_EYE);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ZuluEntities.ARMADILLO.get().create(level, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        super.addAdditionalSaveData(output);
        this.addPersistentAngerSaveData(output);
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
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
}
