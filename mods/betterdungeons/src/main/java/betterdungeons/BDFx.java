package betterdungeons;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Small helpers for what 1.5.2 did with one call on either side: particles (World.spawnParticle), sounds
 * (playSoundEffect), damage (attackEntityFrom) and the damage sources of HelperDamageSource.
 */
public final class BDFx {
    private BDFx() {
    }

    /**
     * World.spawnParticle: on the client the particle itself, on the server sent to the players nearby (count 0, so the
     * three speeds are the particle's motion as in 1.5.2).
     */
    public static void particle(Level level, ParticleOptions particle, double x, double y, double z, double vx, double vy, double vz) {
        if (level instanceof ServerLevel server) {
            server.sendParticles(particle, x, y, z, 0, vx, vy, vz, 1.0);
        } else {
            level.addParticle(particle, x, y, z, vx, vy, vz);
        }
    }

    /** World.playSoundEffect at block coordinates (the original cast the position to int). */
    public static void sound(Level level, double x, double y, double z, SoundEvent sound, float volume, float pitch) {
        if (!level.isClientSide()) {
            level.playSound(null, (int) x, (int) y, (int) z, sound, SoundSource.NEUTRAL, volume, pitch);
        }
    }

    /** The pitch of most sounds of the mod: (1 + (r1 - r2) * 0.2) * 0.7. */
    public static float pitch(Level level) {
        return (1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F) * 0.7F;
    }

    /** Entity.attackEntityFrom; only the server deals damage. */
    public static boolean hurt(Entity target, DamageSource source, float amount) {
        return target.level() instanceof ServerLevel server && target.hurtServer(server, source, amount);
    }

    /** HelperDamageSource.getBulletDamage / causeArrowDamage: an indirect arrow ("arrow", setProjectile). */
    public static DamageSource bullet(Entity projectile, @Nullable Entity shooter) {
        return projectile.damageSources().source(net.minecraft.world.damagesource.DamageTypes.ARROW, projectile, shooter);
    }

    /** HelperDamageSource.causeIndirectMagicDamage. */
    public static DamageSource magic(Entity projectile, @Nullable Entity shooter) {
        return projectile.damageSources().indirectMagic(projectile, shooter);
    }

    /**
     * EntityDamageSourceIndirect("generic" or "fire", projectile, shooter) of the grenades and fire balls: thrown
     * damage with the shooter as cause (no fire damage type, as in 1.5.2).
     */
    public static DamageSource indirect(Entity projectile, @Nullable Entity shooter) {
        return projectile.damageSources().thrown(projectile, shooter);
    }

    /** EntityDamageSource("fire" / "mob" / "player", entity): a melee hit of that entity. */
    public static DamageSource direct(LivingEntity attacker) {
        return attacker instanceof Player player ? attacker.damageSources().playerAttack(player) : attacker.damageSources().mobAttack(attacker);
    }

    /** EntityLiving.isJumping; the server knows a player's jump key from its last input packet. */
    public static boolean isJumping(LivingEntity entity) {
        if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
            return player.getLastClientInput().jump();
        }
        return entity.isJumping();
    }

    /**
     * World.playAuxSFX(2002, x, y, z, potionDamage): the splash of a thrown potion. The colour is the one of the potion
     * whose id is in the low bits of that damage value (0 water, 1 regeneration, 2 speed, 3 fire resistance, 4 poison,
     * 6 night vision, 12 harming).
     */
    public static void splash(Level level, double x, double y, double z, int potionDamage) {
        int color = switch (potionDamage & 15) {
            case 1 -> net.minecraft.world.effect.MobEffects.REGENERATION.value().getColor();
            case 2 -> net.minecraft.world.effect.MobEffects.SPEED.value().getColor();
            case 3 -> net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE.value().getColor();
            case 4 -> net.minecraft.world.effect.MobEffects.POISON.value().getColor();
            case 6 -> net.minecraft.world.effect.MobEffects.NIGHT_VISION.value().getColor();
            case 12 -> net.minecraft.world.effect.MobEffects.INSTANT_DAMAGE.value().getColor();
            default -> 0x385DC6;
        };
        if (!level.isClientSide()) {
            level.levelEvent(null, 2002, BlockPos.containing(Math.round(x), Math.round(y), Math.round(z)), color);
        }
    }

    /** setFire(seconds). */
    public static void ignite(Entity entity, int seconds) {
        entity.igniteForSeconds(seconds);
    }

    public static BlockPos pos(double x, double y, double z) {
        return BlockPos.containing(x, y, z);
    }
}
