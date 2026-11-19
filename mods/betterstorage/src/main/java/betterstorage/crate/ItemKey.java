package betterstorage.crate;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** ItemIdentifier: an item with its data (id, damage and NBT then; item and components now), without the count. */
public final class ItemKey {
    private final ItemStack stack;

    public ItemKey(ItemStack stack) {
        this.stack = stack.copyWithCount(1);
    }

    public Item getItem() {
        return this.stack.getItem();
    }

    public ItemStack createStack(int size) {
        return this.stack.copyWithCount(size);
    }

    public boolean matches(ItemStack other) {
        return ItemStack.isSameItemSameComponents(this.stack, other);
    }

    /** calcNumStacks: how many slots {@code count} items take. */
    public static int calcNumStacks(Item item, int count) {
        int maxStackSize = item.getDefaultMaxStackSize();
        return (count + maxStackSize - 1) / maxStackSize;
    }

    public static int calcNumStacks(ItemStack stack) {
        int maxStackSize = stack.getMaxStackSize();
        return (stack.getCount() + maxStackSize - 1) / maxStackSize;
    }

    public int maxStackSize() {
        return this.stack.getMaxStackSize();
    }

    @Override
    public int hashCode() {
        return ItemStack.hashItemAndComponents(this.stack);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ItemKey other && ItemStack.isSameItemSameComponents(this.stack, other.stack);
    }
}
