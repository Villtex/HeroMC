package tconstruct.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import tconstruct.items.PatternItem;

/** Common parts of the table menus: the table's container (block entity on the server, a copy on the client). */
public abstract class TableMenu extends AbstractContainerMenu {
    protected final Container table;
    protected final int tableSlots;

    protected TableMenu(MenuType<?> type, int id, Container table, int tableSlots) {
        super(type, id);
        this.table = table;
        this.tableSlots = tableSlots;
    }

    public Container table() {
        return this.table;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.table.stillValid(player);
    }

    protected void addPlayer(Inventory inventory, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, x + col * 18, y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, x + col * 18, y + 58));
        }
    }

    /** SlotPattern: patterns and casts only. */
    public static class PatternSlot extends Slot {
        public PatternSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof PatternItem;
        }
    }

    /** SlotOnlyTake. */
    public static class TakeOnlySlot extends Slot {
        public TakeOnlySlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
