package xreliquary.entity;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import xreliquary.XREntities;
import xreliquary.XRFx;
import xreliquary.item.MercyCrossItem;
import xreliquary.item.ShotType;

import java.util.Optional;

/**
 * The nine EntityXxxShot classes of the handgun as one entity with a {@link ShotType}: flies straight at 1.2 without
 * gravity or drag; ricochets (neutral, exorcism: once, seeker: three times, sand: never, storm: once), bursts (blaze),
 * explodes (concussive 1.5 without block damage, buster 4.0), passes through everything (ender) or homes in on the
 * nearest living thing within 5 after 3 ticks (ender, seeker). Damage by type as in 1.5.2, counted as the shooter's.
 * Sand and storm shots that leave the world above clear or bring rain.
 */
public class ShotEntity extends Projectile {
    private static final EntityDataAccessor<Byte> TYPE = SynchedEntityData.defineId(ShotEntity.class, EntityDataSerializers.BYTE);
    private int ticksInAir;
    private int ricochets;
    private boolean scheduledForDeath;

    public ShotEntity(EntityType<? extends ShotEntity> type, Level level) {
        super(type, level);
    }

    public ShotEntity(Level level, Player shooter, ShotType type) {
        this(XREntities.SHOT.get(), level);
        this.setOwner(shooter);
        this.entityData.set(TYPE, (byte) type.ordinal());
        float yaw = shooter.getYRot();
        float pitch = shooter.getXRot();
        double x = shooter.getX() - Mth.cos(yaw / 180.0F * (float) Math.PI) * 0.16F;
        double y = shooter.getEyeY() - 0.2;
        double z = shooter.getZ() - Mth.sin(yaw / 180.0F * (float) Math.PI) * 0.16F;
        this.snapTo(x, y, z, yaw, pitch);
        double mx = -Mth.sin(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI);
        double mz = Mth.cos(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI);
        double my = -Mth.sin(pitch / 180.0F * (float) Math.PI);
        this.heading(mx, my, mz, 0.8F * 1.5F, 1.0F);
        if (type == ShotType.BLAZE) {
            Vec3 m = this.getDeltaMovement();
            this.setPos(this.getX() + m.x, this.getY() + m.y, this.getZ() + m.z);
        }
        if (type == ShotType.ENDER) {
            this.noPhysics = true;
        }
    }

    public ShotType type() {
        return ShotType.of(this.entityData.get(TYPE));
    }

    private void heading(double x, double y, double z, float speed, float inaccuracy) {
        double len = Math.sqrt(x * x + y * y + z * z);
        x = x / len + this.random.nextGaussian() * 0.0075F * inaccuracy;
        y = y / len + this.random.nextGaussian() * 0.0075F * inaccuracy;
        z = z / len + this.random.nextGaussian() * 0.0075F * inaccuracy;
        this.setDeltaMovement(x * speed, y * speed, z * speed);
        double h = Math.sqrt(x * x + z * z);
        this.setYRot((float) (Math.atan2(x, z) * 180.0 / Math.PI));
        this.setXRot((float) (Math.atan2(y, h) * 180.0 / Math.PI));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TYPE, (byte) 0);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    private @Nullable Player shooter() {
        return this.getOwner() instanceof Player p ? p : null;
    }

