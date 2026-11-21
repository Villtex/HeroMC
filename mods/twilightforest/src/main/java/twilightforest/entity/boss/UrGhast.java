package twilightforest.entity.boss;

import hexxit.worldgen152.OldGameIds;
import hexxit.worldgen152.core.populate.OldStack;
import hexxit.worldgen152.core.populate.OldTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import twilightforest.TFEntities;
import twilightforest.TFTower;
import twilightforest.TwilightForest;
import twilightforest.entity.MiniGhast;
import twilightforest.entity.TowerGhast;
import twilightforest.gen.TFTreasure;

import java.util.ArrayList;
import java.util.List;

/**
 * EntityTFUrGhast ("Tower Boss"): the ur-ghast of the dark tower. 250 health, 14 × 18, no clipping, 317 xp, never
 * despawns. It flies its course over the ghast traps it finds (48 / 32 around it, twice), 20 above each; after every
 * round without enough mini ghasts (4) at a trap it calls 6 to two traps with lightning. Every 45 / 48 damage it
 * switches phase: a tantrum (rain and thunder for 6000 ticks, a quarter of the damage, tears, cries, 3 damage every 10
 * ticks to players under the open sky below it, pushes its minions up) or calm, when the trap holds it (stopTantrum).
 * It eats mini ghasts it touches (2 health each). Three fireballs at once (16 damage, explosion 1, fire). When it dies
 * the boss chest (TFTreasure darktower_boss) appears 2 above the centre of the traps.
 */
public class UrGhast extends TowerGhast {
    private static final EntityDataAccessor<Boolean> DATA_TANTRUM = SynchedEntityData.defineId(UrGhast.class, EntityDataSerializers.BOOLEAN);
    private final ServerBossEvent bossEvent = new ServerBossEvent(this.getUUID(), this.getDisplayName(), BossEvent.BossBarColor.PINK,
            BossEvent.BossBarOverlay.PROGRESS);
    private double courseX;
    private double courseY;
    private double courseZ;
    private final List<BlockPos> trapLocations = new ArrayList<>();
    private final List<BlockPos> travelCoords = new ArrayList<>();
    private int currentTravelCoordIndex;
    private int travelPathRepetitions;
    private int nextTantrumCry;
    private int damageUntilNextPhase = 45;
    private boolean noTrapMode;

    public UrGhast(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.aggroRange = 128.0F;
        this.wanderFactor = 32.0F;
        this.noPhysics = true;
        this.xpReward = 317;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TowerGhast.createAttributes().add(Attributes.MAX_HEALTH, 250.0).add(Attributes.FOLLOW_RANGE, 128.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TANTRUM, false);
    }

    public boolean isInTantrum() {
        return this.entityData.get(DATA_TANTRUM);
    }

