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
    return parse(legacy(element), key, context);
  }

  /**
   * Port: 1.20.1 treated a tag nobody defines as empty; 26.3 refuses the whole data reload. Tags given as strings
   * (alone or in a list) therefore become lazy tag ingredients ({@link slimeknights.mantle.util.LazyTagHolderSet}), lists
   * with tags a NeoForge compound ingredient. Everything else goes through the 26.3 ingredient codec.
   */
  private static Ingredient parse(JsonElement element, String key, TypedMap context) {
    if (element.isJsonPrimitive() && element.getAsString().startsWith("#")) {
      net.minecraft.resources.Identifier id = net.minecraft.resources.Identifier.tryParse(element.getAsString().substring(1));
      if (id == null) {
        throw new JsonSyntaxException("Invalid ingredient '" + key + "': invalid tag " + element.getAsString());
      }
      return slimeknights.mantle.util.LazyTagHolderSet.ingredient(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, id));
    }
    if (element.isJsonArray()) {
      JsonArray array = element.getAsJsonArray();
      boolean hasTag = false;
      for (JsonElement child : array) {
        if (child.isJsonPrimitive() && child.getAsString().startsWith("#")) {
          hasTag = true;
          break;
        }
      }
      if (hasTag) {
        Ingredient[] children = new Ingredient[array.size()];
        for (int i = 0; i < children.length; i++) {
          children[i] = parse(array.get(i), key + "[" + i + "]", context);
        }
        return net.neoforged.neoforge.common.crafting.CompoundIngredient.of(children);
      }
    }
    // NeoForge combinations: parse the children the same way, so a missing tag inside does not fail the recipe
    if (element.isJsonObject() && element.getAsJsonObject().has("neoforge:ingredient_type")) {
      JsonObject object = element.getAsJsonObject();
      String type = GsonHelper.getAsString(object, "neoforge:ingredient_type");
      switch (type) {
        case "neoforge:difference" -> {
          return net.neoforged.neoforge.common.crafting.DifferenceIngredient.of(
            parse(legacy(object.get("base")), key + ".base", context), parse(legacy(object.get("subtracted")), key + ".subtracted", context));
        }
        case "neoforge:intersection", "neoforge:compound" -> {
          JsonArray children = GsonHelper.getAsJsonArray(object, "children");
          Ingredient[] parsed = new Ingredient[children.size()];
          for (int i = 0; i < parsed.length; i++) {
            parsed[i] = parse(legacy(children.get(i)), key + ".children[" + i + "]", context);
          }
          return type.equals("neoforge:compound") ? net.neoforged.neoforge.common.crafting.CompoundIngredient.of(parsed) : net.neoforged.neoforge.common.crafting.IntersectionIngredient.of(parsed);
        }
        default -> {}
      }
    }
    return Ingredient.CODEC.parse(ContextKey.registryOps(context), element).getOrThrow(message -> new JsonSyntaxException("Invalid ingredient '" + key + "': " + message));
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
