package legendgear.block;

import legendgear.LGBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockSwordPedestalTechnical: the invisible hitbox of the blade above a pedestal with a sword. Clicking it works like
 * clicking the pedestal, breaking it throws the sword out; it vanishes when the pedestal loses its sword.
 */
public class SwordPedestalTechnicalBlock extends Block {
    public static final BooleanProperty ALONG_X = SwordPedestalBlock.ALONG_X;
    private static final VoxelShape SHAPE_X = Block.box(4, 0, 7.5, 12, 8, 8.5);
    private static final VoxelShape SHAPE_Z = Block.box(7.5, 0, 4, 8.5, 8, 12);

    public SwordPedestalTechnicalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ALONG_X, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ALONG_X);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(ALONG_X) ? SHAPE_X : SHAPE_Z;
    }

    private static boolean held(LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(LGBlocks.SWORD_PEDESTAL.get()) && below.getValue(SwordPedestalBlock.HAS_SWORD);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        return direction == Direction.DOWN && !held(level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        BlockState below = level.getBlockState(pos.below());
        if (below.getBlock() instanceof SwordPedestalBlock pedestal && pedestal.activate(below, level, pos.below(), player, hand)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockState below = level.getBlockState(pos.below());
        if (below.getBlock() instanceof SwordPedestalBlock pedestal && pedestal.activate(below, level, pos.below(), player, InteractionHand.MAIN_HAND)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (held(level, pos) && level.getBlockState(pos.below()).getBlock() instanceof SwordPedestalBlock pedestal) {
            pedestal.ejectSword(level, pos.below());
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return ItemStack.EMPTY;
    }
}
