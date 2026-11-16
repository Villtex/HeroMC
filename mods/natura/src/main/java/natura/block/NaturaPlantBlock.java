package natura.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** FlowerBlock (bluebells): a flower on grass, dirt or farmland. */
public class NaturaPlantBlock extends VegetationBlock {
    private static final VoxelShape SHAPE = Block.box(4.8, 0, 4.8, 11.2, 9.6, 11.2);

    public NaturaPlantBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
