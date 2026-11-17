package projectzulu.block.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import projectzulu.entity.plant.CreeperBlossomPrimed;

/**
 * BlockCreeperBlossom: a flower that a player walking into it (not sneaking) primes like TNT (fuse 40, explosion 1.5),
 * and that an explosion primes with a short fuse.
 */
public class CreeperBlossomBlock extends ZuluFlowerBlock {
    public CreeperBlossomBlock(Properties properties) {
        super(properties);
    }

    /** grass, dirt, farmland, logs (Block.wood). */
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.FARMLAND) || state.is(BlockTags.LOGS);
    }

    @Override
    public void wasExploded(ServerLevel level, BlockPos pos, Explosion explosion) {
        CreeperBlossomPrimed primed = new CreeperBlossomPrimed(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        primed.setFuse(PrimedTnt.getRandomShortFuse(primed.getFuse(), level.getRandom()));
        level.addFreshEntity(primed);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier,
            boolean isPrecise) {
        if (entity instanceof Player player && !level.isClientSide() && !player.isShiftKeyDown()) {
            CreeperBlossomPrimed primed = new CreeperBlossomPrimed(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            level.addFreshEntity(primed);
            level.playSound(null, primed.getX(), primed.getY(), primed.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }
}
