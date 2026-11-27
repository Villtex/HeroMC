package slimeknights.mantle.recipe.container;

import net.minecraft.world.item.ItemStack;

/**
 * Recipe input for recipes that take no items, such as fluid or entity recipes.
 */
public interface IEmptyContainer extends IRecipeContainer {
  /** Empty instance */
  IEmptyContainer EMPTY = new IEmptyContainer() {};

  @Deprecated
  @Override
  default ItemStack getItem(int index) {
    return ItemStack.EMPTY;
  }

  @Deprecated
  @Override
  default boolean isEmpty() {
    return true;
  }

  @Deprecated
  @Override
  default int size() {
    return 0;
  }
}
