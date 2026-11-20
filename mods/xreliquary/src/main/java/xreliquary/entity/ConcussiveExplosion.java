package xreliquary.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import xreliquary.XRFx;

/**
 * ConcussiveExplosion (Reliquary.customConcussiveExplosion): no blocks are broken; every living entity but the shooter
 * within twice the size takes (int)((d² + d) · 6 · 4 · size + 3) with d = (1 - distance / (2 · size)) · exposure and
 * is pushed away by d. Then the explosion sound and particle of 1.5.2.
 */
public final class ConcussiveExplosion {
    private ConcussiveExplosion() {
    }

    public static void explode(ServerLevel level, Entity exploder, @Nullable Player shooter, double x, double y, double z, float size) {
        float radius = size * 2.0F;
        AABB box = new AABB(Mth.floor(x - radius - 1.0), Mth.floor(y - radius - 1.0), Mth.floor(z - radius - 1.0),
                Mth.floor(x + radius + 1.0), Mth.floor(y + radius + 1.0), Mth.floor(z + radius + 1.0));
        Vec3 center = new Vec3(x, y, z);
        DamageSource source = shooter != null ? level.damageSources().playerAttack(shooter) : level.damageSources().explosion(exploder, null);
        for (Entity e : level.getEntities(exploder, box)) {
            if (e instanceof LivingEntity && e != shooter) {
                double dist = Math.sqrt(e.distanceToSqr(x, y, z)) / radius;
                if (dist <= 1.0) {
                    double dx = e.getX() - x;
                    double dy = e.getEyeY() - y;
                    double dz = e.getZ() - z;
                    double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (len != 0.0) {
                        dx /= len;
                        dy /= len;
                        dz /= len;
                        double d = (1.0 - dist) * ServerExplosion.getSeenPercent(center, e);
                        e.hurtServer(level, source, (int) ((d * d + d) * 6.0 * (radius * 2.0F) + 3.0));
                        e.push(dx * d, dy * d, dz * d);
                        e.needsSync = true;
                    }
                }
            }
        }
        XRFx.sound(level, x, y, z, SoundEvents.GENERIC_EXPLODE.value(), 4.0F, (1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F) * 0.7F);
        level.sendParticles(size >= 2.0F ? ParticleTypes.EXPLOSION_EMITTER : ParticleTypes.EXPLOSION, x, y, z, 1, 0, 0, 0, 0);
    }
}
