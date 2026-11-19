package qcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;
import qcraft.QuantumCraft;
import qcraft.block.AreaData;
import qcraft.block.QuantumComputerBlockEntity;

import java.util.function.Consumer;

/** ItemQuantumComputer: entanglement frequency and stored area as components ("e", "R", "d"). */
public class QuantumComputerItem extends BlockItem {
    public QuantumComputerItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static ItemStack create(int entanglementFrequency, int quantity) {
        ItemStack result = new ItemStack(QuantumCraft.QUANTUM_COMPUTER_ITEM.get(), quantity);
        QBlockItem.setEntanglementFrequency(result, entanglementFrequency);
        return result;
    }

    public static int getEntanglementFrequency(ItemStack stack) {
        return QBlockItem.getEntanglementFrequency(stack);
    }

    public static void setStoredData(ItemStack stack, @Nullable AreaData data) {
        if (data != null) {
            stack.set(QComponents.STORED_AREA.get(), data);
        } else {
            stack.remove(QComponents.STORED_AREA.get());
        }
    }

    public static @Nullable AreaData getStoredData(ItemStack stack) {
        return stack.get(QComponents.STORED_AREA.get());
    }

    @Override
    public void onCraftedBy(ItemStack stack, Player player) {
        super.onCraftedBy(stack, player);
        if (getEntanglementFrequency(stack) == 0 && !player.level().isClientSide()) {
            QBlockItem.setEntanglementFrequency(stack, QuantumComputerBlockEntity.getEntanglementRegistry(player.level()).getUnusedFrequency());
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getEntanglementFrequency(stack) >= 0 ? "item.quantumcraft.quantum_computer.entangled"
                : "block.quantumcraft.quantum_computer");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        int frequency = getEntanglementFrequency(stack);
        if (frequency > 0) {
            tooltip.accept(Component.translatable("tooltip.quantumcraft.group", QBlockItem.formatFrequency(frequency)).withStyle(ChatFormatting.GRAY));
        }
        AreaData data = getStoredData(stack);
        if (data != null) {
            AreaData.Shape s = data.shape();
            int w = s.xMax() - s.xMin() + 1;
            int h = s.yMax() - s.yMin() + 1;
            int l = s.zMax() - s.zMin() + 1;
            tooltip.accept(Component.translatable("tooltip.quantumcraft.stored_area", w, l, h).withStyle(ChatFormatting.GRAY));
        }
    }
}
