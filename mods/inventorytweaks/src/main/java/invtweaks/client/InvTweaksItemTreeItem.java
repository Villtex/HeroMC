package invtweaks.client;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

/**
 * InvTweaksItemTreeItem. In 1.5.2 an entry was an item id and a damage value (or the wildcard). In 26.3 it is an
 * item, optionally restricted to one potion (potions were damage values), or an item tag (was an ore dictionary
 * name). {@code item} is null for an id of a mod that is not installed: the keyword stays valid but matches nothing.
 */
public final class InvTweaksItemTreeItem {
    private final String name;
    private final Item item;
    private final Identifier potion;
    private final TagKey<Item> tag;
    private final int order;
    /** Position in the tree file: getItems lists the entries of a stack in this order, as the original did. */
    final int seq;

    InvTweaksItemTreeItem(String name, Item item, Identifier potion, TagKey<Item> tag, int order, int seq) {
        this.name = name;
        this.item = item;
        this.potion = potion;
        this.tag = tag;
        this.order = order;
        this.seq = seq;
    }

    public String getName() {
        return this.name;
    }

    public Item getItem() {
        return this.item;
    }

    public Identifier getPotion() {
        return this.potion;
    }

    public TagKey<Item> getTag() {
        return this.tag;
    }

    public int getOrder() {
        return this.order;
    }

    /** True if the stack is this entry. */
    public boolean matches(ItemStack stack) {
        if (this.tag != null) {
            return stack.is(this.tag);
        }
        return this.item != null && stack.getItem() == this.item && (this.potion == null || this.potion.equals(potionOf(stack)));
    }

    /** InvTweaksItemTreeItem.equals: same id and (wildcard damage or same damage). */
    public boolean covers(InvTweaksItemTreeItem other) {
        if (this == other) {
            return true;
        }
        if (this.tag != null) {
            return other.tag == null && other.item != null && other.item.builtInRegistryHolder().is(this.tag);
        }
        return other.tag == null && this.item != null && this.item == other.item
                && (this.potion == null || this.potion.equals(other.potion));
    }

    /** The registry id of the item, for rules by id ("D5 minecraft:stone"). */
    String itemId() {
        return this.item == null ? null : BuiltInRegistries.ITEM.getKey(this.item).toString();
    }

    static Identifier potionOf(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null || contents.potion().isEmpty()) {
            return null;
        }
        return contents.potion().get().unwrapKey().map(key -> key.identifier()).orElse(null);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
