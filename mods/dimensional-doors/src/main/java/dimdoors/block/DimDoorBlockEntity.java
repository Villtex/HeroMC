package dimdoors.block;

import dimdoors.DimDoors;
import dimdoors.world.DDWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileEntityDimDoor: whether a link starts in this half ({@code openOrClosed}), which the client needs for the portal
 * RenderDimDoor draws in a linked door.
 */
public class DimDoorBlockEntity extends BlockEntity {
    private boolean linked;

    public DimDoorBlockEntity(BlockPos pos, BlockState state) {
        super(DimDoors.DOOR_ENTITY.get(), pos, state);
    }

    public boolean isLinked() {
        return this.linked;
    }

    /**
     * A door of world generation gets its link from the server thread shortly after its chunk was generated, before
     * the chunk is part of the world (DDWorld.createLink cannot reach it then): so the door asks itself when it loads.
     * The clients learn it with the chunk.
     */
    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level instanceof ServerLevel server) {
            boolean now = DDWorld.link(server, this.worldPosition) != null;
            if (now != this.linked) {
                this.linked = now;
                this.setChanged();
            }
        }
    }

    /** Asks the save data again whether a link starts here and tells the clients. */
    public void update() {
        if (this.level instanceof ServerLevel server && !this.isRemoved() && server.isLoaded(this.worldPosition)) {
            boolean now = DDWorld.link(server, this.worldPosition) != null;
            if (now != this.linked) {
                this.linked = now;
                this.setChanged();
                server.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    public void setLevel(net.minecraft.world.level.Level level) {
        super.setLevel(level);
        if (level instanceof ServerLevel server && DDWorld.server() != null) {
            // queued, never run at once: setLevel is called while the chunk is being built, and setChanged on that
            // chunk from inside its construction waits for itself (server watchdog)
            server.getServer().schedule(new net.minecraft.server.TickTask(server.getServer().getTickCount(), this::update));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("openOrClosed", this.linked);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.linked = input.getBooleanOr("openOrClosed", false);
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
