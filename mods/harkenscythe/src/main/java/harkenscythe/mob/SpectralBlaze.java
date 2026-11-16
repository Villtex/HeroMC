package harkenscythe.mob;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * EntityHSSpectralBlaze: shoots small fireballs (EntityAIArrowAttack 0.25, 20-60 ticks, 15 blocks; a burst of three
 * after a 60 tick wind-up, then 100 ticks rest), hovers up to its target, falls slowly, is hurt by water, immune to
 * fire, always fully lit and smokes.
 */
public class SpectralBlaze extends SpectralPet implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> CHARGED = SynchedEntityData.defineId(SpectralBlaze.class, EntityDataSerializers.BOOLEAN);
    private float allowedHeightOffset = 0.5F;
    private int nextHeightOffsetChangeTick;
    private int attackTime;
    private int shots;

    public SpectralBlaze(EntityType<? extends SpectralBlaze> type, Level level) {
        super(type, level, 10.0F, new Sounds(SoundEvents.BLAZE_AMBIENT, SoundEvents.BLAZE_HURT, SoundEvents.BLAZE_DEATH, null, 0.1F));
        this.setupGoals();
    }

    @Override
    protected void setupGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, this.factor(0.25), 20, 60, 15.0F));
        this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F));
        this.goalSelector.addGoal(6, this.creepballTempt());
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, AbstractVillager.class, 5.0F, 0.02F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.registerTargets(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CHARGED, false);
    }

    /** func_70845_n: burning while it shoots. */
    public boolean isCharged() {
        return this.entityData.get(CHARGED);
    }

    @Override
    public boolean isOnFire() {
        return this.isCharged();
    }

    @Override
    public void aiStep() {
        if (this.level() instanceof ServerLevel level) {
            if (this.isInWaterOrRain()) {
                this.hurtServer(level, this.damageSources().drown(), 1.0F);
            }
            if (--this.nextHeightOffsetChangeTick <= 0) {
                this.nextHeightOffsetChangeTick = 100;
                this.allowedHeightOffset = 0.5F + (float) this.random.nextGaussian() * 3.0F;
            }
            LivingEntity target = this.getTarget();
            if (target != null && target.getEyeY() > this.getEyeY() + this.allowedHeightOffset) {
                Vec3 m = this.getDeltaMovement();
                this.setDeltaMovement(m.x, m.y + (0.3F - m.y) * 0.3F, m.z);
            }
            if (this.random.nextInt(24) == 0) {
                level.playSound(null, this.getX() + 0.5, this.getY() + 0.5, this.getZ() + 0.5, SoundEvents.BLAZE_BURN, SoundSource.HOSTILE,
                        1.0F + this.random.nextFloat(), this.random.nextFloat() * 0.7F + 0.3F);
            }
            if (this.attackTime > 0) {
                this.attackTime--;
            }
        } else {
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), 0.0, 0.0, 0.0);
            }
        }
        Vec3 m = this.getDeltaMovement();
        if (!this.onGround() && m.y < 0.0) {
            this.setDeltaMovement(m.multiply(1.0, 0.6, 1.0));
        }
        super.aiStep();
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        double dx = target.getX() - this.getX();
        double dy = target.getBoundingBox().minY + target.getBbHeight() / 2.0F - (this.getY() + this.getBbHeight() / 2.0F);
        double dz = target.getZ() - this.getZ();
        if (this.attackTime == 0) {
            this.shots++;
            if (this.shots == 1) {
                this.attackTime = 60;
                this.entityData.set(CHARGED, true);
            } else if (this.shots <= 4) {
                this.attackTime = 6;
            } else {
                this.attackTime = 100;
                this.shots = 0;
                this.entityData.set(CHARGED, false);
            }
            if (this.shots > 1) {
                float spread = (float) Math.sqrt(power) * 0.5F;
                level.levelEvent(null, 1018, this.blockPosition(), 0);
                SmallFireball fireball = new SmallFireball(level, this,
                        new Vec3(dx + this.random.nextGaussian() * spread, dy,
                                dz + this.random.nextGaussian() * spread));
                fireball.setPos(fireball.getX(), this.getY() + this.getBbHeight() / 2.0F + 0.5, fireball.getZ());
                level.addFreshEntity(fireball);
            }
        }
        this.setYRot((float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }
}
