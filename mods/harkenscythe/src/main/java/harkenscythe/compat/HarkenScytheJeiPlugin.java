package harkenscythe.compat;

import harkenscythe.HSItems;
import harkenscythe.HarkenScythe;
import harkenscythe.altar.AltarRecipe;
import harkenscythe.client.AltarRecipesClient;
import harkenscythe.item.GlaiveItem;
import harkenscythe.item.KeeperItem;
import harkenscythe.item.ScytheItem;
import harkenscythe.recipe.SpectralDyeRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * JEI (NEI is not ported): the spectral dye recipe is a special recipe JEI cannot list, so every armour piece gets an
 * example with one dye; info pages explain scythes, glaives, keepers and the other items whose use no recipe shows.
 * All other recipes (attunement included) are ordinary crafting recipes. The altars have a category each.
 */
@JeiPlugin
public class HarkenScytheJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return HarkenScythe.id("jei_plugin");
    }

    private static List<ItemStack> all(Predicate<Item> filter) {
        List<ItemStack> list = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(HarkenScythe.MOD_ID) && filter.test(item)) {
                list.add(new ItemStack(item));
            }
        }
        return list;
    }

    @SafeVarargs
    private static List<ItemStack> of(Supplier<? extends Item>... items) {
        List<ItemStack> list = new ArrayList<>();
        for (Supplier<? extends Item> item : items) {
            list.add(new ItemStack(item.get()));
        }
        return list;
    }

    private static Component info(String key) {
        return Component.translatable("jei." + HarkenScythe.MOD_ID + "." + key);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new AltarCategory(gui, AltarRecipe.Kind.SOUL, HSItems.ALTAR_OF_SOULS.get()),
                new AltarCategory(gui, AltarRecipe.Kind.BLOOD, HSItems.ALTAR_OF_BLOOD.get()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(AltarCategory.SOULS, HSItems.ALTAR_OF_SOULS.get());
        registration.addCraftingStation(AltarCategory.BLOOD, HSItems.ALTAR_OF_BLOOD.get());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<RecipeHolder<AltarRecipe>> souls = new ArrayList<>();
        List<RecipeHolder<AltarRecipe>> blood = new ArrayList<>();
        for (RecipeHolder<AltarRecipe> holder : AltarRecipesClient.recipes()) {
            if (!AltarCategory.inputs(holder.value()).isEmpty()) {
                (holder.value().altar() == AltarRecipe.Kind.SOUL ? souls : blood).add(holder);
            }
        }
        souls.sort(Comparator.comparingInt(h -> h.value().order()));
        blood.sort(Comparator.comparingInt(h -> h.value().order()));
        registration.addRecipes(AltarCategory.SOULS, souls);
        registration.addRecipes(AltarCategory.BLOOD, blood);
        registration.addItemStackInfo(of(HSItems.ALTAR_OF_SOULS, HSItems.ALTAR_OF_BLOOD), info("altar"));
        List<RecipeHolder<CraftingRecipe>> examples = new ArrayList<>();
        for (Map.Entry<Item, Item> pair : SpectralDyeRecipe.pairs().entrySet()) {
            ShapelessRecipe recipe = new ShapelessRecipe(new Recipe.CommonInfo(false), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.EQUIPMENT, ""),
                    ItemStackTemplate.fromNonEmptyStack(new ItemStack(pair.getValue())),
                    List.of(Ingredient.of(pair.getKey()), Ingredient.of(HSItems.SPECTRAL_DYE.get())));
            Identifier id = BuiltInRegistries.ITEM.getKey(pair.getValue());
            examples.add(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, HarkenScythe.id("jei/" + id.getPath())), recipe));
        }
        registration.addRecipes(RecipeTypes.CRAFTING, examples);
        registration.addItemStackInfo(all(item -> item instanceof ScytheItem), info("scythe"));
        registration.addItemStackInfo(all(item -> item instanceof GlaiveItem), info("glaive"));
        registration.addItemStackInfo(all(item -> item instanceof KeeperItem), info("keeper"));
        registration.addItemStackInfo(of(HSItems.NECRONOMICON, HSItems.NECRONOMICON_PAGE), info("necronomicon"));
        registration.addItemStackInfo(of(HSItems.TALISMAN, HSItems.ETHEREAL_TALISMAN), info("talisman"));
        registration.addItemStackInfo(of(HSItems.OVERWORLD_AMULET, HSItems.NETHER_AMULET, HSItems.ETHEREAL_AMULET), info("amulet"));
        registration.addItemStackInfo(of(HSItems.SOUL_CRUCIBLE, HSItems.BLOOD_CRUCIBLE), info("crucible"));
        registration.addItemStackInfo(of(HSItems.CREEPBALL, HSItems.CREEP_BLOCK, HSItems.GERMINATED_BIOMASS_SEED, HSItems.BIOMASS_SEED), info("creep"));
        registration.addItemStackInfo(of(HSItems.SPECTRAL_DYE), info("spectral_dye"));
        registration.addItemStackInfo(of(HSItems.LIVINGMETAL_INGOT), info("livingmetal"));
        registration.addItemStackInfo(of(HSItems.BIOMASS), info("biomass"));
    }
}
