package betterstorage.item;

import betterstorage.BS;
import betterstorage.menu.KeyringMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

/**
 * ItemKeyring: holds up to nine keys (sneak + right click opens it) and unlocks every lock one of them unlocks,
 * without using their lockpicking or morphing. The icon shows how many keys are on it (0..3+, the damage value of the
 * original, {@link BS#KEYRING_KEYS}).
 */
public class KeyringItem extends Item implements KeyLike {
    public KeyringItem(Properties properties) {
        super(properties);
    }

    /** SlotKey: plain keys only. */
    public static boolean fitsOnKeyring(ItemStack stack) {
        return stack.getItem() instanceof KeyLike key && key.isNormalKey();
    }

    public static NonNullList<ItemStack> contents(ItemStack keyring) {
        NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
        ItemContainerContents contents = keyring.get(DataComponents.CONTAINER);
        if (contents != null) {
            contents.copyInto(items);
        }
        return items;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer sp) {
            ItemStack stack = player.getMainHandItem();
            Component name = stack.get(DataComponents.CUSTOM_NAME);
            Component title = name != null ? name : Component.translatable("container.betterstorage.keyring");
            int slot = sp.getInventory().getSelectedSlot();
            KeyringMenu.open(sp, title, new KeyringInventory(sp, slot, stack), slot);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isNormalKey() {
        return false;
    }

    @Override
    public boolean unlock(ItemStack keyring, ItemStack lock, boolean useAbility) {
        for (ItemStack key : contents(keyring)) {
            if (!key.isEmpty() && key.getItem() instanceof KeyLike type && type.unlock(key, lock, false)) {
                return true;
            }
        }
        return false;
    }

    /** InventoryKeyring: the keys of the keyring in the player's slot; every change is written back into that slot. */
    static final class KeyringInventory extends SimpleContainer {
        private final Player player;
        private final int slot;
        private final ItemStack stack;

        KeyringInventory(Player player, int slot, ItemStack stack) {
            super(9);
            this.player = player;
            this.slot = slot;
            this.stack = stack.copy();
            NonNullList<ItemStack> items = contents(stack);
            for (int i = 0; i < 9; i++) {
                super.setItem(i, items.get(i));
            }
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            this.updateStack();
        }

        @Override
        public void stopOpen(ContainerUser user) {
            super.stopOpen(user);
            this.updateStack();
        }

        /** updateStack: the number of keys as the damage, the keys as contents, back into the player's slot. */
        private void updateStack() {
            List<ItemStack> items = new ArrayList<>();
            int count = 0;
            for (int i = 0; i < 9; i++) {
                ItemStack key = this.getItem(i);
                items.add(key);
                if (!key.isEmpty()) {
                    count++;
                }
            }
            this.stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
            this.stack.set(BS.KEYRING_KEYS.get(), count);
            this.player.getInventory().setItem(this.slot, this.stack.copy());
        }
    }
}
