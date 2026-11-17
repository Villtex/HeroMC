package projectzulu.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import projectzulu.ZuluMonsters;

/**
 * EntityLizard: a desert lizard that never bites but spits poison (EntityLizardSpit) at its target when it is within
 * 15 blocks, then waits 80 ticks; the frill flares up for the last 20 ticks before the next spit (prepareToSpit,
 * synced here so the model shows it). It is angry (120 ticks, refreshed every tick) whenever its brightness is below
 * 0.5, so it hunts players at night and in the dark; a hit makes it angry for 400. Heals 1 every 200 ticks in daylight.
 * Health 20, speed 0.4, 0.9 x 0.5. Steps like an iron golem, hurt sound lizardhurt. The rare drop (lizard head of the
 * mob skulls) belongs to the blocks of Teil 11.
 */
public class LizardEntity extends ZuluMonster {
    private static final EntityDataAccessor<Boolean> PREPARE_TO_SPIT = SynchedEntityData.defineId(LizardEntity.class, EntityDataSerializers.BOOLEAN);

    private int counter;
    private int timeTillSpit;

    public LizardEntity(EntityType<? extends LizardEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ZuluMonster.createZuluAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.4);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 1.0, 32.0F));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 1.0, 120));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new StateTargetGoal<>(this, Player.class, ANGRY));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PREPARE_TO_SPIT, false);
    }

    public boolean isPreparingToSpit() {
        return this.entityData.get(PREPARE_TO_SPIT);
    }

    @Override
    public void aiStep() {
        if (this.level().isClientSide()) {
            super.aiStep();
            return;
        }
        if (this.level().isBrightOutside() && this.counter % 200 == 0) {
            this.heal(1.0F);
        }
        if (this.getLightLevelDependentMagicValue() < 0.5F) {
            this.angerLevel = 120;
        }
        super.aiStep();
        if (this.timeTillSpit == 20) {
            this.entityData.set(PREPARE_TO_SPIT, true);
        }
        if (this.timeTillSpit == 0) {
            LivingEntity target = this.getTarget();
            if (target != null && this.distanceTo(target) < 15.0F) {
                this.spitAt(target);
                this.timeTillSpit = 80;
                this.entityData.set(PREPARE_TO_SPIT, false);
            }
        }
        this.counter++;
        this.timeTillSpit = Math.max(this.timeTillSpit - 1, 0);
    }

    private void spitAt(LivingEntity target) {
        double dx = target.getX() - this.getX() + this.random.nextGaussian() * 0.4;
        double dy = target.getBoundingBox().minY + target.getBbHeight() / 2.0F - (this.getY() + this.getBbHeight() / 2.0F)
                + this.random.nextGaussian() * 0.4;
        double dz = target.getZ() - this.getZ() + this.random.nextGaussian() * 0.4;
        LizardSpitEntity spit = new LizardSpitEntity(this.level(), this, new Vec3(dx, dy, dz));
        Vec3 look = this.getViewVector(1.0F);
        spit.setPos(this.getX() + look.x, this.getY() + this.getBbHeight() / 2.0F, this.getZ() + look.z);
        this.level().addFreshEntity(spit);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ZuluMonsters.LIZARD_HURT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 1.0F);
    }
}
