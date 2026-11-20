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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CastingChannelLogic: holds up to 288 mB. Once a second (unless just filled) it passes 24 mB down, or else up to
 * 48 mB spread over its side neighbours, never back to where the liquid came from. Right-click makes it pull liquid out
 * of neighbouring tanks (3 mB at a time per tick) until none is left. Channels cannot be drained from outside.
 */
public class ChannelBlockEntity extends BlockEntity implements LiquidContainer {
    public static final int FILL_MAX = 288;
    public static final int OUTPUT_MAX = 48;
    public FluidStack liquid = FluidStack.EMPTY;
    private @Nullable Direction lastProvider;
    private int ticks;
    private int ticksLPReset;
    private int recentlyFilledDelay;
    private boolean pullingLiquids;

    public ChannelBlockEntity(BlockPos pos, BlockState state) {
        super(TSmeltery.CHANNEL_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ChannelBlockEntity channel) {
        channel.ticks++;
        channel.ticksLPReset++;
        if (channel.pullingLiquids) {
            channel.pullLiquids();
        }
        if (channel.ticks == 20) {
            if (channel.recentlyFilledDelay != 0) {
                channel.recentlyFilledDelay--;
            }
            if (channel.recentlyFilledDelay == 0) {
                channel.moveLiquidToTanks();
            }
            channel.ticks = 0;
        }
        if (channel.ticksLPReset == 60) {
            channel.lastProvider = null;
            channel.ticksLPReset = 0;
        }
    }

    private void pullLiquids() {
        if (!this.pullLiquid(Direction.EAST) && !this.pullLiquid(Direction.WEST) && !this.pullLiquid(Direction.SOUTH)
                && !this.pullLiquid(Direction.NORTH)) {
            this.pullingLiquids = false;
        }
    }

    /** pullLiquid: 1.5.2 passed the direction of the neighbour as the side (EAST for x+1, NORTH for z+1). */
    private boolean pullLiquid(Direction direction) {
        BlockEntity tank = this.level.getBlockEntity(this.worldPosition.relative(direction));
        Direction side = direction.getAxis() == Direction.Axis.Z ? direction.getOpposite() : direction;
        if (tank instanceof LiquidContainer container && !(tank instanceof ChannelBlockEntity)) {
            FluidStack temp = container.drain(side, 3, false);
            if (!temp.isEmpty()) {
                if (this.liquid.isEmpty()) {
                    this.liquid = container.drain(side, Math.min(temp.getAmount(), FILL_MAX), true);
                    this.sync();
                    return true;
                }
                if (FluidStack.isSameFluidSameComponents(this.liquid, temp)) {
                    int amount = Math.min(temp.getAmount(), FILL_MAX - this.liquid.getAmount());
                    this.liquid.grow(container.drain(side, amount, true).getAmount());
                    this.sync();
                    return true;
                }
            }
        }
        return false;
    }

    private void moveLiquidToTanks() {
        if (this.liquid.isEmpty()) {
            return;
        }
        Map<Direction, BlockEntity> tankMap = this.getOutputs();
        if (tankMap.isEmpty()) {
            return;
        }
        boolean yFilled = false;
        if (tankMap.get(Direction.DOWN) instanceof LiquidContainer below) {
            FluidStack push = this.drainInternal(24, false);
            if (!push.isEmpty()) {
                int filled = below.fill(Direction.UP, push, true);
                if (filled > 0) {
                    yFilled = true;
                    this.drainInternal(filled, true);
                }
            }
        }
        if (yFilled || this.liquid.isEmpty()) {
            return;
        }
        int size = tankMap.size();
        int divisionSurplus = this.liquid.getAmount() % size;
        int amountXp = OUTPUT_MAX / size + (divisionSurplus > 0 ? 1 : 0);
        int amountXm = OUTPUT_MAX / size + (divisionSurplus > 1 ? 1 : 0);
        int amountZp = OUTPUT_MAX / size + (divisionSurplus > 2 ? 1 : 0);
        int amountZm = OUTPUT_MAX / size;
        this.push(tankMap, Direction.EAST, amountXp, Direction.WEST);
        this.push(tankMap, Direction.WEST, amountXm, Direction.EAST);
        this.push(tankMap, Direction.SOUTH, amountZp, Direction.NORTH);
        this.push(tankMap, Direction.NORTH, amountZm, Direction.SOUTH);
    }

    private void push(Map<Direction, BlockEntity> tankMap, Direction to, int amount, Direction from) {
        if (tankMap.get(to) instanceof LiquidContainer tank) {
            FluidStack push = this.drainInternal(amount, false);
            if (!push.isEmpty()) {
                this.drainInternal(tank.fill(from, push, true), true);
            }
        }
    }

    public boolean hasChannelConnected(Direction dir) {
        return this.level.getBlockEntity(this.worldPosition.relative(dir)) instanceof ChannelBlockEntity;
    }

    public boolean hasTankConnected(Direction dir) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition.relative(dir)) instanceof LiquidContainer;
    }

    private Map<Direction, BlockEntity> getOutputs() {
        Map<Direction, BlockEntity> map = new LinkedHashMap<>();
        for (Direction dir : new Direction[]{Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH, Direction.DOWN}) {
            boolean connected = this.pullingLiquids ? this.hasChannelConnected(dir) : this.hasTankConnected(dir);
            if (connected && this.lastProvider != dir) {
                map.put(dir, this.level.getBlockEntity(this.worldPosition.relative(dir)));
            }
        }
        return map;
    }

    /** tankBelow: how far the stream falls into what is below (renderer). */
    public float tankBelow() {
        BlockEntity te = this.level.getBlockEntity(this.worldPosition.below());
        if (te instanceof ChannelBlockEntity) {
            return -0.5F;
        }
        if (te instanceof TankBlockEntity) {
            return -1.0F;
        }
        if (te instanceof CastingBlockEntity cast) {
            return cast.basin ? -0.75F : -0.125F;
        }
        return te instanceof LiquidContainer ? 0.0F : 0.5F;
    }

    @Override
    public int fill(@Nullable Direction from, FluidStack resource, boolean doFill) {
        this.lastProvider = from;
        this.ticksLPReset = 0;
        int filled = this.fillInternal(resource, doFill);
        if (from == Direction.UP) {
            this.moveLiquidToTanks();
        }
        return filled;
    }

    private int fillInternal(FluidStack stack, boolean doFill) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (this.liquid.isEmpty()) {
            int amount = Math.min(stack.getAmount(), FILL_MAX);
            if (doFill) {
                this.liquid = stack.copyWithAmount(amount);
                this.sync();
            }
            this.recentlyFilledDelay = this.lastProvider == Direction.UP ? 3 : 2;
            return amount;
        }
        if (!FluidStack.isSameFluidSameComponents(stack, this.liquid)) {
            return 0;
        }
        if (stack.getAmount() + this.liquid.getAmount() >= FILL_MAX) {
            int space = FILL_MAX - this.liquid.getAmount();
            if (doFill && space > 0) {
                this.liquid.setAmount(FILL_MAX);
                this.sync();
            }
            return space;
        }
        if (doFill) {
            this.liquid.grow(stack.getAmount());
            this.sync();
        }
        return stack.getAmount();
    }

    private FluidStack drainInternal(int maxDrain, boolean doDrain) {
        if (this.liquid.isEmpty() || maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        int toDrain = Math.min(maxDrain, this.liquid.getAmount());
        FluidStack drained = this.liquid.copyWithAmount(toDrain);
        if (doDrain) {
            this.liquid.shrink(toDrain);
            if (this.liquid.isEmpty()) {
                this.liquid = FluidStack.EMPTY;
            }
            this.sync();
        }
        return drained;
    }

    /** drain from outside: 1.5.2 drained and returned null (lost the liquid); the port keeps it. */
    @Override
    public FluidStack drain(@Nullable Direction from, int maxDrain, boolean doDrain) {
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack liquid() {
        return this.liquid;
    }

    @Override
    public int capacity() {
        return FILL_MAX;
    }

    public void setActive() {
        this.pullingLiquids = true;
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
        if (!this.liquid.isEmpty()) {
            output.store("Liquid", FluidStack.CODEC, this.liquid);
        }
        output.putInt("LastProvider", this.lastProvider == null ? -1 : this.lastProvider.get3DDataValue());
        output.putInt("recentlyFilledDelay", this.recentlyFilledDelay);
        output.putBoolean("PullingLiquids", this.pullingLiquids);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.liquid = input.read("Liquid", FluidStack.CODEC).orElse(FluidStack.EMPTY);
        int last = input.getIntOr("LastProvider", -1);
        this.lastProvider = last < 0 ? null : Direction.from3DDataValue(last);
        this.recentlyFilledDelay = input.getIntOr("recentlyFilledDelay", 0);
        this.pullingLiquids = input.getBooleanOr("PullingLiquids", false);
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
