package betterdungeons.mob;

import betterdungeons.BDItems;
import betterdungeons.mob.ai.BossAttackGoal;
import betterdungeons.projectile.BDProjectile;
import betterdungeons.projectile.ProjectileType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;

/**
 * EntityTurtle ("TurtleBoss", the Giant Turtle of the swamp caves): a shell (2.6 x 1.5, 500 health, speed 0.2, 5 xp)
 * with a head (300) and four legs (100 each) that can be hit on their own; their damage also goes to the turtle
 * ({@link TurtlePart}, NeoForge part entities instead of the five EntityTurtlePart mobs). Breathes under water and
 * keeps 3 blocks under its target there (in lava level with it).
 *
 * <p>Attacks ({@link #attackEntity}, 1 in clamp(d, 30, 80) per tick): a third of them a spin (head and legs pulled in,
 * turning faster and faster for 100 ticks and back; from 20 on everything touching the shell takes 6 per tick, from
 * 80 on the way back it rams the target's old direction; below 200 health it spits a bubble at 50), else, if the
 * target looks within 60 degrees towards it, a bubble ray (a bubble ball after 20 ticks, a third of the time another).
 * Below 50 health once (while it still has its head) it pulls in, heals 1 per tick to full and restores the parts'
 * health. While spinning or healing its AI is off. Turns towards the target 2 degrees per tick.
 *
 * <p>Drops 6 turtle scales plus 2 per lost leg, and (killed by a player) the diamond chance of its loot table. As in
 * 1.5.2 losing a part also kills the part opposite to it in the parts list (head and fourth leg, first and third
 * leg): the dead-parts bit mask was read back in reverse order.
 */
public class TurtleBoss extends BDBoss {
    /** isHealing: watcher 16 == 0 (so it starts healing, and stops at once at full health). */
    private static final EntityDataAccessor<Byte> DATA_HEALING = SynchedEntityData.defineId(TurtleBoss.class, EntityDataSerializers.BYTE);
    /** Bit i: part i is gone (the client hides it). */
    private static final EntityDataAccessor<Integer> DATA_PARTS_DEAD = SynchedEntityData.defineId(TurtleBoss.class, EntityDataSerializers.INT);
    /** The spin and bubble flags (server fields in 1.5.2; the client needs them to place the parts). */
    private static final EntityDataAccessor<Byte> DATA_POSE = SynchedEntityData.defineId(TurtleBoss.class, EntityDataSerializers.BYTE);

    private final TurtlePart[] parts = new TurtlePart[5];
    /** body[i] == null in 1.5.2: the turtle noticed the part's death (on its next hurt). */
    private final boolean[] bodyNull = new boolean[5];
    public boolean attacking;
    private double tempRYaw;
    private double tempRPitch;
    private int rotAccel;
    private boolean rotDir = true;
    private boolean bubbleAttack;
    private int bubbleCD;
    private double tempmx;
    private double tempmz;
    private double tempmy;

