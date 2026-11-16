package natura.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/**
 * SaguaroBlock metadata 3-6: a fruit hanging on the side of a trunk. {@code facing} points to the trunk
 * (3 = east, 4 = south, 5 = west, 6 = north). Breaks into a saguaro fruit.
 */
public class SaguaroFruitBlock extends HorizontalDirectionalBlock {
    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.EAST, Block.box(10, 3, 4, 16, 12, 12), Direction.WEST, Block.box(0, 3, 4, 6, 12, 12),
            Direction.SOUTH, Block.box(4, 3, 10, 12, 12, 16), Direction.NORTH, Block.box(4, 3, 0, 12, 12, 6));

    public SaguaroFruitBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    /** 1.5.2 metadata 3-6 to the direction of the trunk. */
    public static Direction facing(int meta) {
        return switch (meta) {
            case 3 -> Direction.EAST;
            case 4 -> Direction.SOUTH;
            case 5 -> Direction.WEST;
            default -> Direction.NORTH;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }
}
