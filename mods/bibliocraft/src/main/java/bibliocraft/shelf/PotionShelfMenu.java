package bibliocraft.shelf;

import bibliocraft.common.BCConfig;
import bibliocraft.common.BiblioMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** ContainerPotionShelf: three rows of four SlotPotionShelf at (53 + 18j, 15 + 19i), player inventory at (8, 84). */
public class PotionShelfMenu extends BiblioMenu {
    public PotionShelfMenu(int id, Inventory inventory, Container container) {
        super(Shelves.POTION_SHELF_MENU.get(), id, container);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                this.addSlot(new Slot(container, j + i * 4, 53 + j * 18, 15 + i * 19) {
                    /** SlotPotionShelf.isItemValid: Config.testPotionValidity. */
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return BCConfig.isPotion(stack);
                    }
                });
            }
        }
        this.addPlayerInventory(inventory, 8, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return this.quickMove(player, index, 12, BCConfig::isPotion);
    }
}
