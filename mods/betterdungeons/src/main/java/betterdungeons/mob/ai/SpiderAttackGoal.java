package betterdungeons.mob.ai;

import betterdungeons.mob.BDBoss;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

/**
 * AISpiderAttack (mutex 3): follows the target like AIMonsterAttack and bites when the squared distance (to the
 * target's feet) is at most (2 * own width) * (2 * target width). The cooldown attackTime = 15 also ran down in
 * EntityLiving.onEntityUpdate, so it fell by two per tick: one bite every 8 ticks.
 */
public class SpiderAttackGoal extends Goal {
    private final PathfinderMob attacker;
    private final double speed;
    private @Nullable LivingEntity target;
    private @Nullable Path path;
    private int delay;
    private int attackTime;

    public SpiderAttackGoal(PathfinderMob attacker, double speed) {
        this.attacker = attacker;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
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
        return this.attacker.getTarget() != null && this.target != null && this.target.isAlive() && !this.attacker.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.attacker.getNavigation().moveTo(this.path, this.speed);
        this.delay = 0;
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
        if (this.attacker.getSensing().hasLineOfSight(t) && --this.delay <= 0) {
            this.delay = 4 + this.attacker.getRandom().nextInt(7);
            this.attacker.getNavigation().moveTo(t, this.speed);
        }
        this.attackTime = Math.max(this.attackTime - 2, 0);
        double bbDist = this.attacker.getBbWidth() * 2.0F * t.getBbWidth() * 2.0F;
        double dist = this.attacker.distanceToSqr(t.getX(), t.getBoundingBox().minY, t.getZ());
        if (dist <= bbDist && this.attackTime <= 0 && this.attacker.level() instanceof ServerLevel level) {
            this.attackTime = 15;
            BDBoss.swingArm(this.attacker);
            this.attacker.doHurtTarget(level, t);
        }
    }
}
