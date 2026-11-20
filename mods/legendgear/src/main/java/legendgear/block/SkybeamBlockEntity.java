package legendgear.block;

import legendgear.LGBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** TileEntitySkybeam: the beam grows to power 10 while the block has redstone power and shrinks without (both sides). */
public class SkybeamBlockEntity extends BlockEntity {
    public static final int MAX_POWER = 10;
    public int powerlevel;

    public SkybeamBlockEntity(BlockPos pos, BlockState state) {
        super(LGBlocks.SKYBEAM_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SkybeamBlockEntity be) {
        if (level.hasNeighborSignal(pos)) {
            be.powerlevel = Math.min(be.powerlevel + 1, MAX_POWER);
        } else {
            be.powerlevel = Math.max(be.powerlevel - 1, 0);
        }
    }
}
