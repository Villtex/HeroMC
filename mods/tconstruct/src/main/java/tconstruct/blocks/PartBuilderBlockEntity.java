package tconstruct.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import tconstruct.TBlocks;
import tconstruct.items.PatternBuilder;
import tconstruct.items.PatternItem;

/**
 * {@code PartCrafterLogic}: two rows, each a pattern (0 / 1), a material (2 / 3) and the part with the leftover shard
 * (4, 5 / 6, 7). Taking the part or the shard of a row uses up the material once (cost / value, rounded up); the row
 * shows its next part only when both of its outputs are empty again.
 */
public class PartBuilderBlockEntity extends TableBlockEntity {
    private boolean craftedTop;
    private boolean craftedBottom;

    public PartBuilderBlockEntity(BlockPos pos, BlockState state) {
        super(TBlocks.PART_BUILDER_ENTITY.get(), pos, state, 10);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack stack = super.removeItem(slot, count);
        this.tryBuildPart(slot);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        if ((slot == 0 || slot == 2) && !this.craftedTop) {
            this.buildTopPart();
        }
        if ((slot == 1 || slot == 3) && !this.craftedBottom) {
            this.buildBottomPart();
        }
    }

    private void use(int patternSlot, int materialSlot) {
        int value = PatternBuilder.partValue(this.items.get(materialSlot));
        int cost = this.items.get(patternSlot).getItem() instanceof PatternItem pattern ? pattern.cost() : 0;
        if (value > 0 && cost > 0) {
            int decrease = cost / value;
            if (cost % value != 0) {
                decrease++;
            }
            ContainerHelper.removeItem(this.items, materialSlot, decrease);
        }
    }

    public void tryBuildPart(int slot) {
        if (slot == 4 || slot == 5) {
            if (!this.craftedTop && !this.items.get(0).isEmpty()) {
                this.use(0, 2);
            }
            this.craftedTop = !this.items.get(4).isEmpty() || !this.items.get(5).isEmpty();
        }
        if (!this.craftedTop) {
            this.buildTopPart();
        }
        if (slot == 6 || slot == 7) {
            if (!this.craftedBottom && !this.items.get(1).isEmpty()) {
                this.use(1, 3);
            }
            this.craftedBottom = !this.items.get(6).isEmpty() || !this.items.get(7).isEmpty();
        }
        if (!this.craftedBottom) {
            this.buildBottomPart();
        }
        this.setChanged();
    }

    private void buildTopPart() {
        ItemStack[] parts = PatternBuilder.toolPart(this.items.get(2), this.items.get(0));
        this.items.set(4, parts != null ? parts[0] : ItemStack.EMPTY);
        this.items.set(5, parts != null ? parts[1] : ItemStack.EMPTY);
    }

    private void buildBottomPart() {
        ItemStack[] parts = PatternBuilder.toolPart(this.items.get(3), this.items.get(1));
        this.items.set(6, parts != null ? parts[0] : ItemStack.EMPTY);
        this.items.set(7, parts != null ? parts[1] : ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("CraftedTop", this.craftedTop);
        output.putBoolean("CraftedBottom", this.craftedBottom);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.craftedTop = input.getBooleanOr("CraftedTop", false);
        this.craftedBottom = input.getBooleanOr("CraftedBottom", false);
    }
}
