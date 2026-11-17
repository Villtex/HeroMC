package projectzulu.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import projectzulu.ZuluBigAnimals;

/**
 * EntityTreeEnt: 1.7 x 3.0, health 30, armor 6, speed 0.3, damage 4 (original table 3/3/4/6). Fights back when hit;
 * while angry it also goes for the nearest monster. Cannot be bred or tempted. Ambient sound treeentliving (the
 * treeenthurt ogg was never used). Snow texture in Taiga and TaigaHills. Reduced knockback with a lift of 0.1.
 */
public class TreeEntEntity extends ZuluBigAnimal {
    public TreeEntEntity(EntityType<? extends TreeEntEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.ARMOR, 6.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new ReachMeleeAttackGoal(this, 1.0));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 1.0, 120));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, this.playerTargetGoal());
        this.targetSelector.addGoal(4, this.monsterTargetGoal());
        this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    @Override
    protected double knockbackLift() {
        return 0.1F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ZuluBigAnimals.TREEENT_LIVING.get();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }
}
