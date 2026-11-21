package twilightforest.entity.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

/** Shared goals of the Twilight mobs (entity/ai of 1.5.2). Speeds are factors of the mob's movement speed. */
public final class TFGoals {
    private TFGoals() {
    }

    /** Something that a goal turns on and off on its mob (panicked kobold, charging minotaur, breathing beetle). */
    public interface Flagged {
        void setFlag(boolean on);
    }

    /** EntityAITFPanicOnFlockDeath: runs off for 40 ticks when one of its kind within 4 x 2 blocks is dying (mutex 1). */
    public static final class PanicOnFlockDeath extends Goal {
        private final PathfinderMob mob;
        private final double speed;
        private Vec3 flee = Vec3.ZERO;
        private int fleeTimer;

        public PanicOnFlockDeath(PathfinderMob mob, double speed) {
            this.mob = mob;
            this.speed = speed;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            boolean yikes = this.fleeTimer > 0;
            for (LivingEntity flocker : this.mob.level().getEntitiesOfClass(this.mob.getClass(), this.mob.getBoundingBox().inflate(4.0, 2.0, 4.0))) {
                if (flocker.deathTime > 0) {
                    yikes = true;
                    break;
                }
            }
            if (!yikes) {
                return false;
            }
            Vec3 target = DefaultRandomPos.getPos(this.mob, 5, 4);
            if (target == null) {
                return false;
            }
            this.flee = target;
            return true;
        }

        @Override
        public void start() {
            this.fleeTimer = 40;
            this.mob.getNavigation().moveTo(this.flee.x, this.flee.y, this.flee.z, this.speed);
            if (this.mob instanceof Flagged f) {
                f.setFlag(true);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.fleeTimer > 0 && !this.mob.getNavigation().isDone();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            this.fleeTimer--;
        }

        @Override
        public void stop() {
            this.fleeTimer -= 20;
            if (this.mob instanceof Flagged f) {
                f.setFlag(false);
            }
        }
    }

    /** EntityAITFFlockToSameKind: 1 in 40, walks towards the centre of its kind within 16 x 4 blocks (no mutex). */
    public static final class FlockToSameKind extends Goal {
        private final Mob mob;
        private final double speed;
        private @Nullable Vec3 flock;
        private int moveTimer;

        public FlockToSameKind(Mob mob, double speed) {
            this.mob = mob;
            this.speed = speed;
            this.setFlags(EnumSet.noneOf(Flag.class));
        }

        @Override
        public boolean canUse() {
            if (this.mob.getRandom().nextInt(40) != 0) {
                return false;
            }
            List<? extends Mob> flock = this.mob.level().getEntitiesOfClass(this.mob.getClass(), this.mob.getBoundingBox().inflate(16.0, 4.0, 16.0));
            double x = 0;
            double y = 0;
            double z = 0;
            for (Mob flocker : flock) {
                x += flocker.getX();
                y += flocker.getY();
                z += flocker.getZ();
            }
            int n = Math.max(1, flock.size());
            x /= n;
            y /= n;
            z /= n;
            if (this.mob.distanceToSqr(x, y, z) < 25.0) {
                return false;
            }
            this.flock = new Vec3(x, y, z);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.flock == null) {
                return false;
            }
            double distance = this.mob.distanceToSqr(this.flock);
            return distance >= 25.0 && distance <= 256.0;
        }

        @Override
        public void start() {
            this.moveTimer = 0;
        }

        @Override
        public void stop() {
            this.flock = null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (--this.moveTimer <= 0 && this.flock != null) {
                this.moveTimer = 10;
                this.mob.getNavigation().moveTo(this.flock.x, this.flock.y, this.flock.z, this.speed);
            }
        }
    }

    /**
     * EntityAITFMagicAttack: walks to the target until it has seen it 20 ticks within 10 blocks, then fires every
     * {@code time} ticks (mutex 3). The shot comes from the supplied consumer.
     */
    public static final class MagicAttack extends Goal {
        private final Mob mob;
        private final double speed;
        private final int maxTime;
        private final float chance;
        private final Consumer<LivingEntity> shoot;
        private @Nullable LivingEntity target;
        private int attackTime;
        private int seeTime;

