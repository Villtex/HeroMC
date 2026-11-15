package invtweaks.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * InvTweaksContainerManager: the sections of the open container and moves between them, all done with the same
 * slot clicks a player would do ({@code MultiPlayerGameMode.handleContainerInput}, PICKUP), so the server sees normal
 * clicks. The player inventory section is found by the slots' container (the player's Inventory, slots 0-35), not
 * by "the last 36 slots" as in 1.5.2, because 26.3 menus have the offhand slot or a result slot after them.
 */
public final class InvTweaksContainerManager {
    public static final int DROP_SLOT = AbstractContainerMenu.SLOT_CLICKED_OUTSIDE;
    public static final int INVENTORY_SIZE = 36;
    public static final int HOTBAR_SIZE = 9;
    private final Minecraft mc;
    private final AbstractContainerScreen<?> screen;
    private final AbstractContainerMenu menu;
    private final Inventory inventory;
    private final Map<ContainerSection, List<Slot>> slotRefs = new EnumMap<>(ContainerSection.class);

    public InvTweaksContainerManager(Minecraft mc) {
        this.mc = mc;
        Screen current = mc.gui.screen();
        if (current instanceof AbstractContainerScreen<?> containerScreen && !(current instanceof CreativeModeInventoryScreen)) {
            this.screen = containerScreen;
            this.menu = containerScreen.getMenu();
        } else {
            this.screen = null;
            this.menu = mc.player.inventoryMenu;
        }
        this.inventory = mc.player.getInventory();
        List<Slot> slots = this.menu.slots;
        List<Slot> main = new ArrayList<>();
        List<Slot> hotbar = new ArrayList<>();
        List<Slot> others = new ArrayList<>();
        for (Slot slot : slots) {
            if (slot.container == this.inventory && slot.getContainerSlot() >= 0 && slot.getContainerSlot() < INVENTORY_SIZE) {
                (slot.getContainerSlot() < HOTBAR_SIZE ? hotbar : main).add(slot);
            } else if (slot.container != this.inventory) {
                others.add(slot);
            }
        }
        main.sort(Comparator.comparingInt(Slot::getContainerSlot));
        hotbar.sort(Comparator.comparingInt(Slot::getContainerSlot));
        if (this.menu instanceof InventoryMenu) {
            this.slotRefs.put(ContainerSection.CRAFTING_OUT, slots.subList(0, 1));
            this.slotRefs.put(ContainerSection.CRAFTING_IN, slots.subList(1, 5));
            this.slotRefs.put(ContainerSection.ARMOR, slots.subList(5, 9));
        } else if (this.menu instanceof CraftingMenu) {
            this.slotRefs.put(ContainerSection.CRAFTING_OUT, slots.subList(0, 1));
            this.slotRefs.put(ContainerSection.CRAFTING_IN, slots.subList(1, 10));
        } else if (this.menu instanceof AbstractFurnaceMenu) {
            this.slotRefs.put(ContainerSection.FURNACE_IN, slots.subList(0, 1));
            this.slotRefs.put(ContainerSection.FURNACE_FUEL, slots.subList(1, 2));
            this.slotRefs.put(ContainerSection.FURNACE_OUT, slots.subList(2, 3));
        } else if (this.menu instanceof EnchantmentMenu) {
            this.slotRefs.put(ContainerSection.ENCHANTMENT, slots.subList(0, 1));
        } else if (this.menu instanceof BrewingStandMenu) {
            this.slotRefs.put(ContainerSection.BREWING_BOTTLES, slots.subList(0, 3));
            this.slotRefs.put(ContainerSection.BREWING_INGREDIENT, slots.subList(3, 4));
        } else if (!others.isEmpty()) {
            this.slotRefs.put(ContainerSection.CHEST, others);
        }
        if (main.size() == 27 && hotbar.size() == HOTBAR_SIZE) {
            List<Slot> all = new ArrayList<>(main);
            all.addAll(hotbar);
            this.slotRefs.put(ContainerSection.INVENTORY, all);
            this.slotRefs.put(ContainerSection.INVENTORY_NOT_HOTBAR, main);
            this.slotRefs.put(ContainerSection.INVENTORY_HOTBAR, hotbar);
        }
    }

