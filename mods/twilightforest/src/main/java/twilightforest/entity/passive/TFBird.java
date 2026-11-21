package twilightforest.entity.passive;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * EntityTFBird: a flapping animal without fall damage and step sounds; it falls slowly (motionY × 0.6 in the air).
 * Drops feathers (loot table). The flap values drive the wing animation (RenderTFBird.getWingRotation).
 */
public abstract class TFBird extends Animal {
    public float flapLength;
    public float flapIntensity;
    public float lastFlapIntensity;
    public float lastFlapLength;
    public float flapSpeed = 1.0F;

    protected TFBird(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.lastFlapLength = this.flapLength;
        this.lastFlapIntensity = this.flapIntensity;
        this.flapIntensity = (float) (this.flapIntensity + (this.onGround() ? -1 : 4) * 0.3);
        if (this.flapIntensity < 0.0F) {
            this.flapIntensity = 0.0F;
        }
        if (this.flapIntensity > 1.0F) {
            this.flapIntensity = 1.0F;
        }
        if (!this.onGround() && this.flapSpeed < 1.0F) {
            this.flapSpeed = 1.0F;
        }
        this.flapSpeed = (float) (this.flapSpeed * 0.9);
        if (!this.onGround() && this.getDeltaMovement().y < 0.0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
        }
        this.flapLength = this.flapLength + this.flapSpeed * 2.0F;
    }

    /** RenderTFBird.getWingRotation. */
    public float wingRotation(float partialTick) {
        float length = this.lastFlapLength + (this.flapLength - this.lastFlapLength) * partialTick;
        float intensity = this.lastFlapIntensity + (this.flapIntensity - this.lastFlapIntensity) * partialTick;
        return ((float) Math.sin(length) + 1.0F) * intensity;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    public boolean isBirdLanded() {
        return true;
    }
}
