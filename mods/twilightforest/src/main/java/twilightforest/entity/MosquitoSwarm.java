package twilightforest.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import twilightforest.TFEntities;

/**
 * EntityTFMosquitoSwarm: 12 health, 0.7 x 1.9, speed 0.23, step height 2.1, bite 3 with hunger (7 / 15 / 30 s by
 * difficulty). In the swamp it spawns in any light. One per chunk.
 */
public class MosquitoSwarm extends TFMonster {
    public MosquitoSwarm(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 12.0).add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.STEP_HEIGHT, 2.1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int attackStrength(Entity target) {
        return 3;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (super.doHurtTarget(level, target)) {
            if (target instanceof LivingEntity living) {
                int seconds = level.getDifficulty() == Difficulty.NORMAL ? 15 : level.getDifficulty() == Difficulty.HARD ? 30 : 7;
                living.addEffect(new MobEffectInstance(MobEffects.HUNGER, seconds * 20, 0));
            }
            return true;
        }
        return false;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TFEntities.MOSQUITO_SOUND.get();
    }
}
