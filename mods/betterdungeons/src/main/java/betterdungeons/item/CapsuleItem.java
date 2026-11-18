package betterdungeons.item;

import betterdungeons.BDFx;
import betterdungeons.block.CapsuleStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * ItemCapsule: used on a capsule station it packs the station's area (width x height x width, the station in the
 * middle of the bottom layer) into itself; used anywhere when full it unpacks the area there (the clicked block is the
 * middle of the bottom layer) and is empty again.
 */
public class CapsuleItem extends Item {
    public CapsuleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        if (!CapsuleData.isFull(stack)) {
            if (!(level.getBlockEntity(pos) instanceof CapsuleStationBlockEntity station)) {
                return InteractionResult.PASS;
            }
            if (level instanceof ServerLevel server) {
                int sx = station.width;
                int sy = station.height;
                BlockPos corner = pos.offset(-sx / 2, 0, -sx / 2);
                explosion(server, corner);
                boolean creative = context.getPlayer() != null && context.getPlayer().hasInfiniteMaterials();
                CapsuleData.copy(server, stack, corner, sx, sy, sx, station.name, creative);
                level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 4.0F, BDFx.pitch(level));
            }
            return InteractionResult.SUCCESS;
        }
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 4.0F, BDFx.pitch(level));
        if (level instanceof ServerLevel server) {
            int[] size = CapsuleData.size(stack);
            BlockPos corner = pos.offset(-size[0] / 2, 0, -size[2] / 2);
            explosion(server, corner);
            CapsuleData.paste(server, stack, corner);
            CapsuleData.clear(stack);
        }
        return InteractionResult.SUCCESS;
    }

    /** ItemCapsule.spawnExplosion: smoke and explosions over a 16 x 16 area from the corner. */
    static void explosion(ServerLevel level, BlockPos corner) {
        for (int v = 0; v < 15; v++) {
            level.sendParticles(ParticleTypes.POOF, corner.getX() + BDItemUtil.RAND.nextInt(16), corner.getY(), corner.getZ() + BDItemUtil.RAND.nextInt(16),
                    0, BDItemUtil.RAND.nextDouble() - 0.5, BDItemUtil.RAND.nextDouble(), BDItemUtil.RAND.nextDouble() - 0.5, 1.0);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, corner.getX() + BDItemUtil.RAND.nextInt(16), corner.getY() + 1.0,
                    corner.getZ() + BDItemUtil.RAND.nextInt(16), 0, BDItemUtil.RAND.nextDouble() - 0.5, BDItemUtil.RAND.nextDouble() - 0.5,
                    BDItemUtil.RAND.nextDouble() - 0.5, 1.0);
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, corner.getX() + BDItemUtil.RAND.nextInt(16), corner.getY() + 1.0,
                    corner.getZ() + BDItemUtil.RAND.nextInt(16), 1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
