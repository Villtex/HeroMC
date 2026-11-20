package tconstruct.smeltery;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;

/** LiquidTank of Forge 1.5.2: one kind of liquid up to a capacity. */
public final class SimpleTank {
    public FluidStack liquid = FluidStack.EMPTY;
    public final int capacity;

    public SimpleTank(int capacity) {
        this.capacity = capacity;
    }

    public int fill(FluidStack resource, boolean doFill) {
        if (resource.isEmpty()) {
            return 0;
        }
        if (this.liquid.isEmpty()) {
            int amount = Math.min(this.capacity, resource.getAmount());
            if (doFill) {
                this.liquid = resource.copyWithAmount(amount);
            }
            return amount;
        }
        if (!FluidStack.isSameFluidSameComponents(this.liquid, resource)) {
            return 0;
        }
        int amount = Math.min(this.capacity - this.liquid.getAmount(), resource.getAmount());
        if (doFill) {
            this.liquid.grow(amount);
        }
        return amount;
    }

    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (this.liquid.isEmpty()) {
            return FluidStack.EMPTY;
        }
        int amount = Math.min(maxDrain, this.liquid.getAmount());
        FluidStack drained = this.liquid.copyWithAmount(amount);
        if (doDrain) {
            this.liquid.shrink(amount);
            if (this.liquid.isEmpty()) {
                this.liquid = FluidStack.EMPTY;
            }
        }
        return drained;
    }

    public void save(ValueOutput output, String key) {
        if (!this.liquid.isEmpty()) {
            output.store(key, FluidStack.CODEC, this.liquid);
        }
    }

    public void load(ValueInput input, String key) {
        this.liquid = input.read(key, FluidStack.CODEC).orElse(FluidStack.EMPTY);
    }
}
