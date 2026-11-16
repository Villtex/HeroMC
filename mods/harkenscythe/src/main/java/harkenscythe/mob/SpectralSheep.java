package harkenscythe.mob;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * EntityHSSpectralSheep: eats grass like a sheep (which grows its spectral fleece back); it cannot be sheared
 * (isShearable false), so it only loses the fleece through saved data.
 */
public class SpectralSheep extends SpectralPet {
    private static final EntityDataAccessor<Boolean> SHEARED = SynchedEntityData.defineId(SpectralSheep.class, EntityDataSerializers.BOOLEAN);
    private int eatAnimationTick;
    private EatBlockGoal eatBlockGoal;

    public SpectralSheep(EntityType<? extends SpectralSheep> type, Level level) {
        super(type, level, 10.0F, new Sounds(HSMobs.sound("entity.sheep.ambient"), HSMobs.sound("entity.sheep.ambient"),
                HSMobs.sound("entity.sheep.ambient"), HSMobs.sound("entity.sheep.step"), 0.4F));
        this.setupGoals();
    }

    @Override
    protected void setupGoals() {
        this.eatBlockGoal = new EatBlockGoal(this);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F));
        this.goalSelector.addGoal(6, this.creepballTempt());
        this.goalSelector.addGoal(7, this.eatBlockGoal);
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.registerTargets(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SHEARED, false);
    }

    public boolean isSheared() {
        return this.entityData.get(SHEARED);
    }

    public void setSheared(boolean sheared) {
        this.entityData.set(SHEARED, sheared);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        this.eatAnimationTick = this.eatBlockGoal.getEatAnimationTick();
        super.customServerAiStep(level);
    }

    @Override
    public void aiStep() {
        if (this.level().isClientSide()) {
            this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
        }
        super.aiStep();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 10) {
            this.eatAnimationTick = 40;
        } else {
            super.handleEntityEvent(id);
        }
    }

    /** func_70894_j */
    public float getHeadEatPositionScale(float a) {
        if (this.eatAnimationTick <= 0) {
            return 0.0F;
        }
        if (this.eatAnimationTick >= 4 && this.eatAnimationTick <= 36) {
            return 1.0F;
        }
        return this.eatAnimationTick < 4 ? (this.eatAnimationTick - a) / 4.0F : -(this.eatAnimationTick - 40 - a) / 4.0F;
    }

    /** func_70890_k */
    public float getHeadEatAngleScale(float a) {
        if (this.eatAnimationTick > 4 && this.eatAnimationTick <= 36) {
            float f = (this.eatAnimationTick - 4 - a) / 32.0F;
            return (float) (Math.PI / 5) + 0.2199115F * Mth.sin(f * 28.7F);
        }
        return this.eatAnimationTick > 0 ? (float) (Math.PI / 5) : this.getXRot(a) * Mth.DEG_TO_RAD;
    }

    /** eatGrassBonus */
    @Override
    public void ate() {
        this.setSheared(false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Sheared", this.isSheared());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSheared(input.getBooleanOr("Sheared", false));
    }
}
