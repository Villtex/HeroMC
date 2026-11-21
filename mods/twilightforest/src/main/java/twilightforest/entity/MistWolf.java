package twilightforest.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.Level;

/**
 * EntityTFMistWolf: a hostile wolf of the dark forest, 1.4 x 1.9, 1.9 times the size and misty, 30 health, bite 6. In
 * the dark (brightness below 0.1) its bite blinds for 7 s on normal and 15 s on hard.
 */
public class MistWolf extends HostileWolf {
    public MistWolf(EntityType<? extends Wolf> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return HostileWolf.createAttributes().add(Attributes.MAX_HEALTH, 30.0);
    }

    @Override
    public int attackStrength(Entity target) {
        return 6;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (target.hurtServer(level, this.damageSources().mobAttack(this), this.attackStrength(target))) {
            float brightness = TFMobUtil.brightness(this);
            if (target instanceof LivingEntity living && brightness < 0.1F) {
                int seconds = level.getDifficulty() == Difficulty.NORMAL ? 7 : level.getDifficulty() == Difficulty.HARD ? 15 : 0;
                if (seconds > 0) {
                    living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, seconds * 20, 0));
                }
            }
            return true;
        }
        return false;
    }
}
