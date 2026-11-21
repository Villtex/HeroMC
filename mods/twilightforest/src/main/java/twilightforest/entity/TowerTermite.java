package twilightforest.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.TFBlocks;

/**
 * EntityTFTowerTermite: a silverfish of the dark tower, 0.3 x 0.7, 15 health, speed 0.27, bite 5, hunts within 8
 * blocks. When hit it wakes the termites in infested tower wood within ±10 (×5 high), each breaking out (stop 1 in 2);
 * idle, it burrows into plain tower wood next to it, which becomes infested. Drops borer essence.
 */
public class TowerTermite extends TFMonster {
    private int allySummonCooldown;

    public TowerTermite(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 15.0).add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.FOLLOW_RANGE, 8.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int attackStrength(Entity target) {
        return 5;
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.EVENTS;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isInvulnerableTo(level, source)) {
            return false;
        }
        if (this.allySummonCooldown <= 0 && (source.getEntity() != null || source.is(net.minecraft.world.damagesource.DamageTypes.MAGIC))) {
            this.allySummonCooldown = 20;
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.allySummonCooldown > 0) {
            this.allySummonCooldown--;
            if (this.allySummonCooldown == 0) {
                this.tryToSummonAllies(level);
            }
        }
        if (this.getTarget() == null && this.getNavigation().isDone()) {
            this.tryToBurrow(level);
        }
    }

    protected void tryToSummonAllies(ServerLevel level) {
        BlockPos origin = this.blockPosition();
        boolean stop = false;
        for (int dy = 0; !stop && dy <= 5 && dy >= -5; dy = dy <= 0 ? 1 - dy : -dy) {
            for (int dx = 0; !stop && dx <= 10 && dx >= -10; dx = dx <= 0 ? 1 - dx : -dx) {
                for (int dz = 0; !stop && dz <= 10 && dz >= -10; dz = dz <= 0 ? 1 - dz : -dz) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.is(TFBlocks.TOWER_WOOD_INFESTED.get())) {
                        level.levelEvent(2001, pos, net.minecraft.world.level.block.Block.getId(state));
                        level.removeBlock(pos, false);
                        twilightforest.block.TowerWoodBlock.TERMITE.accept(level, pos);
                        if (this.random.nextBoolean()) {
                            stop = true;
                            break;
                        }
                    }
                }
            }
        }
    }

    protected void tryToBurrow(ServerLevel level) {
        BlockPos pos = BlockPos.containing(this.getX(), this.getY() + 0.5, this.getZ()).relative(Direction.from3DDataValue(this.random.nextInt(6)));
        if (level.getBlockState(pos).is(TFBlocks.TOWER_WOOD.get())) {
            level.setBlock(pos, TFBlocks.TOWER_WOOD_INFESTED.get().defaultBlockState(), 3);
            this.spawnAnim();
            this.discard();
        }
    }

    @Override
    public void tick() {
        this.yBodyRot = this.getYRot();
        super.tick();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SILVERFISH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SILVERFISH_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SILVERFISH_STEP, 0.15F, 1.0F);
    }
}
