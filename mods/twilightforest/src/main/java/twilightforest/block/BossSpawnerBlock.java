package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import twilightforest.TFBosses;

/**
 * BlockTFBossSpawner (1351, metadata 0 naga, 1 lich, 2 hydra, 3 tower): looks like the mob spawner, hardness 20,
 * resistance 10, drops nothing. Its tile entity summons the boss once a player comes near.
 */
public class BossSpawnerBlock extends BaseEntityBlock {
    /** TileEntityTF*Spawner: the boss, how near a player must be, whether he must stand higher than y - 2, the home radius. */
    public enum Kind {
        NAGA("naga", 50.0, false, 46),
        LICH("lich", 9.0, true, 50),
        HYDRA("hydra", 50.0, false, 50),
        TOWER("ur_ghast", 9.0, true, 50);

        public final String mob;
        public final double range;
        public final boolean above;
        public final int home;

        Kind(String mob, double range, boolean above, int home) {
            this.mob = mob;
            this.range = range;
            this.above = above;
            this.home = home;
        }
    }

    private final Kind kind;

    public BossSpawnerBlock(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() {
        return this.kind;
    }


    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BossSpawnerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TFBosses.BOSS_SPAWNER_ENTITY.get(), BossSpawnerBlockEntity::tick);
    }
}
