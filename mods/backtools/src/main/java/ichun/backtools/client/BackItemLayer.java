package ichun.backtools.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BT_RenderBackItem: the item as a flat 1x1 block icon behind the body, 1.12 blocks above the feet and 0.154 behind
 * the body centre (0.053 more over a chestplate), turned in quarter turns and optionally flipped. Following the
 * body part keeps it on the back when sneaking, like the 28.8 degree tilt of the original.
 */
public class BackItemLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackItemLayer.class);
    /** Player model scale: positions of the original are in blocks, the layer works in model units. */
    private static final float SCALE = 0.9375F;
    private static boolean failed;

    public BackItemLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
        BackToolsClient.BackItem back = state.getRenderData(BackToolsClient.BACK_ITEM);
        if (back == null || back.item().isEmpty()) {
            return;
        }
        try {
            poseStack.pushPose();
            this.getParentModel().body.translateAndRotate(poseStack);
            // model space: y down, +z towards the back; neck at 0, feet 24 px below
            float behind = (0.154F + (back.chestArmor() ? 0.053F : 0.0F)) / SCALE;
            poseStack.translate(0.0F, (24.0F - 1.12F * 16.0F / SCALE) / 16.0F, behind);
            // back to the axes of the original: y up, back towards -z
            poseStack.rotateDegrees(Axis.XP, 180.0F);
            poseStack.scale(1.0F / SCALE, 1.0F / SCALE, 1.0F / SCALE);
            poseStack.rotateDegrees(Axis.YP, back.flipped() ? 180.0F : 0.0F);
            poseStack.rotateDegrees(Axis.ZP, back.orientation() * 90.0F);
            // the item model faces +z; seen from behind the player its front must face -z
            poseStack.rotateDegrees(Axis.YP, 180.0F);
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            back.item().submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        } catch (RuntimeException e) {
            poseStack.popPose();
            if (!failed) {
                failed = true;
                LOGGER.error("Back Tools could not draw a back item", e);
            }
        }
    }
}
