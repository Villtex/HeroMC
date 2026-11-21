package twilightforest.entity.boss;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import twilightforest.TFBosses;
import twilightforest.TFEntities;
import twilightforest.TFItems;
import twilightforest.entity.TFMonster;

import java.util.List;

/**
 * EntityTFLich: the boss of the lich tower. 100 health, 1.1 × 2.5, fire immune, undead, 217 xp, blaze sounds, hits for
 * 6. Phase 1 (shield of 5, scepter of twilight in hand): blocked hits only fizz, unblockable ones (> 2) break a shield;
 * every 100 ticks a bolt or (1 in 3) a bomb, at tick 60 a teleport and up to 2 shadow clones that shoot too. Phase 2
 * (zombie scepter): the clones vanish, up to 3 of 9 minions at a time, bolts / bombs / melee / teleport every 60 ticks.
 * Phase 3 (golden sword): melee. Every 15 ticks it pops one plain skeleton, zombie, enderman, spider, creeper or swarm
 * spider in sight. Hurt, it teleports into sight of its target (in phase 3 1 in 4). Moves with the old EntityCreature
 * AI (updateEntityActionState).
 */
public class Lich extends Monster {
    private static final EntityDataAccessor<Boolean> DATA_CLONE = SynchedEntityData.defineId(Lich.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> DATA_SHIELD = SynchedEntityData.defineId(Lich.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_MINIONS = SynchedEntityData.defineId(Lich.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_ATTACK_TYPE = SynchedEntityData.defineId(Lich.class, EntityDataSerializers.BYTE);
    public static final int MAX_SHADOW_CLONES = 2;
    public static final int INITIAL_SHIELD_STRENGTH = 5;
    public static final int MAX_ACTIVE_MINIONS = 3;
    public static final int INITIAL_MINIONS_TO_SUMMON = 9;

    private @Nullable Lich masterLich;
    private final ServerBossEvent bossEvent = new ServerBossEvent(this.getUUID(), this.getDisplayName(), BossEvent.BossBarColor.PINK,
            BossEvent.BossBarOverlay.PROGRESS);
    private @Nullable LivingEntity entityToAttack;
    private @Nullable Path pathToEntity;
    public int attackTime;
    private boolean hasAttacked;

    public Lich(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 217;
        this.moveControl = new MoveControl(this) {
            @Override
            public void tick() {
            }
        };
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    /** new EntityTFLich(world, master): a shadow clone. */
    public static Lich clone(ServerLevel level, Lich master) {
        Lich clone = TFBosses.LICH.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        clone.setShadowClone(true);
        clone.masterLich = master;
        return clone;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 100.0).add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CLONE, false);
        builder.define(DATA_SHIELD, (byte) INITIAL_SHIELD_STRENGTH);
        builder.define(DATA_MINIONS, (byte) INITIAL_MINIONS_TO_SUMMON);
        builder.define(DATA_ATTACK_TYPE, (byte) 0);
    }

    public boolean isShadowClone() {
        return this.entityData.get(DATA_CLONE);
    }

    public void setShadowClone(boolean clone) {
        this.entityData.set(DATA_CLONE, clone);
        this.bossEvent.setVisible(!clone);
    }

    public int getShieldStrength() {
        return this.entityData.get(DATA_SHIELD);
    }

    public void setShieldStrength(int shield) {
        this.entityData.set(DATA_SHIELD, (byte) shield);
    }

    public int getMinionsToSummon() {
        return this.entityData.get(DATA_MINIONS);
    }

    public void setMinionsToSummon(int minions) {
        this.entityData.set(DATA_MINIONS, (byte) minions);
    }

    public int getNextAttackType() {
        return this.entityData.get(DATA_ATTACK_TYPE);
    }

    public void setNextAttackType(int type) {
        this.entityData.set(DATA_ATTACK_TYPE, (byte) type);
    }

    public int getPhase() {
        if (this.isShadowClone() || this.getShieldStrength() > 0) {
            return 1;
        }
        return this.getMinionsToSummon() > 0 ? 2 : 3;
    }

    /** getHeldItem by phase: scepter of twilight, zombie scepter, golden sword. */
    private ItemStack heldItem() {
        return switch (this.getPhase()) {
            case 1 -> new ItemStack(TFItems.TWILIGHT_SCEPTER.get());
            case 2 -> new ItemStack(TFItems.ZOMBIE_SCEPTER.get());
            default -> new ItemStack(Items.GOLDEN_SWORD);
        };
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 speedMultiplier) {
    }

    @Override
    public boolean isInWater() {
        return false;
    }

    // ---------------------------------------------------------------- onLivingUpdate

    @Override
    public void aiStep() {
        float angle = this.yBodyRot * (float) Math.PI / 180.0F;
        double dx = this.getX() + Mth.cos(angle) * 0.65;
        double dy = this.getY() + this.getBbHeight() * 0.94;
        double dz = this.getZ() + Mth.sin(angle) * 0.65;
        if (this.level().isClientSide()) {
            int factor = (80 - this.attackTime) / 10;
            int particles = factor > 0 ? this.random.nextInt(factor) : 1;
            for (int i = 0; i < particles; i++) {
                float sparkle = 1.0F - (this.attackTime + 1.0F) / 60.0F;
                sparkle *= sparkle;
                float red = 0.37F * sparkle;
                float grn = 0.99F * sparkle;
                float blu = 0.89F * sparkle;
                if (this.getNextAttackType() != 0) {
                    red = 0.99F * sparkle;
                    grn = 0.47F * sparkle;
                    blu = 0.0F;
                }
                this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, red, grn, blu), dx + this.random.nextGaussian() * 0.025,
                        dy + this.random.nextGaussian() * 0.025, dz + this.random.nextGaussian() * 0.025, 0.0, 0.0, 0.0);
            }
        }
        if (this.isShadowClone()) {
            this.checkForMaster();
        }
        if (this.attackTime > 0) {
            this.attackTime--;
        }
        if (!this.level().isClientSide()) {
            ItemStack held = this.heldItem();
            if (!this.getMainHandItem().is(held.getItem())) {
                this.setItemSlot(EquipmentSlot.MAINHAND, held);
            }
        }
        super.aiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    // ---------------------------------------------------------------- EntityCreature.updateEntityActionState

    @Override
    protected void customServerAiStep(ServerLevel level) {
        this.hasAttacked = false;
        float range = 16.0F;
        if (this.entityToAttack == null) {
            this.entityToAttack = this.findPlayerToAttack(level);
            if (this.entityToAttack != null) {
                this.pathToEntity = this.getNavigation().createPath(this.entityToAttack, 0);
            }
        } else if (this.entityToAttack.isAlive()) {
            float distance = this.entityToAttack.distanceTo(this);
            if (this.hasLineOfSight(this.entityToAttack)) {
                this.attackEntity(level, this.entityToAttack, distance);
            }
        } else {
            this.entityToAttack = null;
        }
        if (!this.hasAttacked && this.entityToAttack != null && (this.pathToEntity == null || this.random.nextInt(20) == 0)) {
            this.pathToEntity = this.getNavigation().createPath(this.entityToAttack, 0);
        } else if (!this.hasAttacked && (this.pathToEntity == null && this.random.nextInt(180) == 0 || this.random.nextInt(120) == 0)) {
            this.updateWanderPath();
        }
        int footY = Mth.floor(this.getBoundingBox().minY + 0.5);
        Vec3 next = this.pathToEntity != null && this.random.nextInt(100) != 0 ? this.pathToEntity.getNextEntityPos(this) : null;
        double d = this.getBbWidth() * 2.0F;
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
            double tx = next.x - this.getX();
            double tz = next.z - this.getZ();
            double ty = next.y - footY;
            float wanted = (float) (Math.atan2(tz, tx) * 180.0 / Math.PI) - 90.0F;
            float turn = Mth.clamp(Mth.wrapDegrees(wanted - this.getYRot()), -30.0F, 30.0F);
            float moveSpeed = 0.7F;
            float forward = moveSpeed;
            this.setYRot(this.getYRot() + turn);
            if (this.hasAttacked && this.entityToAttack != null) {
                double ax = this.entityToAttack.getX() - this.getX();
                double az = this.entityToAttack.getZ() - this.getZ();
                float old = this.getYRot();
                this.setYRot((float) (Math.atan2(az, ax) * 180.0 / Math.PI) - 90.0F);
                float f4 = (old - this.getYRot() + 90.0F) * (float) Math.PI / 180.0F;
                this.xxa = -Mth.sin(f4) * forward;
                forward = Mth.cos(f4) * forward;
            }
            this.zza = forward;
            if (ty > 0.0) {
                this.getJumpControl().jump();
            }
        } else {
            this.pathToEntity = null;
        }
        if (this.entityToAttack != null) {
            this.lookAt(this.entityToAttack, 30.0F, 30.0F);
        }
        if (this.horizontalCollision && this.pathToEntity == null) {
            this.getJumpControl().jump();
        }
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
    }

