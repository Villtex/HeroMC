package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.TFBosses;

/** TileEntityTFTrophy: counts its ticks for the ur-ghast's tentacles. */
public class TrophyBlockEntity extends BlockEntity {
    public int ticksExisted;

    public TrophyBlockEntity(BlockPos pos, BlockState state) {
        super(TFBosses.TROPHY_ENTITY.get(), pos, state);
    }
}