    public boolean move(ContainerSection fromSection, int fromIndex, ContainerSection toSection, int toIndex) {
        ItemStack from = this.getItemStack(fromSection, fromIndex);
        ItemStack to = this.getItemStack(toSection, toIndex);
        if (from == null && toIndex != DROP_SLOT) {
            return false;
        }
        if (fromSection == toSection && fromIndex == toIndex) {
            return true;
        }
        if (toIndex != DROP_SLOT) {
            Slot slot = this.getSlot(toSection, toIndex);
            if (slot == null || !slot.mayPlace(from)) {
                return false;
            }
        }
        if (this.getHeldStack() != null) {
            int empty = this.getFirstEmptyIndex(ContainerSection.INVENTORY);
            if (empty == -1) {
                return false;
            }
            this.leftClick(ContainerSection.INVENTORY, empty);
        }
        if (to != null && (isBundle(from) || isBundle(to))) {
            // a click with a stack on a bundle (or with a bundle on a stack) would put the stack into the bundle
            return this.swap(this.getSlot(fromSection, fromIndex), this.getSlot(toSection, toIndex));
        }
        if (to != null && from.getItem() == to.getItem() && (from.getMaxStackSize() == 1 || hasDataTags(from) || hasDataTags(to))) {
            int emptySlot = this.getFirstEmptyUsableSlotNumber();
            ContainerSection emptySection = this.getSlotSection(emptySlot);
            int emptyIndex = this.getSlotIndex(emptySlot);
            if (emptyIndex == -1) {
                return false;
            }
            if (!this.getSlot(emptySection, emptyIndex).mayPlace(to) || !this.getSlot(fromSection, fromIndex).mayPlace(to)) {
                return false;
            }
            this.leftClick(toSection, toIndex);
            this.leftClick(emptySection, emptyIndex);
            this.leftClick(fromSection, fromIndex);
            this.leftClick(toSection, toIndex);
            this.leftClick(emptySection, emptyIndex);
            this.leftClick(fromSection, fromIndex);
        } else {
            this.leftClick(fromSection, fromIndex);
            this.leftClick(toSection, toIndex);
            ItemStack held = this.getHeldStack();
            if (held != null) {
                Slot slot = this.getSlot(fromSection, fromIndex);
                if (slot.mayPlace(held)) {
                    this.leftClick(fromSection, fromIndex);
                } else {
                    int empty = this.getFirstEmptyIndex(ContainerSection.INVENTORY);
                    if (empty != -1) {
                        this.leftClick(ContainerSection.INVENTORY, empty);
                    }
                }
            }
        }
        return true;
    }

    public boolean moveSome(ContainerSection fromSection, int fromIndex, ContainerSection toSection, int toIndex, int amount) {
        ItemStack from = this.getItemStack(fromSection, fromIndex);
        if (from == null || fromSection == toSection && fromIndex == toIndex) {
            return true;
        }
        if (isBundle(from)) {
            // right clicks with a bundle take items out of it: a bundle is a single item anyway
            return this.move(fromSection, fromIndex, toSection, toIndex);
        }
        int size = from.getCount();
        int count = Math.min(amount, size);
        this.leftClick(fromSection, fromIndex);
        for (int i = 0; i < count; i++) {
            this.rightClick(toSection, toIndex);
        }
        if (count < size) {
            this.leftClick(fromSection, fromIndex);
        }
        return true;
    }

    public boolean drop(ContainerSection section, int index) {
        return this.move(section, index, null, DROP_SLOT);
    }

    public boolean dropSome(ContainerSection section, int index, int amount) {
        return this.moveSome(section, index, null, DROP_SLOT, amount);
    }

    public boolean putHoldItemDown(ContainerSection section, int index) {
        if (this.getHeldStack() != null) {
            if (this.getItemStack(section, index) == null) {
                this.click(section, index, false);
                return true;
            }
            return false;
        }
        return true;
    }

    public void leftClick(ContainerSection section, int index) {
        this.click(section, index, false);
    }

    public void rightClick(ContainerSection section, int index) {
        this.click(section, index, true);
    }

    public void click(ContainerSection section, int index, boolean rightClick) {
        int slot = this.indexToSlot(section, index);
        if (slot != -1) {
            this.mc.gameMode.handleContainerInput(this.menu.containerId, slot, rightClick ? 1 : 0, ContainerInput.PICKUP, this.mc.player);
        }
    }

    /**
     * Exchanges two slots with hotbar swaps (number key clicks) instead of picking items up, which bundles would
     * turn into "put into the bundle".
     */
    private boolean swap(Slot a, Slot b) {
        if (a == null || b == null) {
            return false;
        }
        int hotbarA = this.hotbarIndex(a);
        int hotbarB = this.hotbarIndex(b);
        if (hotbarA != -1) {
            this.swapClick(b, hotbarA);
        } else if (hotbarB != -1) {
            this.swapClick(a, hotbarB);
        } else {
            this.swapClick(a, 0);
            this.swapClick(b, 0);
            this.swapClick(a, 0);
        }
        return true;
    }

    private void swapClick(Slot slot, int hotbar) {
        this.mc.gameMode.handleContainerInput(this.menu.containerId, slot.index, hotbar, ContainerInput.SWAP, this.mc.player);
    }

    private int hotbarIndex(Slot slot) {
        return slot.container == this.inventory && slot.getContainerSlot() >= 0 && slot.getContainerSlot() < HOTBAR_SIZE ? slot.getContainerSlot() : -1;
    }

    public Slot getSlotAtMousePosition() {
        return this.screen != null ? this.screen.getHoveredSlot() : null;
    }

