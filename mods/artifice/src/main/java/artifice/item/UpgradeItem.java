package artifice.item;

import net.minecraft.world.item.Item;

/** ItemUpgrade: one of the {@link Upgrade}s, applied in the crafting grid (UpgradeRecipe). */
public class UpgradeItem extends Item {
    private final Upgrade upgrade;

    public UpgradeItem(Properties properties, Upgrade upgrade) {
        super(properties);
        this.upgrade = upgrade;
    }

    public Upgrade upgrade() {
        return this.upgrade;
    }
}
