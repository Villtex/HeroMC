package legendgear.item;

import legendgear.LGBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * MysticSeed (an ItemSeeds of the mystic shrub on grass): planted on the top of a grass block it grows a shrub and the
 * star of twelve around it, energized while it thunders. Always glowing.
 */
public class MysticSeedItem extends Item {
    public MysticSeedItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockPos up = pos.above();
        if (context.getClickedFace() != Direction.UP || !level.getBlockState(pos).is(Blocks.GRASS_BLOCK) || !level.isEmptyBlock(up)
                || context.getPlayer() != null && !context.getPlayer().mayUseItemAt(up, Direction.UP, context.getItemInHand())) {
            return InteractionResult.PASS;
        }
        int thunder = level.isThundering() ? 1 : 0;
        if (!level.isClientSide()) {
            level.setBlock(up, LGBlocks.MYSTIC_SHRUB.get().defaultBlockState(), 3);
            LGBlocks.MYSTIC_SHRUB.get().buildStar(level, up, thunder);
            context.getItemInHand().shrink(1);
            level.playSound(null, up, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else {
            for (int i = 0; i < 12; i++) {
                level.addParticle(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, level.getRandom().nextGaussian(), 1.0,
                        level.getRandom().nextGaussian());
            }
        }
        return InteractionResult.SUCCESS;
    }
}
