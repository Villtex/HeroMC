package twilightforest.entity.boss.hydra;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.entity.PartEntity;
import org.jspecify.annotations.Nullable;
import twilightforest.TFHydra;

import java.util.ArrayList;
import java.util.List;

/**
 * EntityTFHydra ("Hydra", mob id 189, eggs 1321280 / 2719851): the boss of the fire swamp. 16 x 12, 360 health,
 * fire immune, 511 xp, never despawns; it does not walk, it only turns towards the nearest player within 48 (10
 * degrees per tick) while its seven heads ({@link HydraHeadContainer}, three active at first) bite (heads 0-2,
 * 4 to 10 away), breathe fire (within 20) and spit mortars (8 to 32, not at targets above it); heads that face away
 * from the target attack other living things within 20 on their side. Only 1 + heads × 0.3 (0.5 on hard) attacks
 * at a time, a bite counts three. It cannot be hit itself: its body, tail, necks and heads pass hits on, armoured
 * (damage / 8, rounded) unless it is a head with its mouth more than half open; nothing from further than 20 away
 * counts. A head that took more than 120 dies, and 100 ticks after it is dead it grows back, together with another
 * dead head. Heals 1 every 5 ticks once it has not been hurt for 1000 ticks. Breaks every block in its body, tail
 * and active heads (not obsidian, end stone, bedrock) and the floor under it when less than 60 % of it is solid.
 *
 * <p>Death takes 200 ticks: the heads die one by one every 20 ticks, then the xp. Drops (loot table): 5-7 (+ looting)
 * stacks of 5 hydra chops, 7-10 (+ looting) fiery blood, the trophy. Boss bar as the 1.5.2 BossStatus.
 */
public class Hydra extends Mob implements Enemy {
    private static final int TICKS_BEFORE_HEALING = 1000;
    private static final int HEAD_RESPAWN_TICKS = 100;
    private static final int HEAD_MAX_DAMAGE = 120;
    private static final float ARMOR_MULTIPLIER = 8.0F;
    public static final int MAX_HEALTH = 360;
    private static final int SECONDARY_FLAME_CHANCE = 10;
    private static final int SECONDARY_MORTAR_CHANCE = 16;

    public final int numHeads = 7;
    public final HydraPart body;
    public final HydraPart tail;
    public final HydraHeadContainer[] hc;
    private final HydraPart[] partArray;
    private final ServerBossEvent bossEvent = new ServerBossEvent(Mth.createInsecureUUID(this.random), this.getType().getDescription(),
            BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.PROGRESS);
    private float headsActivityFactor = 0.3F;
    private @Nullable Entity currentTarget;
    public int ticksSinceDamaged;
    private int numTicksToChaseTarget;
    private float randomYawVelocity;
    private boolean spawnHeads = true;
    private boolean killedByPlayer;

    public Hydra(EntityType<? extends Hydra> type, Level level) {
        super(type, level);
        this.body = new HydraPart(this, "body", 4.0F, 4.0F, false);
        this.tail = new HydraPart(this, "tail", 4.0F, 4.0F, false);
        this.hc = new HydraHeadContainer[this.numHeads];
        List<HydraPart> parts = new ArrayList<>();
        parts.add(this.body);
        parts.add(this.tail);
        for (int i = 0; i < this.numHeads; i++) {
            this.hc[i] = new HydraHeadContainer(this, i, i < 3);
            parts.addAll(List.of(this.hc[i].getNeckArray()));
        }
        this.partArray = parts.toArray(new HydraPart[0]);
        this.xpReward = 511;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    // ---------------------------------------------------------------- parts

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return this.partArray;
    }

    public HydraPart[] getHydraParts() {
        return this.partArray;
    }

    /** On the server the parts got the ids right after the hydra's own (as the dragon's); the client does the same. */
    @Override
    public void setId(int id) {
        super.setId(id);
        if (this.partArray != null) {
            for (int i = 0; i < this.partArray.length; i++) {
                this.partArray[i].setId(id + i + 1);
            }
        }
    }

