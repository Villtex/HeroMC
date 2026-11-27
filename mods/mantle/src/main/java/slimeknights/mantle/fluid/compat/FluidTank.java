package slimeknights.mantle.fluid.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.recipe.helper.RecipeHelper;

import java.util.function.Predicate;

/**
 * Port of Forge 1.20.1's {@code slimeknights.mantle.fluid.compat.FluidTank}.
 *
 * <p>Saving: {@link #writeToNBT(CompoundTag)} / {@link #readFromNBT(CompoundTag)} keep the 1.20.1 form (fluid under
 * {@code "id"}, {@code "amount"}, {@code "components"}, through the registry access of the running game), and
 * {@link #save(ValueOutput, String)} / {@link #load(ValueInput, String)} are the 26.3 block entity way.
 */
public class FluidTank implements IFluidHandler {
  protected Predicate<FluidStack> validator;
  protected FluidStack fluid = FluidStack.EMPTY;
  protected int capacity;

  public FluidTank(int capacity) {
    this(capacity, e -> true);
  }

  public FluidTank(int capacity, Predicate<FluidStack> validator) {
    this.capacity = capacity;
    this.validator = validator;
  }

  public FluidTank setCapacity(int capacity) {
    this.capacity = capacity;
    return this;
  }

  public FluidTank setValidator(Predicate<FluidStack> validator) {
    if (validator != null) {
      this.validator = validator;
    }
    return this;
  }

  public boolean isFluidValid(FluidStack stack) {
    return validator.test(stack);
  }

  public int getCapacity() {
    return capacity;
  }

  public FluidStack getFluid() {
    return fluid;
  }

  public int getFluidAmount() {
    return fluid.getAmount();
  }

  public FluidTank readFromNBT(CompoundTag nbt) {
    setFluid(FluidStack.OPTIONAL_CODEC.parse(RecipeHelper.registryAccess().createSerializationContext(NbtOps.INSTANCE), nbt).result().orElse(FluidStack.EMPTY));
    return this;
  }

  public CompoundTag writeToNBT(CompoundTag nbt) {
    if (!fluid.isEmpty()) {
      FluidStack.CODEC.encodeStart(RecipeHelper.registryAccess().createSerializationContext(NbtOps.INSTANCE), fluid).result()
        .ifPresent(tag -> nbt.merge((CompoundTag) tag));
    }
    return nbt;
  }

  /** Saves the fluid under the key (26.3 block entity storage) */
  public void save(ValueOutput output, String key) {
    if (!fluid.isEmpty()) {
      output.store(key, FluidStack.CODEC, fluid);
    }
  }

  /** Loads the fluid from the key (26.3 block entity storage) */
  public FluidTank load(ValueInput input, String key) {
    setFluid(input.read(key, FluidStack.CODEC).orElse(FluidStack.EMPTY));
    return this;
  }

  @Override
  public int getTanks() {
    return 1;
  }

  @Override
  public FluidStack getFluidInTank(int tank) {
    return getFluid();
  }

  @Override
  public int getTankCapacity(int tank) {
    return getCapacity();
  }

  @Override
  public boolean isFluidValid(int tank, FluidStack stack) {
    return isFluidValid(stack);
  }

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    if (resource.isEmpty() || !isFluidValid(resource)) {
      return 0;
    }
    if (action.simulate()) {
      if (fluid.isEmpty()) {
        return Math.min(capacity, resource.getAmount());
      }
      if (!FluidStack.isSameFluidSameComponents(fluid, resource)) {
        return 0;
      }
      return Math.min(capacity - fluid.getAmount(), resource.getAmount());
    }
    if (fluid.isEmpty()) {
      fluid = resource.copyWithAmount(Math.min(capacity, resource.getAmount()));
      onContentsChanged();
      return fluid.getAmount();
    }
    if (!FluidStack.isSameFluidSameComponents(fluid, resource)) {
      return 0;
    }
    int filled = capacity - fluid.getAmount();
    if (resource.getAmount() < filled) {
      fluid.grow(resource.getAmount());
      filled = resource.getAmount();
    } else {
      fluid.setAmount(capacity);
    }
    if (filled > 0) {
      onContentsChanged();
    }
    return filled;
  }

  @Override
  public FluidStack drain(FluidStack resource, FluidAction action) {
    if (resource.isEmpty() || !FluidStack.isSameFluidSameComponents(resource, fluid)) {
      return FluidStack.EMPTY;
    }
    return drain(resource.getAmount(), action);
  }

  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    int drained = maxDrain;
    if (fluid.getAmount() < drained) {
      drained = fluid.getAmount();
    }
    FluidStack stack = fluid.copyWithAmount(drained);
    if (action.execute() && drained > 0) {
      fluid.shrink(drained);
      onContentsChanged();
    }
    return stack;
  }

  protected void onContentsChanged() {}

  public void setFluid(FluidStack stack) {
    this.fluid = stack;
  }

  public boolean isEmpty() {
    return fluid.isEmpty();
  }

  public int getSpace() {
    return Math.max(0, capacity - fluid.getAmount());
  }
}
