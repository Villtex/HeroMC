package slimeknights.mantle.fluid.compat;

import net.neoforged.neoforge.fluids.FluidStack;

/** Port of Forge 1.20.1's {@code EmptyFluidHandler} */
public class EmptyFluidHandler implements IFluidHandler {
  public static final EmptyFluidHandler INSTANCE = new EmptyFluidHandler();

  protected EmptyFluidHandler() {}

  @Override
  public int getTanks() {
    return 1;
  }

  @Override
  public FluidStack getFluidInTank(int tank) {
    return FluidStack.EMPTY;
  }

  @Override
  public int getTankCapacity(int tank) {
    return 0;
  }

  @Override
  public boolean isFluidValid(int tank, FluidStack stack) {
    return true;
  }

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    return 0;
  }

  @Override
  public FluidStack drain(FluidStack resource, FluidAction action) {
    return FluidStack.EMPTY;
  }

  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    return FluidStack.EMPTY;
  }
}
