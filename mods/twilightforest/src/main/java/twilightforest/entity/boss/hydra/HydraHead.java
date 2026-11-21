package twilightforest.entity.boss.hydra;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * EntityTFHydraHead ("HydraHead", mod entity 11, tracking 150, every 3 ticks, no velocity): a real entity the hydra
 * spawns once ("head0".."head6") and finds again by its part name after loading (within 16 blocks). An EntityLiving
 * of 1000 health in 1.5.2 that never ran its own AI or movement (EntityTFHydraPart.onUpdate); here a LivingEntity
 * whose tick is only the base tick. Its place, facing, mouth (watcher 18) and state (watcher 19) come from the
 * {@link HydraHeadContainer} on the server. 4 x 4 while active, else 1 x 1. Hits go to the hydra; without a hydra it
 * dies after 1200 ticks, and it goes when the hydra's death is nearly over (deathTime &gt; 190).
 */
public class HydraHead extends LivingEntity {
    private static final EntityDataAccessor<String> DATA_PART_NAME = SynchedEntityData.defineId(HydraHead.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Byte> DATA_MOUTH = SynchedEntityData.defineId(HydraHead.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_STATE = SynchedEntityData.defineId(HydraHead.class, EntityDataSerializers.BYTE);
    private static final EntityDimensions ACTIVE = EntityDimensions.scalable(4.0F, 4.0F);
    private static final EntityDimensions INACTIVE = EntityDimensions.scalable(1.0F, 1.0F);

    public @Nullable Hydra hydraObj;
    /** deathTime of the head on the client while it is dying (clientAnimateHeadDeath). */
    public int dyingTime;

    public HydraHead(EntityType<? extends HydraHead> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.MAX_HEALTH, 1000.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PART_NAME, "");
        builder.define(DATA_MOUTH, (byte) 0);
        builder.define(DATA_STATE, (byte) 0);
    }

    public String getPartName() {
        return this.entityData.get(DATA_PART_NAME);
    }

    public void setPartName(String name) {
        this.entityData.set(DATA_PART_NAME, name);
    }

    public float getMouthOpen() {
        return (this.entityData.get(DATA_MOUTH) & 0xFF) / 255.0F;
    }

    public void setMouthOpen(float openness) {
        openness = Math.max(0.0F, Math.min(1.0F, openness));
        this.entityData.set(DATA_MOUTH, (byte) (Math.round(openness * 255.0F) & 0xFF));
    }

    public int getState() {
        return this.entityData.get(DATA_STATE) & 0xFF;
    }

    public void setState(int state) {
        this.entityData.set(DATA_STATE, (byte) (state & 0xFF));
    }

    /** Neither dying nor dead (the head container's isActive, from the synced state). */
    public boolean isActiveHead() {
        int state = this.getState();
        return state != HydraHeadContainer.STATE_DYING && state != HydraHeadContainer.STATE_DEAD;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (DATA_STATE.equals(accessor)) {
            this.refreshDimensions();
        }
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.isActiveHead() ? ACTIVE : INACTIVE;
    }

    /** EntityTFHydraPart.onUpdate: no AI and no movement, only the entity update. */
    @Override
    public void tick() {
        if (this.hydraObj != null && this.hydraObj.deathTime > 190) {
            this.discard();
            return;
        }
        if (this.hydraObj == null && this.tickCount > 1200) {
            this.discard();
            return;
        }
        this.baseTick();
        this.yHeadRot = this.getYRot();
        this.yHeadRotO = this.yRotO;
        this.yBodyRot = 0.0F;
        this.yBodyRotO = 0.0F;
    }

    /** getLookVec of 1.5.2 (rotationYaw, not the head yaw of today's LivingEntity). */
    public Vec3 look() {
        return Entity.calculateViewVector(this.getXRot(), this.getYRot());
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.is(DamageTypes.GENERIC_KILL)) {
            return super.hurtServer(level, source, damage);
        }
        return this.hydraObj != null && this.hydraObj.attackEntityFromPart(this, level, source, damage);
    }

    @Override
    public boolean is(Entity other) {
        return this == other || this.hydraObj == other;
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return this.hydraObj != null ? this.hydraObj.getPickResult() : null;
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("PartName", this.getPartName());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setPartName(input.getStringOr("PartName", ""));
    }
}
