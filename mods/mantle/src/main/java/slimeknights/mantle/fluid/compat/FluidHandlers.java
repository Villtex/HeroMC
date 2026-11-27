package slimeknights.mantle.fluid.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Port: bridges between the Forge style {@link IFluidHandler} and NeoForge 26.3's {@link ResourceHandler} of
 * {@link FluidResource}, plus the capability lookups that returned {@code LazyOptional} in 1.20.1.
 */
public final class FluidHandlers {
  private FluidHandlers() {}

  /* Lookups */

  /** Fluid handler of the block at the position (Forge: {@code be.getCapability(FLUID_HANDLER, side)}) */
  public static Optional<IFluidHandler> getBlockHandler(Level level, BlockPos pos, @Nullable Direction side) {
    ResourceHandler<FluidResource> handler = level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
    return Optional.ofNullable(handler).map(FluidHandlers::asFluidHandler);
  }

  /**
   * Fluid handler of an item (Forge: {@code FluidUtil.getFluidHandler(stack)}). Works on a copy of the stack with the
   * count of the stack; {@link IFluidHandlerItem#getContainer()} is the stack after the changes.
   */
  public static Optional<IFluidHandlerItem> getItemHandler(ItemStack stack) {
    if (stack.isEmpty()) {
      return Optional.empty();
    }
    SimpleContainer container = new SimpleContainer(stack.copy());
    ItemAccess access = ItemAccess.forHandlerIndex(VanillaContainerWrapper.of(container), 0);
    ResourceHandler<FluidResource> handler = access.getCapability(Capabilities.Fluid.ITEM);
    if (handler == null) {
      return Optional.empty();
    }
    return Optional.of(new ItemHandler(handler, container));
  }

