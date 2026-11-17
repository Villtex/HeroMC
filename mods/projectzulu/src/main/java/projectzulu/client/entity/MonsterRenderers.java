package projectzulu.client.entity;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import projectzulu.ZuluMonsters;
import projectzulu.entity.LizardEntity;
import projectzulu.entity.MimicEntity;
import projectzulu.entity.MinotaurEntity;
import projectzulu.entity.MummyEntity;
import projectzulu.entity.MummyPharaohEntity;
import projectzulu.entity.SandwormEntity;
import projectzulu.entity.ZuluMonster;

/** Renderers and model layers of {@link projectzulu.ZuluMonsters}. */
public final class MonsterRenderers {
    public static final ModelLayerLocation SANDWORM_LAYER = ZuluEntityRenderers.layer("sandworm");
    public static final ModelLayerLocation LIZARD_LAYER = ZuluEntityRenderers.layer("lizard");
    public static final ModelLayerLocation MUMMY_LAYER = ZuluEntityRenderers.layer("mummy");
    public static final ModelLayerLocation MIMIC_LAYER = ZuluEntityRenderers.layer("mimic");
    public static final ModelLayerLocation MINOTAUR_LAYER = ZuluEntityRenderers.layer("minotaur");
    public static final ModelLayerLocation HAUNTED_ARMOR_LAYER = ZuluEntityRenderers.layer("haunted_armor");
    public static final ModelLayerLocation CENTIPEDE_LAYER = ZuluEntityRenderers.layer("centipede");

    private MonsterRenderers() {
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SANDWORM_LAYER, SandwormModel::createBodyLayer);
        event.registerLayerDefinition(LIZARD_LAYER, LizardModel::createBodyLayer);
        event.registerLayerDefinition(MUMMY_LAYER, MummyModel::createBodyLayer);
        event.registerLayerDefinition(MIMIC_LAYER, MimicModel::createBodyLayer);
        event.registerLayerDefinition(MINOTAUR_LAYER, MinotaurModel::createBodyLayer);
        event.registerLayerDefinition(HAUNTED_ARMOR_LAYER, HauntedArmorModel::createBodyLayer);
        event.registerLayerDefinition(CENTIPEDE_LAYER, CentipedeModel::createBodyLayer);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ZuluMonsters.SANDWORM.get(), c -> new MonsterRenderer<SandwormEntity, SandwormModel.State>(c,
                new SandwormModel(c.bakeLayer(SANDWORM_LAYER)), SandwormModel.State::new, (e, s) -> s.hidden = e.isHidden(),
                s -> MonsterRenderer.tex(s.hidden ? "sandworm_hidden" : "sandworm")));
        event.registerEntityRenderer(ZuluMonsters.LIZARD.get(), c -> new MonsterRenderer<LizardEntity, LizardModel.State>(c,
                new LizardModel(c.bakeLayer(LIZARD_LAYER)), LizardModel.State::new, (e, s) -> s.prepareToSpit = e.isPreparingToSpit(), "lizard"));
        event.registerEntityRenderer(ZuluMonsters.LIZARD_SPIT.get(), LizardSpitRenderer::new);
        event.registerEntityRenderer(ZuluMonsters.MUMMY.get(), c -> new MummyRenderer<MummyEntity>(c, "mummy"));
        event.registerEntityRenderer(ZuluMonsters.MUMMY_PHARAOH.get(), c -> new MummyRenderer<MummyPharaohEntity>(c, "mummy_pharaoh"));
        event.registerEntityRenderer(ZuluMonsters.MIMIC.get(), c -> new MonsterRenderer<MimicEntity, MimicModel.State>(c,
                new MimicModel(c.bakeLayer(MIMIC_LAYER)), MimicModel.State::new, (e, s) -> s.idle = e.getState() == ZuluMonster.State.IDLE,
                "mimicchest"));
        event.registerEntityRenderer(ZuluMonsters.MINOTAUR.get(), c -> new MonsterRenderer<MinotaurEntity, MinotaurModel.State>(c,
                new MinotaurModel(c.bakeLayer(MINOTAUR_LAYER)), MinotaurModel.State::new, (e, s) -> {
                    s.attacking = e.getState() == ZuluMonster.State.ATTACKING;
                    s.animTime = e.getAnimTime();
                }, "minotaur"));
        event.registerEntityRenderer(ZuluMonsters.HAUNTED_ARMOR.get(), HauntedArmorRenderer::new);
        event.registerEntityRenderer(ZuluMonsters.CENTIPEDE.get(), CentipedeRenderer::new);
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(MonsterRenderers::layers);
        modBus.addListener(MonsterRenderers::renderers);
    }
}
