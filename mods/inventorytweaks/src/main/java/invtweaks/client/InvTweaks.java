package invtweaks.client;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;

/**
 * InvTweaks: the tick logic of the mod (sort key, ruleset switching, auto-refill, sorting on pickup) and the
 * actions behind the middle click. Screens, buttons and mouse events are wired in {@link InvTweaksClient}.
 */
public final class InvTweaks {
    public static final Logger log = LogUtils.getLogger();
    /** "DEBUG" in the rules file: log what the sorting does. */
    public static boolean debug = false;
    private static InvTweaks instance;
    private final Minecraft mc;
    private final InvTweaksConfigManager cfgManager;
    private int chestAlgorithm = 0;
    private long chestAlgorithmClickTimestamp = 0L;
    private ItemStack storedStack = ItemStack.EMPTY;
    private int storedFocusedSlot = -1;
    private final ItemStack[] hotbarClone = new ItemStack[9];
    private boolean wasInGUI = false;
    private long sortingKeyPressedDate = 0L;
    private boolean sortKeyDown = false;
    private boolean sortKeyEnabled = true;
    private final List<Scheduled> scheduled = new LinkedList<>();

    private record Scheduled(long due, Runnable task) {
    }

    InvTweaks(Minecraft mc) {
        this.mc = mc;
        instance = this;
        this.cfgManager = new InvTweaksConfigManager(mc);
        for (int i = 0; i < this.hotbarClone.length; i++) {
            this.hotbarClone[i] = ItemStack.EMPTY;
        }
    }

    public static InvTweaks getInstance() {
        return instance;
    }

    public static InvTweaksConfigManager getConfigManager() {
        return instance.cfgManager;
    }

    // --- ticks --------------------------------------------------------------------------------------------------

    /** ForgeClientTick.tickEnd. */
    void onTick() {
        if (this.mc.level == null || this.mc.player == null) {
            return;
        }
        this.runScheduled();
        Screen screen = this.mc.gui.screen();
        if (screen != null) {
            this.onTickInGUI(screen);
        } else {
            this.onTickInGame();
        }
    }

    private void onTickInGame() {
        if (this.tick()) {
            this.handleAutoRefill();
            this.wasInGUI = false;
        }
    }

    private void onTickInGUI(Screen screen) {
        if (this.tick()) {
            ItemStack focused = this.getFocusedStack();
            this.storedStack = focused.copy();
            this.wasInGUI = true;
        }
    }

    /** onItemPickup check, before the player tick lowers the pop time of a freshly picked up stack. */
    void onTickStart() {
        if (this.mc.level == null || this.mc.player == null || this.cfgManager.getConfig() == null) {
            return;
        }
        Screen screen = this.mc.gui.screen();
        if (!this.cfgManager.getConfig().getProperty(InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP).equals(InvTweaksConfig.VALUE_FALSE)
                && (screen == null || screen instanceof InventoryScreen)) {
            this.onItemPickup();
        }
        if (screen == null || screen instanceof InventoryScreen) {
            this.cloneHotbar();
        }
    }

    private boolean tick() {
        if (!this.cfgManager.makeSureConfigurationIsLoaded()) {
            return false;
        }
        if (this.isSortingShortcutDown()) {
            if (!this.sortKeyDown) {
                this.sortKeyDown = true;
                this.onSortingKeyPressed();
            }
        } else {
            this.sortKeyDown = false;
        }
        this.handleConfigSwitch();
        return true;
    }

    public void onSortingKeyPressed() {
        if (this.cfgManager.makeSureConfigurationIsLoaded()) {
            Screen screen = this.mc.gui.screen();
            if (screen == null || InvTweaksClient.isValidChest(screen) || InvTweaksClient.isValidInventory(screen)) {
                this.handleSorting();
            }
        }
    }

