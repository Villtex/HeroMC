package invtweaks.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * InvTweaksHandlerShortcuts: a click with shortcut keys held in a chest or inventory moves one item, a stack, all
 * items of a type or everything to the other section, up or down, to a hotbar slot, or drops them. The original
 * polled the mouse in a tick and then re-created the LWJGL mouse to swallow the click; here the click event itself
 * is taken and cancelled.
 */
public final class InvTweaksHandlerShortcuts {
    private final Minecraft mc;
    private final InvTweaksConfig config;
    private InvTweaksContainerManager container;
    private final Map<Integer, Boolean> pressedKeys = new HashMap<>();
    private final Map<InvTweaksShortcutMapping.Type, List<InvTweaksShortcutMapping>> shortcuts = new HashMap<>();

    /** ShortcutSpecification: what the pressed keys ask for. */
    enum Action { MOVE, DROP }

    enum Scope { EVERYTHING, ALL_ITEMS, ONE_STACK, ONE_ITEM }

    enum Target { UP, DOWN, HOTBAR_SLOT, UNSPECIFIED }

    private static final class ShortcutConfig {
        Action action;
        Target target;
        Scope scope;
        ContainerSection fromSection;
        int fromIndex = -1;
        ItemStack fromStack;
        ContainerSection toSection;
        int toIndex = -1;
        boolean forceEmptySlot;
    }

    public InvTweaksHandlerShortcuts(Minecraft mc, InvTweaksConfig config) {
        this.mc = mc;
        this.config = config;
    }

    public void loadShortcuts() {
        this.pressedKeys.clear();
        this.shortcuts.clear();
        Map<String, String> keys = this.config.getProperties(InvTweaksConfig.PROP_SHORTCUT_PREFIX);
        for (Map.Entry<String, String> entry : keys.entrySet()) {
            InvTweaksShortcutMapping.Type type = InvTweaksShortcutMapping.Type.fromConfigKey(entry.getKey());
            if (type != null) {
                for (String mapping : entry.getValue().split("[ ]*,[ ]*")) {
                    this.registerShortcutMapping(type, new InvTweaksShortcutMapping(mapping.split("\\+")));
                }
            }
        }
        this.registerShortcutMapping(InvTweaksShortcutMapping.Type.MOVE_UP, new InvTweaksShortcutMapping(keyCode(this.mc.options.keyUp)));
        this.registerShortcutMapping(InvTweaksShortcutMapping.Type.MOVE_DOWN, new InvTweaksShortcutMapping(keyCode(this.mc.options.keyDown)));
        for (int code : hotbarKeys()) {
            this.registerShortcutMapping(InvTweaksShortcutMapping.Type.MOVE_TO_SPECIFIC_HOTBAR_SLOT, new InvTweaksShortcutMapping(code));
        }
        this.pressedKeys.put(InputConstants.KEY_LSHIFT, false);
        this.pressedKeys.put(InputConstants.KEY_RSHIFT, false);
    }

    /** 1-9 and NUMPAD1-9 in the order of the original (2..10, then 79 80 81 75 76 77 71 72 73). */
    static int[] hotbarKeys() {
        return new int[]{InputConstants.KEY_1, InputConstants.KEY_2, InputConstants.KEY_3, InputConstants.KEY_4, InputConstants.KEY_5,
                InputConstants.KEY_6, InputConstants.KEY_7, InputConstants.KEY_8, InputConstants.KEY_9, InputConstants.KEY_NUMPAD1,
                InputConstants.KEY_NUMPAD2, InputConstants.KEY_NUMPAD3, InputConstants.KEY_NUMPAD4, InputConstants.KEY_NUMPAD5,
                InputConstants.KEY_NUMPAD6, InputConstants.KEY_NUMPAD7, InputConstants.KEY_NUMPAD8, InputConstants.KEY_NUMPAD9};
    }

    static int keyCode(KeyMapping mapping) {
        InputConstants.Key key = mapping.getKey();
        return key.getType() == InputConstants.Type.KEYBOARD ? key.getValue() : InvTweaksKeys.NONE;
    }

    private void registerShortcutMapping(InvTweaksShortcutMapping.Type type, InvTweaksShortcutMapping mapping) {
        this.shortcuts.computeIfAbsent(type, k -> new LinkedList<>()).add(mapping);
        for (int code : mapping.getKeyCodes()) {
            this.pressedKeys.put(code, false);
        }
    }

