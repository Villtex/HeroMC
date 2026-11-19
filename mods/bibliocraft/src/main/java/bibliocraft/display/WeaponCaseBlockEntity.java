package bibliocraft.display;

import bibliocraft.common.BiblioBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileEntityWeaponCase: one slot (stack limit 64, anything goes), the lid state ("LidOpen", animateLid) and whether a
 * block of redstone lies inside (hasRS, worked out again on every change; on a change the case and its six neighbours
 * are told). The angle 0..7 is the block state property {@link WeaponCaseBlock#ANGLE}.
 */
public class WeaponCaseBlockEntity extends BiblioBlockEntity {
    private boolean lidOpen;
    private boolean hasRedstone;

    public WeaponCaseBlockEntity(BlockPos pos, BlockState state) {
        super(Displays.WEAPON_CASE_ENTITY.get(), pos, state, 1);
    }

    /** animateLid: true = the lid is open (items go in and out only then). */
    public boolean isLidOpen() {
        return this.lidOpen;
    }

    /** setAnimate: open or close the lid and send it to the clients. */
    public void setLidOpen(boolean open) {
        this.lidOpen = open;
        this.sync();
    }

    /** hasRedStone: a block of redstone (block id 152) is in the case. */
    public boolean hasRedstone() {
        return this.hasRedstone;
    }

    public boolean isSlotFull() {
        return !this.items.get(0).isEmpty();
    }

    public ItemStack getCaseStack() {
        return this.items.get(0);
    }

    /** setCaseSlot: puts the stack in if the slot is empty (true); EMPTY empties a full slot (false). */
    public boolean setCaseSlot(ItemStack stack) {
        if (stack.isEmpty()) {
            if (this.isSlotFull()) {
                this.setItem(0, ItemStack.EMPTY);
            }
            return false;
        }
        if (!this.isSlotFull()) {
            this.setItem(0, stack);
            return true;
        }
        return false;
    }

    @Override
    protected void contentsChanged() {
        boolean before = this.hasRedstone;
        this.hasRedstone = this.items.get(0).is(Items.REDSTONE_BLOCK);
        if (this.level != null) {
            DisplayLight.set(this.level, this.worldPosition, DisplayLight.of(this.items.get(0)));
            if (before != this.hasRedstone && !this.level.isClientSide()) {
                this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock(), null);
                for (Direction d : Direction.values()) {
                    this.level.updateNeighborsAt(this.worldPosition.relative(d), this.getBlockState().getBlock(), null);
                }
            }
        }
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("LidOpen", this.lidOpen);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        this.lidOpen = input.getBooleanOr("LidOpen", false);
        super.loadAdditional(input);
    }
}
