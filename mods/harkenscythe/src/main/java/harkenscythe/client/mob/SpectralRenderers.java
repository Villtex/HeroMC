package harkenscythe.client.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import harkenscythe.HarkenScythe;
import harkenscythe.mob.EssenceSlime;
import harkenscythe.mob.HSMobs;
import harkenscythe.mob.SimpleSpectral;
import harkenscythe.mob.SpectralBat;
import harkenscythe.mob.SpectralBlaze;
import harkenscythe.mob.SpectralCreeper;
import harkenscythe.mob.SpectralEnderman;
import harkenscythe.mob.SpectralGhast;
import harkenscythe.mob.SpectralOcelot;
import harkenscythe.mob.SpectralPet;
import harkenscythe.mob.SpectralSheep;
import harkenscythe.mob.SpectralSquid;
import harkenscythe.mob.SpectralWolf;
import java.util.function.Function;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractCubeMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * RenderHSSpectral*, RenderHSHarbinger, RenderHSSlime: the renderers of the mobs. Spectral ones draw translucent
 * (blending as in the original models), the Harbinger is drawn like a zombie with the thin skeleton limbs.
 */
public final class SpectralRenderers {
    private SpectralRenderers() {
    }

    static Identifier tex(String path) {
        return HarkenScythe.id("textures/entity/" + path + ".png");
    }

    /** Takes what the models need from the entity. */
    static void fill(Mob e, SpectralRenderState s, float pt) {
        if (e instanceof SpectralPet pet) {
            s.sitting = pet.isInSittingPose();
            s.tame = pet.isTame();
        }
        if (e instanceof SimpleSpectral c && c.kind() == SimpleSpectral.Kind.CHICKEN) {
            float flap = Mth.lerp(pt, c.oFlap, c.flap);
            float speed = Mth.lerp(pt, c.oFlapSpeed, c.flapSpeed);
            s.flap = (Mth.sin(flap) + 1.0F) * speed;
        } else if (e instanceof SpectralSheep sheep) {
            s.headEatPos = sheep.getHeadEatPositionScale(pt);
            s.headEatAngle = sheep.getHeadEatAngleScale(pt);
            s.sheared = sheep.isSheared();
        } else if (e instanceof SpectralWolf wolf) {
            s.tailAngle = wolf.getTailAngle();
            s.shakeHead = wolf.getBodyRollAngle(pt, 0.0F);
            s.shakeMane = wolf.getBodyRollAngle(pt, -0.08F);
            s.shakeBody = wolf.getBodyRollAngle(pt, -0.16F);
            s.shakeTail = wolf.getBodyRollAngle(pt, -0.2F);
            s.wetShade = wolf.isShaking() ? wolf.getWetShade(pt) : 1.0F;
            s.collar = wolf.getCollarColor();
        } else if (e instanceof SpectralOcelot ocelot) {
            s.skin = ocelot.getTameSkin();
        } else if (e instanceof SpectralBat bat) {
            s.resting = bat.isResting();
        } else if (e instanceof SpectralSquid squid) {
            s.xBodyRot = Mth.lerp(pt, squid.xBodyRotO, squid.xBodyRot);
            s.zBodyRot = Mth.lerp(pt, squid.zBodyRotO, squid.zBodyRot);
            s.tentacleAngle = Mth.lerp(pt, squid.oldTentacleAngle, squid.tentacleAngle);
        } else if (e instanceof SpectralCreeper creeper) {
            s.swell = creeper.getSwelling(pt);
            s.powered = creeper.isPowered();
        } else if (e instanceof SpectralEnderman enderman) {
            s.screaming = enderman.isScreaming();
        } else if (e instanceof SpectralGhast ghast) {
            s.charging = ghast.isCharging();
        }
    }

