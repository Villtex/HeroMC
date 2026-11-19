package bibliocraft.display;

import bibliocraft.common.BiblioBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * TileEntityLabel: three slots (0 centre, 1 left, 2 right), stack limit 64. Hoppers cannot put anything in
 * (isStackValidForSlot false). The light is the brightest of the three shown blocks.
 */
public class LabelBlockEntity extends BiblioBlockEntity {
    public LabelBlockEntity(BlockPos pos, BlockState state) {
        super(Displays.LABEL_ENTITY.get(), pos, state, 3);
    }

    @Override
    protected void contentsChanged() {
        if (this.level != null) {
            int light = 0;
            for (ItemStack stack : this.items) {
                light = Math.max(light, DisplayLight.of(stack));
            }
            DisplayLight.set(this.level, this.worldPosition, light);
        }
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }
}
