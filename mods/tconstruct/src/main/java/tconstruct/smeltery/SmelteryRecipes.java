package tconstruct.smeltery;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;
import tconstruct.TItems;
import tconstruct.TSmeltery;
import tconstruct.items.PatternItem;
import tconstruct.items.ToolPartItem;
import tconstruct.tools.PartType;
import tconstruct.tools.TMaterial;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static tconstruct.smeltery.TFluids.*;

/**
 * The tables of {@code Smeltery} (melting, alloys), {@code LiquidCasting} (casting table and basin) and
 * {@code DryingRackRecipes}, filled as TContent.addCraftingRecipes and TEventHandler.registerOre did (pack
 * configuration: bronze gives 3 ingots, no UHC recipe changes). Ore dictionary names are item tags of today
 * ({@code c:ingots/copper}, ...). Built on first use, when all items exist.
 */
public final class SmelteryRecipes {
    /** Smeltery.addMelting: what an item turns into, at which temperature, and which block stands for it inside the smeltery. */
    public record Melting(int temperature, FluidStack result, @Nullable Supplier<BlockState> render, int renderSize) {
        public Melting(int temperature, FluidStack result, @Nullable Supplier<BlockState> render) {
            this(temperature, result, render, 1);
        }

        /** The block drawn for {@code stack} inside the smeltery (Smeltery.getRenderIndex); ores and blocks show themselves. */
        public BlockState renderState(ItemStack stack) {
            if (this.render != null) {
                return this.render.get();
            }
            return Block.byItem(stack.getItem()).defaultBlockState();
        }
    }

    /** One entry for JEI: the items (an item or the items of a tag) and their melting. */
    public record MeltingEntry(List<ItemStack> inputs, Melting melting) {
    }

    /** AlloyMix: the result and the liquids it is made of (amounts are the mixing ratio). */
    public record Alloy(FluidStack result, List<FluidStack> mixers) {
        /** AlloyMix.mix: mixes as much as possible out of the molten metal (changes the list), null if a part is missing. */
        @Nullable FluidStack mix(List<FluidStack> liquids) {
            List<FluidStack> copyMix = new ArrayList<>(this.mixers);
            List<Integer> effectiveAmount = new ArrayList<>();
            for (FluidStack liquid : liquids) {
                for (FluidStack mixer : copyMix) {
                    if (FluidStack.isSameFluid(mixer, liquid)) {
                        effectiveAmount.add(liquid.getAmount() / mixer.getAmount());
                        copyMix.remove(mixer);
                        break;
                    }
                }
            }
            if (!copyMix.isEmpty()) {
                return null;
            }
            int low = effectiveAmount.get(0);
            for (int amount : effectiveAmount) {
                low = Math.min(low, amount);
            }
            List<FluidStack> copyMix2 = new ArrayList<>(this.mixers);
            for (int i = 0; i < liquids.size(); i++) {
                FluidStack liquid = liquids.get(i);
                for (FluidStack mixer : copyMix2) {
                    if (FluidStack.isSameFluid(mixer, liquid)) {
                        liquid.shrink(low * mixer.getAmount());
                        if (liquid.getAmount() <= 0) {
                            liquids.remove(i);
                            i--;
                        }
                        copyMix2.remove(mixer);
                        break;
                    }
                }
            }
            return this.result.copyWithAmount(this.result.getAmount() * low);
        }
    }

    /**
     * CastingRecipe: {@code cast} null = an empty table or basin; otherwise what has to lie in it ({@code castItems} for
     * JEI). {@code amount} is the metal of the recipe (casting a pattern-shaped cast uses the cast's material cost).
     */
    public record Casting(Supplier<ItemStack> output, Fluid fluid, int amount, @Nullable Predicate<ItemStack> cast, Supplier<List<ItemStack>> castItems,
                          boolean consumeCast, int coolTime) {
        public boolean matches(FluidStack metal, ItemStack inputCast) {
            if (metal.isEmpty() || metal.getFluid() != this.fluid) {
                return false;
            }
            return this.cast == null ? inputCast.isEmpty() : !inputCast.isEmpty() && this.cast.test(inputCast);
        }

        public ItemStack result() {
            return this.output.get().copy();
        }
    }

