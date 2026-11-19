package betterstorage.block;

import betterstorage.BS;
import betterstorage.BSConfig;
import betterstorage.BSUtil;
import betterstorage.item.LockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * TileEntityReinforcedChest: reinforcedChestColumns x 3 (13 in the pack), connects to the side like a vanilla chest
 * when neither half is locked. The lock and the redstone pulse of the trigger enchantment belong to the main half.
 * A locked chest offers nothing to hoppers.
 */
public class ReinforcedChestBlockEntity extends ConnectableBlockEntity implements Lockable {
    private static final Direction[] NEIGHBORS = {Direction.EAST, Direction.NORTH, Direction.WEST, Direction.SOUTH};
    private ItemStack lock = ItemStack.EMPTY;
    private boolean powered;

    public ReinforcedChestBlockEntity(BlockPos pos, BlockState state) {
        super(BS.CHEST_ENTITY.get(), pos, state);
    }

    @Override
    public Direction[] getPossibleNeighbors() {
        return NEIGHBORS;
    }

    @Override
    protected String getConnectableName() {
        return "container.betterstorage.reinforced_chest";
    }

    @Override
    public int getColumns() {
        return BSConfig.reinforcedChestColumns();
    }

    /** Only side by side across the front (x differs for north/south facing chests, z for east/west), both unlocked. */
    @Override
    public boolean canConnect(ConnectableBlockEntity other) {
        if (!(other instanceof ReinforcedChestBlockEntity chest)) {
            return false;
        }
        Direction o = this.orientation();
        BlockPos p = this.worldPosition;
        BlockPos q = chest.worldPosition;
        return super.canConnect(other)
                && (p.getX() != q.getX() && (o == Direction.SOUTH || o == Direction.NORTH) || p.getZ() != q.getZ() && (o == Direction.EAST || o == Direction.WEST))
                && this.getLock().isEmpty() && chest.getLock().isEmpty();
    }

    @Override
    public void dropContents() {
        super.dropContents();
        if (this.level != null) {
            BSUtil.dropFromBlock(this.level, this.worldPosition, this.lock);
            this.lock = ItemStack.EMPTY;
        }
    }

    @Override
    protected boolean isAccessible() {
        return this.getLock().isEmpty();
    }

    @Override
    public ItemStack getLock() {
        ConnectableBlockEntity main = this.getMain();
        return main instanceof ReinforcedChestBlockEntity chest ? chest.lock : this.lock;
    }

    @Override
    public boolean isLockValid(ItemStack lock) {
        return lock.getItem() instanceof LockItem;
    }

    @Override
    public void setLock(ItemStack lock) {
        if (!lock.isEmpty() && !this.isLockValid(lock)) {
            throw new IllegalArgumentException("Can't set lock to " + lock + ".");
        }
        if (this.isMain()) {
            this.lock = lock;
            this.sync();
        } else if (this.getMain() instanceof ReinforcedChestBlockEntity main && main != this) {
            main.setLock(lock);
        }
    }

    /** Someone has it open: others may look in without the key. */
    @Override
    public boolean canUse(Player player) {
        return this.getPlayersUsing() > 0;
    }

    @Override
    public void applyTrigger() {
        this.setPowered(true);
    }

    public boolean isPowered() {
        return this.getMain() instanceof ReinforcedChestBlockEntity main ? main.powered : this.powered;
    }

    /** Powers both halves for 10 ticks (scheduled block tick switches it off) and tells all neighbours. */
    public void setPowered(boolean powered) {
        if (this.getMain() instanceof ReinforcedChestBlockEntity main && main != this) {
            main.setPowered(powered);
            return;
        }
        if (this.powered == powered || this.level == null) {
            return;
        }
        this.powered = powered;
        Block block = this.getBlockState().getBlock();
        if (powered) {
            this.level.scheduleTick(this.worldPosition, block, 10);
        }
        BlockPos pos = this.worldPosition;
        this.level.updateNeighborsAt(pos, block);
        for (Direction dir : Direction.values()) {
            this.level.updateNeighborsAt(pos.relative(dir), block);
        }
        Connection c = this.connection();
        if (c == Connection.EAST || c == Connection.SOUTH) {
            BlockPos other = pos.relative(c.direction);
            for (Direction dir : Direction.values()) {
                if (dir != c.direction.getOpposite()) {
                    this.level.updateNeighborsAt(other.relative(dir), block);
                }
            }
        }
    }

    /** getRenderBoundingBox: one block more in every direction (the large chest). */
    public AABB renderBox() {
        return new AABB(this.worldPosition).inflate(1.0);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.lock = input.read("lock", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.powered = input.getBooleanOr("powered", false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.lock.isEmpty()) {
            output.store("lock", ItemStack.CODEC, this.lock);
        }
        if (this.powered) {
            output.putBoolean("powered", true);
        }
    }
}
