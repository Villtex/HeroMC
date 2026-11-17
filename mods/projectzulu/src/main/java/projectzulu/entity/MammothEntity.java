package projectzulu.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import projectzulu.ZuluBigAnimals;

/**
 * EntityMammoth: 4.5 x 5.4, health 30, armor 6, speed 0.3, damage 8 (original table 5/6/8/9), attack reach squared
 * 30.25. Fights back when hit (head swing while attacking). Tempted with an egg but bred with leaves (the original's
 * own mismatch). Reduced knockback with a lift of 0.1. Texture: the snow coat in Taiga and TaigaHills, each with or
 * without saddle.
 * <p>
 * Not rideable, as in the original: EntityMammoth inherits the saddle flag of EntityGenericRideable, but
 * isRideable() stays false, so neither the saddle nor mounting is ever reached in interact(), and it is not
 * tameable either (no taming item), so its sit/follow-owner/owner-target tasks never run. The saddle flag ("Saddle"
 * in NBT, only settable by command) still switches to the saddle textures. causeStampede, riderAttackNearby and the
 * mammothstomp sound were never called and are left out.
 */
public class MammothEntity extends ZuluBigAnimal {
    private static final EntityDataAccessor<Boolean> SADDLED = SynchedEntityData.defineId(MammothEntity.class, EntityDataSerializers.BOOLEAN);

    public MammothEntity(EntityType<? extends MammothEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.ARMOR, 6.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new ReachMeleeAttackGoal(this, 1.0));
        this.goalSelector.addGoal(5, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(6, new TemptGoal(this, 1.0, stack -> stack.is(Items.EGG), false));
        this.goalSelector.addGoal(7, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 1.0, 120));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, this.playerTargetGoal());
        this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SADDLED, false);
    }

    public boolean isSaddled() {
        return this.entityData.get(SADDLED);
    }

    public void setSaddled(boolean saddled) {
        this.entityData.set(SADDLED, saddled);
    }

    @Override
    protected double attackReachSqr() {
        return 30.25;
    }

    @Override
    protected double knockbackLift() {
        return 0.1F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ZuluBigAnimals.MAMMOTH_LIVING.get();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.LEAVES);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Saddle", this.isSaddled());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSaddled(input.getBooleanOr("Saddle", false));
    }
}
