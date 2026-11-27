package slimeknights.mantle.fluid.compat;

import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Port of Forge 1.20.1's {@code slimeknights.mantle.fluid.compat.IFluidHandler}: tanks filled and drained with a
 * simulate flag. Exposed to other mods through {@link FluidHandlers#asResourceHandler(IFluidHandler)}.
 */
public interface IFluidHandler {
  enum FluidAction {
    EXECUTE, SIMULATE;

    public boolean execute() {
      return this == EXECUTE;
    }

    public boolean simulate() {
      return this == SIMULATE;
    }
  }

  /** Number of tanks */
  int getTanks();

  /** Fluid in the tank, do not modify the result */
  FluidStack getFluidInTank(int tank);

  /** Capacity of the tank */
  int getTankCapacity(int tank);

  /** Whether the fluid may go into the tank */
  boolean isFluidValid(int tank, FluidStack stack);

  /** Fills the fluid in, returns the amount filled */
  int fill(FluidStack resource, FluidAction action);

  /** Drains the given fluid, returns what was drained */
  FluidStack drain(FluidStack resource, FluidAction action);

  /** Drains up to the amount of any fluid, returns what was drained */
  FluidStack drain(int maxDrain, FluidAction action);
}
