package twilightforest.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import twilightforest.entity.ai.TFGoals;

/**
 * EntityTFMinotaur: 30 health, speed 0.25, a golden axe in hand, hits for 7. It charges (speed 0.55) from 4..8 blocks
 * and throws what it hits while charging up 0.4. Cow sounds. Drops 0..1 (+ looting) raw meef (steak when burning),
 * rarely a maze map focus.
 */
public class Minotaur extends TFMonster implements TFGoals.Flagged {
    private static final EntityDataAccessor<Boolean> DATA_CHARGING = SynchedEntityData.defineId(Minotaur.class, EntityDataSerializers.BOOLEAN);

    public Minotaur(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_AXE));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new TFGoals.ChargeAttack(this, 0.55 / 0.25));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CHARGING, false);
    }

    public boolean isCharging() {
        return this.entityData.get(DATA_CHARGING);
    }

    @Override
    public void setFlag(boolean on) {
        this.entityData.set(DATA_CHARGING, on);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_AXE));
        return data;
    }

    @Override
    protected void populateDefaultEquipmentSlots(net.minecraft.util.RandomSource random, DifficultyInstance difficulty) {
    }

    @Override
    public int attackStrength(Entity target) {
        return 7;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean success = super.doHurtTarget(level, target);
        if (success && this.isCharging()) {
            target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.4, 0.0));
            target.needsSync = true;
            this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
        }
        return success;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isCharging()) {
            this.walkAnimation.setSpeed(this.walkAnimation.speed() + 0.6F);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TFMobUtil.vanillaSound("entity.cow.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TFMobUtil.vanillaSound("entity.cow.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TFMobUtil.vanillaSound("entity.cow.hurt");
    }

    @Override
    protected void playStepSound(net.minecraft.core.BlockPos pos, BlockState state) {
        this.playSound(TFMobUtil.vanillaSound("entity.cow.step"), 0.15F, 1.0F);
    }
}
