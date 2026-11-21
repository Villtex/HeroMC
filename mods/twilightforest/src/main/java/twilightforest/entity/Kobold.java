package twilightforest.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import twilightforest.TFEntities;
import twilightforest.entity.ai.TFGoals;

/**
 * EntityTFKobold: 13 health, 0.8 x 1.1, speed 0.28, bite 3; flocks to its kind, leaps at its target, and panics
 * (water drops at the eyes) when one of its kind dies nearby. Up to 8 in a chunk. Drops wheat, 1 in 2 gold nuggets.
 */
public class Kobold extends TFMonster implements TFGoals.Flagged {
    private static final EntityDataAccessor<Boolean> DATA_PANIC = SynchedEntityData.defineId(Kobold.class, EntityDataSerializers.BOOLEAN);

    public Kobold(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 13.0).add(Attributes.MOVEMENT_SPEED, 0.28);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TFGoals.PanicOnFlockDeath(this, 0.38 / 0.28));
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.28F));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(4, new TFGoals.FlockToSameKind(this, 1.0));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PANIC, false);
    }

    public boolean isPanicked() {
        return this.entityData.get(DATA_PANIC);
    }

    @Override
    public void setFlag(boolean on) {
        this.entityData.set(DATA_PANIC, on);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isPanicked() && this.level().isClientSide()) {
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.SPLASH, this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 0.5,
                        this.getEyeY(), this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 0.5, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public int attackStrength(Entity target) {
        return 3;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 8;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TFEntities.KOBOLD_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TFEntities.KOBOLD_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TFEntities.KOBOLD_DIE.get();
    }
}
