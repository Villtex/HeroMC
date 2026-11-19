package bibliocraft.shelf;

import bibliocraft.common.BiblioMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** ContainerWeaponRack: SlotWeaponRack (tools) at (53, 15), (107, 15), (53, 53), (107, 53), player inventory at (8, 84). */
public class ToolRackMenu extends BiblioMenu {
    public ToolRackMenu(int id, Inventory inventory, Container container) {
        super(Shelves.TOOL_RACK_MENU.get(), id, container);
        this.addSlot(new ToolSlot(container, 0, 53, 15));
        this.addSlot(new ToolSlot(container, 1, 107, 15));
        this.addSlot(new ToolSlot(container, 2, 53, 53));
        this.addSlot(new ToolSlot(container, 3, 107, 53));
        this.addPlayerInventory(inventory, 8, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return this.quickMove(player, index, 4, ToolRackBlockEntity::isItemTool);
    }

    /** SlotWeaponRack.isItemValid: ContainerWeaponRack.isItemTool (block items with a tool name pass, as in the original). */
    private static final class ToolSlot extends Slot {
        ToolSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return ToolRackBlockEntity.isItemTool(stack);
        }
    }
}
