package slimeknights.mantle.block;

import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.properties.WoodType;

/** Port: uses the vanilla sign block entity, the block is added to its type (see {@code MantleSignBlockEntity}) */
public class MantleWallHangingSignBlock extends WallHangingSignBlock {
  public MantleWallHangingSignBlock(Properties props, WoodType type) {
    super(type, props);
  }

}
