package slimeknights.mantle.inventory.compat;

import lombok.Getter;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** Port: Forge's {@code InvWrapper}, a {@link Container} as item handler */
public class InvWrapper implements IItemHandlerModifiable {
  @Getter
  private final Container inv;

  public InvWrapper(Container inv) {
    this.inv = inv;
  }

  @Override
  public boolean equals(Object o) {
    return this == o || o instanceof InvWrapper that && that.inv.equals(inv);
  }

  @Override
  public int hashCode() {
    return inv.hashCode();
  }

  @Override
  public int getSlots() {
    return inv.getContainerSize();
  }

  @Override
  public ItemStack getStackInSlot(int slot) {
    return inv.getItem(slot);
  }

  @Override
  public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
    if (stack.isEmpty()) {
      return ItemStack.EMPTY;
    }
    ItemStack stackInSlot = inv.getItem(slot);
    int m;
    if (!stackInSlot.isEmpty()) {
      if (stackInSlot.getCount() >= Math.min(stackInSlot.getMaxStackSize(), getSlotLimit(slot))) {
        return stack;
      }
      if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot)) {
        return stack;
      }
      if (!inv.canPlaceItem(slot, stack)) {
        return stack;
      }
      m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot)) - stackInSlot.getCount();
      if (stack.getCount() <= m) {
        if (!simulate) {
          ItemStack copy = stack.copy();
          copy.grow(stackInSlot.getCount());
          inv.setItem(slot, copy);
          inv.setChanged();
        }
        return ItemStack.EMPTY;
      }
      // copy the stack to not modify the original one
      stack = stack.copy();
      if (!simulate) {
        ItemStack copy = stack.split(m);
        copy.grow(stackInSlot.getCount());
        inv.setItem(slot, copy);
        inv.setChanged();
        return stack;
      }
      stack.shrink(m);
      return stack;
    }
    if (!inv.canPlaceItem(slot, stack)) {
      return stack;
    }
    m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
    if (m < stack.getCount()) {
      // copy the stack to not modify the original one
      stack = stack.copy();
      if (!simulate) {
        inv.setItem(slot, stack.split(m));
        inv.setChanged();
        return stack;
      }
      stack.shrink(m);
      return stack;
    }
    if (!simulate) {
      inv.setItem(slot, stack);
      inv.setChanged();
    }
    return ItemStack.EMPTY;
  }

  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    if (amount == 0) {
      return ItemStack.EMPTY;
    }
    ItemStack stackInSlot = inv.getItem(slot);
    if (stackInSlot.isEmpty()) {
      return ItemStack.EMPTY;
    }
    if (simulate) {
      if (stackInSlot.getCount() < amount) {
        return stackInSlot.copy();
      }
      return stackInSlot.copyWithCount(amount);
    }
    int m = Math.min(stackInSlot.getCount(), amount);
    ItemStack decrStackSize = inv.removeItem(slot, m);
    inv.setChanged();
    return decrStackSize;
  }

  @Override
  public void setStackInSlot(int slot, ItemStack stack) {
    inv.setItem(slot, stack);
  }

  @Override
  public int getSlotLimit(int slot) {
    return inv.getMaxStackSize();
  }

  @Override
  public boolean isItemValid(int slot, ItemStack stack) {
    return inv.canPlaceItem(slot, stack);
  }
}