    // ---------------------------------------------------------------- onLivingUpdate

    boolean isHard() {
        return this.level().getDifficulty() == Difficulty.HARD;
    }

    @Override
    public void aiStep() {
        Level level = this.level();
        if ((this.hc[0].headEntity == null || this.hc[1].headEntity == null || this.hc[2].headEntity == null) && this.spawnHeads
                && level instanceof ServerLevel server) {
            for (int i = 0; i < this.numHeads; i++) {
                HydraHead head = TFHydra.HYDRA_HEAD.get().create(server, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
                if (head != null) {
                    head.setPartName("head" + i);
                    head.hydraObj = this;
                    head.setPos(this.getX(), this.getY(), this.getZ());
                    this.hc[i].headEntity = head;
                    server.addFreshEntity(head);
                }
            }
            this.spawnHeads = false;
        }
        this.body.tickPart();
        for (int i = 0; i < this.numHeads; i++) {
            this.hc[i].onUpdate();
        }
        if (this.hurtTime > 0) {
            for (int i = 0; i < this.numHeads; i++) {
                this.hc[i].setHurtTime(this.hurtTime);
            }
        }
        this.ticksSinceDamaged++;
        if (!level.isClientSide() && this.ticksSinceDamaged > TICKS_BEFORE_HEALING && this.ticksSinceDamaged % 5 == 0) {
            this.heal(1.0F);
        }
        this.headsActivityFactor = this.isHard() ? 0.5F : 0.3F;

        super.aiStep();
        this.randomYawVelocity *= 0.9F;
        this.yHeadRot = this.getYRot();

        this.body.setSize(6.0F, 6.0F);
        this.tail.setSize(6.0F, 2.0F);
        float angle = (this.yBodyRot + 180.0F) * 3.141593F / 180.0F;
        this.body.place(this.getX() - Mth.sin(angle) * 3.0, this.getY() + 0.1, this.getZ() + Mth.cos(angle) * 3.0, this.yBodyRot, 0.0F);
        this.tail.setOldPosAndRot();
        this.tail.place(this.getX() - Mth.sin(angle) * 10.5, this.getY() + 0.1, this.getZ() + Mth.cos(angle) * 10.5, this.yBodyRot, 0.0F);

        if (level instanceof ServerLevel server) {
            if (this.hurtTime == 0) {
                this.collideWithEntities(level.getEntities(this, this.body.getBoundingBox()), this.body);
                this.collideWithEntities(level.getEntities(this, this.tail.getBoundingBox()), this.tail);
            }
            this.destroyBlocksInAABB(server, this.body.getBoundingBox());
            this.destroyBlocksInAABB(server, this.tail.getBoundingBox());
            for (int i = 0; i < this.numHeads; i++) {
                if (this.hc[i].headEntity != null && this.hc[i].isActive()) {
                    this.destroyBlocksInAABB(server, this.hc[i].headEntity.getBoundingBox());
                }
            }
            if (this.tickCount % 20 == 0 && this.isUnsteadySurfaceBeneath()) {
                this.destroyBlocksInAABB(server, this.getBoundingBox().move(0.0, -1.0, 0.0));
            }
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }
    }

    /** EntityLiving.onUpdate of 1.5.2: the body follows the yaw, at most 75 degrees behind it. */
    @Override
    protected void tickHeadTurn(float yBodyRotT) {
        float turn = Mth.wrapDegrees(yBodyRotT - this.yBodyRot);
        this.yBodyRot += turn * 0.3F;
        float diff = Mth.wrapDegrees(this.getYRot() - this.yBodyRot);
        if (diff < -75.0F) {
            diff = -75.0F;
        }
        if (diff >= 75.0F) {
            diff = 75.0F;
        }
        this.yBodyRot = this.getYRot() - diff;
        if (diff * diff > 2500.0F) {
            this.yBodyRot += diff * 0.2F;
        }
    }

    // ---------------------------------------------------------------- updateEntityActionState

    @Override
    protected void customServerAiStep(ServerLevel level) {
        this.xxa = 0.0F;
        this.zza = 0.0F;
        float f = 48.0F;
        for (int i = 0; i < this.numHeads; i++) {
            if (this.hc[i].isActive() && this.hc[i].getDamageTaken() > HEAD_MAX_DAMAGE) {
                this.hc[i].setNextState(HydraHeadContainer.STATE_DYING);
                this.hc[i].endCurrentAction();
                this.hc[i].setRespawnCounter(HEAD_RESPAWN_TICKS);
                int otherHead = this.getRandomDeadHead();
                if (otherHead != -1) {
                    this.hc[otherHead].setRespawnCounter(HEAD_RESPAWN_TICKS);
                }
            }
        }
        if (this.random.nextFloat() < 0.7F) {
            Player player = level.getNearestPlayer(this, f);
            if (player != null) {
                this.currentTarget = player;
                this.numTicksToChaseTarget = 100 + this.random.nextInt(20);
            } else {
                this.randomYawVelocity = (this.random.nextFloat() - 0.5F) * 20.0F;
            }
        }
        if (this.currentTarget != null) {
            this.faceEntity(this.currentTarget, 10.0F, this.getVerticalFaceSpeed());
            for (int i = 0; i < this.numHeads; i++) {
                if (!this.isHeadAttacking(this.hc[i]) && !this.hc[i].isSecondaryAttacking) {
                    this.hc[i].setTargetEntity(this.currentTarget);
                }
            }
            if (this.currentTarget.isAlive()) {
                float distance = this.currentTarget.distanceTo(this);
                if (this.hasLineOfSight(this.currentTarget)) {
                    this.attackEntity(this.currentTarget, distance);
                }
            }
            if (this.numTicksToChaseTarget-- <= 0 || this.currentTarget.isRemoved() || this.currentTarget.distanceToSqr(this) > f * f) {
                this.currentTarget = null;
            }
        } else {
            if (this.random.nextFloat() < 0.05F) {
                this.randomYawVelocity = (this.random.nextFloat() - 0.5F) * 20.0F;
            }
            this.setYRot(this.getYRot() + this.randomYawVelocity);
            this.setXRot(0.0F);
            for (int i = 0; i < this.numHeads; i++) {
                if (this.hc[i].currentState == HydraHeadContainer.STATE_IDLE) {
                    this.hc[i].setTargetEntity(null);
                }
            }
        }
        this.secondaryAttacks(level);
        if (this.isInWater() || this.isInLava()) {
            this.setJumping(this.random.nextFloat() < 0.8F);
        }
    }

    /** EntityLiving.faceEntity of 1.5.2 (eye to eye, or to the middle of a non-living entity). */
    private void faceEntity(Entity entity, float maxYaw, float maxPitch) {
        double dx = entity.getX() - this.getX();
        double dz = entity.getZ() - this.getZ();
        double dy;
        if (entity instanceof LivingEntity living) {
            dy = living.getEyeY() - this.getEyeY();
        } else {
            dy = (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0 - this.getEyeY();
        }
        double d = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(dy, d) * 180.0 / Math.PI));
        this.setXRot(oldUpdateRotation(this.getXRot(), pitch, maxPitch));
        this.setYRot(oldUpdateRotation(this.getYRot(), yaw, maxYaw));
    }

