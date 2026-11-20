package xreliquary.item;

import net.minecraft.world.item.Item;

/** ItemBullet / ItemMagazine: one item per damage value, the shot it holds. */
public class AmmoItem extends XRItem {
    public final ShotType shot;
    public final boolean magazine;

    public AmmoItem(Item.Properties properties, ShotType shot, boolean magazine) {
        super(properties, 1);
        this.shot = shot;
        this.magazine = magazine;
    }
}
