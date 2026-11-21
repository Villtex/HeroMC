package tconstruct.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractCubeMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import tconstruct.TConstruct;
import tconstruct.entity.BlueSlime;

/** SlimeRender of Tinkers' Construct: the slime models of today with slimeedible.png, inside and the translucent outside. */
public class BlueSlimeRenderer extends AbstractCubeMobRenderer<BlueSlime, SlimeRenderState, SlimeModel> {
    static final Identifier TEXTURE = TConstruct.id("textures/entity/blue_slime.png");

    public BlueSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel(context.bakeLayer(ModelLayers.SLIME)));
        this.addLayer(new Outer(this, context));
    }

    @Override
    protected void scale(SlimeRenderState state, PoseStack poseStack) {
        this.downscaleSlightly(poseStack);
        super.scale(state, poseStack);
    }

    @Override
    public Identifier getTextureLocation(SlimeRenderState state) {
        return TEXTURE;
    }

    @Override
    public SlimeRenderState createRenderState() {
        return new SlimeRenderState();
    }

    static final class Outer extends RenderLayer<SlimeRenderState, SlimeModel> {
        private final SlimeModel model;

        Outer(RenderLayerParent<SlimeRenderState, SlimeModel> renderer, EntityRendererProvider.Context context) {
            super(renderer);
            this.model = new SlimeModel(context.getModelSet().bakeLayer(ModelLayers.SLIME_OUTER));
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, SlimeRenderState state, float yRot, float xRot) {
            boolean glowing = state.appearsGlowing() && state.isInvisible;
            if (!state.isInvisible || glowing) {
                int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
                collector.order(1).submitModel(this.model, state, poseStack, glowing ? RenderTypes.outline(TEXTURE) : RenderTypes.entityTranslucent(TEXTURE),
                        light, overlay, state.outlineColor);
            }
        }
    }
}
