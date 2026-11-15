package invtweaks.client;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.Equippable;

/**
 * InvTweaksHandlerSorting: the sorting algorithm of 1.54b, unchanged. Rules are applied by priority, locked and
 * frozen slots are kept, the rest is sorted by the tree order. Chests get line rules computed from their content
 * (algorithm 1 = columns, 2 = rows), algorithm 0 is the plain default sorting, 3 the player inventory.
 */
public final class InvTweaksHandlerSorting {
    private static final int MAX_CONTAINER_SIZE = 999;
    private static final int[] DEFAULT_LOCK_PRIORITIES = new int[MAX_CONTAINER_SIZE];
    private static final boolean[] DEFAULT_FROZEN_SLOTS = new boolean[MAX_CONTAINER_SIZE];
    public static final int ALGORITHM_DEFAULT = 0;
    public static final int ALGORITHM_VERTICAL = 1;
    public static final int ALGORITHM_HORIZONTAL = 2;
    public static final int ALGORITHM_INVENTORY = 3;
    public static final int ALGORITHM_EVEN_STACKS = 4;

    private final Minecraft mc;
    private final InvTweaksContainerSectionManager containerMgr;
    private final int algorithm;
    private final int size;
    private final boolean sortArmorParts;
    private final InvTweaksItemTree tree;
    private List<InvTweaksConfigSortingRule> rules;
    private final int[] rulePriority;
    private final int[] keywordOrder;
    private final int[] lockPriorities;
    private final boolean[] frozenSlots;

    public InvTweaksHandlerSorting(Minecraft mc, InvTweaksConfig config, ContainerSection section, int algorithm, int rowSize) throws Exception {
        this.mc = mc;
        this.containerMgr = new InvTweaksContainerSectionManager(mc, section);
        this.size = this.containerMgr.getSize();
        this.sortArmorParts = config.getProperty(InvTweaksConfig.PROP_ENABLE_AUTO_EQUIP_ARMOR).equals(InvTweaksConfig.VALUE_TRUE)
                && !(mc.gui.screen() instanceof CreativeModeInventoryScreen);
        this.rules = config.getRules();
        this.tree = config.getTree();
        if (section == ContainerSection.INVENTORY) {
            this.lockPriorities = config.getLockPriorities();
            this.frozenSlots = config.getFrozenSlots();
            this.algorithm = ALGORITHM_INVENTORY;
        } else {
            this.lockPriorities = DEFAULT_LOCK_PRIORITIES;
            this.frozenSlots = DEFAULT_FROZEN_SLOTS;
            this.algorithm = algorithm;
            if (algorithm != ALGORITHM_DEFAULT) {
                this.computeLineSortingRules(rowSize, algorithm == ALGORITHM_HORIZONTAL);
            }
        }
        this.rulePriority = new int[this.size];
        this.keywordOrder = new int[this.size];
        for (int i = 0; i < this.size; i++) {
            this.rulePriority[i] = -1;
            ItemStack stack = this.containerMgr.getItemStack(i);
            this.keywordOrder[i] = stack != null ? this.getItemOrder(stack) : -1;
        }
    }

    public void sort() {
        long start = System.nanoTime();
        InvTweaksContainerManager globalContainer = this.containerMgr.getContainerManager();
        if (globalContainer.getHeldStack() != null) {
            int empty = globalContainer.getFirstEmptyIndex(ContainerSection.INVENTORY);
            if (empty == -1) {
                return;
            }
            globalContainer.putHoldItemDown(ContainerSection.INVENTORY, empty);
        }
        if (this.algorithm != ALGORITHM_DEFAULT) {
            if (this.algorithm == ALGORITHM_EVEN_STACKS) {
                this.sortEvenStacks();
            } else if (this.algorithm == ALGORITHM_INVENTORY) {
                this.sortInventory(globalContainer);
            }
            this.sortWithRules();
        }
        this.defaultSorting();
        if (InvTweaks.debug) {
            InvTweaks.log.info("Sorting done in {}ns", System.nanoTime() - start);
        }
        if (globalContainer.getHeldStack() != null) {
            int empty = globalContainer.getFirstEmptyIndex(ContainerSection.INVENTORY);
            if (empty != -1) {
                globalContainer.putHoldItemDown(ContainerSection.INVENTORY, empty);
            }
        }
    }

