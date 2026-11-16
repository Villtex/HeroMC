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

    private ZuluEntityRenderers() {
    }

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, name), "main");
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ARMADILLO_LAYER, ArmadilloModel::createBodyLayer);
        event.registerLayerDefinition(ARMADILLO_BABY_LAYER, baby(ArmadilloModel::createBodyLayer));
    }

    /** The 1.5.2 child render scaled the whole model by 0.5. */
    private static Supplier<LayerDefinition> baby(Supplier<LayerDefinition> adult) {
        return () -> adult.get().apply(MeshTransformer.scaling(0.5F));
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ZuluEntities.ARMADILLO.get(), ArmadilloRenderer::new);
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ZuluEntityRenderers::layers);
        modBus.addListener(ZuluEntityRenderers::renderers);
    }
}