    private @Nullable LivingEntity findPlayerToAttack(ServerLevel level) {
        Player player = level.getNearestPlayer(this, 16.0);
        return player != null && !player.isCreative() && !player.isSpectator() ? player : null;
    }

    /** EntityCreature.updateWanderPath: the darkest of 10 spots nearby (EntityMob.getBlockPathWeight 0.5 - brightness). */
    private void updateWanderPath() {
        BlockPos best = null;
        float bestWeight = -99999.0F;
        for (int i = 0; i < 10; i++) {
            BlockPos pos = BlockPos.containing(this.getX() + this.random.nextInt(13) - 6, this.getY() + this.random.nextInt(7) - 3,
                    this.getZ() + this.random.nextInt(13) - 6);
            float weight = 0.5F - this.level().getPathfindingCostFromLightLevels(pos);
            if (weight > bestWeight) {
                bestWeight = weight;
                best = pos;
            }
        }
        if (best != null) {
            this.pathToEntity = this.getNavigation().createPath(best, 0);
        }
    }

    /** EntityMob.attackEntityFrom: the attacker becomes the target. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_WALL) && this.entityToAttack != null) {
            this.teleportToSightOfEntity(level, this.entityToAttack);
        }
        if (this.isShadowClone()) {
            this.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 1.0F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            return false;
        }
        LivingEntity prevTarget = this.entityToAttack;
        if (source.getEntity() instanceof Lich) {
            return false;
        }
        if (this.getShieldStrength() > 0) {
            if (!source.is(DamageTypeTags.BYPASSES_ARMOR) || amount <= 2.0F) {
                this.playSound(SoundEvents.ITEM_BREAK.value(), 1.0F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                if (source.getEntity() instanceof Player player) {
                    this.entityToAttack = player;
                }
            } else {
                this.setShieldStrength(this.getShieldStrength() - 1);
                this.playSound(SoundEvents.ITEM_BREAK.value(), 1.0F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }
            return false;
        }
        if (super.hurtServer(level, source, amount)) {
            if (source.getEntity() instanceof LivingEntity attacker && attacker != this) {
                this.entityToAttack = attacker;
            }
            if (this.entityToAttack instanceof Lich) {
                this.entityToAttack = prevTarget;
            }
            if (this.getPhase() < 3 || this.random.nextInt(4) == 0) {
                this.teleportToSightOfEntity(level, this.entityToAttack);
            }
            return true;
        }
        return false;
    }

    private void attackEntity(ServerLevel level, LivingEntity target, float distance) {
        if (!this.isShadowClone() && this.attackTime % 15 == 0) {
            this.popNearbyMob(level);
        }
        if (this.getPhase() == 1) {
            if (this.attackTime == 60) {
                this.teleportToSightOfEntity(level, target);
                if (!this.isShadowClone() && this.countMyClones(level) < MAX_SHADOW_CLONES) {
                    this.spawnShadowClone(level, target);
                }
            }
            if (this.hasLineOfSight(target) && this.attackTime == 0 && distance < 20.0F) {
                if (this.getNextAttackType() == 0) {
                    this.launchAt(level, target, false);
                } else {
                    this.launchAt(level, target, true);
                }
                this.setNextAttackType(this.random.nextInt(3) > 0 ? 0 : 1);
                this.attackTime = 100;
            }
            this.hasAttacked = true;
        }
        if (this.getPhase() == 2 && !this.isShadowClone()) {
            this.despawnClones(level);
            if (this.attackTime % 15 == 0 && this.getMinionsToSummon() > 0 && this.countMyMinions(level) < MAX_ACTIVE_MINIONS) {
                this.spawnMinionAt(level, target);
                this.setMinionsToSummon(this.getMinionsToSummon() - 1);
            }
            if (this.attackTime == 0) {
                if (distance < 2.0F) {
                    TFMonster.oldAttack(this, level, target, 6);
                    this.attackTime = 20;
                } else if (distance < 20.0F && this.hasLineOfSight(target)) {
                    this.launchAt(level, target, this.getNextAttackType() != 0);
                    this.setNextAttackType(this.random.nextInt(2) > 0 ? 0 : 1);
                    this.attackTime = 60;
                } else {
                    this.teleportToSightOfEntity(level, target);
                    this.attackTime = 20;
                }
            }
            this.hasAttacked = true;
        }
        if (this.getPhase() == 3 && this.attackTime <= 0 && distance < 2.0F && target.getBoundingBox().maxY > this.getBoundingBox().minY
                && target.getBoundingBox().minY < this.getBoundingBox().maxY) {
            this.attackTime = 20;
            TFMonster.oldAttack(this, level, target, 6);
            this.hasAttacked = true;
        }
    }

    /** launchBoltAt / launchBombAt: from the hand (0.65 to the side, 0.82 up), velocity 0.5 / 0.35, spread 1. */
    private void launchAt(ServerLevel level, LivingEntity target, boolean bomb) {
        float facing = this.yBodyRot * (float) Math.PI / 180.0F;
        double sx = this.getX() + Mth.cos(facing) * 0.65;
        double sy = this.getY() + this.getBbHeight() * 0.82;
        double sz = this.getZ() + Mth.sin(facing) * 0.65;
        double tx = target.getX() - sx;
        double ty = target.getBoundingBox().minY + target.getBbHeight() / 2.0F - (this.getY() + this.getBbHeight() / 2.0F);
        double tz = target.getZ() - sz;
        this.playSound(SoundEvents.GHAST_SHOOT, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
        twilightforest.entity.projectile.TFThrowable projectile = bomb ? new LichBomb(level, this) : new LichBolt(level, this);
        projectile.shootOld(tx, ty, tz, bomb ? 0.35F : 0.5F, 1.0F);
        projectile.snapTo(sx, sy, sz, this.getYRot(), this.getXRot());
        level.addFreshEntity(projectile);
    }

    private AABB around() {
        return new AABB(this.getX(), this.getY(), this.getZ(), this.getX() + 1.0, this.getY() + 1.0, this.getZ() + 1.0).inflate(32.0, 16.0, 32.0);
    }

    private void popNearbyMob(ServerLevel level) {
        for (Entity entity : level.getEntities(this, this.around())) {
            if (entity instanceof LivingEntity mob && canPop(entity) && this.hasLineOfSight(entity)) {
                mob.discard();
                level.broadcastEntityEvent(mob, (byte) 20);
                trail(level, ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 1.0F, 0.5F, 0.5F), mob.getX(), mob.getY() + mob.getBbHeight() / 2.0,
                        mob.getZ(), this.getX(), this.getY() + this.getBbHeight() / 2.0, this.getZ());
                break;
            }
        }
    }

