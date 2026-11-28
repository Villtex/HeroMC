package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;
import java.util.stream.Stream;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicateField;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;

import javax.annotation.Nullable;

/**
 * Ingredient matching material items with the given value. Typically, matches ingots or blocks
 */
@Getter
@RequiredArgsConstructor
public class MaterialValueIngredient implements ICustomIngredient {
  private final IJsonPredicate<MaterialVariantId> material;
  private final float minValue;
  private final float maxValue;

  /** Creates an ingredient matching a range of values */
  public static MaterialValueIngredient of(IJsonPredicate<MaterialVariantId> materials, float minValue, float maxValue) {
    return new MaterialValueIngredient(materials, minValue, maxValue);
  }

  /** Creates an ingredient matching an exact value */
  public static MaterialValueIngredient of(IJsonPredicate<MaterialVariantId> materials, float value) {
    return of(materials, value, value);
  }

  /** Checks the given material recipe against our filters */
  public boolean test(MaterialRecipe material) {
    float value = material.getValue() / (float) material.getNeeded();
    return minValue <= value && value <= maxValue && this.material.matches(material.getMaterial().getVariant());
  }

  @Override
  public boolean test(ItemStack stack) {
    MaterialRecipe recipe = MaterialRecipeCache.findRecipe(stack);
    return recipe != MaterialRecipe.EMPTY && test(recipe);
  }

  @Override
  public Stream<Holder<Item>> items() {
    return MaterialRecipeCache.getSortedRecipes().stream()
      .filter(this::test)
      .flatMap(material -> material.getIngredient().items())
      .distinct();
  }

  /** Port: not simple, the matching items come from the material recipes, which change on reload */
  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    return TinkerIngredients.MATERIAL_VALUE.get();
  }


  /* Helpers for ShapedMaterialRecipe */

  /** Checks if this ingredient fully contains the range of the other */
  private boolean contains(MaterialValueIngredient other) {
    return this.minValue <= other.minValue && other.maxValue <= this.maxValue;
  }

  /** Creates an ingredient that matches anything either of the two ingredients matches */
  public MaterialValueIngredient merge(MaterialValueIngredient other) {
    if (this == other) return this;

    // if we have the same predicate, we can possibly skip creating a new instance
    IJsonPredicate<MaterialVariantId> predicate = this.material;
    if (this.material.equals(other.material)) {
      if (this.contains(other)) {
        return this;
      }
      if (other.contains(this)) {
        return other;
      }
    } else {
      predicate = MaterialPredicate.or(this.material, other.material);
    }
    return new MaterialValueIngredient(predicate, Math.min(this.minValue, other.minValue), Math.max(this.maxValue, other.maxValue));
  }

  /** Gets the material matching this recipe */
  @Nullable
  public MaterialVariantId getMaterial(ItemStack stack) {
    MaterialRecipe recipe = MaterialRecipeCache.findRecipe(stack);
    return recipe != MaterialRecipe.EMPTY && test(recipe) ? recipe.getMaterial().getVariant() : null;
  }


  /* JSON */

  public static final Identifier ID = TConstruct.getResource("material_value");
  private static final LoadableField<IJsonPredicate<MaterialVariantId>, MaterialValueIngredient> MATERIAL_FIELD = new MaterialPredicateField<>("material", i -> i.material);
  /** Loader instance */
  public static final RecordLoadable<MaterialValueIngredient> LOADABLE = new RecordLoadable<>() {
    @Override
    public MaterialValueIngredient deserialize(JsonObject json, TypedMap context) {
      float minValue, maxValue;
      JsonElement value = json.get("value");
      if (value.isJsonPrimitive()) {
        minValue = maxValue = value.getAsJsonPrimitive().getAsFloat();
      } else {
        JsonObject object = GsonHelper.convertToJsonObject(value, "value");
        minValue = GsonHelper.getAsFloat(object, "min", 0);
        maxValue = GsonHelper.getAsFloat(object, "max", Float.POSITIVE_INFINITY);
      }
      return new MaterialValueIngredient(MATERIAL_FIELD.get(json, context), minValue, maxValue);
    }

    @Override
    public void serialize(MaterialValueIngredient object, JsonObject json) {
      MATERIAL_FIELD.serialize(object, json);
      if (object.minValue == object.maxValue) {
        json.addProperty("value", object.minValue);
      } else {
        JsonObject value = new JsonObject();
        if (object.minValue > 0) {
          value.addProperty("min", object.minValue);
        }
        if (Float.isFinite(object.maxValue)) {
          value.addProperty("max", object.maxValue);
        }
        json.add("value", value);
      }
    }

    @Override
    public MaterialValueIngredient decode(FriendlyByteBuf buffer, TypedMap context) {
      return new MaterialValueIngredient(MATERIAL_FIELD.decode(buffer, context), buffer.readFloat(), buffer.readFloat());
    }

    @Override
    public void encode(FriendlyByteBuf buffer, MaterialValueIngredient object) {
      MATERIAL_FIELD.encode(buffer, object);
      buffer.writeFloat(object.minValue);
      buffer.writeFloat(object.maxValue);
    }
  };
}