    // ------------------------------------------------------------------------------------------------ renderers
    /** RenderLiving with a spectral model. */
    public static class Spectral<T extends Mob> extends MobRenderer<T, SpectralRenderState, EntityModel<SpectralRenderState>> {
        private final Function<SpectralRenderState, Identifier> texture;
        private final float scale;

        public Spectral(EntityRendererProvider.Context context, EntityModel<SpectralRenderState> model, float shadow, float scale,
                Function<SpectralRenderState, Identifier> texture) {
            super(context, model, shadow);
            this.texture = texture;
            this.scale = scale;
        }

        @Override
        public SpectralRenderState createRenderState() {
            return new SpectralRenderState();
        }

        @Override
        public void extractRenderState(T entity, SpectralRenderState state, float pt) {
            super.extractRenderState(entity, state, pt);
            fill(entity, state, pt);
            state.texture = this.texture.apply(state);
        }

        @Override
        public Identifier getTextureLocation(SpectralRenderState state) {
            return state.texture;
        }

        @Override
        protected void scale(SpectralRenderState state, PoseStack poseStack) {
            float s = this.scale;
            if (state.skin > 0) {
                s *= 0.8F; // tamed cat skins
            }
            poseStack.scale(s, s, s);
            if (state.swell != 0.0F) {
                // updateCreeperScale
                float f = state.swell;
                float wobble = 1.0F + Mth.sin(f * 100.0F) * f * 0.01F;
                f = Mth.clamp(f, 0.0F, 1.0F);
                f *= f;
                f *= f;
                float xz = (1.0F + f * 0.4F) * wobble;
                float y = (1.0F + f * 0.1F) / wobble;
                poseStack.scale(xz, y, xz);
            }
        }

        @Override
        protected float getWhiteOverlayProgress(SpectralRenderState state) {
            float f = state.swell;
            return (int) (f * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(f, 0.5F, 1.0F);
        }

        @Override
        protected int getModelTint(SpectralRenderState state) {
            int v = (int) (state.wetShade * 255.0F);
            return ARGB.color(255, v, v, v);
        }
    }

    static class Bat extends Spectral<SpectralBat> {
        Bat(EntityRendererProvider.Context c) {
            super(c, new SpectralModels.Model(c.bakeLayer(SpectralModels.BAT), SpectralModels.BAT_ANIM), 0.25F, 0.35F, s -> tex("spectral/bat"));
        }

        @Override
        protected void setupRotations(SpectralRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
            if (state.resting) {
                poseStack.translate(0.0F, -0.1F, 0.0F);
            } else {
                poseStack.translate(0.0F, Mth.cos(state.ageInTicks * 0.3F) * 0.1F, 0.0F);
            }
            super.setupRotations(state, poseStack, bodyRot, entityScale);
        }
    }

    static class Squid extends Spectral<SpectralSquid> {
        Squid(EntityRendererProvider.Context c) {
            super(c, new SpectralModels.Model(c.bakeLayer(SpectralModels.SQUID), SpectralModels.SQUID_ANIM), 0.3F, 1.0F, s -> tex("spectral/squid"));
        }

        @Override
        protected void setupRotations(SpectralRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
            poseStack.translate(0.0F, 0.5F, 0.0F);
            poseStack.rotateDegrees(Axis.YP, 180.0F - bodyRot);
            poseStack.rotateDegrees(Axis.XP, state.xBodyRot);
            poseStack.rotateDegrees(Axis.YP, state.zBodyRot);
            poseStack.translate(0.0F, -1.2F, 0.0F);
        }
    }

    static class Blaze extends Spectral<SpectralBlaze> {
        Blaze(EntityRendererProvider.Context c) {
            super(c, new SpectralModels.Model(c.bakeLayer(SpectralModels.BLAZE), SpectralModels.BLAZE_ANIM), 0.5F, 1.0F, s -> tex("spectral/blaze"));
        }

        /** getBrightness 1.0 */
        @Override
        protected int getBlockLightLevel(SpectralBlaze entity, BlockPos pos) {
            return 15;
        }
    }

