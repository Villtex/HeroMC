package betterstorage.block;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** ILockable: something a lock can be put on. */
public interface Lockable {
    ItemStack getLock();

    boolean isLockValid(ItemStack lock);

    void setLock(ItemStack lock);

    /** Can the player use it without unlocking (someone else has it open already)? */
    boolean canUse(Player player);

    /** The trigger enchantment of a lock that was tampered with. */
    void applyTrigger();
}
