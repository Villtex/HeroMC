package betterstorage.compat;

import betterstorage.BS;
import betterstorage.BetterStorage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;

/**
 * JEI: the key, lock and key modifying recipes are special recipes (betterstorage:key, betterstorage:lock) JEI can not
 * list, so they get example crafting recipes; keys, crates and backpacks get an info page.
 */
@JeiPlugin
public class BSJeiPlugin implements IModPlugin {
    @Override
    public net.minecraft.resources.Identifier getPluginUid() {
        return BetterStorage.id("jei_plugin");
    }

    private static RecipeHolder<CraftingRecipe> holder(String name, CraftingRecipe recipe) {
        return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, BetterStorage.id("jei/" + name)), recipe);
    }

    private static CraftingRecipe.CraftingBookInfo info() {
        return new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, "");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<RecipeHolder<CraftingRecipe>> examples = new ArrayList<>();
        Ingredient gold = Ingredient.of(Items.GOLD_INGOT);
        Ingredient nugget = Ingredient.of(Items.GOLD_NUGGET);
        Ingredient key = Ingredient.of(BS.KEY.get());
        Ingredient iron = Ingredient.of(Items.IRON_INGOT);
        ItemStack newKey = new ItemStack(BS.KEY.get());
        examples.add(holder("key", new ShapedRecipe(new Recipe.CommonInfo(false), info(),
                ShapedRecipePattern.of(Map.of('o', gold, '.', nugget), ".o", ".o", " o"), ItemStackTemplate.fromNonEmptyStack(newKey))));
        examples.add(holder("key_copy", new ShapedRecipe(new Recipe.CommonInfo(false), info(),
                ShapedRecipePattern.of(Map.of('o', gold, '.', nugget, 'k', key), ".ok", ".o ", " o "), ItemStackTemplate.fromNonEmptyStack(newKey))));
        ItemStack plated = newKey.copy();
        plated.set(BS.IRON_PLATED.get(), Unit.INSTANCE);
        examples.add(holder("key_plate", new ShapelessRecipe(new Recipe.CommonInfo(false), info(), ItemStackTemplate.fromNonEmptyStack(plated),
                List.of(key, iron))));
        ItemStack colored = newKey.copy();
        colored.set(DataComponents.DYED_COLOR, new DyedItemColor(0x993333));
        examples.add(holder("key_dye", new ShapelessRecipe(new Recipe.CommonInfo(false), info(), ItemStackTemplate.fromNonEmptyStack(colored),
                List.of(key, Ingredient.of(Items.DYE.red())))));
        examples.add(holder("lock", new ShapedRecipe(new Recipe.CommonInfo(false), info(),
                ShapedRecipePattern.of(Map.of('g', gold, 'k', key, 'i', iron), " g ", "gkg", "gig"),
                ItemStackTemplate.fromNonEmptyStack(new ItemStack(BS.LOCK.get())))));
        registration.addRecipes(RecipeTypes.CRAFTING, examples);
        registration.addItemStackInfo(new ItemStack(BS.KEY.get()), Component.translatable("jei.betterstorage.key"));
        registration.addItemStackInfo(new ItemStack(BS.CRATE_ITEM.get()), Component.translatable("jei.betterstorage.crate"));
        registration.addItemStackInfo(List.of(new ItemStack(BS.BACKPACK.get()), new ItemStack(BS.ENDER_BACKPACK.get())),
                Component.translatable("jei.betterstorage.backpack"));
    }
}
