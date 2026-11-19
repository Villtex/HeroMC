package bibliocraft.common;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

/**
 * Base of the containers of the original (GuiHandler + Container): the block's inventory first, then the player
 * inventory (bindPlayerInventory). The client builds the menu from the block position (written by {@link #open}).
 */
public abstract class BiblioMenu extends AbstractContainerMenu {
    protected final Container container;
    protected final @Nullable BlockEntity blockEntity;

    protected BiblioMenu(@Nullable MenuType<?> type, int id, Container container) {
        super(type, id);
        this.container = container;
        this.blockEntity = container instanceof BlockEntity be ? be : null;
    }

    /** Opens the menu: the factory builds the server side; the client side comes from the menu type's buffer factory. */
    public static void open(ServerPlayer player, BlockEntity be, Component title, MenuConstructor factory) {
        BlockPos pos = be.getBlockPos();
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> factory.create(id, inv, (Container) be), title), buf -> buf.writeBlockPos(pos));
    }

    @FunctionalInterface
    public interface MenuConstructor {
        AbstractContainerMenu create(int id, Inventory inventory, Container container);
    }

    /** The block's container on the client side of the menu: the block entity at the sent position, or a stand-in. */
    public static Container clientContainer(Inventory inventory, RegistryFriendlyByteBuf buf, int size) {
        BlockEntity be = inventory.player.level().getBlockEntity(buf.readBlockPos());
        return be instanceof Container c && c.getContainerSize() == size ? c : new SimpleContainer(size);
    }

    /** bindPlayerInventory: three rows at (x, y), the hotbar 58 below. */
    protected void addPlayerInventory(Inventory inventory, int x, int y) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, x + j * 18, y + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(inventory, i, x + i * 18, y + 58));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    /**
     * The transferStackInSlot of the original, with the block's slots 0..blockSlots-1: from the block into the player
     * inventory (from the end), from the player into the block if {@code accepts}. Returns EMPTY when nothing moved.
     */
    protected ItemStack quickMove(Player player, int index, int blockSlots, Predicate<ItemStack> accepts) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack inSlot = slot.getItem();
            result = inSlot.copy();
            if (index < blockSlots) {
                if (!this.moveItemStackTo(inSlot, blockSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!accepts.test(inSlot) || !this.moveItemStackTo(inSlot, 0, blockSlots, false)) {
                return ItemStack.EMPTY;
            }
            if (inSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (inSlot.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, inSlot);
        }
        return result;
    }
}
