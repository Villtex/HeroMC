package qcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;
import qcraft.QCConfig;
import qcraft.QuantumCraft;

import java.util.List;
import java.util.Random;
import java.util.function.BooleanSupplier;

/**
 * TileEntityQBlock. Six side types, the entanglement frequency and the observation state.
 *
 * <p>Server: every tick all players of the level within 96 blocks who look towards the block (dot &lt; -0.4) and wear
 * neither a pumpkin nor quantum goggles vote for the side they see (the major axis from the block to their eyes); a
 * quantum block (sub type 1) flips each vote to the opposite side half of the time. Entangled blocks add the votes of
 * their twins, or take the side of a twin that was already observed this tick. When the block becomes observed it shows
 * the winning side; when nobody observes it any more it keeps the side until it is observed again. A change of the
 * shown type is sent to the clients, where the block swirls for {@link #FUZZ_TIME} ticks.
 *
 * <p>Client: with quantum goggles every qBlock shows fuzz, as does a qBlock showing nothing next to a liquid.
 */
public class QBlockEntity extends BlockEntity {
    public static final int FUZZ_TIME = 9;
    public static final Random RANDOM = new Random();
    public static final EntanglementRegistry<QBlockEntity> REGISTRY = new EntanglementRegistry<>();
    public static final EntanglementRegistry<QBlockEntity> CLIENT_REGISTRY = new EntanglementRegistry<>();
    public static final ModelProperty<Appearance> APPEARANCE = new ModelProperty<>();
    /** QuantumCraft.isLocalPlayerWearingGoggles; set by the client. */
    public static BooleanSupplier localPlayerWearingGoggles = () -> false;

    public enum Appearance { BLOCK, FUZZ, SWIRL }

    private int entanglementFrequency = -1;
    private int[] sideBlockTypes = new int[6];
    private boolean currentlyObserved = false;
    private long timeLastUpdated = -99L;
    private int currentDisplayedSide = -1;
    private int timeSinceLastChange = FUZZ_TIME;
    private boolean goggles = false;
    private boolean wet = false;
    private boolean registered = false;

    public QBlockEntity(BlockPos pos, BlockState state) {
        super(QuantumCraft.QBLOCK_ENTITY.get(), pos, state);
    }

    public static EntanglementRegistry<QBlockEntity> getEntanglementRegistry(Level level) {
        return !level.isClientSide() ? REGISTRY : CLIENT_REGISTRY;
    }

    private EntanglementRegistry<QBlockEntity> registry() {
        return getEntanglementRegistry(this.level);
    }

    // ---- validate / invalidate

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

    // ---- data

    public void setTypes(int[] types) {
        this.sideBlockTypes = types.clone();
        this.setChanged();
    }

    public int[] getTypes() {
        return this.sideBlockTypes.clone();
    }

