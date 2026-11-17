package projectzulu.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import projectzulu.ZuluBigAnimals;

/**
 * EntityRhino: 2.0 x 2.0, health 20, no armor, speed 0.2, damage 4 (original table 3/3/4/6), attack reach squared
 * 7.29. Fights back when hit and alerts the rhinos within 20 blocks. Tempted and bred with tall grass.
 */
public class RhinoEntity extends ZuluBigAnimal {
    public RhinoEntity(EntityType<? extends RhinoEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new ReachMeleeAttackGoal(this, 1.0));
        this.goalSelector.addGoal(5, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(6, new TemptGoal(this, 1.0, GorillaEntity::isTallGrass, false));
        this.goalSelector.addGoal(7, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 1.0, 120));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, this.playerTargetGoal());
        this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    @Override
    protected double attackReachSqr() {
        return 7.29F;
    }

    @Override
    protected boolean shouldNotifySimilar() {
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ZuluBigAnimals.RHINO_LIVING.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ZuluBigAnimals.RHINO_HURT.get();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return GorillaEntity.isTallGrass(stack);
    }
}
