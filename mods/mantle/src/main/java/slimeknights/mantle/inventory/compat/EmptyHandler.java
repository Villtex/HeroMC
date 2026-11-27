package slimeknights.mantle.inventory.compat;

import net.minecraft.world.item.ItemStack;

/** Port: Forge's {@code EmptyHandler}, an item handler without slots */
public class EmptyHandler implements IItemHandlerModifiable {
  public static final IItemHandler INSTANCE = new EmptyHandler();

  @Override
  public int getSlots() {
    return 0;
  }

  @Override
  public ItemStack getStackInSlot(int slot) {
    return ItemStack.EMPTY;
  }

  @Override
  public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
    return stack;
  }

  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    return ItemStack.EMPTY;
  }

  @Override
  public void setStackInSlot(int slot, ItemStack stack) {}

  @Override
  public int getSlotLimit(int slot) {
    return 0;
  }

  @Override
  public boolean isItemValid(int slot, ItemStack stack) {
    return false;
  }
}
