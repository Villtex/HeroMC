package artifice.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** BlockFlora (a BlockFlower on grass, dirt or farmland): bluebell, orchid and iris; the lotus is {@link LotusBlock}. */
public class FloraBlock extends VegetationBlock {
    /** BlockFlower: 0.3..0.7 wide, 0.6 high. */
    private static final VoxelShape SHAPE = Block.box(4.8, 0, 4.8, 11.2, 9.6, 11.2);

    public FloraBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
