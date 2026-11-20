package projectzulu.block.gear;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import projectzulu.ZuluGear;

/** TileEntityMobHeads: the kind of head (metadata of the item, {@link MobHead}) and its rotation on the floor (0-15). */
public class MobHeadBlockEntity extends BlockEntity {
    private int skullType;
    private int rotation;
    private String extraType = "";

    public MobHeadBlockEntity(BlockPos pos, BlockState state) {
        super(ZuluGear.MOB_HEAD_ENTITY.get(), pos, state);
    }

    public MobHead getHead() {
        return MobHead.byMeta(this.skullType);
    }

    public int getRotation() {
        return this.rotation;
    }

    public void setHead(MobHead head, int rotation) {
        this.skullType = head.meta();
        this.rotation = rotation;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putByte("SkullType", (byte) this.skullType);
        output.putByte("Rot", (byte) this.rotation);
        output.putString("ExtraType", this.extraType);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.skullType = input.getByteOr("SkullType", (byte) 0);
        this.rotation = Math.floorMod(input.getByteOr("Rot", (byte) 0), 16);
        this.extraType = input.getStringOr("ExtraType", "");
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
