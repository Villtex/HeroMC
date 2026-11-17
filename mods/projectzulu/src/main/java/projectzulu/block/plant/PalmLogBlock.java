package projectzulu.block.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockPalmTreeLog: a plain wooden block (one texture on all sides; the axis of today only follows placement). Breaking
 * it lets the palm leaves within 4 blocks check for decay (beginLeavesDecay).
 */
public class PalmLogBlock extends RotatedPillarBlock {
    private static final int RANGE = 4;

    public PalmLogBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        PalmLeavesBlock.beginDecay(level, pos, RANGE);
    }
}
