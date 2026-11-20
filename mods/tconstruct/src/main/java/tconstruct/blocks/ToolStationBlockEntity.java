package tconstruct.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import tconstruct.TBlocks;
import tconstruct.tools.ToolBuilder;

/**
 * {@code ToolStationLogic} / {@code ToolForgeLogic}: slot 0 is the result, 1 the tool or head, 2 and 3 (and 4 at the
 * forge) the other parts or the modifier items. Every change of an input builds the result again; the name typed in
 * the GUI names the tool (it is not saved, as in 1.5.2). Breaking the table drops the inputs, not the result.
 */
public class ToolStationBlockEntity extends TableBlockEntity {
    private final boolean forge;
    private String toolName = "";

    public ToolStationBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, state.getBlock() instanceof TableBlock table && table.kind == TableBlock.Kind.TOOL_FORGE);
    }

    private ToolStationBlockEntity(BlockPos pos, BlockState state, boolean forge) {
        super(TBlocks.TOOL_STATION_ENTITY.get(), pos, state, forge ? 5 : 4);
        this.forge = forge;
    }

    public boolean isForge() {
        return this.forge;
    }

    @Override
    protected boolean canDrop(int slot) {
        return slot != 0;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        if (slot != 0) {
            this.buildTool();
        }
        this.setChanged();
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack stack = super.removeItem(slot, count);
        if (slot != 0) {
            this.buildTool();
        }
        return stack;
    }

    public void setToolName(String name) {
        this.toolName = name;
        this.buildTool();
        this.setChanged();
    }

    /** buildTool: the result of the inputs (server only, the client shows what the server sends). */
    public void buildTool() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        ItemStack tool = ToolBuilder.buildTool(this.items.get(1), this.items.get(2), this.items.get(3),
                this.forge ? this.items.get(4) : ItemStack.EMPTY, this.toolName, this.level.registryAccess());
        this.items.set(0, tool == null ? ItemStack.EMPTY : tool);
    }
}
