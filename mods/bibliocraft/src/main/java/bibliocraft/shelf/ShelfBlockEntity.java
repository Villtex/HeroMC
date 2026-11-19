package bibliocraft.shelf;

import bibliocraft.common.BiblioBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;

/**
 * TileEntityGenericShelf: four slots (0, 1 upper shelf, 2, 3 lower shelf) for any item, and "RendersTop": a block
 * stands on top, then the upper centre board is drawn too.
 *
 * <p>The light of the original (BlockGenericShelf.getLightValue: the brightest block in the slots) is kept in the
 * chunk's AuxiliaryLightManager here, set on both sides whenever the contents change (the original relit only on the
 * client, in onDataPacket).
 */
public class ShelfBlockEntity extends BiblioBlockEntity {
    private boolean hasTop;

    public ShelfBlockEntity(BlockPos pos, BlockState state) {
        super(Shelves.SHELF_ENTITY.get(), pos, state, 4);
    }

    public boolean hasTop() {
        return this.hasTop;
    }

    /** setTop: sent to the clients (markBlockForUpdate) when it changes. */
    public void setTop(boolean top) {
        if (this.hasTop != top) {
            this.hasTop = top;
            this.sync();
        }
    }

    /** addStuff: puts the stack into an empty slot. */
    public boolean addStuff(int slot, ItemStack stack) {
        if (this.getItem(slot).isEmpty()) {
            this.setItem(slot, stack);
            return true;
        }
        return false;
    }

    /** removeStuff. */
    public void removeStuff(int slot) {
        if (!this.getItem(slot).isEmpty()) {
            this.setItem(slot, ItemStack.EMPTY);
        }
    }

    /**
     * getLightValue: the highest Block.lightValue of the four stacks. The original looked up any id below 4096, so an
     * item whose id matched a light block's id glowed too; here only block items count (their default state).
     */
    public int lightValue() {
        int light = 0;
        for (ItemStack stack : this.items) {
            if (stack.getItem() instanceof BlockItem bi) {
                light = Math.max(light, bi.getBlock().defaultBlockState().getLightEmission());
            }
        }
        return light;
    }

    @Override
    protected void contentsChanged() {
        this.updateLight();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.updateLight();
    }

    private void updateLight() {
        if (this.level != null) {
            AuxiliaryLightManager lights = this.level.getAuxLightManager(this.worldPosition);
            if (lights != null) {
                lights.setLightAt(this.worldPosition, this.lightValue());
            }
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null) {
            AuxiliaryLightManager lights = this.level.getAuxLightManager(pos);
            if (lights != null) {
                lights.removeLightAt(pos);
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("RendersTop", this.hasTop);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.hasTop = input.getBooleanOr("RendersTop", false);
    }
}
