package artifice.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

/**
 * BlockFrameDetector: when a neighbouring block (not another detector) changes while it is idle, it schedules a tick
 * (tickRate 2) and pulses: metadata 0 -> 1 (strong and weak power 15) -> 2 -> 0, two ticks per step, notifying the
 * blocks around each neighbour after every step.
 */
public class DetectorBlock extends FrameBlock {
    public static final IntegerProperty PULSE = IntegerProperty.create("pulse", 0, 2);
    private static final int TICK_RATE = 2;

    public DetectorBlock(Properties properties) {
        super(properties, true, true);
        this.registerDefaultState(this.stateDefinition.any().setValue(PULSE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PULSE);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(PULSE) == 1 ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.getSignal(state, level, pos, direction);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (block != this && state.getValue(PULSE) == 0 && !level.isClientSide()) {
            level.scheduleTick(pos, this, TICK_RATE);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int pulse = state.getValue(PULSE);
        if (pulse == 0 || pulse == 1) {
            level.setBlock(pos, state.setValue(PULSE, pulse + 1), Block.UPDATE_ALL);
            level.scheduleTick(pos, this, TICK_RATE);
        } else {
            level.setBlock(pos, state.setValue(PULSE, 0), Block.UPDATE_ALL);
        }
        // updateRedstone: notifyBlocksOfNeighborChange around each of the six neighbours (strong power)
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(pos.relative(direction), this);
        }
    }
}
