package asgardshield.compat;

import asgardshield.AsgardShield;
import asgardshield.item.ItemAsgardShield;
import asgardshield.item.ItemZedSword;
import asgardshield.recipe.GildShieldRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JEI: the gilding recipe is a special recipe JEI cannot list, so every shield (also those of the addons) gets an
 * example crafting recipe; shields and giant swords get an info page on guarding. Dyeing uses the vanilla dye
 * recipes, which JEI shows itself.
 */
@JeiPlugin
public class AsgardShieldJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(AsgardShield.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<RecipeHolder<CraftingRecipe>> examples = new ArrayList<>();
        List<ItemStack> shields = new ArrayList<>();
        List<ItemStack> swords = new ArrayList<>();
        Ingredient gold = Ingredient.of(Items.GOLD_INGOT);
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof ItemZedSword) {
                swords.add(new ItemStack(item));
            }
            if (!(item instanceof ItemAsgardShield shield)) {
                continue;
            }
            shields.add(new ItemStack(item));
            ItemStack result = GildShieldRecipe.gild(new ItemStack(item));
            if (result.isEmpty()) {
                continue;
            }
            ShapedRecipePattern pattern = ShapedRecipePattern.of(Map.of('G', gold, 'S', Ingredient.of(shield)), "GGG", "GSG", " G ");
            ShapedRecipe recipe = new ShapedRecipe(new Recipe.CommonInfo(false), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.EQUIPMENT, ""),
                    pattern, ItemStackTemplate.fromNonEmptyStack(result));
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            examples.add(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(AsgardShield.MOD_ID,
                    "jei/gild/" + id.getNamespace() + "/" + id.getPath())), recipe));
        }
        registration.addRecipes(RecipeTypes.CRAFTING, examples);
        registration.addItemStackInfo(shields, Component.translatable("jei." + AsgardShield.MOD_ID + ".shield"));
        registration.addItemStackInfo(swords, Component.translatable("jei." + AsgardShield.MOD_ID + ".giant_sword"));
    }
}
