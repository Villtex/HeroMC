package twilightforest.entity.passive;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jspecify.annotations.Nullable;

/**
 * EntityTFMobileFirefly: an ambient glow that flies to random points nearby like a bat (no pathfinder), 6 health,
 * 0.5 x 0.5, full bright. Spawns only below y 63 where the block light is not above a random 0..3. Bat sounds, quiet.
 */
public class MobileFirefly extends AmbientCreature {
    private @Nullable BlockPos currentFlightTarget;

    public MobileFirefly(EntityType<? extends AmbientCreature> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0);
    }

    @Override
    protected float getSoundVolume() {
        return 0.1F;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.95F;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BAT_DEATH;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.currentFlightTarget != null && (!level.isEmptyBlock(this.currentFlightTarget) || this.currentFlightTarget.getY() < 1)) {
            this.currentFlightTarget = null;
        }
        if (this.currentFlightTarget == null || this.random.nextInt(30) == 0
                || this.currentFlightTarget.distToLowCornerSqr((int) this.getX(), (int) this.getY(), (int) this.getZ()) < 4.0) {
            this.currentFlightTarget = new BlockPos((int) this.getX() + this.random.nextInt(7) - this.random.nextInt(7),
                    (int) this.getY() + this.random.nextInt(6) - 2, (int) this.getZ() + this.random.nextInt(7) - this.random.nextInt(7));
        }
        double speed = 0.05000000149011612;
        TinyBird.flyTowards(this, this.currentFlightTarget, speed, speed * 2.0);
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /** getGlowBrightness: sin(age / 7) + 1. */
    public float glowBrightness(float ageInTicks) {
        return (float) Math.sin(ageInTicks / 7.0) + 1.0F;
    }

    /** getCanSpawnHere: below y 63, light not above rand(4), and no collision (EntityLiving). */
    public static boolean checkSpawnRules(EntityType<MobileFirefly> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos,
            RandomSource random) {
        if (pos.getY() >= 63) {
            return false;
        }
        return level.getMaxLocalRawBrightness(pos) <= random.nextInt(4);
    }
}
