package tconstruct.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** FrypanContainer: the fuel slot, 2 x 4 food slots, the player inventory; the fuel and the progress for the gauges. */
public class FrypanMenu extends TableMenu {
    private final ContainerData data;

    public FrypanMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(10), new SimpleContainerData(3));
    }

    public FrypanMenu(int id, Inventory inventory, Container pan, ContainerData data) {
        super(TMenus.FRYPAN.get(), id, pan, 9);
        this.data = data;
        this.addSlot(new Slot(pan, 1, 26, 45));
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 4; x++) {
                this.addSlot(new Slot(pan, 2 + x + y * 4, 70 + x * 18, 27 + y * 18));
            }
        }
        this.addPlayer(inventory, 8, 84);
        this.addDataSlots(data);
    }

    /** gaugeFuelScaled. */
    public int fuelScaled(int scale) {
        int gague = this.data.get(1);
        if (gague == 0) {
            gague = this.data.get(0);
            if (gague == 0) {
                gague = 1000;
            }
        }
        return this.data.get(0) * scale / gague;
    }

    /** gaugeProgressScaled. */
    public int progressScaled(int scale) {
        return this.data.get(2) * scale / 1000;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < 9) {
            if (!this.moveItemStackTo(stack, 9, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 1, 9, false)) {
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
