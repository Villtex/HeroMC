package chococraft.client;

import chococraft.ChocoCraft;
import chococraft.entity.ChocoboColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** ClientProxyChocoCraft: renderers, model layers and the Chocopedia screens. */
public final class ChocoCraftClient {
    public static final ModelLayerLocation CHOCOBO_LAYER = new ModelLayerLocation(ChocoCraft.id("chocobo"), "main");
    public static final ModelLayerLocation CHICOBO_LAYER = new ModelLayerLocation(ChocoCraft.id("chicobo"), "main");

    private ChocoCraftClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ChocoCraftClient::layers);
        modBus.addListener(ChocoCraftClient::renderers);
        ChocoCraft.chocopediaOpener = chocobo -> Minecraft.getInstance().gui.setScreen(new ChocopediaScreen(chocobo));
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CHOCOBO_LAYER, ChocoboModel::createBodyLayer);
        event.registerLayerDefinition(CHICOBO_LAYER, ChicoboModel::createBodyLayer);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        for (ChocoboColor color : ChocoboColor.values()) {
            event.registerEntityRenderer(ChocoCraft.chocoboType(color), ChocoboRenderer::new);
        }
        event.registerEntityRenderer(ChocoCraft.CHICOBO.get(), ChicoboRenderer::new);
        event.registerEntityRenderer(ChocoCraft.PURPLE_CHOCOBO_EGG_ENTITY.get(), ThrownItemRenderer::new);
    }
}
