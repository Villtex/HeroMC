package legendgear.entity;

import legendgear.LGEntities;
import legendgear.LGFx;
import legendgear.LGParticles;
import legendgear.LGSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * EntityFireblast: the fire medallion's firestorm. Flames swirl out to 8 blocks for 60 ticks, then everything living in
 * that radius burns for 10 s and takes 15 firestorm damage (bypasses armour, fire, magic); flames rise until tick 70.
 */
public class FireblastEntity extends LGEffectEntity {
    public static final int MAX_LIFESPAN = 70;
    public static final int DETONATION_TIME = 60;
    public static final double DETONATION_RADIUS = 8.0;
    public int damagePerHit = 15;
    public boolean noFF;

    public FireblastEntity(EntityType<? extends FireblastEntity> type, Level level) {
        super(type, level);
    }

    public FireblastEntity(Level level, double x, double y, double z, @Nullable Entity responsible) {
        this(LGEntities.FIREBLAST.get(), level);
        this.setPos(x, y, z);
        this.thrower = responsible;
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        boolean client = LGParticles.client(level);
        if (this.lifetime == 0 && client) {
            for (int i = 0; i < 15; i++) {
                LGParticles.rune(level, this.getX() + this.random.nextGaussian() * 0.3, this.getY(), this.getZ() + this.random.nextGaussian() * 0.3, 0.0,
                        this.random.nextDouble(), 0.0, 1.5F);
            }
        }
        if (this.lifetime == 0) {
            LGFx.sound(this, LGSounds.FIREBUILD.get(), 3.0F, 1.0F);
        }
        if (this.lifetime < DETONATION_TIME && client) {
            double progress = 1.0 * this.lifetime / DETONATION_TIME;
            double decel = 1.0 - progress;
            decel = 1.0 - decel * decel * decel;
            double r = DETONATION_RADIUS * decel;
            double theta = decel * Math.PI * 4.0;
            for (int i = 0; i < 3; i++) {
                LGParticles.flare(level, this.getX() + Math.cos(theta) * r, this.getY(), this.getZ() + Math.sin(theta) * r, 0.0, 0.05, 0.0, 3.0F);
                theta += Math.PI * 2.0 / 3.0;
            }
        }
        if (this.lifetime == DETONATION_TIME) {
            LGFx.sound(this, SoundEvents.GHAST_SHOOT, 3.0F, 0.8F);
            LGFx.sound(this, SoundEvents.GHAST_SHOOT, 3.0F, 1.0F);
            LGFx.sound(this, SoundEvents.GHAST_SHOOT, 3.0F, 1.2F);
            if (level instanceof ServerLevel server) {
                this.burnThings(server, DETONATION_RADIUS);
            }
        }
        if (this.lifetime > DETONATION_TIME && client) {
            for (int i = 0; i < 5; i++) {
                LGParticles.flare(level, this.getX() + this.random.nextGaussian() * DETONATION_RADIUS * 0.5, this.getY(),
                        this.getZ() + this.random.nextGaussian() * DETONATION_RADIUS * 0.5, 0.0, 0.5, 0.0, 10.0F);
                level.addParticle(ParticleTypes.LAVA, this.getX() + this.random.nextGaussian() * DETONATION_RADIUS * 0.5, this.getY(),
                        this.getZ() + this.random.nextGaussian() * DETONATION_RADIUS * 0.5, 0.0, 0.5, 0.0);
            }
        }
        this.lifetime++;
        if (this.lifetime >= MAX_LIFESPAN && !level.isClientSide()) {
            this.discard();
        }
    }

    private void burnThings(ServerLevel level, double radius) {
        DamageSource damage = new DamageSource(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                .getOrThrow(legendgear.LegendGear.FIRESTORM), this, this.thrower);
        for (Entity ent : level.getEntities(this, this.getBoundingBox().inflate(radius))) {
            if (ent instanceof LivingEntity el && el.distanceTo(this) <= radius && (el != this.thrower || !this.noFF)) {
                el.igniteForSeconds(10);
                el.hurtServer(level, damage, this.damagePerHit);
            }
        }
    }
}
