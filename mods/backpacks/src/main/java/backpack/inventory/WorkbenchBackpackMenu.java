package backpack.inventory;

import backpack.Backpack;
import backpack.BackpackContainer;
import backpack.BackpackItem;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * ContainerWorkbenchBackpack: result (125, 35), the 3x3 grid at (30, 17), the player inventory (offset 41 when the
 * backpack has storage), then the storage rows at (8, 75) (two rows for the big workbench backpack, none for the
 * small one). Slot numbers as in the original: 0 result, 1-9 grid, 10-36 inventory, 37-45 hotbar, 46.. storage.
 * The grid is kept in the backpack ({@link Backpack#CRAFTING}, the "Crafting" NBT) and is not emptied on closing.
 * Taking the result refills the grid from the storage (SlotCraftingAdvanced).
 */
public class WorkbenchBackpackMenu extends AbstractContainerMenu {
    public static final int RESULT = 0;
    public static final int GRID_START = 1;
    public static final int INV_START = 10;
    public static final int HOTBAR_START = 37;
    public static final int STORAGE_START = 46;

    private final int rows;
    private final Player player;
    private final Container storage;
    private final Grid craftSlots;
    private final ResultContainer resultSlots = new ResultContainer();
    private final BackpackMenu.@Nullable Location location;
    private boolean ready;

    /** The crafting grid, written back into the backpack after every change (InventoryCraftingAdvanced). */
    private static final class Grid extends TransientCraftingContainer {
        private final @Nullable ItemStack backpack;
        private boolean loading = true;

        Grid(AbstractContainerMenu menu, @Nullable ItemStack backpack) {
            super(menu, 3, 3);
            this.backpack = backpack;
            if (backpack != null) {
                var items = BackpackContainer.load(backpack, Backpack.CRAFTING.get(), 9);
                for (int i = 0; i < 9; i++) {
                    this.setItem(i, items.get(i));
                }
            }
            this.loading = false;
        }

        private void save() {
            if (!this.loading && this.backpack != null) {
                BackpackContainer.save(this.backpack, Backpack.CRAFTING.get(), this.getItems());
            }
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            super.setItem(slot, stack);
            this.save();
        }

        @Override
        public ItemStack removeItem(int slot, int count) {
            ItemStack result = super.removeItem(slot, count);
            this.save();
            return result;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack result = super.removeItemNoUpdate(slot);
            this.save();
            return result;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            this.save();
        }
    }

    /** SlotCraftingAdvanced: after crafting, empty grid slots are refilled with the same item from the storage. */
    private final class CraftSlot extends ResultSlot {
        CraftSlot(Player player, int x, int y) {
            super(player, WorkbenchBackpackMenu.this.craftSlots, WorkbenchBackpackMenu.this.resultSlots, 0, x, y);
        }

        @Override
        public void onTake(Player player, ItemStack carried) {
            Grid grid = WorkbenchBackpackMenu.this.craftSlots;
            Container storage = WorkbenchBackpackMenu.this.storage;
            ItemStack[] recipe = new ItemStack[9];
            for (int i = 0; i < 9; i++) {
                recipe[i] = grid.getItem(i).copy();
            }
            super.onTake(player, carried);
            for (int i = 0; i < 9; i++) {
                if (recipe[i].isEmpty()) {
                    continue;
                }
                ItemStack current = grid.getItem(i);
                if (!current.isEmpty()) {
                    // a container item (bucket) was left behind: swap it with another full one from the storage
                    net.minecraft.world.item.ItemStackTemplate template = recipe[i].getCraftingRemainder();
                    ItemStack remainder = template == null ? ItemStack.EMPTY : template.create();
                    if (!ItemStack.isSameItemSameComponents(current, recipe[i]) && !remainder.isEmpty() && ItemStack.isSameItem(current, remainder)) {
                        int index = findAlternative(storage, recipe[i]);
                        if (index != -1) {
                            ItemStack replace = storage.getItem(index);
                            storage.setItem(index, current);
                            grid.setItem(i, replace);
                        }
                    }
                } else {
                    int index = findAlternative(storage, recipe[i]);
                    if (index != -1) {
                        grid.setItem(i, storage.removeItem(index, 1));
                    }
                }
            }
        }
    }

    private static int findAlternative(Container storage, ItemStack original) {
        for (int i = 0; i < storage.getContainerSize(); i++) {
            ItemStack alternative = storage.getItem(i);
            if (!alternative.isEmpty() && ItemStack.isSameItemSameComponents(original, alternative)) {
                return i;
            }
        }
        return -1;
    }

    /** Client side: {@code rows} from the open packet. */
    public WorkbenchBackpackMenu(int id, Inventory inventory, int rows) {
        this(id, inventory, new SimpleContainer(rows * 9), rows, null);
    }

    public WorkbenchBackpackMenu(int id, Inventory inventory, Container storage, int rows, BackpackMenu.@Nullable Location location) {
        super(Backpack.WORKBENCH_BACKPACK_MENU.get(), id);
        this.rows = rows;
        this.player = inventory.player;
        this.storage = storage;
        this.location = location;
        this.craftSlots = new Grid(this, location != null ? location.stack() : null);
        storage.startOpen(inventory.player);
        int offset = rows == 0 ? 0 : 41;
        this.addSlot(new CraftSlot(inventory.player, 125, 35));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new Slot(this.craftSlots, col + row * 3, 30 + col * 18, 17 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + offset + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142 + offset));
        }
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new BackpackMenu.BackpackSlot(storage, col + row * 9, 8 + col * 18, 75 + row * 18));
            }
        }
        this.ready = true;
        this.slotsChanged(this.craftSlots);
    }

    public int rows() {
        return this.rows;
    }

    /** onCraftMatrixChanged: the matching crafting recipe (server side; the client gets the result slot). */
    @Override
    public void slotsChanged(Container container) {
        if (!this.ready || container != this.craftSlots || !(this.player.level() instanceof ServerLevel level)
                || !(this.player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        CraftingInput input = this.craftSlots.asCraftInput();
        ItemStack result = ItemStack.EMPTY;
        Optional<RecipeHolder<CraftingRecipe>> recipe = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level,
                (RecipeHolder<CraftingRecipe>) null);
        if (recipe.isPresent() && this.resultSlots.setRecipeUsed(serverPlayer, recipe.get())) {
            ItemStack assembled = recipe.get().value().assemble(input);
            if (assembled.isItemEnabled(level.enabledFeatures())) {
                result = assembled;
            }
        }
        this.resultSlots.setItem(0, result);
        this.setRemoteSlot(0, result);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), 0, result));
    }

    @Override
    public boolean stillValid(Player player) {
        return this.location == null || this.location.valid();
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput input, Player player) {
        if (this.location != null && this.location.locks(this, slotId, button, input)) {
            return;
        }
        super.clicked(slotId, button, input, player);
    }

    private boolean mergeWithBackpack(ItemStack stack) {
        return this.rows > 0 && !(stack.getItem() instanceof BackpackItem) && this.moveItemStackTo(stack, STORAGE_START, STORAGE_START + this.rows * 9, false);
    }

    /** transferStackInSlot of the original: into the storage first, then hotbar / inventory. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();
        if (index == RESULT) {
            stack.getItem().onCraftedBy(stack, player);
            if (!this.mergeWithBackpack(stack) && !this.moveItemStackTo(stack, HOTBAR_START, STORAGE_START, true)
                    && !this.moveItemStackTo(stack, INV_START, HOTBAR_START, false)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, result);
        } else if (index < INV_START) {
            if (!this.mergeWithBackpack(stack) && !this.moveItemStackTo(stack, HOTBAR_START, STORAGE_START, true)
                    && !this.moveItemStackTo(stack, INV_START, HOTBAR_START, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < HOTBAR_START) {
            if (!this.mergeWithBackpack(stack) && !this.moveItemStackTo(stack, HOTBAR_START, STORAGE_START, true)) {
                return ItemStack.EMPTY;
            }
        } else if (index < STORAGE_START) {
            if (!this.mergeWithBackpack(stack) && !this.moveItemStackTo(stack, INV_START, HOTBAR_START, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, HOTBAR_START, STORAGE_START, true) && !this.moveItemStackTo(stack, INV_START, HOTBAR_START, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        if (index == RESULT) {
            player.drop(stack, false, net.minecraft.util.Prediction.PREDICTED);
        }
        return result;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack carried, Slot target) {
        return target.container != this.resultSlots && super.canTakeItemForPickAll(carried, target);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.storage.stopOpen(player);
    }
}
