package artifice.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * The lotus of BlockFlora (metadata 3 open, 4 closed): it closes when the light at it falls below 8 and opens again
 * from 8 on, checked on random ticks. It drops and picks as the open lotus (damageDropped / getDamageValue).
 */
public class LotusBlock extends FloraBlock {
    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    public LotusBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        updateOpen(state, level, pos, OPEN);
    }

    /** BlockFlora/BlockLotus.updateTick: getBlockLightValue < 8 closes, >= 8 opens (setBlockMetadataWithNotify flag 2). */
    static void updateOpen(BlockState state, ServerLevel level, BlockPos pos, BooleanProperty open) {
        boolean light = level.getMaxLocalRawBrightness(pos) >= 8;
        if (state.getValue(open) != light) {
            level.setBlock(pos, state.setValue(open, light), Block.UPDATE_CLIENTS);
        }
    }
}
