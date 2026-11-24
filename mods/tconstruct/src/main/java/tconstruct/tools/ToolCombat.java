package tconstruct.tools;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import tconstruct.TSounds;

/**
 * AbilityHelper.onLeftClickEntity and the parts of TActiveOmniMod that act on a hit. The normal swing of a player goes
 * through vanilla (attack damage attribute, combat152): {@link #modDamage} and the damage modifier come in through
 * {@link TEvents}, {@link #afterHit} through {@code hurtEnemy}. The scythe (every entity around the target) and the
 * thrown dagger use {@link #attack}, the whole 1.5.2 hit.
 */
public final class ToolCombat {
    private static final RandomSource RANDOM = RandomSource.createThreadSafe();
    /** True while {@link #attack} hurts the target (its damage is complete, {@link TEvents} leaves it alone). */
    static final ThreadLocal<Boolean> ATTACKING = ThreadLocal.withInitial(() -> false);

    private ToolCombat() {
    }

    /**
     * TActiveOmniMod.attackDamage: against the undead the hammer (4..8) and smite (1 + n/18 + 0..n/18), against
     * arthropods bane of arthropods (1 + n/2 + 0..n/2).
     */
    public static int modDamage(ToolType type, InfiTool t, Entity target) {
        int bonus = 0;
        if (target instanceof LivingEntity living) {
            if (living.is(EntityTypeTags.UNDEAD)) {
                if (type == ToolType.HAMMER) {
                    int level = 2;
                    bonus += RANDOM.nextInt(level * 2 + 1) + level * 2;
                }
                if (t.has("ModSmite")) {
                    int base = t.getArray("ModSmite")[0] / 18;
                    bonus += 1 + base + RANDOM.nextInt(base + 1);
                }
            }
            if (living.is(EntityTypeTags.ARTHROPOD) && t.has("ModAntiSpider")) {
                int base = t.getArray("ModAntiSpider")[0] / 2;
                bonus += 1 + base + RANDOM.nextInt(base + 1);
            }
        }
        return bonus;
    }

    /**
     * The whole AbilityHelper.onLeftClickEntity: Attack + base damage, strength / weakness, stonebound, sprint lunge,
     * the modifiers (damage, knockback), a critical hit (falling, or the cutlass in 10), the damage modifier of the tool;
     * a broken tool hits for the base damage or 1. A hit wears the tool, heals with necrotic, pushes the target, sets it
     * on fire (fiery, lava) and calls the hit effects of the tool. True if it caused damage.
     */
    public static boolean attack(ItemStack stack, LivingEntity attacker, Entity target, ToolCoreItem tool, int baseDamage) {
        if (!(attacker.level() instanceof ServerLevel level) || !target.isAttackable() || target.skipAttackInteraction(attacker)) {
            return false;
        }
        InfiTool t = InfiTool.of(stack);
        if (t == null) {
            return false;
        }
        int damage = t.attack + baseDamage;
        ToolAbilities.midStreamModify(stack, tool.type, level);
        MobEffectInstance strength = attacker.getEffect(MobEffects.STRENGTH);
        if (strength != null) {
            damage += 3 << strength.getAmplifier();
        }
        MobEffectInstance weakness = attacker.getEffect(MobEffects.WEAKNESS);
        if (weakness != null) {
            damage -= 2 << weakness.getAmplifier();
        }
        float knockback = 0.0F;
        float stonebound = (float) Math.log(t.damage / 72.0F + 1.0F) * -2.0F * t.shoddy;
        damage = (int) (damage + stonebound);
        if (damage < 1) {
            damage = 1;
        }
        if (attacker.isSprinting()) {
            knockback++;
            float lunge = tool.type.chargeAttack();
            if (lunge > 1.0F) {
                knockback += lunge - 1.0F;
                damage = (int) (damage * lunge);
            }
        }
        knockback += t.getFloat("Knockback");
        damage += modDamage(tool.type, t, target);
        if (damage <= 0) {
            return false;
        }
        boolean critical = attacker.fallDistance > 0.0 && !attacker.onGround() && !attacker.onClimbable() && !attacker.isInWater()
                && !attacker.hasEffect(MobEffects.BLINDNESS) && !attacker.isPassenger() && target instanceof LivingEntity;
        if (tool.type == ToolType.CUTLASS && RANDOM.nextInt(10) == 0) {
            critical = true;
        }
        if (critical) {
            damage += RANDOM.nextInt(damage / 2 + 2);
        }
        if (tool.type.damageModifier != 1.0F) {
            damage = (int) (damage * tool.type.damageModifier);
        }
        if (t.broken) {
            damage = baseDamage > 0 ? baseDamage : 1;
        }
        DamageSource source = attacker instanceof Player player ? level.damageSources().playerAttack(player) : level.damageSources().mobAttack(attacker);
        boolean caused;
        ATTACKING.set(true);
        try {
            caused = target.hurtServer(level, source, damage);
        } finally {
            ATTACKING.set(false);
        }
        if (caused) {
            ToolCoreItem.onBlockChanged(stack, attacker);
            if (knockback > 0.0F) {
                float yaw = attacker.getYRot() * Mth.DEG_TO_RAD;
                target.push(-Mth.sin(yaw) * knockback * 0.5F, 0.1, Mth.cos(yaw) * knockback * 0.5F);
                target.needsSync = true;
                Vec3 motion = attacker.getDeltaMovement();
                attacker.setDeltaMovement(motion.x * 0.6, motion.y, motion.z * 0.6);
                attacker.setSprinting(false);
            }
            if (attacker instanceof Player player && critical) {
                player.crit(target);
            }
            attacker.setLastHurtMob(target);
            if (target instanceof LivingEntity living) {
                necrotic(t, attacker);
                fire(t, living);
                hitEffects(tool.type, stack, living, attacker);
            }
        }
        if (target instanceof Player && attacker instanceof Player player) {
            player.causeFoodExhaustion(0.3F);
        }
        return caused;
    }

