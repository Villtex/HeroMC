package invtweaks.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Item;

/** InvTweaksItemTreeCategory: a keyword with items and sub categories. */
public final class InvTweaksItemTreeCategory {
    private final Map<Item, List<InvTweaksItemTreeItem>> items = new HashMap<>();
    private final List<InvTweaksItemTreeItem> tagItems = new ArrayList<>();
    private final List<String> matchingItems = new ArrayList<>();
    private final List<InvTweaksItemTreeCategory> subCategories = new ArrayList<>();
    private final String name;
    private int order = -1;

    public InvTweaksItemTreeCategory(String name) {
        this.name = name != null ? name.toLowerCase() : null;
    }

    public boolean contains(InvTweaksItemTreeItem item) {
        List<InvTweaksItemTreeItem> list = item.getItem() != null ? this.items.get(item.getItem()) : null;
        if (list != null) {
            for (InvTweaksItemTreeItem own : list) {
                if (own.covers(item)) {
                    return true;
                }
            }
        }
        for (InvTweaksItemTreeItem own : this.tagItems) {
            if (own.covers(item)) {
                return true;
            }
        }
        for (InvTweaksItemTreeCategory category : this.subCategories) {
            if (category.contains(item)) {
                return true;
            }
        }
        return false;
    }

    public void addCategory(InvTweaksItemTreeCategory category) {
        this.subCategories.add(category);
    }

    public void addItem(InvTweaksItemTreeItem item) {
        if (item.getTag() != null) {
            this.tagItems.add(item);
        } else if (item.getItem() != null) {
            this.items.computeIfAbsent(item.getItem(), k -> new ArrayList<>()).add(item);
        }
        this.matchingItems.add(item.getName());
        if (this.order == -1 || this.order > item.getOrder()) {
            this.order = item.getOrder();
        }
    }

    public int getCategoryOrder() {
        if (this.order != -1) {
            return this.order;
        }
        for (InvTweaksItemTreeCategory category : this.subCategories) {
            int order = category.getCategoryOrder();
            if (order != -1) {
                return order;
            }
        }
        return -1;
    }

    public int findCategoryOrder(String keyword) {
        if (keyword.equals(this.name)) {
            return this.getCategoryOrder();
        }
        for (InvTweaksItemTreeCategory category : this.subCategories) {
            int order = category.findCategoryOrder(keyword);
            if (order != -1) {
                return order;
            }
        }
        return -1;
    }

    public int findKeywordDepth(String keyword) {
        if (this.name.equals(keyword)) {
            return 0;
        }
        if (this.matchingItems.contains(keyword)) {
            return 1;
        }
        for (InvTweaksItemTreeCategory category : this.subCategories) {
            int depth = category.findKeywordDepth(keyword);
            if (depth != -1) {
                return depth + 1;
            }
        }
        return -1;
    }

    public Collection<InvTweaksItemTreeCategory> getSubCategories() {
        return this.subCategories;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name + " (" + this.subCategories.size() + " cats, " + this.matchingItems.size() + " items)";
    }
}
