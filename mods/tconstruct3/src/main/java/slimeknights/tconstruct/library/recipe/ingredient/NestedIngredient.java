package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.stream.Stream;

/**
 * Ingredient that contains another ingredient nested inside.
 * <p>Port: NeoForge {@link ICustomIngredient} instead of {@code AbstractIngredient}; in JSON the nested ingredient is
 * either under {@code "match"} or, as in 1.20.1, given inline by {@code "item"} or {@code "tag"}.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class NestedIngredient implements ICustomIngredient {
  @Getter
  protected final Ingredient nested;

  @Override
  public boolean test(ItemStack stack) {
    return nested.test(stack);
  }

  @Override
  public Stream<Holder<Item>> items() {
    return nested.items();
  }

  @Override
  public SlotDisplay display() {
    return nested.display();
  }

  @Override
  public boolean isSimple() {
    return nested.isSimple();
  }

  /** Reads the nested ingredient: {@code "match"}, or the inline 1.20.1 vanilla keys {@code "item"}/{@code "tag"} */
  protected static Ingredient parseNested(JsonObject json, TypedMap context) {
    if (json.has("match")) {
      return IngredientLoadable.DISALLOW_EMPTY.convert(json.get("match"), "match", context);
    }
    JsonObject vanilla = new JsonObject();
    for (String key : new String[] {"item", "tag"}) {
      if (json.has(key)) {
        vanilla.add(key, json.get(key));
      }
    }
    return IngredientLoadable.DISALLOW_EMPTY.convert(vanilla, "match", context);
  }

  /** Writes the nested ingredient under {@code "match"} */
  protected static void serializeNested(Ingredient nested, JsonObject json) {
    JsonElement element = IngredientLoadable.DISALLOW_EMPTY.serialize(nested);
    json.add("match", element);
  }
}
