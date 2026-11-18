package battletowers.entity;

import battletowers.BattleTowers;
import battletowers.TowerDestroyer;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * AS_EntityGolem, the guardian on the top floor of a Battle Tower. It sleeps (does nothing, cave sounds) until a
 * player hits it, comes within 6 blocks in sight, opens a chest within 8 blocks or starts breaking one within 7
 * ({@link battletowers.TowerEvents}). Awake it walks at the player (speed 0.3, 15 melee damage), throws a fireball every
 * 60 ticks after the first 20, and when the player keeps away or stands above it, jumps (0.9 up) and lands with an
 * explosion of power 4, healing 15 if it has 100 or less. When it loses its target it heals to 300 and falls asleep
 * on the ground. Knockback only makes it hop now and then. Its tower type (0 cobblestone .. 5 netherrack) sets its
 * health, 150 + 50 per type, and the diamonds and redstone it drops, 5 + type each, with 8 to 11 clay blocks; a
 * player's kill also drops 0 to 2 paper (loot table). If it had a target when it died, its tower collapses ({@link TowerDestroyer}).
 *
 * <p>1.5.2 set the health to 300 above the maximum of the weaker types; here the maximum is 300 for all and a
 * new golem starts with the health of its type.
 */
public class BattleTowerGolem extends Monster {
    private static final EntityDataAccessor<Boolean> DORMANT = SynchedEntityData.defineId(BattleTowerGolem.class, EntityDataSerializers.BOOLEAN);
    private int rageCounter;
    private int explosionAttack;
    /** Tower type; -1 until set (by the world generation or when loaded), then the health is set once. */
    private int towerID = -1;
    private int drops = 1;
    private int attackCounter;
    private int towerX = -1;
    private int towerY = -1;
    private int towerZ = -1;

