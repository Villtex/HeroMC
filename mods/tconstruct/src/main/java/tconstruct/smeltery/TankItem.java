package tconstruct.smeltery;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;
import tconstruct.TSmeltery;

import java.util.function.Consumer;

/** LavaTankItemBlock: names the liquid a broken tank still holds ("Contains ...", "... mB"). */
public class TankItem extends BlockItem {
    public TankItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        FluidStack liquid = stack.get(TSmeltery.LIQUID.get());
        if (liquid != null && !liquid.isEmpty()) {
            tooltip.accept(Component.translatable("tooltip.tconstruct.contains", liquid.getHoverName()));
            tooltip.accept(Component.translatable("tooltip.tconstruct.mb", liquid.getAmount()));
        }
    }
}
