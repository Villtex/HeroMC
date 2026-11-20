package tconstruct.smeltery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;
import tconstruct.TSmeltery;

/** SmelteryDrainLogic: the liquid of the smeltery goes in and out here. */
public class DrainBlockEntity extends ServantBlockEntity implements LiquidContainer {
    public DrainBlockEntity(BlockPos pos, BlockState state) {
        super(TSmeltery.DRAIN_ENTITY.get(), pos, state);
    }

    private @Nullable SmelteryBlockEntity smeltery() {
        return this.masterEntity() instanceof SmelteryBlockEntity smeltery ? smeltery : null;
    }

    /** fill: a master is enough (1.5.2 did not re-check it here), simulated fills accept everything. */
    @Override
    public int fill(@Nullable Direction from, FluidStack resource, boolean doFill) {
        if (this.master == null || resource.isEmpty()) {
            return 0;
        }
        if (!doFill) {
            return resource.getAmount();
        }
        SmelteryBlockEntity smeltery = this.smeltery();
        return smeltery == null ? 0 : smeltery.fill(resource, true);
    }

    @Override
    public FluidStack drain(@Nullable Direction from, int maxDrain, boolean doDrain) {
        SmelteryBlockEntity smeltery = this.smeltery();
        return smeltery == null ? FluidStack.EMPTY : smeltery.drain(maxDrain, doDrain);
    }

    @Override
    public FluidStack liquid() {
        SmelteryBlockEntity smeltery = this.smeltery();
        return smeltery == null ? FluidStack.EMPTY : smeltery.liquid();
    }

    @Override
    public int capacity() {
        SmelteryBlockEntity smeltery = this.smeltery();
        return smeltery == null ? 0 : smeltery.getCapacity();
    }
}
