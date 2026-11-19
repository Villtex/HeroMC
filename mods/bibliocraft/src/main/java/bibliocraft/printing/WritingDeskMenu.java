package bibliocraft.printing;

import bibliocraft.common.BCConfig;
import bibliocraft.common.BiblioMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * ContainerWritingDesk: SlotWritingDeskBooks for the open book at (80, 36), the left stack at x 36 and the right stack
 * at x 126 (bottom to top: y 62, 44, 26, 8), player inventory at (8, 84). Every slot takes a book that is no block.
 */
public class WritingDeskMenu extends BiblioMenu {
    private static final int[][] SLOTS = {{80, 36}, {36, 62}, {36, 44}, {36, 26}, {36, 8}, {126, 62}, {126, 44}, {126, 26}, {126, 8}};

    public WritingDeskMenu(int id, Inventory inventory, Container container) {
        super(Desks.DESK_MENU.get(), id, container);
        for (int s = 0; s < 9; s++) {
            this.addSlot(new Slot(container, s, SLOTS[s][0], SLOTS[s][1]) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return isDeskBook(stack);
                }
            });
        }
        this.addPlayerInventory(inventory, 8, 84);
    }

    /** SlotWritingDeskBooks.isItemValid: a book (Config.testBookValidity) that is no block item. */
    static boolean isDeskBook(ItemStack stack) {
        return !BCConfig.isBlock(stack) && BCConfig.isBook(stack);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return this.quickMove(player, index, 9, WritingDeskMenu::isDeskBook);
    }
}
