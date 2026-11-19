package bibliocraft.printing;

import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * ItemEnchantedPlate "Enchanted Plate": max damage 3 (each print adds one, the press drops it after the third), always
 * glinting. It carries the tag of the enchanted book it was made from, here the STORED_ENCHANTMENTS component. The
 * enchantment names in the tooltip come from that component (vanilla lists it for every item); without enchantments
 * the tooltip says "No data" as before.
 */
public class EnchantedPlateItem extends Item {
    public EnchantedPlateItem(Properties properties) {
        super(properties);
    }

    public static ItemEnchantments enchantments(ItemStack stack) {
        return stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (enchantments(stack).isEmpty()) {
            tooltip.accept(Component.literal("No data"));
        }
    }
}
