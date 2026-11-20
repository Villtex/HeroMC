package meteors.entity;

import meteors.Craters;
import meteors.MeteorData;
import meteors.MeteorEvents;
import meteors.MeteorType;
import meteors.MeteorsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

/**
 * EntityMeteor: a meteor of a type and size (1..3) falling from y 250 (gravity 0.04, drag 0.98). Where it lands it
 * explodes with 5 * size (fiery for Meteorite, Kreknorite and Unknown) and leaves the crater of its type
 * ({@link Craters}). Over protected land (not summoned) it bursts in the air instead. Kitty comets can wait a while
 * before they start to fall.
 */
public class FallingMeteor extends Entity {
    private static final EntityDataAccessor<Integer> DATA_SIZE = SynchedEntityData.defineId(FallingMeteor.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TYPE = SynchedEntityData.defineId(FallingMeteor.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PAUSE = SynchedEntityData.defineId(FallingMeteor.class, EntityDataSerializers.INT);
    protected boolean summoned;
    private int spawnPauseTicks;
    private boolean clientInit;

    public FallingMeteor(EntityType<? extends FallingMeteor> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    public FallingMeteor(Level level, int size, double x, double z, MeteorType type, boolean summoned) {
        this(MeteorsMod.FALLING_METEOR.get(), level);
        this.entityData.set(DATA_SIZE, size);
        this.entityData.set(DATA_TYPE, type.id());
        this.summoned = summoned;
        this.setPos(x, 250.0, z);
        this.xo = x;
        this.yo = 250.0;
        this.zo = z;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SIZE, 1);
        builder.define(DATA_TYPE, 0);
        builder.define(DATA_PAUSE, 0);
    }

    public int getSize() {
        return this.entityData.get(DATA_SIZE);
    }

    public MeteorType getMeteorType() {
        return MeteorType.byId(this.entityData.get(DATA_TYPE));
    }

    public void setSpawnPauseTicks(int ticks) {
        this.spawnPauseTicks = ticks;
        this.entityData.set(DATA_PAUSE, ticks);
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (level.dimension() != Level.OVERWORLD) {
            // !isSurfaceWorld
            this.discard();
            return;
        }
        if (level instanceof ServerLevel server && !this.summoned && this.inProtectedZone(server)) {
            this.burst(server);
            return;
        }
        if (level.isClientSide() && !this.clientInit) {
            this.clientInit = true;
            this.spawnPauseTicks = this.entityData.get(DATA_PAUSE);
        }
        if (this.spawnPauseTicks > 0) {
            this.spawnPauseTicks--;
            return;
        }
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        Vec3 motion = this.getDeltaMovement().add(0.0, -0.04F, 0.0);
        this.setDeltaMovement(motion);
        this.move(MoverType.SELF, motion);
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98F));
        if (this.onGround()) {
            this.discard();
            if (level instanceof ServerLevel server) {
                this.land(server);
            }
        } else if (level.isClientSide()) {
            for (int n = 0; n < 4; n++) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, this.getX() + (n & 1), this.getY() + 0.49 + 2.0, this.getZ() + (n >> 1), 0.0, 0.0, 0.0);
            }
        } else if (this.tickCount % 100 == 0) {
            MeteorData.keepLoaded((ServerLevel) level, (int) this.getX(), (int) this.getZ());
        }
    }

    /**
     * MeteorInProtectedZone(): the original asked the chunk at (posX, posY) instead of (posX, posZ); kept, the
     * announced meteor was already checked at the right place before it was spawned.
     */
    private boolean inProtectedZone(ServerLevel level) {
        int x = (int) this.getX();
        int z = (int) (this.getY() + 0.49);
        return MeteorData.get(level.getServer()).isProtected(x >> 4, z >> 4);
    }

    private void burst(ServerLevel level) {
        int x = (int) this.getX();
        int z = (int) (this.getY() + 0.49);
        MeteorData.get(level.getServer()).meteorPrevented(level, x >> 4, z >> 4, this.getMeteorType());
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 5.0F,
                (1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F) * 0.7F);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        this.discard();
    }

    private void land(ServerLevel level) {
        if (!this.summoned) {
            AABB box = new AABB(this.getX() - 40.0, this.getY() - 20.0, this.getZ() - 40.0, this.getX() + 40.0, this.getY() + 20.0, this.getZ() + 40.0);
            for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, box)) {
                MeteorEvents.grant(player, "found_meteor");
            }
        }
        // posY of 1.5.2 was the centre of the meteor (yOffset = height / 2)
        double cy = this.getY() + 0.49;
        MeteorType type = this.getMeteorType();
        List<BlockPos> exploded = MeteorEvents.explode(level, this.getX(), cy, this.getZ(), 5.0F * this.getSize(), type.fieryExplosion);
        Craters.crash(level, this.random, type, this.getSize(), exploded, (int) this.getX(), (int) cy, (int) this.getZ());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("size", this.getSize());
        output.putBoolean("summoned", this.summoned);
        output.putInt("metTypeID", this.getMeteorType().id());
        output.putInt("pauseTicks", this.spawnPauseTicks);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.entityData.set(DATA_SIZE, input.getIntOr("size", 1));
        this.summoned = input.getBooleanOr("summoned", false);
        this.entityData.set(DATA_TYPE, input.getIntOr("metTypeID", 0));
        this.setSpawnPauseTicks(input.getIntOr("pauseTicks", 0));
    }
}
