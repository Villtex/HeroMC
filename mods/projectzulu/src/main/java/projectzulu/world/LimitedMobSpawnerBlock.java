package projectzulu.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import projectzulu.ZuluWorld;

/**
 * BlockLimitedMobSpawner (Project Zulu|Dungeon, id 956): looks like the vanilla spawner, drops nothing but 15 to 43
 * experience, and spawns until its limit is reached ({@link LimitedMobSpawnerBlockEntity}). The editor the original
 * opened for players in creative mode is left out; a spawn egg sets the mob like on the vanilla spawner.
 */
public class LimitedMobSpawnerBlock extends BaseEntityBlock {
    public LimitedMobSpawnerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LimitedMobSpawnerBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ZuluWorld.LIMITED_MOB_SPAWNER_ENTITY.get(),
                level.isClientSide() ? LimitedMobSpawnerBlockEntity::clientTick : LimitedMobSpawnerBlockEntity::serverTick);
    }

    /** dropBlockAsItemWithChance: 15 + rand(15) + rand(15) experience. */
    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, tool, dropExperience);
        if (dropExperience) {
            RandomSource random = level.getRandom();
            this.popExperience(level, pos, 15 + random.nextInt(15) + random.nextInt(15));
        }
    }
}
