package projectzulu.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import projectzulu.ZuluWorld;
import projectzulu.client.world.LimitedMobSpawnerRenderer;

/** Teil 11 – client registration of batch World: the mob turning inside the limited mob spawner. */
public final class WorldClient {
    private WorldClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
                event.registerBlockEntityRenderer(ZuluWorld.LIMITED_MOB_SPAWNER_ENTITY.get(), LimitedMobSpawnerRenderer::new));
    }
}
