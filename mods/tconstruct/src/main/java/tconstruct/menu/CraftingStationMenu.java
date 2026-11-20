package tconstruct.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import tconstruct.blocks.CraftingStationBlockEntity;

/**
 * {@code CraftingStationContainer}: the crafting table menu (the client uses the vanilla crafting screen), whose grid
 * is the station's own and stays in the station when the GUI is closed.
 */
public class CraftingStationMenu extends CraftingMenu {
    private final CraftingStationBlockEntity station;

    public CraftingStationMenu(int id, Inventory inventory, CraftingStationBlockEntity station) {
        super(id, inventory, ContainerLevelAccess.create(station.getLevel(), station.getBlockPos()));
        for (int i = 0; i < 9; i++) {
            this.craftSlots.setItem(i, station.getItem(i).copy());
        }
        this.station = station;
        this.slotsChanged(this.craftSlots);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (this.station != null && container == this.craftSlots) {
            for (int i = 0; i < 9; i++) {
                this.station.setItem(i, this.craftSlots.getItem(i).copy());
            }
        }
    }

    @Override
    public void removed(Player player) {
        for (int i = 0; i < 9; i++) {
            this.station.setItem(i, this.craftSlots.getItem(i).copy());
        }
        this.craftSlots.clearContent();
        super.removed(player);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.station.stillValid(player);
    }
}
