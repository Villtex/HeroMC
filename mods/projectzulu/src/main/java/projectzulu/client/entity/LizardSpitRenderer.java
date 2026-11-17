package projectzulu.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import projectzulu.entity.LizardSpitEntity;

/**
 * RenderLizardSpit(0.5): a camera-facing quad (x -0.5..0.5, y -0.25..0.75) at half size with icon 9 of
 * items_projectzulu.png (the lizard spit item icon), full bright.
 */
public class LizardSpitRenderer extends EntityRenderer<LizardSpitEntity, EntityRenderState> {
    private static final RenderType RENDER_TYPE = RenderTypes.entityCutout(MonsterRenderer.tex("lizard_spit"));

    public LizardSpitRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(LizardSpitEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.rotate(camera.orientation);
        collector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, buffer) -> quad(state, pose, buffer, -1));
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static void quad(EntityRenderState state, PoseStack.Pose pose, VertexConsumer buffer, int color) {
        vertex(buffer, pose, state.lightCoords, 0.0F, 0, 0, 1, color);
        vertex(buffer, pose, state.lightCoords, 1.0F, 0, 1, 1, color);
        vertex(buffer, pose, state.lightCoords, 1.0F, 1, 1, 0, color);
        vertex(buffer, pose, state.lightCoords, 0.0F, 1, 0, 0, color);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, int light, float x, int y, int u, int v, int color) {
        buffer.addVertex(pose, x - 0.5F, y - 0.25F, 0.0F)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
