package xreliquary.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** BlockWraithNode: a small cube (2..14 px) the wraith's eye binds to. */
public class WraithNodeBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(2.0, 2.0, 2.0, 14.0, 14.0, 14.0);

    public WraithNodeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
