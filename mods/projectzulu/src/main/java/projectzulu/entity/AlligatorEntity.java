package projectzulu.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import projectzulu.ZuluBigAnimals;

/**
 * EntityCrocodile ("Alligator" in the pack): 1.7 x 0.9, health 20 (EntityGenericAgeable default), no armor, speed
 * 0.25, damage 5 (original table 3/4/5/6). Swims with EntityAISmoothSwimming, never runs out of air
 * (decreaseAirSupply returns the supply unchanged) and is angry for 400-799 ticks whenever it is in water: then it
 * goes for the nearest player within 16 blocks. Tempted and bred with raw chicken; hurt sound crocoodileliving.
 * The doubled water speed of getSpeedModifier was a no-op in the original (it returned the unmodified value).
 */
public class AlligatorEntity extends ZuluBigAnimal {
    public AlligatorEntity(EntityType<? extends AlligatorEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SmoothSwimGoal(this));
        this.goalSelector.addGoal(3, new ReachMeleeAttackGoal(this, 1.0));
        this.goalSelector.addGoal(5, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(6, new TemptGoal(this, 1.0, stack -> stack.is(Items.CHICKEN), false));
        this.goalSelector.addGoal(7, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 1.0, 120));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        // the water anger has no target of its own: like the original angerLevel it means "the nearest player"
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, level) -> this.isAngryAt(target, level) || this.isAngry() && this.getPersistentAngerTarget() == null));
        this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    /** updateAITick: angerLevel = 400 + rand(400) every tick in water. */
    @Override
    public void customServerAiStep(ServerLevel level) {
        if (this.isInWater()) {
            this.setTimeToRemainAngry(400 + this.random.nextInt(400));
        }
        super.customServerAiStep(level);
    }

    @Override
    protected int decreaseAirSupply(int currentSupply) {
        return currentSupply;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ZuluBigAnimals.CROCODILE_LIVING.get();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.CHICKEN);
    }
}
