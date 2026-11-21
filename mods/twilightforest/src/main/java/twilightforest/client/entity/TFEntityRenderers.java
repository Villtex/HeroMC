package twilightforest.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.pig.PigModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import twilightforest.TFEntities;
import twilightforest.TwilightForest;
import twilightforest.entity.passive.BighornSheep;
import twilightforest.entity.passive.QuestRam;
import twilightforest.entity.passive.TFBird;
import twilightforest.entity.passive.TinyBird;

import java.util.Set;
import java.util.function.Supplier;

/** Model layers and renderers of the Twilight mobs (TFClientProxy.registerRenderInformation). */
public final class TFEntityRenderers {
    static final ModelLayerLocation WILD_BOAR = layer("wild_boar");
    static final ModelLayerLocation WILD_BOAR_BABY = layer("wild_boar_baby");
    static final ModelLayerLocation BIGHORN = layer("bighorn_sheep");
    static final ModelLayerLocation BIGHORN_BABY = layer("bighorn_sheep_baby");
    static final ModelLayerLocation BIGHORN_FUR = layer("bighorn_sheep_fur");
    static final ModelLayerLocation BIGHORN_FUR_BABY = layer("bighorn_sheep_fur_baby");
    static final ModelLayerLocation WILD_DEER = layer("wild_deer");
    static final ModelLayerLocation WILD_DEER_BABY = layer("wild_deer_baby");
    static final ModelLayerLocation PENGUIN = layer("penguin");
    static final ModelLayerLocation PENGUIN_BABY = layer("penguin_baby");
    static final ModelLayerLocation TINY_BIRD = layer("tiny_bird");
    static final ModelLayerLocation RAVEN = layer("raven");
    static final ModelLayerLocation SQUIRREL = layer("squirrel");
    static final ModelLayerLocation BUNNY = layer("bunny");
    static final ModelLayerLocation QUEST_RAM = layer("quest_ram");
    static final ModelLayerLocation QUEST_RAM_SEGMENT = layer("quest_ram_segment");
    static final ModelLayerLocation TINY_FIREFLY = layer("tiny_firefly");
    static final ModelLayerLocation REDCAP = layer("redcap");
    static final ModelLayerLocation KOBOLD = layer("kobold");
    static final ModelLayerLocation SKELETON_DRUID = layer("skeleton_druid");
    static final ModelLayerLocation WRAITH = layer("wraith");
    static final ModelLayerLocation MINOTAUR = layer("minotaur");
    static final ModelLayerLocation MOSQUITO_SWARM = layer("mosquito_swarm");
    static final ModelLayerLocation DEATH_TOME = layer("death_tome");
    static final ModelLayerLocation FIRE_BEETLE = layer("fire_beetle");
    static final ModelLayerLocation SLIME_BEETLE = layer("slime_beetle");
    static final ModelLayerLocation PINCH_BEETLE = layer("pinch_beetle");
    static final ModelLayerLocation TOWER_GHAST = layer("tower_ghast");
    static final ModelLayerLocation TOWER_GOLEM = layer("tower_golem");
    static final ModelLayerLocation BLOCK_GOBLIN = layer("block_goblin");
    static final ModelLayerLocation SPIKE_BLOCK = layer("spike_block");
    static final ModelLayerLocation GOBLIN_CHAIN = layer("goblin_chain");
    static final ModelLayerLocation UPPER_GOBLIN_KNIGHT = layer("upper_goblin_knight");
    static final ModelLayerLocation LOWER_GOBLIN_KNIGHT = layer("lower_goblin_knight");
    static final ModelLayerLocation HELMET_CRAB = layer("helmet_crab");

    private TFEntityRenderers() {
    }

