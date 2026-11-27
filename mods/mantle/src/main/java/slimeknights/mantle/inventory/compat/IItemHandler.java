package slimeknights.mantle.inventory.compat;

import net.minecraft.world.item.ItemStack;

/**
 * Port: Forge's slot based item handler. Offer it to other mods through {@link ItemHandlers#asResourceHandler(IItemHandler)}.
 */
public interface IItemHandler {
  /** Number of slots */
  int getSlots();

  /** Stack in the slot, must not be modified */
  ItemStack getStackInSlot(int slot);

  /**
   * Inserts the stack into the slot
   * @param slot      Slot
   * @param stack     Stack to insert, not modified
   * @param simulate  If true, the insertion is only simulated
   * @return  Remainder that did not fit
   */
  ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

  /**
   * Extracts from the slot
   * @param slot      Slot
   * @param amount    Amount to extract
   * @param simulate  If true, the extraction is only simulated
   * @return  Extracted stack
   */
  ItemStack extractItem(int slot, int amount, boolean simulate);

  /** Maximum stack size of the slot */
  int getSlotLimit(int slot);

  /** True if the stack is valid in the slot */
  boolean isItemValid(int slot, ItemStack stack);
}
