package slimeknights.mantle.loot.function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.core.Holder;
import java.util.Optional;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.fluid.compat.IFluidHandler.FluidAction;
import slimeknights.mantle.fluid.compat.IFluidHandlerItem;
import slimeknights.mantle.loot.MantleLoot;

import java.util.List;

/**
 * Loot function to set the fluid on a dropped item
 */
public class SetFluidLootFunction extends LootItemConditionalFunction {
  public static final MapCodec<SetFluidLootFunction> CODEC = RecordCodecBuilder.mapCodec(inst -> commonFields(inst).and(
    new slimeknights.mantle.data.loadable.LoadableCodec<>(slimeknights.mantle.data.loadable.common.FluidStackLoadable.REQUIRED_STACK_NBT).fieldOf("fluid").forGetter(f -> f.fluid)
  ).apply(inst, SetFluidLootFunction::new));

  /** Fluid to add to the item */
  private final FluidStack fluid;

  protected SetFluidLootFunction(Optional<Holder<LootItemCondition>> conditions, FluidStack fluid) {
    super(conditions);
    this.fluid = fluid;
  }

  @Override
  protected ItemStack run(ItemStack stack, LootContext context) {
    IFluidHandlerItem fluidHandler = slimeknights.mantle.fluid.compat.FluidHandlers.getItemHandler(stack).orElse(null);
    if (fluidHandler != null) {
      fluidHandler.fill(fluid.copy(), FluidAction.EXECUTE);
      return fluidHandler.getContainer();
    }
    return stack;
  }

  @Override
  public MapCodec<SetFluidLootFunction> codec() {
    return CODEC;
  }

  /**
   * Creates a new builder with the given fluid
   * @param fluid  Fluid to set
   * @return  Builder instance
   */
  public static Builder<?> builder(FluidStack fluid) {
    return simpleBuilder(conditions -> new SetFluidLootFunction(conditions, fluid));
  }
}
