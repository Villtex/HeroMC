package xreliquary.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import xreliquary.Reliquary;
import xreliquary.XRItems;
import xreliquary.item.VoidContents;

import java.util.List;

/**
 * JEI: the alkahestry and alkahest recipes are ordinary crafting recipes; the plugin explains what alkahest pays for
 * each item and the tome staying in the grid, and shows the void satchel code recipe with an example tear.
 * Loaded by JEI only.
 */
@JeiPlugin
public class ReliquaryJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return Reliquary.id("jei_plugin");
    }

    private static RecipeHolder<CraftingRecipe> example(String name, ItemStack result, Ingredient... ingredients) {
        ShapelessRecipe recipe = new ShapelessRecipe(new Recipe.CommonInfo(false), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
                ItemStackTemplate.fromNonEmptyStack(result), List.of(ingredients));
        return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, Reliquary.id("jei/" + name)), recipe);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ItemStack tear = new ItemStack(XRItems.VOID_TEAR.get());
        VoidContents.set(tear, Items.COBBLESTONE, 64, -1);
        ItemStack satchel = new ItemStack(XRItems.VOID_SATCHEL.get());
        VoidContents.set(satchel, Items.COBBLESTONE, 64, 64);
        ItemStack bigger = new ItemStack(XRItems.VOID_SATCHEL.get());
        VoidContents.set(bigger, Items.COBBLESTONE, 64, 256);
        Ingredient leather = Ingredient.of(Items.LEATHER);
        Ingredient empty = Ingredient.of(XRItems.EMPTY_VOID_TEAR.get());
        registration.addRecipes(RecipeTypes.CRAFTING, List.of(
                example("void_satchel", satchel, leather, Ingredient.of(Items.STRING), leather, leather, DataComponentIngredient.of(true, tear), leather,
                        leather, leather, leather),
                example("void_satchel_upgrade", bigger, empty, empty, empty, DataComponentIngredient.of(true, satchel))));
        registration.addIngredientInfo(XRItems.ALKAHEST.get(), Component.translatable("jei.xreliquary.alkahest"));
        registration.addIngredientInfo(XRItems.ALKAHESTRY_TOME.get(), Component.translatable("jei.xreliquary.alkahestry_tome"));
        registration.addIngredientInfo(XRItems.GLOWING_WATER.get(), Component.translatable("jei.xreliquary.glowing_water"));
        registration.addIngredientInfo(XRItems.VOID_SATCHEL.get(), Component.translatable("jei.xreliquary.void_satchel"));
    }
}
