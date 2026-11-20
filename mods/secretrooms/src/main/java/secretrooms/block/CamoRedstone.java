package secretrooms.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** notifyArround of the camo levers, buttons and plates: the block itself and its four horizontal neighbours. */
final class CamoRedstone {
    private CamoRedstone() {
    }

    static void notifyAround(Level level, BlockPos pos, BlockState state) {
        level.updateNeighborsAt(pos, state.getBlock());
        for (Direction d : Direction.Plane.HORIZONTAL) {
            level.updateNeighborsAt(pos.relative(d), state.getBlock());
        }
    }

    /** "random.click" at the block centre. */
    static void click(Level level, BlockPos pos, double yOff, float pitch) {
        level.playSound(null, pos.getX() + 0.5, pos.getY() + yOff, pos.getZ() + 0.5, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.3F, pitch);
    }
}
