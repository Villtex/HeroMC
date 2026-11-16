package harkenscythe.mob;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * EntityHSSpectralBat: hangs under blocks until a player comes within 4 blocks, then flutters about like a bat while
 * it neither sits nor fights; when it fights it walks with the pet goals (moveSpeed 0.5). No fall damage, does not
 * press plates.
 */
public class SpectralBat extends SpectralPet {
    private static final EntityDataAccessor<Boolean> HANGING = SynchedEntityData.defineId(SpectralBat.class, EntityDataSerializers.BOOLEAN);
    private @Nullable BlockPos flightTarget;

    public SpectralBat(EntityType<? extends SpectralBat> type, Level level) {
        super(type, level, 6.0F, new Sounds(null, HSMobs.sound("entity.bat.hurt"), HSMobs.sound("entity.bat.death"),
                HSMobs.sound("entity.wolf.step"), 0.1F));
        this.setupGoals();
        this.setResting(true);
    }

    @Override
    protected void registerTargets(boolean alertOthers) {
        super.registerTargets(false);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HANGING, false);
    }

    public boolean isResting() {
        return this.entityData.get(HANGING);
    }

    public void setResting(boolean resting) {
        this.entityData.set(HANGING, resting);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isResting() && this.random.nextInt(4) != 0 ? null : HSMobs.sound("entity.bat.ambient");
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.95F;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isResting()) {
            this.setDeltaMovement(Vec3.ZERO);
            this.setPosRaw(this.getX(), Mth.floor(this.getY()) + 1.0 - this.getBbHeight(), this.getZ());
        } else if (this.getTarget() == null) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
        }
    }

    /** updateAITasks of EntityBat, only while it neither sits nor has a target. */
    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.isOrderedToSit() || this.getTarget() != null) {
            return;
        }
        BlockPos pos = this.blockPosition();
        BlockPos above = BlockPos.containing(this.getX(), (int) this.getY() + 1, this.getZ());
        if (this.isResting()) {
            if (!level.getBlockState(above).isRedstoneConductor(level, above)) {
                this.setResting(false);
                level.levelEvent(null, 1025, pos, 0);
            } else {
                if (this.random.nextInt(200) == 0) {
                    this.yHeadRot = this.random.nextInt(360);
                }
                if (level.getNearestPlayer(this, 4.0) != null) {
                    this.setResting(false);
                    level.levelEvent(null, 1025, pos, 0);
                }
            }
        } else {
            if (this.flightTarget != null && (!level.isEmptyBlock(this.flightTarget) || this.flightTarget.getY() < level.getMinY() + 1)) {
                this.flightTarget = null;
            }
            if (this.flightTarget == null || this.random.nextInt(30) == 0
                    || this.flightTarget.distToCenterSqr((int) this.getX(), (int) this.getY(), (int) this.getZ()) < 4.0) {
                this.flightTarget = new BlockPos((int) this.getX() + this.random.nextInt(7) - this.random.nextInt(7),
                        (int) this.getY() + this.random.nextInt(6) - 2, (int) this.getZ() + this.random.nextInt(7) - this.random.nextInt(7));
            }
            double dx = this.flightTarget.getX() + 0.2 - this.getX();
            double dy = this.flightTarget.getY() + 0.1 - this.getY();
            double dz = this.flightTarget.getZ() + 0.2 - this.getZ();
            Vec3 m = this.getDeltaMovement();
            Vec3 n = m.add((Math.signum(dx) * 0.2 - m.x) * 0.1, (Math.signum(dy) * 0.7 - m.y) * 0.1, (Math.signum(dz) * 0.2 - m.z) * 0.1);
            this.setDeltaMovement(n);
            float yaw = (float) (Mth.atan2(n.z, n.x) * 180.0 / Math.PI) - 90.0F;
            this.zza = 0.5F;
            this.setYRot(this.getYRot() + Mth.wrapDegrees(yaw - this.getYRot()));
            if (this.random.nextInt(100) == 0 && level.getBlockState(above).isRedstoneConductor(level, above)) {
                this.setResting(true);
            }
        }
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }
}
