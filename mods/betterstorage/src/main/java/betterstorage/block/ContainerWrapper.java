package betterstorage.block;

import net.minecraft.world.Container;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** InventoryWrapper: passes everything to another inventory; subclasses change use checks and open/close. */
public class ContainerWrapper implements Container {
    public final Container base;

    public ContainerWrapper(Container base) {
        this.base = base;
    }

    @Override
    public int getContainerSize() {
        return this.base.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return this.base.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.base.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        return this.base.removeItem(slot, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return this.base.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.base.setItem(slot, stack);
    }

    @Override
    public void setChanged() {
        this.base.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.base.stillValid(player);
    }

    @Override
    public void startOpen(ContainerUser user) {
        this.base.startOpen(user);
    }

    @Override
    public void stopOpen(ContainerUser user) {
        this.base.stopOpen(user);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return this.base.canPlaceItem(slot, stack);
    }

    @Override
    public void clearContent() {
        this.base.clearContent();
    }
}
