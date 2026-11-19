package betterstorage.block;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * InventoryTileEntity: the contents of one block entity or of both halves of a double chest / large locker one after
 * the other (main first). Opening and closing counts on the main block entity, which moves both lids.
 */
public final class TileInventory implements Container {
    public final ContainerBlockEntity main;
    private final ContainerBlockEntity[] parts;
    public final int columns;
    public final int rows;

    public TileInventory(ContainerBlockEntity main, ContainerBlockEntity... parts) {
        this.main = main;
        this.parts = parts;
        this.columns = main.getColumns();
        this.rows = this.getContainerSize() / this.columns;
    }

    @Override
    public int getContainerSize() {
        int size = 0;
        for (ContainerBlockEntity part : this.parts) {
            size += part.contents.size();
        }
        return size;
    }

    private NonNullList<ItemStack> list(int slot) {
        for (ContainerBlockEntity part : this.parts) {
            if (slot < part.contents.size()) {
                return part.contents;
            }
            slot -= part.contents.size();
        }
        return null;
    }

    private ContainerBlockEntity part(int slot) {
        for (ContainerBlockEntity part : this.parts) {
            if (slot < part.contents.size()) {
                return part;
            }
            slot -= part.contents.size();
        }
        return null;
    }

    private int index(int slot) {
        for (ContainerBlockEntity part : this.parts) {
            if (slot < part.contents.size()) {
                return slot;
            }
            slot -= part.contents.size();
        }
        return -1;
    }

    @Override
    public boolean isEmpty() {
        for (ContainerBlockEntity part : this.parts) {
            for (ItemStack stack : part.contents) {
                if (!stack.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        NonNullList<ItemStack> list = this.list(slot);
        return list == null ? ItemStack.EMPTY : list.get(this.index(slot));
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        NonNullList<ItemStack> list = this.list(slot);
        if (list == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = ContainerHelper.removeItem(list, this.index(slot), count);
        if (!stack.isEmpty()) {
            this.part(slot).setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        NonNullList<ItemStack> list = this.list(slot);
        return list == null ? ItemStack.EMPTY : ContainerHelper.takeItem(list, this.index(slot));
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        NonNullList<ItemStack> list = this.list(slot);
        if (list != null) {
            list.set(this.index(slot), stack);
            stack.limitSize(this.getMaxStackSize(stack));
            this.part(slot).setChanged();
        }
    }

    @Override
    public void setChanged() {
        for (ContainerBlockEntity part : this.parts) {
            part.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        for (ContainerBlockEntity part : this.parts) {
            if (!part.canPlayerUseContainer(player)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void startOpen(ContainerUser user) {
        this.main.onContainerOpened();
    }

    @Override
    public void stopOpen(ContainerUser user) {
        this.main.onContainerClosed();
    }

    @Override
    public void clearContent() {
        for (ContainerBlockEntity part : this.parts) {
            part.contents.replaceAll(s -> ItemStack.EMPTY);
            part.setChanged();
        }
    }
}
