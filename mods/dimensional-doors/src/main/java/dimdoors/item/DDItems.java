package dimdoors.item;

import dimdoors.DimDoors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Helpers the items of Dimensional Doors share. */
public final class DDItems {
    private DDItems() {
    }

    /**
     * getMovingObjectPositionFromPlayer with liquids: the first block along the look within {@code range} that can be
     * hit, rifts and liquids included (rifts cannot be aimed at otherwise).
     */
    public static @Nullable BlockPos target(Level level, Player player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        BlockPos last = null;
        for (double d = 0; d <= range; d += 0.05) {
            BlockPos at = BlockPos.containing(eye.add(look.scale(d)));
            if (at.equals(last)) {
                continue;
            }
            last = at;
            BlockState state = level.getBlockState(at);
            if (state.is(DimDoors.RIFT.get()) || !state.getFluidState().isEmpty()) {
                return at;
            }
            if (!state.getShape(level, at).isEmpty()) {
                var hit = state.getShape(level, at).clip(eye, eye.add(look.scale(range)), at);
                if (hit != null) {
                    return at;
                }
            }
        }
        return null;
    }

    /** itemDimDoor.canPlace: air, fabric, rifts, or anything replaceable. */
    public static boolean canPlace(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(DimDoors.FABRIC.get()) || state.is(DimDoors.RIFT.get()) || state.is(DimDoors.ETERNAL_FABRIC.get())
                || state.canBeReplaced();
    }
}