    /**
     * What follows a hit of a normal swing (from {@code hurtEnemy}, the damage is done): lapis grows, necrotic heals,
     * fiery and lava set the target on fire, the knockback modifier pushes, and the hit effects of the tool.
     */
    public static void afterHit(ToolCoreItem tool, ItemStack stack, LivingEntity target, LivingEntity attacker) {
        InfiTool t = InfiTool.of(stack);
        if (t == null || !(attacker.level() instanceof ServerLevel level)) {
            return;
        }
        ToolAbilities.midStreamModify(stack, tool.type, level);
        t = InfiTool.of(stack);
        necrotic(t, attacker);
        fire(t, target);
        float knockback = t.getFloat("Knockback");
        if (knockback > 0.0F) {
            float yaw = attacker.getYRot() * Mth.DEG_TO_RAD;
            target.push(-Mth.sin(yaw) * knockback * 0.5F, 0.0, Mth.cos(yaw) * knockback * 0.5F);
            target.needsSync = true;
        }
        hitEffects(tool.type, stack, target, attacker);
    }

    /** Necrotic: heals the attacker 0..2n (PHConstruct.necroticUHS is off in the pack). */
    public static void necrotic(InfiTool t, LivingEntity attacker) {
        int drain = t.getInt("Necrotic") * 2;
        if (drain > 0) {
            attacker.heal(RANDOM.nextInt(drain + 1));
        }
    }

    /** Fiery (n / 5 + 1 seconds) and lava (+3). */
    public static void fire(InfiTool t, Entity target) {
        if (t.has("Fiery") || t.has("Lava")) {
            int fireAspect = 0;
            if (t.has("Fiery")) {
                fireAspect += t.getInt("Fiery") / 5 + 1;
            }
            if (t.getBool("Lava")) {
                fireAspect += 3;
            }
            if (fireAspect > 0) {
                target.igniteForSeconds(fireAspect);
            }
        }
    }

    /**
     * hitEntity / onEntityDamaged of the tools: battleaxe (motion x1.5), frying pan (x1.7, slowness 5 s, the pan
     * sound), rapier (vertical motion x0.8, 5 ticks less invulnerability), cleaver (7 ticks more invulnerability).
     */
    public static void hitEffects(ToolType type, ItemStack stack, LivingEntity target, LivingEntity attacker) {
        switch (type) {
            case BATTLEAXE -> knockbackEntity(target, 1.5);
            case FRYPAN -> {
                knockbackEntity(target, 1.7F);
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 0));
                target.level().playSound(null, target.getX(), target.getY(), target.getZ(), TSounds.FRYPAN_HIT.get(), SoundSource.PLAYERS, 1.0F,
                        (RANDOM.nextFloat() - RANDOM.nextFloat()) * 0.2F + 1.0F);
            }
            case RAPIER -> {
                Vec3 m = target.getDeltaMovement();
                target.setDeltaMovement(m.x, m.y * 0.8, m.z);
                target.needsSync = true;
                if (target.getInvulnerableTime() > 18) {
                    target.setInvulnerableTime(target.getInvulnerableTime() - 5);
                }
            }
            case CLEAVER -> target.setInvulnerableTime(target.getInvulnerableTime() + 7);
            default -> {
            }
        }
    }

    /** AbilityHelper.knockbackEntity: the horizontal motion times boost. */
    public static void knockbackEntity(LivingEntity living, double boost) {
        Vec3 m = living.getDeltaMovement();
        living.setDeltaMovement(m.x * boost, m.y, m.z * boost);
        living.needsSync = true;
    }
}
