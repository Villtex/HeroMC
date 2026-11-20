package tconstruct.smeltery;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

/**
 * SmelteryBlock metadata 2 and 4..11 (seared bricks, stone, cobblestone, paver, cracked, road, fancy, square,
 * chiseled): MultiServantLogic blocks that can make up the walls of a smeltery. A change next to them tells their
 * smeltery to check its structure again (onNeighborBlockChange).
 */
public class SearedBrickBlock extends BaseEntityBlock {
    public SearedBrickBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ServantBlockEntity(pos, state);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (!level.isClientSide()) {
            notify(level, pos);
        }
    }

    /** onNeighborBlockChange: servants tell their master, the master checks itself. */
    static void notify(Level level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof ServantBlockEntity servant) {
            servant.notifyMasterOfChange();
        } else if (entity instanceof MasterLogic master) {
            master.notifyChange(pos);
        }
    }
}
