package bibliocraft.bookcase;

import bibliocraft.common.BCConfig;
import bibliocraft.common.BiblioBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * TileEntityBookcase: 16 slots (0..7 upper row, 8..15 lower row). The redstone book state is worked out again
 * after each change (setInventorySlotContents); when it turns on or off, the bookcase and its neighbours are told.
 */
public class BookcaseBlockEntity extends BiblioBlockEntity {
    private boolean hasRedstoneBook;
    private int redstoneBookSlot;
    private int filledSlots;

    public BookcaseBlockEntity(BlockPos pos, BlockState state) {
        super(Bookcases.BOOKCASE_ENTITY.get(), pos, state, 16);
    }

    public boolean hasRedstoneBook() {
        return this.hasRedstoneBook;
    }

    /** redstonebookslot: the first slot with a Redstone book (power = slot number). */
    public int redstoneBookSlot() {
        return this.redstoneBookSlot;
    }

    public int filledSlots() {
        return this.filledSlots;
    }

    /** setBook: puts a stack into an empty slot (true), or empties the slot (EMPTY, false). */
    public boolean setBook(int slot, ItemStack stack) {
        if (!stack.isEmpty() && this.getItem(slot).isEmpty()) {
            this.setItem(slot, stack);
            return true;
        }
        if (stack.isEmpty() && !this.getItem(slot).isEmpty()) {
            this.setItem(slot, ItemStack.EMPTY);
        }
        return false;
    }

    @Override
    protected void contentsChanged() {
        boolean before = this.hasRedstoneBook;
        this.hasRedstoneBook = false;
        for (int x = 0; x < 16; x++) {
            if (this.items.get(x).getItem() instanceof RedstoneBookItem) {
                this.redstoneBookSlot = x;
                this.hasRedstoneBook = true;
                break;
            }
        }
        this.filledSlots = (int) this.items.stream().filter(s -> !s.isEmpty()).count();
        if (before != this.hasRedstoneBook && this.level != null && !this.level.isClientSide()) {
            notifyAround(this.level, this.worldPosition, this.getBlockState().getBlock());
        }
    }

    /** notifyBlocksOfNeighborChange on the block and its six neighbours (strong power reaches through them). */
    static void notifyAround(Level level, BlockPos pos, Block block) {
        level.updateNeighborsAt(pos, block, null);
        for (Direction d : Direction.values()) {
            level.updateNeighborsAt(pos.relative(d), block, null);
        }
    }

    /** isStackValidForSlot: a book that is no block item (hoppers). */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return BCConfig.isShelvableBook(stack);
    }
}
