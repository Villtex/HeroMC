package projectzulu.client.entity;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import projectzulu.ProjectZulu;
import projectzulu.ZuluEntities;
import projectzulu.entity.FoxEntity;
import projectzulu.entity.FrogEntity;
import projectzulu.entity.RabbitEntity;

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
    public static final ModelLayerLocation BOAR_LAYER = layer("boar");
    public static final ModelLayerLocation BOAR_BABY_LAYER = layer("boar_baby");
    public static final ModelLayerLocation FOX_LAYER = layer("fox");
    public static final ModelLayerLocation FOX_BABY_LAYER = layer("fox_baby");
    public static final ModelLayerLocation FROG_LAYER = layer("frog");
    public static final ModelLayerLocation FROG_BABY_LAYER = layer("frog_baby");
    public static final ModelLayerLocation RABBIT_LAYER = layer("rabbit");
    public static final ModelLayerLocation RABBIT_BABY_LAYER = layer("rabbit_baby");
    public static final ModelLayerLocation BEAVER_LAYER = layer("beaver");
    public static final ModelLayerLocation BEAVER_BABY_LAYER = layer("beaver_baby");
    public static final ModelLayerLocation PENGUIN_LAYER = layer("penguin");
    public static final ModelLayerLocation PENGUIN_BABY_LAYER = layer("penguin_baby");
    public static final ModelLayerLocation OSTRICH_LAYER = layer("ostrich");
    public static final ModelLayerLocation OSTRICH_BABY_LAYER = layer("ostrich_baby");

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
        event.registerLayerDefinition(BOAR_LAYER, BoarModel::createBodyLayer);
        event.registerLayerDefinition(BOAR_BABY_LAYER, baby(BoarModel::createBodyLayer));
        event.registerLayerDefinition(FOX_LAYER, FoxModel::createBodyLayer);
        event.registerLayerDefinition(FOX_BABY_LAYER, baby(FoxModel::createBodyLayer));
        event.registerLayerDefinition(FROG_LAYER, FrogModel::createBodyLayer);
        event.registerLayerDefinition(FROG_BABY_LAYER, baby(FrogModel::createBodyLayer));
        event.registerLayerDefinition(RABBIT_LAYER, RabbitModel::createBodyLayer);
        event.registerLayerDefinition(RABBIT_BABY_LAYER, baby(RabbitModel::createBodyLayer));
        event.registerLayerDefinition(BEAVER_LAYER, BeaverModel::createBodyLayer);
        event.registerLayerDefinition(BEAVER_BABY_LAYER, baby(BeaverModel::createBodyLayer));
        event.registerLayerDefinition(PENGUIN_LAYER, PenguinModel::createBodyLayer);
        event.registerLayerDefinition(PENGUIN_BABY_LAYER, baby(PenguinModel::createBodyLayer));
        event.registerLayerDefinition(OSTRICH_LAYER, OstrichModel::createBodyLayer);
        event.registerLayerDefinition(OSTRICH_BABY_LAYER, baby(OstrichModel::createBodyLayer));
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
        // land animals (Batch B)
        event.registerEntityRenderer(ZuluEntities.BOAR.get(), c -> new LandAnimalRenderer<>(c, new BoarModel(c.bakeLayer(BOAR_LAYER)),
                new BoarModel(c.bakeLayer(BOAR_BABY_LAYER)), LivingEntityRenderState::new, "boar"));
        event.registerEntityRenderer(ZuluEntities.FOX.get(), c -> new LandAnimalRenderer<FoxEntity, FoxModel.State>(c,
                new FoxModel(c.bakeLayer(FOX_LAYER)), new FoxModel(c.bakeLayer(FOX_BABY_LAYER)), FoxModel.State::new, "fox",
                (fox, state) -> state.sitting = fox.isInSittingPose()));
        event.registerEntityRenderer(ZuluEntities.FROG.get(), c -> new LandAnimalRenderer<FrogEntity, FrogModel.State>(c,
                new FrogModel(c.bakeLayer(FROG_LAYER)), new FrogModel(c.bakeLayer(FROG_BABY_LAYER)), FrogModel.State::new, "frog",
                (frog, state) -> state.onGround = frog.onGround()));
        event.registerEntityRenderer(ZuluEntities.RABBIT.get(), c -> new LandAnimalRenderer<RabbitEntity, RabbitModel.State>(c,
                new RabbitModel(c.bakeLayer(RABBIT_LAYER)), new RabbitModel(c.bakeLayer(RABBIT_BABY_LAYER)), RabbitModel.State::new, "rabbit",
                (rabbit, state) -> state.onGround = rabbit.onGround()));
        event.registerEntityRenderer(ZuluEntities.BEAVER.get(), c -> new LandAnimalRenderer<>(c, new BeaverModel(c.bakeLayer(BEAVER_LAYER)),
                new BeaverModel(c.bakeLayer(BEAVER_BABY_LAYER)), LivingEntityRenderState::new, "beaver"));
        event.registerEntityRenderer(ZuluEntities.PENGUIN.get(), c -> new LandAnimalRenderer<>(c, new PenguinModel(c.bakeLayer(PENGUIN_LAYER)),
                new PenguinModel(c.bakeLayer(PENGUIN_BABY_LAYER)), LivingEntityRenderState::new, "penguin"));
        event.registerEntityRenderer(ZuluEntities.OSTRICH.get(), c -> new LandAnimalRenderer<>(c, new OstrichModel(c.bakeLayer(OSTRICH_LAYER)),
                new OstrichModel(c.bakeLayer(OSTRICH_BABY_LAYER)), LivingEntityRenderState::new, "ostrich"));
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ZuluEntityRenderers::layers);
        modBus.addListener(ZuluEntityRenderers::renderers);
    }
}
