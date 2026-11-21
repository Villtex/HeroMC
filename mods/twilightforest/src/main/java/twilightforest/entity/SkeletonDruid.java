package twilightforest.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import twilightforest.entity.projectile.NatureBolt;

/**
 * EntityTFSkeletonDruid: an undead with a golden hoe that throws nature bolts every 60 ticks from up to 10 blocks
 * (EntityAIArrowAttack), 20 health, speed 0.25, skeleton sounds. Drops 0..2 (+ looting) torchberries and bones.
 */
public class SkeletonDruid extends TFMonster implements RangedAttackMob {
    public SkeletonDruid(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_HOE));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RestrictSunGoal(this));
        this.goalSelector.addGoal(3, new FleeSunGoal(this, 1.0));
        this.goalSelector.addGoal(4, new RangedAttackGoal(this, 1.0, 60, 10.0F));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_HOE));
        return data;
    }

    @Override
    protected void populateDefaultEquipmentSlots(net.minecraft.util.RandomSource random, DifficultyInstance difficulty) {
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        NatureBolt bolt = new NatureBolt(this.level(), this);
        this.playSound(SoundEvents.GHAST_SHOOT, 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
        bolt.setTarget(target);
        double tx = target.getX() - this.getX();
        double ty = target.getY() + target.getEyeHeight() - 2.699999988079071 - this.getY();
        double tz = target.getZ() - this.getZ();
        float heightOffset = Mth.sqrt((float) (tx * tx + tz * tz)) * 0.2F;
        bolt.shootOld(tx, ty + heightOffset, tz, 0.6F, 6.0F);
        this.level().addFreshEntity(bolt);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }

    @Override
    protected void playStepSound(net.minecraft.core.BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SKELETON_STEP, 0.15F, 1.0F);
    }
}
