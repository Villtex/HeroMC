package xreliquary.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xreliquary.XRData;

/**
 * The "itemID"/"itemMeta"/"itemQuantity"(/"capacity") NBT of void tears and the void satchel: the item is kept by its
 * registry id ("item"), the count as "itemQuantity", the satchel's size as "capacity". Matching in containers
 * compares item and components (isItemEqual of 1.5.2 compared id and damage).
 */
public final class VoidContents {
    private VoidContents() {
    }

    public static Item item(ItemStack holder) {
        String id = XRData.tag(holder).getStringOr("item", "");
        Identifier key = Identifier.tryParse(id);
        return key == null ? Items.AIR : BuiltInRegistries.ITEM.getValue(key);
    }

    public static int quantity(ItemStack holder) {
        return XRData.tag(holder).getIntOr("itemQuantity", 0);
    }

    public static int capacity(ItemStack holder) {
        return XRData.tag(holder).getIntOr("capacity", 0);
    }

    public static void set(ItemStack holder, Item item, int quantity, int capacity) {
        CompoundTag tag = XRData.tag(holder);
        tag.putString("item", BuiltInRegistries.ITEM.getKey(item).toString());
        tag.putInt("itemQuantity", quantity);
        if (capacity >= 0) {
            tag.putInt("capacity", capacity);
        }
        holder.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
    }

    public static void setQuantity(ItemStack holder, int quantity) {
        CompoundTag tag = XRData.tag(holder);
        tag.putInt("itemQuantity", quantity);
        holder.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
    }

    /** getQuantityInInventory over the given slots. */
    public static int count(Container inv, int slots, ItemStack like) {
        int n = 0;
        for (int slot = 0; slot < slots; slot++) {
            ItemStack s = inv.getItem(slot);
            if (!s.isEmpty() && ItemStack.isSameItemSameComponents(s, like)) {
                n += s.getCount();
            }
        }
        return n;
    }

    /** findAndRemoveQuantity. */
    public static void remove(Container inv, int slots, ItemStack like, int quantity) {
        for (int slot = 0; slot < slots && quantity > 0; slot++) {
            ItemStack s = inv.getItem(slot);
            if (!s.isEmpty() && ItemStack.isSameItemSameComponents(s, like)) {
                int take = Math.min(quantity, s.getCount());
                inv.removeItem(slot, take);
                quantity -= take;
            }
        }
        inv.setChanged();
    }

    /** tryToAddToInventory: tops up the first stack that is not full, else fills the first empty slot with one. */
    public static boolean addOne(Container inv, int slots, Item item) {
        for (int slot = 0; slot < slots; slot++) {
            ItemStack s = inv.getItem(slot);
            if (!s.isEmpty() && s.is(item) && s.getComponentsPatch().isEmpty() && s.getCount() != s.getMaxStackSize()) {
                s.grow(1);
                inv.setChanged();
                return true;
            }
        }
        for (int slot = 0; slot < slots; slot++) {
            if (inv.getItem(slot).isEmpty()) {
                inv.setItem(slot, new ItemStack(item));
                return true;
            }
        }
        return false;
    }

    /** The dominant stackable item without data of a container (getTargetItem), or empty. */
    public static ItemStack target(Container inv, int slots, Item exclude) {
        ItemStack best = ItemStack.EMPTY;
        int bestCount = 0;
        for (int slot = 0; slot < slots; slot++) {
            ItemStack s = inv.getItem(slot);
            if (!s.isEmpty() && !s.is(exclude) && s.getMaxStackSize() != 1 && s.getComponentsPatch().isEmpty()) {
                int n = count(inv, slots, s);
                if (n > bestCount) {
                    bestCount = n;
                    best = s;
                }
            }
        }
        return best;
    }
}
