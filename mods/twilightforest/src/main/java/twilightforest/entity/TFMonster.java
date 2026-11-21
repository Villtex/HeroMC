package twilightforest.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * EntityMob of 1.5.2 for the Twilight monsters: the melee damage is getAttackStrength (2 + the held weapon's 1.5.2
 * damage, unless a mob overrides it) plus strength / weakness, not today's attribute sum.
 */
public abstract class TFMonster extends Monster {
    protected TFMonster(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    /** EntityMob.getAttackStrength: attackStrength 2 + the weapon in hand. */
    public int attackStrength(Entity target) {
        return 2 + weaponBonus(this);
    }

    /** ItemStack.getDamageVsEntity of the held item, 0 for an empty hand. */
    public static int weaponBonus(Mob mob) {
        return mob.getMainHandItem().isEmpty() ? 0 : TFMobUtil.oldWeaponDamage(mob.getMainHandItem());
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        return oldAttack(this, level, target, this.attackStrength(target));
    }

    /** EntityMob.attackEntityAsMob with the given strength: potions, the hit, knockback and fire aspect of the weapon. */
    public static boolean oldAttack(Mob mob, ServerLevel level, Entity target, int strength) {
        float damage = strength;
        MobEffectInstance boost = mob.getEffect(MobEffects.STRENGTH);
        if (boost != null) {
            damage += 3 << boost.getAmplifier();
        }
        MobEffectInstance weakness = mob.getEffect(MobEffects.WEAKNESS);
        if (weakness != null) {
            damage -= 2 << weakness.getAmplifier();
        }
        DamageSource source = mob.damageSources().mobAttack(mob);
        Vec3 oldMovement = target.getDeltaMovement();
        boolean hit = target.hurtServer(level, source, damage);
        if (hit) {
            if (mob instanceof TFMonster monster) {
                monster.causeExtraKnockback(target, monster.getKnockback(target, source), oldMovement, source, damage, true);
            }
            net.minecraft.world.item.enchantment.EnchantmentHelper.doPostAttackEffects(level, target, source);
            mob.setLastHurtMob(target);
        }
        return hit;
    }
}
