package tconstruct.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import tconstruct.TBlocks;
import tconstruct.TConstruct;
import tconstruct.TItems;
import tconstruct.items.PatternBuilder;
import tconstruct.items.PatternItem;
import tconstruct.modifiers.Modifiers;
import tconstruct.modifiers.ToolMod;
import tconstruct.tools.InfiTool;
import tconstruct.tools.PartType;
import tconstruct.tools.TMaterial;
import tconstruct.tools.ToolBuilder;
import tconstruct.tools.ToolType;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI (NEI is not ported): the part builder (pattern + material -> part and leftover), tool building on the tool
 * station / forge (the parts of one material -> the tool) and the modifiers (an example tool + the items).
 */
@JeiPlugin
public class TConstructJeiPlugin implements IModPlugin {
    static final IRecipeType<TinkerCategory.Row> PART_BUILDER = IRecipeType.create(TConstruct.id("part_builder"), TinkerCategory.Row.class);
    static final IRecipeType<TinkerCategory.Row> TOOLS = IRecipeType.create(TConstruct.id("tool_assembly"), TinkerCategory.Row.class);
    static final IRecipeType<TinkerCategory.Row> MODIFIERS = IRecipeType.create(TConstruct.id("modifiers"), TinkerCategory.Row.class);
    static final IRecipeType<LiquidCategory.Row> MELTING = IRecipeType.create(TConstruct.id("melting"), LiquidCategory.Row.class);
    static final IRecipeType<LiquidCategory.Row> ALLOYING = IRecipeType.create(TConstruct.id("alloying"), LiquidCategory.Row.class);
    static final IRecipeType<LiquidCategory.Row> TABLE = IRecipeType.create(TConstruct.id("casting_table"), LiquidCategory.Row.class);
    static final IRecipeType<LiquidCategory.Row> BASIN = IRecipeType.create(TConstruct.id("casting_basin"), LiquidCategory.Row.class);
    static final IRecipeType<LiquidCategory.Row> DRYING = IRecipeType.create(TConstruct.id("drying_rack"), LiquidCategory.Row.class);

