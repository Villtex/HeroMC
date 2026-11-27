package slimeknights.mantle.client.book.data.element;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import java.util.List;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.mantle.recipe.ingredient.SizedIngredient;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class IngredientData implements IDataElement {
  public SizedIngredient[] ingredients = new SizedIngredient[0];
  public String action;

  private transient String error;
  private transient NonNullList<ItemStack> items;
  private transient boolean customData;
  /** Port: stacks given with legacy NBT ({@code "type": "forge:nbt"}), they cannot be an {@link SizedIngredient} in 26.3 */
  private transient List<ItemStack> nbtStacks = new ArrayList<>();

  /**
   * Port (Teil 27c): turns item ID, count and legacy NBT of a book JSON into a stack. Mods replace this to map their old
   * NBT keys to components (Tinkers' {@code Material}); the default keeps unknown NBT as {@code minecraft:custom_data}.
   */
  public static NbtStackFactory nbtStackFactory = (item, count, nbt) -> {
    ItemStack stack = new ItemStack(item, count);
    stack.applyComponents(slimeknights.mantle.util.LegacyNbt.toComponents(nbt));
    return stack;
  };

  @FunctionalInterface
  public interface NbtStackFactory {
    ItemStack create(Item item, int count, CompoundTag nbt);
  }

  public NonNullList<ItemStack> getItems() {
    return this.items;
  }

  public static IngredientData getItemStackData(ItemStack stack) {
    IngredientData data = new IngredientData();
    data.items = NonNullList.withSize(1, stack);
    data.customData = true;

    return data;
  }

  public static IngredientData getItemStackData(NonNullList<ItemStack> items) {
    IngredientData data = new IngredientData();
    data.items = items;
    data.customData = true;

    return data;
  }

  @Override
  public void load(BookRepository source) {
    if (this.customData) {
      return;
    }

    ArrayList<ItemStack> stacks = new ArrayList<>();
    for(SizedIngredient ingredient : ingredients) {
      if(ingredient == null) {
        continue;
      }

      stacks.addAll(ingredient.getMatchingStacks());
    }
    stacks.addAll(nbtStacks);

    if(ingredients == null || stacks.isEmpty() || !StringUtil.isNullOrEmpty(error)) {
      items = NonNullList.withSize(1, getMissingItem());
      return;
    }

    items = NonNullList.of(getMissingItem(), stacks.toArray(new ItemStack[0]));
  }

  private ItemStack getMissingItem() {
    return getMissingItem(this.error);
  }

  private ItemStack getMissingItem(String error) {
    ItemStack missingItem = new ItemStack(Items.BARRIER);

    // Port: name and lore as components (were NBT display.Name / display.Lore)
    missingItem.set(DataComponents.CUSTOM_NAME, Component.literal("Error Loading Item"));
    if(!StringUtil.isNullOrEmpty(error)) {
      missingItem.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("Error:").withStyle(ChatFormatting.YELLOW), Component.literal(error).withStyle(ChatFormatting.YELLOW))));
    }

    return missingItem;
  }

  public static class Deserializer implements JsonDeserializer<IngredientData> {
    @Override
    public IngredientData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      IngredientData data = new IngredientData();

      if(json.isJsonArray()) {
        JsonArray array = json.getAsJsonArray();
        data.ingredients = new SizedIngredient[array.size()];

        for(int i = 0; i < array.size(); i++) {
          try {
            if (!readNbtStack(array.get(i), data)) {
              data.ingredients[i] = readIngredient(array.get(i));
            }
          } catch (Exception e) {
            data.ingredients[i] = SizedIngredient.of(Ingredient.of(Items.BARRIER));
          }
        }

        return data;
      }

      try {
        data.ingredients = readNbtStack(json, data) ? new SizedIngredient[0] : new SizedIngredient[]{ readIngredient(json) };
      } catch (Exception e) {
        data.error = e.getMessage();
        return data;
      }

      if(json.isJsonObject()) {
        JsonObject object = json.getAsJsonObject();
        if (object.has("action")) {
          JsonElement action = object.get("action");
          if (action.isJsonPrimitive()) {
            JsonPrimitive primitive = action.getAsJsonPrimitive();
            if (primitive.isString()) {
              data.action = primitive.getAsString();
            }
          }
        }
      }

      return data;
    }

    /** Port: reads {@code {"type": "forge:nbt", "item": ..., "count": ..., "nbt": ...}} into {@link #nbtStacks} */
    private static boolean readNbtStack(JsonElement json, IngredientData data) {
      if (!json.isJsonObject()) {
        return false;
      }
      JsonObject object = json.getAsJsonObject();
      String type = object.has("type") ? object.get("type").getAsString() : "";
      if (!object.has("nbt") || !(type.isEmpty() || type.endsWith(":nbt")) || !object.has("item")) {
        return false;
      }
      Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(object.get("item").getAsString()));
      int count = object.has("count") ? object.get("count").getAsInt() : 1;
      JsonElement nbtJson = object.get("nbt");
      CompoundTag nbt;
      try {
        nbt = nbtJson.isJsonPrimitive() ? net.minecraft.nbt.TagParser.parseCompoundFully(nbtJson.getAsString())
          : (CompoundTag) com.mojang.serialization.JsonOps.INSTANCE.convertTo(net.minecraft.nbt.NbtOps.INSTANCE, nbtJson);
      } catch (Exception e) {
        throw new JsonParseException("Invalid nbt in book item: " + e.getMessage(), e);
      }
      data.nbtStacks.add(nbtStackFactory.create(item, count, nbt));
      return true;
    }

    private SizedIngredient readIngredient(JsonElement json) {
      if(json.isJsonPrimitive()) {
        JsonPrimitive primitive = json.getAsJsonPrimitive();

        if(primitive.isString()) {
          Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(primitive.getAsString()));
          return SizedIngredient.fromItems(item);
        }
      }

      if(!json.isJsonObject()) {
        throw new JsonParseException("Must be an array, string or JSON object");
      }

      JsonObject object = json.getAsJsonObject();
      return SizedIngredient.deserialize(object);
    }
  }
}
