package twilightforest.entity.passive;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import twilightforest.entity.TFMobUtil;

/**
 * The squirrel and the bunny (EntityCreature + IAnimals): 1 health, 0.3 x 0.7, step height 1, no fall damage, never
 * despawn, avoid water. They panic (0.38), follow seeds (0.18), keep 2 blocks from players (0.23 / 0.4) and wander
 * (0.25 and 0.38). Path weight on leaves / wood / grass differs; elsewhere the 1.5.2 brightness - 0.5.
 */
public abstract class SmallCritter extends PathfinderMob {
    protected SmallCritter(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.STEP_HEIGHT, 1.0).add(Attributes.TEMPT_RANGE, 10.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.38 / 0.25));
        this.goalSelector.addGoal(2, new TemptGoal(this, 0.18 / 0.25, stack -> stack.is(Items.WHEAT_SEEDS), true));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 2.0F, 0.23 / 0.25, 0.4 / 0.25));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.38 / 0.25));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    /** getBlockPathWeight on leaves, wood and grass. */
    protected abstract float leaves();

    protected abstract float wood();

    protected abstract float grass();

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return weight(level, pos, this.leaves(), this.wood(), this.grass());
    }

    public static float weight(LevelReader level, BlockPos pos, float leaves, float wood, float grass) {
        BlockState under = level.getBlockState(pos.below());
        if (under.is(net.minecraft.tags.BlockTags.LEAVES)) {
            return leaves;
        }
        if (under.is(net.minecraft.tags.BlockTags.LOGS) || under.is(net.minecraft.tags.BlockTags.PLANKS)) {
            return wood;
        }
        if (under.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)) {
            return grass;
        }
        return TFMobUtil.lightBrightness(level, pos) - 0.5F;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
