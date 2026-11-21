package tconstruct.player;

import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import tconstruct.TPlayer;

/**
 * ArmorExtendedContainer: the 7 extra slots (any item fits, as in 1.5.2), copies of the 4 armor slots and the player
 * inventory. Shift-click does nothing (transferStackInSlot returned null).
 */
public class ArmorExtendedMenu extends AbstractContainerMenu {
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final Identifier[] ICONS = {InventoryMenu.EMPTY_ARMOR_SLOT_HELMET, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
            InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS};
    public final Container armor;

    public ArmorExtendedMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(PlayerData.ARMOR_SLOTS));
    }

    public ArmorExtendedMenu(int id, Inventory inventory, Container armor) {
        super(TPlayer.ARMOR_MENU.get(), id);
        this.armor = armor;
        this.addSlot(new Slot(armor, 0, 80, 17));
        this.addSlot(new Slot(armor, 1, 80, 35));
        this.addSlot(new Slot(armor, 2, 116, 17));
        this.addSlot(new Slot(armor, 3, 116, 35));
        this.addSlot(new Slot(armor, 4, 152, 17));
        this.addSlot(new Slot(armor, 5, 152, 35));
        this.addSlot(new Slot(armor, 6, 152, 53));
        for (int i = 0; i < 4; i++) {
            this.addSlot(new ArmorSlot(inventory, inventory.player, ARMOR[i], 39 - i, 98, 8 + i * 18, ICONS[i]));
        }
        this.addStandardInventorySlots(inventory, 8, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
