package projectzulu.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.level.Level;
import projectzulu.ZuluSounds;

import java.util.EnumSet;

/**
 * EntityHornBill: always in the air (max flight height 20), fights back when hit (anger 400 ticks, then it dives at the
 * nearest player in sight within 16). Health 20, damage 5 (EntityGenericAnimal table 3/4/5/6), 0.8 x 1.2.
 */
public class HornBillEntity extends AerialAnimal {
    public HornBillEntity(EntityType<? extends HornBillEntity> type, Level level) {
        super(type, level);
        this.maxFlightHeight = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AerialAnimal.createAerialAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, AerialGoals.Chase.attackOnCollide(this));
        this.goalSelector.addGoal(6, new AerialGoals.FlyingWander(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new AerialGoals.StateTarget(this, EnumSet.of(EntityState.ATTACKING, EntityState.LOOKING)));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ZuluSounds.HORNBILL_LIVING.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ZuluSounds.HORNBILL_HURT.get();
    }
}
