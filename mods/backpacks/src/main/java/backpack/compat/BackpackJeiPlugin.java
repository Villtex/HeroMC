package backpack.compat;

import backpack.Backpack;
import backpack.BackpackItem;
import backpack.inventory.WorkbenchBackpackMenu;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
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
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JEI (instead of the NEI overlay of the original): the two special crafting recipes as examples in the crafting
 * category (recolouring with every dye and with leather / tanned leather, enlarging every small backpack), the
 * workbench backpacks as crafting stations and the "+" transfer into their grid (with the player inventory and the
 * backpack's storage as source). Loaded by JEI only.
 */
@JeiPlugin
public class BackpackJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return Backpack.id("jei_plugin");
    }

    private static RecipeHolder<CraftingRecipe> holder(String name, CraftingRecipe recipe) {
        return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, Backpack.id("jei/" + name)), recipe);
    }

    private static CraftingRecipe.CraftingBookInfo info() {
        return new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.EQUIPMENT, "");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<RecipeHolder<CraftingRecipe>> examples = new ArrayList<>();
        for (boolean big : new boolean[]{false, true}) {
            Item[] normal = new Item[17];
            for (int c = 0; c <= 16; c++) {
                normal[c] = Backpack.backpack(BackpackItem.Kind.NORMAL, c, big);
            }
            Ingredient any = Ingredient.of(normal);
            String size = big ? "big_" : "";
            for (int c = 0; c < 16; c++) {
                Item dye = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(DyeColor.byId(15 - c).getName() + "_dye"));
                examples.add(holder("recolor/" + size + BackpackItem.COLORS[c], new ShapelessRecipe(new Recipe.CommonInfo(false), info(),
                        ItemStackTemplate.fromNonEmptyStack(new ItemStack(normal[c])), List.of(any, Ingredient.of(dye)))));
            }
            Item leather = big ? Backpack.TANNED_LEATHER.get() : Items.LEATHER;
            examples.add(holder("recolor/" + size + "plain", new ShapelessRecipe(new Recipe.CommonInfo(false), info(),
                    ItemStackTemplate.fromNonEmptyStack(new ItemStack(normal[16])), List.of(any, Ingredient.of(leather)))));
        }
        Map<Character, Ingredient> key = Map.of('L', Ingredient.of(Backpack.TANNED_LEATHER.get()));
        List<BackpackItem> small = Backpack.backpacks().stream().filter(b -> !b.big() && b.kind() != BackpackItem.Kind.ENDER).toList();
        for (BackpackItem item : small) {
            Map<Character, Ingredient> k = new java.util.HashMap<>(key);
            k.put('B', Ingredient.of(item));
            BackpackItem result = Backpack.backpack(item.kind(), item.color(), true);
            examples.add(holder("enhance/" + BuiltInRegistries.ITEM.getKey(item).getPath(), new ShapedRecipe(new Recipe.CommonInfo(false), info(),
                    ShapedRecipePattern.of(k, "LLL", "LBL", "LLL"), ItemStackTemplate.fromNonEmptyStack(new ItemStack(result)))));
        }
        registration.addRecipes(RecipeTypes.CRAFTING, examples);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(RecipeTypes.CRAFTING, Backpack.backpack(BackpackItem.Kind.WORKBENCH, -1, false),
                Backpack.backpack(BackpackItem.Kind.WORKBENCH, -1, true));
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new IRecipeTransferInfo<WorkbenchBackpackMenu, RecipeHolder<CraftingRecipe>>() {
            @Override
            public Class<? extends WorkbenchBackpackMenu> getContainerClass() {
                return WorkbenchBackpackMenu.class;
            }

            @Override
            public Optional<MenuType<WorkbenchBackpackMenu>> getMenuType() {
                return Optional.of(Backpack.WORKBENCH_BACKPACK_MENU.get());
            }

            @Override
            public IRecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
                return RecipeTypes.CRAFTING;
            }

            @Override
            public boolean canHandle(WorkbenchBackpackMenu menu, RecipeHolder<CraftingRecipe> recipe) {
                return true;
            }

            @Override
            public List<Slot> getRecipeSlots(WorkbenchBackpackMenu menu, RecipeHolder<CraftingRecipe> recipe) {
                return menu.slots.subList(WorkbenchBackpackMenu.GRID_START, WorkbenchBackpackMenu.INV_START);
            }

            @Override
            public List<Slot> getInventorySlots(WorkbenchBackpackMenu menu, RecipeHolder<CraftingRecipe> recipe) {
                return menu.slots.subList(WorkbenchBackpackMenu.INV_START, menu.slots.size());
            }
        });
    }
}
