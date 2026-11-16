package asgardshield.item;

import asgardshield.AsgardShield;
import asgardshield.guard.GuardEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * ItemAsgardShield: a shield held like a weapon. Guarding (right-click) takes the hits instead of the player and
 * costs durability ({@link asgardshield.guard.ASShieldPerks}); every shield has a perk and a weakness (1 wood, 2 stone,
 * 3 iron, 4 diamond, 5 nether quartz, 6 ender, 7 skull, 8 patchwork, 9 livingmetal, 10 biomass). The overlay is
 * coloured by the colour API (config) or by dyes, like leather armour; gold ingots turn a shield into its gilded
 * version ({@link asgardshield.recipe.GildShieldRecipe}). Letting go of the guard switches back to the first sword of
 * the hotbar.
 */
public class ItemAsgardShield extends AbstractGuardItem {
    private final int perk;
    private final boolean gilded;
    private final String color;
    private final @Nullable Supplier<? extends Item> gildedVariant;

    /**
     * @param perk          asgardShieledPerk (1-10)
     * @param color         key of the default overlay colour in the colour API config (wood, stone, ... biomass)
     * @param gildedVariant the gilded shield made from this one, null for a gilded shield
     */
    public ItemAsgardShield(Properties properties, int perk, boolean gilded, GuardSound sound, float damageMultiplier, float knockback,
            Special special, String color, @Nullable Supplier<? extends Item> gildedVariant) {
        super(properties, sound, damageMultiplier, knockback, special);
        this.perk = perk;
        this.gilded = gilded;
        this.color = color;
        this.gildedVariant = gildedVariant;
    }

    public int perk() {
        return this.perk;
    }

    /** gBooster / getGildedGuardGuage: stronger perks, a slower Guard Gauge. */
    public boolean gilded() {
        return this.gilded;
    }

    public String colorKey() {
        return this.color;
    }

    public @Nullable Item gildedVariant() {
        return this.gildedVariant == null ? null : this.gildedVariant.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        String base = "tooltip." + AsgardShield.MOD_ID + ".";
        tooltip.accept(Component.translatable(base + "perk", Component.translatable(base + "perk." + this.perk)).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable(base + "weakness", Component.translatable(base + "weakness." + this.perk))
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.empty());
    }

    @Override
    public void onStoppedGuarding(Player player) {
        GuardEvents.switchToSword(player);
    }
}
