/**
 * Port: Forge 1.20.1 fluid handler API ({@code IFluidHandler}, {@code IFluidHandlerItem}, {@code FluidTank},
 * {@code EmptyFluidHandler}) on top of NeoForge 26.3's transfer API, see {@link slimeknights.mantle.fluid.compat.FluidHandlers}
 * for the adapters and capability lookups. Mantle and Tinkers' Construct 3 keep their fill/drain logic this way.
 */
@ParametersAreNonnullByDefault
package slimeknights.mantle.fluid.compat;

import javax.annotation.ParametersAreNonnullByDefault;
