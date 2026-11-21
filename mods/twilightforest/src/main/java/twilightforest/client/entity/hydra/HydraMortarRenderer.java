package twilightforest.client.entity.hydra;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import twilightforest.client.entity.TFMobRenderer;
import twilightforest.entity.boss.hydra.HydraMortar;

/**
 * RenderTFHydraMortar (shadow 0.5): the 8³ box at render scale 0.075 (0.6 blocks), swelling by up to 30 % in the
 * last 10 ticks of the fuse and flashing white every other 5 ticks (1.5.2 drew it a second time untextured with
 * additive blending and alpha (1 - fuse / 100) × 0.8; here the white overlay of the TNT does that). It burns
 * (isBurning), so the fire overlay is drawn around it.
 */
public class HydraMortarRenderer extends EntityRenderer<HydraMortar, HydraModels.MortarState> {
    private static final Identifier TEXTURE = TFMobRenderer.texture("hydra_mortar");
    private final HydraModels.Mortar model;

    public HydraMortarRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new HydraModels.Mortar(context.bakeLayer(HydraRenderers.HYDRA_MORTAR));
        this.shadowRadius = 0.5F;
    }

    @Override
    public HydraModels.MortarState createRenderState() {
        return new HydraModels.MortarState();
    }

    @Override
    public void extractRenderState(HydraMortar entity, HydraModels.MortarState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.fuse = entity.fuse;
        state.fuseTime = entity.fuse - partialTick + 1.0F;
    }

    @Override
    public void submit(HydraModels.MortarState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        if (state.fuseTime < 10.0F) {
            float swell = 1.0F - state.fuseTime / 10.0F;
            swell = Math.max(0.0F, Math.min(1.0F, swell));
            swell *= swell;
            swell *= swell;
            float scale = 1.0F + swell * 0.3F;
            poseStack.scale(scale, scale, scale);
        }
        float alpha = (1.0F - state.fuseTime / 100.0F) * 0.8F;
        poseStack.scale(0.075F / 0.0625F, 0.075F / 0.0625F, 0.075F / 0.0625F);
        int overlay = state.fuse / 5 % 2 == 0 ? OverlayTexture.pack(OverlayTexture.u(Math.max(0.0F, Math.min(1.0F, alpha))), OverlayTexture.WHITE_OVERLAY_V)
                : OverlayTexture.NO_OVERLAY;
        collector.submitModel(this.model, state, poseStack, TEXTURE, state.lightCoords, overlay, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
