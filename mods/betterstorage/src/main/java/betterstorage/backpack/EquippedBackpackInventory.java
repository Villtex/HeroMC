package betterstorage.backpack;

import betterstorage.BSUtil;
import betterstorage.block.ContainerWrapper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * InventoryBackpackEquipped: the backpack someone else wears, opened from behind: usable while the carrier lives,
 * still wears it, is less than 1.5 blocks away and the player stands behind it (more than 135 degrees from its
 * front). Opening lifts the carrier's lid.
 */
public class EquippedBackpackInventory extends ContainerWrapper {
    public final LivingEntity carrier;

    public EquippedBackpackInventory(LivingEntity carrier, Container base) {
        super(base);
        this.carrier = carrier;
    }

    @Override
    public boolean stillValid(Player player) {
        Container current = Backpacks.getBackpackItems(this.carrier, player);
        boolean carriesBackpack = current != null && sameInventory(current, this.base);
        double distance = player.distanceTo(this.carrier);
        double direction = BSUtil.angleDifference(this.carrier.yBodyRot + 90.0F, BSUtil.angleBetween(this.carrier, player));
        return this.carrier.isAlive() && carriesBackpack && distance < 1.5 && Math.abs(direction) > 135.0;
    }

    private static boolean sameInventory(Container a, Container b) {
        if (a instanceof ListContainer la && b instanceof ListContainer lb) {
            return la.items == lb.items;
        }
        if (a instanceof ContainerWrapper wa && b instanceof ContainerWrapper wb) {
            return wa.base == wb.base;
        }
        return a == b;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.base instanceof ListContainer) {
            Backpacks.updateHasItems(this.carrier);
        }
    }

    @Override
    public void startOpen(ContainerUser user) {
        BackpackData data = Backpacks.data(this.carrier);
        Backpacks.setOpen(this.carrier, ++data.playersUsing > 0);
    }

    @Override
    public void stopOpen(ContainerUser user) {
        BackpackData data = Backpacks.data(this.carrier);
        Backpacks.setOpen(this.carrier, --data.playersUsing > 0);
    }
}
