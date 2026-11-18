package betterdungeons.mob;

import betterdungeons.mob.ai.BossAttackGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * EntityBull ("IceBull", the boss of the ice caves): 300 health, 1.2 x 1.3, speed 0.2, 5 xp, cannot be pushed or
 * knocked back and is solid for others. Fire hurts it more (up to 2 damage +3, more doubled). It freezes air, water
 * and lava under its feet to ice and breaks every block around its body except bedrock and obsidian (every tick).
 *
 * <p>Charge ({@link #attackEntity}): when the target looks straight at it (within 5 degrees), it rushes forward with a
 * rising speed for a few ticks; everything in the block box around it then takes 5 + difficulty damage per tick and
 * is thrown away. Turns towards the target 7 degrees per tick. Targets what hurts it, zombies and players. Drops
 * rand(2 + looting) each of blaze rods, diamonds and obsidian.
 */
public class IceBull extends BDBoss {
    private int chargeTime;
    private int attackStrength = 5;
    private @Nullable LivingEntity lastAttacker;

    public IceBull(EntityType<? extends IceBull> type, Level level) {
        super(type, level);
        this.xpReward = 5;
        this.attackStrength = this.attackStrength + level.getDifficulty().getId();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH, 300.0).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new BossAttackGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Zombie.class, 0, true, false, null));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, null));
    }

    /** getChargeTime returned chargeTime++ (so one charge tick counts up by four). */
    private int getChargeTime() {
        return this.chargeTime++;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel level) {
            this.serverUpdate(level);
        }
    }

    private void serverUpdate(ServerLevel level) {
        // EntityMob.attackEntityFrom kept the attacker as entityToAttack
        if (this.lastAttacker != null && this.distanceTo(this.lastAttacker) < 500.0F) {
            this.setTarget(this.lastAttacker);
        }
        LivingEntity target = this.getTarget();
        if (target != null && !this.isAttacking()) {
            double tempRot = this.getYRot() - this.yRotO;
            double rotDiff = Math.abs(angleBetween(this, target));
            double rot = Math.abs(rotDiff - Math.abs(wrap(this.getYRot())));
            float angle = norm360((float) rot);
            angle = Math.abs(angle - 180.0F);
            if (angle > 10.0F) {
                this.yRotO = norm360(this.yRotO);
                if ((int) tempRot > 8) {
                    this.setYRot(this.yRotO + 7.0F);
                }
                if ((int) tempRot < 8) {
                    this.setYRot(this.yRotO - 7.0F);
                }
            }
        }
        int x = Mth.floor(this.getX());
        int y = Mth.floor(this.getY());
        int z = Mth.floor(this.getZ());
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    BlockPos pos = new BlockPos(x + i, y + j, z + k);
                    BlockState state = level.getBlockState(pos);
                    if (j == -1) {
                        if (state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
                            level.setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
                        }
                    } else if (!state.isAir() && !state.is(Blocks.BEDROCK) && !state.is(Blocks.OBSIDIAN)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, state);
                        for (int r = 0; r < 8; r++) {
                            level.sendParticles(particle, this.getX() + i + (this.random.nextFloat() - 0.5), this.getY() + j + this.random.nextFloat() - 0.5,
                                    this.getZ() + k + this.random.nextFloat() - 0.5, 0, 0.0, 0.0, 0.0, 1.0);
                        }
                    }
                }
            }
        }
        if (this.isAttacking()) {
            for (Entity entity : level.getEntities(this, new AABB(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1))) {
                entity.hurtServer(level, this.damageSources().mobAttack(this), this.attackStrength);
                Vec3 m = entity.getDeltaMovement();
                entity.setDeltaMovement(-(this.getX() - entity.getX()), m.y, -(this.getZ() - entity.getZ()));
                entity.needsSync = true;
            }
        }
    }

    @Override
    public void attackEntity(Entity entity, float f) {
        if (this.isAttacking()) {
            double ry = Math.toRadians(this.getYRot() - 180.0F - 20.0F);
            Vec3 m = this.getDeltaMovement();
            double mx = Math.sin(ry) / 20.0 * this.getChargeTime();
            double mz = -Math.cos(ry) / 20.0 * this.getChargeTime();
            this.setDeltaMovement(mx, m.y, mz);
            this.chargeTime = this.getChargeTime() + 1;
            if (this.getChargeTime() > 20) {
                this.setAttacking(false);
            }
            if (this.getYHeadRot() + 5.0F > this.getYRot()) {
                this.setYRot(this.getYRot() + 10.0F);
            } else if (this.getYHeadRot() - 5.0F < this.getYRot()) {
                this.setYRot(this.getYRot() - 10.0F);
            }
        } else {
            double angle = norm360((double) (this.getYRot() - entity.getYRot() - 180.0F));
            if (angle < 5.0 || angle > 355.0) {
                this.setAttacking(true);
                this.chargeTime = 0;
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.is(DamageTypeTags.IS_FIRE)) {
            damage = damage <= 2.0F ? damage + 3.0F : damage * 2.0F;
        }
        if (source.getEntity() instanceof LivingEntity attacker && attacker != this) {
            this.lastAttacker = attacker;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    /** getCollisionBox(entity) = the entity's own box: others stand on and bump into it. */
    @Override
    public boolean canBeCollidedWith(@Nullable Entity entity) {
        return this.isAlive();
    }

    /** dropFewItems: the same rand(2 + looting) for all three (Java, a loot table cannot share one count). */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        int looting = BDCreatures.looting(level, source);
        int count = this.random.nextInt(2 + looting);
        for (ItemStack stack : new ItemStack[] {new ItemStack(Items.BLAZE_ROD), new ItemStack(Items.DIAMOND), new ItemStack(Items.OBSIDIAN)}) {
            for (int j = 0; j < count; j++) {
                this.spawnAtLocation(level, stack.copy());
            }
        }
    }
}
