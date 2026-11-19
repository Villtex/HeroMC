package betterstorage.item;

import net.minecraft.world.item.ItemStack;

/** IKey: keys and keyrings. */
public interface KeyLike {
    /** A plain key (can go on a keyring). */
    boolean isNormalKey();

    /** Does {@code key} open {@code lock}? With {@code useAbility} lockpicking and morphing may be used up. */
    boolean unlock(ItemStack key, ItemStack lock, boolean useAbility);
}
