package tconstruct.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import tconstruct.TBlocks;

/** {@code PatternChestLogic}: 30 slots for patterns and casts. */
public class PatternChestBlockEntity extends TableBlockEntity {
    public PatternChestBlockEntity(BlockPos pos, BlockState state) {
        super(TBlocks.PATTERN_CHEST_ENTITY.get(), pos, state, 30);
    }
}
