package bibliocraft.shelf;

import bibliocraft.common.BCConfig;
import bibliocraft.common.BiblioBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.block.state.BlockState;

/** TileEntityWeaponRack: four slots (0, 1 upper pegs, 2, 3 lower pegs) for tools, stack limit 64. */
public class ToolRackBlockEntity extends BiblioBlockEntity {
    public ToolRackBlockEntity(BlockPos pos, BlockState state) {
        super(Shelves.TOOL_RACK_ENTITY.get(), pos, state, 4);
    }

    /**
     * isItemTool: ItemTool, ItemSword, ItemHoe (now items with a tool or weapon component), ItemBow, ItemFishingRod,
     * ItemShears, ItemFlintAndSteel, or a tool keyword in the name (Config.testToolValidity).
     */
    public static boolean isItemTool(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        return stack.has(DataComponents.TOOL) || stack.has(DataComponents.WEAPON) || item instanceof BowItem || item instanceof FishingRodItem
                || item instanceof ShearsItem || item instanceof FlintAndSteelItem
                || BCConfig.testToolValidity(BCConfig.displayName(stack), BCConfig.codeName(stack));
    }

    /** addTool: puts the stack into an empty slot. */
    public boolean addTool(int slot, ItemStack stack) {
        if (this.getItem(slot).isEmpty()) {
            this.setItem(slot, stack);
            return true;
        }
        return false;
    }

    /** removeTool. */
    public void removeTool(int slot) {
        if (!this.getItem(slot).isEmpty()) {
            this.setItem(slot, ItemStack.EMPTY);
        }
    }

    /** isStackValidForSlot (hoppers). */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return isItemTool(stack);
    }
}