    /** RenderHSSpectralBiped / Zombie / Skeleton / Miner, RenderHSHarbinger: held item and armour. */
    public static class Biped<T extends Mob> extends HumanoidMobRenderer<T, SpectralRenderState, SpectralModels.Biped> {
        private final Identifier texture;
        private final float scale;

        public Biped(EntityRendererProvider.Context c, net.minecraft.client.model.geom.ModelLayerLocation layer, boolean zombieArms, boolean translucent,
                float shadow, float scale, Identifier texture) {
            super(c, new SpectralModels.Biped(c.bakeLayer(layer), zombieArms, translucent), shadow);
            this.texture = texture;
            this.scale = scale;
            this.addLayer(new HumanoidArmorLayer<>(this, ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, c.getModelSet(), HumanoidModel::new),
                    c.getEquipmentRenderer()));
        }

        @Override
        public SpectralRenderState createRenderState() {
            return new SpectralRenderState();
        }

        @Override
        public void extractRenderState(T entity, SpectralRenderState state, float pt) {
            super.extractRenderState(entity, state, pt);
            fill(entity, state, pt);
        }

        @Override
        public Identifier getTextureLocation(SpectralRenderState state) {
            return this.texture;
        }

        @Override
        protected void scale(SpectralRenderState state, PoseStack poseStack) {
            poseStack.scale(this.scale, this.scale, this.scale);
        }
    }

    /** RenderHSSlime: the inner body, then the outer one translucent. */
    static class Slime extends AbstractCubeMobRenderer<EssenceSlime, SlimeRenderState, SlimeModel> {
        private final Identifier texture;

        Slime(EntityRendererProvider.Context c, Identifier texture) {
            super(c, new SlimeModel(c.bakeLayer(SpectralModels.SLIME)));
            this.texture = texture;
            SlimeModel outer = new SlimeModel(c.bakeLayer(ModelLayers.SLIME_OUTER));
            this.addLayer(new RenderLayer<>(this) {
                @Override
                public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, SlimeRenderState state, float yRot, float xRot) {
                    if (!state.isInvisible) {
                        collector.order(1).submitModel(outer, state, poseStack, RenderTypes.entityTranslucent(Slime.this.texture), light,
                                LivingEntityRenderer.getOverlayCoords(state, 0.0F), state.outlineColor);
                    }
                }
            });
        }

        @Override
        public SlimeRenderState createRenderState() {
            return new SlimeRenderState();
        }

