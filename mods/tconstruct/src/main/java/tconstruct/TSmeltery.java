package tconstruct;

import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import tconstruct.smeltery.CastingBlock;
import tconstruct.smeltery.CastingBlockEntity;
import tconstruct.smeltery.CastingChannelBlock;
import tconstruct.smeltery.ChannelBlockEntity;
import tconstruct.smeltery.DrainBlockEntity;
import tconstruct.smeltery.DryingRackBlock;
import tconstruct.smeltery.DryingRackBlockEntity;
import tconstruct.smeltery.FaucetBlock;
import tconstruct.smeltery.FaucetBlockEntity;
import tconstruct.smeltery.SearedBrickBlock;
import tconstruct.smeltery.SearedTankBlock;
import tconstruct.smeltery.ServantBlockEntity;
import tconstruct.smeltery.SmelteryBlockEntity;
import tconstruct.smeltery.SmelteryControllerBlock;
import tconstruct.smeltery.SmelteryDrainBlock;
import tconstruct.smeltery.SmelteryMenu;
import tconstruct.smeltery.SoilBlock;
import tconstruct.smeltery.TFluids;
import tconstruct.smeltery.TankBlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Part 25c, the smeltery (TContent.registerBlocks): smeltery controller, drain and the seared blocks, seared tanks,
 * casting table, faucet and basin, casting channel, drying rack, metal storage blocks, ores (stone/nether and gravel),
 * the special soils (grout for seared bricks), seared slabs, the liquids ({@link TFluids}) and the smeltery menu.
 */
