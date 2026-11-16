package natura.client.entity;

import natura.Natura;
import natura.entity.ImpEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/** RenderLiving(new ImpModel(), 0.0F): no shadow. */
public class ImpRenderer extends MobRenderer<ImpEntity, LivingEntityRenderState, ImpModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Natura.MOD_ID, "textures/entity/imp.png");

    public ImpRenderer(EntityRendererProvider.Context context) {
        super(context, new ImpModel(context.bakeLayer(NaturaEntityRenderers.IMP_LAYER)), 0.0F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}
