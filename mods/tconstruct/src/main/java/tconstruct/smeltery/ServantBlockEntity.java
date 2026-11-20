package tconstruct.smeltery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import tconstruct.TSmeltery;

/**
 * MultiServantLogic: a seared block that belongs to the smeltery whose controller sits at {@link #master}. The master is
 * valid while the same block still stands there (1.5.2 compared block id and metadata).
 */
public class ServantBlockEntity extends BlockEntity {
    protected @Nullable BlockPos master;
    protected Block masterBlock = Blocks.AIR;

    public ServantBlockEntity(BlockPos pos, BlockState state) {
        this(TSmeltery.SERVANT_ENTITY.get(), pos, state);
    }

    protected ServantBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean hasValidMaster() {
        if (this.master == null || this.level == null) {
            return false;
        }
        if (this.level.getBlockState(this.master).is(this.masterBlock)) {
            return true;
        }
        this.master = null;
        return false;
    }

    public @Nullable BlockPos getMasterPosition() {
        return this.master;
    }

    public void overrideMaster(BlockPos pos) {
        this.master = pos.immutable();
        this.masterBlock = this.level == null ? Blocks.AIR : this.level.getBlockState(pos).getBlock();
        this.setChanged();
    }

    public void removeMaster() {
        this.master = null;
        this.masterBlock = Blocks.AIR;
        this.setChanged();
    }

    public boolean verifyMaster(BlockPos pos) {
        return this.master != null && this.level != null && this.master.equals(pos) && this.level.getBlockState(pos).is(this.masterBlock);
    }

    public boolean setMaster(BlockPos pos) {
        if (this.master != null && this.level != null && this.level.getBlockState(this.master).is(this.masterBlock)) {
            return false;
        }
        this.overrideMaster(pos);
        return true;
    }

    public void notifyMasterOfChange() {
        if (this.hasValidMaster() && this.level.getBlockEntity(this.master) instanceof MasterLogic logic) {
            logic.notifyChange(this.worldPosition);
        }
    }

    /** The master's block entity, if valid. */
    public @Nullable BlockEntity masterEntity() {
        return this.hasValidMaster() ? this.level.getBlockEntity(this.master) : null;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.master != null) {
            output.store("Master", BlockPos.CODEC, this.master);
            output.store("MasterBlock", Identifier.CODEC, BuiltInRegistries.BLOCK.getKey(this.masterBlock));
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.master = input.read("Master", BlockPos.CODEC).orElse(null);
        this.masterBlock = input.read("MasterBlock", Identifier.CODEC).map(BuiltInRegistries.BLOCK::getValue).orElse(Blocks.AIR);
    }

    /** Sends the block entity to the client (getDescriptionPacket); subclasses with something to draw use it. */
    protected void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
