package twilightforest.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import twilightforest.entity.TFSpider;

/** RenderSpider for the Twilight spiders: their texture, the vanilla eyes, scaled by spiderScaleAmount. */
public class TFSpiderRenderer<T extends TFSpider> extends SpiderRenderer<T> {
    private final Identifier texture;
    private final float scale;

    public TFSpiderRenderer(EntityRendererProvider.Context context, String texture, float scale) {
        super(context);
        this.texture = TFMobRenderer.texture(texture);
        this.scale = scale;
    }

    @Override
    protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return this.texture;
    }
}
