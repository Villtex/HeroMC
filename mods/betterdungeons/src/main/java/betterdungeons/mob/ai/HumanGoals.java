package betterdungeons.mob.ai;

import betterdungeons.BDItems;
import betterdungeons.BDSounds;
import betterdungeons.mob.BDMobUtil;
import betterdungeons.mob.HumanEntity;
import betterdungeons.projectile.BDProjectile;
import betterdungeons.projectile.ProjectileType;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

/**
 * The goals of mobs.AI (without the spider, turtle and flying ones). Mutex bits of 1.5.2: 1 = MOVE, 2 = LOOK, 4 = JUMP;
 * 8 and the mount/target bits had no other user. Every goal ticks every tick like 1.5.2's updateTask.
 */
public final class HumanGoals {
    private HumanGoals() {
    }

    static EnumSet<Goal.Flag> moveLook() {
        return EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK);
    }

    /** getDistanceSq(target.posX, target.boundingBox.minY, target.posZ). */
    static double distSq(Entity a, Entity target) {
        return a.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ());
    }

    /** AIHumanMeleeAttack. */
    public static class MeleeAttack extends Goal {
        protected final HumanEntity attacker;
        protected final Level level;
        protected @Nullable LivingEntity entityTarget;
        protected final float moveSpeed;
        protected final boolean canSee;
        protected @Nullable Path entityPathEntity;
        protected @Nullable Class<?> classTarget;
        protected int delay;

        public MeleeAttack(HumanEntity attacker, @Nullable Class<?> classTarget, float moveSpeed, boolean canSee) {
            this.attacker = attacker;
            this.level = attacker.level();
            this.moveSpeed = moveSpeed;
            this.canSee = canSee;
            this.classTarget = classTarget;
            this.setFlags(moveLook());
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        protected double nav(double speed) {
            return this.attacker.speed(speed);
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.attacker.getTarget();
            if (target == null) {
                return false;
            }
            if (!target.isAlive()) {
                this.attacker.setTarget(null);
                return false;
            }
            if (!this.attacker.isSuitableTargetAlly(target) && (this.classTarget == null || this.classTarget.isInstance(target))) {
                this.entityTarget = target;
                this.entityPathEntity = this.attacker.getNavigation().createPath(target, 0);
                return this.entityPathEntity != null || this.attacker.isPassenger();
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.attacker.getTarget();
            if (target == null || target != this.entityTarget || !this.entityTarget.isAlive()) {
                return false;
            }
            return !this.canSee ? !this.attacker.getNavigation().isDone() : this.attacker.isWithinHome(this.entityTarget.blockPosition());
        }

        @Override
        public void start() {
            this.attacker.getNavigation().moveTo(this.entityPathEntity, this.nav(this.moveSpeed));
            this.delay = 0;
        }

        @Override
        public void stop() {
            this.entityTarget = null;
            this.attacker.getNavigation().stop();
            this.attacker.setDefending(false);
        }

        @Override
        public void tick() {
            if (this.entityTarget == null) {
                return;
            }
            this.attacker.getLookControl().setLookAt(this.entityTarget, 30.0F, 30.0F);
            if ((this.canSee || this.attacker.getSensing().hasLineOfSight(this.entityTarget)) && --this.delay <= 0) {
                this.delay = 4 + this.attacker.getRandom().nextInt(7);
                this.attacker.getNavigation().moveTo(this.entityTarget, this.nav(this.moveSpeed * this.attacker.speedFactor()));
            }
            this.attacker.attackTime = Math.max(this.attacker.attackTime - 1, 0);
            double reach = this.getMinDistanceToInteract() + this.attacker.getAttackRangeBonus();
            double dist = distSq(this.attacker, this.entityTarget);
            if (this.attacker.isDefending() && dist <= reach * 2.0 && this.attacker.attackTime <= 10) {
                this.attacker.setDefending(false);
            }
            if (dist <= reach && this.attacker.attackTime <= 0) {
                this.attacker.attackTime = this.attacker.getAttackSpeed();
                if (!this.attacker.getMainHandItem().isEmpty()) {
                    this.attacker.swingItem();
                }
                if (this.level instanceof net.minecraft.server.level.ServerLevel server) {
                    this.attacker.doHurtTarget(server, this.entityTarget);
                }
            }
        }

        public double getMinDistanceToInteract() {
            double attackerBB = this.attacker.getBbWidth() * 2.0F;
            if (this.attacker.getVehicle() != null) {
                attackerBB += this.attacker.getVehicle().getBbWidth() * 2.0F;
            }
            double targetBB = this.entityTarget.getBbWidth() * 2.0F;
            if (this.entityTarget.getVehicle() != null) {
                targetBB += this.entityTarget.getVehicle().getBbWidth();
            }
            return attackerBB * targetBB;
        }
    }

    /** AIHumanChargeAttack (minotaurs): runs at the target, now and then charges straight ahead (sprinting). */
    public static class ChargeAttack extends MeleeAttack {
        boolean isAttacking;
        int chargeTime;

        public ChargeAttack(HumanEntity attacker, @Nullable Class<?> classTarget, float moveSpeed, boolean canSee) {
            super(attacker, null, moveSpeed, canSee);
        }

        @Override
        public void start() {
            if (!this.isAttacking) {
                this.attacker.getNavigation().moveTo(this.entityPathEntity, this.nav(this.moveSpeed));
            }
            this.delay = 0;
        }

        void setAttacking(boolean attacking) {
            this.isAttacking = attacking;
            this.attacker.setSprinting(attacking);
        }

        @Override
        public void tick() {
            if (this.entityTarget == null) {
                return;
            }
            this.attacker.getLookControl().setLookAt(this.entityTarget, 30.0F, 30.0F);
            this.attacker.attackTime = Math.max(this.attacker.attackTime - 1, 0);
            double reach = this.getMinDistanceToInteract() + this.attacker.getAttackRangeBonus();
            double dist = distSq(this.attacker, this.entityTarget);
            if (this.isAttacking) {
                this.attacker.getNavigation().stop();
                double ry = Math.toRadians(this.attacker.getYRot() - 180.0F - 20.0F);
                float currentSpeed = (5 + this.chargeTime) * this.moveSpeed / 10.0F;
                this.attacker.setDeltaMovement(Math.sin(ry) * currentSpeed, this.attacker.getDeltaMovement().y, -Math.cos(ry) * currentSpeed);
                this.chargeTime++;
                if (this.chargeTime > 30) {
                    this.setAttacking(false);
                    this.chargeTime = 0;
                }
                float maxRot = 10.0F;
                if (this.attacker.getYHeadRot() + maxRot / 2.0F > this.attacker.getYRot()) {
                    this.attacker.setYRot(this.attacker.getYRot() + maxRot);
                } else if (this.attacker.getYHeadRot() - maxRot / 2.0F < this.attacker.getYRot()) {
                    this.attacker.setYRot(this.attacker.getYRot() - maxRot);
                }
            } else {
                if ((this.canSee || this.attacker.getSensing().hasLineOfSight(this.entityTarget)) && --this.delay <= 0) {
                    this.delay = 4 + this.attacker.getRandom().nextInt(7);
                    this.attacker.getNavigation().moveTo(this.entityTarget, this.nav(this.moveSpeed * this.attacker.speedFactor()));
                }
                if (dist > 16.0 && this.attacker.getRandom().nextInt(50) == 0) {
                    this.setAttacking(true);
                    this.chargeTime = 10;
                }
                if (dist > 64.0) {
                    this.setAttacking(true);
                    this.chargeTime = 0;
                }
            }
            if (this.attacker.isDefending() && dist <= reach * 2.0 && this.attacker.attackTime <= 10) {
                this.attacker.setDefending(false);
            }
            if (dist <= reach && this.attacker.attackTime <= 0) {
                this.attacker.attackTime = this.attacker.getAttackSpeed();
                if (!this.attacker.getMainHandItem().isEmpty()) {
                    this.attacker.swingItem();
                }
                if (this.level instanceof net.minecraft.server.level.ServerLevel server) {
                    this.attacker.doHurtTarget(server, this.entityTarget);
                }
            }
        }
    }

    /** AIHumanAttackHeal: walks to a hurt ally and heals it (2 every attack interval). */
    public static class AttackHeal extends MeleeAttack {
        public AttackHeal(HumanEntity attacker, float moveSpeed, boolean canSee) {
            super(attacker, HumanEntity.class, moveSpeed, canSee);
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.attacker.getTarget();
            if (!this.attacker.isHealer() || target == null || !this.attacker.isSuitableTargetAlly(target)) {
                return false;
            }
            if (target.getHealth() >= target.getMaxHealth()) {
                this.attacker.setTarget(null);
                return false;
            }
            this.entityTarget = target;
            this.entityPathEntity = this.attacker.getNavigation().createPath(target, 0);
            return this.entityPathEntity != null;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.attacker.getTarget();
            if (!this.attacker.isSuitableTargetAlly(target) || target == null || this.entityTarget == null || !this.entityTarget.isAlive()) {
                return false;
            }
            return !this.canSee ? !this.attacker.getNavigation().isDone() : this.attacker.isWithinHome(this.entityTarget.blockPosition());
        }

        @Override
        public void stop() {
            this.attacker.setTarget(null);
            super.stop();
        }

        @Override
        public void tick() {
            if (this.entityTarget == null) {
                return;
            }
            this.attacker.getLookControl().setLookAt(this.entityTarget, 30.0F, 30.0F);
            if ((this.canSee || this.attacker.getSensing().hasLineOfSight(this.entityTarget)) && --this.delay <= 0) {
                this.delay = 4 + this.attacker.getRandom().nextInt(7);
                this.attacker.getNavigation().moveTo(this.entityTarget, this.nav(this.moveSpeed));
            }
            this.attacker.attackTime = Math.max(this.attacker.attackTime - 1, 0);
            double bounds = this.getMinDistanceToInteract();
            double dist = distSq(this.attacker, this.entityTarget);
            if (this.attacker.isDefending() && dist <= bounds * 2.0) {
                this.attacker.setDefending(false);
            }
            if (dist <= bounds && this.attacker.attackTime <= 0) {
                this.attacker.attackTime = this.attacker.attackSpeed;
                if (!this.attacker.getMainHandItem().isEmpty()) {
                    this.attacker.swingItem();
                }
                this.entityTarget.heal(2.0F);
                var rand = this.level.getRandom();
                this.level.playSound(null, (int) this.entityTarget.getX(), (int) this.entityTarget.getY(), (int) this.entityTarget.getZ(),
                        BDSounds.MAGIC.get(), SoundSource.HOSTILE, 4.0F, (1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.2F) * 0.7F);
                BDMobUtil.packetParticles(this.entityTarget, false);
            }
            if (this.entityTarget.getHealth() >= this.entityTarget.getMaxHealth()) {
                this.attacker.setTarget(null);
            }
        }
    }

    /** AIHumanMount: walks to the mount the target task chose and climbs on. */
    public static class Mount extends MeleeAttack {
        public Mount(HumanEntity attacker, float moveSpeed, boolean canSee) {
            super(attacker, null, moveSpeed, canSee);
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.attacker.getTarget();
            if (this.attacker.isSuitableMount(target) && !this.attacker.isPassenger() && !target.isVehicle() && this.attacker.canMount()) {
                this.entityTarget = target;
                this.entityPathEntity = this.attacker.getNavigation().createPath(target, 0);
                return this.entityPathEntity != null;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.attacker.getTarget();
            if (this.attacker.isSuitableMount(target) && !this.attacker.isPassenger() && this.entityTarget != null && !this.entityTarget.isVehicle()) {
                return this.entityTarget.isAlive() && (this.canSee || !this.attacker.getNavigation().isDone());
            }
            return false;
        }

        @Override
        public void stop() {
            this.attacker.setTarget(null);
            this.entityTarget = null;
            this.attacker.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.entityTarget == null) {
                return;
            }
            this.attacker.getLookControl().setLookAt(this.entityTarget, 30.0F, 30.0F);
            if (this.canSee || this.attacker.getSensing().hasLineOfSight(this.entityTarget)) {
                this.attacker.getNavigation().moveTo(this.entityTarget, this.nav(this.moveSpeed));
            }
            this.attacker.attackTime = Math.max(this.attacker.attackTime - 1, 0);
            double bounds = this.attacker.getBbWidth() * 2.0F * this.entityTarget.getBbWidth() * 2.0F;
            if (distSq(this.attacker, this.entityTarget) <= bounds) {
                this.attacker.startRiding(this.entityTarget, false, true);
                this.attacker.setMountAI();
                this.attacker.setTarget(null);
            }
        }
    }

    /** AIGrenadeAttack (JUMP only: runs beside the melee attack): throws the grenade of the second hand. */
    public static class GrenadeAttack extends MeleeAttack {
        int attackTimer;

        public GrenadeAttack(HumanEntity attacker, float moveSpeed) {
            super(attacker, null, moveSpeed, false);
            this.setFlags(EnumSet.of(Goal.Flag.JUMP));
        }

        @Override
        public void tick() {
            if (this.entityTarget == null) {
                return;
            }
            double d1 = distSq(this.attacker, this.entityTarget);
            boolean see = this.attacker.getSensing().hasLineOfSight(this.entityTarget);
            if (!(d1 > 30.0) && !see) {
                this.attacker.getNavigation().stop();
            } else {
                this.attacker.getNavigation().moveTo(this.entityTarget, this.nav(this.moveSpeed * this.attacker.speedFactor()));
            }
            this.attacker.getLookControl().setLookAt(this.entityTarget, 30.0F, 30.0F);
            this.attackTimer = Math.max(this.attackTimer - 1, 0);
            if (this.attackTimer <= 0 && !(d1 > 30.0) && !(d1 < 3.0) && see) {
                int dist = 3;
                for (LivingEntity e : this.level.getEntitiesOfClass(LivingEntity.class, this.entityTarget.getBoundingBox().inflate(dist))) {
                    if (this.attacker.isSuitableTargetAlly(e)) {
                        return;
                    }
                }
                int type = 0;
                ItemStack second = this.attacker.getSecondHandItem();
                for (int i = 0; i < BDItems.GRENADES.size(); i++) {
                    if (second.is(BDItems.GRENADES.get(i).get())) {
                        type = i;
                    }
                }
                BDProjectile.spawn(this.attacker, ProjectileType.byId(type));
                this.attackTimer = 50;
            }
        }
    }

    /** AIHookAttack (JUMP only): shoots webs (EntityBaseBall type 13) at targets five blocks away or more. */
    public static class HookAttack extends MeleeAttack {
        int attackTimer;

        public HookAttack(HumanEntity attacker, float moveSpeed) {
            super(attacker, null, moveSpeed, false);
            this.setFlags(EnumSet.of(Goal.Flag.JUMP));
        }

        @Override
        public void tick() {
            if (this.entityTarget == null) {
                return;
            }
            double distance = distSq(this.attacker, this.entityTarget);
            boolean see = this.attacker.getSensing().hasLineOfSight(this.entityTarget);
            if (!(distance > 30.0) && !see) {
                this.attacker.getNavigation().stop();
            } else {
                this.attacker.getNavigation().moveTo(this.entityTarget, this.nav(this.moveSpeed * this.attacker.speedFactor()));
            }
            this.attacker.getLookControl().setLookAt(this.entityTarget, 30.0F, 30.0F);
            this.attackTimer = Math.max(this.attackTimer - 1, 0);
            if (this.attackTimer <= 0 && !(distance < 5.0) && see) {
                BDProjectile.spawn(this.attacker, ProjectileType.WEB);
                this.attackTimer = 40;
            }
        }
    }

    /** AIArrowAttack (archers): keeps its distance and shoots when the target was in view for a second. */
    public static class ArrowAttack extends Goal {
        protected final HumanEntity entityHost;
        protected final Level level;
        protected @Nullable LivingEntity attackTarget;
        protected final float moveSpeed;
        protected int timeOnView;

        public ArrowAttack(HumanEntity host, float moveSpeed) {
            this.entityHost = host;
            this.level = host.level();
            this.moveSpeed = moveSpeed;
            this.setFlags(moveLook());
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.entityHost.getTarget();
            if (target == null) {
                return false;
            }
            if (!target.isAlive()) {
                this.entityHost.setTarget(null);
                return false;
            }
            if (this.entityHost.isSuitableTargetAlly(target) || target.distanceToSqr(this.entityHost) < 5.0) {
                return false;
            }
            this.attackTarget = target;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void stop() {
            this.attackTarget = null;
        }

        protected void approach(double distance, double range) {
            if (!(distance > range) && this.timeOnView >= 20) {
                this.entityHost.getNavigation().stop();
            } else {
                this.entityHost.getNavigation().moveTo(this.attackTarget, this.entityHost.speed(this.moveSpeed * this.entityHost.speedFactor()));
            }
            this.entityHost.getLookControl().setLookAt(this.attackTarget, 30.0F, 30.0F);
            this.entityHost.attackTime = Math.max(this.entityHost.attackTime - 1, 0);
        }

        @Override
        public void tick() {
            if (this.attackTarget == null) {
                return;
            }
            double range = this.entityHost.range * this.entityHost.range;
            double distance = distSq(this.entityHost, this.attackTarget);
            boolean see = this.entityHost.getSensing().hasLineOfSight(this.attackTarget);
            this.timeOnView = see ? this.timeOnView + 1 : 0;
            this.approach(distance, range);
            if (this.entityHost.attackTime <= 0 && !(distance > range) && see) {
                this.entityHost.attackEntityWithRangedAttack(this.attackTarget, (float) distance / 10.0F);
                this.entityHost.attackTime = 60;
            }
        }
    }

    /** AIGunnerAttack: aims, then fires the revolver (BULLETPISTOL) from closer than the archers. */
    public static class GunnerAttack extends ArrowAttack {
        public GunnerAttack(HumanEntity host, float moveSpeed) {
            super(host, moveSpeed);
        }

        @Override
        public void tick() {
            if (this.attackTarget == null) {
                return;
            }
            double d = this.entityHost.range * 10;
            double d1 = distSq(this.entityHost, this.attackTarget);
            boolean see = this.entityHost.getSensing().hasLineOfSight(this.attackTarget);
            this.timeOnView = see ? this.timeOnView + 1 : 0;
            this.approach(d1, d);
            if (this.entityHost.attackTime <= 0 && !(d1 > d) && see) {
                this.shoot((float) d1 / 10.0F);
                this.entityHost.attackTime = 60;
            }
        }

        void shoot(float f) {
            if (f < this.entityHost.range && this.entityHost.attackTime == 0) {
                if (this.entityHost.isAiming()) {
                    if (!this.level.isClientSide()) {
                        BDProjectile.spawn(this.entityHost, ProjectileType.BULLET_PISTOL);
                        this.entityHost.attackTime = this.entityHost.attackSpeed;
                    }
                    this.entityHost.setAiming(false);
                } else {
                    this.entityHost.setAiming(true);
                    this.entityHost.attackTime = 30;
                }
            }
        }
    }

    /** AIHumanBuff (bannermen): every 200 ticks speed, strength and resistance for the allies within 10 blocks. */
    public static class Buff extends Goal {
        final HumanEntity attacker;
        int attackTick;
        final int cooldown = 200;

        public Buff(HumanEntity attacker) {
            this.attacker = attacker;
            this.setFlags(EnumSet.noneOf(Goal.Flag.class));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return this.attacker.isBannerman();
        }

        @Override
        public boolean canContinueToUse() {
            return true;
        }

        @Override
        public void start() {
            LivingEntity target = this.attacker.getTarget();
            if (target != null && target.isAlive()) {
                this.attacker.setAiming(true);
            }
        }

        @Override
        public void stop() {
            this.attacker.setTarget(null);
            this.attacker.setAiming(false);
            this.attackTick = 0;
        }

        @Override
        public void tick() {
            if (this.attackTick <= 0) {
                double d = 10.0;
                for (LivingEntity entity : this.attacker.level().getEntitiesOfClass(LivingEntity.class, this.attacker.getBoundingBox().inflate(d, 2.0, d))) {
                    if (this.attacker.isSuitableTargetAlly(entity)) {
                        LivingEntity target = this.attacker.getTarget();
                        if (target != null) {
                            if (target.isAlive()) {
                                this.attacker.setAiming(true);
                            }
                        } else {
                            this.attacker.setAiming(false);
                        }
                        entity.addEffect(new MobEffectInstance(MobEffects.SPEED, this.cooldown, 0));
                        entity.addEffect(new MobEffectInstance(MobEffects.STRENGTH, this.cooldown, 0));
                        entity.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, this.cooldown, 0));
                    }
                }
                this.attackTick = this.cooldown;
            } else {
                this.attackTick--;
            }
        }
    }

    /** AIHumanIdleSit: an idle human without a far leader sits down now and then (500..1500 ticks). */
    public static class IdleSit extends Goal {
        final HumanEntity owner;
        int iddleTime;

        public IdleSit(HumanEntity owner) {
            this.owner = owner;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            if (this.owner.getTarget() == null && !this.owner.isPassenger()) {
                LivingEntity leader = this.owner.getOwnerEntity();
                return leader == null || !(this.owner.distanceTo(leader) > 15.0F) ? this.owner.getNavigation().isDone() : false;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse() && this.iddleTime > 0;
        }

        @Override
        public void start() {
            boolean flag = this.owner.getOwnerEntity() instanceof HumanEntity leader && leader.isSitting();
            if (this.owner.getRandom().nextInt(50) == 0 || flag) {
                this.iddleTime = 500 + this.owner.getRandom().nextInt(1000);
                this.owner.setSitting(true);
            }
        }

        @Override
        public void stop() {
            this.owner.setSitting(false);
        }

        @Override
        public void tick() {
            if (this.iddleTime > 0) {
                this.iddleTime--;
            }
        }
    }

    /** AITalkClosest: an idle human looks at a close ally and "talks" (speech bubble). */
    public static class TalkClosest extends LookAtPlayerGoal {
        final HumanEntity owner;

        public TalkClosest(HumanEntity owner, Class<? extends LivingEntity> type, float distance) {
            super(owner, type, distance);
            this.owner = owner;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            boolean flag = super.canUse();
            if (this.owner.getNavigation().isDone() && this.owner.getTarget() == null) {
                return this.lookAt instanceof LivingEntity living && this.owner.isSuitableTargetAlly(living)
                        && this.owner.distanceTo(this.lookAt) < 5.0F && flag;
            }
            return false;
        }

        @Override
        public void start() {
            super.start();
            this.owner.setSpeaking(true);
        }

        @Override
        public void stop() {
            super.stop();
            this.owner.setSpeaking(false);
        }
    }

    /** AIFollowOwner: walks to the leader when more than minDist away; mercenaries teleport to it from 17 blocks. */
    public static class FollowOwner extends Goal {
        protected final HumanEntity theEntity;
        private @Nullable LivingEntity theOwner;
        private final float moveSpeed;
        private final float minDist;
        private final float maxDist;
        private final boolean teleports;
        private int timer;

        public FollowOwner(HumanEntity entity, float speed, float minDist, float maxDist, boolean teleport) {
            this.theEntity = entity;
            this.moveSpeed = speed;
            this.minDist = minDist;
            this.maxDist = maxDist;
            this.teleports = teleport;
            this.setFlags(moveLook());
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            LivingEntity owner = this.theEntity.getOwnerEntity();
            if (owner == null || this.theEntity.distanceToSqr(owner) < this.minDist * this.minDist) {
                return false;
            }
            this.theOwner = owner;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.theOwner != null && !this.theEntity.getNavigation().isDone()
                    && this.theEntity.distanceToSqr(this.theOwner) > this.maxDist * this.maxDist;
        }

        @Override
        public void start() {
            this.timer = 0;
        }

        @Override
        public void stop() {
            this.theOwner = null;
            this.theEntity.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.theOwner == null) {
                return;
            }
            this.theEntity.getLookControl().setLookAt(this.theOwner, 10.0F, this.theEntity.getMaxHeadXRot());
            if (--this.timer <= 0) {
                this.timer = 10;
                if (!this.theEntity.getNavigation().moveTo(this.theOwner, this.theEntity.speed(this.moveSpeed))
                        && this.theEntity.distanceToSqr(this.theOwner) >= 288.0 && this.teleports) {
                    int i = Mth.floor(this.theOwner.getX()) - 2;
                    int j = Mth.floor(this.theOwner.getZ()) - 2;
                    int k = Mth.floor(this.theOwner.getBoundingBox().minY);
                    Level level = this.theEntity.level();
                    for (int l = 0; l <= 4; l++) {
                        for (int i1 = 0; i1 <= 4; i1++) {
                            BlockPos p = new BlockPos(i + l, k, j + i1);
                            if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && level.getBlockState(p.below()).isFaceSturdy(level, p.below(), net.minecraft.core.Direction.UP)
                                    && !level.getBlockState(p).isRedstoneConductor(level, p) && !level.getBlockState(p.above()).isRedstoneConductor(level, p.above())) {
                                if (this.theEntity.getVehicle() != null) {
                                    this.theEntity.getVehicle().snapTo(i + l + 0.5F, k, j + i1 + 0.5F, this.theEntity.getYRot(), this.theEntity.getXRot());
                                }
                                this.theEntity.snapTo(i + l + 0.5F, k, j + i1 + 0.5F, this.theEntity.getYRot(), this.theEntity.getXRot());
                                this.theEntity.getNavigation().stop();
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    /** AIControlledFollowOwner: the mercenary follows only in FOLLOW mode. */
    public static class ControlledFollowOwner extends FollowOwner {
        public ControlledFollowOwner(HumanEntity entity, float speed, float minDist, float maxDist, boolean teleport) {
            super(entity, speed, minDist, maxDist, teleport);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && this.theEntity.AIMode == EnumAiState.FOLLOW.id;
        }
    }

    /** EnumAiState: the orders of the controller screen. */
    public enum EnumAiState {
        FOLLOW(0, "Follow"), WARD(1, "Ward"), HOLD(2, "Hold position"), PATH(3, "Path"), SIT(4, "Sit");

        public final int id;
        public final String ainame;

        EnumAiState(int id, String name) {
            this.id = id;
            this.ainame = name;
        }

        public static String getStateName(int id) {
            for (EnumAiState s : values()) {
                if (s.id == id) {
                    return s.ainame;
                }
            }
            return "";
        }
    }

    /** AIControlled (mercenaries): ward / hold the standing position, walk the path back and forth, or sit. */
    public static class Controlled extends Goal {
        final HumanEntity owner;
        int currentMode;
        boolean pathDirection;
        int currentPathPoint = -1;

        public Controlled(HumanEntity owner) {
            this.owner = owner;
            this.setFlags(moveLook());
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.owner.getTarget();
            if (target != null && (target == this.owner || !target.isAlive() || target == this.owner.getOwnerEntity()
                    || this.owner.distanceToSqr(target) > 4000.0)) {
                this.owner.setTarget(null);
            }
            if (this.currentMode != this.owner.AIMode) {
                this.stop();
                return false;
            }
            int mode = this.owner.AIMode;
            if ((mode == EnumAiState.WARD.id || mode == EnumAiState.PATH.id) && this.owner.getTarget() == null) {
                return true;
            }
            return mode == EnumAiState.HOLD.id || mode == EnumAiState.SIT.id;
        }

        @Override
        public void start() {
            int mode = this.owner.AIMode;
            if (mode == EnumAiState.SIT.id) {
                this.owner.setSitting(true);
                this.owner.setStanding(true);
            }
            if (mode == EnumAiState.WARD.id || mode == EnumAiState.HOLD.id || mode == EnumAiState.PATH.id) {
                this.owner.setStanding(true);
            }
        }

        @Override
        public void stop() {
            this.currentMode = this.owner.AIMode;
            this.owner.setSitting(false);
        }

        @Override
        public void tick() {
            int mode = this.owner.AIMode;
            if (mode != EnumAiState.WARD.id && mode != EnumAiState.HOLD.id) {
                if (mode == EnumAiState.PATH.id) {
                    List<Vec3> path = this.owner.path;
                    if (path != null && !path.isEmpty()) {
                        if (this.currentPathPoint == -1) {
                            this.setNearestPathPoint(path);
                        } else if (this.currentPathPoint < path.size()) {
                            Vec3 p = path.get(this.currentPathPoint);
                            int x = (int) (Mth.floor(this.owner.getX()) - p.x);
                            int y = (int) (Mth.floor(this.owner.getY()) - p.y);
                            int z = (int) (Mth.floor(this.owner.getZ()) - p.z);
                            double dist = x * x + y * y + z * z;
                            if (dist < 4.0) {
                                this.nextPathPoint(path);
                            } else if (this.owner.getNavigation().createPath(p.x, p.y, p.z, 0) != null) {
                                this.tryMoveTo(p.x, p.y, p.z, 0.23F);
                            } else {
                                this.nextPathPoint(path);
                            }
                        }
                    }
                } else if (mode == EnumAiState.SIT.id) {
                    this.owner.setTarget(null);
                    this.owner.getNavigation().stop();
                    if (!this.owner.isSitting()) {
                        this.owner.setSitting(true);
                    }
                }
            } else if (this.owner.standingPosition != null) {
                Vec3 p = this.owner.standingPosition;
                if (this.owner.getNavigation().createPath(p.x, p.y, p.z, 0) != null) {
                    this.tryMoveTo(p.x, p.y, p.z, 0.3F);
                } else {
                    this.owner.setYRot(this.owner.standingYaw);
                }
            }
        }

        boolean tryMoveTo(double x, double y, double z, float speed) {
            if (this.owner.getVehicle() != null) {
                return this.owner.getVehicle() instanceof PathfinderMob mount
                        && mount.getNavigation().moveTo(x, y, z, (speed + 0.5F) / mount.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
            }
            return this.owner.getNavigation().moveTo(x, y, z, this.owner.speed(speed));
        }

        void nextPathPoint(List<Vec3> path) {
            if (this.currentPathPoint >= path.size() - 1) {
                this.pathDirection = false;
                this.currentPathPoint = path.size() - 1;
            }
            if (this.currentPathPoint == 0) {
                this.pathDirection = true;
                this.currentPathPoint++;
            } else if (this.pathDirection) {
                this.currentPathPoint++;
            } else {
                this.currentPathPoint--;
            }
        }

        void setNearestPathPoint(List<Vec3> path) {
            int closest = -1;
            double min = 4000.0;
            for (int i = 0; i < path.size(); i++) {
                int x = (int) (Mth.floor(this.owner.getX()) - path.get(i).x);
                int y = (int) (Mth.floor(this.owner.getY()) - path.get(i).y);
                int z = (int) (Mth.floor(this.owner.getZ()) - path.get(i).z);
                double dist = x * x + y * y + z * z;
                if (dist < min) {
                    closest = i;
                    min = dist;
                }
            }
            this.currentPathPoint = closest;
        }
    }

    /** AIFirefighter (pirates): puts out fire within reach (the search box of 1.5.2 was -8..13 around the pirate). */
    public static class Firefighter extends Goal {
        final HumanEntity owner;
        final float moveSpeed;
        @Nullable BlockPos nearestFire;

        public Firefighter(HumanEntity owner, float moveSpeed) {
            this.owner = owner;
            this.moveSpeed = moveSpeed;
            this.setFlags(moveLook());
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            int x = Mth.floor(this.owner.getX());
            int y = Mth.floor(this.owner.getY());
            int z = Mth.floor(this.owner.getZ());
            if (this.nearestFire == null) {
                Level level = this.owner.level();
                BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
                for (int i = -8; i < 14; i++) {
                    for (int k = -8; k < 14; k++) {
                        for (int j = -2; j < 4; j++) {
                            p.set(x + i, y + j, z + k);
                            if (level.getBlockState(p).is(BlockTags.FIRE)) {
                                if (this.nearestFire == null || this.owner.distanceToSqr(p.getX(), p.getY(), p.getZ())
                                        < this.owner.distanceToSqr(this.nearestFire.getX(), this.nearestFire.getY(), this.nearestFire.getZ())) {
                                    this.nearestFire = p.immutable();
                                }
                            }
                        }
                    }
                }
            }
            return this.nearestFire != null;
        }

        @Override
        public void tick() {
            if (this.nearestFire != null) {
                BlockPos f = this.nearestFire;
                if (this.owner.distanceToSqr(f.getX(), f.getY(), f.getZ()) < 16.0) {
                    this.owner.level().setBlock(f, Blocks.AIR.defaultBlockState(), 3);
                    this.owner.swingItem();
                    this.nearestFire = null;
                } else if (this.owner.getNavigation().createPath(f, 0) != null) {
                    this.owner.getNavigation().moveTo(f.getX(), f.getY(), f.getZ(), this.owner.speed(this.moveSpeed));
                } else {
                    this.nearestFire = null;
                }
            }
        }
    }

    /** The casting of EntityMage, as AIMageAttack sees it. */
    public interface Caster {
        void attackEntity(LivingEntity entity, float distSq);
    }

    /** AIMageAttack: keeps away when close (random spot within 5), casts while within sqrt(300) blocks. */
    public static class MageAttack extends Goal {
        final HumanEntity attacker;
        final Caster caster;
        @Nullable LivingEntity entityTarget;
        final float speed;
        final boolean canSee;
        @Nullable Path path;

        public MageAttack(HumanEntity attacker, Caster caster, float speed, boolean canSee) {
            this.attacker = attacker;
            this.caster = caster;
            this.speed = speed;
            this.canSee = canSee;
            this.setFlags(moveLook());
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.attacker.getTarget();
            if (target == null) {
                return false;
            }
            this.entityTarget = target;
            this.path = this.attacker.getNavigation().createPath(target, 0);
            return this.path != null;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.attacker.getTarget();
            if (target == null || this.entityTarget == null || !this.entityTarget.isAlive()) {
                return false;
            }
            return !this.canSee ? !this.attacker.getNavigation().isDone() : this.attacker.isWithinHome(this.entityTarget.blockPosition());
        }

        @Override
        public void start() {
            double dist = distSq(this.attacker, this.entityTarget);
            if (dist < 100.0) {
                Vec3 v = DefaultRandomPos.getPos(this.attacker, 5, 4);
                if (v != null) {
                    this.attacker.getNavigation().moveTo(v.x, v.y, v.z, this.attacker.speed(this.speed));
                } else {
                    this.attacker.getNavigation().moveTo(this.path, this.attacker.speed(this.speed));
                }
            } else if (dist > 120.0) {
                this.attacker.getNavigation().moveTo(this.entityTarget, this.attacker.speed(this.speed));
            }
        }

        @Override
        public void stop() {
            this.entityTarget = null;
            this.attacker.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.entityTarget == null) {
                return;
            }
            this.attacker.getLookControl().setLookAt(this.entityTarget, 30.0F, 30.0F);
            double dist = distSq(this.attacker, this.entityTarget);
            if (!(dist > 300.0)) {
                this.caster.attackEntity(this.entityTarget, (float) dist);
            }
        }
    }

    /**
     * AIAnimalMountedByEntity, given to the pigs and cows a human rides: turns to the rider's view, follows the rider's
     * path and speeds up (boost of 140..980 ticks) towards the rider's target; jumps up steps.
     */
    public static class AnimalMountedByEntity extends Goal {
        private final PathfinderMob host;
        private final float maxSpeed;
        private float currentSpeed;
        private int speedBoostTime;
        private int maxSpeedBoostTime;
        private boolean speedBoosted;
        boolean inAttackStance;
        float acceleration = 0.02F;

        public AnimalMountedByEntity(PathfinderMob host, float maxSpeed) {
            this.host = host;
            this.maxSpeed = maxSpeed;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return this.host.getFirstPassenger() instanceof HumanEntity && this.host.isAlive() && this.isSpeedBoosted();
        }

        @Override
        public void stop() {
            this.speedBoosted = false;
            this.currentSpeed = 0.0F;
        }

        @Override
        public void tick() {
            if (!(this.host.getFirstPassenger() instanceof HumanEntity rider)) {
                return;
            }
            float rot = Mth.wrapDegrees(rider.getYHeadRot() - this.host.getYRot());
            rot = Mth.clamp(rot, -5.0F, 5.0F);
            this.host.setYRot(Mth.wrapDegrees(this.host.getYRot() + rot));
            rider.setYRot(this.host.getYRot());
            if (this.inAttackStance) {
                this.updateAIAttacking(rider);
            } else if (rider.getNavigation().getPath() != null && rider.getNavigation().getPath().getEndNode() != null) {
                var end = rider.getNavigation().getPath().getEndNode();
                this.host.getNavigation().moveTo(end.x, end.y, end.z, 1.0 / this.host.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
                this.acceleration = 0.0F;
            } else {
                this.acceleration = 0.0F;
            }
            this.currentSpeed = Math.min(Math.max(this.currentSpeed + this.acceleration, 0.01F), this.maxSpeed);
            float finalSpeed = this.currentSpeed;
            if (this.speedBoosted && this.speedBoostTime < this.maxSpeedBoostTime) {
                this.speedBoostTime++;
                finalSpeed += 0.2F;
            }
            int i = Mth.floor(this.host.getX());
            int j = Mth.floor(this.host.getY());
            int k = Mth.floor(this.host.getZ());
            float temp = finalSpeed;
            float despX = Mth.sin(this.host.getYRot() * Mth.DEG_TO_RAD);
            float despZ = Mth.cos(this.host.getYRot() * Mth.DEG_TO_RAD);
            float f7 = this.host.getSpeed() * temp / Math.max(temp, 1.0F);
            temp *= f7;
            despX = -(temp * despX);
            despZ = temp * despZ;
            if (Mth.abs(despX) > Mth.abs(despZ)) {
                despX += Math.signum(despX) * this.host.getBbWidth() / 2.0F;
                despZ = 0.0F;
            } else {
                despX = 0.0F;
                despZ += Math.signum(despZ) * this.host.getBbWidth() / 2.0F;
            }
            int xNext = Mth.floor(this.host.getX() + despX);
            int zNext = Mth.floor(this.host.getZ() + despZ);
            if (i != xNext || k != zNext) {
                Level level = this.host.level();
                BlockState here = level.getBlockState(new BlockPos(i, j, k));
                BlockState below = level.getBlockState(new BlockPos(i, j - 1, k));
                boolean half = isHalf(here) || here.isAir() && isHalf(below);
                if (!half && level.getBlockState(new BlockPos(xNext, j, zNext)).isSolid()
                        && !level.getBlockState(new BlockPos(i, j + 1, k)).isSolid()
                        && !level.getBlockState(new BlockPos(xNext, j + 1, zNext)).isSolid()) {
                    this.host.getJumpControl().jump();
                }
            }
            this.host.setSpeed((float) this.host.getAttributeValue(Attributes.MOVEMENT_SPEED));
            this.host.travel(new Vec3(0.0, 0.0, finalSpeed));
        }

        private static boolean isHalf(BlockState state) {
            return state.getBlock() instanceof StairBlock || state.getBlock() instanceof SlabBlock;
        }

        void updateAIAttacking(HumanEntity rider) {
            LivingEntity target = rider.getTarget();
            if (target != null) {
                double rotDiff = Math.abs(BDMobUtil.angleBetween(this.host, target));
                double rot = Math.abs(rotDiff - Math.abs(Mth.wrapDegrees((double) this.host.getYRot())));
                if (rot > 20.0) {
                    this.acceleration = -0.005F;
                } else {
                    this.acceleration = 0.015F;
                    this.speedBoosted = true;
                    this.speedBoostTime = 0;
                    this.maxSpeedBoostTime = this.host.getRandom().nextInt(841) + 140;
                }
            }
        }

        boolean isSpeedBoosted() {
            if (this.host.getFirstPassenger() instanceof HumanEntity rider) {
                if (rider.getTarget() != null) {
                    if (!rider.isSuitableTargetAlly(rider.getTarget())) {
                        this.inAttackStance = true;
                    }
                    return true;
                }
                this.inAttackStance = false;
                return rider.getOwnerEntity() != null && rider.distanceTo(rider.getOwnerEntity()) > 10.0F;
            }
            return false;
        }
    }
}
