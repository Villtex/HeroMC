package projectzulu.potion;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import projectzulu.ZuluPotions;

import java.util.ArrayList;
import java.util.List;

/**
 * The work of the Zulu effects. 1.5.2 ran cleansing and thorn in PotionEventHookContainerClass and curse/slowfall in
 * Potion.performEffect; here all run from the entity tick (before the effects tick, like LivingUpdateEvent), so that
 * removing or ticking other effects never touches the effect map while vanilla iterates it.
 */
public final class PotionEvents {
    private PotionEvents() {
    }

    /** Potion.isReady of PotionCleansing/PotionCurse (25 >> amp) and PotionSlowFall (4 >> amp). */
    private static boolean isReady(LivingEntity entity, MobEffectInstance effect, int base) {
        int interval = base >> effect.getAmplifier();
        int duration = effect.isInfiniteDuration() ? entity.tickCount : effect.getDuration();
        return interval <= 0 || duration % interval == 0;
    }

    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        slowfall(entity);
        if (entity.level() instanceof ServerLevel level) {
            cleansing(entity);
            curse(level, entity);
        }
    }

    /** PotionSlowFall: limits fall distance and fall speed; runs on both sides so the client moves the player. */
    private static void slowfall(LivingEntity entity) {
        MobEffectInstance effect = entity.getEffect(ZuluPotions.SLOWFALL);
        if (effect == null || !isReady(entity, effect, 4)) {
            return;
        }
        int amp = Math.min(effect.getAmplifier(), 4);
        double maxFall = (4 - amp + 1) * 2;
        if (entity.fallDistance > maxFall) {
            entity.fallDistance = maxFall;
        }
        Vec3 motion = entity.getDeltaMovement();
        double minY = -1.0 + (amp + 1) * 0.15;
        if (motion.y < minY) {
            entity.setDeltaMovement(motion.x, minY, motion.z);
        }
    }

    /** PotionEventHookContainerClass.EntitySelfCleansingPotion: removes one random other effect now and then. */
    private static void cleansing(LivingEntity entity) {
        MobEffectInstance cleansing = entity.getEffect(ZuluPotions.CLEANSING);
        if (cleansing == null || !isReady(entity, cleansing, 25)) {
            return;
        }
        List<MobEffectInstance> effects = new ArrayList<>(entity.getActiveEffects());
        if ((cleansing.getAmplifier() + 1) * 25 + 25 - entity.getRandom().nextInt(100) < 0) {
            return;
        }
        int index = effects.isEmpty() ? 0 : entity.level().getRandom().nextInt(effects.size());
        if (index < effects.size()) {
            Holder<MobEffect> picked = effects.get(index).getEffect();
            if (!picked.is(ZuluPotions.CLEANSING)) {
                entity.removeEffect(picked);
            }
        }
    }

    /** PotionCurse.performEffect: runs one extra tick of a random other active effect. */
    private static void curse(ServerLevel level, LivingEntity entity) {
        MobEffectInstance curse = entity.getEffect(ZuluPotions.CURSE);
        if (curse == null || !isReady(entity, curse, 25)) {
            return;
        }
        List<MobEffectInstance> effects = new ArrayList<>(entity.getActiveEffects());
        if ((curse.getAmplifier() + 1) * 25 + 25 - entity.getRandom().nextInt(100) < 0) {
            return;
        }
        int index = effects.isEmpty() ? 0 : entity.level().getRandom().nextInt(effects.size());
        if (index < effects.size()) {
            MobEffectInstance picked = effects.get(index);
            if (!picked.getEffect().is(ZuluPotions.CURSE) && picked.getDuration() != 0) {
                picked.getEffect().value().applyEffectTick(level, entity, picked.getAmplifier());
            }
        }
    }

    /** PotionEventHookContainerClass.cactusArmorDamage: a living attacker takes 3/4 of the damage it deals. */
    public static void onDamage(LivingDamageEvent.Pre event) {
        LivingEntity hurt = event.getEntity();
        if (hurt.level() instanceof ServerLevel level && hurt.hasEffect(ZuluPotions.THORN)
                && event.getSource().getDirectEntity() instanceof LivingEntity attacker && attacker != hurt) {
            attacker.hurtServer(level, hurt.damageSources().mobAttack(hurt), event.getOriginalDamage() * 3 / 4);
        }
    }
}
