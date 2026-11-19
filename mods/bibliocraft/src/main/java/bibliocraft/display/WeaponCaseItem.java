package bibliocraft.display;

import bibliocraft.Woods;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** BlockItemWeaponCaseMaster: the tooltip names the lining ("Red Lining", "Blue Lining", ...). */
public class WeaponCaseItem extends BlockItem {
    private static final String[] LINING = {"Red Lining", "Blue Lining", "Green Lining", "Black Lining"};

    public WeaponCaseItem(WeaponCaseBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal(LINING[((WeaponCaseBlock) this.getBlock()).cloth % Woods.CASE_CLOTH.length]));
    }
}
