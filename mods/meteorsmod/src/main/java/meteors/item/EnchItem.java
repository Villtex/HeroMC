package meteors.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * ItemEnchArmor / ItemEnchAxe / ItemEnchSword / ...: crafting the item puts its enchantment on (onCreated), and so
 * does a right click while it has none; until then the tooltip says so.
 */
public class EnchItem extends Item {
    private final ResourceKey<Enchantment> enchantment;
    private final int level;

    public EnchItem(Properties properties, ResourceKey<Enchantment> enchantment, int level) {
        super(properties);
        this.enchantment = enchantment;
        this.level = level;
    }

    /** addEnchantment: a second enchantment of the same kind is added again in 1.5.2; today it only raises the level. */
    public void addEnchantment(ItemStack stack, Level level) {
        level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(this.enchantment)
                .ifPresent(holder -> stack.enchant((Holder<Enchantment>) holder, this.level));
    }

    @Override
    public void onCraftedBy(ItemStack stack, Player player) {
        this.addEnchantment(stack, player.level());
        super.onCraftedBy(stack, player);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEnchanted()) {
            this.addEnchantment(stack, level);
            InteractionResult result = super.use(level, player, hand);
            return result == InteractionResult.PASS ? InteractionResult.SUCCESS : result;
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (!stack.isEnchanted()) {
            tooltip.accept(Component.translatable("enchantment.meteors.addEnch.one").withStyle(ChatFormatting.DARK_RED));
            tooltip.accept(Component.translatable("enchantment.meteors.addEnch.two").withStyle(ChatFormatting.DARK_RED));
        }
    }
}
