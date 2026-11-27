package slimeknights.mantle.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import javax.annotation.Nullable;

/**
 * Port: replaces Forge's {@code Item#initCapabilities} for fluid containers. Mantle registers
 * {@code Capabilities.Fluid.ITEM} for every item implementing this interface.
 */
public interface IFluidContainerItem {
  /** Creates the fluid handler for the stack, null for none */
  @Nullable
  ResourceHandler<FluidResource> createFluidHandler(ItemStack stack, ItemAccess access);

  /** Registers the capability for all items implementing this, mod bus listener */
  static void registerCapabilities(RegisterCapabilitiesEvent event) {
    Item[] items = BuiltInRegistries.ITEM.stream().filter(item -> item instanceof IFluidContainerItem).toArray(Item[]::new);
    if (items.length > 0) {
      event.registerItem(Capabilities.Fluid.ITEM, (stack, access) -> ((IFluidContainerItem) stack.getItem()).createFluidHandler(stack, access), items);
    }
  }
}
