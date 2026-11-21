package twilightforest.entity.boss;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.entity.PartEntity;
import org.jspecify.annotations.Nullable;

/**
 * EntityTFNagaSegment: one of the naga's body segments, 1.8 × 1.8. In 1.5.2 a separate entity that both sides spawned
 * themselves; here a part entity of the naga that ticks with it. A segment that is not part of the body (the naga is
 * hurt) is inactive: not hit, not drawn.
 */
public class NagaSegment extends PartEntity<Naga> {
    private static final EntityDimensions SIZE = EntityDimensions.scalable(1.8F, 1.8F);
    private final int segment;
    private boolean active;
    private int deathCounter;

    public NagaSegment(Naga naga, int segment) {
        super(naga);
        this.segment = segment;
        this.refreshDimensions();
    }

    public int index() {
        return this.segment;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isDying() {
        return this.deathCounter > 0;
    }

    void activate(double x, double y, double z, float yaw) {
        this.active = true;
        this.deathCounter = 0;
        this.snapTo(x, y, z, yaw, 0.0F);
    }

    /** selfDestruct: bursts in 30 ticks. */
    void selfDestruct() {
        if (this.active && this.deathCounter == 0) {
            this.deathCounter = 30;
        }
    }

    void setSegmentRotation(float yaw, float pitch) {
        this.setYRot(Mth.wrapDegrees(yaw % 360.0F));
        this.setXRot(pitch % 360.0F);
    }

    /** onUpdate: hurts what it touches, bursts when its time has come. */
    void segmentTick() {
        if (!this.active) {
            return;
        }
        this.tickCount++;
        if (this.level() instanceof ServerLevel level) {
            for (Entity entity : level.getEntities(this, this.getBoundingBox().inflate(0.2, 0.0, 0.2))) {
                if (entity.isPushable() && entity != this.getParent() && !(entity instanceof NagaSegment)) {
                    entity.push(this);
                    if (entity instanceof LivingEntity && !(entity instanceof Naga)) {
                        float strength = entity instanceof Animal ? 6.0F : 2.0F;
                        entity.hurtServer(level, this.damageSources().mobAttack(this.getParent()), strength);
                    }
                }
            }
        }
        if (this.deathCounter > 0 && --this.deathCounter == 0) {
            if (this.level().isClientSide()) {
                for (int k = 0; k < 20; k++) {
                    this.level().addParticle(this.random.nextBoolean() ? ParticleTypes.EXPLOSION : ParticleTypes.POOF,
                            this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
                            this.getY() + this.random.nextFloat() * this.getBbHeight(),
                            this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
                            this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02);
                }
            }
            this.active = false;
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return this.active && !this.isInvulnerableToBase(source) && this.getParent().hurtFromSegment(level, source, amount);
    }

    @Override
    public boolean isPickable() {
        return this.active;
    }

    @Override
    public boolean is(Entity other) {
        return this == other || this.getParent() == other;
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return this.getParent().getPickResult();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return SIZE;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }
}
