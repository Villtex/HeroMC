package extrabiomes.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

/** BlockCatTail: on grass or dirt that has water beside it. */
public class CattailBlock extends EbxlPlantBlock {
    public CattailBlock(VoxelShape shape, Properties properties) {
        super(state -> state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT), shape, properties);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos soil = pos.below();
        if (!this.mayPlaceOn(level.getBlockState(soil), level, soil)) {
            return false;
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (level.getFluidState(soil.relative(side)).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT);
    }
}
