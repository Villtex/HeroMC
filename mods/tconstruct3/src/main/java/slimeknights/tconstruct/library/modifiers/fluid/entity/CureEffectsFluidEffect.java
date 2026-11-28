package slimeknights.tconstruct.library.modifiers.fluid.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.fluid.compat.IFluidHandler.FluidAction;
import slimeknights.mantle.data.loadable.common.ItemStackLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.fluid.EffectLevel;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffect;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectContext;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectContext.Entity;

/**
 * Effect to clear all effects using the given stack
 * @param stack  Stack used for curing, standard is milk bucket
 */
public record CureEffectsFluidEffect(slimeknights.mantle.recipe.helper.ItemOutput stackOutput) implements FluidEffect<FluidEffectContext.Entity> {
  /** Port (Teil 27e): the stack is made on first use, 26.3 has no item components while data loads */
  public ItemStack stack() {
    return stackOutput.get();
  }

  public static final RecordLoadable<CureEffectsFluidEffect> LOADER = RecordLoadable.create(slimeknights.mantle.recipe.helper.ItemOutput.Loadable.REQUIRED_ITEM.requiredField("item", e -> e.stackOutput), CureEffectsFluidEffect::new);

  public CureEffectsFluidEffect(ItemLike item) {
    this(slimeknights.mantle.recipe.helper.ItemOutput.fromItem(item));
  }

  @Override
  public float apply(FluidStack fluid, EffectLevel level, Entity context, FluidAction action) {
    LivingEntity target = context.getLivingTarget();
    if (target != null && level.isFull()) {
      // when simulating, search the effects list directly for curative effects
      // may still be wrong if the event cancels things though, no way to safely simulate it
      if (action.simulate()) {
        return target.getActiveEffects().stream().anyMatch(effect -> slimeknights.tconstruct.library.utils.EffectCures.isCuredBy(effect, stack())) ? 1 : 0;
      }
      return slimeknights.tconstruct.library.utils.EffectCures.cure(target, stack()) ? 1 : 0;
    }
    return 0;
  }

  @Override
  public RecordLoadable<CureEffectsFluidEffect> getLoader() {
    return LOADER;
  }
}
