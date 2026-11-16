package artifice;

import artifice.block.DetectorBlock;
import artifice.block.FloraBlock;
import artifice.block.LotusBlock;
import artifice.block.FrameBlock;
import artifice.block.LotusLilyBlock;
import artifice.block.ScaffoldingBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Blocks and the creative tab of Artifice. 1.5.2 kept sub-blocks as metadata of one block id (basalt 1004:0-5,
 * marble 1005:0-5, flora 1003:0-4, frames 1001/1016/1017/1018:0-3, slabs 1010/1012:0-3); here every sub-block is a
 * block of its own. Items are in {@link ArtificeItems}.
 */
public final class ArtificeBlocks {
    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Artifice.MOD_ID);
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Artifice.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Artifice.MOD_ID);
    /** Creative tab order = order of registration (ArtificeBlocks, then ArtificeItems, as in the original). */
    static final List<Supplier<ItemStack>> TAB_ITEMS = new ArrayList<>();

    static <B extends Block> DeferredBlock<B> block(String name, Function<BlockBehaviour.Properties, B> factory,
            UnaryOperator<BlockBehaviour.Properties> properties) {
        DeferredBlock<B> block = BLOCKS.registerBlock(name, factory, properties);
        DeferredItem<BlockItem> item = ITEMS.registerSimpleBlockItem(block);
        TAB_ITEMS.add(() -> new ItemStack(item.get()));
        return block;
    }

    // ---- materials of 1.5.2 (the default step sound of Block is the stone sound) ----

    /** Material.rock: needs a pickaxe. setResistance(r) of 1.5.2 is r * 3 / 5 today. */
    static BlockBehaviour.Properties rock(BlockBehaviour.Properties p, float hardness, float resistance) {
        return p.mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(hardness, resistance)
                .sound(SoundType.STONE);
    }

    /** BlockBasalt/BlockMarble and the slabs: hardness 1.5, setResistance(10). */
    private static BlockBehaviour.Properties stone(BlockBehaviour.Properties p) {
        return rock(p, 1.5F, 6.0F);
    }

    /** BlockFlower (Material.plants): no collision, breaks at once, grass sound. */
    private static BlockBehaviour.Properties plant(BlockBehaviour.Properties p) {
        return p.mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.POPPED).randomTicks();
    }

    private static DeferredBlock<Block> stone(String name) {
        return block(name, Block::new, ArtificeBlocks::stone);
    }

    // ---- frames (BlockFrame): Material.rock, hardness meta + 5 (blast walls + 10), resistance 0 unless stated ----

    public enum Tier {
        BASIC, REINFORCED, INDUSTRIAL, ADVANCED;

        public final String id = name().toLowerCase(java.util.Locale.ROOT);
    }

    /** A frame-like block of a tier: hardness, explosion resistance and the 1.5.2 isOpaqueCube. */
    private static <B extends Block> DeferredBlock<B> frame(String name, Function<BlockBehaviour.Properties, B> factory, float hardness,
            float resistance, boolean opaque) {
        return block(name, factory, p -> {
            BlockBehaviour.Properties q = rock(p, hardness, resistance);
            return opaque ? q : q.noOcclusion().isSuffocating((s, l, pos) -> false).isViewBlocking((s, l, pos, b) -> false)
                    .isRedstoneConductor((s, l, pos) -> false);
        });
    }

    private static List<DeferredBlock<FrameBlock>> frames(String suffix, boolean opaque, boolean solidTop, float hardness, float[] resistance) {
        List<DeferredBlock<FrameBlock>> list = new ArrayList<>();
        for (Tier tier : Tier.values()) {
            list.add(frame(tier.id + suffix, p -> new FrameBlock(p, opaque, solidTop), hardness + tier.ordinal(), resistance[tier.ordinal()], opaque));
        }
        return List.copyOf(list);
    }

    // ================================================================ world generation blocks (enableWorldGen)
    public static final DeferredBlock<FloraBlock> BLUEBELL = block("bluebell", FloraBlock::new, ArtificeBlocks::plant);
    public static final DeferredBlock<FloraBlock> ORCHID = block("orchid", FloraBlock::new, ArtificeBlocks::plant);
    public static final DeferredBlock<FloraBlock> IRIS = block("iris", FloraBlock::new, ArtificeBlocks::plant);
    /** Flora meta 3 (open) and 4 (closed): one block with {@link LotusBlock#OPEN}. */
    public static final DeferredBlock<LotusBlock> LOTUS = block("lotus", LotusBlock::new, ArtificeBlocks::plant);
    /** BlockLotus (BlockLilyPad): placed on water by its item like a lily pad. */
    public static final DeferredBlock<LotusLilyBlock> LOTUS_LILY = BLOCKS.registerBlock("lotus_lily", LotusLilyBlock::new,
            p -> p.mapColor(MapColor.PLANT).instabreak().sound(SoundType.LILY_PAD).noOcclusion().pushReaction(PushReaction.POPPED).randomTicks());

    static {
        DeferredItem<Item> lily = ITEMS.registerItem("lotus_lily", p -> new PlaceOnWaterBlockItem(LOTUS_LILY.get(), p),
                Item.Properties::useBlockDescriptionPrefix);
        TAB_ITEMS.add(() -> new ItemStack(lily.get()));
    }

    // basalt 1004 / marble 1005: 0 stone, 1 cobblestone, 2 brick, 3 paver, 4 antipaver, 5 chiseled
    public static final DeferredBlock<Block> BASALT = stone("basalt");
    public static final DeferredBlock<Block> BASALT_COBBLESTONE = stone("basalt_cobblestone");
    public static final DeferredBlock<Block> BASALT_BRICK = stone("basalt_brick");
    public static final DeferredBlock<Block> BASALT_PAVER = stone("basalt_paver");
    public static final DeferredBlock<Block> BASALT_ANTIPAVER = stone("basalt_antipaver");
    public static final DeferredBlock<Block> CHISELED_BASALT = stone("chiseled_basalt");
    public static final DeferredBlock<Block> MARBLE = stone("marble");
    public static final DeferredBlock<Block> MARBLE_COBBLESTONE = stone("marble_cobblestone");
    public static final DeferredBlock<Block> MARBLE_BRICK = stone("marble_brick");
    public static final DeferredBlock<Block> MARBLE_PAVER = stone("marble_paver");
    public static final DeferredBlock<Block> MARBLE_ANTIPAVER = stone("marble_antipaver");
    public static final DeferredBlock<Block> CHISELED_MARBLE = stone("chiseled_marble");

    // BlockStairsArtifice (brick and cobblestone of both)
    public static final DeferredBlock<StairBlock> BASALT_BRICK_STAIRS = block("basalt_brick_stairs",
            p -> new StairBlock(BASALT_BRICK.get().defaultBlockState(), p), ArtificeBlocks::stone);
    public static final DeferredBlock<StairBlock> MARBLE_BRICK_STAIRS = block("marble_brick_stairs",
            p -> new StairBlock(MARBLE_BRICK.get().defaultBlockState(), p), ArtificeBlocks::stone);
    public static final DeferredBlock<StairBlock> BASALT_COBBLESTONE_STAIRS = block("basalt_cobblestone_stairs",
            p -> new StairBlock(BASALT_COBBLESTONE.get().defaultBlockState(), p), ArtificeBlocks::stone);
    public static final DeferredBlock<StairBlock> MARBLE_COBBLESTONE_STAIRS = block("marble_cobblestone_stairs",
            p -> new StairBlock(MARBLE_COBBLESTONE.get().defaultBlockState(), p), ArtificeBlocks::stone);

    // BlockBasaltSlab 1010 (double 1011) / BlockMarbleSlab 1012 (double 1013): 0 brick, 1 cobble, 2 paver, 3 antipaver
    public static final DeferredBlock<SlabBlock> BASALT_BRICK_SLAB = block("basalt_brick_slab", SlabBlock::new, ArtificeBlocks::stone);
    public static final DeferredBlock<SlabBlock> BASALT_COBBLESTONE_SLAB = block("basalt_cobblestone_slab", SlabBlock::new, ArtificeBlocks::stone);
    public static final DeferredBlock<SlabBlock> BASALT_PAVER_SLAB = block("basalt_paver_slab", SlabBlock::new, ArtificeBlocks::stone);
    public static final DeferredBlock<SlabBlock> BASALT_ANTIPAVER_SLAB = block("basalt_antipaver_slab", SlabBlock::new, ArtificeBlocks::stone);
    public static final DeferredBlock<SlabBlock> MARBLE_BRICK_SLAB = block("marble_brick_slab", SlabBlock::new, ArtificeBlocks::stone);
    public static final DeferredBlock<SlabBlock> MARBLE_COBBLESTONE_SLAB = block("marble_cobblestone_slab", SlabBlock::new, ArtificeBlocks::stone);
    public static final DeferredBlock<SlabBlock> MARBLE_PAVER_SLAB = block("marble_paver_slab", SlabBlock::new, ArtificeBlocks::stone);
    public static final DeferredBlock<SlabBlock> MARBLE_ANTIPAVER_SLAB = block("marble_antipaver_slab", SlabBlock::new, ArtificeBlocks::stone);

    // ================================================================ frames (enableFrames)
    // BlockFrameBase 1001: not opaque, not solid on any side, resistance 0
    public static final List<DeferredBlock<FrameBlock>> FRAMES = frames("_frame", false, false, 5.0F, new float[]{0, 0, 0, 0});
    /** BlockFrameDetector 1002 (basic tier only): opaque, solid. */
    public static final DeferredBlock<DetectorBlock> DETECTOR = frame("detector", DetectorBlock::new, 5.0F, 0.0F, true);
    // BlockFrameBlastWall 1016: opaque, solid, hardness meta + 10, getExplosionResistance 20/30/50/80
    public static final List<DeferredBlock<FrameBlock>> BLAST_WALLS = frames("_blast_wall", true, true, 10.0F, new float[]{20, 30, 50, 80});
    // BlockFrameGlassWall 1017: transparent, not solid, getExplosionResistance 15/25/45/75
    public static final List<DeferredBlock<FrameBlock>> GLASS_WALLS = frames("_glass_wall", false, false, 5.0F, new float[]{15, 25, 45, 75});
    /** BlockFrameScaffold 1018: not opaque, solid on top and bottom. */
    public static final List<DeferredBlock<ScaffoldingBlock>> SCAFFOLDING = scaffolding();

    private static List<DeferredBlock<ScaffoldingBlock>> scaffolding() {
        List<DeferredBlock<ScaffoldingBlock>> list = new ArrayList<>();
        for (Tier tier : Tier.values()) {
            list.add(frame(tier.id + "_scaffolding", p -> new ScaffoldingBlock(p, tier), 5.0F + tier.ordinal(), 0.0F, false));
        }
        return List.copyOf(list);
    }

    // ================================================================ steel (enableSteel)
    /** BlockSteel 1015: Material.iron, hardness 3 (resistance 15 / 5 = 3), the default (stone) step sound. */
    public static final DeferredBlock<Block> STEEL_BLOCK = block("steel_block", Block::new,
            p -> p.mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0F, 3.0F).sound(SoundType.STONE));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("artifice", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.artifice"))
            // ArtificeCreativeTab: the basic frame while frames are enabled
            .icon(() -> new ItemStack(FRAMES.getFirst().get()))
            .displayItems((parameters, output) -> TAB_ITEMS.forEach(stack -> output.accept(stack.get())))
            .build());

    private ArtificeBlocks() {
    }

    static void register(IEventBus modBus) {
        ArtificeItems.init();
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        TABS.register(modBus);
    }
}
