package hexxit.worldgen152.core.populate;

/**
 * An item stack as 1.5.2 generated it into a chest: numeric item id, damage value, count and, for enchanted
 * books, the stored enchantment ({@code enchantment} is -1 without one). {@code applied} holds enchantments put
 * on the item itself (ItemStack.addEnchantment) as pairs of enchantment id and level, in the order they were added.
 */
public record OldStack(int itemId, int damage, int count, int enchantment, int enchantmentLevel, int[] applied) {
    private static final int[] NONE = new int[0];

    public OldStack(int itemId, int damage, int count) {
        this(itemId, damage, count, -1, 0, NONE);
    }

    public OldStack(int itemId, int damage, int count, int enchantment, int enchantmentLevel) {
        this(itemId, damage, count, enchantment, enchantmentLevel, NONE);
    }

    public OldStack withCount(int newCount) {
        return new OldStack(this.itemId, this.damage, newCount, this.enchantment, this.enchantmentLevel, this.applied);
    }

    /** ItemStack.addEnchantment: the same stack with one more enchantment on it. */
    public OldStack withEnchantment(int id, int level) {
        int[] more = java.util.Arrays.copyOf(this.applied, this.applied.length + 2);
        more[this.applied.length] = id;
        more[this.applied.length + 1] = level;
        return new OldStack(this.itemId, this.damage, this.count, this.enchantment, this.enchantmentLevel, more);
    }
}
