package betterstorage.block;

import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.PlayerEnderChestContainer;

/** BlockEnderBackpack.InventoryEnderBackpack: the player's ender chest, used through the backpack block (its lid moves). */
public class EnderBackpackInventory extends ContainerWrapper {
    private final BackpackBlockEntity backpack;

    public EnderBackpackInventory(PlayerEnderChestContainer base, BackpackBlockEntity backpack) {
        super(base);
        this.backpack = backpack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.backpack.canPlayerUseContainer(player);
    }

    @Override
    public void startOpen(ContainerUser user) {
        this.backpack.onContainerOpened();
    }

    @Override
    public void stopOpen(ContainerUser user) {
        this.backpack.onContainerClosed();
    }
}
