package projectzulu.client.entity;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Renderers and model layers of {@link projectzulu.ZuluHorses}. */
public final class HorseRenderers {
    private HorseRenderers() {
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(HorseRenderers::layers);
        modBus.addListener(HorseRenderers::renderers);
    }
}
