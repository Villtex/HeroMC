package bibliocraft.shelf;

import bibliocraft.common.BCConfig;
import bibliocraft.common.BiblioBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** TileEntityPotionShelf: 12 slots, three shelves of four (0..3 top, 4..7 middle, 8..11 bottom), stack limit 64. */
public class PotionShelfBlockEntity extends BiblioBlockEntity {
    public PotionShelfBlockEntity(BlockPos pos, BlockState state) {
        super(Shelves.POTION_SHELF_ENTITY.get(), pos, state, 12);
    }

    /** addPotion: puts a stack into an empty slot (true), or with EMPTY empties the slot (false). */
    public boolean addPotion(ItemStack stack, int slot) {
        if (stack.isEmpty() && !this.getItem(slot).isEmpty()) {
            this.setItem(slot, ItemStack.EMPTY);
            return false;
        }
        if (!stack.isEmpty() && this.getItem(slot).isEmpty()) {
            this.setItem(slot, stack);
            return true;
        }
        return false;
    }

    /** isStackValidForSlot (hoppers): Config.testPotionValidity. */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return BCConfig.isPotion(stack);
    }
}
