package tconstruct.landmine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import tconstruct.TDeco;
import tconstruct.machine.CamoBlockEntity;

/**
 * TileEntityLandmine: slots 0..2 the trigger items (Behavior), slot 3 the camouflage block. The trigger type is the
 * block of today (landmine, stone, obsidian, redstone landmine). The contents drop when it is broken, not when it goes off.
 */
public class LandmineBlockEntity extends CamoBlockEntity {
    public int soundCooldown;
    public boolean isExploding;

    public LandmineBlockEntity(BlockPos pos, BlockState state) {
        super(TDeco.LANDMINE_ENTITY.get(), pos, state, 4);
    }

    @Override
    protected int camoSlot() {
        return 3;
    }

    @Override
    protected boolean canDrop(int slot) {
        return !this.isExploding;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LandmineBlockEntity mine) {
        if (mine.soundCooldown > 0) {
            mine.soundCooldown--;
        }
    }
}
