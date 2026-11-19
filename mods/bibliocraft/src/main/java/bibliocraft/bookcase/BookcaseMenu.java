package bibliocraft.bookcase;

import bibliocraft.common.BCConfig;
import bibliocraft.common.BiblioMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** BookContainer: two rows of eight SlotBook at (17 + 18j, 17 + 25i), player inventory at (8, 84). */
public class BookcaseMenu extends BiblioMenu {
    public BookcaseMenu(int id, Inventory inventory, Container container) {
        super(Bookcases.BOOKCASE_MENU.get(), id, container);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 8; j++) {
                this.addSlot(new Slot(container, j + i * 8, 17 + j * 18, 17 + i * 25) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return mayHoldBook(stack);
                    }
                });
            }
        }
        this.addPlayerInventory(inventory, 8, 84);
    }

    /** SlotBook.isItemValid: a book; blank books only with AllowEmptyBooks (block items pass, as in the original). */
    static boolean mayHoldBook(ItemStack stack) {
        return BCConfig.isBook(stack) && (BCConfig.isBlock(stack) || BCConfig.allowEmptyBooks() || !stack.is(Items.BOOK));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return this.quickMove(player, index, 16, s -> !BCConfig.isBlock(s) && BCConfig.isBook(s));
    }
}