    static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(TwilightForest.id(name), "main");
    }

    /** The child render of ModelQuadruped / the birds: head full size moved by (y, z), the rest half size. */
    static Supplier<LayerDefinition> baby(Supplier<LayerDefinition> adult, float headY, float headZ) {
        return () -> adult.get().apply(new BabyModelTransform(false, headY, headZ, Set.of("head")));
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(TFEntityRenderers::layers);
        modBus.addListener(TFEntityRenderers::renderers);
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WILD_BOAR, WildBoarModel::createBodyLayer);
        event.registerLayerDefinition(WILD_BOAR_BABY, baby(WildBoarModel::createBodyLayer, 4.0F, 4.0F));
        event.registerLayerDefinition(BIGHORN, BighornModel::createBodyLayer);
        event.registerLayerDefinition(BIGHORN_BABY, baby(BighornModel::createBodyLayer, 8.0F, 4.0F));
        event.registerLayerDefinition(BIGHORN_FUR, BighornFurModel::createBodyLayer);
        event.registerLayerDefinition(BIGHORN_FUR_BABY, baby(BighornFurModel::createBodyLayer, 8.0F, 4.0F));
        event.registerLayerDefinition(WILD_DEER, WildDeerModel::createBodyLayer);
        event.registerLayerDefinition(WILD_DEER_BABY, baby(WildDeerModel::createBodyLayer, 10.0F, 4.0F));
        event.registerLayerDefinition(PENGUIN, PenguinModel::createBodyLayer);
        event.registerLayerDefinition(PENGUIN_BABY, baby(PenguinModel::createBodyLayer, 5.0F, 0.75F));
        event.registerLayerDefinition(TINY_BIRD, TinyBirdModel::createBodyLayer);
        event.registerLayerDefinition(RAVEN, RavenModel::createBodyLayer);
        event.registerLayerDefinition(SQUIRREL, SquirrelModel::createBodyLayer);
        event.registerLayerDefinition(BUNNY, BunnyModel::createBodyLayer);
        event.registerLayerDefinition(QUEST_RAM, QuestRamModel::createBodyLayer);
        event.registerLayerDefinition(QUEST_RAM_SEGMENT, QuestRamModel.Segment::createBodyLayer);
        event.registerLayerDefinition(TINY_FIREFLY, TinyFireflyModel::createBodyLayer);
        event.registerLayerDefinition(REDCAP, RedcapModel::createBodyLayer);
        event.registerLayerDefinition(KOBOLD, KoboldModel::createBodyLayer);
        event.registerLayerDefinition(SKELETON_DRUID, SkeletonDruidModel::createBodyLayer);
        event.registerLayerDefinition(WRAITH, WraithModel::createBodyLayer);
        event.registerLayerDefinition(MINOTAUR, MinotaurModel::createBodyLayer);
        event.registerLayerDefinition(MOSQUITO_SWARM, MosquitoSwarmModel::createBodyLayer);
        event.registerLayerDefinition(DEATH_TOME, DeathTomeModel::createBodyLayer);
        event.registerLayerDefinition(FIRE_BEETLE, FireBeetleModel::createBodyLayer);
        event.registerLayerDefinition(SLIME_BEETLE, SlimeBeetleModel::createBodyLayer);
        event.registerLayerDefinition(PINCH_BEETLE, PinchBeetleModel::createBodyLayer);
        event.registerLayerDefinition(TOWER_GHAST, TowerGhastModel::createBodyLayer);
        event.registerLayerDefinition(TOWER_GOLEM, TowerGolemModel::createBodyLayer);
        event.registerLayerDefinition(BLOCK_GOBLIN, BlockGoblinModel::createBodyLayer);
        event.registerLayerDefinition(SPIKE_BLOCK, SpikeBlockModel::createBodyLayer);
        event.registerLayerDefinition(GOBLIN_CHAIN, SpikeBlockModel::createChainLayer);
        event.registerLayerDefinition(UPPER_GOBLIN_KNIGHT, GoblinKnightUpperModel::createBodyLayer);
        event.registerLayerDefinition(LOWER_GOBLIN_KNIGHT, GoblinKnightLowerModel::createBodyLayer);
        event.registerLayerDefinition(HELMET_CRAB, HelmetCrabModel::createBodyLayer);
    }

    private static void bird(TFBird bird, TFRenderStates.Bird state, float partialTick) {
        state.wing = bird.wingRotation(partialTick);
        state.landed = bird.isBirdLanded();
        state.type = bird instanceof TinyBird tiny ? tiny.getBirdType() : 0;
    }

    private static final String[] TINY_BIRD_TEXTURES = {"tinybirdbrown", "tinybirdblue", "tinybirdred", "tinybirdgold"};
    private static final String[] BUNNY_TEXTURES = {"bunnydutch", "bunnydutch", "bunnywhite", "bunnybrown"};

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        // RenderPig with the saddle of the pig model
        event.registerEntityRenderer(TFEntities.WILD_BOAR.get(), c -> {
            Identifier texture = TFMobRenderer.texture("wildboar");
            TFMobRenderer<twilightforest.entity.passive.WildBoar, PigRenderState> r = new TFMobRenderer<>(c,
                    new WildBoarModel(c.bakeLayer(WILD_BOAR)), new WildBoarModel(c.bakeLayer(WILD_BOAR_BABY)), 0.7F, PigRenderState::new,
                    s -> texture, (e, s) -> s.saddle = e.getItemBySlot(EquipmentSlot.SADDLE).copy());
            r.addLayer(new SimpleEquipmentLayer<>(r, c.getEquipmentRenderer(), EquipmentClientInfo.LayerType.PIG_SADDLE, s -> s.saddle,
                    new PigModel(c.bakeLayer(ModelLayers.PIG_SADDLE)), null));
            return r;
        });
        // RenderSheep: the fleece of the vanilla sheep texture on the bighorn's fur model
        event.registerEntityRenderer(TFEntities.BIGHORN_SHEEP.get(), c -> {
            Identifier texture = TFMobRenderer.texture("bighorn");
            TFMobRenderer<BighornSheep, SheepRenderState> r = new TFMobRenderer<>(c, new BighornModel(c.bakeLayer(BIGHORN)),
                    new BighornModel(c.bakeLayer(BIGHORN_BABY)), 0.7F, SheepRenderState::new, s -> texture, (e, s) -> {
                        float pt = 0.0F;
                        s.headEatAngleScale = e.getHeadEatAngleScale(pt);
                        s.headEatPositionScale = e.getHeadEatPositionScale(pt);
                        s.isSheared = e.isSheared();
                        s.woolColor = e.getColor();
                    });
            r.addLayer(new FurLayer(r, new BighornFurModel(c.bakeLayer(BIGHORN_FUR)), new BighornFurModel(c.bakeLayer(BIGHORN_FUR_BABY))));
            return r;
        });
        event.registerEntityRenderer(TFEntities.WILD_DEER.get(), c -> {
            Identifier texture = TFMobRenderer.texture("wilddeer");
            return new TFMobRenderer<twilightforest.entity.passive.WildDeer, LivingEntityRenderState>(c, new WildDeerModel(c.bakeLayer(WILD_DEER)),
                    new WildDeerModel(c.bakeLayer(WILD_DEER_BABY)), 0.7F, LivingEntityRenderState::new, s -> texture, (e, s) -> {
            });
        });
        event.registerEntityRenderer(TFEntities.PENGUIN.get(), c -> {
            Identifier texture = TFMobRenderer.texture("penguin");
            return new TFMobRenderer<twilightforest.entity.passive.Penguin, TFRenderStates.Bird>(c, new PenguinModel(c.bakeLayer(PENGUIN)),
                    new PenguinModel(c.bakeLayer(PENGUIN_BABY)), 1.0F, TFRenderStates.Bird::new, s -> texture, (e, s) -> bird(e, s, 1.0F));
        });
        event.registerEntityRenderer(TFEntities.TINY_BIRD.get(), c -> new TFMobRenderer<TinyBird, TFRenderStates.Bird>(c,
                new TinyBirdModel(c.bakeLayer(TINY_BIRD)), null, 1.0F, TFRenderStates.Bird::new,
                s -> TFMobRenderer.texture(TINY_BIRD_TEXTURES[Math.floorMod(s.type, 4)]), (e, s) -> bird(e, s, 1.0F)));
        event.registerEntityRenderer(TFEntities.RAVEN.get(), c -> {
            Identifier texture = TFMobRenderer.texture("raven");
            return new TFMobRenderer<twilightforest.entity.passive.Raven, TFRenderStates.Bird>(c, new RavenModel(c.bakeLayer(RAVEN)), null, 1.0F,
                    TFRenderStates.Bird::new, s -> texture, (e, s) -> bird(e, s, 1.0F));
        });
        event.registerEntityRenderer(TFEntities.SQUIRREL.get(), c -> TFMobRenderer.simple(c, new SquirrelModel(c.bakeLayer(SQUIRREL)), 1.0F, "squirrel"));
        event.registerEntityRenderer(TFEntities.BUNNY.get(), c -> new TFMobRenderer<twilightforest.entity.passive.Bunny, TFRenderStates.Variant>(c,
                new BunnyModel(c.bakeLayer(BUNNY)), null, 1.0F, TFRenderStates.Variant::new,
                s -> TFMobRenderer.texture(BUNNY_TEXTURES[Math.floorMod(s.type, 4)]), (e, s) -> s.type = e.getBunnyType()));
        // RenderTFQuestRam: the wool bands tinted with their colour, the glowing lines over everything (scale 1.025)
        event.registerEntityRenderer(TFEntities.QUEST_RAM.get(), c -> {
            Identifier texture = TFMobRenderer.texture("questram");
            TFMobRenderer<QuestRam, TFRenderStates.QuestRam> r = new TFMobRenderer<>(c, new QuestRamModel(c.bakeLayer(QUEST_RAM)), null, 1.0F,
                    TFRenderStates.QuestRam::new, s -> texture, (e, s) -> s.colorFlags = e.getColorFlags());
            r.addLayer(new QuestRamBands(r, c));
            r.addLayer(new QuestRamLines(r, new QuestRamModel(c.bakeLayer(QUEST_RAM))));
            return r;
        });
        event.registerEntityRenderer(TFEntities.FIREFLY.get(), c -> new FireflyRenderer(c, TINY_FIREFLY));
        monsters(event);
        towerAndStronghold(event);
    }

    private static void monsters(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TFEntities.REDCAP.get(), c -> new TFBipedRenderer<>(c, new RedcapModel(c.bakeLayer(REDCAP)), 0.625F, "redcap"));
        event.registerEntityRenderer(TFEntities.REDCAP_SAPPER.get(),
                c -> new TFBipedRenderer<>(c, new RedcapModel(c.bakeLayer(REDCAP)), 0.625F, "redcapsapper"));
        // the jaw opens while the kobold rises (setLivingAnimations: motionY > 0), carried in isFallFlying
        event.registerEntityRenderer(TFEntities.KOBOLD.get(), c -> new TFBipedRenderer<twilightforest.entity.Kobold>(c,
                new KoboldModel(c.bakeLayer(KOBOLD)), 0.625F, "kobold", false, (e, s) -> s.isFallFlying = e.getDeltaMovement().y > 0.0));
        event.registerEntityRenderer(TFEntities.SKELETON_DRUID.get(),
                c -> new TFBipedRenderer<>(c, new SkeletonDruidModel(c.bakeLayer(SKELETON_DRUID)), 0.5F, "skeletondruid"));
        event.registerEntityRenderer(TFEntities.MINOTAUR.get(), c -> new TFBipedRenderer<>(c, new MinotaurModel(c.bakeLayer(MINOTAUR)), 0.625F, "minotaur"));
        // RenderTFWraith: the crowned ghost, then ghost.png added on top (glBlendFunc(1, 1))
        event.registerEntityRenderer(TFEntities.WRAITH.get(), c -> {
            TFBipedRenderer<twilightforest.entity.Wraith> r = new TFBipedRenderer<>(c, new WraithModel(c.bakeLayer(WRAITH)), 0.5F, "ghost_crown",
                    false, (e, s) -> {
            });
            WraithModel glow = new WraithModel(c.bakeLayer(WRAITH));
            Identifier ghost = TFMobRenderer.texture("ghost");
            r.addLayer(new RenderLayer<>(r) {
                @Override
                public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light,
                        net.minecraft.client.renderer.entity.state.HumanoidRenderState state, float yRot, float xRot) {
                    collector.submitModel(glow, state, poseStack, RenderTypes.eyes(ghost), light, LivingEntityRenderer.getOverlayCoords(state, 0.0F), -1);
                }
            });
            return r;
        });
        event.registerEntityRenderer(TFEntities.SWARM_SPIDER.get(), c -> new TFSpiderRenderer<>(c, "swarmspider", 0.5F));
        event.registerEntityRenderer(TFEntities.HEDGE_SPIDER.get(), c -> new TFSpiderRenderer<>(c, "hedgespider", 1.0F));
        event.registerEntityRenderer(TFEntities.KING_SPIDER.get(), c -> new TFSpiderRenderer<>(c, "kingspider", 1.9F));
        event.registerEntityRenderer(TFEntities.HOSTILE_WOLF.get(), net.minecraft.client.renderer.entity.WolfRenderer::new);
        event.registerEntityRenderer(TFEntities.MIST_WOLF.get(), MistWolfRenderer::new);
        event.registerEntityRenderer(TFEntities.MOSQUITO_SWARM.get(),
                c -> TFMobRenderer.simple(c, new MosquitoSwarmModel(c.bakeLayer(MOSQUITO_SWARM)), 0.625F, "mosquitoswarm"));
        // the book texture of the enchanting table (1.5.2: /item/book.png)
        event.registerEntityRenderer(TFEntities.DEATH_TOME.get(), c -> {
            Identifier book = Identifier.withDefaultNamespace("textures/entity/enchantment/enchanting_table_book.png");
            return new TFMobRenderer<twilightforest.entity.DeathTome, LivingEntityRenderState>(c, new DeathTomeModel(c.bakeLayer(DEATH_TOME)), null,
                    0.625F, LivingEntityRenderState::new, s -> book, (e, s) -> {
            });
        });
        // the fire beetle glows while it breathes (getBrightnessForRender 15728880)
        event.registerEntityRenderer(TFEntities.FIRE_BEETLE.get(), c -> {
            Identifier texture = TFMobRenderer.texture("firebeetle");
            return new TFMobRenderer<twilightforest.entity.FireBeetle, LivingEntityRenderState>(c, new FireBeetleModel(c.bakeLayer(FIRE_BEETLE)),
                    null, 1.1F, LivingEntityRenderState::new, s -> texture, (e, s) -> {
            }) {
                @Override
                protected int getBlockLightLevel(twilightforest.entity.FireBeetle entity, net.minecraft.core.BlockPos pos) {
                    return entity.isBreathing() ? 15 : super.getBlockLightLevel(entity, pos);
                }

                @Override
                protected int getSkyLightLevel(twilightforest.entity.FireBeetle entity, net.minecraft.core.BlockPos pos) {
                    return entity.isBreathing() ? 15 : super.getSkyLightLevel(entity, pos);
                }
            };
        });
        // RenderTFSlimeBeetle: the clear slime cube in a translucent pass
        event.registerEntityRenderer(TFEntities.SLIME_BEETLE.get(), c -> {
            Identifier texture = TFMobRenderer.texture("slimebeetle");
            TFMobRenderer<twilightforest.entity.SlimeBeetle, LivingEntityRenderState> r = new TFMobRenderer<>(c,
                    new SlimeBeetleModel(c.bakeLayer(SLIME_BEETLE)), null, 1.1F, LivingEntityRenderState::new, s -> texture, (e, s) -> {
            });
            SlimeBeetleModel cube = new SlimeBeetleModel(c.bakeLayer(SLIME_BEETLE), true);
            r.addLayer(new RenderLayer<>(r) {
                @Override
                public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, LivingEntityRenderState state, float yRot, float xRot) {
                    if (!state.isInvisible) {
                        collector.submitModel(cube, state, poseStack, RenderTypes.entityTranslucent(texture), light,
                                LivingEntityRenderer.getOverlayCoords(state, 0.0F), -1);
                    }
                }
            });
            return r;
        });
        event.registerEntityRenderer(TFEntities.PINCH_BEETLE.get(), c -> {
            Identifier texture = TFMobRenderer.texture("pinchbeetle");
            return new TFMobRenderer<twilightforest.entity.PinchBeetle, TFRenderStates.Flags>(c, new PinchBeetleModel(c.bakeLayer(PINCH_BEETLE)), null,
                    1.1F, TFRenderStates.Flags::new, s -> texture, (e, s) -> s.flag = e.isVehicle());
        });
        event.registerEntityRenderer(TFEntities.MAZE_SLIME.get(), c -> new net.minecraft.client.renderer.entity.SlimeRenderer(c) {
            private final Identifier texture = TFMobRenderer.texture("mazeslime");

            @Override
            public Identifier getTextureLocation(net.minecraft.client.renderer.entity.state.SlimeRenderState state) {
                return this.texture;
            }
        });
        event.registerEntityRenderer(TFEntities.NATURE_BOLT.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        event.registerEntityRenderer(TFEntities.TOME_BOLT.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        event.registerEntityRenderer(TFEntities.SLIME_BLOB.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
    }

    private static final String[] GHAST_TEXTURES = {"towerghast", "towerghast_openeyes", "towerghast_fire"};

    /** Dark tower and knight stronghold mobs. */
    private static void towerAndStronghold(EntityRenderersEvent.RegisterRenderers event) {
        // RenderTFTowerGhast: the swelling formula with the never synced attack counter gives (8 + 1) / 2 in all axes
        event.registerEntityRenderer(TFEntities.TOWER_GHAST.get(), c -> new TFMobRenderer<twilightforest.entity.TowerGhast, TFRenderStates.Variant>(c,
                new TowerGhastModel(c.bakeLayer(TOWER_GHAST)), null, 0.625F, TFRenderStates.Variant::new,
                s -> TFMobRenderer.texture(GHAST_TEXTURES[Math.floorMod(s.type, 3)]), (e, s) -> s.type = e.aggroStatus()).scaled(4.5F));
        event.registerEntityRenderer(TFEntities.MINI_GHAST.get(), c -> new TFMobRenderer<twilightforest.entity.MiniGhast, TFRenderStates.Variant>(c,
                new TowerGhastModel(c.bakeLayer(TOWER_GHAST)), null, 0.625F, TFRenderStates.Variant::new,
                s -> TFMobRenderer.texture(GHAST_TEXTURES[Math.floorMod(s.type, 3)]), (e, s) -> s.type = e.aggroStatus()));
        // RenderTFTowerGolem: the body sways round Z while it walks (rotateCorpse)
        event.registerEntityRenderer(TFEntities.TOWER_GOLEM.get(), c -> {
            Identifier texture = TFMobRenderer.texture("carminitegolem");
            return new TFMobRenderer<twilightforest.entity.TowerGolem, TFRenderStates.Flags>(c, new TowerGolemModel(c.bakeLayer(TOWER_GOLEM)), null,
                    0.5F, TFRenderStates.Flags::new, s -> texture, (e, s) -> s.value = e.getAttackTimer()) {
                @Override
                protected void setupRotations(TFRenderStates.Flags state, PoseStack poseStack, float bodyRot, float scale) {
                    super.setupRotations(state, poseStack, bodyRot, scale);
                    if (state.walkAnimationSpeed >= 0.01F) {
                        float sway = TowerGolemModel.triangle(state.walkAnimationPos + 6.0F, 13.0F);
                        poseStack.rotateDegrees(com.mojang.math.Axis.ZP, 6.5F * sway);
                    }
                }
            };
        });
        // RenderLiving(ModelSilverfish)
        event.registerEntityRenderer(TFEntities.TOWER_TERMITE.get(), c -> {
            Identifier texture = TFMobRenderer.texture("towertermite");
            return new TFMobRenderer<twilightforest.entity.TowerTermite, LivingEntityRenderState>(c,
                    new net.minecraft.client.model.monster.silverfish.SilverfishModel(c.bakeLayer(ModelLayers.SILVERFISH)), null, 0.3F,
                    LivingEntityRenderState::new, s -> texture, (e, s) -> {
            }) {
                @Override
                protected float getFlipDegrees() {
                    return 180.0F;
                }
            };
        });
        event.registerEntityRenderer(TFEntities.TOWER_BROODLING.get(), c -> new TFSpiderRenderer<>(c, "towerbroodling", 0.5F));
        event.registerEntityRenderer(TFEntities.BLOCK_GOBLIN.get(), BlockGoblinRenderer::new);
        event.registerEntityRenderer(TFEntities.UPPER_GOBLIN_KNIGHT.get(), c -> new GoblinKnightRenderer<twilightforest.entity.GoblinKnightUpper>(c,
                new GoblinKnightUpperModel(c.bakeLayer(UPPER_GOBLIN_KNIGHT)), (e, s) -> {
            s.hasArmor = e.hasArmor();
            s.hasShield = e.hasShield();
            s.heavySpearTimer = e.heavySpearTimer;
        }));
        event.registerEntityRenderer(TFEntities.LOWER_GOBLIN_KNIGHT.get(), c -> new GoblinKnightRenderer<twilightforest.entity.GoblinKnightLower>(c,
                new GoblinKnightLowerModel(c.bakeLayer(LOWER_GOBLIN_KNIGHT)), (e, s) -> {
            s.hasArmor = e.hasArmor();
            s.isVehicle = e.isVehicle();
        }));
        event.registerEntityRenderer(TFEntities.HELMET_CRAB.get(),
                c -> TFMobRenderer.simple(c, new HelmetCrabModel(c.bakeLayer(HELMET_CRAB)), 0.625F, "helmetcrab"));
    }

    /** RenderSheep's fleece pass: the vanilla sheep_fur texture tinted with the fleece colour, unless shorn. */
    static final class FurLayer extends RenderLayer<SheepRenderState, EntityModel<? super SheepRenderState>> {
        private static final Identifier FUR = Identifier.withDefaultNamespace("textures/entity/sheep/sheep_wool.png");
        private final EntityModel<SheepRenderState> adult;
        private final EntityModel<SheepRenderState> baby;

        FurLayer(LivingEntityRenderer<?, SheepRenderState, EntityModel<? super SheepRenderState>> parent, EntityModel<SheepRenderState> adult,
                EntityModel<SheepRenderState> baby) {
            super(parent);
            this.adult = adult;
            this.baby = baby;
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, SheepRenderState state, float yRot, float xRot) {
            if (!state.isSheared && !state.isInvisible) {
                coloredCutoutModelCopyLayerRender(state.isBaby ? this.baby : this.adult, FUR, poseStack, collector, light, state,
                        state.getWoolColor(), 1);
            }
        }
    }

    /** The coloured bands of the questing ram. */
    static final class QuestRamBands extends RenderLayer<TFRenderStates.QuestRam, EntityModel<? super TFRenderStates.QuestRam>> {
        private static final Identifier TEXTURE = TFMobRenderer.texture("questram");
        private final QuestRamModel.Segment[] segments = new QuestRamModel.Segment[16];

        QuestRamBands(LivingEntityRenderer<?, TFRenderStates.QuestRam, EntityModel<? super TFRenderStates.QuestRam>> parent,
                EntityRendererProvider.Context context) {
            super(parent);
            for (int i = 0; i < 16; i++) {
                this.segments[i] = new QuestRamModel.Segment(context.bakeLayer(QUEST_RAM_SEGMENT), i);
            }
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, TFRenderStates.QuestRam state, float yRot, float xRot) {
            for (int i = 0; i < 16; i++) {
                if ((state.colorFlags & 1 << i) != 0) {
                    renderColoredCutoutModel(this.segments[i], TEXTURE, poseStack, collector, light, state,
                            ARGB.opaque(DyeColor.byId(i).getTextureDiffuseColor()), 1);
                }
            }
        }
    }

    /** questram_lines.png, additive and full bright, 2.5 % larger. */
    static final class QuestRamLines extends RenderLayer<TFRenderStates.QuestRam, EntityModel<? super TFRenderStates.QuestRam>> {
        private static final Identifier LINES = TFMobRenderer.texture("questram_lines");
        private final QuestRamModel model;

        QuestRamLines(LivingEntityRenderer<?, TFRenderStates.QuestRam, EntityModel<? super TFRenderStates.QuestRam>> parent, QuestRamModel model) {
            super(parent);
            this.model = model;
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, TFRenderStates.QuestRam state, float yRot, float xRot) {
            poseStack.pushPose();
            poseStack.scale(1.025F, 1.025F, 1.025F);
            collector.submitModel(this.model, state, poseStack, RenderTypes.eyes(LINES), 0xF000F0, LivingEntityRenderer.getOverlayCoords(state, 0.0F), -1);
            poseStack.popPose();
        }
    }
}
