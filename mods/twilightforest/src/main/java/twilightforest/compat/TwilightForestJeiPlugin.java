package twilightforest.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import twilightforest.TFUncrafting;
import twilightforest.TwilightForest;

/**
 * JEI: the uncrafting table crafts like a crafting table (catalyst of the crafting recipes, which it also takes apart:
 * every shaped recipe backwards); an info page explains uncrafting and its level costs.
 */
@JeiPlugin
public class TwilightForestJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return TwilightForest.id("jei");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(RecipeTypes.CRAFTING, TFUncrafting.UNCRAFTING_TABLE_ITEM.get());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(TFUncrafting.UNCRAFTING_TABLE_ITEM.get(), Component.translatable("jei.twilightforest.uncrafting_table"));
    }
}
