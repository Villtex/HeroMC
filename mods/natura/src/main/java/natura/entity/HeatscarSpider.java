package natura.entity;

import natura.NaturaEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;

/**
 * HeatscarSpider: a fire-proof spider twice the size, 50 health, 4 damage, 50 experience. It hunts players within
 * 24 blocks in any light, jumps 0.62 high, takes fall damage only from 5 blocks, and sets what it bites on fire (5 s on
 * normal, 10 s on hard). One in ten spawns with a skeleton archer on its back. When it dies, 2 to 4 baby heatscar
 * spiders come out (Natura.txt of the pack: Minimum/Maximum Baby Heatscar Spiders on Spider Death = 2/4).
 * Drops (flamestring): loot table entities/heatscar_spider.
 */
public class HeatscarSpider extends Spider {
    public static final int BABIES_MIN = 2;
    public static final int BABIES_MAX = 4;

    public HeatscarSpider(EntityType<? extends HeatscarSpider> type, Level level) {
        super(type, level);
        this.xpReward = 50;
    }

    /** moveSpeed 1.35 against 0.8 of the 1.5.2 spider (0.3 in 26.3). */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 50.0).add(Attributes.MOVEMENT_SPEED, 0.3 * 1.35 / 0.8)
                .add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.FOLLOW_RANGE, 24.0).add(Attributes.JUMP_STRENGTH, 0.62)
                .add(Attributes.SAFE_FALL_DISTANCE, 5.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.62F));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (!super.doHurtTarget(level, target)) {
            return false;
        }
        if (target instanceof LivingEntity) {
            Difficulty difficulty = level.getDifficulty();
            int seconds = difficulty == Difficulty.NORMAL ? 5 : difficulty == Difficulty.HARD ? 10 : 0;
            if (seconds > 0) {
                target.igniteForSeconds(seconds);
            }
        }
        return true;
    }

    /** initCreature: no spider effects, a skeleton with a bow in one of ten. */
    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData != null ? groupData : BabyHeatscarSpider.NO_SPIDER_EFFECTS);
        this.ejectPassengers();
        if (level.getRandom().nextInt(10) == 0) {
            Skeleton skeleton = EntityTypes.SKELETON.create(this.level(), EntitySpawnReason.JOCKEY);
            if (skeleton != null) {
                skeleton.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                skeleton.finalizeSpawn(level, difficulty, reason, null);
                skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                skeleton.startRiding(this, false, false);
            }
        }
        return data;
    }

    /** setDead: the babies come out where the spider died. */
    @Override
    public void remove(RemovalReason reason) {
        if (reason == RemovalReason.KILLED && this.level() instanceof ServerLevel level) {
            int amount = this.random.nextInt(BABIES_MAX - BABIES_MIN + 1) + BABIES_MIN;
            for (int i = 0; i < amount; i++) {
                double dx = this.random.nextDouble() * 2.0;
                double dz = this.random.nextDouble() * 2.0;
                BabyHeatscarSpider baby = NaturaEntities.BABY_HEATSCAR_SPIDER.get().create(level, EntitySpawnReason.MOB_SUMMONED);
                if (baby != null) {
                    baby.snapTo(this.getX() + dx, this.getY() + 0.5, this.getZ() + dz, this.random.nextFloat() * 360.0F, 0.0F);
                    level.addFreshEntity(baby);
                }
            }
        }
        super.remove(reason);
    }
}
