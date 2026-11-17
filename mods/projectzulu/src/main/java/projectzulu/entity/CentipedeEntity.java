package projectzulu.entity;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * EntityCentipede (an EntityMaster): the head of a centipede dragging nine body segments behind it. In the original
 * the segments were separate EntityFollower mobs that walked along the path the head left (a trail of points
 * width/10 apart, one segment every 10 points, all at the head's height), faced the segment in front and passed any
 * damage to the head (which then hurt all of them, so they died together). Here they are NeoForge part entities
 * ({@link Segment}): hittable, damage goes to the head, the trail is kept on both sides from the head's movement and
 * the renderer draws the segments (ModelFollower) at the part positions.
 *
 * <p>Angry (120 ticks, refreshed) whenever its brightness is below 0.5, so it hunts players at night. Health 20,
 * armour 2, speed 0.25, damage 5 (3/4/5/6), attack reach 2.5 blocks (attackDistanceSq 6.25), 0.65 x 0.5. Cannot be
 * pushed; wanders often (every ~20 ticks); no swimming task.
 */
public class CentipedeEntity extends ZuluMonster {
    public static final int SEGMENTS = 9;
    private static final int DISTANCE_INCREMENTS = 10;

    private final Segment[] segments = new Segment[SEGMENTS];
    /** positionList of EntityMaster (newest point first). */
    private final ArrayDeque<Vec3> trail = new ArrayDeque<>();
    private final float distancePerIncrement;

    public CentipedeEntity(EntityType<? extends CentipedeEntity> type, Level level) {
        super(type, level);
        for (int i = 0; i < SEGMENTS; i++) {
            this.segments[i] = new Segment(this, i);
        }
        this.distancePerIncrement = this.getBbWidth() / DISTANCE_INCREMENTS;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ZuluMonster.createZuluAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ARMOR, 2.0);
    }

    /** No setAvoidsWater in EntityMaster. */
    @Override
    protected boolean avoidsWater() {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(3, new ReachAttackGoal(this, false, 6.25));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 1.0, 20));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new StateTargetGoal<>(this, Player.class, ANGRY));
    }

    @Override
    protected void updateAITick(ServerLevel level) {
        if (this.getLightLevelDependentMagicValue() < 0.5F) {
            this.angerLevel = 120;
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    // ---- the body (EntityMaster.onLivingUpdate) ----

    @Override
    public void aiStep() {
        super.aiStep();
        this.updateTrail();
        double prevX = this.getX();
        double prevZ = this.getZ();
        List<Vec3> points = new ArrayList<>(this.trail);
        for (int i = 0; i < SEGMENTS; i++) {
            Segment segment = this.segments[i];
            Vec3 point = points.get((i + 1) * DISTANCE_INCREMENTS);
            segment.xo = segment.getX();
            segment.yo = segment.getY();
            segment.zo = segment.getZ();
            segment.xOld = segment.getX();
            segment.yOld = segment.getY();
            segment.zOld = segment.getZ();
            segment.yRotO = segment.getYRot();
            segment.setPos(point.x, this.getY(), point.z);
            // setTargetRotation: facing the segment (or head) in front
            segment.setYRot((float) (Math.atan2(point.x - prevX, prevZ - point.z) * (180.0 / Math.PI)));
            prevX = point.x;
            prevZ = point.z;
        }
    }

    private void updateTrail() {
        if (this.trail.isEmpty()) {
            double offset = 0.0;
            while (this.trail.size() < SEGMENTS * DISTANCE_INCREMENTS + 1) {
                this.trail.addLast(new Vec3(this.getX() + offset, this.getY(), this.getZ()));
                offset += this.distancePerIncrement;
            }
            for (Segment segment : this.segments) {
                segment.setPos(this.getX(), this.getY(), this.getZ());
            }
        }
        while (horizontalDistance(this.getX(), this.getZ(), this.trail.getFirst()) > this.distancePerIncrement) {
            Vec3 first = this.trail.getFirst();
            double scale = this.distancePerIncrement / horizontalDistance(this.getX(), this.getZ(), first);
            this.trail.addFirst(new Vec3(first.x + (this.getX() - first.x) * scale, this.getY(), first.z + (this.getZ() - first.z) * scale));
            this.trail.removeLast();
        }
    }

    private static double horizontalDistance(double x, double z, Vec3 point) {
        double dx = point.x - x;
        double dz = point.z - z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public Segment[] getSegments() {
        return this.segments;
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public @Nullable PartEntity<?>[] getParts() {
        return this.segments;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        for (int i = 0; i < SEGMENTS; i++) {
            this.segments[i].setId(packet.getId() + i + 1);
        }
    }

    /** EntityFollower: a hittable body segment that passes damage to the head. */
    public static class Segment extends PartEntity<CentipedeEntity> {
        private static final EntityDimensions SIZE = EntityDimensions.scalable(0.65F, 0.5F);
        private final int index;

        public Segment(CentipedeEntity parent, int index) {
            super(parent);
            this.index = index;
            this.refreshDimensions();
        }

        public int getIndex() {
            return this.index;
        }

        @Override
        protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        }

        @Override
        protected void readAdditionalSaveData(ValueInput input) {
        }

        @Override
        protected void addAdditionalSaveData(ValueOutput output) {
        }

        @Override
        public boolean isPickable() {
            return true;
        }

        @Override
        public @Nullable ItemStack getPickResult() {
            return this.getParent().getPickResult();
        }

        /** attackEntityFrom -> attackEntityFromChild of the master. */
        @Override
        public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
            return !this.isInvulnerableToBase(source) && this.getParent().hurtServer(level, source, damage);
        }

        @Override
        public boolean is(Entity other) {
            return this == other || this.getParent() == other;
        }

        @Override
        public EntityDimensions getDimensions(Pose pose) {
            return SIZE;
        }

        @Override
        public boolean shouldBeSaved() {
            return false;
        }
    }
}
