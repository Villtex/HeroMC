package legendgear.block;

import legendgear.LGBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** TileEntityStarstone: only there for its renderer (the glowing rainbow shell). */
public class StarstoneBlockEntity extends BlockEntity {
    public StarstoneBlockEntity(BlockPos pos, BlockState state) {
        super(LGBlocks.STARSTONE_ENTITY.get(), pos, state);
    }
}
