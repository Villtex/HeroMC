package xreliquary.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import xreliquary.XRFx;
import xreliquary.XRItems;

import java.util.function.Consumer;

/** ItemVoidTear: holds one kind of item; right click empties it into the own inventory or a clicked container. */
public class VoidTearItem extends XRItem {
    public VoidTearItem(Properties properties) {
        super(properties, 0);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.xreliquary.void_tear.tooltip.0"));
        Item item = VoidContents.item(stack);
        tooltip.accept(item == Items.AIR ? Component.translatable("item.xreliquary.void_tear.tooltip.empty")
                : Component.translatable("item.xreliquary.void_tear.tooltip.holds", VoidContents.quantity(stack), new ItemStack(item).getHoverName()));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            unload(player.getItemInHand(hand), player.getInventory(), 36, player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && !context.getLevel().isClientSide()
                && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof Container inv) {
            unload(stack, inv, inv.getContainerSize(), player);
        }
        return InteractionResult.PASS;
    }

    /** unloadContentsInto(Player)Inventory: an empty tear comes back when everything fitted. */
    private static void unload(ItemStack stack, Container inv, int slots, Player player) {
        Item item = VoidContents.item(stack);
        if (item == Items.AIR) {
            return;
        }
        int quantity = VoidContents.quantity(stack);
        while (quantity > 0 && VoidContents.addOne(inv, slots, item)) {
            quantity--;
        }
        XRFx.orb(player);
        if (quantity == 0) {
            XRFx.give(player, new ItemStack(XRItems.EMPTY_VOID_TEAR.get()));
            stack.shrink(1);
        } else {
            VoidContents.setQuantity(stack, quantity);
        }
    }
}
