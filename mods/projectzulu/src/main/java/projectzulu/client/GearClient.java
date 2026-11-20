package projectzulu.client;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import projectzulu.ZuluGear;
import projectzulu.block.gear.TombstoneBlockEntity;
import projectzulu.client.gear.MobHeadRenderer;
import projectzulu.client.gear.TombstoneRenderer;
import projectzulu.client.gear.TombstoneScreen;
import projectzulu.network.gear.OpenTombstonePayload;

/** Teil 11 – client registration of batch Gear: head and tombstone renderers, the tombstone editor. */
public final class GearClient {
    private GearClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> {
            MobHeadRenderer.registerLayers(event);
            event.registerLayerDefinition(TombstoneRenderer.LAYER, TombstoneRenderer::createLayer);
        });
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerBlockEntityRenderer(ZuluGear.MOB_HEAD_ENTITY.get(), MobHeadRenderer::new);
            event.registerBlockEntityRenderer(ZuluGear.TOMBSTONE_ENTITY.get(), TombstoneRenderer::new);
        });
        modBus.addListener((RegisterClientPayloadHandlersEvent event) -> event.register(OpenTombstonePayload.TYPE, GearClient::openTombstone));
    }

    private static void openTombstone(OpenTombstonePayload payload, IPayloadContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.level.getBlockEntity(payload.pos()) instanceof TombstoneBlockEntity tombstone) {
            minecraft.setScreen(new TombstoneScreen(tombstone));
        }
    }
}
