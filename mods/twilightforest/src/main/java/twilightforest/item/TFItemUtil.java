package twilightforest.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/** Small helpers for the items of 1.5.2. */
public final class TFItemUtil {
    private TFItemUtil() {
    }

    /**
     * damageItem of the scepters: they never break (the damage stops at the maximum, the empty scepter is recharged in
     * the crafting grid).
     */
    public static void useCharge(ItemStack stack, int amount) {
        stack.setDamageValue(Math.min(stack.getMaxDamage(), stack.getDamageValue() + amount));
    }

    public static boolean hasCharge(ItemStack stack) {
        return stack.getDamageValue() < stack.getMaxDamage();
    }

    /** damageItem: the stack breaks when it is used up. */
    public static void damage(ItemStack stack, int amount, LivingEntity user) {
        if (user.level() instanceof ServerLevel) {
            EquipmentSlot slot = user.getMainHandItem() == stack ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            stack.hurtAndBreak(amount, user, slot);
        }
    }

    /** "x charges left". */
    public static void chargesTooltip(ItemStack stack, Consumer<Component> tooltip) {
        tooltip.accept(Component.translatable("item.twilightforest.charges", stack.getMaxDamage() - stack.getDamageValue()).withStyle(ChatFormatting.GRAY));
    }

    /** The box of radius r around the point range blocks ahead of the eyes (fan, crumble horn, transformation powder). */
    public static AABB effectBox(Player player, double range, double radius) {
        Vec3 dest = player.getEyePosition().add(player.getLookAngle().scale(range));
        return new AABB(dest.x - radius, dest.y - radius, dest.z - radius, dest.x + radius, dest.y + radius, dest.z + radius);
    }
}
