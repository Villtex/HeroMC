package tconstruct.deco;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.Map;

/**
 * GlassBlockConnected / GlassBlockConnectedMeta (clear glass, clear stained glass): glass whose faces join with the same
 * block next to them (shouldConnectToBlock: the same block, for the stained one also the same colour = the same block of
 * today). The six properties say which neighbours are the same glass; the generated multipart model picks one of the 16
 * border textures per face from the four neighbours in the plane of that face, and hides faces between two of them.
 */
public class ConnectedGlassBlock extends TransparentBlock {
    public static final Map<Direction, BooleanProperty> CONNECT = PipeBlock.PROPERTY_BY_DIRECTION;

    public ConnectedGlassBlock(Properties properties) {
        super(properties);
        BlockState state = this.stateDefinition.any();
        for (BooleanProperty property : CONNECT.values()) {
            state = state.setValue(property, false);
        }
        this.registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        CONNECT.values().forEach(builder::add);
    }

    private BlockState connect(BlockState state, BlockGetter level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            state = state.setValue(CONNECT.get(direction), level.getBlockState(pos.relative(direction)).is(this));
        }
        return state;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.connect(this.defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
                                     BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        return state.setValue(CONNECT.get(direction), neighbor.is(this));
    }
}