    @Override
    public void tick() {
        this.xOld = this.getX();
        this.yOld = this.getY();
        this.zOld = this.getZ();
        this.baseTick();
        if (this.isRemoved()) {
            return;
        }
        ShotType type = this.type();
        boolean server = this.level() instanceof ServerLevel;
        if (this.ticksInAir > (type == ShotType.STORM ? 300 : 600)) {
            this.discard();
            return;
        }
        if (server && (type == ShotType.SAND || type == ShotType.STORM) && this.getY() > this.level().getMaxY() + 1) {
            this.changeWeather((ServerLevel) this.level(), type == ShotType.STORM);
            this.discard();
            return;
        }
        if (server && (type == ShotType.ENDER || type == ShotType.SEEKER) && this.ticksInAir > 3) {
            this.seek();
        }
        if (type == ShotType.ENDER && this.ticksInAir > 100) {
            for (int i = 0; i < 3; i++) {
                this.particle(ParticleTypes.PORTAL, this.getX(), this.getY() - 1.0, this.getZ(), gaussian(this.getDeltaMovement().x),
                        gaussian(this.getDeltaMovement().y), gaussian(this.getDeltaMovement().z));
                this.particle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            }
            this.discard();
            return;
        }
        this.ticksInAir++;
        if (this.level().isClientSide()) {
            if (this.ticksInAir > 1 && this.ticksInAir < 3) {
                this.level().addParticle(ParticleTypes.FLAME, this.getX() + this.smallGauss(0.1), this.getY() + this.smallGauss(0.1),
                        this.getZ() + this.smallGauss(0.1), 0, 0, 0);
                for (int i = 0; i < 3; i++) {
                    this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.5F, 0.5F, 0.5F), this.getX() + this.smallGauss(0.1),
                            this.getY() + this.smallGauss(0.1), this.getZ() + this.smallGauss(0.1), 0.5, 0.5, 0.5);
                    Vec3 m = this.getDeltaMovement();
                    this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), gaussian(m.x), gaussian(m.y), gaussian(m.z));
                }
            } else {
                this.flightParticles(type);
            }
        }
        Vec3 start = this.position();
        Vec3 motion = this.getDeltaMovement();
        Vec3 end = start.add(motion);
        HitResult hit = this.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() != HitResult.Type.MISS) {
            end = hit.getLocation();
        }
        Entity owner = this.getOwner();
        Entity closest = null;
        double best = 0.0;
        for (Entity e : this.level().getEntities(this, this.getBoundingBox().expandTowards(motion).inflate(1.0))) {
            if (e.isPickable() && e.isAlive() && (e != owner || this.ticksInAir >= 5)) {
                Optional<Vec3> clip = e.getBoundingBox().inflate(0.3).clip(start, end);
                if (clip.isPresent()) {
                    double d = start.distanceTo(clip.get());
                    if (d < best || best == 0.0) {
                        closest = e;
                        best = d;
                    }
                }
            }
        }
        if (closest != null) {
            hit = new EntityHitResult(closest);
        }
        if (server && hit.getType() != HitResult.Type.MISS) {
            this.impact((ServerLevel) this.level(), hit, type);
            if (this.scheduledForDeath) {
                this.discard();
            }
            if (this.isRemoved()) {
                return;
            }
        }
        motion = this.getDeltaMovement();
        this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
        this.applyEffectsFromBlocks();
    }

    private void impact(ServerLevel level, HitResult hit, ShotType type) {
        if (hit instanceof EntityHitResult eh) {
            Entity e = eh.getEntity();
            if (e == this.getOwner()) {
                return;
            }
            this.hitEntity(level, e, type);
        } else if (hit instanceof BlockHitResult bh && type != ShotType.ENDER) {
            this.hitGround(level, bh.getDirection(), type);
        }
    }

    private int d6() {
        return this.random.nextInt(6) + 1;
    }

    private int d12() {
        return this.random.nextInt(12) + 1;
    }

    private void damage(ServerLevel level, Entity e, int amount) {
        Player shooter = this.shooter();
        e.hurtServer(level, shooter != null ? level.damageSources().playerAttack(shooter) : level.damageSources().thrown(this, this.getOwner()), amount);
    }

    private void hitEntity(ServerLevel level, Entity e, ShotType type) {
        Vec3 m = this.getDeltaMovement();
        switch (type) {
            case NEUTRAL -> {
                this.damage(level, e, 8 + this.d6());
                this.hitParticles(ParticleTypes.ENCHANTED_HIT, 8);
            }
            case EXORCISM -> {
                this.damage(level, e, MercyCrossItem.undead(e) ? 16 + this.d12() : 8 + this.d6());
                for (int i = 0; i < 8; i++) {
                    this.particle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.5F, 0.5F, 0.5F), this.getX() + this.smallGauss(0.1),
                            this.getY() + this.smallGauss(0.1), this.getZ() + this.smallGauss(0.1), this.posGauss(1.0), this.posGauss(1.0), 0.0);
                }
            }
            case BLAZE -> {
                e.igniteForSeconds(1.0F);
                this.damage(level, e, e.fireImmune() ? 2 : 8 + this.d12());
                this.hitParticles(ParticleTypes.FLAME, 8);
            }
            case ENDER -> {
                this.damage(level, e, 16 + this.d12());
                this.hitParticles(ParticleTypes.WITCH, 8);
                return;
            }
            case CONCUSSIVE -> {
                this.damage(level, e, 4 + this.d6());
                ConcussiveExplosion.explode(level, this, this.shooter(), this.getX(), this.getY(), this.getZ(), 1.5F);
            }
            case BUSTER -> {
                this.damage(level, e, 8 + this.d6());
                level.explode(this, this.getX(), this.getY(), this.getZ(), 4.0F, false, Level.ExplosionInteraction.TNT);
            }
            case SEEKER -> {
                this.damage(level, e, 12 + this.d6());
                for (int i = 0; i < 8; i++) {
                    this.particle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.1F, 1.0F, 1.0F), this.getX() + this.smallGauss(0.1),
                            this.getY() + this.smallGauss(0.1), this.getZ() + this.smallGauss(0.1), 0.1, 1.0, 1.0);
                }
            }
            case SAND -> {
                int amount = 8 + this.d6();
                if (e instanceof Creeper) {
                    amount *= 4;
                }
                if (e instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 1));
                }
                if (level.isRaining()) {
                    amount /= 2;
                }
                this.damage(level, e, amount);
                for (int i = 0; i < 8; i++) {
                    this.particle(new DustParticleOptions(0xB3B34D, 1.0F), this.getX(), this.getY(), this.getZ(), 0.7, 0.7, 0.3);
                }
            }
            case STORM -> {
                int amount = 8 + this.d6();
                if (e instanceof Creeper creeper) {
                    LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
                    if (bolt != null) {
                        bolt.snapTo(e.getX(), e.getY(), e.getZ());
                        creeper.thunderHit(level, bolt);
                    }
                } else {
                    amount = (int) (amount * (1.0F + level.getRainLevel(1.0F) + level.getThunderLevel(1.0F) * 2.0F));
                    net.minecraft.core.BlockPos pos = net.minecraft.core.BlockPos.containing(e.getX() + 0.5, e.getY() + 0.5, e.getZ() + 0.5);
                    if (level.isRainingAt(pos)) {
                        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
                        if (bolt != null) {
                            bolt.snapTo(e.getX(), e.getY(), e.getZ());
                            level.addFreshEntity(bolt);
                        }
                    }
                }
                this.damage(level, e, amount);
                for (int i = 0; i < 18; i++) {
                    this.particle(ParticleTypes.BUBBLE, this.getX(), this.getY(), this.getZ(), gaussian(m.x), this.random.nextFloat() + m.y, gaussian(m.z));
                }
            }
            default -> {
            }
        }
        this.discard();
    }

    private void hitGround(ServerLevel level, Direction side, ShotType type) {
        switch (type) {
            case BLAZE -> {
                Vec3 m = this.getDeltaMovement();
                for (int i = 0; i < 40; i++) {
                    double vx;
                    double vy;
                    double vz;
                    switch (side) {
                        case DOWN -> { vx = this.smallGauss(0.1) + m.x / 4.0; vy = -this.posGauss(0.2); vz = this.smallGauss(0.2) + m.z / 4.0; }
                        case UP -> { vx = this.smallGauss(0.1) + m.x / 4.0; vy = this.posGauss(0.2); vz = this.smallGauss(0.2) + m.z / 4.0; }
                        case NORTH -> { vx = this.smallGauss(0.1) + m.x / 4.0; vy = this.smallGauss(0.1) + m.y / 4.0; vz = -this.posGauss(0.2); }
                        case SOUTH -> { vx = this.smallGauss(0.1) + m.x / 4.0; vy = this.smallGauss(0.1) + m.y / 4.0; vz = this.posGauss(0.2); }
                        case WEST -> { vx = -this.posGauss(0.2); vy = this.smallGauss(0.1) + m.y / 4.0; vz = this.smallGauss(0.1) + m.z / 4.0; }
                        default -> { vx = this.posGauss(0.2); vy = this.smallGauss(0.1) + m.y / 4.0; vz = this.smallGauss(0.1) + m.z / 4.0; }
                    }
                    this.particle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), vx, vy, vz);
                }
                this.discard();
            }
            case CONCUSSIVE -> {
                ConcussiveExplosion.explode(level, this, this.shooter(), this.getX(), this.getY(), this.getZ(), 1.5F);
                this.discard();
            }
            case BUSTER -> {
                level.explode(this, this.getX(), this.getY(), this.getZ(), 4.0F, false, Level.ExplosionInteraction.TNT);
                this.discard();
            }
            default -> this.ricochet(side, type == ShotType.SEEKER ? 3 : type == ShotType.SAND ? 0 : 1);
        }
    }

    private void ricochet(Direction side, int max) {
        Vec3 m = this.getDeltaMovement();
        switch (side.getAxis()) {
            case Y -> this.setDeltaMovement(m.x, -m.y, m.z);
            case Z -> this.setDeltaMovement(m.x, m.y, -m.z);
            case X -> this.setDeltaMovement(-m.x, m.y, m.z);
        }
        this.needsSync = true;
        if (++this.ricochets > max) {
            this.scheduledForDeath = true;
            for (int i = 0; i < 4; i++) {
                double gx = gaussian(0.1);
                double gy = gaussian(0.1);
                double gz = gaussian(0.1);
                switch (side) {
                    case DOWN -> gy = -gy;
                    case NORTH -> gz = -gz;
                    case WEST -> gx = -gx;
                    default -> {
                    }
                }
                this.particle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), gx, gy, gz);
            }
        }
    }

    /** The homing of the ender and seeker shot. */
    private void seek() {
        Entity target = null;
        double closest = Double.MAX_VALUE;
        AABB box = new AABB(this.getX() - 5, this.getY() - 5, this.getZ() - 5, this.getX() + 5, this.getY() + 5, this.getZ() + 5);
        for (Entity e : this.level().getEntities(this, box)) {
            if (e instanceof LivingEntity && e != this.getOwner() && e.isAlive() && this.distanceTo(e) < closest) {
                closest = this.distanceTo(e);
                target = e;
            }
        }
        if (target != null) {
            double dx = target.getX() - this.getX();
            double dy = target.getY() + target.getBbHeight() / 1.75 - this.getY();
            double dz = target.getZ() - this.getZ();
            double mx = Math.sqrt(Math.abs(dx)) / 6.0 * (dx < 0.0 ? -1.0 : 1.0);
            double my = Math.sqrt(Math.abs(dy)) / 6.0 * (dy < 0.0 ? -1.0 : 1.0);
            double mz = Math.sqrt(Math.abs(dz)) / 6.0 * (dz < 0.0 ? -1.0 : 1.0);
            this.setDeltaMovement(adjust(mx), adjust(my), adjust(mz));
            this.needsSync = true;
        }
    }

    private static double adjust(double m) {
        if (Math.abs(m) < 0.6) {
            return m * 1.1;
        }
        return Math.abs(m) > 0.7 ? m * 0.9 : m;
    }

    private void changeWeather(ServerLevel level, boolean storm) {
        var weather = level.getWeatherData();
        if (storm) {
            if (!weather.isRaining()) {
                weather.setRaining(true);
            } else if (!weather.isThundering()) {
                weather.setThundering(true);
            }
        } else if (weather.isRaining()) {
            weather.setRaining(false);
        } else if (weather.isThundering()) {
            weather.setThundering(false);
        }
    }

    private void flightParticles(ShotType type) {
        if (this.ticksInAir % 3 != 0) {
            return;
        }
        Level level = this.level();
        Vec3 m = this.getDeltaMovement();
        switch (type) {
            case EXORCISM -> {
                double g = gaussian(1.0);
                level.addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, (float) Math.min(g, 1.0), (float) Math.min(g, 1.0), 0.0F),
                        this.getX(), this.getY(), this.getZ(), g, g, 0.0);
            }
            case BLAZE -> {
                if (this.ticksInAir < 9) {
                    level.addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), this.lowGauss(m.x), this.lowGauss(m.y), this.lowGauss(m.z));
                }
            }
            case ENDER -> level.addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() - 1.0, this.getZ(), gaussian(m.x), gaussian(m.y), gaussian(m.z));
            case CONCUSSIVE, BUSTER -> level.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), gaussian(m.x), gaussian(m.y), gaussian(m.z));
            case SEEKER -> level.addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.0F, 1.0F, 1.0F), this.getX(), this.getY(), this.getZ(), 0.0,
                    gaussian(1.0), gaussian(1.0));
            default -> {
            }
        }
    }

    private void hitParticles(ParticleOptions particle, int n) {
        Vec3 m = this.getDeltaMovement();
        for (int i = 0; i < n; i++) {
            this.particle(particle, this.getX(), this.getY(), this.getZ(), gaussian(m.x), gaussian(m.y), gaussian(m.z));
        }
    }

    private void particle(ParticleOptions particle, double x, double y, double z, double vx, double vy, double vz) {
        XRFx.particle(this.level(), particle, x, y, z, vx, vy, vz);
    }

    private double smallGauss(double d) {
        return (this.random.nextFloat() - 0.5) * d;
    }

    private double posGauss(double d) {
        return this.random.nextFloat() * 0.5 * d;
    }

    private double lowGauss(double d) {
        return d - d * (this.random.nextFloat() / 4.0F + 0.5);
    }

    private double gaussian(double d) {
        return d + d * ((this.random.nextFloat() - 0.5) / 4.0);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
