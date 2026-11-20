package tconstruct.smeltery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;
import tconstruct.TSmeltery;

/** FaucetLogic: moves up to 144 mB from the block behind into the block below, then shows the stream for amount/6 ticks. */
public class FaucetBlockEntity extends BlockEntity {
    private boolean active;
    /** The liquid still running (drawn as the stream). */
    public FluidStack liquid = FluidStack.EMPTY;

    public FaucetBlockEntity(BlockPos pos, BlockState state) {
        super(TSmeltery.FAUCET_ENTITY.get(), pos, state);
    }

    public Direction facing() {
        return this.getBlockState().getValue(FaucetBlock.FACING);
    }

    private boolean activateFaucet() {
        if (!this.liquid.isEmpty() || !this.active || this.level == null) {
            return false;
        }
        Direction face = this.facing();
        BlockEntity drainEntity = this.level.getBlockEntity(this.worldPosition.relative(face.getOpposite()));
        BlockEntity tankEntity = this.level.getBlockEntity(this.worldPosition.below());
        if (drainEntity instanceof LiquidContainer drain && tankEntity instanceof LiquidContainer tank) {
            FluidStack temp = drain.drain(face, 144, false);
            if (!temp.isEmpty()) {
                int drained = tank.fill(Direction.UP, temp, false);
                if (drained > 0) {
                    this.liquid = drain.drain(face, drained, true);
                    tank.fill(Direction.UP, this.liquid, true);
                    this.sync();
                    return true;
                }
            }
        }
        return false;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FaucetBlockEntity faucet) {
        if (!faucet.liquid.isEmpty()) {
            faucet.liquid.shrink(6);
            if (faucet.liquid.isEmpty()) {
                faucet.liquid = FluidStack.EMPTY;
                if (!level.isClientSide() && !faucet.activateFaucet()) {
                    faucet.active = false;
                    faucet.sync();
                }
            }
        }
    }

    /** setActive: on (and pour) when off, off when on. */
    public void setActive() {
        if (!this.active) {
            this.active = true;
            this.activateFaucet();
        } else {
            this.active = false;
        }
        this.setChanged();
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
        output.putBoolean("Active", this.active);
        if (!this.liquid.isEmpty()) {
            output.store("Liquid", FluidStack.CODEC, this.liquid);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.active = input.getBooleanOr("Active", false);
        this.liquid = input.read("Liquid", FluidStack.CODEC).orElse(FluidStack.EMPTY);
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
