package tconstruct.smeltery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;
import tconstruct.TSmeltery;

/** LavaTankLogic: a seared block of the smeltery with a tank of 4000 mB (the smeltery burns lava from it). */
public class TankBlockEntity extends ServantBlockEntity implements LiquidContainer {
    public final SimpleTank tank = new SimpleTank(4000);

    public TankBlockEntity(BlockPos pos, BlockState state) {
        super(TSmeltery.TANK_ENTITY.get(), pos, state);
    }

    @Override
    public int fill(@Nullable Direction from, FluidStack resource, boolean doFill) {
        int amount = this.tank.fill(resource, doFill);
        if (amount > 0 && doFill) {
            this.changed();
        }
        return amount;
    }

    @Override
    public FluidStack drain(@Nullable Direction from, int maxDrain, boolean doDrain) {
        FluidStack drained = this.tank.drain(maxDrain, doDrain);
        if (!drained.isEmpty() && doDrain) {
            this.changed();
        }
        return drained;
    }

    @Override
    public FluidStack liquid() {
        return this.tank.liquid;
    }

    @Override
    public int capacity() {
        return this.tank.capacity;
    }

    /** getBrightness: the light of the liquid's block. */
    public int brightness() {
        return this.tank.liquid.isEmpty() ? 0 : this.tank.liquid.getFluidType().getLightLevel(this.tank.liquid);
    }

    private void changed() {
        this.sync();
        if (this.level != null) {
            this.level.getLightEngine().checkBlock(this.worldPosition);
        }
    }

    /** The liquid goes with the item when the tank is broken (LavaTankBlock.removeBlockByPlayer). */
    @Override
    protected void collectImplicitComponents(net.minecraft.core.component.DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (!this.tank.liquid.isEmpty()) {
            components.set(TSmeltery.LIQUID.get(), this.tank.liquid.copy());
        }
    }

    @Override
    protected void applyImplicitComponents(net.minecraft.core.component.DataComponentGetter components) {
        super.applyImplicitComponents(components);
        FluidStack liquid = components.get(TSmeltery.LIQUID.get());
        this.tank.liquid = liquid == null ? FluidStack.EMPTY : liquid.copy();
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        output.discard("Liquid");
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.tank.save(output, "Liquid");
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.tank.load(input, "Liquid");
    }
}
