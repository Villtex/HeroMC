package slimeknights.mantle.inventory.compat;

import net.minecraft.core.NonNullList;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Port: Forge's {@code ItemStackHandler}. Saving uses {@link ValueOutput}/{@link ValueInput} instead of
 * {@code serializeNBT}/{@code deserializeNBT}, in the same format ({@code Size} and {@code Items} with {@code Slot}).
 */
public class ItemStackHandler implements IItemHandlerModifiable {
  protected NonNullList<ItemStack> stacks;

  public ItemStackHandler() {
    this(1);
  }

  public ItemStackHandler(int size) {
    stacks = NonNullList.withSize(size, ItemStack.EMPTY);
  }

  public ItemStackHandler(NonNullList<ItemStack> stacks) {
    this.stacks = stacks;
  }

  public void setSize(int size) {
    stacks = NonNullList.withSize(size, ItemStack.EMPTY);
  }

  @Override
  public void setStackInSlot(int slot, ItemStack stack) {
    validateSlotIndex(slot);
    this.stacks.set(slot, stack);
    onContentsChanged(slot);
  }

  @Override
  public int getSlots() {
    return stacks.size();
  }

  @Override
  public ItemStack getStackInSlot(int slot) {
    validateSlotIndex(slot);
    return this.stacks.get(slot);
  }

  @Override
  public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
    if (stack.isEmpty()) {
      return ItemStack.EMPTY;
    }
    if (!isItemValid(slot, stack)) {
      return stack;
    }
    validateSlotIndex(slot);
    ItemStack existing = this.stacks.get(slot);
    int limit = getStackLimit(slot, stack);
    if (!existing.isEmpty()) {
      if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) {
        return stack;
      }
      limit -= existing.getCount();
    }
    if (limit <= 0) {
      return stack;
    }
    boolean reachedLimit = stack.getCount() > limit;
    if (!simulate) {
      if (existing.isEmpty()) {
        this.stacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack);
      } else {
        existing.grow(reachedLimit ? limit : stack.getCount());
      }
      onContentsChanged(slot);
    }
    return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
  }

  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    if (amount == 0) {
      return ItemStack.EMPTY;
    }
    validateSlotIndex(slot);
    ItemStack existing = this.stacks.get(slot);
    if (existing.isEmpty()) {
      return ItemStack.EMPTY;
    }
    int toExtract = Math.min(amount, existing.getMaxStackSize());
    if (existing.getCount() <= toExtract) {
      if (!simulate) {
        this.stacks.set(slot, ItemStack.EMPTY);
        onContentsChanged(slot);
        return existing;
      }
      return existing.copy();
    }
    if (!simulate) {
      this.stacks.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
      onContentsChanged(slot);
    }
    return existing.copyWithCount(toExtract);
  }

  @Override
  public int getSlotLimit(int slot) {
    return 99;
  }

  protected int getStackLimit(int slot, ItemStack stack) {
    return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
  }

  @Override
  public boolean isItemValid(int slot, ItemStack stack) {
    return true;
  }

  /** Port: replaces {@code serializeNBT} */
  public void serialize(ValueOutput output) {
    ValueOutput.TypedOutputList<ItemStackWithSlot> list = output.list("Items", ItemStackWithSlot.CODEC);
    for (int i = 0; i < stacks.size(); i++) {
      ItemStack stack = stacks.get(i);
      if (!stack.isEmpty()) {
        list.add(new ItemStackWithSlot(i, stack));
      }
    }
    output.putInt("Size", stacks.size());
  }

  /** Port: replaces {@code deserializeNBT} */
  public void deserialize(ValueInput input) {
    setSize(input.getIntOr("Size", stacks.size()));
    for (ItemStackWithSlot item : input.listOrEmpty("Items", ItemStackWithSlot.CODEC)) {
      if (item.isValidInContainer(stacks.size())) {
        stacks.set(item.slot(), item.stack());
      }
    }
    onLoad();
  }

  protected void validateSlotIndex(int slot) {
    if (slot < 0 || slot >= stacks.size()) {
      throw new RuntimeException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
    }
  }

  protected void onLoad() {}

  protected void onContentsChanged(int slot) {}
}
