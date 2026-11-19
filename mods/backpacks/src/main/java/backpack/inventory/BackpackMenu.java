package backpack.inventory;

import backpack.Backpack;
import backpack.BackpackItem;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * ContainerBackpack: the backpack's rows at (8, 18), the player inventory below (offset (rows - 4) * 18). Also used for
 * the ender backpack with the player's ender chest inventory. On the client the slots hold a plain container.
 */
public class BackpackMenu extends AbstractContainerMenu {
    /**
     * Where the opened backpack is: the stack itself and its inventory slot (-1 = worn in the chest slot). The menu
     * is valid while exactly this stack is still there (the original compared the display names).
     */
    public record Location(Player player, ItemStack stack, int slot) {
        public boolean valid() {
            ItemStack now = this.slot < 0 ? this.player.getItemBySlot(EquipmentSlot.CHEST) : this.player.getInventory().getItem(this.slot);
            return now == this.stack && now.getItem() instanceof BackpackItem;
        }

        /** True if clicking {@code slot} of the menu (or hotbar-swapping with {@code button}) would move the open backpack. */
        boolean locks(AbstractContainerMenu menu, int slotId, int button, ContainerInput input) {
            if (this.slot < 0) {
                return false;
            }
            if (slotId >= 0 && slotId < menu.slots.size()) {
                Slot s = menu.slots.get(slotId);
                if (s.container == this.player.getInventory() && s.getContainerSlot() == this.slot) {
                    return true;
                }
            }
            return input == ContainerInput.SWAP && button == this.slot;
        }
    }

    /** SlotBackpack: takes everything except backpacks. */
    public static class BackpackSlot extends Slot {
        public BackpackSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !(stack.getItem() instanceof BackpackItem);
        }
    }

    private final int rows;
    private final Container backpack;
    private final @Nullable Location location;

    /** Client side: {@code rows} from the open packet. */
    public BackpackMenu(int id, Inventory inventory, int rows) {
        this(id, inventory, new SimpleContainer(rows * 9), rows, null);
    }

    public BackpackMenu(int id, Inventory inventory, Container backpack, int rows, @Nullable Location location) {
        super(Backpack.BACKPACK_MENU.get(), id);
        this.rows = rows;
        this.backpack = backpack;
        this.location = location;
        backpack.startOpen(inventory.player);
        int offset = (rows - 4) * 18;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new BackpackSlot(backpack, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + offset));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 161 + offset));
        }
    }

    public int rows() {
        return this.rows;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.location == null || this.location.valid() && this.backpack.stillValid(player);
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput input, Player player) {
        if (this.location != null && this.location.locks(this, slotId, button, input)) {
            return;
        }
        super.clicked(slotId, button, input, player);
    }

    /** transferStackInSlot: backpacks do not move; the backpack's rows to the end of the player inventory and back. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        if (stack.getItem() instanceof BackpackItem) {
            return ItemStack.EMPTY;
        }
        ItemStack result = stack.copy();
        int size = this.rows * 9;
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
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.backpack.stopOpen(player);
    }
}
