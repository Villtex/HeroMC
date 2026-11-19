package bibliocraft.display;

import bibliocraft.common.BiblioMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * ContainerArmor: the stand's helmet, chestplate, leggings and boots slots at (80, 8 + 18i), the player inventory at
 * (8, 84) (slots 4..39), then the player's own armor slots at (126, 8 + 18i) (slots 40..43, inventory 39..36).
 */
public class ArmorStandMenu extends BiblioMenu {
    public ArmorStandMenu(int id, Inventory inventory, Container container) {
        super(Displays.ArmorStand.MENU.get(), id, container);
        for (int i = 0; i < 4; i++) {
            int type = i;
            this.addSlot(new Slot(container, i, 80, 8 + i * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return ArmorRules.fitsSlot(stack, type);
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }
        this.addPlayerInventory(inventory, 8, 84);
        for (int i = 0; i < 4; i++) {
            int type = i;
            this.addSlot(new ArmorSlot(inventory, inventory.player, ArmorRules.SLOTS[i], 39 - i, 126, 8 + i * 18, null) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return ArmorRules.fitsSlot(stack, type);
                }
            });
        }
    }

    /** transferStackInSlot of ContainerArmor. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack inSlot = slot.getItem();
        ItemStack result = inSlot.copy();
        int type = ArmorRules.armorType(result);
        if (type >= 0) {
            if (index < 4) {
                // from the stand: onto the player if that armor slot is empty, else into the inventory
                if (player.getItemBySlot(ArmorRules.SLOTS[index]).isEmpty()) {
                    if (!this.moveItemStackTo(inSlot, 40 + index, 41 + index, true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(inSlot, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (player.getItemBySlot(ArmorRules.SLOTS[type]).isEmpty()) {
                // from the player: put it on if that armor slot is empty, else onto the stand
                if (!this.moveItemStackTo(inSlot, 40 + type, 41 + type, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(inSlot, type, type + 1, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (ArmorRules.isSkull(result)) {
            if (index == 0) {
                if (!this.moveItemStackTo(inSlot, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(inSlot, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (inSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (inSlot.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, inSlot);
        return result;
    }
}
