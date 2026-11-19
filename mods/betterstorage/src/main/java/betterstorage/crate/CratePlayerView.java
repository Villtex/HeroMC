package betterstorage.crate;

import betterstorage.BSUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * InventoryCratePlayerView: what one player sees of a pile, up to 54 slots filled with random stacks of the contents.
 * Taking and putting changes the pile right away; changes by others (watched) show up in matching or empty slots.
 * {@link #getItem} hands out copies, so the menu's in-place stack changes always come back through {@link #setItem}
 * (the original's slotClick override did the same for merging stacks).
 */
public final class CratePlayerView implements Container, CrateWatcher {
    private static final int INVENTORY_MAX_SIZE = 54;
    public final CratePileData data;
    public final CrateBlockEntity crate;
    private final ItemStack[] tempContents;
    private final Map<ItemKey, int[]> countData = new HashMap<>();
    private boolean ignoreModifiedItems;

    public CratePlayerView(CrateBlockEntity crate, CratePileData data) {
        this.data = data;
        this.crate = crate;
        int size = Math.min(data.getCapacity(), INVENTORY_MAX_SIZE);
        this.tempContents = new ItemStack[size];
        java.util.Arrays.fill(this.tempContents, ItemStack.EMPTY);
        int totalStacks = data.getOccupiedSlots();
        LinkedList<ItemStack> stacks = new LinkedList<>();
        for (ItemStack contentsStack : data.stacks()) {
            stacks.add(contentsStack.copy());
        }
        for (int slot = 0; totalStacks > 0 && slot < size; slot++) {
            int randomStack = BSUtil.RANDOM.nextInt(totalStacks--);
            ListIterator<ItemStack> iter = stacks.listIterator();
            while (iter.hasNext()) {
                ItemStack contentsStack = iter.next();
                int numStacks = ItemKey.calcNumStacks(contentsStack);
                if (randomStack < numStacks) {
                    ItemStack stack = contentsStack.copyWithCount(Math.min(contentsStack.getCount(), contentsStack.getMaxStackSize()));
                    this.getMapData(stack)[0] += stack.getCount();
                    this.tempContents[slot] = stack;
                    contentsStack.setCount(contentsStack.getCount() - stack.getCount());
                    if (contentsStack.getCount() <= 0) {
                        iter.remove();
                    }
                    break;
                }
                randomStack -= numStacks;
            }
        }
    }

    private int[] getMapData(ItemKey item) {
        return this.countData.computeIfAbsent(item, k -> new int[1]);
    }

    private int[] getMapData(ItemStack item) {
        return this.getMapData(new ItemKey(item));
    }

    @Override
    public int getContainerSize() {
        return this.tempContents.length;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.tempContents) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < this.tempContents.length ? this.tempContents[slot].copy() : ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= this.tempContents.length) {
            return;
        }
        ItemStack oldStack = this.tempContents[slot];
        this.ignoreModifiedItems = true;
        if (!oldStack.isEmpty()) {
            this.getMapData(oldStack)[0] -= oldStack.getCount();
            this.data.removeItems(oldStack);
        }
        if (!stack.isEmpty()) {
            stack = stack.copy();
            stack.setCount(Math.min(stack.getCount(), Math.min(this.data.spaceForItem(stack), stack.getMaxStackSize())));
            if (stack.isEmpty()) {
                this.ignoreModifiedItems = false;
                this.tempContents[slot] = ItemStack.EMPTY;
                this.onSlotEmptied(slot);
                return;
            }
            this.getMapData(stack)[0] += stack.getCount();
            this.data.addItems(stack.copy());
        }
        this.ignoreModifiedItems = false;
        this.tempContents[slot] = stack;
        if (stack.isEmpty()) {
            this.onSlotEmptied(slot);
        }
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= this.tempContents.length) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = this.tempContents[slot];
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        amount = Math.min(amount, stack.getCount());
        ItemKey item = new ItemKey(stack);
        this.getMapData(item)[0] -= amount;
        stack.shrink(amount);
        if (stack.isEmpty()) {
            this.tempContents[slot] = ItemStack.EMPTY;
        }
        this.ignoreModifiedItems = true;
        ItemStack result = this.data.removeItems(item, amount);
        this.ignoreModifiedItems = false;
        if (this.tempContents[slot].isEmpty()) {
            this.onSlotEmptied(slot);
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return this.removeItem(slot, Integer.MAX_VALUE);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        BlockPos pos = this.crate.getBlockPos();
        return player.level().getBlockEntity(pos) == this.crate && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64.0
                && this.getContainerSize() <= this.data.getCapacity();
    }

    @Override
    public void startOpen(ContainerUser user) {
        this.data.addWatcher(this);
    }

    @Override
    public void stopOpen(ContainerUser user) {
        this.data.removeWatcher(this);
    }

    @Override
    public void clearContent() {
    }

    @Override
    public void onCrateItemsModified(ItemStack changed, int delta) {
        if (this.ignoreModifiedItems) {
            return;
        }
        ItemKey item = new ItemKey(changed);
        int amount = delta;
        int[] itemData = this.getMapData(item);
        Queue<Integer> emptySlots = new LinkedList<>();
        for (int slot = 0; slot < this.tempContents.length; slot++) {
            ItemStack stack = this.tempContents[slot];
            if (stack.isEmpty()) {
                emptySlots.add(slot);
            } else if (item.matches(stack)) {
                amount -= this.modifyItemsInSlot(slot, stack, itemData, amount);
                if (amount == 0) {
                    return;
                }
            }
        }
        while (amount > 0 && !emptySlots.isEmpty()) {
            amount -= this.setItemsInSlot(emptySlots.poll(), item, itemData, amount);
        }
    }

    /** A slot became empty: if the view shows fewer items than the pile has room for, refill it from the pile. */
    private void onSlotEmptied(int slot) {
        int emptySlots = 0;
        for (ItemStack stack : this.tempContents) {
            if (stack.isEmpty()) {
                emptySlots++;
            }
        }
        if (emptySlots <= this.data.getFreeSlots()) {
            return;
        }
        int size = this.data.getNumItems();
        List<Integer> randomIndexList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            randomIndexList.add(i);
        }
        while (emptySlots > this.data.getFreeSlots() && !randomIndexList.isEmpty()) {
            int randomIndex = BSUtil.RANDOM.nextInt(randomIndexList.size());
            int index = randomIndexList.get(randomIndex);
            ItemStack stack = this.data.getItemStack(index);
            if (stack == null) {
                break;
            }
            ItemKey item = new ItemKey(stack);
            int[] itemData = this.getMapData(item);
            int count = stack.getCount() - itemData[0];
            if (count > 0) {
                this.setItemsInSlot(slot, item, itemData, count);
                break;
            }
            // the original drew again from the same list here (it never removed the index)
            randomIndexList.remove(randomIndex);
        }
    }

    private int modifyItemsInSlot(int slot, ItemStack stack, int[] itemData, int amount) {
        int count = Math.max(-stack.getCount(), Math.min(amount, stack.getMaxStackSize() - stack.getCount()));
        stack.setCount(stack.getCount() + count);
        if (stack.getCount() <= 0) {
            this.tempContents[slot] = ItemStack.EMPTY;
        }
        itemData[0] += count;
        return count;
    }

    private int setItemsInSlot(int slot, ItemKey item, int[] itemData, int maxAmount) {
        int size = Math.min(maxAmount, item.maxStackSize());
        this.tempContents[slot] = item.createStack(size);
        itemData[0] += size;
        return size;
    }
}
