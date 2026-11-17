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
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import projectzulu.ZuluEntities;
import projectzulu.ZuluSounds;

/**
 * EntityPenguin: fights back when a player hits it (anger 400 ticks), bred and tempted with raw or cooked fish
 * (cod). Health 15, armor 0, speed 0.25, damage 4 (original table 3/3/4/6). Size 0.9 x 1.5. penguinhurt is both
 * ambient and hurt sound. The mob head rare drop belongs to the loot table (tools/gen.py).
 */
public class PenguinEntity extends ZuluLandAnimal {
    public PenguinEntity(EntityType<? extends PenguinEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 15.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FleeGoal(this, 1.0, this::isFleeing));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(6, new TemptGoal(this, 1.0, PenguinEntity::isFish, false));
        this.goalSelector.addGoal(7, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 1.0, 120));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ZuluSounds.PENGUIN_HURT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ZuluSounds.PENGUIN_HURT.get();
    }

    /** Item.fishRaw / Item.fishCooked of 1.5.2 (there was only the one fish). */
    private static boolean isFish(ItemStack stack) {
        return stack.is(Items.COD) || stack.is(Items.COOKED_COD);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return isFish(stack);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ZuluEntities.PENGUIN.get().create(level, EntitySpawnReason.BREEDING);
    }
}