    private void onItemPickup() {
        InvTweaksConfig config = this.cfgManager.getConfig();
        try {
            InvTweaksContainerSectionManager container = new InvTweaksContainerSectionManager(this.mc, ContainerSection.INVENTORY);
            int pickedUp = -1;
            for (int i = 0; i < 9; i++) {
                ItemStack stack = container.getItemStack(i + 27);
                if (stack != null && stack.getPopTime() == 5 && this.hotbarClone[i].isEmpty()) {
                    pickedUp = i + 27;
                }
            }
            if (pickedUp == -1) {
                return;
            }
            List<Integer> preferredPositions = new LinkedList<>();
            InvTweaksItemTree tree = config.getTree();
            List<InvTweaksItemTreeItem> items = tree.getItems(container.getItemStack(pickedUp));
            for (InvTweaksConfigSortingRule rule : config.getRules()) {
                if (tree.matches(items, rule.getKeyword())) {
                    for (int slot : rule.getPreferredSlots()) {
                        preferredPositions.add(slot);
                    }
                }
            }
            boolean hasToBeMoved = true;
            for (int slot : preferredPositions) {
                if (slot == pickedUp) {
                    hasToBeMoved = false;
                    break;
                }
                if (container.getItemStack(slot) == null && container.move(pickedUp, slot)) {
                    break;
                }
            }
            if (hasToBeMoved) {
                for (int i = 0; i < container.getSize(); i++) {
                    if (container.getItemStack(i) == null && container.move(pickedUp, i)) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            this.logInGameError("invtweaks.pickup.error", e);
        }
    }

    public void setSortKeyEnabled(boolean enabled) {
        this.sortKeyEnabled = enabled;
    }

    /** Sort key + number key selects a ruleset, holding the sort key cycles through them every second. */
    private void handleConfigSwitch() {
        InvTweaksConfig config = this.cfgManager.getConfig();
        InvTweaksHandlerShortcuts shortcuts = this.cfgManager.getShortcutsHandler();
        shortcuts.updatePressedKeys();
        InvTweaksShortcutMapping hotbarShortcut = shortcuts.isShortcutDown(InvTweaksShortcutMapping.Type.MOVE_TO_SPECIFIC_HOTBAR_SLOT);
        boolean sortKey = this.isSortingShortcutDown();
        if (sortKey && hotbarShortcut != null) {
            int index = InvTweaksKeys.hotbarSlot(hotbarShortcut.getKeyCodes().get(0));
            String newRuleset = index != -1 ? config.switchConfig(index) : null;
            if (newRuleset != null) {
                this.logInGame(String.format(InvTweaksLocalization.get("invtweaks.loadconfig.enabled"), newRuleset), true);
                this.sortingKeyPressedDate = Integer.MAX_VALUE;
            }
        }
        if (sortKey) {
            long now = System.currentTimeMillis();
            if (this.sortingKeyPressedDate == 0L) {
                this.sortingKeyPressedDate = now;
            } else if (now - this.sortingKeyPressedDate > InvTweaksConst.RULESET_SWAP_DELAY && this.sortingKeyPressedDate != Integer.MAX_VALUE) {
                String previous = config.getCurrentRulesetName();
                String next = config.switchConfig();
                if (previous != null && next != null && !previous.equals(next)) {
                    this.logInGame(String.format(InvTweaksLocalization.get("invtweaks.loadconfig.enabled"), next), true);
                    this.handleSorting();
                }
                this.sortingKeyPressedDate = now;
            }
        } else {
            this.sortingKeyPressedDate = 0L;
        }
    }

    /** Sorts the player inventory (sort key, middle click outside a chest). */
    public void handleSorting() {
        ItemStack focusedBefore = this.getFocusedStack();
        try {
            new InvTweaksHandlerSorting(this.mc, this.cfgManager.getConfig(), ContainerSection.INVENTORY, InvTweaksHandlerSorting.ALGORITHM_INVENTORY, 9).sort();
        } catch (Exception e) {
            this.logInGameError("invtweaks.sort.inventory.error", e);
        }
        this.playClick();
        if (!focusedBefore.isEmpty() && this.getFocusedStack().isEmpty()) {
            this.storedStack = ItemStack.EMPTY;
        }
    }

    private void handleAutoRefill() {
        ItemStack current = this.getFocusedStack();
        Item currentItem = current.isEmpty() ? null : current.getItem();
        Item storedItem = this.storedStack.isEmpty() ? null : this.storedStack.getItem();
        int currentDamage = current.isEmpty() ? 0 : InvTweaksContainerManager.getItemDamage(current);
        int storedDamage = this.storedStack.isEmpty() ? 0 : InvTweaksContainerManager.getItemDamage(this.storedStack);
        int focusedSlot = this.mc.player.getInventory().getSelectedSlot() + 27;
        InvTweaksConfig config = this.cfgManager.getConfig();
        if (currentItem != storedItem || currentDamage != storedDamage) {
            Screen screen = this.mc.gui.screen();
            if (this.storedFocusedSlot != focusedSlot) {
                this.storedFocusedSlot = focusedSlot;
            } else if ((current.isEmpty() || currentItem == Items.BOWL && isStew(storedItem))
                    && (screen == null || screen instanceof AbstractSignEditScreen)) {
                if (!this.storedStack.isEmpty() && config.isAutoRefillEnabled(this.storedStack)) {
                    try {
                        this.cfgManager.getAutoRefillHandler().autoRefillSlot(focusedSlot, this.storedStack);
                    } catch (Exception e) {
                        this.logInGameError("invtweaks.sort.autorefill.error", e);
                    }
                }
            } else {
                int maxDamage = current.getMaxDamage();
                int threshold = config.getIntProperty(InvTweaksConfig.PROP_AUTO_REFILL_DAMAGE_THRESHHOLD);
                if (maxDamage != 0 && maxDamage - currentDamage < threshold && maxDamage - storedDamage >= threshold
                        && config.getProperty(InvTweaksConfig.PROP_AUTO_REFILL_BEFORE_BREAK).equals(InvTweaksConfig.VALUE_TRUE)
                        && !this.storedStack.isEmpty() && config.isAutoRefillEnabled(this.storedStack)) {
                    try {
                        this.cfgManager.getAutoRefillHandler().autoRefillSlot(focusedSlot, this.storedStack);
                    } catch (Exception e) {
                        this.logInGameError("invtweaks.sort.autorefill.error", e);
                    }
                }
            }
        }
        this.storedStack = current.copy();
    }

    /** The original only knew the mushroom stew that leaves a bowl. */
    private static boolean isStew(Item item) {
        return item == Items.MUSHROOM_STEW || item == Items.BEETROOT_SOUP || item == Items.RABBIT_STEW || item == Items.SUSPICIOUS_STEW;
    }

    /**
     * handleMiddleClick: a middle click in a chest sorts the chest (each click within 2 s switches to the next
     * algorithm: default, columns, rows), on a crafting grid it evens out the stacks, elsewhere it sorts the
     * inventory. Returns true when the click was used.
     */
    boolean handleMiddleClick(AbstractContainerScreen<?> screen) {
        if (!this.cfgManager.makeSureConfigurationIsLoaded()) {
            return false;
        }
        InvTweaksConfig config = this.cfgManager.getConfig();
        if (!config.getProperty(InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK).equals(InvTweaksConfig.VALUE_TRUE)) {
            return false;
        }
        InvTweaksContainerManager container = new InvTweaksContainerManager(this.mc);
        Slot slot = container.getSlotAtMousePosition();
        ContainerSection section = slot != null ? container.getSlotSection(slot.index) : null;
        if (InvTweaksClient.isValidChest(screen)) {
            if (ContainerSection.CHEST.equals(section)) {
                this.playClick();
                long now = System.currentTimeMillis();
                if (now - this.chestAlgorithmClickTimestamp > InvTweaksConst.CHEST_ALGORITHM_SWAP_MAX_INTERVAL) {
                    this.chestAlgorithm = 0;
                }
                try {
                    new InvTweaksHandlerSorting(this.mc, config, ContainerSection.CHEST, this.chestAlgorithm, InvTweaksClient.getContainerRowSize(screen)).sort();
                } catch (Exception e) {
                    this.logInGameError("invtweaks.sort.chest.error", e);
                }
                this.chestAlgorithm = (this.chestAlgorithm + 1) % 3;
                this.chestAlgorithmClickTimestamp = now;
            } else if (ContainerSection.CRAFTING_IN.equals(section) || ContainerSection.CRAFTING_IN_PERSISTENT.equals(section)) {
                this.sortCrafting(container, section);
            } else if (ContainerSection.INVENTORY_HOTBAR.equals(section) || ContainerSection.INVENTORY_NOT_HOTBAR.equals(section)) {
                this.handleSorting();
            }
            return true;
        }
        if (InvTweaksClient.isValidInventory(screen)) {
            if (!ContainerSection.CRAFTING_IN.equals(section) && !ContainerSection.CRAFTING_IN_PERSISTENT.equals(section)) {
                this.handleSorting();
            } else {
                this.sortCrafting(container, section);
            }
            return true;
        }
        return false;
    }

    private void sortCrafting(InvTweaksContainerManager container, ContainerSection section) {
        try {
            new InvTweaksHandlerSorting(this.mc, this.cfgManager.getConfig(), section, InvTweaksHandlerSorting.ALGORITHM_EVEN_STACKS,
                    container.getSize(section) == 9 ? 3 : 2).sort();
        } catch (Exception e) {
            this.logInGameError("invtweaks.sort.crafting.error", e);
        }
    }

    /** Chest buttons and the chest sorting of the middle click. */
    public void sortChest(AbstractContainerScreen<?> screen, int algorithm) {
        try {
            new InvTweaksHandlerSorting(this.mc, this.cfgManager.getConfig(), ContainerSection.CHEST, algorithm, InvTweaksClient.getContainerRowSize(screen)).sort();
        } catch (Exception e) {
            this.logInGameError("invtweaks.sort.chest.error", e);
        }
    }

    void resetChestAlgorithm() {
        this.chestAlgorithmClickTimestamp = 0L;
    }

    /** Shortcuts are triggered by a click with the shortcut keys held. */
    boolean handleShortcut(boolean rightClick) {
        if (!this.cfgManager.makeSureConfigurationIsLoaded()
                || !this.cfgManager.getConfig().getProperty(InvTweaksConfig.PROP_ENABLE_SHORTCUTS).equals(InvTweaksConfig.VALUE_TRUE)) {
            return false;
        }
        return this.cfgManager.getShortcutsHandler().handleShortcut(rightClick);
    }

    private boolean isSortingShortcutDown() {
        if (!this.sortKeyEnabled || this.cfgManager.getConfig() == null) {
            return false;
        }
        Screen screen = this.mc.gui.screen();
        // the original read the raw keyboard: never while typing into a text field (chat, anvil, sign)
        if (screen != null && (!(screen instanceof AbstractContainerScreen<?>) || screen.getFocused() instanceof EditBox)) {
            return false;
        }
        return InvTweaksKeys.isKeyDown(this.cfgManager.getConfig().getSortKeyCode());
    }

    private void cloneHotbar() {
        for (int i = 0; i < 9; i++) {
            this.hotbarClone[i] = this.mc.player.getInventory().getItem(i).copy();
        }
    }

    private ItemStack getFocusedStack() {
        return this.mc.player.getInventory().getSelectedItem();
    }

    private void playClick() {
        if (!this.cfgManager.getConfig().getProperty(InvTweaksConfig.PROP_ENABLE_SOUNDS).equals(InvTweaksConfig.VALUE_FALSE)) {
            playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.6F, 1.8F);
        }
    }

    // --- scheduling, sounds, messages ---------------------------------------------------------------------------

    static void schedule(long delayMillis, Runnable task) {
        instance.scheduled.add(new Scheduled(System.currentTimeMillis() + delayMillis, task));
    }

    private void runScheduled() {
        if (this.scheduled.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        List<Runnable> due = new ArrayList<>();
        for (Iterator<Scheduled> it = this.scheduled.iterator(); it.hasNext(); ) {
            Scheduled task = it.next();
            if (task.due() <= now) {
                due.add(task.task());
                it.remove();
            }
        }
        due.forEach(Runnable::run);
    }

    /** playSoundFX(name, volume, pitch). */
    static void playSound(SoundEvent sound, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    public void logInGame(String message) {
        this.logInGame(message, false);
    }

    public void logInGame(String message, boolean alreadyTranslated) {
        String text = buildLogString(false, alreadyTranslated ? message : InvTweaksLocalization.get(message));
        if (this.mc.player != null) {
            this.mc.player.sendSystemMessage(Component.literal(text));
        }
        log.info(text);
    }

    public void logInGameError(String message, Exception e) {
        log.error(message, e);
        String text = buildLogString(true, InvTweaksLocalization.get(message));
        if (e != null) {
            text = text + ": " + e.getMessage();
        }
        if (this.mc.player != null) {
            this.mc.player.sendSystemMessage(Component.literal(text));
        }
    }

    public static void logInGameStatic(String message) {
        if (instance != null) {
            instance.logInGame(message);
        }
    }

    public static void logInGameStatic(String message, boolean alreadyTranslated) {
        if (instance != null) {
            instance.logInGame(message, alreadyTranslated);
        }
    }

    public static void logInGameErrorStatic(String message, Exception e) {
        if (instance != null) {
            instance.logInGameError(message, e);
        } else {
            log.error(message, e);
        }
    }

    private static String buildLogString(boolean error, String message) {
        return InvTweaksConst.INGAME_LOG_PREFIX + (error ? "[ERROR] " : "") + message;
    }
}
