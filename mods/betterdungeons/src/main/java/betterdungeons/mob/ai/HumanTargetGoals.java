package betterdungeons.mob.ai;

import betterdungeons.api.BDTeamMob;
import betterdungeons.mob.BDMobUtil;
import betterdungeons.mob.HumanEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/** The target tasks of the humans (mobs.AI.AITarget*, on top of EntityAITarget and EntityAINearestAttackableTarget of 1.5.2). */
public final class HumanTargetGoals {
    private HumanTargetGoals() {
    }

    /** EntityAITarget of 1.5.2: target distance, optional sight check with 60 ticks of memory. */
    public abstract static class Target extends Goal {
        protected final Mob taskOwner;
        protected final float targetDistance;
        protected final boolean shouldCheckSight;
        private int unseen;

        protected Target(Mob owner, float targetDistance, boolean shouldCheckSight) {
            this.taskOwner = owner;
            this.targetDistance = targetDistance;
            this.shouldCheckSight = shouldCheckSight;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.taskOwner.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            if (this.taskOwner.distanceToSqr(target) > this.targetDistance * this.targetDistance) {
                return false;
            }
            if (this.shouldCheckSight) {
                if (this.taskOwner.getSensing().hasLineOfSight(target)) {
                    this.unseen = 0;
                } else if (++this.unseen > 60) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void start() {
            this.unseen = 0;
        }

        @Override
        public void stop() {
            this.taskOwner.setTarget(null);
        }

        /** isSuitableTarget(entity, ignoreDisabledDamage). */
        protected boolean isSuitableTarget(@Nullable LivingEntity e, boolean ignoreCreative) {
            if (e == null || e == this.taskOwner || !e.isAlive()) {
                return false;
            }
            if (e instanceof Creeper || e instanceof Ghast) {
                return false;
            }
            if (e instanceof Player player && !ignoreCreative && (player.getAbilities().invulnerable || player.isSpectator())) {
                return false;
            }
            if (!this.taskOwner.isWithinHome(e.blockPosition())) {
                return false;
            }
            return !this.shouldCheckSight || this.taskOwner.getSensing().hasLineOfSight(e);
        }
    }

    /** EntityAINearestAttackableTarget (target chance 0): the closest suitable entity of a class in reach. */
    public static class Nearest extends Target {
        protected final Class<?> targetClass;
        protected @Nullable LivingEntity targetEntity;

        public Nearest(Mob owner, Class<?> targetClass, float distance, boolean checkSight) {
            super(owner, distance, checkSight);
            this.targetClass = targetClass;
        }

        @Override
        public boolean canUse() {
            if (this.targetClass == Player.class) {
                if (this.taskOwner.level() instanceof ServerLevel server) {
                    Player player = server.getNearestPlayer(TargetingConditions.forCombat().range(this.targetDistance).ignoreLineOfSight(),
                            this.taskOwner);
                    if (this.isSuitableTarget(player, false)) {
                        this.targetEntity = player;
                        return true;
                    }
                }
                return false;
            }
            List<LivingEntity> list = this.taskOwner.level().getEntitiesOfClass(LivingEntity.class,
                    this.taskOwner.getBoundingBox().inflate(this.targetDistance, 4.0, this.targetDistance), this.targetClass::isInstance);
            list.sort(Comparator.comparingDouble(this.taskOwner::distanceToSqr));
            for (LivingEntity e : list) {
                if (this.isSuitableTarget(e, false)) {
                    this.targetEntity = e;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            this.taskOwner.setTarget(this.targetEntity);
            super.start();
        }
    }

    /**
     * AITargetNearestAttackable: as above, not for allies, and a target behind the human or in the dark is noticed only
     * when close (sneaking hides it at the edge of view).
     */
    public static class NearestAttackable extends Nearest {
        private final HumanEntity human;

        public NearestAttackable(HumanEntity owner, Class<?> targetClass, float distance, boolean checkSight) {
            super(owner, targetClass, distance, checkSight);
            this.human = owner;
        }

        @Override
        protected boolean isSuitableTarget(@Nullable LivingEntity entity, boolean ignoreCreative) {
            boolean flag = super.isSuitableTarget(entity, ignoreCreative);
            if (flag) {
                if (this.human.isSuitableTargetAlly(entity)) {
                    return false;
                }
                if (this.shouldCheckSight || entity.isPassenger() || !entity.isSprinting()) {
                    double rotDiff = Math.abs(BDMobUtil.angleBetween(this.human, entity));
                    double rot = Math.abs(rotDiff - Math.abs(Mth.wrapDegrees((double) this.human.getYHeadRot())));
                    float light = BDMobUtil.lightBrightness(entity.level(), entity.blockPosition());
                    double dist = entity.distanceToSqr(this.human) / 3.0;
                    light = 40.0F - light * 40.0F;
                    if (rot > 50.0F - light - dist) {
                        if (BDMobUtil.isSneaking(entity)) {
                            return false;
                        }
                        if (rot > 90.0F - light - dist) {
                            return false;
                        }
                    }
                }
            }
            return flag;
        }
    }

    /**
     * AITargetNearestRival (team = false) and AITargetNearestTeamRival (team = true, the mercenary: other players' teams
     * too): humans whose type is in the rival list. Did not check life or sight, only the field of view.
     */
    public static class NearestRival extends Nearest {
        private final String[] rivals;
        private final boolean team;

        public NearestRival(Mob owner, Class<?> targetClass, float distance, boolean checkSight, String @Nullable [] rivals, boolean team) {
            super(owner, targetClass, distance, checkSight);
            this.rivals = rivals == null ? new String[0] : rivals;
            this.team = team;
        }

        @Override
        protected boolean isSuitableTarget(@Nullable LivingEntity entity, boolean ignoreCreative) {
            if (entity == null || entity == this.taskOwner || !entity.isAlive()) {
                return false;
            }
            boolean flag = false;
            if (entity instanceof BDTeamMob mob) {
                String enemyType = mob.getBDType();
                if (enemyType == null || enemyType.isEmpty()) {
                    return false;
                }
                if (enemyType.contains(".")) {
                    if (this.team && this.taskOwner instanceof BDTeamMob own && own.getBDType() != null && own.getBDType().contains(".")) {
                        String ownerTeam = own.getBDType().substring(own.getBDType().indexOf('.'));
                        String targetTeam = enemyType.substring(enemyType.indexOf('.'));
                        if (!ownerTeam.equals(targetTeam)) {
                            flag = true;
                        }
                    }
                    enemyType = enemyType.substring(0, enemyType.indexOf('.')).trim();
                }
                for (String rival : this.rivals) {
                    if (enemyType.equals(rival)) {
                        flag = true;
                    }
                }
            }
            if (this.shouldCheckSight || entity.isPassenger()) {
                double rotDiff = Math.abs(BDMobUtil.angleBetween(this.taskOwner, entity));
                double rot = Math.abs(rotDiff - Math.abs(Mth.wrapDegrees((double) this.taskOwner.getYHeadRot())));
                if (rot > 40.0) {
                    if (BDMobUtil.isSneaking(entity)) {
                        return false;
                    }
                    if (rot > 90.0) {
                        return false;
                    }
                }
            }
            return flag;
        }
    }

    /** AITargetNearestHurtAlly (healers): the nearest hurt ally in sight. */
    public static class NearestHurtAlly extends Nearest {
        private final HumanEntity human;

        public NearestHurtAlly(HumanEntity owner, Class<?> targetClass, float distance) {
            super(owner, targetClass, distance, true);
            this.human = owner;
        }

        @Override
        protected boolean isSuitableTarget(@Nullable LivingEntity entity, boolean ignoreCreative) {
            return super.isSuitableTarget(entity, ignoreCreative) && entity.getHealth() < entity.getMaxHealth() && this.human.isSuitableTargetAlly(entity);
        }
    }

    /** AITargetNearestMount: the nearest free mount as "target" (AIHumanMount then climbs on). */
    public static class NearestMount extends Nearest {
        private final HumanEntity human;

        public NearestMount(HumanEntity owner, Class<?> mountClass, float distance) {
            super(owner, mountClass, distance, false);
            this.human = owner;
        }

        @Override
        protected boolean isSuitableTarget(@Nullable LivingEntity entity, boolean ignoreCreative) {
            if (entity == null || this.human.isPassenger() || entity.isVehicle()) {
                return false;
            }
            LivingEntity target = this.human.getTarget();
            if (target != null && this.human.distanceTo(entity) > this.human.distanceToSqr(target)) {
                return false;
            }
            return this.human.isSuitableMount(entity);
        }
    }

    /** AITargetHumanHurtBy (EntityAIHurtByTarget without help calls): the attacker, unless of the same type. */
    public static class HurtBy extends Target {
        private @Nullable LivingEntity started;

        public HurtBy(Mob owner) {
            super(owner, 16.0F, false);
        }

        @Override
        public boolean canUse() {
            LivingEntity attacker = this.taskOwner.getLastHurtByMob();
            if (attacker instanceof BDTeamMob other && this.taskOwner instanceof BDTeamMob own && other.getBDType() != null
                    && other.getBDType().equals(own.getBDType())) {
                return false;
            }
            return this.isSuitableTarget(attacker, true);
        }

        @Override
        public boolean canContinueToUse() {
            return this.taskOwner.getLastHurtByMob() != null && this.taskOwner.getLastHurtByMob() != this.started;
        }

        @Override
        public void start() {
            this.taskOwner.setTarget(this.taskOwner.getLastHurtByMob());
            this.started = this.taskOwner.getLastHurtByMob();
            super.start();
        }

        @Override
        public void stop() {
            if (this.taskOwner.getTarget() instanceof Player player && player.getAbilities().invulnerable) {
                super.stop();
            }
        }
    }

    /** AITargetOwner: what the leader last attacked, what attacked it, or its target. */
    public static class OwnerTarget extends Target {
        private final HumanEntity human;
        private @Nullable LivingEntity theTarget;

        public OwnerTarget(HumanEntity owner) {
            super(owner, 32.0F, false);
            this.human = owner;
        }

        @Override
        public boolean canUse() {
            this.theTarget = this.getOwnerTarget();
            return this.theTarget != null && this.isSuitableTarget(this.theTarget, false);
        }

        private @Nullable LivingEntity getOwnerTarget() {
            LivingEntity owner = this.human.getOwnerEntity();
            if (owner == null) {
                return null;
            }
            Entity last = owner.getLastHurtMob();
            if (last instanceof LivingEntity living) {
                return living;
            }
            if (owner.getLastHurtByMob() != null) {
                return owner.getLastHurtByMob();
            }
            return owner instanceof Mob mob ? mob.getTarget() : null;
        }

        @Override
        public void start() {
            this.taskOwner.setTarget(this.theTarget);
            super.start();
        }
    }
}
