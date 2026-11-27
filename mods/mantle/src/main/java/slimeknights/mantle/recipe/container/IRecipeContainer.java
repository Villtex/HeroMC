package slimeknights.mantle.recipe.container;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Recipe input that is not a full inventory.
 *
 * <p>Port: 1.20.1 recipes took a {@code Container}; 26.3 recipes take a {@link RecipeInput} (read only), so this is
 * now a recipe input. The container style size method stays as an alias.
 */
public interface IRecipeContainer extends RecipeInput {
  /** Same as {@link #size()} (1.20.1 name) */
  default int getContainerSize() {
    return size();
  }

  @Override
  default boolean isEmpty() {
    for (int i = 0; i < size(); i++) {
      if (!getItem(i).isEmpty()) {
        return false;
      }
    }
    return true;
  }

  /** Counts the given item (1.20.1 {@code Container#countItem}) */
  default int countItem(net.minecraft.world.item.Item item) {
    int count = 0;
    for (int i = 0; i < size(); i++) {
      ItemStack stack = getItem(i);
      if (stack.is(item)) {
        count += stack.getCount();
      }
    }
    return count;
  }
}
