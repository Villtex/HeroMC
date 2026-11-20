package xreliquary.item;

import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import xreliquary.XRFx;
import xreliquary.XRItems;

/** ItemEmptyVoidTear: right click absorbs the dominant item of the own inventory (or of a clicked container) into a void tear. */
public class EmptyVoidTearItem extends XRItem {
    public EmptyVoidTearItem(Properties properties) {
        super(properties, 2);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (XRFx.creative(player) || level.isClientSide()) {
            return InteractionResult.PASS;
        }
        ItemStack tear = compress(player.getInventory(), player.getInventory().getContainerSize());
        if (tear.isEmpty()) {
            return InteractionResult.PASS;
        }
        XRFx.orb(player);
        stack.shrink(1);
        XRFx.give(player, tear);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && !context.getLevel().isClientSide()
                && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof Container inv) {
            ItemStack tear = compress(inv, inv.getContainerSize());
            if (!tear.isEmpty()) {
                XRFx.orb(player);
                stack.shrink(1);
                if (stack.isEmpty()) {
                    player.setItemInHand(context.getHand(), tear);
                } else {
                    XRFx.give(player, tear);
                }
            }
        }
        return InteractionResult.PASS;
    }

    /** compressInventoryIntoTearForPlayer. */
    private ItemStack compress(Container inv, int slots) {
        ItemStack target = VoidContents.target(inv, slots, this);
        if (target.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack like = target.copyWithCount(1);
        int quantity = VoidContents.count(inv, slots, like);
        ItemStack tear = new ItemStack(XRItems.VOID_TEAR.get());
        VoidContents.set(tear, like.getItem(), quantity, -1);
        VoidContents.remove(inv, slots, like, quantity);
        return tear;
    }
}
