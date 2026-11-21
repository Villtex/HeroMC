package tconstruct.landmine;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import tconstruct.TDeco;
import tconstruct.menu.TableMenu;

/**
 * ContainerLandmine: three trigger slots (SlotBehavedOnly: only items with a behaviour, a stack put in may not be
 * larger than its limit, a different item only if both fit), the camouflage slot (one opaque full block), the player
 * inventory. Shift click does nothing (transferStackInSlot returned null).
 */
public class LandmineMenu extends TableMenu {
    public LandmineMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(4));
    }

    public LandmineMenu(int id, Inventory inventory, Container mine) {
        super(TDeco.LANDMINE_MENU.get(), id, mine, 4);
        this.addSlot(new TriggerSlot(mine, 0, 35, 53));
        this.addSlot(new TriggerSlot(mine, 1, 80, 53));
        this.addSlot(new TriggerSlot(mine, 2, 125, 53));
        this.addSlot(new CamoSlot(mine, 3, 80, 21));
        this.addPlayer(inventory, 8, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    static class TriggerSlot extends Slot {
        TriggerSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            LandmineLogic.Kind kind = LandmineLogic.kind(stack);
            if (kind == null) {
                return false;
            }
            ItemStack there = this.getItem();
            if (!there.isEmpty() && !ItemStack.isSameItem(there, stack)) {
                return there.getCount() + stack.getCount() <= kind.limit;
            }
            return stack.getCount() <= kind.limit;
        }
    }

    /** SlotOpaqueBlocksOnly. */
    public static class CamoSlot extends Slot {
        public CamoSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof BlockItem item && LandmineLogic.isOpaqueCube(item.getBlock().defaultBlockState());
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
