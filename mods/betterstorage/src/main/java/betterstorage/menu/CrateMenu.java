package betterstorage.menu;

import betterstorage.BS;
import betterstorage.crate.CratePlayerView;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * ContainerCrate: the player's view of a pile (9 columns, two rows per crate up to six) and how full the pile is
 * (0..255, progress bar 0 of the original). Shift-click into the crate puts the stack into the pile directly.
 */
public class CrateMenu extends BSMenu {
    private final @Nullable CratePlayerView view;
    private final DataSlot fullness;

    private CrateMenu(int id, Inventory inventory, net.minecraft.world.Container container, int rows, @Nullable CratePlayerView view) {
        super(BS.CRATE_MENU.get(), id, container, 9, rows);
        this.view = view;
        this.fullness = this.addDataSlot(new DataSlot() {
            private int value;

            @Override
            public int get() {
                if (CrateMenu.this.view != null && CrateMenu.this.view.data.getNumCrates() > 0) {
                    this.value = CrateMenu.this.view.data.getOccupiedSlots() * 255 / CrateMenu.this.view.data.getCapacity();
                }
                return this.value;
            }

            @Override
            public void set(int value) {
                this.value = value;
            }
        });
        this.setup(inventory);
    }

    public static CrateMenu client(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        int rows = buf.readVarInt();
        return new CrateMenu(id, inventory, new SimpleContainer(9 * rows), rows, null);
    }

    public static void open(ServerPlayer player, Component title, CratePlayerView view) {
        int rows = view.getContainerSize() / 9;
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new CrateMenu(id, inv, view, rows, view), title), buf -> buf.writeVarInt(rows));
    }

    /** 0..255. */
    public int getFullness() {
        return this.fullness.get();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem() || this.view == null) {
            return ItemStack.EMPTY;
        }
        ItemStack slotStack = slot.getItem();
        int size = this.containerSlots();
        if (index < size) {
            // into the player inventory, then take that many out of the pile
            int count = slotStack.getCount();
            this.moveItemStackTo(slotStack, size, this.slots.size(), true);
            int amount = count - slotStack.getCount();
            this.view.removeItem(index, amount);
            return ItemStack.EMPTY;
        }
        ItemStack overflow = this.view.data.addItems(slotStack.copy());
        slot.setByPlayer(overflow);
        return ItemStack.EMPTY;
    }
}
