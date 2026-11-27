package slimeknights.mantle.recipe.helper;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.codec.LoadableMapCodec;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.ICommonRecipe;
import slimeknights.mantle.util.typed.TypedMapBuilder;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Recipe serializer instance using loadables.
 *
 * <p>Port: {@link RecipeSerializer} is a record of a map codec and a stream codec in 26.3, so this class only has the
 * factories. While loading, {@link ContextKey#ID} holds the recipe ID ({@link RecipeIdContext}); over the network the
 * ID of an {@link ICommonRecipe} is sent ahead of the recipe. The serializer itself is not in the context any more
 * ({@code SERIALIZER} stays empty), the recipe type is ({@link #TYPE}, with {@link #of(RecordLoadable, Supplier)}).
 */
public final class LoadableRecipeSerializer {
  /** Context key to use if you want the recipe serializer passed into your recipe. Port: no longer filled. */
  @Deprecated
  public static final ContextKey<RecipeSerializer<?>> SERIALIZER = new ContextKey<>("serializer");
  /** Context key to use if you want the recipe type passed into your recipe, requires {@link #of(RecordLoadable, Supplier)} for your serializer. */
  public static final ContextKey<RecipeType<?>> TYPE = new ContextKey<>("type");
  /** Field for a group key in a recipe (common requirement) */
  public static final LoadableField<String,Recipe<?>> RECIPE_GROUP = StringLoadable.DEFAULT.defaultField("group", "", Recipe::group);

  private LoadableRecipeSerializer() {}

  /** Creates a standard serializer from a loadable */
  public static <T extends Recipe<?>> RecipeSerializer<T> of(RecordLoadable<T> loadable) {
    return create(loadable, builder -> {});
  }

  /** Creates a type aware serializer from a loadable */
  public static <T extends R, R extends Recipe<?>> RecipeSerializer<T> of(RecordLoadable<T> loadable, Supplier<? extends RecipeType<R>> type) {
    return create(loadable, builder -> builder.put(TYPE, type.get()));
  }

  /** Creates a serializer for a deprecated recipe format */
  public static <T extends Recipe<?>> RecipeSerializer<T> deprecated(RecordLoadable<T> loadable, String replacement) {
    return create(loadable, builder -> Mantle.logger.warn("Using deprecated recipe serializer for recipe {}, {}", RecipeIdContext.getOrUnknown(), replacement));
  }

  /** Builds the serializer */
  private static <T extends Recipe<?>> RecipeSerializer<T> create(RecordLoadable<T> loadable, Consumer<TypedMapBuilder> extra) {
    LoadableMapCodec<T> codec = new LoadableMapCodec<>(loadable, builder -> {
      Identifier id = RecipeIdContext.getOrUnknown();
      builder.put(ContextKey.ID, id).put(ContextKey.DEBUG, "Recipe " + id);
      extra.accept(builder);
    });
    StreamCodec<RegistryFriendlyByteBuf,T> stream = new StreamCodec<>() {
      @Override
      public T decode(RegistryFriendlyByteBuf buffer) {
        Identifier id = buffer.readIdentifier();
        TypedMapBuilder builder = TypedMapBuilder.builder().put(ContextKey.ID, id).put(ContextKey.DEBUG, "Recipe " + id);
        extra.accept(builder);
        try {
          return loadable.decode(buffer, builder.build());
        } catch (RuntimeException e) {
          Mantle.logger.error("Error reading recipe {} from packet using loadable {}", id, loadable, e);
          throw e;
        }
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buffer, T recipe) {
        buffer.writeIdentifier(recipe instanceof ICommonRecipe<?> common ? common.getId() : RecipeIdContext.UNKNOWN);
        loadable.encode(buffer, recipe);
      }
    };
    return new RecipeSerializer<>(codec, stream);
  }
}
