package slimeknights.mantle.item;

import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;


public class BurnableHangingSignItem extends HangingSignItem {
  /** Port: unused, 26.3 fuel comes from the data map {@code neoforge:furnace_fuels} */
  @SuppressWarnings("unused")
  private final int burnTime;
  public BurnableHangingSignItem(Properties propertiesIn, Block hangingBlock, Block wallBlock, int burnTime) {
    super(hangingBlock, wallBlock, propertiesIn);
    this.burnTime = burnTime;
  }

}
