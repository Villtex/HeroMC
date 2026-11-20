package legendgear.entity;

import legendgear.LGBlocks;
import legendgear.LGEntities;
import legendgear.LGEvents;
import legendgear.LGFx;
import legendgear.LGParticles;
import legendgear.LGSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * EntityGrindStar: carries a player along the line between two starbeam torches and on to the next torch within 6 that
 * lies best ahead (dot product over 0.8, free line of sight). Speed starts at 0.3 or the player's speed along the line,
 * gains 0.02 per unit of descent (0.06 sneaking), loses 0.002 a tick, at most 1; below 0.02, at a missing torch or when
 * the rider jumps (up by 0.5) the ride ends.
 */
public class GrindStarEntity extends Entity {
    private static final EntityDataAccessor<BlockPos> FROM = SynchedEntityData.defineId(GrindStarEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<BlockPos> TO = SynchedEntityData.defineId(GrindStarEntity.class, EntityDataSerializers.BLOCK_POS);
    public static final int NODE_LINK_DISTANCE = 6;
    private static final double YOFF = 0.0625;
    public double speed = 0.3;

    public GrindStarEntity(EntityType<? extends GrindStarEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    private GrindStarEntity(Level level, BlockPos from, BlockPos to, double speeds) {
        this(LGEntities.GRIND_STAR.get(), level);
        this.entityData.set(FROM, from);
        this.entityData.set(TO, to);
        Vec3 start = node(from);
        this.setPos(start.x, start.y, start.z);
        this.speed = Math.max(0.3, speeds);
        this.setDeltaMovement(this.getLineDirection().scale(this.speed));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FROM, BlockPos.ZERO);
        builder.define(TO, BlockPos.ZERO);
    }

    static Vec3 node(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5, pos.getY() + 0.5 + YOFF, pos.getZ() + 0.5);
    }

    static boolean isNode(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.is(LGBlocks.STARBEAM_TORCH.get()) || state.is(LGBlocks.STARBEAM_WALL_TORCH.get());
    }

    public static List<BlockPos> nodesNearButNotAt(Level level, BlockPos c, int radius) {
        List<BlockPos> nodes = new ArrayList<>();
        for (BlockPos p : BlockPos.betweenClosed(c.offset(-radius, -radius, -radius), c.offset(radius, radius, radius))) {
            if (!p.equals(c) && isNode(level, p)) {
                nodes.add(p.immutable());
            }
        }
        return nodes;
    }

    public static @Nullable BlockPos bestAlignedNode(Level level, List<BlockPos> nodes, Vec3 pos, Vec3 dir, double minimumDot, Entity context) {
        BlockPos best = null;
        double match = minimumDot;
        dir = dir.normalize();
        for (BlockPos test : nodes) {
            Vec3 t = node(test);
            double dot = dir.dot(t.subtract(pos).normalize());
            if (dot > match && level.clip(new ClipContext(pos, t, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, context)).getType() == HitResult.Type.MISS) {
                match = dot;
                best = test;
            }
        }
        return best;
    }

    public Vec3 getLineDirection() {
        return node(this.entityData.get(TO)).subtract(node(this.entityData.get(FROM))).normalize();
    }

    public static @Nullable GrindStarEntity tryMakingStar(Level level, BlockPos at, LivingEntity rider) {
        Vec3 from = node(at);
        BlockPos to = bestAlignedNode(level, nodesNearButNotAt(level, at, NODE_LINK_DISTANCE), from, rider.getLookAngle(), 0.0, rider);
        if (to == null) {
            return null;
        }
        Vec3 line = node(to).subtract(from).normalize();
        double speeds = rider.getDeltaMovement().dot(line);
        return new GrindStarEntity(level, at, to, speeds);
    }

    private boolean isPast(Vec3 node) {
        return this.getDeltaMovement().dot(node.subtract(this.position())) <= 0.0;
    }

    private void snapOn() {
        Vec3 from = node(this.entityData.get(FROM));
        Vec3 dir = this.getLineDirection();
        double d = dir.dot(this.position().subtract(from));
        Vec3 p = from.add(dir.scale(d));
        this.setPos(p.x, p.y, p.z);
    }

    private boolean nodeHop() {
        BlockPos old = this.entityData.get(TO);
        Vec3 dir = this.getLineDirection();
        BlockPos next = bestAlignedNode(this.level(), nodesNearButNotAt(this.level(), old, NODE_LINK_DISTANCE), node(old), dir, 0.8, this);
        if (next == null) {
            this.boot();
            return false;
        }
        if (!this.level().isClientSide()) {
            this.entityData.set(FROM, old);
            this.entityData.set(TO, next);
        }
        return true;
    }

    public void boot() {
        if (this.level().isClientSide()) {
            return;
        }
        Entity e = this.getFirstPassenger();
        this.ejectPassengers();
        this.discard();
        if (e != null) {
            e.setDeltaMovement(this.getDeltaMovement());
            e.setPos(this.getX(), this.getY() + 0.6, this.getZ());
            e.resetFallDistance();
            e.needsSync = true;
        }
    }

    @Override
    public void tick() {
        super.tick();
        Entity rider = this.getFirstPassenger();
        if (rider == null) {
            if (!this.level().isClientSide()) {
                this.discard();
            }
            return;
        }
        if (!isNode(this.level(), this.entityData.get(FROM)) || !isNode(this.level(), this.entityData.get(TO))) {
            this.boot();
            return;
        }
        Vec3 dir = this.getLineDirection();
        if (rider instanceof Player player && !this.level().isClientSide() && LGEvents.justJumped(player)) {
            this.boot();
            rider.setDeltaMovement(rider.getDeltaMovement().add(0.0, 0.5, 0.0));
            return;
        }
        this.speed = this.getDeltaMovement().length();
        this.speed += dir.y * -0.02;
        if (rider.isShiftKeyDown()) {
            this.speed += dir.y * -0.04;
        }
        this.speed -= 0.002;
        if (this.speed < 0.02 && !this.level().isClientSide()) {
            this.boot();
            return;
        }
        this.speed = Math.min(this.speed, 1.0);
        this.setDeltaMovement(dir.scale(this.speed));
        Vec3 m = this.getDeltaMovement();
        this.setPos(this.getX() + m.x, this.getY() + m.y, this.getZ() + m.z);
        if (this.isPast(node(this.entityData.get(TO))) && this.nodeHop()) {
            this.snapOn();
        }
        if (LGParticles.client(this.level())) {
            LGParticles.sparkle(this.level(), this.getX(), this.getY(), this.getZ(), this.random.nextGaussian() * 0.1, this.random.nextGaussian() * 0.1,
                    this.random.nextGaussian() * 0.1, 3.0F);
            LGParticles.rune(this.level(), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0, 2.0F);
        }
        if (!this.level().isClientSide() && this.tickCount % 5 == 0) {
            float level = (float) ((this.speed - 0.02) / 0.98);
            LGFx.sound(this, LGSounds.GAH.get(), 0.3F, 0.5F + level * 1.5F);
        }
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, net.minecraft.world.entity.EntityDimensions dimensions, float scale) {
        return new Vec3(0.0, 0.5, 0.0);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
