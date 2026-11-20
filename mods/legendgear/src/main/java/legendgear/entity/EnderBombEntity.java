package legendgear.entity;

import legendgear.LGEntities;
import legendgear.LGFx;
import legendgear.LGParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * EntityEnderBomb: a ring grows to a radius of 10 in 110 ticks and collapses in 10; while it collapses every living
 * entity between the ring and the radius of 10 is teleported up to 20 blocks away and 18 up, where there is room.
 */
public class EnderBombEntity extends LGEffectEntity {
    private static final EntityDataAccessor<Integer> TIMER = SynchedEntityData.defineId(EnderBombEntity.class, EntityDataSerializers.INT);
    public static final int EXPAND_TIME = 110;
    public static final int COLLAPSE_TIME = 10;
    public static final double MAX_RADIUS = 10.0;
    public double radius;

    public EnderBombEntity(EntityType<? extends EnderBombEntity> type, Level level) {
        super(type, level);
    }

    public EnderBombEntity(Level level, double x, double y, double z) {
        this(LGEntities.ENDER_BOMB.get(), level);
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TIMER, 0);
    }

    public int timer() {
        return this.entityData.get(TIMER);
    }

    private boolean teleport(LivingEntity e, double x, double y, double z) {
        double oldX = e.getX();
        double oldY = e.getY();
        double oldZ = e.getZ();
        if (!this.level().hasChunkAt(BlockPos.containing(x, y, z))) {
            return false;
        }
        e.setPos(x, y, z);
        if (!this.level().noCollision(e, e.getBoundingBox()) || this.level().containsAnyLiquid(e.getBoundingBox())) {
            e.setPos(oldX, oldY, oldZ);
            return false;
        }
        e.teleportTo(x, y, z);
        if (this.level() instanceof ServerLevel server) {
            for (int i = 0; i < 128; i++) {
                double t = i / 127.0;
                server.sendParticles(ParticleTypes.PORTAL, oldX + (x - oldX) * t + (this.random.nextDouble() - 0.5) * e.getBbWidth() * 2.0,
                        oldY + (y - oldY) * t + this.random.nextDouble() * e.getBbHeight(),
                        oldZ + (z - oldZ) * t + (this.random.nextDouble() - 0.5) * e.getBbWidth() * 2.0, 0,
                        (this.random.nextFloat() - 0.5F) * 0.2F, (this.random.nextFloat() - 0.5F) * 0.2F, (this.random.nextFloat() - 0.5F) * 0.2F, 1.0);
            }
        }
        LGFx.sound(this.level(), oldX, oldY, oldZ, SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        LGFx.sound(this, SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        int t = this.timer();
        if (t < EXPAND_TIME) {
            if (t == 0) {
                if (LGParticles.client(this.level())) {
                    for (int i = 0; i < 15; i++) {
                        LGParticles.rune(this.level(), this.getX() + this.random.nextGaussian() * 0.3, this.getY(), this.getZ() + this.random.nextGaussian() * 0.3,
                                0.0, this.random.nextDouble(), 0.0, 1.5F);
                    }
                }
                LGFx.sound(this, SoundEvents.ENDERMAN_STARE, 3.0F, 2.0F);
            }
            this.radius = t * 1.0 / EXPAND_TIME * MAX_RADIUS;
        } else {
            this.radius = MAX_RADIUS * (COLLAPSE_TIME - (t - EXPAND_TIME)) * 1.0 / COLLAPSE_TIME;
            if (!this.level().isClientSide()) {
                for (LivingEntity el : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(MAX_RADIUS))) {
                    double dist = el.distanceTo(this);
                    if (dist <= MAX_RADIUS && dist >= this.radius) {
                        this.teleport(el, el.getX() + this.random.nextGaussian() * 20.0, el.getY() + 18.0, el.getZ() + this.random.nextGaussian() * 20.0);
                    }
                }
            }
        }
        if (t > EXPAND_TIME + COLLAPSE_TIME && !this.level().isClientSide()) {
            this.discard();
        }
        if (!this.level().isClientSide()) {
            this.entityData.set(TIMER, t + 1);
        }
    }
}
