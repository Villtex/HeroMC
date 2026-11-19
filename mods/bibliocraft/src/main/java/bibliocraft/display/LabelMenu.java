package bibliocraft.display;

import bibliocraft.common.BiblioMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** ContainerLabel: SlotLabel (anything, up to 64) centre (80, 45), left (35, 26), right (125, 26); inventory at (8, 84). */
public class LabelMenu extends BiblioMenu {
    public LabelMenu(int id, Inventory inventory, Container container) {
        super(Displays.LABEL_MENU.get(), id, container);
        this.addSlot(new LabelSlot(container, 0, 80, 45));
        this.addSlot(new LabelSlot(container, 1, 35, 26));
        this.addSlot(new LabelSlot(container, 2, 125, 26));
        this.addPlayerInventory(inventory, 8, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return this.quickMove(player, index, 3, s -> true);
    }

    /** SlotLabel. */
    private static final class LabelSlot extends Slot {
        LabelSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }
    }
}
