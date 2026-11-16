package natura.entity;

import natura.NaturaEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.pig.PigSoundVariant;
import net.minecraft.world.entity.animal.pig.PigSoundVariants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import org.jspecify.annotations.Nullable;

/**
 * ImpEntity: a fire-proof Nether animal with 40 health and the sounds of a pig. It runs from players, follows and is
 * bred with Natura stews. Speeds of the 1.5.2 AI (base 0.25) as multipliers: panic 0.38, tempt 0.3, follow 0.28.
 * Drops (raw imp meat, imp leather): loot table entities/imp.
 */
public class ImpEntity extends Animal {
    public ImpEntity(EntityType<? extends ImpEntity> type, Level level) {
        super(type, level);
        // getNavigator().setAvoidsWater(true)
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.38 / 0.25));
        this.goalSelector.addGoal(2, new TemptGoal(this, 0.3 / 0.25, stack -> stack.is(NaturaEntities.IMP_TEMPT_ITEMS), false));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.0, 0.3 / 0.25));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 0.28 / 0.25));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    private static PigSoundVariant.PigSoundSet sounds() {
        return SoundEvents.PIG_SOUNDS.get(PigSoundVariants.SoundSet.CLASSIC).adultSounds();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return sounds().ambientSound().value();
    }

    /** mob.pig.say, as the original. */
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return sounds().ambientSound().value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return sounds().deathSound().value();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(sounds().stepSound().value(), 0.15F, 1.0F);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(NaturaEntities.IMP_FOOD);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return NaturaEntities.IMP.get().create(level, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
    }
}
