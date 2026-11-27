package slimeknights.mantle.recipe.ingredient;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.codec.LoadableMapCodec;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Ingredient matching items regardless of potion, displaying all potion variants ({@code mantle:potion_display}).
 * Port: the display list is a {@link SlotDisplay} built on demand instead of a cached stack array.
 */
public class PotionDisplayIngredient extends ItemIngredient {
  public static final RecordLoadable<PotionDisplayIngredient> LOADABLE = new RecordLoadable<>() {
    @Override
    public PotionDisplayIngredient deserialize(JsonObject json, TypedMap context) {
      return new PotionDisplayIngredient(readItems(json, context), readTag(json, context));
    }

    @Override
    public void serialize(PotionDisplayIngredient object, JsonObject json) {
      object.writeItems(json);
    }

    @Override
    public PotionDisplayIngredient decode(FriendlyByteBuf buffer, TypedMap context) {
      return new PotionDisplayIngredient(readItems(buffer), null);
    }

    @Override
    public void encode(FriendlyByteBuf buffer, PotionDisplayIngredient object) {
      object.writeItems(buffer);
    }
  };
  public static final MapCodec<PotionDisplayIngredient> CODEC = new LoadableMapCodec<>(LOADABLE);
  public static final StreamCodec<RegistryFriendlyByteBuf,PotionDisplayIngredient> STREAM_CODEC = StreamCodec.of((buf, value) -> LOADABLE.encode(buf, value), buf -> LOADABLE.decode(buf, TypedMap.EMPTY));

  protected PotionDisplayIngredient(List<Item> items, @Nullable TagKey<Item> tag) {
    super(items, tag);
  }

  /** Creates a ingredient matching a list of items */
  public static Ingredient of(List<ItemLike> items) {
    return new PotionDisplayIngredient(toItem(items), null).toVanilla();
  }

  /** Creates a ingredient matching a list of items */
  public static Ingredient of(ItemLike... items) {
    return of(List.of(items));
  }

  /** Creates a ingredient matching a tag */
  public static Ingredient of(TagKey<Item> tag) {
    return new PotionDisplayIngredient(List.of(), tag).toVanilla();
  }

  @Override
  public boolean isSimple() {
    // Port: not simple in 26.3 terms, as the display differs from the matched items
    return false;
  }

  @Override
  public SlotDisplay display() {
    List<SlotDisplay> displays = items().flatMap(item -> BuiltInRegistries.POTION.listElements().map(potion -> {
      ItemStack stack = new ItemStack(item);
      stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
      return (SlotDisplay) new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(stack));
    })).toList();
    return new SlotDisplay.Composite(displays);
  }

  @Override
  public IngredientType<?> getType() {
    return MantleRecipes.POTION_DISPLAY_INGREDIENT.get();
  }
}
