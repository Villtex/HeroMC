package betterdungeons.client.human;

import betterdungeons.BetterDungeons;
import betterdungeons.mob.HumanBosses;
import betterdungeons.mob.HumanEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

/**
 * client.rendererHuman.RenderHuman (and RenderHumanGremlin/Ogre/Golem/CustomArmor, RenderPirateBoss): the biped of the
 * kind scaled by getSizeModifier, lowered when sitting, with armour (ModelX(1.0)/(0.5)), both hands' items, the
 * captain's feather, the boss cape (a banner sprite of /bdimg/items.png) and the speech bubble. Client check open.
 */
public class HumanRenderer extends HumanoidMobRenderer<HumanEntity, HumanRenderState, HumanModel> {
    public static final Identifier ITEMS = Identifier.fromNamespaceAndPath(BetterDungeons.MOD_ID, "textures/entity/items.png");
    private final Function<HumanEntity, Identifier> texture;
    private final float featherY;
    private final float sitOffset;
    private final float extraOffset;
    private final boolean ghost;
    private final boolean ghostAlways;

    /** Options of the renderer subclasses. */
    public record Options(float featherY, float sitOffset, float extraOffset, boolean ghost, boolean ghostAlways) {
        public static final Options DEFAULT = new Options(-0.5F, 0.6F, 0.0F, false, false);
    }

    public HumanRenderer(EntityRendererProvider.Context context, ModelLayerLocation body, HumanModel.Anim anim,
            @Nullable ArmorModelSet<ModelLayerLocation> armor, Function<HumanEntity, Identifier> texture, float shadow, Options options) {
        super(context, new HumanModel(context.bakeLayer(body), anim), shadow);
        this.texture = texture;
        this.featherY = options.featherY();
        this.sitOffset = options.sitOffset();
        this.extraOffset = options.extraOffset();
        this.ghost = options.ghost();
        this.ghostAlways = options.ghostAlways();
        if (armor != null) {
            this.addLayer(new HumanoidArmorLayer<>(this, ArmorModelSet.bake(armor, context.getModelSet(), part -> new HumanModel(part, anim)),
                    context.getEquipmentRenderer()));
        }
        this.addLayer(new ExtrasLayer(this));
    }

    @Override
    public HumanRenderState createRenderState() {
        return new HumanRenderState();
    }

    @Override
    public void extractRenderState(HumanEntity entity, HumanRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isCrouching = false;
        state.sitting = entity.isSitting();
        state.aiming = entity.isAiming();
        state.defending = entity.isDefending();
        state.twoHanded = entity.isTwoHanded();
        state.gunner = entity.isGunner();
        state.bannerman = entity.isBannerman();
        state.captain = entity.isCaptain();
        state.speaking = entity.isSpeaking();
        state.sneakPose = entity.isSneakingHuman();
        state.riding = entity.isPassenger();
        state.hasMainHandItem = !entity.getMainHandItem().isEmpty();
        state.sizeModifier = entity.getSizeModifier();
        state.showCape = entity.shouldRenderCape();
        state.capeSprite = entity.shiedID;
        state.speechSprite = (entity.tickCount / 160 + entity.getId()) % 16;
        state.bossInvisible = entity instanceof HumanBosses.PirateBoss && entity.isInvisible();
        state.swingInProgress = entity.isSwinging();
        state.texture = this.texture.apply(entity);
        if (state.captain) {
            this.itemModelResolver.updateForLiving(state.feather, new ItemStack(Items.FEATHER), ItemDisplayContext.NONE, entity);
        } else {
            state.feather.clear();
        }
    }

    @Override
    public Identifier getTextureLocation(HumanRenderState state) {
        return state.texture;
    }

    /** preRenderCallback (glScale by the size modifier, the gremlin 0.7 lower) and setSitOffset. */
    @Override
    protected void scale(HumanRenderState state, PoseStack poseStack) {
        float s = state.sizeModifier;
        poseStack.scale(s, s, s);
        if (this.extraOffset != 0.0F) {
            poseStack.translate(0.0F, this.extraOffset, 0.0F);
        }
        if (state.sitting) {
            poseStack.translate(0.0F, this.sitOffset, 0.0F);
        }
    }

