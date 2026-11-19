package qcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import qcraft.QuantumCraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TileEntityQuantumComputer: teleports the area inside a frame of four qBlocks (gold on the side facing the computer,
 * obsidian on the others, up to 8 blocks away in ±x and ±z, in the computer's layer), obsidian pillars above and below
 * them (up to 7) and glass on top of the pillars. Ice next to the computer cools it. Not entangled: the area is
 * swapped with the area stored in the computer (empty at first); entangled: swapped with the area of the twin computer,
 * whose frame must have the same shape. Areas with block entities (other than the computer) cannot be moved.
 */
public class QuantumComputerBlockEntity extends BlockEntity {
    public static final EntanglementRegistry<QuantumComputerBlockEntity> REGISTRY = new EntanglementRegistry<>();
    public static final EntanglementRegistry<QuantumComputerBlockEntity> CLIENT_REGISTRY = new EntanglementRegistry<>();
    public static final int RANGE = 8;

    /** TileEntityQuantumComputer.TeleportError. */
    public static final int OK = 0;
    public static final int NO_DESTINATION = 1;
    public static final int FRAME_INCOMPLETE = 2;
    public static final int DESTINATION_FRAME_INCOMPLETE = 3;
    public static final int FRAME_MISMATCH = 4;
    public static final int INSUFFICIENT_COOLING = 5;
    public static final int AREA_NOT_TRANSPORTABLE = 6;
    public static final int DESTINATION_NOT_TRANSPORTABLE = 7;
    public static final int FRAME_NOT_ACTIVATED = 8;

    private int entanglementFrequency = -1;
    private @Nullable AreaData storedData;
    private boolean registered;

    public QuantumComputerBlockEntity(BlockPos pos, BlockState state) {
        super(QuantumCraft.QUANTUM_COMPUTER_ENTITY.get(), pos, state);
    }

    /** TeleportError.decode as a translation key: null for OK. */
    public static @Nullable String errorKey(int error) {
        return switch (error) {
            case OK -> null;
            case NO_DESTINATION -> "gui.quantumcraft.no_destination";
            case FRAME_INCOMPLETE -> "gui.quantumcraft.frame_incomplete";
            case DESTINATION_FRAME_INCOMPLETE -> "gui.quantumcraft.destination_frame_incomplete";
            case FRAME_MISMATCH -> "gui.quantumcraft.frame_mismatch";
            case INSUFFICIENT_COOLING -> "gui.quantumcraft.insufficient_cooling";
            case AREA_NOT_TRANSPORTABLE -> "gui.quantumcraft.area_not_transportable";
            case DESTINATION_NOT_TRANSPORTABLE -> "gui.quantumcraft.destination_not_transportable";
            case FRAME_NOT_ACTIVATED -> "gui.quantumcraft.frame_not_activated";
            default -> "";
        };
    }

    private EntanglementRegistry<QuantumComputerBlockEntity> registry() {
        return this.level != null && this.level.isClientSide() ? CLIENT_REGISTRY : REGISTRY;
    }

    public static EntanglementRegistry<QuantumComputerBlockEntity> getEntanglementRegistry(Level level) {
        return !level.isClientSide() ? REGISTRY : CLIENT_REGISTRY;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.register();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (this.level != null) {
            this.register();
        }
    }

    private void register() {
        if (!this.registered && this.entanglementFrequency >= 0 && this.level != null) {
            this.registry().register(this.entanglementFrequency, this);
            this.registered = true;
        }
    }

    @Override
    public void setRemoved() {
        if (this.registered && this.level != null) {
            this.registry().unregister(this.entanglementFrequency, this);
            this.registered = false;
        }
        super.setRemoved();
    }

    public void setEntanglementFrequency(int frequency) {
        if (this.entanglementFrequency != frequency) {
            if (this.registered) {
                this.registry().unregister(this.entanglementFrequency, this);
                this.registered = false;
            }
            this.entanglementFrequency = frequency;
            this.register();
            this.setChanged();
        }
    }

    public int getEntanglementFrequency() {
        return this.entanglementFrequency;
    }

    public void setStoredData(@Nullable AreaData data) {
        this.storedData = data;
        this.setChanged();
    }

    public @Nullable AreaData getStoredData() {
        return this.storedData;
    }

    // ---- frame

    private boolean inHeight(int y) {
        return y >= this.level.getMinY() && y <= this.level.getMaxY();
    }

    private boolean isFrame(int x, int y, int z, int side) {
        if (!this.inHeight(y)) {
            return false;
        }
        if (this.level.getBlockEntity(new BlockPos(x, y, z)) instanceof QBlockEntity quantum) {
            int[] types = quantum.getTypes();
            for (int i = 0; i < 6; i++) {
                if (i == side) {
                    if (types[i] != Impostors.GOLD_BLOCK) {
                        return false;
                    }
                } else if (types[i] != Impostors.OBSIDIAN) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean is(int x, int y, int z, Block block) {
        return this.inHeight(y) && this.level.getBlockState(new BlockPos(x, y, z)).is(block);
    }

    public AreaData.@Nullable Shape calculateShape() {
        int x0 = this.worldPosition.getX();
        int y0 = this.worldPosition.getY();
        int z0 = this.worldPosition.getZ();
        int xMin = -99;
        int xMax = -99;
        int zMin = -99;
        int zMax = -99;
        for (int i = 0; i < RANGE; i++) {
            if (xMin == -99 && this.isFrame(x0 - i - 1, y0, z0, 5)) {
                xMin = -i;
            }
            if (xMax == -99 && this.isFrame(x0 + i + 1, y0, z0, 4)) {
                xMax = i;
            }
            if (zMin == -99 && this.isFrame(x0, y0, z0 - i - 1, 3)) {
                zMin = -i;
            }
            if (zMax == -99 && this.isFrame(x0, y0, z0 + i + 1, 2)) {
                zMax = i;
            }
        }
        if (xMin == -99 || xMax == -99 || zMin == -99 || zMax == -99) {
            return null;
        }
        int yMin = 0;
        for (int i = 1; i < RANGE && this.pillars(xMin, xMax, zMin, zMax, y0 - i); i++) {
            yMin = -i;
        }
        int yMax = 0;
        int i = 1;
        while (i < RANGE && this.pillars(xMin, xMax, zMin, zMax, y0 + i)) {
            yMax = i++;
        }
        int top = y0 + yMax + 1;
        if (this.is(x0 + xMin - 1, top, z0, Blocks.GLASS) && this.is(x0 + xMax + 1, top, z0, Blocks.GLASS)
                && this.is(x0, top, z0 + zMin - 1, Blocks.GLASS) && this.is(x0, top, z0 + zMax + 1, Blocks.GLASS)) {
            return new AreaData.Shape(xMin, xMax, yMin, yMax, zMin, zMax);
        }
        return null;
    }

    private boolean pillars(int xMin, int xMax, int zMin, int zMax, int y) {
        int x0 = this.worldPosition.getX();
        int z0 = this.worldPosition.getZ();
        return this.is(x0 + xMin - 1, y, z0, Blocks.OBSIDIAN) && this.is(x0 + xMax + 1, y, z0, Blocks.OBSIDIAN)
                && this.is(x0, y, z0 + zMin - 1, Blocks.OBSIDIAN) && this.is(x0, y, z0 + zMax + 1, Blocks.OBSIDIAN);
    }

    // ---- areas

    private @Nullable AreaData storeArea() {
        AreaData.Shape shape = this.calculateShape();
        if (shape == null) {
            return null;
        }
        List<BlockState> palette = new ArrayList<>();
        Map<BlockState, Integer> indices = new HashMap<>();
        List<Integer> blocks = new ArrayList<>();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int y = shape.yMin(); y <= shape.yMax(); y++) {
            for (int x = shape.xMin(); x <= shape.xMax(); x++) {
                for (int z = shape.zMin(); z <= shape.zMax(); z++) {
                    BlockState state = air;
                    if (x != 0 || y != 0 || z != 0) {
                        BlockPos pos = this.worldPosition.offset(x, y, z);
                        if (this.level.getBlockEntity(pos) != null) {
                            return null;
                        }
                        state = this.level.getBlockState(pos);
                    }
                    BlockState s = state;
                    blocks.add(indices.computeIfAbsent(s, k -> {
                        palette.add(k);
                        return palette.size() - 1;
                    }));
                }
            }
        }
        return new AreaData(shape, palette, blocks);
    }

    private void notifyBlockOfNeighborChange(int x, int y, int z) {
        BlockPos pos = this.worldPosition.offset(x, y, z);
        this.level.neighborChanged(pos, this.level.getBlockState(pos).getBlock(), null);
    }

    /** notifyEdgeBlocks, with the original's slip (minZ twice in the first loop). */
    private void notifyEdgeBlocks(AreaData.Shape s) {
        for (int x = s.xMin(); x <= s.xMax(); x++) {
            for (int y = s.yMin(); y <= s.yMax(); y++) {
                this.notifyBlockOfNeighborChange(x, y, s.zMin());
                this.notifyBlockOfNeighborChange(x, y, s.zMax());
                this.notifyBlockOfNeighborChange(x, y, s.zMin());
                this.notifyBlockOfNeighborChange(x, y, s.zMax() + 1);
            }
        }
        for (int x = s.xMin(); x <= s.xMax(); x++) {
            for (int z = s.zMin(); z <= s.zMax(); z++) {
                this.notifyBlockOfNeighborChange(x, s.yMin(), z);
                this.notifyBlockOfNeighborChange(x, s.yMax(), z);
                this.notifyBlockOfNeighborChange(x, s.yMin() - 1, z);
                this.notifyBlockOfNeighborChange(x, s.yMax() + 1, z);
            }
        }
        for (int y = s.yMin(); y <= s.yMax(); y++) {
            for (int z = s.zMin(); z <= s.zMax(); z++) {
                this.notifyBlockOfNeighborChange(s.xMin(), y, z);
                this.notifyBlockOfNeighborChange(s.xMax(), y, z);
                this.notifyBlockOfNeighborChange(s.xMin() - 1, y, z);
                this.notifyBlockOfNeighborChange(s.xMax() + 1, y, z);
            }
        }
    }

    private void clearArea(AreaData.Shape s) {
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int y = s.yMin(); y <= s.yMax(); y++) {
            for (int x = s.xMin(); x <= s.xMax(); x++) {
                for (int z = s.zMin(); z <= s.zMax(); z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        this.level.setBlock(this.worldPosition.offset(x, y, z), air, 2);
                    }
                }
            }
        }
        this.notifyEdgeBlocks(s);
    }

    private void unpackArea(AreaData data) {
        AreaData.Shape s = data.shape();
        int index = 0;
        for (int y = s.yMin(); y <= s.yMax(); y++) {
            for (int x = s.xMin(); x <= s.xMax(); x++) {
                for (int z = s.zMin(); z <= s.zMax(); z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        this.level.setBlock(this.worldPosition.offset(x, y, z), data.block(index), 2);
                    }
                    index++;
                }
            }
        }
        this.notifyEdgeBlocks(s);
    }

    private @Nullable QuantumComputerBlockEntity findTwin() {
        if (this.entanglementFrequency >= 0) {
            List<QuantumComputerBlockEntity> twins = REGISTRY.getEntangledObjects(this.entanglementFrequency);
            if (twins != null) {
                for (QuantumComputerBlockEntity computer : twins) {
                    if (computer != this && computer.level != null && !computer.isRemoved()) {
                        return computer;
                    }
                }
            }
        }
        return null;
    }

    public boolean checkIce() {
        for (Direction d : Direction.values()) {
            if (this.level.getBlockState(this.worldPosition.relative(d)).is(Blocks.ICE)) {
                return true;
            }
        }
        return false;
    }

    public int canTeleport() {
        QuantumComputerBlockEntity twin = null;
        if (this.entanglementFrequency >= 0) {
            twin = this.findTwin();
            if (twin == null) {
                return NO_DESTINATION;
            }
        }
        AreaData.Shape localShape = this.calculateShape();
        if (localShape == null) {
            return FRAME_INCOMPLETE;
        }
        if (twin != null) {
            AreaData.Shape twinShape = twin.calculateShape();
            if (twinShape == null) {
                return DESTINATION_FRAME_INCOMPLETE;
            }
            if (!localShape.equals(twinShape)) {
                return FRAME_MISMATCH;
            }
        } else if (this.storedData != null && !localShape.equals(this.storedData.shape())) {
            return FRAME_MISMATCH;
        }
        if (!this.checkIce()) {
            return INSUFFICIENT_COOLING;
        }
        if (this.storeArea() == null) {
            return AREA_NOT_TRANSPORTABLE;
        }
        if (twin != null && twin.storeArea() == null) {
            return DESTINATION_NOT_TRANSPORTABLE;
        }
        return OK;
    }

    public int tryTeleport() {
        int error = this.canTeleport();
        if (error == OK) {
            if (this.entanglementFrequency >= 0) {
                QuantumComputerBlockEntity twin = this.findTwin();
                if (twin != null) {
                    AreaData localData = this.storeArea();
                    AreaData twinData = twin.storeArea();
                    if (localData != null && twinData != null) {
                        this.unpackArea(twinData);
                        twin.unpackArea(localData);
                    }
                }
            } else {
                AreaData localData = this.storeArea();
                if (this.storedData != null) {
                    this.unpackArea(this.storedData);
                } else {
                    this.clearArea(localData.shape());
                }
                this.setStoredData(localData);
            }
            this.level.playSound(null, this.worldPosition, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return error;
    }

    // ---- saving and sync (the client only gets the frequency)

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int frequency = input.getIntOr("f", -1);
        if (this.level != null) {
            this.setEntanglementFrequency(frequency);
        } else {
            this.entanglementFrequency = frequency;
        }
        this.storedData = input.read("d", AreaData.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("f", this.entanglementFrequency);
        if (this.storedData != null) {
            output.store("d", AreaData.CODEC, this.storedData);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("f", this.entanglementFrequency);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ValueInput input) {
        this.setEntanglementFrequency(input.getIntOr("f", -1));
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        this.setEntanglementFrequency(input.getIntOr("f", -1));
    }
}
