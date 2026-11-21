package twilightforest.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import twilightforest.TFEntities;

/**
 * EntityTFWraith: a flying ghost (EntityFlying, own action state) of 20 health that drifts to random waypoints within
 * 16 blocks. Once it has seen a player within 16 blocks for 20 ticks it swoops at him; touching (within 2 blocks,
 * overlapping in height) it hits for 5 every 20 ticks. Drops glowstone dust.
 */
public class Wraith extends TFFlyingMonster {
    private @Nullable Entity targeted;
    private int aggroCooldown;
    private int attackCounter;
    private int attackTime;

    public Wraith(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.5);
    }







    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.attackTime > 0) {
            this.attackTime--;
        }
        double d = this.waypointX - this.getX();
        double d1 = this.waypointY - this.getY();
        double d2 = this.waypointZ - this.getZ();
        double d3 = Math.sqrt(d * d + d1 * d1 + d2 * d2);
        if (d3 < 1.0 || d3 > 60.0) {
            this.waypointX = this.getX() + (this.random.nextFloat() * 2.0F - 1.0F) * 16.0F;
            this.waypointY = this.getY() + (this.random.nextFloat() * 2.0F - 1.0F) * 16.0F;
            this.waypointZ = this.getZ() + (this.random.nextFloat() * 2.0F - 1.0F) * 16.0F;
        }
        if (this.courseChangeCooldown-- <= 0) {
            this.courseChangeCooldown += this.random.nextInt(5) + 2;
            if (this.isCourseTraversable(d3)) {
                this.setDeltaMovement(this.getDeltaMovement().add(d / d3 * 0.1, d1 / d3 * 0.1, d2 / d3 * 0.1));
            } else {
                this.waypointX = this.getX();
                this.waypointY = this.getY();
                this.waypointZ = this.getZ();
                this.targeted = null;
            }
        }
        if (this.targeted != null && !this.targeted.isAlive()) {
            this.targeted = null;
        }
        if (this.targeted != null && this.aggroCooldown-- > 0) {
            float distance = this.targeted.distanceTo(this);
            if (this.hasLineOfSight(this.targeted)) {
                this.attack(level, this.targeted, distance);
            }
        } else {
            this.targeted = this.findPlayerToAttack();
            if (this.targeted != null) {
                this.aggroCooldown = 20;
            }
        }
        if (this.targeted != null && this.targeted.distanceToSqr(this) < 64.0 * 64.0) {
            double dx = this.targeted.getX() - this.getX();
            double dz = this.targeted.getZ() - this.getZ();
            float yaw = -((float) Math.atan2(dx, dz)) * 180.0F / (float) Math.PI;
            this.setYRot(yaw);
            this.yBodyRot = yaw;
            if (this.hasLineOfSight(this.targeted)) {
                this.attackCounter++;
                if (this.attackCounter == 20) {
                    this.waypointX = this.targeted.getX();
                    this.waypointY = this.targeted.getY() - this.targeted.getBbHeight() + 0.5;
                    this.waypointZ = this.targeted.getZ();
                    this.attackCounter = -40;
                }
            } else if (this.attackCounter > 0) {
                this.attackCounter--;
            }
        } else {
            Vec3 m = this.getDeltaMovement();
            float yaw = -((float) Math.atan2(m.x, m.z)) * 180.0F / (float) Math.PI;
            this.setYRot(yaw);
            this.yBodyRot = yaw;
            if (this.attackCounter > 0) {
                this.attackCounter--;
            }
        }
    }

    private void attack(ServerLevel level, Entity entity, float distance) {
        if (this.attackTime <= 0 && distance < 2.0F && entity.getBoundingBox().maxY > this.getBoundingBox().minY
                && entity.getBoundingBox().minY < this.getBoundingBox().maxY) {
            this.attackTime = 20;
            entity.hurtServer(level, this.damageSources().mobAttack(this), 5.0F);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (super.hurtServer(level, source, amount)) {
            Entity entity = source.getEntity();
            if (entity != null && entity != this && !this.hasPassenger(entity) && this.getVehicle() != entity) {
                this.targeted = entity;
            }
            return true;
        }
        return false;
    }

    private @Nullable Entity findPlayerToAttack() {
        Player player = this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), 16.0,
                p -> !((Player) p).isCreative() && !p.isSpectator() && p.isAlive());
        return player != null && this.hasLineOfSight(player) ? player : null;
    }


    @Override
    protected SoundEvent getAmbientSound() {
        return TFEntities.WRAITH_SOUND.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TFEntities.WRAITH_SOUND.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TFEntities.WRAITH_SOUND.get();
    }

}