    /** handleShortcut, called for a mouse click (button 0 = left, 1 = right) in a chest or inventory screen. */
    public boolean handleShortcut(boolean rightClick) {
        try {
            ShortcutConfig shortcut = this.computeShortcutToTrigger(rightClick);
            if (shortcut != null) {
                this.runShortcut(shortcut);
                return true;
            }
        } catch (Exception e) {
            InvTweaks.logInGameErrorStatic("invtweaks.shortcut.error", e);
        }
        return false;
    }

    private ShortcutConfig computeCurrentShortcut() {
        Action action = Action.MOVE;
        Target target = Target.UNSPECIFIED;
        Scope scope = Scope.ONE_STACK;
        this.updatePressedKeys();
        boolean triggered = false;
        if (this.isShortcutDown(InvTweaksShortcutMapping.Type.DROP) != null) {
            action = Action.DROP;
            triggered = true;
        }
        if (action != Action.DROP) {
            if (this.isShortcutDown(InvTweaksShortcutMapping.Type.MOVE_TO_SPECIFIC_HOTBAR_SLOT) != null) {
                target = Target.HOTBAR_SLOT;
                triggered = true;
            } else if (this.isShortcutDown(InvTweaksShortcutMapping.Type.MOVE_UP) != null) {
                target = Target.UP;
                triggered = true;
            } else if (this.isShortcutDown(InvTweaksShortcutMapping.Type.MOVE_DOWN) != null) {
                target = Target.DOWN;
                triggered = true;
            }
        }
        if (this.isShortcutDown(InvTweaksShortcutMapping.Type.MOVE_ALL_ITEMS) != null) {
            scope = Scope.ALL_ITEMS;
            triggered = true;
        } else if (this.isShortcutDown(InvTweaksShortcutMapping.Type.MOVE_EVERYTHING) != null) {
            scope = Scope.EVERYTHING;
            triggered = true;
        } else if (this.isShortcutDown(InvTweaksShortcutMapping.Type.MOVE_ONE_ITEM) != null) {
            scope = Scope.ONE_ITEM;
            triggered = true;
        }
        if (!triggered) {
            return null;
        }
        ShortcutConfig shortcut = new ShortcutConfig();
        shortcut.action = action;
        shortcut.scope = scope;
        shortcut.target = target;
        return shortcut;
    }

