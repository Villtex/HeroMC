package natura;

import natura.block.BerryBushBlock;
import natura.block.BloodwoodLogBlock;
import natura.block.CloudBlock;
import natura.block.FusewoodLogBlock;
import natura.block.GlowshroomBlock;
import natura.block.HeatSandBlock;
import natura.block.LargeGlowshroomBlock;
import natura.block.NaturaCropBlock;
import natura.block.NaturaLeavesBlock;
import natura.block.NaturaPlantBlock;
import natura.block.NaturaSaplingBlock;
import natura.block.NaturaTree;
import natura.block.NaturaWorkbenchBlock;
import natura.block.SaguaroBlock;
import natura.block.SaguaroFruitBlock;
import natura.block.SaguaroSproutBlock;
import natura.block.ThornVinesBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Blocks and the creative tab of Natura. 1.5.2 kept sub-blocks as metadata of one block id; here every sub-block is
 * a block of its own (the comments give id:meta of the pack, config/Natura.txt). Items are in {@link NaturaItems}.
 */
public final class NaturaBlocks {
    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Natura.MOD_ID);
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Natura.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Natura.MOD_ID);
    /** Creative tab order = order of registration. */
    static final List<Supplier<ItemStack>> TAB_ITEMS = new ArrayList<>();

    static <B extends Block> DeferredBlock<B> block(String name, Function<BlockBehaviour.Properties, B> factory,
            UnaryOperator<BlockBehaviour.Properties> properties) {
        return block(name, factory, properties, p -> p);
    }

    static <B extends Block> DeferredBlock<B> block(String name, Function<BlockBehaviour.Properties, B> factory,
            UnaryOperator<BlockBehaviour.Properties> properties, UnaryOperator<Item.Properties> itemProperties) {
        DeferredBlock<B> block = BLOCKS.registerBlock(name, factory, properties);
        DeferredItem<BlockItem> item = ITEMS.registerSimpleBlockItem(block, itemProperties);
        TAB_ITEMS.add(() -> new ItemStack(item.get()));
        return block;
    }

    /** TileEntityFurnace: every block of Material.wood burns 300 ticks. */
    static final UnaryOperator<Item.Properties> WOOD_FUEL = p -> p.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS);
    /** NContent.getBurnTime: saplings burn 100 ticks. */
    static final UnaryOperator<Item.Properties> SAPLING_FUEL = p -> p.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_ITEMS_EXTRA_SMALL);

    private static <B extends Block> DeferredBlock<B> woodBlock(String name, Function<BlockBehaviour.Properties, B> factory,
            UnaryOperator<BlockBehaviour.Properties> properties) {
        return block(name, factory, properties, WOOD_FUEL);
    }

    /** A block without an item (crops, sprouts, the side fruit of the saguaro). */
    static <B extends Block> DeferredBlock<B> blockOnly(String name, Function<BlockBehaviour.Properties, B> factory,
            UnaryOperator<BlockBehaviour.Properties> properties) {
        return BLOCKS.registerBlock(name, factory, properties);
    }

    private static DeferredBlock<Block> simple(String name, UnaryOperator<BlockBehaviour.Properties> properties) {
        return block(name, Block::new, properties);
    }

    // ---- material helpers (Material and step sound of 1.5.2) ----

    /** Material.wood. Only flammable woods are ignited by lava (their fire values are in the flammable data map). */
    static UnaryOperator<BlockBehaviour.Properties> wood(MapColor color, float hardness, float resistance, boolean flammable) {
        return properties -> {
            BlockBehaviour.Properties p = properties.mapColor(color).instrument(NoteBlockInstrument.BASS).strength(hardness, resistance)
                    .sound(SoundType.WOOD);
            return flammable ? p.ignitedByLava() : p;
        };
    }

    private static UnaryOperator<BlockBehaviour.Properties> wood(Wood wood, float hardness, float resistance) {
        return wood(wood.color, hardness, resistance, wood.flammable);
    }

    /** Material.leaves: hardness 0.2, grass sound, see-through. */
    private static UnaryOperator<BlockBehaviour.Properties> leaves(boolean flammable) {
        return properties -> {
            BlockBehaviour.Properties p = properties.mapColor(MapColor.PLANT).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion()
                    .isValidSpawn(Blocks::ocelotOrParrot).isSuffocating((s, l, q) -> false).isViewBlocking((s, l, q, b) -> false)
                    .pushReaction(PushReaction.POPPED).isRedstoneConductor((s, l, q) -> false);
            return flammable ? p.ignitedByLava() : p;
        };
    }

    /** Material.plants: no collision, breaks at once. */
    private static UnaryOperator<BlockBehaviour.Properties> plant() {
        return properties -> properties.mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.POPPED);
    }

    // ---- logs ----
    // tree 1259: 0 eucalyptus, 1 sakura, 2 ghostwood, 3 hopseed (+4 lying along x, +8 along z)
    public static final DeferredBlock<RotatedPillarBlock> EUCALYPTUS_LOG = log("eucalyptus_log", Wood.EUCALYPTUS, 1.5F, 3.0F);
    public static final DeferredBlock<RotatedPillarBlock> SAKURA_LOG = log("sakura_log", Wood.SAKURA, 1.5F, 3.0F);
    public static final DeferredBlock<RotatedPillarBlock> GHOSTWOOD_LOG = log("ghostwood_log", Wood.GHOSTWOOD, 1.5F, 3.0F);
    public static final DeferredBlock<RotatedPillarBlock> HOPSEED_LOG = log("hopseed_log", Wood.HOPSEED, 1.5F, 3.0F);
    // redwood 1251: 0 bark, 1 heart, 2 root (no orientation in 1.5.2)
    public static final DeferredBlock<RotatedPillarBlock> REDWOOD_BARK = log("redwood_bark", Wood.REDWOOD, 2.0F, 2.0F);
    public static final DeferredBlock<RotatedPillarBlock> REDWOOD_HEART = log("redwood_heart", Wood.REDWOOD, 2.0F, 2.0F);
    public static final DeferredBlock<RotatedPillarBlock> REDWOOD_ROOT = log("redwood_root", Wood.REDWOOD, 2.0F, 2.0F);
    // bloodwood 1227: 0-3 quarters (+4 along x, +8 along z), 15 bark on all sides; hardness 8, metal sound
    public static final DeferredBlock<BloodwoodLogBlock> BLOODWOOD_LOG = woodBlock("bloodwood_log", BloodwoodLogBlock::new,
            p -> wood(MapColor.COLOR_ORANGE, 8.0F, 8.0F, false).apply(p).sound(SoundType.METAL));
    public static final DeferredBlock<Block> BLOODWOOD_BARK = woodBlock("bloodwood_bark", Block::new,
            p -> wood(MapColor.COLOR_ORANGE, 8.0F, 8.0F, false).apply(p).sound(SoundType.METAL));
    // rare tree 1248: 0 maple, 1 silverbell, 2 amaranth, 3 tiger
    public static final DeferredBlock<RotatedPillarBlock> MAPLE_LOG = log("maple_log", Wood.MAPLE, 2.0F, 2.0F);
    public static final DeferredBlock<RotatedPillarBlock> SILVERBELL_LOG = log("silverbell_log", Wood.SILVERBELL, 2.0F, 2.0F);
    public static final DeferredBlock<RotatedPillarBlock> AMARANTH_LOG = log("amaranth_log", Wood.AMARANTH, 2.0F, 2.0F);
    public static final DeferredBlock<RotatedPillarBlock> TIGER_LOG = log("tiger_log", Wood.TIGER, 2.0F, 2.0F);
    // willow 1258:0
    public static final DeferredBlock<RotatedPillarBlock> WILLOW_LOG = log("willow_log", Wood.WILLOW, 2.0F, 2.0F);
    // dark tree 1235: 0 darkwood, 1 fusewood (explodes when broken); hardness 3.5, resistance 40
    public static final DeferredBlock<RotatedPillarBlock> DARKWOOD_LOG = log("darkwood_log", Wood.DARKWOOD, 3.5F, 24.0F);
    public static final DeferredBlock<FusewoodLogBlock> FUSEWOOD_LOG = woodBlock("fusewood_log", FusewoodLogBlock::new, wood(Wood.FUSEWOOD, 3.5F, 24.0F));

    private static DeferredBlock<RotatedPillarBlock> log(String name, Wood wood, float hardness, float resistance) {
        return woodBlock(name, RotatedPillarBlock::new, wood(wood, hardness, resistance));
    }

    // ---- planks 1245:0-12 ----
    public static final Map<Wood, DeferredBlock<Block>> PLANKS = perWood(w -> woodBlock(w.id + "_planks", Block::new, wood(w, 2.0F, 2.0F)));

    // ---- leaves ----
    // flora leaves 1238 (biome colour): 0 redwood, 1 eucalyptus, 2 hopseed (3 = redwood)
    public static final DeferredBlock<NaturaLeavesBlock> REDWOOD_LEAVES = leavesBlock("redwood_leaves", true);
    public static final DeferredBlock<NaturaLeavesBlock> EUCALYPTUS_LEAVES = leavesBlock("eucalyptus_leaves", true);
    public static final DeferredBlock<NaturaLeavesBlock> HOPSEED_LEAVES = leavesBlock("hopseed_leaves", true);
    // "Sakura Leaves" 1255 (no colour): 0 sakura, 1 ghostwood, 2 bloodwood, 3 willow
    public static final DeferredBlock<NaturaLeavesBlock> SAKURA_LEAVES = leavesBlock("sakura_leaves", true);
    public static final DeferredBlock<NaturaLeavesBlock> GHOSTWOOD_LEAVES = leavesBlock("ghostwood_leaves", false);
    public static final DeferredBlock<NaturaLeavesBlock> BLOODWOOD_LEAVES = leavesBlock("bloodwood_leaves", false);
    public static final DeferredBlock<NaturaLeavesBlock> WILLOW_LEAVES = leavesBlock("willow_leaves", false);
    // rare leaves 1247: 0 maple, 1 silverbell, 2 amaranth, 3 tiger
    public static final DeferredBlock<NaturaLeavesBlock> MAPLE_LEAVES = leavesBlock("maple_leaves", true);
    public static final DeferredBlock<NaturaLeavesBlock> SILVERBELL_LEAVES = leavesBlock("silverbell_leaves", true);
    public static final DeferredBlock<NaturaLeavesBlock> AMARANTH_LEAVES = leavesBlock("amaranth_leaves", true);
    public static final DeferredBlock<NaturaLeavesBlock> TIGER_LEAVES = leavesBlock("tiger_leaves", true);
    // darkwood leaves 1234: 0 darkwood, 1 flowering, 2 with potash apples, 3 fusewood
    public static final DeferredBlock<NaturaLeavesBlock> DARKWOOD_LEAVES = leavesBlock("darkwood_leaves", false);
    public static final DeferredBlock<NaturaLeavesBlock> FLOWERING_DARKWOOD_LEAVES = leavesBlock("flowering_darkwood_leaves", false);
    public static final DeferredBlock<NaturaLeavesBlock> FRUIT_DARKWOOD_LEAVES = leavesBlock("fruit_darkwood_leaves", false);
    public static final DeferredBlock<NaturaLeavesBlock> FUSEWOOD_LEAVES = leavesBlock("fusewood_leaves", false);

    private static DeferredBlock<NaturaLeavesBlock> leavesBlock(String name, boolean flammable) {
        return block(name, NaturaLeavesBlock::new, leaves(flammable));
    }

    // ---- saplings: flora sapling 1256:0-7, rare sapling 1249:0-4 ----
    public static final DeferredBlock<NaturaSaplingBlock> REDWOOD_SAPLING = sapling("redwood_sapling", NaturaTree.REDWOOD,
            NaturaSaplingBlock.Soil.PLAINS, NaturaSaplingBlock.Growth.REDWOOD);
    public static final DeferredBlock<NaturaSaplingBlock> EUCALYPTUS_SAPLING = sapling("eucalyptus_sapling", NaturaTree.EUCALYPTUS,
            NaturaSaplingBlock.Soil.PLAINS, NaturaSaplingBlock.Growth.FLORA);
    public static final DeferredBlock<NaturaSaplingBlock> HOPSEED_SAPLING = sapling("hopseed_sapling", NaturaTree.HOPSEED,
            NaturaSaplingBlock.Soil.PLAINS, NaturaSaplingBlock.Growth.FLORA);
    public static final DeferredBlock<NaturaSaplingBlock> SAKURA_SAPLING = sapling("sakura_sapling", NaturaTree.SAKURA,
            NaturaSaplingBlock.Soil.PLAINS, NaturaSaplingBlock.Growth.FLORA);
    public static final DeferredBlock<NaturaSaplingBlock> GHOSTWOOD_SAPLING = sapling("ghostwood_sapling", NaturaTree.GHOSTWOOD,
            NaturaSaplingBlock.Soil.NETHER, NaturaSaplingBlock.Growth.NETHER);
    public static final DeferredBlock<NaturaSaplingBlock> BLOODWOOD_SAPLING = sapling("bloodwood_sapling", NaturaTree.BLOODWOOD,
            NaturaSaplingBlock.Soil.CEILING, NaturaSaplingBlock.Growth.NETHER);
    public static final DeferredBlock<NaturaSaplingBlock> DARKWOOD_SAPLING = sapling("darkwood_sapling", NaturaTree.DARKWOOD,
            NaturaSaplingBlock.Soil.NETHER, NaturaSaplingBlock.Growth.NETHER);
    public static final DeferredBlock<NaturaSaplingBlock> FUSEWOOD_SAPLING = sapling("fusewood_sapling", NaturaTree.FUSEWOOD,
            NaturaSaplingBlock.Soil.NETHER, NaturaSaplingBlock.Growth.NETHER);
    public static final DeferredBlock<NaturaSaplingBlock> MAPLE_SAPLING = sapling("maple_sapling", NaturaTree.MAPLE,
            NaturaSaplingBlock.Soil.PLAINS, NaturaSaplingBlock.Growth.RARE);
    public static final DeferredBlock<NaturaSaplingBlock> SILVERBELL_SAPLING = sapling("silverbell_sapling", NaturaTree.SILVERBELL,
            NaturaSaplingBlock.Soil.PLAINS, NaturaSaplingBlock.Growth.RARE);
    public static final DeferredBlock<NaturaSaplingBlock> AMARANTH_SAPLING = sapling("amaranth_sapling", NaturaTree.AMARANTH,
            NaturaSaplingBlock.Soil.PLAINS, NaturaSaplingBlock.Growth.RARE);
    public static final DeferredBlock<NaturaSaplingBlock> TIGER_SAPLING = sapling("tiger_sapling", NaturaTree.TIGER,
            NaturaSaplingBlock.Soil.PLAINS, NaturaSaplingBlock.Growth.RARE);
    public static final DeferredBlock<NaturaSaplingBlock> WILLOW_SAPLING = sapling("willow_sapling", NaturaTree.WILLOW,
            NaturaSaplingBlock.Soil.PLAINS, NaturaSaplingBlock.Growth.RARE);

    private static DeferredBlock<NaturaSaplingBlock> sapling(String name, NaturaTree tree, NaturaSaplingBlock.Soil soil, NaturaSaplingBlock.Growth growth) {
        return block(name, p -> new NaturaSaplingBlock(tree, soil, growth, p), p -> plant().apply(p).randomTicks(), SAPLING_FUEL);
    }

    // ---- wood building blocks (one of each per wood) ----
    /** plank slab one 2512:0-7 and two 2513:0-4 (bit 8 = top half). */
    public static final Map<Wood, DeferredBlock<SlabBlock>> SLABS = perWood(w -> woodBlock(w.id + "_slab", SlabBlock::new, wood(w, 2.0F, 2.0F)));
    public static final Map<Wood, DeferredBlock<StairBlock>> STAIRS = perWood(w -> woodBlock(w.id + "_stairs",
            p -> new StairBlock(PLANKS.get(w).get().defaultBlockState(), p), wood(w, 2.0F, 2.0F)));
    /** AlternateFence 1237:0-12. */
    public static final Map<Wood, DeferredBlock<FenceBlock>> FENCES = perWood(w -> woodBlock(w.id + "_fence", FenceBlock::new, wood(w, 2.0F, 3.0F)));
    public static final Map<Wood, DeferredBlock<FenceGateBlock>> FENCE_GATES = perWood(w -> woodBlock(w.id + "_fence_gate",
            p -> new FenceGateBlock(WoodType.OAK, p), p -> wood(w, 2.0F, 3.0F).apply(p).forceSolidOn()));
    public static final Map<Wood, DeferredBlock<PressurePlateBlock>> PRESSURE_PLATES = perWood(w -> woodBlock(w.id + "_pressure_plate",
            p -> new PressurePlateBlock(BlockSetType.OAK, p),
            p -> wood(w, 0.5F, 0.5F).apply(p).forceSolidOn().noCollision().pushReaction(PushReaction.POPPED)));
    public static final Map<Wood, DeferredBlock<ButtonBlock>> BUTTONS = perWood(w -> block(w.id + "_button",
            p -> new ButtonBlock(BlockSetType.OAK, 30, p), p -> p.strength(0.5F).sound(SoundType.WOOD).noCollision().pushReaction(PushReaction.POPPED)));
    public static final Map<Wood, DeferredBlock<TrapDoorBlock>> TRAPDOORS = perWood(w -> woodBlock(w.id + "_trapdoor",
            p -> new TrapDoorBlock(BlockSetType.OAK, p), p -> wood(w, 3.0F, 3.0F).apply(p).noOcclusion().isValidSpawn(Blocks::never)));
    /** AlternateWorkbench 1232:0-12. */
    public static final Map<Wood, DeferredBlock<NaturaWorkbenchBlock>> WORKBENCHES = perWood(w -> woodBlock(w.id + "_workbench",
            NaturaWorkbenchBlock::new, wood(w, 2.5F, 2.5F)));
    /** AlternateBookshelf 1230:0-12. */
    public static final Map<Wood, DeferredBlock<Block>> BOOKSHELVES = perWood(w -> woodBlock(w.id + "_bookshelf", Block::new, wood(w, 1.5F, 1.5F)));

    // ---- doors (NDoor, the door item 5897:0-6) ----
    public static final DeferredBlock<DoorBlock> REDWOOD_DOOR = door("redwood_door", Wood.REDWOOD);
    public static final DeferredBlock<DoorBlock> EUCALYPTUS_DOOR = door("eucalyptus_door", Wood.EUCALYPTUS);
    public static final DeferredBlock<DoorBlock> HOPSEED_DOOR = door("hopseed_door", Wood.HOPSEED);
    public static final DeferredBlock<DoorBlock> SAKURA_DOOR = door("sakura_door", Wood.SAKURA);
    public static final DeferredBlock<DoorBlock> GHOSTWOOD_DOOR = door("ghostwood_door", Wood.GHOSTWOOD);
    public static final DeferredBlock<DoorBlock> BLOODWOOD_DOOR = door("bloodwood_door", Wood.BLOODWOOD);
    public static final DeferredBlock<DoorBlock> REDWOOD_BARK_DOOR = door("redwood_bark_door", Wood.REDWOOD);

    private static DeferredBlock<DoorBlock> door(String name, Wood wood) {
        DeferredBlock<DoorBlock> block = BLOCKS.registerBlock(name, p -> new DoorBlock(BlockSetType.OAK, p),
                p -> wood(wood, 3.0F, 3.0F).apply(p).noOcclusion().pushReaction(PushReaction.POPPED));
        DeferredItem<Item> item = ITEMS.registerItem(name, p -> new DoubleHighBlockItem(block.get(), p), p -> p.useBlockDescriptionPrefix());
        TAB_ITEMS.add(() -> new ItemStack(item.get()));
        return block;
    }

    // ---- clouds 1231: 0 white, 1 dark, 2 ash, 3 sulfur ----
    public static final DeferredBlock<CloudBlock> WHITE_CLOUD = cloud("white_cloud", false);
    public static final DeferredBlock<CloudBlock> DARK_CLOUD = cloud("dark_cloud", false);
    public static final DeferredBlock<CloudBlock> ASH_CLOUD = cloud("ash_cloud", false);
    public static final DeferredBlock<CloudBlock> SULFUR_CLOUD = cloud("sulfur_cloud", true);

    /** CloudMaterial: snow colour, not solid, cannot be pushed; cloth sound, hardness 0.3. */
    private static DeferredBlock<CloudBlock> cloud(String name, boolean sulfur) {
        return block(name, p -> new CloudBlock(sulfur, p), p -> p.mapColor(MapColor.SNOW).strength(0.3F).sound(SoundType.WOOL).noOcclusion()
                .isSuffocating((s, l, q) -> false).isViewBlocking((s, l, q, a) -> false).isRedstoneConductor((s, l, q) -> false)
                .isValidSpawn(Blocks::never).pushReaction(PushReaction.IMMOVEABLE));
    }

    // ---- saguaro 1253: 0 trunk, 1-2 sprout, 3-6 fruit on a side ----
    public static final DeferredBlock<SaguaroBlock> SAGUARO = block("saguaro", SaguaroBlock::new, NaturaBlocks::cactus);
    public static final DeferredBlock<SaguaroSproutBlock> SAGUARO_SPROUT = blockOnly("saguaro_sprout", SaguaroSproutBlock::new,
            p -> cactus(p).noCollision());
    public static final DeferredBlock<SaguaroFruitBlock> SAGUARO_FRUIT = blockOnly("saguaro_fruit", SaguaroFruitBlock::new, NaturaBlocks::cactus);

    /** Material.cactus: hardness 0.3, cloth sound, ticks randomly. */
    private static BlockBehaviour.Properties cactus(BlockBehaviour.Properties p) {
        return p.mapColor(MapColor.PLANT).strength(0.3F).sound(SoundType.WOOL).randomTicks().noOcclusion().pushReaction(PushReaction.POPPED);
    }

    // ---- berry bushes 1226 (raspberry, blueberry, blackberry, maloberry) and nether berry bushes 1244 (blight, dusk, sky, sting) ----
    public static final DeferredBlock<BerryBushBlock> RASPBERRY_BUSH = bush("raspberry_bush", false, () -> NaturaItems.RASPBERRY.get());
    public static final DeferredBlock<BerryBushBlock> BLUEBERRY_BUSH = bush("blueberry_bush", false, () -> NaturaItems.BLUEBERRY.get());
    public static final DeferredBlock<BerryBushBlock> BLACKBERRY_BUSH = bush("blackberry_bush", false, () -> NaturaItems.BLACKBERRY.get());
    public static final DeferredBlock<BerryBushBlock> MALOBERRY_BUSH = bush("maloberry_bush", false, () -> NaturaItems.MALOBERRY.get());
    public static final DeferredBlock<BerryBushBlock> BLIGHTBERRY_BUSH = bush("blightberry_bush", true, () -> NaturaItems.BLIGHTBERRY.get());
    public static final DeferredBlock<BerryBushBlock> DUSKBERRY_BUSH = bush("duskberry_bush", true, () -> NaturaItems.DUSKBERRY.get());
    public static final DeferredBlock<BerryBushBlock> SKYBERRY_BUSH = bush("skyberry_bush", true, () -> NaturaItems.SKYBERRY.get());
    public static final DeferredBlock<BerryBushBlock> STINGBERRY_BUSH = bush("stingberry_bush", true, () -> NaturaItems.STINGBERRY.get());

    /** Material.leaves, hardness 0.3. */
    private static DeferredBlock<BerryBushBlock> bush(String name, boolean nether, Supplier<? extends Item> berry) {
        return block(name, p -> new BerryBushBlock(nether, berry, p), p -> {
            BlockBehaviour.Properties q = p.mapColor(nether ? MapColor.NETHER : MapColor.PLANT).strength(0.3F).sound(SoundType.GRASS).noOcclusion()
                    .randomTicks().isSuffocating((s, l, b) -> false).isViewBlocking((s, l, b, a) -> false).isValidSpawn(Blocks::never)
                    .isRedstoneConductor((s, l, b) -> false).pushReaction(PushReaction.POPPED);
            return nether ? q : q.ignitedByLava();
        });
    }

    // ---- crops 1233: 0-3 barley, 4-8 cotton ----
    public static final DeferredBlock<NaturaCropBlock.Barley> BARLEY_CROP = blockOnly("barley_crop", NaturaCropBlock.Barley::new,
            p -> p.mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.POPPED));
    public static final DeferredBlock<NaturaCropBlock.Cotton> COTTON_CROP = blockOnly("cotton_crop", NaturaCropBlock.Cotton::new,
            p -> p.mapColor(MapColor.PLANT).noCollision().randomTicks().strength(0.5F).sound(SoundType.CROP).pushReaction(PushReaction.POPPED));

    // ---- plants ----
    /** Bluebells 1239. */
    public static final DeferredBlock<NaturaPlantBlock> BLUEBELLS = block("bluebells", NaturaPlantBlock::new, plant());
    /** Thornvines 1257 (vine metadata: 1 south, 2 west, 4 north, 8 east). */
    public static final DeferredBlock<ThornVinesBlock> THORNVINES = block("thornvines", ThornVinesBlock::new,
            p -> p.mapColor(MapColor.NETHER).replaceable().noCollision().randomTicks().strength(1.0F).sound(SoundType.VINE).lightLevel(s -> 9)
                    .pushReaction(PushReaction.POPPED));
    /** Glowing mushroom 1241: 0 green, 1 purple, 2 blue. */
    public static final DeferredBlock<GlowshroomBlock> GREEN_GLOWSHROOM = glowshroom("green_glowshroom", NaturaTree.GREEN_GLOWSHROOM);
    public static final DeferredBlock<GlowshroomBlock> PURPLE_GLOWSHROOM = glowshroom("purple_glowshroom", NaturaTree.PURPLE_GLOWSHROOM);
    public static final DeferredBlock<GlowshroomBlock> BLUE_GLOWSHROOM = glowshroom("blue_glowshroom", NaturaTree.BLUE_GLOWSHROOM);

    private static DeferredBlock<GlowshroomBlock> glowshroom(String name, NaturaTree tree) {
        return block(name, p -> new GlowshroomBlock(tree, p), p -> plant().apply(p).randomTicks().lightLevel(s -> 9));
    }

    /** Blue glowshroom 1229 (light 9), green 1242 and purple 1246 (light 7): metadata 10 and 15 = stem, the rest cap. */
    public static final DeferredBlock<LargeGlowshroomBlock> BLUE_GLOWSHROOM_BLOCK = largeGlowshroom("blue_glowshroom_block", true, 9);
    public static final DeferredBlock<LargeGlowshroomBlock> BLUE_GLOWSHROOM_STEM = largeGlowshroom("blue_glowshroom_stem", true, 9);
    public static final DeferredBlock<LargeGlowshroomBlock> GREEN_GLOWSHROOM_BLOCK = largeGlowshroom("green_glowshroom_block", false, 7);
    public static final DeferredBlock<LargeGlowshroomBlock> GREEN_GLOWSHROOM_STEM = largeGlowshroom("green_glowshroom_stem", false, 7);
    public static final DeferredBlock<LargeGlowshroomBlock> PURPLE_GLOWSHROOM_BLOCK = largeGlowshroom("purple_glowshroom_block", true, 7);
    public static final DeferredBlock<LargeGlowshroomBlock> PURPLE_GLOWSHROOM_STEM = largeGlowshroom("purple_glowshroom_stem", true, 7);

    /** Material.wood, hardness 0.2, wood sound, see-through. */
    private static DeferredBlock<LargeGlowshroomBlock> largeGlowshroom(String name, boolean cullsSame, int light) {
        return woodBlock(name, p -> new LargeGlowshroomBlock(cullsSame, p), p -> p.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS)
                .strength(0.2F).sound(SoundType.WOOD).lightLevel(s -> light).noOcclusion().isSuffocating((s, l, q) -> false)
                .isViewBlocking((s, l, q, a) -> false).isRedstoneConductor((s, l, q) -> false));
    }

    // ---- terrain ----
    /** Tainted soil 253: Material.ground, hardness 2.2, resistance 25, gravel sound; a fire source. */
    public static final DeferredBlock<Block> TAINTED_SOIL = simple("tainted_soil",
            p -> p.mapColor(MapColor.TERRACOTTA_BROWN).strength(2.2F, 15.0F).sound(SoundType.GRAVEL));
    /** Heat sand 252: falling sand, hardness 3. */
    public static final DeferredBlock<HeatSandBlock> HEAT_SAND = block("heat_sand", HeatSandBlock::new,
            p -> p.mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.SNARE).strength(3.0F).sound(SoundType.SAND));
    // topiary grass block 1260 and slab 1261: 0 topiary grass, 1 bluegrass, 2 autumnal grass
    public static final DeferredBlock<Block> TOPIARY_GRASS = simple("topiary_grass", NaturaBlocks::grass);
    public static final DeferredBlock<Block> BLUEGRASS = simple("bluegrass", NaturaBlocks::grass);
    public static final DeferredBlock<Block> AUTUMNAL_GRASS = simple("autumnal_grass", NaturaBlocks::grass);
    public static final DeferredBlock<SlabBlock> TOPIARY_GRASS_SLAB = block("topiary_grass_slab", SlabBlock::new, NaturaBlocks::grass);
    public static final DeferredBlock<SlabBlock> BLUEGRASS_SLAB = block("bluegrass_slab", SlabBlock::new, NaturaBlocks::grass);
    public static final DeferredBlock<SlabBlock> AUTUMNAL_GRASS_SLAB = block("autumnal_grass_slab", SlabBlock::new, NaturaBlocks::grass);

    /** Material.grass, hardness 0.6, grass sound. */
    private static BlockBehaviour.Properties grass(BlockBehaviour.Properties p) {
        return p.mapColor(MapColor.GRASS).strength(0.6F).sound(SoundType.GRASS);
    }

    private static <T> Map<Wood, T> perWood(Function<Wood, T> factory) {
        Map<Wood, T> map = new EnumMap<>(Wood.class);
        for (Wood wood : Wood.values()) {
            map.put(wood, factory.apply(wood));
        }
        return Collections.unmodifiableMap(map);
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("natura", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.natura"))
            .icon(() -> new ItemStack(NaturaItems.WHEAT_SEED_BAG.get()))
            .displayItems((parameters, output) -> {
                TAB_ITEMS.forEach(stack -> output.accept(stack.get()));
                NaturaItems.addEnchantedArmor(parameters.holders(), output);
            })
            .build());

    private NaturaBlocks() {
    }

    static void register(IEventBus modBus) {
        NaturaItems.init();
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        TABS.register(modBus);
    }
}