    private void sortWithRules() {
        for (InvTweaksConfigSortingRule rule : this.rules) {
            int priority = rule.getPriority();
            if (InvTweaks.debug) {
                InvTweaks.log.info("Rule : {}({})", rule.getKeyword(), priority);
            }
            for (int i = 0; i < this.size; i++) {
                ItemStack stack = this.containerMgr.getItemStack(i);
                if (this.hasToBeMoved(i) && this.lockPriorities[i] < priority) {
                    List<InvTweaksItemTreeItem> items = this.tree.getItems(stack);
                    if (this.tree.matches(items, rule.getKeyword())) {
                        int[] preferredSlots = rule.getPreferredSlots();
                        int from = i;
                        for (int j = 0; j < preferredSlots.length; j++) {
                            int target = preferredSlots[j];
                            int moved = this.move(from, target, priority);
                            if (moved != -1) {
                                if (moved == target) {
                                    break;
                                }
                                stack = this.containerMgr.getItemStack(moved);
                                items = this.tree.getItems(stack);
                                if (!this.tree.matches(items, rule.getKeyword())) {
                                    break;
                                }
                                from = moved;
                                j = -1;
                            }
                        }
                    }
                }
            }
        }
        for (int i = 0; i < this.size; i++) {
            if (this.hasToBeMoved(i) && this.lockPriorities[i] > 0) {
                this.markAsMoved(i, 1);
            }
        }
    }

    private void sortInventory(InvTweaksContainerManager container) {
        if (container.hasSection(ContainerSection.CRAFTING_IN)) {
            List<Slot> slots = container.getSlots(ContainerSection.CRAFTING_IN);
            int empty = container.getFirstEmptyIndex(ContainerSection.INVENTORY);
            if (empty != -1) {
                for (Slot slot : slots) {
                    if (slot.hasItem()) {
                        container.move(ContainerSection.CRAFTING_IN, container.getSlotIndex(slot.index), ContainerSection.INVENTORY, empty);
                        empty = container.getFirstEmptyIndex(ContainerSection.INVENTORY);
                        if (empty == -1) {
                            break;
                        }
                    }
                }
            }
        }
        this.sortMergeArmor(container);
    }

    private void sortMergeArmor(InvTweaksContainerManager container) {
        for (int i = this.size - 1; i >= 0; i--) {
            ItemStack stack = this.containerMgr.getItemStack(i);
            if (stack != null) {
                if (stack.isDamageableItem()) {
                    this.moveArmor(container, i, stack);
                } else {
                    this.mergeItem(i, stack);
                }
            }
        }
    }

    private void mergeItem(int from, ItemStack stack) {
        for (int i = 0; i < this.lockPriorities.length; i++) {
            if (this.lockPriorities[i] > 0) {
                ItemStack other = this.containerMgr.getItemStack(i);
                if (other != null && InvTweaksContainerManager.areItemsStackable(stack, other)) {
                    this.move(from, i, Integer.MAX_VALUE);
                    this.markAsNotMoved(i);
                    if (this.containerMgr.getItemStack(from) == null) {
                        break;
                    }
                }
            }
        }
    }