public final class TSmeltery {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TConstruct.MOD_ID);

    /** The liquid a seared tank item carries. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FluidStack>> LIQUID = TItems.COMPONENTS.registerComponentType("liquid",
            b -> b.persistent(FluidStack.CODEC).networkSynchronized(FluidStack.STREAM_CODEC));

    /** The block items of this part in creative tab order. */
    public static final List<DeferredItem<?>> ITEMS = new ArrayList<>();

    private static BlockBehaviour.Properties seared() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).instrument(NoteBlockInstrument.BASEDRUM).strength(12.0F)
                .sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    private static <B extends Block> DeferredBlock<B> block(String name, Function<BlockBehaviour.Properties, B> factory,
                                                           Supplier<BlockBehaviour.Properties> properties) {
        DeferredBlock<B> block = TBlocks.BLOCKS.registerBlock(name, factory, properties);
        ITEMS.add(TItems.ITEMS.registerSimpleBlockItem(block));
        return block;
    }

    public static final DeferredBlock<SmelteryControllerBlock> CONTROLLER = block("smeltery_controller", SmelteryControllerBlock::new,
            () -> seared().lightLevel(s -> s.getValue(SmelteryControllerBlock.LIT) ? 9 : 0));
    public static final DeferredBlock<SmelteryDrainBlock> DRAIN = block("smeltery_drain", SmelteryDrainBlock::new, TSmeltery::seared);
    /** SmelteryBlock metadata 2, 4..11. */
    public static final String[] SEARED_NAMES = {"seared_bricks", "seared_stone", "seared_cobblestone", "seared_paver", "cracked_seared_bricks",
            "seared_road", "fancy_seared_bricks", "square_seared_bricks", "chiseled_seared_bricks"};
    public static final List<DeferredBlock<SearedBrickBlock>> SEARED = new ArrayList<>();

    static {
        for (String name : SEARED_NAMES) {
            SEARED.add(block(name, SearedBrickBlock::new, TSmeltery::seared));
        }
    }

    public static final DeferredBlock<SearedBrickBlock> SEARED_BRICKS = SEARED.get(0);
    public static final DeferredBlock<SearedBrickBlock> SEARED_STONE = SEARED.get(1);

    /** LavaTankBlock 0..2. */
    public static final List<DeferredBlock<SearedTankBlock>> TANKS = new ArrayList<>();

    static {
        for (String name : new String[]{"seared_tank", "seared_glass", "seared_window"}) {
            DeferredBlock<SearedTankBlock> block = TBlocks.BLOCKS.registerBlock(name, SearedTankBlock::new,
                    () -> seared().sound(SoundType.GLASS).noOcclusion());
            ITEMS.add(TItems.ITEMS.registerItem(name, p -> new tconstruct.smeltery.TankItem(block.get(), p), p -> p.useBlockDescriptionPrefix()));
            TANKS.add(block);
        }
    }

    public static final DeferredBlock<CastingBlock> CASTING_TABLE = block("casting_table", p -> new CastingBlock(false, p),
            () -> seared().noOcclusion());
    public static final DeferredBlock<FaucetBlock> FAUCET = block("seared_faucet", FaucetBlock::new, () -> seared().noOcclusion());
    public static final DeferredBlock<CastingBlock> CASTING_BASIN = block("casting_basin", p -> new CastingBlock(true, p), () -> seared().noOcclusion());
    public static final DeferredBlock<CastingChannelBlock> CHANNEL = block("casting_channel", CastingChannelBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(1.0F, 10.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()
                    .noOcclusion());
    public static final DeferredBlock<DryingRackBlock> DRYING_RACK = block("drying_rack", DryingRackBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(12.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
                    .noOcclusion());

    /** TMetalBlock 0..10. */
    public static final String[] METALS = {"cobalt", "ardite", "manyullyn", "copper", "bronze", "tin", "aluminum", "alubrass", "alumite", "steel", "ender"};
    public static final List<DeferredBlock<Block>> METAL_BLOCKS = new ArrayList<>();

    static {
        for (String metal : METALS) {
            METAL_BLOCKS.add(block(metal + "_block", Block::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(10.0F)
                    .sound(SoundType.METAL).requiresCorrectToolForDrops()));
        }
    }

    /** MetalOre (oreSlag) 0..6. */
    public static final String[] ORES = {"nether_slag", "cobalt_ore", "ardite_ore", "copper_ore", "tin_ore", "aluminum_ore", "stone_slag"};
    public static final List<DeferredBlock<Block>> ORE_BLOCKS = new ArrayList<>();

    static {
        for (int i = 0; i < ORES.length; i++) {
            float hardness = i <= 2 ? 10.0F : 3.0F;
            MapColor color = i <= 2 ? MapColor.NETHER : MapColor.STONE;
            ORE_BLOCKS.add(block(ORES[i], Block::new, () -> BlockBehaviour.Properties.of().mapColor(color).strength(hardness, 10.0F)
                    .requiresCorrectToolForDrops()));
        }
    }

    /** GravelOre 0..5. */
    public static final String[] GRAVEL_ORE_NAMES = {"iron", "gold", "copper", "tin", "aluminum", "cobalt"};
    public static final List<DeferredBlock<ColoredFallingBlock>> GRAVEL_ORES = new ArrayList<>();

    static {
        for (int i = 0; i < GRAVEL_ORE_NAMES.length; i++) {
            float hardness = i == 5 ? 10.0F : 3.0F;
            GRAVEL_ORES.add(block(GRAVEL_ORE_NAMES[i] + "_gravel_ore", p -> new ColoredFallingBlock(new ColorRGBA(-8356741), p),
                    () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(hardness).sound(SoundType.GRAVEL)
                            .requiresCorrectToolForDrops()));
        }
    }

    /** SoilBlock (craftedSoil) 0..5. */
    public static final String[] SOILS = {"slimy_mud", "grout", "blue_slimy_mud", "graveyard_soil", "consecrated_soil", "blue_slime_dirt"};
    public static final List<DeferredBlock<SoilBlock>> SOIL_BLOCKS = new ArrayList<>();
    public static final List<DeferredItem<BlockItem>> SOIL_ITEMS = new ArrayList<>();

    static {
        for (int i = 0; i < SOILS.length; i++) {
            int meta = i;
            DeferredBlock<SoilBlock> soil = TBlocks.BLOCKS.registerBlock(SOILS[i], p -> new SoilBlock(meta, p),
                    () -> BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(3.0F).sound(SoundType.GRAVEL));
            DeferredItem<BlockItem> item = TItems.ITEMS.registerSimpleBlockItem(soil);
            SOIL_BLOCKS.add(soil);
            SOIL_ITEMS.add(item);
            ITEMS.add(item);
        }
    }

    public static final Supplier<Item> CONSECRATED_SOIL = () -> SOIL_ITEMS.get(4).get();

    /** SearedSlab 0..7 (made of SmelteryBlock 2, 4, 5, 6, 8, 9, 10, 11). */
    public static final String[] SLABS = {"seared_brick_slab", "seared_stone_slab", "seared_cobblestone_slab", "seared_paver_slab", "seared_road_slab",
            "fancy_seared_slab", "square_seared_slab", "chiseled_seared_slab"};
    public static final List<DeferredBlock<SlabBlock>> SLAB_BLOCKS = new ArrayList<>();

    static {
        for (String slab : SLABS) {
            SLAB_BLOCKS.add(block(slab, SlabBlock::new, TSmeltery::seared));
        }
    }

    // ------------------------------------------------------------------ block entities

    private static Block[] all(List<? extends Supplier<? extends Block>> blocks) {
        return blocks.stream().map(Supplier::get).toArray(Block[]::new);
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SmelteryBlockEntity>> SMELTERY_ENTITY = TBlocks.BLOCK_ENTITIES.register(
            "smeltery", () -> new BlockEntityType<>(SmelteryBlockEntity::new, CONTROLLER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ServantBlockEntity>> SERVANT_ENTITY = TBlocks.BLOCK_ENTITIES.register(
            "seared_block", () -> new BlockEntityType<>(ServantBlockEntity::new, all(SEARED)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DrainBlockEntity>> DRAIN_ENTITY = TBlocks.BLOCK_ENTITIES.register(
            "smeltery_drain", () -> new BlockEntityType<>(DrainBlockEntity::new, DRAIN.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TankBlockEntity>> TANK_ENTITY = TBlocks.BLOCK_ENTITIES.register(
            "seared_tank", () -> new BlockEntityType<>(TankBlockEntity::new, all(TANKS)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CastingBlockEntity>> CASTING_ENTITY = TBlocks.BLOCK_ENTITIES.register(
            "casting", () -> new BlockEntityType<>(CastingBlockEntity::new, CASTING_TABLE.get(), CASTING_BASIN.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FaucetBlockEntity>> FAUCET_ENTITY = TBlocks.BLOCK_ENTITIES.register(
            "seared_faucet", () -> new BlockEntityType<>(FaucetBlockEntity::new, FAUCET.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChannelBlockEntity>> CHANNEL_ENTITY = TBlocks.BLOCK_ENTITIES.register(
            "casting_channel", () -> new BlockEntityType<>(ChannelBlockEntity::new, CHANNEL.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DryingRackBlockEntity>> DRYING_RACK_ENTITY = TBlocks.BLOCK_ENTITIES.register(
            "drying_rack", () -> new BlockEntityType<>(DryingRackBlockEntity::new, DRYING_RACK.get()));

    public static final DeferredHolder<MenuType<?>, MenuType<SmelteryMenu>> SMELTERY_MENU = MENUS.register("smeltery",
            () -> IMenuTypeExtension.create((id, inv, buf) -> SmelteryMenu.client(id, inv, buf.readBlockPos(), buf.readVarInt())));

    private TSmeltery() {
    }

    static void register(IEventBus modBus) {
        MENUS.register(modBus);
        TFluids.register(modBus);
    }

    /** Old ids of TinkersWorkshop.txt: blocks (game id = config id) and their items. */
    static void registerOldIds() {
        TFluids.registerOldIds();
        String ns = TConstruct.MOD_ID + ":";
        OldBlockStates.registerMeta(1322, meta -> switch (meta) {
            case 0 -> CONTROLLER.get().defaultBlockState();
            case 1 -> DRAIN.get().defaultBlockState();
            case 2, 3 -> SEARED_BRICKS.get().defaultBlockState();
            default -> SEARED.get(Math.min(meta - 3, SEARED.size() - 1)).get().defaultBlockState();
        });
        OldGameIds.registerItem(1322, 0, ns + "smeltery_controller");
        OldGameIds.registerItem(1322, 1, ns + "smeltery_drain");
        OldGameIds.registerItem(1322, 2, ns + "seared_bricks");
        for (int meta = 4; meta <= 11; meta++) {
            OldGameIds.registerItem(1322, meta, ns + SEARED_NAMES[meta - 3]);
        }
        OldBlockStates.registerMeta(1312, meta -> TANKS.get(Math.min(meta, 2)).get().defaultBlockState());
        String[] tanks = {"seared_tank", "seared_glass", "seared_window"};
        for (int i = 0; i < 3; i++) {
            OldGameIds.registerItem(1312, i, ns + tanks[i]);
        }
        OldBlockStates.registerMeta(1321, meta -> switch (meta) {
            case 1 -> FAUCET.get().defaultBlockState();
            case 2 -> CASTING_BASIN.get().defaultBlockState();
            default -> CASTING_TABLE.get().defaultBlockState();
        });
        OldGameIds.registerItem(1321, 0, ns + "casting_table");
        OldGameIds.registerItem(1321, 1, ns + "seared_faucet");
        OldGameIds.registerItem(1321, 2, ns + "casting_basin");
        OldBlockStates.register(1344, () -> CHANNEL.get().defaultBlockState());
        OldGameIds.registerItem(1344, ns + "casting_channel");
        OldBlockStates.registerMeta(1332, meta -> DRYING_RACK.get().defaultBlockState().setValue(DryingRackBlock.META, Math.min(meta, 5)));
        OldGameIds.registerItem(1332, ns + "drying_rack");
        OldBlockStates.registerMeta(1315, meta -> METAL_BLOCKS.get(Math.min(meta, 10)).get().defaultBlockState());
        for (int i = 0; i < METALS.length; i++) {
            OldGameIds.registerItem(1315, i, ns + METALS[i] + "_block");
        }
        OldBlockStates.registerMeta(1320, meta -> ORE_BLOCKS.get(Math.min(meta, 6)).get().defaultBlockState());
        for (int i = 0; i < ORES.length; i++) {
            OldGameIds.registerItem(1320, i, ns + ORES[i]);
        }
        OldBlockStates.registerMeta(1319, meta -> GRAVEL_ORES.get(Math.min(meta, 5)).get().defaultBlockState());
        for (int i = 0; i < GRAVEL_ORE_NAMES.length; i++) {
            OldGameIds.registerItem(1319, i, ns + GRAVEL_ORE_NAMES[i] + "_gravel_ore");
        }
        OldBlockStates.registerMeta(1323, meta -> SOIL_BLOCKS.get(Math.min(meta, 5)).get().defaultBlockState());
        for (int i = 0; i < SOILS.length; i++) {
            OldGameIds.registerItem(1323, i, ns + SOILS[i]);
        }
        OldBlockStates.registerMeta(1338, meta -> SLAB_BLOCKS.get(meta % 8).get().defaultBlockState()
                .setValue(SlabBlock.TYPE, meta >= 8 ? SlabType.TOP : SlabType.BOTTOM));
        for (int i = 0; i < SLABS.length; i++) {
            OldGameIds.registerItem(1338, i, ns + SLABS[i]);
        }
    }
}
