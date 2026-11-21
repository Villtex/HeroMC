package twilightforest.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import twilightforest.entity.ai.FireBreathGoal;
import twilightforest.entity.ai.TFGoals;

/**
 * EntityTFFireBeetle: 25 health, 1.1 x 0.75, speed 0.23. 1 time in 10 when it sees its target within 5 blocks it
 * breathes fire for 30 ticks: whatever its look hits takes 2 fire damage and burns 10 s. Glows while breathing.
 * Drops gunpowder.
 */
public class FireBeetle extends TFBeetle implements TFGoals.Flagged {
    private static final EntityDataAccessor<Boolean> DATA_BREATHING = SynchedEntityData.defineId(FireBeetle.class, EntityDataSerializers.BOOLEAN);

    public FireBeetle(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 25.0).add(Attributes.MOVEMENT_SPEED, 0.23);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new FireBreathGoal(this, 5.0F, 30, 0.1F, this::doBreathAttack));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BREATHING, false);
    }

    public boolean isBreathing() {
        return this.entityData.get(DATA_BREATHING);
    }

    @Override
    public void setFlag(boolean on) {
        this.entityData.set(DATA_BREATHING, on);
    }

    @Override
    public int getMaxHeadXRot() {
        return 500;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isBreathing()) {
            Vec3 look = this.getLookAngle();
            double dist = 0.9;
            double px = this.getX() + look.x * dist;
            double py = this.getY() + 0.25 + look.y * dist;
            double pz = this.getZ() + look.z * dist;
            if (this.level().isClientSide()) {
                for (int i = 0; i < 2; i++) {
                    double spread = 5.0 + this.random.nextDouble() * 2.5;
                    double velocity = 0.15 + this.random.nextDouble() * 0.15;
                    double dx = (look.x + this.random.nextGaussian() * 0.0075F * spread) * velocity;
                    double dy = (look.y + this.random.nextGaussian() * 0.0075F * spread) * velocity;
                    double dz = (look.z + this.random.nextGaussian() * 0.0075F * spread) * velocity;
                    this.level().addParticle(ParticleTypes.FLAME, px, py, pz, dx, dy, dz);
                }
            } else {
                this.level().playSound(null, this.getX() + 0.5, this.getY() + 0.5, this.getZ() + 0.5, SoundEvents.GHAST_SHOOT,
                        this.getSoundSource(), this.random.nextFloat() * 0.5F, this.random.nextFloat() * 0.5F);
            }
        }
    }

    public void doBreathAttack(Entity target) {
        if (!target.fireImmune() && this.level() instanceof ServerLevel level && target.hurtServer(level, this.damageSources().inFire(), 2.0F)) {
            target.igniteForSeconds(10);
        }
    }
}
