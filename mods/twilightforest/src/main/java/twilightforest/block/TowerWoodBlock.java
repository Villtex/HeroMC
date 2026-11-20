package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiConsumer;

/**
 * BlockTFTowerWood metadata 4, infested tower wood: hardness 0.75, drops nothing; breaking it lets a tower termite
 * out (onBlockDestroyedByPlayer). The termite is a mob of Teil 24c, which sets {@link #TERMITE}.
 */
public class TowerWoodBlock extends Block {
    /** Spawns a tower termite at the broken block (EntityTFTowerTermite at x+0.5, y, z+0.5 with spawnExplosionParticle). */
    public static BiConsumer<ServerLevel, BlockPos> TERMITE = (level, pos) -> {
    };

    public TowerWoodBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel server) {
            TERMITE.accept(server, pos);
        }
        super.destroy(level, pos, state);
    }
}
