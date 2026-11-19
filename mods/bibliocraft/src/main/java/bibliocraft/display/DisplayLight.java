package bibliocraft.display;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;

/**
 * Block.getLightValue of the weapon case and the label: the light value of the block (Block.lightValue[itemID]) of the
 * shown stack. In 26.3 the light emission of a block state is fixed, so the value lives in the chunk's auxiliary light
 * manager (NeoForge), which the block entity sets on both sides whenever its contents change.
 */
public final class DisplayLight {
    private DisplayLight() {
    }

    /** Block.lightValue[stack.itemID] for block items (item ids below 4096), else 0. */
    public static int of(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem bi ? bi.getBlock().defaultBlockState().getLightEmission() : 0;
    }

    public static int get(BlockGetter level, BlockPos pos) {
        AuxiliaryLightManager lights = level.getAuxLightManager(pos);
        return lights != null ? lights.getLightAt(pos) : 0;
    }

    /** Sets the light at {@code pos} (re-lights the block when it changes). */
    public static void set(Level level, BlockPos pos, int value) {
        AuxiliaryLightManager lights = level.getAuxLightManager(pos);
        if (lights != null && lights.getLightAt(pos) != value) {
            lights.setLightAt(pos, value);
        }
    }

    public static void remove(Level level, BlockPos pos) {
        AuxiliaryLightManager lights = level.getAuxLightManager(pos);
        if (lights != null) {
            lights.removeLightAt(pos);
        }
    }
}
