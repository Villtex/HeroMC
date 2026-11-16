package natura.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * NLeaves (and its subclasses for the rare, uncoloured and dark leaves). Natura leaves did not use the distance of
 * vanilla leaves: on every random tick a leaf that was not placed by a player looks for any block that sustains
 * leaves (a log) within 4 blocks in each direction and decays when there is none.
 */
public class NaturaLeavesBlock extends TintedParticleLeavesBlock {
    private static final int RANGE = 4;

    public NaturaLeavesBlock(Properties properties) {
        super(0.01F, properties);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(PERSISTENT);
    }

    @Override
    protected boolean decaying(BlockState state) {
        return false;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(PERSISTENT)) {
            return;
        }
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-RANGE, -RANGE, -RANGE), pos.offset(RANGE, RANGE, RANGE))) {
            if (level.getBlockState(p).is(BlockTags.LOGS)) {
                return;
            }
        }
        Block.dropResources(state, level, pos);
        level.removeBlock(pos, false);
    }
}
