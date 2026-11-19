package bibliocraft.printing;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * ItemPlate "Print Press Plate": made by the typesetting table, carries the name of the saved book it prints
 * ({@link Presses#PLATE_BOOK}, NBT "bookName"). The tooltip shows the name, or "Not Valid" without one (the original
 * also wrote "Not Valid" into the client's copy of the stack; the server never saw that, so a plate without a name
 * still has an empty name there).
 */
public class PlateItem extends Item {
    public PlateItem(Properties properties) {
        super(properties);
    }

    /** NBT "bookName", "" when missing (getString of a missing key). */
    public static String bookName(ItemStack stack) {
        String name = stack.get(Presses.PLATE_BOOK.get());
        return name == null ? "" : name;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        String name = stack.get(Presses.PLATE_BOOK.get());
        tooltip.accept(Component.literal(name == null ? "Not Valid" : name));
    }
}
