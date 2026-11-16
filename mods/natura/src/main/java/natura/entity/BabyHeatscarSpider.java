package natura.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;

/**
 * BabyHeatscarSpider: a small fire-proof spider, 10 health. It hunts players within 16 blocks in any light and sets
 * what it bites on fire in one of three bites (2 s on normal, 4 s on hard). Drops (flamestring): entities/baby_heatscar_spider.
 */
public class BabyHeatscarSpider extends Spider {
    public BabyHeatscarSpider(EntityType<? extends BabyHeatscarSpider> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 16.0);
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
            int seconds = difficulty == Difficulty.NORMAL ? 2 : difficulty == Difficulty.HARD ? 4 : 0;
            if (seconds > 0 && this.random.nextInt(3) == 0) {
                target.igniteForSeconds(seconds);
            }
        }
        return true;
    }

    /** initCreature does nothing: no spider effects and no skeleton. */
    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData != null ? groupData : NO_SPIDER_EFFECTS);
        this.ejectPassengers();
        return data;
    }

    /**
     * Group data of another kind than Spider.SpiderEffectsGroupData: Spider.finalizeSpawn then gives no random effect.
     * Its 1 % skeleton is taken off again with ejectPassengers (it is not in the world yet).
     */
    static final SpawnGroupData NO_SPIDER_EFFECTS = new SpawnGroupData() {
    };
}
