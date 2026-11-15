package offlawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.TriState;
import org.jspecify.annotations.Nullable;

/**
 * BeanstalkBlock: a climbable stalk (a ladder in 1.5.2) that holds on to solid ground below ({@link #STANDING},
 * metadata bit 2) or a solid ceiling above ({@link #HANGING}, bit 1), directly or through the stalk it is part of.
 * Without either it breaks and drops. Using a beanstalk on a stalk adds one to its free end (top first); flowers
 * grow on top of it.
 */
public class BeanstalkBlock extends Block {
    public static final BooleanProperty HANGING = BooleanProperty.create("hanging");
    public static final BooleanProperty STANDING = BooleanProperty.create("standing");
    /** setBlockBounds(0.15, 0, 0.15, 0.85, 1, 0.85). */
    private static final VoxelShape SHAPE = Block.box(2.4, 0.0, 2.4, 13.6, 16.0, 13.6);
    /** getCollisionBoundingBoxFromPool: only the thin middle (0.4 to 0.6) is solid. */
    private static final VoxelShape COLLISION = Block.box(6.4, 0.0, 6.4, 9.6, 16.0, 9.6);

    public BeanstalkBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HANGING, false).setValue(STANDING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HANGING, STANDING);
    }

    /** getCorrectMetaData. */
    private BlockState supported(LevelReader level, BlockPos pos) {
        boolean standing = false;
        boolean hanging = false;
        BlockPos below = pos.below();
        BlockState floor = level.getBlockState(below);
        if (floor.is(this)) {
            standing = floor.getValue(STANDING);
        } else if (floor.isFaceSturdy(level, below, Direction.UP)) {
            standing = true;
        }
        BlockPos above = pos.above();
        BlockState ceiling = level.getBlockState(above);
        if (ceiling.is(this)) {
            hanging = ceiling.getValue(HANGING);
        } else if (ceiling.isFaceSturdy(level, above, Direction.DOWN)) {
            hanging = true;
        }
        return this.defaultBlockState().setValue(STANDING, standing).setValue(HANGING, hanging);
    }

    private static boolean held(BlockState state) {
        return state.getValue(STANDING) || state.getValue(HANGING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.supported(context.getLevel(), context.getClickedPos());
        return held(state) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return held(this.supported(level, pos));
    }

    /** onNeighborBlockChange: take over the support of the neighbours; with none left, break (with drop) a tick later. */
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        BlockState now = this.supported(level, pos);
        if (!held(now)) {
            ticks.scheduleTick(pos, this, 1);
            return state;
        }
        return now;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!held(this.supported(level, pos))) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!stack.is(this.asItem())) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (held(state) && state.getValue(STANDING) && state.getValue(HANGING)) {
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) {
            boolean placed = false;
            if (!state.getValue(HANGING)) {
                placed = this.extend(level, pos, player, stack, Direction.UP);
            }
            if (!placed && !state.getValue(STANDING)) {
                placed = this.extend(level, pos, player, stack, Direction.DOWN);
            }
            if (placed) {
                stack.consume(1, player);
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** Walks along the stalk to the first free block in that direction and puts a stalk there, if it can hold. */
    private boolean extend(Level level, BlockPos pos, Player player, ItemStack stack, Direction direction) {
        BlockPos.MutableBlockPos at = pos.mutable().move(direction);
        while (level.isInWorldBounds(at)) {
            BlockState there = level.getBlockState(at);
            if (there.isAir()) {
                BlockState placed = this.supported(level, at);
                if (held(placed) && player.mayUseItemAt(at, direction, stack) && level.setBlock(at, placed, Block.UPDATE_ALL)) {
                    SoundType sound = placed.getSoundType(level, at, player);
                    level.playSound(null, at, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
                    return true;
                }
                return false;
            }
            if (!there.is(this)) {
                return false;
            }
            at.move(direction);
        }
        return false;
    }

    /** FakeFlowerBlock: flowers (only they) grow on top of a beanstalk. */
    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
        return facing == Direction.UP && plant.is(BlockTags.SMALL_FLOWERS) ? TriState.TRUE : TriState.DEFAULT;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }
}
