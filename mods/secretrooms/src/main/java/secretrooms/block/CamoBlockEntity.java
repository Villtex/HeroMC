package secretrooms.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;
import org.jspecify.annotations.Nullable;
import secretrooms.SRBlocks;

/**
 * TileEntityCamo: the block state a camo block copies ("copyID"/"copyMeta" of the BlockHolder). Chosen on the server
 * the first tick after the block was placed ({@link CamoSource#chooseCamo}), sent to the client, which draws the
 * copied block's model (ModelData {@link #CAMO}). The copied block entity data of 1.5.2 (copyTE) is not kept.
 */
public class CamoBlockEntity extends BlockEntity {
    public static final ModelProperty<BlockState> CAMO = new ModelProperty<>();
    private @Nullable BlockState camo;

    public CamoBlockEntity(BlockPos pos, BlockState state) {
        this(SRBlocks.CAMO_ENTITY.get(), pos, state);
    }

    public CamoBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public @Nullable BlockState camo() {
        return this.camo;
    }

    public void setCamo(@Nullable BlockState camo) {
        this.camo = camo;
        this.setChanged();
        this.requestModelDataUpdate();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && !this.level.isClientSide() && this.camo == null && this.getBlockState().getBlock() instanceof CamoSource source) {
            BlockState chosen = source.chooseCamo(this.level, this.worldPosition, this.getBlockState());
            this.setCamo(chosen == null ? Blocks.STONE.defaultBlockState() : chosen);
        }
    }

    @Override
    public ModelData getModelData() {
        return this.camo == null ? ModelData.EMPTY : ModelData.of(CAMO, this.camo);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.camo != null) {
            output.store("copy", BlockState.CODEC, this.camo);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        BlockState old = this.camo;
        this.camo = input.read("copy", BlockState.CODEC).orElse(null);
        if (this.level != null && this.level.isClientSide() && old != this.camo) {
            this.requestModelDataUpdate();
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 8);
        }
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