    private static boolean canPop(Entity nearby) {
        EntityType<?> type = nearby.getType();
        return type == EntityTypes.SKELETON || type == EntityTypes.ZOMBIE || type == EntityTypes.ENDERMAN || type == EntityTypes.SPIDER
                || type == EntityTypes.CREEPER || type == TFEntities.SWARM_SPIDER.get();
    }

    private int countMyClones(ServerLevel level) {
        int count = 0;
        for (Lich lich : level.getEntitiesOfClass(Lich.class, this.around())) {
            if (lich.isShadowClone() && lich.masterLich == this) {
                count++;
            }
        }
        return count;
    }

    public boolean wantsNewClone(ServerLevel level, Lich clone) {
        return clone.isShadowClone() && this.countMyClones(level) < MAX_SHADOW_CLONES;
    }

    private void spawnShadowClone(ServerLevel level, LivingEntity target) {
        Vec3 spot = this.findVecInLOSOf(target);
        Lich clone = clone(level, this);
        clone.setPos(spot.x, spot.y, spot.z);
        level.addFreshEntity(clone);
        clone.entityToAttack = target;
        clone.attackTime = 60 + this.random.nextInt(3) - this.random.nextInt(3);
        this.makeTeleportTrail(level, this.getX(), this.getY(), this.getZ(), spot.x, spot.y, spot.z);
    }

