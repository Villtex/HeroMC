package betterdungeons.mob;

import betterdungeons.mob.ai.FlyGoal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * EntityWyvern ("Wyvern"): a four-legged dragon, 200 health, 1.5 x 1.5, fire immune, never takes fall damage, bites for
 * 2 (EntityMob), 5 xp. It hunts monsters and players up to 60 blocks away and flies at them ({@link FlyGoal}); in the
 * air with a target it does not fall. Only its egg made it in 1.5.2 (no dungeon), and its texture (bdimg/dragonbd.png)
 * was missing from the mod.
 */
public class Wyvern extends BDBoss {
    public Wyvern(EntityType<? extends Wyvern> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH, 200.0).add(Attributes.ATTACK_DAMAGE, 2.0).add(Attributes.MOVEMENT_SPEED, 0.33)
                .add(Attributes.FOLLOW_RANGE, 60.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FlyGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 0, true, false, (t, l) -> t instanceof Enemy));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, null));
    }

    @Override
    public void aiStep() {
        if (!this.onGround() && this.getTarget() != null) {
            Vec3 m = this.getDeltaMovement();
            this.setDeltaMovement(m.x, 0.0, m.z);
        }
        super.aiStep();
    }

    /** updateFallState did nothing: it never gathers fall distance. */
    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource source) {
        return false;
    }
}
