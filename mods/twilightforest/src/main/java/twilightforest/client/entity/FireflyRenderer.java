package twilightforest.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import twilightforest.entity.passive.MobileFirefly;

/**
 * RenderTFTinyFirefly for the mobile firefly: the 10 x 10 glow quad of firefly-tiny.png at scale 1/16, turned to the
 * camera, half a block up, with the firefly's glow brightness. Drawn additively and full bright (the original blended
 * with the brightness as alpha).
 */
public class FireflyRenderer extends EntityRenderer<MobileFirefly, FireflyRenderer.State> {
    static final Identifier TEXTURE = TFMobRenderer.texture("firefly_tiny");
    private final ModelPart glow;

    public FireflyRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer) {
        super(context);
        this.glow = context.bakeLayer(layer).getChild("glow1");
    }

    public static class State extends EntityRenderState {
        float brightness;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MobileFirefly entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.brightness = Mth.clamp(entity.glowBrightness(entity.tickCount + partialTick), 0.0F, 1.0F);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.5F, 0.0F);
        poseStack.rotate(camera.orientation);
        float b = state.brightness;
        collector.submitModelPart(this.glow, poseStack, RenderTypes.eyes(TEXTURE), 0xF000F0, OverlayTexture.NO_OVERLAY, null,
                ARGB.colorFromFloat(1.0F, b, b, b));
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
