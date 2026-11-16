package harkenscythe.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * ItemHSSpectralArmor: leather, soulweave or bloodweave armour dyed with spectral dye. 1.5.2 showed the armour points
 * of leather and took 7 % of the damage per piece (ISpecialArmor); here it has the armour points of leather, which
 * for the full set gives the same 28 %. The spectral soulweave and bloodweave pieces count for the set bonuses.
 */
public class SpectralArmorItem extends Item {
    public SpectralArmorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.harkenscythe_core.dyed").withStyle(ChatFormatting.GRAY));
    }
}
