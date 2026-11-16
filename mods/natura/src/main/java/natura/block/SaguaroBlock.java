package natura.block;

import natura.NaturaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/**
 * SaguaroBlock metadata 0: the trunk of the saguaro cactus. Rendered like SaguaroRenderer: a 12/16 wide column,
 * up to the block above when that is a trunk ({@code up}), lifted by 2/16 over air ({@code down} false), with an arm
 * to a neighbouring trunk when this block or the neighbour has air below. Hurts like a cactus. In rain a trunk with air
 * above sometimes grows a fruit on one of its sides (metadata 3-6, {@link SaguaroFruitBlock}).
 */
public class SaguaroBlock extends Block {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    private static final Map<Direction, BooleanProperty> ARMS = Map.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH,
            Direction.WEST, WEST);
    private static final VoxelShape COLLISION = Block.box(2, 0, 2, 14, 14, 14);
    private static final Map<Direction, VoxelShape> ARM_SHAPES = Map.of(
            Direction.NORTH, Block.box(2, 2, 0, 14, 14, 2), Direction.SOUTH, Block.box(2, 2, 14, 14, 14, 16),
            Direction.WEST, Block.box(0, 2, 2, 2, 14, 14), Direction.EAST, Block.box(14, 2, 2, 16, 14, 14));

    public SaguaroBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false)
                .setValue(WEST, false).setValue(UP, false).setValue(DOWN, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    /** Below a saguaro: sand, another saguaro block or air (canBlockStay of the original). */
    public static boolean canStandOn(BlockState below) {
        return below.isAir() || below.is(BlockTags.SAND) || below.is(NaturaBlocks.SAGUARO.get()) || below.is(NaturaBlocks.SAGUARO_SPROUT.get())
                || below.is(NaturaBlocks.SAGUARO_FRUIT.get());
    }

    private BlockState connect(BlockState state, BlockGetter level, BlockPos pos) {
        boolean airBelow = level.getBlockState(pos.below()).isAir();
        state = state.setValue(DOWN, !airBelow).setValue(UP, level.getBlockState(pos.above()).is(this));
        for (Map.Entry<Direction, BooleanProperty> arm : ARMS.entrySet()) {
            BlockPos side = pos.relative(arm.getKey());
            boolean connected = level.getBlockState(side).is(this) && (airBelow || level.getBlockState(side.below()).isAir());
            state = state.setValue(arm.getValue(), connected);
        }
        return state;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.connect(this.defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canStandOn(level.getBlockState(pos.below()));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction == Direction.DOWN && !canStandOn(neighborState)) {
            ticks.scheduleTick(pos, this, 1);
        }
        return this.connect(state, level, pos);
    }

    /** onNeighborBlockChange: drops when the block below can no longer carry it. */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Block.box(2, state.getValue(DOWN) ? 0 : 2, 2, 14, state.getValue(UP) ? 16 : 14, 14);
        for (Map.Entry<Direction, BooleanProperty> arm : ARMS.entrySet()) {
            if (state.getValue(arm.getValue())) {
                shape = Shapes.or(shape, ARM_SHAPES.get(arm.getKey()));
            }
        }
        return shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        if (!(entity instanceof ItemEntity) && level instanceof ServerLevel server) {
            entity.hurtServer(server, level.damageSources().cactus(), 1.0F);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    /** In rain, 1/20: a fruit on a free side, facing the trunk. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isRaining() && random.nextInt(20) == 0 && level.isEmptyBlock(pos.above())) {
            Direction side = switch (random.nextInt(4)) {
                case 0 -> Direction.EAST;
                case 1 -> Direction.SOUTH;
                case 2 -> Direction.WEST;
                default -> Direction.NORTH;
            };
            BlockPos fruit = pos.relative(side);
            if (level.isEmptyBlock(fruit)) {
                level.setBlock(fruit, NaturaBlocks.SAGUARO_FRUIT.get().defaultBlockState().setValue(SaguaroFruitBlock.FACING, side.getOpposite()),
                        Block.UPDATE_ALL);
            }
        }
    }
}
