package betterstorage.menu;

import betterstorage.BS;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * ContainerBetterStorage: {@link #columns} x {@link #rows} slots of the container from (8, 18), the player's
 * inventory {@link #SEPARATION} pixels below and centred under it. The server opens it with the size in the open
 * packet (the original sent its own "openGui" packet with name, columns, rows and title); the client builds it on an
 * empty inventory of that size. Opening counts as openChest, closing as closeChest.
 */
public class BSMenu extends AbstractContainerMenu {
    public static final int SEPARATION = 14;
    public final Container container;
    public final int columns;
    public final int rows;
    protected int startHotbar = -1;

    protected BSMenu(@Nullable MenuType<?> type, int id, Container container, int columns, int rows) {
        super(type, id);
        this.container = container;
        this.columns = columns;
        this.rows = rows;
    }

    public BSMenu(int id, Inventory inventory, Container container, int columns, int rows) {
        this(BS.MENU.get(), id, container, columns, rows);
        this.setup(inventory);
    }

    /** The constructor of the original: container slots, player slots, openChest. Subclasses call it last. */
    protected final void setup(Inventory inventory) {
        this.setupInventoryContainer();
        this.setupInventoryPlayer(inventory);
        this.container.startOpen(inventory.player);
    }

    public static BSMenu client(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        int columns = buf.readVarInt();
        int rows = buf.readVarInt();
        return new BSMenu(id, inventory, new SimpleContainer(columns * rows), columns, rows);
    }

    /** PlayerUtils.openGui. */
    public static void open(ServerPlayer player, Component title, int columns, int rows, Container container) {
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new BSMenu(id, inv, container, columns, rows), title), buf -> {
            buf.writeVarInt(columns);
            buf.writeVarInt(rows);
        });
    }

    protected void setupInventoryContainer() {
        for (int y = 0; y < this.rows; y++) {
            for (int x = 0; x < this.columns; x++) {
                this.addSlot(new Slot(this.container, x + y * this.columns, 8 + x * 18, 18 + y * 18));
            }
        }
    }

    protected void setupInventoryPlayer(Inventory inventory) {
        int offset = (this.columns - 9) * 9;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlot(this.playerSlot(inventory, 9 + x + y * 9, 8 + x * 18 + offset, this.rows * 18 + y * 18 + 18 + SEPARATION));
            }
        }
        this.startHotbar = this.slots.size();
        for (int x = 0; x < 9; x++) {
            this.addSlot(this.playerSlot(inventory, x, 8 + x * 18 + offset, this.rows * 18 + 76 + SEPARATION));
        }
    }

    protected Slot playerSlot(Inventory inventory, int index, int x, int y) {
        return new Slot(inventory, index, x, y);
    }

    public int containerSlots() {
        return this.columns * this.rows;
    }

    /** transferStackInSlot: container to the player (from the hotbar end), player into the container. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack inSlot = slot.getItem();
            result = inSlot.copy();
            int size = this.containerSlots();
            if (index < size) {
                if (!this.moveItemStackTo(inSlot, size, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(inSlot, 0, size, false)) {
                return ItemStack.EMPTY;
            }
            if (inSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}
