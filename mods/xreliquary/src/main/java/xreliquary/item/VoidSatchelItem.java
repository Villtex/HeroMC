package xreliquary.item;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import xreliquary.XRData;
import xreliquary.XRFx;

import java.util.function.Consumer;

/**
 * ItemVoidSatchel: holds up to "capacity" of one item (64, +192 per three empty void tears, see the crafting events).
 * Right click in the air toggles it ("isTurnedOn"): switched on it pulls one of its item per tick out of the
 * inventory. On the top of a block it drops one stack (sneaking: everything); on a container it fills it, sneaking it
 * empties the container into the satchel.
 */
public class VoidSatchelItem extends XRItem {
    public VoidSatchelItem(Properties properties) {
        super(properties, 0);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return XRData.getBool(stack, "isTurnedOn");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.xreliquary.void_satchel.tooltip.0"));
        Item item = VoidContents.item(stack);
        if (item == Items.AIR) {
            tooltip.accept(Component.translatable("item.xreliquary.void_tear.tooltip.empty"));
            tooltip.accept(Component.translatable("item.xreliquary.void_satchel.tooltip.capacity", 64));
        } else {
            tooltip.accept(Component.translatable("item.xreliquary.void_tear.tooltip.holds", VoidContents.quantity(stack), new ItemStack(item).getHoverName()));
            tooltip.accept(Component.translatable("item.xreliquary.void_satchel.tooltip.capacity", VoidContents.capacity(stack)));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (XRData.getBool(stack, "isTurnedOn") && owner instanceof Player player) {
            Item item = VoidContents.item(stack);
            if (item != Items.AIR && !full(stack)) {
                ItemStack like = new ItemStack(item);
                if (VoidContents.count(player.getInventory(), player.getInventory().getContainerSize(), like) > 0) {
                    VoidContents.remove(player.getInventory(), player.getInventory().getContainerSize(), like, 1);
                    VoidContents.setQuantity(stack, VoidContents.quantity(stack) + 1);
                }
            }
        }
    }

    private static boolean full(ItemStack stack) {
        return VoidContents.quantity(stack) >= VoidContents.capacity(stack);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        XRData.setBool(stack, "isTurnedOn", !XRData.getBool(stack, "isTurnedOn"));
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        if (context.getClickedFace() == Direction.UP && player != null) {
            XRData.setBool(stack, "isTurnedOn", false);
            Item item = VoidContents.item(stack);
            int held = VoidContents.quantity(stack);
            int quantity = held >= 64 ? (player.isShiftKeyDown() ? held : 64) : held;
            int depleted = 0;
            var pos = context.getClickedPos();
            while (quantity > 0 && item != Items.AIR) {
                int max = new ItemStack(item).getMaxStackSize();
                int n = Math.min(max, quantity);
                quantity -= n;
                depleted += n;
                if (!context.getLevel().isClientSide()) {
                    context.getLevel().addFreshEntity(new ItemEntity(context.getLevel(), pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                            new ItemStack(item, n)));
                }
            }
            if (item != Items.AIR) {
                VoidContents.setQuantity(stack, held - depleted);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && !context.getLevel().isClientSide()
                && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof Container inv) {
            Item item = VoidContents.item(stack);
            if (item != Items.AIR) {
                if (player.isShiftKeyDown()) {
                    ItemStack like = new ItemStack(item);
                    while (!full(stack) && VoidContents.count(inv, inv.getContainerSize(), like) > 0) {
                        VoidContents.remove(inv, inv.getContainerSize(), like, 1);
                        VoidContents.setQuantity(stack, VoidContents.quantity(stack) + 1);
                    }
                } else {
                    int quantity = VoidContents.quantity(stack);
                    while (quantity > 0 && VoidContents.addOne(inv, inv.getContainerSize(), item)) {
                        quantity--;
                    }
                    XRFx.orb(player);
                    VoidContents.setQuantity(stack, quantity);
                }
            }
        }
        return InteractionResult.PASS;
    }
}
