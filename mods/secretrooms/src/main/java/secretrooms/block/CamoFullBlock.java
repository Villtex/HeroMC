package secretrooms.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import org.jspecify.annotations.Nullable;

/**
 * BlockCamoFull: a full block that copies a neighbour when placed and looks like it (the client model wraps the
 * copied block's model). Pick block gives the copied block, as idPicked did.
 */
public class CamoFullBlock extends BaseEntityBlock implements CamoSource {
    public CamoFullBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CamoBlockEntity(pos, state);
    }

    @Override
    public @Nullable BlockState chooseCamo(Level level, BlockPos pos, BlockState state) {
        return CamoSource.full(level, pos);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        if (level.getBlockEntity(pos) instanceof CamoBlockEntity camo && camo.camo() != null) {
            return new ItemStack(camo.camo().getBlock());
        }
        return super.getCloneItemStack(level, pos, state, includeData);
    }

    protected static void notifyAround(Level level, BlockPos pos, BlockState state) {
        level.updateNeighborsAt(pos, state.getBlock());
    }

    protected static boolean powered(Level level, BlockPos pos) {
        return level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
    }
}
