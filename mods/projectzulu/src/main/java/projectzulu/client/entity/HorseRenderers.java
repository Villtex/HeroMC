package projectzulu.client.entity;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import projectzulu.ZuluHorses;

/** Renderers and model layers of {@link projectzulu.ZuluHorses}: one model, the colour picks the texture. */
public final class HorseRenderers {
    public static final ModelLayerLocation HORSE_LAYER = ZuluEntityRenderers.layer("horse");
    public static final ModelLayerLocation HORSE_BABY_LAYER = ZuluEntityRenderers.layer("horse_baby");

    private HorseRenderers() {
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(HORSE_LAYER, ZuluHorseModel::createBodyLayer);
        event.registerLayerDefinition(HORSE_BABY_LAYER, ZuluHorseModel::createBabyLayer);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ZuluHorses.HORSE_BEIGE.get(), c -> new ZuluHorseRenderer(c, "beige"));
        event.registerEntityRenderer(ZuluHorses.HORSE_BLACK.get(), c -> new ZuluHorseRenderer(c, "black"));
        event.registerEntityRenderer(ZuluHorses.HORSE_BROWN.get(), c -> new ZuluHorseRenderer(c, "brown"));
        event.registerEntityRenderer(ZuluHorses.HORSE_DARK_BLACK.get(), c -> new ZuluHorseRenderer(c, "dark_black"));
        event.registerEntityRenderer(ZuluHorses.HORSE_DARK_BROWN.get(), c -> new ZuluHorseRenderer(c, "dark_brown"));
        event.registerEntityRenderer(ZuluHorses.HORSE_GREY.get(), c -> new ZuluHorseRenderer(c, "grey"));
        event.registerEntityRenderer(ZuluHorses.HORSE_WHITE.get(), c -> new ZuluHorseRenderer(c, "white"));
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(HorseRenderers::layers);
        modBus.addListener(HorseRenderers::renderers);
    }
}
