package betterdungeons.mob.ai;

import betterdungeons.mob.SummonedMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

/**
 * AITargetOwner (range 32, no sight needed): attacks what the summoner last hit, else what last hurt it, else the
 * summoner's own target.
 */
public class TargetOwnerGoal extends TargetGoal {
    private final TargetingConditions conditions = TargetingConditions.forCombat().range(32.0).ignoreLineOfSight();
    private @Nullable LivingEntity candidate;

    public TargetOwnerGoal(Mob mob) {
        super(mob, false);
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    private @Nullable LivingEntity ownerTarget() {
        LivingEntity owner = ((SummonedMob) this.mob).getSummoner();
        if (owner == null) {
            return null;
        }
        if (owner.getLastHurtMob() != null) {
            return owner.getLastHurtMob();
        }
        if (owner.getLastHurtByMob() != null) {
            return owner.getLastHurtByMob();
        }
        return owner instanceof Mob m ? m.getTarget() : null;
    }

    @Override
    public boolean canUse() {
        this.candidate = this.ownerTarget();
        return this.candidate != null && this.candidate != this.mob && this.canAttack(this.candidate, this.conditions);
    }

    @Override
    protected double getFollowDistance() {
        return 32.0;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.candidate);
        super.start();
    }
}