  /** Fluid contained in the item, empty if none (Forge: {@code FluidUtil.getFluidContained}) */
  public static Optional<FluidStack> getFluidContained(ItemStack stack) {
    return getItemHandler(stack).map(handler -> handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE)).filter(fluid -> !fluid.isEmpty());
  }

  /* Adapters */

  /** The NeoForge handler as a Forge style handler; simulating opens a transaction that is not committed */
  public static IFluidHandler asFluidHandler(ResourceHandler<FluidResource> handler) {
    if (handler instanceof ResourceWrapper wrapper) {
      return wrapper.handler;
    }
    return new HandlerWrapper(handler);
  }

  /** The Forge style handler for other mods (register it as {@code Capabilities.Fluid.BLOCK}) */
  public static ResourceHandler<FluidResource> asResourceHandler(IFluidHandler handler) {
    if (handler instanceof HandlerWrapper wrapper) {
      return wrapper.handler;
    }
    return new ResourceWrapper(handler);
  }

  /** NeoForge handler as Forge handler */
  private static class HandlerWrapper implements IFluidHandler {
    protected final ResourceHandler<FluidResource> handler;

    HandlerWrapper(ResourceHandler<FluidResource> handler) {
      this.handler = handler;
    }

    @Override
    public int getTanks() {
      return handler.size();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
      FluidResource resource = handler.getResource(tank);
      return resource.isEmpty() ? FluidStack.EMPTY : resource.toStack(handler.getAmountAsInt(tank));
    }

    @Override
    public int getTankCapacity(int tank) {
      return handler.getCapacityAsInt(tank, handler.getResource(tank));
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
      return stack.isEmpty() || handler.isValid(tank, FluidResource.of(stack));
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
      if (resource.isEmpty()) {
        return 0;
      }
      try (Transaction tx = Transaction.openRoot()) {
        int filled = handler.insert(FluidResource.of(resource), resource.getAmount(), tx);
        if (action.execute()) {
          tx.commit();
        }
        return filled;
      }
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
      if (resource.isEmpty()) {
        return FluidStack.EMPTY;
      }
      FluidResource fluid = FluidResource.of(resource);
      try (Transaction tx = Transaction.openRoot()) {
        int drained = handler.extract(fluid, resource.getAmount(), tx);
        if (action.execute()) {
          tx.commit();
        }
        return drained > 0 ? fluid.toStack(drained) : FluidStack.EMPTY;
      }
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
      for (int i = 0; i < handler.size(); i++) {
        FluidResource fluid = handler.getResource(i);
        if (!fluid.isEmpty() && handler.getAmountAsInt(i) > 0) {
          return drain(fluid.toStack(maxDrain), action);
        }
      }
      return FluidStack.EMPTY;
    }
  }

  /** Item handler, keeping the container */
  private static class ItemHandler extends HandlerWrapper implements IFluidHandlerItem {
    private final SimpleContainer container;

    ItemHandler(ResourceHandler<FluidResource> handler, SimpleContainer container) {
      super(handler);
      this.container = container;
    }

    @Override
    public ItemStack getContainer() {
      return container.getItem(0);
    }
  }

  /** Pending changes of a {@link ResourceWrapper} */
  private record Pending(List<FluidStack> fills, List<FluidStack> drains) {}

  /**
   * Forge style handler as NeoForge handler. The Forge handler only knows simulate, not transactions: inserts and
   * extracts are kept as pending stacks (simulated against the handler together with what is already pending, as far
   * as a simulate allows) and done when the outermost transaction commits.
   */
  private static class ResourceWrapper extends SnapshotJournal<Pending> implements ResourceHandler<FluidResource> {
    private final IFluidHandler handler;
    private List<FluidStack> fills = new ArrayList<>();
    private List<FluidStack> drains = new ArrayList<>();

    ResourceWrapper(IFluidHandler handler) {
      this.handler = handler;
    }

    private static int pending(List<FluidStack> list, FluidResource resource) {
      int amount = 0;
      for (FluidStack stack : list) {
        if (resource.matches(stack)) {
          amount += stack.getAmount();
        }
      }
      return amount;
    }

    @Override
    public int size() {
      return handler.getTanks();
    }

    @Override
    public FluidResource getResource(int index) {
      FluidStack stack = handler.getFluidInTank(index);
      return stack.isEmpty() ? FluidResource.EMPTY : FluidResource.of(stack);
    }

    @Override
    public long getAmountAsLong(int index) {
      FluidStack stack = handler.getFluidInTank(index);
      if (stack.isEmpty()) {
        return 0;
      }
      FluidResource resource = FluidResource.of(stack);
      return Math.max(0, stack.getAmount() + pending(fills, resource) - pending(drains, resource));
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
      return handler.getTankCapacity(index);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
      return handler.isFluidValid(index, resource.toStack(1));
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
      return insert(resource, amount, transaction);
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext transaction) {
      TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
      if (amount == 0) {
        return 0;
      }
      int already = pending(fills, resource);
      int fits = handler.fill(resource.toStack(already + amount), IFluidHandler.FluidAction.SIMULATE) - already;
      int inserted = Math.max(0, Math.min(amount, fits));
      if (inserted > 0) {
        updateSnapshots(transaction);
        fills.add(resource.toStack(inserted));
      }
      return inserted;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
      return extract(resource, amount, transaction);
    }

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext transaction) {
      TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
      if (amount == 0) {
        return 0;
      }
      int already = pending(drains, resource);
      FluidStack drained = handler.drain(resource.toStack(already + amount), IFluidHandler.FluidAction.SIMULATE);
      int available = drained.isEmpty() || !resource.matches(drained) ? 0 : drained.getAmount() - already;
      int extracted = Math.max(0, Math.min(amount, available));
      if (extracted > 0) {
        updateSnapshots(transaction);
        drains.add(resource.toStack(extracted));
      }
      return extracted;
    }

    @Override
    protected Pending createSnapshot() {
      return new Pending(new ArrayList<>(fills), new ArrayList<>(drains));
    }

    @Override
    protected void revertToSnapshot(Pending snapshot) {
      fills = snapshot.fills();
      drains = snapshot.drains();
    }

    @Override
    protected void onRootCommit(Pending originalState) {
      List<FluidStack> doDrain = drains;
      List<FluidStack> doFill = fills;
      drains = new ArrayList<>();
      fills = new ArrayList<>();
      for (FluidStack stack : doDrain) {
        handler.drain(stack, IFluidHandler.FluidAction.EXECUTE);
      }
      for (FluidStack stack : doFill) {
        handler.fill(stack, IFluidHandler.FluidAction.EXECUTE);
      }
    }
  }
}
