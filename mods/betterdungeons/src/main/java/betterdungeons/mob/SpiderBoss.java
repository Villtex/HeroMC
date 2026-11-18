package betterdungeons.mob;

import betterdungeons.api.BDEntityClass;
import betterdungeons.mob.ai.RivalTargetGoal;
import betterdungeons.mob.ai.SpiderAttackGoal;
import betterdungeons.mob.ai.SpiderWebLaunchGoal;
import betterdungeons.projectile.BDProjectile;
import betterdungeons.projectile.ProjectileType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * EntitySpiderBoss ("SpiderBoss", Shelob, an EntitySpider): 300 + 50 per difficulty health, 1.7 x 1.5 (drawn 1.5 times
 * the spider size), speed 0.23, bites for 2 with poison II (7 s on normal, 15 s on hard), fire immune, no fall damage,
 * climbs walls and is not slowed by webs or poisoned (EntitySpider), jumps up when it bumps into a wall. Leaps at its
 * target, walks and bites, shoots webs every 50 ticks (within sqrt(150)). Every hit (not in a wall, not an explosion)
 * has a 1 in (30 - damage) chance to hatch a cave spider and drop a web. Never despawns. Kind "goblin"; attacks
 * players and the rival humans dwarf, walker, zombie and friend.
 *
 * <p>Drops (loot table) its hook, 0..2 diamonds and two spider eye chances, then 1..5 times two diamonds, 10 % one of
 * the twelve potions 1..12; plus a 150 xp orb on top of its 5 xp, whoever killed it.
 */
public class SpiderBoss extends Monster implements BDEntityClass {
    private static final EntityDataAccessor<Byte> DATA_FLAGS = SynchedEntityData.defineId(SpiderBoss.class, EntityDataSerializers.BYTE);

    public SpiderBoss(EntityType<? extends SpiderBoss> type, Level level) {
        super(type, level);
        this.xpReward = 5;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(300 + level.getDifficulty().getId() * 50);
        this.setHealth(this.getMaxHealth());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH, 300.0).add(Attributes.ATTACK_DAMAGE, 2.0).add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    /** initTasks (EntitySpider's own tasks do not exist: it was an old-AI mob). */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(3, new SpiderAttackGoal(this, 1.0));
        this.goalSelector.addGoal(2, new SpiderWebLaunchGoal(this));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, null));
        this.targetSelector.addGoal(2, new RivalTargetGoal(this, true, "dwarf", "walker", "zombie", "friend"));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS, (byte) 0);
    }

    @Override
    public String getEntityClass() {
        return "goblin";
    }

    @Override
    public boolean isHuman() {
        return false;
    }

    // ---------------------------------------------------------------- EntitySpider

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    public void tick() {
        if (this.horizontalCollision) {
            Vec3 m = this.getDeltaMovement();
            this.setDeltaMovement(m.x, 0.3, m.z);
        }
        super.tick();
        if (!this.level().isClientSide()) {
            this.setClimbing(this.horizontalCollision);
        }
    }

    @Override
    public boolean onClimbable() {
        return (this.entityData.get(DATA_FLAGS) & 1) != 0;
    }

    private void setClimbing(boolean value) {
        byte flags = this.entityData.get(DATA_FLAGS);
        this.entityData.set(DATA_FLAGS, (byte) (value ? flags | 1 : flags & ~1));
    }

    /** EntitySpider.setInWeb: webs do not slow it. */
    @Override
    public void makeStuckInBlock(BlockState state, Vec3 speedMultiplier) {
        if (!state.is(net.minecraft.world.level.block.Blocks.COBWEB)) {
            super.makeStuckInBlock(state, speedMultiplier);
        }
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return !effect.is(MobEffects.POISON) && super.canBeAffected(effect);
    }

    // ---------------------------------------------------------------- fight

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.is(DamageTypes.IN_WALL)) {
            return false;
        }
        if (this.random.nextInt(30 - Math.min((int) damage, 28)) == 0 && !source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) {
            CaveSpider spider = EntityTypes.CAVE_SPIDER.create(level, EntitySpawnReason.MOB_SUMMONED);
            if (spider != null) {
                if (source.getEntity() instanceof LivingEntity attacker) {
                    spider.setTarget(attacker);
                }
                spider.setPos(this.getX(), this.getY(), this.getZ());
                level.addFreshEntity(spider);
            }
            BDProjectile.spawn(this, ProjectileType.WEB_VERTICAL);
        }
        return super.hurtServer(level, source, damage);
    }

    /** attackEntityAsMob: poison II, 7 s on normal and 15 s on hard. */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (!super.doHurtTarget(level, target)) {
            return false;
        }
        if (target instanceof LivingEntity living) {
            int seconds = switch (level.getDifficulty()) {
                case NORMAL -> 7;
                case HARD -> 15;
                default -> 0;
            };
            if (seconds > 0) {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, seconds * 20, 1), this);
            }
        }
        return true;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource source) {
        return false;
    }

    /** The extra 150 xp orb of dropFewItems (on any death with mob loot, like the items). */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        ExperienceOrb.award(level, this.position(), 150);
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SPIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.0F);
    }
}
