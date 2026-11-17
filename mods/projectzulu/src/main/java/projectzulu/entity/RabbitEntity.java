package projectzulu.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import projectzulu.ZuluEntities;
import projectzulu.ZuluSounds;

/**
 * EntityRabbit: hops like the frog and flees for 80 ticks when a player hits it, together with every rabbit within
 * 20 blocks (shouldNotifySimilar). No attack task (damage table 1/1/2/3 never applied). Bred and tempted with a
 * carrot or an apple, wanders often (chance 1/40). Health 10, armor 0, speed 0.3. Size 0.5 x 0.5. No step sounds,
 * no ambient sound.
 */
public class RabbitEntity extends ZuluLandAnimal {
    private static final java.util.function.Predicate<ItemStack> FOOD = stack -> stack.is(Items.CARROT) || stack.is(Items.APPLE);

    public RabbitEntity(EntityType<? extends RabbitEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FleeGoal(this, 1.0, this::isFleeing));
        this.goalSelector.addGoal(5, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(6, new TemptGoal(this, 1.0, FOOD, false));
        this.goalSelector.addGoal(7, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 1.0, 40));
    }

    @Override
    protected boolean shouldPanic() {
        return true;
    }

    @Override
    protected boolean shouldNotifySimilar() {
        return true;
    }

    @Override
    protected boolean hops() {
        return true;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ZuluSounds.RABBIT_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ZuluSounds.RABBIT_DEAD.get();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return FOOD.test(stack);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ZuluEntities.RABBIT.get().create(level, EntitySpawnReason.BREEDING);
    }
}
