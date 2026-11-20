package tconstruct.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * {@code Pattern} (wooden pattern, woodPattern) and {@code MetalPattern} (metal cast): the item damage of 1.5.2 is the
 * shape ({@link tconstruct.TItems#PATTERN_NAMES}). Only the wooden patterns work in the part builder; the casts are
 * for the casting table of part 25c.
 */
public class PatternItem extends Item {
    public final int meta;
    public final boolean cast;

    public PatternItem(int meta, boolean cast, Properties properties) {
        super(properties);
        this.meta = meta;
        this.cast = cast;
    }

    /** Pattern.getPatternCost: the material in half ingots (2 = one ingot / plank / crystal). */
    public static int cost(int meta) {
        return switch (meta) {
            case 0, 2, 3, 4, 5, 10, 11, 24, 25 -> 2;
            case 1, 6, 7, 8, 9, 12, 13 -> 1;
            case 14, 15, 22, 23 -> 6;
            case 16, 17, 18, 19, 20, 21 -> 16;
            default -> 0;
        };
    }

    public int cost() {
        return cost(this.meta);
    }

    /** Pattern.addInformation: "Material Cost: 1" / "Material Cost: 0.5". */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        float cost = this.cost() / 2.0F;
        if (cost > 0.0F) {
            String value = cost - (int) cost < 0.1 ? String.valueOf((int) cost) : String.valueOf(cost);
            tooltip.accept(Component.translatable("tooltip.tconstruct.material_cost", value));
        }
    }
}
