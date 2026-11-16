package harkenscythe.mob;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * EntityHSSpectralWolf: shakes itself dry after water, wears a collar (red, dyeable with any dye while tamed) and wags
 * its tail by its health.
 */
public class SpectralWolf extends SpectralPet {
    private static final EntityDataAccessor<Integer> COLLAR = SynchedEntityData.defineId(SpectralWolf.class, EntityDataSerializers.INT);
    private boolean isWet;
    private boolean isShaking;
    private float shakeAnim;
    private float shakeAnimO;

    public SpectralWolf(EntityType<? extends SpectralWolf> type, Level level) {
        super(type, level, 8.0F, new Sounds(null, HSMobs.sound("entity.wolf.hurt"), HSMobs.sound("entity.wolf.death"),
                HSMobs.sound("entity.wolf.step"), 0.4F));
        this.setupGoals();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COLLAR, DyeColor.RED.getId());
    }

    public DyeColor getCollarColor() {
        return DyeColor.byId(this.entityData.get(COLLAR));
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (this.random.nextInt(3) == 0) {
            return this.isTame() && this.getHealth() < 10.0F ? HSMobs.sound("entity.wolf.whine") : HSMobs.sound("entity.wolf.pant");
        }
        return HSMobs.sound("entity.wolf.ambient");
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide() && this.isWet && !this.isShaking && !this.isPathFinding() && this.onGround()) {
            this.isShaking = true;
            this.shakeAnim = 0.0F;
            this.shakeAnimO = 0.0F;
            this.level().broadcastEntityEvent(this, (byte) 8);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isInWaterOrRain()) {
            this.isWet = true;
            this.isShaking = false;
            this.shakeAnim = 0.0F;
            this.shakeAnimO = 0.0F;
        } else if (this.isShaking) {
            if (this.shakeAnim == 0.0F) {
                this.playSound(SoundEvents.WOLF_SHAKE, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            }
            this.shakeAnimO = this.shakeAnim;
            this.shakeAnim += 0.05F;
            if (this.shakeAnimO >= 2.0F) {
                this.isWet = false;
                this.isShaking = false;
                this.shakeAnimO = 0.0F;
                this.shakeAnim = 0.0F;
            }
            if (this.shakeAnim > 0.4F && this.level().isClientSide()) {
                float y = (float) this.getY();
                int count = (int) (Mth.sin((this.shakeAnim - 0.4F) * Mth.PI) * 7.0F);
                for (int i = 0; i < count; i++) {
                    float dx = (this.random.nextFloat() * 2.0F - 1.0F) * this.getBbWidth() * 0.5F;
                    float dz = (this.random.nextFloat() * 2.0F - 1.0F) * this.getBbWidth() * 0.5F;
                    this.level().addParticle(ParticleTypes.SPLASH, this.getX() + dx, y + 0.8F, this.getZ() + dz,
                            this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z);
                }
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 8) {
            this.isShaking = true;
            this.shakeAnim = 0.0F;
            this.shakeAnimO = 0.0F;
        } else {
            super.handleEntityEvent(id);
        }
    }

    public boolean isShaking() {
        return this.isShaking;
    }

    public float getWetShade(float a) {
        return 0.75F + Mth.lerp(a, this.shakeAnimO, this.shakeAnim) / 2.0F * 0.25F;
    }

    public float getBodyRollAngle(float a, float offset) {
        float f = Mth.clamp((Mth.lerp(a, this.shakeAnimO, this.shakeAnim) + offset) / 1.8F, 0.0F, 1.0F);
        return Mth.sin(f * Mth.PI) * Mth.sin(f * Mth.PI * 11.0F) * 0.15F * Mth.PI;
    }

    /** getTailRotation: up by health when tamed. */
    public float getTailAngle() {
        return this.isTame() ? (0.55F - (20.0F - this.getHealth()) * 0.02F) * Mth.PI : (float) (Math.PI / 5);
    }

    @Override
    protected @Nullable InteractionResult interactTamed(Player player, InteractionHand hand, ItemStack stack) {
        DyeColor color = stack.getItem() instanceof DyeItem ? stack.get(net.minecraft.core.component.DataComponents.DYE) : null;
        if (color != null && this.getHealth() < 20.0F && !this.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION)) {
            if (color != this.getCollarColor()) {
                if (!this.level().isClientSide()) {
                    this.entityData.set(COLLAR, color.getId());
                }
                stack.consume(1, player);
                return InteractionResult.SUCCESS;
            }
        }
        return null;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("CollarColor", (byte) this.getCollarColor().getId());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(COLLAR, (int) input.getByteOr("CollarColor", (byte) DyeColor.RED.getId()));
    }
}
