package legendgear.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import legendgear.LGConfig;
import legendgear.LegendGear;
import legendgear.entity.FallingStarEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/**
 * RenderFallingStar: three camera-facing star sprites turning at different speeds in rainbow colours (Rainbow.r/g/b
 * of the time), shrinking while the fallen star dwindles. The light beam of the first ten ticks after the impact is
 * left out. Client check open.
 */
public class FallingStarRenderer extends EntityRenderer<FallingStarEntity, FallingStarRenderer.State> {
    private static final Identifier TEXTURE = LegendGear.id("textures/entity/star.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityTranslucentEmissive(TEXTURE);

    public static class State extends EntityRenderState {
        float scale;
    }

    public FallingStarRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    protected int getBlockLightLevel(FallingStarEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void extractRenderState(FallingStarEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.scale = entity.dwindle() * 3.0F / LGConfig.FALLEN_STAR_LIFETIME;
    }

    static float[] rainbow(float phase) {
        phase = (float) (phase * Math.PI * 2);
        float r = ((float) Math.sin(phase) + 1.0F) * 0.5F;
        float g = ((float) Math.sin(phase + Math.PI * 2.0 / 3.0) + 1.0F) * 0.5F;
        float b = ((float) Math.sin(phase + Math.PI * 4.0 / 3.0) + 1.0F) * 0.5F;
        float min = Math.min(r, Math.min(g, b));
        r -= min;
        g -= min;
        b -= min;
        float s = 1.0F / Math.max(r, Math.max(g, b));
        return new float[]{Math.min(s * r, 1.0F), Math.min(s * g * 0.5F + 0.5F, 1.0F), Math.min(s * b * 0.5F + 0.5F, 1.0F)};
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float phase = (System.currentTimeMillis() % 1000L) / 1000.0F;
        float[] c = rainbow(phase);
        float[][] colors = {{c[0], c[1], c[2]}, {c[1], c[2], c[0]}, {c[2], c[0], c[1]}};
        float[] spins = {180.0F * phase, -270.0F * phase, 90.0F * phase};
        for (int i = 0; i < 3; i++) {
            float[] col = colors[i];
            poseStack.pushPose();
            poseStack.rotate(camera.orientation);
            poseStack.rotate(Axis.ZP.rotationDegrees(spins[i]));
            poseStack.scale(state.scale, state.scale, state.scale);
            int color = 0xFF000000 | (int) (col[0] * 255) << 16 | (int) (col[1] * 255) << 8 | (int) (col[2] * 255);
            collector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, buffer) -> {
                vertex(buffer, pose, color, -0.5F, -0.5F, 0.0F, 1.0F);
                vertex(buffer, pose, color, 0.5F, -0.5F, 1.0F, 1.0F);
                vertex(buffer, pose, color, 0.5F, 0.5F, 1.0F, 0.0F);
                vertex(buffer, pose, color, -0.5F, 0.5F, 0.0F, 0.0F);
            });
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, int color, float x, float y, float u, float v) {
        buffer.addVertex(pose, x, y, 0.0F).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
