package betterdungeons.block;

import betterdungeons.BetterDungeons;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * BlockCapsuleTileEntity: the area a capsule packs from this station (width x height x width) and the name the full
 * capsule gets. Sent to the client for the outline of the area.
 */
public class CapsuleStationBlockEntity extends BlockEntity {
    public int width = 15;
    public int height = 9;
    public String name = "Capsule";

    public CapsuleStationBlockEntity(BlockPos pos, BlockState state) {
        super(BetterDungeons.CAPSULE_STATION_ENTITY.get(), pos, state);
    }

    /** setCapsuleSize(metadata): 1 storage 3 x 1, 2 small 7 x 5, 3 medium 11 x 5, 4 big 15 x 9; 0 (editable) keeps 15 x 9. */
    public void setCapsuleSize(int kind) {
        switch (kind) {
            case 1 -> this.set(3, 1, "Storage capsule");
            case 2 -> this.set(7, 5, "Small capsule");
            case 3 -> this.set(11, 5, "Medium capsule");
            case 4 -> this.set(15, 9, "Big capsule");
            default -> {
            }
        }
    }

    public void set(int width, int height, String name) {
        this.width = width;
        this.height = height;
        this.name = name;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("width", this.width);
        output.putInt("height", this.height);
        output.putString("name", this.name);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.width = input.getIntOr("width", 15);
        this.height = input.getIntOr("height", 9);
        this.name = input.getStringOr("name", "Capsule");
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
