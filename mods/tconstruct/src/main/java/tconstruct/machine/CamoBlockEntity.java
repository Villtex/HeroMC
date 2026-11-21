package tconstruct.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.model.data.ModelData;
import org.jspecify.annotations.Nullable;
import tconstruct.TDeco;
import tconstruct.blocks.TableBlockEntity;

/**
 * A block entity with an inventory, one slot of which holds a camouflage block (landmine, drawbridge): the block draws
 * that block's model ({@link TDeco#CAMO}, client/CamoModel) and gives its light (state property {@link TDeco#LIGHT}).
 */
public abstract class CamoBlockEntity extends TableBlockEntity {
    protected CamoBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int size) {
        super(type, pos, state, size);
    }

    /** The slot of the camouflage. */
    protected abstract int camoSlot();

    public @Nullable BlockState camo() {
        ItemStack stack = this.items.get(this.camoSlot());
        return stack.getItem() instanceof BlockItem block ? block.getBlock().defaultBlockState() : null;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            BlockState camo = this.camo();
            int light = camo == null ? 0 : camo.getLightEmission();
            BlockState state = this.getBlockState();
            if (state.hasProperty(TDeco.LIGHT) && state.getValue(TDeco.LIGHT) != light) {
                this.level.setBlock(this.worldPosition, state.setValue(TDeco.LIGHT, light), 3);
            }
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    @Override
    public ModelData getModelData() {
        BlockState camo = this.camo();
        return camo == null ? ModelData.EMPTY : ModelData.of(TDeco.CAMO, camo);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        if (this.level != null && this.level.isClientSide()) {
            this.requestModelDataUpdate();
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 8);
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
