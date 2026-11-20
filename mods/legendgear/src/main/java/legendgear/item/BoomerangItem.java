package legendgear.item;

import legendgear.LGFx;
import legendgear.entity.MagicBoomerangEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** ItemMagicBoomerang (750 uses): thrown, it flies back to the slot it came from (MagicBoomerangEntity). */
public class BoomerangItem extends Item {
    public BoomerangItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel) {
            MagicBoomerangEntity boomerang = new MagicBoomerangEntity(level, player, stack.copy());
            boomerang.thrownFromSlot = hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : -1;
            level.addFreshEntity(boomerang);
            LGFx.sound(player, SoundEvents.ARROW_SHOOT, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            player.setItemInHand(hand, ItemStack.EMPTY);
        }
        return InteractionResult.SUCCESS;
    }
}
