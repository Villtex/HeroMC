package betterdungeons.client;

import betterdungeons.BetterDungeons;
import betterdungeons.client.human.EndermanArmorLayer;
import betterdungeons.client.human.HumanModel;
import betterdungeons.client.human.HumanModel.Anim;
import betterdungeons.client.human.HumanModel.Geo;
import betterdungeons.client.human.HumanRenderer;
import betterdungeons.client.human.MageRenderer;
import betterdungeons.client.human.MobControlScreen;
import betterdungeons.mob.BDHumans;
import betterdungeons.mob.HumanEntity;
import betterdungeons.mob.HumanMobs;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * ClientProxy.registerRenderInformation for the human mobs (strand b1): RenderHuman with the models of the kinds,
 * RenderHumanEnderman, RenderMage; and the controller screen (GuiMobControl).
 */
public final class BDHumanRenderers {
    private static final Map<Geo, ModelLayerLocation> BODY_32 = new EnumMap<>(Geo.class);
    private static final Map<Geo, ArmorModelSet<ModelLayerLocation>> ARMOR = new EnumMap<>(Geo.class);
    static final ModelLayerLocation BIPED_64 = layer("human_biped_64");
    static final ModelLayerLocation MAGE = layer("mage");

    static {
        for (Geo geo : Geo.values()) {
            String name = geo.name().toLowerCase();
            BODY_32.put(geo, layer("human_" + name));
            ARMOR.put(geo, new ArmorModelSet<>(layer("human_" + name + "_helmet"), layer("human_" + name + "_chestplate"),
                    layer("human_" + name + "_leggings"), layer("human_" + name + "_boots")));
        }
    }

