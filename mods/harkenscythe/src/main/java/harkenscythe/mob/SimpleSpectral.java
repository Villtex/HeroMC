package harkenscythe.mob;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The spectral mobs without own behaviour: EntityHSSpectralChicken (no fall damage, falls slowly, flaps its wings),
 * Cow, Villager (humanoid goals, does not call for help) and Silverfish (bites for 1 when tamed, makes no step events).
 */
public class SimpleSpectral extends SpectralPet {
    public enum Kind {
        CHICKEN, COW, VILLAGER, SILVERFISH
    }

    private final Kind kind;
    // chicken wing flapping (client)
    public float flap;
    public float flapSpeed;
    public float oFlap;
    public float oFlapSpeed;
    private float flapping = 1.0F;

    public SimpleSpectral(EntityType<? extends SimpleSpectral> type, Level level, Kind kind, float health, Sounds sounds) {
        super(type, level, health, sounds);
        this.kind = kind;
        if (kind == Kind.SILVERFISH) {
            this.petDamageTamed = 1;
        }
        this.setupGoals();
    }

    public Kind kind() {
        return this.kind;
    }

    @Override
    protected void setupGoals() {
        if (this.kind == Kind.VILLAGER) {
            this.registerHumanoidGoals(true);
            this.registerTargets(false);
        } else {
            super.setupGoals();
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.kind == Kind.CHICKEN) {
            this.oFlap = this.flap;
            this.oFlapSpeed = this.flapSpeed;
            this.flapSpeed = this.flapSpeed + (this.onGround() ? -1.0F : 4.0F) * 0.3F;
            this.flapSpeed = Math.clamp(this.flapSpeed, 0.0F, 1.0F);
            if (!this.onGround() && this.flapping < 1.0F) {
                this.flapping = 1.0F;
            }
            this.flapping *= 0.9F;
            Vec3 motion = this.getDeltaMovement();
            if (!this.onGround() && motion.y < 0.0) {
                this.setDeltaMovement(motion.multiply(1.0, 0.6, 1.0));
            }
            this.flap += this.flapping * 2.0F;
        } else if (this.kind == Kind.SILVERFISH) {
            this.yBodyRot = this.getYRot();
        }
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
        return this.kind != Kind.CHICKEN && super.causeFallDamage(fallDistance, damageModifier, damageSource);
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return this.kind == Kind.SILVERFISH ? MovementEmission.EVENTS : super.getMovementEmission();
    }
}
