package legendgear.entity;

import legendgear.LGEntities;
import legendgear.LGParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * EntityQuake: the earth medallion's quake pulses every 7 ticks for 60 ticks with a radius growing from 1 by 0.2 per
 * tick, or (oneshot) one pulse of a given radius (geo amulet fall, earth augment). Every living entity on the ground in
 * the radius takes indirect magic damage (7) and is thrown up by 0.5; noFF spares the thrower.
 */
public class QuakeEntity extends LGEffectEntity {
    private static final EntityDataAccessor<Float> ONESHOT_RADIUS = SynchedEntityData.defineId(QuakeEntity.class, EntityDataSerializers.FLOAT);
    public static final int MAX_LIFESPAN = 60;
    public static final int PULSE_INTERVAL = 7;
    public int damagePerHit = 7;
    public boolean noFF;

    public QuakeEntity(EntityType<? extends QuakeEntity> type, Level level) {
        super(type, level);
    }

    public QuakeEntity(Level level, double x, double y, double z, @Nullable Entity responsible, boolean safeToUser) {
        this(LGEntities.QUAKE.get(), level);
        this.noFF = safeToUser;
        this.setPos(x, y, z);
        this.thrower = responsible;
    }

    public QuakeEntity(Level level, double x, double y, double z, @Nullable Entity responsible, boolean safeToUser, double radius) {
        this(level, x, y, z, responsible, safeToUser);
        this.entityData.set(ONESHOT_RADIUS, (float) radius);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ONESHOT_RADIUS, 0.0F);
    }

    private boolean oneshot() {
        return this.entityData.get(ONESHOT_RADIUS) > 0.0F;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.lifetime == 0 && LGParticles.client(this.level()) && !this.oneshot()) {
            for (int i = 0; i < 15; i++) {
                LGParticles.rune(this.level(), this.getX() + this.random.nextGaussian() * 0.3, this.getY(), this.getZ() + this.random.nextGaussian() * 0.3,
                        0.0, this.random.nextDouble(), 0.0, 1.5F);
            }
        }
        if (this.oneshot()) {
            this.doPulse(this.entityData.get(ONESHOT_RADIUS));
            if (!this.level().isClientSide()) {
                this.discard();
            }
            return;
        }
        if (this.lifetime % PULSE_INTERVAL == 0) {
            this.doPulse(1.0 + 0.2 * this.lifetime);
        }
        this.lifetime++;
        if (this.lifetime > MAX_LIFESPAN && !this.level().isClientSide()) {
            this.discard();
        }
    }

    public void doPulse(double radius) {
        if (LGParticles.client(this.level())) {
            for (int i = 0; i < 12; i++) {
                double th = i * Math.PI * 2.0 / 12.0;
                double vx = Math.cos(th);
                double vz = Math.sin(th);
                this.level().addParticle(ParticleTypes.EXPLOSION, this.getX() + vx * radius, this.getY(), this.getZ() + vz * radius, vx * 0.05, 0.2, vz * 0.05);
            }
        } else if (this.level() instanceof ServerLevel server) {
            for (Entity ent : this.level().getEntities(this, this.getBoundingBox().inflate(radius))) {
                if (ent instanceof LivingEntity el && el.onGround() && el.distanceTo(this) <= radius && (el != this.thrower || !this.noFF)) {
                    el.hurtServer(server, this.damageSources().indirectMagic(this, this.thrower), this.damagePerHit);
                    el.push(this.random.nextGaussian() * 0.3, 0.5, this.random.nextGaussian() * 0.3);
                    el.needsSync = true;
                }
            }
        }
    }
}
