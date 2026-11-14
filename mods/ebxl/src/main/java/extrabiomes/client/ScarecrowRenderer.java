package extrabiomes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import extrabiomes.Extrabiomes;
import extrabiomes.entity.ScarecrowEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/** RenderScarecrow: the model scaled by 1.25. */
public class ScarecrowRenderer extends MobRenderer<ScarecrowEntity, LivingEntityRenderState, ScarecrowModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Extrabiomes.MOD_ID, "textures/entity/scarecrow.png");

    public ScarecrowRenderer(EntityRendererProvider.Context context) {
        super(context, new ScarecrowModel(context.bakeLayer(EbxlClient.SCARECROW_LAYER)), 0.5F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
        poseStack.scale(1.25F, 1.25F, 1.25F);
    }
}
