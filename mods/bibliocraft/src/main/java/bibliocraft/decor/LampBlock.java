package bibliocraft.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** BlockLamp "Fancy Lamp": a lamp with a hood of the block's colour, on the floor, the wall or the ceiling. */
public class LampBlock extends LightBlock {
    /** setBlockBoundsBasedOnState by style: floor 0.18..0.82 x 0..1, wall 0.1..0.9 x 0.05..0.55, ceiling 0.1..0.9 x 0..1. */
    private static final VoxelShape[] SHAPES = {
            Block.box(2.88, 0.0, 2.88, 13.12, 16.0, 13.12),
            Block.box(1.6, 0.8, 1.6, 14.4, 8.8, 14.4),
            Block.box(1.6, 0.0, 1.6, 14.4, 16.0, 14.4)};

    public LampBlock(int color, Properties properties) {
        super(color, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LightBlockEntity(Decor.LAMP_ENTITY.get(), pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[style(state)];
    }
}
