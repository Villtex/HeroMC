package slimeknights.tconstruct.library.utils;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;

import java.util.List;

/**
 * Port (Teil 27e): 1.20.1 Forge cured effects with "curative items" (milk, honey); 26.3 has no cure system.
 * An item cures what it removes when consumed ({@code minecraft:consumable} with {@code clear_all_effects} or
 * {@code remove_effects}): milk cures every effect, honey cures poison.
 */
public final class EffectCures {
  private EffectCures() {}

  /** Consume effects of the item */
  private static List<ConsumeEffect> effects(ItemStack stack) {
    Consumable consumable = stack.get(DataComponents.CONSUMABLE);
    return consumable == null ? List.of() : consumable.onConsumeEffects();
  }

  /** True if the item cures the effect (1.20.1 {@code MobEffectInstance#isCurativeItem}) */
  public static boolean isCuredBy(Holder<MobEffect> effect, ItemStack stack) {
    for (ConsumeEffect consume : effects(stack)) {
      if (consume instanceof ClearAllStatusEffectsConsumeEffect || consume instanceof RemoveStatusEffectsConsumeEffect remove && remove.effects().contains(effect)) {
        return true;
      }
    }
    return false;
  }

  /** True if the item cures the effect */
  public static boolean isCuredBy(MobEffectInstance instance, ItemStack stack) {
    return isCuredBy(instance.getEffect(), stack);
  }

  /** Removes all effects the item cures (1.20.1 {@code LivingEntity#curePotionEffects}), true if any was removed */
  public static boolean cure(LivingEntity living, ItemStack stack) {
    if (living.level().isClientSide()) {
      return false;
    }
    List<Holder<MobEffect>> cured = living.getActiveEffects().stream().map(MobEffectInstance::getEffect).filter(effect -> isCuredBy(effect, stack)).toList();
    boolean removed = false;
    for (Holder<MobEffect> effect : cured) {
      removed |= living.removeEffect(effect);
    }
    return removed;
  }
}
