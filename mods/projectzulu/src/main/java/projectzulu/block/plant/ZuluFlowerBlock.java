package projectzulu.block.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockFlower of 1.5.2, the base of the Zulu plants: bounds 0.3..0.7 wide and 0.6 high, no collision, and
 * canBlockStay = (full light >= 8 or sky above) and the soil below ({@link #mayPlaceOn}, canThisPlantGrowOnThisBlockID).
 */
public abstract class ZuluFlowerBlock extends VegetationBlock {
    private static final VoxelShape SHAPE = Block.box(4.8, 0.0, 4.8, 11.2, 9.6, 11.2);

    protected ZuluFlowerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected abstract boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos);

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return (level.getRawBrightness(pos, 0) >= 8 || level.canSeeSky(pos)) && super.canSurvive(state, level, pos);
    }
}
