package betterdungeons.mob.ai;

import betterdungeons.mob.BDBoss;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.function.Predicate;

/**
 * AIMonsterAttack / AITurtleAttack (mutex 3): walks to the target (a new path every 4..10 ticks while it is in sight)
 * unless the boss is in an attack, and calls {@link BDBoss#attackEntity} every tick with the squared distance; that
 * method decides the attacks. The speed is the 1.5.2 move speed, which the boss also has as its movement attribute,
 * so the factor is 1.
 */
public class BossAttackGoal extends Goal {
    private final BDBoss attacker;
    private final double speed;
    /** field_48264_f: follow without sight, stay while the target is in the home area. */
    private final boolean longMemory;
    /** AITurtleAttack looked at the turtle's own "attacking" field, AIMonsterAttack at isAttacking. */
    private final Predicate<BDBoss> attacking;
    private @Nullable LivingEntity target;
    private @Nullable Path path;
    private int delay;

    public BossAttackGoal(BDBoss attacker, double speed, boolean longMemory, Predicate<BDBoss> attacking) {
        this.attacker = attacker;
        this.speed = speed;
        this.longMemory = longMemory;
        this.attacking = attacking;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public BossAttackGoal(BDBoss attacker, double speed) {
        this(attacker, speed, false, BDBoss::isAttacking);
    }

    @Override
    public boolean canUse() {
        LivingEntity t = this.attacker.getTarget();
        if (t == null) {
            return false;
        }
        this.target = t;
        this.path = this.attacker.getNavigation().createPath(t, 0);
        return this.path != null;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity t = this.attacker.getTarget();
        if (t == null || this.target == null || !this.target.isAlive()) {
            return false;
        }
        return !this.longMemory ? !this.attacker.getNavigation().isDone() : this.attacker.isWithinHome(BlockPos.containing(this.target.position()));
    }

    @Override
    public void start() {
        if (!this.attacking.test(this.attacker)) {
            this.attacker.getNavigation().moveTo(this.path, this.speed);
            this.delay = 0;
        }
    }

    @Override
    public void stop() {
        this.target = null;
        this.attacker.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity t = this.target;
        if (t == null) {
            return;
        }
        this.attacker.getLookControl().setLookAt(t, 30.0F, 30.0F);
        if ((this.longMemory || this.attacker.getSensing().hasLineOfSight(t)) && --this.delay <= 0) {
            if (!this.attacking.test(this.attacker)) {
                this.delay = 4 + this.attacker.getRandom().nextInt(7);
                this.attacker.getNavigation().moveTo(t, this.speed);
            } else {
                this.attacker.getNavigation().stop();
            }
        }
        double d = this.attacker.distanceToSqr(t.getX(), t.getBoundingBox().minY, t.getZ());
        this.attacker.attackEntity(t, (float) d);
    }
}
