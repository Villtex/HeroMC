package slimeknights.mantle.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import slimeknights.mantle.recipe.helper.RecipeHelper;

/**
 * Recipe that has an output and is not shown in the recipe book (machines, tables).
 *
 * <p>Port: brings back the parts of the 1.20.1 {@code Recipe} interface that 26.3 removed, so recipes written for it
 * keep their methods: {@link #getId()} (the ID now lives in the recipe holder, Mantle hands it to the recipe while
 * loading, see {@link slimeknights.mantle.recipe.helper.RecipeIdContext}), {@link #getResultItem(RegistryAccess)},
 * {@link #assemble(RecipeInput, RegistryAccess)}, {@link #getGroup()}, {@link #getIngredients()} and
 * {@link #canCraftInDimensions(int, int)}. The 26.3 methods get defaults for recipes outside the recipe book.
 * @param <C>  Recipe input type
 */
public interface ICommonRecipe<C extends RecipeInput> extends Recipe<C> {
  /** ID of the recipe (1.20.1 {@code Recipe#getId}) */
  Identifier getId();

  /** Result of the recipe without an input (1.20.1 {@code Recipe#getResultItem}) */
  ItemStack getResultItem(RegistryAccess access);

  /** 1.20.1 {@code Recipe#assemble} with registries */
  default ItemStack assemble(C inv, RegistryAccess access) {
    return getResultItem(access).copy();
  }

  @Override
  default ItemStack assemble(C input) {
    return assemble(input, RecipeHelper.registryAccess());
  }

  /** 1.20.1 {@code Recipe#canCraftInDimensions}, no longer used by 26.3 */
  @Deprecated
  default boolean canCraftInDimensions(int width, int height) {
    return true;
  }

  /** Ingredients for display (1.20.1 {@code Recipe#getIngredients}) */
  default NonNullList<Ingredient> getIngredients() {
    return NonNullList.create();
  }

  /** Recipe group (1.20.1 {@code Recipe#getGroup}) */
  default String getGroup() {
    return "";
  }

  @Override
  default String group() {
    return getGroup();
  }

  @Override
  default boolean isSpecial() {
    return true;
  }

  @Override
  default boolean showNotification() {
    return false;
  }

  @Override
  default PlacementInfo placementInfo() {
    return PlacementInfo.NOT_PLACEABLE;
  }

  @Override
  default RecipeBookCategory recipeBookCategory() {
    return RecipeBookCategories.CRAFTING_MISC;
  }
}
