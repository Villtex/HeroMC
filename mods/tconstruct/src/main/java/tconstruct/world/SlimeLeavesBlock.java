package tconstruct.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * SlimeLeaves (BlockLeaves): congealed slime holds them like a log. Like the other leaves of the port without the
 * distance of today's leaves: a leaf that was not placed by a player decays on a random tick when no log or congealed
 * slime is within 4 blocks.
 */
public class SlimeLeavesBlock extends TintedParticleLeavesBlock {
    private static final int RANGE = 4;

    public SlimeLeavesBlock(Properties properties) {
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
            BlockState found = level.getBlockState(p);
            if (found.is(BlockTags.LOGS) || found.getBlock() instanceof SlimeGelBlock) {
                return;
            }
        }
        Block.dropResources(state, level, pos);
        level.removeBlock(pos, false);
    }

    static boolean isGel(BlockState state) {
        return state.getBlock() instanceof SlimeGelBlock;
    }
}