    /** enableAutoEquipArmor: put on better armour while sorting. */
    private void moveArmor(InvTweaksContainerManager container, int from, ItemStack stack) {
        if (this.sortArmorParts && isItemArmor(stack) && container.hasSection(ContainerSection.ARMOR)) {
            for (Slot slot : container.getSlots(ContainerSection.ARMOR)) {
                boolean better = false;
                if (!slot.hasItem()) {
                    better = true;
                } else {
                    ItemStack worn = slot.getItem();
                    if (isItemArmor(worn)) {
                        double wornLevel = getArmorLevel(worn);
                        double level = getArmorLevel(stack);
                        if (wornLevel < level || wornLevel == level
                                && InvTweaksContainerManager.getItemDamage(worn) < InvTweaksContainerManager.getItemDamage(stack)) {
                            better = true;
                        }
                    } else {
                        better = true;
                    }
                }
                if (slot.mayPlace(stack) && better) {
                    container.move(ContainerSection.INVENTORY, from, ContainerSection.ARMOR, container.getSlotIndex(slot.index));
                }
            }
        }
    }

    private void sortEvenStacks() {
        List<ItemStack> keys = new ArrayList<>();
        List<int[]> stats = new ArrayList<>();
        for (int i = 0; i < this.size; i++) {
            ItemStack stack = this.containerMgr.getItemStack(i);
            if (stack != null) {
                int index = indexOfSame(keys, stack);
                if (index == -1) {
                    keys.add(stack.copy());
                    stats.add(new int[]{stack.getCount(), 1});
                } else {
                    stats.get(index)[0] += stack.getCount();
                    stats.get(index)[1]++;
                }
            }
        }
        for (int k = 0; k < keys.size(); k++) {
            ItemStack key = keys.get(k);
            int[] stat = stats.get(k);
            int average = stat[0] / stat[1];
            if (average <= key.getMaxStackSize()) {
                LinkedList<Integer> smaller = new LinkedList<>();
                LinkedList<Integer> bigger = new LinkedList<>();
                for (int i = 0; i < this.size; i++) {
                    ItemStack stack = this.containerMgr.getItemStack(i);
                    if (stack != null && ItemStack.isSameItemSameComponents(stack, key)) {
                        int count = stack.getCount();
                        if (count > average) {
                            bigger.offer(i);
                        } else if (count < average) {
                            smaller.offer(i);
                        }
                    }
                }
                while (!smaller.isEmpty() && !bigger.isEmpty()) {
                    int big = bigger.peek();
                    int bigCount = this.containerMgr.getItemStack(big).getCount();
                    int small = smaller.peek();
                    int smallCount = this.containerMgr.getItemStack(small) == null ? 0 : this.containerMgr.getItemStack(small).getCount();
                    this.containerMgr.moveSome(big, small, Math.min(average - smallCount, bigCount - average));
                    bigCount = this.containerMgr.getItemStack(big) == null ? 0 : this.containerMgr.getItemStack(big).getCount();
                    smallCount = this.containerMgr.getItemStack(small) == null ? 0 : this.containerMgr.getItemStack(small).getCount();
                    if (bigCount == average) {
                        bigger.remove();
                    }
                    if (smallCount == average) {
                        smaller.remove();
                    }
                }
                while (bigger.size() > 1) {
                    int big = bigger.poll();
                    int bigCount = this.containerMgr.getItemStack(big).getCount();
                    this.containerMgr.moveSome(big, bigger.peek(), bigCount - average);
                }
            }
        }
        for (int i = 0; i < this.size; i++) {
            this.markAsMoved(i, 1);
        }
    }

    private static int indexOfSame(List<ItemStack> keys, ItemStack stack) {
        for (int i = 0; i < keys.size(); i++) {
            if (ItemStack.isSameItemSameComponents(keys.get(i), stack)) {
                return i;
            }
        }
        return -1;
    }

