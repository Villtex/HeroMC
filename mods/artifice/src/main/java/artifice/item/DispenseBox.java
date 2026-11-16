package artifice.item;

import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.DispenserBlock;

/**
 * DispenserBehaviorBox: a dispenser unpacks a box (at most a full stack) and shoots out the contents. 1.5.2 handed
 * the unpacked stack to the default behaviour, which shot one item and lost the rest; here the whole stack comes out.
 */
public class DispenseBox extends DefaultDispenseItemBehavior {
    private static void shoot(BlockSource source, ItemStack stack) {
        spawnItem(source.level(), stack, 6, source.state().getValue(DispenserBlock.FACING), DispenserBlock.getDispensePosition(source));
    }

    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        ItemStackTemplate contents = BoxItem.contents(stack);
        if (contents == null) {
            return stack;
        }
        int amount = contents.count();
        ItemStack thing = contents.create();
        int max = thing.getMaxStackSize();
        ItemStack extra = null;
        if (amount > max && stack.getCount() > 1) {
            extra = stack.copyWithCount(stack.getCount() - 1);
            stack.setCount(1);
        }
        if (amount > max) {
            shoot(source, thing.copyWithCount(max));
            if (amount - max < 1) {
                stack.shrink(1);
            } else {
                BoxItem.setAmount(stack, amount - max);
            }
        } else {
            stack.shrink(1);
            shoot(source, thing.copyWithCount(amount));
        }
        if (extra != null) {
            ItemStack rest = source.blockEntity().insertItem(extra);
            if (!rest.isEmpty()) {
                shoot(source, rest);
            }
        }
        return stack;
    }
}
