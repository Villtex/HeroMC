package wildcaves.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * Glowing cave plants (BlockFlora): glowcaps, glow weed, iceshrooms. They stand on any normal cube, on ice, or (the
 * upper half of the tall glowcap) on the lower half; without it they vanish without a drop.
 */
public class FloraBlock extends VegetationBlock {
    private final VoxelShape shape;
    /** The lower half of the tall glowcap (metadata 4), the only plant something else stands on. */
    private final Supplier<? extends Block> tallBase;

    public FloraBlock(int meta, Supplier<? extends Block> tallBase, Properties properties) {
        super(properties);
        this.tallBase = tallBase;
        this.shape = switch (meta) {
            case 1 -> Block.box(4, 0, 4, 9.6, 12, 12);
            case 2 -> Block.box(4, 0, 4, 12, 6.4, 12);
            default -> Block.box(4, 0, 4, 12, 16, 12);
        };
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.isRedstoneConductor(level, pos) || state.is(Blocks.ICE) || state.is(this.tallBase.get());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return this.mayPlaceOn(level.getBlockState(below), level, below);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shape;
    }
}
