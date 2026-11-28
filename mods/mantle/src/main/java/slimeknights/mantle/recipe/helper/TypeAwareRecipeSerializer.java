package slimeknights.mantle.recipe.helper;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.function.Supplier;

/**
 * Port: in 1.20.1 a recipe serializer that knows the recipe type it creates, so one recipe class can serve several
 * types (casting table and basin). 26.3 {@link RecipeSerializer} is a final record, so this is a handle that
 * {@link LoadableRecipeSerializer#of(slimeknights.mantle.data.loadable.record.RecordLoadable, Supplier)} creates next to
 * the serializer and puts into the loading context ({@link LoadableRecipeSerializer#TYPED_SERIALIZER}). Recipes keep it
 * and return {@link #serializer()} from {@link Recipe#getSerializer()}.
 */
public final class TypeAwareRecipeSerializer<T extends Recipe<?>> {
  private final Supplier<? extends RecipeType<?>> type;
  private RecipeSerializer<T> serializer;

  TypeAwareRecipeSerializer(Supplier<? extends RecipeType<?>> type) {
    this.type = type;
  }

  /** Recipe type of recipes from this serializer */
  public RecipeType<?> getType() {
    return type.get();
  }

  /** The registered serializer */
  public RecipeSerializer<T> serializer() {
    if (serializer == null) {
      throw new IllegalStateException("Serializer not yet created");
    }
    return serializer;
  }

  void setSerializer(RecipeSerializer<T> serializer) {
    this.serializer = serializer;
  }
}
