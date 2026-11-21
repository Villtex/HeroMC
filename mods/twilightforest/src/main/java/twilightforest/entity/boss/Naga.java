package twilightforest.entity.boss;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.jspecify.annotations.Nullable;
import twilightforest.TFBosses;
import twilightforest.entity.TFMonster;

/**
 * EntityTFNaga: the serpent of the naga courtyard. 120 / 200 / 250 health (easy / normal / hard), 1.75 × 3, step 2,
 * 217 xp, bites for 6, heals 1 every second after 30 seconds without damage. Its body is up to 12 segments
 * (health / (max / 10) + 2) that follow the head two blocks apart, hurt what they touch (2, animals 6) and pass two
 * thirds of the damage they take to the head (not explosions and fire); lost segments burst after 30 ticks.
 * The old-style AI (updateEntityActionState) circles the target 15 times, stops to intimidate, then charges at it
 * (or crumbles the blocks under a target standing higher), breaks the blocks it runs into while it has a target and
 * stays within 46 / 7 / 46 blocks of its home. On peaceful it turns back into its spawner.
 */
public class Naga extends Monster {
    private static final int TICKS_BEFORE_HEALING = 600;
    public static final int MAX_SEGMENTS = 12;
    private static final int LEASH_X = 46;
    private static final int LEASH_Y = 7;
    private static final int LEASH_Z = 46;

    private final NagaSegment[] body = new NagaSegment[MAX_SEGMENTS];
    private final ServerBossEvent bossEvent = new ServerBossEvent(this.getUUID(), this.getDisplayName(), BossEvent.BossBarColor.PINK,
            BossEvent.BossBarOverlay.PROGRESS);
    private int currentSegments;
    private @Nullable Path pathToEntity;
    private @Nullable LivingEntity targetEntity;
    private int circleCount = 15;
    private int intimidateTimer;
    private int crumblePlayerTimer;
    private int chargeCount;
    private boolean clockwise;
    private int attackTime;
    private float moveSpeed = 0.6F;
    private float landMovementFactor = 0.6F;
    public int ticksSinceDamaged;

