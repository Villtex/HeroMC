package projectzulu.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.level.Level;
import projectzulu.ZuluSounds;

import java.util.EnumSet;

/**
 * EntityPelican: the only bird that lands. In the air (max flight height 15) it switches to "grounded" with a chance of
 * 1/200 per tick while air is below it and drops down; a grounded pelican takes off again with 1/800 per tick, or at
 * once while three blocks of air are below it (so it only really lands when it was low). On the ground it walks
 * (EntityAIWander, 1/120) and attacks by path finding. Fights back when hit, reach 4.0 (squared distance). Health 20,
 * damage 5, 1.5 x 1.4, at most 3 per spawn group (getMaxSpawnedInChunk).
 */
public class PelicanEntity extends AerialAnimal {
    public PelicanEntity(EntityType<? extends PelicanEntity> type, Level level) {
        super(type, level);
        this.maxFlightHeight = 15;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AerialAnimal.createAerialAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, AerialGoals.Chase.attackOnCollide(this, 4.0));
        this.goalSelector.addGoal(6, new AerialGoals.FlyingWander(this));
        this.goalSelector.addGoal(9, new AerialGoals.GroundWander(this, 120));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new AerialGoals.StateTarget(this, EnumSet.of(EntityState.ATTACKING, EntityState.LOOKING)));
    }

    private boolean airBelow(int depth) {
        return this.level().isEmptyBlock(new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()) - depth, Mth.floor(this.getZ())));
    }

    /** The landing and take-off rolls ran before the rest of updateAITasks. */
    @Override
    protected void updateBeforeAi() {
        if (!this.isEntityGrounded() && this.airBelow(1) && this.random.nextInt(200) == 0) {
            this.setEntityGrounded(true);
        }
        if (this.isEntityGrounded() && this.random.nextInt(800) == 0) {
            this.setEntityGrounded(false);
        }
        if (this.isEntityGrounded() && this.airBelow(1) && this.airBelow(2) && this.airBelow(3)) {
            this.setEntityGrounded(false);
        }
        super.updateBeforeAi();
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 3;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ZuluSounds.PELICAN_LIVING.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ZuluSounds.PELICAN_HURT.get();
    }
}
