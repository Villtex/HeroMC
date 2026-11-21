package twilightforest.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * ItemTFMinotaurAxe: a player who is sprinting when he hits deals 7 more damage (BONUS_CHARGING_DAMAGE) and shows the
 * enchantment sparks (onEnchantmentCritical).
 */
public class MinotaurAxeItem extends Item {
    private static final float BONUS_CHARGING_DAMAGE = 7.0F;

    public MinotaurAxeItem(Properties properties) {
        super(properties);
    }

    @Override
    public float getAttackDamageBonus(Entity victim, float damage, DamageSource source) {
        if (source.getEntity() instanceof Player player && player.isSprinting()) {
            if (player.level() instanceof ServerLevel) {
                player.magicCrit(victim);
            }
            return BONUS_CHARGING_DAMAGE;
        }
        return 0.0F;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.twilightforest.minotaur_axe.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
