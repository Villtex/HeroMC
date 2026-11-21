package twilightforest.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import twilightforest.entity.Redcap;
import twilightforest.entity.TFMobUtil;

import java.util.EnumSet;

/** The goals of the redcaps (EntityAITFRedcapShy, EntityAITFRedcapLightTNT, EntityAITFRedcapPlantTNT). */
public final class RedcapGoals {
    private RedcapGoals() {
    }

    static @Nullable BlockPos findTnt(Redcap me, int range) {
        BlockPos origin = me.blockPosition();
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (me.level().getBlockState(pos).is(Blocks.TNT)) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    static boolean litTntNearby(Redcap me, int range) {
        return !me.level().getEntitiesOfClass(PrimedTnt.class, me.getBoundingBox().inflate(range)).isEmpty();
    }

    /** EntityAIAvoidEntity with EntityTNTPrimed: runs from primed TNT within {@code distance} blocks (mutex 1). */
    public static final class AvoidTnt extends Goal {
        private final net.minecraft.world.entity.PathfinderMob me;
        private final float distance;
        private final double walkSpeed;
        private final double sprintSpeed;
        private @Nullable PrimedTnt tnt;
        private net.minecraft.world.level.pathfinder.@Nullable Path path;

        public AvoidTnt(net.minecraft.world.entity.PathfinderMob me, float distance, double walkSpeed, double sprintSpeed) {
            this.me = me;
            this.distance = distance;
            this.walkSpeed = walkSpeed;
            this.sprintSpeed = sprintSpeed;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            var list = this.me.level().getEntitiesOfClass(PrimedTnt.class, this.me.getBoundingBox().inflate(this.distance, 3.0, this.distance));
            if (list.isEmpty()) {
                return false;
            }
            this.tnt = list.get(0);
            Vec3 away = net.minecraft.world.entity.ai.util.DefaultRandomPos.getPosAway(this.me, 16, 7, this.tnt.position());
            if (away == null || this.tnt.distanceToSqr(away) < this.tnt.distanceToSqr(this.me)) {
                return false;
            }
            this.path = this.me.getNavigation().createPath(away.x, away.y, away.z, 0);
            return this.path != null;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.me.getNavigation().isDone();
        }

        @Override
        public void start() {
            this.me.getNavigation().moveTo(this.path, this.walkSpeed);
        }

        @Override
        public void stop() {
            this.tnt = null;
        }

        @Override
        public void tick() {
            if (this.tnt != null) {
                this.me.getNavigation().setSpeedModifier(this.me.distanceToSqr(this.tnt) < 49.0 ? this.sprintSpeed : this.walkSpeed);
            }
        }
    }

    /** Keeps 3..6 blocks away from a target looking at it, circling to one side (mutex 1). */
    public static final class Shy extends Goal {
        private final Redcap me;
        private final double speed;
        private final boolean lefty;
        private @Nullable LivingEntity target;
        private Vec3 pos = Vec3.ZERO;

        public Shy(Redcap me, double speed) {
            this.me = me;
            this.speed = speed;
            this.lefty = me.getRandom().nextBoolean();
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity attackTarget = this.me.getTarget();
            if (attackTarget != null && this.me.isShy() && attackTarget.distanceTo(this.me) <= 6.0 && attackTarget.distanceTo(this.me) >= 3.0
                    && TFMobUtil.isLookingAt(attackTarget, this.me)) {
                this.target = attackTarget;
                double vx = this.me.getX() - attackTarget.getX();
                double vz = this.me.getZ() - attackTarget.getZ();
                float angle = (float) Math.atan2(vz, vx);
                angle = (float) (angle + (this.lefty ? 1.0 : -1.0));
                this.pos = new Vec3(attackTarget.getX() + Mth.cos(angle) * 5.0, this.me.getBoundingBox().minY, attackTarget.getZ() + Mth.sin(angle) * 5.0);
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            this.me.getNavigation().moveTo(this.pos.x, this.pos.y, this.pos.z, this.speed);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity attackTarget = this.me.getTarget();
            if (attackTarget == null || this.target == null || !this.target.isAlive() || this.me.getNavigation().isDone()) {
                return false;
            }
            return this.me.isShy() && attackTarget.distanceTo(this.me) < 6.0 && attackTarget.distanceTo(this.me) > 3.0
                    && TFMobUtil.isLookingAt(attackTarget, this.me);
        }

        @Override
        public void tick() {
            if (this.target != null) {
                this.me.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
            }
        }

        @Override
        public void stop() {
            this.target = null;
            this.me.getNavigation().stop();
        }
    }

    /** Walks to a TNT block within 8 blocks and lights it with flint and steel (mutex 3), then waits 20 ticks. */
    public static final class LightTnt extends Goal {
        private final Redcap me;
        private final double speed;
        private int delay;
        private BlockPos tnt = BlockPos.ZERO;

        public LightTnt(Redcap me, double speed) {
            this.me = me;
            this.speed = speed;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            BlockPos nearby = findTnt(this.me, 8);
            if (this.delay > 0) {
                this.delay--;
                return false;
            }
            if (nearby != null) {
                this.tnt = nearby;
                return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.me.level().getBlockState(this.tnt).is(Blocks.TNT);
        }

        @Override
        public void start() {
            this.me.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FLINT_AND_STEEL));
        }

        @Override
        public void stop() {
            this.me.getNavigation().stop();
            this.me.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, this.me.pick());
            this.delay = 20;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            this.me.getLookControl().setLookAt(this.tnt.getX(), this.tnt.getY(), this.tnt.getZ(), 30.0F, this.me.getMaxHeadXRot());
            if (Math.sqrt(this.me.distanceToSqr(this.tnt.getX(), this.tnt.getY(), this.tnt.getZ())) < 2.4) {
                this.me.playAmbientSound();
                if (this.me.level() instanceof ServerLevel level) {
                    TntBlock.prime(level, this.tnt);
                    level.removeBlock(this.tnt, false);
                }
                this.me.getNavigation().stop();
            } else {
                this.me.getNavigation().moveTo(this.tnt.getX(), this.tnt.getY(), this.tnt.getZ(), this.speed);
            }
        }
    }

    /** The sapper puts down TNT behind a target that does not look at it (no mutex, one shot). */
    public static final class PlantTnt extends Goal {
        private final Redcap me;

        public PlantTnt(Redcap me) {
            this.me = me;
            this.setFlags(EnumSet.noneOf(Flag.class));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.me.getTarget();
            return target != null && this.me.getTntLeft() > 0 && this.me.distanceToSqr(target) < 25.0 && !TFMobUtil.isLookingAt(target, this.me)
                    && !litTntNearby(this.me, 8) && findTnt(this.me, 5) == null;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            BlockPos pos = this.me.blockPosition();
            this.me.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new net.minecraft.world.item.ItemStack(Blocks.TNT));
            if (this.me.level().isEmptyBlock(pos)) {
                this.me.setTntLeft(this.me.getTntLeft() - 1);
                this.me.playAmbientSound();
                this.me.level().setBlock(pos, Blocks.TNT.defaultBlockState(), 3);
            }
        }

        @Override
        public void stop() {
            this.me.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, this.me.pick());
        }
    }
}
