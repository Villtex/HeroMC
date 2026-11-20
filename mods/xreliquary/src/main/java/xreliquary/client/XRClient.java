package xreliquary.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import xreliquary.Reliquary;
import xreliquary.XREntities;

/** ClientProxy: renderers, TimeKeeperHandler and the time-dependent item colours. */
public final class XRClient {
    private XRClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, e -> {
            e.registerEntityRenderer(XREntities.SHOT.get(), BillboardRenderer::new);
            e.registerEntityRenderer(XREntities.THROWN.get(), ctx -> new ThrownRenderer(ctx));
        });
        modBus.addListener(RegisterColorHandlersEvent.ItemTintSources.class, e -> e.register(Reliquary.id("time"), TimeTint.MAP_CODEC));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> TimeTint.tick());
    }

    /** The thrown things as billboards, the ice rod's ball as a snowball (RenderSnowball). */
    static final class ThrownRenderer extends BillboardRenderer<xreliquary.entity.ThrownEntity> {
        ThrownRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context) {
            super(context);
        }
    }
}
