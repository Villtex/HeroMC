package twilightforest.entity.passive;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import twilightforest.TFEntities;
import twilightforest.entity.TFMobUtil;

import java.util.EnumSet;

/**
 * EntityTFTinyBird: 1 health, 0.5 x 0.9, four colours (brown, blue, red, gold), never despawns. It sits until a player
 * without seeds comes within 4 blocks, it is hurt, or (1 in 200 per tick) it stands on nothing it can land on; then it
 * flies to random points nearby (no pathfinder) and lands again on leaves or a solid top (1 in 10 per tick). Tempted
 * with seeds; prefers leaves (path weight 200), wood 15, grass 9.
 */
public class TinyBird extends TFBird {
    private static final EntityDataAccessor<Byte> DATA_TYPE = SynchedEntityData.defineId(TinyBird.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_FLAGS = SynchedEntityData.defineId(TinyBird.class, EntityDataSerializers.BYTE);
    private @Nullable BlockPos currentFlightTarget;
    private int currentFlightTime;

    public TinyBird(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setBirdType(this.random.nextInt(4));
        this.setIsBirdLanded(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new BirdFlyGoal(this));
        this.goalSelector.addGoal(1, new TemptGoal(this, 0.18 / 0.25, stack -> stack.is(Items.WHEAT_SEEDS), true));
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TYPE, (byte) 0);
        builder.define(DATA_FLAGS, (byte) 0);
    }

    public int getBirdType() {
        return this.entityData.get(DATA_TYPE);
    }

    public void setBirdType(int type) {
        this.entityData.set(DATA_TYPE, (byte) type);
    }

    @Override
    public boolean isBirdLanded() {
        return (this.entityData.get(DATA_FLAGS) & 1) != 0;
    }

    public void setIsBirdLanded(boolean landed) {
        byte flags = this.entityData.get(DATA_FLAGS);
        this.entityData.set(DATA_FLAGS, (byte) (landed ? flags | 1 : flags & -2));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("BirdType", this.getBirdType());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setBirdType(input.getIntOr("BirdType", 0));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TFEntities.TINY_BIRD_CHIRP.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TFEntities.TINY_BIRD_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TFEntities.TINY_BIRD_HURT.get();
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        BlockState under = level.getBlockState(pos.below());
        if (under.is(net.minecraft.tags.BlockTags.LEAVES)) {
            return 200.0F;
        }
        if (under.is(net.minecraft.tags.BlockTags.LOGS) || under.is(net.minecraft.tags.BlockTags.PLANKS)) {
            return 15.0F;
        }
        if (under.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)) {
            return 9.0F;
        }
        return TFMobUtil.lightBrightness(level, pos) - 0.5F;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isBirdLanded()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
        }
    }

    @Override
    protected void customServerAiStep(net.minecraft.server.level.ServerLevel level) {
        super.customServerAiStep(level);
        if (this.isBirdLanded()) {
            this.currentFlightTime = 0;
            BlockPos below = BlockPos.containing(this.getX(), this.getY() - 1.0, this.getZ());
            if (this.random.nextInt(200) == 0 && !TFMobUtil.isLandable(level, below) || this.isSpooked()) {
                this.setIsBirdLanded(false);
                level.levelEvent(null, 1015, this.blockPosition(), 0);
                this.setDeltaMovement(this.getDeltaMovement().x, 0.4, this.getDeltaMovement().z);
            }
        } else {
            this.currentFlightTime++;
            if (this.currentFlightTarget != null && (!level.isEmptyBlock(this.currentFlightTarget) || this.currentFlightTarget.getY() < 1)) {
                this.currentFlightTarget = null;
            }
            if (this.currentFlightTarget == null || this.random.nextInt(30) == 0
                    || this.currentFlightTarget.distToLowCornerSqr((int) this.getX(), (int) this.getY(), (int) this.getZ()) < 4.0) {
                int yTarget = this.currentFlightTime < 100 ? 2 : 4;
                this.currentFlightTarget = new BlockPos((int) this.getX() + this.random.nextInt(7) - this.random.nextInt(7),
                        (int) this.getY() + this.random.nextInt(6) - yTarget, (int) this.getZ() + this.random.nextInt(7) - this.random.nextInt(7));
            }
            flyTowards(this, this.currentFlightTarget, 0.1F, 0.1F);
            BlockPos below = BlockPos.containing(this.getX(), this.getY() - 1.0, this.getZ());
            if (this.random.nextInt(10) == 0 && TFMobUtil.isLandable(level, below)) {
                this.setIsBirdLanded(true);
                this.setDeltaMovement(this.getDeltaMovement().x, 0.0, this.getDeltaMovement().z);
            }
        }
    }

    /** The flight step of the tiny bird and the firefly: motion towards the target by signum, yaw to the motion. */
    public static void flyTowards(net.minecraft.world.entity.Mob mob, BlockPos target, double horizontal, double vertical) {
        double d0 = target.getX() + 0.5 - mob.getX();
        double d1 = target.getY() + 0.1 - mob.getY();
        double d2 = target.getZ() + 0.5 - mob.getZ();
        Vec3 m = mob.getDeltaMovement();
        double mx = m.x + (Math.signum(d0) * 0.5 - m.x) * horizontal;
        double my = m.y + (Math.signum(d1) * 0.7F - m.y) * vertical;
        double mz = m.z + (Math.signum(d2) * 0.5 - m.z) * horizontal;
        mob.setDeltaMovement(mx, my, mz);
        float f = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0F;
        float f1 = Mth.wrapDegrees(f - mob.getYRot());
        mob.zza = 0.5F;
        mob.setYRot(mob.getYRot() + f1);
    }

    public boolean isSpooked() {
        Player closest = this.level().getNearestPlayer(this, 4.0);
        return this.hurtTime > 0 || closest != null && !closest.getMainHandItem().is(Items.WHEAT_SEEDS);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    /** EntityAITFBirdFly: holds move and jump (mutex 5) while the bird is in the air. */
    static final class BirdFlyGoal extends Goal {
        private final TinyBird bird;

        BirdFlyGoal(TinyBird bird) {
            this.bird = bird;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return !this.bird.isBirdLanded();
        }
    }
}
