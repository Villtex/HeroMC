package extrabiomes.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Leaves of the ExtrabiomesXL trees. The redwood and the huge fir carry leaves up to 8 blocks from the trunk,
 * further than leaves of today reach (6). In 1.5.2 leaves only decayed after their decay bit was set, which
 * happened when a block next to them was broken. Here that bit is {@code check_decay}: it is set when a
 * neighbour turns to air (and passed on to leaves that touch marked leaves), and only marked leaves without a
 * log in reach decay. Generated trees therefore stand until someone cuts them.
 */
public class EbxlLeavesBlock extends TintedParticleLeavesBlock {
    public static final BooleanProperty CHECK_DECAY = BooleanProperty.create("check_decay");

    public EbxlLeavesBlock(Properties properties) {
        super(0.01F, properties);
        this.registerDefaultState(this.defaultBlockState().setValue(CHECK_DECAY, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CHECK_DECAY);
    }

    @Override
    protected boolean decaying(BlockState state) {
        return state.getValue(CHECK_DECAY) && super.decaying(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        BlockState updated = super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
        boolean mark = neighborState.isAir()
                || neighborState.getBlock() instanceof EbxlLeavesBlock && neighborState.getValue(CHECK_DECAY);
        if (mark && updated.is(this) && !updated.getValue(CHECK_DECAY)) {
            updated = updated.setValue(CHECK_DECAY, true);
        }
        return updated;
    }
}
