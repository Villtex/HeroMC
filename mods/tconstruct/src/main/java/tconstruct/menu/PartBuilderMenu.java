package tconstruct.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import tconstruct.blocks.PartBuilderBlockEntity;
import tconstruct.blocks.PatternChestBlockEntity;
import tconstruct.items.PatternItem;

/**
 * {@code PartCrafterContainer} / {@code PartCrafterChestContainer}: patterns (40, 27 / 45), materials (58, 27 / 45),
 * outputs (102 / 120, 27 / 45), and with a pattern chest beside the part builder the chest's 30 slots on the left.
 */
public class PartBuilderMenu extends TableMenu {
    private final @Nullable Container chest;

    public PartBuilderMenu(int id, Inventory inventory, Container builder, @Nullable Container chest) {
        super(TMenus.PART_BUILDER.get(), id, builder, 8);
        this.chest = chest;
        this.addSlot(new PatternSlot(builder, 0, 40, 27));
        this.addSlot(new PatternSlot(builder, 1, 40, 45));
        this.addSlot(new Slot(builder, 2, 58, 27));
        this.addSlot(new Slot(builder, 3, 58, 45));
        this.addSlot(new TakeOnlySlot(builder, 4, 102, 27));
        this.addSlot(new TakeOnlySlot(builder, 5, 120, 27));
        this.addSlot(new TakeOnlySlot(builder, 6, 102, 45));
        this.addSlot(new TakeOnlySlot(builder, 7, 120, 45));
        if (chest != null) {
            for (int column = 0; column < 5; column++) {
                for (int row = 0; row < 6; row++) {
                    this.addSlot(new PatternSlot(chest, row + column * 6, -108 + row * 18, 30 + column * 18));
                }
            }
        }
        this.addPlayer(inventory, 8, 84);
    }

    public boolean hasChest() {
        return this.chest != null;
    }

    /** PartCrafterLogic.getGuiContainer: the first pattern chest in the 3x3 around the part builder, same height. */
    public static @Nullable PatternChestBlockEntity findChest(Level level, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (level.getBlockEntity(pos.offset(x, 0, z)) instanceof PatternChestBlockEntity chest) {
                    return chest;
                }
            }
        }
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && (this.chest == null || this.chest.stillValid(player));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int inventoryStart = this.chest == null ? 8 : 38;
        int total = this.slots.size();
        if (index < inventoryStart) {
            if (!this.moveItemStackTo(stack, inventoryStart, total, true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof PatternItem) {
            if (!this.moveItemStackTo(stack, 0, 2, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 2, 4, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
            if (index < 8 && this.table instanceof PartBuilderBlockEntity builder) {
                builder.tryBuildPart(index);
            }
        }
        slot.setChanged();
        return copy;
    }
}
