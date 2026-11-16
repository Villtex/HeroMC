package harkenscythe.mob;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * EntityHSSpectralPig: a tamed one takes a saddle and is ridden by right-click (not while sitting); with a carrot on a
 * stick the rider steers it (EntityAIControlledByPlayer, max speed 0.34; 26.3 riding without the boost). It follows a
 * carrot on a stick (0.3) and drops its saddle.
 */
public class SpectralPig extends SpectralPet {
    private static final EntityDataAccessor<Boolean> SADDLED = SynchedEntityData.defineId(SpectralPig.class, EntityDataSerializers.BOOLEAN);

    public SpectralPig(EntityType<? extends SpectralPig> type, Level level) {
        super(type, level, 10.0F, new Sounds(HSMobs.sound("entity.pig.ambient"), HSMobs.sound("entity.pig.ambient"),
                HSMobs.sound("entity.pig.death"), HSMobs.sound("entity.pig.step"), 0.4F));
        this.setupGoals();
    }

    @Override
    protected void setupGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F));
        this.goalSelector.addGoal(7, new TemptGoal(this, this.factor(0.3), s -> s.is(Items.CARROT_ON_A_STICK), false));
        this.goalSelector.addGoal(8, this.creepballTempt());
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));
        this.registerTargets(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SADDLED, false);
    }

    public boolean isPigSaddled() {
        return this.entityData.get(SADDLED);
    }

    public void setPigSaddled(boolean saddled) {
        this.entityData.set(SADDLED, saddled);
    }

    @Override
    protected @Nullable InteractionResult interactTamed(Player player, InteractionHand hand, ItemStack stack) {
        if (!stack.isEmpty() && !this.isPigSaddled() && stack.is(Items.SADDLE)) {
            stack.consume(1, player);
            if (!this.level().isClientSide()) {
                this.setPigSaddled(true);
                this.level().playSound(null, this, SoundEvents.PIG_SADDLE.value(), SoundSource.NEUTRAL, 0.5F, 1.0F);
            }
        }
        if (!this.isOrderedToSit() && this.isPigSaddled() && !this.isVehicle()) {
            if (!this.level().isClientSide()) {
                player.startRiding(this);
            }
            return InteractionResult.SUCCESS;
        }
        return null;
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        if (this.isPigSaddled() && this.getFirstPassenger() instanceof Player player
                && (player.getMainHandItem().is(Items.CARROT_ON_A_STICK) || player.getOffhandItem().is(Items.CARROT_ON_A_STICK))) {
            return player;
        }
        return super.getControllingPassenger();
    }

    @Override
    protected void tickRidden(Player controller, Vec3 riddenInput) {
        super.tickRidden(controller, riddenInput);
        this.setRot(controller.getYRot(), controller.getXRot() * 0.5F);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
    }

    @Override
    protected Vec3 getRiddenInput(Player controller, Vec3 selfInput) {
        return new Vec3(0.0, 0.0, 1.0);
    }

    /** EntityAIControlledByPlayer(0.34): the vanilla pig's 0.225 of its speed, scaled to the old 0.34 of 0.25. */
    @Override
    protected float getRiddenSpeed(Player controller) {
        return (float) (this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.225 * 0.34 / 0.25);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (this.isPigSaddled()) {
            this.spawnAtLocation(level, new ItemStack(Items.SADDLE));
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Saddle", this.isPigSaddled());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setPigSaddled(input.getBooleanOr("Saddle", false));
    }
}
