package slimeknights.mantle.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * Fluid handler for a container with a constant fluid: it can only be drained completely, leaving the empty container.
 *
 * <p>Port: a NeoForge 26.3 {@link ItemAccessResourceHandler} (like NeoForge's bucket handler) instead of a Forge
 * {@code IFluidHandlerItem} + {@code ICapabilityProvider}; register it for {@code Capabilities.Fluid.ITEM}, for instance
 * through {@link IFluidContainerItem}. The item in the access is swapped for the empty container on drain.
 */
public class ConstantFluidContainerWrapper extends ItemAccessResourceHandler<FluidResource> {
  /** Contained fluid */
  private final FluidStack fluid;
  /** Empty version of the container */
  private final ItemResource empty;

  public ConstantFluidContainerWrapper(FluidStack fluid, ItemAccess access, ItemStack emptyStack) {
    super(access, 1);
    this.fluid = fluid;
    this.empty = ItemResource.of(emptyStack);
  }

  /** Uses the crafting remainder of the item as the empty container */
  public ConstantFluidContainerWrapper(FluidStack fluid, ItemAccess access) {
    this(fluid, access, remainder(access.getResource().toStack()));
  }

  /** Crafting remainder of the stack's item, empty if none */
  private static ItemStack remainder(ItemStack stack) {
    ItemStackTemplate remainder = stack.getItem().getCraftingRemainder();
    return remainder == null ? ItemStack.EMPTY : remainder.create();
  }

  /** True if the resource is the empty container, not the filled one */
  private boolean isEmpty(ItemResource resource) {
    return resource.isEmpty() || !empty.isEmpty() && resource.equals(empty);
  }

  @Override
  protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
    return isEmpty(accessResource) ? FluidResource.EMPTY : FluidResource.of(fluid);
  }

  @Override
  protected int getAmountFrom(ItemResource accessResource, int index) {
    return isEmpty(accessResource) ? 0 : fluid.getAmount();
  }

  @Override
  protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
    // only full drains are possible, filling is not
    return newAmount == 0 ? empty : ItemResource.EMPTY;
  }

  @Override
  public boolean isValid(int index, FluidResource resource) {
    return resource.isEmpty() || resource.matches(fluid);
  }

  @Override
  protected int getCapacity(int index, FluidResource resource) {
    return fluid.getAmount();
  }
}
