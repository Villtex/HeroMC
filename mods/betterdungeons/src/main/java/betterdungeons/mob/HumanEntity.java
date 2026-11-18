package betterdungeons.mob;

import betterdungeons.BDItems;
import betterdungeons.api.BDCommandableMob;
import betterdungeons.api.BDDungeonMob;
import betterdungeons.api.BDTeamMob;
import betterdungeons.item.BigSwordItem;
import betterdungeons.item.DaggerItem;
import betterdungeons.item.HookShotItem;
import betterdungeons.item.RangedItems;
import betterdungeons.item.SpearItem;
import betterdungeons.mob.ai.HumanGoals;
import betterdungeons.mob.ai.HumanTargetGoals;
import betterdungeons.projectile.BDMagicArrow;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * mobs.EntityHuman of 1.5.2: the armed biped of Better Dungeons with level 0..4 (equipment by level), a class
 * (defender, grenadier, healer, archer, gunner, bannerman, berserker, assassin, spearman, hooker), a team ("type": the
 * same type never fights, rivals are attacked on sight), leaders and captains, riding, talking and sitting, and the
 * mercenary orders of the controller (AIMode, standing position, path).
 *
 * <p>The 1.5.2 constructors map to {@link #initAsWorld()} (EntityHuman(world): eggs, /summon, vanilla spawners and
 * templates), {@link #initAsLevel(int)} (EntityHuman(world, lvl)) and {@link #initAsLevelType(int, int)}; the dungeon
 * spawner reaches them through {@link BDDungeonMob#initDungeonMob}. The second hand item (leftHandItem) is the off hand.
 * Speeds of the goals are the absolute 1.5.2 values, divided by the movement speed attribute ({@link #speed}).
 */
public class HumanEntity extends PathfinderMob implements BDDungeonMob, BDCommandableMob, BDTeamMob {
    static final EntityDataAccessor<Byte> DATA_CLASS = SynchedEntityData.defineId(HumanEntity.class, EntityDataSerializers.BYTE);
    static final EntityDataAccessor<Byte> DATA_ANIM = SynchedEntityData.defineId(HumanEntity.class, EntityDataSerializers.BYTE);
    static final EntityDataAccessor<Byte> DATA_LVL = SynchedEntityData.defineId(HumanEntity.class, EntityDataSerializers.BYTE);

    public static final int DEFENDER = 1;
    public static final int GRENADIER = 2;
    public static final int HEALER = 3;
    public static final int ARCHER = 4;
    public static final int GUNNER = 5;
    public static final int BANNERMAN = 6;
    public static final int BERSERKER = 7;
    public static final int ASSASSIN = 8;
    public static final int SPEARMAN = 9;
    public static final int HOOKER = 10;

    // ---------------------------------------------------------------- configRaze fields
    /** canDespawn() was !despawn: true keeps the mob (the default), false lets it despawn (spawner mobs, see configRaze). */
    protected boolean despawn = true;
    protected boolean canMount = false;
    public boolean renderEars = false;
    /** Always drawn sneaking (goblins, monkeys). */
    public boolean sneak = false;
    public int shiedID;
    public float randomHeightVariation = 1.0F;
    public int attackStrength = 4;
    public int attackSpeed = 25;
    public float arrowAccuracy = 8.0F;
    protected float baseMoveSpeed = 0.25F;
    protected int shiedBlockDefense = 3;
    protected int blockRate = 5;
    public int leaderShip = 0;
    public int range = 25;
    protected String @Nullable [] rivalHumans;
    public int grenadeType = 2;
    /** entityType of 1.5.2 ("pirate", "zombie", "friend.Player"): the team. */
    protected @Nullable String humanType;
    /** experienceValue. */
    protected int experienceValue = 5;

    // ---------------------------------------------------------------- state
    /** attackTime of 1.5.2 EntityLiving: counted down every tick and again by the attack goals. */
    public int attackTime;
    protected @Nullable LivingEntity owner;
    private @Nullable String ownerPlayerName;
    private @Nullable Goal attackAI;
    private @Nullable Goal rangedAI;
    private @Nullable Goal supportAI;
    private @Nullable Goal healerTargetAI;
    /** EnumAiState id (FOLLOW 0, WARD 1, HOLD 2, PATH 3, SIT 4), set by the controller screen. */
    public int AIMode = 0;
    public @Nullable Vec3 standingPosition;
    public float standingYaw;
    public @Nullable List<Vec3> path;
    private boolean isFirstTick = true;
    private int extraTicks;

    public HumanEntity(EntityType<? extends HumanEntity> type, Level level) {
        super(type, level);
        this.configRaze();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(this.maxHealthValue());
        this.setHealth(this.getMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.baseMoveSpeed);
        if (this.getNavigation() instanceof GroundPathNavigation nav) {
            nav.setCanOpenDoors(true);
        }
        this.setlvl(-1);
        this.initCreature();
        this.addAITasks();
        if (!level.isClientSide()) {
            this.initAsWorld();
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 2.0).add(Attributes.FOLLOW_RANGE, 16.0).add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CLASS, (byte) 0);
        builder.define(DATA_ANIM, (byte) 0);
        builder.define(DATA_LVL, (byte) 0);
    }

    /** The race values of 1.5.2 (called again at the end of initCreature, after the level is set). */
    protected void configRaze() {
        this.renderEars = false;
        this.sneak = false;
        this.attackStrength = 2;
        this.attackSpeed = 25;
        this.experienceValue = 5;
        this.baseMoveSpeed = 0.25F;
    }

    /** getMaxHealth of 1.5.2. */
    protected int maxHealthValue() {
        return 20;
    }

    /** The speed modifier of a goal for an absolute 1.5.2 AI speed. */
    public double speed(double absolute) {
        return absolute / this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
    }

    public float getBaseMoveSpeed() {
        return this.baseMoveSpeed;
    }

    // ---------------------------------------------------------------- the 1.5.2 constructors

    /** EntityHuman(world). */
    public void initAsWorld() {
        this.resetClass();
        this.setlvl(-1);
        this.initCreature();
        this.setRandomType();
    }

    /** EntityHuman(world, lvl). */
    public void initAsLevel(int lvl) {
        this.resetClass();
        this.setlvl(lvl);
        this.despawn = false;
        this.initCreature();
        this.setRandomType();
    }

    /** EntityHuman(world, lvl, type). */
    public void initAsLevelType(int lvl, int type) {
        this.resetClass();
        if (lvl > -1) {
            this.setlvl(lvl);
            this.despawn = false;
        } else {
            this.setRandomLevel();
        }
        this.initCreature();
        this.setEntityClass(type);
        if (this.getEntityClass() == 0) {
            this.setRandomType();
        }
    }

    @Override
    public void initDungeonMob(int level, int type) {
        if (type < 0) {
            this.initAsLevel(level);
        } else {
            this.initAsLevelType(level, type);
        }
        this.setHealth(this.getMaxHealth());
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData data) {
        // the world constructor again: /summon and spawners load the entity from its id before (no equipment)
        this.initAsWorld();
        this.setHealth(this.getMaxHealth());
        return data;
    }

    public void initCreature() {
        this.randomHeightVariation = 1.0F + (this.random.nextFloat() - 0.6F) / 5.0F;
        if (!this.level().isClientSide()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, orEmpty(this.getEquipedWeapon()));
            this.setItemSlot(EquipmentSlot.FEET, orEmpty(this.getArmor(1)));
            this.setItemSlot(EquipmentSlot.LEGS, orEmpty(this.getArmor(2)));
            this.setItemSlot(EquipmentSlot.CHEST, orEmpty(this.getArmor(3)));
            this.setItemSlot(EquipmentSlot.HEAD, orEmpty(this.getArmor(4)));
        }
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST,
                EquipmentSlot.HEAD}) {
            this.setDropChance(slot, 0.001F);
        }
        // the second hand item was a field of its own and never dropped
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        this.leaderShip = this.getlvl();
        this.configRaze();
        this.xpReward = this.experienceValue;
    }

    static ItemStack orEmpty(@Nullable ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack;
    }

    // ---------------------------------------------------------------- AI

    @Override
    protected void registerGoals() {
        // addAITasks, from the constructor: the goals need the fields of configRaze
    }

    protected void addAITasks() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.attackAI = new HumanGoals.MeleeAttack(this, LivingEntity.class, this.baseMoveSpeed, false);
        this.goalSelector.addGoal(3, this.attackAI);
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, this.speed(this.baseMoveSpeed)));
        this.goalSelector.addGoal(4, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(5, new HumanGoals.FollowOwner(this, 0.25F * 1.5F, 8.0F, 50.0F, false));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, this.speed(this.baseMoveSpeed / 3.0F)));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new HumanGoals.TalkClosest(this, HumanEntity.class, 8.0F));
        this.goalSelector.addGoal(8, new HumanGoals.IdleSit(this));
        this.targetSelector.addGoal(1, new HumanTargetGoals.HurtBy(this));
        this.targetSelector.addGoal(2, new HumanTargetGoals.OwnerTarget(this));
        this.targetSelector.addGoal(2, new HumanTargetGoals.NearestAttackable(this, Player.class, this.range, true));
        this.targetSelector.addGoal(2, new HumanTargetGoals.NearestAttackable(this, Villager.class, this.range, true));
        if (this.rivalHumans != null) {
            this.targetSelector.addGoal(2, new HumanTargetGoals.NearestRival(this, HumanEntity.class, this.range, true, this.rivalHumans, false));
        }
        this.goalSelector.addGoal(1, new HumanGoals.Mount(this, this.baseMoveSpeed, false));
        this.targetSelector.addGoal(2, new HumanTargetGoals.NearestMount(this, this.getMountClass(), 15.0F));
    }

    public void setRangedAI(int priority, @Nullable Goal goal) {
        if (this.rangedAI != null) {
            this.goalSelector.removeGoal(this.rangedAI);
        }
        this.rangedAI = goal;
        if (goal != null) {
            this.goalSelector.addGoal(priority, goal);
        }
    }

    public void setAttackAI(int priority, @Nullable Goal goal) {
        if (this.attackAI != null) {
            this.goalSelector.removeGoal(this.attackAI);
        }
        this.attackAI = goal;
        if (goal != null) {
            this.goalSelector.addGoal(priority, goal);
        }
    }

    public void setSupportAI(int priority, @Nullable Goal goal) {
        if (this.supportAI != null) {
            this.goalSelector.removeGoal(this.supportAI);
        }
        this.supportAI = goal;
        if (goal != null) {
            this.goalSelector.addGoal(priority, goal);
        }
    }

    /** The healer's AITargetNearestHurtAlly (setHealer added one more each time; here it is replaced). */
    protected void setHealerTarget(@Nullable Goal goal) {
        if (this.healerTargetAI != null) {
            this.targetSelector.removeGoal(this.healerTargetAI);
        }
        this.healerTargetAI = goal;
        if (goal != null) {
            this.targetSelector.addGoal(0, goal);
        }
    }

    // ---------------------------------------------------------------- update

    @Override
    public void aiStep() {
        if (this.isFirstTick) {
            this.isFirstTick = false;
            this.leaderShip = this.getlvl();
            if (this.getVehicle() != null) {
                this.onMounted(this.getVehicle());
            }
        }
        if (this.attackTime > 0) {
            this.attackTime--;
        }
        if (!this.level().isClientSide()) {
            if (this.getVehicle() != null) {
                this.extraTicks++;
                this.setMountAI();
            }
            if ((this.tickCount + this.extraTicks) % 100 == 0) {
                this.getLeader();
            }
            if (this.isAssasin()) {
                LivingEntity target = this.getTarget();
                this.setShiftKeyDown(target != null && BDMobUtil.rotationDiff(this, target) > 110.0);
            }
        }
        super.aiStep();
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.02, 0.0));
        this.resetFallDistance();
    }

    /** getSpeedModifier of 1.5.2: blocking or aiming slows to 70 % (on top of the potions of the attribute). */
    public float speedFactor() {
        return !this.isDefending() && !this.isAiming() ? 1.0F : 0.7F;
    }

    @Override
    public void setSpeed(float speed) {
        super.setSpeed(speed * this.speedFactor());
    }

    @Override
    public int getAmbientSoundInterval() {
        return 450;
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return !this.despawn;
    }

    // ---------------------------------------------------------------- save

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("despawn", this.despawn);
        if (this.owner instanceof Player player) {
            output.putString("playerOwner", player.getGameProfile().name());
        } else if (this.ownerPlayerName != null) {
            output.putString("playerOwner", this.ownerPlayerName);
        }
        output.putByte("lvl", (byte) this.getlvl());
        output.putInt("class", this.getEntityClass());
        output.putInt("AIMode", this.AIMode);
        if (this.standingPosition != null) {
            output.putBoolean("standing", true);
            output.putDouble("standX", this.standingPosition.x);
            output.putDouble("standY", this.standingPosition.y);
            output.putDouble("standZ", this.standingPosition.z);
            output.putFloat("standRotation", this.standingYaw);
        }
        if (this.path != null) {
            output.putInt("pathPoints", this.path.size());
            for (int p = 0; p < this.path.size(); p++) {
                output.putDouble("pathX" + p, this.path.get(p).x);
                output.putDouble("pathY" + p, this.path.get(p).y);
                output.putDouble("pathZ" + p, this.path.get(p).z);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        // the equipment of the entity (with its off hand) is read by super; the class below re-equips like 1.5.2
        super.readAdditionalSaveData(input);
        ItemStack main = this.getItemBySlot(EquipmentSlot.MAINHAND);
        ItemStack off = this.getItemBySlot(EquipmentSlot.OFFHAND);
        this.despawn = input.getBooleanOr("despawn", this.despawn);
        String player = input.getStringOr("playerOwner", "");
        if (!player.isEmpty()) {
            this.ownerPlayerName = player;
            if (this.humanType != null && !this.humanType.contains(".")) {
                this.humanType = this.humanType + "." + player;
            } else if (this.humanType == null) {
                this.humanType = "null." + player;
            }
        }
        if (input.getInt("lvl").isPresent()) {
            this.setlvl(input.getIntOr("lvl", 0));
            this.leaderShip = this.getlvl();
            this.configRaze();
            this.xpReward = this.experienceValue;
        }
        if (input.getInt("class").isPresent()) {
            this.resetClass();
            this.setEntityClass(input.getIntOr("class", 0));
            // a saved entity keeps what it carried (setEntityClass of 1.5.2 re-equipped the class items, the same here)
            if (this.getEntityClass() == 0) {
                this.setItemSlot(EquipmentSlot.MAINHAND, main);
                this.setItemSlot(EquipmentSlot.OFFHAND, off);
            }
        }
        this.AIMode = input.getIntOr("AIMode", 0);
        if (input.getBooleanOr("standing", false)) {
            this.standingPosition = new Vec3(input.getDoubleOr("standX", 0), input.getDoubleOr("standY", 0), input.getDoubleOr("standZ", 0));
            this.standingYaw = input.getFloatOr("standRotation", 0.0F);
        }
        int points = input.getIntOr("pathPoints", 0);
        if (points > 0) {
            List<Vec3> p = new ArrayList<>();
            for (int i = 0; i < points; i++) {
                p.add(new Vec3(input.getDoubleOr("pathX" + i, 0), input.getDoubleOr("pathY" + i, 0), input.getDoubleOr("pathZ" + i, 0)));
            }
            this.path = p;
        }
    }

    // ---------------------------------------------------------------- interact (creative: give the mob the held item)

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.getAbilities().instabuild && this.interactEquip(player, hand, false)) {
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    private void dropStack(ItemStack stack) {
        if (!stack.isEmpty() && this.level() instanceof ServerLevel server) {
            this.spawnAtLocation(server, stack.copy());
        }
    }

    private void dropMain(boolean drop) {
        if (drop) {
            this.dropStack(this.getItemBySlot(EquipmentSlot.MAINHAND));
        }
    }

    private void dropOff(boolean drop) {
        if (drop) {
            this.dropStack(this.getItemBySlot(EquipmentSlot.OFFHAND));
        }
    }

    /** interact(player, dropItems): the class follows the item. */
    public boolean interactEquip(Player player, InteractionHand hand, boolean drop) {
        ItemStack is = player.getItemInHand(hand);
        if (is.isEmpty() || this.level().isClientSide()) {
            return !is.isEmpty();
        }
        boolean ret = false;
        ItemStack copy = is.copy();
        if (is.is(ItemTags.SWORDS)) {
            this.dropMain(drop);
            this.setItemSlot(EquipmentSlot.MAINHAND, copy);
            ret = true;
        }
        var equippable = is.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            if (drop) {
                this.dropStack(this.getItemBySlot(equippable.slot()));
            }
            this.setItemSlot(equippable.slot(), copy);
            ret = true;
        }
        if (is.getItem() instanceof BowItem) {
            this.dropMain(drop);
            this.resetClass();
            this.setArcher(true);
            ret = true;
        }
        if (is.getItem() instanceof RangedItems.Pistol) {
            this.dropOff(drop);
            this.resetClass();
            this.setGunner(true);
            ret = true;
        }
        if (BDItems.SHIELDS.stream().anyMatch(s -> is.is(s.get()))) {
            this.dropOff(drop);
            this.resetClass();
            this.setShied(true);
            this.setItemSlot(EquipmentSlot.OFFHAND, copy);
            ret = true;
        }
        if (is.is(BDItems.STAFF_LIFE.get())) {
            this.dropMain(drop);
            this.resetClass();
            this.setHealer(true);
            ret = true;
        }
        if (is.getItem() instanceof RangedItems.Grenade) {
            this.dropOff(drop);
            this.resetClass();
            this.setGrenade(true);
            this.setItemSlot(EquipmentSlot.OFFHAND, copy);
            ret = true;
        }
        if (BDItems.BANNERS.stream().anyMatch(s -> is.is(s.get()))) {
            this.dropMain(drop);
            this.dropOff(drop);
            this.resetClass();
            this.setBannerman(true);
            this.setItemSlot(EquipmentSlot.MAINHAND, copy);
            ret = true;
        }
        if (is.getItem() instanceof BigSwordItem) {
            this.dropMain(drop);
            this.dropOff(drop);
            this.resetClass();
            this.setBerseker(true);
            this.setItemSlot(EquipmentSlot.MAINHAND, copy);
            ret = true;
        }
        if (is.getItem() instanceof DaggerItem.Ninja) {
            this.dropMain(drop);
            this.resetClass();
            this.setAssasin(true);
            this.setItemSlot(EquipmentSlot.MAINHAND, copy);
            ret = true;
        }
        if (is.getItem() instanceof SpearItem) {
            this.dropMain(drop);
            this.dropOff(drop);
            this.resetClass();
            this.setSpearman(true);
            this.setItemSlot(EquipmentSlot.MAINHAND, copy);
            ret = true;
        }
        if (is.getItem() instanceof HookShotItem) {
            this.dropOff(drop);
            this.resetClass();
            this.setHooker(true);
            this.setItemSlot(EquipmentSlot.OFFHAND, copy);
            ret = true;
        }
        return ret;
    }

    // ---------------------------------------------------------------- type and level

    public void setRandomType() {
        if (this.random.nextInt(500) == 0) {
            this.setBannerman(true);
        } else if (this.random.nextInt(200) == 0) {
            this.setBerseker(true);
        } else if (this.random.nextInt(100) == 0) {
            this.setAssasin(true);
        } else if (this.random.nextInt(80) == 0) {
            this.setHooker(true);
        } else if (this.random.nextInt(50) == 0) {
            this.setGrenade(true);
        } else if (this.random.nextInt(50) == 0) {
            this.setSpearman(true);
        } else if (this.random.nextInt(30) == 0) {
            this.setHealer(true);
        } else if (this.random.nextInt(20) == 0) {
            this.setShied(true);
        }
    }

    public void setRandomLevel() {
        if (this.random.nextInt(3) != 0) {
            if (this.random.nextInt(3) == 0) {
                if (this.random.nextInt(2) == 0) {
                    this.setlvl(this.random.nextInt(3) == 0 ? 4 : 3);
                } else {
                    this.setlvl(2);
                }
            } else {
                this.setlvl(1);
            }
        } else {
            this.setlvl(0);
        }
    }

    public float getSizeModifier() {
        return this.randomHeightVariation;
    }

    public @Nullable ItemStack getEquipedWeapon() {
        return switch (this.getlvl()) {
            case 0 -> new ItemStack(Items.WOODEN_SWORD);
            case 1 -> new ItemStack(Items.STONE_SWORD);
            case 2 -> new ItemStack(Items.IRON_SWORD);
            case 3 -> new ItemStack(Items.GOLDEN_SWORD);
            case 4 -> new ItemStack(Items.DIAMOND_SWORD);
            default -> new ItemStack(Items.STONE_SWORD);
        };
    }

    public @Nullable ItemStack getShiedItem() {
        return BDItems.shield(this.shiedID);
    }

    public ItemStack getSecondHandItem() {
        return this.getItemBySlot(EquipmentSlot.OFFHAND);
    }

    /** getArmor(1 boots .. 4 helmet) by level: leather, chain, iron, gold, diamond. */
    public @Nullable ItemStack getArmor(int slot) {
        int lvl = this.getlvl();
        if (lvl < 0 || lvl > 4) {
            return null;
        }
        return switch (slot) {
            case 1 -> new ItemStack(new net.minecraft.world.item.Item[]{Items.LEATHER_BOOTS, Items.CHAINMAIL_BOOTS, Items.IRON_BOOTS,
                    Items.GOLDEN_BOOTS, Items.DIAMOND_BOOTS}[lvl]);
            case 2 -> new ItemStack(new net.minecraft.world.item.Item[]{Items.LEATHER_LEGGINGS, Items.CHAINMAIL_LEGGINGS, Items.IRON_LEGGINGS,
                    Items.GOLDEN_LEGGINGS, Items.DIAMOND_LEGGINGS}[lvl]);
            case 3 -> new ItemStack(new net.minecraft.world.item.Item[]{Items.LEATHER_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE,
                    Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.DIAMOND_CHESTPLATE}[lvl]);
            case 4 -> new ItemStack(new net.minecraft.world.item.Item[]{Items.LEATHER_HELMET, Items.CHAINMAIL_HELMET, Items.IRON_HELMET,
                    Items.GOLDEN_HELMET, Items.DIAMOND_HELMET}[lvl]);
            default -> null;
        };
    }

    // ---------------------------------------------------------------- ranged attack (archer)

    /** attackEntityWithRangedAttack(entity, distanceSq / 10): aim for 30 ticks, then shoot a plain magic arrow. */
    public void attackEntityWithRangedAttack(LivingEntity entity, float f) {
        if (f < this.range * this.range) {
            double dx = entity.getX() - this.getX();
            double dz = entity.getZ() - this.getZ();
            if (this.attackTime == 0) {
                if (this.isAiming()) {
                    BDMagicArrow arrow = this.shootArrow();
                    if (arrow != null) {
                        if (entity.getY() < this.getY()) {
                            arrow.setCritArrow(true);
                        }
                        double d2 = entity.getY() + entity.getEyeHeight() - 0.7F - arrow.getY();
                        float f1 = (float) Math.sqrt(dx * dx + dz * dz) * (f / (150.0F + f) + 0.1F);
                        this.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
                        arrow.heading(dx, d2 + f1, dz, 1.6F, this.arrowAccuracy);
                        this.level().addFreshEntity(arrow);
                        this.attackTime = this.attackSpeed;
                    }
                    this.setAiming(false);
                } else {
                    this.setAiming(true);
                    this.attackTime = 30;
                }
            }
            this.setYRot((float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F);
        }
    }

    /** shootArrow: EntityArrowMagic(world, this, 1.0, 11) with the attack strength as damage. */
    protected @Nullable BDMagicArrow shootArrow() {
        BDMagicArrow arrow = BDMagicArrow.shoot(this, 1.0F, BDMagicArrow.PLAIN);
        if (arrow != null) {
            arrow.setBaseDamage(this.attackStrength);
        }
        return arrow;
    }

    // ---------------------------------------------------------------- damage

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        double angle = 0.0;
        Entity src = source.getEntity();
        if (src != null) {
            angle = BDMobUtil.rotationDiff(src, this);
        }
        if (!source.is(DamageTypeTags.IS_FIRE) && !source.is(DamageTypeTags.BYPASSES_ARMOR) && angle < 120.0) {
            if (this.isDefending()) {
                this.playSound(SoundEvents.BLAZE_HURT, 1.0F, 1.0F);
                if (this.blockRate < 0) {
                    return false;
                }
                if (source.is(DamageTypeTags.IS_PROJECTILE) || this.random.nextInt(10) >= this.blockRate) {
                    return false;
                }
                amount = Math.max((int) amount / this.shiedBlockDefense, 1);
            } else if (this.haveShied()) {
                this.toogleBlocking();
            }
        }
        if (src instanceof LivingEntity entity) {
            if (entity instanceof HumanEntity e && this.getHumanType() != null && Objects.equals(e.getHumanType(), this.getHumanType())
                    && e.isHealer()) {
                return false;
            }
            if (this.owner instanceof Mob ownerMob && ownerMob.getTarget() == null) {
                boolean setTarget = !(entity instanceof BDTeamMob team && Objects.equals(team.getBDType(), this.getHumanType()));
                if (setTarget) {
                    ownerMob.setTarget(entity);
                    ownerMob.setLastHurtByMob(entity);
                }
            }
            if (this.getTarget() != null && this.distanceTo(this.getTarget()) > this.distanceTo(entity)) {
                this.setTarget(entity);
            }
            if (this.isArcher() && this.isAiming()) {
                this.attackTime = this.attackSpeed;
                this.setAiming(false);
            }
        }
        return super.hurtServer(level, source, amount);
    }

    /** getAttackStrength: the race's strength + the 1.5.2 damage of the weapon - 1. */
    public int getAttackStrength(Entity entity) {
        return this.attackStrength + BDMobUtil.oldWeaponDamage(this.getMainHandItem()) - 1;
    }

    /** attackEntityAsMob. */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity entity) {
        int damage = this.getAttackStrength(entity);
        if (this.isAssasin()) {
            double angle = BDMobUtil.rotationDiff(this, entity);
            if (angle > 130.0F) {
                damage *= 2;
                BDMobUtil.packetParticles(entity, true);
            }
        }
        if (this.isBerseker()) {
            damage += this.attackStrength;
        }
        if (this.isSpearman()) {
            int knockBack = 4;
            entity.push(-Mth.sin(this.getYRot() * Mth.DEG_TO_RAD) * knockBack * 0.5F, 0.1,
                    Mth.cos(this.getYRot() * Mth.DEG_TO_RAD) * knockBack * 0.5F);
            if (entity.isPassenger()) {
                entity.stopRiding();
            }
        }
        MobEffectInstance strength = this.getEffect(MobEffects.STRENGTH);
        if (strength != null) {
            damage += 3 << strength.getAmplifier();
        }
        MobEffectInstance weakness = this.getEffect(MobEffects.WEAKNESS);
        if (weakness != null) {
            damage -= 2 << weakness.getAmplifier();
        }
        DamageSource source = this.damageSources().mobAttack(this);
        Vec3 oldMovement = entity.getDeltaMovement();
        boolean hit = entity.hurtServer(level, source, damage);
        if (hit) {
            // 1.5.2 added the enchantment damage only after the hit (no effect); knockback, fire aspect and thorns count
            this.causeExtraKnockback(entity, this.getKnockback(entity, source), oldMovement, source, damage, true);
            net.minecraft.world.item.enchantment.EnchantmentHelper.doPostAttackEffects(level, entity, source);
            this.setLastHurtMob(entity);
        }
        return hit;
    }

    /** getCanSpawnHere of 1.5.2 (natural spawns and spawners only; the defaults are the obstruction check). */
    protected boolean canSpawnHere(LevelAccessor level) {
        return true;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason reason) {
        if (reason == EntitySpawnReason.SPAWNER || reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION) {
            return this.canSpawnHere(level);
        }
        return super.checkSpawnRules(level, reason);
    }

    // ---------------------------------------------------------------- name

    @Override
    public Component getName() {
        Component custom = this.getCustomName();
        if (custom != null) {
            return custom;
        }
        Component name = this.getTypeName().copy();
        for (String key : this.getClassNameKeys()) {
            name = Component.empty().append(name).append(" ").append(Component.translatable(key));
        }
        return name;
    }

    /** getClassName: the translated class names appended to the mob's name ("bd.archer" ...). */
    public List<String> getClassNameKeys() {
        List<String> keys = new ArrayList<>();
        String[] names = {null, "bd.defender", "bd.grenadier", "bd.healer", "bd.archer", "bd.gunner", "bd.bannerman", "bd.berseker",
                "bd.assasin", "bd.spearman", "bd.hooker"};
        int c = this.getEntityClass();
        if (c > 0 && c < names.length) {
            keys.add(names[c]);
        }
        return keys;
    }

    // ---------------------------------------------------------------- teams

    public boolean isSuitableTargetAlly(@Nullable LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        if (this.isSuitableMount(entity)) {
            return true;
        }
        if (entity instanceof HumanEntity human) {
            String targetType = human.getHumanType();
            String own = this.getHumanType();
            if (targetType != null && targetType.contains(".")) {
                if (own != null && own.contains(".")) {
                    return own.substring(own.indexOf('.')).equals(targetType.substring(targetType.indexOf('.')));
                }
            }
            return Objects.equals(targetType, own);
        }
        return false;
    }

    @Override
    public boolean startRiding(Entity entity, boolean force, boolean sendEventAndTriggers) {
        boolean ok = super.startRiding(entity, force, sendEventAndTriggers);
        if (ok) {
            this.onMounted(entity);
        }
        return ok;
    }

    /** mountEntity: the pigs and cows the human rides get AIAnimalMountedByEntity. */
    protected void onMounted(Entity vehicle) {
        if (!this.level().isClientSide() && this.isSuitableMount(vehicle) && (vehicle instanceof Pig || vehicle instanceof Cow)) {
            Mob mount = (Mob) vehicle;
            var goals = BDMobUtil.goals(mount);
            boolean has = goals.getAvailableGoals().stream().anyMatch(g -> g.getGoal() instanceof HumanGoals.AnimalMountedByEntity);
            if (!has) {
                goals.addGoal(1, new HumanGoals.AnimalMountedByEntity((PathfinderMob) mount, 1.0F));
            }
        }
    }

    public boolean isSuitableMount(@Nullable Entity entity) {
        return entity instanceof Pig || entity instanceof Cow;
    }

    public Class<? extends LivingEntity> getMountClass() {
        return Animal.class;
    }

    public boolean canMount() {
        return this.canMount;
    }

    public void setMountAI() {
    }

    /** setMountAI of the spider and wolf riders: the mount attacks the rider's target. */
    protected void mountAttacksTarget() {
        if (this.getVehicle() instanceof Mob ridden && !this.isSuitableTargetAlly(this.getTarget())) {
            ridden.setTarget(this.getTarget());
        }
    }

    /** swingItem of 1.5.2. */
    public void swingItem() {
        this.swing(InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT);
    }

    public boolean isTwoHanded() {
        return this.isBannerman() || this.isBerseker();
    }

    public boolean isSneakingHuman() {
        return this.sneak || this.isShiftKeyDown();
    }

    // ---------------------------------------------------------------- animation flags (data watcher 17)

    public boolean isAiming() {
        return this.getAnimFlag(1);
    }

    public void setAiming(boolean aiming) {
        this.setAnimFlag(1, aiming);
    }

    public boolean isDefending() {
        return this.getAnimFlag(2);
    }

    public void setDefending(boolean defending) {
        if (this.haveShied()) {
            this.setAnimFlag(2, defending);
        }
    }

    public boolean isSitting() {
        return this.getAnimFlag(3);
    }

    public void setSitting(boolean sitting) {
        this.setAnimFlag(3, sitting);
    }

    public boolean isSpeaking() {
        return this.getAnimFlag(4);
    }

    public void setSpeaking(boolean flag) {
        this.setAnimFlag(4, flag);
    }

    public boolean isStanding() {
        return this.getAnimFlag(5);
    }

    public void setStanding(boolean flag) {
        this.setAnimFlag(5, flag);
    }

    public void setPlayerControlled(boolean flag) {
        this.setAnimFlag(7, flag);
    }

    public boolean isControlledByPlayer() {
        return this.getAnimFlag(7);
    }

    public boolean haveShied() {
        return BDItems.SHIELDS.stream().anyMatch(s -> this.getSecondHandItem().is(s.get())) || this.isDefender();
    }

    /** toogleBlocking (the moveSpeed change of 1.5.2 reached no goal; the slowdown is speedFactor). */
    public void toogleBlocking() {
        if (this.haveShied()) {
            this.setDefending(!this.isDefending());
        } else {
            this.setDefending(false);
        }
    }

    public int getAttackSpeed() {
        int delay = this.isSpearman() ? 40 : (this.isTwoHanded() ? 20 : 0);
        return this.attackSpeed + delay;
    }

    public int getAttackRangeBonus() {
        return this.isSpearman() ? 1 : 0;
    }

    public boolean shouldRenderCape() {
        return false;
    }

    // ---------------------------------------------------------------- leader

    /** IEntityTameable.getOwner: the leader (another mob of the team, or the player of a mercenary). */
    public @Nullable LivingEntity getOwnerEntity() {
        if (this.owner == null && this.ownerPlayerName != null && this.level() instanceof ServerLevel server) {
            this.owner = server.getServer().getPlayerList().getPlayerByName(this.ownerPlayerName);
        }
        return this.owner;
    }

    public void setOwner(@Nullable LivingEntity entity) {
        if (this.isCaptain()) {
            this.setCaptain(false);
        }
        if (entity instanceof HumanEntity human) {
            human.setCaptain(true);
        }
        this.owner = entity;
    }

    public void getLeader() {
        if (this.getOwnerEntity() == null) {
            double d = 6.0;
            for (Entity entity : this.level().getEntities(this, this.getBoundingBox().inflate(d, 2.0, d))) {
                if (entity instanceof BDTeamMob team && entity instanceof LivingEntity living && this.getHumanType() != null
                        && Objects.equals(team.getBDType(), this.getHumanType())) {
                    if (!(entity instanceof HumanEntity human)) {
                        this.setOwner(living);
                        return;
                    }
                    if (human.leaderShip >= this.leaderShip && human.getOwnerEntity() == null) {
                        this.setOwner(human);
                        return;
                    }
                }
            }
        } else if (!this.owner.isAlive()) {
            this.owner = null;
        } else if (this.owner instanceof HumanEntity owner && owner.getOwnerEntity() != null && owner.getOwnerEntity() != this) {
            this.setOwner(owner.getOwnerEntity());
        }
    }

    public @Nullable String getHumanType() {
        return this.humanType;
    }

    @Override
    public @Nullable String getBDType() {
        return this.humanType;
    }

    // ---------------------------------------------------------------- classes (data watcher 16)

    public int getEntityClass() {
        return this.entityData.get(DATA_CLASS);
    }

    public boolean isDefender() {
        return this.getEntityClass() == DEFENDER;
    }

    public boolean isGrenadier() {
        return this.getEntityClass() == GRENADIER;
    }

    public boolean isHealer() {
        return this.getEntityClass() == HEALER;
    }

    public boolean isArcher() {
        return this.getEntityClass() == ARCHER;
    }

    public boolean isGunner() {
        return this.getEntityClass() == GUNNER;
    }

    public boolean isBannerman() {
        return this.getEntityClass() == BANNERMAN;
    }

    public boolean isBerseker() {
        return this.getEntityClass() == BERSERKER;
    }

    public boolean isAssasin() {
        return this.getEntityClass() == ASSASSIN;
    }

    public boolean isSpearman() {
        return this.getEntityClass() == SPEARMAN;
    }

    public boolean isHooker() {
        return this.getEntityClass() == HOOKER;
    }

    protected void setClassByte(int c) {
        this.entityData.set(DATA_CLASS, (byte) c);
    }

    /** No class again: the goals and the target of a class go (1.5.2 kept them when the class changed). */
    public void resetClass() {
        this.setRangedAI(1, null);
        this.setSupportAI(1, null);
        this.setHealerTarget(null);
        this.setClassByte(0);
        if (!this.level().isClientSide()) {
            this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }

    public void setShied(boolean flag) {
        if (flag) {
            this.setRangedAI(3, null);
            this.setItemSlot(EquipmentSlot.OFFHAND, orEmpty(this.getShiedItem()));
            this.setClassByte(DEFENDER);
        } else {
            this.setClassByte(0);
        }
    }

    public void setGrenade(boolean flag) {
        if (flag) {
            this.setRangedAI(3, new HumanGoals.GrenadeAttack(this, this.baseMoveSpeed));
            int g = this.grenadeType >= 0 && this.grenadeType < BDItems.GRENADES.size() ? this.grenadeType : 0;
            this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(BDItems.GRENADES.get(g).get()));
            this.setClassByte(GRENADIER);
        }
    }

    public void setHealer(boolean flag) {
        if (flag) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(BDItems.STAFF_LIFE.get()));
            this.setSupportAI(1, new HumanGoals.AttackHeal(this, this.baseMoveSpeed, true));
            this.setHealerTarget(new HumanTargetGoals.NearestHurtAlly(this, LivingEntity.class, 16.0F));
            this.setRangedAI(1, null);
            this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            this.setClassByte(HEALER);
        }
    }

    public void setArcher(boolean flag) {
        if (flag) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(BDItems.LONG_BOW.get()));
            this.setRangedAI(2, new HumanGoals.ArrowAttack(this, this.baseMoveSpeed));
            this.setSupportAI(1, null);
            this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            this.setClassByte(ARCHER);
        }
    }

    public void setGunner(boolean flag) {
        if (flag) {
            this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(BDItems.REVOLVER.get()));
            this.setRangedAI(1, new HumanGoals.GunnerAttack(this, this.baseMoveSpeed));
            this.setClassByte(GUNNER);
        }
    }

    public void setBannerman(boolean flag) {
        if (flag) {
            this.setItemSlot(EquipmentSlot.MAINHAND, BDItems.banner(this.shiedID));
            this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            this.setRangedAI(1, null);
            this.setSupportAI(1, new HumanGoals.Buff(this));
            this.setClassByte(BANNERMAN);
        }
    }

    public void setBerseker(boolean flag) {
        if (flag) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(this.getlvl() < 4 ? BDItems.IRON_BIGSWORD.get() : BDItems.DIAMOND_BIGSWORD.get()));
            this.setSupportAI(1, null);
            this.setRangedAI(1, null);
            this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            this.setClassByte(BERSERKER);
        }
    }

    public void setAssasin(boolean flag) {
        if (flag) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(this.getlvl() < 4 ? BDItems.IRON_DAGGER.get() : BDItems.DIAMOND_DAGGER.get()));
            this.setSupportAI(1, null);
            this.setClassByte(ASSASSIN);
        }
    }

    public void setSpearman(boolean flag) {
        if (flag) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(this.getlvl() < 4 ? BDItems.IRON_SPEAR.get() : BDItems.DIAMOND_SPEAR.get()));
            this.setSupportAI(1, null);
            this.setRangedAI(1, null);
            this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            this.setClassByte(SPEARMAN);
        }
    }

    public void setHooker(boolean flag) {
        if (flag) {
            this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(BDItems.HOOKSHOT.get()));
            this.setRangedAI(3, new HumanGoals.HookAttack(this, this.baseMoveSpeed));
            this.setSupportAI(1, null);
            this.setClassByte(HOOKER);
        }
    }

    public void setEntityClass(int type) {
        switch (type) {
            case 1 -> this.setShied(true);
            case 2 -> this.setGrenade(true);
            case 3 -> this.setHealer(true);
            case 4 -> this.setArcher(true);
            case 5 -> this.setGunner(true);
            case 6 -> this.setBannerman(true);
            case 7 -> this.setBerseker(true);
            case 8 -> this.setAssasin(true);
            case 9 -> this.setSpearman(true);
            case 10 -> this.setHooker(true);
            default -> {
            }
        }
    }

    public void setCaptain(boolean captain) {
        if (!captain || this.getOwnerEntity() == null) {
            this.setAnimFlag(0, captain);
        }
    }

    public boolean isCaptain() {
        return this.getAnimFlag(0);
    }

    public void setlvl(int lvl) {
        this.entityData.set(DATA_LVL, (byte) lvl);
    }

    public int getlvl() {
        return this.entityData.get(DATA_LVL);
    }

    protected boolean getAnimFlag(int index) {
        return (this.entityData.get(DATA_ANIM) & 1 << index) != 0;
    }

    protected void setAnimFlag(int index, boolean result) {
        byte b = this.entityData.get(DATA_ANIM);
        this.entityData.set(DATA_ANIM, (byte) (result ? b | 1 << index : b & ~(1 << index)));
    }

    // ---------------------------------------------------------------- mercenary orders (BDCommandableMob)

    @Override
    public void setStandingPosition(BlockPos pos, float yaw) {
        this.standingPosition = new Vec3(pos.getX(), pos.getY(), pos.getZ());
        this.standingYaw = yaw;
    }

    @Override
    public void setCommandedTarget(LivingEntity target) {
        this.setTarget(target);
    }

    @Override
    public void setPath(List<Vec3> path) {
        this.path = new ArrayList<>(path);
    }

    @Override
    public boolean isMercenary() {
        return false;
    }

    /** One more summoned helper died (EntityMage.summonCount). */
    public void onSummonDied() {
    }

    /** The particles of 1.5.2 that every client drew for itself (smoke of pirates and so on). */
    protected void clientParticle(net.minecraft.core.particles.ParticleOptions type, double x, double y, double z, double vx, double vy, double vz) {
        if (this.level().isClientSide()) {
            this.level().addParticle(type, x, y, z, vx, vy, vz);
        }
    }
}