    private void despawnClones(ServerLevel level) {
        for (Lich lich : level.getEntitiesOfClass(Lich.class, this.around())) {
            if (lich.isShadowClone()) {
                lich.discard();
            }
        }
    }

    private int countMyMinions(ServerLevel level) {
        int count = 0;
        for (LichMinion minion : level.getEntitiesOfClass(LichMinion.class, this.around())) {
            if (minion.master == this) {
                count++;
            }
        }
        return count;
    }

    public boolean wantsNewMinion(ServerLevel level) {
        return this.countMyMinions(level) < MAX_ACTIVE_MINIONS;
    }

    private void spawnMinionAt(ServerLevel level, LivingEntity target) {
        Vec3 spot = this.findVecInLOSOf(target);
        LichMinion minion = new LichMinion(level, this);
        minion.setPos(spot.x, spot.y, spot.z);
        minion.initCreature(level);
        level.addFreshEntity(minion);
        minion.setTarget(target);
        level.broadcastEntityEvent(minion, (byte) 20);
        minion.playSound(SoundEvents.CHICKEN_EGG, 1.0F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        this.makeBlackMagicTrail(level, this.getX(), this.getEyeY(), this.getZ(), spot.x, spot.y + minion.getBbHeight() / 2.0, spot.z);
    }

    private void checkForMaster() {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.masterLich == null) {
            for (Lich lich : level.getEntitiesOfClass(Lich.class, this.around())) {
                if (!lich.isShadowClone() && lich.wantsNewClone(level, this)) {
                    this.masterLich = lich;
                    this.makeTeleportTrail(level, this.getX(), this.getY(), this.getZ(), lich.getX(), lich.getY(), lich.getZ());
                    this.entityToAttack = lich.entityToAttack;
                    break;
                }
            }
        }
        if (this.masterLich == null || !this.masterLich.isAlive()) {
            this.discard();
        }
    }