    private static float oldUpdateRotation(float current, float intended, float max) {
        float delta = Mth.wrapDegrees(intended - current);
        if (delta > max) {
            delta = max;
        }
        if (delta < -max) {
            delta = -max;
        }
        return current + delta;
    }

    public int getVerticalFaceSpeed() {
        return 500;
    }

    private int getRandomDeadHead() {
        for (int i = 0; i < this.numHeads; i++) {
            if (this.hc[i].currentState == HydraHeadContainer.STATE_DEAD && this.hc[i].respawnCounter == -1) {
                return i;
            }
        }
        return -1;
    }

    private void activateNumberOfHeads(int howMany) {
        int moreHeads = howMany - this.countActiveHeads();
        for (int i = 0; i < moreHeads; i++) {
            int otherHead = this.getRandomDeadHead();
            if (otherHead != -1) {
                this.hc[otherHead].currentState = HydraHeadContainer.STATE_IDLE;
                this.hc[otherHead].setNextState(HydraHeadContainer.STATE_IDLE);
                this.hc[otherHead].endCurrentAction();
            }
        }
    }

    private void attackEntity(Entity target, float distance) {
        int biteChance = 10;
        int flameChance = 100;
        int mortarChance = 160;
        boolean targetAbove = target.getBoundingBox().minY > this.getBoundingBox().maxY;
        for (int i = 0; i < 3; i++) {
            if (this.hc[i].currentState == HydraHeadContainer.STATE_IDLE && !this.areTooManyHeadsAttacking(i)) {
                if (distance > 4.0F && distance < 10.0F && this.random.nextInt(biteChance) == 0 && this.countActiveHeads() > 2
                        && !this.areOtherHeadsBiting(i)) {
                    this.hc[i].setNextState(HydraHeadContainer.STATE_BITE_BEGINNING);
                } else if (distance > 0.0F && distance < 20.0F && this.random.nextInt(flameChance) == 0) {
                    this.hc[i].setNextState(HydraHeadContainer.STATE_FLAME_BEGINNING);
                } else if (distance > 8.0F && distance < 32.0F && !targetAbove && this.random.nextInt(mortarChance) == 0) {
                    this.hc[i].setNextState(HydraHeadContainer.STATE_MORTAR_BEGINNING);
                }
            }
        }
        for (int i = 3; i < this.numHeads; i++) {
            if (this.hc[i].currentState == HydraHeadContainer.STATE_IDLE && !this.areTooManyHeadsAttacking(i)) {
                if (distance > 0.0F && distance < 20.0F && this.random.nextInt(flameChance) == 0) {
                    this.hc[i].setNextState(HydraHeadContainer.STATE_FLAME_BEGINNING);
                } else if (distance > 8.0F && distance < 32.0F && !targetAbove && this.random.nextInt(mortarChance) == 0) {
                    this.hc[i].setNextState(HydraHeadContainer.STATE_MORTAR_BEGINNING);
                }
            }
        }
    }

