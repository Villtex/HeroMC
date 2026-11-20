package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import twilightforest.TFBlocks;
import twilightforest.TwilightForest;

import java.util.EnumMap;
import java.util.Map;

/**
 * BlockTFCritter with BlockTFFirefly, BlockTFCicada and BlockTFMoonworm. {@link #FACING} points away from the block
 * the critter sits on (metadata 1 = on the block at x-1, facing east; 2 x+1; 3 z-1; 4 z+1; 5 on the floor; 6 under
 * the ceiling). A critter may sit on full opaque blocks, leaves and cactus-like blocks (cactus, hedge). When its
 * support goes it drops (the moonworm drops nothing). The animation is in {@link CritterBlockEntity}.
 */
public class CritterBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public enum Kind {
        FIREFLY(0.15F),
        CICADA(0.15F),
        MOONWORM(0.25F);

        final float wide;

        Kind(float wide) {
            this.wide = wide;
        }
    }

    private final Kind kind;
    private final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);

    public CritterBlock(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
        float w = kind.wide;
        boolean worm = kind == Kind.MOONWORM;
        // setBlockBoundsBasedOnState (in blocks); the moonworm is 0.25..0.75 along the wall, the others 0.2..0.8
        double lo = worm ? 0.25 : 0.2;
        double hi = worm ? 0.75 : 0.8;
        double thick = worm ? w : w * 2.0F;
        this.shapes.put(Direction.EAST, bounds(0.0, lo, 0.5 - w, thick, hi, 0.5 + w));
        this.shapes.put(Direction.WEST, bounds(1.0 - thick, lo, 0.5 - w, 1.0, hi, 0.5 + w));
        this.shapes.put(Direction.SOUTH, bounds(0.5 - w, lo, 0.0, 0.5 + w, hi, thick));
        this.shapes.put(Direction.NORTH, bounds(0.5 - w, lo, 1.0 - thick, 0.5 + w, hi, 1.0));
        this.shapes.put(Direction.UP, bounds(0.5 - w, 0.0, lo, 0.5 + w, thick, hi));
        this.shapes.put(Direction.DOWN, bounds(0.5 - w, 1.0 - thick, lo, 0.5 + w, 1.0, hi));
    }

    private static VoxelShape bounds(double x0, double y0, double z0, double x1, double y1, double z1) {
        return Block.box(x0 * 16.0, y0 * 16.0, z0 * 16.0, x1 * 16.0, y1 * 16.0, z1 * 16.0);
    }

    public Kind kind() {
        return this.kind;
    }

    /** Metadata of 1.5.2 to the facing (0 and unknown values: the floor, the chunk writer then looks for a support). */
    public static Direction facing(int meta) {
        return switch (meta & 7) {
            case 1 -> Direction.EAST;
            case 2 -> Direction.WEST;
            case 3 -> Direction.SOUTH;
            case 4 -> Direction.NORTH;
            case 6 -> Direction.DOWN;
            default -> Direction.UP;
        };
    }

    /** The facing as metadata of 1.5.2 (the renderers of the original work with it). */
    public static int meta(Direction facing) {
        return switch (facing) {
            case EAST -> 1;
            case WEST -> 2;
            case SOUTH -> 3;
            case NORTH -> 4;
            case UP -> 5;
            case DOWN -> 6;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shapes.get(state.getValue(FACING));
    }

    /** BlockTFCritter.canPlaceAt: isBlockNormalCube, Material.leaves or Material.cactus (cactus, hedge). */
    public static boolean canPlaceAt(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isSolidRender() || state.is(BlockTags.LEAVES) || state.is(Blocks.CACTUS) || state.is(TwilightForest.HEDGE.get())
                || state.is(TwilightForest.DARKWOOD_LEAVES.get());
    }

    /** onBlockAdded with metadata 0: the first support in the order x-1, x+1, z-1, z+1, below, above. */
    private static @Nullable Direction findSupport(LevelReader level, BlockPos pos) {
        for (Direction facing : new Direction[] {Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH, Direction.UP, Direction.DOWN}) {
            if (canPlaceAt(level, pos.relative(facing.getOpposite()))) {
                return facing;
            }
        }
        return null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canPlaceAt(level, pos.relative(state.getValue(FACING).getOpposite()));
    }

    /** onBlockPlaced: on the clicked face if it can hold the critter, else like onBlockAdded. */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (canPlaceAt(level, pos.relative(face.getOpposite()))) {
            return this.defaultBlockState().setValue(FACING, face);
        }
        Direction found = findSupport(level, pos);
        return found == null ? null : this.defaultBlockState().setValue(FACING, found);
    }

    /**
     * onNeighborBlockChange: the critter drops when its support goes. A critter of 1.5.2 metadata 0 (placed by the
     * world generation) first looks for a support, as onBlockAdded did.
     */
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (this.canSurvive(state, level, pos)) {
            return state;
        }
        Direction found = findSupport(level, pos);
        return found == null ? Blocks.AIR.defaultBlockState() : state.setValue(FACING, found);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CritterBlockEntity(pos, state);
    }

    /** The animation, the little fireflies and the song run on the client only. */
    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() && type == TFBlocks.CRITTER_ENTITY.get()
                ? (l, p, s, be) -> ((CritterBlockEntity) be).clientTick() : null;
    }
}
