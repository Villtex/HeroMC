package extrabiomes.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Predicate;

/**
 * The small plants of ExtrabiomesXL: flowers, grasses, roots, toadstools. Each one names the soil it stands on
 * (canThisPlantGrowOnThisBlockID of the original) and its outline.
 */
public class EbxlPlantBlock extends VegetationBlock {
    private final Predicate<BlockState> soil;
    private final VoxelShape shape;

    public EbxlPlantBlock(Predicate<BlockState> soil, VoxelShape shape, Properties properties) {
        super(properties);
        this.soil = soil;
        this.shape = shape;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return this.soil.test(state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shape;
    }
}
