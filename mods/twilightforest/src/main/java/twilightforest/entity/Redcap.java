package twilightforest.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import twilightforest.TFEntities;
import twilightforest.entity.ai.RedcapGoals;

/**
 * EntityTFRedcap: a goblin with an iron pickaxe and iron boots (each dropped 1 in 5), 20 health, 0.9 x 1.4, speed
 * 0.28. While nothing has hit it lately it keeps away from a player looking at it; it lights TNT lying within 8 blocks
 * and runs from primed TNT. Drops coal.
 */
public class Redcap extends TFMonster {
    private int tntLeft;

    public Redcap(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.equip();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.28);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RedcapGoals.AvoidTnt(this, 2.0F, 1.0, 0.5 / 0.28));
        this.goalSelector.addGoal(2, new RedcapGoals.Shy(this, 1.0));
        this.goalSelector.addGoal(3, new RedcapGoals.LightTnt(this, 1.0));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    protected void equip() {
        this.setItemSlot(EquipmentSlot.MAINHAND, this.pick());
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.2F);
        this.setDropChance(EquipmentSlot.FEET, 0.2F);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        this.equip();
        return data;
    }

    @Override
    protected void populateDefaultEquipmentSlots(net.minecraft.util.RandomSource random, DifficultyInstance difficulty) {
    }

    public ItemStack pick() {
        return new ItemStack(Items.IRON_PICKAXE);
    }

    /** isShy: nobody has hit it in the last 100 ticks. */
    public boolean isShy() {
        return this.lastHurtByPlayerMemoryTime <= 0;
    }

    public int getTntLeft() {
        return this.tntLeft;
    }

    public void setTntLeft(int tntLeft) {
        this.tntLeft = tntLeft;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("TNTLeft", this.tntLeft);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.tntLeft = input.getIntOr("TNTLeft", 0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TFEntities.REDCAP_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TFEntities.REDCAP_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TFEntities.REDCAP_DIE.get();
    }
}
