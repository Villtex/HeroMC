package bibliocraft.printing;

import bibliocraft.common.BiblioMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * ContainerPrintPress: SlotInk (any dye) at (80, 8), SlotPrintPlate (a plate, one) at (80, 29), SlotBlankBook (blank
 * books) at (36, 54), SlotFinishedBook (take only, one) at (123, 54), player inventory at (8, 84). As in the original
 * the block never opens it; it is kept registered with its screen.
 */
public class PrintPressMenu extends BiblioMenu {
    public PrintPressMenu(int id, Inventory inventory, Container container) {
        super(Presses.PRESS_MENU.get(), id, container);
        this.addSlot(new Slot(container, 0, 80, 8) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return PrintPressBlockEntity.isDye(stack);
            }
        });
        this.addSlot(new Slot(container, 1, 80, 29) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof PlateItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addSlot(new Slot(container, 2, 36, 54) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.BOOK);
            }
        });
        this.addSlot(new Slot(container, 3, 123, 54) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addPlayerInventory(inventory, 8, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return this.quickMove(player, index, 4, s -> true);
    }
}
