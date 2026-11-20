package tconstruct.menu;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import tconstruct.TSounds;
import tconstruct.blocks.ToolStationBlockEntity;
import tconstruct.tools.ToolCoreItem;

/**
 * {@code ToolStationContainer} / {@code ToolForgeContainer}: the result (115, 38), three (forge: four) inputs and the
 * player inventory. The screen moves the inputs to the layout of the chosen tool (setSlotType); the positions below
 * are the ones of 1.5.2's container. Taking the result uses one of each input and the tool (all of a stack of arrows).
 */
public class ToolStationMenu extends TableMenu {
    public final boolean forge;

    public ToolStationMenu(int id, Inventory inventory, Container station, boolean forge) {
        super(TMenus.TOOL_STATION.get(), id, station, forge ? 5 : 4);
        this.forge = forge;
        this.addSlot(new ResultSlot(inventory.player, station, this));
        if (forge) {
            this.addSlot(new Slot(station, 1, 57, 29));
            this.addSlot(new Slot(station, 2, 39, 29));
            this.addSlot(new Slot(station, 3, 57, 47));
            this.addSlot(new Slot(station, 4, 39, 47));
        } else {
            this.addSlot(new Slot(station, 1, 57, 29));
            this.addSlot(new Slot(station, 2, 39, 38));
            this.addSlot(new Slot(station, 3, 57, 47));
        }
        this.addPlayer(inventory, 8, 84);
    }

    /** The name typed in the GUI (packet 1 of 1.5.2). */
    public void setToolName(String name) {
        if (this.table instanceof ToolStationBlockEntity station) {
            station.setToolName(name.length() > 40 ? name.substring(0, 40) : name);
            this.broadcastChanges();
        }
    }

    /** SlotTool / SlotToolForge.onCrafting. */
    void craftTool(Player player, ItemStack stack) {
        boolean full = !this.table.getItem(2).isEmpty() || !this.table.getItem(3).isEmpty();
        for (int i = 2; i < this.tableSlots; i++) {
            this.table.removeItem(i, 1);
        }
        int amount = this.table.getItem(1).getItem() instanceof ToolCoreItem ? stack.getCount() : 1;
        this.table.removeItem(1, amount);
        if (full && player.level() instanceof ServerLevel level) {
            if (this.forge) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F,
                        level.getRandom().nextFloat() * 0.1F + 0.9F);
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), TSounds.LITTLE_SAW.get(), SoundSource.BLOCKS, 1.0F,
                        (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F);
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int total = this.slots.size();
        if (index == 0) {
            if (!this.moveItemStackTo(stack, this.tableSlots, total, true)) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, copy);
        } else if (index < this.tableSlots) {
            if (!this.moveItemStackTo(stack, this.tableSlots, total, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 1, this.tableSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    /** SlotTool: nothing goes in; taking it builds the tool. */
    static class ResultSlot extends Slot {
        private final ToolStationMenu menu;

        ResultSlot(Player player, Container container, ToolStationMenu menu) {
            super(container, 0, 115, 38);
            this.menu = menu;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            this.menu.craftTool(player, stack);
            super.onTake(player, stack);
        }
    }
}
