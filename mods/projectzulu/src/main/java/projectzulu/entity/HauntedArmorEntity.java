package projectzulu.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;

/**
 * EntityHauntedArmor: an empty suit of armour (iron or gold, with the matching sword) lying collapsed on the floor
 * while idle. A player within 5 blocks makes it assemble itself (wakeUpTimer 30 ticks, synced for the model) and
 * attack (anger 400). Health 25, speed 0.2, 0.6 x 1.8. Damage 4 + the sword's damage (iron 6, gold 4 in 1.5.2), not
 * scaled by difficulty in the original; here base 5 + the 26.3 sword modifier (iron +5, gold +3) gives the same 10/8.
 * Armour: the worn pieces + 2 (at most 20). Hurt and death sound "random.break". Drops its equipment only 1 in 4
 * times (then each piece with the usual 8.5 % chance). Structures (Teil 11) use {@link #setRandomArmor},
 * {@link #setHovering()} and setPersistenceRequired.
 */
public class HauntedArmorEntity extends ZuluMonster {
    private static final EntityDataAccessor<Integer> WAKE_UP_TIMER = SynchedEntityData.defineId(HauntedArmorEntity.class, EntityDataSerializers.INT);
    public static final int TIME_TO_WAKE_UP = 30;

    private boolean shouldHover;

    public HauntedArmorEntity(EntityType<? extends HauntedArmorEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ZuluMonster.createZuluAttributes()
                .add(Attributes.MAX_HEALTH, 25.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new StayStillGoal(this, State.IDLE));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 1.0, 120));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new StateTargetGoal<>(this, Player.class, ANGRY));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(WAKE_UP_TIMER, 0);
    }

    public int getWakeUpTimer() {
        return this.entityData.get(WAKE_UP_TIMER);
    }

    public void setHovering() {
        this.shouldHover = true;
    }

    /** setRandomArmor: a full iron or gold set with the matching sword. */
    public void setRandomArmor(RandomSource random) {
        boolean gold = random.nextInt(2) == 1;
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(gold ? Items.GOLDEN_SWORD : Items.IRON_SWORD));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(gold ? Items.GOLDEN_BOOTS : Items.IRON_BOOTS));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(gold ? Items.GOLDEN_LEGGINGS : Items.IRON_LEGGINGS));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(gold ? Items.GOLDEN_CHESTPLATE : Items.IRON_CHESTPLATE));
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(gold ? Items.GOLDEN_HELMET : Items.IRON_HELMET));
    }

    /** initCreature. */
    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        this.setRandomArmor(level.getRandom());
        return data;
    }

    @Override
    public void aiStep() {
        if (this.shouldHover) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.0, 1.0));
            if (this.tickCount > 200) {
                this.shouldHover = false;
            }
        }
        super.aiStep();
        if (!this.level().isClientSide()) {
            int wakeUp = this.getWakeUpTimer();
            if (this.getState() == State.IDLE && this.level().getNearestPlayer(this, 5.0) != null) {
                this.angerLevel = 400;
                wakeUp = TIME_TO_WAKE_UP;
            }
            this.entityData.set(WAKE_UP_TIMER, Math.max(wakeUp - 1, 0));
        }
    }

    @Override
    public boolean isNoGravity() {
        return this.shouldHover || super.isNoGravity();
    }

    /** getTotalArmorValue: worn armour + 2, at most 20. */
    @Override
    public int getArmorValue() {
        return Math.min(super.getArmorValue(), 20);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ITEM_BREAK.value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ITEM_BREAK.value();
    }

    /** dropEquipment only 1 in 4 times. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        if (this.random.nextInt(4) == 0) {
            super.dropCustomDeathLoot(level, source, killedByPlayer);
        }
    }
}
