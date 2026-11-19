package bibliocraft.printing;

import bibliocraft.common.BCConfig;
import bibliocraft.common.BiblioBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileEntityWritingDesk: slot 0 is the open book on the desk, 1..4 the stack on the left, 5..8 the stack on the right.
 * The page the open book was left at ("CurrentPage") is kept and sent to the clients; it goes back to 0 when the
 * open book is taken out. The angle is the block state property (deskAngle of the original).
 */
public class WritingDeskBlockEntity extends BiblioBlockEntity {
    private int leftStack;
    private int rightStack;
    private int currentPage;

    public WritingDeskBlockEntity(BlockPos pos, BlockState state) {
        super(Desks.DESK_ENTITY.get(), pos, state, 9);
    }

    /** checkStackedBooks: how many of the slots 1..4 and 5..8 are filled (the renderer shows that many books). */
    @Override
    protected void contentsChanged() {
        // setInventorySlotContents: an emptied slot 0 resets the page
        if (this.items.get(0).isEmpty()) {
            this.currentPage = 0;
        }
        this.leftStack = 0;
        for (int s = 1; s < 5; s++) {
            if (!this.items.get(s).isEmpty()) {
                this.leftStack++;
            }
        }
        this.rightStack = 0;
        for (int s = 5; s < 9; s++) {
            if (!this.items.get(s).isEmpty()) {
                this.rightStack++;
            }
        }
    }

    public int leftBookStack() {
        return this.leftStack;
    }

    public int rightBookStack() {
        return this.rightStack;
    }

    public boolean hasWrittenBook() {
        return !this.items.get(0).isEmpty();
    }

    public ItemStack openBook() {
        return this.items.get(0);
    }

    public int currentPage() {
        return this.currentPage;
    }

    public void setCurrentPage(int page) {
        this.currentPage = page;
        this.sync();
    }

    /** overwriteWrittenBook: only while there is a book on the desk. */
    public void overwriteWrittenBook(ItemStack stack) {
        if (!this.items.get(0).isEmpty()) {
            this.setItem(0, stack);
        }
    }

    /** setOpenBookStack: EMPTY clears slot 0 (false); a stack goes into an empty slot 0 (true). */
    public boolean setOpenBookStack(ItemStack stack) {
        if (stack.isEmpty()) {
            this.setItem(0, ItemStack.EMPTY);
            return false;
        }
        if (this.items.get(0).isEmpty()) {
            this.setItem(0, stack);
            return true;
        }
        return false;
    }

    /** getLeftBook: the top of the left stack (slot 4 down to 1). */
    public ItemStack leftBook() {
        return top(1);
    }

    public ItemStack rightBook() {
        return top(5);
    }

    private ItemStack top(int first) {
        for (int s = first + 3; s >= first; s--) {
            if (!this.items.get(s).isEmpty()) {
                return this.items.get(s);
            }
        }
        return ItemStack.EMPTY;
    }

    /** addLeftBook: EMPTY takes the top book off (slot 4 down to 1), a stack goes onto the first free slot 1..4. */
    public boolean addLeftBook(ItemStack stack) {
        return add(1, stack);
    }

    public boolean addRightBook(ItemStack stack) {
        return add(5, stack);
    }

    private boolean add(int first, ItemStack stack) {
        if (stack.isEmpty()) {
            for (int s = first + 3; s >= first; s--) {
                if (!this.items.get(s).isEmpty()) {
                    this.setItem(s, ItemStack.EMPTY);
                    return true;
                }
            }
            return false;
        }
        for (int s = first; s < first + 4; s++) {
            if (this.items.get(s).isEmpty()) {
                this.setItem(s, stack);
                return true;
            }
        }
        return false;
    }

    /** isStackValidForSlot: a book that is no block item (hoppers). */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return !BCConfig.isBlock(stack) && BCConfig.isBook(stack);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("CurrentPage", this.currentPage);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.currentPage = input.getIntOr("CurrentPage", 0);
    }
}
