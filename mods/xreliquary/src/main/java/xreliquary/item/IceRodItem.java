package xreliquary.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import xreliquary.XRFx;
import xreliquary.entity.ThrownEntity;

/** ItemIceRod: stores snowballs (257) and throws them as fast ice balls (2 damage, 8 to blazes). */
public class IceRodItem extends ChargeItem {
    public IceRodItem(Properties properties) {
        super(properties, 2);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (owner instanceof Player player) {
            refuel(stack, player, Items.SNOWBALL, 1, stack.getMaxDamage() - 1);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || stack.getDamageValue() == 0) {
            return InteractionResult.PASS;
        }
        if (stack.getDamageValue() < stack.getMaxDamage() - 1) {
            XRFx.sound(player, SoundEvents.ARROW_SHOOT, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            level.addFreshEntity(new ThrownEntity(level, player, ThrownEntity.Kind.ICE));
            stack.setDamageValue(stack.getDamageValue() == stack.getMaxDamage() - 2 ? 0 : stack.getDamageValue() + 1);
        }
        return InteractionResult.SUCCESS;
    }
}