        @Override
        public Identifier getTextureLocation(SlimeRenderState state) {
            return this.texture;
        }
    }

    // ------------------------------------------------------------------------------------------------ layers
    /** The fleece pass (spectral_sheep_fur) while not sheared. */
    static class Overlay extends RenderLayer<SpectralRenderState, EntityModel<SpectralRenderState>> {
        private final EntityModel<SpectralRenderState> model;
        private final Function<SpectralRenderState, Identifier> texture;
        private final Function<SpectralRenderState, Integer> color;

        Overlay(RenderLayerParent<SpectralRenderState, EntityModel<SpectralRenderState>> parent, EntityModel<SpectralRenderState> model,
                Function<SpectralRenderState, Identifier> texture, Function<SpectralRenderState, Integer> color) {
            super(parent);
            this.model = model;
            this.texture = texture;
            this.color = color;
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, SpectralRenderState state, float yRot, float xRot) {
            Identifier t = this.texture.apply(state);
            if (t == null || state.isInvisible) {
                return;
            }
            collector.order(1).submitModel(this.model, state, poseStack, RenderTypes.entityTranslucent(t), light,
                    LivingEntityRenderer.getOverlayCoords(state, 0.0F), this.color.apply(state), null, state.outlineColor);
        }
    }

    static class Eyes extends EyesLayer<SpectralRenderState, EntityModel<SpectralRenderState>> {
        private final RenderType type;

        Eyes(RenderLayerParent<SpectralRenderState, EntityModel<SpectralRenderState>> parent, Identifier texture) {
            super(parent);
            this.type = RenderTypes.eyes(texture);
        }

        @Override
        public RenderType renderType() {
            return this.type;
        }
    }

    static class Power extends EnergySwirlLayer<SpectralRenderState, EntityModel<SpectralRenderState>> {
        private static final Identifier POWER = Identifier.withDefaultNamespace("textures/entity/creeper/creeper_armor.png");
        private final EntityModel<SpectralRenderState> model;

        Power(RenderLayerParent<SpectralRenderState, EntityModel<SpectralRenderState>> parent, EntityModel<SpectralRenderState> model) {
            super(parent);
            this.model = model;
        }

        @Override
        protected boolean isPowered(SpectralRenderState state) {
            return state.powered;
        }

        @Override
        protected float xOffset(float t) {
            return t * 0.01F;
        }

        @Override
        protected Identifier getTextureLocation() {
            return POWER;
        }

        @Override
        protected EntityModel<SpectralRenderState> model() {
            return this.model;
        }
    }

    // ------------------------------------------------------------------------------------------------ registration
    private static SpectralModels.Model model(EntityRendererProvider.Context c, net.minecraft.client.model.geom.ModelLayerLocation layer,
            SpectralModels.Anim anim) {
        return new SpectralModels.Model(c.bakeLayer(layer), anim);
    }

    private static <T extends Mob> Spectral<T> simple(EntityRendererProvider.Context c, net.minecraft.client.model.geom.ModelLayerLocation layer,
            SpectralModels.Anim anim, float shadow, float scale, String texture) {
        Identifier t = tex(texture);
        return new Spectral<>(c, model(c, layer, anim), shadow, scale, s -> t);
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(SpectralModels.CHICKEN, SpectralModels::chicken);
        e.registerLayerDefinition(SpectralModels.COW, SpectralModels::cow);
        e.registerLayerDefinition(SpectralModels.PIG, SpectralModels::pig);
        e.registerLayerDefinition(SpectralModels.SHEEP, () -> SpectralModels.sheep(false));
        e.registerLayerDefinition(SpectralModels.SHEEP_FUR, () -> SpectralModels.sheep(true));
        e.registerLayerDefinition(SpectralModels.WOLF, SpectralModels::wolf);
        e.registerLayerDefinition(SpectralModels.OCELOT, SpectralModels::ocelot);
        e.registerLayerDefinition(SpectralModels.BAT, SpectralModels::bat);
        e.registerLayerDefinition(SpectralModels.SQUID, SpectralModels::squid);
        e.registerLayerDefinition(SpectralModels.VILLAGER, SpectralModels::villager);
        e.registerLayerDefinition(SpectralModels.WITCH, SpectralModels::witch);
        e.registerLayerDefinition(SpectralModels.CREEPER, () -> SpectralModels.creeper(0.0F));
        e.registerLayerDefinition(SpectralModels.CREEPER_ARMOR, () -> SpectralModels.creeper(2.0F));
        e.registerLayerDefinition(SpectralModels.SPIDER, SpectralModels::spider);
        e.registerLayerDefinition(SpectralModels.SILVERFISH, SpectralModels::silverfish);
        e.registerLayerDefinition(SpectralModels.ENDERMAN, SpectralModels::enderman);
        e.registerLayerDefinition(SpectralModels.BLAZE, SpectralModels::blaze);
        e.registerLayerDefinition(SpectralModels.GHAST, SpectralModels::ghast);
        e.registerLayerDefinition(SpectralModels.BIPED, () -> SpectralModels.biped(32));
        e.registerLayerDefinition(SpectralModels.BIPED_64, () -> SpectralModels.biped(64));
        e.registerLayerDefinition(SpectralModels.SKELETON, SpectralModels::skeleton);
        e.registerLayerDefinition(SpectralModels.SLIME, SpectralModels::slimeInner);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(HSMobs.SPECTRAL_CHICKEN.get(), c -> simple(c, SpectralModels.CHICKEN, SpectralModels.CHICKEN_ANIM, 0.3F, 1.0F, "spectral/chicken"));
        e.registerEntityRenderer(HSMobs.SPECTRAL_COW.get(), c -> simple(c, SpectralModels.COW, SpectralModels.QUADRUPED_ANIM, 0.3F, 1.0F, "spectral/cow"));
        e.registerEntityRenderer(HSMobs.SPECTRAL_PIG.get(), c -> simple(c, SpectralModels.PIG, SpectralModels.QUADRUPED_ANIM, 0.3F, 1.0F, "spectral/pig"));
        e.registerEntityRenderer(HSMobs.SPECTRAL_SHEEP.get(), c -> {
            Spectral<SpectralSheep> r = simple(c, SpectralModels.SHEEP, SpectralModels.SHEEP_ANIM, 0.3F, 1.0F, "spectral/sheep");
            Identifier fur = tex("spectral/sheep_fur");
            r.addLayer(new Overlay(r, model(c, SpectralModels.SHEEP_FUR, SpectralModels.SHEEP_ANIM), s -> s.sheared ? null : fur, s -> -1));
            return r;
        });
        e.registerEntityRenderer(HSMobs.SPECTRAL_WOLF.get(), c -> {
            Spectral<SpectralWolf> r = simple(c, SpectralModels.WOLF, SpectralModels.WOLF_ANIM, 0.3F, 1.0F, "spectral/wolf");
            Identifier collar = Identifier.withDefaultNamespace("textures/entity/wolf/wolf_collar.png");
            r.addLayer(new Overlay(r, r.getModel(), s -> s.tame ? collar : null, s -> s.collar.getTextureDiffuseColor()));
            return r;
        });
        e.registerEntityRenderer(HSMobs.SPECTRAL_OCELOT.get(), c -> new Spectral<SpectralOcelot>(c,
                model(c, SpectralModels.OCELOT, SpectralModels.OCELOT_ANIM), 0.3F, 1.0F,
                s -> tex(switch (s.skin) {
                    case 1 -> "spectral/cat_black";
                    case 2 -> "spectral/cat_red";
                    case 3 -> "spectral/cat_siamese";
                    default -> "spectral/ocelot";
                })));
        e.registerEntityRenderer(HSMobs.SPECTRAL_BAT.get(), Bat::new);
        e.registerEntityRenderer(HSMobs.SPECTRAL_SQUID.get(), Squid::new);
        e.registerEntityRenderer(HSMobs.SPECTRAL_VILLAGER.get(), c -> simple(c, SpectralModels.VILLAGER, SpectralModels.VILLAGER_ANIM, 0.5F, 0.9375F,
                "spectral/villager"));
        e.registerEntityRenderer(HSMobs.SPECTRAL_WITCH.get(), c -> simple(c, SpectralModels.WITCH, SpectralModels.VILLAGER_ANIM, 0.5F, 0.9375F,
                "spectral/witch"));
        e.registerEntityRenderer(HSMobs.SPECTRAL_CREEPER.get(), c -> {
            Spectral<SpectralCreeper> r = simple(c, SpectralModels.CREEPER, SpectralModels.CREEPER_ANIM, 0.5F, 1.0F, "spectral/creeper");
            r.addLayer(new Power(r, model(c, SpectralModels.CREEPER_ARMOR, SpectralModels.CREEPER_ANIM)));
            return r;
        });
        e.registerEntityRenderer(HSMobs.SPECTRAL_SPIDER.get(), c -> {
            Spectral<harkenscythe.mob.SpectralSpider> r = simple(c, SpectralModels.SPIDER, SpectralModels.SPIDER_ANIM, 1.0F, 1.0F, "spectral/spider");
            r.addLayer(new Eyes(r, tex("spectral/spider_eyes")));
            return r;
        });
        e.registerEntityRenderer(HSMobs.SPECTRAL_CAVE_SPIDER.get(), c -> {
            Spectral<harkenscythe.mob.SpectralSpider> r = simple(c, SpectralModels.SPIDER, SpectralModels.SPIDER_ANIM, 0.7F, 0.7F,
                    "spectral/cave_spider");
            r.addLayer(new Eyes(r, Identifier.withDefaultNamespace("textures/entity/spider/spider_eyes.png")));
            return r;
        });
        e.registerEntityRenderer(HSMobs.SPECTRAL_SILVERFISH.get(), c -> simple(c, SpectralModels.SILVERFISH, SpectralModels.SILVERFISH_ANIM, 0.3F, 1.0F,
                "spectral/silverfish"));
        e.registerEntityRenderer(HSMobs.SPECTRAL_ENDERMAN.get(), c -> {
            Spectral<SpectralEnderman> r = simple(c, SpectralModels.ENDERMAN, SpectralModels.ENDERMAN_ANIM, 0.5F, 1.0F, "spectral/enderman");
            r.addLayer(new Eyes(r, tex("spectral/enderman_eyes")));
            return r;
        });
        e.registerEntityRenderer(HSMobs.SPECTRAL_BLAZE.get(), Blaze::new);
        Identifier ghast = tex("spectral/ghast");
        Identifier ghastFire = tex("spectral/ghast_fire");
        e.registerEntityRenderer(HSMobs.SPECTRAL_GHAST.get(), c -> new Spectral<SpectralGhast>(c, model(c, SpectralModels.GHAST, SpectralModels.GHAST_ANIM),
                0.5F, 4.5F, s -> s.charging ? ghastFire : ghast));
        e.registerEntityRenderer(HSMobs.SPECTRAL_ZOMBIE.get(), c -> new Biped<>(c, SpectralModels.BIPED_64, true, true, 0.5F, 1.0F,
                tex("spectral/zombie")));
        e.registerEntityRenderer(HSMobs.SPECTRAL_PIG_ZOMBIE.get(), c -> new Biped<>(c, SpectralModels.BIPED_64, true, true, 0.3F, 1.0F,
                tex("spectral/pig_zombie")));
        e.registerEntityRenderer(HSMobs.SPECTRAL_SKELETON.get(), c -> new Biped<>(c, SpectralModels.SKELETON, true, true, 0.5F, 1.0F,
                tex("spectral/skeleton")));
        e.registerEntityRenderer(HSMobs.SPECTRAL_WITHER_SKELETON.get(), c -> new Biped<>(c, SpectralModels.SKELETON, true, true, 0.5F, 1.2F,
                tex("spectral/wither_skeleton")));
        e.registerEntityRenderer(HSMobs.SPECTRAL_MINER.get(), c -> new Biped<>(c, SpectralModels.BIPED, false, true, 0.3F, 1.0F, tex("spectral/miner")));
        e.registerEntityRenderer(HSMobs.SPECTRAL_MINER_EVIL.get(), c -> new Biped<>(c, SpectralModels.BIPED, false, true, 0.3F, 1.0F,
                tex("spectral/miner_evil")));
        e.registerEntityRenderer(HSMobs.HARBINGER.get(), c -> new Biped<>(c, SpectralModels.SKELETON, true, false, 0.5F, 1.0F, tex("harbinger")));
        e.registerEntityRenderer(HSMobs.ECTOPLASM.get(), c -> new Slime(c, tex("ectoplasm")));
        e.registerEntityRenderer(HSMobs.HEMOGLOBIN.get(), c -> new Slime(c, tex("hemoglobin")));
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(SpectralRenderers::layers);
        modBus.addListener(SpectralRenderers::renderers);
    }
}
