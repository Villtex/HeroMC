package tconstruct.smeltery;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import tconstruct.TSmeltery;

/**
 * SmelteryContainer: 9 slots per layer of the smeltery in rows of three, 8 rows (24 slots) visible at a time and
 * scrolled in the GUI; the player inventory to the right. Slots hold one item each.
 */
public class SmelteryMenu extends AbstractContainerMenu {
    public final Container smeltery;
    public final ContainerData data;
    public final BlockPos pos;
    public final int size;
    /** slotRow: the first visible row (client). */
    public int slotRow;

    public SmelteryMenu(int id, Inventory inventory, SmelteryBlockEntity smeltery, int size) {
        this(id, inventory, smeltery, smeltery.data, smeltery.getBlockPos(), size);
    }

    public static SmelteryMenu client(int id, Inventory inventory, BlockPos pos, int size) {
        SimpleContainer container = new SimpleContainer(size) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        };
        return new SmelteryMenu(id, inventory, container, new SimpleContainerData(2 + 2 * size), pos, size);
    }

    private SmelteryMenu(int id, Inventory inventory, Container smeltery, ContainerData data, BlockPos pos, int size) {
        super(TSmeltery.SMELTERY_MENU.get(), id);
        this.smeltery = smeltery;
        this.data = data;
        this.pos = pos;
        this.size = size;
        for (int i = 0; i < size; i++) {
            int y = i / 3;
            this.addSlot(new ActiveSlot(smeltery, i, 2 + i % 3 * 22, 8 + y * 18));
        }
        for (int column = 0; column < 3; column++) {
            for (int row = 0; row < 9; row++) {
                this.addSlot(new Slot(inventory, row + column * 9 + 9, 90 + row * 18, 84 + column * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column, 90 + column * 18, 142));
        }
        this.addDataSlots(data);
    }

    /** ActiveSlot: only the 8 rows from {@link #slotRow} on take part. */
    public final class ActiveSlot extends Slot {
        public final int index;

        ActiveSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
            this.index = slot;
        }

        @Override
        public boolean isActive() {
            int row = this.index / 3;
            return row >= SmelteryMenu.this.slotRow && row < SmelteryMenu.this.slotRow + 8;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    public int fuelGague() {
        return this.data.get(0);
    }

    public int fuelAmount() {
        return this.data.get(1);
    }

    public int activeTemp(int slot) {
        return this.data.get(2 + slot * 2);
    }

    public int meltingTemp(int slot) {
        return this.data.get(3 + slot * 2);
    }

    /** scrollTo: the visible row for a scroll position 0..1, -1 if unchanged. */
    public int scrollTo(float scrollPos) {
        float total = (this.size - 24) / 3.0F;
        int row = (int) (total * scrollPos);
        if (row == this.slotRow) {
            return -1;
        }
        this.slotRow = row;
        return row;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.smeltery.stillValid(player);
    }

    /** transferStackInSlot with the stack limit of 1 per smeltery slot. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < this.size) {
            if (!this.moveItemStackTo(stack, this.size, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, this.size, false)) {
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
