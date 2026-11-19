package betterstorage.item;

import betterstorage.BSEnchantments;
import betterstorage.block.Lockable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * ItemLock: put onto a lockable block without a lock (the whole stack goes). Its id ({@link KeyItem#keyId}) is the
 * id of the key it was made from. A failed unlock (power 2 with a wrong key, 1 with none) and hitting the block
 * (power 3) shock the player (magic damage shock * min(power, 2) * 5 / 2, from level 3 also fire for
 * shock * 2 * power seconds) and fire the trigger.
 */
public class LockItem extends Item {
    public LockItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof Lockable lockable) || !lockable.getLock().isEmpty()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        if (!lockable.isLockValid(stack)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            lockable.setLock(stack.copy());
            Player player = context.getPlayer();
            if (player != null) {
                player.setItemInHand(context.getHand() == null ? InteractionHand.MAIN_HAND : context.getHand(), ItemStack.EMPTY);
            } else {
                stack.setCount(0);
            }
        }
        return InteractionResult.SUCCESS;
    }

    public void onUnlock(ItemStack lock, ItemStack key, Lockable lockable, Player player, boolean success) {
        if (!success) {
            this.applyEffects(lock, lockable, player, key.isEmpty() ? 1 : 2);
        }
    }

    public void applyEffects(ItemStack lock, Lockable lockable, Player player, int power) {
        int shock = BSEnchantments.level(lock, BSEnchantments.SHOCK);
        int trigger = BSEnchantments.level(lock, BSEnchantments.TRIGGER);
        if (shock > 0 && player.level() instanceof ServerLevel level) {
            player.hurtServer(level, player.damageSources().magic(), shock * Math.min(power, 2) * 5 / 2);
            if (shock >= 3) {
                player.igniteForSeconds(shock * 2 * power);
            }
        }
        if (trigger > 0) {
            lockable.applyTrigger();
        }
    }
}