    private void teleportToSightOfEntity(ServerLevel level, @Nullable Entity entity) {
        Vec3 dest = this.findVecInLOSOf(entity);
        if (dest == null) {
            return;
        }
        double sx = this.getX();
        double sy = this.getY();
        double sz = this.getZ();
        this.teleportToNoChecks(level, dest.x, dest.y, dest.z);
        this.lookAt(entity, 100.0F, 100.0F);
        this.yBodyRot = this.getYRot();
        if (!this.hasLineOfSight(entity)) {
            this.teleportToNoChecks(level, sx, sy, sz);
        }
    }

    /** findVecInLOSOf: 100 tries around the target (gaussian 16 / 8 / 16) for a free, dry spot on the ground it can see. */
    private @Nullable Vec3 findVecInLOSOf(@Nullable Entity target) {
        if (target == null) {
            return null;
        }
        double tx = 0.0;
        double ty = 0.0;
        double tz = 0.0;
        for (int i = 0; i < 100; i++) {
            tx = target.getX() + this.random.nextGaussian() * 16.0;
            ty = target.getY() + this.random.nextGaussian() * 8.0;
            tz = target.getZ() + this.random.nextGaussian() * 16.0;
            boolean ground = false;
            int bx = Mth.floor(tx);
            int by = Mth.floor(ty);
            int bz = Mth.floor(tz);
            while (!ground && ty > 0.0) {
                if (this.level().getBlockState(new BlockPos(bx, by - 1, bz)).isSolid()) {
                    ground = true;
                } else {
                    ty--;
                    by--;
                }
            }
            if (by != 0 && this.canEntitySee(target, tx, ty, tz)) {
                float half = this.getBbWidth() / 2.0F;
                AABB box = new AABB(tx - half, ty, tz - half, tx + half, ty + this.getBbHeight(), tz + half);
                if (this.level().noCollision(this, box) && !this.level().containsAnyLiquid(box)) {
                    break;
                }
            }
        }
        return new Vec3(tx, ty, tz);
    }

