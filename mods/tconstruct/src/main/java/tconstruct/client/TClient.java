package tconstruct.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent;
import tconstruct.TConstruct;
import tconstruct.entity.TEntities;
import tconstruct.menu.TMenus;

/** Client side: the screens of the tables, the layered tool models, the arrow and dagger renderers. */
public final class TClient {
    private TClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterMenuScreensEvent.class, event -> {
            event.register(TMenus.TOOL_STATION.get(), ToolStationScreen::new);
            event.register(TMenus.PART_BUILDER.get(), PartBuilderScreen::new);
            event.register(TMenus.PATTERN_CHEST.get(), PatternChestScreen::new);
            event.register(TMenus.STENCIL_TABLE.get(), StencilTableScreen::new);
            event.register(TMenus.FRYPAN.get(), FrypanScreen::new);
        });
        modBus.addListener(RegisterSelectItemModelPropertyEvent.class, event -> event.register(TConstruct.id("tool_part"), ToolPartProperty.TYPE));
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> {
            event.registerEntityRenderer(TEntities.ARROW.get(), ToolProjectileRenderer::new);
            event.registerEntityRenderer(TEntities.DAGGER.get(), ToolProjectileRenderer::new);
        });
    }
}