    protected boolean areTooManyHeadsAttacking(int testHead) {
        int otherAttacks = 0;
        for (int i = 0; i < this.numHeads; i++) {
            if (i != testHead && this.isHeadAttacking(this.hc[i])) {
                otherAttacks++;
                if (this.isHeadBiting(this.hc[i])) {
                    otherAttacks += 2;
                }
            }
        }
        return otherAttacks >= 1.0F + this.countActiveHeads() * this.headsActivityFactor;
    }

    public int countActiveHeads() {
        int count = 0;
        for (int i = 0; i < this.numHeads; i++) {
            if (this.hc[i].isActive()) {
                count++;
            }
        }
        return count;
    }

    private boolean isHeadAttacking(HydraHeadContainer head) {
        int s = head.currentState;
        return s == HydraHeadContainer.STATE_BITE_BEGINNING || s == HydraHeadContainer.STATE_BITE_READY || s == HydraHeadContainer.STATE_BITE_BITING
                || s == HydraHeadContainer.STATE_FLAME_BEGINNING || s == HydraHeadContainer.STATE_FLAME_BREATHING
                || s == HydraHeadContainer.STATE_MORTAR_BEGINNING || s == HydraHeadContainer.STATE_MORTAR_LAUNCH;
    }

    protected boolean areOtherHeadsBiting(int testHead) {
        for (int i = 0; i < this.numHeads; i++) {
            if (i != testHead && this.isHeadBiting(this.hc[i])) {
                return true;
            }
        }
        return false;
    }

