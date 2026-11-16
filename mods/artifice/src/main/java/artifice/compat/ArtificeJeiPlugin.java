package artifice.compat;

import artifice.Artifice;
import artifice.ArtificeBlocks;
import artifice.ArtificeItems;
import artifice.item.BoxItem;
import artifice.item.SledgeParts;
import artifice.item.Upgrade;
import artifice.recipe.UpgradeRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JEI: the box and upgrade recipes are special crafting recipes JEI cannot list, so the plugin shows examples of
 * them as crafting recipes (packing and unpacking a box, every upgrade on a fitting item) and adds info pages for
 * the box, the upgrades, sledges (with the parts of every block), sickles, scaffolding, the detector, the lotus and
 * the flowers. Loaded by JEI only; the mod does not need it.
 */
@JeiPlugin
public class ArtificeJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(Artifice.MOD_ID, "jei_plugin");
    }

    private static RecipeHolder<CraftingRecipe> example(String name, ItemStack result, Ingredient... ingredients) {
        ShapelessRecipe recipe = new ShapelessRecipe(new Recipe.CommonInfo(false), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
                ItemStackTemplate.fromNonEmptyStack(result), List.of(ingredients));
        return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(Artifice.MOD_ID, "jei/" + name)), recipe);
    }

    /** A fitting item for each upgrade (the sharpening kit twice: sharpness on a sword, efficiency on a pickaxe). */
    private static final Map<Upgrade, List<Item>> UPGRADE_EXAMPLES = Map.ofEntries(
            Map.entry(Upgrade.SHARPENING_KIT, List.of(Items.IRON_SWORD, Items.IRON_PICKAXE)),
            Map.entry(Upgrade.REINFORCEMENT, List.of(Items.IRON_PICKAXE)),
            Map.entry(Upgrade.REINFORCED_LIMBS, List.of(Items.BOW)),
            Map.entry(Upgrade.PLAITED_STRING, List.of(Items.BOW)),
            Map.entry(Upgrade.COUNTERWEIGHT, List.of(Items.IRON_SWORD)),
            Map.entry(Upgrade.ARMOR_SPIKES, List.of(Items.IRON_CHESTPLATE)),
            Map.entry(Upgrade.LAMINATED_PADDING, List.of(Items.IRON_CHESTPLATE)),
            Map.entry(Upgrade.QUILTED_COVER, List.of(Items.IRON_CHESTPLATE)),
            Map.entry(Upgrade.ELASTIC_SOLES, List.of(Items.IRON_BOOTS)),
            Map.entry(Upgrade.FIREDAMP, List.of(Items.IRON_CHESTPLATE)),
            Map.entry(Upgrade.ELASTIC_LAYERING, List.of(Items.IRON_CHESTPLATE)),
            Map.entry(Upgrade.SCUBA_TANK, List.of(Items.IRON_HELMET)),
            Map.entry(Upgrade.DIVE_KIT, List.of(Items.IRON_HELMET)));

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<RecipeHolder<CraftingRecipe>> examples = new ArrayList<>();
        // RecipeBox: eight equal items and an empty box -> a box of eight; a filled box alone -> its contents
        Item box = ArtificeItems.BOX.get();
        ItemStack full = BoxItem.packed(box, new ItemStack(Items.COBBLESTONE), 8);
        Ingredient cobble = Ingredient.of(Items.COBBLESTONE);
        examples.add(example("box_pack", full, Ingredient.of(box), cobble, cobble, cobble, cobble, cobble, cobble, cobble, cobble));
        examples.add(example("box_unpack", new ItemStack(Items.COBBLESTONE, 8), DataComponentIngredient.of(true, full)));
        // RecipeUpgrade on a fitting item
        HolderLookup.Provider registries = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.registryAccess() : null;
        if (registries != null) {
            for (Upgrade upgrade : Upgrade.values()) {
                Item item = ArtificeItems.UPGRADES.get(upgrade).get();
                for (Item tool : UPGRADE_EXAMPLES.get(upgrade)) {
                    ItemStack result = UpgradeRecipe.apply(CraftingInput.of(2, 1, List.of(new ItemStack(tool), new ItemStack(item))), registries);
                    if (!result.isEmpty()) {
                        examples.add(example("upgrade/" + upgrade.id + "_" + tool.builtInRegistryHolder().key().identifier().getPath(), result,
                                Ingredient.of(tool), Ingredient.of(item)));
                    }
                }
            }
        }
        registration.addRecipes(RecipeTypes.CRAFTING, examples);

        registration.addIngredientInfo(box, Component.translatable("jei.artifice.box"));
        List<ItemStack> upgrades = ArtificeItems.upgradeList().stream().map(u -> new ItemStack(u.get())).toList();
        registration.addItemStackInfo(upgrades, Component.translatable("jei.artifice.upgrade"));
        List<ItemStack> sledges = ArtificeItems.SLEDGES.stream().map(s -> new ItemStack(s.get())).toList();
        registration.addItemStackInfo(sledges, sledgeInfo());
        List<ItemStack> sickles = ArtificeItems.SICKLES.stream().map(s -> new ItemStack(s.get())).toList();
        registration.addItemStackInfo(sickles, Component.translatable("jei.artifice.sickle"));
        List<ItemStack> scaffolding = ArtificeBlocks.SCAFFOLDING.stream().map(s -> new ItemStack(s.get())).toList();
        registration.addItemStackInfo(scaffolding, Component.translatable("jei.artifice.scaffolding"));
        registration.addIngredientInfo(ArtificeBlocks.DETECTOR.get(), Component.translatable("jei.artifice.detector"));
        registration.addItemStackInfo(List.of(new ItemStack(ArtificeBlocks.LOTUS.get()), new ItemStack(ArtificeBlocks.LOTUS_LILY.get())),
                Component.translatable("jei.artifice.lotus"));
        List<DeferredBlock<? extends Block>> flora = List.of(ArtificeBlocks.BLUEBELL, ArtificeBlocks.ORCHID, ArtificeBlocks.IRIS, ArtificeBlocks.LOTUS);
        registration.addItemStackInfo(flora.stream().map(f -> new ItemStack(f.get())).toList(), Component.translatable("jei.artifice.flowers"));
    }

    /** "Breaks these blocks into their parts" and one line per block: "Crafting Table: 4 Oak Planks, ...". */
    private static Component[] sledgeInfo() {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("jei.artifice.sledge"));
        for (Map.Entry<Block, SledgeParts.Entry> e : SledgeParts.all().entrySet()) {
            MutableComponent parts = Component.empty();
            // equal parts in one entry (the anvil lists its blocks and ingots one by one)
            List<ItemStack> list = new ArrayList<>();
            for (ItemStack part : e.getValue().parts()) {
                if (!list.isEmpty() && ItemStack.isSameItem(list.getLast(), part)) {
                    list.getLast().grow(part.getCount());
                } else {
                    list.add(part.copy());
                }
            }
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    parts.append(", ");
                }
                parts.append(list.get(i).getCount() + " ").append(list.get(i).getHoverName());
            }
            lines.add(Component.translatable("jei.artifice.sledge.entry", e.getKey().getName(), parts));
        }
        return lines.toArray(Component[]::new);
    }
}
