package xreliquary.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * ItemXR: an item with the fixed lines of addInformation (language keys {@code <item>.tooltip.0..n-1}).
 */
public class XRItem extends Item {
    private final int lines;

    public XRItem(Properties properties, int lines) {
        super(properties);
        this.lines = lines;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        for (int i = 0; i < this.lines; i++) {
            tooltip.accept(Component.translatable(this.getDescriptionId() + ".tooltip." + i).withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    }
}
