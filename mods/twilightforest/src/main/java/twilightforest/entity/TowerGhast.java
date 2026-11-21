package twilightforest.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import twilightforest.gen.TFFeature;

/**
 * EntityTFTowerGhast: a ghast of the dark tower, 30 health, 4 x 6, fire proof. It stares at a player under the open
 * sky (or one within the stare range) and, after 20 ticks of that, spits fireballs (1 in 6 it calms down after a
 * shot). Without a target it wanders around the tower centre (128 above it, radius 64, y 64..210). Eyes: closed (0),
 * open (1), firing (2) for the texture. Ghast sounds, half as loud.
 */
public class TowerGhast extends TFFlyingMonster {
    private static final EntityDataAccessor<Byte> DATA_AGGRO = SynchedEntityData.defineId(TowerGhast.class, EntityDataSerializers.BYTE);
    protected @Nullable LivingEntity targeted;
    protected boolean isAggressive;
    protected int aggroCounter;
    protected float aggroRange = 64.0F;
    protected float stareRange = 32.0F;
    protected float wanderFactor = 16.0F;
    protected int inTrapCounter;
    public int attackCounter;
    public int prevAttackCounter;
    private @Nullable BlockPos home;

    public TowerGhast(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.FOLLOW_RANGE, 100.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_AGGRO, (byte) 0);
    }

    public int aggroStatus() {
        return this.entityData.get(DATA_AGGRO);
    }

    protected void setAggroStatus(byte status) {
        this.entityData.set(DATA_AGGRO, status);
    }

    @Override
    protected float getSoundVolume() {
        return 0.5F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 8;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.GHAST_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GHAST_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GHAST_DEATH;
    }

    @Override
    public int getMaxHeadXRot() {
        return 500;
    }

    @Override
    public void aiStep() {
        if (this.level().isClientSide() && this.random.nextBoolean()) {
            this.level().addParticle(DustParticleOptions.REDSTONE, this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                    this.getY() + this.random.nextDouble() * this.getBbHeight() - 0.25, this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                    0.0, 0.0, 0.0);
        }
        super.aiStep();
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.checkForTowerHome(level);
        if (this.inTrapCounter > 0) {
            this.inTrapCounter--;
            this.targeted = null;
            return;
        }
        this.prevAttackCounter = this.attackCounter;
        if (this.targeted != null && !this.targeted.isAlive()) {
            this.targeted = null;
        }
        if (this.targeted == null) {
            this.targeted = this.findPlayerInRange();
        } else if (!this.isAggressive && this.targeted instanceof Player player) {
            this.checkToIncreaseAggro(player);
        }
        double offsetX = this.waypointX - this.getX();
        double offsetY = this.waypointY - this.getY();
        double offsetZ = this.waypointZ - this.getZ();
        double distanceDesired = offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ;
        if ((distanceDesired < 1.0 || distanceDesired > 3600.0) && this.wanderFactor > 0.0F) {
            this.waypointX = this.getX() + (this.random.nextFloat() * 2.0F - 1.0F) * this.wanderFactor;
            this.waypointY = this.getY() + (this.random.nextFloat() * 2.0F - 1.0F) * this.wanderFactor;
            this.waypointZ = this.getZ() + (this.random.nextFloat() * 2.0F - 1.0F) * this.wanderFactor;
        }
        if (this.targeted == null && this.wanderFactor > 0.0F) {
            if (this.courseChangeCooldown-- <= 0) {
                this.courseChangeCooldown += this.random.nextInt(20) + 20;
                distanceDesired = Math.sqrt(distanceDesired);
                if (!this.isWithinHomeDistance(Mth.floor(this.waypointX), Mth.floor(this.waypointY), Mth.floor(this.waypointZ))) {
                    int cx = (int) (Math.round(this.getBlockX() / 256.0) * 256L + 8L);
                    int cz = (int) (Math.round(this.getBlockZ() / 256.0) * 256L + 8L);
                    Vec3 homeVector = new Vec3(cx - this.getX(), twilightforest.gen.TFTerrain.SEALEVEL + 128 - this.getY(), cz - this.getZ()).normalize();
                    this.waypointX = this.getX() + homeVector.x * 16.0 + (this.random.nextFloat() * 2.0F - 1.0F) * 16.0F;
                    this.waypointY = this.getY() + homeVector.y * 16.0 + (this.random.nextFloat() * 2.0F - 1.0F) * 16.0F;
                    this.waypointZ = this.getZ() + homeVector.z * 16.0 + (this.random.nextFloat() * 2.0F - 1.0F) * 16.0F;
                }
                if (this.isCourseTraversable(distanceDesired)) {
                    this.setDeltaMovement(this.getDeltaMovement().add(offsetX / distanceDesired * 0.1, offsetY / distanceDesired * 0.1,
                            offsetZ / distanceDesired * 0.1));
                } else {
                    this.waypointX = this.getX();
                    this.waypointY = this.getY();
                    this.waypointZ = this.getZ();
                }
            }
        } else {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.75));
        }
        double targetRange = this.aggroCounter <= 0 && !this.isAggressive ? this.stareRange : this.aggroRange;
        if (this.targeted != null && this.targeted.distanceToSqr(this) < targetRange * targetRange && this.hasLineOfSight(this.targeted)) {
            this.lookAt(this.targeted, 10.0F, this.getMaxHeadXRot());
            if (this.isAggressive) {
                if (this.attackCounter == 10) {
                    level.levelEvent(null, 1015, this.blockPosition(), 0);
                }
                this.attackCounter++;
                if (this.attackCounter == 20) {
                    this.spitFireball(level);
                    this.attackCounter = -40;
                }
            }
        } else {
            this.isAggressive = false;
            this.targeted = null;
            Vec3 m = this.getDeltaMovement();
            float yaw = -((float) Math.atan2(m.x, m.z)) * 180.0F / (float) Math.PI;
            this.setYRot(yaw);
            this.yBodyRot = yaw;
            this.setXRot(0.0F);
        }
        if (this.attackCounter > 0 && !this.isAggressive) {
            this.attackCounter--;
        }
        byte status = (byte) (this.attackCounter > 10 ? 2 : this.aggroCounter <= 0 && !this.isAggressive ? 0 : 1);
        if (this.aggroStatus() != status) {
            this.entityData.set(DATA_AGGRO, status);
        }
    }

    protected void spitFireball(ServerLevel level) {
        if (this.targeted == null) {
            return;
        }
        double offsetX = this.targeted.getX() - this.getX();
        double offsetY = this.targeted.getBoundingBox().minY + this.targeted.getBbHeight() / 2.0F - (this.getY() + this.getBbHeight() / 2.0F);
        double offsetZ = this.targeted.getZ() - this.getZ();
        level.levelEvent(null, 1016, this.blockPosition(), 0);
        LargeFireball fireball = new LargeFireball(level, this, new Vec3(offsetX, offsetY, offsetZ), 1);
        Vec3 look = this.getViewVector(1.0F);
        fireball.setPos(this.getX() + look.x * 0.5, this.getY() + this.getBbHeight() / 2.0F + look.y * 0.5, this.getZ() + look.z * 0.5);
        level.addFreshEntity(fireball);
        if (this.random.nextInt(6) == 0) {
            this.isAggressive = false;
        }
    }

    protected @Nullable LivingEntity findPlayerInRange() {
        Player closest = this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), this.aggroRange,
                p -> p instanceof Player pl && !pl.isCreative() && !pl.isSpectator() && pl.isAlive());
        if (closest != null && (this.distanceTo(closest) < this.stareRange || this.shouldAttackPlayer(closest))) {
            return closest;
        }
        return null;
    }

    protected void checkToIncreaseAggro(Player player) {
        if (this.shouldAttackPlayer(player)) {
            if (this.aggroCounter == 0) {
                this.playSound(SoundEvents.GHAST_AMBIENT, 1.0F, 1.0F);
            }
            if (this.aggroCounter++ >= 20) {
                this.aggroCounter = 0;
                this.isAggressive = true;
            }
        } else {
            this.aggroCounter = 0;
        }
    }

    protected boolean shouldAttackPlayer(Player player) {
        return this.level().canSeeSky(player.blockPosition()) && player.hasLineOfSight(this);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && source.getDirectEntity() instanceof LivingEntity living) {
            this.targeted = living;
            this.isAggressive = true;
        }
        return hurt;
    }

    /** checkForTowerHome: inside the dark tower's area its home is 128 above the centre, radius 64; elsewhere it ages faster. */
    protected void checkForTowerHome(ServerLevel level) {
        if (this.home == null) {
            if (TFMobUtil.nearestFeature(this) != TFFeature.DARK_TOWER) {
                this.setNoActionTime(this.getNoActionTime() + 5);
            } else {
                int cx = (int) (Math.round(this.getBlockX() / 256.0) * 256L + 8L);
                int cz = (int) (Math.round(this.getBlockZ() / 256.0) * 256L + 8L);
                this.home = new BlockPos(cx, twilightforest.gen.TFTerrain.SEALEVEL + 128, cz);
            }
        }
    }

    protected boolean isWithinHomeDistance(int x, int y, int z) {
        if (this.home == null) {
            return true;
        }
        double dx = this.home.getX() - x;
        double dz = this.home.getZ() - z;
        return y > 64 && y < 210 && dx * dx + dz * dz < 64.0 * 64.0;
    }

    /** setInTrap: the ghast trap (Teil 24e) holds it 10 ticks. */
    public void setInTrap() {
        this.inTrapCounter = 10;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
