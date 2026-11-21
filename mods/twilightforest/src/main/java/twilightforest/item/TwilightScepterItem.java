package twilightforest.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import twilightforest.entity.projectile.TwilightWandBolt;

import java.util.function.Consumer;

/** ItemTFTwilightWand: 99 charges; while held down a bolt every 6 ticks (ghast fireball sound). */
public class TwilightScepterItem extends Item {
    public TwilightScepterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (TFItemUtil.hasCharge(player.getItemInHand(hand))) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int count) {
        if (!TFItemUtil.hasCharge(stack)) {
            user.releaseUsingItem();
        } else if (count % 6 == 0) {
            level.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.GHAST_SHOOT, user.getSoundSource(), 1.0F,
                    (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F);
            if (level instanceof ServerLevel server) {
                TwilightWandBolt bolt = new TwilightWandBolt(server, user);
                bolt.shootOld(user.getXRot(), user.getYRot());
                server.addFreshEntity(bolt);
                TFItemUtil.useCharge(stack, 1);
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        TFItemUtil.chargesTooltip(stack, tooltip);
    }
}
