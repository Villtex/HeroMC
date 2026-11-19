package bibliocraft.display;

import bibliocraft.common.BiblioMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** ContainerWeaponCase: one SlotWeaponCase (anything, up to 64) at (80, 36), player inventory at (8, 84). */
public class WeaponCaseMenu extends BiblioMenu {
    public WeaponCaseMenu(int id, Inventory inventory, Container container) {
        super(Displays.WEAPON_CASE_MENU.get(), id, container);
        this.addSlot(new Slot(container, 0, 80, 36) {
            @Override
            public int getMaxStackSize() {
                return 64;
            }
        });
        this.addPlayerInventory(inventory, 8, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return this.quickMove(player, index, 1, s -> true);
    }
}
