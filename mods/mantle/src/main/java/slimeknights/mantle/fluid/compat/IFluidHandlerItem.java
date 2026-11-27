package slimeknights.mantle.fluid.compat;

import net.minecraft.world.item.ItemStack;

/** Port of Forge 1.20.1's {@code IFluidHandlerItem}: a fluid handler of an item, {@link #getContainer()} is the item after the changes. */
public interface IFluidHandlerItem extends IFluidHandler {
  /** Container item after filling or draining */
  ItemStack getContainer();
}