    private ShortcutConfig computeShortcutToTrigger(boolean rightClick) {
        ShortcutConfig shortcut = this.computeCurrentShortcut();
        this.container = new InvTweaksContainerManager(this.mc);
        Slot slot = this.container.getSlotAtMousePosition();
        if (shortcut == null || slot == null || !slot.hasItem() && this.container.getHeldStack() == null) {
            return null;
        }
        Target target = shortcut.target;
        int slotNumber = slot.index;
        shortcut.fromSection = this.container.getSlotSection(slotNumber);
        shortcut.fromIndex = this.container.getSlotIndex(slotNumber);
        shortcut.fromStack = slot.hasItem() ? slot.getItem().copy() : this.container.getHeldStack().copy();
        if (!slot.mayPlace(slot.getItem()) && shortcut.scope == Scope.ONE_ITEM) {
            shortcut.scope = Scope.ONE_STACK;
        }
        if (shortcut.fromSection == null || shortcut.fromIndex == -1) {
            return null;
        }
        if (shortcut.action != Action.DROP) {
            if (target == Target.HOTBAR_SLOT) {
                shortcut.toSection = ContainerSection.INVENTORY_HOTBAR;
                InvTweaksShortcutMapping mapping = this.isShortcutDown(InvTweaksShortcutMapping.Type.MOVE_TO_SPECIFIC_HOTBAR_SLOT);
                if (mapping != null && !mapping.getKeyCodes().isEmpty()) {
                    shortcut.toIndex = InvTweaksKeys.hotbarSlot(mapping.getKeyCodes().get(0));
                }
            } else {
                List<ContainerSection> order = new ArrayList<>();
                if (this.container.hasSection(ContainerSection.CHEST)) {
                    order.add(ContainerSection.CHEST);
                } else if (this.container.hasSection(ContainerSection.CRAFTING_IN)) {
                    order.add(ContainerSection.CRAFTING_IN);
                } else if (this.container.hasSection(ContainerSection.CRAFTING_IN_PERSISTENT)) {
                    order.add(ContainerSection.CRAFTING_IN_PERSISTENT);
                } else if (this.container.hasSection(ContainerSection.FURNACE_IN)) {
                    order.add(ContainerSection.FURNACE_IN);
                } else if (this.container.hasSection(ContainerSection.BREWING_INGREDIENT)) {
                    ItemStack stack = slot.getItem();
                    if (!stack.isEmpty()) {
                        if (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)) {
                            order.add(ContainerSection.BREWING_BOTTLES);
                        } else {
                            order.add(ContainerSection.BREWING_INGREDIENT);
                        }
                    }
                } else if (this.container.hasSection(ContainerSection.ENCHANTMENT)) {
                    order.add(ContainerSection.ENCHANTMENT);
                }
                order.add(ContainerSection.INVENTORY_NOT_HOTBAR);
                order.add(ContainerSection.INVENTORY_HOTBAR);
                if (target != Target.UNSPECIFIED) {
                    int step = target == Target.UP ? -1 : 1;
                    int index = order.indexOf(shortcut.fromSection);
                    shortcut.toSection = index != -1 ? order.get((order.size() + index + step) % order.size()) : ContainerSection.INVENTORY;
                } else {
                    switch (shortcut.fromSection) {
                        case CHEST -> shortcut.toSection = ContainerSection.INVENTORY;
                        case INVENTORY_HOTBAR -> shortcut.toSection = order.contains(ContainerSection.CHEST)
                                ? ContainerSection.CHEST : ContainerSection.INVENTORY_NOT_HOTBAR;
                        case CRAFTING_IN, CRAFTING_IN_PERSISTENT, FURNACE_IN -> shortcut.toSection = ContainerSection.INVENTORY_NOT_HOTBAR;
                        default -> shortcut.toSection = order.contains(ContainerSection.CHEST)
                                ? ContainerSection.CHEST : ContainerSection.INVENTORY_HOTBAR;
                    }
                }
            }
        }
        shortcut.forceEmptySlot = rightClick;
        return shortcut;
    }

    public void updatePressedKeys() {
        if (this.haveControlsChanged()) {
            this.loadShortcuts();
        }
        for (Map.Entry<Integer, Boolean> entry : this.pressedKeys.entrySet()) {
            entry.setValue(entry.getKey() > 0 && InvTweaksKeys.isKeyDown(entry.getKey()));
        }
    }

    private boolean haveControlsChanged() {
        return !this.pressedKeys.containsKey(keyCode(this.mc.options.keyUp)) || !this.pressedKeys.containsKey(keyCode(this.mc.options.keyDown));
    }

    private void runShortcut(ShortcutConfig shortcut) {
        if (this.container.getHeldStack() != null) {
            Slot slot = this.container.getSlotAtMousePosition();
            if (slot == null) {
                return;
            }
            this.container.putHoldItemDown(this.container.getSlotSection(slot.index), this.container.getSlotIndex(slot.index));
            if (this.container.getHeldStack() != null) {
                return;
            }
        }
        if (shortcut.toSection == ContainerSection.INVENTORY_HOTBAR && shortcut.toIndex != -1) {
            this.container.move(shortcut.fromSection, shortcut.fromIndex, shortcut.toSection, shortcut.toIndex);
            return;
        }
        if (shortcut.action == Action.DROP) {
            // the original fell through into the MOVE case after dropping, which dropped more than asked
            switch (shortcut.scope) {
                case ONE_ITEM -> this.container.dropSome(shortcut.fromSection, shortcut.fromIndex, 1);
                case ONE_STACK -> this.container.drop(shortcut.fromSection, shortcut.fromIndex);
                case ALL_ITEMS -> this.dropAll(shortcut, shortcut.fromStack);
                case EVERYTHING -> this.dropAll(shortcut, null);
            }
            return;
        }
        int target = this.getNextTargetIndex(shortcut);
        if (target == -1) {
            return;
        }
        switch (shortcut.scope) {
            case ONE_ITEM -> this.container.moveSome(shortcut.fromSection, shortcut.fromIndex, shortcut.toSection, target, 1);
            case ONE_STACK -> {
                Slot slot = this.container.getSlot(shortcut.fromSection, shortcut.fromIndex);
                if (shortcut.fromSection != ContainerSection.CRAFTING_OUT && shortcut.toSection != ContainerSection.ENCHANTMENT) {
                    while (slot.hasItem() && target != -1) {
                        boolean moved = this.container.move(shortcut.fromSection, shortcut.fromIndex, shortcut.toSection, target);
                        int next = this.getNextTargetIndex(shortcut);
                        target = !moved && next == target ? -1 : next;
                    }
                } else {
                    this.container.move(shortcut.fromSection, shortcut.fromIndex, shortcut.toSection, target);
                }
            }
            case ALL_ITEMS -> {
                this.moveAll(shortcut, shortcut.fromStack);
                if (shortcut.fromSection == ContainerSection.INVENTORY_NOT_HOTBAR && shortcut.toSection == ContainerSection.CHEST) {
                    shortcut.fromSection = ContainerSection.INVENTORY_HOTBAR;
                    this.moveAll(shortcut, shortcut.fromStack);
                }
            }
            case EVERYTHING -> {
                this.moveAll(shortcut, null);
                if (shortcut.fromSection == ContainerSection.INVENTORY_HOTBAR && shortcut.toSection == ContainerSection.CHEST) {
                    shortcut.fromSection = ContainerSection.INVENTORY_HOTBAR;
                    this.moveAll(shortcut, null);
                }
            }
        }
    }

    private void dropAll(ShortcutConfig shortcut, ItemStack type) {
        for (Slot slot : this.container.getSlots(shortcut.fromSection)) {
            if (slot.hasItem() && (type == null || InvTweaksContainerManager.areSameItemType(type, slot.getItem()))) {
                int index = this.container.getSlotIndex(slot.index);
                int guard = 0;
                while (slot.hasItem() && guard++ < 64) {
                    this.container.drop(shortcut.fromSection, index);
                }
            }
        }
    }

    private void moveAll(ShortcutConfig shortcut, ItemStack type) {
        int target = this.getNextTargetIndex(shortcut);
        for (Slot slot : this.container.getSlots(shortcut.fromSection)) {
            if (slot.hasItem() && (type == null || InvTweaksContainerManager.areSameItemType(type, slot.getItem()))) {
                int index = this.container.getSlotIndex(slot.index);
                while (slot.hasItem() && target != -1 && (shortcut.fromSection != shortcut.toSection || index != target)) {
                    boolean moved = this.container.move(shortcut.fromSection, index, shortcut.toSection, target);
                    int next = this.getNextTargetIndex(shortcut);
                    target = !moved && next == target ? -1 : next;
                }
            }
            if (target == -1) {
                break;
            }
        }
    }

    private int getNextTargetIndex(ShortcutConfig shortcut) {
        if (shortcut.action == Action.DROP) {
            return InvTweaksContainerManager.DROP_SLOT;
        }
        int result = -1;
        if (!shortcut.forceEmptySlot) {
            List<Slot> slots = this.container.getSlots(shortcut.toSection);
            for (int i = 0; slots != null && i < slots.size(); i++) {
                Slot slot = slots.get(i);
                if (slot.hasItem()) {
                    ItemStack stack = slot.getItem();
                    if (!InvTweaksContainerManager.hasDataTags(stack) && InvTweaksContainerManager.areItemsEqual(stack, shortcut.fromStack)
                            && stack.getCount() < stack.getMaxStackSize()) {
                        result = i;
                        break;
                    }
                }
            }
        }
        if (result == -1) {
            result = this.container.getFirstEmptyIndex(shortcut.toSection);
        }
        if (result == -1 && shortcut.toSection == ContainerSection.FURNACE_IN) {
            shortcut.toSection = ContainerSection.FURNACE_FUEL;
            result = this.container.getFirstEmptyIndex(shortcut.toSection);
        }
        return result;
    }

    public InvTweaksShortcutMapping isShortcutDown(InvTweaksShortcutMapping.Type type) {
        List<InvTweaksShortcutMapping> mappings = this.shortcuts.get(type);
        if (mappings != null) {
            for (InvTweaksShortcutMapping mapping : mappings) {
                if (mapping.isTriggered(this.pressedKeys)) {
                    return mapping;
                }
            }
        }
        return null;
    }
}
