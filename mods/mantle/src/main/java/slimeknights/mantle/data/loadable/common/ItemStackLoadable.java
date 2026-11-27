package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.field.LegacyField;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.LegacyNbt;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.function.BiFunction;
import java.util.function.Function;

/** Loadable for an item stack */
@SuppressWarnings("unused")  // API
public class ItemStackLoadable {
  private ItemStackLoadable() {}

  /* reused lambdas */
  /** Getter for an item from a stack */
  private static final Function<ItemStack,Item> ITEM_GETTER = ItemStack::getItem;
  /** Maps an item stack that may be empty to a strictly not empty one */
  private static final BiFunction<ItemStack,ErrorFactory,ItemStack> NOT_EMPTY = (stack, error) -> {
    if (stack.isEmpty()) {
      throw error.create("ItemStack cannot be empty");
    }
    return stack;
  };

  /* fields */
  /** Field for an optional item. Port: 26.3 stacks use "id", the 1.20.1 key "item" still works */
  private static final LoadableField<Item,ItemStack> ITEM = new LegacyField<>(Loadables.ITEM.defaultField("id", Items.AIR, false, ITEM_GETTER), "item");
  /** Field for item stack count that allows empty */
  private static final LoadableField<Integer,ItemStack> COUNT = IntLoadable.FROM_ZERO.defaultField("count", 1, true, ItemStack::getCount);
  /** Loadable for a data component patch */
  public static final Loadable<DataComponentPatch> COMPONENT_PATCH = new CodecLoadable<>(DataComponentPatch.CODEC);
  /** Field for the data components of the stack (port: replaces "nbt") */
  private static final LoadableField<DataComponentPatch,ItemStack> COMPONENTS = COMPONENT_PATCH.defaultField("components", DataComponentPatch.EMPTY, false, ItemStack::getComponentsPatch);
  /** Legacy NBT of 1.20.1 data, turned into components */
  private static final LoadableField<CompoundTag,ItemStack> NBT = NBTLoadable.ALLOW_STRING.nullableField("nbt", stack -> null);

  /* Optional */
  /** Single item which may be empty with a count of 1 */
  public static final Loadable<ItemStack> OPTIONAL_ITEM = Loadables.ITEM.flatXmap(item -> makeStack(item, 1, DataComponentPatch.EMPTY), ITEM_GETTER);
  /** Loadable for a stack that may be empty with variable count */
  public static final RecordLoadable<ItemStack> OPTIONAL_STACK = RecordLoadable.create(ITEM, COUNT, (item, count) -> makeStack(item, count, DataComponentPatch.EMPTY))
                                                                               .compact(OPTIONAL_ITEM, stack -> stack.getCount() == 1);
  /** Loadable for a stack that may be empty with components and a count of 1 */
  public static final RecordLoadable<ItemStack> OPTIONAL_ITEM_NBT = DataStack.FIXED_COUNT;
  /** Loadable for a stack that may be empty with variable count and components */
  public static final RecordLoadable<ItemStack> OPTIONAL_STACK_NBT = DataStack.READ_COUNT;

  /* Required */
  /** Single item which may not be empty with a count of 1 */
  public static final Loadable<ItemStack> REQUIRED_ITEM = notEmpty(OPTIONAL_ITEM);
  /** Loadable for a stack that may not be empty with variable count */
  public static final RecordLoadable<ItemStack> REQUIRED_STACK = notEmpty(OPTIONAL_STACK);
  /** Loadable for a stack that may not be empty with components and a count of 1 */
  public static final RecordLoadable<ItemStack> REQUIRED_ITEM_NBT = notEmpty(OPTIONAL_ITEM_NBT);
  /** Loadable for a stack that may not be empty with variable count and components */
  public static final RecordLoadable<ItemStack> REQUIRED_STACK_NBT = notEmpty(OPTIONAL_STACK_NBT);

  /* Names of Mantle 1.21 */
  public static final RecordLoadable<ItemStack> OPTIONAL_ITEM_DATA = OPTIONAL_ITEM_NBT;
  public static final RecordLoadable<ItemStack> OPTIONAL_STACK_DATA = OPTIONAL_STACK_NBT;
  public static final RecordLoadable<ItemStack> REQUIRED_ITEM_DATA = REQUIRED_ITEM_NBT;
  public static final RecordLoadable<ItemStack> REQUIRED_STACK_DATA = REQUIRED_STACK_NBT;


  /* Helpers */

  /** Makes an item stack from the given parameters */
  private static ItemStack makeStack(Item item, int count, DataComponentPatch components) {
    if (item == Items.AIR || count == 0) {
      return ItemStack.EMPTY;
    }
    ItemStack stack = new ItemStack(item, count);
    if (!components.isEmpty()) {
      stack.applyComponents(components);
    }
    return stack;
  }

  /** Creates a non-empty variant of the loadable */
  public static Loadable<ItemStack> notEmpty(Loadable<ItemStack> loadable) {
    return loadable.validate(NOT_EMPTY);
  }

  /** Creates a non-empty variant of the loadable */
  public static RecordLoadable<ItemStack> notEmpty(RecordLoadable<ItemStack> loadable) {
    return loadable.validate(NOT_EMPTY);
  }

  /** Loadable for an item stack with components (port: was NBT) */
  private enum DataStack implements RecordLoadable<ItemStack> {
    /** Reads count from JSON */
    READ_COUNT,
    /** Count is always 1 */
    FIXED_COUNT;

    /* General JSON */

    @Override
    public ItemStack deserialize(JsonObject json, TypedMap context) {
      int count = 1;
      if (this == READ_COUNT) {
        count = COUNT.get(json, context);
      }
      DataComponentPatch components = COMPONENTS.get(json, context);
      if (components.isEmpty() && json.has("nbt")) {
        components = LegacyNbt.toComponents(NBT.get(json, context));
      }
      return makeStack(ITEM.get(json, context), count, components);
    }

    @Override
    public void serialize(ItemStack stack, JsonObject json) {
      ITEM.serialize(stack, json);
      if (this == READ_COUNT) {
        COUNT.serialize(stack, json);
      }
      COMPONENTS.serialize(stack, json);
    }

    /* Compact JSON */

    @Override
    public ItemStack convert(JsonElement element, String key, TypedMap context) {
      if (element.isJsonPrimitive()) {
        return OPTIONAL_ITEM.convert(element, key, context);
      }
      return RecordLoadable.super.convert(element, key, context);
    }

    @Override
    public JsonElement serialize(ItemStack stack) {
      if ((this == FIXED_COUNT || stack.getCount() == 1) && stack.isComponentsPatchEmpty()) {
        return OPTIONAL_ITEM.serialize(stack);
      }
      return RecordLoadable.super.serialize(stack);
    }

    /* Buffer */

    @Override
    public ItemStack decode(FriendlyByteBuf buffer, TypedMap context) {
      ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer);
      if (this == FIXED_COUNT && !stack.isEmpty()) {
        stack.setCount(1);
      }
      return stack;
    }

    @Override
    public void encode(FriendlyByteBuf buffer, ItemStack stack) throws EncoderException {
      ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, stack);
    }
  }
}
