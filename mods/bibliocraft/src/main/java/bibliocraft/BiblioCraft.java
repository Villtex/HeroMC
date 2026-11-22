package bibliocraft;

import bibliocraft.bookcase.Bookcases;
import bibliocraft.common.BCConfig;
import bibliocraft.decor.Decor;
import bibliocraft.display.Displays;
import bibliocraft.printing.Printing;
import bibliocraft.reading.Reading;
import bibliocraft.shelf.Shelves;
import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * BiblioCraft 1.3.3 (FML id "BiblioCraft") for NeoForge. Settings are those of the pack's BiblioCraft.cfg (BCConfig).
 *
 * <p>The blocks are split into groups, each in its own package with an {@code init(IEventBus)} that registers its
 * blocks, items, block entities and menus into the shared registers below, and a client class that registers its
 * renderers and screens. The metadata variants of the original (wood type, colour) are separate blocks now.
 */
@Mod(BiblioCraft.MOD_ID)
public final class BiblioCraft {
    public static final String MOD_ID = "bibliocraft";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MOD_ID);
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    /** SoundLoader: copen/cclose (weapon case), tapeopen/tapeclose (tape measure). */
    public static final DeferredHolder<SoundEvent, SoundEvent> CASE_OPEN = sound("copen");
    public static final DeferredHolder<SoundEvent, SoundEvent> CASE_CLOSE = sound("cclose");
    public static final DeferredHolder<SoundEvent, SoundEvent> TAPE_OPEN = sound("tapeopen");
    public static final DeferredHolder<SoundEvent, SoundEvent> TAPE_CLOSE = sound("tapeclose");

    /** BiblioTab "BiblioCraft": all items in registration order (the original listed them by block/item id). */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("bibliocraft", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.bibliocraft"))
            .icon(() -> new ItemStack(Bookcases.BOOKCASES.getFirst().get()))
            .displayItems((params, output) -> ITEMS.getEntries().forEach(item -> output.accept(item.get())))
            .build());

    public BiblioCraft(IEventBus modBus, ModContainer container) {
        modBus.addListener(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent.class, ItemCapabilities::register);
        container.registerConfig(ModConfig.Type.SYNCED, BCConfig.SPEC, "BiblioCraft.toml");
        // the order of the original BlockLoader.initBlocks / ItemLoader.initItems
        Printing.initDesk(modBus);
        Bookcases.init(modBus);
        Shelves.init(modBus);
        Displays.init(modBus);
        Decor.initTable(modBus);
        Displays.initArmorStand(modBus);
        Printing.init(modBus);
        Decor.init(modBus);
        Reading.init(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        SOUNDS.register(modBus);
        DATA_COMPONENTS.register(modBus);
        TABS.register(modBus);
        registerOldIds();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            bibliocraft.client.BCClient.init(modBus);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id(name)));
    }

    /** Block and item ids of the pack's BiblioCraft.cfg (items shifted by 256), for old loot and templates. */
    private static void registerOldIds() {
        String[] woods = Woods.NAMES;
        for (int m = 0; m < 4; m++) {
            OldGameIds.registerItem(1072, m, id(woods[m] + "_bookcase").toString());
            OldGameIds.registerItem(1073, m, id(woods[m] + "_shelf").toString());
            OldGameIds.registerItem(1076, m, id(woods[m] + "_potion_shelf").toString());
            OldGameIds.registerItem(1078, m, id(woods[m] + "_table").toString());
            OldGameIds.registerItem(1081, m, id(woods[m] + "_tool_rack").toString());
            OldGameIds.registerItem(1082, m, id(woods[m] + "_label").toString());
            OldGameIds.registerItem(1083, m, id(woods[m] + "_desk").toString());
            for (int c = 0; c < 4; c++) {
                OldGameIds.registerItem(1080, m * 4 + c, id(woods[m] + "_case_" + Woods.CASE_CLOTH[c]).toString());
            }
        }
        for (int c = 0; c < 16; c++) {
            OldGameIds.registerItem(1074, c, id(Woods.COLORS[c] + "_lamp").toString());
            OldGameIds.registerItem(1075, c, id(Woods.COLORS[c] + "_lantern").toString());
        }
        OldGameIds.registerItem(1071, id("armor_stand").toString());
        OldGameIds.registerItem(1077, id("printing_press").toString());
        OldGameIds.registerItem(1079, id("typesetting_table").toString());
        OldGameIds.registerItem(5451 + 256, id("chase").toString());
        OldGameIds.registerItem(5452 + 256, id("enchanted_plate").toString());
        OldGameIds.registerItem(5453 + 256, id("plate").toString());
        OldGameIds.registerItem(5454 + 256, 0, id("reading_glasses").toString());
        OldGameIds.registerItem(5454 + 256, 1, id("tinted_glasses").toString());
        OldGameIds.registerItem(5454 + 256, 2, id("monocle").toString());
        OldGameIds.registerItem(5455 + 256, id("redstone_book").toString());
        OldGameIds.registerItem(5456 + 256, id("tape_measure").toString());
        OldGameIds.registerItem(5457 + 256, id("tape_reel").toString());
        OldGameIds.registerEnchantment(196, net.minecraft.resources.ResourceKey.create(Registries.ENCHANTMENT, id("reading")));
    }
}
