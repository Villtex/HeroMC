package betterdungeons.mob;

import betterdungeons.api.BDSummonedMob;
import betterdungeons.api.BDSummoner;
import betterdungeons.mob.ai.FollowOwnerGoal;
import betterdungeons.mob.ai.TargetOwnerGoal;
import betterdungeons.projectile.BDProjectile;
import betterdungeons.projectile.ProjectileType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * EntitySummonedUndead ("SummonedUndead", of the cursed bone and the necromancers): a skeleton warrior with 20 health
 * that hits for 2, speed 0.26, 0 xp, undead. It attacks what its summoner fights, follows it (speed 0.39, teleporting
 * when lost) and ignores its blows; summoned by a mob it also hunts players. Every hit sends a heal ball (its level
 * in health) to the summoner and costs 400 ticks of its 1200 tick life; it crumbles to smoke when its time is up or
 * the summoner dies. Level 3 looks like a wither skeleton. After a reload it has
 * lost its summoner (not saved in 1.5.2 either) and just lives out its 1200 ticks.
 */
public class SummonedUndead extends Monster implements BDSummonedMob, SummonedMob {
    private static final EntityDataAccessor<Byte> DATA_LVL = SynchedEntityData.defineId(SummonedUndead.class, EntityDataSerializers.BYTE);
    private @Nullable LivingEntity summoner;
    private int lifeTime;
    private boolean huntsPlayers;

    public SummonedUndead(EntityType<? extends SummonedUndead> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.ATTACK_DAMAGE, 2.0).add(Attributes.MOVEMENT_SPEED, 0.26);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.5, 5.0F, 50.0F, true));
        this.targetSelector.addGoal(1, new TargetOwnerGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_LVL, (byte) 0);
    }

    /** EntitySummonedUndead(world, summoner, lvl); a summoner that is no player also sets it on players. */
    @Override
    public void initSummoned(LivingEntity summoner, int type, int level) {
        this.summoner = summoner;
        this.setLvl(level);
        if (!(summoner instanceof Player) && !this.huntsPlayers) {
            this.huntsPlayers = true;
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, null));
        }
    }

    @Override
    public @Nullable LivingEntity getSummoner() {
        return this.summoner;
    }

    public int getLvl() {
        return this.entityData.get(DATA_LVL);
    }

    public void setLvl(int lvl) {
        this.entityData.set(DATA_LVL, (byte) lvl);
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide()) {
            if (this.summoner != null && !this.summoner.isAlive() || this.lifeTime > 1200) {
                this.vanish();
            }
            this.lifeTime++;
        }
        super.aiStep();
    }

    /** setDead: smoke (the client spawned 30 puffs), then gone. */
    private void vanish() {
        if (this.level() instanceof ServerLevel level) {
            for (int r = 0; r < 30; r++) {
                level.sendParticles(ParticleTypes.SMOKE, this.getX() + this.random.nextFloat() - 0.5, this.getY() + this.random.nextFloat() * 2.0F,
                        this.getZ() + this.random.nextFloat() - 0.5, 0, 0.0, 0.0, 0.0, 1.0);
            }
        }
        if (this.summoner instanceof BDSummoner counter) {
            counter.summonGone();
        }
        this.discard();
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.summoner instanceof BDSummoner counter) {
            counter.summonGone();
        }
    }

    /** attackEntityAsMob: 2 damage; a hit sends the summoner a heal ball and shortens its life by 400 ticks. */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (!target.hurtServer(level, this.damageSources().mobAttack(this), 2.0F)) {
            return false;
        }
        BDBoss.swingArm(this);
        if (this.summoner != null) {
            BDProjectile ball = BDProjectile.create(this.summoner, ProjectileType.HEALBALL, this.getLvl());
            ball.setPos(this.getX(), this.getY() + this.getBbHeight(), this.getZ());
            level.addFreshEntity(ball);
            this.lifeTime += 400;
        }
        return true;
    }

    /** Blows of the summoner do nothing. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.getEntity() != null && source.getEntity() == this.summoner) {
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }
}
