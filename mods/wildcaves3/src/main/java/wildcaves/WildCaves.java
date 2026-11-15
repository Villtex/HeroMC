package wildcaves;

import hexxit.worldgen152.OldBlockStates;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import wildcaves.block.FloraBlock;
import wildcaves.block.IcicleBlock;
import wildcaves.block.StalactiteBlock;
import wildcaves.gen.WildCavesGen;

import java.util.ArrayList;
import java.util.List;

/**
 * Wild Caves 3 0.4.2: decorates caves by biome with stalactites and stalagmites (stone, in deserts sandstone),
 * icicles and iceshrooms, glowcaps, vines, cobwebs, water springs and skulls, and puts bone piles (fossils) into
 * the stone. 1.5.2 kept each kind as the metadata of one block; here each piece is a block of its own.
 */
@Mod(WildCaves.MOD_ID)
public final class WildCaves {
    public static final String MOD_ID = "wildcaves3";
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    /** Metadata 0 to 12 of the stalactite blocks (ItemStoneStalactite.subNames). */
    public static final List<String> PIECES = List.of("stalactite_1", "stalactite_2", "stalactite_3", "stalactite_4",
            "stalactite_connection_1", "stalactite_connection_2", "stalactite_connection_3", "stalactite_connection_4",
            "stalactite_end", "stalagmite_end", "stalagmite_1", "stalagmite_2", "stalagmite_3");
    /** Metadata 0 to 9 of the flora block (ItemFlora.subNames: glowcap4top is the lower half, 4). */
    public static final List<String> FLORA_NAMES = List.of("glowcap_1", "glowcap_2", "glowcap_3", "gloweed", "glowcap_4_top",
            "glowcap_4_bottom", "iceshroom_1", "iceshroom_2", "iceshroom_3", "iceshroom_4");
    /**
     * "Flora light level" 5 became setLightValue(15 - 5): a float method given 10, so the light value was 150 and
     * the lighting stored 150 &amp; 15. The glowcaps shone with 6.
     */
    public static final int FLORA_LIGHT = 6;

    public static final List<DeferredBlock<StalactiteBlock>> STALACTITES = new ArrayList<>();
    public static final List<DeferredBlock<StalactiteBlock>> SANDSTONE_STALACTITES = new ArrayList<>();
    public static final List<DeferredBlock<FloraBlock>> FLORA = new ArrayList<>();
    public static final List<DeferredBlock<IcicleBlock>> ICICLES = new ArrayList<>();
    private static final List<DeferredItem<BlockItem>> TAB = new ArrayList<>();

    /** BlockFossils: Material.rock, hardness 1, stone sound. */
    public static final DeferredBlock<Block> BONE_PILE = BLOCKS.registerBlock("bone_pile", Block::new,
            p -> p.mapColor(MapColor.STONE).strength(1.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

    static {
        // BlockStalactite: Material.rock, hardness 0.8, default (stone) sound
        for (int i = 0; i < PIECES.size(); i++) {
            int piece = i;
            STALACTITES.add(BLOCKS.registerBlock(PIECES.get(i), p -> new StalactiteBlock(piece, p), WildCaves::stalactite));
        }
        for (int i = 0; i < PIECES.size(); i++) {
            int piece = i;
            SANDSTONE_STALACTITES.add(BLOCKS.registerBlock("sandstone_" + PIECES.get(i), p -> new StalactiteBlock(piece, p),
                    p -> stalactite(p).mapColor(MapColor.SAND)));
        }
        // BlockDecorations: Material.rock, no hardness, resistance 0.6, glass sound
        for (int i = 0; i < 3; i++) {
            int meta = i;
            ICICLES.add(BLOCKS.registerBlock("icicle_" + (i + 1), p -> new IcicleBlock(meta, p),
                    p -> p.mapColor(MapColor.ICE).instabreak().explosionResistance(0.6F).sound(SoundType.GLASS).noCollision()
                            .noOcclusion().requiresCorrectToolForDrops().pushReaction(PushReaction.POPPED)));
        }
        // BlockFlora: Material.plants, resistance 0.6, grass sound
        for (int i = 0; i < FLORA_NAMES.size(); i++) {
            int meta = i;
            FLORA.add(BLOCKS.registerBlock(FLORA_NAMES.get(i), p -> new FloraBlock(meta, () -> FLORA.get(4).get(), p),
                    p -> p.mapColor(MapColor.PLANT).instabreak().explosionResistance(0.6F).sound(SoundType.GRASS).noCollision()
                            .lightLevel(s -> FLORA_LIGHT).pushReaction(PushReaction.POPPED)));
        }
        STALACTITES.forEach(b -> TAB.add(ITEMS.registerSimpleBlockItem(b)));
        SANDSTONE_STALACTITES.forEach(b -> TAB.add(ITEMS.registerSimpleBlockItem(b)));
        FLORA.forEach(b -> TAB.add(ITEMS.registerSimpleBlockItem(b)));
        ICICLES.forEach(b -> TAB.add(ITEMS.registerSimpleBlockItem(b)));
        TAB.add(ITEMS.registerSimpleBlockItem(BONE_PILE));
    }

    private static net.minecraft.world.level.block.state.BlockBehaviour.Properties stalactite(
            net.minecraft.world.level.block.state.BlockBehaviour.Properties p) {
        return p.mapColor(MapColor.STONE).strength(0.8F).sound(SoundType.STONE).noCollision().noOcclusion().requiresCorrectToolForDrops()
                .pushReaction(PushReaction.POPPED);
    }

    public WildCaves(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(WildCaves::tab);
        WildCavesGen.register();
        OldBlockStates.registerMeta(WildCavesGen.STALACTITE, meta -> STALACTITES.get(Math.min(meta, 12)).get().defaultBlockState());
        OldBlockStates.registerMeta(WildCavesGen.SANDSTONE_STALACTITE,
                meta -> SANDSTONE_STALACTITES.get(Math.min(meta, 12)).get().defaultBlockState());
        OldBlockStates.registerMeta(WildCavesGen.FLORA, meta -> FLORA.get(Math.min(meta, 9)).get().defaultBlockState());
        OldBlockStates.registerMeta(WildCavesGen.DECORATIONS, meta -> ICICLES.get(Math.min(meta, 2)).get().defaultBlockState());
        OldBlockStates.register(WildCavesGen.FOSSILS, () -> BONE_PILE.get().defaultBlockState());
    }

    private static void tab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            TAB.forEach(item -> event.accept(item.get()));
        }
    }
}
