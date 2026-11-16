package natura.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import natura.Natura;
import natura.NaturaBlocks;
import natura.NaturaItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * JEI: Natura has only crafting and smelting recipes, which JEI shows by itself. The plugin marks the Natura
 * workbenches as crafting stations (NEI got the workbench as a crafting overlay) and adds info pages for what no
 * recipe shows. Loaded by JEI only; the mod does not need it.
 */
@JeiPlugin
public class NaturaJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(Natura.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        NaturaBlocks.WORKBENCHES.values().forEach(bench -> registration.addCraftingStation(RecipeTypes.CRAFTING, bench.get()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(NaturaBlocks.REDWOOD_SAPLING.get(), Component.translatable("jei.natura.redwood_sapling"));
        registration.addIngredientInfo(NaturaBlocks.BLOODWOOD_SAPLING.get(), Component.translatable("jei.natura.bloodwood_sapling"));
        registration.addIngredientInfo(NaturaItems.SAGUARO_FRUIT.get(), Component.translatable("jei.natura.saguaro_fruit"));
        registration.addIngredientInfo(NaturaItems.COTTON_SEEDS.get(), Component.translatable("jei.natura.cotton"));
        registration.addIngredientInfo(NaturaBlocks.RASPBERRY_BUSH.get(), Component.translatable("jei.natura.berry_bush"));
        registration.addIngredientInfo(NaturaBlocks.BLIGHTBERRY_BUSH.get(), Component.translatable("jei.natura.berry_bush"));
        registration.addIngredientInfo(NaturaBlocks.SULFUR_CLOUD.get(), Component.translatable("jei.natura.sulfur_cloud"));
    }
}