    public record Drying(Item input, int time, Supplier<ItemStack> output) {
    }

    private static boolean built;
    private static final Map<Item, Melting> MELTING = new IdentityHashMap<>();
    private static final List<TagMelting> TAG_MELTING = new ArrayList<>();
    private static final List<MeltingEntry> MELTING_ENTRIES = new ArrayList<>();
    private static final List<Alloy> ALLOYS = new ArrayList<>();
    private static final List<Casting> TABLE = new ArrayList<>();
    private static final List<Casting> BASIN = new ArrayList<>();
    private static final List<Drying> DRYING = new ArrayList<>();

    private record TagMelting(TagKey<Item> tag, Melting melting) {
    }

    private SmelteryRecipes() {
    }

    // ------------------------------------------------------------------ lookups

    /** Smeltery.getSmelteryResult (a copy), EMPTY if the item does not melt. Damaged items did not match in 1.5.2. */
    public static FluidStack result(ItemStack stack) {
        Melting melting = melting(stack);
        return melting == null ? FluidStack.EMPTY : melting.result().copy();
    }

    /** Smeltery.getLiquifyTemperature: 20 for anything that does not melt. */
    public static int temperature(ItemStack stack) {
        Melting melting = melting(stack);
        return melting == null ? 20 : melting.temperature();
    }

    public static @Nullable Melting melting(ItemStack stack) {
        build();
        if (stack.isEmpty() || stack.isDamaged()) {
            return null;
        }
        Melting melting = MELTING.get(stack.getItem());
        if (melting != null) {
            return melting;
        }
        for (TagMelting tag : TAG_MELTING) {
            if (stack.is(tag.tag())) {
                return tag.melting();
            }
        }
        return null;
    }

    /** Smeltery.mixMetals: all alloys that form, the molten metal changes. */
    public static List<FluidStack> mixMetals(List<FluidStack> moltenMetal) {
        build();
        List<FluidStack> liquids = new ArrayList<>();
        for (Alloy alloy : ALLOYS) {
            FluidStack liquid = alloy.mix(moltenMetal);
            if (liquid != null) {
                liquids.add(liquid);
            }
        }
        return liquids;
    }

    public static @Nullable Casting tableCasting(FluidStack metal, ItemStack cast) {
        return find(TABLE, metal, cast);
    }

    public static @Nullable Casting basinCasting(FluidStack metal, ItemStack cast) {
        return find(BASIN, metal, cast);
    }

    private static @Nullable Casting find(List<Casting> list, FluidStack metal, ItemStack cast) {
        build();
        for (Casting recipe : list) {
            if (recipe.matches(metal, cast)) {
                return recipe;
            }
        }
        return null;
    }

    /** DryingRackRecipes.getDryingTime / getDryingResult. */
    public static @Nullable Drying drying(ItemStack stack) {
        build();
        for (Drying recipe : DRYING) {
            if (stack.is(recipe.input())) {
                return recipe;
            }
        }
        return null;
    }

    public static List<MeltingEntry> meltingEntries() {
        build();
        return MELTING_ENTRIES;
    }

    public static List<Alloy> alloys() {
        build();
        return ALLOYS;
    }

    public static List<Casting> table() {
        build();
        return TABLE;
    }

    public static List<Casting> basin() {
        build();
        return BASIN;
    }

    public static List<Drying> dryingRecipes() {
        build();
        return DRYING;
    }

    // ------------------------------------------------------------------ the tables

    private static FluidStack liquid(int index, int amount) {
        return new FluidStack(fluid(index), amount);
    }

    private static void melt(Item item, int temperature, int liquid, int amount, Supplier<BlockState> render) {
        melt(item, temperature, liquid, amount, render, 1);
    }

