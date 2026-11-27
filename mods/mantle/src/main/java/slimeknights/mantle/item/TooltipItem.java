package slimeknights.mantle.item;

import java.util.ArrayList;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import slimeknights.mantle.util.TranslationHelper;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Item with automatic tooltip support
 */
public class TooltipItem extends Item {

  public TooltipItem(Properties properties) {
    super(properties);
  }

  /** Port: 26.3 tooltip method, collects the lines of the 1.20.1 form below */
  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
    List<Component> lines = new ArrayList<>();
    appendHoverText(stack, context.level(), lines, flag);
    lines.forEach(tooltip);
    super.appendHoverText(stack, context, display, tooltip, flag);
  }

  /** Port: 1.20.1 tooltip form, kept so subclasses (Tinkers) can keep overriding it */
  public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
    TranslationHelper.addOptionalTooltip(stack, tooltip);
  }
}
