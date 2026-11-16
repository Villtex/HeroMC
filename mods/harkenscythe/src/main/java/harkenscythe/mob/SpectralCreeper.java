package harkenscythe.mob;

import java.util.EnumSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * EntityHSSpectralCreeper: swells near its target (EntityAIHSSpectralCreeperSwell, fuse 30) and bursts without
 * breaking blocks (Explosion of strength 10, doExplosionB only: sound and particles); instead every living thing but
 * its owner and tamed animals within 4.24 blocks takes 50 mob damage (charged by lightning: within 6 blocks, 100).
 * Then it is gone. Falling shortens the fuse.
 */
public class SpectralCreeper extends SpectralPet {
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(SpectralCreeper.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> POWERED = SynchedEntityData.defineId(SpectralCreeper.class, EntityDataSerializers.BOOLEAN);
    private int oldSwell;
    private int swell;
    private int maxSwell = 30;
    private int explosionRadius = 3;

    public SpectralCreeper(EntityType<? extends SpectralCreeper> type, Level level) {
        super(type, level, 10.0F, new Sounds(null, SoundEvents.CREEPER_HURT, SoundEvents.CREEPER_DEATH, HSMobs.sound("entity.wolf.step"), 0.4F));
        this.setupGoals();
    }

    @Override
    protected void setupGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SwellGoal(this));
        this.goalSelector.addGoal(3, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, this.factor(0.25), false));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F));
        this.goalSelector.addGoal(7, this.creepballTempt());
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.registerTargets(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATE, -1);
        builder.define(POWERED, false);
    }

    public int getSwellDir() {
        return this.entityData.get(STATE);
    }

    public void setSwellDir(int dir) {
        this.entityData.set(STATE, dir);
    }

    public boolean isPowered() {
        return this.entityData.get(POWERED);
    }

    /** getCreeperFlashIntensity */
    public float getSwelling(float a) {
        return (this.oldSwell + (this.swell - this.oldSwell) * a) / (this.maxSwell - 2);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
        boolean result = super.causeFallDamage(fallDistance, damageModifier, damageSource);
        this.swell = (int) (this.swell + fallDistance * 1.5F);
        if (this.swell > this.maxSwell - 5) {
            this.swell = this.maxSwell - 5;
        }
        return result;
    }

    @Override
    public void tick() {
        if (this.isAlive()) {
            this.oldSwell = this.swell;
            int dir = this.getSwellDir();
            if (dir > 0 && this.swell == 0) {
                this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
            }
            this.swell += dir;
            if (this.swell < 0) {
                this.swell = 0;
            }
            if (this.swell >= this.maxSwell) {
                this.swell = this.maxSwell;
                if (this.level() instanceof ServerLevel level) {
                    this.burst(level);
                    return;
                }
            }
        }
        super.tick();
    }

    private void burst(ServerLevel level) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4.0F,
                (1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F) * 0.7F);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        boolean powered = this.isPowered();
        double range = powered ? 36.0 : 18.0;
        LivingEntity owner = this.getOwner();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(7.0))) {
            if (e == owner || !e.isAlive() || this.distanceToSqr(e) >= range) {
                continue;
            }
            // the original skipped tamed animals of other owners (a string compared by reference: in effect all tamed ones)
            if (e instanceof TamableAnimal t && t.isTame()) {
                continue;
            }
            e.hurtServer(level, this.damageSources().mobAttack(this), powered ? 100.0F : 50.0F);
        }
        this.discard();
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt bolt) {
        super.thunderHit(level, bolt);
        this.entityData.set(POWERED, true);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.isPowered()) {
            output.putBoolean("powered", true);
        }
        output.putShort("Fuse", (short) this.maxSwell);
        output.putByte("ExplosionRadius", (byte) this.explosionRadius);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(POWERED, input.getBooleanOr("powered", false));
        this.maxSwell = input.getShortOr("Fuse", (short) 30);
        this.explosionRadius = input.getByteOr("ExplosionRadius", (byte) 3);
    }

    /** EntityAIHSSpectralCreeperSwell */
    static class SwellGoal extends Goal {
        private final SpectralCreeper creeper;
        private @Nullable LivingEntity target;

        SwellGoal(SpectralCreeper creeper) {
            this.creeper = creeper;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity t = this.creeper.getTarget();
            return this.creeper.getSwellDir() > 0 || t != null && this.creeper.distanceToSqr(t) < 9.0;
        }

        @Override
        public void start() {
            this.creeper.getNavigation().stop();
            this.target = this.creeper.getTarget();
        }

        @Override
        public void stop() {
            this.target = null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.target == null || this.creeper.distanceToSqr(this.target) > 49.0 || !this.creeper.getSensing().hasLineOfSight(this.target)) {
                this.creeper.setSwellDir(-1);
            } else {
                this.creeper.setSwellDir(1);
            }
        }
    }
}