    @Override
    public Identifier getPluginUid() {
        return TConstruct.id("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new TinkerCategory(gui, PART_BUILDER, Component.translatable("jei.tconstruct.part_builder"), TBlocks.PART_BUILDERS.get(0).get(), 2, 2),
                new TinkerCategory(gui, TOOLS, Component.translatable("jei.tconstruct.tool_assembly"), TBlocks.TOOL_STATION.get(), 4, 1),
                new TinkerCategory(gui, MODIFIERS, Component.translatable("jei.tconstruct.modifiers"), TBlocks.TOOL_STATION.get(), 3, 1),
                new LiquidCategory(gui, MELTING, Component.translatable("jei.tconstruct.melting"), tconstruct.TSmeltery.CONTROLLER.get(), 1),
                new LiquidCategory(gui, ALLOYING, Component.translatable("jei.tconstruct.alloy"), tconstruct.TSmeltery.CONTROLLER.get(), 4),
                new LiquidCategory(gui, TABLE, Component.translatable("jei.tconstruct.table"), tconstruct.TSmeltery.CASTING_TABLE.get(), 2),
                new LiquidCategory(gui, BASIN, Component.translatable("jei.tconstruct.basin"), tconstruct.TSmeltery.CASTING_BASIN.get(), 2),
                new LiquidCategory(gui, DRYING, Component.translatable("jei.tconstruct.drying"), tconstruct.TSmeltery.DRYING_RACK.get(), 1));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        TBlocks.PART_BUILDERS.forEach(b -> registration.addCraftingStation(PART_BUILDER, b.get()));
        registration.addCraftingStation(MELTING, tconstruct.TSmeltery.CONTROLLER.get());
        registration.addCraftingStation(ALLOYING, tconstruct.TSmeltery.CONTROLLER.get());
        registration.addCraftingStation(TABLE, tconstruct.TSmeltery.CASTING_TABLE.get());
        registration.addCraftingStation(BASIN, tconstruct.TSmeltery.CASTING_BASIN.get());
        registration.addCraftingStation(DRYING, tconstruct.TSmeltery.DRYING_RACK.get());
        registration.addCraftingStation(PART_BUILDER, TBlocks.SLABS.get(2).get());
        for (var type : List.of(TOOLS, MODIFIERS)) {
            registration.addCraftingStation(type, TBlocks.TOOL_STATION.get(), TBlocks.SLABS.get(1).get());
            TBlocks.TOOL_FORGES.forEach(b -> registration.addCraftingStation(type, b.get()));
            registration.addCraftingStation(type, TBlocks.SLABS.get(5).get());
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(PART_BUILDER, partRecipes());
        registration.addRecipes(TOOLS, toolRecipes());
        registration.addRecipes(MODIFIERS, modifierRecipes());
        SmelteryJei.register(registration, MELTING, ALLOYING, TABLE, BASIN, DRYING);
    }

    /** Every wooden pattern with every material that makes a part (enough of it for one part). */
    static List<TinkerCategory.Row> partRecipes() {
        List<TinkerCategory.Row> rows = new ArrayList<>();
        for (var holder : TItems.WOOD_PATTERNS) {
            if (holder == null) {
                continue;
            }
            PatternItem pattern = holder.get();
            ItemStack patternStack = new ItemStack(pattern);
            List<ItemStack> materials = new ArrayList<>();
            List<ItemStack> parts = new ArrayList<>();
            List<ItemStack> leftovers = new ArrayList<>();
            List<ItemStack> candidates = new ArrayList<>();
            for (PatternBuilder.ItemKey key : PatternBuilder.materials()) {
                for (ItemStack example : key.examples().get()) {
                    candidates.add(example.copyWithCount(Math.max(1, (pattern.cost() + key.value() - 1) / key.value())));
                }
            }
            candidates.add(new ItemStack(Items.STRING, 3));
            candidates.add(new ItemStack(Items.FEATHER));
            candidates.add(new ItemStack(Items.OAK_LEAVES));
            for (ItemStack material : candidates) {
                ItemStack[] result = PatternBuilder.toolPart(material, patternStack);
                if (result != null) {
                    materials.add(material);
                    parts.add(result[0]);
                    leftovers.add(result[1]);
                }
            }
            if (!parts.isEmpty()) {
                rows.add(new TinkerCategory.Row(List.of(List.of(patternStack), materials), List.of(parts, leftovers), null));
            }
        }
        return rows;
    }

    private static ItemStack partStack(PartType type, TMaterial material) {
        if (type == PartType.BOWSTRING) {
            return new ItemStack(TItems.BOWSTRING.get());
        }
        if (type == PartType.FLETCHING) {
            return new ItemStack(TItems.FLETCHINGS.get(0).get());
        }
        return new ItemStack(TItems.part(type, material));
    }

    /** One recipe per tool, its parts in every material. */
    static List<TinkerCategory.Row> toolRecipes() {
        List<TinkerCategory.Row> rows = new ArrayList<>();
        for (ToolType type : ToolType.values()) {
            PartType[] parts = type.parts();
            List<List<ItemStack>> inputs = new ArrayList<>();
            for (int i = 0; i < parts.length; i++) {
                inputs.add(new ArrayList<>());
            }
            List<ItemStack> tools = new ArrayList<>();
            for (TMaterial material : TMaterial.values()) {
                ItemStack[] stacks = new ItemStack[4];
                for (int i = 0; i < 4; i++) {
                    stacks[i] = i < parts.length ? partStack(parts[i], material) : ItemStack.EMPTY;
                }
                ItemStack tool = ToolBuilder.buildTool(stacks[0], stacks[1], stacks[2], stacks[3], null, null);
                if (tool != null) {
                    for (int i = 0; i < parts.length; i++) {
                        inputs.get(i).add(stacks[i]);
                    }
                    tools.add(tool);
                }
            }
            rows.add(new TinkerCategory.Row(inputs, List.of(tools), null));
        }
        return rows;
    }

    /** Every modifier on an iron pickaxe (or an iron broadsword where the pickaxe cannot take it). */
    static List<TinkerCategory.Row> modifierRecipes() {
        List<TinkerCategory.Row> rows = new ArrayList<>();
        for (ToolMod mod : Modifiers.all()) {
            if (mod.stacks.isEmpty()) {
                continue;
            }
            ToolType type = mod.validType(ToolType.PICKAXE) ? ToolType.PICKAXE : ToolType.BROADSWORD;
            ItemStack tool = ToolBuilder.creativeTool(type, TMaterial.IRON, TMaterial.IRON, TMaterial.IRON);
            List<List<ItemStack>> inputs = new ArrayList<>();
            inputs.add(List.of(tool));
            ItemStack[] slots = {ItemStack.EMPTY, ItemStack.EMPTY};
            for (int i = 0; i < mod.stacks.size() && i < 2; i++) {
                List<ItemStack> examples = mod.stacks.get(i).get().examples().get();
                if (examples.isEmpty()) {
                    break;
                }
                inputs.add(examples);
                slots[i] = examples.get(0);
            }
            if (inputs.size() - 1 < mod.stacks.size()) {
                continue;
            }
            ItemStack result = ToolBuilder.modifyTool(tool, slots[0], slots[1], ItemStack.EMPTY, null);
            if (result != null && InfiTool.of(result) != null) {
                rows.add(new TinkerCategory.Row(inputs, List.of(List.of(result)), Component.translatable("jei.tconstruct.modifier_note")));
            }
        }
        return rows;
    }
}
