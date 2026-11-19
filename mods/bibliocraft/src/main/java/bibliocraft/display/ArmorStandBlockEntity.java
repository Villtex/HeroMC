package bibliocraft.display;

import bibliocraft.common.BiblioBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * TileEntityArmorStand (only in the lower half): slot 0 helmet, 1 chestplate, 2 leggings, 3 boots, stack limit 1.
 * Hoppers may only put armor of the matching type in (isStackValidForSlot; heads only through the GUI).
 */
public class ArmorStandBlockEntity extends BiblioBlockEntity {
    public ArmorStandBlockEntity(BlockPos pos, BlockState state) {
        super(Displays.ArmorStand.ENTITY.get(), pos, state, 4);
    }

    public ItemStack getArmor(int slot) {
        return this.items.get(slot);
    }

    /** addArmor: puts the stack into the slot of its armor type if that one is empty. */
    public boolean addArmor(ItemStack stack, int armorType) {
        if (armorType >= 0 && armorType < 4 && this.items.get(armorType).isEmpty()) {
            this.setItem(armorType, stack);
            return true;
        }
        return false;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return ArmorRules.armorType(stack) == slot;
    }

    /** getRenderBoundingBox reaches over both halves. */
    public net.minecraft.world.phys.AABB renderBox() {
        return new net.minecraft.world.phys.AABB(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(),
                this.worldPosition.getX() + 1, this.worldPosition.getY() + 2, this.worldPosition.getZ() + 1);
    }
}
