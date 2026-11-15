package invtweaks.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.minecraft.world.item.ItemStack;

/**
 * InvTweaksConfig: the properties of InvTweaks.cfg and the rulesets of InvTweaksRules.txt. The defaults are the
 * values of the Hexxit pack config (they equal the mod's own defaults).
 */
public final class InvTweaksConfig {
    public static final String PROP_VERSION = "version";
    public static final String PROP_ENABLE_MIDDLE_CLICK = "enableMiddleClick";
    public static final String PROP_SHOW_CHEST_BUTTONS = "showChestButtons";
    public static final String PROP_ENABLE_SORTING_ON_PICKUP = "enableSortingOnPickup";
    public static final String PROP_ENABLE_AUTO_EQUIP_ARMOR = "enableAutoEquipArmor";
    public static final String PROP_ENABLE_AUTO_REFILL = "enableAutoRefill";
    public static final String PROP_AUTO_REFILL_BEFORE_BREAK = "autoRefillBeforeBreak";
    public static final String PROP_AUTO_REFILL_DAMAGE_THRESHHOLD = "autoRefillDamageThreshhold";
    public static final String PROP_KEY_SORT_INVENTORY = "keySortInventory";
    public static final String PROP_ENABLE_SHORTCUTS = "enableShortcuts";
    public static final String PROP_SHORTCUT_PREFIX = "shortcutKey";
    public static final String PROP_SHORTCUT_ONE_ITEM = "shortcutKeyOneItem";
    public static final String PROP_SHORTCUT_ALL_ITEMS = "shortcutKeyAllItems";
    public static final String PROP_SHORTCUT_EVERYTHING = "shortcutKeyEverything";
    public static final String PROP_SHORTCUT_DROP = "shortcutKeyDrop";
    public static final String PROP_SHORTCUT_UP = "shortcutKeyToUpperSection";
    public static final String PROP_SHORTCUT_DOWN = "shortcutKeyToLowerSection";
    public static final String PROP_ENABLE_SOUNDS = "enableSounds";
    public static final String PROP_ENABLE_FORGE_ITEMTREE = "enableForgeTreeAdditions";
    /** Kept in the file, without effect: there is no server part any more. */
    public static final String PROP_ENABLE_SERVER_ITEMSWAP = "enableServerItemSwap";
    public static final String VALUE_TRUE = "true";
    public static final String VALUE_FALSE = "false";
    public static final String LOCKED = "locked";
    public static final String FROZEN = "frozen";
    public static final String AUTOREFILL = "autorefill";
    public static final String AUTOREFILL_NOTHING = "nothing";
    public static final String DEBUG = "debug";

    private final Path rulesFile;
    private final Path treeFile;
    private OrderedProperties properties;
    private InvTweaksItemTree tree;
    private List<InvTweaksConfigInventoryRuleset> rulesets;
    private int currentRuleset = 0;
    private String currentRulesetName = null;
    private List<String> invalidKeywords;
    private int sortKeyCode;
    private long storedConfigLastModified;

    public InvTweaksConfig(Path rulesFile, Path treeFile) {
        this.rulesFile = rulesFile;
        this.treeFile = treeFile;
        this.reset();
    }

