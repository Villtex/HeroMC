package bibliocraft.decor;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

/** BlockItemLantern / BlockItemLamp: the colour as a tooltip line ("Red Candle", "Red Lamp Hood"). */
public class LightBlockItem extends BlockItem {
    private final String colorLine;

    public LightBlockItem(Block block, Properties properties, String colorLine) {
        super(block, properties);
        this.colorLine = colorLine;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal(this.colorLine));
    }
}