    public boolean hasSection(ContainerSection section) {
        return this.slotRefs.containsKey(section);
    }

    public List<Slot> getSlots(ContainerSection section) {
        return this.slotRefs.get(section);
    }

    public int getSize() {
        int size = 0;
        for (List<Slot> slots : this.slotRefs.values()) {
            size += slots.size();
        }
        return size;
    }

    public int getSize(ContainerSection section) {
        return this.hasSection(section) ? this.slotRefs.get(section).size() : 0;
    }

    public int getFirstEmptyIndex(ContainerSection section) {
        List<Slot> slots = this.slotRefs.get(section);
        if (slots == null) {
            return -1;
        }
        for (int i = 0; i < slots.size(); i++) {
            if (!slots.get(i).hasItem()) {
                return i;
            }
        }
        return -1;
    }

    public boolean isSlotEmpty(ContainerSection section, int index) {
        return this.hasSection(section) && this.getItemStack(section, index) == null;
    }

    public Slot getSlot(ContainerSection section, int index) {
        List<Slot> slots = this.slotRefs.get(section);
        return slots != null && index >= 0 && index < slots.size() ? slots.get(index) : null;
    }

    public int getSlotIndex(int slotNumber) {
        return this.getSlotIndex(slotNumber, false);
    }

    /** Index of a menu slot in its section (not INVENTORY, or with preferInventory not its two halves). */
    public int getSlotIndex(int slotNumber, boolean preferInventory) {
        for (Map.Entry<ContainerSection, List<Slot>> entry : this.slotRefs.entrySet()) {
            ContainerSection section = entry.getKey();
            if (!preferInventory && section != ContainerSection.INVENTORY
                    || preferInventory && section != ContainerSection.INVENTORY_NOT_HOTBAR && section != ContainerSection.INVENTORY_HOTBAR) {
                List<Slot> slots = entry.getValue();
                for (int i = 0; i < slots.size(); i++) {
                    if (slots.get(i).index == slotNumber) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public ContainerSection getSlotSection(int slotNumber) {
        for (Map.Entry<ContainerSection, List<Slot>> entry : this.slotRefs.entrySet()) {
            if (entry.getKey() != ContainerSection.INVENTORY) {
                for (Slot slot : entry.getValue()) {
                    if (slot.index == slotNumber) {
                        return entry.getKey();
                    }
                }
            }
        }
        return null;
    }

    /** The stack in a slot of a section; null for an empty slot (the 1.5.2 code works with null stacks). */
    public ItemStack getItemStack(ContainerSection section, int index) {
        Slot slot = section == null ? null : this.getSlot(section, index);
        if (slot == null) {
            return null;
        }
        ItemStack stack = slot.getItem();
        return stack.isEmpty() ? null : stack;
    }

    public AbstractContainerMenu getContainer() {
        return this.menu;
    }

    public ItemStack getHeldStack() {
        ItemStack carried = this.menu.getCarried();
        return carried.isEmpty() ? null : carried;
    }

    private int getFirstEmptyUsableSlotNumber() {
        for (List<Slot> slots : this.slotRefs.values()) {
            for (Slot slot : slots) {
                if (slot.getClass() == Slot.class && !slot.hasItem()) {
                    return slot.index;
                }
            }
        }
        return -1;
    }

    private int indexToSlot(ContainerSection section, int index) {
        if (index == DROP_SLOT) {
            return DROP_SLOT;
        }
        Slot slot = this.getSlot(section, index);
        return slot != null ? slot.index : -1;
    }

    // --- stack helpers of InvTweaksObfuscation --------------------------------------------------------------------

    public static boolean isBundle(ItemStack stack) {
        return stack != null && stack.has(DataComponents.BUNDLE_CONTENTS);
    }

    public static boolean hasDataTags(ItemStack stack) {
        return !stack.isComponentsPatchEmpty();
    }

    /** getItemDamage: the damage of a damageable item, 0 otherwise (subtypes are separate items now). */
    public static int getItemDamage(ItemStack stack) {
        return stack.isDamageableItem() ? stack.getDamageValue() : 0;
    }

    /** areItemsEqual (isItemEqual: same id and damage). */
    public static boolean areItemsEqual(ItemStack a, ItemStack b) {
        return ItemStack.isSameItem(a, b) && getItemDamage(a) == getItemDamage(b);
    }

    public static boolean areItemStacksEqual(ItemStack a, ItemStack b) {
        return areItemsEqual(a, b) && a.getCount() == b.getCount();
    }

    public static boolean areSameItemType(ItemStack a, ItemStack b) {
        return areItemsEqual(a, b) || a.isDamageableItem() && a.getItem() == b.getItem();
    }

    public static boolean areItemsStackable(ItemStack a, ItemStack b) {
        return a != null && b != null && a.isStackable() && ItemStack.isSameItemSameComponents(a, b);
    }
}
