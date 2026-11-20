package xreliquary.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import xreliquary.Reliquary;
import xreliquary.entity.ShotEntity;
import xreliquary.entity.ThrownEntity;

/**
 * RenderXxxShot (the shot texture, scale 0.1) and RenderThrown (a 16 x 16 cell of thrownItemsSheet.png, scale 0.5):
 * a camera-facing quad from (-0.5, -0.25) to (0.5, 0.75). The ice rod's snowball is drawn by the vanilla renderer.
 * Client check open.
 */
public class BillboardRenderer<E extends Entity> extends EntityRenderer<E, BillboardRenderer.State> {
    static final Identifier SHEET = Reliquary.id("textures/entity/thrown_items_sheet.png");

    public BillboardRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public static class State extends EntityRenderState {
        Identifier texture = SHEET;
        float scale = 0.5F;
        float u0;
        float u1 = 1.0F;
        float v0;
        float v1 = 1.0F;
        boolean visible = true;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(E entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (entity instanceof ShotEntity shot) {
            state.texture = Reliquary.id("textures/entity/" + shot.type().id + "_shot.png");
            state.scale = 0.1F;
            state.u0 = 0.0F;
            state.u1 = 1.0F;
            state.v0 = 0.0F;
            state.v1 = 1.0F;
            state.visible = shot.type() != xreliquary.item.ShotType.EMPTY;
        } else if (entity instanceof ThrownEntity thrown) {
            int index = thrown.kind().sprite;
            state.texture = SHEET;
            state.scale = 0.5F;
            if (index < 0) {
                state.texture = Identifier.withDefaultNamespace("textures/item/snowball.png");
                state.u0 = 0.0F;
                state.u1 = 1.0F;
                state.v0 = 0.0F;
                state.v1 = 1.0F;
                return;
            }
            state.u0 = (index % 16 * 16) / 256.0F;
            state.u1 = (index % 16 * 16 + 16) / 256.0F;
            state.v0 = (index / 16 * 16) / 256.0F;
            state.v1 = (index / 16 * 16 + 16) / 256.0F;
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.visible) {
            poseStack.pushPose();
            poseStack.scale(state.scale, state.scale, state.scale);
            poseStack.rotate(camera.orientation);
            collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(state.texture), (pose, buffer) -> {
                vertex(buffer, pose, state.lightCoords, -0.5F, -0.25F, state.u0, state.v1);
                vertex(buffer, pose, state.lightCoords, 0.5F, -0.25F, state.u1, state.v1);
                vertex(buffer, pose, state.lightCoords, 0.5F, 0.75F, state.u1, state.v0);
                vertex(buffer, pose, state.lightCoords, -0.5F, 0.75F, state.u0, state.v0);
            });
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, int light, float x, float y, float u, float v) {
        buffer.addVertex(pose, x, y, 0.0F).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
