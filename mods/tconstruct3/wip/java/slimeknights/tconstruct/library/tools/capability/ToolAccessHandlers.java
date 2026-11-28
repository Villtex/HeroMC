package slimeknights.tconstruct.library.tools.capability;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import slimeknights.mantle.fluid.compat.IFluidHandler.FluidAction;
import slimeknights.tconstruct.library.tools.capability.fluid.ToolFluidCapability;
import slimeknights.tconstruct.library.tools.capability.inventory.ToolInventoryCapability;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Port (Teil 27e): NeoForge 26.3 item capabilities for the tank, inventory and energy of tools.
 * <p>In 1.20.1 the Forge capability changed the tool stack directly. In 26.3 an item capability gets an
 * {@link ItemAccess}: the handlers here read a copy of the tool in the access, run the 1.20.1 logic
 * ({@link ToolFluidCapability}, {@link ToolInventoryCapability}, {@link ToolEnergyCapability}) on that copy and
 * exchange the changed copy into the access as part of the transaction. Tools never stack, so only an access holding
 * exactly one tool can be changed.
 */
public final class ToolAccessHandlers {
  private ToolAccessHandlers() {}

  /** Copy of the tool in the access, empty if it does not hold exactly one item */
  static ItemStack copy(ItemAccess access) {
    ItemResource resource = access.getResource();
    if (resource.isEmpty() || access.getAmount() != 1) {
      return ItemStack.EMPTY;
    }
    return resource.toStack();
  }

  /**
   * Runs the action on a copy of the tool and exchanges the changed copy into the access.
   * @param changed  true if the result means the tool changed
   * @param none     result if the access cannot be changed
   */
  static <R> R modify(ItemAccess access, TransactionContext transaction, Function<ToolStack,R> action, Predicate<R> changed, R none) {
    ItemStack copy = copy(access);
    if (copy.isEmpty()) {
      return none;
    }
    R result = action.apply(ToolStack.from(copy));
    if (changed.test(result)) {
      return access.exchange(ItemResource.of(copy), 1, transaction) == 1 ? result : none;
    }
    return result;
  }

  /** Read only view of the tool in the access */
  static ToolStack view(ItemAccess access) {
    return ToolStack.from(copy(access));
  }

  /** Fluid handler for the tanks of a tool ({@code Capabilities.Fluid.ITEM}) */
  public record Fluid(ItemAccess access) implements ResourceHandler<FluidResource> {
    private ToolFluidCapability read() {
      ItemStack copy = copy(access);
      ToolStack tool = ToolStack.from(copy);
      return new ToolFluidCapability(copy, () -> tool);
    }

    @Override
    public int size() {
      return copy(access).isEmpty() ? 0 : read().getTanks();
    }

    @Override
    public FluidResource getResource(int index) {
      return index < size() ? FluidResource.of(read().getFluidInTank(index)) : FluidResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int index) {
      return index < size() ? read().getFluidInTank(index).getAmount() : 0;
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
      return index < size() ? read().getTankCapacity(index) : 0;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
      return index < size() && (resource.isEmpty() || read().isFluidValid(index, resource.toStack(1)));
    }

    /** Tool tanks fill and drain as a whole (1.20.1 hook), the index only checks the tank accepts the fluid */
    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
      if (resource.isEmpty() || amount <= 0 || !isValid(index, resource)) {
        return 0;
      }
      return insert(resource, amount, transaction);
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext transaction) {
      if (resource.isEmpty() || amount <= 0) {
        return 0;
      }
      FluidStack fluid = resource.toStack(amount);
      return modify(access, transaction, tool -> new ToolFluidCapability(ItemStack.EMPTY, () -> tool).fill(fluid, FluidAction.EXECUTE), filled -> filled > 0, 0);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
      if (resource.isEmpty() || amount <= 0 || !resource.matches(read().getFluidInTank(index))) {
        return 0;
      }
      return extract(resource, amount, transaction);
    }

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext transaction) {
      if (resource.isEmpty() || amount <= 0) {
        return 0;
      }
      FluidStack fluid = resource.toStack(amount);
      return modify(access, transaction, tool -> new ToolFluidCapability(ItemStack.EMPTY, () -> tool).drain(fluid, FluidAction.EXECUTE).getAmount(), drained -> drained > 0, 0);
    }
  }

  /** Item handler for the inventory slots of a tool ({@code Capabilities.Item.ITEM}) */
  public record Inventory(ItemAccess access) implements ResourceHandler<ItemResource> {
    private ToolInventoryCapability read() {
      ToolStack tool = view(access);
      return new ToolInventoryCapability(() -> tool);
    }

    @Override
    public int size() {
      return copy(access).isEmpty() ? 0 : read().getSlots();
    }

    @Override
    public ItemResource getResource(int index) {
      return index < size() ? ItemResource.of(read().getStackInSlot(index)) : ItemResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int index) {
      return index < size() ? read().getStackInSlot(index).getCount() : 0;
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
      if (index >= size()) {
        return 0;
      }
      int limit = read().getSlotLimit(index);
      return resource.isEmpty() ? limit : Math.min(limit, resource.getMaxStackSize());
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
      return index < size() && (resource.isEmpty() || read().isItemValid(index, resource.toStack()));
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
      if (resource.isEmpty() || amount <= 0 || index >= size()) {
        return 0;
      }
      ItemStack stack = resource.toStack(amount);
      return modify(access, transaction, tool -> amount - new ToolInventoryCapability(() -> tool).insertItem(index, stack, false).getCount(), inserted -> inserted > 0, 0);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
      if (resource.isEmpty() || amount <= 0 || index >= size() || !resource.matches(read().getStackInSlot(index))) {
        return 0;
      }
      return modify(access, transaction, tool -> new ToolInventoryCapability(() -> tool).extractItem(index, amount, false).getCount(), extracted -> extracted > 0, 0);
    }
  }

  /** Energy handler of a tool ({@code Capabilities.Energy.ITEM}) */
  public record Energy(ItemAccess access) implements EnergyHandler {
    @Override
    public long getAmountAsLong() {
      return copy(access).isEmpty() ? 0 : ToolEnergyCapability.getEnergy(view(access));
    }

    @Override
    public long getCapacityAsLong() {
      return copy(access).isEmpty() ? 0 : ToolEnergyCapability.getMaxEnergy(view(access));
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
      return modify(access, transaction, tool -> new ToolEnergyCapability(() -> tool).receiveEnergy(amount, false), filled -> filled > 0, 0);
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
      return modify(access, transaction, tool -> new ToolEnergyCapability(() -> tool).extractEnergy(amount, false), drained -> drained > 0, 0);
    }
  }
}
