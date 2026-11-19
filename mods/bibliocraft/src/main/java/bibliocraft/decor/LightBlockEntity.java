package bibliocraft.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * TileEntityLantern / TileEntityLamp: only there for the renderer. Their "angle" and "style" are the block state
 * properties {@link bibliocraft.common.BiblioBlock#ANGLE} and {@link LightBlock#STYLE} now.
 */
public class LightBlockEntity extends BlockEntity {
    public LightBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
