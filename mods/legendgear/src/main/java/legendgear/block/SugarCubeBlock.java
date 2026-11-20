package legendgear.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** BlockSugarCube: dissolves into 9 sugar when water touches it (sides or above) or rain falls on it. */
public class SugarCubeBlock extends Block {
    public SugarCubeBlock(Properties properties) {
        super(properties);
    }

    private void checkForDissolve(Level level, BlockPos pos) {
        if (!level.getBlockState(pos).is(this)) {
            return;
        }
        for (Direction d : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP}) {
            if (level.getFluidState(pos.relative(d)).is(FluidTags.WATER)) {
                this.dissolve(level, pos);
                return;
            }
        }
    }

    private void dissolve(Level level, BlockPos pos) {
        if (!level.isClientSide()) {
            level.destroyBlock(pos, true);
        } else {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
            net.minecraft.world.level.redstone.@org.jspecify.annotations.Nullable Orientation orientation, boolean moved) {
        this.checkForDissolve(level, pos);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        this.checkForDissolve(level, pos);
    }

    @Override
    public void handlePrecipitation(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation) {
        if (precipitation == Biome.Precipitation.RAIN && !level.isClientSide()) {
            level.destroyBlock(pos, true);
        }
    }
}
