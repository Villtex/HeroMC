package slimeknights.mantle.inventory.compat;

import net.minecraft.world.item.ItemStack;

/** Port: Forge's item handler that allows setting slots directly */
public interface IItemHandlerModifiable extends IItemHandler {
  /** Sets the stack in the slot, no validity checks */
  void setStackInSlot(int slot, ItemStack stack);
}
