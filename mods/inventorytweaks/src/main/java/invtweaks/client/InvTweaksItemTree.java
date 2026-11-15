package invtweaks.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * InvTweaksItemTree: categories (keywords) and the items in them, in the order of the tree file. Item tags replace
 * the ore dictionary; they are checked against the stack when it is looked up, because tags are only known once a
 * world is loaded (and can change with every data pack reload).
 */
public final class InvTweaksItemTree {
    public static final String UNKNOWN_ITEM = "unknown";
    private final Map<String, InvTweaksItemTreeCategory> categories = new HashMap<>();
    private final Map<Item, List<InvTweaksItemTreeItem>> itemsByItem = new HashMap<>(500);
    private final List<InvTweaksItemTreeItem> tagItems = new ArrayList<>();
    private final Map<String, List<InvTweaksItemTreeItem>> itemsByName = new HashMap<>(500);
    private String rootCategory;
    private int seq;

    public boolean matches(List<InvTweaksItemTreeItem> items, String keyword) {
        if (items == null) {
            return false;
        }
        for (InvTweaksItemTreeItem item : items) {
            if (item.getName() != null && item.getName().equals(keyword)) {
                return true;
            }
        }
        // rule by item id (was "D5 203"): the keyword is the registry id of the stack
        if (keyword.indexOf(':') > 0) {
            for (InvTweaksItemTreeItem item : items) {
                if (keyword.equals(item.itemId())) {
                    return true;
                }
            }
        }
        InvTweaksItemTreeCategory category = this.getCategory(keyword);
        if (category != null) {
            for (InvTweaksItemTreeItem item : items) {
                if (category.contains(item)) {
                    return true;
                }
            }
        }
        return keyword.equals(this.rootCategory);
    }

    public int getKeywordDepth(String keyword) {
        InvTweaksItemTreeCategory root = this.getRootCategory();
        if (root == null) {
            InvTweaks.log.error("The root category is missing");
            return 0;
        }
        return root.findKeywordDepth(keyword);
    }

    public int getKeywordOrder(String keyword) {
        List<InvTweaksItemTreeItem> items = this.getItems(keyword);
        if (items != null && !items.isEmpty()) {
            return items.get(0).getOrder();
        }
        InvTweaksItemTreeCategory root = this.getRootCategory();
        if (root == null) {
            InvTweaks.log.error("The root category is missing");
            return -1;
        }
        return root.findCategoryOrder(keyword);
    }

    public boolean isKeywordValid(String keyword) {
        return this.containsItem(keyword) || this.getCategory(keyword) != null;
    }

    public Collection<InvTweaksItemTreeCategory> getAllCategories() {
        return this.categories.values();
    }

    public InvTweaksItemTreeCategory getRootCategory() {
        return this.categories.get(this.rootCategory);
    }

    public InvTweaksItemTreeCategory getCategory(String keyword) {
        return this.categories.get(keyword);
    }

    /** isItemUnknown(id, damage): no entry of the tree is this stack. */
    public boolean isItemUnknown(ItemStack stack) {
        if (this.itemsByItem.get(stack.getItem()) != null) {
            return false;
        }
        for (InvTweaksItemTreeItem item : this.tagItems) {
            if (item.matches(stack)) {
                return false;
            }
        }
        return true;
    }

    /**
     * getItems(id, damage): every entry that is this stack, in the order of the tree file. A stack the tree does
     * not know gets an entry named after its id at the end of the order (5000 + id * 16 as in the original, with
     * the registry number as id).
     */
    public List<InvTweaksItemTreeItem> getItems(ItemStack stack) {
        List<InvTweaksItemTreeItem> result = new ArrayList<>();
        List<InvTweaksItemTreeItem> exact = this.itemsByItem.get(stack.getItem());
        if (exact != null) {
            Identifier potion = null;
            boolean potionKnown = false;
            for (InvTweaksItemTreeItem item : exact) {
                if (item.getPotion() != null) {
                    if (!potionKnown) {
                        potion = InvTweaksItemTreeItem.potionOf(stack);
                        potionKnown = true;
                    }
                    if (!item.getPotion().equals(potion)) {
                        continue;
                    }
                }
                result.add(item);
            }
        }
        boolean merged = false;
        for (InvTweaksItemTreeItem item : this.tagItems) {
            if (item.matches(stack)) {
                result.add(item);
                merged = true;
            }
        }
        if (merged && !result.isEmpty()) {
            result.sort((a, b) -> Integer.compare(a.seq, b.seq));
        }
        if (result.isEmpty() && this.getRootCategory() != null) {
            Item item = stack.getItem();
            InvTweaksItemTreeItem unknown = new InvTweaksItemTreeItem(BuiltInRegistries.ITEM.getKey(item).toString(), item, null, null,
                    5000 + BuiltInRegistries.ITEM.getId(item) * 16, this.seq++);
            this.addItem(this.getRootCategory().getName(), unknown);
            result.add(unknown);
        }
        return result;
    }

    public List<InvTweaksItemTreeItem> getItems(String keyword) {
        return this.itemsByName.get(keyword);
    }

    public boolean containsItem(String keyword) {
        return this.itemsByName.containsKey(keyword);
    }

    public boolean containsCategory(String keyword) {
        return this.categories.containsKey(keyword);
    }

    public void setRootCategory(InvTweaksItemTreeCategory category) {
        this.rootCategory = category.getName();
        this.categories.put(this.rootCategory, category);
    }

    public void addCategory(String parent, InvTweaksItemTreeCategory category) {
        this.categories.get(parent.toLowerCase()).addCategory(category);
        this.categories.put(category.getName(), category);
    }

    /** A new entry for the loader; the entry gets the next position in the file order. */
    InvTweaksItemTreeItem newItem(String name, Item item, Identifier potion, TagKey<Item> tag, int order) {
        return new InvTweaksItemTreeItem(name, item, potion, tag, order, this.seq++);
    }

    public void addItem(String category, InvTweaksItemTreeItem item) {
        this.categories.get(category.toLowerCase()).addItem(item);
        this.itemsByName.computeIfAbsent(item.getName(), k -> new ArrayList<>()).add(item);
        if (item.getTag() != null) {
            this.tagItems.add(item);
        } else if (item.getItem() != null) {
            this.itemsByItem.computeIfAbsent(item.getItem(), k -> new ArrayList<>()).add(item);
        }
    }

    /**
     * ForgeItemTreeListener (enableForgeTreeAdditions): one category per Forge tool class with the tools of that
     * class. Forge tool classes are the item tags minecraft:pickaxes, minecraft:axes and minecraft:shovels now. The
     * original ordered these entries by harvest level (0-3, so unknown mod tools went first); here they get the
     * order of the tree's own category of that tool, so mod tools sort next to the vanilla ones.
     */
    void addToolClasses() {
        InvTweaksItemTreeCategory root = this.getRootCategory();
        if (root == null) {
            return;
        }
        String[][] classes = {{"pickaxe", "pickaxes"}, {"axe", "axes"}, {"shovel", "shovels"}};
        for (String[] toolClass : classes) {
            String name = "forge_toolclasses_" + toolClass[0];
            this.addCategory(root.getName(), new InvTweaksItemTreeCategory(name));
            int order = Math.max(0, root.findCategoryOrder(toolClass[0]));
            TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace(toolClass[1]));
            this.addItem(name, this.newItem(name + "_item", null, null, tag, order));
        }
    }
}
