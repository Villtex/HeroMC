package harkenscythe.block;

import harkenscythe.HSBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** TileEntityHSSkull: the kind of skull (0-15, see {@link harkenscythe.item.SkullType}) and its rotation on the floor (0-15). */
public class HSSkullBlockEntity extends BlockEntity {
    private int skullType;
    private int rotation;

    public HSSkullBlockEntity(BlockPos pos, BlockState state) {
        super(HSBlocks.SKULL_ENTITY.get(), pos, state);
    }

    public int getSkullType() {
        return this.skullType;
    }

    public int getRotation() {
        return this.rotation;
    }

    public void setSkull(int type, int rotation) {
        this.skullType = type;
        this.rotation = rotation;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putByte("SkullType", (byte) this.skullType);
        output.putByte("Rot", (byte) this.rotation);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.skullType = Math.floorMod(input.getByteOr("SkullType", (byte) 0), 16);
        this.rotation = Math.floorMod(input.getByteOr("Rot", (byte) 0), 16);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
