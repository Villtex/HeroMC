package backpack;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * InventoryBackpack: the slots of a backpack, read from the item once and written back into the same stack after
 * every change (onInventoryChanged -> saveInventory). {@code component} is CONTAINER for the storage and
 * {@link Backpack#CRAFTING} for the crafting grid of the workbench backpack ("Crafting" NBT).
 */
public class BackpackContainer extends SimpleContainer {
    private final ItemStack backpack;
    private final DataComponentType<ItemContainerContents> component;
    private boolean loading = true;

    public BackpackContainer(ItemStack backpack, DataComponentType<ItemContainerContents> component, int size) {
        super(size);
        this.backpack = backpack;
        this.component = component;
        NonNullList<ItemStack> items = load(backpack, component, size);
        for (int i = 0; i < size; i++) {
            this.setItem(i, items.get(i));
        }
        this.loading = false;
    }

    public static NonNullList<ItemStack> load(ItemStack backpack, DataComponentType<ItemContainerContents> component, int size) {
        NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);
        backpack.getOrDefault(component, ItemContainerContents.EMPTY).copyInto(items);
        return items;
    }

    public static void save(ItemStack backpack, DataComponentType<ItemContainerContents> component, java.util.List<ItemStack> items) {
        backpack.set(component, ItemContainerContents.fromItems(items.stream().map(ItemStack::copy).toList()));
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!this.loading) {
            save(this.backpack, this.component, this.getItems());
        }
    }

    /** SlotBackpack.isItemValid: no backpack inside a backpack. */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return !(stack.getItem() instanceof BackpackItem);
    }
}
