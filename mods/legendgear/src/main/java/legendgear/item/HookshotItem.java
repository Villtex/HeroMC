package legendgear.item;

import legendgear.LGFx;
import legendgear.entity.HookshotHookEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

/**
 * ItemHookshot (256 uses): held like a bow; after 3 ticks the hook flies. It holds on to wood and pulls its owner
 * there for as long as the hookshot is held. Letting go after the shot costs one use.
 */
public class HookshotItem extends Item {
    public static final int USE_TIME = 72000;

    public HookshotItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_TIME;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        LGFx.sound(player, SoundEvents.ITEM_BREAK.value(), 0.2F, 2.0F);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {
        if (level instanceof ServerLevel && count == USE_TIME - 3 && player instanceof Player p) {
            level.addFreshEntity(new HookshotHookEntity(level, p));
            LGFx.sound(player, SoundEvents.ARROW_SHOOT, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (timeLeft < USE_TIME - 3) {
            LGFx.damage(stack, 1, entity);
        }
        return true;
    }
}
