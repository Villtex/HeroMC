package betterstorage.crate;

import net.minecraft.world.item.ItemStack;

/** ICrateWatcher: told about every change of a pile's contents ({@code delta} items of {@code item}). */
public interface CrateWatcher {
    void onCrateItemsModified(ItemStack item, int delta);
}
