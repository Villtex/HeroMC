package twilightforest.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * EntityTFTowerGolem (carminite golem): 40 health, 2 armour, 1.4 x 2.9, speed 0.25 (wander 0.16), avoids water. Its
 * swing hits for 9 and throws up 0.4; it picks fights with other monsters it bumps into (1 in 10). Red dust and block
 * bits under its feet. Iron golem sounds. Drops 0..2 iron ingots and 0..2 tower wood. Up to 16 in a chunk.
 */
public class TowerGolem extends TFMonster {
    private int attackTimer;

    public TowerGolem(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.ARMOR, 2.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 0.16 / 0.25));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int attackStrength(Entity target) {
        return 9;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        this.attackTimer = 10;
        level.broadcastEntityEvent(this, (byte) 4);
        boolean success = super.doHurtTarget(level, target);
        if (success) {
            target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.4, 0.0));
            target.needsSync = true;
        }
        this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
        return success;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) {
            this.attackTimer = 10;
            this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
        } else {
            super.handleEntityEvent(id);
        }
    }

    public int getAttackTimer() {
        return this.attackTimer;
    }

    @Override
    protected void doPush(Entity entity) {
        if (entity instanceof Enemy && entity instanceof LivingEntity living && this.random.nextInt(10) == 0) {
            this.setTarget(living);
        }
        super.doPush(entity);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.attackTimer > 0) {
            this.attackTimer--;
        }
        if (this.level().isClientSide()) {
            var m = this.getDeltaMovement();
            if (m.x * m.x + m.z * m.z > 2.5000003E-7F && this.random.nextInt(5) == 0) {
                BlockPos below = BlockPos.containing(this.getX(), this.getY() - 0.2, this.getZ());
                BlockState state = this.level().getBlockState(below);
                if (!state.isAir()) {
                    this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state),
                            this.getX() + (this.random.nextFloat() - 0.5) * this.getBbWidth(), this.getBoundingBox().minY + 0.1,
                            this.getZ() + (this.random.nextFloat() - 0.5) * this.getBbWidth(), 4.0 * (this.random.nextFloat() - 0.5), 0.5,
                            (this.random.nextFloat() - 0.5) * 4.0);
                }
            }
            if (this.random.nextBoolean()) {
                this.level().addParticle(DustParticleOptions.REDSTONE, this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                        this.getY() + this.random.nextDouble() * this.getBbHeight() - 0.25,
                        this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(), 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 16;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 1.0F);
    }
}