    private void defaultSorting() {
        List<Integer> remaining = new ArrayList<>();
        List<Integer> next = new ArrayList<>();
        for (int i = 0; i < this.size; i++) {
            if (this.hasToBeMoved(i)) {
                remaining.add(i);
                next.add(i);
            }
        }
        int iterations = 0;
        while (!remaining.isEmpty() && iterations++ < 50) {
            for (int i : remaining) {
                if (this.hasToBeMoved(i)) {
                    for (int j = 0; j < this.size; j++) {
                        if (this.move(i, j, 1) != -1) {
                            next.remove(Integer.valueOf(j));
                            break;
                        }
                    }
                } else {
                    next.remove(Integer.valueOf(i));
                }
            }
            remaining.clear();
            remaining.addAll(next);
        }
        if (iterations >= 50) {
            InvTweaks.log.warn("Sorting takes too long, aborting.");
        }
    }

    private boolean canMergeStacks(ItemStack a, ItemStack b) {
        if (InvTweaksContainerManager.areItemsStackable(a, b)) {
            if (a.getCount() > a.getMaxStackSize()) {
                return false;
            }
            return b.getCount() < b.getMaxStackSize();
        }
        return false;
    }

    private boolean canSwapSlots(int i, int j, int priority) {
        return this.lockPriorities[j] <= priority
                && (this.rulePriority[j] < priority || this.rulePriority[j] == priority && this.isOrderedBefore(i, j));
    }

    /** Moves the stack of slot i to slot j, returns where the stack of i is now (or where j's stack went), -1 if not moved. */
    private int move(int i, int j, int priority) {
        ItemStack from = this.containerMgr.getItemStack(i);
        ItemStack to = this.containerMgr.getItemStack(j);
        if (from == null || this.frozenSlots[j] || this.frozenSlots[i]) {
            return -1;
        }
        if (this.lockPriorities[i] <= priority) {
            if (i == j) {
                this.markAsMoved(i, priority);
                return j;
            }
            if (to == null && this.lockPriorities[j] <= priority && !this.frozenSlots[j]) {
                this.rulePriority[i] = -1;
                this.keywordOrder[i] = -1;
                this.rulePriority[j] = priority;
                this.keywordOrder[j] = this.getItemOrder(from);
                this.containerMgr.move(i, j);
                return j;
            }
            if (to != null && (this.canSwapSlots(i, j, priority) || this.canMergeStacks(from, to))) {
                this.keywordOrder[j] = this.keywordOrder[i];
                this.rulePriority[j] = priority;
                this.rulePriority[i] = -1;
                this.containerMgr.move(i, j);
                ItemStack remains = this.containerMgr.getItemStack(i);
                if (remains == null) {
                    return j;
                }
                int target = i;
                if (this.lockPriorities[j] > this.lockPriorities[i]) {
                    for (int k = 0; k < this.size; k++) {
                        if (this.containerMgr.getItemStack(k) == null && this.lockPriorities[k] == 0) {
                            target = k;
                            break;
                        }
                    }
                }
                if (target != i) {
                    this.containerMgr.move(i, target);
                }
                this.rulePriority[target] = -1;
                this.keywordOrder[target] = this.getItemOrder(remains);
                return target;
            }
        }
        return -1;
    }

    private void markAsMoved(int i, int priority) {
        this.rulePriority[i] = priority;
    }

    private void markAsNotMoved(int i) {
        this.rulePriority[i] = -1;
    }

    private boolean hasToBeMoved(int i) {
        return this.containerMgr.getItemStack(i) != null && this.rulePriority[i] == -1;
    }

