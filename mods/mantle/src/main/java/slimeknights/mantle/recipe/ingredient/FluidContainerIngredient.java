package slimeknights.mantle.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.codec.LoadableMapCodec;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.fluid.compat.FluidHandlers;
import slimeknights.mantle.fluid.compat.IFluidHandler.FluidAction;
import slimeknights.mantle.fluid.compat.IFluidHandlerItem;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Ingredient that matches a container of fluid ({@code mantle:fluid_container}): exactly the fluid amount, and draining
 * it must leave the crafting remainder of the item.
 *
 * <p>Port: NeoForge {@link ICustomIngredient}; fluid lookups through {@link FluidHandlers}. JSON as in 1.20.1: the
 * fluid ingredient inline (or nested under {@code "fluid"}), {@code "display"} an item ingredient for recipe viewers.
 * {@code fromFluid(FluidObject)} comes back with the registration package.
 */
@SuppressWarnings("unused")  // API
public class FluidContainerIngredient implements ICustomIngredient {
  public static final Identifier ID = Mantle.getResource("fluid_container");
  public static final RecordLoadable<FluidContainerIngredient> LOADABLE = new RecordLoadable<>() {
    @Override
    public FluidContainerIngredient deserialize(JsonObject json, TypedMap context) {
      FluidIngredient fluid;
      // if we have fluid and its not a primitive, then its nested
      if (json.has("fluid") && !json.get("fluid").isJsonPrimitive()) {
        fluid = FluidIngredient.LOADABLE.getIfPresent(json, "fluid", context);
      } else {
        fluid = FluidIngredient.LOADABLE.convert(json, "fluid", context);
      }
      Ingredient display = null;
      if (json.has("display")) {
        display = IngredientLoadable.DISALLOW_EMPTY.convert(JsonHelper.getElement(json, "display"), "display", context);
      }
      return new FluidContainerIngredient(fluid, display);
    }

    @Override
    public void serialize(FluidContainerIngredient object, JsonObject json) {
      JsonElement element = object.fluidIngredient.serialize();
      if (element.isJsonObject()) {
        element.getAsJsonObject().entrySet().forEach(entry -> json.add(entry.getKey(), entry.getValue()));
      } else {
        json.add("fluid", element);
      }
      if (object.display != null) {
        json.add("display", IngredientLoadable.DISALLOW_EMPTY.serialize(object.display));
      }
    }

    @Override
    public FluidContainerIngredient decode(FriendlyByteBuf buffer, TypedMap context) {
      FluidIngredient fluid = FluidIngredient.LOADABLE.decode(buffer, context);
      Ingredient display = buffer.readBoolean() ? IngredientLoadable.DISALLOW_EMPTY.decode(buffer, context) : null;
      return new FluidContainerIngredient(fluid, display);
    }

    @Override
    public void encode(FriendlyByteBuf buffer, FluidContainerIngredient object) {
      FluidIngredient.LOADABLE.encode(buffer, object.fluidIngredient);
      buffer.writeBoolean(object.display != null);
      if (object.display != null) {
        IngredientLoadable.DISALLOW_EMPTY.encode(buffer, object.display);
      }
    }
  };
  public static final MapCodec<FluidContainerIngredient> CODEC = new LoadableMapCodec<>(LOADABLE);
  public static final StreamCodec<RegistryFriendlyByteBuf,FluidContainerIngredient> STREAM_CODEC = StreamCodec.of((buf, value) -> LOADABLE.encode(buf, value), buf -> LOADABLE.decode(buf, TypedMap.EMPTY));

  /** Ingredient to use for matching */
  private final FluidIngredient fluidIngredient;
  /** Internal ingredient to display the ingredient in recipe viewers */
  @Nullable
  private final Ingredient display;

  protected FluidContainerIngredient(FluidIngredient fluidIngredient, @Nullable Ingredient display) {
    this.fluidIngredient = fluidIngredient;
    this.display = display;
  }

  /** Creates an instance from a fluid ingredient with a display container */
  public static Ingredient fromIngredient(FluidIngredient ingredient, Ingredient display) {
    return new FluidContainerIngredient(ingredient, display).toVanilla();
  }

  /** Creates an instance from a fluid ingredient with no display, not recommended */
  public static Ingredient fromIngredient(FluidIngredient ingredient) {
    return new FluidContainerIngredient(ingredient, null).toVanilla();
  }

  @Override
  public boolean test(ItemStack stack) {
    if (stack.isEmpty()) {
      return false;
    }
    ItemStack single = stack.copyWithCount(1);
    Optional<IFluidHandlerItem> optional = FluidHandlers.getItemHandler(single);
    if (optional.isEmpty() || optional.get().getTanks() != 1) {
      return false;
    }
    IFluidHandlerItem handler = optional.get();
    FluidStack contained = handler.getFluidInTank(0);
    if (contained.isEmpty() || !fluidIngredient.test(contained.getFluid()) || fluidIngredient.getAmount(contained.getFluid()) != contained.getAmount()) {
      return false;
    }
    // draining must give exactly the fluid and leave the crafting remainder of the item
    int amount = contained.getAmount();
    FluidStack drained = handler.drain(amount, FluidAction.EXECUTE);
    if (drained.getFluid() != contained.getFluid() || drained.getAmount() != amount) {
      return false;
    }
    ItemStackTemplate remainder = stack.getItem().getCraftingRemainder();
    ItemStack expected = remainder == null ? ItemStack.EMPTY : remainder.create();
    return ItemStack.matches(expected, handler.getContainer());
  }

  @Override
  public Stream<Holder<Item>> items() {
    return display == null ? Stream.empty() : display.items();
  }

  @Override
  public SlotDisplay display() {
    return display == null ? SlotDisplay.Empty.INSTANCE : display.display();
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    return MantleRecipes.FLUID_CONTAINER_INGREDIENT.get();
  }
}
