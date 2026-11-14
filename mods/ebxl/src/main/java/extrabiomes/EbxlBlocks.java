package extrabiomes;

import extrabiomes.block.CattailBlock;
import extrabiomes.block.EbxlPlantBlock;
import extrabiomes.block.EbxlSaplingBlock;
import extrabiomes.block.LeafPileBlock;
import extrabiomes.block.QuarterLogBlock;
import extrabiomes.block.QuicksandBlock;
import extrabiomes.entity.ScarecrowEntity;
import extrabiomes.gen.EbxlIds;
import extrabiomes.item.LogTurnerItem;
import extrabiomes.item.ScarecrowItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Blocks, items, the scarecrow and the creative tab of ExtrabiomesXL. 1.5.2 kept sub-blocks as metadata of one
 * block id; here every sub-block is a block of its own (names in the comments: id:meta of the pack).
 */
public final class EbxlBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Extrabiomes.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Extrabiomes.MOD_ID);
    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(Extrabiomes.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Extrabiomes.MOD_ID);
    /** Creative tab order = order of registration. */
    private static final List<Supplier<? extends Item>> TAB_ITEMS = new ArrayList<>();

    private static <B extends Block> DeferredBlock<B> block(String name, Function<BlockBehaviour.Properties, B> factory, UnaryOperator<BlockBehaviour.Properties> properties) {
        DeferredBlock<B> block = BLOCKS.registerBlock(name, factory, properties);
        DeferredItem<BlockItem> item = ITEMS.registerSimpleBlockItem(block);
        TAB_ITEMS.add(item);
        return block;
    }

    private static DeferredBlock<Block> simple(String name, UnaryOperator<BlockBehaviour.Properties> properties) {
        return block(name, Block::new, properties);
    }

    private static DeferredItem<Item> item(String name, Function<Item.Properties, Item> factory, UnaryOperator<Item.Properties> properties) {
        DeferredItem<Item> item = ITEMS.registerItem(name, factory, properties);
        TAB_ITEMS.add(item);
        return item;
    }

    // ---- material helpers (Material and step sound of 1.5.2) ----

    /** Material.rock: stone sound and map colour, needs a pickaxe; explosion resistance 6 (= 1.2 today). */
    private static UnaryOperator<BlockBehaviour.Properties> rock(float hardness, float resistance) {
        return properties -> properties.mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
                .strength(hardness, resistance);
    }

    /** Material.wood with the given hardness, flammable. */
    private static UnaryOperator<BlockBehaviour.Properties> wood(MapColor color, float hardness, float resistance) {
        return properties -> properties.mapColor(color).instrument(NoteBlockInstrument.BASS).strength(hardness, resistance).sound(SoundType.WOOD)
                .ignitedByLava();
    }

    /** Material.leaves: hardness 0.2, grass sound, see-through, decays. */
    private static UnaryOperator<BlockBehaviour.Properties> leaves(MapColor color) {
        return properties -> properties.mapColor(color).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion()
                .isValidSpawn(Blocks::ocelotOrParrot).isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p, b) -> false)
                .ignitedByLava().pushReaction(PushReaction.POPPED).isRedstoneConductor((s, l, p) -> false);
    }

    /** Material.plants / Material.vine: no collision, breaks at once. */
    private static UnaryOperator<BlockBehaviour.Properties> plant(boolean replaceable) {
        return properties -> {
            BlockBehaviour.Properties p = properties.mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS)
                    .pushReaction(PushReaction.POPPED);
            return replaceable ? p.replaceable().ignitedByLava() : p;
        };
    }

    private static boolean dirtLike(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.FARMLAND);
    }

    /** BlockCustomFlower: grass, dirt, farmland, sand and red rock. */
    private static boolean flowerSoil(BlockState state) {
        return dirtLike(state) || state.is(Blocks.SAND) || state.is(RED_ROCK.get());
    }

    /** BlockCustomTallGrass: grass, dirt, farmland, red rock and cracked sand. */
    private static boolean grassSoil(BlockState state) {
        return dirtLike(state) || state.is(RED_ROCK.get()) || state.is(CRACKED_SAND.get());
    }

    private static final VoxelShape FLOWER_SHAPE = Block.box(4.8, 0, 4.8, 11.2, 9.6, 11.2);
    private static final VoxelShape GRASS_SHAPE = Block.box(1.6, 0, 1.6, 14.4, 12.8, 14.4);
    private static final VoxelShape CATTAIL_SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    private static DeferredBlock<EbxlPlantBlock> flower(String name) {
        return block(name, p -> new EbxlPlantBlock(EbxlBlocks::flowerSoil, FLOWER_SHAPE, p), plant(false));
    }

    private static DeferredBlock<EbxlPlantBlock> grass(String name) {
        return block(name, p -> new EbxlPlantBlock(EbxlBlocks::grassSoil, GRASS_SHAPE, p), plant(true));
    }

    private static DeferredBlock<EbxlSaplingBlock> sapling(String name, int type) {
        return block(name, p -> new EbxlSaplingBlock(type, p), properties -> plant(false).apply(properties).randomTicks());
    }

    // ---- terrain (redrock 254:0-2, crackedsand 255) ----
    public static final DeferredBlock<Block> RED_ROCK = simple("red_rock", rock(1.5F, 1.2F));
    public static final DeferredBlock<Block> RED_COBBLESTONE = simple("red_cobblestone", rock(2.0F, 1.2F));
    public static final DeferredBlock<Block> RED_ROCK_BRICK = simple("red_rock_brick", rock(1.5F, 1.2F));
    public static final DeferredBlock<Block> CRACKED_SAND = simple("cracked_sand", rock(1.2F, 1.2F));
    // quicksand 917: Material.sand, hardness 4, shovel
    public static final DeferredBlock<QuicksandBlock> QUICKSAND = block("quicksand", QuicksandBlock::new,
            p -> p.mapColor(MapColor.SAND).strength(4.0F).sound(SoundType.SAND).noOcclusion().isSuffocating((s, l, q) -> false));

    // ---- wood: customlog 904 (fir, acacia), quarterlog 913-916 (redwood, fir, oak), planks 912 ----
    public static final DeferredBlock<RotatedPillarBlock> FIR_LOG = block("fir_log", RotatedPillarBlock::new, wood(MapColor.PODZOL, 2.0F, 2.0F));
    public static final DeferredBlock<RotatedPillarBlock> ACACIA_LOG = block("acacia_log", RotatedPillarBlock::new, wood(MapColor.STONE, 2.0F, 2.0F));
    public static final DeferredBlock<QuarterLogBlock> REDWOOD_QUARTER_LOG = block("redwood_quarter_log", QuarterLogBlock::new,
            wood(MapColor.COLOR_RED, 2.0F, 2.0F));
    public static final DeferredBlock<QuarterLogBlock> FIR_QUARTER_LOG = block("fir_quarter_log", QuarterLogBlock::new, wood(MapColor.PODZOL, 2.0F, 2.0F));
    public static final DeferredBlock<QuarterLogBlock> OAK_QUARTER_LOG = block("oak_quarter_log", QuarterLogBlock::new, wood(MapColor.WOOD, 2.0F, 2.0F));
    public static final DeferredBlock<Block> REDWOOD_PLANKS = simple("redwood_planks", wood(MapColor.COLOR_RED, 2.0F, 1.0F));
    public static final DeferredBlock<Block> FIR_PLANKS = simple("fir_planks", wood(MapColor.PODZOL, 2.0F, 1.0F));
    public static final DeferredBlock<Block> ACACIA_PLANKS = simple("acacia_planks", wood(MapColor.COLOR_ORANGE, 2.0F, 1.0F));

    // ---- leaves: greenleaves 910 (fir, redwood, acacia), autumnleaves 902 (brown, orange, purple, yellow) ----
    public static final DeferredBlock<TintedParticleLeavesBlock> FIR_LEAVES = leavesBlock("fir_leaves");
    public static final DeferredBlock<TintedParticleLeavesBlock> REDWOOD_LEAVES = leavesBlock("redwood_leaves");
    public static final DeferredBlock<TintedParticleLeavesBlock> ACACIA_LEAVES = leavesBlock("acacia_leaves");
    public static final DeferredBlock<TintedParticleLeavesBlock> BROWN_AUTUMN_LEAVES = leavesBlock("brown_autumn_leaves");
    public static final DeferredBlock<TintedParticleLeavesBlock> ORANGE_AUTUMN_LEAVES = leavesBlock("orange_autumn_leaves");
    public static final DeferredBlock<TintedParticleLeavesBlock> PURPLE_AUTUMN_LEAVES = leavesBlock("purple_autumn_leaves");
    public static final DeferredBlock<TintedParticleLeavesBlock> YELLOW_AUTUMN_LEAVES = leavesBlock("yellow_autumn_leaves");

    private static DeferredBlock<TintedParticleLeavesBlock> leavesBlock(String name) {
        return block(name, p -> new TintedParticleLeavesBlock(0.01F, p), leaves(MapColor.PLANT));
    }

    // ---- saplings 922:0-6 ----
    public static final DeferredBlock<EbxlSaplingBlock> BROWN_AUTUMN_SAPLING = sapling("brown_autumn_sapling", EbxlIds.SAPLING_BROWN);
    public static final DeferredBlock<EbxlSaplingBlock> ORANGE_AUTUMN_SAPLING = sapling("orange_autumn_sapling", EbxlIds.SAPLING_ORANGE);
    public static final DeferredBlock<EbxlSaplingBlock> PURPLE_AUTUMN_SAPLING = sapling("purple_autumn_sapling", EbxlIds.SAPLING_PURPLE);
    public static final DeferredBlock<EbxlSaplingBlock> YELLOW_AUTUMN_SAPLING = sapling("yellow_autumn_sapling", EbxlIds.SAPLING_YELLOW);
    public static final DeferredBlock<EbxlSaplingBlock> FIR_SAPLING = sapling("fir_sapling", EbxlIds.SAPLING_FIR);
    public static final DeferredBlock<EbxlSaplingBlock> REDWOOD_SAPLING = sapling("redwood_sapling", EbxlIds.SAPLING_REDWOOD);
    public static final DeferredBlock<EbxlSaplingBlock> ACACIA_SAPLING = sapling("acacia_sapling", EbxlIds.SAPLING_ACACIA);

    // ---- flowers 908:0-7, tallgrass 909:0-4, cattail 903, leafpile 911 ----
    public static final DeferredBlock<EbxlPlantBlock> AUTUMN_SHRUB = flower("autumn_shrub");
    public static final DeferredBlock<EbxlPlantBlock> HYDRANGEA = flower("hydrangea");
    public static final DeferredBlock<EbxlPlantBlock> BUTTERCUPS = flower("buttercups");
    public static final DeferredBlock<EbxlPlantBlock> PURPLE_FLOWER = flower("purple_flower");
    public static final DeferredBlock<EbxlPlantBlock> TINY_CACTUS = flower("tiny_cactus");
    public static final DeferredBlock<EbxlPlantBlock> ROOT = flower("root");
    public static final DeferredBlock<EbxlPlantBlock> TOADSTOOL = flower("toadstool");
    public static final DeferredBlock<EbxlPlantBlock> CALLA_LILIES = flower("calla_lilies");
    public static final DeferredBlock<EbxlPlantBlock> BROWN_GRASS = grass("brown_grass");
    public static final DeferredBlock<EbxlPlantBlock> SHORT_BROWN_GRASS = grass("short_brown_grass");
    public static final DeferredBlock<EbxlPlantBlock> DEAD_GRASS = grass("dead_grass");
    public static final DeferredBlock<EbxlPlantBlock> TALL_DEAD_GRASS = grass("tall_dead_grass");
    public static final DeferredBlock<EbxlPlantBlock> DEAD_YELLOW_GRASS = grass("dead_yellow_grass");
    public static final DeferredBlock<CattailBlock> CATTAIL = block("cattail", p -> new CattailBlock(CATTAIL_SHAPE, p), plant(false));
    public static final DeferredBlock<LeafPileBlock> LEAF_PILE = block("leaf_pile", LeafPileBlock::new,
            properties -> plant(true).apply(properties).randomTicks());

    // ---- fabrica: stairs, slabs (the double slabs are a state of today's slab), wall ----
    public static final DeferredBlock<StairBlock> REDWOOD_STAIRS = stairs("redwood_stairs", REDWOOD_PLANKS, wood(MapColor.COLOR_RED, 2.0F, 1.0F));
    public static final DeferredBlock<StairBlock> FIR_STAIRS = stairs("fir_stairs", FIR_PLANKS, wood(MapColor.PODZOL, 2.0F, 1.0F));
    public static final DeferredBlock<StairBlock> ACACIA_STAIRS = stairs("acacia_stairs", ACACIA_PLANKS, wood(MapColor.COLOR_ORANGE, 2.0F, 1.0F));
    public static final DeferredBlock<StairBlock> RED_COBBLESTONE_STAIRS = stairs("red_cobblestone_stairs", RED_COBBLESTONE, rock(2.0F, 1.2F));
    public static final DeferredBlock<StairBlock> RED_ROCK_BRICK_STAIRS = stairs("red_rock_brick_stairs", RED_ROCK_BRICK, rock(1.5F, 1.2F));
    public static final DeferredBlock<SlabBlock> REDWOOD_SLAB = block("redwood_slab", SlabBlock::new, wood(MapColor.COLOR_RED, 2.0F, 1.0F));
    public static final DeferredBlock<SlabBlock> FIR_SLAB = block("fir_slab", SlabBlock::new, wood(MapColor.PODZOL, 2.0F, 1.0F));
    public static final DeferredBlock<SlabBlock> ACACIA_SLAB = block("acacia_slab", SlabBlock::new, wood(MapColor.COLOR_ORANGE, 2.0F, 1.0F));
    public static final DeferredBlock<SlabBlock> RED_ROCK_SLAB = block("red_rock_slab", SlabBlock::new, rock(2.0F, 2.0F));
    public static final DeferredBlock<SlabBlock> RED_COBBLESTONE_SLAB = block("red_cobblestone_slab", SlabBlock::new, rock(2.0F, 2.0F));
    public static final DeferredBlock<SlabBlock> RED_ROCK_BRICK_SLAB = block("red_rock_brick_slab", SlabBlock::new, rock(2.0F, 2.0F));
    public static final DeferredBlock<WallBlock> RED_COBBLESTONE_WALL = block("red_cobblestone_wall", WallBlock::new,
            properties -> rock(2.0F, 1.2F).apply(properties).forceSolidOn());

    private static DeferredBlock<StairBlock> stairs(String name, DeferredBlock<Block> base, UnaryOperator<BlockBehaviour.Properties> properties) {
        return block(name, p -> new StairBlock(base.get().defaultBlockState(), p), properties);
    }

    // ---- items: logturner 5001, paste 5002, scarecrow 5003 ----
    public static final DeferredItem<Item> LOG_TURNER = item("log_turner", LogTurnerItem::new, p -> p.stacksTo(1));
    public static final DeferredItem<Item> CACTUS_PASTE = item("cactus_paste", Item::new, p -> p);
    public static final DeferredItem<Item> SCARECROW_ITEM = item("scarecrow", ScarecrowItem::new, p -> p);

    public static final DeferredHolder<EntityType<?>, EntityType<ScarecrowEntity>> SCARECROW = ENTITIES.registerEntityType("scarecrow",
            ScarecrowEntity::new, MobCategory.MISC, builder -> builder.sized(0.6F, 1.8F).clientTrackingRange(10));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("extrabiomes", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.extrabiomesxl"))
            .icon(() -> new ItemStack(RED_ROCK.get()))
            .displayItems((parameters, output) -> TAB_ITEMS.forEach(item -> output.accept(item.get())))
            .build());

    private EbxlBlocks() {
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(SCARECROW.get(), ScarecrowEntity.createAttributes().build());
    }

    static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        ENTITIES.register(modBus);
        TABS.register(modBus);
        modBus.addListener(EbxlBlocks::attributes);
    }
}
