package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.InstrumentComponent;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.json.TinkerLoadables;

import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * Ingredient matching an {@link net.minecraft.world.item.InstrumentItem} with a particular instrument.
 * <p>Port: {@link ICustomIngredient}; the instrument is the {@code minecraft:instrument} component (1.20.1 NBT
 * {@code instrument}). Without the component the stack counts as having no instrument.
 */
public class InstrumentIngredient implements ICustomIngredient {
  public static final Identifier ID = TConstruct.getResource("instrument");

  private final Item item;
  @Nullable
  private final ResourceKey<Instrument> instrument;
  @Nullable
  private final TagKey<Instrument> ignore;
  protected InstrumentIngredient(Item item, @Nullable ResourceKey<Instrument> instrument, @Nullable TagKey<Instrument> ignore) {
    this.item = item.asItem();
    this.instrument = instrument;
    this.ignore = ignore;
  }

  /** Creates a new instance matching the given instrument */
  public static InstrumentIngredient of(ItemLike item, ResourceKey<Instrument> instrument) {
    return new InstrumentIngredient(item.asItem(), instrument, null);
  }

  /** Creates a new instance ignoring the given tag */
  public static InstrumentIngredient of(ItemLike item, TagKey<Instrument> ignore) {
    return new InstrumentIngredient(item.asItem(), null, ignore);
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public boolean test(ItemStack stack) {
    if (!stack.is(item)) {
      return false;
    }
    InstrumentComponent component = stack.get(DataComponents.INSTRUMENT);
    if (component != null) {
      Holder<Instrument> holder = component.instrument();
      if (this.instrument != null) {
        return holder.is(this.instrument);
      }
      assert this.ignore != null;
      return !holder.is(ignore);
    }
    // if no instrument, its fine as long as we don't have a specific instrument
    return this.instrument == null;
  }

  @Override
  public Stream<Holder<Item>> items() {
    return Stream.of(item.builtInRegistryHolder());
  }

  @Override
  public SlotDisplay display() {
    // the instrument itself is in a dynamic registry, the plain item is shown
    return new SlotDisplay.ItemSlotDisplay(item.builtInRegistryHolder());
  }

  @Override
  public IngredientType<?> getType() {
    return TinkerIngredients.INSTRUMENT.get();
  }

  /** Loader instance */
  public static final RecordLoadable<InstrumentIngredient> LOADABLE = new RecordLoadable<>() {
    @Override
    public InstrumentIngredient deserialize(JsonObject json, TypedMap context) {
      Item item = Loadables.ITEM.getIfPresent(json, "item");
      ResourceKey<Instrument> instrument = null;
      TagKey<Instrument> ignore = null;
      if (json.has("instrument")) {
        instrument = ResourceKey.create(Registries.INSTRUMENT, JsonHelper.getResourceLocation(json, "instrument"));
      } else if (json.has("ignore")) {
        ignore = TinkerLoadables.INSTRUMENT_TAGS.getIfPresent(json, "ignore");
      } else {
        throw new JsonSyntaxException("Invalid InstrumentIngredient: must set either 'instrument' or 'ignore'");
      }
      return new InstrumentIngredient(item, instrument, ignore);
    }

    @Override
    public void serialize(InstrumentIngredient object, JsonObject json) {
      json.addProperty("item", Loadables.ITEM.getString(object.item));
      if (object.instrument != null) {
        json.addProperty("instrument", object.instrument.identifier().toString());
      }
      if (object.ignore != null) {
        json.addProperty("ignore", object.ignore.location().toString());
      }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void encode(FriendlyByteBuf buffer, InstrumentIngredient ingredient) {
      buffer.writeById(BuiltInRegistries.ITEM::getId, ingredient.item);
      if (ingredient.instrument != null) {
        buffer.writeBoolean(true);
        buffer.writeIdentifier(ingredient.instrument.identifier());
      } else {
        assert ingredient.ignore != null;
        buffer.writeBoolean(false);
        buffer.writeIdentifier(ingredient.ignore.location());
      }
    }

    @SuppressWarnings("deprecation")
    @Override
    public InstrumentIngredient decode(FriendlyByteBuf buffer, TypedMap context) {
      Item item = buffer.readById(BuiltInRegistries.ITEM::byId);
      // swap missing items for barriers
      if (item == null) {
        item = Items.BARRIER;
      }
      ResourceKey<Instrument> instrument = null;
      TagKey<Instrument> ignore = null;
      if (buffer.readBoolean()) {
        instrument = ResourceKey.create(Registries.INSTRUMENT, buffer.readIdentifier());
      } else {
        ignore = TagKey.create(Registries.INSTRUMENT, buffer.readIdentifier());
      }
      return new InstrumentIngredient(item, instrument, ignore);
    }
  };
}
