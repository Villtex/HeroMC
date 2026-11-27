package slimeknights.mantle.recipe.helper;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Function;

/** Simple implementation of a recipe serializer with no properties other than recipe ID. */
public record SimpleRecipeSerializer<T extends Recipe<?>>(Function<Identifier,T> constructor) implements RecipeSerializer<T> {
  @Override
  public T fromJson(Identifier id, JsonObject pSerializedRecipe) {
    return constructor.apply(id);
  }

  @Override
  public T fromNetwork(Identifier id, FriendlyByteBuf pBuffer) {
    return constructor.apply(id);
  }

  @Override
  public void toNetwork(FriendlyByteBuf pBuffer, T pRecipe) {}
}
