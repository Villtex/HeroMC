package tconstruct.machine;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import tconstruct.TDeco;
import tconstruct.menu.TableMenu;

/**
 * DrawbridgeContainer: the block slot (only while nothing is laid out, DrawbridgeSlot), the camouflage slot, the player
 * inventory; data 0 = placement direction (the five buttons of the GUI, menu button id 0..4), 1 = extended or not.
 */
public class DrawbridgeMenu extends TableMenu {
    private final ContainerData data;
    private final DrawbridgeBlockEntity bridge;

    public DrawbridgeMenu(int id, Inventory inventory, int placement) {
        this(id, inventory, new SimpleContainer(2), null, new SimpleContainerData(2));
        this.data.set(0, placement);
    }

    public DrawbridgeMenu(int id, Inventory inventory, DrawbridgeBlockEntity bridge) {
        this(id, inventory, bridge, bridge, new ContainerData() {
            @Override
            public int get(int index) {
                return index == 0 ? bridge.placement : bridge.hasExtended() ? 1 : 0;
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) {
                    bridge.placement = value;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        });
    }

    private DrawbridgeMenu(int id, Inventory inventory, Container container, DrawbridgeBlockEntity bridge, ContainerData data) {
        super(TDeco.DRAWBRIDGE_MENU.get(), id, container, 2);
        this.bridge = bridge;
        this.data = data;
        this.addSlot(new Slot(container, 0, 80, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return DrawbridgeMenu.this.data.get(1) == 0;
            }

            @Override
            public boolean mayPickup(Player player) {
                return DrawbridgeMenu.this.data.get(1) == 0;
            }
        });
        this.addSlot(new Slot(container, 1, 35, 36));
        this.addPlayer(inventory, 8, 84);
        this.addDataSlots(data);
    }

    public int placement() {
        return this.data.get(0);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id <= 4) {
            this.data.set(0, id);
            if (this.bridge != null) {
                this.bridge.setChanged();
            }
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < 2) {
            if (!this.moveItemStackTo(stack, 2, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, 2, false)) {
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
