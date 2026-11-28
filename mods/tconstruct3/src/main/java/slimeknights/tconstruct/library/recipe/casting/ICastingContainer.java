package slimeknights.tconstruct.library.recipe.casting;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.recipe.container.ISingleStackContainer;

import javax.annotation.Nullable;

/**
 * Inventory containing a single item and a fluid
 */
public interface ICastingContainer extends ISingleStackContainer {
  /**
   * Gets the contained fluid in this inventory
   * @return  Contained fluid
   */
  Fluid getFluid();

  /**
   * Gets the NBT for the contained fluid
   * @return  Fluid's NBT
   */
  /** Port: components of the fluid (1.20.1 {@code getFluidTag}), empty for a plain fluid */
  default DataComponentPatch getFluidComponents() {
    return DataComponentPatch.EMPTY;
  }
}
