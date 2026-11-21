package twilightforest.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import twilightforest.TFEntities;
import twilightforest.entity.LoyalZombie;

import java.util.function.Consumer;

/**
 * ItemTFZombieWand: 9 charges; a right click calls a loyal zombie (strength II for a minute) where the look hits a
 * block within 20 blocks.
 */
public class ZombieScepterItem extends Item {
    public ZombieScepterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!TFItemUtil.hasCharge(stack)) {
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand);
        if (level instanceof ServerLevel server) {
            Vec3 src = player.getEyePosition();
            Vec3 dest = src.add(player.getViewVector(1.0F).scale(20.0));
            BlockHitResult hit = level.clip(new ClipContext(src, dest, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (hit.getType() != HitResult.Type.MISS) {
                LoyalZombie zombie = TFEntities.LOYAL_ZOMBIE.get().create(server, EntitySpawnReason.MOB_SUMMONED);
                if (zombie != null) {
                    zombie.snapTo(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, 1.0F, 1.0F);
                    zombie.setTame(true, false);
                    zombie.setOwner(player);
                    zombie.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 1200, 1));
                    server.addFreshEntity(zombie);
                    TFItemUtil.useCharge(stack, 1);
                }
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 30;
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
