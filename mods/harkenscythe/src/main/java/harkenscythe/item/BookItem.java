package harkenscythe.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * ItemHSBook: the Book of Shadows (0) and the Book of Carnage (1), altar items; the Necronomicon (2, see
 * {@link NecronomiconItem}) and its pages (3). All epic; all but the page shine.
 */
public class BookItem extends Item {
    private final int kind;

    public BookItem(int kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public static Item.Properties properties(Item.Properties p) {
        return p.stacksTo(1).rarity(Rarity.EPIC).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        int lines = switch (this.kind) {
            case 2 -> 5;
            default -> 2;
        };
        for (int i = 1; i <= lines; i++) {
            builder.accept(Component.translatable("tooltip.harkenscythe_core.book." + this.kind + "." + i).withStyle(ChatFormatting.GRAY));
        }
    }
}
