package artifice.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockFrame and its kinds of 1.5.2: frame, blast wall, glass wall (and, as subclasses, scaffolding and the
 * detector). Hardness and explosion resistance are fixed per block (ArtificeBlocks.frames); this class keeps
 * isOpaqueCube (transparent frames let light and sky light through) and isBlockSolidOnSide, which the
 * scaffolding asks for its support ({@link #isSolidUp}).
 */
public class FrameBlock extends Block {
    private final boolean opaque;
    private final boolean solidTop;

    public FrameBlock(Properties properties, boolean opaque, boolean solidTop) {
        super(properties);
        this.opaque = opaque;
        this.solidTop = solidTop;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        // Block: lightOpacity 0 for blocks that are not opaque cubes
        return !this.opaque || super.propagatesSkylightDown(state);
    }

    /** World.isBlockSolidOnSide(x, y, z, UP): the 1.5.2 answer of Artifice frames, else a sturdy top face. */
    public static boolean isSolidUp(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof FrameBlock frame) {
            return frame.solidTop;
        }
        return state.isFaceSturdy(level, pos, Direction.UP);
    }
}
