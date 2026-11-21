package twilightforest.client.entity.hydra;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import twilightforest.client.entity.TFMobRenderer;
import twilightforest.entity.boss.hydra.Hydra;

/**
 * RenderTFHydra (RenderLiving with ModelTFHydra, shadow 1): the body, legs and tail; the heads and necks are the head
 * entities' own renderers. It does not tip over when dying (getDeathMaxRotation 0) and is drawn even when its box is
 * out of view (ignoreFrustumCheck). The boss bar is the server's {@code ServerBossEvent}.
 */
public class HydraRenderer extends MobRenderer<Hydra, LivingEntityRenderState, HydraModels.Body> {
    static final Identifier TEXTURE = TFMobRenderer.texture("hydra");

    public HydraRenderer(EntityRendererProvider.Context context) {
        super(context, new HydraModels.Body(context.bakeLayer(HydraRenderers.HYDRA)), 1.0F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    protected boolean affectedByCulling(Hydra entity) {
        return false;
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}
