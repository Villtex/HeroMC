package qcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import qcraft.QuantumCraft;
import qcraft.block.QBlockEntity;

import java.util.function.Consumer;

/**
 * ItemQBlock: sub type and side types in {@link QComponents#QBLOCK}, the entanglement frequency in
 * {@link QComponents#ENTANGLEMENT}. Not in the creative tab (getSubItems was empty). A freshly crafted entangled stack
 * has frequency 0 and gets an unused one when it is taken out of the crafting grid (onCreated).
 */
public class QBlockItem extends BlockItem {
    public QBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static ItemStack create(int subType, int[] types, int entanglementFrequency, int quantity) {
        ItemStack result = new ItemStack(QuantumCraft.QBLOCK_ITEM.get(), quantity);
        result.set(QComponents.QBLOCK.get(), QComponents.QBlockData.of(subType, types));
        setEntanglementFrequency(result, entanglementFrequency);
        return result;
    }

    public static int getSubType(ItemStack stack) {
        QComponents.QBlockData data = stack.get(QComponents.QBLOCK.get());
        return data != null ? data.subType() : 0;
    }

    public static int[] getTypes(ItemStack stack) {
        QComponents.QBlockData data = stack.get(QComponents.QBLOCK.get());
        return data != null ? data.types() : new int[6];
    }

    /** setEntanglementFrequency of ItemQBlock and ItemQuantumComputer: "e" and the random "R" for frequency 0. */
    public static void setEntanglementFrequency(ItemStack stack, int entanglementFrequency) {
        if (entanglementFrequency < 0) {
            stack.remove(QComponents.ENTANGLEMENT.get());
            stack.remove(QComponents.NONCE.get());
        } else if (entanglementFrequency == 0) {
            stack.set(QComponents.ENTANGLEMENT.get(), 0);
            stack.set(QComponents.NONCE.get(), QBlockEntity.RANDOM.nextInt(16777215));
        } else {
            stack.set(QComponents.ENTANGLEMENT.get(), entanglementFrequency);
            stack.remove(QComponents.NONCE.get());
        }
    }

    public static int getEntanglementFrequency(ItemStack stack) {
        Integer frequency = stack.get(QComponents.ENTANGLEMENT.get());
        return frequency != null ? frequency : -1;
    }

    public static String formatFrequency(int frequency) {
        String result = Integer.toHexString(frequency).toUpperCase();
        return result.length() == 1 ? "0" + result : result;
    }

    @Override
    public void onCraftedBy(ItemStack stack, Player player) {
        super.onCraftedBy(stack, player);
        if (getEntanglementFrequency(stack) == 0 && !player.level().isClientSide()) {
            setEntanglementFrequency(stack, QBlockEntity.getEntanglementRegistry(player.level()).getUnusedFrequency());
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        boolean entangled = getEntanglementFrequency(stack) >= 0;
        String key = getSubType(stack) == 1 ? (entangled ? "item.quantumcraft.qblock.super.entangled" : "item.quantumcraft.qblock.super")
                : (entangled ? "item.quantumcraft.qblock.entangled" : "item.quantumcraft.qblock");
        return Component.translatable(key);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        int frequency = getEntanglementFrequency(stack);
        if (frequency > 0) {
            tooltip.accept(Component.translatable("tooltip.quantumcraft.group", formatFrequency(frequency)).withStyle(ChatFormatting.GRAY));
        }
    }
}
