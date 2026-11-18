package betterdungeons.mob.ai;

import betterdungeons.mob.SummonedMob;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

/**
 * AIFollowOwner (mutex 3): starts when the summoner is more than minDist away and runs on only while a path exists
 * and the summoner is more than maxDist away (closer than that it stops after one path and starts again on the next
 * evaluation). With teleport, a failed path 12+ blocks away puts the mob on a free spot of the 5 x 5 ring around the
 * summoner.
 */
public class FollowOwnerGoal extends Goal {
    private final Mob mob;
    private final double speed;
    private final float minDist;
    private final float maxDist;
    private final boolean teleports;
    private @Nullable LivingEntity owner;
    private int timer;

    public FollowOwnerGoal(Mob mob, double speed, float minDist, float maxDist, boolean teleports) {
        this.mob = mob;
        this.speed = speed;
        this.minDist = minDist;
        this.maxDist = maxDist;
        this.teleports = teleports;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity o = ((SummonedMob) this.mob).getSummoner();
        if (o == null || this.mob.distanceToSqr(o) < this.minDist * this.minDist) {
            return false;
        }
        this.owner = o;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.owner != null && !this.mob.getNavigation().isDone() && this.mob.distanceToSqr(this.owner) > this.maxDist * this.maxDist;
    }

    @Override
    public void start() {
        this.timer = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity o = this.owner;
        if (o == null) {
            return;
        }
        this.mob.getLookControl().setLookAt(o, 10.0F, this.mob.getMaxHeadXRot());
        if (--this.timer > 0) {
            return;
        }
        this.timer = 10;
        if (!this.mob.getNavigation().moveTo(o, this.speed) && this.mob.distanceToSqr(o) >= 288.0 && this.teleports) {
            int x = Mth.floor(o.getX()) - 2;
            int z = Mth.floor(o.getZ()) - 2;
            int y = Mth.floor(o.getBoundingBox().minY);
            Level level = this.mob.level();
            for (int l = 0; l <= 4; l++) {
                for (int m = 0; m <= 4; m++) {
                    BlockPos below = new BlockPos(x + l, y - 1, z + m);
                    if ((l < 1 || m < 1 || l > 3 || m > 3) && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                            && !level.getBlockState(below.above()).isRedstoneConductor(level, below.above())
                            && !level.getBlockState(below.above(2)).isRedstoneConductor(level, below.above(2))) {
                        this.mob.snapTo(x + l + 0.5, y, z + m + 0.5, this.mob.getYRot(), this.mob.getXRot());
                        this.mob.getNavigation().stop();
                        return;
                    }
                }
            }
        }
    }
}