    /** {@code size}: the stack size of 1.5.2's render index (an ingot fills a quarter of its block inside the smeltery). */
    private static void melt(Item item, int temperature, int liquid, int amount, Supplier<BlockState> render, int size) {
        Melting melting = new Melting(temperature, liquid(liquid, amount), render, size);
        MELTING.put(item, melting);
        MELTING_ENTRIES.add(new MeltingEntry(List.of(new ItemStack(item)), melting));
    }

    private static void melt(Item item, int temperature, FluidStack result, Supplier<BlockState> render) {
        Melting melting = new Melting(temperature, result, render);
        MELTING.put(item, melting);
        MELTING_ENTRIES.add(new MeltingEntry(List.of(new ItemStack(item)), melting));
    }

    /** addMelting(Block, meta, ...): the block melts and is shown as itself. */
    private static void meltBlock(Block block, int temperature, int liquid, int amount) {
        melt(block.asItem(), temperature, liquid, amount, block::defaultBlockState);
    }

    private static void meltTag(String tag, int temperature, int liquid, int amount, Supplier<BlockState> render) {
        TagKey<Item> key = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", tag));
        Melting melting = new Melting(temperature, liquid(liquid, amount), render, render == null ? 1 : 2);
        TAG_MELTING.add(new TagMelting(key, melting));
        List<ItemStack> items = new ArrayList<>();
        BuiltInRegistries.ITEM.getTagOrEmpty(key).forEach(h -> items.add(new ItemStack(h)));
        MELTING_ENTRIES.add(new MeltingEntry(items, melting));
    }

    /** A tag whose own blocks melt as themselves (ores, storage blocks). */
    private static void meltBlockTag(String tag, int temperature, int liquid, int amount) {
        meltTag(tag, temperature, liquid, amount, null);
    }

    private static Supplier<BlockState> block(Block block) {
        return block::defaultBlockState;
    }

    private static Supplier<BlockState> metal(int meta) {
        return () -> TSmeltery.METAL_BLOCKS.get(meta).get().defaultBlockState();
    }

    private static Item material(String name) {
        return TItems.MATERIALS.get(name).get();
    }

    private static void table(Supplier<ItemStack> output, int liquid, int amount, @Nullable Item cast, boolean consume, int delay) {
        TABLE.add(new Casting(output, fluid(liquid), amount, cast == null ? null : s -> s.is(cast),
                () -> cast == null ? List.of() : List.of(new ItemStack(cast)), consume, delay));
    }

    private static void basin(Supplier<ItemStack> output, int liquid, int amount, @Nullable Item cast) {
        BASIN.add(new Casting(output, fluid(liquid), amount, cast == null ? null : s -> s.is(cast),
                () -> cast == null ? List.of() : List.of(new ItemStack(cast)), true, 100));
    }

    private static Supplier<ItemStack> stack(Item item) {
        return () -> new ItemStack(item);
    }

    private static Supplier<ItemStack> stack(Block block) {
        return () -> new ItemStack(block);
    }

