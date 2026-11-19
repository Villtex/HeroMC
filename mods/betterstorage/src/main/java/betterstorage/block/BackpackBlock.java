package betterstorage.block;

import betterstorage.BS;
import betterstorage.BSUtil;
import betterstorage.menu.BSMenu;
import betterstorage.network.BSNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * BlockBackpack and BlockEnderBackpack: a backpack set down. It drops nothing itself; when broken the backpack item
 * and its contents fall out (not in creative). Sneaking while breaking it with nothing on the chest puts it on.
 * The ender backpack opens the ender chest of whoever uses it and, broken, teleports away as a block instead of
 * dropping (64 tries, the last ones also in mid air).
 */
public class BackpackBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE_NS = box(2, 0, 3, 14, 13, 13);
    private static final VoxelShape SHAPE_EW = box(3, 0, 2, 13, 13, 14);
    public final boolean ender;

    public BackpackBlock(boolean ender, Properties properties) {
        super(properties);
        this.ender = ender;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BackpackBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? SHAPE_NS : SHAPE_EW;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BS.BACKPACK_ENTITY.get(), (l, p, s, be) -> be.tick(l));
    }

    /** getPickBlock: the backpack item itself. */
    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return level.getBlockEntity(pos) instanceof BackpackBlockEntity be && !be.stack.isEmpty() ? be.stack.copy() : super.getCloneItemStack(level, pos, state, includeData);
    }

    /** removeBlockByPlayer: sneaking with an empty chest slot puts it on instead. */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack toolStack, boolean willHarvest,
            FluidState fluid) {
        if (level.getBlockEntity(pos) instanceof BackpackBlockEntity backpack) {
            if (player.isCreative()) {
                backpack.brokenInCreative = true;
                if (this.ender) {
                    backpack.equipped = true;
                }
            }
            if (!level.isClientSide() && player.isShiftKeyDown() && player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
                return backpack.equip(player);
            }
        }
        return super.onDestroyedByPlayer(state, level, pos, player, toolStack, willHarvest, fluid);
    }

    /** breakBlock: drop backpack and contents, or teleport the ender backpack. */
    void onRemoved(BackpackBlockEntity backpack) {
        Level level = backpack.getLevel();
        if (level == null || level.isClientSide() || backpack.equipped) {
            return;
        }
        BlockPos pos = backpack.getBlockPos();
        if (this.ender) {
            ItemStack stack = backpack.stack.isEmpty() ? new ItemStack(BS.ENDER_BACKPACK.get()) : backpack.stack;
            for (int i = 0; i < 64 && !teleportRandomly((ServerLevel) level, pos.getX(), pos.getY(), pos.getZ(), i > 48, stack); i++) {
            }
            return;
        }
        if (!backpack.brokenInCreative) {
            BSUtil.dropFromBlock(level, pos, backpack.stack);
        }
        backpack.dropContents();
    }

    /**
     * BlockEnderBackpack.teleportRandomly: a free spot within 12 blocks (8 up and down) on solid ground (unless
     * {@code canFloat}) gets the ender backpack, with portal particles along the way and the enderman sound at both ends.
     */
    public static boolean teleportRandomly(ServerLevel level, double sourceX, double sourceY, double sourceZ, boolean canFloat, ItemStack stack) {
        int x = (int) sourceX + BSUtil.getInt(-12, 13);
        int y = (int) sourceY + BSUtil.getInt(-8, 9);
        int z = (int) sourceZ + BSUtil.getInt(-12, 13);
        y = Math.max(level.getMinY() + 1, Math.min(level.getMaxY(), y));
        BlockPos pos = new BlockPos(x, y, z);
        if (!level.isLoaded(pos)) {
            return false;
        }
        BlockState there = level.getBlockState(pos);
        if (!there.isAir() && !there.canBeReplaced()) {
            return false;
        }
        if (!canFloat && !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
            return false;
        }
        BSNetwork.sendTeleport(level, sourceX, sourceY, sourceZ, x, y, z);
        level.playSound(null, sourceX + 0.5, sourceY + 0.5, sourceZ + 0.5, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.setBlock(pos, BS.ENDER_BACKPACK_BLOCK.get().defaultBlockState().setValue(FACING, Direction.from3DDataValue(BSUtil.getInt(2, 6))), 3);
        if (level.getBlockEntity(pos) instanceof BackpackBlockEntity backpack) {
            backpack.stack = stack.copy();
            backpack.sync();
        }
        return true;
    }

    /** onBlockActivated: the backpack's GUI; the ender backpack shows the player's ender chest (9 x 3). */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BackpackBlockEntity backpack && player instanceof ServerPlayer sp) {
            if (this.ender) {
                PlayerEnderChestContainer ender = sp.getEnderChestInventory();
                Component title = backpack.getCustomTitle() != null ? backpack.getCustomTitle()
                        : Component.translatable("container.betterstorage.ender_backpack");
                BSMenu.open(sp, title, 9, 3, new EnderBackpackInventory(ender, backpack));
            } else {
                backpack.openGui(sp);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
