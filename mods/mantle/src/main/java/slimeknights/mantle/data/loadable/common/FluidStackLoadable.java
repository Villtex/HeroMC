package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonObject;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.util.LegacyNbt;
import slimeknights.mantle.util.typed.TypedMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/** Loadable for a fluid stack */
@SuppressWarnings("unused")  // API
public class FluidStackLoadable {
  private FluidStackLoadable() {}

  /* reused lambdas */
  /** Getter for an item from a stack */
  private static final Function<FluidStack,Fluid> FLUID_GETTER = FluidStack::getFluid;
  /** Checks if a stack can be serialized to a primitive, ignoring count */
  private static final Predicate<FluidStack> COMPACT_NBT = stack -> stack.isComponentsPatchEmpty();
  /** Maps a fluid stack that may be empty to a strictly not empty one */
  private static final BiFunction<FluidStack,ErrorFactory,FluidStack> NOT_EMPTY = (stack, error) -> {
    if (stack.isEmpty()) {
      throw error.create("FluidStack cannot be empty");
    }
    return stack;
  };

  /* fields */
  /** Field for an optional fluid */
  private static final LoadableField<Fluid,FluidStack> FLUID = Loadables.FLUID.defaultField("fluid", Fluids.EMPTY, false, FLUID_GETTER);
  /** Field for fluid stack count that allows empty */
  private static final LoadableField<Integer,FluidStack> AMOUNT = IntLoadable.FROM_ZERO.requiredField("amount", FluidStack::getAmount);
  /** Field for the data components (port: the 1.20.1 key "nbt" still works, see {@link LegacyNbt}) */
  private static final LoadableField<DataComponentPatch,FluidStack> NBT = new ComponentsField();


  /* Optional */
  /** Single item which may be empty with an amount of 1000 */
  public static final Loadable<FluidStack> OPTIONAL_BUCKET = fixedSize(FluidType.BUCKET_VOLUME);
  /** Loadable for a stack that may be empty with variable count */
  public static final RecordLoadable<FluidStack> OPTIONAL_STACK = RecordLoadable.create(FLUID, AMOUNT, (fluid, count) -> makeStack(fluid, count, DataComponentPatch.EMPTY));
  /** Loadable for a stack that may be empty with NBT and an amount of 1000 */
  public static final RecordLoadable<FluidStack> OPTIONAL_BUCKET_NBT = fixedSizeNBT(FluidType.BUCKET_VOLUME);
  /** Loadable for a stack that may be empty with variable count and NBT */
  public static final RecordLoadable<FluidStack> OPTIONAL_STACK_NBT = RecordLoadable.create(FLUID, AMOUNT, NBT, FluidStackLoadable::makeStack);


  /* Required */
  /** Single item which may not be empty with an amount of 1000 */
  public static final Loadable<FluidStack> REQUIRED_BUCKET = notEmpty(OPTIONAL_BUCKET);
  /** Loadable for a stack that may not be empty with variable count */
  public static final RecordLoadable<FluidStack> REQUIRED_STACK = notEmpty(OPTIONAL_STACK);
  /** Loadable for a stack that may not be empty with NBT and an amount of 1000 */
  public static final RecordLoadable<FluidStack> REQUIRED_BUCKET_NBT = notEmpty(OPTIONAL_BUCKET_NBT);
  /** Loadable for a stack that may not be empty with variable count and NBT */
  public static final RecordLoadable<FluidStack> REQUIRED_STACK_NBT = notEmpty(OPTIONAL_STACK_NBT);


  /* Helpers */

  /** Port: reads {@code "components"} (or legacy {@code "nbt"}) without building a stack, for lazy outputs */
  public static DataComponentPatch readComponents(JsonObject json, TypedMap context) {
    return NBT.get(json, context);
  }

  /** Makes an item stack from the given parameters */
  private static FluidStack makeStack(Fluid fluid, int amount, DataComponentPatch components) {
    if (fluid == Fluids.EMPTY || amount <= 0) {
      return FluidStack.EMPTY;
    }
    return new FluidStack(fluid.builtInRegistryHolder(), amount, components);
  }

  /** Reads "components", or the legacy "nbt" */
  private record ComponentsField() implements LoadableField<DataComponentPatch,FluidStack> {
    @Override
    public String key() {
      return "components";
    }

    @Override
    public DataComponentPatch get(JsonObject json, String key, TypedMap context) {
      if (json.has(key)) {
        return ItemStackLoadable.COMPONENT_PATCH.convert(json.get(key), key, context);
      }
      if (json.has("nbt")) {
        return LegacyNbt.toComponents(NBTLoadable.ALLOW_STRING.convert(json.get("nbt"), "nbt", context));
      }
      return DataComponentPatch.EMPTY;
    }

    @Override
    public void serialize(FluidStack parent, JsonObject json) {
      if (!parent.isComponentsPatchEmpty()) {
        json.add(key(), ItemStackLoadable.COMPONENT_PATCH.serialize(parent.getComponentsPatch()));
      }
    }

    @Override
    public DataComponentPatch decode(FriendlyByteBuf buffer, TypedMap context) {
      return DataComponentPatch.STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer);
    }

    @Override
    public void encode(FriendlyByteBuf buffer, FluidStack parent) {
      DataComponentPatch.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, parent.getComponentsPatch());
    }
  }

  /** Creates a loadable for a stack with a single item */
  public static Loadable<FluidStack> fixedSize(int amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException("Count must be positive, received " + amount);
    }
    return Loadables.FLUID.flatXmap(fluid -> makeStack(fluid, amount, DataComponentPatch.EMPTY), FLUID_GETTER);
  }

  /** Creates a loadable for a stack with a single item */
  public static RecordLoadable<FluidStack> fixedSizeNBT(int amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException("Amount must be positive, received " + amount);
    }
    return RecordLoadable.create(FLUID, NBT, (fluid, tag) -> makeStack(fluid, amount, tag))
                         .compact(OPTIONAL_BUCKET, COMPACT_NBT);
  }

  /** Creates a non-empty variant of the loadable */
  public static Loadable<FluidStack> notEmpty(Loadable<FluidStack> loadable) {
    return loadable.validate(NOT_EMPTY);
  }

  /** Creates a non-empty variant of the loadable */
  public static RecordLoadable<FluidStack> notEmpty(RecordLoadable<FluidStack> loadable) {
    return loadable.validate(NOT_EMPTY);
  }
}
