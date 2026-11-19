package betterstorage.backpack;

import betterstorage.item.BackpackItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * SlotArmorBackpack: the chest slot of the player's inventory while a backpack is worn. The backpack can only be
 * taken out while it is closed, empty and allowed by its type (canTake, containsItems).
 */
public class BackpackArmorSlot extends ArmorSlot {
    private final Player player;

    public BackpackArmorSlot(Player player, Slot old) {
        super(old.container, player, EquipmentSlot.CHEST, old.getContainerSlot(), old.x, old.y, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE);
        this.player = player;
    }

    @Override
    public boolean mayPickup(Player player) {
        ItemStack backpack = this.getItem();
        if (backpack.getItem() instanceof BackpackItem type) {
            return !Backpacks.isOpen(this.player) && type.canTake(backpack) && !type.containsItems(this.player) && super.mayPickup(player);
        }
        return super.mayPickup(player);
    }
}
