package bibliocraft.decor;

import bibliocraft.common.BiblioBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileEntityTable: one slot (any item, up to 64), the legs leg1..4 / monoleg and the corners top1..4 (true = rounded,
 * false = square, where a neighbour table joins), and the turn of the item on the table (slotxangle, slotyangle 0..3).
 * Corner/leg 1 is at +x -z, 2 at +x +z, 3 at -x +z, 4 at -x -z. A new table stands on the middle leg.
 */
public class TableBlockEntity extends BiblioBlockEntity {
    boolean leg1;
    boolean leg2;
    boolean leg3;
    boolean leg4;
    boolean monoleg = true;
    boolean top1 = true;
    boolean top2 = true;
    boolean top3 = true;
    boolean top4 = true;
    private int slotxangle;
    private int slotyangle;

    public TableBlockEntity(BlockPos pos, BlockState state) {
        super(Decor.TABLE_ENTITY.get(), pos, state, 1);
    }

    public boolean isSlotFull() {
        return !this.items.get(0).isEmpty();
    }

    /** {leg1, leg2, leg3, leg4, monoleg}. */
    public boolean[] legs() {
        return new boolean[] {this.leg1, this.leg2, this.leg3, this.leg4, this.monoleg};
    }

    /** {top1, top2, top3, top4}. */
    public boolean[] tops() {
        return new boolean[] {this.top1, this.top2, this.top3, this.top4};
    }

    public int slotX() {
        return this.slotxangle;
    }

    public int slotY() {
        return this.slotyangle;
    }

    /** setLegs + setTops, then markBlockForUpdate. */
    void setShape(boolean l1, boolean l2, boolean l3, boolean l4, boolean ml, boolean t1, boolean t2, boolean t3, boolean t4) {
        this.leg1 = l1;
        this.leg2 = l2;
        this.leg3 = l3;
        this.leg4 = l4;
        this.monoleg = ml;
        this.top1 = t1;
        this.top2 = t2;
        this.top3 = t3;
        this.top4 = t4;
        this.sync();
    }

    void setSlotX(int angle) {
        this.slotxangle = angle;
        this.sync();
    }

    void setSlotY(int angle) {
        this.slotyangle = angle;
        this.sync();
    }

    /** onDataPacket called updateAllLightTypes: the light of a block on the table changes with the slot. */
    @Override
    protected void changed() {
        super.changed();
        this.relight();
    }

    @Override
    protected void contentsChanged() {
        // the client gets the new slot through the update packet (loadAdditional)
        if (this.level != null && this.level.isClientSide() && this.level.getBlockEntity(this.worldPosition) == this) {
            this.relight();
        }
    }

    private void relight() {
        if (this.level != null) {
            this.level.getLightEngine().checkBlock(this.worldPosition);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("leg1", this.leg1);
        output.putBoolean("leg2", this.leg2);
        output.putBoolean("leg3", this.leg3);
        output.putBoolean("leg4", this.leg4);
        output.putBoolean("top1", this.top1);
        output.putBoolean("top2", this.top2);
        output.putBoolean("top3", this.top3);
        output.putBoolean("top4", this.top4);
        output.putBoolean("monoleg", this.monoleg);
        output.putInt("slotxangle", this.slotxangle);
        output.putInt("slotyangle", this.slotyangle);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        this.leg1 = input.getBooleanOr("leg1", false);
        this.leg2 = input.getBooleanOr("leg2", false);
        this.leg3 = input.getBooleanOr("leg3", false);
        this.leg4 = input.getBooleanOr("leg4", false);
        this.top1 = input.getBooleanOr("top1", true);
        this.top2 = input.getBooleanOr("top2", true);
        this.top3 = input.getBooleanOr("top3", true);
        this.top4 = input.getBooleanOr("top4", true);
        this.monoleg = input.getBooleanOr("monoleg", true);
        this.slotxangle = input.getIntOr("slotxangle", 0);
        this.slotyangle = input.getIntOr("slotyangle", 0);
        super.loadAdditional(input);
    }

    /** isStackValidForSlot: anything. */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return true;
    }
}
