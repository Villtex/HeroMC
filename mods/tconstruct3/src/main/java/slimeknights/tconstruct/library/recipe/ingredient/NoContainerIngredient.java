package slimeknights.tconstruct.library.recipe.ingredient;

import slimeknights.mantle.util.LazyTagHolderSet;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.Nullable;

/** Ingredient matching an item with no container item, used to ensure NBT fluid items are empty */
public class NoContainerIngredient extends NestedIngredient {
  public static final Identifier ID = TConstruct.getResource("no_container");

  protected NoContainerIngredient(Ingredient nested) {
    super(nested);
  }

  @Override
  public boolean test(ItemStack stack) {
    return super.test(stack) && stack.getItem().getCraftingRemainder(stack) == null;
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    return TinkerIngredients.NO_CONTAINER.get();
  }

  /** Loader instance */
  public static final RecordLoadable<NoContainerIngredient> LOADABLE = new RecordLoadable<>() {
    @Override
    public NoContainerIngredient deserialize(JsonObject json, TypedMap context) {
      return new NoContainerIngredient(parseNested(json, context));
    }

    @Override
    public void serialize(NoContainerIngredient object, JsonObject json) {
      serializeNested(object.nested, json);
    }

    @Override
    public NoContainerIngredient decode(FriendlyByteBuf buffer, TypedMap context) {
      return new NoContainerIngredient(IngredientLoadable.DISALLOW_EMPTY.decode(buffer, context));
    }

    @Override
    public void encode(FriendlyByteBuf buffer, NoContainerIngredient object) {
      IngredientLoadable.DISALLOW_EMPTY.encode(buffer, object.nested);
    }
  };


  /* Static constructors */

  /** Creates an instance from the given nested ingredient */
  public static NoContainerIngredient of(Ingredient ingredient) {
    return new NoContainerIngredient(ingredient);
  }

  /** Creates an instance from the given items */
  public static NoContainerIngredient of(ItemLike... items) {
    return of(Ingredient.of(items));
  }

  /** Creates an instance from the given tag */
  public static NoContainerIngredient of(TagKey<Item> tag) {
    return of(LazyTagHolderSet.ingredient(tag));
  }
}
