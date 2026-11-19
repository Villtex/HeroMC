package bibliocraft.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Base of the BiblioCraft tile entities with an inventory (IInventory). The whole inventory goes to the client with
 * every change (getDescriptionPacket/onDataPacket of the original), since the renderers draw it. Subclasses save
 * their own fields in saveAdditional/loadAdditional (call super) and react to changes in {@link #contentsChanged()}.
 */
public abstract class BiblioBlockEntity extends BlockEntity implements Container {
    protected final NonNullList<ItemStack> items;

    protected BiblioBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int size) {
        super(type, pos, state);
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    /** Called after every change of a slot, on both sides (the client gets it from the update packet too). */
    protected void contentsChanged() {
    }

    /** setChanged + markBlockForUpdate: saves and sends the block entity to the clients. */
    public void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items, true);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < this.items.size(); i++) {
            this.items.set(i, ItemStack.EMPTY);
        }
        ContainerHelper.loadAllItems(input, this.items);
        this.contentsChanged();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack stack = ContainerHelper.removeItem(this.items, slot, count);
        if (!stack.isEmpty()) {
            this.changed();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = ContainerHelper.takeItem(this.items, slot);
        this.changed();
        return stack;
    }

    /** setInventorySlotContents: clamps to the stack limit, then updates and syncs. */
    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        this.changed();
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    /** A slot changed: {@link #contentsChanged()}, then {@link #sync()}. */
    protected void changed() {
        this.contentsChanged();
        this.sync();
    }

    /** isUseableByPlayer: this block entity is still there and the player within 8 blocks. */
    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < this.items.size(); i++) {
            this.items.set(i, ItemStack.EMPTY);
        }
        this.changed();
    }
}
