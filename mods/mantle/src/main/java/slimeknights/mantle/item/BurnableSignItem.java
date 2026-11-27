package slimeknights.mantle.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;


public class BurnableSignItem extends StandingAndWallBlockItem {
  /** Port: unused, 26.3 fuel comes from the data map {@code neoforge:furnace_fuels} */
  @SuppressWarnings("unused")
  private final int burnTime;
  public BurnableSignItem(Properties propertiesIn, Block floorBlockIn, Block wallBlockIn, int burnTime) {
    super(floorBlockIn, wallBlockIn, Direction.DOWN, propertiesIn);
    this.burnTime = burnTime;
  }

}
