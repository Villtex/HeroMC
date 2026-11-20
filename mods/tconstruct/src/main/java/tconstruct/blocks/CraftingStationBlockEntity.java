package tconstruct.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import tconstruct.TBlocks;

/** {@code CraftingStationLogic}: a crafting table that keeps the nine items of its grid. */
public class CraftingStationBlockEntity extends TableBlockEntity {
    public CraftingStationBlockEntity(BlockPos pos, BlockState state) {
        super(TBlocks.CRAFTING_STATION_ENTITY.get(), pos, state, 9);
    }
}
