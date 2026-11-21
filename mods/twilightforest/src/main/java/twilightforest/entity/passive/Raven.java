package twilightforest.entity.passive;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import twilightforest.TFEntities;

/**
 * EntityTFRaven: a tiny bird with 10 health, 0.3 x 0.7, step height 1, that is spooked only by being hurt; drops raven
 * feathers. Its look helper (EntityTFRavenLookHelper) keeps the pitch at 0 and turns the head 30 degrees past the
 * target, so the raven eyes things sideways.
 */
public class Raven extends TinyBird {
    public Raven(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.lookControl = new RavenLookControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.38 / 0.25));
        this.goalSelector.addGoal(2, new TemptGoal(this, 0.18 / 0.25, stack -> stack.is(Items.WHEAT_SEEDS), true));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TFEntities.RAVEN_CAW.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TFEntities.RAVEN_SQUAWK.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TFEntities.RAVEN_SQUAWK.get();
    }

    @Override
    public boolean isSpooked() {
        return this.hurtTime > 0;
    }

    /** EntityTFRavenLookHelper. */
    static final class RavenLookControl extends LookControl {
        RavenLookControl(Raven raven) {
            super(raven);
        }

        @Override
        public void tick() {
            this.mob.setXRot(0.0F);
            if (this.lookAtCooldown > 0) {
                this.lookAtCooldown--;
                double dx = this.wantedX - this.mob.getX();
                double dy = this.wantedY - this.mob.getEyeY();
                double dz = this.wantedZ - this.mob.getZ();
                double horizontal = Math.sqrt(dx * dx + dz * dz);
                float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 30.0F;
                float pitch = (float) (-(Math.atan2(dy, horizontal) * 180.0 / Math.PI));
                this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), pitch, this.xMaxRotAngle));
                this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, yaw, this.yMaxRotSpeed);
            } else {
                this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, this.mob.yBodyRot, 10.0F);
            }
            float diff = Mth.wrapDegrees(this.mob.yHeadRot - this.mob.yBodyRot);
            if (!this.mob.getNavigation().isDone()) {
                if (diff < -75.0F) {
                    this.mob.yHeadRot = this.mob.yBodyRot - 75.0F;
                }
                if (diff > 75.0F) {
                    this.mob.yHeadRot = this.mob.yBodyRot + 75.0F;
                }
            }
        }
    }
}
