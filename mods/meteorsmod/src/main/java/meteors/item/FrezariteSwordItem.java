package meteors.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** ItemFrezariteSword: every hit slows the target (moveSlowdown, 40 ticks, amplifier 5). */
public class FrezariteSwordItem extends Item {
    public FrezariteSwordItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 5));
        super.hurtEnemy(stack, target, attacker);
    }
}
