package tconstruct.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import tconstruct.TItems;

/**
 * {@code PatternShaperContainer}: the blank (48, 35) and the pattern (106, 35). The buttons "Next / Previous Pattern"
 * choose the pattern (patternIndex of StencilTableGui, kept here on the server; the full guard is skipped). No
 * shift-clicking, as in 1.5.2.
 */
public class StencilTableMenu extends TableMenu {
    private int patternIndex;

    public StencilTableMenu(int id, Inventory inventory, Container table) {
        super(TMenus.STENCIL_TABLE.get(), id, table, 2);
        this.addSlot(new Slot(table, 0, 48, 35));
        this.addSlot(new TakeOnlySlot(table, 1, 106, 35));
        this.addPlayer(inventory, 8, 84);
    }

    /** actionPerformed: 0 next, 1 previous. */
    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != 0 && id != 1) {
            return false;
        }
        ItemStack blank = this.table.getItem(0);
        if (!blank.is(TItems.BLANK_PATTERN.get())) {
            return true;
        }
        int count = 25;
        if (id == 0) {
            this.patternIndex++;
            if (this.patternIndex == 21) {
                this.patternIndex++;
            }
            if (this.patternIndex >= count) {
                this.patternIndex = 0;
            }
        } else {
            this.patternIndex--;
            if (this.patternIndex == 21) {
                this.patternIndex--;
            }
            if (this.patternIndex < 0) {
                this.patternIndex = count - 1;
            }
        }
        this.table.setItem(1, new ItemStack(TItems.woodPattern(this.patternIndex + 1)));
        this.broadcastChanges();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
