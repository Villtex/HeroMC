package tconstruct.smeltery;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

/**
 * ITankContainer of Forge 1.5.2 as the smeltery blocks used it among themselves: fill and drain with a simulate flag.
 * {@code from} is the side the liquid comes from or goes to (UNKNOWN = null).
 */
public interface LiquidContainer {
    int fill(@Nullable Direction from, FluidStack resource, boolean doFill);

    FluidStack drain(@Nullable Direction from, int maxDrain, boolean doDrain);

    /** The liquid in the tank (ILiquidTank.getLiquid), EMPTY if none. */
    FluidStack liquid();

    /** ILiquidTank.getCapacity. */
    int capacity();
}
