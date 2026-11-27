package slimeknights.mantle.inventory.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Port: bridges between the Forge style {@link IItemHandler} and NeoForge 26.3's {@link ResourceHandler} of
 * {@link ItemResource}, plus the capability lookup that returned {@code LazyOptional} in 1.20.1.
 */
public final class ItemHandlers {
  private ItemHandlers() {}

  /** Item handler of the block at the position (Forge: {@code be.getCapability(ITEM_HANDLER, side)}) */
  public static Optional<IItemHandler> getBlockHandler(Level level, BlockPos pos, @Nullable Direction side) {
    ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, side);
    return Optional.ofNullable(handler).map(ItemHandlers::asItemHandler);
  }

  /** The NeoForge handler as a Forge style handler; simulating opens a transaction that is not committed */
  public static IItemHandler asItemHandler(ResourceHandler<ItemResource> handler) {
    if (handler instanceof ResourceWrapper wrapper) {
      return wrapper.handler;
    }
    return new HandlerWrapper(handler);
  }

  /** The Forge style handler for other mods (register it as {@code Capabilities.Item.BLOCK}) */
  public static ResourceHandler<ItemResource> asResourceHandler(IItemHandler handler) {
    if (handler instanceof HandlerWrapper wrapper) {
      return wrapper.handler;
    }
    return new ResourceWrapper(handler);
  }

  /** NeoForge handler as Forge handler */
  private record HandlerWrapper(ResourceHandler<ItemResource> handler) implements IItemHandler {
    @Override
    public int getSlots() {
      return handler.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
      ItemResource resource = handler.getResource(slot);
      return resource.isEmpty() ? ItemStack.EMPTY : resource.toStack(handler.getAmountAsInt(slot));
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
      if (stack.isEmpty()) {
        return ItemStack.EMPTY;
      }
      try (Transaction tx = Transaction.openRoot()) {
        int inserted = handler.insert(slot, ItemResource.of(stack), stack.getCount(), tx);
        if (!simulate) {
          tx.commit();
        }
        return inserted >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - inserted);
      }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
      ItemResource resource = handler.getResource(slot);
      if (amount <= 0 || resource.isEmpty()) {
        return ItemStack.EMPTY;
      }
      try (Transaction tx = Transaction.openRoot()) {
        int extracted = handler.extract(slot, resource, amount, tx);
        if (!simulate) {
          tx.commit();
        }
        return extracted > 0 ? resource.toStack(extracted) : ItemStack.EMPTY;
      }
    }

    @Override
    public int getSlotLimit(int slot) {
      return handler.getCapacityAsInt(slot, handler.getResource(slot));
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
      return stack.isEmpty() || handler.isValid(slot, ItemResource.of(stack));
    }
  }

  /** Pending change of a {@link ResourceWrapper}: positive inserts, negative extracts */
  private record Change(int slot, ItemResource resource, int amount) {}

  /**
   * Forge style handler as NeoForge handler. The Forge handler only knows simulate, not transactions: inserts and
   * extracts are kept as pending changes per slot (simulated together with what is already pending in that slot) and
   * done when the outermost transaction commits.
   */
  private static class ResourceWrapper extends SnapshotJournal<List<Change>> implements ResourceHandler<ItemResource> {
    private final IItemHandler handler;
    private List<Change> changes = new ArrayList<>();

    ResourceWrapper(IItemHandler handler) {
      this.handler = handler;
    }

    /** Pending inserts (positive) or extracts (negative) for the slot */
    private int pending(int slot, boolean insert) {
      int amount = 0;
      for (Change change : changes) {
        if (change.slot == slot && (change.amount > 0) == insert) {
          amount += Math.abs(change.amount);
        }
      }
      return amount;
    }

    @Override
    public int size() {
      return handler.getSlots();
    }

    @Override
    public ItemResource getResource(int index) {
      ItemStack stack = handler.getStackInSlot(index);
      if (stack.isEmpty()) {
        for (Change change : changes) {
          if (change.slot == index && change.amount > 0) {
            return change.resource;
          }
        }
        return ItemResource.EMPTY;
      }
      return ItemResource.of(stack);
    }

    @Override
    public long getAmountAsLong(int index) {
      return Math.max(0, handler.getStackInSlot(index).getCount() + pending(index, true) - pending(index, false));
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
      int limit = handler.getSlotLimit(index);
      return resource.isEmpty() ? limit : Math.min(limit, resource.getMaxStackSize());
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
      return handler.isItemValid(index, resource.toStack());
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
      TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
      if (amount == 0 || index < 0 || index >= handler.getSlots()) {
        return 0;
      }
      // a slot only takes one kind of item
      for (Change change : changes) {
        if (change.slot == index && change.amount > 0 && !change.resource.equals(resource)) {
          return 0;
        }
      }
      int already = pending(index, true);
      ItemStack remainder = handler.insertItem(index, resource.toStack(already + amount), true);
      int inserted = Math.max(0, Math.min(amount, amount - remainder.getCount()));
      if (inserted > 0) {
        updateSnapshots(transaction);
        changes.add(new Change(index, resource, inserted));
      }
      return inserted;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
      TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
      if (amount == 0 || index < 0 || index >= handler.getSlots() || !resource.matches(handler.getStackInSlot(index))) {
        return 0;
      }
      int already = pending(index, false);
      ItemStack extracted = handler.extractItem(index, already + amount, true);
      int available = extracted.isEmpty() || !resource.matches(extracted) ? 0 : extracted.getCount() - already;
      int result = Math.max(0, Math.min(amount, available));
      if (result > 0) {
        updateSnapshots(transaction);
        changes.add(new Change(index, resource, -result));
      }
      return result;
    }

    @Override
    protected List<Change> createSnapshot() {
      return new ArrayList<>(changes);
    }

    @Override
    protected void revertToSnapshot(List<Change> snapshot) {
      changes = snapshot;
    }

    @Override
    protected void onRootCommit(List<Change> originalState) {
      List<Change> done = changes;
      changes = new ArrayList<>();
      // extracts first, then inserts
      for (Change change : done) {
        if (change.amount < 0) {
          handler.extractItem(change.slot, -change.amount, false);
        }
      }
      for (Change change : done) {
        if (change.amount > 0) {
          handler.insertItem(change.slot, change.resource.toStack(change.amount), false);
        }
      }
    }
  }
}