    private static synchronized void build() {
        if (built) {
            return;
        }
        built = true;
        PatternItem ingotCast = TItems.CASTS.get(0).get();

        // ---- table casting
        table(() -> new ItemStack(TItems.BLANK_CAST.get()), ALUBRASS, 144, null, false, 80);
        table(() -> new ItemStack(TItems.GOLD_BLANK_CAST.get()), GOLD, 288, null, false, 80);
        Object[][] ingots = {{Items.IRON_INGOT, IRON}, {Items.GOLD_INGOT, GOLD}, {material("copper_ingot"), COPPER}, {material("tin_ingot"), TIN},
                {material("aluminum_ingot"), ALUMINUM}, {material("cobalt_ingot"), COBALT}, {material("ardite_ingot"), ARDITE},
                {material("bronze_ingot"), BRONZE}, {material("alubrass_ingot"), ALUBRASS}, {material("manyullyn_ingot"), MANYULLYN},
                {material("alumite_ingot"), ALUMITE}, {material("obsidian_ingot"), OBSIDIAN}, {material("steel_ingot"), STEEL}};
        for (Object[] ingot : ingots) {
            table(stack((Item) ingot[0]), (Integer) ingot[1], 144, ingotCast, false, 80);
        }
        table(stack(material("seared_brick")), STONE, 36, ingotCast, false, 80);
        for (int i = 0; i < 16; i++) {
            Item bucket = get(i).bucket().get();
            table(stack(bucket), i, 1000, Items.BUCKET, true, 10);
        }
        // clear glass pane of part 25f; the vanilla pane stands in
        table(stack(Items.GLASS_PANE), GLASS, 250, null, false, 80);
        int[] liquids = {IRON, COPPER, COBALT, ARDITE, MANYULLYN, BRONZE, ALUMITE, OBSIDIAN, STEEL};
        TMaterial[] liquidMaterials = {TMaterial.IRON, TMaterial.COPPER, TMaterial.COBALT, TMaterial.ARDITE, TMaterial.MANYULLYN, TMaterial.BRONZE,
                TMaterial.ALUMITE, TMaterial.OBSIDIAN, TMaterial.STEEL};
        for (int meta = 1; meta < TItems.CASTS.size(); meta++) {
            PartType type = PartType.byPattern(meta);
            if (type == null || TItems.CASTS.get(meta) == null) {
                continue;
            }
            PatternItem cast = TItems.CASTS.get(meta).get();
            Predicate<ItemStack> anyPart = s -> s.getItem() instanceof ToolPartItem part && part.type == type;
            Supplier<List<ItemStack>> parts = () -> {
                List<ItemStack> list = new ArrayList<>();
                for (TMaterial material : TMaterial.values()) {
                    if (TItems.partHolder(type, material) != null) {
                        list.add(new ItemStack(TItems.part(type, material)));
                    }
                }
                return list;
            };
            TABLE.add(new Casting(stack(cast), fluid(ALUBRASS), 144, anyPart, parts, false, 50));
            TABLE.add(new Casting(stack(cast), fluid(GOLD), 288, anyPart, parts, false, 50));
            for (int i = 0; i < liquids.length; i++) {
                TMaterial material = liquidMaterials[i];
                if (TItems.partHolder(type, material) != null) {
                    table(() -> new ItemStack(TItems.part(type, material)), liquids[i], cast.cost() * 144 / 2, cast, false, 50);
                }
            }
        }
        Item[] ingotShapes = {Items.IRON_INGOT, Items.GOLD_INGOT, Items.BRICK, Items.NETHER_BRICK, material("seared_brick")};
        for (Item shape : ingotShapes) {
            table(stack(ingotCast), ALUBRASS, 144, shape, false, 50);
            table(stack(ingotCast), GOLD, 288, shape, false, 50);
        }
        // TEventHandler.registerOre: anything registered as "ingot..." shapes an ingot cast
        TagKey<Item> anyIngot = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots"));
        Supplier<List<ItemStack>> ingotItems = () -> {
            List<ItemStack> list = new ArrayList<>();
            BuiltInRegistries.ITEM.getTagOrEmpty(anyIngot).forEach(h -> list.add(new ItemStack(h)));
            return list;
        };
        TABLE.add(new Casting(stack(ingotCast), fluid(ALUBRASS), 144, s -> s.is(anyIngot), ingotItems, false, 50));
        TABLE.add(new Casting(stack(ingotCast), fluid(GOLD), 288, s -> s.is(anyIngot), ingotItems, false, 50));
        table(stack(Items.GOLDEN_APPLE), GOLD, 128, Items.APPLE, true, 50);

        // ---- basin casting
        basin(stack(Blocks.IRON_BLOCK), IRON, 1296, null);
        basin(stack(Blocks.GOLD_BLOCK), GOLD, 1296, null);
        int[][] metalBlocks = {{3, COPPER}, {5, TIN}, {6, ALUMINUM}, {0, COBALT}, {1, ARDITE}, {4, BRONZE}, {7, ALUBRASS}, {2, MANYULLYN}, {8, ALUMITE}};
        for (int[] m : metalBlocks) {
            basin(() -> new ItemStack(TSmeltery.METAL_BLOCKS.get(m[0]).get()), m[1], 1296, null);
        }
        basin(stack(Blocks.OBSIDIAN), OBSIDIAN, 288, null);
        basin(() -> new ItemStack(TSmeltery.METAL_BLOCKS.get(9).get()), STEEL, 1296, null);
        // clear glass of part 25f; the vanilla block stands in
        basin(stack(Blocks.GLASS), GLASS, 1000, null);
        basin(() -> new ItemStack(TSmeltery.SEARED_STONE.get()), STONE, 144, null);
        // brownstone (speedBlock, tin on gravel) follows with part 25f
        basin(stack(Blocks.END_STONE), ENDER, 25, Items.OBSIDIAN);
        basin(() -> new ItemStack(TSmeltery.METAL_BLOCKS.get(10).get()), ENDER, 1000, null);
        BASIN.add(new Casting(stack(Items.ENCHANTED_GOLDEN_APPLE), fluid(GOLD), 10368, s -> s.is(Items.APPLE), () -> List.of(new ItemStack(Items.APPLE)),
                true, 50));

        // ---- melting
        meltBlock(Blocks.IRON_ORE, 600, IRON, 288);
        meltBlock(Blocks.DEEPSLATE_IRON_ORE, 600, IRON, 288);
        meltBlock(Blocks.GOLD_ORE, 400, GOLD, 288);
        meltBlock(Blocks.DEEPSLATE_GOLD_ORE, 400, GOLD, 288);
        meltBlock(TSmeltery.GRAVEL_ORES.get(0).get(), 600, IRON, 288);
        meltBlock(TSmeltery.GRAVEL_ORES.get(1).get(), 400, GOLD, 288);
        melt(Items.IRON_INGOT, 500, IRON, 144, block(Blocks.IRON_BLOCK), 4);
        melt(Items.GOLD_INGOT, 300, GOLD, 144, block(Blocks.GOLD_BLOCK), 4);
        melt(Items.GOLD_NUGGET, 150, GOLD, 16, block(Blocks.GOLD_BLOCK), 4);
        melt(material("obsidian_ingot"), 750, OBSIDIAN, 144, block(Blocks.OBSIDIAN));
        melt(TItems.BLANK_CAST.get(), 150, ALUBRASS, 144, metal(7), 4);
        melt(TItems.GOLD_BLANK_CAST.get(), 150, GOLD, 288, metal(7), 4);
        melt(Items.ENDER_PEARL, 500, ENDER, 250, metal(10), 4);
        // ore berries (part 25d): 16 mB each at 100 degrees
        melt(tconstruct.TWorld.ORE_BERRIES.get(0).get(), 100, IRON, 16, block(Blocks.IRON_BLOCK), 4);
        melt(tconstruct.TWorld.ORE_BERRIES.get(1).get(), 100, GOLD, 16, block(Blocks.GOLD_BLOCK), 4);
        melt(tconstruct.TWorld.ORE_BERRIES.get(2).get(), 100, COPPER, 16, metal(3), 4);
        melt(tconstruct.TWorld.ORE_BERRIES.get(3).get(), 100, TIN, 16, metal(5), 4);
        melt(tconstruct.TWorld.ORE_BERRIES.get(4).get(), 100, ALUMINUM, 16, metal(6), 4);
        melt(TSmeltery.METAL_BLOCKS.get(10).get().asItem(), 500, ENDER, 1000, metal(10));
        meltBlock(Blocks.IRON_BLOCK, 600, IRON, 1296);
        meltBlock(Blocks.GOLD_BLOCK, 400, GOLD, 1296);
        meltBlock(Blocks.OBSIDIAN, 800, OBSIDIAN, 288);
        melt(Items.ICE, 75, new FluidStack(Fluids.WATER, 1000), block(Blocks.ICE));
        meltBlock(Blocks.SAND, 625, GLASS, 1000);
        meltBlock(Blocks.GLASS, 625, GLASS, 1000);
        meltBlock(Blocks.STONE, 800, STONE, 8);
        meltBlock(Blocks.COBBLESTONE, 800, STONE, 8);
        // clear glass, its pane and the stained clear glass come with part 25f
        Object[][] armor = {{Items.IRON_HELMET, 720}, {Items.IRON_CHESTPLATE, 1152}, {Items.IRON_LEGGINGS, 1008}, {Items.IRON_BOOTS, 576},
                {Items.IRON_HOE, 288}, {Items.IRON_SWORD, 288}, {Items.IRON_SHOVEL, 144}, {Items.IRON_PICKAXE, 432}, {Items.IRON_AXE, 432}};
        for (Object[] a : armor) {
            melt((Item) a[0], 600, IRON, (Integer) a[1], block(Blocks.IRON_BLOCK));
        }
        Object[][] gold = {{Items.GOLDEN_HELMET, 720}, {Items.GOLDEN_CHESTPLATE, 1152}, {Items.GOLDEN_LEGGINGS, 1008}, {Items.GOLDEN_BOOTS, 576},
                {Items.GOLDEN_HOE, 288}, {Items.GOLDEN_SWORD, 288}, {Items.GOLDEN_SHOVEL, 144}, {Items.GOLDEN_PICKAXE, 432}, {Items.GOLDEN_AXE, 432}};
        for (Object[] a : gold) {
            melt((Item) a[0], 350, GOLD, (Integer) a[1], block(Blocks.GOLD_BLOCK));
        }
        Object[][] chain = {{Items.CHAINMAIL_HELMET, 144}, {Items.CHAINMAIL_CHESTPLATE, 288}, {Items.CHAINMAIL_LEGGINGS, 288}, {Items.CHAINMAIL_BOOTS, 144}};
        for (Object[] a : chain) {
            melt((Item) a[0], 700, STEEL, (Integer) a[1], metal(9));
        }
        Object[][] iron = {{Items.FLINT_AND_STEEL, 144}, {Items.COMPASS, 576}, {Items.BUCKET, 432}, {Items.MINECART, 720}, {Items.IRON_DOOR, 864},
                {Items.IRON_BARS, 54}, {Items.HEAVY_WEIGHTED_PRESSURE_PLATE, 288}, {Items.RAIL, 54}, {Items.DETECTOR_RAIL, 144},
                {Items.ACTIVATOR_RAIL, 144}, {Items.CAULDRON, 1008}};
        for (Object[] a : iron) {
            melt((Item) a[0], 600, IRON, (Integer) a[1], block(Blocks.IRON_BLOCK));
        }
        melt(Items.LIGHT_WEIGHTED_PRESSURE_PLATE, 600, GOLD, 288, block(Blocks.GOLD_BLOCK), 4);
        melt(Items.POWERED_RAIL, 350, GOLD, 144, block(Blocks.GOLD_BLOCK));
        melt(Items.ENCHANTING_TABLE, 750, OBSIDIAN, 576, block(Blocks.OBSIDIAN));
        for (Item anvil : new Item[]{Items.ANVIL, Items.CHIPPED_ANVIL, Items.DAMAGED_ANVIL}) {
            melt(anvil, 800, IRON, 4464, block(Blocks.IRON_BLOCK));
        }
        // TEventHandler.registerOre: ingots (shown as their storage block), ores and storage blocks of the ore dictionary
        meltTag("ingots/copper", 450, COPPER, 144, metal(3));
        meltTag("ingots/tin", 175, TIN, 144, metal(5));
        meltTag("ingots/bronze", 500, BRONZE, 144, metal(4));
        meltTag("ingots/cobalt", 650, COBALT, 144, metal(0));
        meltTag("ingots/ardite", 650, ARDITE, 144, metal(1));
        meltTag("ingots/manyullyn", 750, MANYULLYN, 144, metal(2));
        meltTag("ingots/aluminum", 250, ALUMINUM, 144, metal(6));
        meltTag("ingots/aluminum_brass", 350, ALUBRASS, 144, metal(7));
        meltTag("ingots/alumite", 500, ALUMITE, 144, metal(8));
        meltTag("ingots/steel", 500, STEEL, 144, metal(9));
        meltTag("ingots/nickel", 400, NICKEL, 144, metal(0));
        meltTag("ingots/lead", 400, LEAD, 144, metal(0));
        meltTag("ingots/silver", 400, SILVER, 144, metal(0));
        meltTag("ingots/platinum", 400, PLATINUM, 144, metal(0));
        meltTag("ingots/invar", 400, INVAR, 144, metal(0));
        meltTag("ingots/electrum", 400, ELECTRUM, 144, metal(0));
        meltBlockTag("ores/copper", 550, COPPER, 288);
        meltBlockTag("ores/tin", 275, TIN, 288);
        meltBlockTag("ores/aluminum", 350, ALUMINUM, 288);
        meltBlockTag("ores/cobalt", 750, COBALT, 288);
        meltBlockTag("ores/ardite", 750, ARDITE, 288);
        meltBlockTag("ores/nickel", 400, NICKEL, 288);
        meltBlockTag("ores/lead", 400, LEAD, 288);
        meltBlockTag("ores/silver", 400, SILVER, 288);
        meltBlockTag("ores/platinum", 400, PLATINUM, 288);
        meltBlockTag("ores/invar", 400, INVAR, 288);
        meltBlockTag("ores/electrum", 400, ELECTRUM, 288);
        meltBlockTag("storage_blocks/copper", 550, COPPER, 1296);
        meltBlockTag("storage_blocks/tin", 275, TIN, 1296);
        meltBlockTag("storage_blocks/bronze", 500, BRONZE, 1296);
        meltBlockTag("storage_blocks/aluminum", 350, ALUMINUM, 1296);
        meltBlockTag("storage_blocks/cobalt", 750, COBALT, 1296);
        meltBlockTag("storage_blocks/ardite", 750, ARDITE, 1296);
        meltBlockTag("storage_blocks/manyullyn", 800, MANYULLYN, 1296);
        meltBlockTag("storage_blocks/aluminum_brass", 350, ALUBRASS, 1296);
        meltBlockTag("storage_blocks/alumite", 500, ALUMITE, 1296);
        meltBlockTag("storage_blocks/steel", 500, STEEL, 1296);
        meltBlockTag("storage_blocks/nickel", 400, NICKEL, 1296);
        meltBlockTag("storage_blocks/lead", 400, LEAD, 1296);
        meltBlockTag("storage_blocks/silver", 400, SILVER, 1296);
        meltBlockTag("storage_blocks/platinum", 400, PLATINUM, 1296);
        meltBlockTag("storage_blocks/invar", 400, INVAR, 1296);
        meltBlockTag("storage_blocks/electrum", 400, ELECTRUM, 1296);
        // oreberries (part 25d) melt at 100 into 16 mB

        // ---- alloys (harder bronze off: 3 ingots)
        alloy(liquid(BRONZE, 24), liquid(COPPER, 24), liquid(TIN, 8));
        alloy(liquid(ALUBRASS, 16), liquid(ALUMINUM, 24), liquid(COPPER, 8));
        alloy(liquid(MANYULLYN, 16), liquid(COBALT, 32), liquid(ARDITE, 32));
        alloy(liquid(ALUMITE, 48), liquid(ALUMINUM, 80), liquid(IRON, 32), liquid(OBSIDIAN, 32));
        alloy(liquid(INVAR, 24), liquid(IRON, 16), liquid(NICKEL, 8));
        alloy(liquid(ELECTRUM, 16), liquid(GOLD, 8), liquid(SILVER, 8));

        // ---- drying rack
        DRYING.add(new Drying(Items.ROTTEN_FLESH, 6000, stack(Items.LEATHER)));
    }

    private static void alloy(FluidStack result, FluidStack... mixers) {
        ALLOYS.add(new Alloy(result, List.of(mixers)));
    }
}
