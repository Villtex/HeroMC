package betterstorage.menu;

import betterstorage.BS;
import betterstorage.item.KeyItem;
import betterstorage.item.KeyringItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * ContainerKeyring: nine slots for keys (SlotKey) above the player inventory, in which the slot of the keyring itself
 * can be neither taken from nor put into (SlotProtected). Shift-click only moves keys.
 */
public class KeyringMenu extends BSMenu {
    private final int protectedIndex;

    private KeyringMenu(int id, Inventory inventory, Container container, int protectedIndex) {
        super(BS.KEYRING_MENU.get(), id, container, 9, 1);
        this.protectedIndex = protectedIndex;
        this.setup(inventory);
    }

    public static KeyringMenu client(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new KeyringMenu(id, inventory, new SimpleContainer(9), buf.readVarInt());
    }

    /** ItemKeyring.onItemRightClick: the held keyring's keys. */
    public static void open(ServerPlayer player, Component title, Container keys, int slot) {
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new KeyringMenu(id, inv, keys, slot), title), buf -> buf.writeVarInt(slot));
    }

    @Override
    protected void setupInventoryContainer() {
        for (int x = 0; x < 9; x++) {
            this.addSlot(new Slot(this.container, x, 8 + x * 18, 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return KeyringItem.fitsOnKeyring(stack);
                }
            });
        }
    }

    @Override
    protected Slot playerSlot(Inventory inventory, int index, int x, int y) {
        if (index != this.protectedIndex) {
            return super.playerSlot(inventory, index, x, y);
        }
        return new Slot(inventory, index, x, y) {
            @Override
            public boolean mayPickup(Player player) {
                return false;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        };
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot.hasItem() && !(slot.getItem().getItem() instanceof KeyItem)) {
            return ItemStack.EMPTY;
        }
        return super.quickMoveStack(player, index);
    }

    /** The keyring must stay where it is: no number key swap with its hotbar slot. */
    @Override
    public void clicked(int slotIndex, int button, ContainerInput input, Player player) {
        if (input == ContainerInput.SWAP && button == this.protectedIndex) {
            return;
        }
        super.clicked(slotIndex, button, input, player);
    }
}
