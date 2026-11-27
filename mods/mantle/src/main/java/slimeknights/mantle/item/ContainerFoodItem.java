package slimeknights.mantle.item;

import java.util.ArrayList;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Food item with a container that is returned when the item is consumed. Supports eating stackable items with containers.
 * Technically also works for items with no container.
 */
@SuppressWarnings("unused") // API
public class ContainerFoodItem extends Item {
  private final ItemUseAnimation useAnim;
  public ContainerFoodItem(Properties props, ItemUseAnimation useAnim) {
    super(props);
    this.useAnim = useAnim;
  }

  public ContainerFoodItem(Properties props) {
    this(props, ItemUseAnimation.DRINK);
  }

  @Override
  public ItemUseAnimation getUseAnimation(ItemStack pStack) {
    return useAnim;
  }

  /** Port: 26.3 food effects are consume effects of the {@code minecraft:consumable} component */
  public static List<MobEffectInstance> getEffects(ItemStack stack) {
    Consumable consumable = stack.get(DataComponents.CONSUMABLE);
    List<MobEffectInstance> effects = new ArrayList<>();
    if (consumable != null) {
      for (ConsumeEffect effect : consumable.onConsumeEffects()) {
        if (effect instanceof ApplyStatusEffectsConsumeEffect apply) {
          effects.addAll(apply.effects());
        }
      }
    }
    return effects;
  }

  /** Adds effects to the tooltip */
  public static void addEffectTooltip(List<MobEffectInstance> effects, List<Component> tooltip) {
    // add effects to the tooltip, code based on potion items
    for (MobEffectInstance effect : effects) {
      MutableComponent mutable = Component.translatable(effect.getDescriptionId());
      if (effect.getAmplifier() > 0) {
        mutable = Component.translatable("potion.withAmplifier", mutable, Component.translatable("potion.potency." + effect.getAmplifier()));
      }
      if (effect.getDuration() > 20) {
        mutable = Component.translatable("potion.withDuration", mutable, MobEffectUtil.formatDuration(effect, 1.0f, 20f));
      }
      tooltip.add(mutable.withStyle(effect.getEffect().value().getCategory().getTooltipFormatting()));
    }
  }

  /** Port: 26.3 tooltip method, see {@link TooltipItem} */
  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
    List<Component> lines = new ArrayList<>();
    appendHoverText(stack, context.level(), lines, flag);
    lines.forEach(tooltip);
    super.appendHoverText(stack, context, display, tooltip, flag);
  }

  /** Port: 1.20.1 tooltip form */
  public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
    addEffectTooltip(getEffects(stack), tooltip);
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
    ItemStackTemplate remainder = stack.getItem().getCraftingRemainder();
    ItemStack container = remainder == null ? ItemStack.EMPTY : remainder.create();
    ItemStack result = super.finishUsingItem(stack, level, living);
    Player player = living instanceof Player p ? p : null;
    if (!container.isEmpty() && (player == null || !player.getAbilities().instabuild)) {
      container = container.copy();
      if (result.isEmpty()) {
        return container;
      }
      if (player != null) {
        if (!player.getInventory().add(container)) {
          player.drop(container, false, Prediction.SERVER_ONLY);
        }
      }
    }
    return result;
  }

  /** Fluid containing variant of {@link ContainerFoodItem} */
  public static class FluidContainerFoodItem extends ContainerFoodItem implements IFluidContainerItem {
    private final Supplier<FluidStack> fluid;
    public FluidContainerFoodItem(Properties props, Supplier<FluidStack> fluid) {
      super(props);
      this.fluid = fluid;
    }

    @Override
    public ResourceHandler<FluidResource> createFluidHandler(ItemStack stack, ItemAccess access) {
      return new ConstantFluidContainerWrapper(fluid.get(), access);
    }
  }
}
