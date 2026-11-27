package slimeknights.mantle.recipe.helper;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.recipe.ICommonRecipe;

import java.util.function.Function;

/**
 * Recipe serializer for a recipe without fields, built from its ID.
 *
 * <p>Port: {@link RecipeSerializer} is a record in 26.3, so this is a factory now ({@link #of(Function)}).
 */
public final class SimpleRecipeSerializer {
  private SimpleRecipeSerializer() {}

  /** Creates the serializer */
  public static <T extends Recipe<?>> RecipeSerializer<T> of(Function<Identifier,T> constructor) {
    MapCodec<T> codec = MapCodec.unit(() -> constructor.apply(RecipeIdContext.getOrUnknown()));
    StreamCodec<RegistryFriendlyByteBuf,T> stream = StreamCodec.of(
      (buffer, recipe) -> buffer.writeIdentifier(recipe instanceof ICommonRecipe<?> common ? common.getId() : RecipeIdContext.UNKNOWN),
      buffer -> constructor.apply(buffer.readIdentifier()));
    return new RecipeSerializer<>(codec, stream);
  }
}
