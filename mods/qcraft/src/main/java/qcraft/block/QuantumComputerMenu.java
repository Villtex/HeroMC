package qcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import qcraft.QuantumCraft;

/**
 * ContainerQuantumComputer: no slots, only the result of {@code canTeleport} (progress bar 0), recomputed every tick
 * on the server. The "Energize" button (QuantumCraftPacket Teleport) is menu button 1.
 */
public class QuantumComputerMenu extends AbstractContainerMenu {
    public static final int BUTTON_ENERGIZE = 1;
    private final BlockPos pos;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    /** Client. */
    public QuantumComputerMenu(int id, Inventory inventory, BlockPos pos) {
        super(QuantumCraft.QUANTUM_COMPUTER_MENU.get(), id);
        this.pos = pos;
        this.access = ContainerLevelAccess.create(inventory.player.level(), pos);
        if (inventory.player.level().isClientSide()) {
            this.data = new SimpleContainerData(1);
            this.data.set(0, -1);
        } else {
            this.data = new ContainerData() {
                @Override
                public int get(int index) {
                    return inventory.player.level().getBlockEntity(pos) instanceof QuantumComputerBlockEntity computer ? computer.canTeleport()
                            : QuantumComputerBlockEntity.FRAME_INCOMPLETE;
                }

                @Override
                public void set(int index, int value) {
                }

                @Override
                public int getCount() {
                    return 1;
                }
            };
        }
        this.addDataSlots(this.data);
    }

    public int getError() {
        return this.data.get(0);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_ENERGIZE && player instanceof ServerPlayer
                && player.level().getBlockEntity(this.pos) instanceof QuantumComputerBlockEntity computer) {
            computer.tryTeleport();
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, QuantumCraft.QUANTUM_COMPUTER.get());
    }
}