    private BDHumanRenderers() {
    }

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(BetterDungeons.MOD_ID, name), "main");
    }

    static Identifier bd(String texture) {
        return Identifier.fromNamespaceAndPath(BetterDungeons.MOD_ID, "textures/entity/" + texture + ".png");
    }

    static Identifier mc(String texture) {
        return Identifier.withDefaultNamespace("textures/entity/" + texture + ".png");
    }

    private static <T extends HumanEntity> void human(EntityRenderersEvent.RegisterRenderers event, EntityType<T> type, ModelLayerLocation body,
            Anim anim, @Nullable Geo armor, Function<HumanEntity, Identifier> texture, float shadow, HumanRenderer.Options options) {
        event.registerEntityRenderer(type, (EntityRendererProvider<T>) context -> (net.minecraft.client.renderer.entity.EntityRenderer) new HumanRenderer(
                context, body, anim, armor == null ? null : ARMOR.get(armor), texture, shadow, options));
    }

    private static <T extends HumanEntity> void human(EntityRenderersEvent.RegisterRenderers event, EntityType<T> type, Geo geo, Anim anim,
            Geo armor, String texture) {
        Identifier tex = bd(texture);
        human(event, type, BODY_32.get(geo), anim, armor, e -> tex, 0.5F, HumanRenderer.Options.DEFAULT);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        HumanRenderer.Options def = HumanRenderer.Options.DEFAULT;
        HumanRenderer.Options ogre = new HumanRenderer.Options(-0.3F, 0.6F, 0.0F, false, false);
        HumanRenderer.Options gremlin = new HumanRenderer.Options(-0.3F, 0.1F, 0.7F, false, false);
        Identifier skeleton = mc("skeleton/skeleton");
        Identifier wither = mc("skeleton/wither_skeleton");
        human(event, BDHumans.ARMORED_SKELETON.get(), BODY_32.get(Geo.SKELETON), Anim.HUMAN, Geo.BIPED,
                e -> e instanceof HumanMobs.ArmoredSkeleton s && s.getSkeletonType() == 1 ? wither : skeleton, 0.5F, def);
        Identifier zombie = mc("zombie/zombie");
        human(event, BDHumans.ARMORED_ZOMBIE.get(), BIPED_64, Anim.ZOMBIE, Geo.BIPED, e -> zombie, 0.5F, def);
        human(event, BDHumans.DWARF.get(), Geo.BIPED, Anim.HUMAN, Geo.BIPED, "dwarf");
        human(event, BDHumans.PIRATE.get(), Geo.BIPED, Anim.HUMAN, Geo.BIPED, "pirate");
        Identifier pigZombie = mc("piglin/zombified_piglin");
        human(event, BDHumans.ARMORED_PIG_ZOMBIE.get(), ModelLayers.ZOMBIFIED_PIGLIN, Anim.ZOMBIE, Geo.BIPED, e -> pigZombie, 0.5F, def);
        human(event, BDHumans.GOBLIN.get(), Geo.BIPED, Anim.HUMAN, Geo.BIPED, "goblin_green");
        human(event, BDHumans.SNOW_GOBLIN.get(), Geo.BIPED, Anim.HUMAN, Geo.BIPED, "goblin_blue");
        human(event, BDHumans.WALKER.get(), Geo.BIPED, Anim.HUMAN, Geo.BIPED, "shadow");
        Identifier steve = mc("player/wide/steve");
        human(event, BDHumans.SPECTER.get(), BIPED_64, Anim.HUMAN, Geo.BIPED, e -> steve, 0.5F, new HumanRenderer.Options(-0.5F, 0.6F, 0.0F, true, true));
        human(event, BDHumans.ARMORED_ENDERMAN.get(), BODY_32.get(Geo.ENDERMAN), Anim.ENDERMAN, null, e -> mc("enderman/enderman"), 0.4F, def);
        human(event, BDHumans.MONKEY.get(), Geo.MONKEY, Anim.MONKEY, Geo.BIPED, "mandril_small");
        human(event, BDHumans.TRITON.get(), Geo.NAGA, Anim.NAGA, Geo.NAGA, "snake");
        Identifier goblin = bd("goblin");
        human(event, BDHumans.GREMLIN.get(), BODY_32.get(Geo.GREMLIN), Anim.GREMLIN, Geo.GREMLIN, e -> goblin, 0.5F, gremlin);
        Identifier ogreTex = bd("ogre");
        human(event, BDHumans.OGRE_ARMORED.get(), BODY_32.get(Geo.OGRE), Anim.HUMAN, Geo.OGRE, e -> ogreTex, 0.8F, ogre);
        human(event, BDHumans.GOLEM_SMALL.get(), Geo.GOLEM, Anim.HUMAN, Geo.GOLEM, "golem_small");
        human(event, BDHumans.MINOTAUR_ARMORED.get(), Geo.MINOTAUR, Anim.HUMAN, Geo.BIPED, "minotaur");
        Identifier orc = bd("orc");
        human(event, BDHumans.ORC_ARMORED.get(), BODY_32.get(Geo.OGRE), Anim.HUMAN, Geo.OGRE, e -> orc, 0.8F, ogre);
        human(event, BDHumans.WALKER_BOSS.get(), Geo.BIPED, Anim.HUMAN, Geo.BIPED, "shadow");
        human(event, BDHumans.PIRATE_BOSS.get(), Geo.BIPED, Anim.HUMAN, Geo.BIPED, "pirate_boss");
        Identifier specterBoss = bd("specter_boss");
        human(event, BDHumans.SPECTER_BOSS.get(), BODY_32.get(Geo.BIPED), Anim.HUMAN, Geo.BIPED, e -> specterBoss, 0.5F,
                new HumanRenderer.Options(-0.5F, 0.6F, 0.0F, true, false));
        Identifier shaman = bd("goblin_shaman");
        human(event, BDHumans.GREMLIN_BOSS.get(), BODY_32.get(Geo.GREMLIN), Anim.GREMLIN, Geo.GREMLIN, e -> shaman, 0.5F, gremlin);
        human(event, BDHumans.MINOTAUR_BOSS.get(), Geo.MINOTAUR, Anim.HUMAN, Geo.BIPED, "minotaur");
        human(event, BDHumans.GOBLIN_BOSS.get(), Geo.BIPED, Anim.HUMAN, Geo.BIPED, "goblin_green");
        // EntityKnight: RenderHuman with ModelHuman and /bdimg/ogre.png
        human(event, BDHumans.FRIENDLY_HUMAN.get(), Geo.BIPED, Anim.HUMAN, Geo.BIPED, "ogre");
        event.registerEntityRenderer(BDHumans.NECROMANCER.get(), c -> new MageRenderer(c, MAGE, "necromancer"));
        event.registerEntityRenderer(BDHumans.LICHE.get(), c -> new MageRenderer(c, MAGE, "liche"));
        event.registerEntityRenderer(BDHumans.PIG_MAGE.get(), c -> new MageRenderer(c, MAGE, "pig_mage"));
    }

    /** The enderman's armour layer needs the renderer: added after construction. */
    private static void addLayers(EntityRenderersEvent.AddLayers event) {
        Object renderer = event.getRenderer(BDHumans.ARMORED_ENDERMAN.get());
        if (renderer instanceof HumanRenderer human) {
            human.addLayer(new EndermanArmorLayer(human, ArmorModelSet.bake(ARMOR.get(Geo.ENDERMAN), event.getEntityModels(),
                    part -> new HumanModel(part, Anim.ENDERMAN))));
        }
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (Geo geo : Geo.values()) {
            MeshDefinition body = HumanModel.mesh(geo, CubeDeformation.NONE);
            event.registerLayerDefinition(BODY_32.get(geo), () -> LayerDefinition.create(body, 64, 32));
            // the enderman's armour had the body's size in 1.5.2 (its constructor ignored the inflation); a little more here
            ArmorModelSet<MeshDefinition> armor = geo == Geo.ENDERMAN
                    ? new ArmorModelSet<>(HumanModel.mesh(geo, new CubeDeformation(0.1F)), HumanModel.mesh(geo, new CubeDeformation(0.1F)),
                    HumanModel.mesh(geo, new CubeDeformation(0.05F)), HumanModel.mesh(geo, new CubeDeformation(0.1F)))
                    : HumanModel.armorMeshes(geo);
            ArmorModelSet<ModelLayerLocation> locations = ARMOR.get(geo);
            event.registerLayerDefinition(locations.head(), () -> LayerDefinition.create(armor.head(), 64, 32));
            event.registerLayerDefinition(locations.chest(), () -> LayerDefinition.create(armor.chest(), 64, 32));
            event.registerLayerDefinition(locations.legs(), () -> LayerDefinition.create(armor.legs(), 64, 32));
            event.registerLayerDefinition(locations.feet(), () -> LayerDefinition.create(armor.feet(), 64, 32));
        }
        event.registerLayerDefinition(BIPED_64, () -> LayerDefinition.create(HumanModel.mesh(Geo.BIPED, CubeDeformation.NONE), 64, 64));
        event.registerLayerDefinition(MAGE, MageRenderer.Model::createBodyLayer);
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(BDHumanRenderers::renderers);
        modBus.addListener(BDHumanRenderers::layers);
        modBus.addListener(BDHumanRenderers::addLayers);
        BDHumans.openControlScreen = MobControlScreen::open;
    }
}
