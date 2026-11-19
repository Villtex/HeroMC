package betterstorage.block;

import betterstorage.BS;
import betterstorage.BSUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * TileEntityArmorStand: four armor pieces (slot 0 boots .. 3 helmet, like the player's armorInventory) and the
 * rotation in sixteenths of a turn. Only the lower half has one.
 */
public class ArmorStandBlockEntity extends BlockEntity {
    public final NonNullList<ItemStack> armor = NonNullList.withSize(4, ItemStack.EMPTY);
    public int rotation;

    public ArmorStandBlockEntity(BlockPos pos, BlockState state) {
        super(BS.ARMOR_STAND_ENTITY.get(), pos, state);
    }

    public void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void dropContents() {
        if (this.level != null) {
            for (ItemStack stack : this.armor) {
                BSUtil.dropFromBlock(this.level, this.worldPosition, stack);
            }
            this.armor.replaceAll(s -> ItemStack.EMPTY);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        this.dropContents();
    }

    /** getRenderBoundingBox: both halves. */
    public AABB renderBox() {
        return new AABB(this.worldPosition).expandTowards(0.0, 1.0, 0.0);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.rotation = input.getByteOr("rotation", (byte) 0);
        this.armor.replaceAll(s -> ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.armor);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putByte("rotation", (byte) this.rotation);
        ContainerHelper.saveAllItems(output, this.armor, true);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
