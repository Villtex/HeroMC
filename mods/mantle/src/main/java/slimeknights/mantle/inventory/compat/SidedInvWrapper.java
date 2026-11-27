package slimeknights.mantle.inventory.compat;

import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/** Port: Forge's {@code SidedInvWrapper}, the slots of a {@link WorldlyContainer} reachable from one side */
public class SidedInvWrapper implements IItemHandlerModifiable {
  protected final WorldlyContainer inv;
  @Nullable
  protected final Direction side;
  private final InvWrapper wrapper;

  public SidedInvWrapper(WorldlyContainer inv, @Nullable Direction side) {
    this.inv = inv;
    this.side = side;
    this.wrapper = new InvWrapper(inv);
  }

  /** Container slot of the handler slot, -1 if none */
  public static int getSlot(WorldlyContainer inv, int slot, @Nullable Direction side) {
    int[] slots = inv.getSlotsForFace(side == null ? Direction.UP : side);
    if (slot < slots.length) {
      return slots[slot];
    }
    return -1;
  }

  private int slot(int slot) {
    return getSlot(inv, slot, side);
  }

  @Override
  public int getSlots() {
    return inv.getSlotsForFace(side == null ? Direction.UP : side).length;
  }

  @Override
  public ItemStack getStackInSlot(int slot) {
    int i = slot(slot);
    return i == -1 ? ItemStack.EMPTY : inv.getItem(i);
  }

  @Override
  public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
    int i = slot(slot);
    if (i == -1 || !inv.canPlaceItemThroughFace(i, stack, side)) {
      return stack;
    }
    return wrapper.insertItem(i, stack, simulate);
  }

  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    int i = slot(slot);
    if (i == -1 || !inv.canTakeItemThroughFace(i, inv.getItem(i), side == null ? Direction.UP : side)) {
      return ItemStack.EMPTY;
    }
    return wrapper.extractItem(i, amount, simulate);
  }

  @Override
  public void setStackInSlot(int slot, ItemStack stack) {
    int i = slot(slot);
    if (i != -1) {
      inv.setItem(i, stack);
    }
  }

  @Override
  public int getSlotLimit(int slot) {
    return inv.getMaxStackSize();
  }

  @Override
  public boolean isItemValid(int slot, ItemStack stack) {
    int i = slot(slot);
    return i != -1 && inv.canPlaceItem(i, stack);
  }
}
