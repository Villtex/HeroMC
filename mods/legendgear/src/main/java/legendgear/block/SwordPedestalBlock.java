package legendgear.block;

import legendgear.LGBlocks;
import legendgear.LGSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * BlockSwordPedestal: a half-high stone block along one axis (metadata bit 2: {@link #ALONG_X}). A sword put in with
 * right click stands in it (bit 8: {@link #HAS_SWORD}, a technical block above as its hitbox) and powers the block
 * below with 15; an empty hand takes it out. Over a powered skybeam block the sword is slowly repaired.
 */
public class SwordPedestalBlock extends BaseEntityBlock {
    public static final BooleanProperty ALONG_X = BooleanProperty.create("along_x");
    public static final BooleanProperty HAS_SWORD = BooleanProperty.create("has_sword");
    private static final VoxelShape SHAPE_X = Block.box(0, 0, 4, 16, 8, 12);
    private static final VoxelShape SHAPE_Z = Block.box(4, 0, 0, 12, 8, 16);

    public SwordPedestalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ALONG_X, false).setValue(HAS_SWORD, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ALONG_X, HAS_SWORD);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /** onBlockPlacedBy: facing south or north (yaw quadrant 0 or 2) lays it along x. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        return this.defaultBlockState().setValue(ALONG_X, facing.getAxis() == Direction.Axis.Z);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(ALONG_X) ? SHAPE_X : SHAPE_Z;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PedestalBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, LGBlocks.PEDESTAL_ENTITY.get(), PedestalBlockEntity::tick);
    }

    /** isProvidingWeakPower / StrongPower: 15 to the block below while a sword is in it. */
    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(HAS_SWORD) && direction == Direction.UP ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.getSignal(state, level, pos, direction);
    }

    /** isSword: every sword (the PedestalSwords list of the pack is empty). */
    public static boolean isSword(ItemStack stack) {
        return stack.is(ItemTags.SWORDS);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        return this.activate(state, level, pos, player, hand) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return this.activate(state, level, pos, player, InteractionHand.MAIN_HAND) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    public boolean activate(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !(level.getBlockEntity(pos) instanceof PedestalBlockEntity be)) {
            return false;
        }
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty() && !be.contents.isEmpty()) {
            if (!level.isClientSide()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, be.contents);
                be.setContents(ItemStack.EMPTY);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), LGSounds.SWORDTAKE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlock(pos, state.setValue(HAS_SWORD, false), 3);
                if (level.getBlockState(pos.above()).is(LGBlocks.PEDESTAL_TECHNICAL.get())) {
                    level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);
                }
                level.updateNeighborsAt(pos.below(), this);
            }
            return true;
        }
        if (!held.isEmpty() && be.contents.isEmpty() && isSword(held)
                && (level.isEmptyBlock(pos.above()) || level.getBlockState(pos.above()).is(LGBlocks.PEDESTAL_TECHNICAL.get()))) {
            if (!level.isClientSide()) {
                be.setContents(held);
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), LGSounds.SWORDPLACE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlock(pos, state.setValue(HAS_SWORD, true), 3);
                level.setBlock(pos.above(), LGBlocks.PEDESTAL_TECHNICAL.get().defaultBlockState()
                        .setValue(SwordPedestalTechnicalBlock.ALONG_X, state.getValue(ALONG_X)), 2);
                level.updateNeighborsAt(pos.below(), this);
            }
            return true;
        }
        return false;
    }

    /** ejectSword(stillAround = true): the technical block above was broken. */
    public void ejectSword(Level level, BlockPos pos) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof PedestalBlockEntity be && !be.contents.isEmpty()) {
            net.minecraft.world.entity.item.ItemEntity item = new net.minecraft.world.entity.item.ItemEntity(level,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, be.contents);
            item.setPickUpDelay(10);
            level.addFreshEntity(item);
            be.setContents(ItemStack.EMPTY);
            level.setBlock(pos, level.getBlockState(pos).setValue(HAS_SWORD, false), 2);
            level.updateNeighborsAt(pos.below(), this);
        }
    }
}
