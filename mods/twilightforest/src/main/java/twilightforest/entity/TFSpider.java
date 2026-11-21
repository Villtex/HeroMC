package twilightforest.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;

/**
 * The spiders of the Twilight Forest (EntitySpider subclasses with findPlayerToAttack overridden): they hunt players
 * within 16 blocks in any light, leap and bite. No spider effects or skeleton jockeys of today; the bite is the
 * 1.5.2 attack strength.
 */
public abstract class TFSpider extends Spider {
    /** Group data of another kind: Spider.finalizeSpawn then rolls no spider effects. */
    private static final SpawnGroupData NO_EFFECTS = new SpawnGroupData() {
    };

    protected TFSpider(EntityType<? extends Spider> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    /** EntitySpider: attackStrength 2. */
    public int attackStrength(Entity target) {
        return 2;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        return TFMonster.oldAttack(this, level, target, this.attackStrength(target));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData != null ? groupData : NO_EFFECTS);
        this.ejectPassengers();
        return data;
    }

    /** RenderSpider.preRenderCallback: spiderScaleAmount. */
    public float renderScale() {
        return 1.0F;
    }
}
