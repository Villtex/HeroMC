package harkenscythe.altar;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

/**
 * ContainerHSSoulAltar / ContainerHSBloodAltar: the altar's slot (25, 47) and the player inventory. The server works
 * out what the GUI of 1.5.2 worked out on the client (the essence of the crucibles, the recipe of the item and its
 * cost) and sends it as data; the button of the GUI asks the server to do the recipe (the HSPacket of 1.5.2).
 */
public class AltarMenu extends AbstractContainerMenu {
    public static final int DATA_ESSENCE = 0;
    public static final int DATA_ACTION = 1;
    public static final int DATA_COST = 2;
    private final Container altar;
    private final ContainerLevelAccess access;
    private final ContainerData data;
    private final boolean blood;

    /** Client. */
    public static AltarMenu fromNetwork(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        buf.readBlockPos();
        return new AltarMenu(id, inventory, new SimpleContainer(1), ContainerLevelAccess.NULL, buf.readBoolean());
    }

    /** Server. */
    public AltarMenu(int id, Inventory inventory, BlockPos pos, boolean blood) {
        this(id, inventory, inventory.player.level().getBlockEntity(pos) instanceof AltarBlockEntity altar ? altar : new SimpleContainer(1),
                ContainerLevelAccess.create(inventory.player.level(), pos), blood);
    }

    private AltarMenu(int id, Inventory inventory, Container altar, ContainerLevelAccess access, boolean blood) {
        super(Altars.ALTAR_MENU.get(), id);
        this.altar = altar;
        this.access = access;
        this.blood = blood;
        this.data = new SimpleContainerData(3);
        this.addSlot(new Slot(altar, 0, 25, 47));
        this.addStandardInventorySlots(inventory, 8, 84);
        this.addDataSlots(this.data);
    }

    public boolean isBlood() {
        return this.blood;
    }

    /** soulCountTotal / bloodCountTotal. */
    public int essence() {
        return this.data.get(DATA_ESSENCE);
    }

    /** The button of the item's recipe, or null. */
    public AltarRecipe.@Nullable Action action() {
        int a = this.data.get(DATA_ACTION);
        return a <= 0 || a > AltarRecipe.Action.values().length ? null : AltarRecipe.Action.values()[a - 1];
    }

    /** SARL[1] or SAAL[1] times the stack size. */
    public int cost() {
        return this.data.get(DATA_COST);
    }

    private @Nullable AltarRecipe recipe(ServerLevel level, AltarBlock block, ItemStack stack) {
        return AltarRecipe.find(level.recipeAccess().recipeMap().byType(Altars.ALTAR_TYPE.get()), block.kind(), stack);
    }

    @Override
    public void broadcastChanges() {
        this.access.execute((level, pos) -> {
            if (level instanceof ServerLevel server && level.getBlockState(pos).getBlock() instanceof AltarBlock block) {
                ItemStack stack = this.altar.getItem(0);
                AltarRecipe recipe = stack.isEmpty() ? null : this.recipe(server, block, stack);
                this.data.set(DATA_ESSENCE, block.essence(level, pos));
                this.data.set(DATA_ACTION, recipe == null ? 0 : recipe.action().ordinal() + 1);
                this.data.set(DATA_COST, recipe == null ? 0 : recipe.totalCost(stack));
            }
        });
        super.broadcastChanges();
    }

    /**
     * The button (actionPerformed and TileEntityHSSoulAltar.updateEntity): if the crucibles hold the cost (counted in
     * whole crucible levels, cost / 5 or / 10 as in 1.5.2), they are drained and the slot gets the result.
     */
    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId != 0) {
            return false;
        }
        this.access.execute((level, pos) -> {
            if (!(level instanceof ServerLevel server) || !(level.getBlockState(pos).getBlock() instanceof AltarBlock block)) {
                return;
            }
            ItemStack stack = this.altar.getItem(0);
            AltarRecipe recipe = stack.isEmpty() ? null : this.recipe(server, block, stack);
            if (recipe == null) {
                return;
            }
            int unit = block.kind().unit;
            int levels = recipe.totalCost(stack) / unit;
            if (levels > block.essence(level, pos) / unit) {
                return;
            }
            block.drain(level, pos, levels);
            this.altar.setItem(0, recipe.apply(stack, level.getRandom(), level.registryAccess()));
            this.broadcastChanges();
        });
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        Block[] block = {null};
        this.access.execute((level, pos) -> block[0] = level.getBlockState(pos).getBlock());
        return block[0] instanceof AltarBlock && stillValid(this.access, player, block[0]);
    }

    /** onCraftGuiClosed: the item of the slot goes back to the player. */
    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.altar));
    }

    /** transferStackInSlot: the altar slot to the inventory; an inventory stack (all of it) into the empty altar slot. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index == 0) {
            if (!this.moveItemStackTo(stack, 1, 37, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            Slot altarSlot = this.slots.get(0);
            if (altarSlot.hasItem() || !altarSlot.mayPlace(stack)) {
                return ItemStack.EMPTY;
            }
            altarSlot.setByPlayer(stack.copy());
            stack.setCount(0);
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return copy;
    }
}
