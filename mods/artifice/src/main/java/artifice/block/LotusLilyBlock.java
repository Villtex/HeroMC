package artifice.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LilyPadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * BlockLotus: a lily pad with a lotus on it (metadata 0 open, 1 closed), opening and closing with the light like the
 * lotus flower. Drops and picks as the open lotus lily.
 */
public class LotusLilyBlock extends LilyPadBlock {
    public static final BooleanProperty OPEN = LotusBlock.OPEN;

    public LotusLilyBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        LotusBlock.updateOpen(state, level, pos, OPEN);
    }
}