    public TurtleBoss(EntityType<? extends TurtleBoss> type, Level level) {
        super(type, level);
        this.xpReward = 5;
        for (int i = 0; i < 5; i++) {
            this.parts[i] = new TurtlePart(this, i, i == 0);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH, 500.0).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BossAttackGoal(this, 1.0, false, b -> ((TurtleBoss) b).attacking));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, null));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, 0, true, false, null));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HEALING, (byte) 0);
        builder.define(DATA_PARTS_DEAD, 0);
        builder.define(DATA_POSE, (byte) 0);
    }

    public boolean isHealing() {
        return this.entityData.get(DATA_HEALING) == 0;
    }

    public void setHealing(boolean healing) {
        this.entityData.set(DATA_HEALING, healing ? (byte) 0 : (byte) 1);
    }

    public boolean isPartDead(int index) {
        return (this.entityData.get(DATA_PARTS_DEAD) >> index & 1) != 0;
    }

    void killPart(int index) {
        this.entityData.set(DATA_PARTS_DEAD, this.entityData.get(DATA_PARTS_DEAD) | 1 << index);
    }

    /** Spinning (head and legs pulled in) or healing, as the client sees it. */
    public boolean isPulledIn() {
        return (this.entityData.get(DATA_POSE) & 1) != 0 || this.isHealing();
    }

    public boolean isBubbling() {
        return (this.entityData.get(DATA_POSE) & 2) != 0;
    }

    @Override
    protected boolean isAIEnabled() {
        return !this.attacking && !this.isHealing();
    }

    // ---------------------------------------------------------------- onUpdate

    @Override
    public void tick() {
        Level level = this.level();
        if (!level.isClientSide()) {
            this.entityData.set(DATA_POSE, (byte) ((this.attacking ? 1 : 0) | (this.bubbleAttack ? 2 : 0)));
            if (this.getHealth() < 50.0F && !this.isHealing() && !this.bodyNull[0]) {
                for (TurtlePart part : this.parts) {
                    part.heartsLife = part.maxHealth();
                }
                this.setHealing(true);
            }
        }
        if (this.isHealing()) {
            if (level.isClientSide()) {
                level.addParticle(ParticleTypes.HEART, this.getX() + this.random.nextDouble() * 2.0 - 1.0, this.getY() + 1.0 + this.random.nextDouble() * 2.0,
                        this.getZ() + this.random.nextDouble() * 2.0 - 1.0, 0.0, 1.0, 0.0);
            } else {
                this.heal(1.0F);
                if (this.getHealth() >= this.getMaxHealth()) {
                    this.setHealing(false);
                }
            }
        }
        LivingEntity target = this.getTarget();
        Vec3 m = this.getDeltaMovement();
        if (!level.isClientSide()) {
            if (target != null) {
                if (this.isInWater()) {
                    if (this.getY() + 3.0 < target.getY() && m.y < 0.2) {
                        this.setDeltaMovement(m.x, m.y + 0.03, m.z);
                    } else if (this.getY() + 3.0 > target.getY() && m.y > -0.2) {
                        this.setDeltaMovement(m.x, m.y - 0.03, m.z);
                    }
                } else if (this.isInLava()) {
                    if (this.getY() < target.getY() && m.y < 0.2) {
                        this.setDeltaMovement(m.x, m.y + 0.03, m.z);
                    } else if (this.getY() + 3.0 > target.getY() && m.y > -0.2) {
                        this.setDeltaMovement(m.x, m.y - 0.03, m.z);
                    }
                }
            } else if (this.isInWater() && m.y < 0.1) {
                this.setDeltaMovement(m.x, m.y - 0.03, m.z);
            }
        }
        super.tick();
        if (!level.isClientSide()) {
            this.serverAfterTick();
        }
        this.placeParts();
    }

    private void serverAfterTick() {
        LivingEntity target = this.getTarget();
        if (target != null) {
            double tempRot = this.getYRot() - this.yRotO;
            float angle = norm360(this.getYRot() - target.getYRot());
            angle = Math.abs(angle - 180.0F);
            if (angle > 10.0F) {
                this.yRotO = norm360(this.yRotO);
                if ((int) tempRot > 3) {
                    this.setYRot(this.yRotO + 2.0F);
                }
                if ((int) tempRot < 3) {
                    this.setYRot(this.yRotO - 2.0F);
                }
            }
        }
        if (this.attacking) {
            this.doAttack();
        }
        if (this.bubbleAttack) {
            this.bubbleRay();
        }
        // setParts / getPartsAlive: bit (4 - i) for a noticed dead part i, read back as bit i
        int b = 0;
        for (int i = 0; i < 5; i++) {
            b = (b << 1) + (this.bodyNull[i] ? 1 : 0);
        }
        for (int i = 4; i >= 0; i--) {
            if ((b >> i) % 2 == 1 && !this.bodyNull[i]) {
                this.killPart(i);
            }
        }
    }

    /** The parts' places (both sides: the client moves its part entities the same way). */
    private void placeParts() {
        float yaw = this.getYRot();
        if (!this.isPulledIn()) {
            if (!this.isBubbling()) {
                double hx = -Math.sin(Math.toRadians(yaw)) * 2.0;
                double hz = Math.cos(Math.toRadians(yaw)) * 2.0;
                this.parts[0].place(this.getX() + hx, this.getY(), this.getZ() + hz, this.getYHeadRot());
            } else {
                this.parts[0].place(this.getX(), this.getY(), this.getZ(), this.parts[0].getYRot());
            }
            double lx = -Math.sin(Math.toRadians(yaw + 45.0F)) * 2.0;
            double lz = Math.cos(Math.toRadians(yaw + 45.0F)) * 2.0;
            this.parts[1].place(this.getX() + lx, this.getY() - 0.3, this.getZ() + lz, yaw + 45.0F);
            this.parts[2].place(this.getX() - lx, this.getY() - 0.3, this.getZ() - lz, yaw - 45.0F);
            lx = -Math.sin(Math.toRadians(yaw - 45.0F)) * 2.0;
            lz = Math.cos(Math.toRadians(yaw - 45.0F)) * 2.0;
            this.parts[3].place(this.getX() + lx, this.getY() - 0.3, this.getZ() + lz, yaw - 45.0F);
            this.parts[4].place(this.getX() - lx, this.getY() - 0.3, this.getZ() - lz, yaw + 45.0F);
        } else {
            for (TurtlePart part : this.parts) {
                part.place(this.getX(), this.getY(), this.getZ(), yaw);
            }
        }
    }

    /** Old-AI movement while the AI is off (EntityCreature.updateEntityActionState walked to the target). */
    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        LivingEntity target = this.getTarget();
        if (!this.isAIEnabled() && target != null && (this.getNavigation().isDone() || this.random.nextInt(20) == 0)) {
            this.getNavigation().moveTo(target, 1.0);
        }
    }

    // ---------------------------------------------------------------- attacks

    @Override
    public void attackEntity(Entity entity, float f) {
        if (this.attacking || this.bubbleAttack || this.isHealing()) {
            return;
        }
        int attackChance = Mth.clamp((int) f, 30, 80);
        if (this.random.nextInt(attackChance) == 0) {
            if (this.random.nextInt(3) == 0) {
                this.attacking = true;
                this.rotDir = true;
            } else {
                float angle = norm360(this.getYRot() - entity.getYRot());
                angle = Math.abs(angle - 180.0F);
                if (angle < 60.0F) {
                    this.startBubble();
                }
            }
        }
    }

    private void doAttack() {
        Entity e = this.getTarget();
        if (e == null || !(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.rotDir) {
            this.rotAccel++;
        } else {
            this.rotAccel--;
            if (this.rotAccel > 80) {
                this.setDeltaMovement(this.tempmx, this.tempmy, this.tempmz);
            }
        }
        this.setYRot(this.getYRot() + this.rotAccel / 2);
        if (this.rotAccel >= 100) {
            this.rotDir = false;
        }
        if (this.rotAccel == 50 && this.getHealth() < 200.0F) {
            this.startBubble();
        }
        if (this.rotAccel == 80) {
            this.aimAt(e);
        }
        if (this.rotAccel >= 20) {
            for (Entity entity1 : level.getEntities(this, this.getBoundingBox().inflate(1.0))) {
                if (entity1.isPickable() && !entity1.is(this) && !this.hasPassenger(entity1)) {
                    entity1.hurtServer(level, this.damageSources().mobAttack(this), 6.0F);
                }
            }
        }
        if (this.rotAccel == 0) {
            this.attacking = false;
        }
    }

    private void aimAt(Entity e) {
        double px = this.getX() - e.getX();
        double pz = this.getZ() - e.getZ();
        double py = this.getY() - e.getY();
        this.tempRYaw = Math.atan2(px, pz);
        this.tempRPitch = Math.atan2(py, Math.sqrt(px * px + pz * pz));
        this.tempmy = -Math.sin(this.tempRPitch);
        this.tempmx = -Math.sin(this.tempRYaw);
        this.tempmz = -Math.cos(this.tempRYaw);
    }

    private void startBubble() {
        Entity e = this.getTarget();
        if (e != null) {
            this.aimAt(e);
        }
        this.bubbleAttack = true;
    }

    private void bubbleRay() {
        if (this.bubbleCD >= 20) {
            BDProjectile.spawn(this, ProjectileType.BUBBLE);
            this.bubbleAttack = false;
            this.bubbleCD = 0;
            if (!this.attacking && this.random.nextInt(3) == 0) {
                this.bubbleAttack = true;
            }
        } else {
            this.bubbleCD++;
        }
    }

    // ---------------------------------------------------------------- damage

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean flag = super.hurtServer(level, source, damage);
        for (int i = 0; i < 5; i++) {
            if (!this.bodyNull[i] && this.isPartDead(i)) {
                this.bodyNull[i] = true;
            }
        }
        return flag;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    /** The scales: 6 plus 2 for every leg it has lost (body[1..4] == null). */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        int j = 6;
        for (int k = 1; k < 5; k++) {
            if (this.bodyNull[k]) {
                j += 2;
            }
        }
        this.spawnAtLocation(level, new ItemStack(BDItems.TURTLE_SCALE.get(), j));
    }

    // ---------------------------------------------------------------- parts

    public TurtlePart[] getAllParts() {
        return this.parts;
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    /** All five; the lost ones cannot be hit or picked ({@link TurtlePart#isPickable}). */
    @Override
    public PartEntity<?>[] getParts() {
        return this.parts;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        for (int i = 0; i < 5; i++) {
            this.parts[i].setId(packet.getId() + i + 1);
        }
    }
}
