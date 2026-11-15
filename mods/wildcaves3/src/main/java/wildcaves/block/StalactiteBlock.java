package wildcaves.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * One piece of a stalactite or stalagmite (BlockStalactite, one metadata value of the stone or sandstone block).
 * Pieces 1, 2, 3, 7 and 11 hang from a solid block above, 6, 8, 9, 10 and 12 stand on one below, 0, 4 and 5 need
 * either, each through the other pieces of the column. Without hold it breaks. No collision ("Solid
 * stalactites/stalgmites" = false); walking through slows like the original (motion x 0.7).
 */
public class StalactiteBlock extends Block {
    private final int piece;
    private final VoxelShape shape;

    public StalactiteBlock(int piece, Properties properties) {
        super(properties);
        this.piece = piece;
        this.shape = shape(piece);
    }

    /** setBlockBoundsBasedOnState (BlockStalactite and BlockDecorations share it). */
    public static VoxelShape shape(int meta) {
        return switch (meta) {
            case 1 -> Block.box(4, 3.2, 4, 12, 16, 12);
            case 2 -> Block.box(4, 8, 4, 12, 16, 12);
            case 9 -> Block.box(4, 0, 4, 12, 12.8, 12);
            case 10 -> Block.box(4, 0, 4, 12, 6.4, 12);
            default -> Block.box(4, 0, 4, 12, 16, 12);
        };
    }

    public int piece() {
        return this.piece;
    }

    /** canBlockStay. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        int m = this.piece;
        if ((m == 0 || m >= 4) && m != 7 && m != 11) {
            if (m == 6 || m > 7 && m < 11 || m == 12) {
                return connected(level, pos, false);
            }
            return (m == 0 || m == 4 || m == 5) && (connected(level, pos, true) || connected(level, pos, false));
        }
        return connected(level, pos, true);
    }

    /** connected: past the pieces of the column (stone or sandstone) to the first other block, a normal cube? */
    static boolean connected(LevelReader level, BlockPos pos, boolean up) {
        BlockPos.MutableBlockPos at = pos.mutable();
        do {
            at.move(up ? Direction.UP : Direction.DOWN);
        } while (level.getBlockState(at).getBlock() instanceof StalactiteBlock);
        return level.getBlockState(at).isRedstoneConductor(level, at);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    /** onNeighborBlockChange: world.destroyBlock(x, y, z, true), a tick later (the shape update cannot drop). */
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            ticks.scheduleTick(pos, this, 1);
        }
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean precise) {
        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x * 0.7, motion.y, motion.z * 0.7);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }
}