    public void setInTantrum(boolean tantrum) {
        this.entityData.set(DATA_TANTRUM, tantrum);
        this.damageUntilNextPhase = 48;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    protected float getSoundVolume() {
        return 16.0F;
    }

    @Override
    public float getVoicePitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 0.5F;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.deathTime > 0 && this.level().isClientSide()) {
            for (int k = 0; k < 5; k++) {
                this.level().addParticle(this.random.nextBoolean() ? ParticleTypes.EXPLOSION_EMITTER : ParticleTypes.POOF,
                        this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
                        this.getY() + this.random.nextFloat() * this.getBbHeight(),
                        this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
                        this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02);
            }
        }
        if (this.isInTantrum() && this.level().isClientSide()) {
            // shedTear: TFClientProxy "bosstear", a falling ghast tear
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, Items.GHAST_TEAR),
                    this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(), this.getY() + this.random.nextDouble() * this.getBbHeight() - 0.25,
                    this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(), 0.0, 0.0, 0.0);
        }
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_WALL)) {
            return false;
        }
        float damage = this.isInTantrum() ? (int) amount / 4 : amount;
        boolean hurt;
        if (source.is(DamageTypes.FIREBALL) && source.getEntity() instanceof Player) {
            hurt = super.hurtServer(level, this.damageSources().thrown(source.getDirectEntity(), source.getEntity()), damage);
        } else {
            hurt = super.hurtServer(level, source, damage);
        }
        if (hurt && this.hurtTime == this.hurtDuration) {
            this.damageUntilNextPhase -= (int) this.lastHurt;
            TwilightForest.LOGGER.debug("Attack successful, {} damage until phase switch.", this.damageUntilNextPhase);
            if (this.damageUntilNextPhase <= 0) {
                if (this.isInTantrum()) {
                    this.stopTantrum();
                } else {
                    this.startTantrum(level);
                }
                this.damageUntilNextPhase = 48;
            }
        }
        return hurt;
    }

    private void startTantrum(ServerLevel level) {
        this.setInTantrum(true);
        level.getServer().setWeatherParameters(0, 6000, true, true);
        this.spawnGhastsAtTraps(level);
    }

    public void stopTantrum() {
        this.setInTantrum(false);
    }

    private void spawnGhastsAtTraps(ServerLevel level) {
        List<BlockPos> spawns = new ArrayList<>(this.trapLocations);
        int count = Math.min(2, spawns.size());
        for (int i = 0; i < count; i++) {
            BlockPos trap = spawns.remove(this.random.nextInt(spawns.size()));
            this.spawnMinionGhastsAt(level, trap.getX(), trap.getY(), trap.getZ());
        }
    }

    private void spawnMinionGhastsAt(ServerLevel level, int x, int y, int z) {
        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.EVENT);
        if (bolt != null) {
            bolt.snapTo(x, y + 4, z);
            level.addFreshEntity(bolt);
        }
        int spawns = 0;
        for (int i = 0; i < 24; i++) {
            MiniGhast minion = TFEntities.MINI_GHAST.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (minion == null) {
                break;
            }
            double sx = x + (this.random.nextDouble() - this.random.nextDouble()) * 4;
            double sy = y + this.random.nextDouble() * 8;
            double sz = z + (this.random.nextDouble() - this.random.nextDouble()) * 4;
            minion.snapTo(sx, sy, sz, level.getRandom().nextFloat() * 360.0F, 0.0F);
            minion.makeBossMinion();
            if (level.getDifficulty() != Difficulty.PEACEFUL && level.noCollision(minion) && !level.containsAnyLiquid(minion.getBoundingBox())) {
                level.addFreshEntity(minion);
                level.broadcastEntityEvent(minion, (byte) 20);
            }
            if (++spawns >= 6) {
                break;
            }
        }
    }

    /** updateEntityActionState of the ur-ghast (instead of the tower ghast's). */
    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            this.discard();
            return;
        }
        for (MiniGhast ghast : level.getEntitiesOfClass(MiniGhast.class, this.getBoundingBox().inflate(1.0))) {
            ghast.discard();
            this.heal(2.0F);
        }
        if (this.trapLocations.isEmpty() && !this.noTrapMode) {
            this.scanForTrapsTwice(level);
        }
        if (this.trapLocations.isEmpty() && !this.noTrapMode) {
            TwilightForest.LOGGER.info("[TwilightForest] Ur-ghast cannot find traps nearby, entering trap-less mode");
            this.noTrapMode = true;
        }
        if (this.inTrapCounter > 0) {
            this.inTrapCounter--;
            this.targeted = null;
            return;
        }
        this.prevAttackCounter = this.attackCounter;
        if (this.targeted != null && !this.targeted.isAlive()) {
            this.targeted = null;
        }
        if (this.targeted == null) {
            this.targeted = this.findPlayerInRange();
        } else if (!this.isAggressive && this.targeted instanceof Player player) {
            this.checkToIncreaseAggro(player);
        }
        if (this.isInTantrum()) {
            this.targeted = null;
            if (--this.nextTantrumCry <= 0) {
                this.playSound(SoundEvents.GHAST_HURT, this.getSoundVolume(), this.getVoicePitch());
                this.nextTantrumCry = 20 + this.random.nextInt(30);
            }
            if (this.tickCount % 10 == 0) {
                this.doTantrumDamageEffects(level);
            }
        }
        this.checkAndChangeCourse(level);
        double offsetX = this.waypointX - this.getX();
        double offsetY = this.waypointY - this.getY();
        double offsetZ = this.waypointZ - this.getZ();
        double distance = offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ;
        if (distance < 1.0 || distance > 3600.0) {
            this.makeNewWaypoint();
        }
        if (this.courseChangeCooldown-- <= 0) {
            this.courseChangeCooldown += this.random.nextInt(5);
            distance = Math.sqrt(distance);
            this.setDeltaMovement(this.getDeltaMovement().add(offsetX / distance * 0.05, offsetY / distance * 0.05, offsetZ / distance * 0.05));
        }
        double targetRange = this.aggroCounter <= 0 && !this.isAggressive ? this.stareRange : this.aggroRange;
        if (this.targeted != null && this.targeted.distanceToSqr(this) < targetRange * targetRange && this.hasLineOfSight(this.targeted)) {
            this.lookAt(this.targeted, 10.0F, this.getMaxHeadXRot());
            if (this.isAggressive) {
                if (this.attackCounter == 10) {
                    this.playSound(SoundEvents.GHAST_WARN, this.getSoundVolume(), this.getVoicePitch());
                }
                this.attackCounter++;
                if (this.attackCounter == 20) {
                    this.spitFireball(level);
                    this.attackCounter = -40;
                }
            }
        } else {
            this.isAggressive = false;
            this.targeted = null;
            Vec3 m = this.getDeltaMovement();
            float yaw = -((float) Math.atan2(m.x, m.z)) * 180.0F / (float) Math.PI;
            this.setYRot(yaw);
            this.yBodyRot = yaw;
            this.setXRot(0.0F);
        }
        if (this.attackCounter > 0 && !this.isAggressive) {
            this.attackCounter--;
        }
        byte status = (byte) (this.attackCounter > 10 ? 2 : this.aggroCounter <= 0 && !this.isAggressive ? 0 : 1);
        if (this.aggroStatus() != status) {
            this.setAggroStatus(status);
        }
    }

    private void doTantrumDamageEffects(ServerLevel level) {
        AABB below = this.getBoundingBox().move(0.0, -16.0, 0.0).inflate(0.0, 16.0, 0.0);
        for (Player player : level.getEntitiesOfClass(Player.class, below)) {
            if (level.canSeeSky(player.blockPosition())) {
                player.hurtServer(level, this.damageSources().fallingBlock(this), 3.0F);
            }
        }
        for (MiniGhast ghast : level.getEntitiesOfClass(MiniGhast.class, below)) {
            ghast.setDeltaMovement(ghast.getDeltaMovement().add(0.0, 1.0, 0.0));
        }
    }

    private void makeNewWaypoint() {
        double closest = this.distanceToSqr(this.courseX, this.courseY, this.courseZ);
        for (int i = 0; i < 50; i++) {
            double px = this.getX() + (this.random.nextFloat() * 2.0F - 1.0F) * this.wanderFactor;
            double py = this.courseY + this.random.nextFloat() * 8.0F - 4.0;
            double pz = this.getZ() + (this.random.nextFloat() * 2.0F - 1.0F) * this.wanderFactor;
            double ox = this.courseX - px;
            double oy = this.courseY - py;
            double oz = this.courseZ - pz;
            double d = ox * ox + oy * oy + oz * oz;
            if (d < closest) {
                this.waypointX = px;
                this.waypointY = py;
                this.waypointZ = pz;
                closest = d;
            }
        }
    }

    private void checkAndChangeCourse(ServerLevel level) {
        if (this.courseX == 0.0 && this.courseY == 0.0 && this.courseZ == 0.0) {
            this.changeCourse(level);
        }
        double ox = this.courseX - this.getX();
        double oy = this.courseY - this.getY();
        double oz = this.courseZ - this.getZ();
        if (ox * ox + oy * oy + oz * oz < 100.0) {
            this.changeCourse(level);
        }
    }

    private void changeCourse(ServerLevel level) {
        if (this.travelCoords.isEmpty()) {
            this.makeTravelPath();
        }
        if (!this.travelCoords.isEmpty()) {
            if (this.currentTravelCoordIndex >= this.travelCoords.size()) {
                this.currentTravelCoordIndex = 0;
                this.travelPathRepetitions++;
                if (!this.checkGhastsAtTraps(level)) {
                    this.spawnGhastsAtTraps(level);
                }
            }
            BlockPos next = this.travelCoords.get(this.currentTravelCoordIndex);
            this.courseX = next.getX();
            this.courseY = next.getY() + 20;
            this.courseZ = next.getZ();
            this.currentTravelCoordIndex++;
        }
    }

    private boolean checkGhastsAtTraps(ServerLevel level) {
        int enough = 0;
        for (BlockPos trap : this.trapLocations) {
            AABB box = new AABB(trap).inflate(8.0, 16.0, 8.0);
            if (level.getEntitiesOfClass(MiniGhast.class, box).size() >= 4) {
                enough++;
            }
        }
        return enough >= 1;
    }

    private void makeTravelPath() {
        int px = Mth.floor(this.getX());
        int py = Mth.floor(this.getY());
        int pz = Mth.floor(this.getZ());
        List<BlockPos> points;
        if (!this.noTrapMode) {
            points = new ArrayList<>(this.trapLocations);
        } else {
            points = new ArrayList<>(List.of(new BlockPos(px + 20, py - 20, pz), new BlockPos(px, py - 20, pz - 20), new BlockPos(px - 20, py - 20, pz),
                    new BlockPos(px, py - 20, pz + 20)));
        }
        this.travelCoords.clear();
        while (!points.isEmpty()) {
            this.travelCoords.add(points.remove(this.random.nextInt(points.size())));
        }
        if (this.noTrapMode) {
            this.travelCoords.add(new BlockPos(px, py - 20, pz));
        }
    }

    /** Three fireballs from 8.5 blocks ahead, two of them spread by up to 8 sideways; explosion power 1. */
    @Override
    protected void spitFireball(ServerLevel level) {
        if (this.targeted == null) {
            return;
        }
        double ox = this.targeted.getX() - this.getX();
        double oy = this.targeted.getBoundingBox().minY + this.targeted.getBbHeight() / 2.0F - (this.getY() + this.getBbHeight() / 2.0F);
        double oz = this.targeted.getZ() - this.getZ();
        level.levelEvent(null, 1016, this.blockPosition(), 0);
        Vec3 look = this.getViewVector(1.0F);
        for (int i = 0; i < 3; i++) {
            double sx = i == 0 ? ox : ox + (this.random.nextFloat() - this.random.nextFloat()) * 8.0F;
            double sz = i == 0 ? oz : oz + (this.random.nextFloat() - this.random.nextFloat()) * 8.0F;
            UrGhastFireball fireball = new UrGhastFireball(level, this, new Vec3(sx, oy, sz));
            fireball.setPos(this.getX() + look.x * 8.5, this.getY() + this.getBbHeight() / 2.0F + look.y * 8.5, this.getZ() + look.z * 8.5);
            level.addFreshEntity(fireball);
        }
    }

    private void scanForTrapsTwice(ServerLevel level) {
        int px = Mth.floor(this.getX());
        int py = Mth.floor(this.getY());
        int pz = Mth.floor(this.getZ());
        this.scanForTraps(level, 48, 32, px, py, pz);
        if (!this.trapLocations.isEmpty()) {
            BlockPos centre = this.trapCentre();
            this.scanForTraps(level, 48, 32, centre.getX(), centre.getY(), centre.getZ());
        }
    }

    private BlockPos trapCentre() {
        int ax = 0;
        int ay = 0;
        int az = 0;
        for (BlockPos trap : this.trapLocations) {
            ax += trap.getX();
            ay += trap.getY();
            az += trap.getZ();
        }
        int n = this.trapLocations.size();
        return new BlockPos(ax / n, ay / n, az / n);
    }

    private void scanForTraps(ServerLevel level, int rangeXZ, int rangeY, int px, int py, int pz) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int sx = -rangeXZ; sx <= rangeXZ; sx++) {
            for (int sz = -rangeXZ; sz <= rangeXZ; sz++) {
                for (int sy = -rangeY; sy <= rangeY; sy++) {
                    pos.set(px + sx, py + sy, pz + sz);
                    if (level.isLoaded(pos) && isTrap(level, pos) && !this.trapLocations.contains(pos)) {
                        this.trapLocations.add(pos.immutable());
                    }
                }
            }
        }
    }

    /** isTrapAt: tower device 10 or 11 (ghast trap, off or on). */
    private static boolean isTrap(ServerLevel level, BlockPos pos) {
        var block = level.getBlockState(pos).getBlock();
        return block == TFTower.DEVICES.get(10).get() || block == TFTower.DEVICES.get(11).get();
    }

    /** onDeathUpdate: at death tick 20 the boss chest. */
    @Override
    protected void tickDeath() {
        super.tickDeath();
        if (this.deathTime == 20 && this.level() instanceof ServerLevel level) {
            BlockPos chestPos = this.trapLocations.isEmpty() ? this.blockPosition() : this.trapCentre().above(2);
            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
                OldTile.Chest contents = TFTreasure.DARKTOWER_BOSS.contents(level.getSeed(), chestPos.getX(), chestPos.getY(), chestPos.getZ());
                OldStack[] slots = contents.slots();
                for (int i = 0; i < slots.length && i < chest.getContainerSize(); i++) {
                    if (slots[i] != null) {
                        try {
                            chest.setItem(i, OldGameIds.stack(slots[i], level.registryAccess()));
                        } catch (IllegalStateException e) {
                            TwilightForest.LOGGER.warn("Ur-ghast chest item left out: {}", e.getMessage());
                        }
                    }
                }
            }
        }
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
        output.putBoolean("inTantrum", this.isInTantrum());
        super.addAdditionalSaveData(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setInTantrum(input.getBooleanOr("inTantrum", false));
    }
}
