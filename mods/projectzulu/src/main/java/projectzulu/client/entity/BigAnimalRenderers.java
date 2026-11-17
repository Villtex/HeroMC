package projectzulu.client.entity;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Renderers and model layers of {@link projectzulu.ZuluBigAnimals}. */
public final class BigAnimalRenderers {
    private BigAnimalRenderers() {
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(BigAnimalRenderers::layers);
        modBus.addListener(BigAnimalRenderers::renderers);
    }
}
