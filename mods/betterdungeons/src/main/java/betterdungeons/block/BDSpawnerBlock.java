package betterdungeons.block;

import betterdungeons.BetterDungeons;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * The mod's spawner (BlockMobSpawner, id 2002 in the pack): looks like the vanilla spawner, drops nothing and spawns
 * its dungeon mob once a player comes near (see {@link BDSpawnerBlockEntity}).
 */
public class BDSpawnerBlock extends BaseEntityBlock {
    public BDSpawnerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BDSpawnerBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, BetterDungeons.SPAWNER_ENTITY.get(), BDSpawnerBlockEntity::serverTick);
    }
}
