package projectzulu.client.entity;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import projectzulu.ZuluBigAnimals;

/** Renderers and model layers of {@link projectzulu.ZuluBigAnimals}. */
public final class BigAnimalRenderers {
    public static final ModelLayerLocation BLACK_BEAR_LAYER = ZuluEntityRenderers.layer("black_bear");
    public static final ModelLayerLocation BLACK_BEAR_BABY_LAYER = ZuluEntityRenderers.layer("black_bear_baby");
    public static final ModelLayerLocation BROWN_BEAR_LAYER = ZuluEntityRenderers.layer("brown_bear");
    public static final ModelLayerLocation BROWN_BEAR_BABY_LAYER = ZuluEntityRenderers.layer("brown_bear_baby");
    public static final ModelLayerLocation POLAR_BEAR_LAYER = ZuluEntityRenderers.layer("polar_bear");
    public static final ModelLayerLocation POLAR_BEAR_BABY_LAYER = ZuluEntityRenderers.layer("polar_bear_baby");
    public static final ModelLayerLocation GORILLA_LAYER = ZuluEntityRenderers.layer("gorilla");
    public static final ModelLayerLocation GORILLA_BABY_LAYER = ZuluEntityRenderers.layer("gorilla_baby");
    public static final ModelLayerLocation RHINO_LAYER = ZuluEntityRenderers.layer("rhino");
    public static final ModelLayerLocation RHINO_BABY_LAYER = ZuluEntityRenderers.layer("rhino_baby");
    public static final ModelLayerLocation ALLIGATOR_LAYER = ZuluEntityRenderers.layer("alligator");
    public static final ModelLayerLocation ALLIGATOR_BABY_LAYER = ZuluEntityRenderers.layer("alligator_baby");
    public static final ModelLayerLocation GIRAFFE_LAYER = ZuluEntityRenderers.layer("giraffe");
    public static final ModelLayerLocation ELEPHANT_LAYER = ZuluEntityRenderers.layer("elephant");
    public static final ModelLayerLocation ELEPHANT_BABY_LAYER = ZuluEntityRenderers.layer("elephant_baby");
    public static final ModelLayerLocation MAMMOTH_LAYER = ZuluEntityRenderers.layer("mammoth");
    public static final ModelLayerLocation MAMMOTH_BABY_LAYER = ZuluEntityRenderers.layer("mammoth_baby");
    public static final ModelLayerLocation TREE_ENT_LAYER = ZuluEntityRenderers.layer("tree_ent");
    public static final ModelLayerLocation TREE_ENT_BABY_LAYER = ZuluEntityRenderers.layer("tree_ent_baby");