    private boolean isOrderedBefore(int i, int j) {
        ItemStack a = this.containerMgr.getItemStack(i);
        ItemStack b = this.containerMgr.getItemStack(j);
        if (b == null) {
            return true;
        }
        if (a == null || this.keywordOrder[i] == -1) {
            return false;
        }
        if (this.keywordOrder[i] != this.keywordOrder[j]) {
            return this.keywordOrder[i] < this.keywordOrder[j];
        }
        int idA = BuiltInRegistries.ITEM.getId(a.getItem());
        int idB = BuiltInRegistries.ITEM.getId(b.getItem());
        if (idA != idB) {
            return idA > idB;
        }
        boolean namedA = a.has(DataComponents.CUSTOM_NAME);
        boolean namedB = b.has(DataComponents.CUSTOM_NAME);
        if (namedA || namedB) {
            if (!namedA) {
                return false;
            }
            if (!namedB) {
                return true;
            }
            String nameA = a.getHoverName().getString();
            String nameB = b.getHoverName().getString();
            if (!nameA.equals(nameB)) {
                return nameA.compareTo(nameB) < 0;
            }
        }
        ItemEnchantments enchantsA = EnchantmentHelper.getEnchantmentsForCrafting(a);
        ItemEnchantments enchantsB = EnchantmentHelper.getEnchantmentsForCrafting(b);
        if (enchantsA.size() == enchantsB.size()) {
            int[] maxA = this.highestEnchantment(enchantsA);
            int[] maxB = this.highestEnchantment(enchantsB);
            if (maxA[0] != maxB[0]) {
                return maxA[0] > maxB[0];
            }
            if (maxA[1] != maxB[1]) {
                return maxA[1] > maxB[1];
            }
            int damageA = InvTweaksContainerManager.getItemDamage(a);
            int damageB = InvTweaksContainerManager.getItemDamage(b);
            if (damageA == damageB) {
                return a.getCount() > b.getCount();
            }
            return a.isDamageableItem() ? damageA > damageB : damageA < damageB;
        }
        return enchantsA.size() > enchantsB.size();
    }

