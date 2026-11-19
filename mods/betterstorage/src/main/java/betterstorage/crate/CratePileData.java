package betterstorage.crate;

import betterstorage.BSUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * CratePileData: the shared inventory of a crate pile. It holds items, not slots: every kind of item once with its
 * total count (any size, even beyond a stack), 18 slots of room per crate. The players' views
 * ({@link CratePlayerView}) show a random selection and watch it for changes.
 */
public final class CratePileData {
    private static final int MAX_CRATE_PILE_SIZE = 8192;
    private record SavedStack(ItemStack item, int count) {
        static final Codec<SavedStack> CODEC = RecordCodecBuilder.create(i -> i.group(
                ItemStack.CODEC.fieldOf("item").forGetter(SavedStack::item),
                Codec.INT.fieldOf("Count").forGetter(SavedStack::count)).apply(i, SavedStack::new));
    }
    static final Codec<CratePileData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("id").forGetter(d -> d.id),
            Codec.INT.fieldOf("numCrates").forGetter(d -> d.numCrates),
            SavedStack.CODEC.listOf().fieldOf("stacks").forGetter(d -> d.contents.values().stream()
                    .map(s -> new SavedStack(s.copyWithCount(1), s.getCount())).toList()),
            CratePileMap.CODEC.optionalFieldOf("map").forGetter(d -> Optional.ofNullable(d.map))).apply(i, CratePileData::fromSaved));

    public @Nullable CratePileCollection collection;
    public final int id;
    private final Map<ItemKey, ItemStack> contents = new LinkedHashMap<>();
    private ItemStack[] contentsArray = new ItemStack[0];
    private int numCrates;
    private int numSlots;
    private boolean destroyed;
    private @Nullable CratePileMap map;
    private final Set<CrateWatcher> watchers = new HashSet<>();

    public CratePileData(@Nullable CratePileCollection collection, int id, int numCrates) {
        this.collection = collection;
        this.id = id;
        this.numCrates = numCrates;
    }

    private static CratePileData fromSaved(int id, int numCrates, List<SavedStack> stacks, Optional<CratePileMap> map) {
        CratePileData data = new CratePileData(null, id, numCrates);
        for (SavedStack s : stacks) {
            if (!s.item().isEmpty() && s.count() > 0) {
                data.addItems(s.item().copyWithCount(s.count()), false);
            }
        }
        data.map = map.orElse(null);
        return data;
    }

    public int getNumCrates() {
        return this.numCrates;
    }

    public List<ItemStack> getContents() {
        return List.of(this.contentsArray);
    }

    public Collection<ItemStack> stacks() {
        return this.contents.values();
    }

    public int getCapacity() {
        return this.numCrates * 18;
    }

    public int getNumItems() {
        return this.contents.size();
    }

    public int getOccupiedSlots() {
        return this.numSlots;
    }

    public int getFreeSlots() {
        return this.getCapacity() - this.getOccupiedSlots();
    }

    /** Within the pile's box, or growing it the way the original allowed (keeps piles roughly box-shaped). */
    public boolean canAdd(BlockPos crate) {
        return this.map != null && this.numCrates < MAX_CRATE_PILE_SIZE && (this.map.region.contains(crate) || this.canExpand(crate));
    }

    private boolean canExpand(BlockPos crate) {
        Region region = this.map.region;
        int volume = region.volume();
        if (this.numCrates < Math.min((int) (volume * 0.8), volume - 5)) {
            return false;
        }
        if (crate.getX() < region.minX || crate.getX() > region.maxX) {
            int maxDiff = region.height() == 1 ? 1 : 3;
            if (region.width() >= maxDiff + Math.min(region.height(), region.depth())) {
                return false;
            }
        } else if (crate.getZ() < region.minZ || crate.getZ() > region.maxZ) {
            int maxDiff = region.width() == 1 ? 1 : 3;
            if (region.height() >= maxDiff + Math.min(region.width(), region.depth())) {
                return false;
            }
        } else if (crate.getY() < region.minY || crate.getY() > region.maxY) {
            int maxDiff = region.width() != 1 && region.height() != 1 ? 4 : 1;
            if (region.depth() >= maxDiff + Math.min(region.width(), region.height())) {
                return false;
            }
        }
        return true;
    }

    public void trimMap() {
        if (this.map != null) {
            this.map.trim();
        }
    }

    public void addCrate(BlockPos crate) {
        if (this.numCrates == 0) {
            this.map = new CratePileMap(crate);
        }
        this.map.add(crate);
        this.numCrates++;
        this.markDirty();
    }

    public void removeCrate(BlockPos crate) {
        if (--this.numCrates <= 0) {
            if (this.collection != null) {
                this.collection.removeCratePile(this);
            }
            this.destroyed = true;
        } else {
            if (this.map != null) {
                this.map.remove(crate);
            }
            this.markDirty();
        }
    }

    private @Nullable ItemStack getItemStack(ItemKey item, boolean copy) {
        ItemStack stack = this.contents.get(item);
        return stack == null ? null : copy ? stack.copy() : stack;
    }

    public @Nullable ItemStack getItemStack(ItemStack item) {
        return item.isEmpty() ? null : this.getItemStack(new ItemKey(item), true);
    }

    /** The stack at {@code index} of the contents array (the count may be beyond a stack). */
    public @Nullable ItemStack getItemStack(int index) {
        return index >= 0 && index < this.getNumItems() ? this.contentsArray[index] : null;
    }

    public int getItemCount(ItemStack item) {
        ItemStack stack = this.getItemStack(new ItemKey(item), false);
        return stack != null ? stack.getCount() : 0;
    }

    private ItemStack addItems(ItemStack stack, boolean dirty) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack overflow = ItemStack.EMPTY;
        int space = this.spaceForItem(stack);
        if (space > 0) {
            if (space < stack.getCount()) {
                overflow = stack.split(stack.getCount() - space);
            }
            ItemKey item = new ItemKey(stack);
            ItemStack contentsStack = this.getItemStack(item, false);
            int stacksBefore;
            if (contentsStack == null) {
                stacksBefore = 0;
                contentsStack = item.createStack(stack.getCount());
                this.contents.put(item, contentsStack);
                this.updateContentsArray();
            } else {
                stacksBefore = ItemKey.calcNumStacks(contentsStack);
                contentsStack.setCount(contentsStack.getCount() + stack.getCount());
            }
            int stacksAfter = ItemKey.calcNumStacks(contentsStack);
            this.numSlots += stacksAfter - stacksBefore;
            for (CrateWatcher watcher : List.copyOf(this.watchers)) {
                watcher.onCrateItemsModified(stack, stack.getCount());
            }
        } else {
            overflow = stack;
        }
        if (dirty) {
            this.markDirty();
        }
        return overflow;
    }

    /** Adds as much as fits; returns what did not. */
    public ItemStack addItems(ItemStack stack) {
        return this.addItems(stack, true);
    }

    public List<ItemStack> addItems(Collection<ItemStack> stacks) {
        List<ItemStack> overflow = new ArrayList<>();
        for (ItemStack stack : stacks) {
            ItemStack rest = this.addItems(stack);
            if (!rest.isEmpty()) {
                overflow.add(rest);
            }
        }
        return overflow;
    }

    public ItemStack removeItems(ItemKey item, int amount) {
        ItemStack stack = this.getItemStack(item, false);
        if (stack == null || amount <= 0) {
            return ItemStack.EMPTY;
        }
        int stacksBefore = ItemKey.calcNumStacks(stack);
        int stacksAfter;
        if (amount < stack.getCount()) {
            stack.setCount(stack.getCount() - amount);
            stacksAfter = ItemKey.calcNumStacks(stack);
            stack = stack.copyWithCount(amount);
        } else {
            this.contents.remove(item);
            this.updateContentsArray();
            stacksAfter = 0;
        }
        this.numSlots -= stacksBefore - stacksAfter;
        for (CrateWatcher watcher : List.copyOf(this.watchers)) {
            watcher.onCrateItemsModified(stack, -stack.getCount());
        }
        this.markDirty();
        return stack;
    }

    public ItemStack removeItems(ItemStack item, int amount) {
        return item.isEmpty() ? ItemStack.EMPTY : this.removeItems(new ItemKey(item), amount);
    }

    public ItemStack removeItems(ItemStack stack) {
        return this.removeItems(stack, stack.getCount());
    }

    /** Free slots times the stack size, plus what fits onto the item's last stack. */
    public int spaceForItem(ItemStack item) {
        if (item.isEmpty()) {
            return 0;
        }
        int stackLimit = item.getMaxStackSize();
        int space = this.getFreeSlots() * stackLimit;
        ItemStack stack = this.getItemStack(new ItemKey(item), false);
        if (stack != null) {
            space += ItemKey.calcNumStacks(stack) * stackLimit - stack.getCount();
        }
        return space;
    }

    /** pickItemStacks: {@code amount} random full or partial stacks of the contents. */
    public List<ItemStack> pickItemStacks(int amount) {
        amount = Math.min(amount, this.getOccupiedSlots());
        if (amount <= 0) {
            return new ArrayList<>();
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack contentsStack : this.contents.values()) {
            int numStacks = ItemKey.calcNumStacks(contentsStack);
            int maxStackSize = contentsStack.getMaxStackSize();
            for (int i = 0; i < numStacks; i++) {
                int max = Math.min(contentsStack.getCount() - maxStackSize * i, maxStackSize);
                stacks.add(contentsStack.copyWithCount(Math.min(contentsStack.getCount(), max)));
            }
        }
        List<ItemStack> result = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            result.add(stacks.remove(BSUtil.RANDOM.nextInt(stacks.size())));
        }
        return result;
    }

    public List<ItemStack> pickAndRemoveItemStacks(int amount) {
        List<ItemStack> stacks = this.pickItemStacks(amount);
        for (ItemStack stack : stacks) {
            this.removeItems(stack);
        }
        return stacks;
    }

    public void addWatcher(CrateWatcher watcher) {
        this.watchers.add(watcher);
    }

    public void removeWatcher(CrateWatcher watcher) {
        this.watchers.remove(watcher);
    }

    private void updateContentsArray() {
        this.contentsArray = this.contents.values().toArray(new ItemStack[0]);
    }

    private void markDirty() {
        if (!this.destroyed && this.collection != null) {
            this.collection.setDirty();
        }
    }
}
