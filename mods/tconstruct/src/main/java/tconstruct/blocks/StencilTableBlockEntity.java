package tconstruct.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import tconstruct.TBlocks;
import tconstruct.TItems;
import tconstruct.items.PatternItem;

/**
 * {@code StencilTableLogic}: a blank pattern (0) and the chosen pattern (1, chosen with the buttons of the GUI). Taking
 * the pattern uses one blank; the pattern stays while blanks are left. As in 1.5.2, an ingot cast in the input shows a
 * tool rod pattern and a tool rod pattern or cast an ingot cast.
 */
public class StencilTableBlockEntity extends TableBlockEntity {
    public StencilTableBlockEntity(BlockPos pos, BlockState state) {
        super(TBlocks.STENCIL_TABLE_ENTITY.get(), pos, state, 2);
    }

    @Override
    protected boolean canDrop(int slot) {
        return slot == 0;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        if (slot == 0 && stack.getItem() instanceof PatternItem pattern) {
            if (pattern.meta == 0) {
                super.setItem(1, new ItemStack(TItems.woodPattern(1)));
            } else if (pattern.meta == 1) {
                super.setItem(1, new ItemStack(TItems.CASTS.get(0).get()));
            }
        }
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        if (slot == 1) {
            ContainerHelper.removeItem(this.items, 0, 1);
            this.setChanged();
            return this.items.get(0).isEmpty() ? super.removeItem(1, count) : this.items.get(1).copy();
        }
        ItemStack stack = super.removeItem(slot, count);
        if (this.items.get(0).isEmpty()) {
            ContainerHelper.removeItem(this.items, 1, 1);
        }
        return stack;
    }
}
