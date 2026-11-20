package legendgear.block;

import legendgear.LGBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** TileEntityJar: one stack ("Item"); the pedestal extends it with the sword it holds. */
public class JarBlockEntity extends BlockEntity {
    public ItemStack contents = ItemStack.EMPTY;

    public JarBlockEntity(BlockPos pos, BlockState state) {
        this(LGBlocks.JAR_ENTITY.get(), pos, state);
    }

    protected JarBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setContents(ItemStack stack) {
        this.contents = stack;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.contents.isEmpty()) {
            output.store("Item", ItemStack.CODEC, this.contents);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.contents = input.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    /** BlockJar.breakBlock / BlockSwordPedestal.breakBlock: the contents come out (pickup delay 10). */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (this.level != null && !this.level.isClientSide() && !this.contents.isEmpty()) {
            net.minecraft.world.entity.item.ItemEntity item = new net.minecraft.world.entity.item.ItemEntity(this.level,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, this.contents);
            item.setPickUpDelay(10);
            this.level.addFreshEntity(item);
            this.contents = ItemStack.EMPTY;
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
