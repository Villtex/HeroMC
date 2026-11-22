package tconstruct.smeltery;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/**
 * Teil 26e: a {@link LiquidContainer} (Forge 1.5.2 ITankContainer of the smeltery blocks) as the fluid handler of
 * NeoForge 26.3, so pipes and machines of other mods fill and drain tanks, the smeltery drain and the casting blocks.
 * One slot: the liquid that {@link LiquidContainer#drain} gives (the lowest one of the smeltery).
 *
 * <p>The containers only know fill and drain with a simulate flag, not transactions: inserts and extracts are kept as
 * pending amounts (simulated against the container with what is already pending) and done when the outermost
 * transaction commits; an aborted transaction forgets them.
 */
public final class LiquidHandler extends SnapshotJournal<LiquidHandler.Pending> implements ResourceHandler<FluidResource> {
    record Pending(FluidResource in, int inAmount, int outAmount) {
    }

    private final LiquidContainer container;
    private final @Nullable Direction side;
    private FluidResource in = FluidResource.EMPTY;
    private int inAmount;
    private int outAmount;

    public LiquidHandler(LiquidContainer container, @Nullable Direction side) {
        this.container = container;
        this.side = side;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public FluidResource getResource(int index) {
        FluidStack liquid = this.container.liquid();
        return liquid.isEmpty() ? this.in : FluidResource.of(liquid);
    }

    @Override
    public long getAmountAsLong(int index) {
        FluidStack liquid = this.container.liquid();
        int amount = liquid.isEmpty() ? 0 : liquid.getAmount();
        if (this.inAmount > 0 && (liquid.isEmpty() || this.in.matches(liquid))) {
            amount += this.inAmount;
        }
        return Math.max(0, amount - this.outAmount);
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return this.container.capacity();
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return !resource.isEmpty();
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0 || this.inAmount > 0 && !this.in.equals(resource)) {
            return 0;
        }
        int fits = this.container.fill(this.side, resource.toStack(this.inAmount + amount), false) - this.inAmount;
        int inserted = Math.max(0, Math.min(amount, fits));
        if (inserted > 0) {
            this.updateSnapshots(transaction);
            this.in = resource;
            this.inAmount += inserted;
        }
        return inserted;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        FluidStack liquid = this.container.liquid();
        if (amount == 0 || liquid.isEmpty() || !resource.matches(liquid)) {
            return 0;
        }
        FluidStack drained = this.container.drain(this.side, this.outAmount + amount, false);
        int available = drained.isEmpty() || !resource.matches(drained) ? 0 : drained.getAmount() - this.outAmount;
        int extracted = Math.max(0, Math.min(amount, available));
        if (extracted > 0) {
            this.updateSnapshots(transaction);
            this.outAmount += extracted;
        }
        return extracted;
    }

    @Override
    protected Pending createSnapshot() {
        return new Pending(this.in, this.inAmount, this.outAmount);
    }

    @Override
    protected void revertToSnapshot(Pending snapshot) {
        this.in = snapshot.in();
        this.inAmount = snapshot.inAmount();
        this.outAmount = snapshot.outAmount();
    }

    @Override
    protected void onRootCommit(Pending originalState) {
        int out = this.outAmount;
        FluidResource resource = this.in;
        int inAmount = this.inAmount;
        this.in = FluidResource.EMPTY;
        this.inAmount = 0;
        this.outAmount = 0;
        if (out > 0) {
            this.container.drain(this.side, out, true);
        }
        if (inAmount > 0) {
            this.container.fill(this.side, resource.toStack(inAmount), true);
        }
    }
}
