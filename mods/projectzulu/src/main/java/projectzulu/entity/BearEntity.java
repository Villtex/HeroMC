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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import projectzulu.ZuluBigAnimals;

/**
 * EntityBear and its three kinds: fights back when hit and alerts the bears of the same kind within 20 blocks; while
 * angry it also goes for the nearest monster. Tempted by a spider eye but bred with raw or cooked fish (the original's
 * own mismatch). Speed 0.3; hurt sound bearliving, no ambient sound. Mob heads (rare drop) come from the loot tables.
 */
public abstract class BearEntity extends ZuluBigAnimal {
    protected BearEntity(EntityType<? extends BearEntity> type, Level level) {
        super(type, level);
    }

    protected static AttributeSupplier.Builder bearAttributes(double health, double armor, double damage) {
        return createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, damage)
                .add(Attributes.ARMOR, armor);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new ReachMeleeAttackGoal(this, 1.0));
        this.goalSelector.addGoal(5, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(6, new TemptGoal(this, 1.0, stack -> stack.is(Items.SPIDER_EYE), false));
        this.goalSelector.addGoal(7, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 1.0, 120));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, this.playerTargetGoal());
        this.targetSelector.addGoal(4, this.monsterTargetGoal());
        this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    @Override
    protected boolean shouldNotifySimilar() {
        return true;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ZuluBigAnimals.BEAR_LIVING.get();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.COD) || stack.is(Items.COOKED_COD);
    }

    /** EntityBlackBear: 1.0 x 1.35, health 15, armor 2, damage 4 (3/3/4/6). */
    public static class Black extends BearEntity {
        public Black(EntityType<? extends Black> type, Level level) {
            super(type, level);
        }

        public static AttributeSupplier.Builder createAttributes() {
            return bearAttributes(15.0, 2.0, 4.0);
        }
    }

    /** EntityBrownBear: 1.5 x 2.1, health 20, armor 4, damage 5 (3/4/5/7). */
    public static class Brown extends BearEntity {
        public Brown(EntityType<? extends Brown> type, Level level) {
            super(type, level);
        }

        public static AttributeSupplier.Builder createAttributes() {
            return bearAttributes(20.0, 4.0, 5.0);
        }
    }

    /** EntityPolarBear: 2.0 x 2.7, health 25, armor 6, damage 6 (3/5/6/7), attack reach squared 13. */
    public static class Polar extends BearEntity {
        public Polar(EntityType<? extends Polar> type, Level level) {
            super(type, level);
        }

        public static AttributeSupplier.Builder createAttributes() {
            return bearAttributes(25.0, 6.0, 6.0);
        }

        @Override
        protected double attackReachSqr() {
            return 13.0;
        }
    }
}
