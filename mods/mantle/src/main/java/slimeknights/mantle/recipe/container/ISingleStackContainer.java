package slimeknights.mantle.recipe.container;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/**
 * Recipe input containing a single item stack
 */
public interface ISingleStackContainer extends IRecipeContainer {
  /**
   * Gets the stack in this inventory
   * @return  Stack in the inventory
   */
  ItemStack getStack();

  /* Multistack methods, redundant now */

  @Deprecated
  @Override
  default ItemStack getItem(int index) {
    return index == 0 ? getStack() : ItemStack.EMPTY;
  }

  @Override
  default boolean isEmpty() {
    return getStack().isEmpty();
  }

  @Deprecated
  @Override
  default int size() {
    return 1;
  }

  /* Utils made more efficient */

  @Override
  default int countItem(Item itemIn) {
    ItemStack stack = getStack();
    if (stack.getItem() == itemIn) {
      return stack.getCount();
    }
    return 0;
  }

  /** Checks if the stack is one of the items */
  default boolean hasAnyOf(Set<Item> set) {
    ItemStack stack = getStack();
    return !stack.isEmpty() && set.contains(getStack().getItem());
  }
}
