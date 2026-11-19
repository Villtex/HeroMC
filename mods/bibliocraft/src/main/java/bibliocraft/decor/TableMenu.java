package bibliocraft.decor;

import bibliocraft.common.BiblioMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** ContainerTable: the table slot (SlotTable: anything, up to 64) at (80, 36), player inventory at (8, 84). */
public class TableMenu extends BiblioMenu {
    public TableMenu(int id, Inventory inventory, Container container) {
        super(Decor.TABLE_MENU.get(), id, container);
        this.addSlot(new Slot(container, 0, 80, 36));
        this.addPlayerInventory(inventory, 8, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return this.quickMove(player, index, 1, s -> true);
    }
}
