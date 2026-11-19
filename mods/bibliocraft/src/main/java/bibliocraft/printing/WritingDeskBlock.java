package bibliocraft.printing;

import bibliocraft.common.BCConfig;
import bibliocraft.common.BiblioBlock;
import bibliocraft.common.BiblioMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * BlockWritingDesk: a full block (the bounds are the whole cube for every angle) that needs air above it. On top, the
 * left and right half at the back hold up to four books each, the front middle the open book. Right click with a book
 * puts the held stack on the place under the cursor; sneaking with an empty hand takes the top book off again; a click
 * on the open book (top face) shows it in the book screen (writable books can be written in, signed books read);
 * everything else opens the GUI. The candle flickers (randomDisplayTick).
 */
public class WritingDeskBlock extends BiblioBlock {
    /** Metadata of the original: 0 oak, 1 spruce, 2 birch, 3 jungle (texture writingdesk1..4). */
    public final int wood;

    public WritingDeskBlock(int wood, Properties properties) {
        super(true, properties);
        this.wood = wood;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WritingDeskBlockEntity(pos, state);
    }

    /** canPlaceBlockAt: the block above must be air. */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return context.getLevel().isEmptyBlock(context.getClickedPos().above()) ? super.getStateForPlacement(context) : null;
    }

    /** isWritingBook: the front half, in the middle. */
    public static boolean isWritingBook(double hitx, double hitz, int angle) {
        return switch (angle) {
            case 0 -> hitz > 0.2 && hitz < 0.8 && hitx < 0.5;
            case 1 -> hitx > 0.2 && hitx < 0.8 && hitz < 0.5;
            case 2 -> hitz > 0.2 && hitz < 0.8 && hitx > 0.5;
            case 3 -> hitx > 0.2 && hitx < 0.8 && hitz > 0.5;
            default -> false;
        };
    }

    public static boolean isLeftBookStack(double hitx, double hitz, int angle) {
        return switch (angle) {
            case 0 -> hitx > 0.5 && hitz < 0.5;
            case 1 -> hitx > 0.5 && hitz > 0.5;
            case 2 -> hitx < 0.5 && hitz > 0.5;
            case 3 -> hitx < 0.5 && hitz < 0.5;
            default -> false;
        };
    }

    public static boolean isRightBookStack(double hitx, double hitz, int angle) {
        return switch (angle) {
            case 0 -> hitx > 0.5 && hitz > 0.5;
            case 1 -> hitx < 0.5 && hitz > 0.5;
            case 2 -> hitx < 0.5 && hitz < 0.5;
            case 3 -> hitx > 0.5 && hitz < 0.5;
            default -> false;
        };
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND || !(level.getBlockEntity(pos) instanceof WritingDeskBlockEntity desk)) {
            return InteractionResult.PASS;
        }
        double hitx = hit.getLocation().x - pos.getX();
        double hitz = hit.getLocation().z - pos.getZ();
        int angle = angle(state);
        if (!level.isClientSide()) {
            this.activate(level, pos, player, held, desk, hitx, hitz, angle);
        } else if (!player.isShiftKeyDown() && hit.getDirection() == Direction.UP && isWritingBook(hitx, hitz, angle)) {
            // the client side of onBlockActivated: the book screen for the open book (the Thaumonomicon case of the
            // original has no counterpart here)
            ItemStack book = desk.openBook();
            if (book.is(Items.WRITTEN_BOOK)) {
                Printing.client.openBook(pos, false);
            } else if (book.is(Items.WRITABLE_BOOK)) {
                Printing.client.openBook(pos, true);
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** onBlockActivated (server). */
    private void activate(Level level, BlockPos pos, Player player, ItemStack held, WritingDeskBlockEntity desk, double hitx, double hitz, int angle) {
        if (player.isShiftKeyDown()) {
            if (isLeftBookStack(hitx, hitz, angle) && desk.leftBookStack() > 0) {
                dropWritingBook(level, pos, desk.leftBook());
                desk.addLeftBook(ItemStack.EMPTY);
                return;
            }
            if (isRightBookStack(hitx, hitz, angle) && desk.rightBookStack() > 0) {
                dropWritingBook(level, pos, desk.rightBook());
                desk.addRightBook(ItemStack.EMPTY);
                return;
            }
            if (isWritingBook(hitx, hitz, angle) && desk.hasWrittenBook()) {
                dropWritingBook(level, pos, desk.openBook());
                desk.setOpenBookStack(ItemStack.EMPTY);
            }
            return;
        }
        if (!held.isEmpty() && !BCConfig.isBlock(held) && BCConfig.isBook(held)) {
            // the zones are tested one after another; the last one hit decides (they do not overlap)
            boolean hadRoomOnDesk = false;
            if (isLeftBookStack(hitx, hitz, angle)) {
                hadRoomOnDesk = desk.addLeftBook(held);
            }
            if (isRightBookStack(hitx, hitz, angle)) {
                hadRoomOnDesk = desk.addRightBook(held);
            }
            if (isWritingBook(hitx, hitz, angle)) {
                hadRoomOnDesk = desk.setOpenBookStack(held);
            }
            if (hadRoomOnDesk) {
                player.getInventory().setItem(player.getInventory().getSelectedSlot(), ItemStack.EMPTY);
            }
        } else if (!isWritingBook(hitx, hitz, angle) && player instanceof ServerPlayer sp) {
            BiblioMenu.open(sp, desk, Component.literal("Wooden Desk"), WritingDeskMenu::new);
        }
    }

    /** dropWritingBook: the book appears 1.4 above the desk's corner + 0.5, without motion. */
    private static void dropWritingBook(Level level, BlockPos pos, ItemStack book) {
        if (!book.isEmpty()) {
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5F, pos.getY() + 1.4F, pos.getZ() + 0.5F, book.copy(), 0.0, 0.0, 0.0));
        }
    }

    /** randomDisplayTick: the candle flame (EntityCandleFX, a small flame) above the candle for the angle. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        double x;
        double z;
        switch (angle(state)) {
            case 0 -> {
                x = i + 0.4F;
                z = k + 0.16F;
            }
            case 1 -> {
                x = i + 0.84F;
                z = k + 0.4F;
            }
            case 2 -> {
                x = i + 0.6F;
                z = k + 0.84F;
            }
            case 3 -> {
                x = i + 0.16F;
                z = k + 0.6F;
            }
            default -> {
                x = i + 0.5F;
                z = k + 0.5F;
            }
        }
        level.addParticle(ParticleTypes.SMALL_FLAME, x, j + 1.24F, z, 0.0, 0.001, 0.0);
    }
}
