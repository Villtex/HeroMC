package chococraft.entity;

import chococraft.ChocoConfig;
import chococraft.ChocoCraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.FluidTags;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

/**
 * EntityChocobo with EntityChocoboRideable and the nine colour classes (the colour is the entity type, the values are
 * in {@link ChocoboColor}). Size 1.3 x 1.9, rendered at 0.8.
 * <p>
 * Gear: a saddle (tamed, no saddle, no pack bags; stops wandering and following), saddle bags on a saddle (36 slots),
 * pack bags instead of a saddle (54 slots, follows the owner). Sneak + right click opens the bags. A saddled chocobo is
 * mounted by a right click with anything the chocobo does not take. Feather: follow -> stay -> wander (follow/stay
 * only with gear, saddledCanWander=false), string: follow/stay. Loverly or golden gysahl: in love (tamed, owner, no
 * breeding cool-down), pink/red gysahl turns a gold chocobo into a pink/red one. Every 600-1200 ticks a 15 % chance
 * to lay a feather; 1-3 feathers, the gear and the bag contents on death, 0-2 legs (cooked when burning).
 * <p>
 * Riding (moveEntity of EntityChocoboRideable): the chocobo takes the rider's yaw; its horizontal motion is the
 * rider's own movement input (moveFlying with 0.02, normalised) times landSpeedFactor, or waterSpeedFactor when it
 * swims off the ground (the airborne factor of the original only applied for the one tick the tracker had not yet
 * cleared isAirBorne, i.e. practically never). Jump: the normal jump, flying colours rise by 0.1 per tick while the
 * key is held, white and black jump 0.4 higher once. Sneaking dismounts in 26.3, so flyers sink by letting go of the
 * jump key (the original sank by 0.15 with sneak). Rider effects: blue water breathing, black night vision, purple fire
 * resistance; the purple chocobo walks through lava like through air and is immune to fire. Mounted step height 1 or
 * 2. Mobs touching the ridden chocobo are pushed aside.
 */
public class Chocobo extends AbstractChocobo {
    private static final EntityDataAccessor<Byte> DATA_GEAR = SynchedEntityData.defineId(Chocobo.class, EntityDataSerializers.BYTE);
    // Constants.DW_VAL_ECR_*
    private static final int SADDLED = 1;
    private static final int SADDLE_BAGGED = 2;
    private static final int PACK_BAGGED = 4;
    /** ChocoSaddleBagInventory.INV_SIZE_SMALL / ChocoPackBagInventory.INV_SIZE_BIG. */
    public static final int SADDLE_BAG_SIZE = 36;
    public static final int PACK_BAG_SIZE = 54;
    /** landMovementFactor 0.2 with moveSpeed 0.7 of the non-AI path following: 0.14 per tick, see createAttributes. */
    private static final float LAND_MOVEMENT_FACTOR = 0.2F;

    private final ChocoboColor color;
    private @Nullable BagContainer bags;
    /** Not saved: the chocobo was made to love with a golden gysahl. */
    public boolean fedGold;
    private boolean flying;
    private boolean highJumping;
    private int timeUntilNextFeather;
    // client: wing flapping of EntityChocobo.onLivingUpdate (ModelChicken style)
    public float wingRotation;
    public float destPos;
    public float wingRotDelta = 1.0F;
    public float oWingRotation;
    public float oDestPos;

    // mating (findPlayerToAttack / attackEntity of the non-AI update)
    @Nullable Chocobo mate;
    int breeding;

    public Chocobo(EntityType<? extends Chocobo> type, Level level) {
        super(type, level);
        this.color = ChocoCraft.colorOf(type);
        this.timeUntilNextFeather = this.random.nextInt(ChocoConfig.FEATHER_DELAY_RANDOM.get()) + ChocoConfig.FEATHER_DELAY_STATIC.get();
    }

