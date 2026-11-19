package betterstorage.block;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

/** TileEntityConnectable.connected: the side the other half is on, or none (ForgeDirection.UNKNOWN). */
public enum Connection implements StringRepresentable {
    NONE("none", null),
    DOWN("down", Direction.DOWN),
    UP("up", Direction.UP),
    NORTH("north", Direction.NORTH),
    SOUTH("south", Direction.SOUTH),
    WEST("west", Direction.WEST),
    EAST("east", Direction.EAST);

    private final String name;
    public final @Nullable Direction direction;

    Connection(String name, @Nullable Direction direction) {
        this.name = name;
        this.direction = direction;
    }

    public static Connection of(@Nullable Direction direction) {
        if (direction == null) {
            return NONE;
        }
        for (Connection c : values()) {
            if (c.direction == direction) {
                return c;
            }
        }
        return NONE;
    }

    /** isMain: not connected, or the other half lies towards positive x/y/z (this one is the west/north/lower half). */
    public boolean isMain() {
        return this.direction == null || this.direction.getStepX() + this.direction.getStepY() + this.direction.getStepZ() > 0;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
