package xreliquary.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import xreliquary.XRFx;

import java.util.Set;

/**
 * ItemDestructionCatalyst: for three gunpowder it removes the mundane blocks (dirt, grass, gravel, cobblestone, stone,
 * sand, sandstone, snow layer, soul sand, netherrack, end stone) of the 3 x 3 x 3 cube behind the clicked block.
 */
public class DestructionCatalystItem extends XRItem {
    private static final Set<Block> BREAKABLE = Set.of(Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.GRAVEL, Blocks.COBBLESTONE, Blocks.STONE, Blocks.SAND,
            Blocks.SANDSTONE, Blocks.SNOW, Blocks.SOUL_SAND, Blocks.NETHERRACK, Blocks.END_STONE);

    public DestructionCatalystItem(Properties properties) {
        super(properties, 3);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && count(player) >= 3) {
            Level level = context.getLevel();
            BlockPos center = context.getClickedPos().relative(context.getClickedFace().getOpposite());
            boolean destroyed = false;
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
                if (BREAKABLE.contains(level.getBlockState(pos).getBlock())) {
                    if (!level.isClientSide()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        if (level.getRandom().nextInt(2) == 0) {
                            XRFx.particle(level, ParticleTypes.EXPLOSION, pos.getX(), pos.getY(), pos.getZ(), 1.0, 0.0, 0.0);
                        }
                        if (!destroyed) {
                            XRFx.sound(level, center.getX(), center.getY(), center.getZ(), SoundEvents.GENERIC_EXPLODE.value(), 4.0F,
                                    (1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F) * 0.7F);
                        }
                    }
                    destroyed = true;
                }
            }
            if (destroyed && !level.isClientSide()) {
                int cost = 3;
                for (int slot = 0; slot < player.getInventory().getContainerSize() && cost > 0; slot++) {
                    ItemStack s = player.getInventory().getItem(slot);
                    if (s.is(Items.GUNPOWDER)) {
                        int take = Math.min(cost, s.getCount());
                        player.getInventory().removeItem(slot, take);
                        cost -= take;
                    }
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static int count(Player player) {
        int n = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack s = player.getInventory().getItem(slot);
            if (s.is(Items.GUNPOWDER)) {
                n += s.getCount();
            }
        }
        return n;
    }
}
