package bibliocraft.decor;

import bibliocraft.BiblioCraft;
import bibliocraft.Woods;
import bibliocraft.common.BiblioMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * BlockTable ("BiblioTable", metadata = wood), BlockLantern ("BiblioLantern") and BlockLamp ("BiblioLamp", metadata =
 * colour), TapeMeasure and ItemTape. The groups register in the order of the original BlockLoader / ItemLoader, so the
 * holders are filled by {@link #initTable} and {@link #init}, not by the class initialiser.
 */
public final class Decor {
    /** EntityCandleFX of BlockLantern.randomDisplayTick, as a particle type of its own. */
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, BiblioCraft.MOD_ID);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CANDLE_FLAME = PARTICLES.register("candle_flame",
            () -> new SimpleParticleType(false));

    public static final List<DeferredBlock<TableBlock>> TABLES = new ArrayList<>();
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TableBlockEntity>> TABLE_ENTITY;
    public static DeferredHolder<MenuType<?>, MenuType<TableMenu>> TABLE_MENU;

    public static final List<DeferredBlock<LanternBlock>> LANTERNS = new ArrayList<>();
    public static final List<DeferredBlock<LampBlock>> LAMPS = new ArrayList<>();
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<LightBlockEntity>> LANTERN_ENTITY;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<LightBlockEntity>> LAMP_ENTITY;

    public static DeferredItem<TapeMeasureItem> TAPE_MEASURE;
    public static DeferredItem<Item> TAPE_REEL;

    /** BlockItemLantern.candleColors / BlockItemLamp.hoodColors: the tooltip line of each colour. */
    private static final String[] COLOR_NAMES = {"White", "Light Gray", "Gray", "Black", "Red", "Orange", "Yellow", "Lime", "Green", "Cyan",
            "Light Blue", "Blue", "Purple", "Magenta", "Pink", "Brown"};

    private Decor() {
    }

    /** BlockTable: Material.wood, hardness 3.0, wood sound. */
    public static void initTable(IEventBus modBus) {
        for (int w = 0; w < 4; w++) {
            int wood = w;
            DeferredBlock<TableBlock> block = BiblioCraft.BLOCKS.registerBlock(Woods.NAMES[w] + "_table", p -> new TableBlock(wood, p),
                    p -> p.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(3.0F).sound(SoundType.WOOD).noOcclusion()
                            .dynamicShape().ignitedByLava());
            BiblioCraft.ITEMS.registerSimpleBlockItem(block);
            TABLES.add(block);
        }
        TABLE_ENTITY = BiblioCraft.BLOCK_ENTITIES.register("table",
                () -> new BlockEntityType<>(TableBlockEntity::new, TABLES.stream().map(DeferredBlock::get).toArray(TableBlock[]::new)));
        TABLE_MENU = BiblioCraft.MENUS.register("table",
                () -> IMenuTypeExtension.create((id, inv, buf) -> new TableMenu(id, inv, BiblioMenu.clientContainer(inv, buf, 1))));
    }

    /**
     * BlockLantern and BlockLamp (Material.iron: hardness 2.0, metal sound, needs a pickaxe to drop, light 15), then
     * TapeMeasure (stack size 1) and ItemTape (the reel).
     */
    public static void init(IEventBus modBus) {
        for (int c = 0; c < 16; c++) {
            int color = c;
            DeferredBlock<LanternBlock> block = BiblioCraft.BLOCKS.registerBlock(Woods.COLORS[c] + "_lantern", p -> new LanternBlock(color, p),
                    Decor::metal);
            BiblioCraft.ITEMS.registerItem(Woods.COLORS[c] + "_lantern", p -> new LightBlockItem(block.get(), p, COLOR_NAMES[color] + " Candle"),
                    Item.Properties::useBlockDescriptionPrefix);
            LANTERNS.add(block);
        }
        for (int c = 0; c < 16; c++) {
            int color = c;
            DeferredBlock<LampBlock> block = BiblioCraft.BLOCKS.registerBlock(Woods.COLORS[c] + "_lamp", p -> new LampBlock(color, p), Decor::metal);
            BiblioCraft.ITEMS.registerItem(Woods.COLORS[c] + "_lamp", p -> new LightBlockItem(block.get(), p, COLOR_NAMES[color] + " Lamp Hood"),
                    Item.Properties::useBlockDescriptionPrefix);
            LAMPS.add(block);
        }
        LANTERN_ENTITY = BiblioCraft.BLOCK_ENTITIES.register("lantern", () -> new BlockEntityType<>(
                (pos, state) -> new LightBlockEntity(LANTERN_ENTITY.get(), pos, state),
                LANTERNS.stream().map(DeferredBlock::get).toArray(LanternBlock[]::new)));
        LAMP_ENTITY = BiblioCraft.BLOCK_ENTITIES.register("lamp", () -> new BlockEntityType<>(
                (pos, state) -> new LightBlockEntity(LAMP_ENTITY.get(), pos, state),
                LAMPS.stream().map(DeferredBlock::get).toArray(LampBlock[]::new)));
        TAPE_MEASURE = BiblioCraft.ITEMS.registerItem("tape_measure", TapeMeasureItem::new, p -> p.stacksTo(1));
        TAPE_REEL = BiblioCraft.ITEMS.registerItem("tape_reel", Item::new);
        PARTICLES.register(modBus);
    }

    private static net.minecraft.world.level.block.state.BlockBehaviour.Properties metal(
            net.minecraft.world.level.block.state.BlockBehaviour.Properties p) {
        return p.mapColor(MapColor.METAL).strength(2.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion().lightLevel(s -> 15);
    }
}
