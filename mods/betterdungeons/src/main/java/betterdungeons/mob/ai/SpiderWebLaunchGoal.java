package betterdungeons.mob.ai;

import betterdungeons.projectile.BDProjectile;
import betterdungeons.projectile.ProjectileType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

/**
 * AISpiderWebLaunch: while the target is reachable, a web ball (EntityBaseBall type 13) every 50 ticks when the target
 * is within sqrt(150) blocks. In 1.5.2 it had the same mutex bits and walking as AISpiderAttack at a higher priority,
 * so the two kept pushing each other out (bites mostly happened when a path had run out); here the web launcher has no
 * flags and only shoots, the walking and biting are {@link SpiderAttackGoal}'s.
 */
public class SpiderWebLaunchGoal extends Goal {
    private final PathfinderMob attacker;
    private @Nullable LivingEntity target;
    private int attackCountDown;

    public SpiderWebLaunchGoal(PathfinderMob attacker) {
        this.attacker = attacker;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        LivingEntity t = this.attacker.getTarget();
        if (t == null || this.attacker.getNavigation().createPath(t, 0) == null) {
            return false;
        }
        this.target = t;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.attacker.getTarget() == this.target && this.target != null && this.target.isAlive();
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
        LivingEntity t = this.target;
        if (t == null) {
            return;
        }
        this.attackCountDown = Math.max(this.attackCountDown - 1, 0);
        if (!(this.attacker.distanceToSqr(t.getX(), t.getBoundingBox().minY, t.getZ()) > 150.0) && this.attackCountDown == 0) {
            this.attackCountDown = 50;
            BDProjectile.spawn(this.attacker, ProjectileType.WEB);
        }
    }
}