    /** ModelSpecter: additive "ghost" drawing while not swinging (always for the specters, not for their lord). */
    @Override
    protected @Nullable RenderType getRenderType(HumanRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
        if (this.ghost && isBodyVisible && (this.ghostAlways || !state.swingInProgress)) {
            return RenderTypes.entityTranslucentEmissive(this.getTextureLocation(state));
        }
        return super.getRenderType(state, isBodyVisible, forceTransparent, appearGlowing);
    }

    @Override
    public void submit(HumanRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.speaking) {
            // renderSpeech: a 0.6 bubble 3 blocks above the feet, facing the camera, one of 16 sprites in row 13
            poseStack.pushPose();
            poseStack.translate(0.0F, 3.0F, 0.0F);
            poseStack.rotate(camera.orientation);
            int sprite = state.speechSprite;
            float u0 = (sprite % 16 * 16) / 256.0F;
            float u1 = (sprite % 16 * 16 + 16) / 256.0F;
            float v0 = 0.8125F;
            float v1 = 0.875F;
            float size = 0.6F;
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(ITEMS), (pose, buffer) -> {
                quad(buffer, pose, state.lightCoords, -size / 2, 0.0F, size / 2, size, u0, v1, u1, v0);
            });
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }

    static void quad(VertexConsumer buffer, PoseStack.Pose pose, int light, float x0, float y0, float x1, float y1, float u0, float v0, float u1,
            float v1) {
        vertex(buffer, pose, light, x0, y0, u0, v0);
        vertex(buffer, pose, light, x1, y0, u1, v0);
        vertex(buffer, pose, light, x1, y1, u1, v1);
        vertex(buffer, pose, light, x0, y1, u0, v1);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, int light, float x, float y, float u, float v) {
        buffer.addVertex(pose, x, y, 0.0F).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    /** renderHelmetFeather (captains) and renderCape (bosses); the pirate captain shows neither while invisible. */
    class ExtrasLayer extends RenderLayer<HumanRenderState, HumanModel> {
        ExtrasLayer(RenderLayerParent<HumanRenderState, HumanModel> parent) {
            super(parent);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, HumanRenderState state, float yRot, float xRot) {
            if (state.bossInvisible || state.isInvisible) {
                return;
            }
            if (state.captain && !state.feather.isEmpty()) {
                poseStack.pushPose();
                this.getParentModel().head.translateAndRotate(poseStack);
                poseStack.translate(-0.05F, HumanRenderer.this.featherY, 0.01F);
                poseStack.rotate(new org.joml.Quaternionf().rotateAxis(Mth.PI, new Vector3f(1.0F, 0.0F, 1.0F).normalize()));
                poseStack.rotateDegrees(Axis.YP, 125.0F);
                poseStack.scale(0.5F, 0.5F, 0.5F);
                // renderItem of 1.5.2 drew the sprite from 0 to 1; the NONE model is centred
                poseStack.translate(0.5F, 0.5F, -0.03125F);
                state.feather.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
                poseStack.popPose();
            }
            if (state.showCape) {
                poseStack.pushPose();
                poseStack.translate(-0.5F, 0.0F, 0.2F);
                poseStack.rotateDegrees(Axis.XP, 10.0F);
                this.getParentModel().body.translateAndRotate(poseStack);
                int sprite = state.capeSprite;
                float u0 = (sprite % 16 * 16) / 256.0F;
                float u1 = (sprite % 16 * 16 + 16) / 256.0F;
                collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(ITEMS), (pose, buffer) -> {
                    quad(buffer, pose, light, 0.0F, 0.0F, 1.0F, 1.2F, u0, 0.0625F, u1, 0.1875F);
                });
                poseStack.popPose();
            }
        }
    }
}
