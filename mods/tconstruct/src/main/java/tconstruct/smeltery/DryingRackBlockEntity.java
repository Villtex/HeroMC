package tconstruct.smeltery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import tconstruct.TSmeltery;
import tconstruct.blocks.TableBlockEntity;

/** DryingRackLogic: one item; after its drying time (rotten flesh: 6000 ticks) it turns into the result. */
public class DryingRackBlockEntity extends TableBlockEntity {
    private int currentTime;
    private int maxTime;

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(TSmeltery.DRYING_RACK_ENTITY.get(), pos, state, 1);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity rack) {
        if (rack.maxTime > 0 && rack.currentTime < rack.maxTime) {
            rack.currentTime++;
            if (rack.currentTime >= rack.maxTime) {
                SmelteryRecipes.Drying recipe = SmelteryRecipes.drying(rack.items.get(0));
                rack.items.set(0, recipe == null ? ItemStack.EMPTY : recipe.output().get().copy());
                rack.updateDryingTime();
            }
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        this.updateDryingTime();
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack stack = super.removeItem(slot, count);
        this.maxTime = 0;
        this.currentTime = 0;
        this.sync();
        return stack;
    }

    private void updateDryingTime() {
        this.currentTime = 0;
        SmelteryRecipes.Drying recipe = SmelteryRecipes.drying(this.items.get(0));
        this.maxTime = this.items.get(0).isEmpty() || recipe == null ? 0 : recipe.time();
        this.sync();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return this.items.get(0).isEmpty();
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Time", this.currentTime);
        output.putInt("MaxTime", this.maxTime);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.currentTime = input.getIntOr("Time", 0);
        this.maxTime = input.getIntOr("MaxTime", 0);
        if (this.maxTime == 0 && !this.items.get(0).isEmpty()) {
            SmelteryRecipes.Drying recipe = SmelteryRecipes.drying(this.items.get(0));
            this.maxTime = recipe == null ? 0 : recipe.time();
        }
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}
