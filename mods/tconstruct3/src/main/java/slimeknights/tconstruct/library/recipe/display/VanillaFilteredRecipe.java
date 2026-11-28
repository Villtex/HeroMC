package slimeknights.tconstruct.library.recipe.display;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Interface for vanilla recipes to allow them to expose filtered recipes. Used since JEI will not filter the list to remove filtered recipes from the recipe manager. */
public interface VanillaFilteredRecipe<T extends FilteredRecipe> {
  /**
   * Gets a list of filtered recipes for adding to a recipe manager plugin.
   * @return  List of recipes
   * @param access  Registry access instance
   */
  List<T> getFilteredRecipes(RegistryAccess access);


  /** Gets a  modifiable list of filtered recipes matching the given recipe type. Port: from the 26.3 recipe map ({@code RecipeHelper.recipeMap(level)}) */
  static <T extends FilteredRecipe, C extends RecipeInput, R extends Recipe<C>> List<T> getRecipes(RegistryAccess access, RecipeMap map, RecipeType<R> type, Class<T> recipeClass) {
    return map.byType(type).stream().map(RecipeHolder::value)
      .flatMap(recipe -> recipe instanceof VanillaFilteredRecipe<?> filtered ? filtered.getFilteredRecipes(access).stream() : Stream.empty())
      .filter(recipeClass::isInstance).map(recipeClass::cast).collect(Collectors.toList());
  }
}
