package projectzulu.block.gear;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import projectzulu.ZuluItems;

/**
 * BlockSpikes ("Ivory Spikes"): no collision, an entity falling into them (or crossing their middle towards -x/-z)
 * takes 2 damage; poisoned spikes add Poison II, sticky ones Slowness V (2 seconds). A poison droplet poisons them,
 * a slime ball makes them sticky. Metadata of 1.5.2: side 0-5 (+6 poisoned, +12 sticky) is {@link #FACING} (the
 * direction the points face) and {@link #TYPE}. On a fence or on iron bars below they sit thin on top of it
 * ({@link #MOUNT}, the bars' connections in the four side properties), like RenderSpike drew them.
 */
public class SpikesBlock extends Block {
    public enum Type implements StringRepresentable {
        NORMAL, POISON, STICKY;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase();
        }
    }

    public enum Mount implements StringRepresentable {
        NONE, FENCE, BARS;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase();
        }
    }

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final EnumProperty<Type> TYPE = EnumProperty.create("type", Type.class);
    public static final EnumProperty<Mount> MOUNT = EnumProperty.create("mount", Mount.class);
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    /** Side of 1.5.2 (metadata % 6) to the facing of the points. */
    private static final Direction[] BY_SIDE = {Direction.UP, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.DOWN};

    public SpikesBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(TYPE, Type.NORMAL)
                .setValue(MOUNT, Mount.NONE).setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE, MOUNT, NORTH, EAST, SOUTH, WEST);
    }

    /** The block state for 1.5.2 metadata 0-15 (OldBlockStates). */
    public BlockState byMeta(int meta) {
        Type type = meta < 6 ? Type.NORMAL : meta < 12 ? Type.POISON : Type.STICKY;
        return this.defaultBlockState().setValue(FACING, BY_SIDE[meta % 6]).setValue(TYPE, type);
    }

    // ---------------------------------------------------------------- shape (setBlockBoundsBasedOnState)

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(MOUNT)) {
            case FENCE -> Block.box(6, 0, 6, 10, 6.4, 10);
            case BARS -> {
                boolean n = state.getValue(NORTH), s = state.getValue(SOUTH), w = state.getValue(WEST), e = state.getValue(EAST);
                if (!n && !s && !w && !e) {
                    yield Block.box(0, 0, 0, 16, 6.4, 16);
                }
                yield Block.box(w ? 0 : 6.4, 0, n ? 0 : 6.4, e ? 16 : 9.6, 6.4, s ? 16 : 9.6);
            }
            case NONE -> switch (state.getValue(FACING)) {
                case EAST -> Block.box(0, 0, 0, 8, 16, 16);
                case SOUTH -> Block.box(0, 0, 0, 16, 16, 8);
                case WEST -> Block.box(8, 0, 0, 16, 16, 16);
                case NORTH -> Block.box(0, 0, 8, 16, 16, 16);
                case DOWN -> Block.box(0, 8, 0, 16, 16, 16);
                default -> Block.box(0, 0, 0, 16, 8, 16);
            };
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return true;
    }

    // ---------------------------------------------------------------- placing (onBlockPlaced)

    /** canPlaceSpikeOn: a solid top surface, glass or a piston. */
    private static boolean canPlaceSpikeOn(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isFaceSturdy(level, pos, Direction.UP) || state.is(Blocks.GLASS) || state.is(Blocks.PISTON)
                || state.is(Blocks.PISTON_HEAD) || state.is(Blocks.STICKY_PISTON);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        Direction facing = canPlaceSpikeOn(context.getLevel(), pos.relative(face.getOpposite())) ? face : Direction.UP;
        return withMount(this.defaultBlockState().setValue(FACING, facing), context.getLevel(), pos);
    }

    /** What RenderSpike/setBlockBoundsBasedOnState looked at: a fence or iron bars below. */
    private static BlockState withMount(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        Mount mount = below.is(BlockTags.FENCES) ? Mount.FENCE : below.is(Blocks.IRON_BARS) ? Mount.BARS : Mount.NONE;
        boolean bars = mount == Mount.BARS;
        return state.setValue(MOUNT, mount)
                .setValue(NORTH, bars && below.getValue(CrossCollisionBlock.NORTH))
                .setValue(EAST, bars && below.getValue(CrossCollisionBlock.EAST))
                .setValue(SOUTH, bars && below.getValue(CrossCollisionBlock.SOUTH))
                .setValue(WEST, bars && below.getValue(CrossCollisionBlock.WEST));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        return direction == Direction.DOWN ? withMount(state, level, pos) : state;
    }

    // ---------------------------------------------------------------- effects

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        if (level instanceof ServerLevel serverLevel && entity instanceof LivingEntity living) {
            double threshold = pos.getY() + this.getShape(state, level, pos, CollisionContext.empty()).max(Direction.Axis.Y) * 0.9F;
            boolean hit = entity.yo > threshold && entity.getY() < threshold
                    || entity.xo > pos.getX() + 0.5 && entity.getX() < pos.getX() + 0.5
                    || entity.zo > pos.getZ() + 0.5 && entity.getZ() < pos.getZ() + 0.5;
            if (hit) {
                if (state.getValue(TYPE) == Type.POISON) {
                    living.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 1));
                }
                if (state.getValue(TYPE) == Type.STICKY) {
                    living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 4));
                }
                living.hurtServer(serverLevel, serverLevel.damageSources().generic(), 2.0F);
            }
        }
        super.entityInside(state, level, pos, entity, effects, intersects);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        Type type = state.getValue(TYPE);
        Type changed = null;
        if (stack.is(ZuluItems.POISON_DROPLET.get()) && type != Type.POISON) {
            changed = Type.POISON;
        } else if (stack.is(Items.SLIME_BALL) && type != Type.STICKY) {
            changed = Type.STICKY;
        }
        if (changed == null) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (!level.isClientSide()) {
            stack.consume(1, player);
            level.setBlock(pos, state.setValue(TYPE, changed), Block.UPDATE_ALL);
        }
        return InteractionResult.SUCCESS;
    }
}
