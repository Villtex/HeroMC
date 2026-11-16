package natura.client.entity;

import natura.Natura;
import natura.NaturaEntities;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** NProxyClient.registerRenderer: the renderers of the mobs and boats. */
public final class NaturaEntityRenderers {
    public static final ModelLayerLocation IMP_LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Natura.MOD_ID, "imp"), "main");
    public static final ModelLayerLocation BOAT_LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Natura.MOD_ID, "boat"), "main");

    private NaturaEntityRenderers() {
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(IMP_LAYER, ImpModel::createBodyLayer);
        event.registerLayerDefinition(BOAT_LAYER, NaturaBoatModel::createBodyLayer);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NaturaEntities.IMP.get(), ImpRenderer::new);
        event.registerEntityRenderer(NaturaEntities.HEATSCAR_SPIDER.get(), context -> new HeatscarSpiderRenderer<>(context, 2.0F));
        event.registerEntityRenderer(NaturaEntities.BABY_HEATSCAR_SPIDER.get(), context -> new HeatscarSpiderRenderer<>(context, 0.75F));
        event.registerEntityRenderer(NaturaEntities.NITRO_CREEPER.get(), NitroCreeperRenderer::new);
        event.registerEntityRenderer(NaturaEntities.REDWOOD_BOAT_ENTITY.get(), context -> new NaturaBoatRenderer(context, "redwood"));
        event.registerEntityRenderer(NaturaEntities.BLOODWOOD_BOAT_ENTITY.get(), context -> new NaturaBoatRenderer(context, "bloodwood"));
        event.registerEntityRenderer(NaturaEntities.GHOSTWOOD_BOAT_ENTITY.get(), context -> new NaturaBoatRenderer(context, "ghostwood"));
        event.registerEntityRenderer(NaturaEntities.EUCALYPTUS_BOAT_ENTITY.get(), context -> new NaturaBoatRenderer(context, "eucalyptus"));
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(NaturaEntityRenderers::layers);
        modBus.addListener(NaturaEntityRenderers::renderers);
    }
}
