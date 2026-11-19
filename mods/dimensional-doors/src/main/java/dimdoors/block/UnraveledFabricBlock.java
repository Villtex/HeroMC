package dimdoors.block;

import dimdoors.world.DDWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

/** BlockLimbo, Unraveled Fabric: the ground of Limbo. Gravel lying on it joins the blocks that decay. */
public class UnraveledFabricBlock extends Block {
    public UnraveledFabricBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        if (!level.isClientSide() && level.getBlockState(pos.above()).is(Blocks.GRAVEL)) {
            DDWorld.data().blocksToDecay().add(new int[] {pos.getX(), pos.getY() + 1, pos.getZ()});
            DDWorld.data().setDirty();
        }
    }
}
