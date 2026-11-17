package projectzulu.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import projectzulu.ZuluMonsters;

/**
 * EntitySandWorm: lies buried in the desert sand (texture sandworm_hidden, mouth closed, no movement) while idle. It
 * surfaces when a player comes within 4 blocks (anger 100), touches it or anything hurts it (anger 400), then chases
 * players and villagers within 16 blocks. It never wanders. Heals 1 every 200 ticks in daylight. Health 20, speed
 * 0.22, damage 4 (getAttackStrength 3/3/4/6), 1.5 x 1.0. No living or death sound; its hurt roar came from
 * SoundHookContainerClass. The step sound "sand" did not exist in 1.5.2, so it walked silently.
 */
public class SandwormEntity extends ZuluMonster {
    public SandwormEntity(EntityType<? extends SandwormEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ZuluMonster.createZuluAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    /** setAvoidsWater(false). */
    @Override
    protected boolean avoidsWater() {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new StayStillGoal(this, State.IDLE));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(4, new MoveTowardsTargetGoal(this, 1.0, 32.0F));
        this.targetSelector.addGoal(4, new StateTargetGoal<>(this, Player.class, ANGRY));
        this.targetSelector.addGoal(5, new StateTargetGoal<>(this, Villager.class, ANGRY));
    }

    /** The buried look: idle (synced AI state). */
    public boolean isHidden() {
        return this.getState() == State.IDLE;
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide() && this.level().isBrightOutside() && this.tickCount % 200 == 0) {
            this.heal(1.0F);
        }
        super.aiStep();
        if (!this.level().isClientSide() && this.getState() == State.IDLE
                && this.level().getNearestPlayer(this, 4.0) != null) {
            this.angerLevel = 100;
        }
    }

    /** onCollideWithPlayer. */
    @Override
    public void playerTouch(Player player) {
        if (!this.level().isClientSide()) {
            this.angerLevel = 400;
        }
        super.playerTouch(player);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        this.angerLevel = 400;
        return super.hurtServer(level, source, damage);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    /** sounds.sandwormroar of SoundHookContainerClass on every hurt. */
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ZuluMonsters.SANDWORM_ROAR.get();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }
}
