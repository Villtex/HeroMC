package betterdungeons.item;

import betterdungeons.BDItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

import java.util.function.Predicate;

/**
 * ItemBag and ItemQuiver with InventoryBag / InventoryQuiver and ContainerBDChest: one row of nine slots kept in the
 * item (DataComponents.CONTAINER instead of the "Items" NBT list). The screen is the vanilla one-row chest. On closing,
 * a bag drops the bags put into it and a quiver everything that is no arrow (getStackInSlotOnClosing). Only the
 * 9-slot bag (damage 0) could be made in 1.5.2; bigger bags (damage * 9 + 9 slots) did not exist in the game.
 */
public class BagItem extends Item {
    public static final int SIZE = 9;
    private final boolean quiver;

    public BagItem(Properties properties, boolean quiver) {
        super(properties);
        this.quiver = quiver;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && hand == InteractionHand.MAIN_HAND) {
            int slot = player.getInventory().getSelectedSlot();
            Component title = Component.translatable(this.quiver ? "container.betterdungeons.quiver" : "container.betterdungeons.bag");
            player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new Menu(id, inventory, slot, this.quiver), title));
        }
        return InteractionResult.SUCCESS;
    }

    public static boolean isArrow(ItemStack stack) {
        return stack.is(Items.ARROW) || BDItems.isMagicArrow(stack);
    }

    /** The contents of a bag or quiver (nine slots). */
    public static NonNullList<ItemStack> contents(ItemStack bag) {
        NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        bag.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);
        return items;
    }

    public static void save(ItemStack bag, NonNullList<ItemStack> items) {
        bag.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
    }

    /** ContainerBDChest on the server; the client opens the vanilla one-row chest menu. */
    static final class Menu extends ChestMenu {
        private final Player player;
        private final int bagSlot;
        private final Predicate<ItemStack> dropOnClose;

        Menu(int id, Inventory inventory, int bagSlot, boolean quiver) {
            this(id, inventory, bagSlot, quiver, new Holder(inventory.player, bagSlot));
        }

        private Menu(int id, Inventory inventory, int bagSlot, boolean quiver, Holder holder) {
            super(MenuType.GENERIC_9x1, id, inventory, holder, 1);
            this.player = inventory.player;
            this.bagSlot = bagSlot;
            Item bagItem = inventory.getItem(bagSlot).getItem();
            this.dropOnClose = quiver ? s -> !s.isEmpty() && !isArrow(s) : s -> s.is(bagItem);
        }

        private ItemStack bag() {
            return this.player.getInventory().getItem(this.bagSlot);
        }

        @Override
        public boolean stillValid(Player player) {
            return this.bag().getItem() instanceof BagItem;
        }

        /** InventoryBag.isUseableByPlayer kept the open bag where it was: its own slot cannot be clicked. */
        @Override
        public void clicked(int slotId, int button, ContainerInput input, Player player) {
            if (slotId >= 0 && slotId < this.slots.size() && this.slots.get(slotId).container == player.getInventory()
                    && this.slots.get(slotId).getContainerSlot() == this.bagSlot) {
                return;
            }
            if (input == ContainerInput.SWAP && button == this.bagSlot) {
                return;
            }
            super.clicked(slotId, button, input, player);
        }

        @Override
        public void removed(Player player) {
            SimpleContainer container = (SimpleContainer) this.getContainer();
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (this.dropOnClose.test(stack)) {
                    container.setItem(i, ItemStack.EMPTY);
                    player.drop(stack, false, net.minecraft.util.Prediction.SERVER_ONLY);
                }
            }
            super.removed(player);
        }
    }

    /** The nine slots, written back into the bag in the player's inventory after every change. */
    static final class Holder extends SimpleContainer {
        private final Player player;
        private final int bagSlot;
        private boolean loading = true;

        Holder(Player player, int bagSlot) {
            super(SIZE);
            this.player = player;
            this.bagSlot = bagSlot;
            NonNullList<ItemStack> items = contents(player.getInventory().getItem(bagSlot));
            for (int i = 0; i < SIZE; i++) {
                this.setItem(i, items.get(i));
            }
            this.loading = false;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            if (this.loading) {
                return;
            }
            ItemStack bag = this.player.getInventory().getItem(this.bagSlot);
            if (bag.getItem() instanceof BagItem) {
                NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
                for (int i = 0; i < SIZE; i++) {
                    items.set(i, this.getItem(i).copy());
                }
                save(bag, items);
            }
        }
    }

    // ---------------------------------------------------------------- ItemQuiver statics (EventHandlerBD)

    /** ItemQuiver.haveArrows. */
    public static boolean haveArrows(ItemStack quiver) {
        return contents(quiver).stream().anyMatch(s -> !s.isEmpty());
    }

    /** ItemQuiver.decreaseArrow: one of the first non-empty stack, or empty. */
    public static ItemStack decreaseArrow(ItemStack quiver) {
        NonNullList<ItemStack> items = contents(quiver);
        for (ItemStack s : items) {
            if (!s.isEmpty()) {
                ItemStack result = s.copyWithCount(1);
                s.shrink(1);
                save(quiver, items);
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    /** ItemQuiver.addArrow: onto a stack of the same arrow with room for all of it, else into the first empty slot. */
    public static boolean addArrow(ItemStack quiver, ItemStack arrow) {
        NonNullList<ItemStack> items = contents(quiver);
        for (ItemStack s : items) {
            if (!s.isEmpty() && ItemStack.isSameItemSameComponents(s, arrow) && s.getCount() < s.getMaxStackSize() - arrow.getCount()) {
                s.grow(arrow.getCount());
                save(quiver, items);
                return true;
            }
        }
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isEmpty()) {
                items.set(i, arrow.copy());
                save(quiver, items);
                return true;
            }
        }
        return false;
    }
}