    private boolean canEntitySee(Entity entity, double x, double y, double z) {
        return this.level().clip(new ClipContext(entity.getEyePosition(), new Vec3(x, y, z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                entity)).getType() == HitResult.Type.MISS;
    }

    private void teleportToNoChecks(ServerLevel level, double x, double y, double z) {
        double sx = this.getX();
        double sy = this.getY();
        double sz = this.getZ();
        this.teleportTo(x, y, z);
        this.makeTeleportTrail(level, sx, sy, sz, x, y, z);
        level.playSound(null, sx, sy, sz, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
        this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        this.setJumping(false);
    }

    private void makeTeleportTrail(ServerLevel level, double sx, double sy, double sz, double dx, double dy, double dz) {
        for (int i = 0; i < 128; i++) {
            double f = i / 127.0;
            level.sendParticles(SpellParticleOption.create(ParticleTypes.EFFECT, -1, 1.0F), sx + (dx - sx) * f + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 2.0,
                    sy + (dy - sy) * f + this.random.nextDouble() * this.getBbHeight(), sz + (dz - sz) * f + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 2.0,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    void makeBlackMagicTrail(ServerLevel level, double sx, double sy, double sz, double dx, double dy, double dz) {
        trail(level, ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.2F, 0.2F, 0.2F), sx, sy, sz, dx, dy, dz);
    }

    private void trail(ServerLevel level, ParticleOptions particle, double sx, double sy, double sz, double dx, double dy, double dz) {
        for (int i = 0; i < 32; i++) {
            double f = i / 31.0;
            level.sendParticles(particle, sx + (dx - sx) * f + this.random.nextGaussian() * 0.005, sy + (dy - sy) * f + this.random.nextGaussian() * 0.005,
                    sz + (dz - sz) * f + this.random.nextGaussian() * 0.005, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    public @Nullable LivingEntity attackTarget() {
        return this.entityToAttack;
    }

    // ---------------------------------------------------------------- sounds, bar, save

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BLAZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLAZE_DEATH;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (!this.isShadowClone()) {
            this.bossEvent.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    /** Clones leave no loot (in 1.5.2 they died at once without their master and dropped nothing either). */
    @Override
    protected boolean shouldDropLoot(ServerLevel level) {
        return !this.isShadowClone() && super.shouldDropLoot(level);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("ShadowClone", this.isShadowClone());
        output.putByte("ShieldStrength", (byte) this.getShieldStrength());
        output.putByte("MinionsToSummon", (byte) this.getMinionsToSummon());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setShadowClone(input.getBooleanOr("ShadowClone", false));
        this.setShieldStrength(input.getByteOr("ShieldStrength", (byte) INITIAL_SHIELD_STRENGTH));
        this.setMinionsToSummon(input.getByteOr("MinionsToSummon", (byte) INITIAL_MINIONS_TO_SUMMON));
    }

    public static List<Lich> nearby(ServerLevel level, Entity around) {
        return level.getEntitiesOfClass(Lich.class, new AABB(around.getX(), around.getY(), around.getZ(), around.getX() + 1.0, around.getY() + 1.0,
                around.getZ() + 1.0).inflate(32.0, 16.0, 32.0));
    }
}
