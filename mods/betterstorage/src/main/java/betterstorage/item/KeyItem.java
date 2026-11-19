package betterstorage.item;

import betterstorage.BS;
import betterstorage.BSEnchantments;
import betterstorage.BSUtil;
import betterstorage.block.Lockable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * ItemKey: its id (the damage value of the original, {@link BS#KEY_ID}) is set to a random number 1..31999 when it
 * is made or first sits in an inventory; a lock made from it gets the same id. Used on a locked block (sneaking) it
 * takes the lock off and drops it. Enchantments: unlocking opens locks whose id differs only in the low bits,
 * lockpicking opens any lock once per level, morphing turns the key into the key of the next lock it tries; the
 * lock's security lowers all three. Colour (dyes) and iron plating come from the key recipe.
 */
public class KeyItem extends Item implements KeyLike {
    public KeyItem(Properties properties) {
        super(properties);
    }

    public static int keyId(ItemStack stack) {
        Integer id = stack.get(BS.KEY_ID.get());
        return id == null ? 0 : id;
    }

    /** ensureHasDamage. */
    public static void ensureHasId(ItemStack stack) {
        if (keyId(stack) == 0) {
            stack.set(BS.KEY_ID.get(), BSUtil.getInt(1, 32000));
        }
    }

    @Override
    public void onCraftedPostProcess(ItemStack stack, Level level) {
        super.onCraftedPostProcess(stack, level);
        if (!level.isClientSide()) {
            ensureHasId(stack);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        ensureHasId(stack);
    }

    /** onItemUse: on a locked block a fitting key takes the lock off (sneak, else the block opens instead). */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof Lockable lockable) || lockable.getLock().isEmpty()) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        if (level.isClientSide() || player == null) {
            return InteractionResult.SUCCESS;
        }
        ItemStack key = context.getItemInHand();
        ItemStack lock = lockable.getLock();
        boolean success = this.unlock(key, lock, true);
        if (lock.getItem() instanceof LockItem lockType) {
            lockType.onUnlock(lock, key, lockable, player, success);
        }
        if (success) {
            lockable.setLock(ItemStack.EMPTY);
            ItemEntity item = player.drop(lock.copy(), false, net.minecraft.util.Prediction.SERVER_ONLY);
            if (item != null) {
                item.setNoPickUpDelay();
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isNormalKey() {
        return true;
    }

    @Override
    public boolean unlock(ItemStack key, ItemStack lock, boolean useAbility) {
        if (!(lock.getItem() instanceof LockItem)) {
            return false;
        }
        int lockId = keyId(lock);
        int keyId = keyId(key);
        if (lockId == keyId) {
            return true;
        }
        int lockSecurity = BSEnchantments.level(lock, BSEnchantments.SECURITY);
        int unlocking = BSEnchantments.level(key, BSEnchantments.UNLOCKING);
        int lockpicking = BSEnchantments.level(key, BSEnchantments.LOCKPICKING);
        int morphing = BSEnchantments.level(key, BSEnchantments.MORPHING);
        int effectiveUnlocking = Math.max(0, unlocking - lockSecurity);
        int effectiveLockpicking = Math.max(0, lockpicking - lockSecurity);
        int effectiveMorphing = Math.max(0, morphing - lockSecurity);
        if (effectiveUnlocking > 0) {
            int div = (int) Math.pow(2.0, 10 + effectiveUnlocking);
            if (lockId / div == keyId / div) {
                return true;
            }
        }
        if (useAbility && effectiveLockpicking > 0) {
            BSEnchantments.setLevel(key, BSEnchantments.LOCKPICKING, lockpicking - 1);
            return true;
        }
        if (useAbility && effectiveMorphing > 0) {
            key.set(BS.KEY_ID.get(), lockId);
            BSEnchantments.setLevel(key, BSEnchantments.MORPHING, 0);
            return true;
        }
        return false;
    }
}
