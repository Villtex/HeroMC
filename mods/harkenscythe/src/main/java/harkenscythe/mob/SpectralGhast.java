package harkenscythe.mob;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * EntityHSSpectralGhast: drifts towards random waypoints while it neither sits nor has a target, rises to its target,
 * sinks very slowly, shoots ghast fireballs (EntityAIArrowAttack 0.25, 20-60 ticks, 30 blocks; fires on the 15th count
 * while it sees the target, its face glows from the 13th) and dies at once from a fireball sent back by a player
 * other than its owner. Immune to fire.
 */
public class SpectralGhast extends SpectralPet implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> CHARGING = SynchedEntityData.defineId(SpectralGhast.class, EntityDataSerializers.BOOLEAN);
    private int courseChangeCooldown;
    private double waypointX;
    private double waypointY;
    private double waypointZ;
    private int attackCounter;
    private int explosionPower = 1;

    public SpectralGhast(EntityType<? extends SpectralGhast> type, Level level) {
        super(type, level, 10.0F, new Sounds(SoundEvents.GHAST_AMBIENT, SoundEvents.GHAST_HURT, SoundEvents.GHAST_DEATH, null, 0.1F));
        this.setupGoals();
    }

    @Override
    protected void setupGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, this.factor(0.25), 20, 60, 30.0F));
        this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.0, 50.0F, 10.0F));
        this.goalSelector.addGoal(6, this.creepballTempt());
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.registerTargets(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CHARGING, false);
    }

    public boolean isCharging() {
        return this.entityData.get(CHARGING);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.getDirectEntity() instanceof LargeFireball && source.getEntity() instanceof Player player) {
            if (this.isOwnedBy(player)) {
                return false;
            }
            super.hurtServer(level, source, 1000.0F);
            return true;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide() && !this.isOrderedToSit()) {
            Vec3 m = this.getDeltaMovement();
            if (this.getTarget() == null) {
                double dx = this.waypointX - this.getX();
                double dy = this.waypointY - this.getY();
                double dz = this.waypointZ - this.getZ();
                double d = dx * dx + dy * dy + dz * dz;
                if (d < 1.0 || d > 3600.0) {
                    this.waypointX = this.getX() + (this.random.nextFloat() * 3.0F - 1.0F) * 16.0F;
                    this.waypointY = this.getY() + (this.random.nextFloat() * 1.0F - 1.0F) * 16.0F;
                    this.waypointZ = this.getZ() + (this.random.nextFloat() * 3.0F - 1.0F) * 16.0F;
                }
                if (this.courseChangeCooldown-- <= 0) {
                    this.courseChangeCooldown += this.random.nextInt(5) + 2;
                    d = Math.sqrt(d);
                    if (this.isCourseTraversable(d)) {
                        m = m.add(dx / d * 0.1, dy / d * 1.0, dz / d * 0.1);
                    } else {
                        this.waypointX = this.getX();
                        this.waypointY = this.getY() + 1.0;
                        this.waypointZ = this.getZ();
                    }
                }
            }
            LivingEntity target = this.getTarget();
            if (target != null && target.getEyeY() > this.getEyeY()) {
                m = new Vec3(m.x, m.y + (0.3F - m.y) * 0.3F, m.z);
            }
            this.setDeltaMovement(m);
        }
        Vec3 m = this.getDeltaMovement();
        if (!this.onGround() && m.y < 0.0) {
            this.setDeltaMovement(m.multiply(1.0, 0.1, 1.0));
        }
        super.aiStep();
    }

    private boolean isCourseTraversable(double dist) {
        double sx = (this.waypointX - this.getX()) / dist;
        double sy = (this.waypointY - this.getY()) / dist;
        double sz = (this.waypointZ - this.getZ()) / dist;
        AABB box = this.getBoundingBox();
        for (int i = 1; i < dist; i++) {
            box = box.move(sx, sy, sz);
            if (!this.level().noCollision(this, box)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void performRangedAttack(LivingEntity unused, float power) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntity target = this.getTarget();
        if (target != null && target.distanceToSqr(this) < 64.0 * 64.0) {
            this.attackCounter++;
            double dx = target.getX() - this.getX();
            double dy = target.getBoundingBox().minY + target.getBbHeight() / 2.0F - (this.getY() + this.getBbHeight() / 2.0F);
            double dz = target.getZ() - this.getZ();
            this.setYRot(-((float) Mth.atan2(dx, dz)) * Mth.RAD_TO_DEG);
            this.yBodyRot = this.getYRot();
            if (this.hasLineOfSight(target)) {
                if (this.attackCounter == 13) {
                    level.levelEvent(null, 1015, this.blockPosition(), 0);
                }
                this.attackCounter++;
                if (this.attackCounter >= 15) {
                    level.levelEvent(null, 1016, this.blockPosition(), 0);
                    LargeFireball fireball = new LargeFireball(level, this, new Vec3(dx, dy, dz), this.explosionPower);
                    Vec3 look = this.getViewVector(1.0F);
                    fireball.setPos(this.getX() + look.x * 4.0, this.getY() + this.getBbHeight() / 2.0F + 0.5, this.getZ() + look.z * 4.0);
                    level.addFreshEntity(fireball);
                    this.attackCounter = 0;
                }
            }
        } else {
            Vec3 m = this.getDeltaMovement();
            this.setYRot(-((float) Mth.atan2(m.x, m.z)) * Mth.RAD_TO_DEG);
            this.yBodyRot = this.getYRot();
        }
        this.entityData.set(CHARGING, this.attackCounter > 13);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("ExplosionPower", this.explosionPower);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.explosionPower = input.getIntOr("ExplosionPower", 1);
    }
}
