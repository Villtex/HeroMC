package hexxit.worldgen152.core.populate;

/**
 * An item stack as 1.5.2 generated it into a chest: numeric item id, damage value, count and, for enchanted
 * books, the stored enchantment ({@code enchantment} is -1 without one).
 */
public record OldStack(int itemId, int damage, int count, int enchantment, int enchantmentLevel) {
    public OldStack(int itemId, int damage, int count) {
        this(itemId, damage, count, -1, 0);
    }

    public OldStack withCount(int newCount) {
        return new OldStack(this.itemId, this.damage, newCount, this.enchantment, this.enchantmentLevel);
    }
}
