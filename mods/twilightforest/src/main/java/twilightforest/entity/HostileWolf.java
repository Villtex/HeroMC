package twilightforest.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import twilightforest.gen.TFFeature;

/**
 * EntityTFHostileWolf: a wolf that is always angry and hunts players within 16 blocks; it cannot be tamed or bred.
 * 10 health, bite 2 (the untamed wolf of 1.5.2). Spawns like a monster in the dark, in the hedge maze in any light.
 */
public class HostileWolf extends Wolf implements Enemy {
    public HostileWolf(EntityType<? extends Wolf> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Wolf.createAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            this.setPersistentAngerEndTime(Long.MAX_VALUE);
        }
    }

    public int attackStrength(Entity target) {
        return 2;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        return TFMonster.oldAttack(this, level, target, this.attackStrength(target));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return true;
    }

    /** getCanSpawnHere: monster light (isValidMobLightLevel) except in the hedge maze; no obstruction. */
    public static boolean checkSpawnRules(EntityType<? extends Wolf> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
            RandomSource random) {
        if (TFMobUtil.nearestFeature(level, pos) == TFFeature.HEDGE_MAZE) {
            return true;
        }
        return Monster.isDarkEnoughToSpawn(level, pos, random);
    }
}
