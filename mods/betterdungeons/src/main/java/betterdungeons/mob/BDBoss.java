package betterdungeons.mob;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * EntityBaseBoss: an EntityMob that never despawns (also not in peaceful, where setDead only went through at zero
 * health), with an animation flag byte (watcher 17, bit 0 "attacking") and the hook {@link #attackEntity} that
 * AIMonsterAttack calls every tick with the squared distance to the target. No boss bar: 1.5.2 only drew one for the
 * dragon and the wither (IBossDisplayData alone did nothing).
 *
 * <p>isAIEnabled: while it is false the AI tasks of 1.5.2 did not run at all (no goals, no target tasks); here the
 * move, look and jump flags are switched off, which stops those goals until it is true again. The target goals go on
 * (stopping them would drop the target, which the attacks need; in 1.5.2 it just stayed).
 */
public abstract class BDBoss extends Monster {
    private static final EntityDataAccessor<Byte> DATA_ANIM = SynchedEntityData.defineId(BDBoss.class, EntityDataSerializers.BYTE);

    protected BDBoss(EntityType<? extends BDBoss> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ANIM, (byte) 0);
    }

    /** AIMonsterAttack.updateTask: called with the squared distance to the target (from its feet). */
    public void attackEntity(Entity target, float distanceSq) {
    }

    /** isAIEnabled of 1.5.2. */
    protected boolean isAIEnabled() {
        return true;
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide()) {
            boolean ai = this.isAIEnabled();
            for (Goal.Flag flag : new Goal.Flag[] {Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP}) {
                this.goalSelector.setControlFlag(flag, ai);
            }
        }
        super.aiStep();
    }

    public boolean isAttacking() {
        return this.getAnimFlag(0);
    }

    public void setAttacking(boolean attacking) {
        this.setAnimFlag(0, attacking);
    }

    protected boolean getAnimFlag(int index) {
        return (this.entityData.get(DATA_ANIM) & 1 << index) != 0;
    }

    protected void setAnimFlag(int index, boolean value) {
        byte b = this.entityData.get(DATA_ANIM);
        this.entityData.set(DATA_ANIM, (byte) (value ? b | 1 << index : b & ~(1 << index)));
    }

    /** Watcher 17 as one byte (EntityGiant compares the whole byte with 1). */
    protected byte getAnimByte() {
        return this.entityData.get(DATA_ANIM);
    }

    protected void setAnimByte(byte value) {
        this.entityData.set(DATA_ANIM, value);
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return false;
    }

    /** EntityLiving's "damage.hurtflesh" for hurt and death. */
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_HURT;
    }

    // ------------------------------------------------------------------------------------------------ helpers

    /** swingItem. */
    public static void swingArm(LivingEntity entity) {
        entity.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, false);
    }

    /** MathHelper.wrapAngleTo180_double. */
    public static double wrap(double angle) {
        return Mth.wrapDegrees(angle);
    }

    /** BDHelper.getAngleBetweenEntities: the yaw from the entity towards the target. */
    public static double angleBetween(Entity entity, Entity target) {
        double d = entity.getX() - target.getX();
        double d2 = entity.getZ() - target.getZ();
        double angle = Math.toDegrees(Math.atan2(d, d2));
        return -wrap(angle - 180.0);
    }

    /** The "while (a > 360) a -= 360; while (a < 0) a += 360" of the originals. */
    public static double norm360(double angle) {
        while (angle > 360.0) {
            angle -= 360.0;
        }
        while (angle < 0.0) {
            angle += 360.0;
        }
        return angle;
    }

    /** The float version (the loops ran on floats there). */
    public static float norm360(float angle) {
        while (angle > 360.0F) {
            angle -= 360.0F;
        }
        while (angle < 0.0F) {
            angle += 360.0F;
        }
        return angle;
    }
}
