package qcraft.compat;

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
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import qcraft.QuantumCraft;
import qcraft.block.Impostors;
import qcraft.item.QBlockItem;
import qcraft.item.QuantumComputerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JEI (NEI was not part of qCraft): the qBlock and entanglement recipes are special recipes JEI cannot list, so they
 * get example crafting recipes (an observer dependent block of six different sides, a quantum block, the frame block
 * of a quantum computer, entangling two qBlocks and two computers) and info pages.
 */
@JeiPlugin
public class QCraftJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return QuantumCraft.id("jei_plugin");
    }

    private static RecipeHolder<CraftingRecipe> shaped(String name, Map<Character, Ingredient> key, ItemStack result, String... pattern) {
        ShapedRecipe recipe = new ShapedRecipe(new Recipe.CommonInfo(false), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.BUILDING, ""),
                ShapedRecipePattern.of(key, pattern), ItemStackTemplate.fromNonEmptyStack(result));
        return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, QuantumCraft.id("jei/" + name)), recipe);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<RecipeHolder<CraftingRecipe>> examples = new ArrayList<>();
        Ingredient eoo = Ingredient.of(QuantumCraft.EOO.get());
        Ingredient eos = Ingredient.of(QuantumCraft.EOS.get());
        Ingredient eoe = Ingredient.of(QuantumCraft.EOE.get());
        // types: 0 bottom, 1 top, 2 north (above), 3 south (below), 4 west (left), 5 east (right)
        int[] odb = {3, 2, 1, 22, 5, 6};
        examples.add(shaped("observer_dependent_block", Map.of('E', eoo, 'T', Ingredient.of(Blocks.GRASS_BLOCK), 'N', Ingredient.of(Blocks.STONE),
                'S', Ingredient.of(Blocks.COBBLESTONE), 'W', Ingredient.of(Blocks.SAND), 'O', Ingredient.of(Blocks.GRAVEL),
                'B', Ingredient.of(Blocks.DIRT)), QBlockItem.create(0, odb, -1, 1), "TN ", "WEO", "BS "));
        int[] quantum = {0, 0, 31, 31, 30, 30};
        examples.add(shaped("quantum_block", Map.of('E', eos, 'G', Ingredient.of(Blocks.GOLD_BLOCK), 'I', Ingredient.of(Blocks.IRON_BLOCK)),
                QBlockItem.create(1, quantum, -1, 1), " G ", "IEI", " G "));
        int o = Impostors.OBSIDIAN;
        int[] frame = {o, o, o, o, o, Impostors.GOLD_BLOCK};
        ItemStack frameBlock = QBlockItem.create(0, frame, -1, 1);
        examples.add(shaped("frame_block", Map.of('E', eoo, 'O', Ingredient.of(Blocks.OBSIDIAN), 'G', Ingredient.of(Blocks.GOLD_BLOCK)),
                frameBlock, "OO ", "OEG", "OO "));
        examples.add(shaped("entangled_block", Map.of('E', eoe, 'Q', DataComponentIngredient.of(false, QBlockItem.create(0, odb, -1, 1))),
                QBlockItem.create(0, odb, 0, 2), "QEQ"));
        examples.add(shaped("entangled_quantum_computer", Map.of('E', eoe, 'C', DataComponentIngredient.of(false, QuantumComputerItem.create(-1, 1))),
                QuantumComputerItem.create(0, 2), "CEC"));
        registration.addRecipes(RecipeTypes.CRAFTING, examples);

        List<ItemStack> qblocks = List.of(QBlockItem.create(0, odb, -1, 1), QBlockItem.create(1, quantum, -1, 1), frameBlock);
        registration.addItemStackInfo(qblocks, Component.translatable("jei.quantumcraft.qblock"));
        registration.addItemStackInfo(List.of(new ItemStack(QuantumCraft.EOE.get())), Component.translatable("jei.quantumcraft.entangled"));
        registration.addItemStackInfo(List.of(QuantumComputerItem.create(-1, 1)), Component.translatable("jei.quantumcraft.quantum_computer"));
        registration.addIngredientInfo(QuantumCraft.QUANTUM_GOGGLES.get(), Component.translatable("jei.quantumcraft.quantum_goggles"));
        registration.addIngredientInfo(QuantumCraft.QUANTUM_ORE_GLOWING.get(), Component.translatable("jei.quantumcraft.quantum_ore"));
    }
}
