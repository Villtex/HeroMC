package twilightforest.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;

/**
 * RenderTFMistWolf: the wolf model 1.9 times the size with mistwolf.png, blended: brighter and more solid in light
 * (colour min(1, brightness × 3 + 0.25), alpha brightness × 2 + 0.6).
 */
public class MistWolfRenderer extends WolfRenderer {
    private static final Identifier TEXTURE = TFMobRenderer.texture("mistwolf");

    public MistWolfRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void scale(WolfRenderState state, PoseStack poseStack) {
        poseStack.scale(1.9F, 1.9F, 1.9F);
    }

    @Override
    public Identifier getTextureLocation(WolfRenderState state) {
        return TEXTURE;
    }

    @Override
    protected RenderType getRenderType(WolfRenderState state, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderTypes.entityTranslucent(TEXTURE);
    }

    @Override
    protected int getModelTint(WolfRenderState state) {
        int light = Math.max(LightCoordsUtil.block(state.lightCoords), LightCoordsUtil.sky(state.lightCoords));
        float f = 1.0F - light / 15.0F;
        float brightness = (1.0F - f) / (f * 3.0F + 1.0F);
        float misty = Math.min(1.0F, brightness * 3.0F + 0.25F);
        float smoky = Math.min(1.0F, brightness * 2.0F + 0.6F);
        return ARGB.colorFromFloat(smoky, misty, misty, misty);
    }
}