    public synchronized void load() throws Exception {
        this.reset();
        this.loadProperties();
        this.saveProperties();
        this.tree = InvTweaksItemTreeLoader.load(this.treeFile, !this.getProperty(PROP_ENABLE_FORGE_ITEMTREE).equals(VALUE_FALSE));
        String text = Files.readString(this.rulesFile, StandardCharsets.UTF_8);
        String[] lines = text.replace("\r\n", "\n").replace('\r', '\n').split("\n");
        InvTweaksConfigInventoryRuleset ruleset = new InvTweaksConfigInventoryRuleset(this.tree, "Default");
        boolean defaultRuleset = true;
        boolean defaultRulesetEmpty = true;
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.matches("^[\\w]*[\\s]*\\:$")) {
                if (!defaultRuleset || !defaultRulesetEmpty) {
                    ruleset.finalizeRules();
                    this.rulesets.add(ruleset);
                }
                defaultRuleset = false;
                ruleset = new InvTweaksConfigInventoryRuleset(this.tree, line.substring(0, line.length() - 1));
            } else {
                try {
                    String invalid = ruleset.registerLine(line);
                    if (defaultRuleset) {
                        defaultRulesetEmpty = false;
                    }
                    if (invalid != null) {
                        this.invalidKeywords.add(invalid);
                    }
                } catch (InvalidParameterException e) {
                    // not a rule (comment, title...)
                }
            }
        }
        ruleset.finalizeRules();
        this.rulesets.add(ruleset);
        this.currentRuleset = 0;
        if (this.currentRulesetName != null) {
            for (int i = 0; i < this.rulesets.size(); i++) {
                if (this.rulesets.get(i).getName().equals(this.currentRulesetName)) {
                    this.currentRuleset = i;
                    break;
                }
            }
        }
        if (this.currentRuleset == 0) {
            this.currentRulesetName = this.rulesets.isEmpty() ? null : this.rulesets.get(0).getName();
        }
    }

    public boolean refreshProperties() throws IOException {
        long modified = lastModified(InvTweaksConst.CONFIG_PROPS_FILE);
        if (this.storedConfigLastModified != modified) {
            this.storedConfigLastModified = modified;
            this.loadProperties();
            return true;
        }
        return false;
    }

    public void saveProperties() {
        Path file = this.getPropertyFile();
        if (file != null) {
            try (OutputStream out = Files.newOutputStream(file)) {
                this.properties.store(out, "Inventory Tweaks Configuration\n(Regarding shortcuts, all key names can be found at: "
                        + "http://www.lwjgl.org/javadoc/org/lwjgl/input/Keyboard.html)");
            } catch (IOException e) {
                InvTweaks.logInGameStatic("Failed to save config file " + InvTweaksConst.CONFIG_PROPS_FILE);
            }
            this.storedConfigLastModified = lastModified(InvTweaksConst.CONFIG_PROPS_FILE);
            this.sortKeyCode = InvTweaksKeys.getKeyIndex(this.getProperty(PROP_KEY_SORT_INVENTORY));
        }
    }

    public Map<String, String> getProperties(String prefix) {
        Map<String, String> result = new HashMap<>();
        for (String key : this.properties.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                result.put(key, this.properties.getProperty(key));
            }
        }
        return result;
    }

    public String getProperty(String key) {
        String value = this.properties.getProperty(key);
        return value != null ? value : "";
    }

    public int getIntProperty(String key) {
        try {
            return Integer.parseInt(this.getProperty(key).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void setProperty(String key, String value) {
        this.properties.put(key, value);
        this.saveProperties();
    }

    public InvTweaksItemTree getTree() {
        return this.tree;
    }

    public String getCurrentRulesetName() {
        return this.currentRulesetName;
    }

    public String switchConfig(int index) {
        if (!this.rulesets.isEmpty() && index < this.rulesets.size() && index != this.currentRuleset) {
            this.currentRuleset = index;
            this.currentRulesetName = this.rulesets.get(this.currentRuleset).getName();
            return this.currentRulesetName;
        }
        return null;
    }

    public String switchConfig() {
        return this.currentRuleset == -1 ? this.switchConfig(0) : this.switchConfig((this.currentRuleset + 1) % this.rulesets.size());
    }

    public List<InvTweaksConfigSortingRule> getRules() {
        return this.rulesets.get(this.currentRuleset).getRules();
    }

    public List<String> getInvalidKeywords() {
        return this.invalidKeywords;
    }

    public int[] getLockPriorities() {
        return this.rulesets.get(this.currentRuleset).getLockPriorities();
    }

    public boolean[] getFrozenSlots() {
        return this.rulesets.get(this.currentRuleset).getFrozenSlots();
    }

    public List<Integer> getLockedSlots() {
        return this.rulesets.get(this.currentRuleset).getLockedSlots();
    }

    public boolean isDebugEnabled() {
        return this.rulesets.get(this.currentRuleset).isDebugEnabled();
    }

    /** isAutoRefillEnabled(id, damage): the AUTOREFILL keywords of the ruleset (root category if there are none). */
    public boolean isAutoRefillEnabled(ItemStack stack) {
        if (this.getProperty(PROP_ENABLE_AUTO_REFILL).equals(VALUE_FALSE)) {
            return false;
        }
        List<InvTweaksItemTreeItem> items = this.tree.getItems(stack);
        List<String> autoReplaceRules = this.rulesets.get(this.currentRuleset).getAutoReplaceRules();
        boolean found = false;
        for (String keyword : autoReplaceRules) {
            if (keyword.equals(AUTOREFILL_NOTHING)) {
                return false;
            }
            if (this.tree.matches(items, keyword)) {
                found = true;
            }
        }
        return found || autoReplaceRules.isEmpty();
    }

    public int getSortKeyCode() {
        return this.sortKeyCode;
    }

    private void reset() {
        this.rulesets = new ArrayList<>();
        this.currentRuleset = -1;
        this.properties = new OrderedProperties();
        this.properties.put(PROP_ENABLE_MIDDLE_CLICK, VALUE_TRUE);
        this.properties.put(PROP_SHOW_CHEST_BUTTONS, VALUE_TRUE);
        this.properties.put(PROP_ENABLE_SORTING_ON_PICKUP, VALUE_FALSE);
        this.properties.put(PROP_ENABLE_AUTO_REFILL, VALUE_TRUE);
        this.properties.put(PROP_AUTO_REFILL_BEFORE_BREAK, VALUE_FALSE);
        this.properties.put(PROP_AUTO_REFILL_DAMAGE_THRESHHOLD, "5");
        this.properties.put(PROP_ENABLE_SOUNDS, VALUE_TRUE);
        this.properties.put(PROP_ENABLE_SHORTCUTS, VALUE_TRUE);
        this.properties.put(PROP_ENABLE_AUTO_EQUIP_ARMOR, VALUE_FALSE);
        this.properties.put(PROP_ENABLE_FORGE_ITEMTREE, VALUE_TRUE);
        this.properties.put(PROP_ENABLE_SERVER_ITEMSWAP, VALUE_TRUE);
        this.properties.put(PROP_KEY_SORT_INVENTORY, "R");
        this.properties.put(PROP_SHORTCUT_ALL_ITEMS, "LCONTROL+LSHIFT, RCONTROL+RSHIFT");
        this.properties.put(PROP_SHORTCUT_EVERYTHING, "SPACE");
        this.properties.put(PROP_SHORTCUT_ONE_ITEM, "LCONTROL, RCONTROL");
        this.properties.put(PROP_SHORTCUT_UP, "UP");
        this.properties.put(PROP_SHORTCUT_DOWN, "DOWN");
        this.properties.put(PROP_SHORTCUT_DROP, "LALT, RALT");
        this.properties.put(PROP_VERSION, InvTweaksConst.MOD_VERSION);
        this.invalidKeywords = new ArrayList<>();
    }

    private void loadProperties() throws IOException {
        Path file = this.getPropertyFile();
        OrderedProperties loaded = new OrderedProperties();
        if (file != null) {
            try (InputStream in = Files.newInputStream(file)) {
                loaded.load(in);
            }
        }
        loaded.sortKeys();
        loaded.remove("enableSortingSound");
        loaded.remove("enableAutoRefillSound");
        loaded.remove("shortcutKeyOneStack");
        if (loaded.get(PROP_VERSION) != null) {
            for (String key : loaded.stringPropertyNames()) {
                this.properties.put(key, loaded.getProperty(key));
            }
        }
    }

    private Path getPropertyFile() {
        Path file = InvTweaksConst.CONFIG_PROPS_FILE;
        if (!Files.exists(file)) {
            try {
                Files.createDirectories(file.getParent());
                Files.createFile(file);
            } catch (IOException e) {
                InvTweaks.logInGameStatic("invtweaks.propsfile.errors");
                return null;
            }
        }
        return file;
    }

    static long lastModified(Path file) {
        try {
            return Files.exists(file) ? Files.getLastModifiedTime(file).toMillis() : 0L;
        } catch (IOException e) {
            return 0L;
        }
    }

    /** InvTweaksConfigProperties: properties that keep the insertion order of their keys when saved. */
    static final class OrderedProperties extends Properties {
        private static final long serialVersionUID = 1L;
        private final List<Object> keys = new LinkedList<>();

        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(new LinkedHashSet<>(this.keys));
        }

        @Override
        public synchronized java.util.Set<Map.Entry<Object, Object>> entrySet() {
            // Properties.store iterates entrySet() since Java 9: give it the key order
            java.util.Set<Map.Entry<Object, Object>> ordered = new LinkedHashSet<>();
            for (Object key : new LinkedHashSet<>(this.keys)) {
                Object value = super.get(key);
                if (value != null) {
                    ordered.add(new java.util.AbstractMap.SimpleImmutableEntry<>(key, value));
                }
            }
            return ordered;
        }

        @Override
        public synchronized Object put(Object key, Object value) {
            this.keys.add(key);
            return super.put(key, value);
        }

        @Override
        public synchronized Object remove(Object key) {
            this.keys.removeIf(k -> k.equals(key));
            return super.remove(key);
        }

        void sortKeys() {
            this.keys.sort((a, b) -> String.valueOf(a).compareTo(String.valueOf(b)));
        }
    }
}