    /**
     * Health per colour (getMaxHealth). MOVEMENT_SPEED: the old path following moved with moveSpeed 0.7 and
     * landMovementFactor 0.2, an acceleration of 0.14 per tick; 26.3 accelerates by speed^2 * 1.327 (zza = speed and
     * 0.216 / 0.546^3), so 0.325. Falls hurt from 6 blocks (fall: ceil(distance - 6)).
     */
    public static AttributeSupplier.Builder createAttributes(ChocoboColor color) {
        return createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, color.health)
                .add(Attributes.MOVEMENT_SPEED, 0.325)
                .add(Attributes.SAFE_FALL_DISTANCE, 6.0)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    public ChocoboColor getColor() {
        return this.color;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new ActionGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_GEAR, (byte) 0);
    }

    private boolean gear(int bit) {
        return (this.entityData.get(DATA_GEAR) & bit) != 0;
    }

    private void setGear(int bit, boolean on) {
        byte g = this.entityData.get(DATA_GEAR);
        this.entityData.set(DATA_GEAR, (byte) (on ? g | bit : g & ~bit));
    }

    public boolean isSaddled() {
        return this.gear(SADDLED);
    }

    public void setSaddled(boolean saddled) {
        this.setGear(SADDLED, saddled);
    }

    public boolean isSaddleBagged() {
        return this.gear(SADDLE_BAGGED);
    }

    public boolean isPackBagged() {
        return this.gear(PACK_BAGGED);
    }

    /** setSaddleBagged: a new, empty bag inventory when the state changes. */
    public void setSaddleBagged(boolean on) {
        if (on != this.isSaddleBagged() || this.bags == null) {
            this.bags = on ? new BagContainer(this, SADDLE_BAG_SIZE) : null;
            this.setGear(SADDLE_BAGGED, on);
        }
    }

    public void setPackBagged(boolean on) {
        if (on != this.isPackBagged() || this.bags == null) {
            this.bags = on ? new BagContainer(this, PACK_BAG_SIZE) : null;
            this.setGear(PACK_BAGGED, on);
        }
    }

    public @Nullable SimpleContainer getBags() {
        return this.bags;
    }

    /** injectInventory: the bag contents of the chocobo this one replaces (painting). */
    public void injectBags(@Nullable SimpleContainer from) {
        if (from != null && this.bags != null) {
            for (int i = 0; i < from.getContainerSize() && i < this.bags.getContainerSize(); i++) {
                this.bags.setItem(i, from.getItem(i));
            }
        }
    }

    // ---- save data ----

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Saddle", this.isSaddled());
        output.putBoolean("SaddleBag", this.isSaddleBagged());
        output.putBoolean("PackBag", this.isPackBagged());
        if (this.bags != null) {
            NonNullList<ItemStack> items = NonNullList.withSize(this.bags.getContainerSize(), ItemStack.EMPTY);
            for (int i = 0; i < items.size(); i++) {
                items.set(i, this.bags.getItem(i));
            }
            ContainerHelper.saveAllItems(output.child("SaddleBagInventory"), items);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSaddled(input.getBooleanOr("Saddle", false));
        this.setSaddleBagged(input.getBooleanOr("SaddleBag", false));
        this.setPackBagged(input.getBooleanOr("PackBag", false));
        this.setWander(!this.isFollowing() && !this.isSaddled() && !this.isPackBagged());
        if (this.bags != null) {
            NonNullList<ItemStack> items = NonNullList.withSize(this.bags.getContainerSize(), ItemStack.EMPTY);
            input.child("SaddleBagInventory").ifPresent(child -> ContainerHelper.loadAllItems(child, items));
            for (int i = 0; i < items.size(); i++) {
                this.bags.setItem(i, items.get(i));
            }
        }
    }

    // ---- ticking ----

    @Override
    public void tick() {
        super.tick();
        if (!this.isVehicle() && this.flying && !this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.25, 0.0));
        }
        if (this.flying && this.onGround()) {
            this.flying = false;
        }
        if (this.highJumping && this.onGround()) {
            this.highJumping = false;
        }
        if (this.isInWater() && this.color.canCrossWater) {
            // buoyancy of EntityChocobo.onUpdate: five slices of the bounding box
            double minY = this.getBoundingBox().minY;
            double height = this.getBoundingBox().maxY - minY;
            double submerged = 0.0;
            for (int j = 0; j < 5; j++) {
                double y0 = minY + height * j / 5.0 - 0.125;
                double y1 = minY + height * (j + 1) / 5.0 - 0.125;
                if (this.level().getBlockStates(this.getBoundingBox().setMinY(y0).setMaxY(y1)).anyMatch(s -> s.getFluidState().is(FluidTags.WATER))) {
                    submerged += 0.2;
                }
            }
            Vec3 dm = this.getDeltaMovement();
            if (submerged < 1.0) {
                this.setDeltaMovement(dm.x, dm.y + 0.04 * (submerged * 2.0 - 1.0), dm.z);
            } else {
                this.setDeltaMovement(dm.x, (dm.y < 0.0 ? dm.y / 2.0 : dm.y) + 0.007, dm.z);
            }
        }
        // step height and the rider (setStepHeight / setRiderAbilities)
        this.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(this.isVehicle() ? this.color.mountedStepHeight : 0.5F);
    }

    @Override
    public void aiStep() {
        Entity rider = this.getFirstPassenger();
        if (rider != null && !this.level().isClientSide()) {
            // mobs touching the chocobo are pushed away
            List<Entity> near = this.level().getEntities(this, this.getBoundingBox().inflate(0.21, 0.0, 0.21));
            for (Entity e : near) {
                if (e instanceof Enemy && e.isPushable() && e != rider) {
                    e.push(this);
                }
            }
            if (rider instanceof Player player && ChocoConfig.RIDER_BUFFS_ENABLED.get()) {
                switch (this.color) {
                    case BLUE -> player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 100, 0, true, true));
                    case BLACK -> player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 100, 0, true, true));
                    case PURPLE -> {
                        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, true, true));
                        player.clearFire();
                    }
                    default -> {
                    }
                }
            }
            if (this.color == ChocoboColor.PURPLE) {
                rider.clearFire();
            }
        }
        super.aiStep();
        if (this.color == ChocoboColor.PURPLE) {
            this.clearFire();
        }
        if (this.mate != null && this.isInLove() && !this.mate.isInLove()) {
            this.mate = null;
        }
        if (this.level().isClientSide()) {
            this.oWingRotation = this.wingRotation;
            this.oDestPos = this.destPos;
            this.destPos += (this.onGround() ? -1.0F : 4.0F) * 0.3F;
            this.destPos = Mth.clamp(this.destPos, 0.0F, 1.0F);
            if (!this.onGround()) {
                this.wingRotDelta = Math.max(this.wingRotDelta, 1.0F);
            }
            this.wingRotDelta *= 0.9F;
            this.wingRotation += this.wingRotDelta * 2.0F;
        } else if (--this.timeUntilNextFeather <= 0) {
            if (this.random.nextInt(100) < ChocoConfig.FEATHER_DROP_CHANCE.get()) {
                this.spawnAtLocation((ServerLevel) this.level(), new ItemStack(ChocoCraft.CHOCOBO_FEATHER.get()));
            }
            this.timeUntilNextFeather = this.random.nextInt(ChocoConfig.FEATHER_DELAY_RANDOM.get()) + ChocoConfig.FEATHER_DELAY_STATIC.get();
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        int prob = ChocoConfig.LIVING_SOUND_PROBABILITY.get();
        return prob != 0 && this.random.nextInt(4) == 0 && this.random.nextInt(100) < prob ? ChocoCraft.KWEH.get() : null;
    }

    // ---- movement ----

    @Override
    protected boolean shouldTravelInFluid(FluidState fluid) {
        // EntityChocoboPurple.handleLavaMovement() = false: lava is no fluid for it
        if (this.color == ChocoboColor.PURPLE && fluid.is(FluidTags.LAVA)) {
            return false;
        }
        return super.shouldTravelInFluid(fluid);
    }

    @Override
    public void travel(Vec3 input) {
        if (this.getControllingPassenger() instanceof Player rider && this.isAlive()) {
            this.travelMounted(rider);
            return;
        }
        if (this.isInWater() && this.color.canCrossWater) {
            // moveEntityWithHeading in water: landMovementFactor * 0.3 for the swimmers
            this.moveRelative(LAND_MOVEMENT_FACTOR * 0.3F, input);
            this.move(MoverType.SELF, this.getDeltaMovement());
            Vec3 dm = this.getDeltaMovement().scale(0.8);
            if (this.horizontalCollision && this.isFree(dm.x, dm.y + 0.6, dm.z)) {
                dm = new Vec3(dm.x, 0.3, dm.z);
            }
            this.setDeltaMovement(dm);
            return;
        }
        if (this.flying && !this.onGround()) {
            this.moveRelative(this.color.flyMovementFactor, input);
            this.move(MoverType.SELF, this.getDeltaMovement());
            Vec3 dm = this.getDeltaMovement().scale(0.8);
            this.setDeltaMovement(dm.x, dm.y - 0.02, dm.z);
            return;
        }
        super.travel(input);
    }

    /** moveEntity / moveEntityWithHeading while a player rides: see the class comment. */
    private void travelMounted(Player rider) {
        this.setYRot(rider.getYRot());
        this.yRotO = this.getYRot();
        this.setXRot(0.0F);
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
        float strafe = rider.xxa;
        float forward = rider.zza;
        double vx = 0.0;
        double vz = 0.0;
        float len = strafe * strafe + forward * forward;
        if (len >= 1.0E-4F) {
            len = Mth.sqrt(len);
            if (len < 1.0F) {
                len = 1.0F;
            }
            len = 0.02F / len;
            strafe *= len;
            forward *= len;
            float sin = Mth.sin(this.getYRot() * Mth.DEG_TO_RAD);
            float cos = Mth.cos(this.getYRot() * Mth.DEG_TO_RAD);
            vx = strafe * cos - forward * sin;
            vz = forward * cos + strafe * sin;
        }
        boolean inWater = this.isInWater();
        double factor = !this.onGround() && inWater ? this.color.waterSpeedFactor : this.color.landSpeedFactor;
        double vy = this.getDeltaMovement().y;
        boolean jump = rider.isJumping();
        if (jump) {
            // isJumping = rider.isJumping: the normal jump on the ground, a little lift in water
            if (inWater || this.isInLava() && this.color != ChocoboColor.PURPLE) {
                vy += 0.04;
            } else if (this.onGround()) {
                vy = this.getJumpPower();
            }
            if (this.color.canFly) {
                vy += 0.1;
                this.flying = true;
            } else if (this.color.canJumpHigh && !this.highJumping) {
                vy += 0.4;
                this.highJumping = true;
            }
        }
        this.move(MoverType.SELF, new Vec3(vx * factor, vy, vz * factor));
        Vec3 moved = new Vec3(vx * factor, vy, vz * factor);
        if (inWater) {
            moved = moved.scale(0.8);
            if (this.horizontalCollision && this.isFree(moved.x, moved.y + 0.6, moved.z)) {
                moved = new Vec3(moved.x, 0.3, moved.z);
            }
        } else if (this.isInLava() && this.color != ChocoboColor.PURPLE) {
            moved = moved.scale(0.5).add(0.0, -0.02, 0.0);
        } else if (this.flying) {
            moved = moved.scale(0.8).add(0.0, -0.02, 0.0);
        } else {
            moved = new Vec3(moved.x * 0.546, (moved.y - 0.08) * 0.98, moved.z * 0.546);
        }
        this.setDeltaMovement(moved);
        this.calculateEntityAnimation(false);
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        return this.isSaddled() && this.getFirstPassenger() instanceof Player player ? player : super.getControllingPassenger();
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return !this.color.ignoresFalls() && super.causeFallDamage(distance, multiplier, source);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return this.color == ChocoboColor.BLUE;
    }

    // ---- interaction ----

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult result = this.interactCommon(player, hand);
        if (result != null) {
            return result;
        }
        ItemStack stack = player.getItemInHand(hand);
        boolean client = this.level().isClientSide();
        // EntityChocoboRideable.interact
        if (player.isShiftKeyDown() && (this.isSaddleBagged() || this.isPackBagged())) {
            return this.openBags(player);
        }
        if (stack.is(ChocoCraft.CHOCOBO_SADDLE.get())) {
            if (!client && this.isTame() && !this.isSaddled() && !this.isPackBagged()) {
                this.setSaddled(true);
                this.setWander(false);
                this.setFollowing(false);
                this.useItem(player, stack);
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.is(ChocoCraft.CHOCOBO_SADDLE_BAGS.get())) {
            if (!client && this.isTame() && this.isSaddled() && !this.isSaddleBagged()) {
                this.setSaddleBagged(true);
                this.useItem(player, stack);
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.is(ChocoCraft.CHOCOBO_PACK_BAGS.get())) {
            if (!client && this.isTame() && !this.isSaddled() && !this.isPackBagged()) {
                this.setPackBagged(true);
                this.setWander(false);
                this.setFollowing(true);
                this.useItem(player, stack);
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.is(ChocoCraft.CHOCOBO_WHISTLE.get())) {
            // onWhistleUse did nothing
            return InteractionResult.SUCCESS;
        }
        // EntityChocobo.interact
        if (!stack.isEmpty()) {
            if (stack.is(ChocoCraft.GYSAHL_PINK.get()) || stack.is(ChocoCraft.GYSAHL_RED.get())) {
                if (!client) {
                    this.onPaint(player, stack, stack.is(ChocoCraft.GYSAHL_PINK.get()));
                }
                return InteractionResult.SUCCESS;
            }
            boolean golden = stack.is(ChocoCraft.GYSAHL_GOLDEN.get());
            if (golden || stack.is(ChocoCraft.GYSAHL_LOVERLY.get())) {
                if (!client) {
                    this.onBreedGysahlUse(player, stack, golden);
                }
                return InteractionResult.SUCCESS;
            }
            if (stack.is(ChocoCraft.CHOCOBO_FEATHER.get())) {
                if (!client) {
                    if (this.isTame() && this.isOwner(player)) {
                        this.toggleFollowWanderStay();
                    } else {
                        this.particles(ParticleTypes.SMOKE, 7);
                    }
                }
                return InteractionResult.SUCCESS;
            }
            if (stack.is(Items.STRING)) {
                if (!client) {
                    if (this.isTame() && this.isOwner(player)) {
                        this.toggleFollowStay();
                    } else {
                        this.particles(ParticleTypes.SMOKE, 7);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        // onEmptyHandInteraction (reached with any item nothing above took): mount a saddled chocobo
        if (this.isSaddled() && !this.isVehicle()) {
            if (!client) {
                player.setSprinting(false);
                player.startRiding(this);
            }
            return InteractionResult.SUCCESS;
        }
        return player.isShiftKeyDown() ? InteractionResult.SUCCESS : super.mobInteract(player, hand);
    }

    private InteractionResult openBags(Player player) {
        if (!this.level().isClientSide() && this.bags != null) {
            BagContainer bag = this.bags;
            int rows = bag.getContainerSize() / 9;
            player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new ChestMenu(rows == 4 ? MenuType.GENERIC_9x4 : MenuType.GENERIC_9x6, id, inventory, bag, rows), Component.literal(this.getChocoboName() + " saddle bags")));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void toggleFollowWanderStay() {
        if (ChocoConfig.SADDLED_CAN_WANDER.get() || !this.isSaddled() && !this.isPackBagged()) {
            super.toggleFollowWanderStay();
        } else {
            this.toggleFollowStay();
        }
    }

    private void onBreedGysahlUse(Player player, ItemStack stack, boolean golden) {
        if (this.isTame() && this.getAge() == 0 && this.isOwner(player)) {
            this.fedGold = golden;
            this.useItem(player, stack);
            this.setInLove(true);
            this.mate = null;
            this.particles(ParticleTypes.HEART, 7);
        } else {
            this.particles(ParticleTypes.SMOKE, 7);
        }
    }

    /** onPaint: only the gold chocobo turns into a pink or red one; the others only puff. The gysahl is used up. */
    private void onPaint(Player player, ItemStack stack, boolean pink) {
        if (this.color == ChocoboColor.GOLD) {
            Chocobo painted = ChocoCraft.chocoboType(pink ? ChocoboColor.PINK : ChocoboColor.RED).create(this.level(),
                    net.minecraft.world.entity.EntitySpawnReason.CONVERSION);
            if (painted != null) {
                this.copyStateTo(painted);
                painted.setAge(this.getAge());
                painted.setSaddled(this.isSaddled());
                painted.setSaddleBagged(this.isSaddleBagged());
                painted.setPackBagged(this.isPackBagged());
                painted.setWander(this.isWander());
                painted.setFollowing(this.isFollowing());
                painted.injectBags(this.bags);
                this.level().addFreshEntity(painted);
                if (this.bags != null) {
                    this.bags.clearContent();
                }
                this.particles(ParticleTypes.POOF, 20);
                this.discard();
            }
        } else {
            this.particles(ParticleTypes.SMOKE, 7);
        }
        this.useItem(player, stack);
    }

    /** FactoryEntityChocobo.createChocobo: name, owner, name hidden, tamed, following, wander, gender, position. */
    public void copyStateTo(AbstractChocobo other) {
        other.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
        other.setChocoboName(this.getChocoboName());
        other.setOwnerReference(this.getOwnerReference());
        other.setOwnerName(this.getOwnerName());
        other.setHidename(this.isHidename());
        other.setTame(this.isTame(), false);
        other.setFollowing(this.isFollowing());
        other.setWander(this.isWander());
        other.setIsMale(this.isMale());
    }

    /** sendDropSaddleAndBags (Chocopedia "Drop Gear"): the pack bags went without their contents in the original. */
    public void dropGear() {
        if (!(this.level() instanceof ServerLevel level) || this.isVehicle()) {
            return;
        }
        if (this.isSaddleBagged()) {
            this.spawnAtLocation(level, new ItemStack(ChocoCraft.CHOCOBO_SADDLE_BAGS.get()));
            this.dropBagContents(level);
            this.setSaddleBagged(false);
        }
        if (this.isSaddled()) {
            this.spawnAtLocation(level, new ItemStack(ChocoCraft.CHOCOBO_SADDLE.get()));
            this.setSaddled(false);
        }
        if (this.isPackBagged()) {
            this.spawnAtLocation(level, new ItemStack(ChocoCraft.CHOCOBO_PACK_BAGS.get()));
            this.setPackBagged(false);
        }
    }

    private void dropBagContents(ServerLevel level) {
        if (this.bags != null) {
            for (int i = 0; i < this.bags.getContainerSize(); i++) {
                ItemStack stack = this.bags.removeItemNoUpdate(i);
                if (!stack.isEmpty()) {
                    this.spawnAtLocation(level, stack);
                }
            }
        }
    }

    // ---- death ----

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        // EntityChocoboRideable.onDeath: the gear and the bags; EntityChocobo.onDeath: one to three feathers
        if (this.isSaddled()) {
            this.spawnAtLocation(level, new ItemStack(ChocoCraft.CHOCOBO_SADDLE.get()));
        }
        if (this.isSaddleBagged()) {
            this.spawnAtLocation(level, new ItemStack(ChocoCraft.CHOCOBO_SADDLE_BAGS.get()));
            this.dropBagContents(level);
        }
        if (this.isPackBagged()) {
            this.spawnAtLocation(level, new ItemStack(ChocoCraft.CHOCOBO_PACK_BAGS.get()));
            this.dropBagContents(level);
        }
        int feathers = this.random.nextInt(3);
        for (int i = 0; i <= feathers; i++) {
            this.spawnAtLocation(level, new ItemStack(ChocoCraft.CHOCOBO_FEATHER.get()));
        }
    }

    // ---- breeding ----

    @Override
    protected void onHurtStopMating() {
        this.mate = null;
    }

    /** procreate: a wild chicobo of the colour of the table; purple chocobos lay one egg (one in ten: three). */
    void procreate(Chocobo other) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.color == ChocoboColor.PURPLE) {
            this.setAge(this.isMale() ? 3000 : 9000);
            other.setAge(other.isMale() ? 3000 : 9000);
            this.endMating(other);
            int eggs = this.random.nextInt(10) == 0 ? 3 : 1;
            this.spawnAtLocation(level, new ItemStack(ChocoCraft.PURPLE_CHOCOBO_EGG.get(), eggs));
            return;
        }
        ChocoboColor babyColor = ChocoboBreeding.babyColor(this.color, other.color, this.fedGold && other.fedGold, this.random);
        Chicobo baby = ChocoCraft.CHICOBO.get().create(level, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
        if (baby != null) {
            baby.setColor(babyColor);
            baby.setTimeUntilAdult(this.random.nextInt(ChocoConfig.GROWUP_DELAY_RANDOM.get()) + ChocoConfig.GROWUP_DELAY_STATIC.get());
            baby.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            level.addFreshEntity(baby);
            this.setAge(this.isMale() ? ChocoConfig.BREEDING_DELAY_MALE.get() : ChocoConfig.BREEDING_DELAY_FEMALE.get());
            other.setAge(other.isMale() ? ChocoConfig.BREEDING_DELAY_MALE.get() : ChocoConfig.BREEDING_DELAY_FEMALE.get());
            this.endMating(other);
            this.particles(ParticleTypes.HEART, 7);
        }
    }

    private void endMating(Chocobo other) {
        this.mate = null;
        other.mate = null;
        this.breeding = 0;
        other.breeding = 0;
        this.setInLove(false);
        other.setInLove(false);
    }

    /** findPlayerToAttack: an adult chocobo of the other gender in love within 8 blocks. */
    @Nullable Chocobo findMate() {
        if (this.fleeingTick > 0 || !this.isInLove()) {
            return null;
        }
        for (Chocobo other : this.level().getEntitiesOfClass(Chocobo.class, this.getBoundingBox().inflate(8.0))) {
            if (other != this && other.isInLove() && other.isMale() != this.isMale()) {
                return other;
            }
        }
        return null;
    }

    /** attackEntity between two chocobos in love: 60 ticks within 5 blocks of each other make a chicobo. */
    void courtMate(Chocobo other, float distance) {
        if (this.isInLove() && other.isInLove()) {
            if (other.mate == null) {
                other.mate = this;
            }
            if (other.mate == this && distance < 5.0F) {
                other.setInLove(true);
                this.setInLove(true);
                this.breeding++;
                if (this.breeding % 4 == 0) {
                    this.particles(ParticleTypes.HEART, 1);
                }
                if (this.breeding == 60) {
                    this.procreate(other);
                }
            } else {
                other.mate = null;
                this.breeding = 0;
            }
        } else {
            this.breeding = 0;
        }
    }

    /** EntityChocobo.updateEntityActionState with the path following of EntityAnimalChocobo, as one goal. */
    static final class ActionGoal extends Goal {
        private final Chocobo chocobo;

        ActionGoal(Chocobo chocobo) {
            this.chocobo = chocobo;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return !this.chocobo.isVehicle();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            Chocobo c = this.chocobo;
            boolean hasPath = !c.getNavigation().isDone();
            if (!hasPath && c.isFollowing() && c.isTame()) {
                Player owner = c.getOwnerPlayer();
                if (owner != null) {
                    if (!owner.isAlive()) {
                        c.setFollowing(false);
                        c.setWander(false);
                        return;
                    }
                    float distance = owner.distanceTo(c);
                    if (distance > 10.0F) {
                        // getPathOrWalkableBlock
                        if (!c.getNavigation().moveTo(owner, 1.0) && distance > 12.0F) {
                            c.teleportToOwner();
                        }
                        return;
                    }
                }
            } else if (c.isSaddled() && !c.isFollowing() && !c.isInLove()) {
                // a saddled chocobo that does not follow stands still
                return;
            }
            this.update(c);
        }

        /** EntityAnimalChocobo.updateEntityActionState. */
        private void update(Chocobo c) {
            if (c.mate == null) {
                c.mate = c.findMate();
                if (c.mate != null) {
                    c.getNavigation().moveTo(c.mate, 1.0);
                }
            } else if (!c.mate.isAlive()) {
                c.mate = null;
            } else if (c.hasLineOfSight(c.mate)) {
                c.courtMate(c.mate, c.mate.distanceTo(c));
            }
            boolean pathDone = c.getNavigation().isDone();
            if (c.mate == null || !pathDone && c.getRandom().nextInt(20) != 0) {
                if (c.isWander() && (pathDone && c.getRandom().nextInt(180) == 0 || c.getRandom().nextInt(120) == 0 || c.fleeingTick > 0)
                        && c.getNoActionTime() < 100) {
                    // updateWanderPath: a spot up to 6 blocks away, 3 up or down
                    double x = Mth.floor(c.getX() + c.getRandom().nextInt(13) - 6.0);
                    double y = Mth.floor(c.getY() + c.getRandom().nextInt(7) - 3.0);
                    double z = Mth.floor(c.getZ() + c.getRandom().nextInt(13) - 6.0);
                    c.getNavigation().moveTo(x, y, z, 1.0);
                }
            } else if (c.mate != null) {
                c.getNavigation().moveTo(c.mate, 1.0);
            }
            if (c.mate != null) {
                c.getLookControl().setLookAt(c.mate, 30.0F, 30.0F);
            }
            if (!c.getNavigation().isDone() && c.getRandom().nextInt(100) == 0) {
                c.getNavigation().stop();
            }
            if (c.getRandom().nextFloat() < 0.8F && (c.isInWater() || c.isInLava())) {
                c.getJumpControl().jump();
            }
        }
    }

    /** SimpleContainer of the bags: usable within 8 blocks of the living chocobo (ChocoBagInventory.isUseableByPlayer). */
    static final class BagContainer extends SimpleContainer {
        private final Chocobo chocobo;

        BagContainer(Chocobo chocobo, int size) {
            super(size);
            this.chocobo = chocobo;
        }

        @Override
        public boolean stillValid(Player player) {
            return this.chocobo.isAlive() && player.distanceToSqr(this.chocobo) <= 64.0;
        }
    }

    /** Container view for tests and the Chocopedia. */
    public @Nullable Container bagContainer() {
        return this.bags;
    }
}
