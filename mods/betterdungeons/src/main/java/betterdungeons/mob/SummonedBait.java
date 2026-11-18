package betterdungeons.mob;

import betterdungeons.api.BDSummonedMob;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * EntityBaiter ("SummonedBait", the trickster dagger's decoy): looks like, is named like and wears what its summoner
 * wears (health and maximum health too), runs at the summoner's target (or what it last hit) and vanishes in smoke
 * when it reaches it, after 600 ticks (every hit taken adds 100, the hit itself only hurts) or when the summoner is
 * gone. On its first tick every living thing within 40 blocks (sideways) that was after the summoner turns on the
 * bait. Speed 0.26, no xp, drops nothing. The summoner's id is synced so the client can draw its skin.
 */
public class SummonedBait extends PathfinderMob implements BDSummonedMob, SummonedMob {
    private static final EntityDataAccessor<Integer> DATA_SUMMONER = SynchedEntityData.defineId(SummonedBait.class, EntityDataSerializers.INT);
    private @Nullable LivingEntity summoner;
    private int lifeTime;
    private boolean firstTick = true;

    public SummonedBait(EntityType<? extends SummonedBait> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            this.setDropChance(slot, 0.0F);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.26).add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 1.0));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SUMMONER, -1);
    }

    /** EntityBaiter(world, summoner). */
    @Override
    public void initSummoned(LivingEntity summoner, int type, int level) {
        this.summoner = summoner;
        this.entityData.set(DATA_SUMMONER, summoner.getId());
        LivingEntity target = summoner instanceof Mob mob ? mob.getTarget() : null;
        this.setTarget(target != null ? target : summoner.getLastHurtMob());
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(summoner.getMaxHealth());
        this.setHealth(summoner.getHealth());
    }

    @Override
    public @Nullable LivingEntity getSummoner() {
        if (this.summoner == null && this.level().isClientSide()) {
            return this.level().getEntity(this.entityData.get(DATA_SUMMONER)) instanceof LivingEntity living ? living : null;
        }
        return this.summoner;
    }

    @Override
    public void aiStep() {
        if (this.level() instanceof ServerLevel level) {
            if (this.summoner != null) {
                if (this.firstTick) {
                    this.copyLooks(level);
                    this.firstTick = false;
                }
                if (!this.summoner.isAlive()) {
                    this.vanish();
                }
            } else {
                this.vanish();
            }
            if (this.lifeTime > 600) {
                this.vanish();
            }
            this.lifeTime++;
        }
        super.aiStep();
    }

    private void copyLooks(ServerLevel level) {
        LivingEntity s = this.summoner;
        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD,
                EquipmentSlot.MAINHAND}) {
            this.setItemSlot(slot, s.getItemBySlot(slot).copy());
        }
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(40.0, 0.0, 40.0))) {
            if (e instanceof Mob mob && (mob.getTarget() == s || mob.getLastHurtByMob() == s)) {
                mob.setTarget(this);
                mob.setLastHurtByMob(this);
            }
        }
    }

    private void vanish() {
        if (this.isRemoved() || !(this.level() instanceof ServerLevel level)) {
            return;
        }
        for (int r = 0; r < 30; r++) {
            level.sendParticles(ParticleTypes.SMOKE, this.getX() + this.random.nextFloat() - 0.5, this.getY() + this.random.nextFloat() * 2.0F,
                    this.getZ() + this.random.nextFloat() - 0.5, 0, 0.0, 0.0, 0.0, 1.0);
        }
        this.discard();
    }

    /** attackEntityAsMob: reaching the target ends the trick. */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        this.vanish();
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        this.lifeTime += 100;
        super.hurtServer(level, source, damage);
        return false;
    }

    @Override
    protected Component getTypeName() {
        LivingEntity s = this.getSummoner();
        return s != null ? s.getName() : Component.literal("Bait");
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
