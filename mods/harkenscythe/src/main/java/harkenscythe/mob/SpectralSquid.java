package harkenscythe.mob;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * EntityHSSpectralSquid: silent; without a target it drifts (its random swimming was never set, the AI version of the
 * entity never ran updateEntityActionState) and moves its tentacles like a squid, falls on land and suffocates there
 * (air 300, 2 damage a second after it runs out). With a target it walks and bites like the others.
 */
public class SpectralSquid extends SpectralPet {
    public float xBodyRot;
    public float xBodyRotO;
    public float zBodyRot;
    public float zBodyRotO;
    public float tentacleMovement;
    public float oldTentacleMovement;
    public float tentacleAngle;
    public float oldTentacleAngle;
    private float speed;
    private float tentacleSpeed;
    private float rotateSpeed;

    public SpectralSquid(EntityType<? extends SpectralSquid> type, Level level) {
        super(type, level, 10.0F, new Sounds(null, null, null, null, 0.4F));
        this.tentacleSpeed = 1.0F / (this.random.nextFloat() + 1.0F) * 0.2F;
        this.setupGoals();
    }

    @Override
    protected void setupGoals() {
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F));
        this.registerTargets(true);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.getTarget() != null) {
            return;
        }
        this.xBodyRotO = this.xBodyRot;
        this.zBodyRotO = this.zBodyRot;
        this.oldTentacleMovement = this.tentacleMovement;
        this.oldTentacleAngle = this.tentacleAngle;
        this.tentacleMovement += this.tentacleSpeed;
        if (this.tentacleMovement > Mth.TWO_PI) {
            this.tentacleMovement -= Mth.TWO_PI;
            if (this.random.nextInt(10) == 0) {
                this.tentacleSpeed = 1.0F / (this.random.nextFloat() + 1.0F) * 0.2F;
            }
        }
        if (this.isInWater()) {
            if (this.tentacleMovement < Mth.PI) {
                float f = this.tentacleMovement / Mth.PI;
                this.tentacleAngle = Mth.sin(f * f * Mth.PI) * Mth.PI * 0.25F;
                if (f > 0.75) {
                    this.speed = 1.0F;
                    this.rotateSpeed = 1.0F;
                } else {
                    this.rotateSpeed *= 0.8F;
                }
            } else {
                this.tentacleAngle = 0.0F;
                this.speed *= 0.9F;
                this.rotateSpeed *= 0.99F;
            }
            if (!this.level().isClientSide()) {
                // randomMotionVec stays 0: the original squid hangs still in the water
                this.setDeltaMovement(Vec3.ZERO);
            }
            Vec3 m = this.getDeltaMovement();
            double h = m.horizontalDistance();
            this.yBodyRot += (-((float) Mth.atan2(m.x, m.z)) * Mth.RAD_TO_DEG - this.yBodyRot) * 0.1F;
            this.setYRot(this.yBodyRot);
            this.zBodyRot += Mth.PI * this.rotateSpeed * 1.5F;
            this.xBodyRot += (-((float) Mth.atan2(h, m.y)) * Mth.RAD_TO_DEG - this.xBodyRot) * 0.1F;
        } else {
            this.tentacleAngle = Mth.abs(Mth.sin(this.tentacleMovement)) * Mth.PI * 0.25F;
            if (!this.level().isClientSide()) {
                Vec3 m = this.getDeltaMovement();
                this.setDeltaMovement(0.0, (m.y - 0.08) * 0.98F, 0.0);
            }
            this.xBodyRot = this.xBodyRot + (-90.0F - this.xBodyRot) * 0.02F;
        }
    }

    /** moveEntityWithHeading: without a target it only moves by its motion (no gravity, no friction). */
    @Override
    public void travel(Vec3 input) {
        if (this.getTarget() != null) {
            super.travel(input);
        } else {
            this.move(MoverType.SELF, this.getDeltaMovement());
        }
    }

    /** onEntityUpdate: out of water the air runs out (2 damage at -20). */
    @Override
    public void baseTick() {
        int air = this.getAirSupply();
        super.baseTick();
        if (this.isAlive() && !this.isEyeInFluid(net.minecraft.tags.FluidTags.WATER)) {
            this.setAirSupply(air - 1);
            if (this.getAirSupply() == -20) {
                this.setAirSupply(0);
                if (this.level() instanceof ServerLevel level) {
                    this.hurtServer(level, this.damageSources().drown(), 2.0F);
                }
            }
        } else {
            this.setAirSupply(300);
        }
    }
}
