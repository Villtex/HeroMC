package bibliocraft.printing;

import net.minecraft.world.item.Item;

/**
 * ItemChase "Print Press Chase": the frame the typesetting table turns into a plate. getItemEnchantability 20 is kept
 * as the enchantable component; no enchantment lists it, as in 1.5.2 where no enchantment type took it.
 */
public class ChaseItem extends Item {
    public ChaseItem(Properties properties) {
        super(properties);
    }
}
