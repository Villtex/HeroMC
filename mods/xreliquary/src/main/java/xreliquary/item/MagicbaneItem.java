package xreliquary.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * ItemMagicbane: a gold sword (16 uses) whose hits weaken, slow, poison, confuse, wither or blind (by a d16 as in
 * 1.5.2) and then hit again for 4 + the sum of its enchantment levels (counts as far as it exceeds the first hit).
 */
public class MagicbaneItem extends XRItem {
    public MagicbaneItem(Properties properties) {
        super(properties, 2);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!(target.level() instanceof ServerLevel level)) {
            return;
        }
        int r = level.getRandom().nextInt(16);
        if (r <= 4) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2));
        }
        if (r <= 11) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
        } else if (r <= 13) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 2));
            target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 2));
        } else if (r == 14) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 2));
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 2));
        }
        if (attacker instanceof Player player) {
            int bonus = 0;
            for (var entry : stack.getEnchantments().entrySet()) {
                bonus += entry.getIntValue();
            }
            target.hurtServer(level, level.damageSources().playerAttack(player), bonus + 4);
        }
    }
}
