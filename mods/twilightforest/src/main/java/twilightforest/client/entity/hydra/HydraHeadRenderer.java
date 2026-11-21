package twilightforest.client.entity.hydra;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;
import twilightforest.entity.boss.hydra.Hydra;
import twilightforest.entity.boss.hydra.HydraHead;
import twilightforest.entity.boss.hydra.HydraHeadContainer;
import twilightforest.entity.boss.hydra.HydraPart;

/**
 * RenderTFHydraHead (RenderLiving with ModelTFHydraHead, shadow 1): the head until it has been dying for 20 ticks,
 * tipping over sideways meanwhile (90 degrees, as RenderLiving.rotateCorpse), then each of its five neck segments
 * (RenderLiving with ModelTFHydraNeck) until the explosions reach it. The head entity's body yaw stays 0, so the
 * models carry the world yaw themselves. Red while hurt. Drawn even when its box is out of view.
 */
public class HydraHeadRenderer extends EntityRenderer<HydraHead, HydraModels.HeadState> {
    private final HydraModels.Head head;
    private final HydraModels.Neck neck;

    public HydraHeadRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.head = new HydraModels.Head(context.bakeLayer(HydraRenderers.HYDRA_HEAD));
        this.neck = new HydraModels.Neck(context.bakeLayer(HydraRenderers.HYDRA_NECK));
        this.shadowRadius = 1.0F;
    }

    @Override
    public HydraModels.HeadState createRenderState() {
        return new HydraModels.HeadState();
    }

    @Override
    protected boolean affectedByCulling(HydraHead entity) {
        return false;
    }

    @Override
    public void extractRenderState(HydraHead entity, HydraModels.HeadState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        HydraHeadContainer headCon = getHeadObject(entity);
        state.visible = headCon == null || headCon.shouldRenderHead();
        state.yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        state.pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        state.mouthOpen = entity.getMouthOpen();
        state.red = entity.hurtTime > 0 || entity.dyingTime > 0;
        state.deathTime = entity.dyingTime > 0 ? entity.dyingTime + partialTick : 0.0F;
        HydraPart[] necks = headCon != null ? headCon.getNeckArray() : null;
        for (int i = 0; i < state.necks.length; i++) {
            HydraModels.PartState ns = state.necks[i];
            ns.visible = necks != null && headCon.shouldRenderNeck(i);
            if (!ns.visible) {
                continue;
            }
            HydraPart part = necks[i];
            ns.dx = Mth.lerp(partialTick, part.xOld, part.getX()) - state.x;
            ns.dy = Mth.lerp(partialTick, part.yOld, part.getY()) - state.y;
            ns.dz = Mth.lerp(partialTick, part.zOld, part.getZ()) - state.z;
            ns.yaw = Mth.rotLerp(partialTick, part.yRotO, part.getYRot());
            ns.pitch = Mth.lerp(partialTick, part.xRotO, part.getXRot());
            ns.red = part.hurtTime > 0;
            ns.lightCoords = state.lightCoords;
            ns.outlineColor = state.outlineColor;
        }
    }

    private static @Nullable HydraHeadContainer getHeadObject(HydraHead entity) {
        Hydra hydra = entity.hydraObj;
        if (hydra != null) {
            for (int i = 0; i < hydra.numHeads; i++) {
                if (hydra.hc[i].headEntity == entity) {
                    return hydra.hc[i];
                }
            }
        }
        return null;
    }

    @Override
    public void submit(HydraModels.HeadState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.visible) {
            poseStack.pushPose();
            poseStack.rotateDegrees(Axis.YP, 180.0F);
            if (state.deathTime > 0.0F) {
                float fall = Mth.sqrt((state.deathTime - 1.0F) / 20.0F * 1.6F);
                poseStack.rotateDegrees(Axis.ZP, Math.min(fall, 1.0F) * 90.0F);
            }
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            poseStack.translate(0.0F, -1.501F, 0.0F);
            collector.submitModel(this.head, state, poseStack, HydraRenderer.TEXTURE, state.lightCoords,
                    OverlayTexture.pack(OverlayTexture.u(0.0F), OverlayTexture.v(state.red)), state.outlineColor);
            poseStack.popPose();
        }
        for (HydraModels.PartState ns : state.necks) {
            if (!ns.visible) {
                continue;
            }
            poseStack.pushPose();
            poseStack.translate(ns.dx, ns.dy, ns.dz);
            poseStack.rotateDegrees(Axis.YP, 180.0F);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            poseStack.translate(0.0F, -1.501F, 0.0F);
            collector.submitModel(this.neck, ns, poseStack, HydraRenderer.TEXTURE, ns.lightCoords,
                    OverlayTexture.pack(OverlayTexture.u(0.0F), OverlayTexture.v(ns.red)), ns.outlineColor);
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }
}
