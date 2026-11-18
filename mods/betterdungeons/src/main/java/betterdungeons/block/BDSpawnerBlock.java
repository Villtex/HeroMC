package betterdungeons.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The mod's spawner (BlockMobSpawner, id 2002 in the pack): looks like the vanilla spawner and holds which dungeon mob
 * it brings. Spawning those mobs when a player comes near follows with the mobs in part 13.
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
}
