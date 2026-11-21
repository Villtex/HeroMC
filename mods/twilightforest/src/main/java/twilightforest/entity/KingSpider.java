package twilightforest.entity;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import org.jspecify.annotations.Nullable;
import twilightforest.TFEntities;

/**
 * EntityTFKingSpider: a spider of 30 health, 1.6 x 1.6, 1.9 times the size, speed 0.35, bite 6, that cannot climb
 * walls and avoids water. It comes with a skeleton druid on its back.
 */
public class KingSpider extends TFSpider {
    public KingSpider(EntityType<? extends Spider> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.35);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.35F));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.2 / 0.35));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int attackStrength(Entity target) {
        return 6;
    }

    @Override
    public float renderScale() {
        return 1.9F;
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public boolean isClimbing() {
        return false;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        SkeletonDruid druid = TFEntities.SKELETON_DRUID.get().create(this.level(), EntitySpawnReason.JOCKEY);
        if (druid != null) {
            druid.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
            druid.finalizeSpawn(level, difficulty, reason, null);
            druid.startRiding(this, false, false);
        }
        return data;
    }
}
