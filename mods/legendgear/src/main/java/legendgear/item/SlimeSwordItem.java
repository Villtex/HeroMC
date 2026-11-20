package legendgear.item;

import legendgear.LGFx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * ItemSlimeSword (EnumToolMaterial SLIME: 131 uses, enchantability 18, slime balls repair): hits for 1, leaves the mob
 * nauseous for a second, heals it by 1 and resets its hurt time, so the next hit lands at once.
 */
public class SlimeSwordItem extends Item {
    public SlimeSwordItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player && target.level() instanceof ServerLevel) {
            LGFx.sound(target, SoundEvents.SLIME_SQUISH_SMALL, 1.0F, 0.7F + target.getRandom().nextFloat() * 0.5F);
            target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 20));
            target.heal(1.0F);
            target.setInvulnerableTime(20);
        }
    }
}
