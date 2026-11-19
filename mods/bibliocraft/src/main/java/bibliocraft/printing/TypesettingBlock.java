package bibliocraft.printing;

import bibliocraft.common.BiblioBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * BlockTypeMachine "Typesetting Table" (the angle was the metadata): bounds 0..0.92 high. On the top face there are
 * three zones (by angle): the chase stack (a chase in hand is added, else the stack drops), the book place (a plate or
 * book in hand is put there, else it drops; sneaking with an empty hand saves the signed book, melts an old plate back
 * into a chase or makes the enchanted plate) and the new plate (drops it). A click anywhere else on the top opens the
 * list of saved books (GuiTypesetting, client side). A redstone signal at the table or the block above turns a chase
 * into a plate of the chosen book, 10 ticks after a neighbouring power source changed.
 */
public class TypesettingBlock extends BiblioBlock {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16 * 0.92, 16);

    public TypesettingBlock(Properties properties) {
        super(true, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TypesettingBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** The zones of the top face: 1 the chase stack, 0 the book place, 2 the new plate, -1 none. */
    public static int zone(int angle, double hitx, double hitz) {
        int iCheck = (int) (hitx * 2.0F);
        int iCheck2 = (int) (hitx * 3.0F);
        int kCheck = (int) (hitz * 3.0F);
        int kCheck2 = (int) (hitz * 2.0F);
        switch (angle) {
            case 0:
                if (iCheck == 1 && kCheck == 1) {
                    return 1;
                }
                if (iCheck == 0 && kCheck2 == 0) {
                    return 0;
                }
                if (iCheck == 0 && kCheck2 == 1) {
                    return 2;
                }
                break;
            case 1:
                if (kCheck2 == 1 && iCheck2 == 1) {
                    return 1;
                }
                if (iCheck == 1 && kCheck2 == 0) {
                    return 0;
                }
                if (iCheck == 0 && kCheck2 == 0) {
                    return 2;
                }
                break;
            case 2:
                if (iCheck == 0 && kCheck == 1) {
                    return 1;
                }
                if (iCheck == 1 && kCheck2 == 1) {
                    return 0;
                }
                if (iCheck == 1 && kCheck2 == 0) {
                    return 2;
                }
                break;
            case 3:
                if (iCheck2 == 1 && kCheck2 == 0) {
                    return 1;
                }
                if (iCheck == 0 && kCheck2 == 1) {
                    return 0;
                }
                if (iCheck == 1 && kCheck2 == 1) {
                    return 2;
                }
                break;
            default:
                break;
        }
        return -1;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND || !(level.getBlockEntity(pos) instanceof TypesettingBlockEntity tile)) {
            return InteractionResult.PASS;
        }
        double hitx = hit.getLocation().x - pos.getX();
        double hitz = hit.getLocation().z - pos.getZ();
        int zone = zone(angle(state), hitx, hitz);
        boolean top = hit.getDirection() == Direction.UP;
        if (!level.isClientSide()) {
            if (top) {
                this.activate(level, pos, player, held, tile, zone);
                if (zone == -1) {
                    // the integrated server of 1.5.2 let the client read the book folder itself; here the server
                    // refreshes the list before the client's GUI shows it
                    tile.booklistset();
                }
            }
        } else if (top && zone == -1) {
            // guiZoneCheck (client)
            Printing.client.openTypesetting(pos);
        }
        return InteractionResult.SUCCESS;
    }

    /** onBlockActivated (server, top face). */
    private void activate(Level level, BlockPos pos, Player player, ItemStack held, TypesettingBlockEntity tile, int zone) {
        if (zone == 1) {
            if (!this.addChase(tile, player, held)) {
                dropSlot(level, pos, tile, 1);
            }
        } else if (zone == 0) {
            if (!player.isShiftKeyDown()) {
                if (!this.addBookorPlate(tile, player, held)) {
                    dropSlot(level, pos, tile, 0);
                }
                return;
            }
            if (plateResetorSaveBook(tile, player)) {
                tile.booklistset();
            }
        } else if (zone == 2 && tile.hasNewPlate()) {
            dropSlot(level, pos, tile, 2);
        }
    }

    private boolean addChase(TypesettingBlockEntity tile, Player player, ItemStack held) {
        if (!(held.getItem() instanceof ChaseItem)) {
            return false;
        }
        int slot = player.getInventory().getSelectedSlot();
        int chasereturn = tile.addChase(held);
        if (chasereturn != 0) {
            held.setCount(chasereturn);
            player.getInventory().setItem(slot, held);
        } else {
            player.getInventory().setItem(slot, ItemStack.EMPTY);
        }
        return true;
    }

    private boolean addBookorPlate(TypesettingBlockEntity tile, Player player, ItemStack held) {
        if (!held.isEmpty() && tile.addBookorPlate(held)) {
            player.getInventory().setItem(player.getInventory().getSelectedSlot(), ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    /** plateResetorSaveBook: all three are tried. */
    private static boolean plateResetorSaveBook(TypesettingBlockEntity tile, Player player) {
        boolean hasReset = tile.resetPlate();
        boolean hasSavedBook = tile.saveBook();
        boolean hasEnchantedBook = tile.enchantPlate(player);
        return hasReset || hasSavedBook || hasEnchantedBook;
    }

    /** dropSlot: the slot's stack appears 1.2 above the corner + 0.5, without motion. */
    static void dropSlot(Level level, BlockPos pos, net.minecraft.world.Container inventory, int slot) {
        ItemStack stuff = inventory.getItem(slot);
        if (!stuff.isEmpty()) {
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5F, pos.getY() + 1.2F, pos.getZ() + 0.5F, stuff.copy(), 0.0, 0.0, 0.0));
            inventory.setItem(slot, ItemStack.EMPTY);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TypesettingBlockEntity tile) {
            tile.booklistset();
        }
    }

    private static boolean powered(Level level, BlockPos pos) {
        return level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
    }

    /** onNeighborBlockChange: a power source next to it and power here: tick in 10 (tickRate). */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        if (!level.isClientSide() && block.defaultBlockState().isSignalSource() && powered(level, pos)) {
            level.scheduleTick(pos, this, 10);
        }
    }

    /** updateTick: still powered -> setPlate. */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (powered(level, pos) && level.getBlockEntity(pos) instanceof TypesettingBlockEntity tile) {
            tile.setPlate();
        }
    }
}