    private BigAnimalRenderers() {
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // ModelBlackBear/ModelBrownBear/ModelPolarBear: heightToRaise 0/7/12, render scale 1/1.4/2; the child
        // parameters (body divisor, head scale, head offset j/g/h) of ModelBear.render per kind
        event.registerLayerDefinition(BLACK_BEAR_LAYER, () -> BearModel.createBodyLayer(0.0F, 1.0F));
        event.registerLayerDefinition(BLACK_BEAR_BABY_LAYER, () -> BearModel.createBabyLayer(0.0F, 1.0F, 2.0F, 1.2F, -0.8F, 4.0F, 5.0F));
        event.registerLayerDefinition(BROWN_BEAR_LAYER, () -> BearModel.createBodyLayer(7.0F, 1.4F));
        event.registerLayerDefinition(BROWN_BEAR_BABY_LAYER, () -> BearModel.createBabyLayer(7.0F, 1.4F, 2.5F, 1.0F, -0.55F, 8.0F, 4.0F));
        event.registerLayerDefinition(POLAR_BEAR_LAYER, () -> BearModel.createBodyLayer(12.0F, 2.0F));
        event.registerLayerDefinition(POLAR_BEAR_BABY_LAYER, () -> BearModel.createBabyLayer(12.0F, 2.0F, 3.0F, 0.7F, -0.3F, 8.0F, 4.0F));
        event.registerLayerDefinition(GORILLA_LAYER, GorillaModel::createBodyLayer);
        event.registerLayerDefinition(GORILLA_BABY_LAYER, GorillaModel::createBabyLayer);
        event.registerLayerDefinition(RHINO_LAYER, RhinoModel::createBodyLayer);
        event.registerLayerDefinition(RHINO_BABY_LAYER, RhinoModel::createBabyLayer);
        event.registerLayerDefinition(ALLIGATOR_LAYER, AlligatorModel::createBodyLayer);
        event.registerLayerDefinition(ALLIGATOR_BABY_LAYER, AlligatorModel::createBabyLayer);
        event.registerLayerDefinition(GIRAFFE_LAYER, GiraffeModel::createBodyLayer);
        event.registerLayerDefinition(ELEPHANT_LAYER, ElephantModel::createBodyLayer);
        event.registerLayerDefinition(ELEPHANT_BABY_LAYER, ElephantModel::createBabyLayer);
        event.registerLayerDefinition(MAMMOTH_LAYER, MammothModel::createBodyLayer);
        event.registerLayerDefinition(MAMMOTH_BABY_LAYER, MammothModel::createBabyLayer);
        event.registerLayerDefinition(TREE_ENT_LAYER, TreeEntModel::createBodyLayer);
        event.registerLayerDefinition(TREE_ENT_BABY_LAYER, TreeEntModel::createBabyLayer);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ZuluBigAnimals.BLACK_BEAR.get(), c -> new BigAnimalRenderer<>(c, new BearModel(c.bakeLayer(BLACK_BEAR_LAYER)),
                new BearModel(c.bakeLayer(BLACK_BEAR_BABY_LAYER)), "black_bear"));
        event.registerEntityRenderer(ZuluBigAnimals.BROWN_BEAR.get(), c -> new BigAnimalRenderer<>(c, new BearModel(c.bakeLayer(BROWN_BEAR_LAYER)),
                new BearModel(c.bakeLayer(BROWN_BEAR_BABY_LAYER)), "brown_bear"));
        event.registerEntityRenderer(ZuluBigAnimals.POLAR_BEAR.get(), c -> new BigAnimalRenderer<>(c, new BearModel(c.bakeLayer(POLAR_BEAR_LAYER)),
                new BearModel(c.bakeLayer(POLAR_BEAR_BABY_LAYER)), "polar_bear"));
        event.registerEntityRenderer(ZuluBigAnimals.GORILLA.get(), c -> new BigAnimalRenderer<>(c, new GorillaModel(c.bakeLayer(GORILLA_LAYER)),
                new GorillaModel(c.bakeLayer(GORILLA_BABY_LAYER)), "gorilla"));
        event.registerEntityRenderer(ZuluBigAnimals.RHINO.get(), c -> new BigAnimalRenderer<>(c, new RhinoModel(c.bakeLayer(RHINO_LAYER)),
                new RhinoModel(c.bakeLayer(RHINO_BABY_LAYER)), "rhino"));
        event.registerEntityRenderer(ZuluBigAnimals.ALLIGATOR.get(), c -> new BigAnimalRenderer<>(c, new AlligatorModel(c.bakeLayer(ALLIGATOR_LAYER)),
                new AlligatorModel(c.bakeLayer(ALLIGATOR_BABY_LAYER)), "alligator"));
        // no isChild branch in ModelGiraffe: the young giraffe uses the adult mesh
        event.registerEntityRenderer(ZuluBigAnimals.GIRAFFE.get(), c -> new BigAnimalRenderer<>(c, new GiraffeModel(c.bakeLayer(GIRAFFE_LAYER)),
                new GiraffeModel(c.bakeLayer(GIRAFFE_LAYER)), "giraffe"));
        event.registerEntityRenderer(ZuluBigAnimals.ELEPHANT.get(), c -> new BigAnimalRenderer<>(c, new ElephantModel(c.bakeLayer(ELEPHANT_LAYER)),
                new ElephantModel(c.bakeLayer(ELEPHANT_BABY_LAYER)), "elephant"));
        // EntityMammoth.getTexture: snow coat in Taiga/TaigaHills, saddle textures when saddled
        Identifier mammoth = BigAnimalRenderer.texture("mammoth");
        Identifier mammothSaddle = BigAnimalRenderer.texture("mammoth_saddle");
        Identifier mammothSnow = BigAnimalRenderer.texture("mammoth_snow");
        Identifier mammothSnowSaddle = BigAnimalRenderer.texture("mammoth_snow_saddle");
        event.registerEntityRenderer(ZuluBigAnimals.MAMMOTH.get(), c -> new BigAnimalRenderer<>(c, new MammothModel(c.bakeLayer(MAMMOTH_LAYER)),
                new MammothModel(c.bakeLayer(MAMMOTH_BABY_LAYER)),
                state -> state.saddled ? (state.snowy ? mammothSnowSaddle : mammothSaddle) : (state.snowy ? mammothSnow : mammoth)));
        // EntityTreeEnt.getTexture: snow in Taiga/TaigaHills
        Identifier treeEnt = BigAnimalRenderer.texture("tree_ent");
        Identifier treeEntSnow = BigAnimalRenderer.texture("tree_ent_snow");
        event.registerEntityRenderer(ZuluBigAnimals.TREE_ENT.get(), c -> new BigAnimalRenderer<>(c, new TreeEntModel(c.bakeLayer(TREE_ENT_LAYER)),
                new TreeEntModel(c.bakeLayer(TREE_ENT_BABY_LAYER)), state -> state.snowy ? treeEntSnow : treeEnt));
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(BigAnimalRenderers::layers);
        modBus.addListener(BigAnimalRenderers::renderers);
    }
}
