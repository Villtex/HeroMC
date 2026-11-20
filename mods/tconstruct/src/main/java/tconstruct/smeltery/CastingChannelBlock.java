package tconstruct.smeltery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import tconstruct.TSmeltery;

/**
 * CastingChannelBlock: a seared gutter 6/16 wide between y 6 and 10 that reaches out to every side with a tank
 * (anything that holds liquid). Right-click (not sneaking) starts pulling liquid from neighbouring tanks.
 */
public class CastingChannelBlock extends BaseEntityBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    private final VoxelShape[] shapes = new VoxelShape[16];

    public CastingChannelBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, false).setValue(SOUTH, false).setValue(WEST, false).setValue(EAST, false));
        for (int i = 0; i < 16; i++) {
            double minZ = (i & 1) != 0 ? 0 : 5, maxZ = (i & 2) != 0 ? 16 : 11, minX = (i & 4) != 0 ? 0 : 5, maxX = (i & 8) != 0 ? 16 : 11;
            this.shapes[i] = Block.box(minX, 6, minZ, maxX, 10, maxZ);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, WEST, EAST);
    }

    static boolean holdsLiquid(BlockState state) {
        Block block = state.getBlock();
        return block instanceof SearedTankBlock || block instanceof CastingBlock || block instanceof CastingChannelBlock
                || block instanceof SmelteryDrainBlock;
    }

    private BlockState connect(BlockState state, BlockGetter level, BlockPos pos) {
        return state.setValue(NORTH, holdsLiquid(level.getBlockState(pos.north()))).setValue(SOUTH, holdsLiquid(level.getBlockState(pos.south())))
                .setValue(WEST, holdsLiquid(level.getBlockState(pos.west()))).setValue(EAST, holdsLiquid(level.getBlockState(pos.east())));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.connect(this.defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighbor, RandomSource random) {
        return this.connect(state, level, pos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int i = (state.getValue(NORTH) ? 1 : 0) | (state.getValue(SOUTH) ? 2 : 0) | (state.getValue(WEST) ? 4 : 0) | (state.getValue(EAST) ? 8 : 0);
        return this.shapes[i];
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChannelBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, TSmeltery.CHANNEL_ENTITY.get(), ChannelBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && !player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof ChannelBlockEntity channel) {
            channel.setActive();
        }
        return InteractionResult.SUCCESS;
    }
}
