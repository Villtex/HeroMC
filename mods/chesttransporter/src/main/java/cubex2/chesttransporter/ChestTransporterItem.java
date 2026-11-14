package cubex2.chesttransporter;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * ItemChestTransporter. Damage 0 = empty, damage 1 = carries a chest; here the second state is the CONTAINER
 * component (the chest's slots, positions kept like the "Slot" tags of the original).
 */
public class ChestTransporterItem extends Item {
    public ChestTransporterItem(Properties properties) {
        super(properties);
    }

    public static boolean isFull(ItemStack stack) {
        return stack.has(DataComponents.CONTAINER);
    }

    /** Moves every item of the chest into the transporter and empties the chest. */
    public static void takeFrom(Container chest, ItemStack stack) {
        NonNullList<ItemStack> items = NonNullList.withSize(chest.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < chest.getContainerSize(); i++) {
            items.set(i, chest.removeItemNoUpdate(i));
        }
        chest.setChanged();
        stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
    }

    /** Puts the carried items back into their slots of the chest (slots it does not have are lost, as in 1.5.2). */
    public static void putInto(ItemStack stack, Container chest) {
        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        NonNullList<ItemStack> items = NonNullList.withSize(contents.getSlots(), ItemStack.EMPTY);
        contents.copyInto(items);
        for (int i = 0; i < items.size() && i < chest.getContainerSize(); i++) {
            if (!items.get(i).isEmpty()) {
                chest.setItem(i, items.get(i));
            }
        }
        chest.setChanged();
        stack.remove(DataComponents.CONTAINER);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (isFull(stack)) {
            int count = stack.get(DataComponents.CONTAINER).nonEmptyItemCopyStream().mapToInt(ItemStack::getCount).sum();
            tooltip.accept(Component.translatable("item.chesttransporter.chest_transporter.contains", count));
        }
    }
}
