package betterstorage.block;

import betterstorage.BS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** TileEntityLocker: 9 x 3, connects to the locker above or below with the same orientation and door side. */
public class LockerBlockEntity extends ConnectableBlockEntity {
    private static final Direction[] NEIGHBORS = {Direction.DOWN, Direction.UP};

    public LockerBlockEntity(BlockPos pos, BlockState state) {
        super(BS.LOCKER_ENTITY.get(), pos, state);
    }

    public boolean mirror() {
        BlockState state = this.getBlockState();
        return state.hasProperty(LockerBlock.MIRROR) && state.getValue(LockerBlock.MIRROR);
    }

    @Override
    public Direction[] getPossibleNeighbors() {
        return NEIGHBORS;
    }

    @Override
    protected String getConnectableName() {
        return "container.betterstorage.locker";
    }

    @Override
    public boolean canConnect(ConnectableBlockEntity other) {
        return other instanceof LockerBlockEntity locker && super.canConnect(other) && this.mirror() == locker.mirror();
    }

    /** getRenderBoundingBox: one block higher (the large locker). */
    public AABB renderBox() {
        return new AABB(this.worldPosition).expandTowards(0.0, 1.0, 0.0);
    }
}
