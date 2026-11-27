package slimeknights.mantle.recipe.ingredient;

import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.array.ArrayLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Abstract ingredient that matches a list of items or a tag, mirroring the 1.20.1 syntax ({@code "item"} as ID or list,
 * {@code "tag"}).
 *
 * <p>Port: a NeoForge {@link ICustomIngredient} instead of a Forge {@code AbstractIngredient}. The JSON keeps the
 * Mantle 1.20.1 fields (the data converter leaves them), the network form sends the resolved items without the tag.
 */
public abstract class ItemIngredient implements ICustomIngredient {
  private static final Loadable<List<Item>> ITEM_LIST = Loadables.ITEM.list(ArrayLoadable.COMPACT_OR_EMPTY);
  /** Network form of the matched items */
  protected static final StreamCodec<RegistryFriendlyByteBuf,List<Holder<Item>>> ITEMS_STREAM = ByteBufCodecs.holderRegistry(Registries.ITEM).apply(ByteBufCodecs.list());

  protected final List<Item> items;
  @Nullable
  protected final TagKey<Item> tag;

  protected ItemIngredient(List<Item> items, @Nullable TagKey<Item> tag) {
    this.items = items;
    this.tag = tag;
  }

  /** Maps the list to a list of items */
  protected static List<Item> toItem(List<ItemLike> items) {
    return items.stream().map(ItemLike::asItem).toList();
  }

  /** Reads the item list from JSON */
  protected static List<Item> readItems(JsonObject json, TypedMap context) {
    return ITEM_LIST.getOrDefault(json, "item", List.of(), context);
  }

  /** Reads the item tag from JSON */
  @Nullable
  protected static TagKey<Item> readTag(JsonObject json, TypedMap context) {
    return json.has("tag") ? Loadables.ITEM_TAG.getIfPresent(json, "tag", context) : null;
  }

  /** Writes items and tag to JSON */
  protected void writeItems(JsonObject json) {
    if (!items.isEmpty()) {
      json.add("item", ITEM_LIST.serialize(items));
    }
    if (tag != null) {
      json.add("tag", Loadables.ITEM_TAG.serialize(tag));
    }
  }

  /** Writes the resolved items to the network */
  protected void writeItems(FriendlyByteBuf buffer) {
    ITEMS_STREAM.encode((RegistryFriendlyByteBuf) buffer, items().toList());
  }

  /** Reads the resolved items from the network */
  protected static List<Item> readItems(FriendlyByteBuf buffer) {
    return ITEMS_STREAM.decode((RegistryFriendlyByteBuf) buffer).stream().map(Holder::value).toList();
  }

  /** Checks the item part of the ingredient */
  protected boolean testItem(ItemStack stack) {
    return !stack.isEmpty() && (items.contains(stack.getItem()) || tag != null && stack.is(tag));
  }

  @Override
  public boolean test(ItemStack stack) {
    return testItem(stack);
  }

  @Override
  public Stream<Holder<Item>> items() {
    List<Holder<Item>> list = new ArrayList<>();
    for (Item item : items) {
      list.add(BuiltInRegistries.ITEM.wrapAsHolder(item));
    }
    if (tag != null) {
      for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
        if (!list.contains(holder)) {
          list.add(holder);
        }
      }
    }
    return list.stream();
  }
}
