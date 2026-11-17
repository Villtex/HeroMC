package projectzulu.block.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import projectzulu.ZuluPlants;

/**
 * BlockCoconut (a BlockCocoa): hangs on the side of the topmost palm log ({@code facing} points to the log), grows
 * three stages with chance 1/5 per random tick, no bone meal. Drops a coconut seed, when ripe a whole coconut (loot
 * table). Placed with the coconut seed; pick block gives the seed.
 */
public class CoconutBlock extends CocoaBlock {
    public CoconutBlock(Properties properties) {
        super(properties);
    }

    /** canBlockStay: a palm log on the facing side, and no palm log above that log. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos log = pos.relative(state.getValue(FACING));
        return level.getBlockState(log).is(ZuluPlants.PALM_LOG.get()) && !level.getBlockState(log.above()).is(ZuluPlants.PALM_LOG.get());
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        } else if (level.getRandom().nextInt(5) == 0) {
            int age = state.getValue(AGE);
            if (age < 2) {
                level.setBlock(pos, state.setValue(AGE, age + 1), 3);
            }
        }
    }

    /** onNeighborBlockChange: any neighbour (the log above the support counts too). */
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour,
            BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        return state.canSurvive(level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return false;
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(ZuluPlants.COCONUT_SEED.get());
    }
}
