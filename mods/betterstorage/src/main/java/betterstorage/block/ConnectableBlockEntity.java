package betterstorage.block;

import betterstorage.menu.BSMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * TileEntityConnectable: two of the same kind next to each other (as {@link #getPossibleNeighbors} allows) form one
 * large container. The half towards negative x/y/z is the main one; it draws both and holds the shared state. As an
 * IInventory (hoppers and other machines) it offers the whole large inventory, unless {@link #isAccessible} says no.
 */
public abstract class ConnectableBlockEntity extends ContainerBlockEntity implements Container {
    protected ConnectableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract Direction[] getPossibleNeighbors();

    protected abstract String getConnectableName();

    public Direction orientation() {
        BlockState state = this.getBlockState();
        return state.hasProperty(ConnectableBlock.FACING) ? state.getValue(ConnectableBlock.FACING) : Direction.SOUTH;
    }

    public Connection connection() {
        BlockState state = this.getBlockState();
        return state.hasProperty(ConnectableBlock.CONNECTION) ? state.getValue(ConnectableBlock.CONNECTION) : Connection.NONE;
    }

    public boolean isConnected() {
        return this.connection() != Connection.NONE;
    }

    public boolean isMain() {
        return this.connection().isMain();
    }

    public ConnectableBlockEntity getMain() {
        if (this.isMain()) {
            return this;
        }
        ConnectableBlockEntity connected = this.getConnected();
        return connected != null ? connected : this;
    }

    public @Nullable ConnectableBlockEntity getConnected() {
        Direction dir = this.connection().direction;
        if (dir == null || this.level == null) {
            return null;
        }
        return this.level.getBlockEntity(this.worldPosition.relative(dir)) instanceof ConnectableBlockEntity c ? c : null;
    }

    public boolean canConnect(@Nullable ConnectableBlockEntity other) {
        return other != null && this.getBlockState().getBlock() == other.getBlockState().getBlock() && this.orientation() == other.orientation()
                && !this.isConnected() && !other.isConnected();
    }

    /** Connects to the one neighbour that can connect; with two candidates it stays alone. */
    public void checkForConnections() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        ConnectableBlockEntity found = null;
        Direction foundDir = null;
        for (Direction dir : this.getPossibleNeighbors()) {
            ConnectableBlockEntity other = this.level.getBlockEntity(this.worldPosition.relative(dir)) instanceof ConnectableBlockEntity c ? c : null;
            if (this.canConnect(other)) {
                if (found != null) {
                    return;
                }
                found = other;
                foundDir = dir;
            }
        }
        if (found != null) {
            setConnection(found, Connection.of(foundDir.getOpposite()));
            setConnection(this, Connection.of(foundDir));
        }
    }

    private static void setConnection(ConnectableBlockEntity be, Connection connection) {
        if (be.level != null && be.getBlockState().hasProperty(ConnectableBlock.CONNECTION)) {
            be.level.setBlock(be.worldPosition, be.getBlockState().setValue(ConnectableBlock.CONNECTION, connection), 3);
        }
    }

    /** Called while this block is removed: the other half is alone again. */
    public void disconnect() {
        if (this.isConnected()) {
            ConnectableBlockEntity other = this.getConnected();
            if (other != null) {
                setConnection(other, Connection.NONE);
            }
        }
    }

    @Override
    public final String getName() {
        return this.getConnectableName() + (this.isConnected() ? "_large" : "");
    }

    @Override
    public void openGui(ServerPlayer player) {
        if (this.canPlayerUseContainer(player)) {
            TileInventory inventory = this.getPlayerInventory();
            BSMenu.open(player, this.getContainerTitle(), this.getColumns(), (this.isConnected() ? 2 : 1) * this.getRows(), inventory);
        }
    }

    /** Both halves, the main one first; opening counts on this one (the one clicked). */
    @Override
    public TileInventory getPlayerInventory() {
        if (this.isConnected()) {
            ConnectableBlockEntity main = this.getMain();
            ConnectableBlockEntity other = main == this ? this.getConnected() : this;
            if (other != null && other != main) {
                return new TileInventory(this, main, other);
            }
        }
        return super.getPlayerInventory();
    }

    /** updateEntity: the main half plays the chest sounds between both halves, with the lid of the one further open. */
    @Override
    public void tick(Level level) {
        super.tick(level);
        double x = this.worldPosition.getX() + 0.5;
        double y = this.worldPosition.getY() + 0.5;
        double z = this.worldPosition.getZ() + 0.5;
        if (this.isConnected()) {
            if (!this.isMain()) {
                return;
            }
            ConnectableBlockEntity other = this.getConnected();
            if (other != null) {
                x = (x + other.worldPosition.getX() + 0.5) / 2.0;
                z = (z + other.worldPosition.getZ() + 0.5) / 2.0;
                this.lidAngle = Math.max(this.lidAngle, other.lidAngle);
            }
        }
        if (level.isClientSide()) {
            return;
        }
        float pitch = level.getRandom().nextFloat() * 0.1F + 0.9F;
        if (this.lidAngle > 0.0F && this.prevLidAngle == 0.0F) {
            level.playSound(null, x, y, z, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, pitch);
        }
        if (this.lidAngle < 0.5F && this.prevLidAngle >= 0.5F) {
            level.playSound(null, x, y, z, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, pitch);
        }
    }

    /** Original drop of breakBlock: own contents (and the lock, see subclasses), then disconnect. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        this.dropContents();
        this.disconnect();
    }

    // ---- IInventory ----

    protected boolean isAccessible() {
        return true;
    }

    @Override
    public int getContainerSize() {
        return this.isAccessible() ? this.getPlayerInventory().getContainerSize() : 0;
    }

    @Override
    public boolean isEmpty() {
        return !this.isAccessible() || this.getPlayerInventory().isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.isAccessible() ? this.getPlayerInventory().getItem(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        return this.isAccessible() ? this.getPlayerInventory().removeItem(slot, count) : ItemStack.EMPTY;
    }

    /** getStackInSlotOnClosing gave nothing. */
    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (this.isAccessible()) {
            this.getPlayerInventory().setItem(slot, stack);
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return this.isAccessible();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.isAccessible() && this.getPlayerInventory().stillValid(player);
    }

    @Override
    public void startOpen(ContainerUser user) {
        if (this.isAccessible()) {
            this.getPlayerInventory().startOpen(user);
        }
    }

    @Override
    public void stopOpen(ContainerUser user) {
        if (this.isAccessible()) {
            this.getPlayerInventory().stopOpen(user);
        }
    }

    @Override
    public void clearContent() {
        this.contents.replaceAll(s -> ItemStack.EMPTY);
    }
}