    /** {id, level} of the highest enchantment (by level, then by id), ids are the registry numbers. */
    private int[] highestEnchantment(ItemEnchantments enchantments) {
        int id = 0;
        int level = 0;
        Registry<Enchantment> registry = this.mc.level != null ? this.mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT) : null;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            int entryId = registry != null ? registry.getId(entry.getKey().value()) : 0;
            int entryLevel = entry.getIntValue();
            if (entryLevel > level) {
                id = entryId;
                level = entryLevel;
            } else if (entryLevel == level && entryId > id) {
                id = entryId;
            }
        }
        return new int[]{id, level};
    }

    private int getItemOrder(ItemStack stack) {
        List<InvTweaksItemTreeItem> items = this.tree.getItems(stack);
        return items != null && !items.isEmpty() ? items.get(0).getOrder() : Integer.MAX_VALUE;
    }

    private void computeLineSortingRules(int rowSize, boolean horizontal) {
        this.rules = new ArrayList<>();
        Map<InvTweaksItemTreeItem, Integer> stats = this.computeContainerStats();
        List<InvTweaksItemTreeItem> itemOrder = new ArrayList<>();
        int distinctItems = stats.size();
        int columnSize = this.getContainerColumnSize(rowSize);
        int availableSlots = this.size;
        int remainingStacks = 0;
        for (int count : stats.values()) {
            remainingStacks += count;
        }
        if (distinctItems == 0) {
            return;
        }
        List<InvTweaksItemTreeItem> unorderedItems = new ArrayList<>(stats.keySet());
        boolean hasStacksToOrderFirst = true;
        while (hasStacksToOrderFirst) {
            hasStacksToOrderFirst = false;
            for (InvTweaksItemTreeItem item : unorderedItems) {
                int count = stats.get(item);
                if (count > (horizontal ? rowSize : columnSize) && !itemOrder.contains(item)) {
                    hasStacksToOrderFirst = true;
                    itemOrder.add(item);
                    unorderedItems.remove(item);
                    break;
                }
            }
        }
        unorderedItems.sort(Comparator.comparingInt(InvTweaksItemTreeItem::getOrder));
        itemOrder.addAll(unorderedItems);
        int rowsPerItem;
        int columnsPerItem;
        if (horizontal) {
            rowsPerItem = 1;
            columnsPerItem = rowSize / ((distinctItems + columnSize - 1) / columnSize);
        } else {
            columnsPerItem = 1;
            rowsPerItem = columnSize / ((distinctItems + rowSize - 1) / rowSize);
        }
        char row = 'a';
        char maxRow = (char) (row - 1 + columnSize);
        char column = '1';
        char maxColumn = (char) (column - 1 + rowSize);
        for (InvTweaksItemTreeItem item : itemOrder) {
            int thisItemColumns = columnsPerItem;
            int thisItemRows = rowsPerItem;
            while (stats.get(item) > thisItemRows * thisItemColumns) {
                if (horizontal) {
                    if (column + thisItemColumns < maxColumn) {
                        thisItemColumns = maxColumn - column + 1;
                    } else {
                        if (row + thisItemRows >= maxRow) {
                            break;
                        }
                        thisItemRows++;
                    }
                } else if (row + thisItemRows < maxRow) {
                    thisItemRows = maxRow - row + 1;
                } else {
                    if (column + thisItemColumns >= maxColumn) {
                        break;
                    }
                    thisItemColumns++;
                }
            }
            if (horizontal && column + thisItemColumns == maxColumn) {
                thisItemColumns++;
            } else if (!horizontal && row + thisItemRows == maxRow) {
                thisItemRows++;
            }
            String constraint = row + "" + column + "-" + (char) (row - 1 + thisItemRows) + (char) (column - 1 + thisItemColumns);
            if (!horizontal) {
                constraint = constraint + 'v';
            }
            this.rules.add(new InvTweaksConfigSortingRule(this.tree, constraint, item.getName(), this.size, rowSize));
            availableSlots -= thisItemRows * thisItemColumns;
            remainingStacks -= stats.get(item);
            if (availableSlots < remainingStacks) {
                break;
            }
            if (horizontal) {
                if (column + thisItemColumns + columnsPerItem <= maxColumn + 1) {
                    column = (char) (column + thisItemColumns);
                } else {
                    column = '1';
                    row = (char) (row + thisItemRows);
                }
            } else if (row + thisItemRows + rowsPerItem <= maxRow + 1) {
                row = (char) (row + thisItemRows);
            } else {
                row = 'a';
                column = (char) (column + thisItemColumns);
            }
            if (row > maxRow || column > maxColumn) {
                break;
            }
        }
        String defaultRule;
        if (horizontal) {
            defaultRule = maxRow + "1-a" + maxColumn;
        } else {
            defaultRule = "a" + maxColumn + "-" + maxRow + "1v";
        }
        this.rules.add(new InvTweaksConfigSortingRule(this.tree, defaultRule, this.tree.getRootCategory().getName(), this.size, rowSize));
    }

    private Map<InvTweaksItemTreeItem, Integer> computeContainerStats() {
        Map<InvTweaksItemTreeItem, Integer> stats = new LinkedHashMap<>();
        Map<List<Object>, InvTweaksItemTreeItem> itemSearch = new LinkedHashMap<>();
        for (int i = 0; i < this.size; i++) {
            ItemStack stack = this.containerMgr.getItemStack(i);
            if (stack != null) {
                List<Object> key = Arrays.asList(stack.getItem(), stack.getMaxStackSize() != 1 ? InvTweaksContainerManager.getItemDamage(stack) : 0,
                        String.valueOf(InvTweaksItemTreeItem.potionOf(stack)));
                InvTweaksItemTreeItem item = itemSearch.get(key);
                if (item == null) {
                    item = this.tree.getItems(stack).get(0);
                    itemSearch.put(key, item);
                    stats.put(item, stats.getOrDefault(item, 0) + 1);
                } else {
                    stats.put(item, stats.get(item) + 1);
                }
            }
        }
        return stats;
    }

    private int getContainerColumnSize(int rowSize) {
        return this.size / rowSize;
    }

    static boolean isItemArmor(ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }

    /** ItemArmor.damageReduceAmount: the armour points of the piece. */
    static double getArmorLevel(ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (equippable == null || modifiers == null) {
            return 0;
        }
        return modifiers.compute(Attributes.ARMOR, 0, equippable.slot());
    }
}
