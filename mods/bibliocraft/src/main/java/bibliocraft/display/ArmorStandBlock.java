package bibliocraft.display;

import bibliocraft.common.BiblioBlock;
import bibliocraft.common.BiblioMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
 * BlockArmorStand: two blocks high. The original kept the angle in the metadata and put a second stand block with
 * metadata angle + 4 on top; here that is {@link #HALF} (upper = metadata 4..7), the angle is {@link BiblioBlock#ANGLE}
 * in both halves. Only the lower half has the block entity and drops the stand (loot table: half = lower); breaking
 * either half removes the other, like a door.
 *
 * <p>Right click: with a piece of armor of the type under the cursor (lower half: boots below, leggings above; upper
 * half: chestplate below, helmet above) it goes onto the stand; sneaking swaps that piece with the player's (all four
 * if either half is powered); anything else opens the GUI.
 */
public class ArmorStandBlock extends BiblioBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    /** setBlockBoundsBasedOnState: 0.3..0.7 across, the whole block high and deep. */
    private static final VoxelShape SHAPE_X = box(4.8, 0, 0, 11.2, 16, 16);
    private static final VoxelShape SHAPE_Z = box(0, 0, 4.8, 16, 16, 11.2);

    public ArmorStandBlock(Properties properties) {
        super(false, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ANGLE, 0).setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HALF);
    }

    public static boolean isUpper(BlockState state) {
        return state.hasProperty(HALF) && state.getValue(HALF) == DoubleBlockHalf.UPPER;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // hasTileEntity(metadata < 4)
        return isUpper(state) ? null : new ArmorStandBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return angle(state) % 2 == 0 ? SHAPE_X : SHAPE_Z;
    }

    /** canPlaceBlockAt: the block above is air; onBlockPlacedBy: the angle as usual. */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() >= level.getMaxY() || !level.getBlockState(pos.above()).isAir()) {
            return null;
        }
        Player player = context.getPlayer();
        return this.defaultBlockState().setValue(ANGLE, player != null ? angle(player) : 0);
    }

    /** onBlockPlacedBy: the upper stand block with the same angle (metadata angle + 4). */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    /** breakBlock removed the other half: a half without its partner turns into air (and the lower half drops). */
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        boolean lower = !isUpper(state);
        if (direction.getAxis() == Direction.Axis.Y && lower == (direction == Direction.UP)) {
            return neighbourState.is(this) && isUpper(neighbourState) != isUpper(state) ? state : Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
    }

    /** No stand from the lower half when the upper one is broken in creative or without a pickaxe (DoorBlock). */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && isUpper(state) && (player.preventsBlockDrops() || !player.hasCorrectToolForDrops(state, level, pos))) {
            BlockPos below = pos.below();
            BlockState lower = level.getBlockState(below);
            if (lower.is(this) && !isUpper(lower)) {
                // the armor still falls out (dropItems of the original): flag 35 keeps the container drop
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
            this.activate(level, pos, state, player, held, hit);
        }
        return InteractionResult.SUCCESS;
    }

    /** onBlockActivated (server). */
    private void activate(Level level, BlockPos pos, BlockState state, Player player, ItemStack held, BlockHitResult hit) {
        boolean upper = isUpper(state);
        BlockPos lowerPos = upper ? pos.below() : pos;
        if (!(level.getBlockEntity(lowerPos) instanceof ArmorStandBlockEntity stand)) {
            return;
        }
        int yCheck = (int) ((hit.getLocation().y - pos.getY()) * 2.0F);
        boolean powered = level.hasNeighborSignal(pos) || level.hasNeighborSignal(upper ? pos.below() : pos.above());
        if (player.isShiftKeyDown()) {
            if (!powered) {
                if (yCheck == 0) {
                    swapArmor(player, stand, upper ? 2 : 0);
                }
                if (yCheck == 1) {
                    swapArmor(player, stand, upper ? 3 : 1);
                }
                return;
            }
            for (int t = 0; t < 4; t++) {
                swapArmor(player, stand, t);
            }
            return;
        }
        int type = ArmorRules.armorType(held);
        if (type >= 0) {
            int want = upper ? (yCheck == 0 ? 1 : yCheck == 1 ? 0 : -1) : (yCheck == 0 ? 3 : yCheck == 1 ? 2 : -1);
            if (type == want && stand.addArmor(held, type)) {
                player.getInventory().setItem(player.getInventory().getSelectedSlot(), ItemStack.EMPTY);
                return;
            }
        }
        if (player instanceof ServerPlayer sp) {
            BiblioMenu.open(sp, stand, Component.literal("Armor Stand"), ArmorStandMenu::new);
        }
    }

    /**
     * handleArmorTransation: swaps the player's armor in armorInventory[armortype] (0 boots .. 3 helmet) with stand
     * slot 3 - armortype, without checking what it is. The original then sent the player's new armor in the
     * "BiblioAStand" packet; the server syncs the player's equipment by itself now. A piece with Curse of Binding
     * stays on the player (the curse did not exist in 1.5.2).
     */
    private static void swapArmor(Player player, ArmorStandBlockEntity stand, int armortype) {
        int standSlot = 3 - armortype;
        EquipmentSlot slot = ArmorRules.SLOTS[standSlot];
        ItemStack worn = player.getItemBySlot(slot);
        if (!worn.isEmpty() && !player.isCreative() && EnchantmentHelper.has(worn, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
            return;
        }
        ItemStack onStand = stand.getArmor(standSlot).copy();
        ItemStack fromPlayer = worn.copy();
        player.setItemSlot(slot, onStand);
        stand.setItem(standSlot, fromPlayer);
    }
}
