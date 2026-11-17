package projectzulu.client.entity;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Renderers and model layers of {@link projectzulu.ZuluMonsters}. */
public final class MonsterRenderers {
    private MonsterRenderers() {
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(MonsterRenderers::layers);
        modBus.addListener(MonsterRenderers::renderers);
    }
}
