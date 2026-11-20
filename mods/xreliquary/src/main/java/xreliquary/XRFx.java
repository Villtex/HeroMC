package xreliquary;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** The one-call effects of 1.5.2 (spawnParticle, playSoundAtEntity, playSoundEffect) and inventory helpers. */
public final class XRFx {
    private XRFx() {
    }

    /** World.spawnParticle: on the client the particle, on the server sent to the players nearby with the same motion. */
    public static void particle(Level level, ParticleOptions particle, double x, double y, double z, double vx, double vy, double vz) {
        if (level instanceof ServerLevel server) {
            server.sendParticles(particle, x, y, z, 0, vx, vy, vz, 1.0);
        } else {
            level.addParticle(particle, x, y, z, vx, vy, vz);
        }
    }

    /** World.playSoundAtEntity on the server. */
    public static void sound(Entity entity, SoundEvent sound, float volume, float pitch) {
        if (!entity.level().isClientSide()) {
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, SoundSource.NEUTRAL, volume, pitch);
        }
    }

    /** World.playSoundEffect at a position. */
    public static void sound(Level level, double x, double y, double z, SoundEvent sound, float volume, float pitch) {
        if (!level.isClientSide()) {
            level.playSound(null, x, y, z, sound, SoundSource.NEUTRAL, volume, pitch);
        }
    }

    /** "random.orb" with the pitch the Reliquary used everywhere. */
    public static void orb(Player player) {
        sound(player, net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 0.1F,
                0.5F * ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.8F));
    }

    public static boolean creative(LivingEntity entity) {
        return entity instanceof Player player && player.getAbilities().instabuild;
    }

    /** addItemStackToInventory, or dropPlayerItem when it does not fit. */
    public static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack) && !stack.isEmpty()) {
            player.drop(stack, false, net.minecraft.util.Prediction.SERVER_ONLY);
        }
    }

    /** new EntityItem at the player + spawnEntityInWorld (no throw). */
    public static void spawnAt(Player player, ItemStack stack) {
        if (!player.level().isClientSide()) {
            player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack));
        }
    }

    /** First stack of the main inventory (36 slots) holding this item, or -1. */
    public static int findMain(Player player, net.minecraft.world.item.Item item) {
        var inv = player.getInventory();
        for (int slot = 0; slot < 36; slot++) {
            if (inv.getItem(slot).is(item)) {
                return slot;
            }
        }
        return -1;
    }

    /** Takes one of the item out of the main inventory. */
    public static boolean consumeMain(Player player, net.minecraft.world.item.Item item) {
        int slot = findMain(player, item);
        if (slot < 0) {
            return false;
        }
        player.getInventory().removeItem(slot, 1);
        return true;
    }
}
