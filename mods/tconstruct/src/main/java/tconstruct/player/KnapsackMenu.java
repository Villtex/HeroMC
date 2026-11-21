package tconstruct.player;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import tconstruct.TPlayer;

/** KnapsackContainer: 3 x 9 knapsack slots above the player inventory, shift-click moves between both. */
public class KnapsackMenu extends AbstractContainerMenu {
    private final Container knapsack;

    public KnapsackMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(PlayerData.KNAPSACK_SLOTS));
    }

    public KnapsackMenu(int id, Inventory inventory, Container knapsack) {
        super(TPlayer.KNAPSACK_MENU.get(), id);
        this.knapsack = knapsack;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(knapsack, col + row * 9, 8 + col * 18, 17 + row * 18));
            }
        }
        this.addStandardInventorySlots(inventory, 8, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int size = this.knapsack.getContainerSize();
        if (index < size) {
            if (!this.moveItemStackTo(stack, size, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, size, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
