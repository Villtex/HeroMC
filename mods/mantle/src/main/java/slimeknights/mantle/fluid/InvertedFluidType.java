package slimeknights.mantle.fluid;

import net.neoforged.neoforge.fluids.FluidType;


/** Fluid type adding an extra flipped texture for the in world block */
// Port: NeoForge 26.3 registers client fluid extensions in RegisterClientExtensionsEvent; Teil 27c registers
// ClientInvertedFluidType there for every instance of this class.
public class InvertedFluidType extends FluidType {
  public InvertedFluidType(Properties properties) {
    super(properties);
  }

}
