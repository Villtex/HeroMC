package harkenscythe.item;

import harkenscythe.HSItems;
import harkenscythe.HarkenScythe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * ItemHSTalisman: a talisman in the inventory saves its owner from a deadly hit (harkenscythe.event.HSEvents) for
 * 10 souls and 20 blood (Soul Attuned: 20 souls, and regeneration and speed; Blood Attuned: 40 blood, and strength and
 * resistance). It heals fully, fills the food bar, clears all effects and puts out fire. The talisman breaks at once,
 * the ethereal one after five rescues.
 */
public class TalismanItem extends Item {
    public TalismanItem(Properties properties) {
        super(properties);
    }

    private static boolean attuned(Level level, ItemStack stack, net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key) {
        return level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).get(key)
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack) > 0).orElse(false);
    }

    /** {soul cost, blood cost} of a talisman (also of the amulets, halved). */
    static int[] cost(Level level, ItemStack stack, int souls, int blood) {
        if (attuned(level, stack, HarkenScythe.SOUL_ATTUNED)) {
            return new int[] {souls * 2, 0};
        }
        if (attuned(level, stack, HarkenScythe.BLOOD_ATTUNED)) {
            return new int[] {0, blood * 2};
        }
        return new int[] {souls, blood};
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        int left = stack.getMaxDamage() - stack.getDamageValue();
        builder.accept(Component.translatable(left > 1 ? "tooltip.harkenscythe_core.talisman.breaks_after_plural"
                : "tooltip.harkenscythe_core.talisman.breaks_after", left).withStyle(ChatFormatting.GRAY));
        String key = attunement(stack);
        if (key != null) {
            builder.accept(Component.translatable("tooltip.harkenscythe_core.talisman." + key).withStyle(ChatFormatting.GRAY));
        }
        builder.accept(Component.translatable("tooltip.harkenscythe_core.talisman.cost" + (key == null ? "" : "." + key)).withStyle(ChatFormatting.GRAY));
    }

    /** "soul" / "blood" for an attuned stack, without a level at hand (tooltips): by enchantment id. */
    static String attunement(ItemStack stack) {
        for (var entry : stack.getEnchantments().entrySet()) {
            if (entry.getKey().is(HarkenScythe.SOUL_ATTUNED)) {
                return "soul";
            }
            if (entry.getKey().is(HarkenScythe.BLOOD_ATTUNED)) {
                return "blood";
            }
        }
        return null;
    }

    /**
     * talismanActivate: if a talisman in the main inventory (talismans before ethereal talismans) can be paid for,
     * pays, saves the player and returns true.
     */
    public static boolean activate(Player player) {
        Level level = player.level();
        NonNullList<ItemStack> items = player.getInventory().getNonEquipmentItems();
        for (Item kind : new Item[] {HSItems.TALISMAN.get(), HSItems.ETHEREAL_TALISMAN.get()}) {
            for (ItemStack stack : items) {
                if (!stack.is(kind)) {
                    continue;
                }
                int[] cost = cost(level, stack, 10, 20);
                if (!Essence.has(player, cost[0], false) || !Essence.has(player, cost[1], true)) {
                    continue;
                }
                Essence.consume(player, cost[0], false);
                Essence.consume(player, cost[1], true);
                special(level, stack, player);
                player.heal(player.getMaxHealth());
                player.getFoodData().eat(20, 0.0F);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BREATH, SoundSource.PLAYERS, 1.0F, 1.0F);
                if (kind == HSItems.TALISMAN.get()) {
                    stack.shrink(1);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                } else {
                    stack.setDamageValue(stack.getDamageValue() + 1);
                    if (stack.getDamageValue() >= 5) {
                        stack.shrink(1);
                        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
                return true;
            }
        }
        return false;
    }

    /** talismanSpecial. */
    private static void special(Level level, ItemStack stack, Player player) {
        player.removeAllEffects();
        player.clearFire();
        if (attuned(level, stack, HarkenScythe.SOUL_ATTUNED)) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 0));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 600, 0));
        } else if (attuned(level, stack, HarkenScythe.BLOOD_ATTUNED)) {
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 600, 0));
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 600, 0));
        }
    }
}
