package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.GsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.data.loadable.field.ContextKey;
import java.util.Optional;
import java.util.stream.Stream;
import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.util.typed.TypedMap;

/** Loadable for ingredients, handling Forge ingredients */
public enum IngredientLoadable implements Loadable<Ingredient> {
  ALLOW_EMPTY,
  DISALLOW_EMPTY;

  /** Port: 26.3 refuses ingredients without items, an empty ingredient is {@code mantle:empty} (matches nothing) */
  public static Ingredient empty() {
    return slimeknights.mantle.recipe.ingredient.EmptyIngredient.ingredient();
  }

  @Override
  public Ingredient convert(JsonElement element, String key, TypedMap context) {
    if (this == ALLOW_EMPTY && (element.isJsonNull() || element.isJsonArray() && element.getAsJsonArray().isEmpty())) {
      return empty();
    }
    return Ingredient.CODEC.parse(ContextKey.registryOps(context), legacy(element)).getOrThrow(message -> new JsonSyntaxException("Invalid ingredient '" + key + "': " + message));
  }

  /**
   * Port: 1.20.1 ingredient objects {@code {"item": id}} and {@code {"tag": id}} (also in lists) in the 26.3 form
   * ({@code "id"}, {@code "#id"}), for data the converter did not touch.
   */
  public static JsonElement legacy(JsonElement element) {
    if (element.isJsonObject()) {
      JsonObject object = element.getAsJsonObject();
      if (object.size() == 1 && object.has("item")) {
        return object.get("item");
      }
      if (object.size() == 1 && object.has("tag")) {
        return new JsonPrimitive("#" + GsonHelper.getAsString(object, "tag"));
      }
    } else if (element.isJsonArray()) {
      JsonArray array = new JsonArray();
      for (JsonElement child : element.getAsJsonArray()) {
        array.add(legacy(child));
      }
      return array.size() == 1 ? array.get(0) : array;
    }
    return element;
  }

  @Override
  public JsonElement serialize(Ingredient object) {
    if (object.isEmpty()) {
      if (this == DISALLOW_EMPTY) {
        throw new IllegalArgumentException("Ingredient cannot be empty");
      }
      return new JsonArray();
    }
    return Ingredient.CODEC.encodeStart(ContextKey.registryOps(TypedMap.EMPTY), object).getOrThrow();
  }

  @Override
  public Ingredient decode(FriendlyByteBuf buffer, TypedMap context) {
    return Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer).orElseGet(IngredientLoadable::empty);
  }

  @Override
  public void encode(FriendlyByteBuf buffer, Ingredient object) {
    Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, object.isEmpty() ? Optional.empty() : Optional.of(object));
  }
}