    protected boolean isHeadBiting(HydraHeadContainer head) {
        int s = head.currentState;
        return s == HydraHeadContainer.STATE_BITE_BEGINNING || s == HydraHeadContainer.STATE_BITE_READY || s == HydraHeadContainer.STATE_BITE_BITING
                || head.nextState == HydraHeadContainer.STATE_BITE_BEGINNING;
    }

    private void secondaryAttacks(ServerLevel level) {
        for (int i = 0; i < this.numHeads; i++) {
            if (this.hc[i].headEntity == null) {
                return;
            }
        }
        LivingEntity secondaryTarget = this.findSecondaryTarget(level, 20.0);
        if (secondaryTarget != null) {
            float distance = secondaryTarget.distanceTo(this);
            for (int i = 1; i < this.numHeads; i++) {
                if (this.hc[i].isActive() && this.hc[i].currentState == HydraHeadContainer.STATE_IDLE && this.isTargetOnThisSide(i, secondaryTarget)) {
                    if (distance > 0.0F && distance < 20.0F && this.random.nextInt(SECONDARY_FLAME_CHANCE) == 0) {
                        this.hc[i].setTargetEntity(secondaryTarget);
                        this.hc[i].isSecondaryAttacking = true;
                        this.hc[i].setNextState(HydraHeadContainer.STATE_FLAME_BEGINNING);
                    } else if (distance > 8.0F && distance < 32.0F && this.random.nextInt(SECONDARY_MORTAR_CHANCE) == 0) {
                        this.hc[i].setTargetEntity(secondaryTarget);
                        this.hc[i].isSecondaryAttacking = true;
                        this.hc[i].setNextState(HydraHeadContainer.STATE_MORTAR_BEGINNING);
                    }
                }
            }
        }
    }

    public boolean isTargetOnThisSide(int headNum, Entity target) {
        return distanceSqXZ(this.hc[headNum].headEntity, target) < distanceSqXZ(this, target);
    }

    private static double distanceSqXZ(Entity a, Entity b) {
        double distX = a.getX() - b.getX();
        double distZ = a.getZ() - b.getZ();
        return distX * distX + distZ * distZ;
    }

