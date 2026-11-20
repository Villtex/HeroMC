package secretrooms.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import secretrooms.SecretRooms;

/**
 * BlockOneWay: glass on every side but the one it faces, which shows the copied block. It faces the placer, or away
 * from them after the one-way key (backslash) was pressed. Copies one of the four blocks around its facing axis.
 */
public class OneWayGlassBlock extends CamoFullBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public OneWayGlassBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction dir = context.getNearestLookingDirection().getOpposite();
        Player player = context.getPlayer();
        if (player != null && !SecretRooms.facesTowards(player)) {
            dir = dir.getOpposite();
        }
        return this.defaultBlockState().setValue(FACING, dir);
    }

    @Override
    public @Nullable BlockState chooseCamo(Level level, BlockPos pos, BlockState state) {
        return CamoSource.around(level, pos, state.getValue(FACING));
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState neighbor, Direction direction) {
        return neighbor.is(this) || super.skipRendering(state, neighbor, direction);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return state.getValue(FACING) == Direction.UP;
    }
}
