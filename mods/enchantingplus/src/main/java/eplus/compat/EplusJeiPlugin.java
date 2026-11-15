package eplus.compat;

import eplus.EnchantHelper;
import eplus.EnchantingPlus;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * JEI (replaces NEI): an info page for the enchanting table and the category "Enchanting Plus" with one page per
 * enchantment of the table: the items it goes on and the cost of each level for the shown item.
 */
@JeiPlugin
public class EplusJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(EnchantingPlus.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new EnchantingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(EnchantingCategory.TYPE, Items.ENCHANTING_TABLE);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(Items.ENCHANTING_TABLE, Component.translatable("jei.eplus.enchanting_table"));
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        HolderLookup.Provider registries = minecraft.level.registryAccess();
        List<EnchantingCategory.TableRecipe> recipes = new ArrayList<>();
        for (Holder<Enchantment> enchantment : EnchantHelper.tableEnchantments(registries)) {
            List<ItemStack> items = new ArrayList<>();
            for (Item item : BuiltInRegistries.ITEM) {
                ItemStack stack = item.getDefaultInstance();
                if (!stack.is(EnchantHelper.BLACKLIST) && EnchantHelper.isItemEnchantable(stack) && EnchantHelper.canEnchantItem(stack, enchantment)) {
                    items.add(stack);
                }
            }
            if (!items.isEmpty()) {
                recipes.add(new EnchantingCategory.TableRecipe(enchantment, items));
            }
        }
        registration.addRecipes(EnchantingCategory.TYPE, recipes);
    }
}