    public @Nullable LivingEntity findSecondaryTarget(ServerLevel level, double range) {
        double closestRange = -1.0;
        LivingEntity closestEntity = null;
        AABB box = new AABB(this.getX(), this.getY(), this.getZ(), this.getX() + 1.0, this.getY() + 1.0, this.getZ() + 1.0).inflate(range);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (!(nearby instanceof Hydra) && !(nearby instanceof HydraHead) && nearby != this.currentTarget && !nearby.isSpectator()
                    && !this.isAnyHeadTargeting(nearby) && this.hasLineOfSight(nearby)) {
                double curDist = nearby.distanceToSqr(this.getX(), this.getY(), this.getZ());
                if ((range < 0.0 || curDist < range * range) && (closestRange == -1.0 || curDist < closestRange)) {
                    closestRange = curDist;
                    closestEntity = nearby;
                }
            }
        }
        return closestEntity;
    }

    boolean isAnyHeadTargeting(Entity targetEntity) {
        for (int i = 0; i < this.numHeads; i++) {
            if (this.hc[i].targetEntity != null && this.hc[i].targetEntity.equals(targetEntity)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Pushes living things out of the body and the tail. Players are left out: in 1.5.2 addVelocity on the server
     * never reached a player's client (no velocity packet without a hit).
     */
    private void collideWithEntities(List<Entity> list, Entity part) {
        double pushPower = 4.0;
        double centerX = (part.getBoundingBox().minX + part.getBoundingBox().maxX) / 2.0;
        double centerZ = (part.getBoundingBox().minZ + part.getBoundingBox().maxZ) / 2.0;
        for (Entity entity : list) {
            if (entity instanceof LivingEntity && !(entity instanceof Player) && !(entity instanceof HydraHead)) {
                double distX = entity.getX() - centerX;
                double distZ = entity.getZ() - centerZ;
                double sqDist = distX * distX + distZ * distZ;
                entity.push(distX / sqDist * pushPower, 0.2F, distZ / sqDist * pushPower);
            }
        }
    }

    private boolean isUnsteadySurfaceBeneath() {
        AABB bb = this.getBoundingBox();
        int minX = Mth.floor(bb.minX);
        int minZ = Mth.floor(bb.minZ);
        int maxX = Mth.floor(bb.maxX);
        int maxZ = Mth.floor(bb.maxZ);
        int dy = Mth.floor(bb.minY) - 1;
        int solid = 0;
        int total = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = minX; dx <= maxX; dx++) {
            for (int dz = minZ; dz <= maxZ; dz++) {
                total++;
                if (this.level().getBlockState(pos.set(dx, dy, dz)).isSolid()) {
                    solid++;
                }
            }
        }
        return (float) solid / total < 0.6F;
    }

    /** Sets every block in the box to air with the break effect (flag 2), except obsidian, end stone and bedrock. */
    private boolean destroyBlocksInAABB(ServerLevel level, AABB box) {
        int minX = Mth.floor(box.minX);
        int minY = Mth.floor(box.minY);
        int minZ = Mth.floor(box.minZ);
        int maxX = Mth.floor(box.maxX);
        int maxY = Mth.floor(box.maxY);
        int maxZ = Mth.floor(box.maxZ);
        boolean wasBlocked = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = minX; dx <= maxX; dx++) {
            for (int dy = minY; dy <= maxY; dy++) {
                for (int dz = minZ; dz <= maxZ; dz++) {
                    pos.set(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (!state.isAir()) {
                        if (!state.is(Blocks.OBSIDIAN) && !state.is(Blocks.END_STONE) && !state.is(Blocks.BEDROCK)
                                && state.getDestroySpeed(level, pos) >= 0.0F) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
                        } else {
                            wasBlocked = true;
                        }
                    }
                }
            }
        }
        return wasBlocked;
    }

    // ---------------------------------------------------------------- damage

    /** attackEntityFromPart(EntityTFHydraPart / EntityDragonPart): hits on the heads, necks, body and tail. */
    boolean attackEntityFromPart(Entity part, ServerLevel level, DamageSource source, float damage) {
        if (source.is(DamageTypes.IN_WALL)) {
            this.destroyBlocksInAABB(level, part.getBoundingBox());
        }
        double range = this.calculateRange(source);
        if (range > 400.0) {
            return false;
        }
        int damageAmount = Math.round(damage);
        if (!(part instanceof HydraHead) && !(part instanceof HydraPart p && p.isNeck())) {
            // EntityDragonPart (body, tail): always armoured
            return this.superAttackFrom(level, source, Math.round(damageAmount / ARMOR_MULTIPLIER));
        }
        HydraHeadContainer headCon = null;
        for (int i = 0; i < this.numHeads; i++) {
            if (this.hc[i].headEntity == part) {
                headCon = this.hc[i];
            }
        }
        if (headCon != null && !headCon.isActive()) {
            return false;
        }
        boolean tookDamage;
        if (headCon != null && headCon.getCurrentMouthOpen() > 0.5) {
            tookDamage = this.superAttackFrom(level, source, damageAmount);
            headCon.addDamage(damageAmount);
        } else {
            int armoredDamage = Math.round(damageAmount / ARMOR_MULTIPLIER);
            tookDamage = this.superAttackFrom(level, source, armoredDamage);
            if (headCon != null) {
                headCon.addDamage(armoredDamage);
            }
        }
        if (tookDamage) {
            this.ticksSinceDamaged = 0;
        }
        return tookDamage;
    }

    private boolean superAttackFrom(ServerLevel level, DamageSource source, int damage) {
        return super.hurtServer(level, source, damage);
    }

    protected double calculateRange(DamageSource source) {
        double range = -1.0;
        if (source.getDirectEntity() != null) {
            range = this.distanceToSqr(source.getDirectEntity());
        }
        if (source.getEntity() != null) {
            range = this.distanceToSqr(source.getEntity());
        }
        return range;
    }

    /** The hydra itself cannot be hit (only /kill gets through). */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return source.is(DamageTypes.GENERIC_KILL) && super.hurtServer(level, source, damage);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void knockback(double strength, double x, double z, DamageSource source, float damage, boolean byAttack) {
    }

    @Override
    public void knockback(double strength, double x, double z, DamageSource source, float damage) {
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    // ---------------------------------------------------------------- sounds

    @Override
    protected SoundEvent getAmbientSound() {
        return TFHydra.HYDRA_GROWL.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TFHydra.HYDRA_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TFHydra.HYDRA_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 2.0F;
    }

    // ---------------------------------------------------------------- death

    @Override
    public void die(DamageSource source) {
        this.killedByPlayer = this.lastHurtByPlayerMemoryTime > 0;
        super.die(source);
    }

    /** The xp comes at the end of the death (deathTime 200), see {@link #tickDeath}. */
    @Override
    protected void dropExperience(ServerLevel level, @Nullable Entity killer) {
    }

    /** onDeathUpdate: 200 ticks, a head dies every 20 ticks, explosions all over the body. */
    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime == 1) {
            for (int i = 0; i < this.numHeads; i++) {
                this.hc[i].setRespawnCounter(-1);
                if (this.hc[i].isActive()) {
                    this.hc[i].setNextState(HydraHeadContainer.STATE_IDLE);
                    this.hc[i].endCurrentAction();
                    this.hc[i].setHurtTime(200);
                }
            }
        }
        if (this.deathTime <= 140 && this.deathTime % 20 == 0) {
            int headToDie = this.deathTime / 20 - 1;
            if (this.hc[headToDie].isActive()) {
                this.hc[headToDie].setNextState(HydraHeadContainer.STATE_DYING);
                this.hc[headToDie].endCurrentAction();
            }
        }
        if (this.deathTime == 200) {
            // 1.5.2 asked for recentlyHit here, which had run out 100 ticks after the last hit (so no xp ever)
            if (this.level() instanceof ServerLevel server && this.killedByPlayer && server.getGameRules().get(GameRules.MOB_DROPS)) {
                ExperienceOrb.award(server, this.position(), this.getExperienceReward(server, this.getLastHurtByPlayer()));
            }
            if (!this.level().isClientSide()) {
                this.remove(RemovalReason.KILLED);
            }
        }
        if (this.level().isClientSide()) {
            float width = this.body.getBbWidth();
            float height = this.body.getBbHeight();
            for (int i = 0; i < 20; i++) {
                double vx = this.random.nextGaussian() * 0.02;
                double vy = this.random.nextGaussian() * 0.02;
                double vz = this.random.nextGaussian() * 0.02;
                ParticleOptions particle = this.random.nextInt(2) == 0 ? ParticleTypes.EXPLOSION : ParticleTypes.POOF;
                this.level().addParticle(particle, this.getX() + this.random.nextFloat() * width * 2.0F - width,
                        this.getY() + this.random.nextFloat() * height, this.getZ() + this.random.nextFloat() * width * 2.0F - width, vx, vy, vz);
            }
        }
    }

    // ---------------------------------------------------------------- boss bar, NBT

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
        output.putBoolean("SpawnHeads", this.spawnHeads);
        output.putByte("NumHeads", (byte) this.countActiveHeads());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.spawnHeads = input.getBooleanOr("SpawnHeads", true);
        this.activateNumberOfHeads(input.getByteOr("NumHeads", (byte) 3));
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }
}
