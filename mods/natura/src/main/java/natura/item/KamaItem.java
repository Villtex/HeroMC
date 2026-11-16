package natura.item;

import net.minecraft.world.item.ShearsItem;

/**
 * NaturaKama: a sword (ItemSword of the material) that also shears sheep and leaves and cuts cobwebs, leaves (15x)
 * and wool (5x) quickly. The sword values and the mining speeds come from the item properties
 * ({@code NaturaItems.kama}); shearing from {@link ShearsItem}.
 */
public class KamaItem extends ShearsItem {
    public KamaItem(Properties properties) {
        super(properties);
    }
}
