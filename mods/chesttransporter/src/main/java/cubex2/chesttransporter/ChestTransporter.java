package cubex2.chesttransporter;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Chest Transporter 1.1.6: a stick item that picks up a chest with its contents (or the chest of a chest
 * minecart) and puts it down again somewhere else, breaking on the way. While it carries a chest, the player is
 * slow, mines slowly, jumps lower and gets hungry. The Iron Chests and MultiPageChest support of the original is
 * left out: neither mod was in the pack.
 */
@Mod(ChestTransporter.MOD_ID)
public final class ChestTransporter {
    public static final String MOD_ID = "chesttransporter";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    public static final DeferredItem<ChestTransporterItem> CHEST_TRANSPORTER = ITEMS.registerItem("chest_transporter",
            ChestTransporterItem::new, p -> p.stacksTo(1)
                    // the carried items are told as "Contains N items", not listed one by one
                    .component(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.CONTAINER, true)));

    public ChestTransporter(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(ChestTransporter::tab);
        NeoForge.EVENT_BUS.register(TransportEvents.class);
    }

    private static void tab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(CHEST_TRANSPORTER);
        }
    }
}
