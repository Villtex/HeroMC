package xreliquary.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Ice Magus Rod, Sojourner's Staff, Touchstone of Midas: the charge is the item damage of 1.5.2 (0 = empty, the most
 * charge at 1, max - 1 = one left; durability bar as in 1.5.2). Refilled one fuel item per tick.
 */
public class ChargeItem extends XRItem {
    public ChargeItem(Properties properties, int lines) {
        super(properties, lines);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    /** onUpdate: (damage == 0 || damage > step) and fuel found -> 0 becomes max - 1 (resp. refill), else damage - step. */
    protected static void refuel(ItemStack stack, Player player, Item fuel, int step, int fromEmpty) {
        int damage = stack.getDamageValue();
        if ((damage == 0 || damage > step) && xreliquary.XRFx.consumeMain(player, fuel)) {
            stack.setDamageValue(damage == 0 ? fromEmpty : damage - step);
        }
    }
}
