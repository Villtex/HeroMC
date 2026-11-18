package betterdungeons.client;

import betterdungeons.BetterDungeons;
import betterdungeons.projectile.BDMagicArrow;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * RenderMagicArrow: the 1.5.2 arrow (two crossed 16 x 4 quads and the 4 x 4 back) with the cell of the element in
 * /bdimg/arrows.png (types 0..5: column type % 2, row type / 2 of 10 pixels) or arrows2.png (6 and up). Client check
 * open.
 */
public class MagicArrowRenderer extends EntityRenderer<BDMagicArrow, MagicArrowRenderer.State> {
    private static final Identifier ARROWS = Identifier.fromNamespaceAndPath(BetterDungeons.MOD_ID, "textures/entity/arrows.png");
    private static final Identifier ARROWS2 = Identifier.fromNamespaceAndPath(BetterDungeons.MOD_ID, "textures/entity/arrows2.png");

    public MagicArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public static class State extends EntityRenderState {
        float yRot;
        float xRot;
        float shake;
        int type;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BDMagicArrow entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.shake = entity.shakeTime - partialTicks;
        state.type = entity.getArrowType();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        int type = state.type;
        float offX = type % 2 == 1 ? 0.5F : 0.0F;
        float offY = (type >= 6 ? type - 6 : type) / 2 * 10.0F / 32.0F;
        poseStack.pushPose();
        poseStack.rotateDegrees(Axis.YP, state.yRot - 90.0F);
        poseStack.rotateDegrees(Axis.ZP, state.xRot);
        if (state.shake > 0.0F) {
            poseStack.rotateDegrees(Axis.ZP, -Mth.sin(state.shake * 3.0F) * state.shake);
        }
        poseStack.rotateDegrees(Axis.XP, 45.0F);
        float s = 0.05625F;
        poseStack.scale(s, s, s);
        poseStack.translate(-4.0F, 0.0F, 0.0F);
        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(type >= 6 ? ARROWS2 : ARROWS), (pose, buffer) -> {
            float u0 = offX;
            float u1 = 0.15625F + offX;
            float v0 = 5 / 32.0F + offY;
            float v1 = 10 / 32.0F + offY;
            vertex(buffer, pose, light, -7, -2, -2, u0, v0, 1, 0, 0);
            vertex(buffer, pose, light, -7, -2, 2, u1, v0, 1, 0, 0);
            vertex(buffer, pose, light, -7, 2, 2, u1, v1, 1, 0, 0);
            vertex(buffer, pose, light, -7, 2, -2, u0, v1, 1, 0, 0);
            vertex(buffer, pose, light, -7, 2, -2, u0, v0, -1, 0, 0);
            vertex(buffer, pose, light, -7, 2, 2, u1, v0, -1, 0, 0);
            vertex(buffer, pose, light, -7, -2, 2, u1, v1, -1, 0, 0);
            vertex(buffer, pose, light, -7, -2, -2, u0, v1, -1, 0, 0);
        });
        for (int i = 0; i < 4; i++) {
            poseStack.rotateDegrees(Axis.XP, 90.0F);
            collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(type >= 6 ? ARROWS2 : ARROWS), (pose, buffer) -> {
                float u0 = offX;
                float u1 = 0.5F + offX;
                float v0 = offY;
                float v1 = 5 / 32.0F + offY;
                vertex(buffer, pose, light, -8, -2, 0, u0, v0, 0, 0, 1);
                vertex(buffer, pose, light, 8, -2, 0, u1, v0, 0, 0, 1);
                vertex(buffer, pose, light, 8, 2, 0, u1, v1, 0, 0, 1);
                vertex(buffer, pose, light, -8, 2, 0, u0, v1, 0, 0, 1);
            });
        }
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, int light, float x, float y, float z, float u, float v, float nx, float ny,
            float nz) {
        buffer.addVertex(pose, x, y, z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
    }
}