        public MagicAttack(Mob mob, double speed, int time, float chance, Consumer<LivingEntity> shoot) {
            this.mob = mob;
            this.speed = speed;
            this.maxTime = time;
            this.chance = chance;
            this.shoot = shoot;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity t = this.mob.getTarget();
            if (t != null && !(this.mob.getRandom().nextFloat() > this.chance)) {
                this.target = t;
                return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse() || !this.mob.getNavigation().isDone();
        }

        @Override
        public void stop() {
            this.target = null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.target == null) {
                return;
            }
            double distance = this.mob.distanceToSqr(this.target.getX(), this.target.getBoundingBox().minY, this.target.getZ());
            boolean canSee = this.mob.getSensing().hasLineOfSight(this.target);
            this.seeTime = canSee ? this.seeTime + 1 : 0;
            if (distance <= 100.0 && this.seeTime >= 20) {
                this.mob.getNavigation().stop();
            } else {
                this.mob.getNavigation().moveTo(this.target, this.speed);
            }
            this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
            this.attackTime = Math.max(this.attackTime - 1, 0);
            if (this.attackTime <= 0 && distance <= 100.0 && canSee) {
                this.shoot.accept(this.target);
                this.attackTime = this.maxTime;
            }
        }
    }

    /**
     * EntityAITFChargeAttack: from 4..8 blocks on the ground, winds up 15..44 ticks (legs spinning), then runs 2.1
     * blocks past the target and hits it once on the way (mutex 3).
     */
    public static final class ChargeAttack extends Goal {
        private final PathfinderMob charger;
        private final double speed;
        private @Nullable LivingEntity target;
        private Vec3 charge = Vec3.ZERO;
        private int windup;
        private boolean hasAttacked;

        public ChargeAttack(PathfinderMob charger, double speed) {
            this.charger = charger;
            this.speed = speed;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            this.target = this.charger.getTarget();
            if (this.target == null) {
                return false;
            }
            double distance = this.charger.distanceToSqr(this.target);
            if (distance < 16.0 || distance > 64.0 || !this.charger.onGround()) {
                return false;
            }
            double vx = this.target.getX() - this.charger.getX();
            double vz = this.target.getZ() - this.charger.getZ();
            float angle = (float) Math.atan2(vz, vx);
            double length = Math.sqrt(vx * vx + vz * vz);
            Vec3 point = new Vec3(this.charger.getX() + Math.cos(angle) * (length + 2.1), this.target.getY(),
                    this.charger.getZ() + Math.sin(angle) * (length + 2.1));
            Vec3 eye = new Vec3(this.target.getX(), this.target.getEyeY(), this.target.getZ());
            boolean visible = this.target.level().clip(new ClipContext(eye, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                    this.target)).getType() == HitResult.Type.MISS;
            if (visible) {
                this.charge = point;
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            this.windup = 15 + this.charger.getRandom().nextInt(30);
        }

        @Override
        public boolean canContinueToUse() {
            return this.windup > 0 || !this.charger.getNavigation().isDone();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            this.charger.getLookControl().setLookAt(this.charge.x, this.charge.y - 1.0, this.charge.z, 10.0F, this.charger.getMaxHeadXRot());
            if (this.windup > 0) {
                if (--this.windup == 0) {
                    this.charger.getNavigation().moveTo(this.charge.x, this.charge.y, this.charge.z, this.speed);
                } else {
                    this.charger.walkAnimation.setSpeed(this.charger.walkAnimation.speed() + 0.8F);
                    if (this.charger instanceof Flagged f) {
                        f.setFlag(true);
                    }
                }
            }
            if (this.target != null) {
                double reach = this.charger.getBbWidth() * 2.1F * this.charger.getBbWidth() * 2.1F;
                if (this.charger.distanceToSqr(this.target.getX(), this.target.getBoundingBox().minY, this.target.getZ()) <= reach && !this.hasAttacked) {
                    this.hasAttacked = true;
                    if (this.charger.level() instanceof ServerLevel level) {
                        this.charger.doHurtTarget(level, this.target);
                    }
                }
            }
        }

        @Override
        public void stop() {
            this.windup = 0;
            this.target = null;
            this.hasAttacked = false;
            if (this.charger instanceof Flagged f) {
                f.setFlag(false);
            }
        }
    }

    /** EntityAITFKidnapRider: 1 in 5, runs to a random point nearby while something rides it (mutex 1). */
    public static final class KidnapRider extends Goal {
        private final PathfinderMob mob;
        private final double speed;
        private Vec3 pos = Vec3.ZERO;

        public KidnapRider(PathfinderMob mob, double speed) {
            this.mob = mob;
            this.speed = speed;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.mob.isVehicle() && this.mob.getRandom().nextInt(5) <= 0) {
                Vec3 target = DefaultRandomPos.getPos(this.mob, 5, 4);
                if (target != null) {
                    this.pos = target;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            this.mob.getNavigation().moveTo(this.pos.x, this.pos.y, this.pos.z, this.speed);
        }

        @Override
        public boolean canContinueToUse() {
            return !this.mob.getNavigation().isDone();
        }
    }
}
