package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import twilightforest.TFBosses;

/**
 * BlockTFTrophy (1373) with TileEntityTFTrophy: the boss heads, placed like skulls (ItemTFTrophy). A standing trophy
 * turns in 16 steps, a wall trophy faces away from its wall. Bounds 0.25..0.75 × 0..0.5 standing, the hydra head
 * reaches through the whole block on a wall, the ur-ghast floats in the upper half. The renderer draws the boss model.
 */
public class TrophyBlock extends BaseEntityBlock {
    public enum Kind {
        HYDRA, NAGA, LICH, UR_GHAST
    }

    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    private static final VoxelShape STANDING = Block.box(4.0, 0.0, 4.0, 12.0, 8.0, 12.0);
    private static final VoxelShape UR_GHAST = Block.box(4.0, 8.0, 4.0, 12.0, 16.0, 12.0);
    private final Kind kind;

    public TrophyBlock(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
        this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, 0));
    }

    public Kind kind() {
        return this.kind;
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }

    /** onBlockPlacedBy: the rotation from the player's yaw in 16 steps, facing him (as a skull today). */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(ROTATION, net.minecraft.world.level.block.state.properties.RotationSegment.convertToSegment(context.getRotation() + 180.0F));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.kind == Kind.UR_GHAST ? UR_GHAST : STANDING;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TrophyBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? createTickerHelper(type, TFBosses.TROPHY_ENTITY.get(), (l, p, s, be) -> be.ticksExisted++) : null;
    }

    /** The wall variant: FACING points away from the wall. */
    public static class Wall extends BaseEntityBlock {
        public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
        private final Kind kind;

        public Wall(Kind kind, Properties properties) {
            super(properties);
            this.kind = kind;
            this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
        }

        public Kind kind() {
            return this.kind;
        }


        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FACING);
        }

        @Override
        public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
            for (Direction direction : context.getNearestLookingDirections()) {
                if (direction.getAxis().isHorizontal()) {
                    BlockState state = this.defaultBlockState().setValue(FACING, direction.getOpposite());
                    if (context.getLevel().getBlockState(context.getClickedPos().relative(direction)).isSolid()) {
                        return state;
                    }
                }
            }
            return null;
        }

        @Override
        protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            if (this.kind == Kind.UR_GHAST) {
                return UR_GHAST;
            }
            Direction facing = state.getValue(FACING);
            if (this.kind == Kind.HYDRA) {
                return facing.getAxis() == Direction.Axis.Z ? Block.box(4.0, 4.0, 0.0, 12.0, 12.0, 16.0) : Block.box(0.0, 4.0, 4.0, 16.0, 12.0, 12.0);
            }
            return switch (facing) {
                case SOUTH -> Block.box(4.0, 4.0, 0.0, 12.0, 12.0, 8.0);
                case WEST -> Block.box(8.0, 4.0, 4.0, 16.0, 12.0, 12.0);
                case EAST -> Block.box(0.0, 4.0, 4.0, 8.0, 12.0, 12.0);
                default -> Block.box(4.0, 4.0, 8.0, 12.0, 12.0, 16.0);
            };
        }

        @Override
        protected RenderShape getRenderShape(BlockState state) {
            return RenderShape.INVISIBLE;
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new TrophyBlockEntity(pos, state);
        }

        @Override
        public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
            return level.isClientSide() ? createTickerHelper(type, TFBosses.TROPHY_ENTITY.get(), (l, p, s, be) -> be.ticksExisted++) : null;
        }
    }
}
