package tconstruct.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import tconstruct.menu.TMenus;

/** Client side of the tool core: the screens of the tables. */
public final class TClient {
    private TClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterMenuScreensEvent.class, event -> {
            event.register(TMenus.TOOL_STATION.get(), ToolStationScreen::new);
            event.register(TMenus.PART_BUILDER.get(), PartBuilderScreen::new);
            event.register(TMenus.PATTERN_CHEST.get(), PatternChestScreen::new);
            event.register(TMenus.STENCIL_TABLE.get(), StencilTableScreen::new);
        });
    }
}
