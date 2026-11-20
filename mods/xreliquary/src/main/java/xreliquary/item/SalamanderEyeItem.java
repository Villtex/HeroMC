package xreliquary.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;
import xreliquary.XRFx;

/**
 * ItemSalamanderEye (and the wraith's eye): held in the main hand it puts out its holder and every fire within 3
 * blocks, snuffs blaze fireballs within 3 and hits ghast fireballs within 5 (removed within 4).
 */
public class SalamanderEyeItem extends XRItem {
    public SalamanderEyeItem(Properties properties, int lines) {
        super(properties, lines);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (owner instanceof Player player && player.getMainHandItem().getItem() instanceof SalamanderEyeItem) {
            fireballs(level, player);
            extinguish(level, player);
        }
    }

    private static float fizzPitch(ServerLevel level) {
        return 2.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8F;
    }

    private static void extinguish(ServerLevel level, Player player) {
        if (player.isOnFire()) {
            player.clearFire();
        }
        BlockPos base = BlockPos.containing(player.getX(), player.getY(), player.getZ());
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-3, -3, -3), base.offset(3, 3, 3))) {
            if (level.getBlockState(pos).is(Blocks.FIRE)) {
                level.removeBlock(pos, false);
                XRFx.sound(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.FIRE_EXTINGUISH, 0.5F, fizzPitch(level));
            }
        }
    }

    private static void fireballs(ServerLevel level, Player player) {
        for (LargeFireball fireball : level.getEntitiesOfClass(LargeFireball.class,
                new AABB(player.getX() - 5, player.getY() - 5, player.getZ() - 5, player.getX() + 5, player.getY() + 5, player.getZ() + 5))) {
            if (player.distanceTo(fireball) < 4.0F) {
                fireball.discard();
            }
            fireball.hurtServer(level, level.damageSources().playerAttack(player), 1.0F);
            XRFx.sound(level, fireball.getX(), fireball.getY(), fireball.getZ(), SoundEvents.FIRE_EXTINGUISH, 0.5F, fizzPitch(level));
        }
        AABB small = new AABB(player.getX() - 3, player.getY() - 3, player.getZ() - 3, player.getX() + 3, player.getY() + 3, player.getZ() + 3);
        for (SmallFireball fireball : level.getEntitiesOfClass(SmallFireball.class, small)) {
            for (int i = 0; i < 4; i++) {
                XRFx.particle(level, new DustParticleOptions(0xFF0000, 1.0F), fireball.getX(), fireball.getY(), fireball.getZ(), 0, 0, 0);
            }
            XRFx.sound(level, fireball.getX(), fireball.getY(), fireball.getZ(), SoundEvents.FIRE_EXTINGUISH, 0.5F, fizzPitch(level));
            fireball.discard();
        }
    }
}
