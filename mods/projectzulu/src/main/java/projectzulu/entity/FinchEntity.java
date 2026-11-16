package projectzulu.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import projectzulu.ZuluSounds;

/**
 * EntityFinch (red, green and blue finch share the class; only the texture differs, see the renderer). Always in the
 * air, flies around low (max flight height 5), flees instead of fighting (only the state changes: the finch has no
 * flee task) and now and then sits still on a full block ("posture", up to 400 ticks, until a player comes within 4
 * blocks). Health 8, 0.5 x 0.5. The living and hurt sounds are swapped in the original (living = "birdhurt", hurt =
 * "bird") and are kept that way; the "birdplop" step sound never played (canTriggerWalking false).
 */
public class FinchEntity extends AerialAnimal {
    private int stayStillTimer;

    public FinchEntity(EntityType<? extends FinchEntity> type, Level level) {
        super(type, level);
        this.maxFlightHeight = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AerialAnimal.createAerialAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new AerialGoals.StayStill(this, EntityState.POSTURE));
        this.goalSelector.addGoal(6, new AerialGoals.FlyingWander(this));
    }

    @Override
    protected boolean shouldPanic() {
        return true;
    }

    @Override
    protected EntityState computeEntityState() {
        return this.stayStillTimer > 0 ? EntityState.POSTURE : super.computeEntityState();
    }

    private boolean onFullBlock() {
        BlockPos below = new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()) - 1, Mth.floor(this.getZ()));
        return this.level().getBlockState(below).isRedstoneConductor(this.level(), below);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.random.nextInt(100) == 0 && this.onFullBlock()) {
            this.stayStillTimer = 400;
        }
        if (this.getEntityState() == EntityState.POSTURE) {
            if (!this.onFullBlock()) {
                this.stayStillTimer = 0;
            } else {
                if (this.random.nextInt(200) == 0) {
                    this.setYHeadRot(this.random.nextInt(360));
                }
                if (level.getNearestPlayer(this, 4.0) != null || this.random.nextInt(400) == 0) {
                    this.stayStillTimer = 0;
                }
            }
            this.stayStillTimer = Math.max(this.stayStillTimer - 1, 0);
        }
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ZuluSounds.BIRD_HURT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ZuluSounds.BIRD.get();
    }
}