    public void setEntanglementFrequency(int frequency) {
        if (frequency != this.entanglementFrequency) {
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

    public int getSubType() {
        return this.getBlockState().getValue(QBlock.SUBTYPE);
    }

    public int getObservedType() {
        int side = this.currentDisplayedSide < 0 ? 1 : this.currentDisplayedSide;
        return QBlock.clampType(this.sideBlockTypes[side]);
    }

    public Appearance getAppearance() {
        if (this.currentDisplayedSide < 0) {
            return Appearance.SWIRL;
        }
        int type = this.sideBlockTypes[this.currentDisplayedSide];
        if (!this.goggles && (!this.wet || type != 0)) {
            return this.timeSinceLastChange < FUZZ_TIME ? Appearance.SWIRL : Appearance.BLOCK;
        }
        return Appearance.FUZZ;
    }

    @Override
    public ModelData getModelData() {
        return ModelData.of(APPEARANCE, this.getAppearance());
    }

    /**
     * blockUpdate: markBlockForUpdate, a scheduled tick (falling) and the neighbours. The server also puts the shown type
     * into the block state, so that the block behaves like its impostor.
     */
    private void blockUpdate() {
        Level level = this.level;
        if (level == null) {
            return;
        }
        BlockState state = this.getBlockState();
        if (level.isClientSide()) {
            this.requestModelDataUpdate();
            level.sendBlockUpdated(this.worldPosition, state, state, 8);
            return;
        }
        int type = this.getObservedType();
        if (state.getValue(QBlock.TYPE) != type) {
            state = state.setValue(QBlock.TYPE, type);
            level.setBlock(this.worldPosition, state, 3);
        }
        this.setChanged();
        level.sendBlockUpdated(this.worldPosition, state, state, 3);
        level.scheduleTick(this.worldPosition, state.getBlock(), 2);
        level.updateNeighborsAt(this.worldPosition, state.getBlock());
    }

    private void setObservedSide(boolean observed, int side) {
        this.currentlyObserved = observed;
        if (this.currentDisplayedSide != side) {
            int oldSide = this.currentDisplayedSide;
            int oldType = this.getObservedType();
            this.currentDisplayedSide = side;
            int newType = this.getObservedType();
            if (newType != oldType || oldSide < 0) {
                this.timeSinceLastChange = 0;
                this.blockUpdate();
            }
        }
    }

    // ---- observation (server)

    private boolean checkRayClear(Vec3 playerPos, Vec3 blockPos, Player player) {
        BlockHitResult hit = this.level.clip(new ClipContext(playerPos, blockPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(this.worldPosition);
    }

    private static boolean blindsObserver(ItemStack headGear) {
        return headGear.is(Items.CARVED_PUMPKIN) || headGear.is(QuantumCraft.QUANTUM_GOGGLES.get());
    }

    private int[] collectVotes() {
        int[] votes = new int[6];
        double centerX = this.worldPosition.getX() + 0.5;
        double centerY = this.worldPosition.getY() + 0.5;
        double centerZ = this.worldPosition.getZ() + 0.5;
        List<? extends Player> players = this.level.players();
        for (Player player : players) {
            if (blindsObserver(player.getItemBySlot(EquipmentSlot.HEAD))) {
                continue;
            }
            double x = player.getX() - centerX;
            // posY + 1.62 - yOffset: the eye height of a standing player on the server
            double y = player.getY() + 1.62 - centerY;
            double z = player.getZ() - centerZ;
            double distance = Math.sqrt(x * x + y * y + z * z);
            if (distance >= 96.0) {
                continue;
            }
            double dx = x / distance;
            double dy = y / distance;
            double dz = z / distance;
            float pitch = player.getXRot();
            float yaw = player.getYRot();
            float f3 = Mth.cos(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
            float f4 = Mth.sin(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
            float f5 = -Mth.cos(-pitch * (float) (Math.PI / 180.0));
            float f6 = Mth.sin(-pitch * (float) (Math.PI / 180.0));
            double fx = f4 * f5;
            double fy = f6;
            double fz = f3 * f5;
            double dot = fx * dx + fy * dy + fz * dz;
            if (dot >= -0.4) {
                continue;
            }
            if (QCConfig.occlusionTesting()) {
                Vec3 playerPos = new Vec3(centerX + x, centerY + y, centerZ + z);
                boolean lineOfSightFound = false;
                for (int side = 0; side < 6; side++) {
                    Direction d = Direction.from3DDataValue(side);
                    Vec3 blockPos = new Vec3(centerX + 0.49 * d.getStepX(), centerY + 0.49 * d.getStepY(), centerZ + 0.49 * d.getStepZ());
                    if (this.checkRayClear(playerPos, blockPos, player)) {
                        lineOfSightFound = true;
                        break;
                    }
                }
                if (!lineOfSightFound) {
                    continue;
                }
            }
            int majoraxis = -1;
            double majorweight = 0.0;
            if (-dy >= majorweight) {
                majoraxis = 0;
                majorweight = -dy;
            }
            if (dy >= majorweight) {
                majoraxis = 1;
                majorweight = dy;
            }
            if (-dz >= majorweight) {
                majoraxis = 2;
                majorweight = -dz;
            }
            if (dz >= majorweight) {
                majoraxis = 3;
                majorweight = dz;
            }
            if (-dx >= majorweight) {
                majoraxis = 4;
                majorweight = -dx;
            }
            if (dx >= majorweight) {
                majoraxis = 5;
            }
            if (majoraxis >= 0) {
                if (this.getSubType() == 1 && RANDOM.nextBoolean()) {
                    majoraxis = majoraxis ^ 1;
                }
                votes[majoraxis]++;
            }
        }
        return votes;
    }

    private static int[] addVotes(int[] a, int[] b) {
        int[] c = new int[6];
        for (int i = 0; i < 6; i++) {
            c[i] = a[i] + b[i];
        }
        return c;
    }

    private static int tallyVotes(int[] votes) {
        int winner = 0;
        int winnerVotes = 0;
        for (int i = 0; i < 6; i++) {
            if (votes[i] > winnerVotes) {
                winner = i;
                winnerVotes = votes[i];
            }
        }
        return winnerVotes > 0 ? winner : -1;
    }

    private int getObservationResult(long currentTime) {
        int[] votes = new int[6];
        if (this.entanglementFrequency >= 0) {
            List<QBlockEntity> twins = this.registry().getEntangledObjects(this.entanglementFrequency);
            if (twins != null) {
                for (QBlockEntity twin : twins) {
                    if (twin != this && twin.level != null && !twin.isRemoved()) {
                        if (twin.currentlyObserved && twin.timeLastUpdated == currentTime) {
                            return twin.currentDisplayedSide;
                        }
                        votes = addVotes(votes, twin.collectVotes());
                    }
                }
            }
        }
        votes = addVotes(votes, this.collectVotes());
        return tallyVotes(votes);
    }

    private void redetermineObservedSide() {
        long currentTime = this.level.getGameTime();
        int winner = this.getObservationResult(currentTime);
        if (winner >= 0) {
            if (!this.currentlyObserved) {
                this.setObservedSide(true, winner);
            }
        } else if (this.currentlyObserved) {
            this.setObservedSide(false, this.currentDisplayedSide);
        }
        this.timeLastUpdated = currentTime;
    }

    private boolean isTouchingLiquid() {
        for (int i = 1; i < 6; i++) {
            BlockPos p = this.worldPosition.relative(Direction.from3DDataValue(i));
            if (this.level.getBlockState(p).getBlock() instanceof LiquidBlock) {
                return true;
            }
        }
        return false;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, QBlockEntity qblock) {
        if (!level.isClientSide()) {
            qblock.redetermineObservedSide();
        }
        qblock.timeSinceLastChange++;
        boolean goggles = level.isClientSide() && localPlayerWearingGoggles.getAsBoolean();
        boolean wet = qblock.isTouchingLiquid();
        if (qblock.goggles != goggles || qblock.wet != wet || qblock.timeSinceLastChange == FUZZ_TIME) {
            qblock.wet = wet;
            qblock.goggles = goggles;
            qblock.blockUpdate();
        }
    }

    // ---- saving and sync

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.currentlyObserved = input.getBooleanOr("o", false);
        this.currentDisplayedSide = input.getIntOr("d", -1);
        int frequency = input.getIntOr("f", -1);
        if (frequency != this.entanglementFrequency) {
            if (this.registered) {
                this.registry().unregister(this.entanglementFrequency, this);
                this.registered = false;
            }
            this.entanglementFrequency = frequency;
            if (this.level != null) {
                this.register();
            }
        }
        for (int i = 0; i < 6; i++) {
            this.sideBlockTypes[i] = input.getIntOr("s" + i, 0);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("o", this.currentlyObserved);
        output.putInt("d", this.currentDisplayedSide);
        output.putInt("f", this.entanglementFrequency);
        for (int i = 0; i < 6; i++) {
            output.putInt("s" + i, this.sideBlockTypes[i]);
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

    @Override
    public void onDataPacket(Connection net, ValueInput input) {
        int oldSide = this.currentDisplayedSide;
        int oldType = this.getObservedType();
        this.loadWithComponents(input);
        int newType = this.getObservedType();
        if (newType != oldType || oldSide < 0) {
            this.timeSinceLastChange = 0;
            this.blockUpdate();
        }
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        super.handleUpdateTag(input);
        this.requestModelDataUpdate();
    }
}
