package twilightforest.entity.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * EntityAITFFireBreath: when the target is in sight within range, with the given chance per tick, the mob breathes for
 * {@code time} ticks at where the target was (mutex 7); after 5 ticks whatever its look ray (30 blocks) hits first is
 * burnt each tick.
 */
public class FireBreathGoal extends Goal {
    private final Mob host;
    private final float range;
    private final int maxDuration;
    private final float chance;
    private final Consumer<Entity> breath;
    private @Nullable LivingEntity target;
    private Vec3 aim = Vec3.ZERO;
    private int durationLeft;

    public FireBreathGoal(Mob host, float range, int time, float chance, Consumer<Entity> breath) {
        this.host = host;
        this.range = range;
        this.maxDuration = time;
        this.chance = chance;
        this.breath = breath;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        this.target = this.host.getTarget();
        if (this.target != null && this.host.distanceTo(this.target) <= this.range && this.host.hasLineOfSight(this.target)) {
            this.aim = new Vec3(this.target.getX(), this.target.getEyeY(), this.target.getZ());
            return this.host.getRandom().nextFloat() < this.chance;
        }
        return false;
    }

    @Override
    public void start() {
        this.durationLeft = this.maxDuration;
        if (this.host instanceof TFGoals.Flagged f) {
            f.setFlag(true);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.durationLeft > 0 && this.host.isAlive() && this.target != null && this.target.isAlive()
                && this.host.distanceTo(this.target) <= this.range && this.host.hasLineOfSight(this.target);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.durationLeft--;
        this.host.getLookControl().setLookAt(this.aim.x, this.aim.y, this.aim.z, 100.0F, 100.0F);
        this.faceVec(this.aim.x, this.aim.y, this.aim.z, 100.0F, 100.0F);
        if (this.maxDuration - this.durationLeft > 5) {
            Entity hit = this.headLookTarget();
            if (hit != null) {
                this.breath.accept(hit);
            }
        }
    }

    @Override
    public void stop() {
        this.durationLeft = 0;
        if (this.host instanceof TFGoals.Flagged f) {
            f.setFlag(false);
        }
    }

    private @Nullable Entity headLookTarget() {
        double range = 30.0;
        Vec3 src = new Vec3(this.host.getX(), this.host.getY() + 0.25, this.host.getZ());
        Vec3 look = this.host.getViewVector(1.0F);
        Vec3 dest = src.add(look.x * range, look.y * range, look.z * range);
        AABB search = this.host.getBoundingBox().expandTowards(look.scale(range)).inflate(3.0);
        Entity pointed = null;
        double hitDist = 0.0;
        for (Entity possible : this.host.level().getEntities(this.host, search)) {
            if (possible.isPickable() && possible != this.host) {
                float border = possible.getPickRadius();
                AABB box = possible.getBoundingBox().inflate(border);
                Optional<Vec3> intercept = box.clip(src, dest);
                if (box.contains(src)) {
                    if (0.0 < hitDist || hitDist == 0.0) {
                        pointed = possible;
                        hitDist = 0.0;
                    }
                } else if (intercept.isPresent()) {
                    double d = src.distanceTo(intercept.get());
                    if (d < hitDist || hitDist == 0.0) {
                        pointed = possible;
                        hitDist = d;
                    }
                }
            }
        }
        return pointed;
    }

    private void faceVec(double x, double y, double z, float yawLimit, float pitchLimit) {
        double xOffset = x - this.host.getX();
        double zOffset = z - this.host.getZ();
        double yOffset = this.host.getY() + 0.25 - y;
        double distance = Math.sqrt(xOffset * xOffset + zOffset * zOffset);
        float yaw = (float) (Math.atan2(zOffset, xOffset) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(yOffset, distance) * 180.0 / Math.PI));
        this.host.setXRot(-rotate(this.host.getXRot(), pitch, pitchLimit));
        this.host.setYRot(rotate(this.host.getYRot(), yaw, yawLimit));
    }

    private static float rotate(float from, float to, float limit) {
        float d = Mth.clamp(Mth.wrapDegrees(to - from), -limit, limit);
        return from + d;
    }
}
