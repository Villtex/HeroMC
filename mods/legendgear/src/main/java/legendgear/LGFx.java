package legendgear;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/** Small helpers for the one-call effects of 1.5.2 (spawnParticle, playSoundAtEntity, playSoundEffect). */
public final class LGFx {
    private LGFx() {
    }

    /** World.spawnParticle: on the client the particle, on the server sent to the players nearby with the same motion. */
    public static void particle(Level level, ParticleOptions particle, double x, double y, double z, double vx, double vy, double vz) {
        if (level instanceof ServerLevel server) {
            server.sendParticles(particle, x, y, z, 0, vx, vy, vz, 1.0);
        } else {
            level.addParticle(particle, x, y, z, vx, vy, vz);
        }
    }

    /** World.playSoundAtEntity on the server: heard by everyone nearby (1.5.2 played it for all, the entity included). */
    public static void sound(Entity entity, SoundEvent sound, float volume, float pitch) {
        if (!entity.level().isClientSide()) {
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, SoundSource.NEUTRAL, volume, pitch);
        }
    }

    public static void sound(Entity entity, Supplier<SoundEvent> sound, float volume, float pitch) {
        sound(entity, sound.get(), volume, pitch);
    }

    public static void sound(Entity entity, Holder<SoundEvent> sound, float volume, float pitch) {
        sound(entity, sound.value(), volume, pitch);
    }

    /** World.playSoundEffect at a position. */
    public static void sound(Level level, double x, double y, double z, SoundEvent sound, float volume, float pitch) {
        if (!level.isClientSide()) {
            level.playSound(null, x, y, z, sound, SoundSource.NEUTRAL, volume, pitch);
        }
    }

    /** ItemStack.damageItem(n, entity): breaks the stack at its maximum. */
    public static void damage(ItemStack stack, int amount, LivingEntity entity) {
        if (amount > 0 && stack.isDamageableItem() && entity.level() instanceof ServerLevel server) {
            stack.hurtAndBreak(amount, server, entity instanceof net.minecraft.server.level.ServerPlayer sp ? sp : null, item -> {
            });
        }
    }

    public static boolean creative(LivingEntity entity) {
        return entity instanceof Player player && player.getAbilities().instabuild;
    }
}
