package projectzulu.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import projectzulu.ZuluHorses;

/**
 * EntityHorseBase (the seven colour classes only differ in their texture; here one class, the colour is the entity
 * type). A wild horse goes for a player who hits it (angerLevel 400). Health 20, armor 0, speed 0.3, damage 4 (base
 * table 0/3/4/5). Size 1.5 x 2.0. Breeds with an apple or a carrot (only with the same colour: canMateWith compared
 * the class), follows a held apple.
 * <p>
 * Riding (EntityGenericRideable): a right click with a saddle saddles the horse (the original kept the saddle in the
 * player's hand and never dropped it again, as here), a right click on a saddled horse mounts it. The rider steers
 * with a stick in the main hand (EntityAIControlledByPlayer, the 1.5.2 pig AI with max speed 0.34): the horse turns
 * by half the difference to the rider's yaw (at most 10 degrees per tick), speeds up by 1 % of the remaining
 * difference per tick and jumps over one-block steps. Switching to any other item keeps it going straight ahead
 * (shouldIgnorePlayerRot); with an empty hand the rider loses control and the horse wanders on with him. A rider
 * cannot hurt his own horse.
 * <p>
 * Not tameable: EntityHorseBase sits on EntityGenericTameable but never named a taming item, so wild and tame were
 * the same; it left peaceful like every untamed EntityGenericAnimal (notInPeaceful).
 */
public class ZuluHorseEntity extends ZuluLandAnimal {
    private static final EntityDataAccessor<Boolean> DATA_SADDLED = SynchedEntityData.defineId(ZuluHorseEntity.class, EntityDataSerializers.BOOLEAN);
    /** maxSpeed of EntityAIControlledByPlayer. */
    private static final float MAX_RIDDEN_SPEED = 0.34F;
    /** maxRotation of EntityAIControlledByPlayer. */
    private static final float MAX_TURN = 10.0F;

    /** The controlled-by-player task is running (continueExecuting keeps it alive with any held item). */
    private boolean steered;
    /** currentSpeed of EntityAIControlledByPlayer. */
    private float riddenSpeed;

    public ZuluHorseEntity(EntityType<? extends ZuluHorseEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected void registerGoals() {
        // 1: EntityAIControlledByPlayer -> getControllingPassenger/tickRidden below
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new FleeGoal(this, 1.0, this::isFleeing));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, stack -> stack.is(Items.APPLE), false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(6, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 1.0, 20));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SADDLED, false);
    }

    /** getSaddled of EntityGenericRideable (its own flag, not the vanilla saddle slot). */
    public boolean hasSaddle() {
        return this.entityData.get(DATA_SADDLED);
    }

    public void setSaddled(boolean saddled) {
        this.entityData.set(DATA_SADDLED, saddled);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // EntityGenericBreedable first (apple/carrot), then the saddle logic of EntityGenericRideable
        InteractionResult result = super.mobInteract(player, hand);
        if (result.consumesAction()) {
            return result;
        }
        if (this.hasSaddle()) {
            if (!this.isVehicle()) {
                if (!this.level().isClientSide()) {
                    player.startRiding(this);
                }
                return InteractionResult.SUCCESS;
            }
            return this.hasPassenger(player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (player.getItemInHand(hand).is(Items.SADDLE)) {
            if (!this.level().isClientSide()) {
                this.setSaddled(true);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    // ---- riding (EntityAIControlledByPlayer) ----

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        if (this.getFirstPassenger() instanceof Player player) {
            ItemStack held = player.getMainHandItem();
            // shouldExecute: canBeSteered (stick); continueExecuting: also shouldIgnorePlayerRot (any other item)
            if (held.is(Items.STICK) || this.steered && !held.isEmpty()) {
                return player;
            }
        }
        return super.getControllingPassenger();
    }

    @Override
    public void tick() {
        if (!(this.getControllingPassenger() instanceof Player)) {
            // resetTask
            this.steered = false;
            this.riddenSpeed = 0.0F;
        }
        super.tick();
    }

    @Override
    protected void tickRidden(Player controller, Vec3 riddenInput) {
        super.tickRidden(controller, riddenInput);
        this.steered = true;
        ItemStack held = controller.getMainHandItem();
        float turn = held.is(Items.STICK) ? Mth.clamp(Mth.wrapDegrees(controller.getYRot() - this.getYRot()) * 0.5F, -MAX_TURN, MAX_TURN) : 0.0F;
        this.setYRot(this.getYRot() + turn);
        this.yBodyRot = this.yHeadRot = this.getYRot();
        if (this.riddenSpeed < MAX_RIDDEN_SPEED) {
            this.riddenSpeed += (MAX_RIDDEN_SPEED - this.riddenSpeed) * 0.01F;
        }
        // the path-finder check for a one-block step ahead
        this.setJumping(this.horizontalCollision && this.onGround());
        // the carrot on a stick wears out while it rides along (a leftover of the pig AI)
        if (!this.level().isClientSide() && !controller.getAbilities().instabuild && this.riddenSpeed >= MAX_RIDDEN_SPEED * 0.5F
                && this.random.nextFloat() < 0.006F && held.is(Items.CARROT_ON_A_STICK)) {
            controller.setItemInHand(InteractionHand.MAIN_HAND, held.hurtAndConvertOnBreak(1, Items.FISHING_ROD, controller, EquipmentSlot.MAINHAND));
        }
    }

    @Override
    protected Vec3 getRiddenInput(Player controller, Vec3 selfInput) {
        return new Vec3(0.0, 0.0, 1.0);
    }

    /** moveEntityWithHeading(0, currentSpeed) with the horse's own move speed. */
    @Override
    protected float getRiddenSpeed(Player controller) {
        return this.riddenSpeed * (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    @Override
    public void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.getControllingPassenger() instanceof Player) {
            // the controlling task held all mutex bits: no path-finding meanwhile
            this.navigation.stop();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        // the rider cannot hurt his own mount
        if (source.getEntity() != null && this.hasPassenger(source.getEntity())
                || source.getDirectEntity() != null && this.hasPassenger(source.getDirectEntity())) {
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    // ---- breeding, sounds, save data ----

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.APPLE) || stack.is(Items.CARROT);
    }

    @Override
    public boolean canMate(Animal partner) {
        return partner.getType() == this.getType() && super.canMate(partner);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return (AgeableMob) this.getType().create(level, EntitySpawnReason.BREEDING);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ZuluHorses.HORSE_LIVING.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ZuluHorses.HORSE_HURT.get();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Saddle", this.hasSaddle());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSaddled(input.getBooleanOr("Saddle", false));
    }
}
