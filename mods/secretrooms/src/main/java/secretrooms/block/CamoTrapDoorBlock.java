package secretrooms.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

/**
 * BlockCamoTrapDoor: a wooden trapdoor that hangs only on the side of a block and wears that block's look (taken
 * from the world while drawing, no block entity). It falls off when the block goes.
 */
public class CamoTrapDoorBlock extends TrapDoorBlock {
    public CamoTrapDoorBlock(BlockSetType type, Properties properties) {
        super(type, properties);
    }

    public static BlockPos support(BlockPos pos, BlockState state) {
        return pos.relative(state.getValue(FACING).getOpposite());
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!context.getClickedFace().getAxis().isHorizontal()) {
            return null;
        }
        BlockState state = super.getStateForPlacement(context);
        return state != null && state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos support = support(pos, state);
        BlockState s = level.getBlockState(support);
        return s.isFaceSturdy(level, support, state.getValue(FACING)) || s.is(Blocks.GLOWSTONE);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighborPos,
            BlockState neighbor, RandomSource random) {
        if (direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
    }
}
