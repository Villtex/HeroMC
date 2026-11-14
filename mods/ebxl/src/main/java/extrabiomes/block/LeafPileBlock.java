package extrabiomes.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** BlockLeafPile: a thin layer of leaves on grass or dirt, replaced like tall grass. */
public class LeafPileBlock extends EbxlPlantBlock {
    public LeafPileBlock(Properties properties) {
        super(state -> state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT), Block.box(0, 0, 0, 16, 0.25, 16), properties);
    }
}
