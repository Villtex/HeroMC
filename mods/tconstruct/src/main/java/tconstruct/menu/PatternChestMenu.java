package tconstruct.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import tconstruct.items.PatternItem;

/** {@code PatternChestContainer}: 3 x 10 pattern slots, the player inventory at (17, 86). Only patterns move. */
public class PatternChestMenu extends TableMenu {
    public PatternChestMenu(int id, Inventory inventory, Container chest) {
        super(TMenus.PATTERN_CHEST.get(), id, chest, 30);
        for (int column = 0; column < 3; column++) {
            for (int row = 0; row < 10; row++) {
                this.addSlot(new PatternSlot(chest, row + column * 10, 8 + row * 18, 18 + column * 18));
            }
        }
        this.addPlayer(inventory, 17, 86);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem() || !(slot.getItem().getItem() instanceof PatternItem)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < 30) {
            if (!this.moveItemStackTo(stack, 30, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, 30, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }
}
