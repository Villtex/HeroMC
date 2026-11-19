package bibliocraft.shelf;

import bibliocraft.common.BiblioMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** ContainerGenericShelf: SlotGenericShelf (any item) at (64, 22), (96, 22), (64, 51), (96, 51), player inventory at (8, 84). */
public class ShelfMenu extends BiblioMenu {
    public ShelfMenu(int id, Inventory inventory, Container container) {
        super(Shelves.SHELF_MENU.get(), id, container);
        this.addSlot(new Slot(container, 0, 64, 22));
        this.addSlot(new Slot(container, 1, 96, 22));
        this.addSlot(new Slot(container, 2, 64, 51));
        this.addSlot(new Slot(container, 3, 96, 51));
        this.addPlayerInventory(inventory, 8, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return this.quickMove(player, index, 4, s -> true);
    }
}
