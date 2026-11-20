package tconstruct;

import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import tconstruct.blocks.CraftingStationBlockEntity;
import tconstruct.blocks.PartBuilderBlockEntity;
import tconstruct.blocks.PatternChestBlockEntity;
import tconstruct.blocks.StencilTableBlockEntity;
import tconstruct.blocks.TableBlock;
import tconstruct.blocks.TableSlabBlock;
import tconstruct.blocks.ToolStationBlockEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The tables of the tool core (TContent.registerBlocks): tool station, part builders and stencil tables of the four
 * woods of 1.5.2, pattern chest, the tool forges of 14 metals, crafting station and the six crafting slabs, with their
 * block entities.
 */
public final class TBlocks {
    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TConstruct.MOD_ID);
    static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TConstruct.MOD_ID);
    static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TConstruct.MOD_ID);

    public static final String[] WOODS = {"oak", "spruce", "birch", "jungle"};
    /** ToolForgeBlock damage 0..13. */
    public static final String[] FORGE_METALS = {"iron", "gold", "diamond", "emerald", "cobalt", "ardite", "manyullyn", "copper", "bronze", "tin",
            "aluminum", "alubrass", "alumite", "steel"};

    /** Every table block with its item, in creative tab order. */
    public static final Map<String, DeferredBlock<TableBlock>> ALL = new LinkedHashMap<>();
    public static final List<DeferredItem<?>> BLOCK_ITEMS = new ArrayList<>();

    private static BlockBehaviour.Properties wood() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD)
                .noOcclusion().ignitedByLava();
    }

    private static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
                .noOcclusion();
    }

    private static DeferredBlock<TableBlock> table(String name, TableBlock.Kind kind, boolean metal) {
        DeferredBlock<TableBlock> block = BLOCKS.registerBlock(name, p -> new TableBlock(kind, false, p), () -> metal ? metal() : wood());
        ALL.put(name, block);
        BLOCK_ITEMS.add(TItems.ITEMS.registerSimpleBlockItem(block));
        return block;
    }

    private static DeferredBlock<TableBlock> slab(String name, TableBlock.Kind kind) {
        DeferredBlock<TableBlock> block = BLOCKS.registerBlock(name, p -> new TableSlabBlock(kind, p), TBlocks::wood);
        ALL.put(name, block);
        BLOCK_ITEMS.add(TItems.ITEMS.registerSimpleBlockItem(block));
        return block;
    }

    public static final DeferredBlock<TableBlock> TOOL_STATION = table("tool_station", TableBlock.Kind.TOOL_STATION, false);
    public static final List<DeferredBlock<TableBlock>> PART_BUILDERS = new ArrayList<>();
    public static final DeferredBlock<TableBlock> PATTERN_CHEST;
    public static final List<DeferredBlock<TableBlock>> STENCIL_TABLES = new ArrayList<>();
    public static final List<DeferredBlock<TableBlock>> TOOL_FORGES = new ArrayList<>();
    public static final DeferredBlock<TableBlock> CRAFTING_STATION;
    /** CraftingSlab 0..5. */
    public static final List<DeferredBlock<TableBlock>> SLABS = new ArrayList<>();

    static {
        for (String wood : WOODS) {
            PART_BUILDERS.add(table(wood + "_part_builder", TableBlock.Kind.PART_BUILDER, false));
        }
        PATTERN_CHEST = table("pattern_chest", TableBlock.Kind.PATTERN_CHEST, false);
        for (String wood : WOODS) {
            STENCIL_TABLES.add(table(wood + "_stencil_table", TableBlock.Kind.STENCIL_TABLE, false));
        }
        for (String metal : FORGE_METALS) {
            TOOL_FORGES.add(table(metal + "_tool_forge", TableBlock.Kind.TOOL_FORGE, true));
        }
        CRAFTING_STATION = table("crafting_station", TableBlock.Kind.CRAFTING_STATION, false);
        SLABS.add(slab("crafting_slab", TableBlock.Kind.CRAFTING_STATION));
        SLABS.add(slab("tool_station_slab", TableBlock.Kind.TOOL_STATION));
        SLABS.add(slab("part_builder_slab", TableBlock.Kind.PART_BUILDER));
        SLABS.add(slab("stencil_table_slab", TableBlock.Kind.STENCIL_TABLE));
        SLABS.add(slab("pattern_chest_slab", TableBlock.Kind.PATTERN_CHEST));
        SLABS.add(slab("tool_forge_slab", TableBlock.Kind.TOOL_FORGE));
    }

    private static TableBlock[] of(TableBlock.Kind... kinds) {
        List<TableBlock> list = new ArrayList<>();
        for (DeferredBlock<TableBlock> block : ALL.values()) {
            for (TableBlock.Kind kind : kinds) {
                if (block.get().kind == kind) {
                    list.add(block.get());
                }
            }
        }
        return list.toArray(new TableBlock[0]);
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ToolStationBlockEntity>> TOOL_STATION_ENTITY = BLOCK_ENTITIES.register(
            "tool_station", () -> new BlockEntityType<>(ToolStationBlockEntity::new, of(TableBlock.Kind.TOOL_STATION, TableBlock.Kind.TOOL_FORGE)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PartBuilderBlockEntity>> PART_BUILDER_ENTITY = BLOCK_ENTITIES.register(
            "part_builder", () -> new BlockEntityType<>(PartBuilderBlockEntity::new, of(TableBlock.Kind.PART_BUILDER)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PatternChestBlockEntity>> PATTERN_CHEST_ENTITY = BLOCK_ENTITIES.register(
            "pattern_chest", () -> new BlockEntityType<>(PatternChestBlockEntity::new, of(TableBlock.Kind.PATTERN_CHEST)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StencilTableBlockEntity>> STENCIL_TABLE_ENTITY = BLOCK_ENTITIES.register(
            "stencil_table", () -> new BlockEntityType<>(StencilTableBlockEntity::new, of(TableBlock.Kind.STENCIL_TABLE)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CraftingStationBlockEntity>> CRAFTING_STATION_ENTITY = BLOCK_ENTITIES.register(
            "crafting_station", () -> new BlockEntityType<>(CraftingStationBlockEntity::new, of(TableBlock.Kind.CRAFTING_STATION)));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLOCK_TAB = TABS.register("blocks", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tconstruct.blocks"))
            .icon(() -> new net.minecraft.world.item.ItemStack(TOOL_STATION.get()))
            .withTabsBefore(TItems.MATERIAL_TAB.getKey())
            .displayItems((params, output) -> BLOCK_ITEMS.forEach(item -> output.accept(item.get())))
            .build());

    private TBlocks() {
    }

    static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        TABS.register(modBus);
    }

    /** EquipBlock "HeldItemBlock" (1311): the frying pan put down; no item of its own. */
    public static final DeferredBlock<tconstruct.blocks.FrypanBlock> FRYPAN = BLOCKS.registerBlock("frypan", tconstruct.blocks.FrypanBlock::new,
            () -> BlockBehaviour.Properties.of().strength(0.3F).sound(net.minecraft.world.level.block.SoundType.METAL).noOcclusion()
                    .lightLevel(state -> state.getValue(tconstruct.blocks.FrypanBlock.LIT) ? 9 : 0));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<tconstruct.blocks.FrypanBlockEntity>> FRYPAN_ENTITY = BLOCK_ENTITIES.register(
            "frypan", () -> new BlockEntityType<>(tconstruct.blocks.FrypanBlockEntity::new, FRYPAN.get()));

    /** ToolStationBlock 1326 (0 station, 1-4 part builders, 5-9 pattern chest, 10-13 stencil tables), forge, station, slab. */
    static void registerOldIds() {
        OldBlockStates.registerMeta(1326, meta -> {
            if (meta == 0) {
                return TOOL_STATION.get().defaultBlockState();
            }
            if (meta <= 4) {
                return PART_BUILDERS.get(meta - 1).get().defaultBlockState();
            }
            if (meta <= 9) {
                return PATTERN_CHEST.get().defaultBlockState();
            }
            return STENCIL_TABLES.get(Math.min(meta - 10, 3)).get().defaultBlockState();
        });
        OldBlockStates.registerMeta(1341, meta -> TOOL_FORGES.get(Math.min(meta, 13)).get().defaultBlockState());
        OldBlockStates.register(1347, () -> CRAFTING_STATION.get().defaultBlockState());
        OldBlockStates.register(1311, () -> FRYPAN.get().defaultBlockState());
        OldBlockStates.registerMeta(1346, meta -> SLABS.get(Math.min(meta % 8, 5)).get().defaultBlockState().setValue(TableSlabBlock.TOP, meta >= 8));
        String ns = TConstruct.MOD_ID + ":";
        OldGameIds.registerItem(1326, 0, ns + "tool_station");
        for (int i = 0; i < 4; i++) {
            OldGameIds.registerItem(1326, i + 1, ns + WOODS[i] + "_part_builder");
            OldGameIds.registerItem(1326, i + 10, ns + WOODS[i] + "_stencil_table");
        }
        OldGameIds.registerItem(1326, 5, ns + "pattern_chest");
        for (int i = 0; i < FORGE_METALS.length; i++) {
            OldGameIds.registerItem(1341, i, ns + FORGE_METALS[i] + "_tool_forge");
        }
        OldGameIds.registerItem(1347, ns + "crafting_station");
        String[] slabs = {"crafting_slab", "tool_station_slab", "part_builder_slab", "stencil_table_slab", "pattern_chest_slab", "tool_forge_slab"};
        for (int i = 0; i < slabs.length; i++) {
            OldGameIds.registerItem(1346, i, ns + slabs[i]);
        }
    }
}
