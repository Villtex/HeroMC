package projectzulu.client.entity;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import projectzulu.ProjectZulu;
import projectzulu.ZuluEntities;

import java.util.function.Supplier;

/** The mob renderers and model layers of Project Zulu. Babies use the adult mesh at half scale (the original did the same). */
public final class ZuluEntityRenderers {
    public static final ModelLayerLocation ARMADILLO_LAYER = layer("armadillo");
    public static final ModelLayerLocation ARMADILLO_BABY_LAYER = layer("armadillo_baby");
    public static final ModelLayerLocation FINCH_LAYER = layer("finch");
    public static final ModelLayerLocation EAGLE_LAYER = layer("eagle");
    public static final ModelLayerLocation VULTURE_LAYER = layer("vulture");
    public static final ModelLayerLocation HORN_BILL_LAYER = layer("horn_bill");
    public static final ModelLayerLocation PELICAN_LAYER = layer("pelican");

    private ZuluEntityRenderers() {
    }

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, name), "main");
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ARMADILLO_LAYER, ArmadilloModel::createBodyLayer);
        event.registerLayerDefinition(ARMADILLO_BABY_LAYER, baby(ArmadilloModel::createBodyLayer));
        event.registerLayerDefinition(FINCH_LAYER, FinchModel::createBodyLayer);
        event.registerLayerDefinition(EAGLE_LAYER, EagleModel::createBodyLayer);
        event.registerLayerDefinition(VULTURE_LAYER, VultureModel::createBodyLayer);
        event.registerLayerDefinition(HORN_BILL_LAYER, HornBillModel::createBodyLayer);
        event.registerLayerDefinition(PELICAN_LAYER, PelicanModel::createBodyLayer);
    }

    /** The 1.5.2 child render scaled the whole model by 0.5. */
    private static Supplier<LayerDefinition> baby(Supplier<LayerDefinition> adult) {
        return () -> adult.get().apply(MeshTransformer.scaling(0.5F));
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ZuluEntities.ARMADILLO.get(), ArmadilloRenderer::new);
        event.registerEntityRenderer(ZuluEntities.RED_FINCH.get(), c -> new AerialRenderer<>(c,
                new FinchModel(c.bakeLayer(FINCH_LAYER)), BirdRenderState::new, "finch_red"));
        event.registerEntityRenderer(ZuluEntities.GREEN_FINCH.get(), c -> new AerialRenderer<>(c,
                new FinchModel(c.bakeLayer(FINCH_LAYER)), BirdRenderState::new, "finch_green"));
        event.registerEntityRenderer(ZuluEntities.BLUE_FINCH.get(), c -> new AerialRenderer<>(c,
                new FinchModel(c.bakeLayer(FINCH_LAYER)), BirdRenderState::new, "finch_blue"));
        event.registerEntityRenderer(ZuluEntities.EAGLE.get(), c -> new AerialRenderer<>(c,
                new EagleModel(c.bakeLayer(EAGLE_LAYER)), BirdRenderState::new, "eagle"));
        event.registerEntityRenderer(ZuluEntities.VULTURE.get(), VultureRenderer::new);
        event.registerEntityRenderer(ZuluEntities.HORN_BILL.get(), c -> new AerialRenderer<>(c,
                new HornBillModel(c.bakeLayer(HORN_BILL_LAYER)), BirdRenderState::new, "horn_bill"));
        event.registerEntityRenderer(ZuluEntities.PELICAN.get(), c -> new AerialRenderer<>(c,
                new PelicanModel(c.bakeLayer(PELICAN_LAYER)), BirdRenderState::new, "pelican"));
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ZuluEntityRenderers::layers);
        modBus.addListener(ZuluEntityRenderers::renderers);
    }
}