    public Naga(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 217;
        this.moveControl = new MoveControl(this) {
            @Override
            public void tick() {
                // updateEntityActionState sets the movement itself
            }
        };
        for (int i = 0; i < MAX_SEGMENTS; i++) {
            this.body[i] = new NagaSegment(this, i);
        }
        this.setSegmentsPerHealth();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 200.0).add(Attributes.MOVEMENT_SPEED, 0.6)
                .add(Attributes.STEP_HEIGHT, 2.0).add(Attributes.FOLLOW_RANGE, 80.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.0);
    }

    /** getMaxHealth: by the difficulty of the world. */
    private static int maxHealthFor(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 120;
            case HARD -> 250;
            default -> 200;
        };
    }

    private int segmentHealth() {
        return Math.max(1, (int) this.getMaxHealth() / 10);
    }

    public NagaSegment[] getSegments() {
        return this.body;
    }

    public int currentSegments() {
        return this.currentSegments;
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public @Nullable PartEntity<?>[] getParts() {
        return this.body;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        for (int i = 0; i < MAX_SEGMENTS; i++) {
            this.body[i].setId(packet.getId() + i + 1);
        }
    }

    /** setSegmentsPerHealth: lost segments burst, new ones grow next to the head; slower with every segment. */
    private void setSegmentsPerHealth() {
        int oldSegments = this.currentSegments;
        int health = (int) Math.ceil(this.getHealth());
        int newSegments = Mth.clamp(health / this.segmentHealth() + (health > 0 ? 2 : 0), 0, MAX_SEGMENTS);
        if (newSegments < oldSegments) {
            for (int i = newSegments; i < oldSegments; i++) {
                this.body[i].selfDestruct();
            }
        } else if (newSegments > oldSegments) {
            for (int i = oldSegments; i < newSegments; i++) {
                if (!this.body[i].isActive()) {
                    this.body[i].activate(this.getX() + 0.1 * i, this.getY() + 0.5, this.getZ() + 0.1 * i, this.random.nextFloat() * 360.0F);
                }
            }
        }
        this.currentSegments = newSegments;
        this.landMovementFactor = 0.6F - this.currentSegments / 12.0F * 0.2F;
        if (this.getAttributes().hasAttribute(Attributes.MOVEMENT_SPEED)) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.landMovementFactor);
        }
    }

    /** jumpMovementFactor = landMovementFactor / 2. */
    @Override
    protected float getFlyingSpeed() {
        return this.landMovementFactor / 2.0F;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            int max = maxHealthFor(this.level().getDifficulty());
            if (this.getAttribute(Attributes.MAX_HEALTH).getBaseValue() != max) {
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(max);
                if (this.getHealth() > max) {
                    this.setHealth(max);
                }
            }
        }
        if (this.deathTime > 0 && this.level().isClientSide()) {
            for (int k = 0; k < 5; k++) {
                this.level().addParticle(this.random.nextBoolean() ? ParticleTypes.EXPLOSION_EMITTER : ParticleTypes.POOF,
                        this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
                        this.getY() + this.random.nextFloat() * this.getBbHeight(),
                        this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
                        this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02);
            }
        }
        this.ticksSinceDamaged++;
        if (!this.level().isClientSide() && this.ticksSinceDamaged > TICKS_BEFORE_HEALING && this.ticksSinceDamaged % 20 == 0) {
            this.heal(1.0F);
        }
        if (this.attackTime > 0) {
            this.attackTime--;
        }
        this.setSegmentsPerHealth();
        // the segments come before the head in the entity list of 1.5.2: they tick first, then the head moves them
        for (NagaSegment segment : this.body) {
            segment.segmentTick();
        }
        super.tick();
        this.moveSegments();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void checkDespawn() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            if (this.hasHome()) {
                this.level().setBlock(this.getHomePosition(), twilightforest.TFBlocks.BOSS_SPAWNERS.get(0).get().defaultBlockState(), 2);
            }
            this.discard();
        } else {
            this.noActionTime = 0;
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    // ---------------------------------------------------------------- updateEntityActionState

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.horizontalCollision && this.hasTarget()) {
            this.breakNearbyBlocks(level);
        }
        if (this.targetEntity != null && !this.isEntityWithinHomeArea(this.targetEntity)) {
            this.targetEntity = null;
        }
        if (this.targetEntity == null) {
            this.targetEntity = this.findTarget(level);
            if (this.targetEntity != null) {
                this.acquireNewPath(level);
            }
        } else if (!this.targetEntity.isAlive()) {
            this.targetEntity = null;
        } else {
            float targetDistance = this.targetEntity.distanceTo(this);
            if (targetDistance > 80.0F) {
                this.targetEntity = null;
            } else if (this.hasLineOfSight(this.targetEntity)) {
                this.attackEntity(level, this.targetEntity, targetDistance);
            }
        }
        if (this.pathToEntity == null) {
            this.acquireNewPath(level);
        }
        boolean inFluid = this.isInWater() || this.isInLava();
        Vec3 next = this.pathToEntity != null ? this.pathToEntity.getNextEntityPos(this) : null;
        double d = this.getBbWidth() * 4.0F;
        while (next != null && next.distanceToSqr(this.getX(), next.y, this.getZ()) < d * d) {
            this.pathToEntity.advance();
            if (this.pathToEntity.isDone()) {
                next = null;
                this.pathToEntity = null;
            } else {
                next = this.pathToEntity.getNextEntityPos(this);
            }
        }
        this.zza = 0.0F;
        this.xxa = 0.0F;
        if (next != null) {
            double dx = next.x - this.getX();
            double dz = next.z - this.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            int footY = Mth.floor(this.getBoundingBox().minY + 0.5);
            double dy = next.y - footY;
            float wanted = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
            float turn = Mth.clamp(Mth.wrapDegrees(wanted - this.getYRot()), -30.0F, 30.0F);
            this.zza = this.moveSpeed;
            if (dist > 4.0 && this.chargeCount == 0) {
                this.xxa = Mth.cos(this.tickCount * 0.3F) * this.moveSpeed * 0.6F;
            }
            this.setYRot(this.getYRot() + turn);
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
            if (dy > 0.0) {
                this.getJumpControl().jump();
            }
        }
        if (this.intimidateTimer > 0 && this.hasTarget()) {
            this.lookAt(this.targetEntity, 30.0F, 30.0F);
            this.zza = 0.1F;
        }
        if (this.random.nextFloat() < 0.8F && inFluid) {
            this.getJumpControl().jump();
        }
    }

    private void breakNearbyBlocks(ServerLevel level) {
        AABB box = this.getBoundingBox();
        int minX = Mth.floor(box.minX - 0.5);
        int minY = Mth.floor(box.minY + 1.01);
        int minZ = Mth.floor(box.minZ - 0.5);
        int maxX = Mth.floor(box.maxX + 0.5);
        int maxY = Mth.floor(box.maxY + 0.001);
        int maxZ = Mth.floor(box.maxZ + 0.5);
        if (!level.hasChunksAt(minX, minZ, maxX, maxZ)) {
            return;
        }
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    this.breakBlock(level, new BlockPos(x, y, z));
                }
            }
        }
    }

    /** dropBlockAsItem + setBlock(0) + the break effect, for any block. */
    private void breakBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.isAir()) {
            level.destroyBlock(pos, true, this);
        }
    }

    private void acquireNewPath(ServerLevel level) {
        if (!this.hasTarget()) {
            this.wanderRandomly();
        } else if (this.intimidateTimer > 0) {
            this.pathToEntity = null;
            this.intimidateTimer--;
            if (this.intimidateTimer == 0) {
                this.clockwise = !this.clockwise;
                if (this.targetEntity.getBoundingBox().minY > this.getBoundingBox().maxY) {
                    this.crumblePlayerTimer = 20 + this.random.nextInt(20);
                    this.goSlow();
                } else {
                    this.doCharge();
                }
            }
        } else {
            if (this.crumblePlayerTimer > 0) {
                this.pathToEntity = null;
                this.crumblePlayerTimer--;
                this.crumbleBelowTarget(level, 2);
                this.crumbleBelowTarget(level, 3);
                if (this.crumblePlayerTimer == 0) {
                    this.doCharge();
                }
            }
            if (this.chargeCount > 0) {
                this.chargeCount--;
                Vec3 point = this.findCirclePoint(this.targetEntity, 14.0, Math.PI);
                this.pathToEntity = this.getNavigation().createPath(Mth.floor(point.x), Mth.floor(point.y), Mth.floor(point.z), 0);
                if (this.chargeCount == 0) {
                    this.circleCount += 10 + this.random.nextInt(10);
                    this.moveSpeed = 0.6F;
                }
            }
            if (this.circleCount > 0) {
                this.circleCount--;
                double radius = this.circleCount % 2 == 0 ? 12.0 : 14.0;
                double rotation = 1.0;
                if (this.circleCount > 1 && this.circleCount < 3) {
                    radius = 16.0;
                }
                if (this.circleCount == 1) {
                    rotation = 0.1;
                }
                Vec3 point = this.findCirclePoint(this.targetEntity, radius, rotation);
                this.pathToEntity = this.getNavigation().createPath((int) point.x, (int) point.y, (int) point.z, 0);
                if (this.circleCount == 0) {
                    this.intimidateTimer += 15 + this.random.nextInt(10);
                    this.goSlow();
                }
            }
        }
    }

    private void crumbleBelowTarget(ServerLevel level, int range) {
        int floor = (int) this.getBoundingBox().minY;
        int targetY = (int) this.targetEntity.getBoundingBox().minY;
        if (targetY > floor) {
            int dx = (int) this.targetEntity.getX() + this.random.nextInt(range) - this.random.nextInt(range);
            int dz = (int) this.targetEntity.getZ() + this.random.nextInt(range) - this.random.nextInt(range);
            int dy = targetY - this.random.nextInt(range) + this.random.nextInt(range > 1 ? range - 1 : range);
            if (dy <= floor) {
                dy = targetY;
            }
            BlockPos pos = new BlockPos(dx, dy, dz);
            if (!level.getBlockState(pos).isAir()) {
                this.breakBlock(level, pos);
                for (int k = 0; k < 20; k++) {
                    level.sendParticles(ParticleTypes.CRIT, this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
                            this.getY() + this.random.nextFloat() * this.getBbHeight(),
                            this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(), 0,
                            this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, 1.0);
                }
            }
        }
    }

    private void doCharge() {
        this.chargeCount = 4;
        this.moveSpeed = 1.0F;
    }

    private void goSlow() {
        this.xxa = 0.0F;
        this.moveSpeed = 0.1F;
        this.pathToEntity = null;
    }

    private Vec3 findCirclePoint(Entity toCircle, double radius, double rotation) {
        double vx = this.getX() - toCircle.getX();
        double vz = this.getZ() - toCircle.getZ();
        float angle = (float) Math.atan2(vz, vx);
        angle = (float) (angle + (this.clockwise ? rotation : -rotation));
        double dx = Mth.cos(angle) * radius;
        double dz = Mth.sin(angle) * radius;
        double dy = Math.min(this.getBoundingBox().minY, toCircle.getY());
        return new Vec3(toCircle.getX() + dx, dy, toCircle.getZ() + dz);
    }

    public boolean hasTarget() {
        return this.targetEntity != null;
    }

    private @Nullable LivingEntity findTarget(ServerLevel level) {
        Player player = level.getNearestPlayer(this, 32.0);
        return player != null && !player.isCreative() && !player.isSpectator() && this.hasLineOfSight(player) && this.isEntityWithinHomeArea(player)
                ? player : null;
    }

    private void attackEntity(ServerLevel level, Entity target, float distance) {
        if (this.attackTime <= 0 && distance < 4.0F && target.getBoundingBox().maxY > this.getBoundingBox().minY - 2.5
                && target.getBoundingBox().minY < this.getBoundingBox().maxY + 2.5) {
            this.attackTime = 20;
            TFMonster.oldAttack(this, level, target, 6);
            if (this.moveSpeed > 0.8) {
                target.push(-Mth.sin(this.getYRot() * (float) Math.PI / 180.0F), 0.1, Mth.cos(this.getYRot() * (float) Math.PI / 180.0F));
                target.needsSync = true;
            }
        }
    }

    private void wanderRandomly() {
        this.moveSpeed = 0.6F;
        boolean found = false;
        int tx = -1;
        int ty = -1;
        int tz = -1;
        float worst = -99999.0F;
        for (int l = 0; l < 10; l++) {
            int dx = Mth.floor(this.getX() + this.random.nextInt(21) - 6.0);
            int dy = Mth.floor(this.getY() + this.random.nextInt(7) - 3.0);
            int dz = Mth.floor(this.getZ() + this.random.nextInt(21) - 6.0);
            if (!this.isWithinHomeDistance(dx, dy, dz)) {
                BlockPos home = this.getHomePosition();
                dx = home.getX() + this.random.nextInt(21) - this.random.nextInt(21);
                dy = home.getY() + this.random.nextInt(7) - this.random.nextInt(7);
                dz = home.getZ() + this.random.nextInt(21) - this.random.nextInt(21);
            }
            float weight = this.isWithinHomeDistance(dx, dy, dz) ? 0.0F : Float.MIN_VALUE;
            if (weight > worst) {
                worst = weight;
                tx = dx;
                ty = dy;
                tz = dz;
                found = true;
            }
        }
        if (found) {
            this.pathToEntity = this.getNavigation().createPath(tx, ty, tz, 0);
        }
    }

    private boolean isWithinHomeDistance(int x, int y, int z) {
        if (!this.hasHome()) {
            return true;
        }
        BlockPos home = this.getHomePosition();
        return Math.abs(home.getX() - x) <= LEASH_X && Math.abs(home.getY() - y) <= LEASH_Y && Math.abs(home.getZ() - z) <= LEASH_Z;
    }

    public boolean isEntityWithinHomeArea(Entity entity) {
        return this.isWithinHomeDistance(Mth.floor(entity.getX()), Mth.floor(entity.getY()), Mth.floor(entity.getZ()));
    }

    /** moveSegments: every segment two blocks behind its leader, pulled straight by its leader's heading. */
    private void moveSegments() {
        for (int i = 0; i < this.currentSegments; i++) {
            Entity leader = i == 0 ? this : this.body[i - 1];
            NagaSegment segment = this.body[i];
            float angle = (leader.getYRot() + 180.0F) * (float) Math.PI / 180.0F;
            double straighten = 0.05 + 1.0 / (i + 1) * 0.5;
            double idealX = -Mth.sin(angle) * straighten;
            double idealZ = Mth.cos(angle) * straighten;
            Vec3 diff = new Vec3(segment.getX() - leader.getX(), segment.getY() - leader.getY(), segment.getZ() - leader.getZ()).normalize()
                    .add(idealX, 0.0, idealZ).normalize();
            double f = 2.0;
            segment.setOldPosAndRot();
            segment.setPos(leader.getX() + f * diff.x, leader.getY() + f * diff.y, leader.getZ() + f * diff.z);
            segment.setDeltaMovement(diff.scale(f));
            double distance = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
            double dy = i == 0 ? diff.y - 0.15 : diff.y;
            segment.setSegmentRotation((float) (Math.atan2(diff.z, diff.x) * 180.0 / Math.PI) + 90.0F, -((float) (Math.atan2(dy, distance) * 180.0 / Math.PI)));
        }
    }

    // ---------------------------------------------------------------- damage, death, save

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.getDirectEntity() != null && !this.isEntityWithinHomeArea(source.getDirectEntity())) {
            return false;
        }
        if (source.getEntity() != null && !this.isEntityWithinHomeArea(source.getEntity())) {
            return false;
        }
        if (super.hurtServer(level, source, amount)) {
            this.setSegmentsPerHealth();
            if (source.getEntity() instanceof LivingEntity living && living != this) {
                this.targetEntity = living;
            }
            this.ticksSinceDamaged = 0;
            return true;
        }
        return false;
    }

    /** A segment's share: 2/3 of the damage, rounded; explosions and fire do nothing. */
    boolean hurtFromSegment(ServerLevel level, DamageSource source, float amount) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION) || source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            return false;
        }
        return this.hurtServer(level, source, Math.round(amount * 2.0F / 3.0F));
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.random.nextInt(3) != 0 ? TFBosses.NAGA_HISS.get() : TFBosses.NAGA_RATTLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TFBosses.NAGA_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TFBosses.NAGA_HURT.get();
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("HasHome", this.hasHome());
        if (this.hasHome()) {
            output.putInt("HomeX", this.getHomePosition().getX());
            output.putInt("HomeY", this.getHomePosition().getY());
            output.putInt("HomeZ", this.getHomePosition().getZ());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        if (input.getBooleanOr("HasHome", false)) {
            this.setHomeTo(new BlockPos(input.getIntOr("HomeX", 0), input.getIntOr("HomeY", 0), input.getIntOr("HomeZ", 0)), LEASH_X);
        }
        this.setSegmentsPerHealth();
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }
}
