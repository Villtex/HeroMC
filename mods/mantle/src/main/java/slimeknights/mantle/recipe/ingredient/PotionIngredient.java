package slimeknights.mantle.recipe.ingredient;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.codec.LoadableMapCodec;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * Simple ingredient checking for an item with a specific potion ({@code mantle:potion}).
 * Port: potion contents are a data component; a stack with custom effects does not match.
 */
public class PotionIngredient extends ItemIngredient {
  /** JSON form: {@code item}/{@code tag} and {@code potion} */
  public static final RecordLoadable<PotionIngredient> LOADABLE = new RecordLoadable<>() {
    @Override
    public PotionIngredient deserialize(JsonObject json, TypedMap context) {
      return new PotionIngredient(readItems(json, context), readTag(json, context), Loadables.POTION.getIfPresent(json, "potion", context));
    }

    @Override
    public void serialize(PotionIngredient object, JsonObject json) {
      object.writeItems(json);
      json.add("potion", Loadables.POTION.serialize(object.potion));
    }

    @Override
    public PotionIngredient decode(FriendlyByteBuf buffer, TypedMap context) {
      return new PotionIngredient(readItems(buffer), null, Loadables.POTION.decode(buffer, context));
    }

    @Override
    public void encode(FriendlyByteBuf buffer, PotionIngredient object) {
      object.writeItems(buffer);
      Loadables.POTION.encode(buffer, object.potion);
    }
  };
  public static final MapCodec<PotionIngredient> CODEC = new LoadableMapCodec<>(LOADABLE);
  public static final StreamCodec<RegistryFriendlyByteBuf,PotionIngredient> STREAM_CODEC = StreamCodec.of((buf, value) -> LOADABLE.encode(buf, value), buf -> LOADABLE.decode(buf, TypedMap.EMPTY));

  private final Potion potion;
  protected PotionIngredient(List<Item> items, @Nullable TagKey<Item> tag, Potion potion) {
    super(items, tag);
    this.potion = potion;
  }

  /** Creates a potion ingredient matching a list of items */
  public static Ingredient of(Potion potion, List<ItemLike> items) {
    return new PotionIngredient(toItem(items), null, potion).toVanilla();
  }

  /** Creates a potion ingredient matching a list of items */
  public static Ingredient of(Potion potion, ItemLike... items) {
    return of(potion, Arrays.asList(items));
  }

  /** Creates a potion ingredient matching a tag */
  public static Ingredient of(Potion potion, TagKey<Item> tag) {
    return new PotionIngredient(List.of(), tag, potion).toVanilla();
  }

  private Holder<Potion> holder() {
    return BuiltInRegistries.POTION.wrapAsHolder(potion);
  }

  @Override
  public boolean test(ItemStack stack) {
    if (!testItem(stack)) {
      return false;
    }
    PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    return contents.customEffects().isEmpty() && contents.is(holder());
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public SlotDisplay display() {
    PotionContents contents = new PotionContents(holder());
    return new SlotDisplay.Composite(items().map(item -> {
      ItemStack stack = new ItemStack(item);
      stack.set(DataComponents.POTION_CONTENTS, contents);
      return (SlotDisplay) new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(stack));
    }).toList());
  }

  @Override
  public IngredientType<?> getType() {
    return MantleRecipes.POTION_INGREDIENT.get();
  }
}
