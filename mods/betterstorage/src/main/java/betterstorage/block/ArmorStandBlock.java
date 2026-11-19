package betterstorage.block;

import betterstorage.item.BackpackItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * BlockArmorStand: two blocks high (metadata 0 below with the tile entity, 1 on top: {@link #HALF}). Right click puts
 * the piece of armor in hand onto the stand at the height clicked (boots at the bottom, helmet at the top) or takes
 * it into the empty hand; sneaking swaps it with the piece the player wears there. Only the lower half drops the stand
 * (loot table); a half without its partner turns into air.
 */
public class ArmorStandBlock extends BaseEntityBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    /** Armor slot s (0 boots .. 3 helmet) of the stand and of the player's armorInventory. */
    public static final EquipmentSlot[] SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
    private static final VoxelShape SHAPE = box(2, 0, 2, 14, 16, 14);

    public ArmorStandBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF);
    }

    public static boolean isUpper(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isUpper(state) ? null : new ArmorStandBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** Item.isValidArmor: armor of that slot (also pumpkins and heads on top); backpacks never. */
    public static boolean isValidArmor(ItemStack stack, int slot) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot() == SLOTS[slot] && !(stack.getItem() instanceof BackpackItem);
    }

    /** ItemArmorStand.onItemUse: the block above must be replaceable too. */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() >= level.getMaxY() - 1 || !level.getBlockState(pos.above()).canBeReplaced(context)) {
            return null;
        }
        return this.defaultBlockState();
    }

    /** onBlockPlacedBy: rotation from the head yaw in sixteenths, then the upper half. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer != null && level.getBlockEntity(pos) instanceof ArmorStandBlockEntity stand) {
            stand.rotation = Math.round((placer.getYHeadRot() + 180.0F) * 16.0F / 360.0F);
            stand.sync();
        }
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    /** onNeighborBlockChange: without its other half it turns into air (the lower half drops the stand then). */
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        boolean lower = !isUpper(state);
        if (direction.getAxis() == Direction.Axis.Y && lower == (direction == Direction.UP)) {
            return neighbourState.is(this) && isUpper(neighbourState) == lower ? state : Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
    }

    /** No stand from the lower half when the upper one is broken in creative (the armor still falls out). */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && isUpper(state) && player.preventsBlockDrops()) {
            BlockPos below = pos.below();
            BlockState lower = level.getBlockState(below);
            if (lower.is(this) && !isUpper(lower)) {
                level.setBlock(below, Blocks.AIR.defaultBlockState(), 35);
                level.levelEvent(player, 2001, below, Block.getId(lower));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            BlockPos lower = isUpper(state) ? pos.below() : pos;
            if (level.getBlockEntity(lower) instanceof ArmorStandBlockEntity stand) {
                activate(stand, player, slot(hit, lower));
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** min(3, (int) (hitY * 2)), hitY from the bottom of the lower half. */
    public static int slot(BlockHitResult hit, BlockPos lower) {
        return Mth.clamp((int) ((hit.getLocation().y - lower.getY()) * 2.0), 0, 3);
    }

    /** onBlockActivated of the lower half (the upper one passed the click down with hitY + 1). */
    private static void activate(ArmorStandBlockEntity stand, Player player, int slot) {
        ItemStack item = stand.armor.get(slot);
        ItemStack holding = player.getMainHandItem();
        EquipmentSlot equipment = SLOTS[slot];
        ItemStack worn = player.getItemBySlot(equipment);
        if (player.isShiftKeyDown()) {
            if ((!item.isEmpty() || !worn.isEmpty()) && (worn.isEmpty() || isValidArmor(worn, slot))) {
                stand.armor.set(slot, worn.copy());
                player.setItemSlot(equipment, item.copy());
                stand.sync();
            }
        } else if (!item.isEmpty() && holding.isEmpty() || !holding.isEmpty() && isValidArmor(holding, slot)) {
            stand.armor.set(slot, holding.copy());
            player.setItemInHand(InteractionHand.MAIN_HAND, item.copy());
            stand.sync();
        }
    }
}
