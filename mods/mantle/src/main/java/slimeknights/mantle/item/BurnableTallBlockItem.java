package slimeknights.mantle.item;

import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;


public class BurnableTallBlockItem extends DoubleHighBlockItem {
  /** Port: unused, 26.3 fuel comes from the data map {@code neoforge:furnace_fuels} */
  @SuppressWarnings("unused")
  private final int burnTime;
  public BurnableTallBlockItem(Block blockIn, Properties builder, int burnTime) {
    super(blockIn, builder);
    this.burnTime = burnTime;
  }

}
