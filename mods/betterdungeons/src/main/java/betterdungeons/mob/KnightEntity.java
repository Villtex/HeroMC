package betterdungeons.mob;

import betterdungeons.mob.ai.HumanGoals;
import betterdungeons.mob.ai.HumanTargetGoals;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * friendlyMobs.EntityKnight ("FriendlyHuman"): the mercenary. Right click makes the player its owner (team
 * "friend.&lt;player&gt;"); the owner gives it equipment by right click, orders through the controller (AIMode, standing
 * position, target) and paths through the path marker.
 */
public class KnightEntity extends HumanEntity {
    public KnightEntity(EntityType<? extends KnightEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void configRaze() {
        this.despawn = false;
        this.renderEars = false;
        this.sneak = false;
        this.canMount = true;
        this.attackStrength = 2;
        this.attackSpeed = 30;
        this.experienceValue = 0;
        this.baseMoveSpeed = 0.25F;
        if (this.humanType == null || !this.humanType.startsWith("friend")) {
            this.humanType = "friend";
        }
    }

    @Override
    protected void addAITasks() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new HumanGoals.MeleeAttack(this, LivingEntity.class, this.baseMoveSpeed, false));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, this.speed(this.baseMoveSpeed)));
        this.goalSelector.addGoal(5, new HumanGoals.ControlledFollowOwner(this, 0.25F * 1.5F, 5.0F, 50.0F, true));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, this.speed(this.baseMoveSpeed / 3.0F)));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new HumanGoals.TalkClosest(this, HumanEntity.class, 8.0F));
        this.goalSelector.addGoal(8, new HumanGoals.IdleSit(this));
        this.targetSelector.addGoal(1, new HumanTargetGoals.HurtBy(this));
        this.targetSelector.addGoal(2, new HumanTargetGoals.OwnerTarget(this));
        this.targetSelector.addGoal(3, new HumanTargetGoals.NearestAttackable(this, Enemy.class, 30.0F, true));
        this.targetSelector.addGoal(3, new HumanTargetGoals.NearestAttackable(this, Player.class, 30.0F, true));
        this.targetSelector.addGoal(2, new HumanTargetGoals.NearestRival(this, HumanEntity.class, 30.0F, true, this.rivalHumans, true));
        this.goalSelector.addGoal(2, new HumanGoals.Controlled(this));
        this.goalSelector.addGoal(1, new HumanGoals.Mount(this, this.baseMoveSpeed, false));
    }

    @Override
    public void setRandomType() {
    }

    @Override
    public void getLeader() {
        if (this.getOwnerEntity() == null) {
            super.getLeader();
        } else if (this.owner instanceof HumanEntity leader && leader.getOwnerEntity() != null && !(leader.getOwnerEntity() instanceof Player)) {
            this.owner = leader.getOwnerEntity();
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!(this.getOwnerEntity() instanceof Player)) {
            if (!this.level().isClientSide()) {
                player.sendSystemMessage(Component.empty().append(this.getName().copy().withStyle(ChatFormatting.DARK_GREEN))
                        .append(Component.literal(" at your orders").withStyle(ChatFormatting.WHITE)));
                this.owner = player;
                this.humanType = "friend." + player.getGameProfile().name();
                // deliberately different from 1.5.2 (canDespawn was true for every mercenary): one that serves a player stays
                this.setPersistenceRequired();
            }
            return InteractionResult.SUCCESS;
        }
        if (this.getOwnerEntity() != player) {
            return InteractionResult.PASS;
        }
        return this.interactEquip(player, hand, !player.getAbilities().instabuild) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public boolean isSuitableTargetAlly(@Nullable LivingEntity entity) {
        if (entity instanceof Player player) {
            String type = this.getHumanType();
            return type == null || !type.contains(".") || type.contains(player.getGameProfile().name());
        }
        return super.isSuitableTargetAlly(entity);
    }

    @Override
    public boolean isMercenary() {
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }
}
