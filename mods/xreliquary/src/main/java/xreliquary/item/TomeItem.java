package xreliquary.item;

import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jspecify.annotations.Nullable;

/** ItemAlkahestryTome: setContainerItem(this), it stays in the crafting grid. */
public class TomeItem extends XRItem {
    public TomeItem(Properties properties) {
        super(properties, 2);
    }

    @Override
    public @Nullable ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
        return new ItemStackTemplate(this);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