    public BattleTowerGolem(EntityType<? extends BattleTowerGolem> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 300.0).add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 15.0).add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true) {
            @Override
            public boolean canUse() {
                return !BattleTowerGolem.this.isDormant() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !BattleTowerGolem.this.isDormant() && super.canContinueToUse();
            }
        });
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DORMANT, true);
    }

    /** The tower type (AS_EntityGolem(world, towerID)): health 150 + 50 per type, 5 + type diamonds. */
    public void setTowerType(int type) {
        this.towerID = type;
        this.drops = 5 + type;
        this.setHealth(150 + 50 * type);
    }

    public int towerType() {
        return Math.max(this.towerID, 0);
    }

    public boolean isDormant() {
        return this.entityData.get(DORMANT);
    }

    private void setDormant() {
        this.entityData.set(DORMANT, true);
    }

    /** setAwake: the golem wakes with a roar, if it slept. */
    public void setAwake() {
        if (this.isDormant()) {
            this.playSound(BattleTowers.GOLEM_AWAKEN.get(), this.getSoundVolume() * 2.0F,
                    ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * 1.8F);
        }
        this.entityData.set(DORMANT, false);
    }

    /** Wakes the golem and sets the player as its target (the chest events). */
    public void wakeFor(Player player) {
        this.setAwake();
        this.setTarget(player);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.getEntity() instanceof Player player) {
            this.wakeFor(player);
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public void knockback(double power, double xd, double zd, DamageSource source, float damage, boolean comesFromEffect) {
        if (this.random.nextInt(5) == 0) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x * 1.5, motion.y + 0.6F, motion.z * 1.5);
        }
    }

    @Override
    public void tick() {
        if (this.level() instanceof ServerLevel level) {
            if (this.towerID < 0) {
                this.setTowerType(0);
            }
            this.serverUpdate(level);
        }
        super.tick();
    }

    private void serverUpdate(ServerLevel level) {
        LivingEntity target = this.getTarget();
        if (!this.isDormant()) {
            if (target == null || !target.isAlive()) {
                this.setHealth(300.0F);
                this.rageCounter = 125;
                this.explosionAttack = 0;
                this.setTarget(null);
                if (this.onGround()) {
                    this.setDormant();
                }
            } else if (this.rageCounter <= 0 && this.explosionAttack == 0) {
                this.playSound(BattleTowers.GOLEM_SPECIAL.get(), this.getSoundVolume() * 2.0F,
                        ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * 1.8F);
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.9, 0.0));
                this.explosionAttack = 1;
            } else if ((this.rageCounter <= -30 || this.onGround()) && this.explosionAttack == 1) {
                if (this.getHealth() <= 100.0F) {
                    this.setHealth(this.getHealth() + 15.0F);
                }
                if (this.getY() - target.getY() > 0.3) {
                    level.explode(this, this.getX(), this.getY() - 0.3, this.getZ(), 4.0F, false, Level.ExplosionInteraction.TNT);
                }
                this.rageCounter = 125;
                this.explosionAttack = 0;
            }
        }
        this.checkForVictim(level);
    }

    private void checkForVictim(ServerLevel level) {
        LivingEntity target = this.getTarget();
        if (this.isDormant()) {
            Player player = level.getNearestPlayer(this, 6.0);
            if (player != null && !player.isSpectator() && !player.isCreative() && this.hasLineOfSight(player)) {
                this.wakeFor(player);
                this.rageCounter = 175;
            }
        } else if (target != null) {
            if (this.towerY == -1) {
                this.towerX = (int) this.getX();
                this.towerY = (int) this.getY();
                this.towerZ = (int) this.getZ();
            }
            boolean targetNearby = target.distanceToSqr(this) < 36.0;
            if (targetNearby && this.explosionAttack != 1 && !(this.getY() - target.getY() > 0.3)) {
                this.rageCounter = 175;
            } else {
                this.rageCounter -= 2;
            }
            if (this.attackCounter == 10) {
                this.playSound(BattleTowers.GOLEM_CHARGE.get(), this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            }
            this.attackCounter++;
            if (this.attackCounter >= 20) {
                this.conjureFireBall(level, target);
                this.attackCounter = -40;
            }
        } else if (this.attackCounter > 0) {
            this.attackCounter--;
        }
    }

    private void conjureFireBall(ServerLevel level, LivingEntity target) {
        double diffX = target.getX() - this.getX();
        double diffY = target.getBoundingBox().minY + target.getBbHeight() / 2.0F - (this.getY() + this.getBbHeight() * 0.8);
        double diffZ = target.getZ() - this.getZ();
        float yaw = -((float) Mth.atan2(diffX, diffZ)) * (180.0F / (float) Math.PI);
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.playSound(SoundEvents.GHAST_SHOOT, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
        GolemFireball fireball = new GolemFireball(level, this, new Vec3(diffX, diffY, diffZ));
        Vec3 look = this.getViewVector(1.0F);
        fireball.setPos(this.getX() + look.x * 2.0, this.getY() + this.getBbHeight() * 0.8 + look.y * 0.5, this.getZ() + look.z * 2.0);
        level.addFreshEntity(fireball);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity entity) {
        if (this.onGround()) {
            double d = entity.getX() - this.getX();
            double d1 = entity.getZ() - this.getZ();
            float f1 = Mth.sqrt((float) (d * d + d1 * d1));
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(d / f1 * 0.5 * 0.20000000192092895 + motion.x * 0.20000000098023224, motion.y,
                    d1 / f1 * 0.5 * 0.10000000192092896 + motion.z * 0.20000000098023224);
        }
        return super.doHurtTarget(level, entity);
    }

    @Override
    public void die(DamageSource source) {
        LivingEntity target = this.getTarget();
        super.die(source);
        if (this.level() instanceof ServerLevel level && this.dead) {
            for (int j = 0; j < this.drops; j++) {
                this.spawnAtLocation(level, new ItemStack(Items.DIAMOND));
                this.spawnAtLocation(level, new ItemStack(Items.REDSTONE));
            }
            int clay = this.random.nextInt(4) + 8;
            for (int k = 0; k < clay; k++) {
                this.spawnAtLocation(level, new ItemStack(Items.CLAY));
            }
            if (target != null && BattleTowers.config().towerDestroyerEnabled != 0) {
                if (this.towerY == -1) {
                    this.towerX = (int) this.getX();
                    this.towerY = (int) this.getY();
                    this.towerZ = (int) this.getZ();
                }
                TowerDestroyer.start(level, this.towerX, this.towerY, this.towerZ, target);
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        if (this.towerY == -1) {
            this.towerX = (int) this.getX();
            this.towerY = (int) this.getY();
            this.towerZ = (int) this.getZ();
        }
        super.addAdditionalSaveData(output);
        output.putBoolean("isDormant", this.isDormant());
        output.putInt("hasexplosionAttacked", this.explosionAttack);
        output.putInt("rageCounter", this.rageCounter);
        output.putInt("Drops", this.drops);
        output.putInt("TowerType", this.towerType());
        output.putInt("towerX", this.towerX);
        output.putInt("towerY", this.towerY);
        output.putInt("towerZ", this.towerZ);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(DORMANT, input.getBooleanOr("isDormant", true));
        this.explosionAttack = input.getIntOr("hasexplosionAttacked", 0);
        this.rageCounter = input.getIntOr("rageCounter", 0);
        // without "TowerType" (a /summon) the type is set on the first tick, with its health
        this.towerID = input.getIntOr("TowerType", -1);
        this.drops = input.getIntOr("Drops", 5 + Math.max(this.towerID, 0));
        this.towerX = input.getIntOr("towerX", -1);
        this.towerY = input.getIntOr("towerY", -1);
        this.towerZ = input.getIntOr("towerZ", -1);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isDormant() ? SoundEvents.AMBIENT_CAVE.value() : BattleTowers.GOLEM_LIVING.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return BattleTowers.GOLEM_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return BattleTowers.GOLEM_DEATH.get();
    }
}
