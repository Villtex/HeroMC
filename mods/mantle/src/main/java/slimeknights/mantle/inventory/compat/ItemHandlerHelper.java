package slimeknights.mantle.inventory.compat;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/** Port: Forge's {@code ItemHandlerHelper} for {@link IItemHandler} */
public final class ItemHandlerHelper {
  private ItemHandlerHelper() {}

  /** Inserts the stack into all slots in order, returns the remainder */
  public static ItemStack insertItem(@Nullable IItemHandler dest, ItemStack stack, boolean simulate) {
    if (dest == null || stack.isEmpty()) {
      return stack;
    }
    for (int i = 0; i < dest.getSlots(); i++) {
      stack = dest.insertItem(i, stack, simulate);
      if (stack.isEmpty()) {
        return ItemStack.EMPTY;
      }
    }
    return stack;
  }

  /** Port: stacks are equal in item and components, ignoring count. Empty stacks never stack. */
  public static boolean canItemStacksStack(ItemStack a, ItemStack b) {
    return !a.isEmpty() && ItemStack.isSameItemSameComponents(a, b);
  }

  /** Same as {@link #canItemStacksStack(ItemStack, ItemStack)}, but also requires the stacks to be stackable */
  public static boolean canItemStacksStackRelaxed(ItemStack a, ItemStack b) {
    return !a.isEmpty() && !b.isEmpty() && a.isStackable() && ItemStack.isSameItemSameComponents(a, b);
  }

  /** Copy of the stack with the given size */
  public static ItemStack copyStackWithSize(ItemStack stack, int size) {
    if (size == 0) {
      return ItemStack.EMPTY;
    }
    return stack.copyWithCount(size);
  }

  /** Inserts first into slots holding the same item, then into empty slots */
  public static ItemStack insertItemStacked(@Nullable IItemHandler inventory, ItemStack stack, boolean simulate) {
    if (inventory == null || stack.isEmpty()) {
      return stack;
    }
    // not stackable: just insert into a new slot
    if (!stack.isStackable()) {
      return insertItem(inventory, stack, simulate);
    }
    int size = inventory.getSlots();
    // go through the inventory and try to fill up already existing items
    for (int i = 0; i < size; i++) {
      ItemStack slot = inventory.getStackInSlot(i);
      if (canItemStacksStackRelaxed(slot, stack)) {
        stack = inventory.insertItem(i, stack, simulate);
        if (stack.isEmpty()) {
          return ItemStack.EMPTY;
        }
      }
    }
    // insert remainder into empty slots
    for (int i = 0; i < size; i++) {
      if (inventory.getStackInSlot(i).isEmpty()) {
        stack = inventory.insertItem(i, stack, simulate);
        if (stack.isEmpty()) {
          return ItemStack.EMPTY;
        }
      }
    }
    return stack;
  }

  /** Gives the item to the player, dropping what does not fit */
  public static void giveItemToPlayer(Player player, ItemStack stack) {
    giveItemToPlayer(player, stack, -1);
  }

  /**
   * Gives the item to the player, preferring the given slot, and drops what does not fit.
   * Port: on top of {@link net.minecraft.world.entity.player.Inventory#add(int, ItemStack)}.
   */
  public static void giveItemToPlayer(Player player, ItemStack stack, int preferredSlot) {
    if (stack.isEmpty()) {
      return;
    }
    Level level = player.level();
    int before = stack.getCount();
    if (preferredSlot >= 0 && preferredSlot < player.getInventory().getNonEquipmentItems().size()) {
      player.getInventory().add(preferredSlot, stack);
    }
    if (!stack.isEmpty()) {
      player.getInventory().add(stack);
    }
    if (stack.getCount() != before) {
      level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
                      0.2F, ((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
    }
    if (!stack.isEmpty() && !level.isClientSide()) {
      player.drop(stack, false, Prediction.SERVER_ONLY);
    }
  }
}
