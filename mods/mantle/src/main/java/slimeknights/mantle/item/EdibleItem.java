package slimeknights.mantle.item;

import java.util.ArrayList;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import slimeknights.mantle.util.TranslationHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class EdibleItem extends Item {
  public EdibleItem(FoodProperties foodIn) {
    this(new Properties().food(foodIn));
  }

  public EdibleItem(Item.Properties properties) {
    super(properties);
    // Port: food is a data component in 26.3, checked by the properties (food(...)) instead
  }

  /** Port: 26.3 tooltip method; effect names from the consume effects */
  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
    List<Component> lines = new ArrayList<>();
    TranslationHelper.addOptionalTooltip(stack, lines);
    for (MobEffectInstance effect : ContainerFoodItem.getEffects(stack)) {
      lines.add(Component.translatable(effect.getDescriptionId()).withStyle(ChatFormatting.GRAY));
    }
    lines.forEach(tooltip);
    super.appendHoverText(stack, context, display, tooltip, flag);
  }
}
