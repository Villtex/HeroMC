package natura.item;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * ImpMeat.onFoodEaten: the effects add their time to what is still running. Raw: hunger 8 s, set on fire for 10 s,
 * poison 5 s with 75 %. Cooked: fire resistance 15 s, poison 5 s with 75 %.
 */
public class ImpMeatItem extends Item {
    private final boolean cooked;

    public ImpMeatItem(Properties properties, boolean cooked) {
        super(properties);
        this.cooked = cooked;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide()) {
            if (this.cooked) {
                extend(entity, MobEffects.FIRE_RESISTANCE, 300);
            } else {
                extend(entity, MobEffects.HUNGER, 160);
                entity.igniteForSeconds(10.0F);
            }
            if (level.getRandom().nextFloat() < 0.75F) {
                extend(entity, MobEffects.POISON, 100);
            }
        }
        return result;
    }

    private static void extend(LivingEntity entity, Holder<MobEffect> effect, int ticks) {
        MobEffectInstance running = entity.getEffect(effect);
        int duration = running != null ? running.getDuration() : 0;
        entity.addEffect(new MobEffectInstance(effect, duration + ticks, 0));
    }
}
