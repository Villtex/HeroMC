package slimeknights.mantle.block;

import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.state.properties.WoodType;

/** Port: uses the vanilla sign block entity, the block is added to its type (see {@code MantleSignBlockEntity}) */
public class MantleCeilingHangingSignBlock extends CeilingHangingSignBlock {
  public MantleCeilingHangingSignBlock(Properties props, WoodType type) {
    super(type, props);
  }

}
